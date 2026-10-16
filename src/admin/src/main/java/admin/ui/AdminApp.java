package admin.ui;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** JavaFX entry point for the administration console. */
public class AdminApp extends Application {
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        showLogin();
        stage.show();
    }

    private void showLogin() {
        show(new AdminLoginView(this::showDashboard, () -> stage.close()), "Chat System · Admin sign in", 460, 560);
    }

    private void showDashboard() {
        show(new AdminMainWindowView(this::showLogin), "Chat System · Administration", 1280, 760);
    }

    private void show(Parent root, String title, double width, double height) {
        Scene scene = new Scene(root, width, height);
        var stylesheet = AdminApp.class.getResource("/common/theme.css");
        if (stylesheet != null) scene.getStylesheets().add(stylesheet.toExternalForm());
        stage.setTitle(title);
        stage.setMinWidth(Math.min(width, 960));
        stage.setMinHeight(Math.min(height, 600));
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static void main(String[] args) { launch(args); }
}
