package admin.ui;

import common.mock.MockData;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.OperatorFilter;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Screen #13 (A-14): Users and Friend Counts screen.
 * Displays direct-friends and friends-of-friends (FoF) network reach, with name filter,
 * sorting, and `=`, `<`, `>` operator filter on direct friends count.
 */
public class FriendCountsView extends VBox {

    public static class FriendStat {
        private final User user;
        private final int directFriends;
        private final int friendsOfFriends;

        public FriendStat(User user, int directFriends, int friendsOfFriends) {
            this.user = user;
            this.directFriends = directFriends;
            this.friendsOfFriends = friendsOfFriends;
        }

        public User getUser() { return user; }
        public int getDirectFriends() { return directFriends; }
        public int getFriendsOfFriends() { return friendsOfFriends; }
        public int getTotalReach() { return directFriends + friendsOfFriends; }
    }

    private final ObservableList<FriendStat> masterStats = FXCollections.observableArrayList();
    private final FilteredList<FriendStat> filteredStats = new FilteredList<>(masterStats, p -> true);
    private final SortedList<FriendStat> sortedStats = new SortedList<>(filteredStats);
    private final TableView<FriendStat> table = new TableView<>(sortedStats);

    private final TextField searchField = new TextField();
    private final OperatorFilter operatorFilter = new OperatorFilter();
    private final ComboBox<String> sortCombo = new ComboBox<>();
    private final Label summaryLabel = new Label();

    public FriendCountsView() {
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("admin-content");

        buildHeader();
        buildFilterBar();
        buildTable();

        reloadStats();
        sortedStats.comparatorProperty().bind(table.comparatorProperty());
    }

    private void buildHeader() {
        VBox titleBlock = new VBox(4);
        Label heading = new Label("Users & Friend Network Counts");
        heading.getStyleClass().add("admin-heading");
        Label sub = new Label("Inspect social connectivity: direct mutual friends and second-degree friends-of-friends (FoF).");
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

        Label opLabel = new Label("Direct Friends:");
        opLabel.getStyleClass().add("text-muted");
        HBox opBox = new HBox(6, opLabel, operatorFilter);
        opBox.setAlignment(Pos.CENTER_LEFT);

        operatorFilter.getValueText(); // ensure initialized
        // Listen to operator filter changes
        // OperatorFilter has value and operator
        // We can inspect changes via text listener on OperatorFilter's text field or polling
        // Let's add listener to operatorFilter children if needed or by exposing textProperty
        // Looking at OperatorFilter: operator and value are children.
        for (javafx.scene.Node node : operatorFilter.getChildren()) {
            if (node instanceof TextField tf) {
                tf.textProperty().addListener((o, ov, nv) -> applyFilters());
            } else if (node instanceof ComboBox<?> cb) {
                cb.valueProperty().addListener((o, ov, nv) -> applyFilters());
            }
        }

        sortCombo.setItems(FXCollections.observableArrayList(
                "Sort: Default",
                "Direct Friends (High → Low)",
                "Friends-of-Friends (High → Low)",
                "Total Reach (High → Low)",
                "Name (A → Z)"
        ));
        sortCombo.setValue("Sort: Default");
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySort(newVal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button resetBtn = new Button("Reset Filter");
        resetBtn.getStyleClass().add("btn-ghost");
        resetBtn.setOnAction(e -> {
            searchField.clear();
            operatorFilter.setValue(null);
            applyFilters();
        });

        bar.getChildren().addAll(searchField, opBox, sortCombo, spacer, resetBtn);
        getChildren().add(bar);
    }

    private void buildTable() {
        table.getStyleClass().add("table-view-clean");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<FriendStat, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getUser().getId()));
        idCol.setPrefWidth(60);

        TableColumn<FriendStat, FriendStat> nameCol = new TableColumn<>("User");
        nameCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(FriendStat item, boolean empty) {
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

        TableColumn<FriendStat, String> unameCol = new TableColumn<>("Username");
        unameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper("@" + c.getValue().getUser().getUsername()));
        unameCol.setPrefWidth(140);

        TableColumn<FriendStat, Number> directCol = new TableColumn<>("Direct Friends");
        directCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getDirectFriends()));
        directCol.setPrefWidth(120);

        TableColumn<FriendStat, Number> fofCol = new TableColumn<>("Friends of Friends (FoF)");
        fofCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getFriendsOfFriends()));
        fofCol.setPrefWidth(160);

        TableColumn<FriendStat, Number> reachCol = new TableColumn<>("Total Reach");
        reachCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTotalReach()));
        reachCol.setPrefWidth(110);

        TableColumn<FriendStat, User> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getUser()));
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
        statusCol.setPrefWidth(90);

        table.getColumns().addAll(idCol, nameCol, unameCol, directCol, fofCol, reachCol, statusCol);

        summaryLabel.getStyleClass().add("text-muted");
        getChildren().addAll(table, summaryLabel);
    }

    private void reloadStats() {
        List<FriendStat> list = new ArrayList<>();
        for (User u : MockData.USERS) {
            if (u.getId() == 0) continue;
            int[] counts = MockData.getFriendCounts(u.getId());
            list.add(new FriendStat(u, counts[0], counts[1]));
        }
        masterStats.setAll(list);
        applyFilters();
    }

    private void applyFilters() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        Integer filterVal = operatorFilter.getNumericValue();
        String op = operatorFilter.getOperator();

        filteredStats.setPredicate(stat -> {
            User u = stat.getUser();
            boolean matchQuery = q.isEmpty()
                    || u.getFullName().toLowerCase().contains(q)
                    || u.getUsername().toLowerCase().contains(q);

            boolean matchOp = true;
            if (filterVal != null) {
                int count = stat.getDirectFriends();
                if ("<".equals(op)) matchOp = count < filterVal;
                else if (">".equals(op)) matchOp = count > filterVal;
                else matchOp = count == filterVal;
            }

            return matchQuery && matchOp;
        });

        summaryLabel.setText("Showing " + filteredStats.size() + " of " + masterStats.size() + " users matching criteria");
    }

    private void applySort(String criteria) {
        if (criteria == null) return;
        switch (criteria) {
            case "Direct Friends (High → Low)" -> masterStats.sort((a, b) -> Integer.compare(b.getDirectFriends(), a.getDirectFriends()));
            case "Friends-of-Friends (High → Low)" -> masterStats.sort((a, b) -> Integer.compare(b.getFriendsOfFriends(), a.getFriendsOfFriends()));
            case "Total Reach (High → Low)" -> masterStats.sort((a, b) -> Integer.compare(b.getTotalReach(), a.getTotalReach()));
            case "Name (A → Z)" -> masterStats.sort(Comparator.comparing(s -> s.getUser().getFullName(), String.CASE_INSENSITIVE_ORDER));
            default -> masterStats.sort(Comparator.comparingInt(s -> s.getUser().getId()));
        }
    }
}

