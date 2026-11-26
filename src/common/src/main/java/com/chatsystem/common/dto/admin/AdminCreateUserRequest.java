package com.chatsystem.common.dto.admin;

import com.chatsystem.common.domain.Gender;
import java.time.LocalDate;
import java.util.Objects;

/** Request payload for {@code POST /api/v1/admin/users} (A-02, A-DLG-01). */
public record AdminCreateUserRequest(
        String username,
        String fullName,
        String email,
        String address,
        LocalDate dateOfBirth,
        Gender gender
) {
    public AdminCreateUserRequest {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(email, "email");
        if (username.isBlank() || fullName.isBlank() || email.isBlank()) {
            throw new IllegalArgumentException("username, fullName, and email must not be blank");
        }
    }
}
