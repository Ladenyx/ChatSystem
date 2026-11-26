package com.chatsystem.common.dto.messaging;

import com.chatsystem.common.domain.ConversationType;
import com.chatsystem.common.domain.PresenceStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Conversation list row rendered in U-SCR-07. */
public record ConversationSummaryDto(
        UUID id,
        ConversationType type,
        String title,
        UUID peerUserId,
        PresenceStatus peerPresence,
        boolean encrypted,
        int unreadCount,
        int memberCount,
        String lastMessagePreview,
        Instant lastMessageAt,
        Instant createdAt,
        Instant closedAt
) {
    public ConversationSummaryDto {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(createdAt, "createdAt");
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (unreadCount < 0) {
            throw new IllegalArgumentException("unreadCount must be >= 0");
        }
        if (memberCount < 1) {
            throw new IllegalArgumentException("memberCount must be >= 1");
        }
        if (type == ConversationType.GROUP && !encrypted) {
            throw new IllegalArgumentException("GROUP conversations must be encrypted per DEC-05");
        }
        if (type == ConversationType.DIRECT && encrypted) {
            throw new IllegalArgumentException("DIRECT conversations must not be marked E2EE");
        }
    }
}
