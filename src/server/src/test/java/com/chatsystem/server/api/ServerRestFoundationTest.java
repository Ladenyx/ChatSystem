package com.chatsystem.server.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chatsystem.common.api.ApiError;
import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.auth.SessionInfo;
import com.chatsystem.common.dto.messaging.SyncEventsResponse;
import com.chatsystem.common.ws.WsEventType;
import com.chatsystem.server.auth.AuthenticatedSession;
import com.chatsystem.server.auth.SessionRepository;
import com.chatsystem.server.auth.SessionTokenHasher;
import com.chatsystem.server.ws.PresenceRepository;
import com.chatsystem.server.ws.UserEventLog;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServerRestFoundationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserEventLog userEventLog;

    @MockitoBean
    private DataSource dataSource;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private SessionRepository sessionRepository;

    @MockitoBean
    private PresenceRepository presenceRepository;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void healthEndpointChecksDatabaseAndPropagatesRequestId() throws Exception {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        UUID requestId = UUID.randomUUID();

        HttpResponse<String> ok = http.send(
                HttpRequest.newBuilder(uri("/api/v1/health"))
                        .header(ApiHeaders.REQUEST_ID, requestId.toString())
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(ok.statusCode()).isEqualTo(200);
        assertThat(ok.headers().firstValue(ApiHeaders.REQUEST_ID)).contains(requestId.toString());
        assertThat(ok.body()).contains("\"database\":\"UP\"");

        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new DataAccessResourceFailureException("db down"));
        HttpResponse<String> down = http.send(
                HttpRequest.newBuilder(uri("/api/v1/health")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(down.statusCode()).isEqualTo(503);
        ApiError error = objectMapper.readValue(down.body(), ApiError.class);
        assertThat(error.code()).isEqualTo(ErrorCode.SERVICE_UNAVAILABLE);
        assertThat(error.retryable()).isTrue();
    }

    @Test
    void authMeAndLogoutWorkWithValidTokenAndRejectRevokedOrLockedAccounts() throws Exception {
        String rawToken = "valid-user-token";
        String tokenHash = SessionTokenHasher.sha256Hex(rawToken);
        AuthenticatedSession session = activeSession(SessionScope.USER_CLIENT, UserRole.USER);
        when(sessionRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(session));
        when(sessionRepository.revokeSession(eq(session.sessionId()), any())).thenReturn(true);

        HttpResponse<String> meResponse = http.send(
                HttpRequest.newBuilder(uri("/api/v1/auth/me"))
                        .header(ApiHeaders.AUTHORIZATION, "Bearer " + rawToken)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(meResponse.statusCode()).isEqualTo(200);
        SessionInfo info = objectMapper.readValue(meResponse.body(), SessionInfo.class);
        assertThat(info.sessionId()).isEqualTo(session.sessionId());
        assertThat(info.scope()).isEqualTo(SessionScope.USER_CLIENT);

        HttpResponse<String> logoutResponse = http.send(
                HttpRequest.newBuilder(uri("/api/v1/auth/logout"))
                        .header(ApiHeaders.AUTHORIZATION, "Bearer " + rawToken)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(logoutResponse.statusCode()).isEqualTo(204);
        verify(sessionRepository).revokeSession(eq(session.sessionId()), any());

        String lockedHash = SessionTokenHasher.sha256Hex("locked-token");
        when(sessionRepository.findByTokenHash(lockedHash)).thenReturn(Optional.of(
                new AuthenticatedSession(
                        UUID.randomUUID(), UUID.randomUUID(), "locked", "Locked User",
                        "locked@example.test", UserRole.USER, AccountStatus.LOCKED,
                        true, SessionScope.USER_CLIENT, Instant.now(), null, null
                )
        ));
        HttpResponse<String> lockedResponse = http.send(
                HttpRequest.newBuilder(uri("/api/v1/auth/me"))
                        .header(ApiHeaders.AUTHORIZATION, "Bearer locked-token")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(lockedResponse.statusCode()).isEqualTo(403);
        assertThat(objectMapper.readValue(lockedResponse.body(), ApiError.class).code())
                .isEqualTo(ErrorCode.ACCOUNT_LOCKED);
    }

    @Test
    void syncEventsEnforcesUserClientScopeAndQueryValidation() throws Exception {
        String userToken = "sync-user-token";
        AuthenticatedSession userSession = activeSession(SessionScope.USER_CLIENT, UserRole.USER);
        when(sessionRepository.findByTokenHash(SessionTokenHasher.sha256Hex(userToken)))
                .thenReturn(Optional.of(userSession));
        userEventLog.append(userSession.userId(), WsEventType.MESSAGE_CREATED, null, Map.of("sample", "ok"));

        HttpResponse<String> ok = http.send(
                HttpRequest.newBuilder(uri("/api/v1/sync/events?afterSeq=0&limit=50"))
                        .header(ApiHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(ok.statusCode()).isEqualTo(200);
        SyncEventsResponse sync = objectMapper.readValue(ok.body(), SyncEventsResponse.class);
        assertThat(sync.events()).hasSize(1);
        assertThat(sync.latestSeq()).isEqualTo(1L);

        HttpResponse<String> badLimit = http.send(
                HttpRequest.newBuilder(uri("/api/v1/sync/events?afterSeq=0&limit=500"))
                        .header(ApiHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(badLimit.statusCode()).isEqualTo(400);
        assertThat(objectMapper.readValue(badLimit.body(), ApiError.class).code())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);

        String adminToken = "sync-admin-token";
        when(sessionRepository.findByTokenHash(SessionTokenHasher.sha256Hex(adminToken)))
                .thenReturn(Optional.of(activeSession(SessionScope.ADMIN_APP, UserRole.ADMIN)));
        HttpResponse<String> forbiddenAdmin = http.send(
                HttpRequest.newBuilder(uri("/api/v1/sync/events"))
                        .header(ApiHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(forbiddenAdmin.statusCode()).isEqualTo(403);
        assertThat(objectMapper.readValue(forbiddenAdmin.body(), ApiError.class).code())
                .isEqualTo(ErrorCode.SESSION_SCOPE_MISMATCH);
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }

    private static AuthenticatedSession activeSession(SessionScope scope, UserRole role) {
        return new AuthenticatedSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "tester",
                "Tester",
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
