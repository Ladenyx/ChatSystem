package com.chatsystem.user.controller;

import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import javafx.fxml.FXML;

public final class ConversationsController implements ViewController {
    private ViewNavigator navigator;

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
    }

    @FXML
    private void openContacts() {
        navigator.navigate(UserViews.CONTACTS);
    }

    @FXML
    private void createGroup() {
        navigator.showModal(UserViews.CREATE_GROUP);
    }

    @FXML
    private void openInfo() {
        navigator.showDrawer(UserViews.CONVERSATION_INFO);
    }
}
