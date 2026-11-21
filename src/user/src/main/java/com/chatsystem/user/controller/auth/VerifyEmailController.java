package com.chatsystem.user.controller.auth;

import com.chatsystem.user.UserViews;
import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

public final class VerifyEmailController extends AuthFormController {
    @FXML private Label maskedEmail;
    @FXML private Label feedback;
    @FXML private TextField authVerifyDigit1;
    @FXML private TextField authVerifyDigit2;
    @FXML private TextField authVerifyDigit3;
    @FXML private TextField authVerifyDigit4;
    @FXML private TextField authVerifyDigit5;
    @FXML private TextField authVerifyDigit6;
    @FXML private Button authVerifySubmit;
    @FXML private Button authVerifyResend;
    private List<TextField> digits;
    private Timeline clock;
    private boolean updating;

    @FXML
    private void initialize() {
        digits = List.of(authVerifyDigit1, authVerifyDigit2, authVerifyDigit3,
                authVerifyDigit4, authVerifyDigit5, authVerifyDigit6);
        for (int i = 0; i < digits.size(); i++) {
            setupDigit(digits.get(i), i);
        }
        clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> refresh()));
        clock.setCycleCount(Timeline.INDEFINITE);
    }

    @Override
    void enter() {
        maskedEmail.setText(maskedEmail(flow.signUpEmail));
        updateFeedback("", false, false);
        refresh();
        clock.play();
        focus(authVerifyDigit1);
    }

    private void setupDigit(TextField field, int index) {
        field.setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.isContentChange() || change.getText().isEmpty()) {
                return change;
            }
            if (change.getText().matches("[0-9]{6}")) {
                String pasted = change.getText();
                Platform.runLater(() -> applyPaste(pasted));
                return null;
            }
            if (!change.getText().matches("[0-9]")) {
                return null;
            }
            change.setRange(0, change.getControlText().length());
            change.selectRange(1, 1);
            return change;
        }));
        field.textProperty().addListener((obs, previous, value) -> {
            if (!updating) {
                if (flow != null && flow.codeActive()) {
                    updateFeedback(code().isEmpty() || code().length() == 6 ? "" : "Nhập đủ 6 chữ số.", false, false);
                }
                refresh();
                if (!value.isEmpty() && index < digits.size() - 1 && field.isFocused()) {
                    digits.get(index + 1).requestFocus();
                }
            }
        });
        field.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE && field.getText().isEmpty() && index > 0) {
                TextField previous = digits.get(index - 1);
                previous.clear();
                previous.requestFocus();
                event.consume();
            }
        });
    }

    private void applyPaste(String code) {
        if (authVerifyDigit1.getScene() == null) {
            return;
        }
        updating = true;
        for (int i = 0; i < digits.size(); i++) {
            digits.get(i).setText(code.substring(i, i + 1));
        }
        updating = false;
        if (flow.codeActive()) {
            updateFeedback("", false, false);
        }
        refresh();
        authVerifyDigit6.requestFocus();
    }

    @FXML
    private void verify() {
        if (authVerifySubmit.isDisabled() || !flow.codeActive()) {
            refresh();
            return;
        }
        if (AuthDemoFlow.DEMO_CODE.equals(code())) {
            flow.emailVerified = true;
            flow.signUpStep = AuthDemoFlow.SignUpStep.PROFILE;
            go(UserViews.SIGN_UP);
        } else {
            flow.codeFailures++;
            updateFeedback(flow.codeFailures < 5 ? "Mã xác thực không đúng." : "Mã không còn hiệu lực.", true, true);
            refresh();
        }
    }

    @FXML
    private void resend() {
        if (flow.signUpActive() && AuthDemoFlow.secondsUntil(flow.codeIssued, 60) == 0) {
            flow.issueCode();
            digits.forEach(TextField::clear);
            updateFeedback("Đã cấp lại mã mẫu. Không có email thật được gửi.", false, false);
            refresh();
            focus(authVerifyDigit1);
        }
    }

    @FXML
    private void changeEmail() {
        if (flow.signUpActive()) {
            flow.signUpStep = AuthDemoFlow.SignUpStep.EMAIL;
            go(UserViews.SIGN_UP);
        } else {
            AuthDemoFlow.clear(navigator);
            go(UserViews.SIGN_UP);
        }
    }

    @FXML
    private void cancel() {
        clearAndGo(UserViews.SIGN_IN);
    }

    private String code() {
        return digits.stream().map(TextField::getText).collect(Collectors.joining());
    }

    private void refresh() {
        if (flow == null) {
            return;
        }
        long wait = AuthDemoFlow.secondsUntil(flow.codeIssued, 60);
        authVerifySubmit.setDisable(!flow.codeActive() || code().length() != 6);
        authVerifyResend.setDisable(!flow.signUpActive() || wait > 0);
        authVerifyResend.setText(wait > 0 ? "Gửi lại sau %02d:%02d".formatted(wait / 60, wait % 60) : "Gửi lại mã");
        if (!flow.signUpActive()) {
            updateFeedback("Yêu cầu đã hết hạn.", true, false);
        } else if (!flow.codeActive() && flow.codeFailures < 5) {
            updateFeedback("Mã đã hết hạn hoặc không còn hiệu lực.", true, true);
        }
    }

    private void updateFeedback(String text, boolean error, boolean invalidCode) {
        feedback.getStyleClass().setAll("label", "auth-inline-error", error ? "auth-error" : "auth-muted");
        message(feedback, text);
        digits.forEach(digit -> invalid(digit, invalidCode));
    }

    @Override
    public void onLeave() {
        clock.stop();
        updating = true;
        digits.forEach(TextField::clear);
        updating = false;
    }
}
