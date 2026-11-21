package com.chatsystem.user.controller.messaging;

import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.messaging.ConversationCatalog;
import com.chatsystem.user.navigation.UserViewInputs;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class ConversationInfoController implements ViewController {
    @FXML private ConversationAvatar avatar;
    @FXML private Label title;
    @FXML private Label status;
    @FXML private Label kindValue;
    @FXML private Label securityValue;
    @FXML private Label memberCountValue;
    @FXML private VBox participantsList;

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        if (input instanceof UserViewInputs.Conversation selection) {
            var catalog = UserViews.CONVERSATIONS_DATA;
            catalog.conversations().stream().filter(chat -> chat.id().equals(selection.conversationId()))
                    .findFirst().ifPresent(chat -> showConversation(chat, catalog));
        }
    }

    private void showConversation(MockConversation chat, ConversationCatalog catalog) {
        title.setText(chat.title());
        status.setText(catalog.statusText(chat));
        status.setGraphic(ConversationAvatar.statusGraphic(chat, catalog));
        avatar.show(chat, catalog);

        boolean isGroup = chat.kind() == MockConversation.Kind.GROUP;
        kindValue.setText(isGroup ? "Nhóm trò chuyện" : "Trực tiếp");
        securityValue.setText(chat.encrypted() ? "Mã hóa đầu cuối" : "Tiêu chuẩn");
        memberCountValue.setText(chat.participantIds().size() + " người");

        participantsList.getChildren().clear();
        for (String participantId : chat.participantIds()) {
            participantsList.getChildren().add(memberRow(catalog.user(participantId), catalog.currentUserId()));
        }
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
}
