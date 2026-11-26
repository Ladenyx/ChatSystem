package com.chatsystem.common.dto.auth;

/** Outcome returned by {@code POST /api/v1/auth/login} per DEC-06 and DEC-07. */
public enum AuthNextAction {
    SESSION_GRANTED,
    EMAIL_VERIFICATION_REQUIRED,
    FORCE_PASSWORD_CHANGE_REQUIRED
}
