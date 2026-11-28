package com.chatsystem.desktop.skeleton;

import java.util.Objects;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/** Factory and accessibility guard helpers ensuring all skeleton nodes remain non-focusable and transparent. */
public final class Skeletons {
    private Skeletons() {
    }

    public static <T extends Node> T nonInteractive(T node) {
        Objects.requireNonNull(node, "node");
        node.setFocusTraversable(false);
        node.setMouseTransparent(true);
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                nonInteractive(child);
            }
        }
        return node;
    }

    public static Region bar(double width, double height) {
        return fixedRegion(width, height, "cs-skeleton-block");
    }

    public static Region strongBar(double width, double height) {
        return fixedRegion(width, height, "cs-skeleton-block-strong");
    }

    public static Region flexBar(double height) {
        requirePositiveFinite(height, "height");
        Region region = new Region();
        region.getStyleClass().add("cs-skeleton-block");
        region.setMinHeight(height);
        region.setPrefHeight(height);
        region.setMaxHeight(height);
        region.setMinWidth(0);
        region.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(region, Priority.ALWAYS);
        return nonInteractive(region);
    }

    public static Region circle(double diameter) {
        return fixedRegion(diameter, diameter, "cs-skeleton-circle");
    }

    public static Region pill(double width, double height) {
        return fixedRegion(width, height, "cs-skeleton-pill");
    }

    static int requirePositiveCount(int count, String name) {
        if (count < 1) {
            throw new IllegalArgumentException(name + " must be >= 1, got: " + count);
        }
        return count;
    }

    static Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return nonInteractive(spacer);
    }

    static void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }

    private static Region fixedRegion(double width, double height, String styleClass) {
        requirePositiveFinite(width, "width");
        requirePositiveFinite(height, "height");
        Region region = new Region();
        region.getStyleClass().add(styleClass);
        region.setMinSize(width, height);
        region.setPrefSize(width, height);
        region.setMaxSize(width, height);
        return nonInteractive(region);
    }

    private static void requirePositiveFinite(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and > 0: " + value);
        }
    }
}
