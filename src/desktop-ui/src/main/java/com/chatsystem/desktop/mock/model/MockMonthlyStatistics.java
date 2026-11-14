package com.chatsystem.desktop.mock.model;

import java.time.YearMonth;

/** Active users are distinct app openers per month, never a sum of daily unique counts. */
public record MockMonthlyStatistics(YearMonth month, int registrations, int activeUsers) {
}
