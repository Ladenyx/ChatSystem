package com.chatsystem.common.api;

import java.util.Objects;

/** Validation error detail attached to a specific input field inside {@link ApiError}. */
public record FieldError(String field, String code, String message) {
    public FieldError {
        requireNonBlank(field, "field");
        requireNonBlank(code, "code");
        requireNonBlank(message, "message");
    }

    private static void requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
