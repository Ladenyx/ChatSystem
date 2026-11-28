package com.chatsystem.server.ws;

import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.ws.SessionRevocationReason;
import com.chatsystem.common.ws.WsCloseCode;
import com.chatsystem.common.ws.WsEvent;
import com.chatsystem.common.ws.WsEventType;
import com.chatsystem.common.ws.WsSessionRevokedPayload;
import com.chatsystem.server.auth.AuthenticatedSession;
import com.chatsystem.server.auth.SessionRevocationPublisher;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/** Thread-safe in-memory registry of active WebSocket connections partitioned by {@link SessionScope}. */
@Component
public class WsSessionRegistry implements SessionRevocationPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(WsSessionRegistry.class);

    private final ConcurrentMap<String, WsConnection> byWsId = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Set<WsConnection>> bySessionId = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Set<WsConnection>> userClientByUserId = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Set<WsConnection>> adminByUserId = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public WsSessionRegistry(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public WsConnection register(WebSocketSession rawSession, AuthenticatedSession session) {
        Objects.requireNonNull(rawSession, "rawSession");
        Objects.requireNonNull(session, "session");
        WsConnection connection = new WsConnection(rawSession, session, clock.instant());
        byWsId.put(connection.wsId(), connection);
        bySessionId.computeIfAbsent(session.sessionId(), ignored -> ConcurrentHashMap.newKeySet()).add(connection);
        targetScopeMap(session.scope())
                .computeIfAbsent(session.userId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(connection);
        return connection;
    }

    public WsConnection unregister(String wsId) {
        if (wsId == null) {
            return null;
        }
        WsConnection connection = byWsId.remove(wsId);
        if (connection == null) {
            return null;
        }
        AuthenticatedSession session = connection.session();
        removeFromBucket(bySessionId, session.sessionId(), connection);
        removeFromBucket(targetScopeMap(session.scope()), session.userId(), connection);
        return connection;
    }

    public WsConnection findByWsId(String wsId) {
        return wsId == null ? null : byWsId.get(wsId);
    }

    public Collection<WsConnection> allConnections() {
        return List.copyOf(byWsId.values());
    }

    public List<WsConnection> userClientConnections(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        Set<WsConnection> set = userClientByUserId.get(userId);
        return set == null ? List.of() : List.copyOf(set);
    }

    public List<WsConnection> allAdminConnections() {
        List<WsConnection> result = new ArrayList<>();
        adminByUserId.values().forEach(result::addAll);
        return result;
    }

    public boolean hasActiveUserClientConnections(UUID userId) {
        Set<WsConnection> set = userClientByUserId.get(userId);
        return set != null && !set.isEmpty();
    }

    @Override
    public void publishSessionRevoked(UUID sessionId, SessionRevocationReason reason, String message) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(message, "message");
        Set<WsConnection> connections = bySessionId.get(sessionId);
        if (connections == null || connections.isEmpty()) {
            return;
        }
        String payloadJson = serializeRevokedEvent(sessionId, reason, message);
        WsCloseCode closeCode = closeCodeFor(reason);
        for (WsConnection connection : List.copyOf(connections)) {
            sendRevokedAndClose(connection, payloadJson, closeCode, message);
        }
    }

    public void revokeConnection(WsConnection connection, SessionRevocationReason reason, String message) {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(message, "message");
        String payloadJson = serializeRevokedEvent(connection.session().sessionId(), reason, message);
        sendRevokedAndClose(connection, payloadJson, closeCodeFor(reason), message);
    }

    private void sendRevokedAndClose(
            WsConnection connection,
            String payloadJson,
            WsCloseCode closeCode,
            String message
    ) {
        if (payloadJson != null) {
            connection.sendText(payloadJson);
        }
        connection.close(closeCode, message);
    }

    private String serializeRevokedEvent(UUID sessionId, SessionRevocationReason reason, String message) {
        WsEvent<WsSessionRevokedPayload> event = WsEvent.ephemeral(
                WsEventType.SESSION_REVOKED,
                UUID.randomUUID(),
                null,
                clock.instant(),
                new WsSessionRevokedPayload(sessionId, reason, message)
        );
        return serializeQuietly(event);
    }

    private ConcurrentMap<UUID, Set<WsConnection>> targetScopeMap(SessionScope scope) {
        return scope == SessionScope.ADMIN_APP ? adminByUserId : userClientByUserId;
    }

    private String serializeQuietly(WsEvent<?> event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JacksonException ex) {
            LOG.warn("Failed serializing session.revoked frame: {}", ex.getMessage());
            return null;
        }
    }

    private static WsCloseCode closeCodeFor(SessionRevocationReason reason) {
        return switch (reason) {
            case ADMIN_NEW_LOGIN -> WsCloseCode.ADMIN_SESSION_REPLACED;
            case ACCOUNT_LOCKED, ACCOUNT_DELETED -> WsCloseCode.ACCOUNT_RESTRICTED;
            case PASSWORD_CHANGED, LOGOUT -> WsCloseCode.SESSION_REVOKED;
        };
    }

    private static void removeFromBucket(
            ConcurrentMap<UUID, Set<WsConnection>> map,
            UUID key,
            WsConnection connection
    ) {
        map.computeIfPresent(key, (ignored, set) -> {
            set.remove(connection);
            return set.isEmpty() ? null : set;
        });
    }
}
