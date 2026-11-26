package com.chatsystem.common.dto.messaging;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Response returned after recalling a message within the 48-hour window (U-16, DEC-02). */
public record RecallMessageResponse(UUID messageId, UUID conversationId, Instant recalledAt) {
    public RecallMessageResponse {
        Objects.requireNonNull(messageId, "messageId");
        Objects.requireNonNull(conversationId, "conversationId");
        Objects.requireNonNull(recalledAt, "recalledAt");
    }
}
