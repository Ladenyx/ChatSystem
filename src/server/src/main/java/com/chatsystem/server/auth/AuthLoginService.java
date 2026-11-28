package com.chatsystem.server.auth;

import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.auth.AuthenticatedUserSummary;
import com.chatsystem.common.dto.auth.LoginRequest;
import com.chatsystem.common.dto.auth.LoginResponse;
import com.chatsystem.common.dto.auth.SessionInfo;
import com.chatsystem.server.api.ApiException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Authenticates user/admin credentials against PostgreSQL and issues opaque session tokens. */
@Service
public class AuthLoginService {
    private static final int TOKEN_BYTES = 32;

    private final AuthAccountRepository accountRepository;
    private final SessionRepository sessionRepository;
    private final PasswordHasher passwordHasher;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public AuthLoginService(
            AuthAccountRepository accountRepository,
            SessionRepository sessionRepository,
            PasswordHasher passwordHasher,
            Clock clock
    ) {
        this.accountRepository = Objects.requireNonNull(accountRepository, "accountRepository");
        this.sessionRepository = Objects.requireNonNull(sessionRepository, "sessionRepository");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        if (request == null
                || request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isEmpty()
                || request.scope() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Thiếu thông tin đăng nhập bắt buộc.");
        }
        AuthAccountRecord account = accountRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new ApiException(
                        ErrorCode.UNAUTHORIZED,
                        "Tên đăng nhập hoặc mật khẩu không đúng."
                ));
        if (!passwordHasher.matches(request.password(), account.passwordHash())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng.");
        }
        validateAccountEligible(account, request.scope());

        Instant now = clock.instant();
        if (request.scope() == SessionScope.ADMIN_APP) {
            // user_sessions_one_live_admin_uq permits at most one active ADMIN_APP session per account.
            sessionRepository.revokeActiveAdminSessions(account.id(), now, null);
        }

        String rawToken = generateOpaqueToken();
        String tokenHash = SessionTokenHasher.sha256Hex(rawToken);
        UUID sessionId = accountRepository.insertSession(account.id(), request.scope(), tokenHash, now);
        accountRepository.insertLoginLog(account.id(), request.scope(), now);
        if (request.scope() == SessionScope.USER_CLIENT && request.launchId() != null) {
            accountRepository.recordAppOpen(account.id(), request.launchId(), request.scope());
        }

        SessionInfo sessionInfo = new SessionInfo(
                sessionId,
                request.scope(),
                now,
                new AuthenticatedUserSummary(
                        account.id(),
                        account.username(),
                        account.fullName(),
                        account.email(),
                        account.role(),
                        account.status(),
                        account.emailVerified()
                )
        );
        return LoginResponse.sessionGranted(rawToken, sessionInfo);
    }

    private static void validateAccountEligible(AuthAccountRecord account, SessionScope scope) {
        if (account.status() == AccountStatus.LOCKED) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED, "Tài khoản của bạn hiện đang bị khóa.");
        }
        if (account.status() == AccountStatus.DELETED) {
            throw new ApiException(ErrorCode.ACCOUNT_DELETED, "Tài khoản này đã bị xóa hoặc đang chờ hủy.");
        }
        if (!account.emailVerified()) {
            throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED, "Tài khoản chưa xác minh địa chỉ email.");
        }
        if (scope == SessionScope.ADMIN_APP && account.role() != UserRole.ADMIN) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Tài khoản không có quyền quản trị viên.");
        }
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
