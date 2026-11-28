package com.chatsystem.admin.service;

import com.chatsystem.common.api.SortDirection;
import com.chatsystem.common.domain.AccountStatus;
import java.util.Objects;

/** Query filter and sort criteria for Admin user directory ({@code A-01}). */
public record AdminUserFilter(
        String name,
        String username,
        AccountStatus status,
        String sortBy,
        SortDirection sortDir,
        int page,
        int size
) {
    public AdminUserFilter {
        name = trimToNull(name);
        username = trimToNull(username);
        sortBy = (sortBy == null || sortBy.isBlank()) ? "fullName" : sortBy.trim();
        sortDir = Objects.requireNonNullElse(sortDir, SortDirection.ASC);
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be >= 1");
        }
    }

    private static String trimToNull(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
