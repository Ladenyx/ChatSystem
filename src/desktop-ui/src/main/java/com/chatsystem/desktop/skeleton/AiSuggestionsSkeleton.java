package com.chatsystem.desktop.skeleton;

import com.chatsystem.common.api.ApiHeaders;
import javafx.scene.layout.VBox;

/** Three-suggestion card skeleton matching {@code ai-assistant.fxml} ({@code ApiHeaders.AI_SUGGESTION_COUNT}). */
public final class AiSuggestionsSkeleton extends VBox {
    public AiSuggestionsSkeleton() {
        setSpacing(8);
        setMinWidth(0);
        setPrefWidth(416);
        setMaxWidth(416);
        for (int i = 0; i < ApiHeaders.AI_SUGGESTION_COUNT; i++) {
            VBox card = new VBox(
                    8,
                    Skeletons.pill(76, 18),
                    Skeletons.flexBar(13),
                    Skeletons.bar(240, 12)
            );
            card.getStyleClass().add("cs-skeleton-card");
            getChildren().add(card);
        }
        Skeletons.nonInteractive(this);
    }
}
