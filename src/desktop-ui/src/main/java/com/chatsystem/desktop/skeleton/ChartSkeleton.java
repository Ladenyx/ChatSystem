package com.chatsystem.desktop.skeleton;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Chart panel skeleton matching Admin Overview trend and Analytics monthly chart containers. */
public final class ChartSkeleton extends VBox {
    private static final double[] BAR_HEIGHTS = {64, 92, 78, 120, 104, 140, 88, 132, 116, 152, 128, 144};

    public ChartSkeleton() {
        setSpacing(16);
        setMinWidth(0);
        getStyleClass().add("cs-skeleton-card");

        HBox header = new HBox(8, Skeletons.strongBar(180, 16), Skeletons.spacer(), Skeletons.pill(96, 28));
        header.setAlignment(Pos.CENTER_LEFT);

        HBox bars = new HBox(10);
        bars.setAlignment(Pos.BOTTOM_LEFT);
        bars.setMinHeight(168);
        bars.setPrefHeight(168);
        VBox.setVgrow(bars, Priority.ALWAYS);
        for (double height : BAR_HEIGHTS) {
            Region bar = Skeletons.bar(20, height);
            HBox slot = new HBox(bar);
            slot.setAlignment(Pos.BOTTOM_CENTER);
            slot.setMinWidth(0);
            HBox.setHgrow(slot, Priority.ALWAYS);
            bars.getChildren().add(slot);
        }

        Region axisLine = Skeletons.flexBar(2);
        getChildren().addAll(header, bars, axisLine);
        Skeletons.nonInteractive(this);
    }
}
