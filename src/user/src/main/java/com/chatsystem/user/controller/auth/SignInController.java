package com.chatsystem.user.controller.auth;

import com.chatsystem.user.UserViews;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public final class SignInController extends AuthFormController {
    @FXML private PasswordField authSignInPassword;
    @FXML private TextField authSignInPasswordVisible;
    @FXML private Button authSignInShowPassword;
    @FXML private void initialize() { bridge(authSignInPassword, authSignInPasswordVisible, authSignInShowPassword); }
    @Override void enter() {
        navigator.getRoot().getProperties().remove(SIGN_UP_STEP);
        navigator.getRoot().getProperties().remove(SIGN_UP_EMAIL);
    }
    // UI-only navigation, not authentication.
    @FXML private void submit() { go(UserViews.CONVERSATIONS); }
    @FXML private void openSignUp() { go(UserViews.SIGN_UP); }
    @FXML private void openForgotPassword() { go(UserViews.FORGOT_PASSWORD); }
    @Override public void onLeave() { authSignInPassword.clear(); }
}
