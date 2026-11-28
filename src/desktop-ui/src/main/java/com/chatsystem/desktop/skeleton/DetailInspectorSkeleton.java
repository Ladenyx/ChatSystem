package com.chatsystem.desktop.skeleton;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Right-hand detail inspector skeleton independent of the left table in master-detail Admin views. */
public final class DetailInspectorSkeleton extends VBox {
    public DetailInspectorSkeleton() {
        setSpacing(16);
        setMinWidth(0);
        getStyleClass().add("cs-skeleton-card");

        HBox identity = new HBox(
                12,
                Skeletons.circle(48),
                new VBox(8, Skeletons.strongBar(144, 16), Skeletons.bar(96, 12)),
                Skeletons.spacer(),
                Skeletons.pill(76, 24)
        );
        identity.setAlignment(Pos.CENTER_LEFT);

        HBox actions = new HBox(
                8,
                Skeletons.pill(84, 30),
                Skeletons.pill(84, 30),
                Skeletons.pill(88, 30),
                Skeletons.pill(72, 30)
        );
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox metadata = new VBox(
                10,
                fieldRow(84, 180),
                fieldRow(72, 160),
                fieldRow(68, 120),
                fieldRow(64, 96)
        );

        HBox tabs = new HBox(12, Skeletons.strongBar(110, 14), Skeletons.bar(96, 14));
        VBox tabContent = new VBox(8, Skeletons.flexBar(36), Skeletons.flexBar(36), Skeletons.flexBar(36));

        getChildren().addAll(identity, actions, metadata, tabs, tabContent);
        Skeletons.nonInteractive(this);
    }

    private static HBox fieldRow(double labelWidth, double valueWidth) {
        HBox row = new HBox(12, Skeletons.bar(labelWidth, 12), Skeletons.flexBar(14));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(labelWidth + valueWidth + 40);
        return row;
    }
}
