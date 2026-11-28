package com.chatsystem.server.auth;

import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.UserRole;
import java.util.Objects;
import java.util.UUID;

/** Account credential and lifecycle snapshot read from {@code public.users}. */
public record AuthAccountRecord(
        UUID id,
        String username,
        String fullName,
        String email,
        UserRole role,
        AccountStatus status,
        String passwordHash,
        boolean emailVerified
) {
    public AuthAccountRecord {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(status, "status");
    }
}
