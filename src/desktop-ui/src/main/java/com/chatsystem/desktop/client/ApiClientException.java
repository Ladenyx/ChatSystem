package com.chatsystem.desktop.client;

import com.chatsystem.common.api.ApiError;
import java.util.Optional;

/** Client-side exception representing either a structured server {@link ApiError} or a transport failure. */
public class ApiClientException extends RuntimeException {
    private final ApiError apiError;
    private final int httpStatus;
    private final boolean retryable;

    public ApiClientException(ApiError apiError, int httpStatus) {
        super(apiError.message());
        this.apiError = apiError;
        this.httpStatus = httpStatus;
        this.retryable = apiError.retryable();
    }

    public ApiClientException(int httpStatus, String message, boolean retryable, Throwable cause) {
        super(message, cause);
        this.apiError = null;
        this.httpStatus = httpStatus;
        this.retryable = retryable;
    }

    public Optional<ApiError> apiError() {
        return Optional.ofNullable(apiError);
    }

    public int httpStatus() {
        return httpStatus;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
