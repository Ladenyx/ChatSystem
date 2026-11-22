package com.chatsystem.admin.controller.dialogs;

import atlantafx.base.theme.Styles;
import java.util.Objects;
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
        Button submitButton = (Button) pane.lookupButton(submit);
        submitButton.setId("submitUserDialog");
        submitButton.getStyleClass().add(destructive ? Styles.DANGER : "admin-dialog-primary");
        if (destructive) {
            submitButton.setDefaultButton(false);
            cancelButton.requestFocus();
        }
        return submitButton;
    }
}
