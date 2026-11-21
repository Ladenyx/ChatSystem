package com.chatsystem.user.controller.messaging;

import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.navigation.UserViewInputs;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class ConversationInfoController implements ViewController {
    @FXML private ConversationAvatar avatar;
    @FXML private Label title;
    @FXML private Label status;

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        if (input instanceof UserViewInputs.Conversation selection) {
            var catalog = UserViews.CONVERSATIONS_DATA;
            catalog.conversations().stream().filter(chat -> chat.id().equals(selection.conversationId()))
                    .findFirst().ifPresent(chat -> {
                        title.setText(chat.title());
                        status.setText(catalog.statusText(chat));
                        avatar.show(chat, catalog);
                    });
        }
    }
}
