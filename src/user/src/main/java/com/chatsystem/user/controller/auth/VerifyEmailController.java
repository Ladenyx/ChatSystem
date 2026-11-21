package com.chatsystem.user.controller.auth;

import com.chatsystem.user.UserViews;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class VerifyEmailController extends AuthFormController {
    @FXML private Label maskedEmail;
    @Override void enter() {
        Object email = navigator.getRoot().getProperties().get(SIGN_UP_EMAIL);
        String value = email instanceof String text ? text : "";
        int at = value.indexOf('@');
        maskedEmail.setText(at > 0 ? value.substring(0, 1) + "***" + value.substring(at) : "");
    }
    @FXML private void verify() { SignUpController.continueAfterVerification(navigator); go(UserViews.SIGN_UP); }
    // No email is sent in the UI-only version.
    @FXML private void resend() { }
    @FXML private void changeEmail() { SignUpController.changeEmail(navigator); go(UserViews.SIGN_UP); }
    @FXML private void cancel() { go(UserViews.SIGN_IN); }
}
