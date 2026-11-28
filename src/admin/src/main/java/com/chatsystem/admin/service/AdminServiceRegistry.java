package com.chatsystem.admin.service;

import com.chatsystem.desktop.client.ClientConfig;
import com.chatsystem.desktop.client.ClientSessionStore;
import com.chatsystem.desktop.client.RestApiClient;
import java.util.Objects;

/** Coordinates Admin App client transport dependencies and service implementations. */
public final class AdminServiceRegistry {
    private final ClientConfig config;
    private final ClientSessionStore sessionStore;
    private final RestApiClient restApiClient;
    private final AdminSessionService adminSessionService;
    private final AdminUserDirectoryService adminUserDirectoryService;

    private AdminServiceRegistry(
            ClientConfig config,
            ClientSessionStore sessionStore,
            RestApiClient restApiClient,
            AdminSessionService adminSessionService,
            AdminUserDirectoryService adminUserDirectoryService
    ) {
        this.config = Objects.requireNonNull(config, "config");
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.restApiClient = Objects.requireNonNull(restApiClient, "restApiClient");
        this.adminSessionService = Objects.requireNonNull(adminSessionService, "adminSessionService");
        this.adminUserDirectoryService = Objects.requireNonNull(adminUserDirectoryService, "adminUserDirectoryService");
    }

    public static AdminServiceRegistry mockDefault() {
        ClientConfig config = ClientConfig.defaults();
        ClientSessionStore sessionStore = new ClientSessionStore();
        RestApiClient restApiClient = new RestApiClient(config, sessionStore);
        return new AdminServiceRegistry(
                config,
                sessionStore,
                restApiClient,
                new MockAdminSessionService(),
                new MockAdminUserDirectoryService()
        );
    }

    public static AdminServiceRegistry live(ClientConfig config) {
        Objects.requireNonNull(config, "config");
        ClientSessionStore sessionStore = new ClientSessionStore();
        RestApiClient restApiClient = new RestApiClient(config, sessionStore);
        return new AdminServiceRegistry(
                config,
                sessionStore,
                restApiClient,
                new HttpAdminSessionService(restApiClient, sessionStore),
                new HttpAdminUserDirectoryService(restApiClient)
        );
    }

    public ClientConfig config() {
        return config;
    }

    public ClientSessionStore sessionStore() {
        return sessionStore;
    }

    public RestApiClient restApiClient() {
        return restApiClient;
    }

    public AdminSessionService adminSessionService() {
        return adminSessionService;
    }

    public AdminUserDirectoryService adminUserDirectoryService() {
        return adminUserDirectoryService;
    }
}
