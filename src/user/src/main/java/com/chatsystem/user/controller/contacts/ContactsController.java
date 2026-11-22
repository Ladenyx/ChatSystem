package com.chatsystem.user.controller.contacts;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.mock.MockContacts;
import com.chatsystem.user.mock.MockContacts.Relationship;
import com.chatsystem.user.navigation.UserViewInputs;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/** Version 1 preview: relationship changes live only in this cached view. */
public final class ContactsController implements ViewController {
    private enum Tab {
        ALL("Tất cả bạn bè", "Lọc bạn bè theo tên", "bạn bè", "Chưa có bạn bè"),
        ONLINE("Đang online", "Lọc bạn bè theo tên", "bạn bè online", "Không có bạn bè đang online"),
        REQUESTS("Lời mời kết bạn", "Lọc lời mời theo tên", "lời mời", "Không có lời mời đang chờ"),
        SEARCH("Tìm người dùng", "Tìm theo họ tên hoặc tên đăng nhập", "kết quả", "Nhập tên để tìm người dùng"),
        BLOCKED("Đã chặn", "", "tài khoản", "Chưa chặn tài khoản nào");

        private final String title;
        private final String prompt;
        private final String countUnit;
        private final String emptyText;

        Tab(String title, String prompt, String countUnit, String emptyText) {
            this.title = title;
            this.prompt = prompt;
            this.countUnit = countUnit;
            this.emptyText = emptyText;
        }
    }

    private enum ContactAction {
        UNFRIEND("Hủy kết bạn", "Hủy kết bạn với %s?", "Tin nhắn và nhóm chung vẫn được giữ nguyên.",
                Relationship.NONE, "Đã hủy kết bạn", true),
        BLOCK("Chặn", "Chặn %s?",
                "Tình bạn và lời mời đang chờ sẽ bị hủy. Hai bên không thể gửi tin nhắn riêng hoặc lời mời mới.",
                Relationship.BLOCKED, "Đã chặn", true),
        UNBLOCK("Bỏ chặn", "Bỏ chặn %s?", "Tình bạn và lời mời cũ sẽ không tự khôi phục.",
                Relationship.NONE, "Đã bỏ chặn", false);

        private final String label;
        private final String question;
        private final String detail;
        private final Relationship next;
        private final String status;
        private final boolean destructive;

        ContactAction(String label, String question, String detail, Relationship next, String status, boolean destructive) {
            this.label = label;
            this.question = question;
            this.detail = detail;
            this.next = next;
            this.status = status;
            this.destructive = destructive;
        }
    }

    private static final String CHAT_PREVIEW = "Mở bản xem Đoạn chat, chưa chọn cuộc trò chuyện";
    private static final String GROUP_PREVIEW = "Mở bản xem Tạo nhóm, chưa chọn thành viên";

    @FXML private BorderPane contactsRoot;
    @FXML private ToggleGroup contactsTabs;
    @FXML private ToggleButton contactsTabAll;
    @FXML private ToggleButton contactsTabOnline;
    @FXML private ToggleButton contactsTabRequests;
    @FXML private ToggleButton contactsTabSearch;
    @FXML private ToggleButton contactsTabBlocked;
    @FXML private TextField contactsFilter;
    @FXML private StackPane contactsSearchRow;
    @FXML private ScrollPane contactsScroll;
    @FXML private Label contactsTitle;
    @FXML private Label contactsCount;
    @FXML private Label contactsStatus;
    @FXML private Label contactsNote;
    @FXML private VBox contactsList;

    private final List<MockUser> users = MockData.create().users().stream()
            .filter(user -> !user.id().equals(MockData.PREVIEW_USER_ID))
            .filter(user -> user.status() == MockUser.Status.ACTIVE)
            .toList();
    private final Map<String, Relationship> relationships = new HashMap<>(MockContacts.initialRelationships());
    private ViewNavigator navigator;
    private Dialog<ButtonType> confirmation;
    private Tab tab;

    @FXML
    private void initialize() {
        contactsTabAll.setUserData(Tab.ALL);
        contactsTabOnline.setUserData(Tab.ONLINE);
        contactsTabRequests.setUserData(Tab.REQUESTS);
        contactsTabSearch.setUserData(Tab.SEARCH);
        contactsTabBlocked.setUserData(Tab.BLOCKED);

        contactsSearchRow.managedProperty().bind(contactsSearchRow.visibleProperty());
        contactsStatus.managedProperty().bind(contactsStatus.visibleProperty());
        contactsTabs.selectedToggleProperty().addListener((observable, previous, selected) -> {
            if (selected == null) {
                contactsTabs.selectToggle(previous);
            } else if (selected.getUserData() != tab) {
                selectTab((Tab) selected.getUserData());
            }
        });
        contactsFilter.textProperty().addListener((observable, previous, current) -> {
            contactsStatus.setVisible(false);
            render();
            contactsScroll.setVvalue(0);
        });
        contactsTabs.selectToggle(contactsTabAll);
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
    }

