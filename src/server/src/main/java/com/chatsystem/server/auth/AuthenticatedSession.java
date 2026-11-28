package com.chatsystem.server.auth;

import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.auth.AuthenticatedUserSummary;
import com.chatsystem.common.dto.auth.SessionInfo;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Server-side authenticated session snapshot joined from {@code user_sessions} and {@code users}. */
public record AuthenticatedSession(
        UUID sessionId,
        UUID userId,
        String username,
        String fullName,
        String email,
        UserRole role,
        AccountStatus status,
        boolean emailVerified,
        SessionScope scope,
        Instant createdAt,
        Instant revokedAt,
        UUID deviceId
) {
    public static final String REQUEST_ATTRIBUTE = AuthenticatedSession.class.getName() + ".CURRENT";

    public AuthenticatedSession {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public AuthenticatedSession withDeviceId(UUID clientDeviceId) {
        return new AuthenticatedSession(
                sessionId,
                userId,
                username,
                fullName,
                email,
                role,
                status,
                emailVerified,
                scope,
                createdAt,
                revokedAt,
                clientDeviceId
        );
    }

    public boolean isActiveAccountAndSession() {
        return revokedAt == null && status == AccountStatus.ACTIVE && emailVerified;
    }

    public SessionInfo toSessionInfo() {
        return new SessionInfo(
                sessionId,
                scope,
                createdAt,
                new AuthenticatedUserSummary(
                        userId,
                        username,
                        fullName,
                        email,
                        role,
                        status,
                        emailVerified
                )
        );
    }
}
