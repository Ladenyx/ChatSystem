package com.chatsystem.common.domain;

import java.util.Objects;

/**
 * Optional gender value on user profiles.
 * JSON contracts use enum names ({@code MALE}, {@code FEMALE}, {@code OTHER});
 * PostgreSQL {@code users.gender} stores Vietnamese labels ({@code Nam}, {@code Nữ}, {@code Khác}).
 */
public enum Gender {
    MALE("Nam"),
    FEMALE("Nữ"),
    OTHER("Khác");

    private final String databaseValue;

    Gender(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public static Gender fromDatabaseValue(String value) {
        Objects.requireNonNull(value, "value");
        for (Gender candidate : values()) {
            if (candidate.databaseValue.equals(value)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Unknown database gender label: " + value);
    }
}
