package com.chatsystem.user;

import com.chatsystem.desktop.DesktopWindow;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.desktop.theme.ThemeManager;
import com.chatsystem.user.controller.UserShellController;
import java.io.IOException;
import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public final class UserApplication extends Application {
    public static ViewNavigator createNavigator() throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(UserApplication.class.getResource(
                "/com/chatsystem/user/fxml/shell.fxml")));
        Parent shell = loader.load();
        UserShellController controller = loader.getController();
        ViewNavigator navigator = new ViewNavigator(shell, controller.getViewHost(), new ThemeManager());
        controller.connect(navigator);
        navigator.reset(UserViews.CONVERSATIONS);
        return navigator;
    }

    @Override
    public void start(Stage stage) throws IOException {
        DesktopWindow.show(stage, "WhatsUp · User Client", createNavigator());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
