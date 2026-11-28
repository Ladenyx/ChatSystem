package com.chatsystem.desktop.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chatsystem.common.api.ApiError;
import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.api.OffsetPage;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.admin.AdminUserListItemDto;
import com.chatsystem.common.dto.auth.AuthenticatedUserSummary;
import com.chatsystem.common.dto.auth.SessionInfo;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class RestApiClientTest {
    private HttpServer server;
    private RestApiClient client;
    private ClientSessionStore sessionStore;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        ClientConfig config = new ClientConfig(
                URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
                Duration.ofSeconds(3),
                Duration.ofSeconds(5),
                null,
                null
        );
        sessionStore = new ClientSessionStore();
        client = new RestApiClient(config, sessionStore);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void sendsAuthAndTracingHeadersAndParsesSessionInfoAndApiErrors() {
        SessionInfo expected = new SessionInfo(
                UUID.randomUUID(),
                SessionScope.ADMIN_APP,
                Instant.parse("2026-11-28T03:00:00Z"),
                new AuthenticatedUserSummary(
                        UUID.randomUUID(), "sysadmin", "Admin", "admin@example.test",
                        UserRole.ADMIN, AccountStatus.ACTIVE, true
                )
        );
        UUID deviceId = UUID.randomUUID();
        sessionStore.updateSession("token-123", expected);
        sessionStore.setDeviceId(deviceId);

        server.createContext("/api/v1/auth/me", exchange -> {
            assertEquals("Bearer token-123", exchange.getRequestHeaders().getFirst(ApiHeaders.AUTHORIZATION));
            assertEquals(deviceId.toString(), exchange.getRequestHeaders().getFirst(ApiHeaders.CLIENT_DEVICE_ID));
            assertNotNull(exchange.getRequestHeaders().getFirst(ApiHeaders.REQUEST_ID));
            byte[] bytes = jsonMapper.writeValueAsBytes(expected);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        server.createContext("/api/v1/auth/logout", exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });

        server.createContext("/api/v1/fail", exchange -> {
            ApiError error = ApiError.of(
                    ErrorCode.SESSION_REVOKED,
                    "Phiên đã bị thu hồi.",
                    Instant.parse("2026-11-28T03:01:00Z"),
                    "/api/v1/fail",
                    UUID.randomUUID().toString()
            );
            byte[] bytes = jsonMapper.writeValueAsBytes(error);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(401, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        SessionInfo actual = client.getAsync("/api/v1/auth/me", SessionInfo.class).join();
        assertEquals(expected, actual);

        assertNull(client.postEmptyAsync("/api/v1/auth/logout", null).join());

        CompletionException failure = assertThrows(
                CompletionException.class,
                () -> client.getAsync("/api/v1/fail", SessionInfo.class).join()
        );
        assertTrue(failure.getCause() instanceof ApiClientException);
        ApiClientException apiEx = (ApiClientException) failure.getCause();
        assertEquals(401, apiEx.httpStatus());
        assertEquals(ErrorCode.SESSION_REVOKED, apiEx.apiError().orElseThrow().code());
    }

    @Test
    void deserializesParameterizedTypesViaTypeReference() {
        OffsetPage<AdminUserListItemDto> expectedPage = OffsetPage.of(
                List.of(new AdminUserListItemDto(
                        UUID.randomUUID(),
                        "minhan",
                        "Nguyễn Minh An",
                        UserRole.USER,
                        AccountStatus.ACTIVE,
                        Instant.parse("2026-11-28T03:00:00Z")
                )),
                0,
                25,
                1
        );
        server.createContext("/api/v1/admin/users", exchange -> {
            byte[] bytes = jsonMapper.writeValueAsBytes(expectedPage);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        OffsetPage<AdminUserListItemDto> actual = client.getAsync(
                "/api/v1/admin/users?page=0&size=25",
                new TypeReference<OffsetPage<AdminUserListItemDto>>() {}
        ).join();
        assertEquals(expectedPage, actual);
    }
}
