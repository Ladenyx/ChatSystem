package com.chatsystem.user.controller.ai;

import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.navigation.UserViewInputs;
import java.util.List;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public final class AiAssistantController implements ViewController {
    private static final PseudoClass REFINE = PseudoClass.getPseudoClass("refine");
    private static final List<String> REPLY_EXAMPLES = List.of(
            "Mình đã hiểu rồi, cảm ơn bạn nhé.",
            "Mình sẽ xem lại và phản hồi bạn sớm.",
            "Cảm ơn bạn đã cập nhật. Mình sẽ kiểm tra phần này nhé.");

    @FXML private Label heading;
    @FXML private VBox suggestions;

    private ViewNavigator navigator;

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        this.navigator = navigator;
        if (!(input instanceof UserViewInputs.AiAssistant preview)) {
            heading.setText(view.title());
            return;
        }

        String draft = preview.draft().strip();
        boolean refine = preview.mode() == UserViewInputs.AiMode.REFINE && !draft.isEmpty();
        heading.setText(refine ? "Viết lại bản nháp" : "Gợi ý trả lời");

        var examples = refine ? draftExamples(draft) : REPLY_EXAMPLES;
        var options = examples.stream()
                .map(example -> option(example, preview))
                .toList();
        suggestions.pseudoClassStateChanged(REFINE, refine);
        suggestions.getChildren().setAll(options.stream().map(AiAssistantController::optionRow).toList());
        Platform.runLater(options.getFirst()::requestFocus);
    }

    private static List<String> draftExamples(String draft) {
        int first = draft.codePointAt(0);
        String sentence = Character.toString(Character.toUpperCase(first))
                + draft.substring(Character.charCount(first));
        return List.of(sentence, "Chào bạn,\n" + sentence, sentence + "\nCảm ơn bạn nhé.");
    }

    private Button option(String example, UserViewInputs.AiAssistant preview) {
        var option = new Button(example);
        option.getStyleClass().add("chat-ai-option");
        option.setAccessibleText("Dùng gợi ý " + example);
        option.setWrapText(true);
        option.setMaxWidth(Double.MAX_VALUE);
        option.setOnAction(event -> {
            close();
            preview.useSuggestion().accept(example);
        });
        return option;
    }

    private static StackPane optionRow(Button option) {
        var insert = new FontIcon(Feather.CORNER_DOWN_LEFT);
        insert.setIconSize(16);
        insert.setMouseTransparent(true);
        insert.getStyleClass().add("chat-ai-insert");
        StackPane.setAlignment(insert, Pos.CENTER_RIGHT);
        StackPane.setMargin(insert, new Insets(0, 12, 0, 0));
        var row = new StackPane(option, insert);
        row.getStyleClass().add("chat-ai-option-row");
        return row;
    }

    @FXML
    private void close() {
        navigator.closePopover();
    }
}
