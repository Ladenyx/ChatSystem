package com.chatsystem.common.dto.admin;

import com.chatsystem.common.domain.AccountStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Direct friend row rendered in the A-SCR-03 Friends detail tab ({@code A-06}). */
public record AdminUserFriendDto(
        UUID userId,
        String username,
        String fullName,
        AccountStatus status,
        Instant friendsSince
) {
    public AdminUserFriendDto {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(friendsSince, "friendsSince");
    }
}
