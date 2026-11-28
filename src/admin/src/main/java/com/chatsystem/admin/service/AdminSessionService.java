package com.chatsystem.admin.service;

import com.chatsystem.common.dto.auth.SessionInfo;
import java.util.concurrent.CompletableFuture;

/** Admin App session service boundary allowing seamless toggle between Version 1 mock previews and live Server. */
public interface AdminSessionService {
    CompletableFuture<SessionInfo> fetchCurrentSession();

    CompletableFuture<Void> logout();

    boolean isLiveBackend();
}
