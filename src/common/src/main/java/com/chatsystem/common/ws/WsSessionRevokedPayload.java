package com.chatsystem.common.ws;

import java.util.Objects;
import java.util.UUID;

/** Payload for {@code session.revoked} delivered immediately before closing the revoked WebSocket. */
public record WsSessionRevokedPayload(UUID sessionId, SessionRevocationReason reason, String message) {
    public WsSessionRevokedPayload {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(message, "message");
        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
