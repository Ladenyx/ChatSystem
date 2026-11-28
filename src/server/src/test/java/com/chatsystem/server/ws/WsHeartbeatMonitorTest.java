package com.chatsystem.server.ws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.ws.WsCloseCode;
import com.chatsystem.server.auth.AuthenticatedSession;
import com.chatsystem.server.config.ChatServerProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.json.JsonMapper;

class WsHeartbeatMonitorTest {
    @Test
    void closesOnlyConnectionsIdleBeyondHeartbeatTimeout() throws Exception {
        Instant now = Instant.parse("2026-11-28T08:00:00Z");
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        WsSessionRegistry registry = new WsSessionRegistry(JsonMapper.builder().build(), clock);
        ChatServerProperties props = new ChatServerProperties(
                new ChatServerProperties.WsProperties(25, 60, 10, 50, List.of("*")),
                null,
                null
        );
        WsHeartbeatMonitor monitor = new WsHeartbeatMonitor(registry, props, clock);

        WebSocketSession staleSocket = mockSocket("ws-stale");
        WsConnection stale = registry.register(staleSocket, session());
        stale.touch(now.minusSeconds(61));

        WebSocketSession freshSocket = mockSocket("ws-fresh");
        WsConnection fresh = registry.register(freshSocket, session());
        fresh.touch(now.minusSeconds(10));

        monitor.sweepIdleConnections();

        org.mockito.Mockito.verify(staleSocket)
                .close(org.mockito.ArgumentMatchers.argThat(
                        (CloseStatus status) -> status.getCode() == WsCloseCode.HEARTBEAT_TIMEOUT.code()
                ));
        org.mockito.Mockito.verify(freshSocket, org.mockito.Mockito.never()).close(org.mockito.ArgumentMatchers.any());
        assertThat(stale.lastActivityAt()).isBefore(fresh.lastActivityAt());
    }

    private static WebSocketSession mockSocket(String id) {
        WebSocketSession socket = mock(WebSocketSession.class);
        when(socket.getId()).thenReturn(id);
        when(socket.isOpen()).thenReturn(true);
        return socket;
    }

    private static AuthenticatedSession session() {
        return new AuthenticatedSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "tester",
                "Tester",
                "tester@example.test",
                UserRole.USER,
                AccountStatus.ACTIVE,
                true,
                SessionScope.USER_CLIENT,
                Instant.parse("2026-11-28T07:00:00Z"),
                null,
                null
        );
    }
}
