package com.chatsystem.user.controller.auth;

import com.chatsystem.user.UserViews;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class ForgotPasswordController extends AuthFormController {
    private enum Step { USERNAME, EMAIL, CHECK_EMAIL }
    @FXML private Label stepTitle, stepHelp, authForgotMaskedEmail;
    @FXML private VBox usernameStep, emailStep, checkEmailStep;
    @FXML private Button authForgotContinue;
    private Step step;
    @Override void enter() { step = Step.USERNAME; render(); }
    @FXML private void submit() {
        switch (step) {
            case USERNAME -> step = Step.EMAIL;
            case EMAIL -> step = Step.CHECK_EMAIL;
            case CHECK_EMAIL -> { go(UserViews.FORCE_CHANGE_PASSWORD); return; }
        }
        render();
    }
    @FXML private void resend() { }
    @FXML private void cancel() { go(UserViews.SIGN_IN); }
    private void render() {
        show(usernameStep, step == Step.USERNAME);
        show(emailStep, step == Step.EMAIL);
        show(checkEmailStep, step == Step.CHECK_EMAIL);
        switch (step) {
            case USERNAME -> { stepTitle.setText("Nhập tên đăng nhập"); stepHelp.setText("Nhập tên đăng nhập của tài khoản cần khôi phục."); authForgotContinue.setText("Tiếp tục"); }
            case EMAIL -> { stepTitle.setText("Xác nhận email"); stepHelp.setText("Nhập đầy đủ email đã liên kết với tài khoản."); authForgotMaskedEmail.setText("Email gợi ý: a***@example.com"); authForgotContinue.setText("Gửi"); }
            case CHECK_EMAIL -> { stepTitle.setText("Kiểm tra email"); stepHelp.setText("Tiếp tục khi bạn đã có mật khẩu tạm."); authForgotContinue.setText("Tiếp tục"); }
        }
    }
}
