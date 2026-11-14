package com.chatsystem.desktop.controller;

import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.desktop.theme.ThemeManager;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public final class DisplayOptionsController implements ViewController {
    public static final View VIEW = new View("display-options", "Tùy chọn hiển thị",
            DisplayOptionsController.class, "/com/chatsystem/desktop/fxml/display-options.fxml");

    @FXML private ToggleButton lightButton;
    @FXML private ToggleButton darkButton;
    @FXML private CheckBox reducedMotion;
    private ThemeManager themes;
    private final ChangeListener<ThemeManager.Mode> listener = (observable, old, next) -> select(next);

    @FXML
    private void initialize() {
        ToggleGroup group = new ToggleGroup();
        lightButton.setToggleGroup(group);
        darkButton.setToggleGroup(group);
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        themes = navigator.themes();
        select(themes.getMode());
        themes.modeProperty().addListener(listener);
        reducedMotion.selectedProperty().bindBidirectional(themes.reducedMotionProperty());
    }

    @Override
    public void onLeave() {
        themes.modeProperty().removeListener(listener);
        reducedMotion.selectedProperty().unbindBidirectional(themes.reducedMotionProperty());
    }

    @FXML
    private void useLight() {
        themes.setMode(ThemeManager.Mode.LIGHT);
        select(themes.getMode());
    }

    @FXML
    private void useDark() {
        themes.setMode(ThemeManager.Mode.DARK);
        select(themes.getMode());
    }

    private void select(ThemeManager.Mode mode) {
        lightButton.setSelected(mode == ThemeManager.Mode.LIGHT);
        darkButton.setSelected(mode == ThemeManager.Mode.DARK);
    }
}
