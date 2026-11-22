package com.chatsystem.admin.controller.overview;

import com.chatsystem.admin.AdminViews;
import com.chatsystem.desktop.mock.MockChartSeries;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockLoginLog;
import com.chatsystem.desktop.mock.model.MockSpamReport;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public final class OverviewController implements ViewController {
    private static final int RECENT_ROW_LIMIT = 3;
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(MockData.DISPLAY_ZONE);

    @FXML private Button totalUsersCard;
    @FXML private Button onlineUsersCard;
    @FXML private Button totalGroupsCard;
    @FXML private Button pendingSpamCard;
    @FXML private Label totalUsersValue;
    @FXML private Label onlineUsersValue;
    @FXML private Region onlineIndicator;
    @FXML private Label totalGroupsValue;
    @FXML private Label pendingSpamValue;
    @FXML private Label trendTitle;
    @FXML private Label registrationsLegend;
    @FXML private Label activityLegend;
    @FXML private LineChart<String, Number> monthlyTrend;
    @FXML private TableView<MockSpamReport> recentReports;
    @FXML private TableColumn<MockSpamReport, String> reportedAccount;
    @FXML private TableColumn<MockSpamReport, String> reporterAccount;
    @FXML private TableColumn<MockSpamReport, String> reportedAt;
    @FXML private TableView<MockLoginLog> recentLogins;
    @FXML private TableColumn<MockLoginLog, String> loginUsername;
    @FXML private TableColumn<MockLoginLog, String> loginFullName;
    @FXML private TableColumn<MockLoginLog, String> loggedInAt;

    private ViewNavigator navigator;

    @FXML
    private void initialize() {
        MockData data = MockData.create();
        showCount(totalUsersCard, totalUsersValue, data.users().size());
        long onlineUsers = data.users().stream()
                .filter(user -> user.presence() == MockUser.Presence.ONLINE).count();
        showCount(onlineUsersCard, onlineUsersValue, onlineUsers);
        onlineIndicator.setVisible(onlineUsers > 0);
        onlineIndicator.managedProperty().bind(onlineIndicator.visibleProperty());
        showCount(totalGroupsCard, totalGroupsValue, data.groups().size());
        showCount(pendingSpamCard, pendingSpamValue, data.spamReports().stream()
                .filter(report -> report.status() == MockSpamReport.Status.PENDING).count());
        showTrend(data, data.snapshotDate().getYear());
        showRecentReports(data);
        showRecentLogins(data);
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
    }

    private static void showCount(Button card, Label valueLabel, long count) {
        valueLabel.setText(Long.toString(count));
        card.setAccessibleText(card.getAccessibleText() + ", " + count);
    }

    private void showTrend(MockData data, int year) {
        trendTitle.setText("Đăng ký và hoạt động · " + year);
        var registrations = MockChartSeries.registrations(data, year);
        var activeUsers = MockChartSeries.activeUsers(data, year);
        activeUsers.setName("Người dùng duy nhất mở ứng dụng");
        registrationsLegend.setText(registrations.getName());
        activityLegend.setText(activeUsers.getName());

        int maximum = 0;
        for (int month = 0; month < registrations.getData().size(); month++) {
            var registrationPoint = registrations.getData().get(month);
            var activityPoint = activeUsers.getData().get(month);
            maximum = Math.max(maximum,
                    Math.max(registrationPoint.getYValue().intValue(), activityPoint.getYValue().intValue()));
            Tooltip tooltip = new Tooltip("Tháng " + (month + 1) + "/" + year
                    + "\n" + registrations.getName() + " · " + registrationPoint.getYValue()
                    + "\n" + activeUsers.getName() + " · " + activityPoint.getYValue());
            for (var point : List.of(registrationPoint, activityPoint)) {
                StackPane symbol = new StackPane();
                point.setNode(symbol);
                Tooltip.install(symbol, tooltip);
            }
        }

        NumberAxis axis = (NumberAxis) monthlyTrend.getYAxis();
        int tickUnit = Math.max(1, (maximum + 3) / 4);
        int headroom = Math.max(1, (maximum + 4) / 5);
        axis.setTickUnit(tickUnit);
        axis.setUpperBound(((maximum + headroom + tickUnit - 1) / tickUnit) * tickUnit);
        monthlyTrend.getData().setAll(List.of(registrations, activeUsers));
    }

    private void showRecentReports(MockData data) {
        reportedAccount.setCellValueFactory(row ->
                new ReadOnlyStringWrapper("@" + data.user(row.getValue().reportedUserId()).username()));
        reporterAccount.setCellValueFactory(row ->
                new ReadOnlyStringWrapper("@" + data.user(row.getValue().reporterId()).username()));
        reportedAt.setCellValueFactory(row -> new ReadOnlyStringWrapper(DATE_TIME.format(row.getValue().createdAt())));
        recentReports.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        recentReports.getItems().setAll(data.spamReports().stream()
                .sorted(Comparator.comparing(MockSpamReport::createdAt).reversed())
                .limit(RECENT_ROW_LIMIT)
                .toList());
    }

    private void showRecentLogins(MockData data) {
        loginUsername.setCellValueFactory(row ->
                new ReadOnlyStringWrapper(data.user(row.getValue().userId()).username()));
        loginFullName.setCellValueFactory(row ->
                new ReadOnlyStringWrapper(data.user(row.getValue().userId()).fullName()));
        loggedInAt.setCellValueFactory(row -> new ReadOnlyStringWrapper(DATE_TIME.format(row.getValue().loggedInAt())));
        recentLogins.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        recentLogins.getItems().setAll(data.loginLogs().stream()
                .sorted(Comparator.comparing(MockLoginLog::loggedInAt).reversed())
                .limit(RECENT_ROW_LIMIT)
                .toList());
    }

    @FXML
    private void openUsers() {
        navigator.navigate(AdminViews.USERS);
    }

    @FXML
    private void openGroups() {
        navigator.navigate(AdminViews.GROUPS);
    }

    @FXML
    private void openSpam() {
        navigator.navigate(AdminViews.SPAM);
    }

    @FXML
    private void openLoginLogs() {
        navigator.navigate(AdminViews.LOGIN_LOGS);
    }

    @FXML
    private void openRegistrations() {
        navigator.navigate(AdminViews.REGISTRATIONS);
    }

    @FXML
    private void openActivity() {
        navigator.navigate(AdminViews.ACTIVITY);
    }
}
