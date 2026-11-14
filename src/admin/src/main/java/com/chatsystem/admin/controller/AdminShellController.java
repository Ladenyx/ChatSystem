package com.chatsystem.admin.controller;

import com.chatsystem.admin.AdminViews;
import com.chatsystem.desktop.controller.DisplayOptionsController;
import com.chatsystem.desktop.controller.MockDataPreviewController;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.desktop.theme.ThemeManager;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public final class AdminShellController {
    @FXML private StackPane viewHost;
    @FXML private VBox overviewNav;
    @FXML private VBox managementNav;
    @FXML private VBox analyticsNav;
    @FXML private Button backButton;
    @FXML private Button themeButton;
    @FXML private Button displayButton;
    @FXML private Label pageTitle;
    private final Map<View, ToggleButton> buttons = new LinkedHashMap<>();
    private final ToggleGroup group = new ToggleGroup();
    private ViewNavigator navigator;

    public void connect(ViewNavigator navigator) {
        this.navigator = navigator;
        add(overviewNav, AdminViews.OVERVIEW, "Dashboard", Feather.GRID);
        add(managementNav, AdminViews.USERS, "Người dùng", Feather.USERS);
        add(managementNav, AdminViews.GROUPS, "Nhóm chat", Feather.MESSAGE_SQUARE);
        add(managementNav, AdminViews.SPAM, "Báo cáo spam", Feather.SHIELD);
        add(managementNav, AdminViews.LOGIN_LOGS, "Nhật ký đăng nhập", Feather.CLOCK);
        add(analyticsNav, AdminViews.REGISTRATIONS, "Đăng ký mới", Feather.USER_PLUS);
        add(analyticsNav, AdminViews.ACTIVITY, "Hoạt động", Feather.ACTIVITY);
        add(analyticsNav, AdminViews.FRIEND_NETWORK, "Mạng lưới bạn bè", Feather.SHARE_2);
        backButton.disableProperty().bind(navigator.canGoBackProperty().not());
        navigator.currentViewProperty().addListener((observable, old, view) -> {
            group.selectToggle(buttons.get(view));
            pageTitle.setText(view == null ? "" : view.title());
        });
        navigator.themes().modeProperty().addListener((observable, old, mode) -> updateTheme(mode));
        updateTheme(navigator.themes().getMode());
    }

    private void add(VBox section, View view, String text, Feather icon) {
        FontIcon graphic = new FontIcon(icon);
        graphic.setIconSize(20);
        ToggleButton button = new ToggleButton(text, graphic);
        button.setId("nav-" + view.id());
        button.setAccessibleText(view.title());
        button.getStyleClass().add("nav-item");
        button.setToggleGroup(group);
        button.setOnAction(event -> {
            navigator.navigate(view);
            button.setSelected(true);
        });
        buttons.put(view, button);
        section.getChildren().add(button);
    }

    public StackPane getViewHost() {
        return viewHost;
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
    private void openUserDialog() {
        navigator.showModal(AdminViews.EDIT_USER);
    }

    @FXML
    private void exit() {
        Platform.exit();
    }

    private void updateTheme(ThemeManager.Mode mode) {
        boolean dark = mode == ThemeManager.Mode.DARK;
        themeButton.setText(dark ? "Giao diện tối" : "Giao diện sáng");
        themeButton.setGraphic(new FontIcon(dark ? Feather.MOON : Feather.SUN));
        themeButton.setAccessibleText(dark ? "Chuyển sang giao diện sáng" : "Chuyển sang giao diện tối");
    }
}
