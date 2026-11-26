package com.chatsystem.common.dto.messaging;

import com.chatsystem.common.ws.WsEvent;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Catch-up sync batch shared by {@code GET /api/v1/sync/events} and WebSocket {@code sync.batch}. */
public record SyncEventsResponse(
        List<WsEvent<Map<String, Object>>> events,
        long nextSeq,
        long latestSeq,
        boolean hasMore,
        boolean fullResyncRequired
) {
    public SyncEventsResponse {
        Objects.requireNonNull(events, "events");
        events = List.copyOf(events);
        if (nextSeq < 0 || latestSeq < 0) {
            throw new IllegalArgumentException("Sequence numbers must be >= 0");
        }
        if (nextSeq > latestSeq) {
            throw new IllegalArgumentException("nextSeq (" + nextSeq + ") must not exceed latestSeq (" + latestSeq + ")");
        }
    }
}
