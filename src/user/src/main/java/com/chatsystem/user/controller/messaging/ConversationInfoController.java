package com.chatsystem.user.controller.messaging;

import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ChatIcon;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.control.SearchResultCard;
import com.chatsystem.user.messaging.ConversationCatalog;
import com.chatsystem.user.navigation.UserViewInputs;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

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
    private List<MockMessage> matches = List.of();
    private int currentIndex = -1;

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        this.navigator = navigator;
        if (input instanceof UserViewInputs.MessageSearch details) {
            search = details;
            catalog.conversations().stream().filter(chat -> chat.id().equals(details.conversationId()))
                    .findFirst().ifPresent(this::renderInfo);
            conversationSearchField.textProperty().addListener((observable, old, query) -> renderResults(query));
        }
        showInfo();
    }

    private void renderInfo(MockConversation chat) {
        var avatar = new ConversationAvatar();
        avatar.show(chat, catalog);
        var title = new Label(chat.title());
        title.getStyleClass().add("chat-heading");
        title.setWrapText(true);
        var identity = new VBox(8, avatar, title, new Label(catalog.statusText(chat)),
                action("searchInConversation", "search", "Tìm kiếm trong hội thoại", this::showSearch));
        identity.getStyleClass().add("chat-drawer-section");
        infoContent.getChildren().setAll(identity);
        if (chat.encrypted()) {
            var members = new VBox(8, new Label("Thành viên"));
            chat.participantIds().stream().map(catalog::user).forEach(user ->
                    members.getChildren().add(new Label(user.fullName())));
            members.getChildren().add(action("addConversationMember", "add-member", "Thêm thành viên", null));
            members.getStyleClass().add("chat-drawer-section");
            infoContent.getChildren().add(members);
        } else {
            infoContent.getChildren().add(action("blockConversationPeer", "block-user", "Chặn người dùng", null));
        }
        infoContent.getChildren().addAll(
                action("clearConversationHistory", "clear-history", "Xóa lịch sử phía mình", null),
                action("reportConversation", "spam-report", "Báo cáo spam", null));
    }

    private Button action(String id, String icon, String text, Runnable handler) {
        var button = new Button(text, new ChatIcon(icon));
        button.setId(id);
        button.setWrapText(true);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("chat-drawer-action");
        if (handler == null) {
            button.setDisable(true);
        } else {
            button.setOnAction(event -> handler.run());
        }
        return button;
    }

    @FXML
    private void showInfo() {
        drawerLayout.setCenter(infoPane);
        heading.setText("Thông tin đoạn chat");
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
            var result = new SearchResultCard(catalog.user(message.senderId()).fullName(), message.text(),
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
