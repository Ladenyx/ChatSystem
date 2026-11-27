package com.chatsystem.common.dto.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Full 12-month analytics series envelope shared by A-SCR-07, A-SCR-08, and A-SCR-09. */
public record AdminMonthlySeriesDto(
        int year,
        LocalDate asOfDate,
        List<Integer> availableYears,
        List<MonthlyCountPointDto> points
) {
    public AdminMonthlySeriesDto {
        Objects.requireNonNull(asOfDate, "asOfDate");
        Objects.requireNonNull(availableYears, "availableYears");
        availableYears = List.copyOf(availableYears);
        points = MonthlyCountPointDto.requireOrderedTwelveMonths(year, points, "points");
    }
}
