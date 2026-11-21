package com.chatsystem.user.controller.messaging;

import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.user.control.SearchResultCard;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.controller.chat.ChatHistoryController;
import com.chatsystem.user.control.ConversationCell;
import com.chatsystem.user.messaging.ConversationCatalog;
import java.util.Locale;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
    @FXML private Button createGroupButton;
    @FXML private HBox filterBar;
    @FXML private ScrollPane globalSearch;
    @FXML private VBox globalResults;
    @FXML private VBox emptyWorkspace;
    @FXML private BorderPane selectedWorkspace;
    @FXML private ChatHistoryController selectedWorkspaceController;

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
        for (var node : java.util.List.of(createGroupButton, filterBar, conversationItems, globalSearch)) {
            node.managedProperty().bind(node.visibleProperty());
        }

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
        showSearchResults(query);
    }

    private void showSearchResults(String query) {
        boolean searching = !query.isBlank();
        globalSearch.setVisible(searching);
        createGroupButton.setVisible(!searching);
        filterBar.setVisible(!searching);
        conversationItems.setVisible(!searching);
        globalResults.getChildren().clear();
        if (!searching) {
            return;
        }
        for (var conversation : catalog.conversations()) {
            if (conversation.title().toLowerCase(Locale.ROOT).contains(query)) {
                var avatar = new ConversationAvatar();
                avatar.show(conversation, catalog);
                globalResults.getChildren().add(new SearchResultCard(conversation.title(),
                        catalog.statusText(conversation), null, avatar, () -> openResult(conversation, null)));
            }
            catalog.messagesFor(conversation.id()).stream()
                    .filter(message -> message.state() != MockMessage.State.RECALLED)
                    .filter(message -> message.text().toLowerCase(Locale.ROOT).contains(query))
                    .limit(5)
                    .forEach(message -> globalResults.getChildren().add(new SearchResultCard(conversation.title(),
                            message.text(), message.sentAt(), null, () -> openResult(conversation, message.id()))));
        }
        if (globalResults.getChildren().isEmpty()) {
            globalResults.getChildren().add(new Label("Không có kết quả"));
        }
    }

    private void openResult(MockConversation conversation, String messageId) {
        selected = conversation;
        allFilter.setSelected(true);
        conversationFilter.clear();
        navigator.closeDrawer();
        showSelection(conversation);
        if (messageId != null) {
            selectedWorkspaceController.scrollToMessage(messageId);
        }
    }

    private void showSelection(MockConversation conversation) {
        emptyWorkspace.setVisible(conversation == null);
        selectedWorkspace.setVisible(conversation != null);
        if (conversation != null) {
            selectedWorkspaceController.show(navigator, catalog, conversation);
        }
    }
}
