package com.chatsystem.user.controller.auth;

import com.chatsystem.user.UserViews;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

public final class SignUpController extends AuthFormController {
    private static final DateTimeFormatter BIRTH_DATE_FORMAT = DateTimeFormatter
            .ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final ZoneId VIETNAM_TIME = ZoneId.of("Asia/Ho_Chi_Minh");

    @FXML private Label stepTitle;
    @FXML private Label stepHelp;
    @FXML private VBox usernameStep;
    @FXML private VBox passwordStep;
    @FXML private VBox emailStep;
    @FXML private VBox profileStep;
    @FXML private TextField authSignUpUsername;
    @FXML private PasswordField authSignUpPassword;
    @FXML private TextField visiblePassword;
    @FXML private Button showPassword;
    @FXML private PasswordField authSignUpPasswordConfirm;
    @FXML private TextField visibleConfirmation;
    @FXML private Button showConfirmation;
    @FXML private Label ruleLength;
    @FXML private Label ruleUpper;
    @FXML private Label ruleLower;
    @FXML private Label ruleDigit;
    @FXML private Label ruleSymbol;
    @FXML private TextField authSignUpEmail;
    @FXML private TextField authSignUpFullName;
    @FXML private TextField address;
    @FXML private DatePicker dateOfBirth;
    @FXML private ToggleGroup gender;
    @FXML private Button clearGenderButton;
    @FXML private Label authSignUpUsernameError;
    @FXML private Label authSignUpPasswordError;
    @FXML private Label authSignUpPasswordConfirmError;
    @FXML private Label authSignUpEmailError;
    @FXML private Label authSignUpFullNameError;
    @FXML private Label authSignUpAddressError;
    @FXML private Label authSignUpBirthDateError;
    @FXML private Label formError;
    @FXML private Label emailWait;
    @FXML private Button authSignUpContinue;
    @FXML private Button backButton;
    private FieldFeedback usernameFeedback;
    private FieldFeedback passwordFeedback;
    private FieldFeedback confirmationFeedback;
    private FieldFeedback emailFeedback;
    private FieldFeedback nameFeedback;
    private FieldFeedback addressFeedback;
    private FieldFeedback birthDateFeedback;
    private Timeline clock;

