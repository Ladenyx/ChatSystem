package com.chatsystem.admin.controller.dialogs;

import atlantafx.base.theme.Styles;
import java.util.Objects;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

final class UserDialogButtons {
    private UserDialogButtons() {
    }

    static Button install(DialogPane pane, String submitText, boolean destructive) {
        pane.getStylesheets().add(Objects.requireNonNull(UserDialogButtons.class.getResource(
                "/com/chatsystem/admin/css/user-dialogs.css")).toExternalForm());
        pane.getStyleClass().add("admin-user-dialog");

        var cancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        var submit = new ButtonType(submitText, ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().setAll(cancel, submit);
        Button cancelButton = (Button) pane.lookupButton(cancel);
        cancelButton.setId("cancelUserDialog");
        cancelButton.getStyleClass().add("admin-dialog-cancel");
        ButtonBar.setButtonUniformSize(cancelButton, false);
        Button submitButton = (Button) pane.lookupButton(submit);
        submitButton.setId("submitUserDialog");
        if (destructive) {
            submitButton.getStyleClass().addAll(Styles.DANGER, "admin-dialog-danger");
            submitButton.setDefaultButton(false);
            cancelButton.setDefaultButton(true);
            Platform.runLater(cancelButton::requestFocus);
            cancelButton.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    cancelButton.requestFocus();
                }
            });
        } else {
            submitButton.getStyleClass().add("admin-dialog-primary");
        }
        ButtonBar.setButtonUniformSize(submitButton, false);
        return submitButton;
    }
}
