package com.chatsystem.common.dto.messaging;

import com.chatsystem.common.domain.MessageDeliveryState;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Server durability acknowledgement emitted only after PostgreSQL commits the message and send-event row. */
public record SendMessageAckDto(
        UUID messageId,
        UUID conversationId,
        Instant createdAt,
        MessageDeliveryState state,
        long seq
) {
    public SendMessageAckDto {
        Objects.requireNonNull(messageId, "messageId");
        Objects.requireNonNull(conversationId, "conversationId");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(state, "state");
        if (!state.isAcceptedByServer()) {
            throw new IllegalArgumentException("Ack state must be accepted by server, got: " + state);
        }
        if (seq < 1) {
            throw new IllegalArgumentException("seq must be >= 1");
        }
    }
}
