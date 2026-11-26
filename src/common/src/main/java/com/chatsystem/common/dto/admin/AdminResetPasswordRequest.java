package com.chatsystem.common.dto.admin;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Request body for {@code POST /api/v1/admin/users/{userId}/reset-password} (A-04, UC-ADM-USER-MGMT-09).
 * Supplies the email shown in {@code A-DLG-02} so the Server rejects concurrent email changes with 409 CONFLICT.
 */
public record AdminResetPasswordRequest(String expectedEmail) {
    public static final int MAX_EMAIL_LENGTH = 254;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@.\\s]+(\\.[^@.\\s]+)*$");

    public AdminResetPasswordRequest {
        Objects.requireNonNull(expectedEmail, "expectedEmail");
        if (!expectedEmail.equals(expectedEmail.strip())
                || expectedEmail.isEmpty()
                || expectedEmail.length() > MAX_EMAIL_LENGTH
                || !EMAIL_PATTERN.matcher(expectedEmail).matches()) {
            throw new IllegalArgumentException(
                    "expectedEmail must be a trimmed valid email address of at most " + MAX_EMAIL_LENGTH + " characters");
        }
    }
}
