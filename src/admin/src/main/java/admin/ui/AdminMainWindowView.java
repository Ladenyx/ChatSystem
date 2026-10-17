package admin.ui;

import common.mock.MockData;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.NavSidebar;
import common.ui.component.StatusBadge;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

/**
 * Screen #2: Admin Console Main Window shell.
 * Hosts the navigation rail and links to all administration modules (Screens #3 to #15).
 */
public class AdminMainWindowView extends BorderPane {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final BorderPane shell = new BorderPane();
    private NavSidebar sidebar;

    public AdminMainWindowView(Runnable onLogout) {
        getStyleClass().add("main-bg");
        shell.getStyleClass().add("content-area");

        sidebar = new NavSidebar(section -> switchSection(section),
                "Overview",
                "Users",
                "Logins",
                "Groups",
                "Spam",
                "Registrations",
                "Reg Chart",
                "Friend Counts",
                "Active Users",
                "Activity Chart"
        );

        sidebar.select("Overview");
        sidebar.addBottomAction("Log out", onLogout);

        shell.setLeft(sidebar);
        shell.setCenter(createOverviewPane());
        setCenter(shell);
    }

    private void switchSection(String section) {
        switch (section) {
            case "Overview" -> shell.setCenter(createOverviewPane());
            case "Users" -> shell.setCenter(new UserManagementView());
            case "Logins" -> shell.setCenter(new SystemLoginHistoryView());
            case "Groups" -> shell.setCenter(new GroupListView());
            case "Spam" -> shell.setCenter(new SpamReportsView());
            case "Registrations" -> shell.setCenter(new NewRegistrationsView());
            case "Reg Chart" -> shell.setCenter(new RegistrationsChartView());
            case "Friend Counts" -> shell.setCenter(new FriendCountsView());
            case "Active Users" -> shell.setCenter(new ActiveUsersView());
            case "Activity Chart" -> shell.setCenter(new ActiveUsersChartView());
            default -> shell.setCenter(createOverviewPane());
        }
    }

    private void navigateTo(String section) {
        sidebar.select(section);
        switchSection(section);
    }

    private VBox createOverviewPane() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.getStyleClass().add("admin-content");

        // Heading
        HBox heading = new HBox(12);
        heading.setAlignment(Pos.CENTER_LEFT);
        VBox titleBlock = new VBox(4);
        Label headingTitle = new Label("Administration Overview");
        headingTitle.getStyleClass().add("admin-heading");
        Label headingSub = new Label("System health summary, live connectivity metrics, and recent administrative logs.");
        headingSub.getStyleClass().add("text-muted");
        titleBlock.getChildren().addAll(headingTitle, headingSub);
        HBox.setHgrow(titleBlock, Priority.ALWAYS);
        heading.getChildren().add(titleBlock);

