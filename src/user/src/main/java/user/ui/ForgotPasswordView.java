package user.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ForgotPasswordView extends VBox {
    public ForgotPasswordView(Runnable onBackToLogin) {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(28));
        getStyleClass().add("login-root");
        Label title = new Label("Reset your password");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Enter your email or username and we’ll send a reset link.");
        subtitle.getStyleClass().add("login-subtitle");
        subtitle.setWrapText(true);
        VBox card = new VBox(12);
        card.setMaxWidth(360);
        card.getStyleClass().add("login-card");
        TextField account = new TextField();
        account.setPromptText("Email or username");
        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.getStyleClass().add("text-muted");
        Button send = new Button("Send reset instructions");
        send.getStyleClass().add("btn-accent");
        send.setMaxWidth(Double.MAX_VALUE);
        send.setOnAction(event -> feedback.setText(account.getText().isBlank()
                ? "Enter your email or username."
                : "Password reset is a UI preview; connect the user service to send instructions."));
        Button back = new Button("Back to sign in");
        back.getStyleClass().add("login-link");
        back.setOnAction(event -> onBackToLogin.run());
        card.getChildren().addAll(new Label("Email or username"), account, feedback, send, back);
        getChildren().addAll(title, subtitle, card);
    }
}
