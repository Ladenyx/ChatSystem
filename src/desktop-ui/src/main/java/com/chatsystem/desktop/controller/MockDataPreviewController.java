package com.chatsystem.desktop.controller;

import com.chatsystem.desktop.mock.MockChartSeries;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockGroup;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockMonthlyStatistics;
import com.chatsystem.desktop.mock.model.MockSpamReport;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.View;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** Shared fixture browser, separate from the future business screens and their permissions. */
public final class MockDataPreviewController {
    public static final View VIEW = new View("mock-data", "Dữ liệu mẫu", MockDataPreviewController.class,
            "/com/chatsystem/desktop/fxml/mock-data.fxml");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
            .withZone(MockData.DISPLAY_ZONE);

    @FXML private Label summary;
    @FXML private TabPane mockTabs;
    private final MockData data = MockData.create();

    @FXML
    private void initialize() {
        summary.setText("%d tài khoản · %d hội thoại · %d tin nhắn · %d nhóm · %d báo cáo\nDữ liệu hư cấu · Mốc mẫu: %s (Asia/Ho_Chi_Minh)"
                .formatted(data.users().size(), data.conversations().size(), data.messages().size(),
                        data.groups().size(), data.spamReports().size(), time(MockData.SNAPSHOT_AT)));
        summary.setMinHeight(Region.USE_PREF_SIZE);
        addTab("Người dùng", users());
        addTab("Hội thoại", conversations());
        addTab("Nhóm", groups());
        addTab("Báo cáo spam", reports());
        addTab("12 tháng", statistics());
    }

    private Node users() {
        TableView<MockUser> table = table("mockUsers", data.users());
        table.getColumns().add(column("Username", MockUser::username, 130));
        table.getColumns().add(column("Họ tên", MockUser::fullName, 180));
        table.getColumns().add(column("Tài khoản", MockUser::status, 110));
        table.getColumns().add(column("Kết nối", MockUser::presence, 110));
        table.getColumns().add(column("Ngày tạo", user -> time(user.createdAt()), 155));
        Label detail = note("");
        table.getSelectionModel().selectedItemProperty().addListener((observable, old, user) -> {
            if (user != null) {
                detail.setText(user.email() + " · " + user.role() + " · "
                        + (user.address().isBlank() ? "Chưa nhập địa chỉ" : user.address())
                        + user.deletedAt().map(at -> " · Xóa mềm: " + time(at)).orElse(""));
            }
        });
        table.getSelectionModel().selectFirst();
        return expanding(table, detail);
    }

    private Node conversations() {
        ComboBox<MockConversation> choice = new ComboBox<>(FXCollections.observableArrayList(data.conversations()));
        choice.setId("mockConversation");
        choice.setMaxWidth(Double.MAX_VALUE);
        choice.setConverter(converter(chat -> chat.title() + (chat.encrypted() ? " · Nhóm E2EE" : " · 1–1")));
        ListView<MockMessage> messages = messageList("mockMessages", Function.identity(), message -> "");
        Label count = note("");
        choice.valueProperty().addListener((observable, old, chat) -> {
            messages.getItems().setAll(data.messagesFor(chat.id()));
            count.setText(messages.getItems().size() + " tin mẫu · " + chat.unreadCount() + " chưa đọc · "
                    + "Người xem: " + data.previewUser().fullName());
        });
        choice.getSelectionModel().select(5); // A 56-message group also covers the future 50-row page boundary.
        Label label = new Label("Hội thoại mẫu");
        label.setLabelFor(choice);
        VBox box = new VBox(12, label, choice, count, messages,
                note("Bản rõ giả lập tại client; nhãn E2EE không thực hiện mã hóa."));
        VBox.setVgrow(messages, Priority.ALWAYS);
        return box;
    }

