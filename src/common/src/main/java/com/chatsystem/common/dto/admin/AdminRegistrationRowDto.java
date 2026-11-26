package com.chatsystem.common.dto.admin;

import com.chatsystem.common.domain.AccountStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Registration table row for A-12 ({@code A-SCR-07}); survives physical account purge via {@code accountKey}. */
public record AdminRegistrationRowDto(
        UUID accountKey,
        UUID userId,
        String username,
        String fullName,
        String email,
        AccountStatus status,
        Instant registeredAt
) {
    public AdminRegistrationRowDto {
        Objects.requireNonNull(accountKey, "accountKey");
        Objects.requireNonNull(registeredAt, "registeredAt");
    }
}
