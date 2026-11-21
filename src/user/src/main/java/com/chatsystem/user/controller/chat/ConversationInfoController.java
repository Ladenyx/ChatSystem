package com.chatsystem.user.controller.chat;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockGroup;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.control.ChatIcon;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.control.SearchResultCard;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

public final class ConversationInfoController implements ViewController {
    @FXML private BorderPane drawerLayout;
    @FXML private Label heading;
    @FXML private Button back;
    @FXML private ScrollPane infoPane;
    @FXML private VBox infoContent;
    @FXML private VBox searchPane;
    @FXML private TextField conversationSearchField;
    @FXML private Label resultCount;
    @FXML private Button previousResult;
    @FXML private Button nextResult;
    @FXML private ScrollPane localResultScroll;
    @FXML private VBox localResults;

    private final List<SearchResultCard> cards = new ArrayList<>();
    private final InvalidationListener revisionListener = ignored -> updateView();
    private List<MockMessage> matches = List.of();
    private String selectedMessageId;
    private ViewNavigator navigator;
    private ChatDemoFlow flow;
    private MockConversation conversation;
    private Alert confirmation;
    private boolean syncing;

    @FXML
    private void initialize() {
        back.managedProperty().bind(back.visibleProperty());
        conversationSearchField.textProperty().addListener((observable, old, query) -> {
            selectedMessageId = null;
            localResultScroll.setVvalue(0);
            updateSearch();
        });
        conversationSearchField.setOnAction(event -> {
            int index = selectedIndex();
            if (!matches.isEmpty()) {
                pick(index < 0 ? 0 : index);
            }
        });
        conversationSearchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                if (event.getCode() == KeyCode.UP) {
                    previous();
                } else {
                    next();
                }
                event.consume();
            }
        });
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
        flow = ChatDemoFlow.get(navigator);
        conversation = flow.selectedProperty().get();
        flow.searchRevisionProperty().addListener(revisionListener);
        showInfo();
    }

    @Override
    public void onLeave() {
        flow.searchRevisionProperty().removeListener(revisionListener);
        flow = null;
        navigator = null;
        if (confirmation != null) {
            confirmation.close();
        }
    }

    @FXML
    private void close() {
        navigator.closeDrawer();
    }

    @FXML
    private void showInfo() {
        drawerLayout.setCenter(infoPane);
        back.setVisible(false);
        heading.setText(conversation != null && conversation.encrypted() ? "Thông tin nhóm" : "Thông tin đoạn chat");
        showDetails();
    }

    private void startSearch() {
        drawerLayout.setCenter(searchPane);
        heading.setText("Tìm trong đoạn chat");
        back.setVisible(true);
        updateSearch();
        conversationSearchField.requestFocus();
    }

    private void updateView() {
        if (drawerLayout.getCenter() == searchPane) {
            updateSearch();
        }
    }

    private void showDetails() {
        infoContent.getChildren().clear();
        if (conversation == null) {
            return;
        }
        var avatar = new ConversationAvatar();
        avatar.show(conversation, flow);
        var identity = section(avatar, text(conversation.title(), "chat-heading"),
                text(conversation.encrypted() ? "E2EE · " + conversation.participantIds().size() + " thành viên"
                        : flow.statusText(conversation), "chat-caption"));
        identity.getStyleClass().add("chat-drawer-identity");
        identity.getChildren().add(action("searchInConversation", "search", "Tìm kiếm trong hội thoại", false,
                this::startSearch));
        infoContent.getChildren().add(identity);
        if (conversation.encrypted()) {
            addGroupDetails();
        } else {
            infoContent.getChildren().add(section(text("Dữ liệu hội thoại", "chat-section-label"), clearAction()));
            boolean blocked = flow.chat().access(conversation.id()) == OfflineChatDemoService.Access.BLOCKED_BY_SELF;
            infoContent.getChildren().add(section(text("An toàn và hỗ trợ", "chat-section-label"), reportAction(),
                    action("blockConversationPeer", "block-user", blocked ? "Bỏ chặn người dùng" : "Chặn người dùng",
                            !blocked, () -> confirmBlock(!blocked))));
        }
    }

    private void addGroupDetails() {
        var group = flow.data().groups().stream().filter(item -> item.id().equals(conversation.id()))
                .findFirst().orElseThrow();
        boolean admin = group.members().stream().anyMatch(member -> member.userId().equals(MockData.PREVIEW_USER_ID)
                && member.role() == MockGroup.Role.ADMIN);
        if (admin) {
            infoContent.getChildren().add(section(text("Quản lý nhóm", "chat-section-label"),
                    action("renameConversationGroup", "refine", "Đổi tên nhóm", false, null)));
        }
        var members = section(text("Thành viên", "chat-section-label"),
                action("addConversationMember", "add-member", "Thêm thành viên", false, null));
        for (var member : group.members()) {
            var user = flow.data().user(member.userId());
            boolean self = user.id().equals(MockData.PREVIEW_USER_ID);
            var memberAvatar = new ConversationAvatar();
            memberAvatar.show(user);
            var labels = new VBox(4, text(self ? "Bạn" : user.fullName(), "chat-result-title"),
                    text(member.role() == MockGroup.Role.ADMIN ? "Admin" : "Thành viên", "chat-caption"));
            HBox.setHgrow(labels, Priority.ALWAYS);
            var memberRow = new HBox(12, memberAvatar, labels);
            memberRow.setAlignment(Pos.CENTER_LEFT);
            memberRow.getStyleClass().add("chat-member-row");
            if (admin && !self) {
                Button options = new Button("", new ChatIcon("message-more"));
                options.setAccessibleText("Quản lý " + user.fullName());
                options.getStyleClass().add("chat-drawer-icon");
                options.setDisable(true);
                memberRow.getChildren().add(options);
            }
            members.getChildren().add(memberRow);
        }

        var security = section(text("An toàn và hỗ trợ", "chat-section-label"));
        Label description = text("Nội dung nhóm chỉ đọc được trên thiết bị có khóa E2EE hợp lệ.", "chat-caption");
        description.setVisible(false);
        description.managedProperty().bind(description.visibleProperty());
        security.getChildren().addAll(action("showConversationEncryption", "lock", "Thông tin mã hóa E2EE", false,
                        () -> description.setVisible(!description.isVisible())), description, reportAction());
        var role = section(text("Vai trò và tư cách thành viên", "chat-section-label"));
        if (admin) {
            role.getChildren().add(action("resignConversationAdmin", "resign-admin", "Thôi quyền Admin", true, null));
        }
        role.getChildren().add(action("leaveConversationGroup", "logout", "Rời nhóm", true, null));
        infoContent.getChildren().addAll(members, security,
                section(text("Dữ liệu hội thoại", "chat-section-label"), clearAction()), role);
    }

    private Button clearAction() {
        return action("clearConversationHistory", "clear-history", "Xóa lịch sử phía mình", false,
                () -> confirm("Xóa lịch sử phía mình",
                        "Toàn bộ lịch sử của đoạn chat này sẽ bị xóa ở phía bạn. Người khác vẫn giữ tin nhắn của họ.",
                        () -> flow.chat().clearHistory(conversation.id())));
    }

    private Button reportAction() {
        return action("reportConversation", "spam-report", "Báo cáo spam", false, null);
    }

    private void confirmBlock(boolean blocked) {
        String title = blocked ? "Chặn người dùng" : "Bỏ chặn người dùng";
        String text = blocked ? "Hai bên không thể gửi tin nhắn riêng mới. Lịch sử cũ vẫn được giữ."
                : "Đoạn chat chỉ có thể gửi tin mới khi cả hai bên đều không chặn nhau.";
        confirm(title, text, () -> flow.chat().setBlocked(conversation.id(), blocked));
    }

    private void confirm(String title, String message, Runnable change) {
        ButtonType proceed = new ButtonType(title);
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, message, proceed, ButtonType.CANCEL);
        confirmation = dialog;
        dialog.initOwner(drawerLayout.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        navigator.themes().apply(dialog.getDialogPane());
        dialog.setOnHidden(event -> {
            confirmation = null;
            if (flow != null && dialog.getResult() == proceed) {
                change.run();
                flow.conversationChanged();
                showDetails();
            }
        });
        dialog.show();
    }

    private void updateSearch() {
        if (flow == null || conversation == null) {
            return;
        }
        String query = conversationSearchField.getText();
        var result = flow.search(conversation.id(), query);
        syncing = result.syncing();
        matches = result.messages();
        if (selectedIndex() < 0) {
            selectedMessageId = null;
        }
        cards.clear();
        localResults.getChildren().clear();
        for (int i = 0; i < matches.size(); i++) {
            MockMessage message = matches.get(i);
            String sender = message.senderId().equals(MockData.PREVIEW_USER_ID) ? "Bạn"
                    : flow.data().user(message.senderId()).fullName();
            var card = new SearchResultCard(sender, message.text(), message.sentAt(), null, () -> pick(message));
            card.setId("local-message-" + message.id());
            cards.add(card);
        }
        localResults.getChildren().addAll(cards);
        if (result.missingKey()) {
            localResults.getChildren().add(text("Chưa thể tìm trong lịch sử nhóm. Cần khôi phục khóa E2EE.", "chat-warning-text"));
        } else if (result.syncing()) {
            localResults.getChildren().add(text("Đang đồng bộ lịch sử. Kết quả hiện tại chưa đầy đủ.", "chat-caption"));
        } else if (matches.isEmpty() && !query.isBlank()) {
            Label empty = text("Không tìm thấy tin nhắn. Thử từ khóa khác.", "chat-caption");
            empty.setId("localSearchEmpty");
            localResults.getChildren().add(empty);
        }
        updateNavigation();
    }

    @FXML
    private void previous() {
        int index = selectedIndex();
        if (index > 0) {
            pick(index - 1);
        }
    }

    @FXML
    private void next() {
        int index = selectedIndex() + 1;
        if (index < matches.size()) {
            pick(index);
        }
    }

    private int selectedIndex() {
        for (int i = 0; selectedMessageId != null && i < matches.size(); i++) {
            if (matches.get(i).id().equals(selectedMessageId)) {
                return i;
            }
        }
        return -1;
    }

    private void pick(int index) {
        pick(matches.get(index));
    }

    private void pick(MockMessage message) {
        if (flow.jumpToMessage(conversation.id(), message.id())) {
            selectedMessageId = message.id();
            updateNavigation();
            int index = selectedIndex();
            localResults.layout();
            double overflow = localResults.getHeight() - localResultScroll.getViewportBounds().getHeight();
            if (overflow > 0 && index >= 0) {
                localResultScroll.setVvalue(Math.clamp(cards.get(index).getBoundsInParent().getMinY() / overflow, 0, 1));
            }
        }
    }

    private void updateNavigation() {
        int index = selectedIndex();
        previousResult.setDisable(index < 1);
        nextResult.setDisable(index + 1 >= matches.size());
        String count = conversationSearchField.getText().isBlank() ? "Nhập từ khóa"
                : index < 0 ? matches.size() + " kết quả" : "Kết quả " + (index + 1) + " / " + matches.size();
        resultCount.setText(syncing ? count + " · đang đồng bộ" : count);
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setCurrent(i == index);
        }
    }

    private static Button action(String id, String icon, String title, boolean danger, Runnable callback) {
        Button button = new Button(title, new ChatIcon(icon));
        button.setId(id);
        button.setWrapText(true);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add("chat-drawer-action");
        if (danger) {
            button.getStyleClass().add("chat-drawer-danger");
        }
        if (callback == null) {
            button.setDisable(true);
        } else {
            button.setOnAction(event -> callback.run());
        }
        return button;
    }

    private static VBox section(Node... children) {
        VBox section = new VBox(8, children);
        section.getStyleClass().add("chat-drawer-section");
        return section;
    }

    private static Label text(String value, String style) {
        Label label = new Label(value);
        label.setWrapText(true);
        label.getStyleClass().add(style);
        return label;
    }
}
