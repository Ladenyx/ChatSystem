package com.chatsystem.admin;

import com.chatsystem.admin.controller.AdminShellController;
import com.chatsystem.desktop.DesktopWindow;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.desktop.theme.ThemeManager;
import java.io.IOException;
import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public final class AdminApplication extends Application {
    static final String WINDOW_TITLE = "WhatsUp · Admin App";

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
        launch(args);
    }
}
