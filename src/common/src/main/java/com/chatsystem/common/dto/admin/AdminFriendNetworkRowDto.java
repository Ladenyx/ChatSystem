package com.chatsystem.common.dto.admin;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Friend-network scale row for A-14 ({@code A-SCR-09}). */
public record AdminFriendNetworkRowDto(
        UUID userId,
        String username,
        String fullName,
        Instant createdAt,
        int directFriends,
        int extendedFriends
) {
    public AdminFriendNetworkRowDto {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(createdAt, "createdAt");
        if (directFriends < 0 || extendedFriends < directFriends) {
            throw new IllegalArgumentException("extendedFriends must be >= directFriends >= 0");
        }
    }
}
