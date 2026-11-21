package com.chatsystem.user;

import com.chatsystem.desktop.DesktopWindow;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.desktop.theme.ThemeManager;
import com.chatsystem.user.controller.UserShellController;
import com.chatsystem.user.service.SpamReportService;
import com.chatsystem.user.service.mock.MockSpamReportService;
import java.io.IOException;
import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public final class UserApplication extends Application {
    private static SpamReportService spamReportService = new MockSpamReportService();

    public static SpamReportService getSpamReportService() {
        return spamReportService;
    }

    public static void setSpamReportService(SpamReportService service) {
        spamReportService = Objects.requireNonNull(service);
    }
    public static ViewNavigator createNavigator() throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(UserApplication.class.getResource(
                "/com/chatsystem/user/fxml/shell.fxml")));
        Parent shell = loader.load();
        UserShellController controller = loader.getController();
        ViewNavigator navigator = new ViewNavigator(shell, controller.getViewHost(), new ThemeManager());
        controller.connect(navigator);
        navigator.reset(UserViews.SIGN_IN);
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
