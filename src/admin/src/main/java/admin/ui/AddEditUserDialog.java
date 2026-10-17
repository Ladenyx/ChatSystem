package admin.ui;

import common.model.User;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Screen #4 (A-02): Dialog for adding a new user or editing an existing user.
 */
public class AddEditUserDialog extends Dialog<User> {

    private final TextField usernameField = new TextField();
    private final TextField fullNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField addressField = new TextField();
    private final DatePicker dobPicker = new DatePicker();
    private final ComboBox<User.Gender> genderCombo = new ComboBox<>();
    private final ComboBox<User.Status> statusCombo = new ComboBox<>();
    private final Label errorLabel = new Label();

    private final User editingUser;

    public AddEditUserDialog(User userToEdit) {
        this.editingUser = userToEdit;
        boolean isEdit = userToEdit != null;

        setTitle(isEdit ? "Edit User Account" : "Add New User Account");
        setHeaderText(null);

        DialogPane pane = getDialogPane();
        pane.getStyleClass().add("card");
        pane.getStylesheets().add(getClass().getResource("/common/theme.css").toExternalForm());

        ButtonType saveBtnType = new ButtonType(isEdit ? "Save Changes" : "Create Account", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        Button saveBtn = (Button) pane.lookupButton(saveBtnType);
        saveBtn.getStyleClass().add("btn-accent");

        VBox root = new VBox(16);
        root.setPadding(new Insets(10));
        root.setPrefWidth(420);

        Label headerTitle = new Label(isEdit ? "Update User Details" : "New User Registration");
        headerTitle.getStyleClass().add("section-title");

        Label headerSubtitle = new Label(isEdit ? "Modify information for account @" + userToEdit.getUsername()
                : "Fill in the details to create a new user account.");
        headerSubtitle.getStyleClass().add("text-muted");

        errorLabel.getStyleClass().add("text-danger");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);

        usernameField.setPromptText("e.g. quan.nguyen");
        fullNameField.setPromptText("e.g. Nguyễn Thế Quân");
        emailField.setPromptText("e.g. user@example.com");
        addressField.setPromptText("e.g. 123 Lý Thường Kiệt, Q.10, TP.HCM");
        dobPicker.setPromptText("Date of birth");
        dobPicker.setMaxWidth(Double.MAX_VALUE);

        genderCombo.setItems(FXCollections.observableArrayList(User.Gender.values()));
        genderCombo.setValue(User.Gender.MALE);
        genderCombo.setMaxWidth(Double.MAX_VALUE);

        statusCombo.setItems(FXCollections.observableArrayList(User.Status.values()));
        statusCombo.setValue(User.Status.ACTIVE);
        statusCombo.setMaxWidth(Double.MAX_VALUE);

        if (isEdit) {
            usernameField.setText(userToEdit.getUsername());
            usernameField.setDisable(true);
            fullNameField.setText(userToEdit.getFullName());
            emailField.setText(userToEdit.getEmail());
            addressField.setText(userToEdit.getAddress() != null ? userToEdit.getAddress() : "");
            dobPicker.setValue(userToEdit.getDateOfBirth() != null ? userToEdit.getDateOfBirth() : LocalDate.of(2005, 1, 1));
            genderCombo.setValue(userToEdit.getGender() != null ? userToEdit.getGender() : User.Gender.MALE);
            statusCombo.setValue(userToEdit.getStatus() != null ? userToEdit.getStatus() : User.Status.ACTIVE);
        } else {
            dobPicker.setValue(LocalDate.of(2005, 1, 1));
        }

        int row = 0;
        form.add(fieldLabel("Username:"), 0, row);
        form.add(usernameField, 1, row++);

        form.add(fieldLabel("Full Name:"), 0, row);
        form.add(fullNameField, 1, row++);

        form.add(fieldLabel("Email:"), 0, row);
        form.add(emailField, 1, row++);

        form.add(fieldLabel("Address:"), 0, row);
        form.add(addressField, 1, row++);

        form.add(fieldLabel("Date of Birth:"), 0, row);
        form.add(dobPicker, 1, row++);

        form.add(fieldLabel("Gender:"), 0, row);
        form.add(genderCombo, 1, row++);

        form.add(fieldLabel("Status:"), 0, row);
        form.add(statusCombo, 1, row++);

        root.getChildren().addAll(headerTitle, headerSubtitle, errorLabel, form);
        pane.setContent(root);

        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String u = usernameField.getText().trim();
            String n = fullNameField.getText().trim();
            String e = emailField.getText().trim();

            if (u.isEmpty() || u.length() < 3) {
                showError("Username must be at least 3 characters.");
                event.consume();
                return;
            }
            if (n.isEmpty()) {
                showError("Full name is required.");
                event.consume();
                return;
            }
            if (e.isEmpty() || !e.contains("@") || !e.contains(".")) {
                showError("Please enter a valid email address.");
                event.consume();
                return;
            }
            errorLabel.setVisible(false);
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                if (isEdit) {
                    editingUser.setFullName(fullNameField.getText().trim());
                    editingUser.setEmail(emailField.getText().trim());
                    editingUser.setAddress(addressField.getText().trim());
                    editingUser.setDateOfBirth(dobPicker.getValue());
                    editingUser.setGender(genderCombo.getValue());
                    editingUser.setStatus(statusCombo.getValue());
                    return editingUser;
                } else {
                    User newUser = new User();
                    newUser.setUsername(usernameField.getText().trim());
                    newUser.setFullName(fullNameField.getText().trim());
                    newUser.setEmail(emailField.getText().trim());
                    newUser.setAddress(addressField.getText().trim());
                    newUser.setDateOfBirth(dobPicker.getValue());
                    newUser.setGender(genderCombo.getValue());
                    newUser.setStatus(statusCombo.getValue());
                    newUser.setOnline(false);
                    return newUser;
                }
            }
            return null;
        });
    }

    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("text-semibold");
        return lbl;
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    public static Optional<User> showDialog(Window owner, User userToEdit, Consumer<User> onSaved) {
        AddEditUserDialog dialog = new AddEditUserDialog(userToEdit);
        if (owner != null) dialog.initOwner(owner);
        Optional<User> result = dialog.showAndWait();
        result.ifPresent(u -> {
            if (onSaved != null) onSaved.accept(u);
        });
        return result;
    }
}