    private Node groups() {
        TableView<MockGroup> table = table("mockGroups", data.groups());
        table.getColumns().add(column("Tên nhóm", MockGroup::name, 220));
        table.getColumns().add(column("Thành viên", group -> group.members().size(), 100));
        table.getColumns().add(column("Admin nhóm", group -> group.members().stream()
                .filter(member -> member.role() == MockGroup.Role.ADMIN).count(), 110));
        table.getColumns().add(column("Ngày tạo", group -> time(group.createdAt()), 155));
        Label members = note("");
        table.getSelectionModel().selectedItemProperty().addListener((observable, old, group) -> {
            if (group != null) {
                members.setText(group.members().stream().map(member -> data.user(member.userId()).fullName()
                        + (member.role() == MockGroup.Role.ADMIN ? " (Admin nhóm)" : ""))
                        .collect(Collectors.joining(" · ")));
            }
        });
        table.getSelectionModel().selectFirst();
        return expanding(table, members);
    }

    private Node reports() {
        TableView<MockSpamReport> table = table("mockReports", data.spamReports());
        table.setMinHeight(120);
        table.setPrefHeight(150);
        table.getColumns().add(column("Mã", MockSpamReport::id, 80));
        table.getColumns().add(column("Bị báo cáo", report -> data.user(report.reportedUserId()).username(), 125));
        table.getColumns().add(column("Trạng thái", MockSpamReport::status, 110));
        table.getColumns().add(column("Thời điểm", report -> time(report.createdAt()), 155));
        table.getColumns().add(column("Bằng chứng", report -> report.evidence().size(), 100));
        Label reason = note("");
        ListView<MockSpamReport.Evidence> evidence = messageList("mockEvidence", MockSpamReport.Evidence::message,
                item -> (item.selected() ? "Selected " : "") + (item.context() ? "Context " : ""));
        table.getSelectionModel().selectedItemProperty().addListener((observable, old, report) -> {
            if (report != null) {
                reason.setText("Người báo cáo: " + data.user(report.reporterId()).username() + " · " + report.reason());
                evidence.getItems().setAll(report.evidence());
            }
        });
        table.getSelectionModel().selectFirst();
        VBox box = new VBox(12, table, reason, evidence,
                note("Bản rõ do người báo cáo cung cấp; chỉ xem gói bằng chứng Selected ∪ Context (tối đa 30 tin Context)."));
        VBox.setVgrow(evidence, Priority.ALWAYS);
        return box;
    }

