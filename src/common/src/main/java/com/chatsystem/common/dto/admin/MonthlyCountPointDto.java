package com.chatsystem.common.dto.admin;

import java.util.List;
import java.util.Objects;

/** Single month data point inside 12-month Admin analytics charts (A-13, A-16, A-SCR-09). */
public record MonthlyCountPointDto(int year, int month, long count) {
    public static final int MONTHS_IN_YEAR = 12;

    public MonthlyCountPointDto {
        if (month < 1 || month > MONTHS_IN_YEAR) {
            throw new IllegalArgumentException("month must be between 1 and 12: " + month);
        }
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0: " + count);
        }
    }

    /**
     * Validates and defensively copies a 12-month series for {@code year},
     * ensuring index {@code 0..11} strictly corresponds to months {@code 1..12}.
     */
    public static List<MonthlyCountPointDto> requireOrderedTwelveMonths(
            int year,
            List<MonthlyCountPointDto> points,
            String fieldName
    ) {
        Objects.requireNonNull(points, fieldName);
        List<MonthlyCountPointDto> copy = List.copyOf(points);
        if (copy.size() != MONTHS_IN_YEAR) {
            throw new IllegalArgumentException(fieldName + " must contain exactly 12 points (T1..T12)");
        }
        for (int index = 0; index < MONTHS_IN_YEAR; index++) {
            MonthlyCountPointDto point = copy.get(index);
            if (point.year() != year || point.month() != index + 1) {
                throw new IllegalArgumentException(
                        fieldName + "[" + index + "] must match year " + year + " and month " + (index + 1));
            }
        }
        return copy;
    }
}
