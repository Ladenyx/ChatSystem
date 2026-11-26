package com.chatsystem.common.dto.admin;

import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.Gender;
import com.chatsystem.common.domain.UserRole;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Full user profile loaded separately in the A-SCR-03 right inspector ({@code GET /api/v1/admin/users/{userId}}).
 * Mirrors PostgreSQL {@code users_deleted_check} soft-delete invariants.
 */
public record AdminUserDetailDto(
        UUID id,
        String username,
        String fullName,
        String email,
        String address,
        LocalDate dateOfBirth,
        Gender gender,
        UserRole role,
        AccountStatus status,
        AccountStatus statusBeforeDelete,
        boolean emailVerified,
        Instant createdAt,
        Instant deletedAt,
        Instant restoreDeadlineAt,
        int directFriendCount
) {
    public AdminUserDetailDto {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
        if (directFriendCount < 0) {
            throw new IllegalArgumentException("directFriendCount must be >= 0");
        }
        if (status == AccountStatus.DELETED) {
            if (deletedAt == null || restoreDeadlineAt == null
                    || (statusBeforeDelete != AccountStatus.ACTIVE && statusBeforeDelete != AccountStatus.LOCKED)) {
                throw new IllegalArgumentException(
                        "DELETED user requires deletedAt, restoreDeadlineAt, and statusBeforeDelete in {ACTIVE, LOCKED}");
            }
        } else if (deletedAt != null || restoreDeadlineAt != null || statusBeforeDelete != null) {
            throw new IllegalArgumentException("Non-DELETED user must not carry deletion metadata");
        }
    }
}
