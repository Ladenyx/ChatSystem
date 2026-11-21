package com.chatsystem.user.controller.auth;

import com.chatsystem.user.UserViews;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public final class SignInController extends AuthFormController {
    @FXML private TextField authSignInUsername;
    @FXML private PasswordField authSignInPassword;
    @FXML private TextField authSignInPasswordVisible;
    @FXML private Button authSignInShowPassword;
    @FXML private Label authSignInFeedback;
    @FXML private Label authSignInUsernameError;
    @FXML private Label authSignInPasswordError;
    private boolean showingSignInError;

    @FXML
    private void initialize() {
        bridge(authSignInPassword, authSignInPasswordVisible, authSignInShowPassword);
        authSignInUsername.textProperty().addListener((obs, oldValue, newValue) -> {
            usernameError("");
            clearSignInError();
        });
        authSignInPassword.textProperty().addListener((obs, oldValue, newValue) -> {
            passwordError("");
            clearSignInError();
        });
    }

    @Override
    void enter() {
        authSignInUsername.clear();
        authSignInPassword.clear();
        mask(authSignInPassword, authSignInPasswordVisible, authSignInShowPassword);
        showingSignInError = false;
        authSignInFeedback.getStyleClass().setAll("label", "auth-success");
        message(authSignInFeedback, flow.loginNotice);
        flow.loginNotice = "";
        focus(authSignInUsername);
    }

    @FXML
    private void submit() {
        String username = authSignInUsername.getText().trim();
        String password = authSignInPassword.getText();
        showingSignInError = false;
        message(authSignInFeedback, "");
        usernameError(username.isEmpty() ? "Vui lòng nhập tên đăng nhập." : "");
        passwordError(password.isEmpty() ? "Vui lòng nhập mật khẩu." : "");
        if (username.isEmpty() || password.isEmpty()) {
            focus(username.isEmpty() ? authSignInUsername
                    : authSignInPassword.isVisible() ? authSignInPassword : authSignInPasswordVisible);
        } else if (OfflineAuthDemoService.matchesDemoCredentials(username, password)) {
            clearAndGo(UserViews.CONVERSATIONS);
        } else {
            switch (flow.temporaryPasswordStatus(username, password)) {
                case VALID -> {
                    flow.temporaryAccepted = true;
                    go(UserViews.FORCE_CHANGE_PASSWORD);
                }
                case EXPIRED -> showSignInError("Mật khẩu tạm không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu lại.");
                case INVALID -> showSignInError("Tên đăng nhập hoặc mật khẩu không đúng.");
            }
        }
    }

    private void usernameError(String text) {
        message(authSignInUsernameError, text);
        invalid(authSignInUsername, !text.isEmpty());
    }

    private void passwordError(String text) {
        message(authSignInPasswordError, text);
        invalid(authSignInPassword, !text.isEmpty());
        invalid(authSignInPasswordVisible, !text.isEmpty());
    }

    private void clearSignInError() {
        if (showingSignInError) {
            showingSignInError = false;
            message(authSignInFeedback, "");
        }
    }

    private void showSignInError(String text) {
        authSignInPassword.clear();
        mask(authSignInPassword, authSignInPasswordVisible, authSignInShowPassword);
        showingSignInError = true;
        authSignInFeedback.getStyleClass().setAll("label", "auth-error");
        message(authSignInFeedback, text);
        focus(authSignInFeedback);
    }

    @FXML
    private void openSignUp() {
        clearAndGo(UserViews.SIGN_UP);
    }

    @FXML
    private void openForgotPassword() {
        String username = authSignInUsername.getText().trim();
        flow.recoveryName = username;
        flow.temporaryAccepted = false;
        go(UserViews.FORGOT_PASSWORD);
    }

    @Override
    public void onLeave() {
        authSignInPassword.clear();
        mask(authSignInPassword, authSignInPasswordVisible, authSignInShowPassword);
    }
}
