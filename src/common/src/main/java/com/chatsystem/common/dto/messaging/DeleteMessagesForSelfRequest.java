package com.chatsystem.common.dto.messaging;

import com.chatsystem.common.api.ApiHeaders;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Request payload for {@code POST /api/v1/conversations/{id}/messages/delete-for-self} (U-16). */
public record DeleteMessagesForSelfRequest(List<UUID> messageIds) {
    public DeleteMessagesForSelfRequest {
        Objects.requireNonNull(messageIds, "messageIds");
        messageIds = List.copyOf(messageIds);
        if (messageIds.isEmpty() || messageIds.size() > ApiHeaders.MAX_BULK_DELETE_SIZE) {
            throw new IllegalArgumentException(
                    "messageIds must contain between 1 and " + ApiHeaders.MAX_BULK_DELETE_SIZE + " entries");
        }
    }
}
