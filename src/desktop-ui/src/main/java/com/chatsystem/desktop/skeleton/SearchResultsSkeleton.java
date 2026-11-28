package com.chatsystem.desktop.skeleton;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Search hit skeleton matching in-conversation and global message search result lists. */
public final class SearchResultsSkeleton extends VBox {
    public SearchResultsSkeleton() {
        this(5);
    }

    public SearchResultsSkeleton(int rowCount) {
        Skeletons.requirePositiveCount(rowCount, "rowCount");
        setSpacing(8);
        setMinWidth(0);
        for (int i = 0; i < rowCount; i++) {
            HBox top = new HBox(8, Skeletons.strongBar(136, 14), Skeletons.spacer(), Skeletons.bar(48, 11));
            top.setAlignment(Pos.CENTER_LEFT);
            VBox card = new VBox(8, top, Skeletons.flexBar(12), Skeletons.bar(180, 12));
            card.getStyleClass().add("cs-skeleton-card");
            getChildren().add(card);
        }
        Skeletons.nonInteractive(this);
    }
}
