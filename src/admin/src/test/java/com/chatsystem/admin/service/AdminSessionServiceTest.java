package com.chatsystem.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.auth.AuthenticatedUserSummary;
import com.chatsystem.common.dto.auth.SessionInfo;
import com.chatsystem.desktop.client.ClientConfig;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class AdminSessionServiceTest {
    @Test
    void mockServiceRunsOfflineWithAdminAppScope() {
        AdminServiceRegistry mockRegistry = AdminServiceRegistry.mockDefault();
        assertFalse(mockRegistry.adminSessionService().isLiveBackend());
        SessionInfo session = mockRegistry.adminSessionService().fetchCurrentSession().join();
        assertEquals(SessionScope.ADMIN_APP, session.scope());
        assertEquals(UserRole.ADMIN, session.user().role());
    }

    @Test
    void httpServiceFetchesSessionAndClearsStoreOnLogout() throws IOException {
        JsonMapper jsonMapper = JsonMapper.builder().build();
        SessionInfo expected = new SessionInfo(
                UUID.randomUUID(),
                SessionScope.ADMIN_APP,
                Instant.parse("2026-11-28T04:00:00Z"),
                new AuthenticatedUserSummary(
                        UUID.randomUUID(), "sysadmin", "Admin", "admin@example.test",
                        UserRole.ADMIN, AccountStatus.ACTIVE, true
                )
        );
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/auth/me", exchange -> {
            byte[] bytes = jsonMapper.writeValueAsBytes(expected);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.createContext("/api/v1/auth/logout", exchange -> {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();
        try {
            ClientConfig config = new ClientConfig(
                    URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
                    Duration.ofSeconds(3),
                    Duration.ofSeconds(5),
                    null,
                    null
            );
            AdminServiceRegistry liveRegistry = AdminServiceRegistry.live(config);
            liveRegistry.sessionStore().updateSession("live-admin-token", expected);
            assertTrue(liveRegistry.adminSessionService().isLiveBackend());
            assertTrue(liveRegistry.sessionStore().isAuthenticated());

            SessionInfo fetched = liveRegistry.adminSessionService().fetchCurrentSession().join();
            assertEquals(expected, fetched);

            liveRegistry.adminSessionService().logout().join();
            assertFalse(liveRegistry.sessionStore().isAuthenticated());
        } finally {
            server.stop(0);
        }
    }
}
