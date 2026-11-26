package com.chatsystem.common.dto.messaging;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Search match entry for U-17 (in-conversation search) and U-18 (global search). */
public record MessageSearchHitDto(
        UUID messageId,
        UUID conversationId,
        UUID senderUserId,
        Instant createdAt,
        String snippet
) {
    public MessageSearchHitDto {
        Objects.requireNonNull(messageId, "messageId");
        Objects.requireNonNull(conversationId, "conversationId");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(snippet, "snippet");
    }
}
