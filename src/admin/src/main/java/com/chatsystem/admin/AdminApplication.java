package com.chatsystem.admin;

import com.chatsystem.admin.controller.AdminShellController;
import com.chatsystem.admin.service.AdminServiceRegistry;
import com.chatsystem.desktop.DesktopWindow;
import com.chatsystem.desktop.client.ClientConfig;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.desktop.theme.ThemeManager;
import java.io.IOException;
import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public final class AdminApplication extends Application {
    static final String WINDOW_TITLE = "WhatsUp";
    private static AdminServiceRegistry services = AdminServiceRegistry.mockDefault();

    public static AdminServiceRegistry getServices() {
        return services;
    }

    public static void setServices(AdminServiceRegistry serviceRegistry) {
        services = Objects.requireNonNull(serviceRegistry, "serviceRegistry");
    }

    public static ViewNavigator createNavigator() throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(AdminApplication.class.getResource(
                "/com/chatsystem/admin/fxml/shell.fxml")));
        Parent shell = loader.load();
        AdminShellController controller = loader.getController();
        ViewNavigator navigator = new ViewNavigator(shell, controller.getViewHost(), new ThemeManager());
        controller.connect(navigator);
        navigator.reset(AdminViews.SIGN_IN);
        return navigator;
    }

    @Override
    public void start(Stage stage) throws IOException {
        DesktopWindow.show(stage, WINDOW_TITLE, createNavigator());
    }

    public static void main(String[] args) {
        if (!Boolean.getBoolean("chatsystem.mock")) {
            setServices(AdminServiceRegistry.live(ClientConfig.fromSystemPropertiesOrDefaults()));
        }
        launch(args);
    }
}
