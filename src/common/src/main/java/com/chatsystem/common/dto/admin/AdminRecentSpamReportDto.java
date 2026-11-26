package com.chatsystem.common.dto.admin;

import com.chatsystem.common.domain.SpamReviewStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Metadata-only summary row for the recent spam reports table on {@code A-SCR-02}. */
public record AdminRecentSpamReportDto(
        UUID id,
        UUID reportedUserId,
        String reportedUsername,
        UUID reporterUserId,
        String reporterUsername,
        Instant receivedAt,
        SpamReviewStatus reviewStatus
) {
    public AdminRecentSpamReportDto {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(reportedUserId, "reportedUserId");
        Objects.requireNonNull(reportedUsername, "reportedUsername");
        Objects.requireNonNull(reporterUserId, "reporterUserId");
        Objects.requireNonNull(reporterUsername, "reporterUsername");
        Objects.requireNonNull(receivedAt, "receivedAt");
        Objects.requireNonNull(reviewStatus, "reviewStatus");
        if (reportedUsername.isBlank() || reporterUsername.isBlank()) {
            throw new IllegalArgumentException("reportedUsername and reporterUsername must not be blank");
        }
    }
}
