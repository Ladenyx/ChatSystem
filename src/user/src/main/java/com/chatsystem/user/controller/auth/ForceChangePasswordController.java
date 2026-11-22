package com.chatsystem.user.controller.auth;

import com.chatsystem.user.UserViews;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class ForceChangePasswordController extends AuthFormController {
    @FXML private Label forceTitle, stepTitle, stepHelp, stepCounter;
    @FXML private HBox stepProgress;
    @FXML private VBox temporaryStep, passwordStep;
    @FXML private PasswordField authTemporaryPassword, authNewPassword, authConfirmNewPassword;
    @FXML private TextField visibleTemporaryPassword, visibleNewPassword, visibleConfirmation;
    @FXML private Button showTemporaryPassword, showNewPassword, showConfirmation, authForceContinue;
    private boolean passwordStepVisible;

    @FXML
    private void initialize() {
        bridge(authTemporaryPassword, visibleTemporaryPassword, showTemporaryPassword);
        bridge(authNewPassword, visibleNewPassword, showNewPassword);
        bridge(authConfirmNewPassword, visibleConfirmation, showConfirmation);
    }

    @Override
    void enter() {
        passwordStepVisible = false;
        render();
    }

    @FXML
    private void submit() {
        if (passwordStepVisible) {
            go(UserViews.SIGN_IN);
            return;
        }
        passwordStepVisible = true;
        render();
    }

    @FXML
    private void cancel() {
        go(UserViews.SIGN_IN);
    }

    private void render() {
        show(temporaryStep, !passwordStepVisible);
        show(passwordStep, passwordStepVisible);
        updateProgress(stepCounter, stepProgress, passwordStepVisible ? 2 : 1, 2);
        forceTitle.setText(passwordStepVisible ? "Đặt mật khẩu mới" : "Xác nhận mật khẩu tạm");
        stepTitle.setText(passwordStepVisible ? "Mật khẩu mới" : "Mật khẩu tạm");
        stepHelp.setText(passwordStepVisible
                ? "Chọn mật khẩu mới để hoàn tất luồng khôi phục"
                : "Nhập mật khẩu tạm để tiếp tục đặt mật khẩu mới");
        authForceContinue.setText(passwordStepVisible ? "Lưu mật khẩu" : "Xác nhận");
    }

    @Override
    public void onLeave() {
        authTemporaryPassword.clear();
        authNewPassword.clear();
        authConfirmNewPassword.clear();
    }
}
