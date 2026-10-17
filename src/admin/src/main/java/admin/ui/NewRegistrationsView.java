package admin.ui;

import common.mock.MockData;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.DateRangePicker;
import common.ui.component.StatusBadge;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

/**
 * Screen #11 (A-12): New Registrations screen.
 * Displays accounts created within an adjustable date range, with name filter and sorting by name or creation time.
 */
public class NewRegistrationsView extends VBox {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    private final ObservableList<User> masterUsers = FXCollections.observableArrayList();
    private final FilteredList<User> filteredUsers = new FilteredList<>(masterUsers, p -> true);
    private final SortedList<User> sortedUsers = new SortedList<>(filteredUsers);
    private final TableView<User> table = new TableView<>(sortedUsers);

    private final DateRangePicker datePicker = new DateRangePicker();
    private final TextField searchField = new TextField();
    private final ComboBox<String> sortCombo = new ComboBox<>();
    private final Label summaryLabel = new Label();

    public NewRegistrationsView() {
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("admin-content");

        buildHeader();
        buildFilterBar();
        buildTable();

        // Default range: covering full platform lifecycle (e.g. 2025-01-01 to today)
        datePicker.setRange(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 10, 18));
        reloadUsers();
        sortedUsers.comparatorProperty().bind(table.comparatorProperty());
    }

    private void buildHeader() {
        VBox titleBlock = new VBox(4);
        Label heading = new Label("New Registrations");
        heading.getStyleClass().add("admin-heading");
        Label sub = new Label("Track newly registered accounts across custom date windows with search and sorting.");
        sub.getStyleClass().add("text-muted");
        titleBlock.getChildren().addAll(heading, sub);
        getChildren().add(titleBlock);
    }

    private void buildFilterBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Filter by name or username...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(240);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        datePicker.getFromDatePicker().valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        datePicker.getToDatePicker().valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        sortCombo.setItems(FXCollections.observableArrayList(
                "Registered: Newest",
                "Registered: Oldest",
                "Name (A → Z)",
                "Name (Z → A)"
        ));
        sortCombo.setValue("Registered: Newest");
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySort(newVal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button resetBtn = new Button("Reset Range");
        resetBtn.getStyleClass().add("btn-ghost");
        resetBtn.setOnAction(e -> {
            datePicker.setRange(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 10, 18));
            searchField.clear();
            applyFilters();
        });

        bar.getChildren().addAll(datePicker, searchField, sortCombo, spacer, resetBtn);
        getChildren().add(bar);
    }

    private void buildTable() {
        table.getStyleClass().add("table-view-clean");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<User, Number> idCol = new TableColumn<>("User ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        idCol.setPrefWidth(75);

        TableColumn<User, User> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    AvatarLabel av = new AvatarLabel(item.getInitials(), false, true);
                    Label nameLbl = new Label(item.getFullName());
                    nameLbl.getStyleClass().add("text-semibold");
                    HBox box = new HBox(8, av, nameLbl);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });
        nameCol.setPrefWidth(220);

        TableColumn<User, String> unameCol = new TableColumn<>("Username");
        unameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper("@" + c.getValue().getUsername()));
        unameCol.setPrefWidth(140);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEmail()));
        emailCol.setPrefWidth(180);

        TableColumn<User, String> createdCol = new TableColumn<>("Registration Date");
        createdCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().format(DATE_FMT) : "—"));
        createdCol.setPrefWidth(170);

        TableColumn<User, User> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(StatusBadge.accountStatus(item));
                }
            }
        });
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(idCol, nameCol, unameCol, emailCol, createdCol, statusCol);

        summaryLabel.getStyleClass().add("text-muted");
        getChildren().addAll(table, summaryLabel);
    }

    private void reloadUsers() {
        masterUsers.setAll(MockData.USERS.stream().filter(u -> u.getId() != 0).toList());
        applyFilters();
    }

    private void applyFilters() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        LocalDate from = datePicker.getFromDate();
        LocalDate to = datePicker.getToDate();

        filteredUsers.setPredicate(u -> {
            if (u.getId() == 0) return false;
            boolean matchQuery = q.isEmpty()
                    || u.getFullName().toLowerCase().contains(q)
                    || u.getUsername().toLowerCase().contains(q)
                    || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q));

            boolean matchDate = true;
            if (u.getCreatedAt() != null) {
                LocalDate createdDate = u.getCreatedAt().toLocalDate();
                if (from != null && createdDate.isBefore(from)) matchDate = false;
                if (to != null && createdDate.isAfter(to)) matchDate = false;
            }

            return matchQuery && matchDate;
        });

        summaryLabel.setText("Total new registrations in selected period: " + filteredUsers.size() + " accounts");
    }

    private void applySort(String criteria) {
        if (criteria == null) return;
        switch (criteria) {
            case "Registered: Newest" -> masterUsers.sort((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
            case "Registered: Oldest" -> masterUsers.sort((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return a.getCreatedAt().compareTo(b.getCreatedAt());
            });
            case "Name (A → Z)" -> masterUsers.sort(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER));
            case "Name (Z → A)" -> masterUsers.sort(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER).reversed());
        }
    }
}

