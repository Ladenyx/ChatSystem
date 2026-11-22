package com.chatsystem.user.controller.auth;

import com.chatsystem.user.UserViews;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class ForgotPasswordController extends AuthFormController {
    private enum Step { USERNAME, EMAIL, CHECK_EMAIL }

    @FXML private Label stepTitle, stepHelp, authForgotMaskedEmail, stepCounter;
    @FXML private HBox stepProgress;
    @FXML private VBox usernameStep, emailStep, checkEmailStep;
    @FXML private Button authForgotContinue;
    private Step step;

    @Override
    void enter() {
        step = Step.USERNAME;
        render();
    }

    @FXML
    private void submit() {
        switch (step) {
            case USERNAME -> step = Step.EMAIL;
            case EMAIL -> step = Step.CHECK_EMAIL;
            case CHECK_EMAIL -> {
                go(UserViews.FORCE_CHANGE_PASSWORD);
                return;
            }
        }
        render();
    }

    @FXML
    private void cancel() {
        go(UserViews.SIGN_IN);
    }

    private void render() {
        show(usernameStep, step == Step.USERNAME);
        show(emailStep, step == Step.EMAIL);
        show(checkEmailStep, step == Step.CHECK_EMAIL);
        updateProgress(stepCounter, stepProgress, step.ordinal() + 1, Step.values().length);
        switch (step) {
            case USERNAME -> {
                stepTitle.setText("Nhập tên đăng nhập");
                stepHelp.setText("Nhập tên đăng nhập của tài khoản cần khôi phục");
                authForgotContinue.setText("Tiếp tục");
            }
            case EMAIL -> {
                stepTitle.setText("Xác nhận email");
                authForgotMaskedEmail.setText("Email liên kết: a***@example.test");
                authForgotContinue.setText("Gửi mật khẩu tạm");
            }
            case CHECK_EMAIL -> {
                stepTitle.setText("Kiểm tra email");
                stepHelp.setText("Mật khẩu tạm đã được gửi tới email liên kết của bạn. Chọn Tiếp tục để đặt mật khẩu mới.");
                authForgotContinue.setText("Tiếp tục");
            }
        }
    }
}
