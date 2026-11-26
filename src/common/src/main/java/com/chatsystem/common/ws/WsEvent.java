package com.chatsystem.common.ws;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Raw JSON Server -> Client WebSocket event envelope. */
public record WsEvent<T>(
        String type,
        UUID eventId,
        Long seq,
        UUID requestId,
        Instant timestamp,
        T payload
) {
    public WsEvent {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(payload, "payload");
        WsEventType parsed = WsEventType.fromWireName(type);
        if (parsed.direction() != WsEventType.Direction.SERVER_TO_CLIENT) {
            throw new IllegalArgumentException("WsEvent requires SERVER_TO_CLIENT type, got: " + type);
        }
        if (parsed.sequenced() && (seq == null || seq < 1)) {
            throw new IllegalArgumentException("Sequenced event " + type + " requires positive seq");
        }
        if (!parsed.sequenced() && seq != null) {
            throw new IllegalArgumentException("Ephemeral event " + type + " must have null seq");
        }
    }

    public static <T> WsEvent<T> ephemeral(
            WsEventType eventType,
            UUID eventId,
            UUID requestId,
            Instant timestamp,
            T payload
    ) {
        Objects.requireNonNull(eventType, "eventType");
        return new WsEvent<>(eventType.wireName(), eventId, null, requestId, timestamp, payload);
    }

    public static <T> WsEvent<T> sequenced(
            WsEventType eventType,
            UUID eventId,
            long seq,
            UUID requestId,
            Instant timestamp,
            T payload
    ) {
        Objects.requireNonNull(eventType, "eventType");
        return new WsEvent<>(eventType.wireName(), eventId, seq, requestId, timestamp, payload);
    }

    public WsEventType eventType() {
        return WsEventType.fromWireName(type);
    }
}
