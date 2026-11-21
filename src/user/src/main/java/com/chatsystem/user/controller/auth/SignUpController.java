package com.chatsystem.user.controller.auth;

import com.chatsystem.user.UserViews;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public final class SignUpController extends AuthFormController {
    private enum Step { USERNAME, PASSWORD, EMAIL, PROFILE }
    @FXML private Label stepTitle, stepHelp;
    @FXML private VBox usernameStep, passwordStep, emailStep, profileStep;
    @FXML private PasswordField authSignUpPassword, authSignUpPasswordConfirm;
    @FXML private TextField visiblePassword, visibleConfirmation, authSignUpEmail;
    @FXML private Button showPassword, showConfirmation, clearGenderButton, authSignUpContinue, backButton;
    @FXML private ToggleGroup gender;
    private Step step;

    @FXML private void initialize() {
        bridge(authSignUpPassword, visiblePassword, showPassword);
        bridge(authSignUpPasswordConfirm, visibleConfirmation, showConfirmation);
        clearGenderButton.visibleProperty().bind(gender.selectedToggleProperty().isNotNull());
        clearGenderButton.managedProperty().bind(clearGenderButton.visibleProperty());
    }
    @Override void enter() {
        Object saved = navigator.getRoot().getProperties().remove(SIGN_UP_STEP);
        step = saved instanceof Step value ? value : Step.USERNAME;
        Object email = navigator.getRoot().getProperties().get(SIGN_UP_EMAIL);
        if (email instanceof String value) authSignUpEmail.setText(value);
        render();
    }
    @FXML private void submit() {
        switch (step) {
            case USERNAME -> step = Step.PASSWORD;
            case PASSWORD -> step = Step.EMAIL;
            case EMAIL -> {
                navigator.getRoot().getProperties().put(SIGN_UP_EMAIL, authSignUpEmail.getText().trim());
                go(UserViews.VERIFY_EMAIL);
                return;
            }
            case PROFILE -> { go(UserViews.SIGN_IN); return; }
        }
        render();
    }
    @FXML private void back() {
        switch (step) {
            case USERNAME, PROFILE -> { cancel(); return; }
            case PASSWORD -> step = Step.USERNAME;
            case EMAIL -> step = Step.PASSWORD;
        }
        render();
    }
    @FXML private void cancel() { go(UserViews.SIGN_IN); }
    @FXML private void clearGender() { gender.selectToggle(null); }
    static void continueAfterVerification(com.chatsystem.desktop.navigation.ViewNavigator navigator) {
        navigator.getRoot().getProperties().put(SIGN_UP_STEP, Step.PROFILE);
    }
    static void changeEmail(com.chatsystem.desktop.navigation.ViewNavigator navigator) {
        navigator.getRoot().getProperties().put(SIGN_UP_STEP, Step.EMAIL);
    }
    private void render() {
        show(usernameStep, step == Step.USERNAME);
        show(passwordStep, step == Step.PASSWORD);
        show(emailStep, step == Step.EMAIL);
        show(stepHelp, step == Step.EMAIL);
        show(profileStep, step == Step.PROFILE);
        show(backButton, step != Step.USERNAME);
        switch (step) {
            case USERNAME -> { stepTitle.setText("Tạo tên đăng nhập"); stepHelp.setText("Chọn tên đăng nhập để bắt đầu tạo tài khoản."); authSignUpContinue.setText("Tiếp tục"); }
            case PASSWORD -> { stepTitle.setText("Tạo mật khẩu"); stepHelp.setText("Tạo mật khẩu đáp ứng các điều kiện bên dưới."); authSignUpContinue.setText("Tiếp tục"); }
            case EMAIL -> {
                stepTitle.setText("Nhập email");
                stepHelp.setText("Tiếp tục để xem màn xác minh. Bản xem trước không gửi mã.");
                authSignUpContinue.setText("Tiếp tục xác minh");
            }
            case PROFILE -> { stepTitle.setText("Thông tin cá nhân"); stepHelp.setText("Thêm thông tin để hoàn tất."); authSignUpContinue.setText("Hoàn tất đăng ký"); }
        }
    }
    @Override public void onLeave() { authSignUpPassword.clear(); authSignUpPasswordConfirm.clear(); }
}
