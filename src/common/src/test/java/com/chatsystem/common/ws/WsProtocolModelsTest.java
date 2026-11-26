package com.chatsystem.common.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chatsystem.common.domain.MessageDeliveryState;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WsProtocolModelsTest {
    private static final Instant NOW = Instant.parse("2026-11-15T03:00:00Z");

    @Test
    void everyWsEventTypeRoundTripsByWireName() {
        for (WsEventType type : WsEventType.values()) {
            assertEquals(type, WsEventType.fromWireName(type.wireName()));
        }
        assertTrue(WsEventType.findByWireName("unknown.event").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> WsEventType.fromWireName("unknown.event"));
    }

    @Test
    void wsCommandAllowsOnlyClientToServerTypes() {
        var command = WsCommand.of(WsEventType.MESSAGE_SEND, UUID.randomUUID(), NOW, Map.of("k", "v"));
        assertEquals("message.send", command.type());
        assertEquals(WsEventType.MESSAGE_SEND, command.eventType());

        assertThrows(IllegalArgumentException.class, () -> WsCommand.of(
                WsEventType.MESSAGE_CREATED, UUID.randomUUID(), NOW, Map.of()));
    }

    @Test
    void wsEventEnforcesSequencedAndEphemeralRules() {
        var sequenced = WsEvent.sequenced(
                WsEventType.MESSAGE_CREATED, UUID.randomUUID(), 42L, null, NOW, Map.of());
        assertEquals(42L, sequenced.seq());
        assertNull(sequenced.requestId());

        var ephemeral = WsEvent.ephemeral(
                WsEventType.SYSTEM_PONG, UUID.randomUUID(), UUID.randomUUID(), NOW, Map.of());
        assertNull(ephemeral.seq());

        assertThrows(IllegalArgumentException.class, () -> WsEvent.ephemeral(
                WsEventType.MESSAGE_CREATED, UUID.randomUUID(), null, NOW, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> WsEvent.sequenced(
                WsEventType.SYSTEM_PONG, UUID.randomUUID(), 1L, null, NOW, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> WsEvent.ephemeral(
                WsEventType.MESSAGE_SEND, UUID.randomUUID(), null, NOW, Map.of()));
    }

    @Test
    void wsCloseCodesClassifyReconnectability() {
        assertFalse(WsCloseCode.ADMIN_SESSION_REPLACED.reconnectable());
        assertFalse(WsCloseCode.SESSION_REVOKED.reconnectable());
        assertTrue(WsCloseCode.HEARTBEAT_TIMEOUT.reconnectable());
        assertTrue(WsCloseCode.RESYNC_REQUIRED.reconnectable());
        assertEquals(WsCloseCode.ADMIN_SESSION_REPLACED, WsCloseCode.fromCode(4004));
    }

    @Test
    void receiptPayloadRejectsNonReceiptStatesOrEmptyTargets() {
        var valid = new WsReceiptPayload(
                UUID.randomUUID(), UUID.randomUUID(), List.of(), UUID.randomUUID(), MessageDeliveryState.READ, NOW);
        assertEquals(MessageDeliveryState.READ, valid.state());

        assertThrows(IllegalArgumentException.class, () -> new WsReceiptPayload(
                UUID.randomUUID(), UUID.randomUUID(), List.of(UUID.randomUUID()), null, MessageDeliveryState.STORED, NOW));
        assertThrows(IllegalArgumentException.class, () -> new WsReceiptPayload(
                UUID.randomUUID(), UUID.randomUUID(), List.of(), null, MessageDeliveryState.DELIVERED, NOW));
    }
}
