package admin.ui;

import common.mock.MockData;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.DateRangePicker;
import common.ui.component.OperatorFilter;
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
 * Screen #14 (A-15): Active Users screen.
 * Tracks engagement across an adjustable date window (app opens, people chatted with, groups chatted in),
 * with name filter, sort, and operator filter (=, <, >) on activity metrics.
 */
public class ActiveUsersView extends VBox {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, HH:mm");

    private final ObservableList<MockData.UserActivity> masterActivities = FXCollections.observableArrayList();
    private final FilteredList<MockData.UserActivity> filteredActivities = new FilteredList<>(masterActivities, p -> true);
    private final SortedList<MockData.UserActivity> sortedActivities = new SortedList<>(filteredActivities);
    private final TableView<MockData.UserActivity> table = new TableView<>(sortedActivities);

    private final DateRangePicker datePicker = new DateRangePicker();
    private final TextField searchField = new TextField();
    private final ComboBox<String> metricCombo = new ComboBox<>();
    private final OperatorFilter operatorFilter = new OperatorFilter();
    private final ComboBox<String> sortCombo = new ComboBox<>();
    private final Label summaryLabel = new Label();

    public ActiveUsersView() {
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("admin-content");

        buildHeader();
        buildFilterBar();
        buildTable();

        datePicker.setRange(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 18));
        reloadActivities();
        sortedActivities.comparatorProperty().bind(table.comparatorProperty());
    }

    private void buildHeader() {
        VBox titleBlock = new VBox(4);
        Label heading = new Label("Active Users & Engagement");
        heading.getStyleClass().add("admin-heading");
        Label sub = new Label("Quantify user engagement within date windows: session launches, direct peers, and group participations.");
        sub.getStyleClass().add("text-muted");
        titleBlock.getChildren().addAll(heading, sub);
        getChildren().add(titleBlock);
    }

    private void buildFilterBar() {
        HBox row1 = new HBox(12);
        row1.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Filter by name or username...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(240);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        datePicker.getFromDatePicker().valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        datePicker.getToDatePicker().valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        sortCombo.setItems(FXCollections.observableArrayList(
                "App Opens (High → Low)",
                "People Chatted (High → Low)",
                "Groups Chatted (High → Low)",
                "Name (A → Z)"
        ));
        sortCombo.setValue("App Opens (High → Low)");
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySort(newVal));

        row1.getChildren().addAll(datePicker, searchField, spacer, sortCombo);

        HBox row2 = new HBox(10);
        row2.setAlignment(Pos.CENTER_LEFT);

        Label metricLbl = new Label("Filter metric:");
        metricLbl.getStyleClass().add("text-muted");

        metricCombo.setItems(FXCollections.observableArrayList("App Opens", "People Chatted", "Groups Chatted"));
        metricCombo.setValue("App Opens");
        metricCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        for (javafx.scene.Node node : operatorFilter.getChildren()) {
            if (node instanceof TextField tf) {
                tf.textProperty().addListener((o, ov, nv) -> applyFilters());
            } else if (node instanceof ComboBox<?> cb) {
                cb.valueProperty().addListener((o, ov, nv) -> applyFilters());
            }
        }

        Button clearFilterBtn = new Button("Clear Filters");
        clearFilterBtn.getStyleClass().add("btn-ghost");
        clearFilterBtn.setOnAction(e -> {
            searchField.clear();
            operatorFilter.setValue(null);
            datePicker.setRange(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 18));
            applyFilters();
        });

        row2.getChildren().addAll(metricLbl, metricCombo, operatorFilter, clearFilterBtn);

        getChildren().addAll(row1, row2);
    }

    private void buildTable() {
        table.getStyleClass().add("table-view-clean");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<MockData.UserActivity, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getUser().getId()));
        idCol.setPrefWidth(60);

        TableColumn<MockData.UserActivity, MockData.UserActivity> nameCol = new TableColumn<>("User");
        nameCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(MockData.UserActivity item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    User u = item.getUser();
                    AvatarLabel av = new AvatarLabel(u.getInitials(), false, true);
                    Label nameLbl = new Label(u.getFullName());
                    nameLbl.getStyleClass().add("text-semibold");
                    HBox box = new HBox(8, av, nameLbl);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });
        nameCol.setPrefWidth(220);

        TableColumn<MockData.UserActivity, String> unameCol = new TableColumn<>("Username");
        unameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper("@" + c.getValue().getUser().getUsername()));
        unameCol.setPrefWidth(140);

        TableColumn<MockData.UserActivity, Number> opensCol = new TableColumn<>("App Opens");
        opensCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAppOpens()));
        opensCol.setPrefWidth(110);

        TableColumn<MockData.UserActivity, Number> peopleCol = new TableColumn<>("People Chatted");
        peopleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPeopleChattedWith()));
        peopleCol.setPrefWidth(130);

        TableColumn<MockData.UserActivity, Number> groupsCol = new TableColumn<>("Groups Chatted");
        groupsCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getGroupsChattedIn()));
        groupsCol.setPrefWidth(130);

        TableColumn<MockData.UserActivity, String> activeCol = new TableColumn<>("Last Active");
        activeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getLastActive() != null ? c.getValue().getLastActive().format(DATE_FMT) : "—"));
        activeCol.setPrefWidth(120);

        table.getColumns().addAll(idCol, nameCol, unameCol, opensCol, peopleCol, groupsCol, activeCol);

        summaryLabel.getStyleClass().add("text-muted");
        getChildren().addAll(table, summaryLabel);
    }

    private void reloadActivities() {
        masterActivities.setAll(MockData.getActiveUserData(datePicker.getFromDate(), datePicker.getToDate()));
        applyFilters();
    }

    private void applyFilters() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        Integer filterVal = operatorFilter.getNumericValue();
        String op = operatorFilter.getOperator();
        String metric = metricCombo.getValue();

        filteredActivities.setPredicate(act -> {
            User u = act.getUser();
            boolean matchQuery = q.isEmpty()
                    || u.getFullName().toLowerCase().contains(q)
                    || u.getUsername().toLowerCase().contains(q);

            boolean matchOp = true;
            if (filterVal != null) {
                int val = switch (metric != null ? metric : "App Opens") {
                    case "People Chatted" -> act.getPeopleChattedWith();
                    case "Groups Chatted" -> act.getGroupsChattedIn();
                    default -> act.getAppOpens();
                };
                if ("<".equals(op)) matchOp = val < filterVal;
                else if (">".equals(op)) matchOp = val > filterVal;
                else matchOp = val == filterVal;
            }

            return matchQuery && matchOp;
        });

        summaryLabel.setText("Showing " + filteredActivities.size() + " of " + masterActivities.size() + " active users matching criteria");
    }

    private void applySort(String criteria) {
        if (criteria == null) return;
        switch (criteria) {
            case "App Opens (High → Low)" -> masterActivities.sort((a, b) -> Integer.compare(b.getAppOpens(), a.getAppOpens()));
            case "People Chatted (High → Low)" -> masterActivities.sort((a, b) -> Integer.compare(b.getPeopleChattedWith(), a.getPeopleChattedWith()));
            case "Groups Chatted (High → Low)" -> masterActivities.sort((a, b) -> Integer.compare(b.getGroupsChattedIn(), a.getGroupsChattedIn()));
            case "Name (A → Z)" -> masterActivities.sort(Comparator.comparing(a -> a.getUser().getFullName(), String.CASE_INSENSITIVE_ORDER));
        }
    }
}

