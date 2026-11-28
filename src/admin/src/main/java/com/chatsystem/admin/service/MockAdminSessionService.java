package com.chatsystem.admin.service;

import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.auth.AuthenticatedUserSummary;
import com.chatsystem.common.dto.auth.LoginResponse;
import com.chatsystem.common.dto.auth.SessionInfo;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Deterministic offline mock implementation used by Version 1 previews and UI tests. */
public final class MockAdminSessionService implements AdminSessionService {
    private static final SessionInfo DEFAULT_SESSION = new SessionInfo(
            UUID.fromString("b2c3d4e5-0003-4000-8000-000000000003"),
            SessionScope.ADMIN_APP,
            Instant.parse("2026-11-15T03:01:00Z"),
            new AuthenticatedUserSummary(
                    UUID.fromString("10000000-0000-4000-8000-000000000099"),
                    "sysadmin",
                    "Quản Trị Hệ Thống",
                    "admin@example.test",
                    UserRole.ADMIN,
                    AccountStatus.ACTIVE,
                    true
            )
    );

    @Override
    public CompletableFuture<LoginResponse> login(String username, String password) {
        return CompletableFuture.completedFuture(LoginResponse.sessionGranted("mock-admin-token", DEFAULT_SESSION));
    }

    @Override
    public CompletableFuture<SessionInfo> fetchCurrentSession() {
        return CompletableFuture.completedFuture(DEFAULT_SESSION);
    }

    @Override
    public CompletableFuture<Void> logout() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isLiveBackend() {
        return false;
    }
}
