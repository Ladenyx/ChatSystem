package com.chatsystem.admin.controller.analytics;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockUser;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;

final class AnalyticsTable {
    private static final DateTimeFormatter CREATED_AT = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
            .withZone(MockData.DISPLAY_ZONE);

    private AnalyticsTable() {
    }

    static <T, V> void bind(TableColumn<T, V> column, Function<T, V> value) {
        column.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(value.apply(row.getValue())));
    }

    static String createdAt(MockUser user) {
        return CREATED_AT.format(user.createdAt());
    }
}
