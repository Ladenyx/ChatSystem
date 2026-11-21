package com.chatsystem.user.controller;

import com.chatsystem.desktop.controller.DisplayOptionsController;
import com.chatsystem.desktop.controller.MockDataPreviewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.desktop.theme.ThemeManager;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.controller.chat.ChatDemoFlow;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class UserShellController {
    @FXML private StackPane viewHost;
    @FXML private VBox workspaceRail;
    @FXML private ToggleButton chatsButton;
    @FXML private ToggleButton contactsButton;
    @FXML private Button backButton;
    @FXML private MenuButton settingsButton;
    @FXML private MenuItem themeItem;
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
            workspaceRail.setVisible(view != null);
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
        navigator.showPopover(DisplayOptionsController.VIEW, settingsButton);
    }

    @FXML
    private void openMockData() {
        navigator.navigate(MockDataPreviewController.VIEW);
    }

    @FXML
    private void signOut() {
        ChatDemoFlow.clear(navigator);
        Platform.exit();
    }

    private void updateTheme(ThemeManager.Mode mode) {
        themeItem.setText(mode == ThemeManager.Mode.DARK ? "Chuyển sang giao diện sáng" : "Chuyển sang giao diện tối");
    }
}