    private Node statistics() {
        ComboBox<Integer> year = new ComboBox<>(FXCollections.observableArrayList(data.availableYears()));
        year.setId("mockYear");
        ComboBox<String> metric = new ComboBox<>(FXCollections.observableArrayList("Đăng ký mới", "Người mở ứng dụng"));
        metric.setId("mockMetric");
        metric.getSelectionModel().selectFirst();
        Label yearLabel = new Label("Năm");
        yearLabel.setLabelFor(year);
        Label metricLabel = new Label("Chỉ số");
        metricLabel.setLabelFor(metric);
        HBox filters = new HBox(12, yearLabel, year, metricLabel, metric);
        filters.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        CategoryAxis months = new CategoryAxis();
        months.setLabel("Tháng");
        NumberAxis counts = new NumberAxis();
        counts.setLabel("Số người");
        counts.setTickLabelFormatter(new NumberAxis.DefaultFormatter(counts) {
            @Override public String toString(Number value) {
                return value.doubleValue() == value.intValue() ? Integer.toString(value.intValue()) : "";
            }
        });
        BarChart<String, Number> chart = new BarChart<>(months, counts);
        chart.setId("mockChart");
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setMinHeight(180);
        chart.setPrefHeight(220);
        TableView<MockMonthlyStatistics> table = table("mockMonths", java.util.List.of());
        table.getColumns().add(column("Tháng", row -> row.month().getMonthValue(), 100));
        table.getColumns().add(column("Đăng ký mới", MockMonthlyStatistics::registrations, 180));
        table.getColumns().add(column("Người mở ứng dụng duy nhất", MockMonthlyStatistics::activeUsers, 250));
        Runnable update = () -> {
            if (year.getValue() == null) {
                return;
            }
            table.getItems().setAll(data.monthlyStatistics(year.getValue()));
            XYChart.Series<String, Number> series = metric.getSelectionModel().getSelectedIndex() == 0
                    ? MockChartSeries.registrations(data, year.getValue()) : MockChartSeries.activeUsers(data, year.getValue());
            chart.setTitle(series.getName() + " · " + year.getValue());
            chart.setAccessibleText(chart.getTitle() + ". Bảng bên dưới chứa đủ 12 tháng và giá trị chính xác.");
            chart.getData().setAll(java.util.List.of(series));
            for (XYChart.Data<String, Number> point : series.getData()) {
                point.nodeProperty().addListener((observable, old, node) -> describePoint(node, point));
                describePoint(point.getNode(), point);
            }
        };
        year.valueProperty().addListener((observable, old, next) -> update.run());
        metric.valueProperty().addListener((observable, old, next) -> update.run());
        year.getSelectionModel().selectLast();
        VBox box = new VBox(12, filters, chart, table,
                note("Dữ liệu đến mốc mẫu 15/11/2026; tháng trống = 0. Bảng số liệu có thể đọc và sắp xếp bằng bàn phím."));
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private static void describePoint(Node node, XYChart.Data<String, Number> point) {
        if (node != null) {
            String description = point.getXValue() + ": " + point.getYValue();
            Tooltip.install(node, new Tooltip(description));
        }
    }

    private <T> ListView<T> messageList(String id, Function<T, MockMessage> message, Function<T, String> prefix) {
        ListView<T> list = new ListView<>();
        list.setId(id);
        list.setMinHeight(80);
        list.setPlaceholder(new Label("Không có tin nhắn mẫu"));
        list.setCellFactory(view -> new ListCell<>() {
            private final Label meta = note("");
            private final Label body = new Label();
            private final VBox content = new VBox(4, meta, body);
            {
                body.setWrapText(true);
                body.maxWidthProperty().bind(list.widthProperty().subtract(56));
                meta.maxWidthProperty().bind(body.maxWidthProperty());
                content.getStyleClass().add("mock-message");
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    MockMessage row = message.apply(item);
                    meta.setText(prefix.apply(item) + data.user(row.senderId()).fullName()
                            + " · " + time(row.sentAt()) + " · " + row.state());
                    body.setText(row.state() == MockMessage.State.RECALLED ? "Tin nhắn đã thu hồi" : row.text());
                    setGraphic(content);
                }
            }
        });
        return list;
    }

    private void addTab(String name, Node content) {
        Tab tab = new Tab(name, content);
        tab.setClosable(false);
        mockTabs.getTabs().add(tab);
    }

    private static <T> TableView<T> table(String id, Collection<T> rows) {
        TableView<T> table = new TableView<>(FXCollections.observableArrayList(rows));
        table.setId(id);
        table.setMinHeight(80);
        table.setPlaceholder(new Label("Không có dữ liệu mẫu"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        return table;
    }

    /** Explicit accessors support Java records; PropertyValueFactory expects bean-style getters. */
    private static <T, V> TableColumn<T, V> column(String title, Function<T, V> value, double width) {
        TableColumn<T, V> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(value.apply(cell.getValue())));
        column.setPrefWidth(width);
        return column;
    }

    private static Label note(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.getStyleClass().add("secondary-text");
        return label;
    }

    private static VBox expanding(Node main, Node footer) {
        VBox box = new VBox(12, main, footer);
        VBox.setVgrow(main, Priority.ALWAYS);
        return box;
    }

    private static <T> StringConverter<T> converter(Function<T, String> text) {
        return new StringConverter<>() {
            @Override public String toString(T value) { return value == null ? "" : text.apply(value); }
            @Override public T fromString(String value) { throw new UnsupportedOperationException("Selection only"); }
        };
    }

    private static String time(Instant at) {
        return TIME.format(at);
    }
}
