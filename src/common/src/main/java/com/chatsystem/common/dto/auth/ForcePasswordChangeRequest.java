package com.chatsystem.common.dto.auth;

import java.util.Objects;

/** Request payload for {@code POST /api/v1/auth/force-password-change} (U-SCR-05). */
public record ForcePasswordChangeRequest(String newPassword) {
    public ForcePasswordChangeRequest {
        Objects.requireNonNull(newPassword, "newPassword");
        if (newPassword.length() < 8 || newPassword.length() > 128 || newPassword.isBlank()) {
            throw new IllegalArgumentException("newPassword must be 8-128 non-blank characters");
        }
    }
}
