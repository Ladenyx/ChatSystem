package com.chatsystem.desktop;

import com.chatsystem.desktop.navigation.ViewNavigator;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class DesktopWindow {
    private DesktopWindow() {
    }

    public static void show(Stage stage, String title, ViewNavigator navigator) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setTitle(title);
        stage.setScene(new Scene(navigator.getRoot(),
                Math.min(1280, bounds.getWidth() - 48), Math.min(720, bounds.getHeight() - 80)));
        stage.setMinWidth(1024);
        stage.setMinHeight(640);
        stage.setOnHidden(event -> navigator.close());
        stage.show();
        stage.centerOnScreen();
    }
}
