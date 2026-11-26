package com.chatsystem.common.dto.auth;

import com.chatsystem.common.domain.SessionScope;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Active session descriptor returned by {@code GET /api/v1/auth/me} and successful logins. */
public record SessionInfo(
        UUID sessionId,
        SessionScope scope,
        Instant createdAt,
        AuthenticatedUserSummary user
) {
    public SessionInfo {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(user, "user");
    }
}
