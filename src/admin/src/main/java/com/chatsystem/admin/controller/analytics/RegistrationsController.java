package com.chatsystem.admin.controller.analytics;

import com.chatsystem.admin.controller.analytics.AnalyticsFiltersController.Counts;
import com.chatsystem.admin.controller.analytics.AnalyticsFiltersController.DateRange;
import com.chatsystem.desktop.mock.MockChartSeries;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockUser;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class RegistrationsController {
    @FXML private MonthlyChartController chartController;
    @FXML private AnalyticsFiltersController filtersController;
    @FXML private TableView<MockUser> registrationRows;
    @FXML private Label registrationCount;
    @FXML private TableColumn<MockUser, String> accountId;
    @FXML private TableColumn<MockUser, String> fullName;
    @FXML private TableColumn<MockUser, String> email;
    @FXML private TableColumn<MockUser, String> status;
    @FXML private TableColumn<MockUser, String> createdAt;

    private final MockData data = MockData.create();

    @FXML
    private void initialize() {
        AnalyticsTable.bind(accountId, MockUser::id);
        AnalyticsTable.bind(fullName, MockUser::fullName);
        AnalyticsTable.bind(email, MockUser::email);
        AnalyticsTable.bind(status, user -> switch (user.status()) {
            case ACTIVE -> "Hoạt động";
            case LOCKED -> "Bị khóa";
            case DELETED -> "Xóa mềm";
        });
        AnalyticsTable.bind(createdAt, AnalyticsTable::createdAt);
        filtersController.configure(new DateRange(data.activityFrom(), data.snapshotDate()), Counts.NONE,
                this::showRegistrations);
        chartController.configure(data, MonthlyChartController.Measure.REGISTRATIONS,
                year -> MockChartSeries.registrations(data, year), filtersController::selectMonth);
        showRegistrations();
    }

    private void showRegistrations() {
        DateRange range = filtersController.dateRange();
        List<MockUser> rows = range.valid() ? data.users().stream()
                .filter(user -> range.includes(user.createdAt()))
                .filter(filtersController::matchesName)
                .sorted(filtersController.accountOrder())
                .toList() : List.of();
        registrationRows.getItems().setAll(rows);
        registrationCount.setText(rows.size() + " tài khoản");
        chartController.markMonth(filtersController.selectedMonth());
    }
}
