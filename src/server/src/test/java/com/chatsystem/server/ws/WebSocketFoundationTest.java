package com.chatsystem.server.ws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.chatsystem.common.api.ApiError;
import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.ws.SessionRevocationReason;
import com.chatsystem.common.ws.WsCloseCode;
import com.chatsystem.common.ws.WsCommand;
import com.chatsystem.common.ws.WsEvent;
import com.chatsystem.common.ws.WsEventType;
import com.chatsystem.common.ws.WsSyncResumePayload;
import com.chatsystem.server.auth.AuthenticatedSession;
import com.chatsystem.server.auth.SessionRepository;
import com.chatsystem.server.auth.SessionRevocationService;
import com.chatsystem.server.auth.SessionTokenHasher;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocketHandshakeException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"chatsystem.ws.presence-offline-grace-sec=0"}
)
class WebSocketFoundationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserEventLog userEventLog;

    @Autowired
    private SessionRevocationService revocationService;

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
    void rejectsMissingBearerTokenDuringHandshake() {
        CompletableFuture<WebSocket> future = http.newWebSocketBuilder()
                .buildAsync(wsUri(), new RecordingListener());
        assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(WebSocketHandshakeException.class);
    }

    @Test
    void connectsHandlesPingSyncErrorAndSessionRevocation() throws Exception {
        String userToken = "ws-user-token";
        AuthenticatedSession userSession = new AuthenticatedSession(
                UUID.randomUUID(), UUID.randomUUID(), "alice", "Alice",
                "alice@example.test", UserRole.USER, AccountStatus.ACTIVE,
                true, SessionScope.USER_CLIENT, Instant.now(), null, null
        );
        when(sessionRepository.findByTokenHash(SessionTokenHasher.sha256Hex(userToken)))
                .thenReturn(Optional.of(userSession));
        when(sessionRepository.findBySessionId(userSession.sessionId()))
                .thenReturn(Optional.of(userSession));
        when(presenceRepository.findEligibleContactIds(userSession.userId())).thenReturn(List.of());
        userEventLog.append(userSession.userId(), WsEventType.MESSAGE_CREATED, null, Map.of("hello", "world"));

        RecordingListener userListener = new RecordingListener();
        WebSocket ws = http.newWebSocketBuilder()
                .header(ApiHeaders.AUTHORIZATION, "Bearer " + userToken)
                .buildAsync(wsUri(), userListener)
                .get(5, TimeUnit.SECONDS);

        WsEvent<JsonNode> connected = userListener.awaitEvent(WsEventType.SYSTEM_CONNECTED);
        assertThat(connected.payload().get("latestSeq").asLong()).isEqualTo(1L);
        assertThat(connected.payload().get("heartbeatIntervalSec").asInt()).isEqualTo(25);

        WsEvent<JsonNode> snapshot = userListener.awaitEvent(WsEventType.PRESENCE_SNAPSHOT);
        assertThat(snapshot.payload().get("onlineUserIds")).isEmpty();

        UUID pingReq = UUID.randomUUID();
        ws.sendText(objectMapper.writeValueAsString(
                WsCommand.of(WsEventType.SYSTEM_PING, pingReq, Instant.now(), Map.of())
        ), true).get(5, TimeUnit.SECONDS);
        WsEvent<JsonNode> pong = userListener.awaitEvent(WsEventType.SYSTEM_PONG);
        assertThat(pong.requestId()).isEqualTo(pingReq);

        UUID syncReq = UUID.randomUUID();
        ws.sendText(objectMapper.writeValueAsString(
                WsCommand.of(WsEventType.SYNC_RESUME, syncReq, Instant.now(), new WsSyncResumePayload(0L, 50))
        ), true).get(5, TimeUnit.SECONDS);
        WsEvent<JsonNode> batch = userListener.awaitEvent(WsEventType.SYNC_BATCH);
        assertThat(batch.requestId()).isEqualTo(syncReq);
        assertThat(batch.payload().get("events")).hasSize(1);

        UUID invalidReq = UUID.randomUUID();
        ws.sendText(objectMapper.writeValueAsString(
                WsCommand.of(WsEventType.MESSAGE_SEND, invalidReq, Instant.now(), Map.of("msg", "hi"))
        ), true).get(5, TimeUnit.SECONDS);
        WsEvent<JsonNode> sysErr = userListener.awaitEvent(WsEventType.SYSTEM_ERROR);
        assertThat(sysErr.requestId()).isEqualTo(invalidReq);
        ApiError apiError = objectMapper.treeToValue(sysErr.payload(), ApiError.class);
        assertThat(apiError.code()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        ws.sendClose(WsCloseCode.NORMAL_CLOSURE.code(), "done").get(5, TimeUnit.SECONDS);

        // Verify live Admin session revocation pushes session.revoked and closes with 4004
        String adminToken = "ws-admin-token";
        AuthenticatedSession adminSession = new AuthenticatedSession(
                UUID.randomUUID(), UUID.randomUUID(), "sysadmin", "Admin",
                "admin@example.test", UserRole.ADMIN, AccountStatus.ACTIVE,
                true, SessionScope.ADMIN_APP, Instant.now(), null, null
        );
        when(sessionRepository.findByTokenHash(SessionTokenHasher.sha256Hex(adminToken)))
                .thenReturn(Optional.of(adminSession));
        when(sessionRepository.findBySessionId(adminSession.sessionId()))
                .thenReturn(Optional.of(adminSession));
        when(sessionRepository.revokeActiveAdminSessions(eq(adminSession.userId()), any(), eq(null)))
                .thenReturn(List.of(adminSession.sessionId()));

        RecordingListener adminListener = new RecordingListener();
        http.newWebSocketBuilder()
                .header(ApiHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .buildAsync(wsUri(), adminListener)
                .get(5, TimeUnit.SECONDS);
        adminListener.awaitEvent(WsEventType.SYSTEM_CONNECTED);

        revocationService.revokeActiveAdminSessions(
                adminSession.userId(),
                SessionRevocationReason.ADMIN_NEW_LOGIN,
                "Phiên Admin đã đăng nhập ở thiết bị khác.",
                null
        );

        WsEvent<JsonNode> revoked = adminListener.awaitEvent(WsEventType.SESSION_REVOKED);
        assertThat(revoked.payload().get("reason").asString()).isEqualTo("ADMIN_NEW_LOGIN");
        assertThat(adminListener.closedCode.get(5, TimeUnit.SECONDS)).isEqualTo(WsCloseCode.ADMIN_SESSION_REPLACED.code());
    }

    private URI wsUri() {
        return URI.create("ws://127.0.0.1:" + port + ApiHeaders.WS_PATH);
    }

    private final class RecordingListener implements WebSocket.Listener {
        private final BlockingQueue<WsEvent<JsonNode>> events = new LinkedBlockingQueue<>();
        private final CompletableFuture<Integer> closedCode = new CompletableFuture<>();
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                WsEvent<JsonNode> ev = objectMapper.readValue(buffer.toString(), new TypeReference<>() {
                });
                buffer.setLength(0);
                events.add(ev);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            closedCode.complete(statusCode);
            return null;
        }

        private WsEvent<JsonNode> awaitEvent(WsEventType expectedType) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (System.nanoTime() < deadline) {
                WsEvent<JsonNode> ev = events.poll(500, TimeUnit.MILLISECONDS);
                if (ev != null && ev.eventType() == expectedType) {
                    return ev;
                }
            }
            throw new AssertionError("Timed out waiting for WS event " + expectedType);
        }
    }
}
