package user.ui;

import common.mock.MockData;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.StatusBadge;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Screen #12: Account Info & Profile Update Dialog (Roadmap U-03).
 * Features:
 * - View account details with avatar, username, and active status.
 * - Editable profile information: Full name, Email, Address, Date of birth, Gender.
 * - Direct shortcut to Change Password dialog (U-04).
 */
public class AccountInfoDialog extends Dialog<Boolean> {

    private final TextField fullNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField addressField = new TextField();
    private final DatePicker dobPicker = new DatePicker();
    private final ComboBox<User.Gender> genderCombo = new ComboBox<>(FXCollections.observableArrayList(User.Gender.values()));

    public AccountInfoDialog() {
        setTitle("Account Profile");
        setHeaderText(null);

        DialogPane pane = getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        Button saveBtn = (Button) pane.lookupButton(ButtonType.OK);
        saveBtn.setText("Save Changes");
        saveBtn.getStyleClass().add("btn-accent");

        User user = MockData.CURRENT_USER;

        VBox root = new VBox(16);
        root.setPadding(new Insets(16));
        root.setPrefWidth(420);

        // Header with Avatar and Basic Info
        AvatarLabel avatar = new AvatarLabel(user);
        avatar.setStyle("-fx-font-size: 16px; -fx-min-width: 52px; -fx-min-height: 52px;");

        Label nameTitle = new Label(user.getFullName());
        nameTitle.getStyleClass().add("text-name");
        nameTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700;");

        Label usernameLbl = new Label("@" + user.getUsername());
        usernameLbl.getStyleClass().add("text-muted");

        HBox userHeaderRow = new HBox(12, avatar, new VBox(4, nameTitle, usernameLbl, StatusBadge.online(user.isOnline())));
        userHeaderRow.setAlignment(Pos.CENTER_LEFT);
        userHeaderRow.setPadding(new Insets(0, 0, 10, 0));
        userHeaderRow.setStyle("-fx-border-color: #ebebfc; -fx-border-width: 0 0 1 0;");

        // Form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);

        // Populate fields with current user data
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        addressField.setText(user.getAddress() != null ? user.getAddress() : "");
        dobPicker.setValue(user.getDateOfBirth() != null ? user.getDateOfBirth() : LocalDate.of(2005, 1, 1));
        dobPicker.setMaxWidth(Double.MAX_VALUE);
        genderCombo.setValue(user.getGender() != null ? user.getGender() : User.Gender.OTHER);
        genderCombo.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Address:"), 0, 2);
        grid.add(addressField, 1, 2);
        grid.add(new Label("Date of Birth:"), 0, 3);
        grid.add(dobPicker, 1, 3);
        grid.add(new Label("Gender:"), 0, 4);
        grid.add(genderCombo, 1, 4);

        // Change password shortcut button
        Button changePassBtn = new Button("🔑 Change Password...");
        changePassBtn.getStyleClass().add("btn-ghost");
        changePassBtn.setMaxWidth(Double.MAX_VALUE);
        changePassBtn.setOnAction(e -> ChangePasswordDialog.showDialog());

        root.getChildren().addAll(userHeaderRow, grid, changePassBtn);
        pane.setContent(root);

        setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                user.setFullName(fullNameField.getText().trim());
                user.setEmail(emailField.getText().trim());
                user.setAddress(addressField.getText().trim());
                user.setDateOfBirth(dobPicker.getValue());
                user.setGender(genderCombo.getValue());

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Profile Updated");
                success.setHeaderText(null);
                success.setContentText("Your profile information has been successfully updated.");
                success.showAndWait();
                return true;
            }
            return false;
        });
    }

    public static boolean showDialog() {
        Optional<Boolean> result = new AccountInfoDialog().showAndWait();
        return result.orElse(false);
    }
}

