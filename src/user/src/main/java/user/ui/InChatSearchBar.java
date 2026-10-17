package user.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.Consumer;

/**
 * Collapsible search toolbar inside a single conversation (Roadmap U-17).
 * Supports:
 * - Query input with real-time matching.
 * - Previous / Next navigation between matches.
 * - Match counter (e.g. "1 of 4").
 * - Close button to exit search mode and clear highlights.
 */
public class InChatSearchBar extends HBox {

    private final TextField searchField = new TextField();
    private final Label matchLabel = new Label("0 matches");
    private final Button prevBtn = new Button("▲");
    private final Button nextBtn = new Button("▼");
    private final Button closeBtn = new Button("✕");

    private Consumer<String> onQueryChanged;
    private Runnable onNext;
    private Runnable onPrev;
    private Runnable onClose;

    public InChatSearchBar() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(8);
        setPadding(new Insets(8, 16, 8, 16));
        setStyle("-fx-background-color: #ebebfc; -fx-background-radius: 8;");

        searchField.setPromptText("Find in this conversation...");
        searchField.getStyleClass().add("search-field");
        searchField.setStyle("-fx-background-color: white;");
        searchField.setPrefWidth(240);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (onQueryChanged != null) onQueryChanged.accept(newVal == null ? "" : newVal.trim());
        });

        matchLabel.getStyleClass().add("text-muted");
        matchLabel.setMinWidth(80);

        prevBtn.getStyleClass().add("btn-ghost");
        prevBtn.setStyle("-fx-padding: 4 8 4 8; -fx-font-size: 11px;");
        prevBtn.setOnAction(e -> { if (onPrev != null) onPrev.run(); });

        nextBtn.getStyleClass().add("btn-ghost");
        nextBtn.setStyle("-fx-padding: 4 8 4 8; -fx-font-size: 11px;");
        nextBtn.setOnAction(e -> { if (onNext != null) onNext.run(); });

        closeBtn.getStyleClass().add("btn-ghost");
        closeBtn.setStyle("-fx-padding: 4 8 4 8; -fx-font-size: 11px; -fx-font-weight: 700;");
        closeBtn.setOnAction(e -> { if (onClose != null) onClose.run(); });

        getChildren().addAll(searchField, matchLabel, prevBtn, nextBtn, closeBtn);
    }

    public void updateMatchCount(int currentMatchIndex, int totalMatches) {
        if (totalMatches == 0) {
            matchLabel.setText("0 matches");
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
        } else {
            matchLabel.setText((currentMatchIndex + 1) + " of " + totalMatches);
            prevBtn.setDisable(false);
            nextBtn.setDisable(false);
        }
    }

    public void reset() {
        searchField.clear();
        matchLabel.setText("0 matches");
        prevBtn.setDisable(true);
        nextBtn.setDisable(true);
    }

    public void focusInput() {
        searchField.requestFocus();
    }

    public void setOnQueryChanged(Consumer<String> onQueryChanged) { this.onQueryChanged = onQueryChanged; }
    public void setOnNext(Runnable onNext) { this.onNext = onNext; }
    public void setOnPrev(Runnable onPrev) { this.onPrev = onPrev; }
    public void setOnClose(Runnable onClose) { this.onClose = onClose; }
}

