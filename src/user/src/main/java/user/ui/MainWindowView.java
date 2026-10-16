package user.ui;

import common.mock.MockData;
import common.model.Conversation;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.NavSidebar;
import common.ui.component.StatusBadge;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MainWindowView extends BorderPane {
    public MainWindowView(Runnable onLogout) {
        getStyleClass().add("main-bg");
        BorderPane frame = new BorderPane();
        frame.getStyleClass().add("content-area");
        NavSidebar sidebar = new NavSidebar("Chats", "Friends", "Online", "Requests", "Find people", "Groups");
        sidebar.select("Chats");
        sidebar.addBottomAction("Profile", () -> {});
        sidebar.addBottomAction("Log out", onLogout);
        frame.setLeft(sidebar);

        VBox conversations = new VBox(12);
        conversations.setPadding(new Insets(20, 14, 16, 14));
        conversations.setPrefWidth(280);
        Label heading = new Label("Messages");
        heading.getStyleClass().add("login-title");
        TextField search = new TextField();
        search.setPromptText("Search conversations");
        search.getStyleClass().add("search-field");
        ListView<Conversation> list = new ListView<>();
        list.getStyleClass().add("list-view-clean");
        FilteredList<Conversation> filtered = new FilteredList<>(FXCollections.observableArrayList(MockData.CONVERSATIONS));
        list.setItems(filtered);
        search.textProperty().addListener((obs, oldText, newText) -> {
            String query = newText == null ? "" : newText.trim().toLowerCase();
            filtered.setPredicate(conversation -> conversation.getOtherUser() != null
                    && (conversation.getOtherUser().getFullName().toLowerCase().contains(query)
                    || conversation.getOtherUser().getUsername().toLowerCase().contains(query)));
        });
        list.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(Conversation conversation, boolean empty) {
                super.updateItem(conversation, empty);
                if (empty || conversation == null) { setGraphic(null); setText(null); return; }
                User contact = conversation.getOtherUser();
                Label name = new Label(contact == null ? "Conversation" : contact.getFullName());
                name.getStyleClass().add("text-name");
                String preview = conversation.getLastMessage() == null ? "No messages yet" : conversation.getLastMessage().getContent();
                Label message = new Label(preview);
                message.getStyleClass().add("text-muted");
                message.setMaxWidth(190);
                message.setWrapText(false);
                VBox details = new VBox(4, name, message);
                HBox row = new HBox(10, new AvatarLabel(contact), details);
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
            }
        });
        VBox.setVgrow(list, Priority.ALWAYS);
        conversations.getChildren().addAll(heading, search, list);
        VBox welcome = new VBox(12);
        welcome.setAlignment(Pos.CENTER);
        welcome.setPadding(new Insets(32));
        Label logo = new AvatarLabel(MockData.CURRENT_USER);
        Label title = new Label("Hello, " + MockData.CURRENT_USER.getFullName());
        title.getStyleClass().add("login-title");
        Label hint = new Label("Choose a conversation to start chatting.");
        hint.getStyleClass().add("login-subtitle");
        StatusBadge status = StatusBadge.online(MockData.CURRENT_USER.isOnline());
        welcome.getChildren().addAll(logo, title, hint, status);
        frame.setRight(null);
        frame.setCenter(new HBox(conversations, welcome));
        HBox.setHgrow(welcome, Priority.ALWAYS);
        setCenter(frame);
    }
}
