package com.chatsystem.desktop.mock;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MockChartSeriesTest {
    @Test
    void seriesKeepJanuaryThroughDecemberAndFreshMutableStateForEachChart() {
        MockData data = MockData.create();
        var registrations = MockChartSeries.registrations(data, 2026);
        var another = MockChartSeries.registrations(data, 2026);
        var active = MockChartSeries.activeUsers(data, 2026);
        assertNotSame(registrations, another);
        assertNotSame(registrations.getData().getFirst(), another.getData().getFirst());
        assertEquals(12, registrations.getData().size());
        assertEquals(12, active.getData().size());
        for (int index = 0; index < 12; index++) {
            assertEquals("T" + (index + 1), registrations.getData().get(index).getXValue());
            assertEquals(data.monthlyStatistics(2026).get(index).registrations(), registrations.getData().get(index).getYValue());
            assertEquals(data.monthlyStatistics(2026).get(index).activeUsers(), active.getData().get(index).getYValue());
        }
        registrations.getData().getFirst().setYValue(999);
        registrations.getData().removeLast();
        assertEquals(12, another.getData().size());
        assertEquals(1, another.getData().getFirst().getYValue());
        assertEquals(1, data.monthlyStatistics(2026).getFirst().registrations());
        assertTrue(MockChartSeries.activeUsers(data, 2030).getData().stream()
                .allMatch(point -> point.getYValue().intValue() == 0));
    }
}
