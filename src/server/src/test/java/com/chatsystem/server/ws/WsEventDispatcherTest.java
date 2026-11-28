package com.chatsystem.server.ws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.ws.WsCloseCode;
import com.chatsystem.common.ws.WsEventType;
import com.chatsystem.server.auth.AuthenticatedSession;
import com.chatsystem.server.auth.SessionAuthenticator;
import com.chatsystem.server.config.ChatServerProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class WsEventDispatcherTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-28T07:00:00Z"), ZoneOffset.UTC);
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void doesNotLogOrSendSequencedEventWhenPermissionCheckFails() throws Exception {
        UUID userId = UUID.randomUUID();
        SessionAuthenticator authenticator = mock(SessionAuthenticator.class);
        WsSessionRegistry registry = new WsSessionRegistry(objectMapper, clock);
        UserEventLog eventLog = new UserEventLog(defaultProps(), clock);
        WsEventDispatcher dispatcher = new WsEventDispatcher(registry, authenticator, eventLog, objectMapper, clock);

        WebSocketSession socket = mock(WebSocketSession.class);
        when(socket.getId()).thenReturn("ws-denied");
        when(socket.isOpen()).thenReturn(true);
        AuthenticatedSession session = session(userId);
        when(authenticator.isSessionStillActive(session.sessionId())).thenReturn(true);
        registry.register(socket, session);

        Optional<?> sent = dispatcher.sendToUser(
                userId,
                WsEventType.MESSAGE_CREATED,
                null,
                Map.of("body", "secret"),
                () -> false
        );

        assertThat(sent).isEmpty();
        assertThat(eventLog.latestSeq(userId)).isZero();
        verify(socket, never()).sendMessage(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void revokesAndClosesInactiveSessionDuringUserFanOut() throws Exception {
        UUID userId = UUID.randomUUID();
        SessionAuthenticator authenticator = mock(SessionAuthenticator.class);
        WsSessionRegistry registry = new WsSessionRegistry(objectMapper, clock);
        UserEventLog eventLog = new UserEventLog(defaultProps(), clock);
        WsEventDispatcher dispatcher = new WsEventDispatcher(registry, authenticator, eventLog, objectMapper, clock);

        WebSocketSession socket = mock(WebSocketSession.class);
        when(socket.getId()).thenReturn("ws-stale");
        when(socket.isOpen()).thenReturn(true);
        AuthenticatedSession session = session(userId);
        when(authenticator.isSessionStillActive(session.sessionId())).thenReturn(false);
        registry.register(socket, session);

        dispatcher.sendToUser(
                userId,
                WsEventType.MESSAGE_CREATED,
                null,
                Map.of("body", "hello"),
                () -> true
        );

        verify(socket).sendMessage(argThat((TextMessage msg) -> msg.getPayload().contains("session.revoked")));
        verify(socket, never()).sendMessage(argThat((TextMessage msg) -> msg.getPayload().contains("message.created")));
        verify(socket).close(argThat((CloseStatus status) -> status.getCode() == WsCloseCode.SESSION_REVOKED.code()));
    }

    private static ChatServerProperties defaultProps() {
        return new ChatServerProperties(
                new ChatServerProperties.WsProperties(25, 60, 10, 50, List.of("*")),
                null,
                null
        );
    }

    private static AuthenticatedSession session(UUID userId) {
        return new AuthenticatedSession(
                UUID.randomUUID(),
                userId,
                "alice",
                "Alice",
                "alice@example.test",
                UserRole.USER,
                AccountStatus.ACTIVE,
                true,
                SessionScope.USER_CLIENT,
                Instant.parse("2026-11-28T06:00:00Z"),
                null,
                null
        );
    }
}
