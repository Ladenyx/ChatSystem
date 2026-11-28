package com.chatsystem.server.admin;

import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.server.api.ApiException;
import java.util.Locale;

/** Supported sort columns for {@code GET /api/v1/admin/users} ({@code A-01}). */
public enum AdminUserSortField {
    FULL_NAME("fullName"),
    CREATED_AT("createdAt");

    private final String apiName;

    AdminUserSortField(String apiName) {
        this.apiName = apiName;
    }

    public String apiName() {
        return apiName;
    }

    public static AdminUserSortField parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return FULL_NAME;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "fullname", "full_name" -> FULL_NAME;
            case "createdat", "created_at" -> CREATED_AT;
            default -> throw new ApiException(
                    ErrorCode.VALIDATION_ERROR,
                    "Tiêu chí sắp xếp không hợp lệ: " + raw
            );
        };
    }
}
