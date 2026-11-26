package com.chatsystem.common.ws;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Raw JSON Client -> Server WebSocket command envelope. */
public record WsCommand<T>(String type, UUID requestId, Instant timestamp, T payload) {
    public WsCommand {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(payload, "payload");
        WsEventType parsed = WsEventType.fromWireName(type);
        if (parsed.direction() != WsEventType.Direction.CLIENT_TO_SERVER) {
            throw new IllegalArgumentException("WsCommand requires CLIENT_TO_SERVER type, got: " + type);
        }
    }

    public static <T> WsCommand<T> of(WsEventType eventType, UUID requestId, Instant timestamp, T payload) {
        Objects.requireNonNull(eventType, "eventType");
        return new WsCommand<>(eventType.wireName(), requestId, timestamp, payload);
    }

    public WsEventType eventType() {
        return WsEventType.fromWireName(type);
    }
}
