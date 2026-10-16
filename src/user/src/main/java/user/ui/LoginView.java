package user.ui;

import common.ui.component.AvatarLabel;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginView extends VBox {
    public LoginView(Runnable onRegister, Runnable onForgotPassword, Runnable onLogin) {
        setAlignment(Pos.CENTER);
        setSpacing(14);
        setPadding(new javafx.geometry.Insets(28));
        getStyleClass().add("login-root");

        AvatarLabel mark = new AvatarLabel("CS");
        Label title = new Label("Welcome back");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Sign in to continue to Chat System");
        subtitle.getStyleClass().add("login-subtitle");

        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(360);
        card.getStyleClass().add("login-card");
        Label usernameLabel = new Label("Username");
        TextField username = new TextField();
        username.setPromptText("Enter your username");
        Label passwordLabel = new Label("Password");
        PasswordField password = new PasswordField();
        password.setPromptText("Enter your password");
        Label error = new Label();
        error.getStyleClass().add("text-danger");
        Button submit = new Button("Sign in");
        submit.getStyleClass().add("btn-accent");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(event -> {
            if (username.getText().isBlank() || password.getText().isBlank()) {
                error.setText("Enter your username and password.");
            } else {
                error.setText("");
                onLogin.run();
            }
        });
        Button forgot = link("Forgot password?", onForgotPassword);
        Button register = link("Create an account", onRegister);
        card.getChildren().addAll(usernameLabel, username, passwordLabel, password, error, submit, forgot, register);
        getChildren().addAll(mark, title, subtitle, card);
    }

    private static Button link(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("login-link");
        button.setOnAction(event -> action.run());
        return button;
    }
}
