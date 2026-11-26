package com.chatsystem.common.dto.spam;

import com.chatsystem.common.api.ApiHeaders;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Request payload for {@code POST /api/v1/spam-reports} (U-26, DEC-10).
 * Enforces Selected (1..20), Context (0..30), unique message IDs, and unique display order.
 * Byte limit (1 MiB) and SHA-256 fingerprint are computed by {@link CanonicalSpamReportEncoder}.
 */
public record SubmitSpamReportRequest(
        UUID id,
        UUID reportedUserId,
        UUID conversationId,
        String reason,
        List<SpamEvidenceItemDto> evidence
) {
    public SubmitSpamReportRequest {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(reportedUserId, "reportedUserId");
        Objects.requireNonNull(conversationId, "conversationId");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(evidence, "evidence");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        evidence = List.copyOf(evidence);
        validateEvidenceSet(evidence);
    }

    private static void validateEvidenceSet(List<SpamEvidenceItemDto> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("evidence must not be empty");
        }
        int selectedCount = 0;
        int contextCount = 0;
        Set<UUID> seenMessages = new HashSet<>();
        Set<Integer> seenOrders = new HashSet<>();
        for (SpamEvidenceItemDto item : items) {
            if (!seenMessages.add(item.sourceMessageId())) {
                throw new IllegalArgumentException("Duplicate sourceMessageId in evidence: " + item.sourceMessageId());
            }
            if (!seenOrders.add(item.displayOrder())) {
                throw new IllegalArgumentException("Duplicate displayOrder in evidence: " + item.displayOrder());
            }
            if (item.selected()) {
                selectedCount++;
            }
            if (item.context()) {
                contextCount++;
            }
        }
        if (selectedCount < 1 || selectedCount > ApiHeaders.MAX_SPAM_SELECTED) {
            throw new IllegalArgumentException(
                    "Selected evidence count must be 1.." + ApiHeaders.MAX_SPAM_SELECTED + ", got: " + selectedCount);
        }
        if (contextCount > ApiHeaders.MAX_SPAM_CONTEXT) {
            throw new IllegalArgumentException(
                    "Context evidence count must be <= " + ApiHeaders.MAX_SPAM_CONTEXT + ", got: " + contextCount);
        }
    }
}
