package com.chatsystem.admin.controller.auth;

import com.chatsystem.admin.AdminViews;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public final class SignInController implements ViewController {
    @FXML private TextField signInUsername;
    @FXML private PasswordField signInPassword;
    @FXML private Button signInSubmit;
    private ViewNavigator navigator;

    @FXML
    private void initialize() {
        signInSubmit.disableProperty().bind(Bindings.createBooleanBinding(
                () -> signInUsername.getText().isBlank() || signInPassword.getLength() == 0,
                signInUsername.textProperty(), signInPassword.textProperty()));
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
    }

    @Override
    public void onLeave() {
        signInPassword.clear();
    }

    @FXML
    private void openOverview() {
        navigator.reset(AdminViews.OVERVIEW);
    }
}
