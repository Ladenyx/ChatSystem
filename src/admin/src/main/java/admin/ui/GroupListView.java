package admin.ui;

import common.mock.MockData;
import common.model.ChatGroup;
import common.model.User;
import common.ui.component.AvatarLabel;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;

/**
 * Screen #8 (A-08): Chat Group List screen.
 * Allows filtering by group name, sorting by name or creation time, and viewing group members & admins (A-09).
 */
public class GroupListView extends VBox {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    private final ObservableList<ChatGroup> masterGroups = FXCollections.observableArrayList();
    private final FilteredList<ChatGroup> filteredGroups = new FilteredList<>(masterGroups, p -> true);
    private final SortedList<ChatGroup> sortedGroups = new SortedList<>(filteredGroups);
    private final TableView<ChatGroup> table = new TableView<>(sortedGroups);

    private final TextField searchField = new TextField();
    private final ComboBox<String> sortCombo = new ComboBox<>();
    private final Button viewMembersBtn = new Button("View Members & Admins");

    public GroupListView() {
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("admin-content");

        buildHeader();
        buildFilterBar();
        buildTable();
        buildActionBar();

        reloadGroups();
        sortedGroups.comparatorProperty().bind(table.comparatorProperty());
    }

    private void buildHeader() {
        VBox titleBlock = new VBox(4);
        Label heading = new Label("Chat Groups");
        heading.getStyleClass().add("admin-heading");
        Label sub = new Label("Monitor group conversations, manage participant roles, and inspect encryption status.");
        sub.getStyleClass().add("text-muted");
        titleBlock.getChildren().addAll(heading, sub);
        getChildren().add(titleBlock);
    }

    private void buildFilterBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Filter by group name...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(280);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));

        sortCombo.setItems(FXCollections.observableArrayList(
                "Sort: Default",
                "Name (A → Z)",
                "Name (Z → A)",
                "Created (Newest)",
                "Created (Oldest)"
        ));
        sortCombo.setValue("Sort: Default");
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySort(newVal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-ghost");
        refreshBtn.setOnAction(e -> reloadGroups());

        bar.getChildren().addAll(searchField, sortCombo, spacer, refreshBtn);
        getChildren().add(bar);
    }

    private void buildTable() {
        table.getStyleClass().add("table-view-clean");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ID column
        TableColumn<ChatGroup, Number> idCol = new TableColumn<>("Group ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        idCol.setPrefWidth(80);

        // Group Name column (Avatar + Name)
        TableColumn<ChatGroup, ChatGroup> nameCol = new TableColumn<>("Group Name");
        nameCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ChatGroup item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    AvatarLabel av = new AvatarLabel(item.getInitials(), true, true);
                    Label nameLbl = new Label(item.getName());
                    nameLbl.getStyleClass().add("text-semibold");
                    HBox box = new HBox(8, av, nameLbl);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });
        nameCol.setPrefWidth(220);

        // Creator column
        TableColumn<ChatGroup, String> creatorCol = new TableColumn<>("Creator");
        creatorCol.setCellValueFactory(c -> {
            User creator = MockData.getUserById(c.getValue().getCreatorId());
            return new ReadOnlyStringWrapper(creator != null ? creator.getFullName() : "User #" + c.getValue().getCreatorId());
        });
        creatorCol.setPrefWidth(160);

        // Created At column
        TableColumn<ChatGroup, String> createdCol = new TableColumn<>("Created At");
        createdCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().format(DATE_FMT) : "—"));
        createdCol.setPrefWidth(160);

        // Members Count column
        TableColumn<ChatGroup, Number> membersCol = new TableColumn<>("Members");
        membersCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getMembers().size()));
        membersCol.setPrefWidth(85);

        // Admins Count column
        TableColumn<ChatGroup, Number> adminsCol = new TableColumn<>("Admins");
        adminsCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAdmins().size()));
        adminsCol.setPrefWidth(80);

        // Encryption column
        TableColumn<ChatGroup, ChatGroup> encCol = new TableColumn<>("Security");
        encCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        encCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ChatGroup item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    if (item.isEncrypted()) {
                        Label badge = new Label("🔒 Encrypted");
                        badge.getStyleClass().add("badge-encrypted");
                        setGraphic(badge);
                    } else {
                        Label badge = new Label("Standard");
                        badge.getStyleClass().add("badge-offline");
                        setGraphic(badge);
                    }
                }
            }
        });
        encCol.setPrefWidth(110);

        table.getColumns().addAll(idCol, nameCol, creatorCol, createdCol, membersCol, adminsCol, encCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            viewMembersBtn.setDisable(newSel == null);
        });

        table.setRowFactory(tv -> {
            TableRow<ChatGroup> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewMembers();
                }
            });
            return row;
        });

        getChildren().add(table);
    }

    private void buildActionBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_RIGHT);

        viewMembersBtn.getStyleClass().add("btn-accent");
        viewMembersBtn.setDisable(true);
        viewMembersBtn.setOnAction(e -> handleViewMembers());

        bar.getChildren().add(viewMembersBtn);
        getChildren().add(bar);
    }

    private void reloadGroups() {
        masterGroups.setAll(MockData.GROUPS);
        applyFilter(searchField.getText());
    }

    private void applyFilter(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        filteredGroups.setPredicate(g -> {
            if (q.isEmpty()) return true;
            return g.getName().toLowerCase().contains(q);
        });
    }

    private void applySort(String criteria) {
        if (criteria == null) return;
        switch (criteria) {
            case "Name (A → Z)" -> masterGroups.sort(Comparator.comparing(ChatGroup::getName, String.CASE_INSENSITIVE_ORDER));
            case "Name (Z → A)" -> masterGroups.sort(Comparator.comparing(ChatGroup::getName, String.CASE_INSENSITIVE_ORDER).reversed());
            case "Created (Newest)" -> masterGroups.sort((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
            case "Created (Oldest)" -> masterGroups.sort((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return a.getCreatedAt().compareTo(b.getCreatedAt());
            });
            default -> masterGroups.sort(Comparator.comparingInt(ChatGroup::getId));
        }
    }

    private void handleViewMembers() {
        ChatGroup sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        GroupMembersDialog.showDialog(getScene() != null ? getScene().getWindow() : null, sel);
    }
}

