package com.chatsystem.common.dto.admin;

/** Single month data point inside 12-month Admin analytics charts (A-13, A-16, A-SCR-09). */
public record MonthlyCountPointDto(int year, int month, long count) {
    public MonthlyCountPointDto {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12: " + month);
        }
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0: " + count);
        }
    }
}
