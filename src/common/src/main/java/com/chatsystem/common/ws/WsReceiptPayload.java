package com.chatsystem.common.ws;

import com.chatsystem.common.domain.MessageDeliveryState;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Payload shared by {@code receipt.delivered}, {@code receipt.read}, and {@code receipt.updated} frames. */
public record WsReceiptPayload(
        UUID conversationId,
        UUID userId,
        List<UUID> messageIds,
        UUID upToMessageId,
        MessageDeliveryState state,
        Instant updatedAt
) {
    public WsReceiptPayload {
        Objects.requireNonNull(conversationId, "conversationId");
        Objects.requireNonNull(state, "state");
        if (state != MessageDeliveryState.DELIVERED && state != MessageDeliveryState.READ) {
            throw new IllegalArgumentException("Receipt state must be DELIVERED or READ, got: " + state);
        }
        messageIds = messageIds == null ? List.of() : List.copyOf(messageIds);
        if (messageIds.isEmpty() && upToMessageId == null) {
            throw new IllegalArgumentException("Either messageIds or upToMessageId must be provided");
        }
    }
}
