package com.chatsystem.server.ws;

import static org.assertj.core.api.Assertions.assertThat;

import com.chatsystem.common.dto.messaging.SyncEventsResponse;
import com.chatsystem.common.ws.WsEvent;
import com.chatsystem.common.ws.WsEventType;
import com.chatsystem.server.config.ChatServerProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserEventLogTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-11-28T05:00:00Z"), ZoneOffset.UTC);

    @Test
    void assignsMonotonicSequenceAndFiltersUnauthorizedEvents() {
        ChatServerProperties props = new ChatServerProperties(
                new ChatServerProperties.WsProperties(25, 60, 10, 10, List.of("*")),
                null,
                null
        );
        UserEventLog log = new UserEventLog(props, FIXED_CLOCK);
        UUID userId = UUID.randomUUID();

        WsEvent<Map<String, Object>> e1 = log.append(userId, WsEventType.MESSAGE_CREATED, null, Map.of("allow", true, "n", 1));
        WsEvent<Map<String, Object>> e2 = log.append(userId, WsEventType.MESSAGE_CREATED, null, Map.of("allow", false, "n", 2));
        WsEvent<Map<String, Object>> e3 = log.append(userId, WsEventType.FRIEND_REQUEST_CREATED, null, Map.of("allow", true, "n", 3));

        assertThat(e1.seq()).isEqualTo(1L);
        assertThat(e2.seq()).isEqualTo(2L);
        assertThat(e3.seq()).isEqualTo(3L);
        assertThat(log.latestSeq(userId)).isEqualTo(3L);

        SyncEventsResponse page = log.fetchAfter(
                userId,
                0L,
                200,
                event -> Boolean.TRUE.equals(event.payload().get("allow"))
        );
        assertThat(page.fullResyncRequired()).isFalse();
        assertThat(page.hasMore()).isFalse();
        assertThat(page.nextSeq()).isEqualTo(3L);
        assertThat(page.latestSeq()).isEqualTo(3L);
        assertThat(page.events()).extracting(WsEvent::seq).containsExactly(1L, 3L);
    }

    @Test
    void flagsFullResyncWhenClientSeqIsEvictedOrAhead() {
        ChatServerProperties props = new ChatServerProperties(
                new ChatServerProperties.WsProperties(25, 60, 10, 2, List.of("*")),
                null,
                null
        );
        UserEventLog log = new UserEventLog(props, FIXED_CLOCK);
        UUID userId = UUID.randomUUID();

        log.append(userId, WsEventType.MESSAGE_CREATED, null, Map.of("n", 1));
        log.append(userId, WsEventType.MESSAGE_CREATED, null, Map.of("n", 2));
        log.append(userId, WsEventType.MESSAGE_CREATED, null, Map.of("n", 3));

        SyncEventsResponse evicted = log.fetchAfter(userId, 0L, 200, ignored -> true);
        assertThat(evicted.fullResyncRequired()).isTrue();
        assertThat(evicted.events()).isEmpty();
        assertThat(evicted.latestSeq()).isEqualTo(3L);

        SyncEventsResponse ahead = log.fetchAfter(userId, 99L, 200, ignored -> true);
        assertThat(ahead.fullResyncRequired()).isTrue();
        assertThat(ahead.nextSeq()).isEqualTo(3L);
    }
}
