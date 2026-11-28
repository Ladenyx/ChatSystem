package com.chatsystem.admin.service;

import com.chatsystem.common.api.OffsetPage;
import com.chatsystem.common.dto.admin.AdminUserListItemDto;
import java.util.concurrent.CompletableFuture;

/** Admin App boundary for listing, filtering, and sorting user accounts ({@code A-01}). */
public interface AdminUserDirectoryService {
    CompletableFuture<OffsetPage<AdminUserListItemDto>> listUsers(AdminUserFilter filter);

    boolean isLiveBackend();
}
