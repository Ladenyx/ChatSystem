package com.chatsystem.server.ws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.PresenceStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.ws.WsEvent;
import com.chatsystem.common.ws.WsEventType;
import com.chatsystem.common.ws.WsPresenceChangedPayload;
import com.chatsystem.common.ws.WsPresenceSnapshotPayload;
import com.chatsystem.server.auth.AuthenticatedSession;
import com.chatsystem.server.auth.SessionAuthenticator;
import com.chatsystem.server.config.ChatServerProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.json.JsonMapper;

class PresenceServiceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-28T06:00:00Z"), ZoneOffset.UTC);
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @AfterEach
    void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    void broadcastsOnlineAndOfflineOnlyToEligibleOnlineUserClientsAndIgnoresAdminApp() throws Exception {
        UUID userA = UUID.randomUUID();
        UUID friendB = UUID.randomUUID();
        UUID adminUser = UUID.randomUUID();

        PresenceRepository presenceRepo = mock(PresenceRepository.class);
        when(presenceRepo.findEligibleContactIds(userA)).thenReturn(List.of(friendB));
        when(presenceRepo.findEligibleContactIds(friendB)).thenReturn(List.of(userA));

        SessionAuthenticator authenticator = mock(SessionAuthenticator.class);
        when(authenticator.isSessionStillActive(any())).thenReturn(true);

        WsSessionRegistry registry = new WsSessionRegistry(jsonMapper, clock);
        UserEventLog eventLog = new UserEventLog(
                new ChatServerProperties(new ChatServerProperties.WsProperties(25, 60, 0, 50, List.of("*")), null, null),
                clock
        );
        WsEventDispatcher dispatcher = new WsEventDispatcher(registry, authenticator, eventLog, jsonMapper, clock);
        PresenceService presenceService = new PresenceService(presenceRepo, registry, dispatcher, 0, clock, scheduler, false);

        WebSocketSession wsFriend = openSocket("ws-b");
        WsConnection connFriend = registry.register(wsFriend, session(friendB, SessionScope.USER_CLIENT, UserRole.USER));
        presenceService.onConnected(connFriend);

        WebSocketSession wsAdmin = openSocket("ws-admin");
        WsConnection connAdmin = registry.register(wsAdmin, session(adminUser, SessionScope.ADMIN_APP, UserRole.ADMIN));
        presenceService.onConnected(connAdmin);
        assertThat(presenceService.isOnline(adminUser)).isFalse();

        WebSocketSession wsUserA = openSocket("ws-a");
        WsConnection connUserA = registry.register(wsUserA, session(userA, SessionScope.USER_CLIENT, UserRole.USER));
        presenceService.onConnected(connUserA);

        assertThat(presenceService.isOnline(userA)).isTrue();

        ArgumentCaptor<org.springframework.web.socket.TextMessage> userAMessages =
                ArgumentCaptor.forClass(org.springframework.web.socket.TextMessage.class);
        verify(wsUserA).sendMessage(userAMessages.capture());
        WsEvent<?> snapshotEvent = jsonMapper.readValue(userAMessages.getValue().getPayload(), WsEvent.class);
        assertThat(snapshotEvent.eventType()).isEqualTo(WsEventType.PRESENCE_SNAPSHOT);
        WsPresenceSnapshotPayload snapshotPayload = jsonMapper.convertValue(snapshotEvent.payload(), WsPresenceSnapshotPayload.class);
        assertThat(snapshotPayload.onlineUserIds()).containsExactly(friendB);

        registry.unregister("ws-a");
        presenceService.onDisconnected(connUserA);
        assertThat(presenceService.isOnline(userA)).isFalse();

        ArgumentCaptor<org.springframework.web.socket.TextMessage> friendMessages =
                ArgumentCaptor.forClass(org.springframework.web.socket.TextMessage.class);
        verify(wsFriend, org.mockito.Mockito.atLeast(3)).sendMessage(friendMessages.capture());
        List<PresenceStatus> statuses = friendMessages.getAllValues().stream()
                .map(msg -> jsonMapper.readValue(msg.getPayload(), WsEvent.class))
                .filter(ev -> ev.eventType() == WsEventType.PRESENCE_CHANGED)
                .map(ev -> jsonMapper.convertValue(ev.payload(), WsPresenceChangedPayload.class))
                .filter(payload -> payload.userId().equals(userA))
                .map(WsPresenceChangedPayload::status)
                .toList();
        assertThat(statuses).containsExactly(PresenceStatus.ONLINE, PresenceStatus.OFFLINE);
    }

    @Test
    void cancelsPendingOfflineGraceWhenUserReconnectsAndBroadcastsOfflineWhenGraceExpires() throws Exception {
        UUID userA = UUID.randomUUID();
        UUID friendB = UUID.randomUUID();

        PresenceRepository presenceRepo = mock(PresenceRepository.class);
        when(presenceRepo.findEligibleContactIds(userA)).thenReturn(List.of(friendB));
        when(presenceRepo.findEligibleContactIds(friendB)).thenReturn(List.of(userA));

        SessionAuthenticator authenticator = mock(SessionAuthenticator.class);
        when(authenticator.isSessionStillActive(any())).thenReturn(true);

        WsSessionRegistry registry = new WsSessionRegistry(jsonMapper, clock);
        UserEventLog eventLog = new UserEventLog(
                new ChatServerProperties(new ChatServerProperties.WsProperties(25, 60, 10, 50, List.of("*")), null, null),
                clock
        );
        WsEventDispatcher dispatcher = new WsEventDispatcher(registry, authenticator, eventLog, jsonMapper, clock);
        CapturingScheduler capturingScheduler = new CapturingScheduler();
        PresenceService presenceService = new PresenceService(
                presenceRepo,
                registry,
                dispatcher,
                10,
                clock,
                capturingScheduler,
                false
        );

        WebSocketSession wsFriend = openSocket("ws-friend");
        presenceService.onConnected(registry.register(wsFriend, session(friendB, SessionScope.USER_CLIENT, UserRole.USER)));

        WsConnection firstConn = registry.register(openSocket("ws-a1"), session(userA, SessionScope.USER_CLIENT, UserRole.USER));
        presenceService.onConnected(firstConn);
        assertThat(presenceService.isOnline(userA)).isTrue();

        // Disconnect inside grace window: still online, offline task scheduled
        registry.unregister("ws-a1");
        presenceService.onDisconnected(firstConn);
        assertThat(presenceService.isOnline(userA)).isTrue();
        assertThat(capturingScheduler.tasks).hasSize(1);
        CapturingScheduledFuture firstTask = capturingScheduler.tasks.getFirst();

        // Reconnect before grace expires; even if stale task runs, user stays online with no OFFLINE event
        WsConnection secondConn = registry.register(openSocket("ws-a2"), session(userA, SessionScope.USER_CLIENT, UserRole.USER));
        presenceService.onConnected(secondConn);
        assertThat(firstTask.cancelled.get()).isTrue();
        firstTask.task.run();
        assertThat(presenceService.isOnline(userA)).isTrue();

        // Disconnect again and let grace window expire
        registry.unregister("ws-a2");
        presenceService.onDisconnected(secondConn);
        assertThat(capturingScheduler.tasks).hasSize(2);
        capturingScheduler.tasks.getLast().task.run();
        assertThat(presenceService.isOnline(userA)).isFalse();

        ArgumentCaptor<org.springframework.web.socket.TextMessage> friendMessages =
                ArgumentCaptor.forClass(org.springframework.web.socket.TextMessage.class);
        verify(wsFriend, org.mockito.Mockito.atLeast(2)).sendMessage(friendMessages.capture());
        List<PresenceStatus> statuses = friendMessages.getAllValues().stream()
                .map(msg -> jsonMapper.readValue(msg.getPayload(), WsEvent.class))
                .filter(ev -> ev.eventType() == WsEventType.PRESENCE_CHANGED)
                .map(ev -> jsonMapper.convertValue(ev.payload(), WsPresenceChangedPayload.class))
                .filter(payload -> payload.userId().equals(userA))
                .map(WsPresenceChangedPayload::status)
                .toList();
        assertThat(statuses).containsExactly(PresenceStatus.ONLINE, PresenceStatus.OFFLINE);
    }

    private static final class CapturingScheduler extends java.util.concurrent.AbstractExecutorService
            implements ScheduledExecutorService {
        private final List<CapturingScheduledFuture> tasks = new ArrayList<>();

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            CapturingScheduledFuture future = new CapturingScheduledFuture(command);
            tasks.add(future);
            return future;
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void shutdown() {
        }

        @Override
        public List<Runnable> shutdownNow() {
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    private static final class CapturingScheduledFuture implements ScheduledFuture<Object> {
        private final Runnable task;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        private CapturingScheduledFuture(Runnable task) {
            this.task = task;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed other) {
            return 0;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled.set(true);
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Object get() {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            return null;
        }
    }

    private static WebSocketSession openSocket(String id) {
        WebSocketSession ws = mock(WebSocketSession.class);
        when(ws.getId()).thenReturn(id);
        when(ws.isOpen()).thenReturn(true);
        return ws;
    }

    private static AuthenticatedSession session(UUID userId, SessionScope scope, UserRole role) {
        return new AuthenticatedSession(
                UUID.randomUUID(),
                userId,
                "user_" + userId.toString().substring(0, 4),
                "Test User",
                "user@example.test",
                role,
                AccountStatus.ACTIVE,
                true,
                scope,
                Instant.parse("2026-11-28T05:00:00Z"),
                null,
                null
        );
    }
}
