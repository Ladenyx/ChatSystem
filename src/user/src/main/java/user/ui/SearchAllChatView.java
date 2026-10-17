package user.ui;

import common.mock.MockData;
import common.model.ChatGroup;
import common.model.Conversation;
import common.model.Message;
import common.ui.component.AvatarLabel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen #10: Search All Chat History (Roadmap U-18).
 * Features:
 * - Full-text search across all direct conversations and group chats.
 * - Displays matched messages with context, conversation title, sender, and timestamp.
 * - "Jump to message" action that navigates to the target conversation and highlights the message.
 */
public class SearchAllChatView extends BorderPane {

    public static class SearchResult {
        public final Conversation conversation; // null if group message
        public final ChatGroup group;           // null if direct message
        public final Message message;

        public SearchResult(Conversation conversation, ChatGroup group, Message message) {
            this.conversation = conversation;
            this.group = group;
            this.message = message;
        }

        public boolean isGroup() {
            return group != null;
        }

        public String getConversationTitle() {
            if (isGroup()) return group.getName();
            return conversation != null && conversation.getOtherUser() != null
                    ? conversation.getOtherUser().getFullName()
                    : "Direct Chat";
        }
    }

    public interface JumpHandler {
        void onJump(Conversation conversation, ChatGroup group, Message message);
    }

    private final ObservableList<SearchResult> resultsList = FXCollections.observableArrayList();
    private final ListView<SearchResult> listView = new ListView<>();
    private final Label countBadge = new Label();
    private final JumpHandler jumpHandler;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public SearchAllChatView(JumpHandler jumpHandler) {
        this.jumpHandler = jumpHandler;
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));

        // Header Section
        VBox header = createHeader();
        setTop(header);

        // Center Section
        buildListView();
        VBox centerBox = new VBox(listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        setCenter(centerBox);

        updateCount(0);
    }

    private VBox createHeader() {
        Label title = new Label("Search Chat History");
        title.getStyleClass().add("section-title");

        countBadge.getStyleClass().add("badge-active");

        HBox titleRow = new HBox(12, title, countBadge);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Find messages, links, and discussions across all direct messages and group chats.");
        subtitle.getStyleClass().add("login-subtitle");

        TextField searchField = new TextField();
        searchField.setPromptText("Type keyword to search across all chats (e.g. database, code, deadline)...");
        searchField.getStyleClass().add("search-field");
        searchField.setMaxWidth(460);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> performSearch(newVal));

        VBox header = new VBox(8, titleRow, subtitle, searchField);
        header.setPadding(new Insets(0, 0, 16, 0));
        return header;
    }

    private void buildListView() {
        listView.getStyleClass().add("list-view-clean");
        listView.setItems(resultsList);
        listView.setPlaceholder(new Label("Type keywords in the search box to find messages."));

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(SearchResult result, boolean empty) {
                super.updateItem(result, empty);
                if (empty || result == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // Top: Conversation Name + Badge (Direct/Group) + Time
                Label convTitle = new Label(result.getConversationTitle());
                convTitle.getStyleClass().add("text-name");

                Label typeBadge = new Label(result.isGroup() ? "Group" : "Direct");
                typeBadge.getStyleClass().add(result.isGroup() ? "avatar-group" : "badge-active");
                typeBadge.setStyle("-fx-font-size: 10px; -fx-padding: 1 6 1 6; -fx-background-radius: 4;");

                Region topSpacer = new Region();
                HBox.setHgrow(topSpacer, Priority.ALWAYS);

                String timeStr = result.message.getSentAt() != null
                        ? result.message.getSentAt().format(TIME_FORMATTER)
                        : "";
                Label timeLbl = new Label(timeStr);
                timeLbl.getStyleClass().add("text-muted");

                HBox topRow = new HBox(8, convTitle, typeBadge, topSpacer, timeLbl);
                topRow.setAlignment(Pos.CENTER_LEFT);

                // Middle: Sender & Message Content snippet
                Label senderLbl = new Label(result.message.getSenderName() + ": ");
                senderLbl.getStyleClass().add("text-bold");
                senderLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #7678ed;");

                Label contentLbl = new Label(result.message.getContent());
                contentLbl.setWrapText(true);
                contentLbl.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #202022;");
                HBox.setHgrow(contentLbl, Priority.ALWAYS);

                HBox msgRow = new HBox(4, senderLbl, contentLbl);
                msgRow.setAlignment(Pos.CENTER_LEFT);

                // Bottom: Jump Button
                Region btmSpacer = new Region();
                HBox.setHgrow(btmSpacer, Priority.ALWAYS);

                Button jumpBtn = new Button("Jump to chat →");
                jumpBtn.getStyleClass().add("btn-accent");
                jumpBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10 4 10;");
                jumpBtn.setOnAction(e -> {
                    if (jumpHandler != null) {
                        jumpHandler.onJump(result.conversation, result.group, result.message);
                    }
                });

                HBox actionRow = new HBox(btmSpacer, jumpBtn);
                actionRow.setAlignment(Pos.CENTER_RIGHT);

                // Full Card
                VBox card = new VBox(6, topRow, msgRow, actionRow);
                card.getStyleClass().add("card");
                card.setPadding(new Insets(12, 16, 10, 16));
                HBox.setHgrow(card, Priority.ALWAYS);

                setGraphic(card);
                setText(null);
            }
        });
    }

    private void performSearch(String query) {
        resultsList.clear();
        if (query == null || query.isBlank()) {
            updateCount(0);
            return;
        }

        String lower = query.trim().toLowerCase();
        List<SearchResult> matched = new ArrayList<>();

        // 1. Search in Direct Conversations
        for (Conversation conv : MockData.CONVERSATIONS) {
            for (Message msg : conv.getMessages()) {
                if (msg.getContent() != null && msg.getContent().toLowerCase().contains(lower)) {
                    matched.add(new SearchResult(conv, null, msg));
                }
            }
        }

        // 2. Search in Groups
        for (ChatGroup group : MockData.GROUPS) {
            for (Message msg : group.getMessages()) {
                if (msg.getContent() != null && msg.getContent().toLowerCase().contains(lower)) {
                    matched.add(new SearchResult(null, group, msg));
                }
            }
        }

        resultsList.setAll(matched);
        updateCount(matched.size());
    }

    private void updateCount(int count) {
        countBadge.setText(count + " found");
        countBadge.setVisible(count > 0);
    }
}

