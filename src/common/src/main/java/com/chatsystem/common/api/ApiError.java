package com.chatsystem.common.api;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Uniform error payload returned for all 4xx/5xx REST responses and WebSocket {@code system.error} frames. */
public record ApiError(
        ErrorCode code,
        String message,
        int status,
        Instant timestamp,
        String path,
        String requestId,
        boolean retryable,
        List<FieldError> details
) {
    public ApiError {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(requestId, "requestId");
        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        if (requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (status < 400 || status > 599) {
            throw new IllegalArgumentException("status must be an HTTP 4xx/5xx code: " + status);
        }
        details = details == null ? List.of() : List.copyOf(details);
    }

    public static ApiError of(ErrorCode code, String message, Instant timestamp, String path, String requestId) {
        return of(code, message, timestamp, path, requestId, List.of());
    }

    public static ApiError of(
            ErrorCode code,
            String message,
            Instant timestamp,
            String path,
            String requestId,
            List<FieldError> details
    ) {
        Objects.requireNonNull(code, "code");
        return new ApiError(
                code,
                message,
                code.defaultStatus(),
                timestamp,
                path,
                requestId,
                code.defaultRetryable(),
                details
        );
    }
}
