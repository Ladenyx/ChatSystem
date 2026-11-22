package com.chatsystem.admin.controller.analytics;

import com.chatsystem.desktop.mock.MockData;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public final class MonthlyChartController {
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass FUTURE = PseudoClass.getPseudoClass("future");
    private static final PseudoClass HISTORY = PseudoClass.getPseudoClass("history");
    private static final DateTimeFormatter DAY_IN_YEAR = DateTimeFormatter.ofPattern("dd/MM");

    enum Measure {
        REGISTRATIONS("Tài khoản đăng ký mới theo tháng", "Tài khoản",
                "đăng ký trong năm", "tháng có đăng ký", false),
        ACTIVITY("Người dùng mở ứng dụng theo tháng", "Người dùng duy nhất",
                "người dùng ở tháng cao nhất", "tháng có lượt mở", true),
        FRIENDSHIPS("Lượt chấp nhận kết bạn theo tháng", "Lượt chấp nhận",
                "lượt chấp nhận trong năm", "tháng có chấp nhận", false);

        final String heading;
        final String unit;
        final String primaryLabel;
        final String monthsLabel;
        final boolean showPeak;

        Measure(String heading, String unit, String primaryLabel, String monthsLabel, boolean showPeak) {
            this.heading = heading;
            this.unit = unit;
            this.primaryLabel = primaryLabel;
            this.monthsLabel = monthsLabel;
            this.showPeak = showPeak;
        }
    }

    @FXML private Label chartHeading;
    @FXML private ComboBox<Integer> chartYear;
    @FXML private Label primaryLabel;
    @FXML private Label primaryValue;
    @FXML private Label monthsLabel;
    @FXML private Label monthsValue;
    @FXML private Label chartUnit;
    @FXML private Label monthHint;
    @FXML private StackPane chartHost;

    private Measure measure;
    private IntFunction<XYChart.Series<String, Number>> seriesForYear;
    private Consumer<YearMonth> chooseMonth;
    private XYChart<String, Number> monthlyChart;
    private LocalDate snapshotDate;
    private YearMonth selectedMonth;

    void configure(MockData data, Measure measure, IntFunction<XYChart.Series<String, Number>> seriesForYear,
                   Consumer<YearMonth> chooseMonth) {
        this.measure = measure;
        this.seriesForYear = seriesForYear;
        this.chooseMonth = chooseMonth;
        snapshotDate = data.snapshotDate();
        chartHeading.setText(measure.heading);
        monthHint.setVisible(chooseMonth != null);
        monthHint.setManaged(chooseMonth != null);
        monthlyChart = createChart();
        chartHost.getChildren().setAll(monthlyChart);
        primaryLabel.setText(measure.primaryLabel);
        monthsLabel.setText(measure.monthsLabel);
        chartYear.getItems().setAll(data.availableYears().reversed());
        chartYear.valueProperty().addListener((observable, previous, year) -> {
            if (year != null) {
                showYear(year);
            }
        });
        chartYear.setValue(data.snapshotDate().getYear());
    }

    void markMonth(YearMonth month) {
        selectedMonth = month;
        monthHint.setText(month == null ? "Chọn tháng để lọc bảng"
                : "Bảng đang lọc tháng " + month.getMonthValue() + "/" + month.getYear());
        monthHint.pseudoClassStateChanged(SELECTED, month != null);
        for (var series : monthlyChart.getData()) {
            for (var point : series.getData()) {
                point.getNode().pseudoClassStateChanged(SELECTED, point.getExtraValue().equals(month));
            }
        }
    }

    private XYChart<String, Number> createChart() {
        var xAxis = new CategoryAxis();
        xAxis.setAnimated(false);
        xAxis.setTickMarkVisible(false);
        var yAxis = new NumberAxis(0, 5, 1);
        yAxis.setAnimated(false);
        yAxis.setAutoRanging(false);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickMarkVisible(false);

        XYChart<String, Number> chart;
        if (measure == Measure.ACTIVITY) {
            chart = new LineChart<>(xAxis, yAxis);
        } else {
            var bars = new BarChart<>(xAxis, yAxis);
            bars.setBarGap(2);
            bars.setCategoryGap(28);
            chart = bars;
        }
        chart.setId("monthlyChart");
        chart.getStyleClass().add("admin-monthly-chart");
        chart.pseudoClassStateChanged(HISTORY, measure == Measure.FRIENDSHIPS);
        chart.setAccessibleText(measure.heading);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setAlternativeColumnFillVisible(false);
        chart.setAlternativeRowFillVisible(false);
        chart.setMinSize(0, 144);
        chart.setPrefHeight(164);
        return chart;
    }

    private void showYear(int year) {
        chartUnit.setText(year == snapshotDate.getYear()
                ? measure.unit + " · Cập nhật đến " + DAY_IN_YEAR.format(snapshotDate) : measure.unit);
        var series = seriesForYear.apply(year);
        int peak = 0;
        int total = 0;
        int monthsWithData = 0;
        for (int index = 0; index < series.getData().size(); index++) {
            var point = series.getData().get(index);
            int count = point.getYValue().intValue();
            peak = Math.max(peak, count);
            total += count;
            if (count > 0) {
                monthsWithData++;
            }
            YearMonth month = YearMonth.of(year, index + 1);
            point.setExtraValue(month);
            point.setNode(monthMarker(month, count));
        }
        primaryValue.setText(Integer.toString(measure.showPeak ? peak : total));
        monthsValue.setText(Integer.toString(monthsWithData));

        NumberAxis axis = (NumberAxis) monthlyChart.getYAxis();
        int tick = Math.max(1, (peak + 3) / 4);
        axis.setTickUnit(tick);
        axis.setUpperBound(Math.max(2, (peak + tick - 1) / tick + 1) * tick);
        YearMonth snapshotMonth = YearMonth.from(snapshotDate);
        var futurePoints = measure == Measure.ACTIVITY ? series.getData().stream()
                .filter(point -> ((YearMonth) point.getExtraValue()).isAfter(snapshotMonth))
                .toList() : List.<XYChart.Data<String, Number>>of();
        if (futurePoints.isEmpty()) {
            monthlyChart.getData().setAll(List.of(series));
        } else {
            // Keep future zero values visible without joining them to the observed trend.
            var future = new XYChart.Series<String, Number>();
            series.getData().removeAll(futurePoints);
            future.getData().setAll(futurePoints);
            monthlyChart.getData().setAll(List.of(series, future));
        }
        markMonth(selectedMonth);
    }

    private StackPane monthMarker(YearMonth month, int count) {
        boolean future = month.atDay(1).isAfter(snapshotDate);
        String description = "Tháng " + month.getMonthValue() + "/" + month.getYear()
                + " · " + count + " " + measure.unit.toLowerCase(Locale.ROOT);
        if (future) {
            description += " · Chưa có dữ liệu";
        }
        Labeled value;
        if (chooseMonth == null) {
            value = new Label(Integer.toString(count));
        } else {
            Button button = new Button(Integer.toString(count));
            button.setId("month-" + month.getMonthValue());
            button.setOnAction(event -> chooseMonth.accept(month));
            button.setOnMouseClicked(event -> event.consume());
            button.setAccessibleText(description + " · Lọc bảng theo tháng");
            value = button;
        }
        value.getStyleClass().add("admin-month-value");
        value.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        value.setTranslateY(measure == Measure.ACTIVITY ? -24 : -20);
        StackPane marker = new StackPane(value);
        StackPane.setAlignment(value, Pos.TOP_CENTER);
        if (measure == Measure.ACTIVITY) {
            marker.setMinSize(8, 8);
            marker.setPrefSize(8, 8);
            marker.setMaxSize(8, 8);
        }
        marker.pseudoClassStateChanged(FUTURE, future);
        Tooltip.install(marker, new Tooltip(description));
        if (chooseMonth != null) {
            marker.setCursor(Cursor.HAND);
            marker.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    chooseMonth.accept(month);
                }
            });
        }
        return marker;
    }
}
