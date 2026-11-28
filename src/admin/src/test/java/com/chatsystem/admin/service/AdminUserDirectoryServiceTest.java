package com.chatsystem.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chatsystem.common.api.OffsetPage;
import com.chatsystem.common.api.SortDirection;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.admin.AdminUserListItemDto;
import com.chatsystem.desktop.client.ClientConfig;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class AdminUserDirectoryServiceTest {
    @Test
    void mockDirectoryFiltersAndSortsUsersDeterministically() {
        MockAdminUserDirectoryService service = new MockAdminUserDirectoryService();
        assertFalse(service.isLiveBackend());

        OffsetPage<AdminUserListItemDto> active = service.listUsers(new AdminUserFilter(
                "nguyễn",
                "minhan",
                AccountStatus.ACTIVE,
                "fullName",
                SortDirection.ASC,
                0,
                25
        )).join();

        assertEquals(1, active.totalItems());
        assertEquals("minhan", active.items().getFirst().username());
    }

    @Test
    void httpDirectoryEncodesQueryParametersAndParsesOffsetPage() throws IOException {
        JsonMapper jsonMapper = JsonMapper.builder().build();
        OffsetPage<AdminUserListItemDto> expected = OffsetPage.of(
                List.of(new AdminUserListItemDto(
                        UUID.randomUUID(),
                        "user01",
                        "Nguyễn Minh An",
                        UserRole.USER,
                        AccountStatus.ACTIVE,
                        Instant.parse("2026-11-28T03:00:00Z")
                )),
                0,
                25,
                1
        );
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/admin/users", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            assertTrue(query.contains("sortBy=createdAt"));
            assertTrue(query.contains("sortDir=DESC"));
            assertTrue(query.contains("status=ACTIVE"));
            assertTrue(query.contains("name="));
            byte[] bytes = jsonMapper.writeValueAsBytes(expected);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
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
            AdminServiceRegistry registry = AdminServiceRegistry.live(config);
            assertTrue(registry.adminUserDirectoryService().isLiveBackend());

            OffsetPage<AdminUserListItemDto> actual = registry.adminUserDirectoryService().listUsers(
                    new AdminUserFilter("Minh An", null, AccountStatus.ACTIVE, "createdAt", SortDirection.DESC, 0, 25)
            ).join();
            assertEquals(expected, actual);
        } finally {
            server.stop(0);
        }
    }
}
