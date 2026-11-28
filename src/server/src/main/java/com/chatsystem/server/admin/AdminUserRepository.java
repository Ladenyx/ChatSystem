package com.chatsystem.server.admin;

import com.chatsystem.common.api.OffsetPage;
import com.chatsystem.common.dto.admin.AdminUserListItemDto;

/** Persistence port for browsing and filtering accounts in Admin App ({@code A-01}). */
public interface AdminUserRepository {
    OffsetPage<AdminUserListItemDto> findUsers(AdminUserQuery query);
}
