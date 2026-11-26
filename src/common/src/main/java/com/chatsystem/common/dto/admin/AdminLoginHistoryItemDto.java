package com.chatsystem.common.dto.admin;

import com.chatsystem.common.domain.SessionScope;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Login timeline entry for A-05 (per-user tab) and A-07 (global login logs); user fields are null after purge. */
public record AdminLoginHistoryItemDto(
        UUID id,
        UUID userId,
        String username,
        String fullName,
        Instant loggedInAt,
        SessionScope scope
) {
    public AdminLoginHistoryItemDto {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(loggedInAt, "loggedInAt");
        Objects.requireNonNull(scope, "scope");
    }
}
