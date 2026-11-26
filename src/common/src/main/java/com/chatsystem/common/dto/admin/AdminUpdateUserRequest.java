package com.chatsystem.common.dto.admin;

import com.chatsystem.common.domain.Gender;
import java.time.LocalDate;
import java.util.Objects;

/** Request payload for {@code PATCH /api/v1/admin/users/{userId}} (A-02, A-DLG-01; username is immutable). */
public record AdminUpdateUserRequest(
        String fullName,
        String email,
        String address,
        LocalDate dateOfBirth,
        Gender gender
) {
    public AdminUpdateUserRequest {
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(email, "email");
        if (fullName.isBlank() || email.isBlank()) {
            throw new IllegalArgumentException("fullName and email must not be blank");
        }
    }
}
