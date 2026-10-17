package user.ui;

import common.mock.MockData;
import common.model.FriendRequest;
import common.ui.component.AvatarLabel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

/**
 * Screen #6: Friend Requests (Roadmap: U-08).
 * Features:
 * - Real-time filtering by sender name or username.
 * - Accept request (adds to friends list, removes from pending).
 * - Decline request (dismisses request).
 */
public class FriendRequestsView extends BorderPane {

    private final ObservableList<FriendRequest> masterRequests = FXCollections.observableArrayList();
    private final FilteredList<FriendRequest> filteredRequests;
    private final ListView<FriendRequest> listView = new ListView<>();
    private final Label countBadge = new Label();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private final Runnable onRequestChanged;

    public FriendRequestsView(Runnable onRequestChanged) {
        this.onRequestChanged = onRequestChanged;
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));

        // Load friend requests
        masterRequests.setAll(MockData.FRIEND_REQUESTS);
        filteredRequests = new FilteredList<>(masterRequests, p -> p.getStatus() == FriendRequest.Status.PENDING);

        // Header Section
        VBox header = createHeader();
        setTop(header);

        // Center List Section
        buildListView();
        VBox centerBox = new VBox(listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        setCenter(centerBox);

        updateCount();
    }

    private VBox createHeader() {
        Label title = new Label("Friend Requests");
        title.getStyleClass().add("section-title");

        countBadge.getStyleClass().add("badge-unread");

        HBox titleRow = new HBox(12, title, countBadge);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Review incoming friend requests from people who want to connect with you.");
        subtitle.getStyleClass().add("login-subtitle");

        TextField searchField = new TextField();
        searchField.setPromptText("Filter requests by sender name or @username...");
        searchField.getStyleClass().add("search-field");
        searchField.setMaxWidth(380);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = (newVal == null) ? "" : newVal.trim().toLowerCase();
            filteredRequests.setPredicate(req -> {
                if (req.getStatus() != FriendRequest.Status.PENDING) return false;
                if (query.isEmpty()) return true;
                return req.getSender().getFullName().toLowerCase().contains(query)
                        || req.getSender().getUsername().toLowerCase().contains(query);
            });
            updateCount();
        });

        VBox header = new VBox(8, titleRow, subtitle, searchField);
        header.setPadding(new Insets(0, 0, 16, 0));
        return header;
    }

    private void buildListView() {
        listView.getStyleClass().add("list-view-clean");
        listView.setItems(filteredRequests);
        listView.setPlaceholder(new Label("No pending friend requests."));

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(FriendRequest req, boolean empty) {
                super.updateItem(req, empty);
                if (empty || req == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // Left: Sender Avatar
                AvatarLabel avatar = new AvatarLabel(req.getSender());

                // Middle: Request Details
                Label nameLbl = new Label(req.getSender().getFullName());
                nameLbl.getStyleClass().add("text-name");

                Label usernameLbl = new Label("@" + req.getSender().getUsername());
                usernameLbl.getStyleClass().add("text-muted");

                Label pendingBadge = new Label("Pending");
                pendingBadge.getStyleClass().add("badge-locked"); // Amber/warn tint for pending

                HBox nameRow = new HBox(8, nameLbl, usernameLbl, pendingBadge);
                nameRow.setAlignment(Pos.CENTER_LEFT);

                String formattedTime = req.getSentAt() != null ? req.getSentAt().format(TIME_FORMATTER) : "Recently";
                Label timeLbl = new Label("Requested on: " + formattedTime + " • " + req.getSender().getEmail());
                timeLbl.getStyleClass().add("text-small");
                timeLbl.setStyle("-fx-text-fill: #8a8a95;");

                VBox infoBox = new VBox(4, nameRow, timeLbl);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                // Right: Accept & Decline Actions
                Button acceptBtn = new Button("Accept");
                acceptBtn.getStyleClass().add("btn-accent");
                acceptBtn.setOnAction(e -> handleAccept(req));

                Button declineBtn = new Button("Decline");
                declineBtn.getStyleClass().add("btn-ghost");
                declineBtn.setOnAction(e -> handleDecline(req));

                HBox actions = new HBox(8, acceptBtn, declineBtn);
                actions.setAlignment(Pos.CENTER_RIGHT);

                // Row Card
                HBox row = new HBox(16, avatar, infoBox, actions);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("card");
                row.setPadding(new Insets(12, 16, 12, 16));
                HBox.setHgrow(row, Priority.ALWAYS);

                setGraphic(row);
                setText(null);
            }
        });
    }

    private void handleAccept(FriendRequest req) {
        MockData.acceptFriendRequest(req);
        refreshList();
        if (onRequestChanged != null) onRequestChanged.run();
    }

    private void handleDecline(FriendRequest req) {
        MockData.declineFriendRequest(req);
        refreshList();
        if (onRequestChanged != null) onRequestChanged.run();
    }

    public void refreshList() {
        masterRequests.setAll(MockData.FRIEND_REQUESTS);
        updateCount();
    }

    private void updateCount() {
        long count = filteredRequests.size();
        countBadge.setText(count + " pending");
        countBadge.setVisible(count > 0);
    }
}

