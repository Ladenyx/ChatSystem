package admin.ui;

import common.ui.component.AvatarLabel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Minimal administration sign-in screen for the Day 1 UI milestone. */
public class AdminLoginView extends VBox {
    public AdminLoginView(Runnable onLogin, Runnable onExit) {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(28));
        setSpacing(14);
        getStyleClass().add("login-root");

        AvatarLabel logo = new AvatarLabel("CS");
        Label title = new Label("Administration");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Sign in to manage Chat System");
        subtitle.getStyleClass().add("login-subtitle");

        VBox card = new VBox(12);
        card.setMaxWidth(360);
        card.getStyleClass().add("login-card");
        TextField username = new TextField();
        username.setPromptText("Administrator username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        Label feedback = new Label();
        feedback.getStyleClass().add("text-danger");
        Label previewNote = new Label("UI preview only; credentials are not validated yet.");
        previewNote.getStyleClass().add("text-muted");
        previewNote.setWrapText(true);

        Button signIn = new Button("Sign in");
        signIn.getStyleClass().add("btn-accent");
        signIn.setMaxWidth(Double.MAX_VALUE);
        signIn.setOnAction(event -> {
            if (username.getText().isBlank() || password.getText().isBlank()) {
                feedback.setText("Enter your username and password.");
                return;
            }
            feedback.setText("Opening the administration UI preview.");
            onLogin.run();
        });
        Button exit = new Button("Back to desktop");
        exit.getStyleClass().add("login-link");
        exit.setOnAction(event -> onExit.run());
        card.getChildren().addAll(new Label("Username"), username, new Label("Password"), password,
                previewNote, feedback, signIn, exit);
        getChildren().addAll(logo, title, subtitle, card);
    }
}
