package admin.ui;

import common.mock.MockData;
import common.ui.component.YearPicker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Screen #15 (A-16): Active Users Chart screen.
 * Displays monthly active users (MAU) for a selected year via an interactive LineChart with KPI summaries.
 */
public class ActiveUsersChartView extends VBox {

    private static final String[] MONTHS = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private final YearPicker yearPicker = new YearPicker(2026);
    private final LineChart<String, Number> lineChart;
    private final XYChart.Series<String, Number> series = new XYChart.Series<>();

    private final Label peakKpiValue = new Label("—");
    private final Label avgKpiValue = new Label("0");
    private final Label minKpiValue = new Label("—");

    public ActiveUsersChartView() {
        setSpacing(20);
        setPadding(new Insets(24));
        getStyleClass().add("admin-content");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month of Year");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Active Users (MAU)");
        yAxis.setTickUnit(2);
        yAxis.setMinorTickVisible(false);

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Monthly Active Users Trend");
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false);
        lineChart.getData().add(series);
        VBox.setVgrow(lineChart, Priority.ALWAYS);

        buildHeader();
        buildKpiCards();
        getChildren().add(lineChart);

        yearPicker.setOnYearChanged(year -> updateChart(year));
        updateChart(yearPicker.getYear());
    }

    private void buildHeader() {
        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox titleBlock = new VBox(4);
        Label heading = new Label("Active Users Chart (MAU)");
        heading.getStyleClass().add("admin-heading");
        Label sub = new Label("Analyze seasonal retention dynamics and monthly active user growth curves.");
        sub.getStyleClass().add("text-muted");
        titleBlock.getChildren().addAll(heading, sub);
        HBox.setHgrow(titleBlock, Priority.ALWAYS);

        top.getChildren().addAll(titleBlock, yearPicker);
        getChildren().add(top);
    }

    private void buildKpiCards() {
        GridPane grid = new GridPane();
        grid.setHgap(16);

        grid.add(createCard("Peak Active Month", peakKpiValue, "Month with highest active population"), 0, 0);
        grid.add(createCard("Average Monthly Active", avgKpiValue, "Average unique active accounts per month"), 1, 0);
        grid.add(createCard("Lowest Active Month", minKpiValue, "Base seasonal traffic low"), 2, 0);

        for (int i = 0; i < 3; i++) {
            javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
            cc.setPercentWidth(33.333);
            grid.getColumnConstraints().add(cc);
        }

        getChildren().add(grid);
    }

    private VBox createCard(String title, Label valueLabel, String subtitle) {
        VBox card = new VBox(6);
        card.getStyleClass().add("admin-summary-card");

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("text-muted");

        valueLabel.getStyleClass().add("admin-heading");

        Label subLbl = new Label(subtitle);
        subLbl.getStyleClass().add("text-muted");

        card.getChildren().addAll(titleLbl, valueLabel, subLbl);
        return card;
    }

    private void updateChart(int year) {
        Map<Integer, Long> data = MockData.getMonthlyActiveUsers(year);
        series.getData().clear();

        long total = 0;
        long maxVal = -1;
        long minVal = Long.MAX_VALUE;
        String peakMonth = "—";
        String minMonth = "—";

        for (int m = 1; m <= 12; m++) {
            long count = data.getOrDefault(m, 0L);
            total += count;
            String monthName = MONTHS[m - 1];

            if (count > maxVal) {
                maxVal = count;
                peakMonth = monthName + " (" + count + " users)";
            }
            if (count < minVal) {
                minVal = count;
                minMonth = monthName + " (" + count + " users)";
            }
            series.getData().add(new XYChart.Data<>(monthName, count));
        }

        double avg = total / 12.0;

        peakKpiValue.setText(peakMonth);
        avgKpiValue.setText(String.format("%.1f users", avg));
        minKpiValue.setText(minMonth);
        lineChart.setTitle("Monthly Active Users (" + year + ")");
    }
}

