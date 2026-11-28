package com.chatsystem.desktop.skeleton;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Table skeleton with configurable row count and column weights matching Admin directory tables. */
public final class TableSkeleton extends VBox {
    private static final double[] STAGGER_FACTORS = {0.82, 0.68, 0.90};

    public TableSkeleton() {
        this(6, 0.45, 0.25, 0.30);
    }

    public TableSkeleton(int rowCount, double... columnWeights) {
        Skeletons.requirePositiveCount(rowCount, "rowCount");
        if (columnWeights == null || columnWeights.length == 0) {
            throw new IllegalArgumentException("columnWeights must not be empty");
        }
        double totalWeight = 0.0;
        for (double weight : columnWeights) {
            if (!Double.isFinite(weight) || weight <= 0.0) {
                throw new IllegalArgumentException("All column weights must be finite and > 0");
            }
            totalWeight += weight;
        }

        setSpacing(0);
        setMinWidth(0);
        getChildren().add(buildHeaderRow(columnWeights, totalWeight));
        for (int row = 0; row < rowCount; row++) {
            getChildren().add(buildDataRow(row, columnWeights, totalWeight));
        }
        Skeletons.nonInteractive(this);
    }

    private static HBox buildHeaderRow(double[] weights, double totalWeight) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("cs-skeleton-row");
        header.setMinHeight(36);
        header.setPrefHeight(36);
        for (double weight : weights) {
            Region cell = Skeletons.strongBar(Math.max(40, Math.round(220 * (weight / totalWeight))), 12);
            HBox wrapper = new HBox(cell);
            wrapper.setAlignment(Pos.CENTER_LEFT);
            wrapper.setMinWidth(0);
            wrapper.setPrefWidth(1000 * (weight / totalWeight));
            HBox.setHgrow(wrapper, Priority.ALWAYS);
            header.getChildren().add(wrapper);
        }
        return header;
    }

    private static HBox buildDataRow(int rowIndex, double[] weights, double totalWeight) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("cs-skeleton-row");
        row.setMinHeight(48);
        row.setPrefHeight(48);
        double factor = STAGGER_FACTORS[rowIndex % STAGGER_FACTORS.length];
        for (int col = 0; col < weights.length; col++) {
            double share = weights[col] / totalWeight;
            double barWidth = Math.max(44, Math.round(260 * share * factor));
            Region cell = col == 1 ? Skeletons.pill(Math.min(84, barWidth), 20) : Skeletons.bar(barWidth, 14);
            HBox wrapper = new HBox(cell);
            wrapper.setAlignment(Pos.CENTER_LEFT);
            wrapper.setMinWidth(0);
            wrapper.setPrefWidth(1000 * share);
            HBox.setHgrow(wrapper, Priority.ALWAYS);
            row.getChildren().add(wrapper);
        }
        return row;
    }
}
