package com.chatsystem.desktop.controller;

import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class PlaceholderController implements ViewController {
    @FXML private Label screenTitle;
    @FXML private Label screenId;

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        screenTitle.setText(view.title());
        screenId.setText(view.id());
    }
}
