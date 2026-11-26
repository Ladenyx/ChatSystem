package com.chatsystem.common.dto.spam;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Single deduplicated evidence snapshot row inside a DEC-10 spam report submission. */
public record SpamEvidenceItemDto(
        UUID sourceMessageId,
        UUID senderUserId,
        Instant messageCreatedAt,
        String contentPlain,
        boolean selected,
        boolean context,
        int displayOrder
) {
    public SpamEvidenceItemDto {
        Objects.requireNonNull(sourceMessageId, "sourceMessageId");
        Objects.requireNonNull(senderUserId, "senderUserId");
        Objects.requireNonNull(messageCreatedAt, "messageCreatedAt");
        Objects.requireNonNull(contentPlain, "contentPlain");
        if (!selected && !context) {
            throw new IllegalArgumentException("Evidence item must be selected, context, or both");
        }
        if (displayOrder < 1) {
            throw new IllegalArgumentException("displayOrder must be >= 1");
        }
    }
}
