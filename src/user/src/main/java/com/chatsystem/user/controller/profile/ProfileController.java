package com.chatsystem.user.controller.profile;

import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ConversationAvatar;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

/** Offline profile & security preview controller; manages profile, password change and E2EE keys. */
public final class ProfileController implements ViewController {
    private static final String PROFILE_DRAFT = ProfileController.class.getName() + ".draft";
    private static final String CURRENT_EMAIL = ProfileController.class.getName() + ".email";
    private static final PseudoClass RULE_MET = PseudoClass.getPseudoClass("met");
    private record Draft(String name, String address, String date, String gender) { }

    private static final String EMAIL_ATOM = "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+";
    private static final String DOMAIN_LABEL = "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?";
    private static final String EMAIL_PATTERN = EMAIL_ATOM + "(?:\\." + EMAIL_ATOM + ")*@"
            + DOMAIN_LABEL + "(?:\\." + DOMAIN_LABEL + ")+";

    @FXML private TextField username;
    @FXML private Label accountName;
    @FXML private Label accountInitials;
    @FXML private TextField fullName;
    @FXML private TextField address;
    @FXML private TextField email;
    @FXML private DatePicker birthDate;
    @FXML private RadioButton genderMale;
    @FXML private RadioButton genderFemale;
    @FXML private RadioButton genderOther;
    private final ToggleGroup gender = new ToggleGroup();

    @FXML private Button changeEmailButton;
    @FXML private VBox emailChangeForm;
    @FXML private TextField newEmail;
    @FXML private PasswordField emailPassword;
    @FXML private Label emailError;
    @FXML private Label emailPasswordError;
    @FXML private Label nameError;
    @FXML private Label addressError;
    @FXML private Label dateError;
    @FXML private Label profileNotice;

    @FXML private PasswordField currentPassword;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField confirmPassword;
    @FXML private Label passwordNotice;
    @FXML private Label currentPasswordError;
    @FXML private Label newPasswordError;
    @FXML private Label confirmPasswordError;
    @FXML private TextField currentPasswordVisible;
    @FXML private TextField newPasswordVisible;
    @FXML private TextField confirmPasswordVisible;
    @FXML private Button showCurrentPassword;
    @FXML private Button showNewPassword;
    @FXML private Button showConfirmPassword;
    @FXML private Label passwordLength;
    @FXML private Label passwordUpper;
    @FXML private Label passwordLower;
    @FXML private Label passwordDigit;
    @FXML private Label passwordSymbol;

    @FXML private Label deviceStatusLabel;
    @FXML private VBox keyStatus;
    @FXML private VBox keyForm;
    @FXML private HBox keyDigits;
    @FXML private HBox keyConfirmDigits;
    @FXML private Label keyError;
    @FXML private Button keySubmit;
    private final List<PasswordField> keyFields = new ArrayList<>();
    private final List<PasswordField> keyConfirmation = new ArrayList<>();
    private boolean pastingKey;
    private ViewNavigator navigator;

    @FXML
    private void initialize() {
        var catalog = UserViews.CONVERSATIONS_DATA;
        var account = catalog.user(catalog.currentUserId());
        username.setText(account.username());
        fullName.setText(account.fullName());
        email.setText(account.email());
        showIdentity(account.fullName());
        genderMale.setToggleGroup(gender);
        genderFemale.setToggleGroup(gender);
        genderOther.setToggleGroup(gender);

        for (Label feedback : List.of(nameError, addressError, dateError, emailError, emailPasswordError,
                currentPasswordError, newPasswordError, confirmPasswordError, keyError, profileNotice, passwordNotice)) {
            feedback.visibleProperty().bind(feedback.textProperty().isNotEmpty());
            feedback.managedProperty().bind(feedback.visibleProperty());
        }

        initKeyDigits(keyDigits, keyFields, "profileKey", "Mã khôi phục");
        initKeyDigits(keyConfirmDigits, keyConfirmation, "profileKeyConfirm", "Mã khôi phục nhập lại");
        updateKeySubmit();

        bridge(currentPassword, currentPasswordVisible, showCurrentPassword);
        bridge(newPassword, newPasswordVisible, showNewPassword);
        bridge(confirmPassword, confirmPasswordVisible, showConfirmPassword);
        for (Label rule : List.of(passwordLength, passwordUpper, passwordLower, passwordDigit, passwordSymbol)) {
            FontIcon icon = new FontIcon("fth-minus");
            icon.setIconSize(16);
            icon.setMouseTransparent(true);
            rule.setGraphic(icon);
        }
        updatePasswordRules();

        newPassword.textProperty().addListener((observable, old, value) -> {
            updatePasswordRules();
            updateConfirmation();
        });
        confirmPassword.textProperty().addListener((observable, old, value) -> updateConfirmation());

        setupDatePicker();
    }

