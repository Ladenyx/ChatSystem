package com.chatsystem.server.ws;

import com.chatsystem.common.ws.WsCloseCode;
import com.chatsystem.server.config.ChatServerProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Closes idle WebSocket connections that exceed the configured heartbeat timeout (default 60s). */
@Component
public class WsHeartbeatMonitor {
    private final WsSessionRegistry sessionRegistry;
    private final Duration heartbeatTimeout;
    private final Clock clock;

    public WsHeartbeatMonitor(WsSessionRegistry sessionRegistry, ChatServerProperties properties, Clock clock) {
        this.sessionRegistry = Objects.requireNonNull(sessionRegistry, "sessionRegistry");
        this.heartbeatTimeout = Duration.ofSeconds(
                Objects.requireNonNull(properties, "properties").ws().heartbeatTimeoutSec()
        );
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Scheduled(fixedDelay = 5_000L)
    public void sweepIdleConnections() {
        Instant cutoff = clock.instant().minus(heartbeatTimeout);
        for (WsConnection connection : sessionRegistry.allConnections()) {
            if (connection.lastActivityAt().isBefore(cutoff)) {
                connection.close(WsCloseCode.HEARTBEAT_TIMEOUT, "Heartbeat timeout");
            }
        }
    }
}
