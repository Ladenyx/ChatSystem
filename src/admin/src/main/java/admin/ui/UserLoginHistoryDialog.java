package admin.ui;

import common.mock.MockData;
import common.model.LoginRecord;
import common.model.User;
import common.ui.component.AvatarLabel;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Screen #5 (A-05): Dialog showing login history for a specific user.
 */
public class UserLoginHistoryDialog extends Dialog<Void> {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserLoginHistoryDialog(User user) {
        setTitle("Login History — " + (user != null ? user.getFullName() : "User"));
        setHeaderText(null);

        DialogPane pane = getDialogPane();
        pane.getStyleClass().add("card");
        pane.getStylesheets().add(getClass().getResource("/common/theme.css").toExternalForm());
        pane.getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(14);
        content.setPadding(new Insets(10));
        content.setPrefWidth(540);
        content.setPrefHeight(420);

        // Header with Avatar and User Information
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        AvatarLabel avatar = new AvatarLabel(user != null ? user.getInitials() : "?", false);
        VBox meta = new VBox(2);
        Label nameLbl = new Label(user != null ? user.getFullName() : "Unknown User");
        nameLbl.getStyleClass().add("section-title");
        Label subLbl = new Label("@" + (user != null ? user.getUsername() : "") + " • Account ID: " + (user != null ? user.getId() : 0));
        subLbl.getStyleClass().add("text-muted");
        meta.getChildren().addAll(nameLbl, subLbl);

        header.getChildren().addAll(avatar, meta);

        // Filter / Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("Filter login records...");
        searchField.getStyleClass().add("search-field");
        searchField.setMaxWidth(Double.MAX_VALUE);

        // Table
        List<LoginRecord> records = MockData.getUserLoginHistory(user != null ? user.getId() : 0);
        ObservableList<LoginRecord> masterData = FXCollections.observableArrayList(records);
        FilteredList<LoginRecord> filtered = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filtered.setPredicate(record -> {
                if (q.isEmpty()) return true;
                String timeStr = record.getLoginAt() != null ? record.getLoginAt().format(TIME_FMT).toLowerCase() : "";
                return timeStr.contains(q) || record.getUsername().toLowerCase().contains(q);
            });
        });

        TableView<LoginRecord> table = new TableView<>(filtered);
        table.getStyleClass().add("table-view-clean");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No login records found for this user."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<LoginRecord, String> timeCol = new TableColumn<>("Login Time");
        timeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getLoginAt() != null ? c.getValue().getLoginAt().format(TIME_FMT) : "—"));
        timeCol.setPrefWidth(180);

        TableColumn<LoginRecord, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getUsername()));
        userCol.setPrefWidth(120);

        TableColumn<LoginRecord, String> statusCol = new TableColumn<>("Result");
        statusCol.setCellValueFactory(c -> new ReadOnlyStringWrapper("SUCCESS"));
        statusCol.setPrefWidth(90);

        table.getColumns().addAll(timeCol, userCol, statusCol);

        Label countLbl = new Label("Total login sessions: " + records.size());
        countLbl.getStyleClass().add("text-muted");

        content.getChildren().addAll(header, searchField, table, countLbl);
        pane.setContent(content);
    }

    public static void showDialog(Window owner, User user) {
        UserLoginHistoryDialog dialog = new UserLoginHistoryDialog(user);
        if (owner != null) dialog.initOwner(owner);
        dialog.showAndWait();
    }
}

