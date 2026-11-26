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
        Objects.requireNonNull(points, "points");
        availableYears = List.copyOf(availableYears);
        points = List.copyOf(points);
        if (points.size() != 12) {
            throw new IllegalArgumentException("Monthly series must contain exactly 12 points (T1..T12)");
        }
        for (int index = 0; index < 12; index++) {
            MonthlyCountPointDto point = points.get(index);
            if (point.year() != year || point.month() != index + 1) {
                throw new IllegalArgumentException(
                        "Point at index " + index + " must match year " + year + " and month " + (index + 1));
            }
        }
    }
}
