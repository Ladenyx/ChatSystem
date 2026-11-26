package com.chatsystem.common.dto.auth;

import com.chatsystem.common.domain.SessionScope;
import java.util.Objects;
import java.util.UUID;

/** Request payload for {@code POST /api/v1/auth/login}. */
public record LoginRequest(String username, String password, SessionScope scope, UUID launchId) {
    public LoginRequest {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        Objects.requireNonNull(scope, "scope");
        if (username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (password.isEmpty()) {
            throw new IllegalArgumentException("password must not be empty");
        }
        if (scope == SessionScope.USER_CLIENT && launchId == null) {
            throw new IllegalArgumentException("launchId is required for USER_CLIENT login");
        }
    }
}
