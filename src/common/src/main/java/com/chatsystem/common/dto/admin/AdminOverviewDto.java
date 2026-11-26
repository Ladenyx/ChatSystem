package com.chatsystem.common.dto.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Snapshot returned by {@code GET /api/v1/admin/overview} for A-SCR-02. */
public record AdminOverviewDto(
        long totalUsers,
        long onlineUsers,
        long totalGroups,
        long pendingSpamReports,
        int year,
        LocalDate asOfDate,
        List<MonthlyCountPointDto> monthlyRegistrations,
        List<MonthlyCountPointDto> monthlyActiveUsers,
        List<AdminRecentSpamReportDto> recentSpamReports,
        List<AdminLoginHistoryItemDto> recentLogins
) {
    public AdminOverviewDto {
        if (totalUsers < 0 || onlineUsers < 0 || totalGroups < 0 || pendingSpamReports < 0) {
            throw new IllegalArgumentException("Overview KPI counts must be >= 0");
        }
        Objects.requireNonNull(asOfDate, "asOfDate");
        Objects.requireNonNull(monthlyRegistrations, "monthlyRegistrations");
        Objects.requireNonNull(monthlyActiveUsers, "monthlyActiveUsers");
        Objects.requireNonNull(recentSpamReports, "recentSpamReports");
        Objects.requireNonNull(recentLogins, "recentLogins");
        monthlyRegistrations = List.copyOf(monthlyRegistrations);
        monthlyActiveUsers = List.copyOf(monthlyActiveUsers);
        recentSpamReports = List.copyOf(recentSpamReports);
        recentLogins = List.copyOf(recentLogins);
        if (monthlyRegistrations.size() != 12 || monthlyActiveUsers.size() != 12) {
            throw new IllegalArgumentException("Monthly series must contain exactly 12 points (T1..T12)");
        }
    }
}
