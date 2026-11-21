package com.chatsystem.user.controller.auth;

import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.navigation.UserViewInputs.E2eeKeySetup;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Offline presentation of recovery-code entry. No codes or key material are saved. */
public final class KeySetupController implements ViewController {
    private static final int CODE_LENGTH = 6;
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    private enum Mode {
        CHOOSE, SET_UP, RECOVER
    }

    @FXML private VBox choices;
    @FXML private VBox codeForm;
    @FXML private VBox confirmationGroup;
    @FXML private Label formTitle;
    @FXML private Label formHelp;
    @FXML private Label notice;
    @FXML private HBox digitRow;
    @FXML private HBox confirmRow;
    @FXML private Button keySubmit;

    private final List<PasswordField> digits = new ArrayList<>();
    private final List<PasswordField> confirmation = new ArrayList<>();
    private ViewNavigator navigator;
    private Mode mode = Mode.CHOOSE;
    private boolean updatingDigits;
    private long formRevision;

    @FXML
    private void initialize() {
        createDigits(digitRow, digits, "keyDigit", "Mã khôi phục");
        createDigits(confirmRow, confirmation, "keyConfirm", "Nhập lại mã");
        notice.visibleProperty().bind(notice.textProperty().isNotEmpty());
        notice.managedProperty().bind(notice.visibleProperty());
        showMode(Mode.CHOOSE);
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        this.navigator = navigator;
        if (input == E2eeKeySetup.SET_UP) {
            showMode(Mode.SET_UP);
        } else if (input == E2eeKeySetup.RECOVER) {
            showMode(Mode.RECOVER);
        } else {
            showMode(Mode.CHOOSE);
        }
    }

    @Override
    public void onLeave() {
        navigator = null;
        clearDigits();
        setNotice("", false);
        updateSubmit();
    }

    @FXML
    private void setup() {
        showMode(Mode.SET_UP);
        digits.getFirst().requestFocus();
    }

    @FXML
    private void restore() {
        showMode(Mode.RECOVER);
        digits.getFirst().requestFocus();
    }

    @FXML
    private void cancel() {
        if (navigator != null && !navigator.goBack()) {
            navigator.navigate(UserViews.CONVERSATIONS);
        }
    }

    @FXML
    private void submit() {
        if (mode == Mode.CHOOSE || !complete(digits) || (mode == Mode.SET_UP && !complete(confirmation))) {
            return;
        }
        if (mode == Mode.SET_UP && !confirmationMatches()) {
            setNotice("Mã mẫu nhập lại chưa khớp.", true);
            confirmation.getFirst().requestFocus();
            return;
        }

        boolean setup = mode == Mode.SET_UP;
        clearDigits();
        setNotice(setup
                ? "Đã xem bước thiết lập. Không có mã nào được lưu hoặc khóa nào được tạo."
                : "Đã xem bước khôi phục. Không có khóa hoặc tin nhắn nào được khôi phục.", false);
        updateSubmit();
    }

    private void showMode(Mode next) {
        mode = next;
        clearDigits();
        setNotice("", false);
        setShown(choices, next == Mode.CHOOSE);
        setShown(codeForm, next != Mode.CHOOSE);
        setShown(confirmationGroup, next == Mode.SET_UP);
        if (next == Mode.SET_UP) {
            formTitle.setText("Tạo mã khôi phục 6 số");
            formHelp.setText("Nhập 6 chữ số mẫu và nhập lại để xem bước thiết lập.");
            keySubmit.setText("Xem bước thiết lập");
        } else if (next == Mode.RECOVER) {
            formTitle.setText("Khôi phục bằng mã 6 số");
            formHelp.setText("Nhập 6 chữ số mẫu để xem bước khôi phục.");
            keySubmit.setText("Xem bước khôi phục");
        }
        updateSubmit();
    }

    private void createDigits(HBox row, List<PasswordField> fields, String prefix, String accessibleName) {
        for (int index = 0; index < CODE_LENGTH; index++) {
            int position = index;
            PasswordField field = new PasswordField();
            field.setId(prefix + (position + 1));
            field.setAccessibleText(accessibleName + ", số " + (position + 1));
            field.getStyleClass().add("key-digit");
            field.setTextFormatter(new TextFormatter<String>(change -> {
                if (!change.isContentChange() || change.getText().isEmpty()) {
                    return change;
                }
                String input = change.getText();
                if (input.length() == CODE_LENGTH && input.chars().allMatch(KeySetupController::isDigit)) {
                    // A formatter cannot safely edit its own control while the change is being filtered.
                    long revision = formRevision;
                    Platform.runLater(() -> applyPaste(fields, input, revision));
                    return null;
                }
                if (input.length() != 1 || !isDigit(input.charAt(0))) {
                    return null;
                }
                change.setRange(0, change.getControlText().length());
                change.selectRange(1, 1);
                return change;
            }));
            field.textProperty().addListener((observable, previous, value) -> {
                if (!updatingDigits) {
                    setNotice("", false);
                    updateSubmit();
                    if (value != null && !value.isEmpty() && field.isFocused() && position + 1 < CODE_LENGTH) {
                        fields.get(position + 1).requestFocus();
                    }
                }
            });
            field.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.BACK_SPACE && field.getLength() == 0 && position > 0) {
                    PasswordField previous = fields.get(position - 1);
                    previous.clear();
                    previous.requestFocus();
                    event.consume();
                } else if (event.getCode() == KeyCode.ENTER && !keySubmit.isDisabled()) {
                    submit();
                    event.consume();
                }
            });
            fields.add(field);
            row.getChildren().add(field);
        }
    }

    private void applyPaste(List<PasswordField> fields, String input, long revision) {
        if (navigator == null || revision != formRevision || mode == Mode.CHOOSE
                || (fields == confirmation && mode != Mode.SET_UP) || fields.getFirst().getScene() == null) {
            return;
        }
        updatingDigits = true;
        try {
            for (int index = 0; index < CODE_LENGTH; index++) {
                fields.get(index).setText(input.substring(index, index + 1));
            }
        } finally {
            updatingDigits = false;
        }
        setNotice("", false);
        updateSubmit();
        fields.getLast().requestFocus();
    }

    private void clearDigits() {
        // Prevent a queued paste from repopulating a cleared or cached form.
        formRevision++;
        updatingDigits = true;
        try {
            digits.forEach(PasswordField::clear);
            confirmation.forEach(PasswordField::clear);
        } finally {
            updatingDigits = false;
        }
    }

    private void setNotice(String message, boolean error) {
        notice.setText(message);
        notice.pseudoClassStateChanged(ERROR, error);
    }

    private void updateSubmit() {
        keySubmit.setDisable(mode == Mode.CHOOSE || !complete(digits)
                || (mode == Mode.SET_UP && !complete(confirmation)));
    }

    private boolean confirmationMatches() {
        for (int index = 0; index < CODE_LENGTH; index++) {
            if (!digits.get(index).getText().equals(confirmation.get(index).getText())) {
                return false;
            }
        }
        return true;
    }

    private static boolean complete(List<PasswordField> fields) {
        return fields.size() == CODE_LENGTH && fields.stream().allMatch(field -> {
            String text = field.getText();
            return text != null && text.length() == 1 && isDigit(text.charAt(0));
        });
    }

    private static boolean isDigit(int character) {
        return character >= '0' && character <= '9';
    }

    private static void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }
}
