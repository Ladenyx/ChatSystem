package com.chatsystem.server.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.auth.AuthNextAction;
import com.chatsystem.common.dto.auth.LoginRequest;
import com.chatsystem.common.dto.auth.LoginResponse;
import com.chatsystem.server.api.ApiException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthLoginServiceTest {
    private static final Instant FIXED_NOW = Instant.parse("2026-11-28T16:00:00Z");

    private AuthAccountRepository accountRepository;
    private SessionRepository sessionRepository;
    private Pbkdf2PasswordHasher passwordHasher;
    private AuthLoginService service;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AuthAccountRepository.class);
        sessionRepository = mock(SessionRepository.class);
        passwordHasher = new Pbkdf2PasswordHasher();
        service = new AuthLoginService(
                accountRepository,
                sessionRepository,
                passwordHasher,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void adminLoginRevokesPreviousAdminSessionAndGrantsNewSession() {
        UUID adminId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        AuthAccountRecord admin = new AuthAccountRecord(
                adminId, "sysadmin", "Quản Trị Hệ Thống", "sysadmin@whatsup.local",
                UserRole.ADMIN, AccountStatus.ACTIVE, passwordHasher.hash("Admin@123"), true
        );
        when(accountRepository.findByUsernameIgnoreCase("sysadmin")).thenReturn(Optional.of(admin));
        when(sessionRepository.revokeActiveAdminSessions(adminId, FIXED_NOW, null)).thenReturn(List.of());
        when(accountRepository.insertSession(eq(adminId), eq(SessionScope.ADMIN_APP), any(), eq(FIXED_NOW)))
                .thenReturn(sessionId);

        LoginResponse response = service.login(new LoginRequest(" sysadmin ", "Admin@123", SessionScope.ADMIN_APP, null));

        assertThat(response.nextAction()).isEqualTo(AuthNextAction.SESSION_GRANTED);
        assertThat(response.sessionToken()).isNotBlank();
        assertThat(response.session().sessionId()).isEqualTo(sessionId);
        assertThat(response.session().scope()).isEqualTo(SessionScope.ADMIN_APP);
        verify(sessionRepository).revokeActiveAdminSessions(adminId, FIXED_NOW, null);
        verify(accountRepository).insertLoginLog(adminId, SessionScope.ADMIN_APP, FIXED_NOW);
        verify(accountRepository, never()).recordAppOpen(any(), any(), any());
    }

    @Test
    void userLoginRecordsAppOpenWithoutRevokingAdminSessions() {
        UUID userId = UUID.randomUUID();
        UUID launchId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        AuthAccountRecord user = new AuthAccountRecord(
                userId, "user01", "Nguyễn Minh An", "minhan@whatsup.local",
                UserRole.USER, AccountStatus.ACTIVE, passwordHasher.hash("User@123"), true
        );
        when(accountRepository.findByUsernameIgnoreCase("user01")).thenReturn(Optional.of(user));
        when(accountRepository.insertSession(eq(userId), eq(SessionScope.USER_CLIENT), any(), eq(FIXED_NOW)))
                .thenReturn(sessionId);

        LoginResponse response = service.login(new LoginRequest("user01", "User@123", SessionScope.USER_CLIENT, launchId));

        assertThat(response.nextAction()).isEqualTo(AuthNextAction.SESSION_GRANTED);
        verify(sessionRepository, never()).revokeActiveAdminSessions(any(), any(), any());
        verify(accountRepository).insertLoginLog(userId, SessionScope.USER_CLIENT, FIXED_NOW);
        verify(accountRepository).recordAppOpen(userId, launchId, SessionScope.USER_CLIENT);
    }

    @Test
    void rejectsNonAdminLockedDeletedAndWrongPasswordAccounts() {
        UUID id = UUID.randomUUID();
        String hash = passwordHasher.hash("Secret@123");

        when(accountRepository.findByUsernameIgnoreCase("user01")).thenReturn(Optional.of(
                new AuthAccountRecord(id, "user01", "Minh An", "a@b.local", UserRole.USER, AccountStatus.ACTIVE, hash, true)
        ));
        assertThatThrownBy(() -> service.login(new LoginRequest("user01", "Wrong", SessionScope.USER_CLIENT, UUID.randomUUID())))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);

        assertThatThrownBy(() -> service.login(new LoginRequest("user01", "Secret@123", SessionScope.ADMIN_APP, null)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        when(accountRepository.findByUsernameIgnoreCase("locked01")).thenReturn(Optional.of(
                new AuthAccountRecord(id, "locked01", "Quốc Bảo", "l@b.local", UserRole.USER, AccountStatus.LOCKED, hash, true)
        ));
        assertThatThrownBy(() -> service.login(new LoginRequest("locked01", "Secret@123", SessionScope.USER_CLIENT, UUID.randomUUID())))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_LOCKED);

        when(accountRepository.findByUsernameIgnoreCase("deleted01")).thenReturn(Optional.of(
                new AuthAccountRecord(id, "deleted01", "Hải Đăng", "d@b.local", UserRole.USER, AccountStatus.DELETED, hash, true)
        ));
        assertThatThrownBy(() -> service.login(new LoginRequest("deleted01", "Secret@123", SessionScope.USER_CLIENT, UUID.randomUUID())))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_DELETED);
    }
}