    @Override
    public void onLeave() {
        navigator = null;
        if (confirmation != null) {
            confirmation.close();
        }
    }

    private void selectTab(Tab next) {
        tab = next;
        contactsTitle.setText(tab.title);
        contactsSearchRow.setVisible(tab != Tab.BLOCKED);
        contactsFilter.setPromptText(tab.prompt);
        contactsFilter.setAccessibleText(tab.prompt);
        contactsStatus.setVisible(false);
        contactsNote.setText(switch (tab) {
            case ALL -> "Dữ liệu mẫu · Nhắn tin chỉ mở bản xem Đoạn chat, chưa chọn cuộc trò chuyện";
            case ONLINE, SEARCH -> "Dữ liệu mẫu · Nhắn tin và Tạo nhóm chỉ mở bản xem, chưa chọn người";
            case REQUESTS -> "Dữ liệu mẫu · Thay đổi chỉ áp dụng trong bản xem này";
            case BLOCKED -> "Dữ liệu mẫu · Bỏ chặn không khôi phục tình bạn hay lời mời cũ";
        });
        if (contactsFilter.getText().isEmpty()) {
            render();
        } else {
            contactsFilter.clear();
        }
        contactsScroll.setVvalue(0);
    }

    private void render() {
        String query = contactsFilter.getText().strip().toLowerCase(Locale.ROOT);
        List<MockUser> visible = users.stream()
                .filter(user -> matchesTab(user, query))
                .filter(user -> matchesQuery(user, query))
                .toList();
        contactsCount.setText(visible.size() + " " + tab.countUnit);
        if (visible.isEmpty()) {
            Label empty = new Label(query.isEmpty() ? tab.emptyText : "Không có kết quả phù hợp");
            empty.setId("contactsEmpty");
            empty.setWrapText(true);
            empty.getStyleClass().add("secondary-text");
            VBox panel = new VBox(empty);
            panel.setAlignment(Pos.CENTER);
            panel.getStyleClass().add("contacts-empty");
            contactsList.getChildren().setAll(panel);
        } else {
            contactsList.getChildren().setAll(visible.stream().map(this::row).toList());
        }
    }

    private boolean matchesTab(MockUser user, String query) {
        Relationship relationship = relationship(user);
        return switch (tab) {
            case ALL -> relationship == Relationship.FRIEND;
            case ONLINE -> relationship == Relationship.FRIEND && user.presence() == MockUser.Presence.ONLINE;
            case REQUESTS -> relationship == Relationship.INCOMING;
            case SEARCH -> !query.isEmpty()
                    && relationship != Relationship.BLOCKED && relationship != Relationship.BLOCKED_ME;
            case BLOCKED -> relationship == Relationship.BLOCKED;
        };
    }

