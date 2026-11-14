package com.chatsystem.user.controller;

import com.chatsystem.desktop.controller.DisplayOptionsController;
import com.chatsystem.desktop.controller.MockDataPreviewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.desktop.theme.ThemeManager;
import com.chatsystem.user.UserViews;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public final class UserShellController {
    @FXML private StackPane viewHost;
    @FXML private ToggleButton chatsButton;
    @FXML private ToggleButton contactsButton;
    @FXML private Button backButton;
    @FXML private Button themeButton;
    @FXML private Button displayButton;
    private ViewNavigator navigator;

    public void connect(ViewNavigator navigator) {
        this.navigator = navigator;
        ToggleGroup group = new ToggleGroup();
        chatsButton.setToggleGroup(group);
        contactsButton.setToggleGroup(group);
        backButton.disableProperty().bind(navigator.canGoBackProperty().not());
        navigator.currentViewProperty().addListener((observable, old, view) -> {
            chatsButton.setSelected(UserViews.CONVERSATIONS.equals(view));
            contactsButton.setSelected(UserViews.CONTACTS.equals(view));
        });
        navigator.themes().modeProperty().addListener((observable, old, mode) -> updateTheme(mode));
        updateTheme(navigator.themes().getMode());
    }

    public StackPane getViewHost() {
        return viewHost;
    }

    @FXML
    private void openChats() {
        navigator.navigate(UserViews.CONVERSATIONS);
        chatsButton.setSelected(true);
    }

    @FXML
    private void openContacts() {
        navigator.navigate(UserViews.CONTACTS);
        contactsButton.setSelected(true);
    }

    @FXML
    private void openProfile() {
        navigator.showModal(UserViews.PROFILE);
    }

    @FXML
    private void goBack() {
        navigator.goBack();
    }

    @FXML
    private void toggleTheme() {
        navigator.themes().toggle();
    }

    @FXML
    private void openDisplayOptions() {
        navigator.showPopover(DisplayOptionsController.VIEW, displayButton);
    }

    @FXML
    private void openMockData() {
        navigator.navigate(MockDataPreviewController.VIEW);
    }

    @FXML
    private void exit() {
        Platform.exit();
    }

    private void updateTheme(ThemeManager.Mode mode) {
        boolean dark = mode == ThemeManager.Mode.DARK;
        themeButton.setText(dark ? "Tối" : "Sáng");
        themeButton.setGraphic(new FontIcon(dark ? Feather.MOON : Feather.SUN));
        themeButton.setTooltip(new Tooltip(dark ? "Chuyển sang giao diện sáng" : "Chuyển sang giao diện tối"));
        themeButton.setAccessibleText(themeButton.getTooltip().getText());
    }
}
