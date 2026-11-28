package com.chatsystem.desktop.skeleton;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Friend/contact row skeleton matching {@code contacts.fxml}. */
public final class ContactListSkeleton extends VBox {
    public ContactListSkeleton() {
        this(6);
    }

    public ContactListSkeleton(int rowCount) {
        Skeletons.requirePositiveCount(rowCount, "rowCount");
        setSpacing(8);
        setMinWidth(0);
        for (int i = 0; i < rowCount; i++) {
            VBox identity = new VBox(6, Skeletons.strongBar(i % 2 == 0 ? 132 : 108, 14), Skeletons.bar(84, 11));
            identity.setMinWidth(0);
            HBox.setHgrow(identity, Priority.ALWAYS);
            HBox row = new HBox(
                    12,
                    Skeletons.circle(36),
                    identity,
                    Skeletons.pill(56, 20),
                    Skeletons.pill(76, 28)
            );
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("cs-skeleton-row");
            row.setMinHeight(54);
            getChildren().add(row);
        }
        Skeletons.nonInteractive(this);
    }
}
