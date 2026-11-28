package com.chatsystem.desktop.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.auth.AuthenticatedUserSummary;
import com.chatsystem.common.dto.auth.SessionInfo;
import com.chatsystem.common.ws.WsCloseCode;
import com.chatsystem.common.ws.WsEventType;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import org.junit.jupiter.api.Test;

class WsClientTest {
    @Test
    void serializesConcurrentSendCommandCallsWithoutSendPendingError() throws Exception {
        CompletableFuture<WebSocket> firstSendGate = new CompletableFuture<>();
        CompletableFuture<WebSocket> secondSendDone = new CompletableFuture<>();
        AtomicBoolean sendInFlight = new AtomicBoolean(false);
        List<String> sentFrames = new CopyOnWriteArrayList<>();

        WebSocket stubSocket = new StubWebSocket() {
            private int count = 0;

            @Override
            public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) {
                if (!sendInFlight.compareAndSet(false, true)) {
                    throw new IllegalStateException("Send pending");
                }
                sentFrames.add(data.toString());
                count++;
                if (count == 1) {
                    return firstSendGate.whenComplete((ws, err) -> sendInFlight.set(false));
                }
                return secondSendDone.whenComplete((ws, err) -> sendInFlight.set(false));
            }
        };

        StubHttpClient httpClient = new StubHttpClient(() -> CompletableFuture.completedFuture(stubSocket));
        try (WsClient wsClient = new WsClient(ClientConfig.defaults(), authenticatedStore(), httpClient)) {
            wsClient.connect().get(2, TimeUnit.SECONDS);

            CompletableFuture<Void> first = wsClient.sendCommand(WsEventType.SYSTEM_PING, Map.of("n", 1));
            CompletableFuture<Void> second = wsClient.sendCommand(WsEventType.SYSTEM_PING, Map.of("n", 2));

            // Allow first sendText to complete, then second should begin cleanly
            firstSendGate.complete(stubSocket);
            secondSendDone.complete(stubSocket);

            first.get(2, TimeUnit.SECONDS);
            second.get(2, TimeUnit.SECONDS);
            assertEquals(2, sentFrames.size());
        }
    }

    @Test
    void closesLateCompletedHandshakeSocketAndHandlesNonReconnectableCloseSafely() throws Exception {
        CompletableFuture<WebSocket> handshakeFuture = new CompletableFuture<>();
        CompletableFuture<Integer> lateCloseCode = new CompletableFuture<>();
        StubWebSocket lateSocket = new StubWebSocket() {
            @Override
            public CompletableFuture<WebSocket> sendClose(int statusCode, String reason) {
                lateCloseCode.complete(statusCode);
                return CompletableFuture.completedFuture(this);
            }
        };
        StubHttpClient httpClient = new StubHttpClient(() -> handshakeFuture);

        WsClient wsClient = new WsClient(ClientConfig.defaults(), authenticatedStore(), httpClient);
        CompletableFuture<Void> connectFuture = wsClient.connect();
        wsClient.close();
        handshakeFuture.complete(lateSocket);

        connectFuture.get(2, TimeUnit.SECONDS);
        assertEquals(WsCloseCode.NORMAL_CLOSURE.code(), lateCloseCode.get(2, TimeUnit.SECONDS));

        // Now verify non-reconnectable close notifies listeners and never schedules onto shutdown executor
        StubHttpClient secondClient = new StubHttpClient(() -> CompletableFuture.completedFuture(new StubWebSocket()));
        WsClient reconnectClient = new WsClient(ClientConfig.defaults(), authenticatedStore(), secondClient);
        List<WsCloseCode> nonReconnectable = new ArrayList<>();
        reconnectClient.addNonReconnectableCloseListener(nonReconnectable::add);
        reconnectClient.connect().get(2, TimeUnit.SECONDS);

        assertDoesNotThrow(() -> secondClient.lastListener.onClose(
                new StubWebSocket(),
                WsCloseCode.ADMIN_SESSION_REPLACED.code(),
                "Replaced"
        ));
        assertEquals(List.of(WsCloseCode.ADMIN_SESSION_REPLACED), nonReconnectable);
        assertEquals(1, secondClient.buildCount);
        reconnectClient.close();

        assertDoesNotThrow(() -> secondClient.lastListener.onClose(
                new StubWebSocket(),
                WsCloseCode.HEARTBEAT_TIMEOUT.code(),
                "Late close after shutdown"
        ));
        assertTrue(secondClient.buildCount == 1);
    }

    private static ClientSessionStore authenticatedStore() {
        ClientSessionStore store = new ClientSessionStore();
        store.updateSession(
                "opaque-token",
                new SessionInfo(
                        UUID.randomUUID(),
                        SessionScope.USER_CLIENT,
                        Instant.parse("2026-11-28T01:00:00Z"),
                        new AuthenticatedUserSummary(
                                UUID.randomUUID(), "alice", "Alice", "alice@example.test",
                                UserRole.USER, AccountStatus.ACTIVE, true
                        )
                )
        );
        return store;
    }

    @FunctionalInterface
    private interface SocketSupplier {
        CompletableFuture<WebSocket> get();
    }

    private static final class StubHttpClient extends HttpClient {
        private final SocketSupplier supplier;
        private volatile WebSocket.Listener lastListener;
        private volatile int buildCount;

        private StubHttpClient(SocketSupplier supplier) {
            this.supplier = supplier;
        }

        @Override
        public WebSocket.Builder newWebSocketBuilder() {
            return new WebSocket.Builder() {
                @Override
                public WebSocket.Builder header(String name, String value) {
                    return this;
                }

                @Override
                public WebSocket.Builder connectTimeout(Duration timeout) {
                    return this;
                }

                @Override
                public WebSocket.Builder subprotocols(String mostPreferred, String... lesserPreferred) {
                    return this;
                }

                @Override
                public CompletableFuture<WebSocket> buildAsync(URI uri, WebSocket.Listener listener) {
                    buildCount++;
                    lastListener = listener;
                    return supplier.get();
                }
            };
        }

        @Override public Optional<CookieHandler> cookieHandler() { return Optional.empty(); }
        @Override public Optional<Duration> connectTimeout() { return Optional.empty(); }
        @Override public Redirect followRedirects() { return Redirect.NEVER; }
        @Override public Optional<ProxySelector> proxy() { return Optional.empty(); }
        @Override public SSLContext sslContext() { return null; }
        @Override public SSLParameters sslParameters() { return null; }
        @Override public Optional<Authenticator> authenticator() { return Optional.empty(); }
        @Override public Version version() { return Version.HTTP_1_1; }
        @Override public Optional<Executor> executor() { return Optional.empty(); }

        @Override
        public <T> HttpResponse<T> send(HttpRequest req, HttpResponse.BodyHandler<T> handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest req, HttpResponse.BodyHandler<T> handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest req,
                HttpResponse.BodyHandler<T> handler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler
        ) {
            throw new UnsupportedOperationException();
        }
    }

    private static class StubWebSocket implements WebSocket {
        @Override
        public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public CompletableFuture<WebSocket> sendBinary(ByteBuffer data, boolean last) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public CompletableFuture<WebSocket> sendPing(ByteBuffer message) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public CompletableFuture<WebSocket> sendPong(ByteBuffer message) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public CompletableFuture<WebSocket> sendClose(int statusCode, String reason) {
            return CompletableFuture.completedFuture(this);
        }

        @Override public void request(long n) { }
        @Override public String getSubprotocol() { return ""; }
        @Override public boolean isOutputClosed() { return false; }
        @Override public boolean isInputClosed() { return false; }
        @Override public void abort() { }
    }
}
