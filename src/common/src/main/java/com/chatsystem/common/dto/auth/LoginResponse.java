package com.chatsystem.common.dto.auth;

import java.time.Instant;
import java.util.Objects;

/**
 * Response payload for {@code POST /api/v1/auth/login}.
 * Strictly separates full application sessions from short-lived pre-session transition tokens.
 */
public record LoginResponse(
        AuthNextAction nextAction,
        String sessionToken,
        SessionInfo session,
        String transitionToken,
        Instant transitionExpiresAt
) {
    public LoginResponse {
        Objects.requireNonNull(nextAction, "nextAction");
        if (nextAction == AuthNextAction.SESSION_GRANTED) {
            if (sessionToken == null || sessionToken.isBlank() || session == null) {
                throw new IllegalArgumentException("SESSION_GRANTED requires sessionToken and session");
            }
            if (transitionToken != null || transitionExpiresAt != null) {
                throw new IllegalArgumentException("SESSION_GRANTED must not include transition fields");
            }
        } else {
            if (transitionToken == null || transitionToken.isBlank() || transitionExpiresAt == null) {
                throw new IllegalArgumentException(nextAction + " requires transitionToken and transitionExpiresAt");
            }
            if (sessionToken != null || session != null) {
                throw new IllegalArgumentException(nextAction + " must not grant an application session");
            }
        }
    }

    public static LoginResponse sessionGranted(String sessionToken, SessionInfo session) {
        return new LoginResponse(AuthNextAction.SESSION_GRANTED, sessionToken, session, null, null);
    }

    public static LoginResponse emailVerificationRequired(String transitionToken, Instant expiresAt) {
        return new LoginResponse(
                AuthNextAction.EMAIL_VERIFICATION_REQUIRED, null, null, transitionToken, expiresAt);
    }

    public static LoginResponse forcePasswordChangeRequired(String transitionToken, Instant expiresAt) {
        return new LoginResponse(
                AuthNextAction.FORCE_PASSWORD_CHANGE_REQUIRED, null, null, transitionToken, expiresAt);
    }
}
