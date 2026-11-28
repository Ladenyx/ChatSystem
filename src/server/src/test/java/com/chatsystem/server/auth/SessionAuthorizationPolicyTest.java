package com.chatsystem.server.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.server.api.ApiException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SessionAuthorizationPolicyTest {
    private final SessionAuthorizationPolicy policy = new SessionAuthorizationPolicy();

    @Test
    void identifiesPublicRoutesAccurately() {
        assertThat(policy.isPublic("GET", "/api/v1/health")).isTrue();
        assertThat(policy.isPublic("POST", "/api/v1/auth/login")).isTrue();
        assertThat(policy.isPublic("POST", "/api/v1/auth/registrations")).isTrue();
        assertThat(policy.isPublic("POST", "/api/v1/auth/registrations/123/verify")).isTrue();
        assertThat(policy.isPublic("POST", "/api/v1/auth/password-resets")).isTrue();
        assertThat(policy.isPublic("POST", "/api/v1/auth/force-password-change")).isTrue();

        assertThat(policy.isPublic("GET", "/api/v1/auth/me")).isFalse();
        assertThat(policy.isPublic("POST", "/api/v1/auth/logout")).isFalse();
        assertThat(policy.isPublic("GET", "/api/v1/admin/users")).isFalse();
        assertThat(policy.isPublic("GET", "/api/v1/sync/events")).isFalse();
    }

    @Test
    void allowsBothScopesOnMeAndLogout() {
        AuthenticatedSession userSession = session(SessionScope.USER_CLIENT, UserRole.USER);
        AuthenticatedSession adminSession = session(SessionScope.ADMIN_APP, UserRole.ADMIN);

        assertThatCode(() -> policy.authorize("/api/v1/auth/me", userSession)).doesNotThrowAnyException();
        assertThatCode(() -> policy.authorize("/api/v1/auth/logout", userSession)).doesNotThrowAnyException();
        assertThatCode(() -> policy.authorize("/api/v1/auth/me", adminSession)).doesNotThrowAnyException();
        assertThatCode(() -> policy.authorize("/api/v1/auth/logout", adminSession)).doesNotThrowAnyException();
    }

    @Test
    void enforcesStrictScopeSeparationBetweenUserClientAndAdminApp() {
        AuthenticatedSession adminActingAsUser = session(SessionScope.USER_CLIENT, UserRole.ADMIN);
        AuthenticatedSession adminAppSession = session(SessionScope.ADMIN_APP, UserRole.ADMIN);
        AuthenticatedSession nonAdminOnAdminScope = session(SessionScope.ADMIN_APP, UserRole.USER);

        assertThatThrownBy(() -> policy.authorize("/api/v1/admin/users", adminActingAsUser))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SESSION_SCOPE_MISMATCH);

        assertThatThrownBy(() -> policy.authorize("/api/v1/sync/events", adminAppSession))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SESSION_SCOPE_MISMATCH);

        assertThatThrownBy(() -> policy.authorize("/api/v1/admin/users", nonAdminOnAdminScope))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        assertThatCode(() -> policy.authorize("/api/v1/admin/users", adminAppSession)).doesNotThrowAnyException();
        assertThatCode(() -> policy.authorize("/api/v1/sync/events", adminActingAsUser)).doesNotThrowAnyException();
    }

    private static AuthenticatedSession session(SessionScope scope, UserRole role) {
        return new AuthenticatedSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "tester",
                "Test User",
                "tester@example.test",
                role,
                AccountStatus.ACTIVE,
                true,
                scope,
                Instant.parse("2026-11-28T01:00:00Z"),
                null,
                null
        );
    }
}
