package com.chatsystem.admin.controller.analytics;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockActivity;
import com.chatsystem.desktop.mock.model.MockUser;
import java.text.Collator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.ToIntFunction;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalDateStringConverter;

public final class AnalyticsFiltersController {
    enum Counts { NONE, ACTIVITY, DIRECT_FRIENDS }

    record DateRange(LocalDate from, LocalDate to) {
        boolean valid() {
            return from != null && to != null && !from.isAfter(to);
        }

        boolean includes(Instant instant) {
            LocalDate date = instant.atZone(MockData.DISPLAY_ZONE).toLocalDate();
            return !date.isBefore(from) && !date.isAfter(to);
        }
    }

    private enum Sort {
        NEWEST("Ngày tạo ↓", Comparator.comparing(MockUser::createdAt).reversed()),
        OLDEST("Ngày tạo ↑", Comparator.comparing(MockUser::createdAt)),
        NAME_ASC("Họ tên · A → Z", byName()),
        NAME_DESC("Họ tên · Z → A", byName().reversed());

        private final String label;
        private final Comparator<MockUser> comparison;

        Sort(String label, Comparator<MockUser> comparison) {
            this.label = label;
            this.comparison = comparison.thenComparing(MockUser::id);
        }

        @Override
        public String toString() {
            return label;
        }

        private static Comparator<MockUser> byName() {
            return Comparator.comparing(MockUser::fullName, Collator.getInstance(Locale.of("vi", "VN")));
        }
    }

    private enum Metric {
        OPENS("Lượt mở ứng dụng", MockActivity::appOpens),
        PEOPLE("Số người đã chat", MockActivity::peopleChattedWith),
        GROUPS("Số nhóm đã chat", MockActivity::groupsChattedIn);

        private final String label;
        private final ToIntFunction<MockActivity> read;

        Metric(String label, ToIntFunction<MockActivity> read) {
            this.label = label;
            this.read = read;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @FXML private VBox fromField;
    @FXML private VBox toField;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private TextField nameFilter;
    @FXML private ComboBox<Sort> accountSort;
    @FXML private FlowPane countFilters;
    @FXML private VBox metricField;
    @FXML private ComboBox<Metric> activityMetric;
    @FXML private Label comparisonLabel;
    @FXML private ToggleGroup comparison;
    @FXML private ToggleButton equalTo;
    @FXML private TextField threshold;
    @FXML private Label filterError;

    private DateRange defaultRange;
    private DateRange rangeBeforeMonth;
    private YearMonth selectedMonth;
    private boolean updating;
    private Runnable onChange = () -> { };

    @FXML
    private void initialize() {
        var displayDate = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        fromDate.setConverter(new LocalDateStringConverter(displayDate, displayDate));
        toDate.setConverter(new LocalDateStringConverter(displayDate, displayDate));
        accountSort.getItems().setAll(Sort.values());
        accountSort.setValue(Sort.NEWEST);
        activityMetric.getItems().setAll(Metric.values());
        activityMetric.setValue(Metric.OPENS);
        threshold.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("[0-9]{0,10}") ? change : null));

        fromDate.valueProperty().addListener(observable -> dateChanged());
        toDate.valueProperty().addListener(observable -> dateChanged());
        for (var property : List.<Observable>of(nameFilter.textProperty(), accountSort.valueProperty(),
                activityMetric.valueProperty(), threshold.textProperty())) {
            property.addListener(observable -> refresh());
        }
        comparison.selectedToggleProperty().addListener((observable, previous, selected) -> {
            if (selected == null) {
                comparison.selectToggle(previous);
            } else {
                refresh();
            }
        });
    }

    void configure(DateRange range, Counts counts, Runnable onChange) {
        defaultRange = range;
        show(fromField, range != null);
        show(toField, range != null);
        show(countFilters, counts != Counts.NONE);
        show(metricField, counts == Counts.ACTIVITY);
        countFilters.setPrefWrapLength(counts == Counts.DIRECT_FRIENDS ? 236 : 432);
        comparisonLabel.setText(counts == Counts.DIRECT_FRIENDS ? "Số bạn trực tiếp" : "Toán tử");
        setRange(range);
        this.onChange = onChange;
    }

    DateRange dateRange() {
        return new DateRange(fromDate.getValue(), toDate.getValue());
    }

    YearMonth selectedMonth() {
        return selectedMonth;
    }

    void selectMonth(YearMonth month) {
        if (month.equals(selectedMonth)) {
            selectedMonth = null;
            setRange(rangeBeforeMonth);
        } else {
            if (selectedMonth == null) {
                var current = dateRange();
                rangeBeforeMonth = current.valid() ? current : defaultRange;
            }
            selectedMonth = month;
            setRange(new DateRange(month.atDay(1), month.atEndOfMonth()));
        }
        refresh();
    }

    boolean matchesName(MockUser user) {
        return user.fullName().toLowerCase(Locale.ROOT)
                .contains(nameFilter.getText().strip().toLowerCase(Locale.ROOT));
    }

    Comparator<MockUser> accountOrder() {
        return accountSort.getValue().comparison;
    }

    boolean matchesActivity(MockActivity row) {
        return matchesCount(activityMetric.getValue().read.applyAsInt(row));
    }

    boolean matchesCount(int count) {
        if (threshold.getText().isEmpty()) {
            return true;
        }
        long value = Long.parseLong(threshold.getText());
        return switch (((ToggleButton) comparison.getSelectedToggle()).getText()) {
            case "<" -> count < value;
            case ">" -> count > value;
            default -> count == value;
        };
    }

    @FXML
    private void clearFilters() {
        updating = true;
        selectedMonth = null;
        nameFilter.clear();
        accountSort.setValue(Sort.NEWEST);
        activityMetric.setValue(Metric.OPENS);
        comparison.selectToggle(equalTo);
        threshold.clear();
        if (defaultRange != null) {
            fromDate.setValue(defaultRange.from());
            toDate.setValue(defaultRange.to());
        }
        updating = false;
        refresh();
    }

    private void setRange(DateRange range) {
        if (range == null) {
            return;
        }
        updating = true;
        fromDate.setValue(range.from());
        toDate.setValue(range.to());
        updating = false;
    }

    private void dateChanged() {
        if (!updating) {
            selectedMonth = null;
            refresh();
        }
    }

    private void refresh() {
        if (!updating) {
            show(filterError, defaultRange != null && !dateRange().valid());
            onChange.run();
        }
    }

    private static void show(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }
}
