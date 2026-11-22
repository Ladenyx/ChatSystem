package com.chatsystem.user.controller.group;

import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.DialogViewController;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ConversationAvatar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
    @FXML private ScrollPane candidateScroll;
    @FXML private VBox candidates;
    @FXML private Label footnote;

    private final ToggleGroup successorChoices = new ToggleGroup();
    private final Set<String> selectedUserIds = new LinkedHashSet<>();
    private final Map<String, MockUser> usersById = new LinkedHashMap<>();
    private List<MockUser> allCandidates = List.of();
    private Mode mode;
    private Button submitButton;

    @FXML
    private void initialize() {
        for (Node node : List.of(nameSection, picker, searchSection, description, footnote)) {
            node.managedProperty().bind(node.visibleProperty());
        }
        groupNameField.textProperty().addListener((observable, oldText, newText) -> updateFormState());
        memberSearchField.textProperty().addListener((observable, oldText, query) -> renderCandidates(query));
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
        String title = conversation.map(chat -> chat.title()).orElse("");

        selectedUserIds.clear();
        usersById.clear();
        submitButton = null;

        groupNameField.setText(mode == Mode.RENAME ? title : "");
        nameSection.setVisible(mode == Mode.CREATE || mode == Mode.RENAME);
        picker.setVisible(mode != Mode.RENAME);
        searchSection.setVisible(mode == Mode.CREATE || mode == Mode.ADD);

        switch (mode) {
            case CREATE -> {
                description.setText("");
                footnote.setText("Nhóm chat được bảo vệ bằng mã hóa đầu cuối (E2EE)");
            }
            case ADD -> {
                description.setText(title);
                footnote.setText("Chỉ hiển thị bạn bè chưa thuộc nhóm");
            }
            case RENAME -> {
                description.setText("Tên nhóm mới sẽ hiển thị với tất cả thành viên");
                footnote.setText("");
            }
            case SUCCESSOR -> {
                description.setText((title.isBlank() ? "" : title + " · ")
                        + "Chọn một thành viên để bàn giao quyền Admin");
                footnote.setText("Quyền Admin sẽ được chuyển giao ngay sau khi xác nhận");
            }
        }
        description.setVisible(!description.getText().isBlank());
        footnote.setVisible(!footnote.getText().isBlank());

        List<String> members = conversation.map(chat -> chat.participantIds()).orElse(List.of());
        List<String> available = mode == Mode.SUCCESSOR ? members : catalog.conversations().stream()
                .flatMap(chat -> chat.participantIds().stream()).distinct().filter(id -> !members.contains(id)).toList();
        allCandidates = available.stream().filter(id -> !catalog.currentUserId().equals(id)).map(catalog::user).toList();
        for (MockUser user : allCandidates) {
            usersById.put(user.id(), user);
        }
        int visibleRows = Math.clamp(allCandidates.size(), 1, 3);
        candidateScroll.setPrefViewportHeight(visibleRows * 56.0 + (visibleRows - 1) * 8.0);

        renderCandidates("");
        updateFormState();
    }

    private void renderCandidates(String query) {
        String filter = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        var rows = allCandidates.stream()
                .filter(user -> user.fullName().toLowerCase(Locale.ROOT).contains(filter)
                        || user.username().toLowerCase(Locale.ROOT).contains(filter))
                .map(this::candidate)
                .toList();
        if (rows.isEmpty()) {
            var empty = new Label("Không tìm thấy thành viên phù hợp");
            empty.getStyleClass().add("chat-caption");
            empty.setWrapText(true);
            candidates.getChildren().setAll(empty);
        } else {
            candidates.getChildren().setAll(rows);
        }
    }

    private ButtonBase candidate(MockUser user) {
        Label badge = new Label(ConversationAvatar.initials(user.fullName()));
        badge.getStyleClass().add("chat-info-member-avatar");

        Label name = new Label(user.fullName());
        name.getStyleClass().add("chat-result-title");

        String presence = user.presence() == MockUser.Presence.ONLINE ? "Đang hoạt động" : "Ngoại tuyến";
        Label meta = new Label("@" + user.username() + " · " + presence);
        meta.getStyleClass().add("chat-caption");

        VBox text = new VBox(2, name, meta);
        text.setMinWidth(0);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox cardContent = new HBox(12, badge, text);
        cardContent.setAlignment(Pos.CENTER_LEFT);
        cardContent.getStyleClass().add("chat-group-candidate-label");
        if (user.presence() == MockUser.Presence.ONLINE) {
            Region onlineDot = new Region();
            onlineDot.getStyleClass().addAll("chat-status-dot", "status-online");
            cardContent.getChildren().add(onlineDot);
        }

        ButtonBase control;
        if (mode == Mode.SUCCESSOR) {
            var radio = new RadioButton();
            radio.setToggleGroup(successorChoices);
            radio.setSelected(selectedUserIds.contains(user.id()));
            radio.setOnAction(event -> {
                selectedUserIds.clear();
                if (radio.isSelected()) {
                    selectedUserIds.add(user.id());
                }
                updateFormState();
            });
            control = radio;
        } else {
            var check = new CheckBox();
            check.setSelected(selectedUserIds.contains(user.id()));
            check.setOnAction(event -> {
                if (check.isSelected()) {
                    selectedUserIds.add(user.id());
                } else {
                    selectedUserIds.remove(user.id());
                }
                updateFormState();
            });
            control = check;
        }
        control.setId("group-candidate-" + user.id());
        cardContent.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0, control.getWidth() - 56), control.widthProperty()));
        control.setGraphic(cardContent);
        control.getStyleClass().add("chat-group-candidate");
        control.setMaxWidth(Double.MAX_VALUE);
        control.setAccessibleText(user.fullName() + " · " + meta.getText());
        return control;
    }

    private void updateFormState() {
        if (mode == null) {
            return;
        }
        if (mode == Mode.SUCCESSOR) {
            String chosen = selectedUserIds.stream()
                    .map(usersById::get)
                    .filter(user -> user != null)
                    .map(MockUser::fullName)
                    .findFirst()
                    .orElse(null);
            selectionSummary.setText(chosen == null ? "Chọn một thành viên" : "Đã chọn: " + chosen);
        } else if (mode != Mode.RENAME) {
            selectionSummary.setText("Chọn thành viên (" + selectedUserIds.size() + " đã chọn)");
        }
        if (submitButton != null) {
            submitButton.setDisable(!isValid());
        }
    }

    private boolean isValid() {
        if (mode == null) {
            return false;
        }
        return switch (mode) {
            case RENAME -> !groupNameField.getText().isBlank();
            case CREATE -> !groupNameField.getText().isBlank() && !selectedUserIds.isEmpty();
            case ADD, SUCCESSOR -> !selectedUserIds.isEmpty();
        };
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

        var cancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        String action = switch (mode) {
            case CREATE -> "Tạo nhóm";
            case ADD -> "Thêm thành viên";
            case RENAME -> "Lưu thay đổi";
            case SUCCESSOR -> "Bàn giao Admin";
        };
        var submit = new ButtonType(action, ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().setAll(cancel, submit);

        var cancelButton = (Button) pane.lookupButton(cancel);
        cancelButton.getStyleClass().add("chat-dialog-cancel");
        ButtonBar.setButtonUniformSize(cancelButton, false);

        submitButton = (Button) pane.lookupButton(submit);
        submitButton.setId("groupFormSubmit");
        submitButton.getStyleClass().add("chat-primary");
        ButtonBar.setButtonUniformSize(submitButton, false);
        updateFormState();
    }
}
