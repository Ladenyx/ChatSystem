package com.chatsystem.user.controller.messaging;

import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.control.ConversationCell;
import com.chatsystem.user.messaging.ConversationCatalog;
import com.chatsystem.user.navigation.UserViewInputs;
import java.util.Locale;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public final class ConversationsController implements ViewController {
    private static final double MIN_SIDEBAR = 280;
    private static final double MAX_SIDEBAR = 384;
    private static final double CHAT_PREFERRED_SPACE = 672;

    @FXML private BorderPane workspace;
    @FXML private VBox conversationSidebar;
    @FXML private ListView<MockConversation> conversationItems;
    @FXML private TextField conversationFilter;
    @FXML private ToggleGroup filters;
    @FXML private ToggleButton allFilter;
    @FXML private ToggleButton unreadFilter;
    @FXML private ToggleButton groupFilter;
    @FXML private VBox emptyWorkspace;
    @FXML private BorderPane selectedWorkspace;
    @FXML private Label conversationTitle;
    @FXML private Label conversationStatus;
    @FXML private ConversationAvatar conversationAvatar;

    private ViewNavigator navigator;
    private final ConversationCatalog catalog = UserViews.CONVERSATIONS_DATA;
    private MockConversation selected;
    private FilteredList<MockConversation> visibleConversations;
    private boolean updatingSelection;

    @FXML
    private void initialize() {
        conversationSidebar.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.clamp(workspace.getWidth() - CHAT_PREFERRED_SPACE, MIN_SIDEBAR, MAX_SIDEBAR),
                workspace.widthProperty()));
        emptyWorkspace.managedProperty().bind(emptyWorkspace.visibleProperty());
        selectedWorkspace.managedProperty().bind(selectedWorkspace.visibleProperty());

        conversationItems.getSelectionModel().selectedItemProperty().addListener((observable, previous, current) -> {
            if (!updatingSelection && current != null) {
                selected = current;
                navigator.closeDrawer();
                showSelection(current);
            }
        });
        conversationFilter.textProperty().addListener((observable, previous, current) -> applyFilter());
        filters.selectedToggleProperty().addListener((observable, previous, current) -> {
            if (current == null && previous != null) {
                filters.selectToggle(previous);
            } else {
                applyFilter();
            }
        });
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
        if (visibleConversations == null) {
            visibleConversations = new FilteredList<>(FXCollections.observableArrayList(catalog.conversations()));
            conversationItems.setCellFactory(list -> new ConversationCell(catalog));
            conversationItems.setItems(visibleConversations);
            allFilter.setSelected(true);
        }
        applyFilter();
        showSelection(selected);
    }

    @Override
    public void onLeave() {
        navigator = null;
    }

    @FXML
    private void createGroup() {
        navigator.showModal(UserViews.CREATE_GROUP);
    }

    @FXML
    private void openInfo() {
        if (!navigator.isDrawerOpen()) {
            navigator.showDrawer(UserViews.CONVERSATION_INFO, new UserViewInputs.Conversation(selected.id()));
        } else {
            navigator.closeDrawer();
        }
    }

    private void applyFilter() {
        if (navigator == null || visibleConversations == null) {
            return;
        }
        String query = conversationFilter.getText().strip().toLowerCase(Locale.ROOT);
        updatingSelection = true;
        try {
            visibleConversations.setPredicate(chat -> chat.title().toLowerCase(Locale.ROOT).contains(query)
                    && (!unreadFilter.isSelected() || chat.unreadCount() > 0)
                    && (!groupFilter.isSelected() || chat.kind() == MockConversation.Kind.GROUP));
            var selection = conversationItems.getSelectionModel();
            int selectedIndex = visibleConversations.indexOf(selected);
            if (selectedIndex < 0) {
                selection.clearSelection();
            } else {
                selection.select(selectedIndex);
            }
        } finally {
            updatingSelection = false;
        }
    }

    private void showSelection(MockConversation conversation) {
        emptyWorkspace.setVisible(conversation == null);
        selectedWorkspace.setVisible(conversation != null);
        if (conversation != null) {
            conversationTitle.setText(conversation.title());
            conversationStatus.setText(catalog.statusText(conversation));
            conversationStatus.setGraphic(ConversationAvatar.statusGraphic(conversation, catalog));
            conversationAvatar.show(conversation, catalog);
        }
    }
}
