package com.chatsystem.admin.controller.analytics;

import com.chatsystem.admin.controller.analytics.AnalyticsFiltersController.Counts;
import com.chatsystem.admin.controller.analytics.AnalyticsFiltersController.DateRange;
import com.chatsystem.desktop.mock.MockChartSeries;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockActivity;
import com.chatsystem.desktop.mock.model.MockUser;
import java.util.Comparator;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class ActivityController {
    private record ActivityRow(MockUser user, MockActivity activity) {
    }

    @FXML private MonthlyChartController chartController;
    @FXML private AnalyticsFiltersController filtersController;
    @FXML private TableView<ActivityRow> activityRows;
    @FXML private Label activityCount;
    @FXML private TableColumn<ActivityRow, String> accountId;
    @FXML private TableColumn<ActivityRow, String> fullName;
    @FXML private TableColumn<ActivityRow, String> createdAt;
    @FXML private TableColumn<ActivityRow, Integer> appOpens;
    @FXML private TableColumn<ActivityRow, Integer> peopleChattedWith;
    @FXML private TableColumn<ActivityRow, Integer> groupsChattedIn;

    private final MockData data = MockData.create();
    private DateRange displayedRange;
    private List<ActivityRow> periodRows = List.of();

    @FXML
    private void initialize() {
        AnalyticsTable.bind(accountId, row -> row.user().id());
        AnalyticsTable.bind(fullName, row -> row.user().fullName());
        AnalyticsTable.bind(createdAt, row -> AnalyticsTable.createdAt(row.user()));
        AnalyticsTable.bind(appOpens, row -> row.activity().appOpens());
        AnalyticsTable.bind(peopleChattedWith, row -> row.activity().peopleChattedWith());
        AnalyticsTable.bind(groupsChattedIn, row -> row.activity().groupsChattedIn());
        filtersController.configure(new DateRange(data.activityFrom(), data.snapshotDate()), Counts.ACTIVITY,
                this::showActivity);
        chartController.configure(data, MonthlyChartController.Measure.ACTIVITY,
                year -> MockChartSeries.activeUsers(data, year), filtersController::selectMonth);
        showActivity();
    }

    private void showActivity() {
        DateRange range = filtersController.dateRange();
        if (!range.equals(displayedRange)) {
            periodRows = range.valid() ? data.activities(range.from(), range.to()).stream()
                    .filter(row -> row.appOpens() > 0 || row.peopleChattedWith() > 0 || row.groupsChattedIn() > 0)
                    .map(row -> new ActivityRow(data.user(row.userId()), row))
                    .toList() : List.of();
            displayedRange = range;
        }
        var rows = periodRows.stream()
                .filter(row -> filtersController.matchesName(row.user()))
                .filter(row -> filtersController.matchesActivity(row.activity()))
                .sorted(Comparator.comparing(ActivityRow::user, filtersController.accountOrder()))
                .toList();
        activityRows.getItems().setAll(rows);
        activityCount.setText(rows.size() + " tài khoản");
        chartController.markMonth(filtersController.selectedMonth());
    }
}
