package com.chatsystem.common.dto.spam;

import com.chatsystem.common.domain.SpamReviewStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Server receipt for spam report submission and Admin review confirmation (U-26, A-10, DEC-10). */
public record SpamReportReceiptDto(
        UUID reportId,
        Instant receivedAt,
        SpamReviewStatus reviewStatus,
        UUID reviewedByUserId,
        Instant reviewedAt,
        String reviewNote
) {
    public SpamReportReceiptDto {
        Objects.requireNonNull(reportId, "reportId");
        Objects.requireNonNull(receivedAt, "receivedAt");
        Objects.requireNonNull(reviewStatus, "reviewStatus");
        if (reviewStatus == SpamReviewStatus.PENDING) {
            if (reviewedByUserId != null || reviewedAt != null || reviewNote != null) {
                throw new IllegalArgumentException("PENDING report must not have review metadata");
            }
        } else if (reviewedByUserId == null || reviewedAt == null || reviewNote == null || reviewNote.isBlank()) {
            throw new IllegalArgumentException("REVIEWED report requires reviewedByUserId, reviewedAt, and reviewNote");
        }
    }
}
