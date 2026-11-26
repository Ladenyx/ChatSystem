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
    public static final int MAX_RECENT_ITEMS = 3;

    public AdminOverviewDto {
        if (totalUsers < 0 || onlineUsers < 0 || totalGroups < 0 || pendingSpamReports < 0) {
            throw new IllegalArgumentException("Overview KPI counts must be >= 0");
        }
        Objects.requireNonNull(asOfDate, "asOfDate");
        Objects.requireNonNull(recentSpamReports, "recentSpamReports");
        Objects.requireNonNull(recentLogins, "recentLogins");
        monthlyRegistrations = MonthlyCountPointDto.requireOrderedTwelveMonths(
                year, monthlyRegistrations, "monthlyRegistrations");
        monthlyActiveUsers = MonthlyCountPointDto.requireOrderedTwelveMonths(
                year, monthlyActiveUsers, "monthlyActiveUsers");
        recentSpamReports = List.copyOf(recentSpamReports);
        recentLogins = List.copyOf(recentLogins);
        if (recentSpamReports.size() > MAX_RECENT_ITEMS) {
            throw new IllegalArgumentException(
                    "recentSpamReports must contain at most " + MAX_RECENT_ITEMS + " entries");
        }
        if (recentLogins.size() > MAX_RECENT_ITEMS) {
            throw new IllegalArgumentException(
                    "recentLogins must contain at most " + MAX_RECENT_ITEMS + " entries");
        }
    }
}
