package com.chatsystem.desktop.skeleton;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Alternating incoming/outgoing message bubble skeleton matching {@code chat-history.fxml}. */
public final class MessageHistorySkeleton extends VBox {
    public MessageHistorySkeleton() {
        this(6);
    }

    public MessageHistorySkeleton(int bubbleCount) {
        Skeletons.requirePositiveCount(bubbleCount, "bubbleCount");
        setSpacing(12);
        setMinWidth(0);
        for (int i = 0; i < bubbleCount; i++) {
            boolean outgoing = i % 2 == 1;
            getChildren().add(outgoing ? buildOutgoingBubble(i) : buildIncomingBubble(i));
        }
        Skeletons.nonInteractive(this);
    }

    private static HBox buildIncomingBubble(int index) {
        double width = index % 3 == 0 ? 220 : 168;
        VBox bubble = new VBox(
                6,
                Skeletons.bar(width, 13),
                Skeletons.bar(width * 0.65, 12)
        );
        bubble.getStyleClass().add("cs-skeleton-bubble-in");
        HBox row = new HBox(8, Skeletons.circle(28), bubble, Skeletons.spacer());
        row.setAlignment(Pos.BOTTOM_LEFT);
        return row;
    }

    private static HBox buildOutgoingBubble(int index) {
        double width = index % 3 == 1 ? 200 : 148;
        VBox bubble = new VBox(
                6,
                Skeletons.bar(width, 13),
                Skeletons.bar(width * 0.55, 12)
        );
        bubble.getStyleClass().add("cs-skeleton-bubble-out");
        HBox row = new HBox(8, Skeletons.spacer(), bubble);
        row.setAlignment(Pos.BOTTOM_RIGHT);
        return row;
    }
}
