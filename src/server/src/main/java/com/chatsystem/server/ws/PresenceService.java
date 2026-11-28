package com.chatsystem.server.ws;

import com.chatsystem.common.domain.PresenceStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.ws.WsEvent;
import com.chatsystem.common.ws.WsEventType;
import com.chatsystem.common.ws.WsPresenceChangedPayload;
import com.chatsystem.common.ws.WsPresenceSnapshotPayload;
import com.chatsystem.server.config.ChatServerProperties;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Tracks in-memory {@code USER_CLIENT} presence using atomic per-user state transitions,
 * a configurable offline grace period, and contact-scoped fan-out.
 */
@Service
public class PresenceService implements DisposableBean {
    private final PresenceRepository presenceRepository;
    private final WsEventDispatcher dispatcher;
    private final Clock clock;
    private final int offlineGraceSec;
    private final ScheduledExecutorService scheduler;
    private final boolean ownsScheduler;

    private final ConcurrentMap<UUID, UserPresenceState> states = new ConcurrentHashMap<>();

    @Autowired
    public PresenceService(
            PresenceRepository presenceRepository,
            WsSessionRegistry sessionRegistry,
            WsEventDispatcher dispatcher,
            ChatServerProperties properties,
            Clock clock
    ) {
        this(
                presenceRepository,
                sessionRegistry,
                dispatcher,
                Objects.requireNonNull(properties, "properties").ws().presenceOfflineGraceSec(),
                clock,
                Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "presence-grace-timer");
                    t.setDaemon(true);
                    return t;
                }),
                true
        );
    }

    PresenceService(
            PresenceRepository presenceRepository,
            WsSessionRegistry sessionRegistry,
            WsEventDispatcher dispatcher,
            int offlineGraceSec,
            Clock clock,
            ScheduledExecutorService scheduler,
            boolean ownsScheduler
    ) {
        this.presenceRepository = Objects.requireNonNull(presenceRepository, "presenceRepository");
        Objects.requireNonNull(sessionRegistry, "sessionRegistry");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        this.offlineGraceSec = Math.max(0, offlineGraceSec);
        this.clock = Objects.requireNonNull(clock, "clock");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.ownsScheduler = ownsScheduler;
    }

    public void onConnected(WsConnection connection) {
        Objects.requireNonNull(connection, "connection");
        if (connection.session().scope() != SessionScope.USER_CLIENT) {
            return;
        }
        UUID userId = connection.session().userId();
        boolean[] becameOnline = {false};
        states.compute(userId, (ignored, existing) -> {
            UserPresenceState state = existing == null ? new UserPresenceState() : existing;
            state.generation++;
            state.cancelPendingOffline();
            state.socketIds.add(connection.wsId());
            becameOnline[0] = !state.online;
            state.online = true;
            return state;
        });

        sendSnapshot(connection, userId);
        if (becameOnline[0]) {
            broadcastPresenceChange(userId, PresenceStatus.ONLINE);
        }
    }

    public void onDisconnected(WsConnection connection) {
        if (connection == null || connection.session().scope() != SessionScope.USER_CLIENT) {
            return;
        }
        UUID userId = connection.session().userId();
        long[] expectedGenToSchedule = {-1L};
        boolean[] broadcastOfflineImmediately = {false};

        states.computeIfPresent(userId, (ignored, state) -> {
            state.socketIds.remove(connection.wsId());
            if (!state.socketIds.isEmpty()) {
                return state;
            }
            long expectedGen = ++state.generation;
            if (offlineGraceSec == 0) {
                broadcastOfflineImmediately[0] = state.online;
                state.online = false;
                return null;
            }
            expectedGenToSchedule[0] = expectedGen;
            return state;
        });

        if (broadcastOfflineImmediately[0]) {
            broadcastPresenceChange(userId, PresenceStatus.OFFLINE);
            return;
        }
        long expectedGen = expectedGenToSchedule[0];
        if (expectedGen >= 0) {
            scheduleGraceOffline(userId, expectedGen);
        }
    }

    public boolean isOnline(UUID userId) {
        if (userId == null) {
            return false;
        }
        UserPresenceState state = states.get(userId);
        return state != null && state.online;
    }

    @Override
    public void destroy() {
        states.values().forEach(UserPresenceState::cancelPendingOffline);
        states.clear();
        if (ownsScheduler) {
            scheduler.shutdownNow();
        }
    }

    private void scheduleGraceOffline(UUID userId, long expectedGen) {
        ScheduledFuture<?> task = scheduler.schedule(
                () -> completeGraceOffline(userId, expectedGen),
                offlineGraceSec,
                TimeUnit.SECONDS
        );
        states.computeIfPresent(userId, (ignored, state) -> {
            if (state.generation == expectedGen && state.socketIds.isEmpty()) {
                state.cancelPendingOffline();
                state.pendingOfflineTask = task;
            } else {
                task.cancel(false);
            }
            return state;
        });
    }

    private void completeGraceOffline(UUID userId, long expectedGen) {
        boolean[] wentOffline = {false};
        states.computeIfPresent(userId, (ignored, state) -> {
            if (state.generation != expectedGen || !state.socketIds.isEmpty()) {
                return state;
            }
            state.pendingOfflineTask = null;
            wentOffline[0] = state.online;
            state.online = false;
            return null;
        });
        if (wentOffline[0]) {
            broadcastPresenceChange(userId, PresenceStatus.OFFLINE);
        }
    }

    private void sendSnapshot(WsConnection connection, UUID userId) {
        List<UUID> eligibleContacts = presenceRepository.findEligibleContactIds(userId);
        List<UUID> onlineContacts = new ArrayList<>();
        for (UUID contactId : eligibleContacts) {
            if (isOnline(contactId)) {
                onlineContacts.add(contactId);
            }
        }
        WsEvent<WsPresenceSnapshotPayload> snapshot = WsEvent.ephemeral(
                WsEventType.PRESENCE_SNAPSHOT,
                UUID.randomUUID(),
                null,
                clock.instant(),
                new WsPresenceSnapshotPayload(onlineContacts)
        );
        dispatcher.sendDirect(connection, snapshot);
    }

    private void broadcastPresenceChange(UUID userId, PresenceStatus status) {
        WsPresenceChangedPayload payload = new WsPresenceChangedPayload(userId, status, clock.instant());
        for (UUID contactId : presenceRepository.findEligibleContactIds(userId)) {
            if (!isOnline(contactId)) {
                continue;
            }
            dispatcher.sendToUser(contactId, WsEventType.PRESENCE_CHANGED, null, payload, null);
        }
    }

    private static final class UserPresenceState {
        private final Set<String> socketIds = new HashSet<>();
        private volatile boolean online;
        private long generation;
        private ScheduledFuture<?> pendingOfflineTask;

        private void cancelPendingOffline() {
            if (pendingOfflineTask != null) {
                pendingOfflineTask.cancel(false);
                pendingOfflineTask = null;
            }
        }
    }
}
