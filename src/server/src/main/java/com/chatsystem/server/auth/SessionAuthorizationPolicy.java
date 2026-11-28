package com.chatsystem.server.auth;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.server.api.ApiException;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** Enforces public route exclusions, {@link SessionScope}, and {@link UserRole} rules from {@code rest-conventions.md}. */
@Component
public class SessionAuthorizationPolicy {
    private static final String HEALTH_PATH = ApiHeaders.API_V1_PREFIX + "/health";
    private static final String LOGIN_PATH = ApiHeaders.API_V1_PREFIX + "/auth/login";
    private static final String LOGOUT_PATH = ApiHeaders.API_V1_PREFIX + "/auth/logout";
    private static final String ME_PATH = ApiHeaders.API_V1_PREFIX + "/auth/me";
    private static final String REGISTRATIONS_PREFIX = ApiHeaders.API_V1_PREFIX + "/auth/registrations";
    private static final String PASSWORD_RESETS_PATH = ApiHeaders.API_V1_PREFIX + "/auth/password-resets";
    private static final String FORCE_PASSWORD_CHANGE_PATH = ApiHeaders.API_V1_PREFIX + "/auth/force-password-change";
    private static final String ADMIN_PREFIX = ApiHeaders.API_V1_PREFIX + "/admin";

    public boolean isPublic(String method, String path) {
        if (path == null) {
            return false;
        }
        if ("GET".equalsIgnoreCase(method) && HEALTH_PATH.equals(path)) {
            return true;
        }
        if ("POST".equalsIgnoreCase(method) && (LOGIN_PATH.equals(path)
                || PASSWORD_RESETS_PATH.equals(path)
                || FORCE_PASSWORD_CHANGE_PATH.equals(path))) {
            return true;
        }
        return path.equals(REGISTRATIONS_PREFIX) || path.startsWith(REGISTRATIONS_PREFIX + "/");
    }

    public void authorize(String path, AuthenticatedSession session) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(session, "session");
        if (ME_PATH.equals(path) || LOGOUT_PATH.equals(path)) {
            return;
        }
        if (isAdminPath(path)) {
            if (session.scope() != SessionScope.ADMIN_APP) {
                throw new ApiException(
                        ErrorCode.SESSION_SCOPE_MISMATCH,
                        "Phiên hiện tại không có phạm vi quản trị (ADMIN_APP)."
                );
            }
            if (session.role() != UserRole.ADMIN) {
                throw new ApiException(ErrorCode.FORBIDDEN, "Tài khoản không có quyền quản trị viên.");
            }
            return;
        }
        if (session.scope() != SessionScope.USER_CLIENT) {
            throw new ApiException(
                    ErrorCode.SESSION_SCOPE_MISMATCH,
                    "Phiên quản trị không được phép gọi API người dùng."
            );
        }
    }

    private static boolean isAdminPath(String path) {
        return path.equals(ADMIN_PREFIX) || path.startsWith(ADMIN_PREFIX + "/");
    }
}
