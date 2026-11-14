package com.chatsystem.desktop.mock.model;

import java.time.Instant;
import java.util.List;

/** Evidence is a frozen, user-supplied plaintext snapshot, not unrestricted admin chat access. */
public record MockSpamReport(String id, String reporterId, String reportedUserId,
                             String conversationId, String reason, Instant createdAt,
                             Status status, List<Evidence> evidence) {
    public MockSpamReport {
        evidence = List.copyOf(evidence);
    }

    public record Evidence(MockMessage message, boolean selected, boolean context) {
        public Evidence {
            if (!selected && !context) {
                throw new IllegalArgumentException("Evidence must be selected, context, or both");
            }
        }
    }

    public enum Status { PENDING, REVIEWED }
}
