package com.chatsystem.user.controller.auth;

import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/** Shared presentation helpers; authentication is not implemented in version 1. */
abstract class AuthFormController implements ViewController {
    static final String SIGN_UP_STEP = AuthFormController.class.getName() + ".signUpStep";
    static final String SIGN_UP_EMAIL = AuthFormController.class.getName() + ".signUpEmail";
    ViewNavigator navigator;

    @Override
    public final void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
        enter();
    }

    abstract void enter();

    final void go(View view) {
        navigator.reset(view);
    }

    static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    static void updateProgress(Label counter, HBox progressBar, int currentStep, int totalSteps) {
        counter.setText("Bước " + currentStep + " / " + totalSteps);
        var segments = progressBar.getChildren();
        for (int i = 0; i < segments.size(); i++) {
            var classes = segments.get(i).getStyleClass();
            if (i < currentStep) {
                if (!classes.contains("active")) {
                    classes.add("active");
                }
            } else {
                classes.remove("active");
            }
        }
    }

    static void bridge(PasswordField password, TextField visible, Button toggle) {
        Node hiddenIcon = toggle.getGraphic();
        FontIcon shownIcon = new FontIcon(Feather.EYE);
        shownIcon.setIconSize(16);
        shownIcon.setMouseTransparent(true);
        visible.textProperty().bindBidirectional(password.textProperty());
        toggle.setOnAction(event -> {
            boolean reveal = !visible.isVisible();
            show(password, !reveal);
            show(visible, reveal);
            toggle.setGraphic(reveal ? shownIcon : hiddenIcon);
            toggle.setText(reveal ? "Ẩn" : "Hiện");
            toggle.setAccessibleText(reveal ? "Ẩn mật khẩu" : "Hiện mật khẩu");
        });
    }
}
