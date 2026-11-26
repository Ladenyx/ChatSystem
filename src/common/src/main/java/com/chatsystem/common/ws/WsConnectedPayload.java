package com.chatsystem.common.ws;

import com.chatsystem.common.domain.SessionScope;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Payload for the initial {@code system.connected} server frame after WSS handshake. */
public record WsConnectedPayload(
        UUID sessionId,
        UUID userId,
        SessionScope scope,
        long latestSeq,
        Instant serverTime,
        int heartbeatIntervalSec
) {
    public WsConnectedPayload {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(serverTime, "serverTime");
        if (latestSeq < 0) {
            throw new IllegalArgumentException("latestSeq must be >= 0");
        }
        if (heartbeatIntervalSec <= 0) {
            throw new IllegalArgumentException("heartbeatIntervalSec must be > 0");
        }
    }
}
