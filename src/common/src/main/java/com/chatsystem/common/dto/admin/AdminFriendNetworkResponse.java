package com.chatsystem.common.dto.admin;

import com.chatsystem.common.api.OffsetPage;
import java.util.Objects;

/** Combined KPI summary and paginated friend-network table returned by {@code GET /api/v1/admin/analytics/friend-network}. */
public record AdminFriendNetworkResponse(
        long currentAccounts,
        long connectedAccounts,
        OffsetPage<AdminFriendNetworkRowDto> page
) {
    public AdminFriendNetworkResponse {
        if (currentAccounts < 0 || connectedAccounts < 0) {
            throw new IllegalArgumentException("Friend network KPI counts must be >= 0");
        }
        if (connectedAccounts > currentAccounts) {
            throw new IllegalArgumentException("connectedAccounts must not exceed currentAccounts");
        }
        Objects.requireNonNull(page, "page");
    }
}
