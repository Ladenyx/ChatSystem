package admin.ui;

import common.mock.MockData;
import common.ui.component.YearPicker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Screen #12 (A-13): New Registrations Chart screen.
 * Displays monthly new registration counts for a selected year via an interactive BarChart with KPI summaries.
 */
public class RegistrationsChartView extends VBox {

    private static final String[] MONTHS = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private final YearPicker yearPicker = new YearPicker(2025);
    private final BarChart<String, Number> barChart;
    private final XYChart.Series<String, Number> series = new XYChart.Series<>();

    private final Label totalKpiValue = new Label("0");
    private final Label peakKpiValue = new Label("—");
    private final Label avgKpiValue = new Label("0");

    public RegistrationsChartView() {
        setSpacing(20);
        setPadding(new Insets(24));
        getStyleClass().add("admin-content");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month of Year");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("New User Accounts");
        yAxis.setTickUnit(2);
        yAxis.setMinorTickVisible(false);

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("New Registrations by Month");
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.getData().add(series);
        VBox.setVgrow(barChart, Priority.ALWAYS);

        buildHeader();
        buildKpiCards();
        getChildren().add(barChart);

        yearPicker.setOnYearChanged(year -> updateChart(year));
        updateChart(yearPicker.getYear());
    }

    private void buildHeader() {
        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox titleBlock = new VBox(4);
        Label heading = new Label("New Registrations Chart");
        heading.getStyleClass().add("admin-heading");
        Label sub = new Label("Visualize user acquisition growth and identify peak onboarding seasons.");
        sub.getStyleClass().add("text-muted");
        titleBlock.getChildren().addAll(heading, sub);
        HBox.setHgrow(titleBlock, Priority.ALWAYS);

        top.getChildren().addAll(titleBlock, yearPicker);
        getChildren().add(top);
    }

    private void buildKpiCards() {
        GridPane grid = new GridPane();
        grid.setHgap(16);

        grid.add(createCard("Total Registrations", totalKpiValue, "Accounts created in selected year"), 0, 0);
        grid.add(createCard("Peak Month", peakKpiValue, "Highest acquisition velocity"), 1, 0);
        grid.add(createCard("Monthly Average", avgKpiValue, "Average registrations per month"), 2, 0);

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
        Map<Integer, Long> data = MockData.getMonthlyRegistrations(year);
        series.getData().clear();

        long total = 0;
        long maxVal = -1;
        String peakMonth = "—";

        for (int m = 1; m <= 12; m++) {
            long count = data.getOrDefault(m, 0L);
            total += count;
            String monthName = MONTHS[m - 1];
            if (count > maxVal) {
                maxVal = count;
                peakMonth = monthName + " (" + count + ")";
            }
            series.getData().add(new XYChart.Data<>(monthName, count));
        }

        double avg = total / 12.0;

        totalKpiValue.setText(String.valueOf(total));
        peakKpiValue.setText(peakMonth);
        avgKpiValue.setText(String.format("%.1f / mo", avg));
        barChart.setTitle("New Registrations by Month (" + year + ")");
    }
}

