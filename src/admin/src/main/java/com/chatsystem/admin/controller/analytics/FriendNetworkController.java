package com.chatsystem.admin.controller.analytics;

import com.chatsystem.admin.controller.analytics.AnalyticsFiltersController.Counts;
import com.chatsystem.admin.mock.UserManagementFixtures;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockUser;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public final class FriendNetworkController {
    private record NetworkRow(MockUser user, int direct, int extended) {
    }

    @FXML private MonthlyChartController chartController;
    @FXML private AnalyticsFiltersController filtersController;
    @FXML private Label currentAccounts;
    @FXML private Label connectedAccounts;
    @FXML private TableView<NetworkRow> networkRows;
    @FXML private Label networkCount;
    @FXML private TableColumn<NetworkRow, String> accountId;
    @FXML private TableColumn<NetworkRow, String> fullName;
    @FXML private TableColumn<NetworkRow, String> createdAt;
    @FXML private TableColumn<NetworkRow, Integer> directFriends;
    @FXML private TableColumn<NetworkRow, NetworkRow> extendedFriends;

    private final List<NetworkRow> network = UserManagementFixtures.users().stream()
            .filter(user -> user.status() != MockUser.Status.DELETED)
            .map(FriendNetworkController::networkFor)
            .toList();

    @FXML
    private void initialize() {
        AnalyticsTable.bind(accountId, row -> row.user().id());
        AnalyticsTable.bind(fullName, row -> row.user().fullName());
        AnalyticsTable.bind(createdAt, row -> AnalyticsTable.createdAt(row.user()));
        AnalyticsTable.bind(directFriends, NetworkRow::direct);
        AnalyticsTable.bind(extendedFriends, row -> row);
        extendedFriends.setCellFactory(column -> new ReachCell());
        currentAccounts.setText(Integer.toString(network.size()));
        connectedAccounts.setText(Long.toString(network.stream().filter(row -> row.direct() > 0).count()));
        filtersController.configure(null, Counts.DIRECT_FRIENDS, this::showNetwork);
        chartController.configure(MockData.create(), MonthlyChartController.Measure.FRIENDSHIPS,
                FriendNetworkController::monthlyAcceptances, null);
        showNetwork();
    }

    private void showNetwork() {
        var rows = network.stream()
                .filter(row -> filtersController.matchesName(row.user()))
                .filter(row -> filtersController.matchesCount(row.direct()))
                .sorted(Comparator.comparing(NetworkRow::user, filtersController.accountOrder()))
                .toList();
        networkRows.getItems().setAll(rows);
        networkCount.setText(rows.size() + " kết quả");
        networkCount.setVisible(rows.size() != network.size());
        networkCount.setManaged(networkCount.isVisible());
    }

    private static NetworkRow networkFor(MockUser user) {
        var direct = UserManagementFixtures.friends(user.id());
        long extended = Stream.concat(direct.stream(), direct.stream().flatMap(friend ->
                        UserManagementFixtures.friends(friend.id()).stream()))
                .map(MockUser::id)
                .filter(id -> !id.equals(user.id()) && !UserManagementFixtures.hasBlockBetween(user.id(), id))
                .distinct()
                .count();
        return new NetworkRow(user, direct.size(), (int) extended);
    }

    private static XYChart.Series<String, Number> monthlyAcceptances(int year) {
        var acceptances = UserManagementFixtures.friendshipAcceptances();
        var series = new XYChart.Series<String, Number>();
        for (int month = 1; month <= 12; month++) {
            YearMonth period = YearMonth.of(year, month);
            long count = acceptances.stream().filter(period::equals).count();
            series.getData().add(new XYChart.Data<>("T" + month, count));
        }
        return series;
    }

    private static final class ReachCell extends TableCell<NetworkRow, NetworkRow> {
        private final Label count = new Label();
        private final ProgressBar ratio = new ProgressBar(0);
        private final Tooltip details = new Tooltip();
        private final HBox content = new HBox(12, count, ratio);

        private ReachCell() {
            count.getStyleClass().add("admin-analytics-ratio-count");
            ratio.getStyleClass().add("admin-friend-ratio");
            ratio.setFocusTraversable(false);
            content.setAlignment(Pos.CENTER_LEFT);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(NetworkRow row, boolean empty) {
            super.updateItem(row, empty);
            if (empty || row == null) {
                setGraphic(null);
                setTooltip(null);
                setAccessibleText(null);
                return;
            }
            count.setText(Integer.toString(row.extended()));
            ratio.setProgress(row.extended() == 0 ? 0 : (double) row.direct() / row.extended());
            String description = row.extended() + " bạn mở rộng, gồm " + row.direct() + " bạn trực tiếp";
            details.setText(description);
            setTooltip(details);
            setAccessibleText(description);
            setGraphic(content);
        }
    }
}
