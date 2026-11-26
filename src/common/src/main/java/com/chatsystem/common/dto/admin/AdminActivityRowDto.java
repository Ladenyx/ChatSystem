package com.chatsystem.common.dto.admin;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Active-user table row for A-15 ({@code A-SCR-08}); survives physical account purge via {@code accountKey}. */
public record AdminActivityRowDto(
        UUID accountKey,
        UUID userId,
        String username,
        String fullName,
        Instant createdAt,
        int appOpens,
        int peopleChattedWith,
        int groupsChattedIn
) {
    public AdminActivityRowDto {
        Objects.requireNonNull(accountKey, "accountKey");
        if (appOpens < 0 || peopleChattedWith < 0 || groupsChattedIn < 0) {
            throw new IllegalArgumentException("Activity counts must be >= 0");
        }
    }
}
