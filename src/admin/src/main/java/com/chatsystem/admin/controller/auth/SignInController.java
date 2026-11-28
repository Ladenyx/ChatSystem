package com.chatsystem.admin.controller.auth;

import com.chatsystem.admin.AdminApplication;
import com.chatsystem.admin.AdminViews;
import com.chatsystem.admin.service.AdminSessionService;
import com.chatsystem.common.dto.auth.AuthNextAction;
import com.chatsystem.desktop.async.SubmitActionGuard;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public final class SignInController implements ViewController {
    @FXML private TextField signInUsername;
    @FXML private PasswordField signInPassword;
    @FXML private Label signInError;
    @FXML private Button signInSubmit;

    private final SimpleBooleanProperty submitting = new SimpleBooleanProperty(false);
    private SubmitActionGuard submitGuard;
    private ViewNavigator navigator;

    @FXML
    private void initialize() {
        submitGuard = new SubmitActionGuard(
                () -> AdminApplication.getServices().adminSessionService(),
                busy -> {
                    submitting.set(busy);
                    signInSubmit.setText(busy ? "Đang đăng nhập..." : "Đăng nhập");
                }
        );
        signInSubmit.disableProperty().bind(Bindings.createBooleanBinding(
                () -> submitting.get()
                        || signInUsername.getText().isBlank()
                        || signInPassword.getLength() == 0,
                submitting,
                signInUsername.textProperty(),
                signInPassword.textProperty()
        ));
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
    }

    @Override
    public void onLeave() {
        if (submitGuard != null) {
            submitGuard.cancel();
        }
        signInPassword.clear();
        showError(null);
    }

    @FXML
    private void openOverview() {
        AdminSessionService sessionService = AdminApplication.getServices().adminSessionService();
        if (!sessionService.isLiveBackend()) {
            navigator.reset(AdminViews.OVERVIEW);
            return;
        }
        showError(null);
        String username = signInUsername.getText().trim();
        String password = signInPassword.getText();
        submitGuard.submit(
                () -> sessionService.login(username, password),
                response -> {
                    if (response.nextAction() == AuthNextAction.SESSION_GRANTED) {
                        navigator.reset(AdminViews.OVERVIEW);
                    } else {
                        showError("Tài khoản cần hoàn tất xác minh trước khi đăng nhập quản trị.");
                    }
                },
                failure -> showError(failure.message())
        );
    }

    private void showError(String message) {
        if (signInError == null) {
            return;
        }
        boolean visible = message != null && !message.isBlank();
        signInError.setText(visible ? message : "");
        signInError.setVisible(visible);
        signInError.setManaged(visible);
    }
}
