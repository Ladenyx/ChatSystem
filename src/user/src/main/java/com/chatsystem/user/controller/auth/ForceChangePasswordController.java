package com.chatsystem.user.controller.auth;

import com.chatsystem.user.UserViews;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public final class ForceChangePasswordController extends AuthFormController {
    @FXML private Label forceTitle;
    @FXML private Label stepTitle;
    @FXML private Label stepHelp;
    @FXML private VBox temporaryStep;
    @FXML private VBox passwordStep;
    @FXML private PasswordField authTemporaryPassword;
    @FXML private TextField visibleTemporaryPassword;
    @FXML private Button showTemporaryPassword;
    @FXML private PasswordField authNewPassword;
    @FXML private TextField visibleNewPassword;
    @FXML private Button showNewPassword;
    @FXML private PasswordField authConfirmNewPassword;
    @FXML private TextField visibleConfirmation;
    @FXML private Button showConfirmation;
    @FXML private Label ruleLength;
    @FXML private Label ruleUpper;
    @FXML private Label ruleLower;
    @FXML private Label ruleDigit;
    @FXML private Label ruleSymbol;
    @FXML private Label formError;
    @FXML private Label authTemporaryPasswordError;
    @FXML private Label authNewPasswordError;
    @FXML private Label authConfirmNewPasswordError;
    @FXML private Button authForceContinue;
    private FieldFeedback temporaryFeedback;
    private FieldFeedback passwordFeedback;
    private FieldFeedback confirmationFeedback;

    @FXML
    private void initialize() {
        bridge(authTemporaryPassword, visibleTemporaryPassword, showTemporaryPassword);
        bridge(authNewPassword, visibleNewPassword, showNewPassword);
        bridge(authConfirmNewPassword, visibleConfirmation, showConfirmation);
        authNewPassword.textProperty().addListener((obs, oldValue, value) -> updateRules());
        temporaryFeedback = new FieldFeedback(authTemporaryPasswordError,
                () -> authTemporaryPassword.getText().isEmpty() ? "Nhập mật khẩu tạm." : "",
                authTemporaryPassword.textProperty(), authTemporaryPassword, visibleTemporaryPassword);
        passwordFeedback = new FieldFeedback(authNewPasswordError, this::newPasswordError,
                authNewPassword.textProperty(), authNewPassword, visibleNewPassword);
        confirmationFeedback = new FieldFeedback(authConfirmNewPasswordError, this::confirmationError,
                authConfirmNewPassword.textProperty(), authConfirmNewPassword, visibleConfirmation);
        confirmationFeedback.dependsOn(authNewPassword.textProperty());
    }

    @Override
    void enter() {
        renderStep();
    }

    @FXML
    private void submit() {
        message(formError, "");
        if (!flow.temporaryAccepted) {
            confirmTemporaryPassword();
            return;
        }
        if (!flow.recoveryActive()) {
            error("Yêu cầu đã hết hạn. Quay lại đăng nhập để bắt đầu lại.");
            return;
        }
        boolean passwordValid = passwordFeedback.validate();
        boolean confirmationValid = confirmationFeedback.validate();
        if (!passwordValid) {
            focus(authNewPassword.isVisible() ? authNewPassword : visibleNewPassword);
        } else if (!confirmationValid) {
            focus(authConfirmNewPassword.isVisible() ? authConfirmNewPassword : visibleConfirmation);
        } else {
            finish("Đã hoàn tất đặt mật khẩu mẫu. Tài khoản demo không thay đổi.");
        }
    }

    private void confirmTemporaryPassword() {
        if (!temporaryFeedback.validate()) {
            focus(authTemporaryPassword.isVisible() ? authTemporaryPassword : visibleTemporaryPassword);
            return;
        }
        switch (flow.temporaryPasswordStatus(flow.recoveryName, authTemporaryPassword.getText())) {
            case INVALID -> {
                authTemporaryPassword.clear();
                mask(authTemporaryPassword, visibleTemporaryPassword, showTemporaryPassword);
                temporaryFeedback.reject("Mật khẩu tạm không hợp lệ.");
                focus(authTemporaryPassword);
            }
            case EXPIRED -> {
                authTemporaryPassword.clear();
                mask(authTemporaryPassword, visibleTemporaryPassword, showTemporaryPassword);
                temporaryFeedback.reject("Mật khẩu tạm đã hết hạn.");
                focus(authTemporaryPassword);
            }
            case VALID -> {
                clearPasswords();
                flow.temporaryAccepted = true;
                renderStep();
            }
        }
    }

    @FXML
    private void cancel() {
        flow.temporaryAccepted = false;
        go(UserViews.SIGN_IN);
    }

    private void renderStep() {
        boolean accepted = flow.temporaryAccepted;
        show(temporaryStep, !accepted);
        show(passwordStep, accepted);
        temporaryFeedback.reset();
        passwordFeedback.reset();
        confirmationFeedback.reset();
        message(formError, "");
        forceTitle.setText(accepted ? "Đặt mật khẩu mới" : "Xác nhận mật khẩu tạm");
        stepTitle.setText(accepted ? "Mật khẩu mới" : "Mật khẩu tạm");
        stepHelp.setText(accepted ? "Chọn mật khẩu mới để hoàn tất luồng khôi phục."
                : "Nhập mật khẩu tạm để tiếp tục đặt mật khẩu mới.");
        authForceContinue.setText(accepted ? "Lưu mật khẩu" : "Xác nhận");
        updateRules();
        focus(accepted ? authNewPassword : authTemporaryPassword);
    }

    private String newPasswordError() {
        String password = authNewPassword.getText();
        if (password.isEmpty()) {
            return "Nhập mật khẩu mới.";
        }
        return validPassword(password) ? "" : "Mật khẩu mới chưa đáp ứng đủ điều kiện.";
    }

    private String confirmationError() {
        String confirmation = authConfirmNewPassword.getText();
        if (confirmation.isEmpty()) {
            return "Nhập lại mật khẩu.";
        }
        return confirmation.equals(authNewPassword.getText()) ? "" : "Mật khẩu chưa khớp.";
    }

    private void updateRules() {
        showPasswordRules(List.of(ruleLength, ruleUpper, ruleLower, ruleDigit, ruleSymbol), authNewPassword.getText());
    }

    private void error(String text) {
        message(formError, text);
        focus(formError);
    }

    private void clearPasswords() {
        authTemporaryPassword.clear();
        authNewPassword.clear();
        authConfirmNewPassword.clear();
        mask(authTemporaryPassword, visibleTemporaryPassword, showTemporaryPassword);
        mask(authNewPassword, visibleNewPassword, showNewPassword);
        mask(authConfirmNewPassword, visibleConfirmation, showConfirmation);
    }

    @Override
    public void onLeave() {
        clearPasswords();
    }
}
