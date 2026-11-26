package com.chatsystem.common.dto.messaging;

import com.chatsystem.common.api.ApiHeaders;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Bidirectional cursor page returned by {@code GET /api/v1/conversations/{id}/messages}.
 * Items are ordered chronologically ascending {@code (createdAt ASC, id ASC)} with at most 50 entries.
 */
public record MessageWindowResponse(
        List<MessageDto> items,
        String nextCursor,
        boolean hasMoreOlder,
        String newerCursor,
        boolean hasMoreNewer,
        UUID anchorMessageId
) {
    public MessageWindowResponse {
        Objects.requireNonNull(items, "items");
        items = List.copyOf(items);
        if (items.size() > ApiHeaders.MAX_MESSAGE_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "items size must not exceed " + ApiHeaders.MAX_MESSAGE_PAGE_SIZE);
        }
        if (hasMoreOlder && (nextCursor == null || nextCursor.isBlank())) {
            throw new IllegalArgumentException("nextCursor is required when hasMoreOlder is true");
        }
        if (hasMoreNewer && (newerCursor == null || newerCursor.isBlank())) {
            throw new IllegalArgumentException("newerCursor is required when hasMoreNewer is true");
        }
    }
}
