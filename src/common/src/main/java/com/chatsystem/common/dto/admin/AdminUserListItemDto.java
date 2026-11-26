package com.chatsystem.common.dto.admin;

import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.UserRole;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Lightweight row for the left master table in A-SCR-03 ({@code GET /api/v1/admin/users}). */
public record AdminUserListItemDto(
        UUID id,
        String username,
        String fullName,
        UserRole role,
        AccountStatus status,
        Instant createdAt
) {
    public AdminUserListItemDto {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}
