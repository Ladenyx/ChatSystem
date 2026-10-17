package user.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Screen #13: Change Password Dialog (Roadmap U-04).
 * Features:
 * - Old password verification.
 * - New password with length validation (min 6 characters).
 * - Confirm password matching check.
 */
public class ChangePasswordDialog extends Dialog<Boolean> {

    private final PasswordField currentPassField = new PasswordField();
    private final PasswordField newPassField = new PasswordField();
    private final PasswordField confirmPassField = new PasswordField();
    private final Label errorLabel = new Label();

    public ChangePasswordDialog() {
        setTitle("Change Password");
        setHeaderText(null);

        DialogPane pane = getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        Button updateBtn = (Button) pane.lookupButton(ButtonType.OK);
        updateBtn.setText("Update Password");
        updateBtn.getStyleClass().add("btn-accent");

        VBox root = new VBox(14);
        root.setPadding(new Insets(16));
        root.setPrefWidth(380);

        Label title = new Label("Change Password");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label("Ensure your account is using a strong and unique password.");
        subtitle.getStyleClass().add("login-subtitle");

        errorLabel.getStyleClass().add("text-danger");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);

        Label oldLbl = new Label("Current Password:");
        oldLbl.getStyleClass().add("text-bold");
        currentPassField.setPromptText("Enter current password");

        Label newLbl = new Label("New Password:");
        newLbl.getStyleClass().add("text-bold");
        newPassField.setPromptText("At least 6 characters");

        Label confirmLbl = new Label("Confirm Password:");
        confirmLbl.getStyleClass().add("text-bold");
        confirmPassField.setPromptText("Re-type new password");

        grid.add(oldLbl, 0, 0);
        grid.add(currentPassField, 1, 0);
        grid.add(newLbl, 0, 1);
        grid.add(newPassField, 1, 1);
        grid.add(confirmLbl, 0, 2);
        grid.add(confirmPassField, 1, 2);

        root.getChildren().addAll(title, subtitle, errorLabel, grid);
        pane.setContent(root);

        // Validation upon submit
        updateBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String oldPass = currentPassField.getText();
            String newPass = newPassField.getText();
            String confirmPass = confirmPassField.getText();

            if (oldPass.isBlank()) {
                showError("Please enter your current password.");
                event.consume();
                return;
            }
            if (newPass.length() < 6) {
                showError("New password must be at least 6 characters long.");
                event.consume();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                showError("New passwords do not match. Please verify.");
                event.consume();
                return;
            }
        });

        setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Password Updated");
                success.setHeaderText(null);
                success.setContentText("Your password has been changed successfully.");
                success.showAndWait();
                return true;
            }
            return false;
        });
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    public static boolean showDialog() {
        Optional<Boolean> result = new ChangePasswordDialog().showAndWait();
        return result.orElse(false);
    }
}

