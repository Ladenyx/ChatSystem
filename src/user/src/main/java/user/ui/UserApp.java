package user.ui;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** JavaFX entry point for the user client. */
public class UserApp extends Application {
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        showLogin();
        stage.show();
    }

    void showLogin() {
        show(new LoginView(this::showRegister, this::showForgotPassword, this::showMain), "Chat System · Sign in", 460, 600);
    }

    private void showRegister() {
        show(new RegisterView(this::showLogin), "Chat System · Create account", 500, 720);
    }

    private void showForgotPassword() {
        show(new ForgotPasswordView(this::showLogin), "Chat System · Reset password", 460, 520);
    }

    private void showMain() {
        show(new MainWindowView(this::showLogin), "Chat System", 1100, 700);
    }

    private void show(Parent root, String title, double width, double height) {
        Scene scene = new Scene(root, width, height);
        var stylesheet = UserApp.class.getResource("/common/theme.css");
        if (stylesheet != null) scene.getStylesheets().add(stylesheet.toExternalForm());
        stage.setTitle(title);
        stage.setMinWidth(420);
        stage.setMinHeight(500);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static void main(String[] args) { launch(args); }
}