    private boolean matchesQuery(MockUser user, String query) {
        if (tab == Tab.BLOCKED || user.fullName().toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        String usernameQuery = query.startsWith("@") ? query.substring(1) : query;
        return tab == Tab.SEARCH && !usernameQuery.isEmpty() && user.username().contains(usernameQuery);
    }

    private HBox row(MockUser user) {
        Label avatar = new Label(ConversationAvatar.initials(user.fullName()));
        avatar.getStyleClass().add("contacts-avatar");
        Label name = new Label(user.fullName());
        name.getStyleClass().add("contacts-name");
        Label username = new Label("@" + user.username());
        username.getStyleClass().add("caption");
        HBox metadata = new HBox(8, username, relationshipStatus(user));
        metadata.setAlignment(Pos.CENTER_LEFT);
        VBox identity = new VBox(4, name, metadata);
        identity.setMinWidth(0);
        HBox.setHgrow(identity, Priority.ALWAYS);

        HBox row = new HBox(12, avatar, identity, actions(user));
        row.setId("contact-" + user.id());
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("contacts-row");
        return row;
    }

    private Label relationshipStatus(MockUser user) {
        Label status = new Label(switch (tab) {
            case ALL, ONLINE -> user.presence() == MockUser.Presence.ONLINE ? "Online" : "Offline";
            case REQUESTS -> "Chờ phản hồi";
            case BLOCKED -> "Bạn đã chặn";
            case SEARCH -> switch (relationship(user)) {
                case FRIEND -> "Bạn bè";
                case INCOMING -> "Đã nhận lời mời";
                case OUTGOING -> "Đã gửi lời mời";
                default -> "Chưa kết bạn";
            };
        });
        status.getStyleClass().add("caption");
        if (tab == Tab.ALL || tab == Tab.ONLINE) {
            Region dot = new Region();
            dot.getStyleClass().add("contacts-presence");
            if (user.presence() == MockUser.Presence.ONLINE) {
                dot.getStyleClass().add("is-online");
                status.getStyleClass().add("contacts-online");
            }
            status.setGraphic(dot);
        }
        return status;
    }

    private HBox actions(MockUser user) {
        HBox actions = new HBox(8);
        actions.setMinWidth(Region.USE_PREF_SIZE);
        actions.setAlignment(Pos.CENTER_RIGHT);
        List<? extends Node> buttons = switch (tab) {
            case ALL -> List.of(chat(user), confirmationAction(user, "unfriend", ContactAction.UNFRIEND),
                    confirmationAction(user, "block", ContactAction.BLOCK));
            case ONLINE -> List.of(chat(user), group(user));
            case REQUESTS -> List.of(
                    button(user, "accept", "Đồng ý", true, () -> change(user, Relationship.FRIEND, "Đã chấp nhận lời mời từ")),
                    button(user, "decline", "Từ chối", false, () -> change(user, Relationship.NONE, "Đã từ chối lời mời từ")));
            case SEARCH -> switch (relationship(user)) {
                case FRIEND -> List.of(chat(user), group(user));
                case INCOMING -> List.of(chat(user), button(user, "view-request", "Xem lời mời", false, () -> {
                    contactsTabs.selectToggle(contactsTabRequests);
                    contactsFilter.setText(user.fullName());
                    contactsFilter.requestFocus();
                }));
                case OUTGOING -> List.of(chat(user));
                default -> List.of(chat(user), button(user, "invite", "Kết bạn", true,
                        () -> change(user, Relationship.OUTGOING, "Đã gửi lời mời tới")));
            };
            case BLOCKED -> List.of(confirmationAction(user, "unblock", ContactAction.UNBLOCK));
        };
        actions.getChildren().setAll(buttons);
        return actions;
    }

    private Button chat(MockUser user) {
        Button button = button(user, "chat", "Nhắn tin", tab != Tab.SEARCH || relationship(user) != Relationship.NONE,
                () -> navigator.navigate(UserViews.CONVERSATIONS, new UserViewInputs.DirectChat(user.id())));
        button.setTooltip(new Tooltip(CHAT_PREVIEW));
        button.setAccessibleHelp(CHAT_PREVIEW);
        return button;
    }

    private Button group(MockUser user) {
        Button button = button(user, "group", "Tạo nhóm", false,
                () -> navigator.showModal(UserViews.CREATE_GROUP, new UserViewInputs.CreateGroup(List.of(user.id()))));
        button.setTooltip(new Tooltip(GROUP_PREVIEW));
        button.setAccessibleHelp(GROUP_PREVIEW);
        return button;
    }

    private static Button button(MockUser user, String action, String label, boolean primary, Runnable handler) {
        Button button = new Button(label);
        button.setId("contact-" + action + "-" + user.id());
        button.setAccessibleText(label + " · " + user.fullName());
        if (primary) {
            button.getStyleClass().add("accent");
        }
        button.setOnAction(event -> handler.run());
        return button;
    }

    private Relationship relationship(MockUser user) {
        return relationships.getOrDefault(user.id(), Relationship.NONE);
    }

    private void change(MockUser user, Relationship next, String message) {
        if (next == Relationship.NONE) {
            relationships.remove(user.id());
        } else {
            relationships.put(user.id(), next);
        }
        render();
        contactsStatus.setText(message + " " + user.fullName() + " trong bản xem");
        contactsStatus.setVisible(true);
        if (tab == Tab.BLOCKED) {
            contactsTabBlocked.requestFocus();
        } else {
            contactsFilter.requestFocus();
        }
    }

    private Button confirmationAction(MockUser user, String id, ContactAction action) {
        return button(user, id, action.label, false, () -> confirm(user, action));
    }

    private void confirm(MockUser user, ContactAction action) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(contactsRoot.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        String question = action.question.formatted(user.fullName());
        dialog.setTitle(question);
        var pane = dialog.getDialogPane();
        pane.setId("contactsConfirmation");
        pane.getStyleClass().add("contacts-confirmation");
        pane.setPrefWidth(384);
        Label title = new Label(question);
        title.setId("contactsConfirmationTitle");
        title.getStyleClass().add("section-title");
        title.setWrapText(true);
        Label description = new Label(action.detail);
        description.getStyleClass().add("secondary-text");
        description.setWrapText(true);
        pane.setContent(new VBox(8, title, description));
        ButtonType confirm = new ButtonType(action.label, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Quay lại", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().setAll(cancel, confirm);
        Button confirmButton = (Button) pane.lookupButton(confirm);
        Button cancelButton = (Button) pane.lookupButton(cancel);
        confirmButton.setId("confirm-contact-action");
        confirmButton.setDefaultButton(false);
        if (action.destructive) {
            confirmButton.getStyleClass().add("contacts-confirm-danger");
        }
        cancelButton.setId("cancel-contact-action");
        cancelButton.setDefaultButton(true);
        navigator.themes().apply(pane);
        pane.getStylesheets().addAll(contactsRoot.getStylesheets());
        dialog.setOnHidden(event -> {
            confirmation = null;
            if (dialog.getResult() == confirm && navigator != null) {
                change(user, action.next, action.status);
            }
        });
        confirmation = dialog;
        dialog.show();
        cancelButton.requestFocus();
    }
}
