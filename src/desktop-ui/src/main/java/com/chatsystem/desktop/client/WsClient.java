package com.chatsystem.desktop.client;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.ws.WsCloseCode;
import com.chatsystem.common.ws.WsCommand;
import com.chatsystem.common.ws.WsConnectedPayload;
import com.chatsystem.common.ws.WsEvent;
import com.chatsystem.common.ws.WsEventType;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Shared Raw-JSON WebSocket client built on JDK 25 {@link WebSocket} with serialized frame sends,
 * header authentication, heartbeat, and exponential-backoff reconnect honoring {@link WsCloseCode}.
 */
public final class WsClient implements AutoCloseable {
    private static final System.Logger LOG = System.getLogger(WsClient.class.getName());
    private static final TypeReference<WsEvent<JsonNode>> EVENT_TYPE = new TypeReference<>() {
    };
    private static final int[] BACKOFF_SECONDS = {1, 2, 4, 8, 16, 30};

    private final ClientConfig config;
    private final ClientSessionStore sessionStore;
    private final HttpClient httpClient;
    private final JsonMapper jsonMapper;
    private final ScheduledExecutorService scheduler;
    private final boolean ownsScheduler;
    private final List<Consumer<WsEvent<JsonNode>>> eventListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<WsCloseCode>> nonReconnectableCloseListeners = new CopyOnWriteArrayList<>();

    private final AtomicReference<WebSocket> activeSocket = new AtomicReference<>();
    private final AtomicBoolean closing = new AtomicBoolean(false);
    private final AtomicInteger reconnectAttempt = new AtomicInteger(0);
    private final AtomicReference<ScheduledFuture<?>> pingTask = new AtomicReference<>();
    private final AtomicReference<ScheduledFuture<?>> reconnectTask = new AtomicReference<>();
    private final Object sendLock = new Object();
    private CompletableFuture<Void> sendTail = CompletableFuture.completedFuture(null);

