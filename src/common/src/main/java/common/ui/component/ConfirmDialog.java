package common.ui.component;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Optional;

/** Small reusable confirmation dialog. */
public class ConfirmDialog extends Dialog<Boolean> {
    public ConfirmDialog(String title, String message) {
        setTitle(title == null ? "Confirm" : title);
        setHeaderText(null);
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        Label content = new Label(message == null ? "Are you sure?" : message);
        content.setWrapText(true);
        content.setMaxWidth(360);
        getDialogPane().setContent(new VBox(content));
        setResultConverter(button -> button == ButtonType.OK);
    }

    public static boolean confirm(String title, String message) {
        Optional<Boolean> result = new ConfirmDialog(title, message).showAndWait();
        return result.orElse(false);
    }
}
