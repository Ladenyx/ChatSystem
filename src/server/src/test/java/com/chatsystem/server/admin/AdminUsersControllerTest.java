package com.chatsystem.server.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.chatsystem.common.api.ApiError;
import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.api.OffsetPage;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.admin.AdminUserListItemDto;
import com.chatsystem.common.dto.auth.LoginRequest;
import com.chatsystem.common.dto.auth.LoginResponse;
import com.chatsystem.server.auth.AuthAccountRecord;
import com.chatsystem.server.auth.AuthAccountRepository;
import com.chatsystem.server.auth.AuthenticatedSession;
import com.chatsystem.server.auth.PasswordHasher;
import com.chatsystem.server.auth.SessionRepository;
import com.chatsystem.server.auth.SessionTokenHasher;
import com.chatsystem.server.ws.PresenceRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminUsersControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordHasher passwordHasher;

    @MockitoBean
    private DataSource dataSource;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private SessionRepository sessionRepository;

    @MockitoBean
    private PresenceRepository presenceRepository;

    @MockitoBean
    private AuthAccountRepository authAccountRepository;

    @MockitoBean
    private AdminUserRepository adminUserRepository;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void loginAndAdminUsersEndpointEnforceScopeAndReturnOffsetPage() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(authAccountRepository.findByUsernameIgnoreCase("sysadmin")).thenReturn(Optional.of(
                new AuthAccountRecord(
                        adminId, "sysadmin", "Quản Trị Hệ Thống", "sysadmin@whatsup.local",
                        UserRole.ADMIN, AccountStatus.ACTIVE, passwordHasher.hash("Admin@123"), true
                )
        ));
        when(authAccountRepository.insertSession(any(), any(), any(), any())).thenReturn(sessionId);

        String loginJson = objectMapper.writeValueAsString(
                new LoginRequest("sysadmin", "Admin@123", SessionScope.ADMIN_APP, null)
        );
        HttpResponse<String> loginRes = http.send(
                HttpRequest.newBuilder(uri("/api/v1/auth/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(loginRes.statusCode()).isEqualTo(200);
        LoginResponse login = objectMapper.readValue(loginRes.body(), LoginResponse.class);
        assertThat(login.sessionToken()).isNotBlank();

        String adminToken = "admin-token-a01";
        when(sessionRepository.findByTokenHash(SessionTokenHasher.sha256Hex(adminToken))).thenReturn(Optional.of(
                new AuthenticatedSession(
                        sessionId, adminId, "sysadmin", "Quản Trị Hệ Thống", "sysadmin@whatsup.local",
                        UserRole.ADMIN, AccountStatus.ACTIVE, true, SessionScope.ADMIN_APP,
                        Instant.parse("2026-11-28T05:00:00Z"), null, null
                )
        ));
        OffsetPage<AdminUserListItemDto> expectedPage = OffsetPage.of(
                List.of(new AdminUserListItemDto(
                        UUID.randomUUID(), "user01", "Nguyễn Minh An",
                        UserRole.USER, AccountStatus.ACTIVE, Instant.parse("2026-11-20T03:00:00Z")
                )),
                0,
                25,
                1
        );
        when(adminUserRepository.findUsers(any())).thenReturn(expectedPage);

        HttpResponse<String> ok = http.send(
                HttpRequest.newBuilder(uri("/api/v1/admin/users?name=An&status=ACTIVE&sortBy=fullName&sortDir=ASC&page=0&size=25"))
                        .header(ApiHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(ok.statusCode()).isEqualTo(200);
        OffsetPage<AdminUserListItemDto> actual = objectMapper.readValue(
                ok.body(),
                new TypeReference<OffsetPage<AdminUserListItemDto>>() {}
        );
        assertThat(actual).isEqualTo(expectedPage);

        HttpResponse<String> invalid = http.send(
                HttpRequest.newBuilder(uri("/api/v1/admin/users?status=BOGUS"))
                        .header(ApiHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(invalid.statusCode()).isEqualTo(400);
        assertThat(objectMapper.readValue(invalid.body(), ApiError.class).code()).isEqualTo(ErrorCode.VALIDATION_ERROR);

        String userToken = "user-token-a01";
        when(sessionRepository.findByTokenHash(SessionTokenHasher.sha256Hex(userToken))).thenReturn(Optional.of(
                new AuthenticatedSession(
                        UUID.randomUUID(), UUID.randomUUID(), "user01", "Minh An", "a@b.local",
                        UserRole.USER, AccountStatus.ACTIVE, true, SessionScope.USER_CLIENT,
                        Instant.parse("2026-11-28T05:00:00Z"), null, null
                )
        ));
        HttpResponse<String> forbidden = http.send(
                HttpRequest.newBuilder(uri("/api/v1/admin/users"))
                        .header(ApiHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(forbidden.statusCode()).isEqualTo(403);
        assertThat(objectMapper.readValue(forbidden.body(), ApiError.class).code())
                .isEqualTo(ErrorCode.SESSION_SCOPE_MISMATCH);

        HttpResponse<String> emptyLogin = http.send(
                HttpRequest.newBuilder(uri("/api/v1/auth/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(emptyLogin.statusCode()).isEqualTo(400);
        assertThat(objectMapper.readValue(emptyLogin.body(), ApiError.class).code())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }
}
