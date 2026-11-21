package com.chatsystem.admin.controller;

import atlantafx.base.controls.Popover;
import com.chatsystem.admin.AdminViews;
import com.chatsystem.desktop.controller.DisplayOptionsController;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewNavigator;
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
    @FXML private Button displayButton;
    @FXML private Label pageTitle;
    @FXML private Label overviewDataNote;
    private final Map<View, ToggleButton> buttons = new LinkedHashMap<>();
    private final ToggleGroup group = new ToggleGroup();
    private ViewNavigator navigator;

    public void connect(ViewNavigator navigator) {
        this.navigator = navigator;
        add(overviewNav, AdminViews.OVERVIEW, "Tổng quan", Feather.GRID);
        add(managementNav, AdminViews.USERS, "Người dùng", Feather.USERS);
        add(managementNav, AdminViews.GROUPS, "Nhóm chat", Feather.MESSAGE_SQUARE);
        add(managementNav, AdminViews.SPAM, "Báo cáo spam", Feather.SHIELD);
        add(managementNav, AdminViews.LOGIN_LOGS, "Nhật ký đăng nhập", Feather.CLOCK);
        add(analyticsNav, AdminViews.REGISTRATIONS, "Đăng ký mới", Feather.USER_PLUS);
        add(analyticsNav, AdminViews.ACTIVITY, "Hoạt động", Feather.ACTIVITY);
        add(analyticsNav, AdminViews.FRIEND_NETWORK, "Mạng lưới bạn bè", Feather.SHARE_2);
        backButton.disableProperty().bind(navigator.canGoBackProperty().not());
        backButton.visibleProperty().bind(navigator.canGoBackProperty());
        backButton.managedProperty().bind(backButton.visibleProperty());
        overviewDataNote.visibleProperty().bind(navigator.currentViewProperty().isEqualTo(AdminViews.OVERVIEW));
        overviewDataNote.managedProperty().bind(overviewDataNote.visibleProperty());
        navigator.currentViewProperty().addListener((observable, old, view) -> {
            group.selectToggle(buttons.get(view));
            pageTitle.setText(view == null ? "" : view.title());
        });
    }

    private void add(VBox section, View view, String text, Feather icon) {
        FontIcon graphic = new FontIcon(icon);
        graphic.setIconSize(16);
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
    private void openDisplayOptions() {
        var popup = navigator.showPopover(DisplayOptionsController.VIEW, displayButton);
        popup.setArrowLocation(Popover.ArrowLocation.LEFT_BOTTOM);
        popup.setHeaderAlwaysVisible(false);
    }

    @FXML
    private void exit() {
        Platform.exit();
    }
}
