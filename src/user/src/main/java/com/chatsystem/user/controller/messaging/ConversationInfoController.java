package com.chatsystem.user.controller.messaging;

import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.control.SearchResultCard;
import com.chatsystem.user.controller.group.GroupFormController;
import com.chatsystem.user.messaging.ConversationCatalog;
import com.chatsystem.user.navigation.UserViewInputs;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.javafx.FontIcon;

public final class ConversationInfoController implements ViewController {
    @FXML private BorderPane drawerLayout;
    @FXML private Button back;
    @FXML private Label heading;
    @FXML private ScrollPane infoPane;
    @FXML private VBox infoContent;
    @FXML private VBox searchPane;
    @FXML private TextField conversationSearchField;
    @FXML private Label resultCount;
    @FXML private Button previousResult;
    @FXML private Button nextResult;
    @FXML private VBox localResults;

    private final ConversationCatalog catalog = UserViews.CONVERSATIONS_DATA;
    private final List<SearchResultCard> cards = new ArrayList<>();
    private ViewNavigator navigator;
    private UserViewInputs.MessageSearch search;
    private MockConversation currentChat;
    private List<MockMessage> matches = List.of();
    private int currentIndex = -1;

    @FXML
    private void initialize() {
        back.managedProperty().bind(back.visibleProperty());
        conversationSearchField.textProperty().addListener((observable, old, query) -> {
            if (search != null) {
                renderResults(query);
            }
        });
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        this.navigator = navigator;
        if (input instanceof UserViewInputs.MessageSearch details) {
            search = details;
            catalog.conversations().stream().filter(chat -> chat.id().equals(details.conversationId()))
                    .findFirst().ifPresent(this::renderInfo);
        }
        showInfo();
    }

    private void renderInfo(MockConversation chat) {
        currentChat = chat;
        var avatar = new ConversationAvatar();
        avatar.getStyleClass().add("chat-info-avatar");
        avatar.show(chat, catalog);

        var title = new Label(chat.title());
        title.getStyleClass().add("chat-info-title");
        title.setWrapText(true);
        title.setTextAlignment(TextAlignment.CENTER);

        var status = new Label(catalog.statusText(chat));
        status.getStyleClass().add("chat-header-status");
        status.setGraphic(ConversationAvatar.statusGraphic(chat, catalog));
        status.setGraphicTextGap(6);

        var profile = new VBox(8, avatar, title, status);
        profile.setAlignment(Pos.CENTER);

        var identity = new VBox(12, profile,
                action("searchInConversation", "fth-search", "Tìm kiếm trong hội thoại", false, this::showSearch));
        identity.getStyleClass().addAll("chat-drawer-section", "chat-drawer-identity");
        infoContent.getChildren().setAll(identity);

        if (chat.encrypted()) {
            var memberRows = new VBox(8);
            for (String participantId : chat.participantIds()) {
                memberRows.getChildren().add(memberRow(catalog.user(participantId), catalog.currentUserId()));
            }
            var members = new VBox(8, sectionLabel("Thành viên"),
                    action("addConversationMember", "fth-user-plus", "Thêm thành viên", false,
                            () -> openGroupForm(chat, GroupFormController.Mode.ADD)),
                    memberRows);
            members.getStyleClass().add("chat-drawer-section");
            infoContent.getChildren().add(members);

            var groupButtons = new VBox(4,
                    action("renameConversationGroup", "fth-edit-2", "Đổi tên nhóm", false,
                            () -> openGroupForm(chat, GroupFormController.Mode.RENAME)),
                    action("resignConversationAdmin", "fth-shield", "Chọn Admin kế nhiệm", false,
                            () -> openGroupForm(chat, GroupFormController.Mode.SUCCESSOR)),
                    action("clearConversationHistory", "fth-trash-2", "Xóa lịch sử phía mình", false, null),
                    action("reportConversation", "fth-flag", "Báo cáo spam", true, null),
                    action("leaveConversationGroup", "fth-log-out", "Rời nhóm", true, null));
            var groupOptions = new VBox(8, sectionLabel("Quản lý nhóm"), groupButtons);
            groupOptions.getStyleClass().add("chat-drawer-section");
            infoContent.getChildren().add(groupOptions);
        } else {
            var optionButtons = new VBox(4,
                    action("blockConversationPeer", "fth-slash", "Chặn người dùng", true, null),
                    action("clearConversationHistory", "fth-trash-2", "Xóa lịch sử phía mình", false, null),
                    action("reportConversation", "fth-flag", "Báo cáo spam", true, null));
            var options = new VBox(8, sectionLabel("Tùy chọn hội thoại"), optionButtons);
            options.getStyleClass().add("chat-drawer-section");
            infoContent.getChildren().add(options);
        }
    }

