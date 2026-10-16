package admin.ui;

import common.mock.MockData;
import common.model.User;
import common.ui.component.NavSidebar;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/** Day 1 administration shell with a useful overview and sample user table. */
public class AdminMainWindowView extends BorderPane {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public AdminMainWindowView(Runnable onLogout) {
        getStyleClass().add("main-bg");
        BorderPane shell = new BorderPane();
        shell.getStyleClass().add("content-area");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.getStyleClass().add("admin-content");
        HBox heading = new HBox(12);
        heading.setAlignment(Pos.CENTER_LEFT);
        VBox titleBlock = new VBox(4, title("Administration overview"), muted("Monitor users and recent system activity."));
        HBox.setHgrow(titleBlock, Priority.ALWAYS);
        heading.getChildren().add(titleBlock);

        GridPane summaries = new GridPane();
        summaries.setHgap(16);
        summaries.setVgap(16);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(33.333);
            summaries.getColumnConstraints().add(column);
        }
        summaries.add(summaryCard("Total users", String.valueOf(MockData.USERS.size() - 1), "Registered accounts"), 0, 0);
        summaries.add(summaryCard("Online now", String.valueOf(MockData.USERS.stream().filter(User::isOnline).count()), "Current sample status"), 1, 0);
        summaries.add(summaryCard("Locked accounts", String.valueOf(MockData.USERS.stream().filter(u -> u.getStatus() == User.Status.LOCKED).count()), "Requires review"), 2, 0);
        VBox usersPanel = new VBox(12);
        usersPanel.getStyleClass().add("admin-panel");
        HBox tableHeading = new HBox(12);
        tableHeading.setAlignment(Pos.CENTER_LEFT);
        Label usersTitle = new Label("Recent users");
        usersTitle.getStyleClass().add("text-name");
        TextField search = new TextField();
        search.setPromptText("Search name or username");
        search.getStyleClass().add("search-field");
        search.setMaxWidth(280);
        HBox.setHgrow(usersTitle, Priority.ALWAYS);
        tableHeading.getChildren().addAll(usersTitle, search);

        FilteredList<User> filteredUsers = new FilteredList<>(FXCollections.observableArrayList(MockData.USERS), user -> user.getId() != 0);
        TableView<User> table = new TableView<>(filteredUsers);
        table.getStyleClass().add("table-view-clean");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        TableColumn<User, String> fullName = new TableColumn<>("Full name");
        fullName.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().getFullName()));
        TableColumn<User, String> username = new TableColumn<>("Username");
        username.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().getUsername()));
        TableColumn<User, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().getStatus().name()));
        TableColumn<User, String> created = new TableColumn<>("Created");
        created.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(
                cell.getValue().getCreatedAt() == null ? "—" : cell.getValue().getCreatedAt().format(DATE_FORMAT)));
        table.getColumns().addAll(fullName, username, status, created);
        search.textProperty().addListener((obs, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase();
            filteredUsers.setPredicate(user -> user.getId() != 0 && (user.getFullName().toLowerCase().contains(query)
                    || user.getUsername().toLowerCase().contains(query)));
        });
        usersPanel.getChildren().addAll(tableHeading, table);
        VBox.setVgrow(usersPanel, Priority.ALWAYS);

        content.getChildren().addAll(heading, summaries, usersPanel);
        NavSidebar sidebar = new NavSidebar(section -> {
            if (section.equals("Overview")) shell.setCenter(content);
            else shell.setCenter(sectionPlaceholder(section));
        }, "Overview", "Users", "Login history", "Groups", "Spam reports", "Analytics");
        sidebar.select("Overview");
        sidebar.addBottomAction("Log out", onLogout);
        shell.setLeft(sidebar);
        shell.setCenter(content);
        setCenter(shell);
    }

    private static VBox sectionPlaceholder(String section) {
        VBox page = new VBox(10, title(section), muted("This section is planned for a later UI milestone."));
        page.setPadding(new Insets(28));
        page.getStyleClass().add("admin-content");
        return page;
    }

    private static VBox summaryCard(String label, String value, String note) {
        VBox card = new VBox(7, muted(label), title(value), muted(note));
        card.getStyleClass().add("admin-summary-card");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private static Label title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("admin-heading");
        return label;
    }

    private static Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("text-muted");
        return label;
    }
}
