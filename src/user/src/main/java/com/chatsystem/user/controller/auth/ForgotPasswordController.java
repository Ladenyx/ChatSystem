package com.chatsystem.user.controller.auth;

import com.chatsystem.user.UserViews;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public final class ForgotPasswordController extends AuthFormController {
    private enum Step { USERNAME, EMAIL, CHECK_EMAIL }

    @FXML private Label stepTitle;
    @FXML private Label stepHelp;
    @FXML private Label formError;
    @FXML private VBox usernameStep;
    @FXML private VBox emailStep;
    @FXML private VBox checkEmailStep;
    @FXML private TextField authForgotUsername;
    @FXML private TextField authForgotEmail;
    @FXML private Label authForgotUsernameError;
    @FXML private Label authForgotEmailError;
    @FXML private Label authForgotMaskedEmail;
    @FXML private Button authForgotContinue;
    @FXML private Button authForgotResend;
    private Step step = Step.USERNAME;
    private String emailHint = "";
    private FieldFeedback usernameFeedback;
    private FieldFeedback emailFeedback;
    private Timeline clock;

    @FXML
    private void initialize() {
        usernameFeedback = new FieldFeedback(authForgotUsernameError,
                () -> authForgotUsername.getText().isBlank() ? "Nhập tên đăng nhập." : "",
                authForgotUsername.textProperty(), authForgotUsername);
        emailFeedback = new FieldFeedback(authForgotEmailError, this::recoveryEmailError,
                authForgotEmail.textProperty(), authForgotEmail);
        clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> refreshResend()));
        clock.setCycleCount(Timeline.INDEFINITE);
    }

    @Override
    void enter() {
        authForgotUsername.setText(flow.recoveryName);
        renderStep();
    }

    @FXML
    private void submit() {
        message(formError, "");
        switch (step) {
            case USERNAME -> {
                if (!usernameFeedback.validate()) {
                    focus(authForgotUsername);
                    return;
                }
                String username = authForgotUsername.getText().trim();
                String hint = OfflineAuthDemoService.recoveryEmailHint(username);
                if (hint.isEmpty()) {
                    usernameFeedback.reject("Thông tin chưa đúng.");
                    focus(authForgotUsername);
                } else {
                    flow.recoveryName = username;
                    emailHint = hint;
                    step = Step.EMAIL;
                    renderStep();
                }
            }
            case EMAIL -> {
                if (!emailFeedback.validate()) {
                    focus(authForgotEmail);
                } else if (!OfflineAuthDemoService.verifyRecoveryEmail(flow.recoveryName, authForgotEmail.getText().trim())) {
                    emailFeedback.reject("Thông tin chưa đúng.");
                    focus(authForgotEmail);
                } else {
                    if (AuthDemoFlow.secondsUntil(flow.recoveryIssued, 60) == 0) {
                        flow.issueTemporaryPassword();
                    }
                    step = Step.CHECK_EMAIL;
                    renderStep();
                }
            }
            case CHECK_EMAIL -> {
                if (flow.recoveryActive()) {
                    go(UserViews.FORCE_CHANGE_PASSWORD);
                } else {
                    error("Yêu cầu mẫu đã hết hạn. Hãy chọn Gửi lại.");
                }
            }
        }
    }

    private String recoveryEmailError() {
        String email = authForgotEmail.getText().trim();
        if (!validEmail(email)) {
            return "Nhập email hợp lệ.";
        }
        return matchesVisibleEmailHint(email, emailHint) ? "" : "Email không khớp với gợi ý.";
    }

    /** The UI can only compare the first visible character and domain; remaining content is unknown. */
    private static boolean matchesVisibleEmailHint(String email, String hint) {
        if (hint.isEmpty()) {
            return true;
        }
        String firstCharacter = email.substring(0, email.offsetByCodePoints(0, 1));
        return firstCharacter.equalsIgnoreCase(hint.substring(0, hint.offsetByCodePoints(0, 1)))
                && email.substring(email.indexOf('@')).equalsIgnoreCase(hint.substring(hint.lastIndexOf('@')));
    }

    @FXML
    private void resend() {
        if (step == Step.CHECK_EMAIL && AuthDemoFlow.secondsUntil(flow.recoveryIssued, 60) == 0) {
            flow.issueTemporaryPassword();
            message(formError, "");
            refreshResend();
        }
    }

    @FXML
    private void cancel() {
        if (flow.recoveryActive()) {
            go(UserViews.SIGN_IN);
        } else {
            clearAndGo(UserViews.SIGN_IN);
        }
    }

    private void renderStep() {
        show(usernameStep, step == Step.USERNAME);
        show(emailStep, step == Step.EMAIL);
        show(checkEmailStep, step == Step.CHECK_EMAIL);
        usernameFeedback.reset();
        emailFeedback.reset();
        message(formError, "");
        switch (step) {
            case USERNAME -> {
                stepTitle.setText("Nhập tên đăng nhập");
                stepHelp.setText("Nhập tên đăng nhập của tài khoản cần khôi phục.");
                authForgotContinue.setText("Tiếp tục");
                focus(authForgotUsername);
            }
            case EMAIL -> {
                stepTitle.setText("Xác nhận email");
                stepHelp.setText("Nhập đầy đủ email đã liên kết với tài khoản.");
                authForgotMaskedEmail.setText("Email gợi ý: " + emailHint);
                authForgotContinue.setText("Gửi");
                focus(authForgotEmail);
            }
            case CHECK_EMAIL -> {
                stepTitle.setText("Kiểm tra email");
                stepHelp.setText("Tiếp tục khi bạn đã có mật khẩu tạm.");
                authForgotContinue.setText("Tiếp tục");
                refreshResend();
                clock.play();
                focus(authForgotContinue);
            }
        }
    }

    private void refreshResend() {
        long wait = AuthDemoFlow.secondsUntil(flow.recoveryIssued, 60);
        authForgotResend.setDisable(wait > 0);
        authForgotResend.setText(wait > 0 ? "Gửi lại sau %02d:%02d".formatted(wait / 60, wait % 60) : "Gửi lại");
    }

    private void error(String text) {
        message(formError, text);
        focus(formError);
    }

    @Override
    public void onLeave() {
        clock.stop();
    }
}
