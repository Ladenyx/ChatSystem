package com.chatsystem.common.api;

/** Canonical error codes shared across REST responses and WebSocket {@code system.error} frames. */
public enum ErrorCode {
    VALIDATION_ERROR(400, false),
    INVALID_CURSOR(400, false),
    INVALID_DATE_RANGE(400, false),

    UNAUTHORIZED(401, false),
    SESSION_REVOKED(401, false),
    TRANSITION_TOKEN_EXPIRED(401, false),

    FORBIDDEN(403, false),
    SESSION_SCOPE_MISMATCH(403, false),
    ACCOUNT_LOCKED(403, false),
    ACCOUNT_DELETED(403, false),
    EMAIL_NOT_VERIFIED(403, false),
    BLOCKED_BY_RELATIONSHIP(403, false),
    NOT_CONVERSATION_MEMBER(403, false),
    GROUP_ADMIN_REQUIRED(403, false),
    E2EE_DEVICE_UNTRUSTED(403, false),

    NOT_FOUND(404, false),

    CONFLICT(409, false),
    USERNAME_ALREADY_EXISTS(409, false),
    EMAIL_ALREADY_EXISTS(409, false),
    IDEMPOTENCY_CONFLICT(409, false),
    LAST_GROUP_ADMIN_REQUIRED(409, false),
    GROUP_CLOSED(409, false),
    RECALL_WINDOW_EXPIRED(409, false),
    ALREADY_REVIEWED(409, false),
    E2EE_EPOCH_STALE(409, true),
    E2EE_BACKUP_REVISION_CONFLICT(409, false),

    RESOURCE_EXPIRED(410, false),
    PAYLOAD_TOO_LARGE(413, false),
    RATE_LIMITED(429, true),

    INTERNAL_ERROR(500, true),
    EMAIL_DELIVERY_FAILED(502, true),
    AI_PROVIDER_ERROR(502, true),
    SERVICE_UNAVAILABLE(503, true),
    AI_PROVIDER_TIMEOUT(504, true);

    private final int defaultStatus;
    private final boolean defaultRetryable;

    ErrorCode(int defaultStatus, boolean defaultRetryable) {
        this.defaultStatus = defaultStatus;
        this.defaultRetryable = defaultRetryable;
    }

    public int defaultStatus() {
        return defaultStatus;
    }

    public boolean defaultRetryable() {
        return defaultRetryable;
    }
}
