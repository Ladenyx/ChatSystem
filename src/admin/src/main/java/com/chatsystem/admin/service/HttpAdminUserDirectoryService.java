package com.chatsystem.admin.service;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.api.OffsetPage;
import com.chatsystem.common.dto.admin.AdminUserListItemDto;
import com.chatsystem.desktop.client.RestApiClient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import tools.jackson.core.type.TypeReference;

/** Live REST client for {@code GET /api/v1/admin/users} ({@code A-01}). */
public final class HttpAdminUserDirectoryService implements AdminUserDirectoryService {
    private static final String USERS_PATH = ApiHeaders.API_V1_PREFIX + "/admin/users";
    private static final TypeReference<OffsetPage<AdminUserListItemDto>> PAGE_TYPE = new TypeReference<>() {};

    private final RestApiClient restApiClient;

    public HttpAdminUserDirectoryService(RestApiClient restApiClient) {
        this.restApiClient = Objects.requireNonNull(restApiClient, "restApiClient");
    }

    @Override
    public CompletableFuture<OffsetPage<AdminUserListItemDto>> listUsers(AdminUserFilter filter) {
        Objects.requireNonNull(filter, "filter");
        return restApiClient.getAsync(buildPathWithQuery(filter), PAGE_TYPE);
    }

    @Override
    public boolean isLiveBackend() {
        return true;
    }

    static String buildPathWithQuery(AdminUserFilter filter) {
        StringBuilder query = new StringBuilder(USERS_PATH)
                .append("?sortBy=").append(encode(filter.sortBy()))
                .append("&sortDir=").append(filter.sortDir().name())
                .append("&page=").append(filter.page())
                .append("&size=").append(filter.size());
        if (filter.name() != null) {
            query.append("&name=").append(encode(filter.name()));
        }
        if (filter.username() != null) {
            query.append("&username=").append(encode(filter.username()));
        }
        if (filter.status() != null) {
            query.append("&status=").append(filter.status().name());
        }
        return query.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
