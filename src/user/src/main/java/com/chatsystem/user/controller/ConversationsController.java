package com.chatsystem.user.controller;

import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.control.ConversationCell;
import com.chatsystem.user.control.SearchResultCard;
import com.chatsystem.user.controller.chat.ChatDemoFlow;
import com.chatsystem.user.controller.chat.ChatHistoryController;
import java.util.Locale;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    @FXML private Button createGroupButton;
    @FXML private HBox filterBar;
    @FXML private ScrollPane globalSearch;
    @FXML private VBox globalResults;
    @FXML private Label globalSyncNotice;
    @FXML private ToggleGroup filters;
    @FXML private ToggleButton allFilter;
    @FXML private ToggleButton unreadFilter;
    @FXML private ToggleButton groupFilter;
    @FXML private VBox emptyWorkspace;
    @FXML private BorderPane selectedWorkspace;
    @FXML private ChatHistoryController selectedWorkspaceController;

    private final ObservableList<MockConversation> conversations = FXCollections.observableArrayList();
    private ViewNavigator navigator;
    private ChatDemoFlow flow;
    private FilteredList<MockConversation> visibleConversations;
    private boolean updatingSelection;
    private final InvalidationListener searchChanged = ignored -> refreshSidebar();

    @FXML
    private void initialize() {
        conversationSidebar.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.clamp(workspace.getWidth() - CHAT_PREFERRED_SPACE, MIN_SIDEBAR, MAX_SIDEBAR),
                workspace.widthProperty()));
        emptyWorkspace.managedProperty().bind(emptyWorkspace.visibleProperty());
        selectedWorkspace.managedProperty().bind(selectedWorkspace.visibleProperty());
        for (var node : List.of(createGroupButton, filterBar, conversationItems, globalSearch, globalSyncNotice)) {
            node.managedProperty().bind(node.visibleProperty());
        }

        conversationItems.getSelectionModel().selectedItemProperty().addListener((observable, previous, current) -> {
            if (!updatingSelection && flow != null && current != null) {
                openConversation(current);
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
        flow = ChatDemoFlow.get(navigator);
        flow.connectWorkspace(this::jumpToMessage, selectedWorkspaceController::refreshAfterDrawerChange,
                this::openCreatedGroup);
        flow.searchRevisionProperty().addListener(searchChanged);
        if (visibleConversations == null) {
            visibleConversations = new FilteredList<>(conversations);
            ChatDemoFlow session = flow;
            conversationItems.setCellFactory(list -> new ConversationCell(session));
            conversationItems.setItems(visibleConversations);
            allFilter.setSelected(true);
        }
        refreshSidebar();
        showSelection(flow.selectedProperty().get());
    }

    @Override
    public void onLeave() {
        selectedWorkspaceController.detach();
        flow.disconnectWorkspace();
        flow.searchRevisionProperty().removeListener(searchChanged);
        navigator = null;
        flow = null;
    }

    @FXML
    private void createGroup() {
        navigator.showModal(UserViews.CREATE_GROUP);
    }

    private void refreshSidebar() {
        updateSidebar(true);
    }

    private void applyFilter() {
        updateSidebar(false);
    }

    private void updateSidebar(boolean refresh) {
        if (flow == null || visibleConversations == null) {
            return;
        }
        String query = conversationFilter.getText().strip().toLowerCase(Locale.ROOT);
        updatingSelection = true;
        try {
            var selection = conversationItems.getSelectionModel();
            int previousIndex = selection.getSelectedIndex();
            if (refresh) {
                conversations.setAll(flow.chat().conversations());
            }
            visibleConversations.setPredicate(chat -> chat.title().toLowerCase(Locale.ROOT).contains(query)
                    && (!unreadFilter.isSelected() || chat.unreadCount() > 0)
                    && (!groupFilter.isSelected() || chat.kind() == MockConversation.Kind.GROUP));
            MockConversation selected = flow.selectedProperty().get();
            int selectedIndex = -1;
            for (int i = 0; selected != null && i < visibleConversations.size(); i++) {
                if (visibleConversations.get(i).id().equals(selected.id())) {
                    selectedIndex = i;
                    break;
                }
            }
            if (selectedIndex < 0) {
                selection.clearSelection();
            } else {
                selection.select(selectedIndex);
            }
            if (refresh) {
                conversationItems.refresh();
                if (selectedIndex == 0 && previousIndex > 0) {
                    conversationItems.scrollTo(0);
                }
            }
        } finally {
            updatingSelection = false;
        }
        showGlobalResults();
    }

    private void showGlobalResults() {
        String query = conversationFilter.getText();
        boolean searching = !query.isBlank();
        globalSearch.setVisible(searching);
        filterBar.setVisible(!searching);
        createGroupButton.setVisible(!searching);
        conversationItems.setVisible(!searching);
        globalResults.getChildren().clear();
        if (!searching) {
            globalSyncNotice.setVisible(false);
            return;
        }
        var result = flow.search(null, query);
        globalSyncNotice.setVisible(result.syncing());
        globalResults.getChildren().add(resultHeading("Hội thoại", "globalConversationHeading"));
        for (var chat : result.conversations()) {
            var avatar = new ConversationAvatar();
            avatar.show(chat, flow);
            var latest = flow.chat().latest(chat.id());
            var card = new SearchResultCard(chat.title(), flow.statusText(chat), latest == null ? null : latest.sentAt(),
                    avatar, () -> openConversation(chat));
            card.setId("global-conversation-" + chat.id());
            globalResults.getChildren().add(card);
        }
        globalResults.getChildren().add(resultHeading("Tin nhắn", "globalMessageHeading"));
        for (var message : result.messages()) {
            var chat = conversations.stream().filter(item -> item.id().equals(message.conversationId())).findFirst().orElseThrow();
            var card = new SearchResultCard(chat.title(), message.text(), message.sentAt(), null,
                    () -> flow.jumpToMessage(chat.id(), message.id()));
            card.setId("global-message-" + message.id());
            globalResults.getChildren().add(card);
        }
        if (!result.syncing() && result.conversations().isEmpty() && result.messages().isEmpty()) {
            globalResults.getChildren().add(searchNote("Không tìm thấy kết quả. Thử từ khóa khác.", "globalSearchEmpty"));
        }
    }

    private static Label resultHeading(String title, String id) {
        var label = new Label(title);
        label.setId(id);
        label.getStyleClass().add("chat-result-title");
        return label;
    }

    private static Label searchNote(String text, String id) {
        var label = new Label(text);
        label.setId(id);
        label.setWrapText(true);
        label.getStyleClass().add("chat-caption");
        return label;
    }

    private void openConversation(MockConversation chat) {
        var selected = flow.selectedProperty().get();
        if (selected == null || !selected.id().equals(chat.id())) {
            navigator.closeDrawer();
        }
        flow.select(chat);
        flow.chat().markRead(chat.id());
        showSelection(chat);
        refreshSidebar();
    }

    private void openCreatedGroup(MockConversation chat) {
        conversationFilter.clear();
        allFilter.setSelected(true);
        openConversation(chat);
        conversationItems.scrollTo(chat);
    }

    private boolean jumpToMessage(String conversationId, String messageId) {
        int limit = flow.chat().historyLimitFor(conversationId, messageId);
        var target = conversations.stream().filter(chat -> chat.id().equals(conversationId)).findFirst().orElse(null);
        var selected = flow.selectedProperty().get();
        if (limit > 0 && target != null) {
            if (selected == null || !selected.id().equals(conversationId)) {
                openConversation(target);
            }
            selectedWorkspaceController.revealMessage(messageId, limit);
            return true;
        }
        if (selected == null && target != null) {
            openConversation(target);
        }
        if (flow.selectedProperty().get() != null) {
            selectedWorkspaceController.showUnavailableResult(() -> {
                var local = navigator.getRoot().lookup("#conversationSearchField");
                (local != null ? local : conversationFilter).requestFocus();
            });
        }
        flow.searchChanged();
        return false;
    }

    private void showSelection(MockConversation conversation) {
        emptyWorkspace.setVisible(conversation == null);
        selectedWorkspace.setVisible(conversation != null);
        if (conversation != null) {
            selectedWorkspaceController.show(navigator, flow, conversation, flow::searchChanged);
        }
    }
}
