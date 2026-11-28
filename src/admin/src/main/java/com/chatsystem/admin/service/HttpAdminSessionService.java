package com.chatsystem.admin.service;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.dto.auth.AuthNextAction;
import com.chatsystem.common.dto.auth.LoginRequest;
import com.chatsystem.common.dto.auth.LoginResponse;
import com.chatsystem.common.dto.auth.SessionInfo;
import com.chatsystem.desktop.client.ClientSessionStore;
import com.chatsystem.desktop.client.RestApiClient;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Live REST implementation backed by {@link RestApiClient}. */
public final class HttpAdminSessionService implements AdminSessionService {
    private static final String LOGIN_PATH = ApiHeaders.API_V1_PREFIX + "/auth/login";
    private static final String ME_PATH = ApiHeaders.API_V1_PREFIX + "/auth/me";
    private static final String LOGOUT_PATH = ApiHeaders.API_V1_PREFIX + "/auth/logout";

    private final RestApiClient restApiClient;
    private final ClientSessionStore sessionStore;

    public HttpAdminSessionService(RestApiClient restApiClient, ClientSessionStore sessionStore) {
        this.restApiClient = Objects.requireNonNull(restApiClient, "restApiClient");
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
    }

    @Override
    public CompletableFuture<LoginResponse> login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password, SessionScope.ADMIN_APP, null);
        return restApiClient.postAsync(LOGIN_PATH, request, LoginResponse.class)
                .thenApply(response -> {
                    if (response.nextAction() == AuthNextAction.SESSION_GRANTED) {
                        sessionStore.updateSession(response.sessionToken(), response.session());
                    }
                    return response;
                });
    }

    @Override
    public CompletableFuture<SessionInfo> fetchCurrentSession() {
        return restApiClient.getAsync(ME_PATH, SessionInfo.class);
    }

    @Override
    public CompletableFuture<Void> logout() {
        return restApiClient.postEmptyAsync(LOGOUT_PATH, null)
                .whenComplete((ignored, error) -> sessionStore.clear());
    }

    @Override
    public boolean isLiveBackend() {
        return true;
    }
}
