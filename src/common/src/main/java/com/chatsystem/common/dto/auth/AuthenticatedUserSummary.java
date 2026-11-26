package com.chatsystem.common.dto.auth;

import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.UserRole;
import java.util.Objects;
import java.util.UUID;

/** Compact authenticated account summary embedded inside {@link SessionInfo}. */
public record AuthenticatedUserSummary(
        UUID id,
        String username,
        String fullName,
        String email,
        UserRole role,
        AccountStatus status,
        boolean emailVerified
) {
    public AuthenticatedUserSummary {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(status, "status");
    }
}