    private void showIdentity(String name) {
        accountName.setText(name);
        accountInitials.setText(ConversationAvatar.initials(name));
    }

    private void setupDatePicker() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        birthDate.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return LocalDate.parse(string.trim(),
                                DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
                    } catch (Exception ignored) {
                        return null;
                    }
                }
                return null;
            }
        });
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
        Object current = navigator.getRoot().getProperties().get(CURRENT_EMAIL);
        if (current instanceof String value) {
            email.setText(value);
        }
        Object saved = navigator.getRoot().getProperties().remove(PROFILE_DRAFT);
        if (saved instanceof Draft draft) {
            fullName.setText(draft.name());
            address.setText(draft.address());
            birthDate.getEditor().setText(draft.date());
            setGender(draft.gender());
        }
    }

    @FXML
    private void saveProfile() {
        if (validateProfile()) {
            showIdentity(fullName.getText().trim());
            profileNotice.setText("Đã lưu hồ sơ trong bản xem trước");
        }
    }

    private boolean validateProfile() {
        String name = fullName.getText().trim();
        String addr = address.getText().trim();
        nameError.setText(name.isEmpty() ? "Nhập họ tên"
                : name.codePointCount(0, name.length()) > 100 ? "Họ tên tối đa 100 ký tự" : "");
        addressError.setText(addr.codePointCount(0, addr.length()) > 255 ? "Địa chỉ tối đa 255 ký tự" : "");
        try {
            String raw = birthDate.getEditor().getText().trim();
            LocalDate date = raw.isEmpty() ? null : LocalDate.parse(raw,
                    DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
            dateError.setText(date != null && date.isAfter(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                    ? "Ngày sinh phải là hôm nay hoặc trước đó" : "");
        } catch (Exception ex) {
            dateError.setText("Nhập ngày sinh theo dạng dd/mm/yyyy");
        }
        return nameError.getText().isEmpty() && addressError.getText().isEmpty() && dateError.getText().isEmpty();
    }

    @FXML
    private void clearGender() {
        gender.selectToggle(null);
    }

    @FXML
    private void changeEmail() {
        emailChangeForm.setManaged(true);
        emailChangeForm.setVisible(true);
        profileNotice.setText("");
        newEmail.requestFocus();
    }

    @FXML
    private void cancelEmailChange() {
        emailPassword.clear();
        newEmail.clear();
        emailError.setText("");
        emailPasswordError.setText("");
        emailChangeForm.setVisible(false);
        emailChangeForm.setManaged(false);
        changeEmailButton.requestFocus();
    }

    @FXML
    private void requestEmailVerification() {
        String proposed = newEmail.getText().trim();
        String current = emailPassword.getText();
        validateNewEmail();
        emailPasswordError.setText(current.isEmpty() ? "Nhập mật khẩu hiện tại" : "");
        if (!emailError.getText().isEmpty() || !emailPasswordError.getText().isEmpty()) {
            return;
        }
        email.setText(proposed);
        navigator.getRoot().getProperties().put(CURRENT_EMAIL, proposed);
        cancelEmailChange();
        profileNotice.setText("Đã cập nhật email trong bản xem trước");
    }

    private void validateNewEmail() {
        String proposed = newEmail.getText().trim();
        emailError.setText(proposed.isEmpty() ? "Nhập email mới"
                : proposed.codePointCount(0, proposed.length()) > 254 ? "Email tối đa 254 ký tự"
                : !proposed.matches(EMAIL_PATTERN) ? "Nhập email hợp lệ"
                : proposed.equalsIgnoreCase(email.getText()) ? "Dùng email khác với email hiện tại" : "");
    }

    private String selectedGender() {
        return gender.getSelectedToggle() instanceof RadioButton selected ? selected.getText() : null;
    }

    private void setGender(String value) {
        gender.selectToggle(null);
        if ("Nam".equals(value)) {
            gender.selectToggle(genderMale);
        } else if ("Nữ".equals(value)) {
            gender.selectToggle(genderFemale);
        } else if ("Khác".equals(value)) {
            gender.selectToggle(genderOther);
        }
    }

    @FXML
    private void changePassword() {
        if (!validatePassword()) {
            return;
        }
        passwordNotice.setText("Đã đổi mật khẩu trong bản xem trước");
        currentPassword.clear();
        newPassword.clear();
        confirmPassword.clear();
    }

    private boolean validatePassword() {
        String curr = currentPassword.getText();
        String next = newPassword.getText();
        String confirm = confirmPassword.getText();
        currentPasswordError.setText(curr.isEmpty() ? "Nhập mật khẩu hiện tại" : "");
        boolean valid = next.length() >= 8 && next.length() <= 128
                && next.chars().anyMatch(c -> c >= 'A' && c <= 'Z')
                && next.chars().anyMatch(c -> c >= 'a' && c <= 'z')
                && next.chars().anyMatch(c -> c >= '0' && c <= '9')
                && next.chars().anyMatch(c -> c >= '!' && c <= '~' && !Character.isLetterOrDigit(c));
        newPasswordError.setText(next.isEmpty() ? "Nhập mật khẩu mới"
                : valid ? "" : "Dùng 8–128 ký tự với đủ chữ hoa, chữ thường, số và ký tự đặc biệt");
        confirmPasswordError.setText(confirm.isEmpty() ? "Nhập lại mật khẩu mới"
                : !next.equals(confirm) ? "Mật khẩu nhập lại chưa khớp" : "");
        return currentPasswordError.getText().isEmpty() && newPasswordError.getText().isEmpty()
                && confirmPasswordError.getText().isEmpty();
    }

    @FXML
    private void openKeys() {
        keyStatus.setVisible(false);
        keyStatus.setManaged(false);
        keyForm.setManaged(true);
        keyForm.setVisible(true);
        if (!keyFields.isEmpty()) {
            keyFields.getFirst().requestFocus();
        }
    }

    private void closeDialog() {
        if (username != null && username.getScene() != null && username.getScene().getWindow() != null) {
            username.getScene().getWindow().hide();
        }
    }

    @FXML
    private void openKeySetupScreen() {
        closeDialog();
        if (navigator != null) {
            navigator.navigate(UserViews.E2EE_KEY_SETUP);
        }
    }

    private void initKeyDigits(HBox row, List<PasswordField> fields, String prefix, String fieldName) {
        for (int i = 0; i < 6; i++) {
            PasswordField field = new PasswordField();
            field.setId(prefix + (i + 1));
            field.setAccessibleText(fieldName + ", chữ số " + (i + 1));
            field.getStyleClass().add("profile-key-digit");
            field.setTextFormatter(new TextFormatter<>(change -> {
                String text = change.getText();
                return text.matches("[0-9]*") && change.getControlNewText().length() <= 1 ? change : null;
            }));
            final int index = i;
            field.textProperty().addListener((observable, old, value) -> {
                if (!pastingKey && value != null && value.length() == 1 && index < 5) {
                    Platform.runLater(() -> fields.get(index + 1).requestFocus());
                }
                updateKeySubmit();
            });
            field.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.BACK_SPACE && field.getText().isEmpty() && index > 0) {
                    fields.get(index - 1).requestFocus();
                    event.consume();
                }
            });
            fields.add(field);
            row.getChildren().add(field);
        }
    }

    private void updateKeySubmit() {
        keySubmit.setDisable(keyFields.size() != 6 || keyConfirmation.size() != 6
                || keyFields.stream().anyMatch(field -> !field.getText().matches("[0-9]"))
                || keyConfirmation.stream().anyMatch(field -> !field.getText().matches("[0-9]")));
    }

    private String keyText(List<PasswordField> fields) {
        StringBuilder code = new StringBuilder(6);
        fields.forEach(field -> code.append(field.getText()));
        return code.toString();
    }

    @FXML
    private void submitKey() {
        if (keySubmit.isDisabled()) {
            return;
        }
        if (!keyText(keyFields).equals(keyText(keyConfirmation))) {
            keyError.setText("Mã nhập lại chưa khớp");
            keyConfirmation.getFirst().requestFocus();
            return;
        }
        clearKey();
        keyError.setText("");
        keyForm.setVisible(false);
        keyForm.setManaged(false);
        keyStatus.setManaged(true);
        keyStatus.setVisible(true);
        if (deviceStatusLabel != null) {
            deviceStatusLabel.setText("Đã thiết lập mã khôi phục");
        }
    }

    @FXML
    private void cancelKey() {
        clearKey();
        keyError.setText("");
        keyForm.setVisible(false);
        keyForm.setManaged(false);
        keyStatus.setManaged(true);
        keyStatus.setVisible(true);
    }

    private void clearKey() {
        pastingKey = true;
        keyFields.forEach(PasswordField::clear);
        keyConfirmation.forEach(PasswordField::clear);
        pastingKey = false;
        updateKeySubmit();
    }

    @FXML
    private void forgotPassword() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn sẽ rời bản xem trước và quay về luồng khôi phục mật khẩu. Tiếp tục?",
                ButtonType.CANCEL, ButtonType.OK);
        if (currentPassword.getScene() != null && currentPassword.getScene().getWindow() != null) {
            confirm.initOwner(currentPassword.getScene().getWindow());
        }
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            closeDialog();
            if (navigator != null) {
                navigator.reset(UserViews.FORGOT_PASSWORD);
            }
        }
    }

    private void updateConfirmation() {
        String confirm = confirmPassword.getText();
        confirmPasswordError.setText(!confirm.isEmpty() && !confirm.equals(newPassword.getText())
                ? "Mật khẩu nhập lại chưa khớp" : "");
    }

    private void updatePasswordRules() {
        String value = newPassword.getText() == null ? "" : newPassword.getText();
        showRule(passwordLength, value.length() >= 8 && value.length() <= 128, "8–128 ký tự");
        showRule(passwordUpper, value.chars().anyMatch(c -> c >= 'A' && c <= 'Z'), "Chữ hoa");
        showRule(passwordLower, value.chars().anyMatch(c -> c >= 'a' && c <= 'z'), "Chữ thường");
        showRule(passwordDigit, value.chars().anyMatch(c -> c >= '0' && c <= '9'), "Chữ số");
        showRule(passwordSymbol, value.chars().anyMatch(c -> c >= '!' && c <= '~'
                && !Character.isLetterOrDigit(c)), "Ký tự đặc biệt");
    }

    private static void showRule(Label label, boolean matches, String text) {
        label.setText(text);
        label.setAccessibleText(text + (matches ? ", đã đạt" : ", chưa đạt"));
        ((FontIcon) label.getGraphic()).setIconLiteral(matches ? "fth-check" : "fth-minus");
        label.pseudoClassStateChanged(RULE_MET, matches);
    }

    private static void bridge(PasswordField masked, TextField visible, Button button) {
        FontIcon icon = (FontIcon) button.getGraphic();
        String fieldName = masked.getAccessibleText().toLowerCase(Locale.ROOT);
        visible.textProperty().bindBidirectional(masked.textProperty());
        button.setOnAction(event -> {
            boolean reveal = !visible.isVisible();
            TextField previous = reveal ? masked : visible;
            int anchor = previous.getAnchor();
            int caret = previous.getCaretPosition();
            visible.setVisible(reveal);
            visible.setManaged(reveal);
            masked.setVisible(!reveal);
            masked.setManaged(!reveal);
            button.setText(reveal ? "Ẩn" : "Hiện");
            button.setAccessibleText((reveal ? "Ẩn " : "Hiện ") + fieldName);
            icon.setIconLiteral(reveal ? "fth-eye" : "fth-eye-off");
            TextField next = reveal ? visible : masked;
            next.requestFocus();
            next.selectRange(anchor, caret);
        });
    }

    @Override
    public void onLeave() {
        clearKey();
        if (emailPassword != null) emailPassword.clear();
        if (currentPassword != null) currentPassword.clear();
        if (newPassword != null) newPassword.clear();
        if (confirmPassword != null) confirmPassword.clear();
    }
}
