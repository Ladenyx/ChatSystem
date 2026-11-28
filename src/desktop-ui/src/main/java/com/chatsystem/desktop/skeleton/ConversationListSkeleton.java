package com.chatsystem.desktop.skeleton;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Conversation list skeleton matching the User Client left sidebar list. */
public final class ConversationListSkeleton extends VBox {
    public ConversationListSkeleton() {
        this(7);
    }

    public ConversationListSkeleton(int rowCount) {
        Skeletons.requirePositiveCount(rowCount, "rowCount");
        setSpacing(8);
        setMinWidth(0);
        for (int i = 0; i < rowCount; i++) {
            getChildren().add(buildRow(i));
        }
        Skeletons.nonInteractive(this);
    }

    private static HBox buildRow(int index) {
        HBox topLine = new HBox(
                8,
                Skeletons.strongBar(index % 2 == 0 ? 128 : 104, 14),
                Skeletons.spacer(),
                Skeletons.bar(36, 10)
        );
        topLine.setAlignment(Pos.CENTER_LEFT);
        VBox lines = new VBox(6, topLine, Skeletons.flexBar(12));
        lines.setMinWidth(0);
        HBox.setHgrow(lines, Priority.ALWAYS);

        HBox row = new HBox(12, Skeletons.circle(40), lines);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("cs-skeleton-row");
        row.setMinHeight(58);
        return row;
    }
}
