package com.chatsystem.server.admin;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.api.SortDirection;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.server.api.ApiException;
import java.util.Objects;

/** Validated query parameters for {@code GET /api/v1/admin/users} ({@code A-01}). */
public record AdminUserQuery(
        String name,
        String username,
        AccountStatus status,
        AdminUserSortField sortBy,
        SortDirection sortDir,
        int page,
        int size
) {
    public AdminUserQuery {
        name = trimToNull(name);
        username = trimToNull(username);
        sortBy = Objects.requireNonNullElse(sortBy, AdminUserSortField.FULL_NAME);
        sortDir = Objects.requireNonNullElse(sortDir, SortDirection.ASC);
        if (page < 0) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Chỉ số trang (page) phải >= 0.");
        }
        if (size < 1 || size > ApiHeaders.MAX_PAGE_SIZE) {
            throw new ApiException(
                    ErrorCode.VALIDATION_ERROR,
                    "Kích thước trang (size) phải từ 1 đến " + ApiHeaders.MAX_PAGE_SIZE + "."
            );
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