    @FXML
    private void initialize() {
        bridge(authSignUpPassword, visiblePassword, showPassword);
        bridge(authSignUpPasswordConfirm, visibleConfirmation, showConfirmation);
        authSignUpPassword.textProperty().addListener((obs, oldValue, value) -> updateRules());
        authSignUpEmail.textProperty().addListener((obs, oldValue, value) -> refreshEmailWait());
        bindFieldValidation();
        clearGenderButton.visibleProperty().bind(gender.selectedToggleProperty().isNotNull());
        clearGenderButton.managedProperty().bind(clearGenderButton.visibleProperty());
        clearGenderButton.disableProperty().bind(gender.selectedToggleProperty().isNull());
        clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> refreshEmailWait()));
        clock.setCycleCount(Timeline.INDEFINITE);
    }

    @Override
    void enter() {
        authSignUpUsername.setText(flow.signUpName);
        authSignUpEmail.setText(flow.signUpEmail);
        renderStep();
    }

    @FXML
    private void submit() {
        message(formError, "");
        switch (flow.signUpStep) {
            case USERNAME -> {
                String username = authSignUpUsername.getText().trim();
                if (!usernameFeedback.validate()) {
                    focus(authSignUpUsername);
                } else if (OfflineAuthDemoService.usernameInUse(username)) {
                    usernameFeedback.reject("Tên đăng nhập đã được đăng ký.");
                    focus(authSignUpUsername);
                } else {
                    flow.signUpName = username;
                    step(AuthDemoFlow.SignUpStep.PASSWORD);
                }
            }
            case PASSWORD -> {
                boolean passwordValid = passwordFeedback.validate();
                boolean confirmationValid = confirmationFeedback.validate();
                if (!passwordValid) {
                    focus(authSignUpPassword.isVisible() ? authSignUpPassword : visiblePassword);
                } else if (!confirmationValid) {
                    focus(authSignUpPasswordConfirm.isVisible() ? authSignUpPasswordConfirm : visibleConfirmation);
                } else {
                    clearPasswords();
                    step(AuthDemoFlow.SignUpStep.EMAIL);
                }
            }
            case EMAIL -> {
                String email = authSignUpEmail.getText().trim();
                if (!emailFeedback.validate()) {
                    focus(authSignUpEmail);
                } else if (OfflineAuthDemoService.emailInUse(email)) {
                    emailFeedback.reject("Email đã được đăng ký.");
                    focus(authSignUpEmail);
                } else if (flow.signUpExpires != null && !flow.signUpActive()) {
                    error("Yêu cầu đăng ký đã hết hạn. Hãy quay lại đăng nhập và bắt đầu lại.");
                } else if (emailWaitSeconds() == 0) {
                    flow.signUpEmail = email;
                    flow.issueCode();
                    go(UserViews.VERIFY_EMAIL);
                }
            }
            case PROFILE -> {
                boolean nameValid = nameFeedback.validate();
                boolean addressValid = addressFeedback.validate();
                boolean birthDateValid = birthDateFeedback.validate();
                if (!flow.emailVerified || !flow.signUpActive()) {
                    error("Yêu cầu đăng ký đã hết hạn. Hãy quay lại đăng nhập và bắt đầu lại.");
                } else if (!nameValid) {
                    focus(authSignUpFullName);
                } else if (!addressValid) {
                    focus(address);
                } else if (!birthDateValid) {
                    focus(dateOfBirth.getEditor());
                } else {
                    finish("Đã hoàn tất đăng ký mẫu. Không có tài khoản thật được tạo.");
                }
            }
        }
    }

    @FXML
    private void back() {
        switch (flow.signUpStep) {
            case USERNAME, PROFILE -> cancel();
            case PASSWORD -> {
                clearPasswords();
                step(AuthDemoFlow.SignUpStep.USERNAME);
            }
            case EMAIL -> {
                if (flow.codeIssued == null) {
                    step(AuthDemoFlow.SignUpStep.PASSWORD);
                } else if (flow.signUpActive()) {
                    go(UserViews.VERIFY_EMAIL);
                } else {
                    cancel();
                }
            }
        }
    }

    @FXML
    private void cancel() {
        clearAndGo(UserViews.SIGN_IN);
    }

    @FXML
    private void clearGender() {
        gender.selectToggle(null);
    }

    private void bindFieldValidation() {
        usernameFeedback = new FieldFeedback(authSignUpUsernameError, this::usernameError,
                authSignUpUsername.textProperty(), authSignUpUsername);
        passwordFeedback = new FieldFeedback(authSignUpPasswordError, this::passwordError,
                authSignUpPassword.textProperty(), authSignUpPassword, visiblePassword);
        confirmationFeedback = new FieldFeedback(authSignUpPasswordConfirmError, this::confirmationError,
                authSignUpPasswordConfirm.textProperty(), authSignUpPasswordConfirm, visibleConfirmation);
        confirmationFeedback.dependsOn(authSignUpPassword.textProperty());
        emailFeedback = new FieldFeedback(authSignUpEmailError, () -> emailError(authSignUpEmail.getText().trim()),
                authSignUpEmail.textProperty(), authSignUpEmail);
        nameFeedback = new FieldFeedback(authSignUpFullNameError, this::nameError,
                authSignUpFullName.textProperty(), authSignUpFullName);
        addressFeedback = new FieldFeedback(authSignUpAddressError,
                () -> characterCount(address.getText().trim()) > 255 ? "Địa chỉ không được vượt quá 255 ký tự." : "",
                address.textProperty(), address);
        dateOfBirth.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate value) {
                return value == null ? "" : value.format(BIRTH_DATE_FORMAT);
            }

            @Override
            public LocalDate fromString(String text) {
                try {
                    return parseBirthDate(text);
                } catch (DateTimeParseException ignored) {
                    // JavaFX restores the prior text on conversion failure at blur.
                    // Keep the committed value unchanged and validate the raw editor separately.
                    return dateOfBirth.getValue();
                }
            }
        });
        TextField editor = dateOfBirth.getEditor();
        birthDateFeedback = new FieldFeedback(authSignUpBirthDateError, this::birthDateError,
                editor.textProperty(), dateOfBirth, editor);
        dateOfBirth.addEventFilter(KeyEvent.ANY, event -> {
            if (event.getCode() == KeyCode.ENTER && !birthDateFeedback.validate()) {
                event.consume();
            }
        });
    }

    private String usernameError() {
        String username = authSignUpUsername.getText().trim();
        if (username.isEmpty()) {
            return "Vui lòng nhập tên đăng nhập.";
        }
        if (username.length() < 6 || username.length() > 30) {
            return "Tên đăng nhập phải có từ 6 đến 30 ký tự.";
        }
        return username.matches("[A-Za-z0-9_-]+") ? "" : "Chỉ dùng chữ cái không dấu, chữ số, dấu _ và dấu -.";
    }

    private String passwordError() {
        String password = authSignUpPassword.getText();
        if (password.isEmpty()) {
            return "Vui lòng nhập mật khẩu.";
        }
        return validPassword(password) ? "" : "Mật khẩu chưa đáp ứng đủ điều kiện.";
    }

    private String confirmationError() {
        String confirmation = authSignUpPasswordConfirm.getText();
        if (confirmation.isEmpty()) {
            return "Vui lòng nhập lại mật khẩu.";
        }
        return confirmation.equals(authSignUpPassword.getText()) ? "" : "Mật khẩu nhập lại chưa khớp.";
    }

    private String nameError() {
        String name = authSignUpFullName.getText().trim();
        if (name.isEmpty()) {
            return "Vui lòng nhập họ tên.";
        }
        return characterCount(name) <= 100 ? "" : "Họ tên không được vượt quá 100 ký tự.";
    }

    private String birthDateError() {
        try {
            LocalDate parsed = parseBirthDate(dateOfBirth.getEditor().getText());
            return parsed != null && parsed.isAfter(LocalDate.now(VIETNAM_TIME))
                    ? "Ngày sinh không được ở tương lai." : "";
        } catch (DateTimeParseException ignored) {
            return "Ngày sinh không hợp lệ.";
        }
    }

    private static LocalDate parseBirthDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String value = text.trim();
        if (!value.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}") || value.endsWith("/0000")) {
            throw new DateTimeParseException("Expected a date in DD/MM/YYYY format", value, 0);
        }
        return LocalDate.parse(value, BIRTH_DATE_FORMAT);
    }

    private void step(AuthDemoFlow.SignUpStep next) {
        flow.signUpStep = next;
        renderStep();
    }

    private void renderStep() {
        var step = flow.signUpStep;
        show(usernameStep, step == AuthDemoFlow.SignUpStep.USERNAME);
        show(passwordStep, step == AuthDemoFlow.SignUpStep.PASSWORD);
        show(emailStep, step == AuthDemoFlow.SignUpStep.EMAIL);
        show(profileStep, step == AuthDemoFlow.SignUpStep.PROFILE);
        show(backButton, step != AuthDemoFlow.SignUpStep.USERNAME);
        List.of(usernameFeedback, passwordFeedback, confirmationFeedback, emailFeedback,
                nameFeedback, addressFeedback, birthDateFeedback).forEach(FieldFeedback::reset);
        message(formError, "");
        authSignUpContinue.setDisable(false);
        clock.stop();
        switch (step) {
            case USERNAME -> {
                stepTitle.setText("Tạo tên đăng nhập");
                stepHelp.setText("Chọn tên đăng nhập để bắt đầu tạo tài khoản.");
                authSignUpContinue.setText("Tiếp tục");
                focus(authSignUpUsername);
            }
            case PASSWORD -> {
                stepTitle.setText("Tạo mật khẩu");
                stepHelp.setText("Tạo mật khẩu đáp ứng các điều kiện bên dưới.");
                authSignUpContinue.setText("Tiếp tục");
                updateRules();
                focus(authSignUpPassword);
            }
            case EMAIL -> {
                stepTitle.setText("Nhập email");
                stepHelp.setText("Nhập email để tiếp tục với bước xác minh.");
                authSignUpContinue.setText("Gửi mã xác minh");
                refreshEmailWait();
                clock.play();
                focus(authSignUpEmail);
            }
            case PROFILE -> {
                stepTitle.setText("Thông tin cá nhân");
                stepHelp.setText("Email đã xác minh. Thêm thông tin để hoàn tất.");
                authSignUpContinue.setText("Hoàn tất đăng ký");
                focus(authSignUpFullName);
            }
        }
    }

    private void updateRules() {
        showPasswordRules(List.of(ruleLength, ruleUpper, ruleLower, ruleDigit, ruleSymbol),
                authSignUpPassword.getText());
    }

    private void clearPasswords() {
        authSignUpPassword.clear();
        authSignUpPasswordConfirm.clear();
        mask(authSignUpPassword, visiblePassword, showPassword);
        mask(authSignUpPasswordConfirm, visibleConfirmation, showConfirmation);
    }

    private long emailWaitSeconds() {
        boolean sameEmail = flow.signUpEmail.equalsIgnoreCase(authSignUpEmail.getText().trim());
        return AuthDemoFlow.secondsUntil(flow.codeIssued, sameEmail ? 60 : 30);
    }

    private void refreshEmailWait() {
        if (flow == null || flow.signUpStep != AuthDemoFlow.SignUpStep.EMAIL) {
            return;
        }
        long wait = emailWaitSeconds();
        message(emailWait, wait == 0 ? "" : "Có thể gửi mã sau " + wait + " giây.");
        authSignUpContinue.setDisable(wait > 0);
    }

    private void error(String text) {
        message(formError, text);
        focus(formError);
    }

    @Override
    public void onLeave() {
        clock.stop();
        clearPasswords();
    }
}
