package com.chatsystem.server.admin;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.api.OffsetPage;
import com.chatsystem.common.api.SortDirection;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.dto.admin.AdminUserListItemDto;
import com.chatsystem.server.api.ApiException;
import java.util.Locale;
import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Exposes filtered and sorted user directory queries for Admin App ({@code A-01}). */
@RestController
@RequestMapping(ApiHeaders.API_V1_PREFIX + "/admin/users")
public class AdminUsersController {
    private final AdminUserRepository adminUserRepository;

    public AdminUsersController(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = Objects.requireNonNull(adminUserRepository, "adminUserRepository");
    }

    @GetMapping
    public OffsetPage<AdminUserListItemDto> listUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "fullName") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDir,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "25") int size
    ) {
        AdminUserQuery query = new AdminUserQuery(
                name,
                username,
                parseStatus(status),
                AdminUserSortField.parse(sortBy),
                parseSortDirection(sortDir),
                page,
                size
        );
        return adminUserRepository.findUsers(query);
    }

    private static AccountStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return AccountStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Trạng thái tài khoản không hợp lệ: " + raw);
        }
    }

    private static SortDirection parseSortDirection(String raw) {
        if (raw == null || raw.isBlank()) {
            return SortDirection.ASC;
        }
        try {
            return SortDirection.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Hướng sắp xếp không hợp lệ: " + raw);
        }
    }
}
