package com.chatsystem.server.ws;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.dto.messaging.SyncEventsResponse;
import com.chatsystem.common.ws.WsEvent;
import com.chatsystem.common.ws.WsEventType;
import com.chatsystem.server.config.ChatServerProperties;
import java.time.Clock;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;

/**
 * Maintains per-user monotonic sequence numbers and bounded catch-up buffers for reconnect sync.
 * Future durable persistence can implement the same contract backed by PostgreSQL.
 */
@Component
public class UserEventLog {
    private final int maxBufferSizePerUser;
    private final Clock clock;
    private final ConcurrentMap<UUID, UserBuffer> buffers = new ConcurrentHashMap<>();

    public UserEventLog(ChatServerProperties properties, Clock clock) {
        this.maxBufferSizePerUser = Objects.requireNonNull(properties, "properties")
                .ws()
                .syncBufferSizePerUser();
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public long latestSeq(UUID userId) {
        Objects.requireNonNull(userId, "userId");
        UserBuffer buffer = buffers.get(userId);
        return buffer == null ? 0L : buffer.latestSeq();
    }

    public WsEvent<Map<String, Object>> append(
            UUID userId,
            WsEventType type,
            UUID requestId,
            Map<String, Object> payload
    ) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(payload, "payload");
        if (!type.sequenced()) {
            throw new IllegalArgumentException("UserEventLog accepts only sequenced WsEventType: " + type);
        }
        UserBuffer buffer = buffers.computeIfAbsent(userId, ignored -> new UserBuffer(maxBufferSizePerUser));
        return buffer.append(type, requestId, clock.instant(), Map.copyOf(payload));
    }

    public SyncEventsResponse fetchAfter(
            UUID userId,
            long afterSeq,
            int limit,
            Predicate<WsEvent<Map<String, Object>>> permissionFilter
    ) {
        Objects.requireNonNull(userId, "userId");
        if (afterSeq < 0) {
            throw new IllegalArgumentException("afterSeq must be >= 0");
        }
        if (limit < 1 || limit > ApiHeaders.MAX_SYNC_BATCH_SIZE) {
            throw new IllegalArgumentException("limit must be between 1 and " + ApiHeaders.MAX_SYNC_BATCH_SIZE);
        }
        Predicate<WsEvent<Map<String, Object>>> filter = permissionFilter != null ? permissionFilter : ignored -> true;
        UserBuffer buffer = buffers.get(userId);
        if (buffer == null) {
            boolean ahead = afterSeq > 0;
            return new SyncEventsResponse(List.of(), 0L, 0L, false, ahead);
        }
        return buffer.fetchAfter(afterSeq, limit, filter);
    }

    private static final class UserBuffer {
        private final int capacity;
        private final Deque<WsEvent<Map<String, Object>>> events = new ArrayDeque<>();
        private long latestSeq = 0L;
        private long evictedUpToSeq = 0L;

        private UserBuffer(int capacity) {
            this.capacity = capacity;
        }

        private synchronized long latestSeq() {
            return latestSeq;
        }

        private synchronized WsEvent<Map<String, Object>> append(
                WsEventType type,
                UUID requestId,
                java.time.Instant timestamp,
                Map<String, Object> payload
        ) {
            latestSeq++;
            WsEvent<Map<String, Object>> event = WsEvent.sequenced(
                    type,
                    UUID.randomUUID(),
                    latestSeq,
                    requestId,
                    timestamp,
                    payload
            );
            if (events.size() >= capacity) {
                WsEvent<Map<String, Object>> evicted = events.removeFirst();
                evictedUpToSeq = Math.max(evictedUpToSeq, evicted.seq());
            }
            events.addLast(event);
            return event;
        }

        private synchronized SyncEventsResponse fetchAfter(
                long afterSeq,
                int limit,
                Predicate<WsEvent<Map<String, Object>>> filter
        ) {
            if (afterSeq > latestSeq || afterSeq < evictedUpToSeq) {
                return new SyncEventsResponse(List.of(), latestSeq, latestSeq, false, true);
            }
            if (afterSeq == latestSeq) {
                return new SyncEventsResponse(List.of(), latestSeq, latestSeq, false, false);
            }
            List<WsEvent<Map<String, Object>>> collected = new ArrayList<>();
            long nextSeq = afterSeq;
            boolean hasMore = false;
            for (WsEvent<Map<String, Object>> event : events) {
                long seq = event.seq();
                if (seq <= afterSeq) {
                    continue;
                }
                if (collected.size() >= limit) {
                    hasMore = true;
                    break;
                }
                nextSeq = seq;
                if (filter.test(event)) {
                    collected.add(event);
                }
            }
            if (!hasMore) {
                nextSeq = latestSeq;
            }
            return new SyncEventsResponse(collected, nextSeq, latestSeq, hasMore, false);
        }
    }
}
