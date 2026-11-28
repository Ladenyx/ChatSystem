package com.chatsystem.server.auth;

import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.server.api.ApiException;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DefaultSessionAuthenticator implements SessionAuthenticator {
    private final SessionRepository sessionRepository;

    public DefaultSessionAuthenticator(SessionRepository sessionRepository) {
        this.sessionRepository = Objects.requireNonNull(sessionRepository, "sessionRepository");
    }

    @Override
    public AuthenticatedSession authenticate(String rawBearerToken, UUID clientDeviceId) {
        if (rawBearerToken == null || rawBearerToken.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Thiếu hoặc không hợp lệ mã xác thực phiên.");
        }
        String tokenHash = SessionTokenHasher.sha256Hex(rawBearerToken.trim());
        AuthenticatedSession session = sessionRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ."));
        if (session.revokedAt() != null) {
            throw new ApiException(ErrorCode.SESSION_REVOKED, "Phiên đăng nhập đã bị thu hồi.");
        }
        if (session.status() == AccountStatus.LOCKED) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED, "Tài khoản của bạn hiện đang bị khóa.");
        }
        if (session.status() == AccountStatus.DELETED) {
            throw new ApiException(ErrorCode.ACCOUNT_DELETED, "Tài khoản này đã bị xóa hoặc đang chờ hủy.");
        }
        if (!session.emailVerified()) {
            throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED, "Tài khoản chưa xác minh địa chỉ email.");
        }
        return session.withDeviceId(clientDeviceId);
    }

    @Override
    public boolean isSessionStillActive(UUID sessionId) {
        if (sessionId == null) {
            return false;
        }
        return sessionRepository.findBySessionId(sessionId)
                .map(AuthenticatedSession::isActiveAccountAndSession)
                .orElse(false);
    }
}
