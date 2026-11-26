package com.chatsystem.common.ws;

import com.chatsystem.common.domain.PresenceStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Payload for {@code presence.changed} scoped strictly to eligible non-blocked contacts. */
public record WsPresenceChangedPayload(UUID userId, PresenceStatus status, Instant changedAt) {
    public WsPresenceChangedPayload {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(changedAt, "changedAt");
    }
}
