package com.chatsystem.user.controller.group;

import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.DialogViewController;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserApplication;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.controller.chat.ChatDemoFlow;
import com.chatsystem.user.controller.group.OfflineGroupDemoService.ExitAction;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class GroupFormController implements DialogViewController {
    private enum Mode { CREATE, ADD, RENAME, SUCCESSOR }
    private record Request(Mode mode, String groupId, ExitAction action) {}
    private static final View EDIT_GROUP = new View("group-edit", "Quản lý nhóm", UserApplication.class,
            "/com/chatsystem/user/fxml/group-form.fxml");

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
    @FXML private Label feedback;
    @FXML private Label footnote;

    private final Set<String> selectedIds = new LinkedHashSet<>();
    private final IntegerProperty selectedCount = new SimpleIntegerProperty();
    private ChatDemoFlow flow;
    private Request request;
    private Runnable afterCommit;

    public static void openAdd(ViewNavigator navigator, String groupId) {
        open(navigator, EDIT_GROUP, new Request(Mode.ADD, groupId, null));
    }

    public static void openRename(ViewNavigator navigator, String groupId) {
        open(navigator, EDIT_GROUP, new Request(Mode.RENAME, groupId, null));
    }

    public static void openSuccessor(ViewNavigator navigator, String groupId, ExitAction action) {
        open(navigator, UserViews.SUCCESSOR_ADMIN, new Request(Mode.SUCCESSOR, groupId, action));
    }

    private static void open(ViewNavigator navigator, View view, Request request) {
        var properties = navigator.getRoot().getProperties();
        properties.put(Request.class, request);
        try {
            navigator.showModal(view, true);
        } finally {
            properties.remove(Request.class);
        }
    }

    @FXML
    private void initialize() {
        for (Node node : List.of(nameSection, picker, searchSection, feedback, description, footnote)) {
            node.managedProperty().bind(node.visibleProperty());
        }
        memberSearchField.textProperty().addListener((observable, oldText, newText) -> {
            candidateScroll.setVvalue(0);
            renderCandidates();
        });
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        flow = ChatDemoFlow.get(navigator);
        Object context = navigator.getRoot().getProperties().get(Request.class);
        request = view.equals(UserViews.CREATE_GROUP) ? new Request(Mode.CREATE, null, null) : (Request) context;
        var group = flow.chat().groups().group(request.groupId());
        switch (request.mode()) {
            case CREATE -> {
                heading.setText("Tạo nhóm mới");
                footnote.setText("Chọn ít nhất một người bạn đủ điều kiện, kể cả khi họ ngoại tuyến. "
                        + "Nhóm dùng E2EE trong bản demo và bạn là Admin đầu tiên.");
            }
            case ADD -> {
                heading.setText("Thêm bạn vào " + group.name());
                description.setText("Chọn bạn bè chưa thuộc nhóm và không có chiều chặn với bạn.");
            }
            case RENAME -> {
                heading.setText("Đổi tên nhóm");
                groupNameField.setText(group.name());
                description.setText("Thành viên và lịch sử của nhóm được giữ nguyên.");
            }
            case SUCCESSOR -> {
                heading.setText(request.action() == ExitAction.LEAVE
                        ? "Chọn Admin kế nhiệm để rời nhóm" : "Chọn Admin kế nhiệm để thôi quyền");
                description.setText("Chọn một thành viên. Khi xác nhận, quyền Admin được bàn giao trong cùng một lần cập nhật.");
            }
        }
        boolean invite = request.mode() == Mode.CREATE || request.mode() == Mode.ADD;
        nameSection.setVisible(request.mode() == Mode.CREATE || request.mode() == Mode.RENAME);
        picker.setVisible(request.mode() != Mode.RENAME);
        searchSection.setVisible(invite);
        description.setVisible(!description.getText().isBlank());
        footnote.setVisible(!footnote.getText().isBlank());
        renderCandidates();
    }

    @Override
    public void configureDialog(Dialog<ButtonType> dialog) {
        dialog.setTitle(heading.getText());
        closeButton.setOnAction(event -> dialog.close());
        var pane = dialog.getDialogPane();
        pane.getStyleClass().add("chat-group-dialog");
        for (String file : List.of("chat-tokens.css", "chat.css")) {
            pane.getStylesheets().add(UserApplication.class.getResource("/com/chatsystem/user/css/" + file).toExternalForm());
        }
        var cancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        var submit = new ButtonType(submitLabel(), ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().setAll(cancel, submit);
        var primary = pane.lookupButton(submit);
        primary.setId("groupFormSubmit");
        primary.getStyleClass().add("chat-primary");
        pane.lookupButton(cancel).setId("groupFormCancel");
        pane.lookupButton(cancel).getStyleClass().add("chat-outline");
        primary.disableProperty().bind(Bindings.createBooleanBinding(
                () -> (nameSection.isVisible() && groupNameField.getText().isBlank())
                        || (picker.isVisible() && selectedCount.get() == 0),
                groupNameField.textProperty(), selectedCount));
        primary.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                submit();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                event.consume();
                feedback.setText(exception.getMessage());
                feedback.setVisible(true);
                renderCandidates();
            }
        });
    }

    private String submitLabel() {
        return switch (request.mode()) {
            case CREATE -> "Tạo nhóm E2EE";
            case ADD -> "Xác nhận danh sách";
            case RENAME -> "Lưu tên nhóm";
            case SUCCESSOR -> request.action() == ExitAction.LEAVE ? "Bàn giao và rời nhóm" : "Bàn giao và thôi quyền";
        };
    }

    private void submit() {
        var chat = flow.chat();
        switch (request.mode()) {
            case CREATE -> {
                var group = chat.createGroup(groupNameField.getText(), selectedIds);
                afterCommit = () -> flow.openCreatedGroup(group);
            }
            case ADD -> {
                chat.groups().addMembers(request.groupId(), selectedIds);
                afterCommit = flow::conversationChanged;
            }
            case RENAME -> {
                chat.groups().rename(request.groupId(), groupNameField.getText());
                afterCommit = flow::conversationChanged;
            }
            case SUCCESSOR -> {
                chat.exitGroup(request.groupId(), request.action(), selectedIds.iterator().next());
                afterCommit = flow::conversationChanged;
            }
        }
    }

    @Override
    public void onLeave() {
        if (afterCommit != null) {
            var action = afterCommit;
            afterCommit = null;
            action.run();
        }
        flow = null;
    }

    private void renderCandidates() {
        if (flow == null || request.mode() == Mode.RENAME) {
            return;
        }
        var groups = flow.chat().groups();
        boolean successor = request.mode() == Mode.SUCCESSOR;
        var eligible = successor ? groups.successors(request.groupId()) : groups.invitees(request.groupId());
        selectedIds.retainAll(eligible.stream().map(MockUser::id).toList());
        updateSelectionSummary();
        String query = groupSearchKey(memberSearchField.getText());
        candidates.getChildren().clear();
        for (var user : eligible) {
            if (groupSearchKey(user.fullName()).contains(query) || groupSearchKey(user.username()).contains(query)) {
                candidates.getChildren().add(candidate(user, successor));
            }
        }
        if (candidates.getChildren().isEmpty()) {
            var empty = new Label(successor ? "Chưa có người kế nhiệm hợp lệ." : "Không có bạn bè phù hợp.");
            empty.setWrapText(true);
            empty.getStyleClass().add("chat-caption");
            candidates.getChildren().add(empty);
        }
        int visibleRows = Math.clamp(candidates.getChildren().size(), 1, 3);
        candidateScroll.setPrefViewportHeight(successor ? visibleRows * 76 : visibleRows * 72);
    }

    private CheckBox candidate(MockUser user, boolean successor) {
        var name = new Label(user.fullName());
        name.getStyleClass().add("chat-result-title");
        String presence = user.presence() == MockUser.Presence.ONLINE ? "Đang hoạt động" : "Ngoại tuyến";
        var hint = new Label(successor ? "Thành viên · " + presence + " · Đủ điều kiện" : "Bạn bè · " + presence);
        hint.getStyleClass().add("chat-caption");
        var check = new CheckBox();
        check.setId("group-candidate-" + user.id());
        check.setAccessibleText(user.fullName() + " · " + hint.getText());
        var labels = new VBox(4, name, hint);
        labels.getStyleClass().add("chat-group-candidate-label");
        check.setGraphic(labels);
        check.getStyleClass().add("chat-group-candidate");
        check.setMaxWidth(Double.MAX_VALUE);
        check.setSelected(selectedIds.contains(user.id()));
        check.setOnAction(event -> {
            if (check.isSelected()) {
                if (successor) {
                    selectedIds.clear();
                    candidates.getChildren().stream().filter(CheckBox.class::isInstance)
                            .map(CheckBox.class::cast).filter(other -> other != check)
                            .forEach(other -> other.setSelected(false));
                }
                selectedIds.add(user.id());
            } else {
                selectedIds.remove(user.id());
            }
            updateSelectionSummary();
            feedback.setVisible(false);
        });
        return check;
    }

    private void updateSelectionSummary() {
        selectedCount.set(selectedIds.size());
        selectionSummary.setText(request.mode() == Mode.SUCCESSOR
                ? "Chọn một thành viên" : "Thành viên đã chọn · " + selectedIds.size() + " người bạn");
    }

    private static String groupSearchKey(String value) {
        return java.text.Normalizer.normalize(value.strip().toLowerCase(java.util.Locale.ROOT), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd');
    }
}
