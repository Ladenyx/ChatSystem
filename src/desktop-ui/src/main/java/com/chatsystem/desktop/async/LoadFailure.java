package com.chatsystem.desktop.async;

import com.chatsystem.common.api.ApiError;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.desktop.client.ApiClientException;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/** Structured failure descriptor distinguishing disconnected, retryable, and authorization/session errors. */
public record LoadFailure(
        ViewLoadStatus status,
        String message,
        ErrorCode errorCode,
        int httpStatus,
        boolean retryable,
        Throwable cause
) {
    public LoadFailure {
        Objects.requireNonNull(status, "status");
        if (status != ViewLoadStatus.RETRYABLE_ERROR && status != ViewLoadStatus.DISCONNECTED) {
            throw new IllegalArgumentException("LoadFailure status must be RETRYABLE_ERROR or DISCONNECTED: " + status);
        }
        Objects.requireNonNull(message, "message");
        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }

    public static LoadFailure fromThrowable(Throwable throwable) {
        Throwable root = unwrap(Objects.requireNonNull(throwable, "throwable"));
        if (root instanceof ApiClientException apiEx) {
            String message = nonBlankOr(apiEx.getMessage(), "Mất kết nối tới máy chủ.");
            if (apiEx.httpStatus() == 0) {
                return new LoadFailure(ViewLoadStatus.DISCONNECTED, message, null, 0, true, apiEx);
            }
            ErrorCode code = apiEx.apiError().map(ApiError::code).orElse(null);
            return new LoadFailure(
                    ViewLoadStatus.RETRYABLE_ERROR,
                    message,
                    code,
                    apiEx.httpStatus(),
                    apiEx.isRetryable(),
                    apiEx
            );
        }
        if (root instanceof IOException || root instanceof TimeoutException) {
            return new LoadFailure(
                    ViewLoadStatus.DISCONNECTED,
                    "Mất kết nối tới máy chủ.",
                    null,
                    0,
                    true,
                    root
            );
        }
        return new LoadFailure(
                ViewLoadStatus.RETRYABLE_ERROR,
                "Đã xảy ra lỗi khi tải dữ liệu.",
                null,
                0,
                true,
                root
        );
    }

    public boolean isSessionExpired() {
        return httpStatus == 401
                || errorCode == ErrorCode.UNAUTHORIZED
                || errorCode == ErrorCode.SESSION_REVOKED
                || errorCode == ErrorCode.TRANSITION_TOKEN_EXPIRED;
    }

    public boolean isPermissionDenied() {
        return httpStatus == 403
                || errorCode == ErrorCode.FORBIDDEN
                || errorCode == ErrorCode.SESSION_SCOPE_MISMATCH
                || errorCode == ErrorCode.ACCOUNT_LOCKED
                || errorCode == ErrorCode.ACCOUNT_DELETED
                || errorCode == ErrorCode.EMAIL_NOT_VERIFIED
                || errorCode == ErrorCode.BLOCKED_BY_RELATIONSHIP
                || errorCode == ErrorCode.NOT_CONVERSATION_MEMBER
                || errorCode == ErrorCode.GROUP_ADMIN_REQUIRED
                || errorCode == ErrorCode.E2EE_DEVICE_UNTRUSTED;
    }

    public boolean isPermissionOrSessionLoss() {
        return isSessionExpired() || isPermissionDenied();
    }

    private static Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while ((current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private static String nonBlankOr(String candidate, String fallback) {
        return (candidate == null || candidate.isBlank()) ? fallback : candidate;
    }
}
