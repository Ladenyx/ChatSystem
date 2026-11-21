package com.chatsystem.user.controller.group;

import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.DialogViewController;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import java.util.List;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class GroupFormController implements DialogViewController {
    public enum Mode { CREATE, ADD, RENAME, SUCCESSOR }

    public record Input(Mode mode, String conversationId) implements ViewInput {}

    @FXML private Label heading;
    @FXML private Button closeButton;
    @FXML private Label description;
    @FXML private VBox nameSection;
    @FXML private TextField groupNameField;
    @FXML private VBox picker;
    @FXML private Label selectionSummary;
    @FXML private StackPane searchSection;
    @FXML private TextField memberSearchField;
    @FXML private VBox candidates;
    @FXML private Label footnote;

    private final ToggleGroup successorChoices = new ToggleGroup();
    private Mode mode;

    @FXML
    private void initialize() {
        for (Node node : List.of(nameSection, picker, searchSection, description)) {
            node.managedProperty().bind(node.visibleProperty());
        }
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        var settings = input instanceof Input context ? context
                : new Input(view.equals(UserViews.ASSIGN_SUCCESSOR_ADMIN) ? Mode.SUCCESSOR : Mode.CREATE, "");
        mode = settings.mode();
        heading.setText(switch (mode) {
            case CREATE -> "Tạo nhóm mới";
            case ADD -> "Thêm thành viên";
            case RENAME -> "Đổi tên nhóm";
            case SUCCESSOR -> "Chọn Admin kế nhiệm";
        });

        var catalog = UserViews.CONVERSATIONS_DATA;
        var conversation = catalog.conversations().stream()
                .filter(chat -> chat.id().equals(settings.conversationId())).findFirst();
        groupNameField.setText(mode == Mode.RENAME ? conversation.map(chat -> chat.title()).orElse("") : "");
        nameSection.setVisible(mode == Mode.CREATE || mode == Mode.RENAME);
        picker.setVisible(mode != Mode.RENAME);
        searchSection.setVisible(mode == Mode.CREATE || mode == Mode.ADD);
        description.setText(conversation.map(chat -> chat.title()).orElse(""));
        description.setVisible(!description.getText().isBlank());
        selectionSummary.setText(mode == Mode.SUCCESSOR ? "Chọn một thành viên" : "Chọn thành viên");
        footnote.setText("Bản xem giao diện · Thay đổi chưa được lưu");

        List<String> members = conversation.map(chat -> chat.participantIds()).orElse(List.of());
        List<String> available = mode == Mode.SUCCESSOR ? members : catalog.conversations().stream()
                .flatMap(chat -> chat.participantIds().stream()).distinct().filter(id -> !members.contains(id)).toList();
        var users = available.stream().filter(id -> !catalog.currentUserId().equals(id)).map(catalog::user).toList();
        renderCandidates(users, "");
        memberSearchField.textProperty().addListener((observable, old, query) -> renderCandidates(users, query));
    }

    private void renderCandidates(List<MockUser> users, String query) {
        String filter = query.strip().toLowerCase(Locale.ROOT);
        var rows = users.stream().filter(user -> user.fullName().toLowerCase(Locale.ROOT).contains(filter))
                .map(this::candidate).toList();
        candidates.getChildren().setAll(rows);
    }

    private Node candidate(MockUser user) {
        if (mode == Mode.SUCCESSOR) {
            var radio = new RadioButton(user.fullName());
            radio.setToggleGroup(successorChoices);
            return radio;
        }
        return new CheckBox(user.fullName());
    }

    @Override
    public void configureDialog(Dialog<ButtonType> dialog) {
        dialog.setTitle(heading.getText());
        closeButton.setOnAction(event -> dialog.close());
        var pane = dialog.getDialogPane();
        pane.getStyleClass().add("chat-group-dialog");
        for (String name : List.of("chat-tokens.css", "chat.css")) {
            pane.getStylesheets().add(UserViews.class.getResource("/com/chatsystem/user/css/" + name).toExternalForm());
        }
        String action = switch (mode) {
            case CREATE -> "Tạo nhóm";
            case ADD -> "Thêm";
            case RENAME -> "Lưu";
            case SUCCESSOR -> "Xác nhận";
        };
        var submit = new ButtonType(action, ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().add(submit);
        pane.lookupButton(submit).setId("groupFormSubmit");
        pane.lookupButton(submit).setDisable(true);
    }
}
