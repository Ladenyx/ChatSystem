package com.chatsystem.desktop.skeleton;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** 4-card KPI strip skeleton matching {@code overview.fxml}. */
public final class AdminKpiStripSkeleton extends GridPane {
    private static final int KPI_CARD_COUNT = 4;

    public AdminKpiStripSkeleton() {
        setHgap(8);
        setMinWidth(0);
        setPadding(new Insets(4));
        for (int i = 0; i < KPI_CARD_COUNT; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            getColumnConstraints().add(col);
            add(buildKpiCard(), i, 0);
        }
        Skeletons.nonInteractive(this);
    }

    private static VBox buildKpiCard() {
        HBox header = new HBox(8, Skeletons.bar(96, 12), Skeletons.spacer(), Skeletons.circle(16));
        header.setAlignment(Pos.CENTER_LEFT);
        VBox card = new VBox(12, header, Skeletons.strongBar(72, 26));
        card.getStyleClass().add("cs-skeleton-card");
        card.setMinHeight(88);
        card.setPrefHeight(88);
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }
}
