package com.chatsystem.common.dto.admin;

/**
 * Optional request body for {@code POST /api/v1/admin/users/{userId}/reset-password} (A-04, UC-ADM-USER-MGMT-09).
 * Supplies the email shown in {@code A-DLG-02} so the Server rejects concurrent email changes with 409 CONFLICT.
 */
public record AdminResetPasswordRequest(String expectedEmail) {
    public AdminResetPasswordRequest {
        if (expectedEmail != null && expectedEmail.isBlank()) {
            throw new IllegalArgumentException("expectedEmail must not be blank when provided");
        }
    }
}
