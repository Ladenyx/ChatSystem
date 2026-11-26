package com.chatsystem.common.dto.spam;

import java.util.Objects;

/** Request payload for {@code POST /api/v1/admin/spam-reports/{reportId}/review} (A-10, DEC-10). */
public record MarkSpamReportReviewedRequest(String reviewNote) {
    public MarkSpamReportReviewedRequest {
        Objects.requireNonNull(reviewNote, "reviewNote");
        if (reviewNote.isBlank()) {
            throw new IllegalArgumentException("reviewNote must not be blank");
        }
    }
}
