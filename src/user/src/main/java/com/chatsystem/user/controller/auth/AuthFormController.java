package com.chatsystem.user.controller.auth;

import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

abstract class AuthFormController implements ViewController {
    private static final Object MASKED_GRAPHIC = new Object();
    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
    private static final String EMAIL_ATOM = "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+";
    private static final String DOMAIN_LABEL = "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?";
    private static final Pattern EMAIL = Pattern.compile(
            EMAIL_ATOM + "(?:\\." + EMAIL_ATOM + ")*@" + DOMAIN_LABEL + "(?:\\." + DOMAIN_LABEL + ")+");
    private static final List<String> PASSWORD_RULES = List.of(
            "8–128 ký tự", "Có chữ hoa", "Có chữ thường", "Có chữ số", "Có ký tự đặc biệt");

    ViewNavigator navigator;
    AuthDemoFlow flow;

    @Override
    public final void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
        flow = AuthDemoFlow.get(navigator);
        enter();
    }

    abstract void enter();

    final void go(View view) {
        navigator.reset(view);
    }

    final void clearAndGo(View view) {
        AuthDemoFlow.clear(navigator);
        go(view);
    }

    final void finish(String notice) {
        AuthDemoFlow.clear(navigator);
        AuthDemoFlow.get(navigator).loginNotice = notice;
        go(UserViews.SIGN_IN);
    }

    static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    static void message(Label label, String message) {
        label.setText(message);
        show(label, !message.isEmpty());
    }

    static void invalid(Node control, boolean invalid) {
        control.pseudoClassStateChanged(INVALID, invalid);
    }

    static void focus(Node node) {
        Platform.runLater(() -> {
            if (node.getScene() != null && node.isVisible()) {
                node.requestFocus();
            }
        });
    }

    static boolean validEmail(String email) {
        return emailError(email).isEmpty();
    }

    static String emailError(String email) {
        if (email.isBlank()) {
            return "Vui lòng nhập email.";
        }
        if (characterCount(email) > 254) {
            return "Email không được vượt quá 254 ký tự.";
        }
        return EMAIL.matcher(email).matches() ? "" : "Vui lòng nhập email hợp lệ.";
    }

    static String maskedEmail(String email) {
        int at = email.indexOf('@');
        return at < 1 ? "" : email.substring(0, email.offsetByCodePoints(0, 1)) + "***" + email.substring(at);
    }

    static int characterCount(String value) {
        return value.codePointCount(0, value.length());
    }

    static void bridge(PasswordField password, TextField visible, Button toggle) {
        visible.textProperty().bindBidirectional(password.textProperty());
        if (toggle.getGraphic() != null) {
            toggle.getProperties().put(MASKED_GRAPHIC, toggle.getGraphic());
        }
        toggle.setOnAction(event -> {
            boolean reveal = !visible.isVisible();
            TextField previous = reveal ? password : visible;
            TextField next = reveal ? visible : password;
            int anchor = previous.getAnchor();
            int position = previous.getCaretPosition();
            show(password, !reveal);
            show(visible, reveal);
            next.selectRange(anchor, position);
            updateRevealLabel(toggle, reveal);
        });
    }

    static void mask(PasswordField password, TextField visible, Button toggle) {
        show(password, true);
        show(visible, false);
        updateRevealLabel(toggle, false);
    }

    private static void updateRevealLabel(Button toggle, boolean revealed) {
        toggle.setText(revealed ? "Ẩn" : "Hiện");
        toggle.setAccessibleText(revealed ? "Ẩn mật khẩu" : "Hiện mật khẩu");
        if (toggle.getProperties().get(MASKED_GRAPHIC) instanceof Node maskedGraphic) {
            if (revealed) {
                FontIcon eye = new FontIcon(Feather.EYE);
                eye.setIconSize(18);
                toggle.setGraphic(eye);
            } else {
                toggle.setGraphic(maskedGraphic);
            }
        }
    }

    static void showPasswordRules(List<Label> labels, String password) {
        boolean[] matches = passwordRules(password);
        for (int i = 0; i < labels.size(); i++) {
            labels.get(i).setText((matches[i] ? "✓ " : "○ ") + PASSWORD_RULES.get(i));
        }
    }

    static boolean validPassword(String password) {
        for (boolean matches : passwordRules(password)) {
            if (!matches) {
                return false;
            }
        }
        return true;
    }

    private static boolean[] passwordRules(String password) {
        int length = characterCount(password);
        return new boolean[] {
            length >= 8 && length <= 128,
            password.chars().anyMatch(ch -> ch >= 'A' && ch <= 'Z'),
            password.chars().anyMatch(ch -> ch >= 'a' && ch <= 'z'),
            password.chars().anyMatch(ch -> ch >= '0' && ch <= '9'),
            password.chars().anyMatch(ch -> ch >= '!' && ch <= '~' && !Character.isLetterOrDigit(ch))
        };
    }

    /** Tracks only whether a field was edited/submitted, never input contents. */
    static final class FieldFeedback {
        private final Label label;
        private final Supplier<String> error;
        private final List<Node> controls;
        private boolean touched;

        FieldFeedback(Label label, Supplier<String> error, ObservableValue<?> edited, Node... controls) {
            this.label = label;
            this.error = error;
            this.controls = List.of(controls);
            edited.addListener((obs, oldValue, value) -> validate());
        }

        void dependsOn(ObservableValue<?> edited) {
            edited.addListener((obs, oldValue, value) -> {
                if (touched) {
                    validate();
                }
            });
        }

        boolean validate() {
            touched = true;
            String text = error.get();
            showError(text);
            return text.isEmpty();
        }

        void reject(String text) {
            touched = true;
            showError(text);
        }

        void reset() {
            touched = false;
            showError("");
        }

        private void showError(String text) {
            message(label, text);
            controls.forEach(control -> invalid(control, !text.isEmpty()));
        }
    }
}
