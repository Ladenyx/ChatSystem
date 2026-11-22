package com.chatsystem.user.controller;

import atlantafx.base.controls.ToggleSwitch;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;

public final class UserShellController {
    private static final String SETTINGS_STYLESHEET = UserShellController.class.getResource(
            "/com/chatsystem/user/css/rail-settings.css").toExternalForm();

    @FXML private StackPane viewHost;
    @FXML private VBox workspaceRail;
    @FXML private ToggleButton chatsButton;
    @FXML private ToggleButton contactsButton;
    @FXML private Button backButton;
    @FXML private Button settingsButton;
    @FXML private VBox settingsContent;
    private Popup settingsPopup;
    @FXML private ToggleSwitch darkModeSwitch;
    private ViewNavigator navigator;

    public void connect(ViewNavigator navigator) {
        this.navigator = navigator;
        ToggleGroup group = new ToggleGroup();
        chatsButton.setToggleGroup(group);
        contactsButton.setToggleGroup(group);
        workspaceRail.managedProperty().bind(workspaceRail.visibleProperty());
        workspaceRail.setVisible(false);
        backButton.visibleProperty().bind(navigator.canGoBackProperty());
        backButton.managedProperty().bind(backButton.visibleProperty());
        navigator.currentViewProperty().addListener((observable, old, view) -> {
            workspaceRail.setVisible(view != null && !UserViews.isAuthView(view));
            chatsButton.setSelected(UserViews.CONVERSATIONS.equals(view));
            contactsButton.setSelected(UserViews.CONTACTS.equals(view));
        });
        var themes = navigator.themes();
        themes.apply(settingsContent);
        settingsContent.getStylesheets().add(SETTINGS_STYLESHEET);
        darkModeSwitch.setSelected(themes.getMode() == ThemeManager.Mode.DARK);
        themes.modeProperty().addListener((observable, old, mode) ->
                darkModeSwitch.setSelected(mode == ThemeManager.Mode.DARK));
        darkModeSwitch.selectedProperty().addListener((observable, old, selected) -> {
            var mode = selected ? ThemeManager.Mode.DARK : ThemeManager.Mode.LIGHT;
            if (themes.getMode() != mode) {
                themes.setMode(mode);
            }
        });
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
    private void openSettings() {
        if (settingsPopup == null) {
            settingsPopup = new Popup();
            settingsPopup.setAutoHide(true);
            settingsPopup.setHideOnEscape(true);
            settingsPopup.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_TOP_LEFT);
            settingsPopup.getContent().add(settingsContent);
        }
        if (settingsPopup.isShowing()) {
            settingsPopup.hide();
            return;
        }
        var anchor = settingsButton.localToScreen(settingsButton.getBoundsInLocal());
        settingsContent.applyCss();
        settingsContent.layout();
        double contentHeight = settingsContent.prefHeight(-1);
        double targetX = anchor.getMaxX() + 8;
        double targetY = anchor.getMaxY() - contentHeight;
        settingsPopup.show(settingsButton, targetX, targetY);
        darkModeSwitch.requestFocus();
    }

    @FXML
    private void openProfile() {
        settingsPopup.hide();
        navigator.showModal(UserViews.PROFILE);
    }

    @FXML
    private void goBack() {
        navigator.goBack();
    }

    @FXML
    private void openDisplayOptions() {
        settingsPopup.hide();
        navigator.showPopover(DisplayOptionsController.VIEW, settingsButton);
    }

    @FXML
    private void openMockData() {
        settingsPopup.hide();
        navigator.navigate(MockDataPreviewController.VIEW);
    }

    @FXML
    private void signOut() {
        Platform.exit();
    }
}