        // KPI Summary Cards
        GridPane summaries = new GridPane();
        summaries.setHgap(16);
        summaries.setVgap(16);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(25.0);
            summaries.getColumnConstraints().add(column);
        }

        long totalUsers = MockData.USERS.stream().filter(u -> u.getId() != 0).count();
        long onlineUsers = MockData.USERS.stream().filter(u -> u.getId() != 0 && u.isOnline()).count();
        long lockedUsers = MockData.USERS.stream().filter(u -> u.getId() != 0 && u.getStatus() == User.Status.LOCKED).count();
        int totalGroups = MockData.GROUPS.size();

        summaries.add(summaryCard("Total Users", String.valueOf(totalUsers), "Registered accounts", () -> navigateTo("Users")), 0, 0);
        summaries.add(summaryCard("Online Now", String.valueOf(onlineUsers), "Active chat sessions", () -> navigateTo("Active Users")), 1, 0);
        summaries.add(summaryCard("Locked Accounts", String.valueOf(lockedUsers), "Security flags pending", () -> navigateTo("Spam")), 2, 0);
        summaries.add(summaryCard("Chat Groups", String.valueOf(totalGroups), "Active collaboration channels", () -> navigateTo("Groups")), 3, 0);

        // Quick Navigation Buttons Bar
        HBox quickBar = new HBox(10);
        quickBar.setAlignment(Pos.CENTER_LEFT);

        Label quickLbl = new Label("Quick actions:");
        quickLbl.getStyleClass().add("text-semibold");

        Button btnUsers = new Button("Manage Users");
        btnUsers.getStyleClass().add("btn-accent");
        btnUsers.setOnAction(e -> navigateTo("Users"));

        Button btnSpam = new Button("Review Spam (" + MockData.SPAM_REPORTS.stream().filter(r -> r.getStatus() == common.model.SpamReport.Status.PENDING).count() + ")");
        btnSpam.getStyleClass().add("btn-warn");
        btnSpam.setOnAction(e -> navigateTo("Spam"));

        Button btnRegChart = new Button("Signups Chart");
        btnRegChart.getStyleClass().add("btn-ghost");
        btnRegChart.setOnAction(e -> navigateTo("Reg Chart"));

        Button btnMauChart = new Button("MAU Trend");
        btnMauChart.getStyleClass().add("btn-ghost");
        btnMauChart.setOnAction(e -> navigateTo("Activity Chart"));

        quickBar.getChildren().addAll(quickLbl, btnUsers, btnSpam, btnRegChart, btnMauChart);

        // Recent Users Table Panel
        VBox usersPanel = new VBox(12);
        usersPanel.getStyleClass().add("admin-panel");
        HBox tableHeading = new HBox(12);
        tableHeading.setAlignment(Pos.CENTER_LEFT);
        Label usersTitle = new Label("Recent Registered Accounts");
        usersTitle.getStyleClass().add("text-name");

        TextField search = new TextField();
        search.setPromptText("Search name or username...");
        search.getStyleClass().add("search-field");
        search.setPrefWidth(260);

        Region tableSpacer = new Region();
        HBox.setHgrow(tableSpacer, Priority.ALWAYS);

        tableHeading.getChildren().addAll(usersTitle, tableSpacer, search);

        FilteredList<User> filteredUsers = new FilteredList<>(FXCollections.observableArrayList(MockData.USERS), user -> user.getId() != 0);
        TableView<User> table = new TableView<>(filteredUsers);
        table.getStyleClass().add("table-view-clean");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<User, User> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
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
        nameCol.setPrefWidth(200);

        TableColumn<User, String> unameCol = new TableColumn<>("Username");
        unameCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper("@" + cell.getValue().getUsername()));
        unameCol.setPrefWidth(130);

        TableColumn<User, User> statusCol = new TableColumn<>("Account Status");
        statusCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
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
        statusCol.setPrefWidth(120);

        TableColumn<User, User> onlineCol = new TableColumn<>("Presence");
        onlineCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        onlineCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(StatusBadge.onlineStatus(item));
                }
            }
        });
        onlineCol.setPrefWidth(100);

        TableColumn<User, String> createdCol = new TableColumn<>("Created");
        createdCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getCreatedAt() == null ? "—" : cell.getValue().getCreatedAt().format(DATE_FORMAT)));
        createdCol.setPrefWidth(130);

        table.getColumns().addAll(nameCol, unameCol, statusCol, onlineCol, createdCol);

        search.textProperty().addListener((obs, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase();
            filteredUsers.setPredicate(user -> user.getId() != 0 && (user.getFullName().toLowerCase().contains(query)
                    || user.getUsername().toLowerCase().contains(query)));
        });

        usersPanel.getChildren().addAll(tableHeading, table);
        VBox.setVgrow(usersPanel, Priority.ALWAYS);

        content.getChildren().addAll(heading, summaries, quickBar, usersPanel);
        return content;
    }

    private static VBox summaryCard(String label, String value, String note, Runnable onClick) {
        VBox card = new VBox(6);
        card.getStyleClass().add("admin-summary-card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setCursor(javafx.scene.Cursor.HAND);

        Label topLbl = new Label(label);
        topLbl.getStyleClass().add("text-muted");

        Label valLbl = new Label(value);
        valLbl.getStyleClass().add("admin-heading");

        Label subLbl = new Label(note);
        subLbl.getStyleClass().add("text-muted");

        card.getChildren().addAll(topLbl, valLbl, subLbl);
        if (onClick != null) {
            card.setOnMouseClicked(e -> onClick.run());
        }
        return card;
    }
}
