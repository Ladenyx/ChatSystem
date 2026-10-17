package admin.ui;

import common.mock.MockData;
import common.model.LoginRecord;
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
 * Screen #7 (A-07): System Login History screen.
 * Chronological table of all system logins with time, username, full name, and user ID.
 */
public class SystemLoginHistoryView extends VBox {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObservableList<LoginRecord> masterRecords = FXCollections.observableArrayList();
    private final FilteredList<LoginRecord> filteredRecords = new FilteredList<>(masterRecords, p -> true);
    private final SortedList<LoginRecord> sortedRecords = new SortedList<>(filteredRecords);
    private final TableView<LoginRecord> table = new TableView<>(sortedRecords);

    private final TextField searchField = new TextField();
    private final Label summaryLabel = new Label();

    public SystemLoginHistoryView() {
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("admin-content");

        buildHeader();
        buildFilterBar();
        buildTable();

        reloadRecords();
        sortedRecords.comparatorProperty().bind(table.comparatorProperty());
    }

    private void buildHeader() {
        VBox titleBlock = new VBox(4);
        Label heading = new Label("System Login History");
        heading.getStyleClass().add("admin-heading");
        Label sub = new Label("Chronological audit log of all successful logins and sessions across the chat system.");
        sub.getStyleClass().add("text-muted");
        titleBlock.getChildren().addAll(heading, sub);
        getChildren().add(titleBlock);
    }

    private void buildFilterBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Filter by username or full name...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh Log");
        refreshBtn.getStyleClass().add("btn-ghost");
        refreshBtn.setOnAction(e -> reloadRecords());

        bar.getChildren().addAll(searchField, spacer, refreshBtn);
        getChildren().add(bar);
    }

    private void buildTable() {
        table.getStyleClass().add("table-view-clean");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ID column
        TableColumn<LoginRecord, Number> idCol = new TableColumn<>("Log ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        idCol.setPrefWidth(80);

        // Login Time column (chronological default)
        TableColumn<LoginRecord, String> timeCol = new TableColumn<>("Login Time");
        timeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getLoginAt() != null ? c.getValue().getLoginAt().format(TIME_FMT) : "—"));
        timeCol.setPrefWidth(190);

        // User ID column
        TableColumn<LoginRecord, Number> uidCol = new TableColumn<>("User ID");
        uidCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getUserId()));
        uidCol.setPrefWidth(80);

        // Username column
        TableColumn<LoginRecord, String> unameCol = new TableColumn<>("Username");
        unameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper("@" + c.getValue().getUsername()));
        unameCol.setPrefWidth(150);

        // Full Name column
        TableColumn<LoginRecord, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getFullName()));
        nameCol.setPrefWidth(220);

        // Result Status column
        TableColumn<LoginRecord, String> statusCol = new TableColumn<>("Auth Result");
        statusCol.setCellValueFactory(c -> new ReadOnlyStringWrapper("SUCCESS"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label("SUCCESS");
                    badge.getStyleClass().add("badge-active");
                    setGraphic(badge);
                }
            }
        });
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(idCol, timeCol, uidCol, unameCol, nameCol, statusCol);

        summaryLabel.getStyleClass().add("text-muted");
        getChildren().addAll(table, summaryLabel);
    }

    private void reloadRecords() {
        // Chronological order: newest logins first
        masterRecords.setAll(MockData.LOGIN_RECORDS.stream()
                .sorted(Comparator.comparing(LoginRecord::getLoginAt).reversed())
                .toList());
        applyFilter(searchField.getText());
    }

    private void applyFilter(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        filteredRecords.setPredicate(rec -> {
            if (q.isEmpty()) return true;
            return (rec.getUsername() != null && rec.getUsername().toLowerCase().contains(q))
                    || (rec.getFullName() != null && rec.getFullName().toLowerCase().contains(q))
                    || String.valueOf(rec.getUserId()).equals(q);
        });
        summaryLabel.setText("Showing " + filteredRecords.size() + " of " + masterRecords.size() + " total login records");
    }
}

