package com.chatsystem.user.controller;

import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ConversationCell;
import com.chatsystem.user.controller.chat.ChatDemoFlow;
import com.chatsystem.user.controller.chat.ChatHistoryController;
import java.util.Locale;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
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
    @FXML private ChatHistoryController selectedWorkspaceController;

    private final ObservableList<MockConversation> conversations = FXCollections.observableArrayList();
    private ViewNavigator navigator;
    private ChatDemoFlow flow;
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
            if (!updatingSelection && flow != null && current != null) {
                flow.select(current);
                flow.chat().markRead(current.id());
                navigator.closeDrawer();
                showSelection(current);
                refreshSidebar();
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
    }

    private void showSelection(MockConversation conversation) {
        emptyWorkspace.setVisible(conversation == null);
        selectedWorkspace.setVisible(conversation != null);
        if (conversation != null) {
            selectedWorkspaceController.show(navigator, flow, conversation, this::refreshSidebar);
        }
    }
}
