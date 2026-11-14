package com.chatsystem.desktop.mock;

import com.chatsystem.desktop.mock.model.MockMonthlyStatistics;
import java.util.List;
import java.util.function.ToIntFunction;
import javafx.scene.chart.XYChart;

/** Fresh series/data nodes on every call; never attach the same series to two charts. */
public final class MockChartSeries {
    private MockChartSeries() {
    }

    public static XYChart.Series<String, Number> registrations(MockData data, int year) {
        return series("Đăng ký mới", data.monthlyStatistics(year), MockMonthlyStatistics::registrations);
    }

    public static XYChart.Series<String, Number> activeUsers(MockData data, int year) {
        return series("Người mở ứng dụng", data.monthlyStatistics(year), MockMonthlyStatistics::activeUsers);
    }

    private static XYChart.Series<String, Number> series(String name, List<MockMonthlyStatistics> rows,
                                                        ToIntFunction<MockMonthlyStatistics> value) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(name);
        for (MockMonthlyStatistics row : rows) {
            series.getData().add(new XYChart.Data<>("T" + row.month().getMonthValue(), value.applyAsInt(row)));
        }
        return series;
    }
}
