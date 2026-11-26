package com.chatsystem.common.dto.admin;

import java.util.Objects;

/**
 * Response returned by {@code POST /api/v1/admin/users} (A-02, UC-ADM-USER-MGMT-03).
 * Preserves the created user record even when SMTP delivery fails so the client does not retry creation blindly.
 */
public record AdminCreateUserResponse(
        AdminUserDetailDto user,
        boolean activationEmailSent,
        String emailDeliveryError
) {
    public AdminCreateUserResponse {
        Objects.requireNonNull(user, "user");
        if (activationEmailSent && emailDeliveryError != null) {
            throw new IllegalArgumentException("emailDeliveryError must be null when activationEmailSent is true");
        }
        if (!activationEmailSent && (emailDeliveryError == null || emailDeliveryError.isBlank())) {
            throw new IllegalArgumentException(
                    "emailDeliveryError must be non-blank when activationEmailSent is false");
        }
    }
}
