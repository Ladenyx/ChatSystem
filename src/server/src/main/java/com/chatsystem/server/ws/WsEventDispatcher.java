package com.chatsystem.server.ws;

import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.ws.SessionRevocationReason;
import com.chatsystem.common.ws.WsEvent;
import com.chatsystem.common.ws.WsEventType;
import com.chatsystem.server.auth.SessionAuthenticator;
import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** Fan-out dispatcher that sequences durable events only after permission checks and re-verifies live sessions. */
@Component
public class WsEventDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(WsEventDispatcher.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final WsSessionRegistry sessionRegistry;
    private final SessionAuthenticator authenticator;
    private final UserEventLog userEventLog;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public WsEventDispatcher(
            WsSessionRegistry sessionRegistry,
            SessionAuthenticator authenticator,
            UserEventLog userEventLog,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.sessionRegistry = Objects.requireNonNull(sessionRegistry, "sessionRegistry");
        this.authenticator = Objects.requireNonNull(authenticator, "authenticator");
        this.userEventLog = Objects.requireNonNull(userEventLog, "userEventLog");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public boolean sendDirect(WsConnection connection, WsEvent<?> event) {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(event, "event");
        String json = serialize(event);
        return json != null && connection.sendText(json);
    }

    public Optional<WsEvent<?>> sendToUser(
            UUID userId,
            WsEventType type,
            UUID requestId,
            Object payload,
            BooleanSupplier permissionCheck
    ) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(payload, "payload");
        if (permissionCheck != null && !permissionCheck.getAsBoolean()) {
            return Optional.empty();
        }
        WsEvent<?> event = createUserEvent(userId, type, requestId, payload);
        String json = serialize(event);
        if (json == null) {
            return Optional.of(event);
        }
        for (WsConnection connection : sessionRegistry.userClientConnections(userId)) {
            if (!authenticator.isSessionStillActive(connection.session().sessionId())) {
                sessionRegistry.revokeConnection(
                        connection,
                        SessionRevocationReason.LOGOUT,
                        "Phiên đăng nhập không còn hiệu lực."
                );
                continue;
            }
            connection.sendText(json);
        }
        return Optional.of(event);
    }

    public void sendToAdminApps(WsEventType type, Object payload) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(payload, "payload");
        if (type.sequenced() || !type.wireName().startsWith("admin.")) {
            throw new IllegalArgumentException("Admin push requires ephemeral admin.* WsEventType: " + type);
        }
        WsEvent<Object> event = WsEvent.ephemeral(type, UUID.randomUUID(), null, clock.instant(), payload);
        String json = serialize(event);
        if (json == null) {
            return;
        }
        for (WsConnection connection : sessionRegistry.allAdminConnections()) {
            if (connection.session().scope() != SessionScope.ADMIN_APP
                    || connection.session().role() != UserRole.ADMIN) {
                continue;
            }
            if (!authenticator.isSessionStillActive(connection.session().sessionId())) {
                sessionRegistry.revokeConnection(
                        connection,
                        SessionRevocationReason.LOGOUT,
                        "Phiên đăng nhập không còn hiệu lực."
                );
                continue;
            }
            connection.sendText(json);
        }
    }

    private WsEvent<?> createUserEvent(UUID userId, WsEventType type, UUID requestId, Object payload) {
        if (type.sequenced()) {
            Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);
            return userEventLog.append(userId, type, requestId, payloadMap);
        }
        return WsEvent.ephemeral(type, UUID.randomUUID(), requestId, clock.instant(), payload);
    }

    private String serialize(WsEvent<?> event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JacksonException ex) {
            LOG.error("Failed serializing WsEvent {}: {}", event.type(), ex.getMessage());
            return null;
        }
    }
}