    public WsClient(ClientConfig config, ClientSessionStore sessionStore, HttpClient httpClient) {
        this(
                config,
                sessionStore,
                httpClient,
                Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "chatsystem-ws-client");
                    t.setDaemon(true);
                    return t;
                }),
                true
        );
    }

    WsClient(
            ClientConfig config,
            ClientSessionStore sessionStore,
            HttpClient httpClient,
            ScheduledExecutorService scheduler,
            boolean ownsScheduler
    ) {
        this.config = Objects.requireNonNull(config, "config");
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.jsonMapper = JsonMapper.builder().build();
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.ownsScheduler = ownsScheduler;
    }

    public void addEventListener(Consumer<WsEvent<JsonNode>> listener) {
        eventListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void addNonReconnectableCloseListener(Consumer<WsCloseCode> listener) {
        nonReconnectableCloseListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public CompletableFuture<Void> connect() {
        String token = sessionStore.sessionToken()
                .orElseThrow(() -> new IllegalStateException("Cannot connect WebSocket without session token"));
        closing.set(false);
        WebSocket.Builder builder = httpClient.newWebSocketBuilder()
                .connectTimeout(config.connectTimeout())
                .header(ApiHeaders.AUTHORIZATION, ApiHeaders.BEARER_PREFIX + token);
        sessionStore.deviceId()
                .ifPresent(deviceId -> builder.header(ApiHeaders.CLIENT_DEVICE_ID, deviceId.toString()));

        return builder.buildAsync(config.wsUri(), new SocketListener())
                .thenAccept(ws -> {
                    if (closing.get()) {
                        ws.sendClose(WsCloseCode.NORMAL_CLOSURE.code(), "Client closed");
                        return;
                    }
                    WebSocket previous = activeSocket.getAndSet(ws);
                    if (previous != null && previous != ws) {
                        previous.sendClose(WsCloseCode.NORMAL_CLOSURE.code(), "Replaced by new connection");
                    }
                    reconnectAttempt.set(0);
                });
    }

    public CompletableFuture<Void> sendCommand(WsEventType commandType, Object payload) {
        Objects.requireNonNull(commandType, "commandType");
        Objects.requireNonNull(payload, "payload");
        if (closing.get() || activeSocket.get() == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("WebSocket is not connected"));
        }
        WsCommand<Object> command = WsCommand.of(commandType, UUID.randomUUID(), Instant.now(), payload);
        String json;
        try {
            json = jsonMapper.writeValueAsString(command);
        } catch (JacksonException ex) {
            return CompletableFuture.failedFuture(ex);
        }
        synchronized (sendLock) {
            CompletableFuture<Void> next = sendTail
                    .handle((ignored, err) -> null)
                    .thenCompose(ignored -> sendSingleFrame(json));
            sendTail = next;
            return next;
        }
    }

    @Override
    public void close() {
        closing.set(true);
        cancelScheduledTasks();
        WebSocket ws = activeSocket.getAndSet(null);
        if (ws != null) {
            ws.sendClose(WsCloseCode.NORMAL_CLOSURE.code(), "Client closed");
        }
        if (ownsScheduler) {
            scheduler.shutdownNow();
        }
    }

    private CompletableFuture<Void> sendSingleFrame(String json) {
        WebSocket current = activeSocket.get();
        if (closing.get() || current == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("WebSocket is not connected"));
        }
        return current.sendText(json, true).thenApply(ignored -> null);
    }

    private void startHeartbeat(int intervalSec) {
        cancelTask(pingTask);
        if (closing.get() || scheduler.isShutdown()) {
            return;
        }
        long interval = Math.max(5L, intervalSec);
        try {
            ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                    () -> sendCommand(WsEventType.SYSTEM_PING, Map.of()),
                    interval,
                    interval,
                    TimeUnit.SECONDS
            );
            pingTask.set(future);
        } catch (RejectedExecutionException ignored) {
            // Scheduler closed concurrently.
        }
    }

    private void handleClosed(int statusCode) {
        activeSocket.set(null);
        cancelTask(pingTask);
        if (closing.get()) {
            return;
        }
        boolean reconnectable = true;
        try {
            WsCloseCode closeCode = WsCloseCode.fromCode(statusCode);
            reconnectable = closeCode.reconnectable();
            if (!reconnectable) {
                nonReconnectableCloseListeners.forEach(listener -> listener.accept(closeCode));
            }
        } catch (IllegalArgumentException ignored) {
            reconnectable = statusCode != 1000;
        }
        if (reconnectable && sessionStore.sessionToken().isPresent()) {
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        if (closing.get() || scheduler.isShutdown()) {
            return;
        }
        int index = Math.min(reconnectAttempt.getAndIncrement(), BACKOFF_SECONDS.length - 1);
        long baseMillis = BACKOFF_SECONDS[index] * 1000L;
        long jitterMillis = ThreadLocalRandom.current().nextLong(0L, 500L);
        try {
            ScheduledFuture<?> next = scheduler.schedule(() -> {
                if (!closing.get() && sessionStore.sessionToken().isPresent()) {
                    connect().exceptionally(ex -> {
                        scheduleReconnect();
                        return null;
                    });
                }
            }, baseMillis + jitterMillis, TimeUnit.MILLISECONDS);
            ScheduledFuture<?> previous = reconnectTask.getAndSet(next);
            if (previous != null) {
                previous.cancel(false);
            }
        } catch (RejectedExecutionException ignored) {
            // Client closed concurrently.
        }
    }

    private void dispatchTextFrame(String text) {
        try {
            WsEvent<JsonNode> event = jsonMapper.readValue(text, EVENT_TYPE);
            if (event.eventType() == WsEventType.SYSTEM_CONNECTED) {
                WsConnectedPayload connected = jsonMapper.treeToValue(event.payload(), WsConnectedPayload.class);
                startHeartbeat(connected.heartbeatIntervalSec());
            }
            eventListeners.forEach(listener -> listener.accept(event));
        } catch (JacksonException | IllegalArgumentException ex) {
            LOG.log(System.Logger.Level.WARNING, "Failed parsing incoming WS event: {0}", ex.getMessage());
        }
    }

    private void cancelScheduledTasks() {
        cancelTask(pingTask);
        cancelTask(reconnectTask);
    }

    private static void cancelTask(AtomicReference<ScheduledFuture<?>> ref) {
        ScheduledFuture<?> task = ref.getAndSet(null);
        if (task != null) {
            task.cancel(false);
        }
    }

    private final class SocketListener implements WebSocket.Listener {
        private final StringBuilder frameBuffer = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            frameBuffer.append(data);
            if (last) {
                String completeFrame = frameBuffer.toString();
                frameBuffer.setLength(0);
                dispatchTextFrame(completeFrame);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            handleClosed(statusCode);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            handleClosed(WsCloseCode.RESYNC_REQUIRED.code());
        }
    }
}