    private void openGroupForm(MockConversation chat, GroupFormController.Mode mode) {
        var route = mode == GroupFormController.Mode.SUCCESSOR
                ? UserViews.ASSIGN_SUCCESSOR_ADMIN
                : UserViews.CREATE_GROUP;
        navigator.showModal(route, new GroupFormController.Input(mode, chat.id()), true);
    }

    private static Label sectionLabel(String text) {
        var label = new Label(text);
        label.getStyleClass().add("chat-section-label");
        return label;
    }

    private Button action(String id, String iconLiteral, String text, boolean danger, Runnable handler) {
        var icon = new FontIcon(iconLiteral);
        icon.setIconSize(15);
        var button = new Button(text, icon);
        button.setId(id);
        button.setWrapText(true);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add("chat-drawer-action");
        if (danger) {
            button.getStyleClass().add("chat-drawer-danger");
        }
        if (handler == null) {
            button.setDisable(true);
        } else {
            button.setOnAction(event -> handler.run());
        }
        return button;
    }

    private static HBox memberRow(MockUser member, String currentUserId) {
        Label badge = new Label(ConversationAvatar.initials(member.fullName()));
        badge.getStyleClass().add("chat-info-member-avatar");

        String displayName = currentUserId.equals(member.id())
                ? member.fullName() + " (Bạn)"
                : member.fullName();
        Label name = new Label(displayName);
        name.getStyleClass().add("chat-info-member-name");

        Label handle = new Label("@" + member.username());
        handle.getStyleClass().add("chat-info-member-handle");

        VBox text = new VBox(2, name, handle);
        text.setMinWidth(0);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox row = new HBox(8, badge, text);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("chat-info-member");
        if (member.presence() == MockUser.Presence.ONLINE) {
            Region onlineDot = new Region();
            onlineDot.getStyleClass().addAll("chat-status-dot", "status-online");
            row.getChildren().add(onlineDot);
        }
        return row;
    }

    @FXML
    private void showInfo() {
        drawerLayout.setCenter(infoPane);
        boolean isGroup = currentChat != null
                && (currentChat.kind() == MockConversation.Kind.GROUP || currentChat.encrypted());
        heading.setText(isGroup ? "Thông tin nhóm" : "Thông tin đoạn chat");
        back.setVisible(false);
    }

    private void showSearch() {
        drawerLayout.setCenter(searchPane);
        heading.setText("Tìm trong đoạn chat");
        back.setVisible(true);
        renderResults(conversationSearchField.getText());
        conversationSearchField.requestFocus();
    }

    private void renderResults(String query) {
        String value = query.strip().toLowerCase(Locale.ROOT);
        matches = value.isEmpty() ? List.of() : catalog.messagesFor(search.conversationId()).stream()
                .filter(message -> message.state() != MockMessage.State.RECALLED)
                .filter(message -> message.text().toLowerCase(Locale.ROOT).contains(value)).toList();
        localResults.getChildren().clear();
        cards.clear();
        for (int i = 0; i < matches.size(); i++) {
            int index = i;
            var message = matches.get(i);
            String senderName = message.senderId().equals(catalog.currentUserId())
                    ? "Bạn"
                    : catalog.user(message.senderId()).fullName();
            var result = new SearchResultCard(senderName, message.text(),
                    message.sentAt(), null, () -> reveal(index));
            cards.add(result);
        }
        localResults.getChildren().setAll(cards);
        currentIndex = -1;
        resultCount.setText(value.isEmpty() ? "Nhập từ khóa" : matches.size() + " kết quả");
        previousResult.setDisable(true);
        nextResult.setDisable(matches.isEmpty());
    }

    private void reveal(int index) {
        if (index < 0 || index >= matches.size()) {
            return;
        }
        currentIndex = index;
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setCurrent(i == index);
        }
        resultCount.setText("Kết quả " + (index + 1) + " / " + matches.size());
        previousResult.setDisable(index == 0);
        nextResult.setDisable(index == matches.size() - 1);
        search.revealMessage().accept(matches.get(index).id());
    }

    @FXML
    private void previous() {
        reveal(currentIndex - 1);
    }

    @FXML
    private void next() {
        reveal(currentIndex + 1);
    }

    @FXML
    private void close() {
        navigator.closeDrawer();
    }
}
