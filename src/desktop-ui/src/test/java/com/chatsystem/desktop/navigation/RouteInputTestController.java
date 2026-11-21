package com.chatsystem.desktop.navigation;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class RouteInputTestController implements ViewController {
    record Input(String value) implements ViewInput {
    }

    @FXML private Label routeInput;

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        routeInput.setText(input instanceof Input testInput ? testInput.value() : "");
    }
}
