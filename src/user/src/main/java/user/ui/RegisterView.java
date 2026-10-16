package user.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class RegisterView extends VBox {
    public RegisterView(Runnable onBackToLogin) {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(24));
        getStyleClass().add("login-root");
        Label title = new Label("Create your account");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Join your friends on Chat System");
        subtitle.getStyleClass().add("login-subtitle");

        VBox card = new VBox(9);
        card.setMaxWidth(390);
        card.getStyleClass().add("login-card");
        TextField username = field("Username");
        TextField fullName = field("Full name");
        TextField address = field("Address");
        DatePicker dateOfBirth = new DatePicker();
        dateOfBirth.setPromptText("Date of birth");
        dateOfBirth.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> gender = new ComboBox<>();
        gender.getItems().addAll("Male", "Female", "Other");
        gender.setPromptText("Gender");
        gender.setMaxWidth(Double.MAX_VALUE);
        TextField email = field("Email");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        Label feedback = new Label();
        feedback.getStyleClass().add("text-danger");
        Button create = new Button("Create account");
        create.getStyleClass().add("btn-accent");
        create.setMaxWidth(Double.MAX_VALUE);
        create.setOnAction(event -> feedback.setText("Registration is a UI preview; connect the user service to create accounts."));
        Button back = new Button("Already have an account? Sign in");
        back.getStyleClass().add("login-link");
        back.setOnAction(event -> onBackToLogin.run());
        card.getChildren().addAll(label("Username"), username, label("Full name"), fullName,
                label("Address"), address, label("Date of birth"), dateOfBirth,
                label("Gender"), gender, label("Email"), email, label("Password"), password,
                feedback, create, back);
        getChildren().addAll(title, subtitle, card);
    }

    private static TextField field(String prompt) { TextField field = new TextField(); field.setPromptText(prompt); return field; }
    private static Label label(String text) { return new Label(text); }
}
