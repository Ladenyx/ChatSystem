package com.chatsystem.server.api;

import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.api.FieldError;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/** Domain/API exception mapped uniformly to {@link com.chatsystem.common.api.ApiError}. */
public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;
    private final List<FieldError> details;
    private final Duration retryAfter;

    public ApiException(ErrorCode errorCode, String message) {
        this(errorCode, message, List.of(), null);
    }

    public ApiException(ErrorCode errorCode, String message, List<FieldError> details, Duration retryAfter) {
        super(Objects.requireNonNull(message, "message"));
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
        this.details = details == null ? List.of() : List.copyOf(details);
        this.retryAfter = retryAfter;
    }

    public static ApiException validation(String field, String code, String message) {
        return new ApiException(
                ErrorCode.VALIDATION_ERROR,
                message,
                List.of(new FieldError(field, code, message)),
                null
        );
    }

    public static ApiException rateLimited(String message, Duration retryAfter) {
        return new ApiException(ErrorCode.RATE_LIMITED, message, List.of(), retryAfter);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<FieldError> getDetails() {
        return details;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
