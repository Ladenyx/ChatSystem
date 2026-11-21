package com.chatsystem.user.controller.chat;

import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.control.ConversationAvatar;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class ConversationInfoController implements ViewController {
    @FXML private ConversationAvatar avatar;
    @FXML private Label title;
    @FXML private Label status;

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        var flow = ChatDemoFlow.get(navigator);
        var chat = flow.selectedProperty().get();
        if (chat != null) {
            title.setText(chat.title());
            status.setText(flow.statusText(chat));
            avatar.show(chat, flow);
        }
    }
}
