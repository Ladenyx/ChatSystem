package com.chatsystem.admin.controller.spam;

import com.chatsystem.admin.AdminViews;
import com.chatsystem.admin.navigation.AdminViewInputs;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockSpamReport;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import java.text.Collator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public final class SpamController implements ViewController {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
            .withZone(MockData.DISPLAY_ZONE);
    private static final Collator VIETNAMESE_COLLATOR = Collator.getInstance(Locale.of("vi", "VN"));

    @FXML private DatePicker startDateFilter;
    @FXML private DatePicker endDateFilter;
    @FXML private TextField reportedUserFilter;
    @FXML private ToggleGroup statusFilters;
    @FXML private ToggleButton allReports;
    @FXML private ToggleButton pendingReports;
    @FXML private ToggleButton reviewedReports;
    @FXML private ComboBox<String> sortOrder;

    @FXML private TableView<MockSpamReport> reportsTable;
    @FXML private TableColumn<MockSpamReport, MockSpamReport> reportedUserColumn;
    @FXML private TableColumn<MockSpamReport, MockSpamReport> reporterColumn;
    @FXML private TableColumn<MockSpamReport, Instant> reportedAtColumn;
    @FXML private TableColumn<MockSpamReport, MockSpamReport.Status> statusColumn;
    @FXML private Label reportCount;
    @FXML private Hyperlink clearFilters;

    @FXML private VBox unselectedReport;
    @FXML private ScrollPane reportInspector;
    @FXML private Label reportIdTitle;
    @FXML private Label reportStatusBadge;
    @FXML private Label reportTimestamp;

    @FXML private Label reporterName;
    @FXML private Label reporterUsername;
    @FXML private Label reporterId;

    @FXML private Label reportedName;
    @FXML private Label reportedUsername;
    @FXML private Label reportedId;
    @FXML private Label reportedUserStatus;

    @FXML private Label reportReason;
    @FXML private Label evidenceCountLabel;
    @FXML private VBox chatPreviewList;

    @FXML private Button lockAccountButton;
    @FXML private FontIcon lockIcon;
    @FXML private VBox pendingReviewBox;
    @FXML private TextField reviewNoteField;
    @FXML private Label reviewNoteError;
    @FXML private Button markReviewedButton;

    @FXML private VBox reviewedBox;
    @FXML private Label reviewedHeader;
    @FXML private Label reviewedNoteText;

    private final MockData data = MockData.create();
    private final ObservableList<MockSpamReport> allReportsList = FXCollections.observableArrayList();
    private final FilteredList<MockSpamReport> filteredReports = new FilteredList<>(allReportsList);
    private final Map<String, MockSpamReport.Status> reportStatuses = new HashMap<>();
    private final Map<String, String> reviewNotes = new HashMap<>();
    private final Map<String, Instant> reviewedAtMap = new HashMap<>();
    private final Map<String, MockUser.Status> userStatuses = new HashMap<>();

    private ViewNavigator navigator;

    @FXML
    private void initialize() {
        for (MockSpamReport r : data.spamReports()) {
            reportStatuses.put(r.id(), r.status());
            if (r.status() == MockSpamReport.Status.REVIEWED) {
                reviewNotes.put(r.id(), "Đã đối chiếu với bằng chứng người báo cáo cung cấp và xác minh vi phạm.");
                reviewedAtMap.put(r.id(), r.createdAt().plusSeconds(3600));
            }
        }
        allReportsList.setAll(data.spamReports());

        configureTables();
        configureSortOptions();

        pendingReports.setUserData(MockSpamReport.Status.PENDING);
        reviewedReports.setUserData(MockSpamReport.Status.REVIEWED);

        reportedUserFilter.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        startDateFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        endDateFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        sortOrder.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        statusFilters.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                statusFilters.selectToggle(oldVal);
            } else {
                applyFilter();
            }
        });

        reportCount.textProperty().bind(Bindings.size(filteredReports).asString("%d báo cáo"));
        clearFilters.visibleProperty().bind(reportedUserFilter.textProperty().isNotEmpty()
                .or(startDateFilter.valueProperty().isNotNull())
                .or(endDateFilter.valueProperty().isNotNull())
                .or(allReports.selectedProperty().not())
                .or(sortOrder.getSelectionModel().selectedIndexProperty().isNotEqualTo(0)));
        clearFilters.managedProperty().bind(clearFilters.visibleProperty());

        reportsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, selected) -> showReportDetails(selected));

        applyFilter();
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
    }

    private void configureSortOptions() {
        sortOrder.getItems().setAll(
                "Thời gian gửi (Mới nhất)",
                "Thời gian gửi (Cũ nhất)",
                "Tên bị báo cáo (A → Z)"
        );
        sortOrder.getSelectionModel().select(0);
    }

    private void configureTables() {
        reportedUserColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        reportedUserColumn.setCellFactory(col -> new UserDisplayCell(report -> data.user(report.reportedUserId())));

        reporterColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        reporterColumn.setCellFactory(col -> new UserDisplayCell(report -> data.user(report.reporterId())));

        reportedAtColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().createdAt()));
        reportedAtColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Instant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DATE_TIME.format(item));
                }
            }
        });

        statusColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(reportStatuses.getOrDefault(cell.getValue().id(), cell.getValue().status())));
        statusColumn.setCellFactory(col -> new StatusBadgeCell());
    }

    private void applyFilter() {
        String usernameQuery = reportedUserFilter.getText() == null ? ""
                : reportedUserFilter.getText().trim().toLowerCase(Locale.ROOT);
        LocalDate start = startDateFilter.getValue();
        LocalDate end = endDateFilter.getValue();
        var selectedToggle = statusFilters.getSelectedToggle();
        MockSpamReport.Status targetStatus = selectedToggle == null ? null
                : (MockSpamReport.Status) selectedToggle.getUserData();

        filteredReports.setPredicate(report -> {
            MockSpamReport.Status currentStatus = reportStatuses.getOrDefault(report.id(), report.status());
            if (targetStatus != null && currentStatus != targetStatus) {
                return false;
            }

            MockUser reportedUser = data.user(report.reportedUserId());
            if (!usernameQuery.isEmpty()) {
                boolean matchesUsername = reportedUser.username().toLowerCase(Locale.ROOT).contains(usernameQuery);
                boolean matchesName = reportedUser.fullName().toLowerCase(Locale.ROOT).contains(usernameQuery);
                if (!matchesUsername && !matchesName) {
                    return false;
                }
            }

            LocalDate reportDate = report.createdAt().atZone(MockData.DISPLAY_ZONE).toLocalDate();
            if (start != null && reportDate.isBefore(start)) {
                return false;
            }
            if (end != null && reportDate.isAfter(end)) {
                return false;
            }

            return true;
        });

        Comparator<MockSpamReport> comparator = switch (sortOrder.getSelectionModel().getSelectedIndex()) {
            case 1 -> Comparator.comparing(MockSpamReport::createdAt);
            case 2 -> (a, b) -> VIETNAMESE_COLLATOR.compare(
                    data.user(a.reportedUserId()).username(), data.user(b.reportedUserId()).username());
            default -> Comparator.comparing(MockSpamReport::createdAt).reversed();
        };

        SortedList<MockSpamReport> sorted = new SortedList<>(filteredReports, comparator);
        reportsTable.setItems(sorted);

        MockSpamReport selected = reportsTable.getSelectionModel().getSelectedItem();
        if (selected != null && !sorted.contains(selected)) {
            reportsTable.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void clearFilters() {
        reportedUserFilter.clear();
        startDateFilter.setValue(null);
        endDateFilter.setValue(null);
        allReports.setSelected(true);
        sortOrder.getSelectionModel().select(0);
    }

    private void showReportDetails(MockSpamReport report) {
        boolean hasSelection = report != null;
        unselectedReport.setVisible(!hasSelection);
        unselectedReport.setManaged(!hasSelection);
        reportInspector.setVisible(hasSelection);
        reportInspector.setManaged(hasSelection);

        if (!hasSelection) {
            return;
        }

        MockSpamReport.Status currentStatus = reportStatuses.getOrDefault(report.id(), report.status());
        reportIdTitle.setText("Báo cáo #" + report.id());
        updateStatusBadge(reportStatusBadge, currentStatus);
        reportTimestamp.setText("Thời điểm gửi: " + DATE_TIME.format(report.createdAt()));

        MockUser reporter = data.user(report.reporterId());
        reporterName.setText(reporter.fullName());
        reporterUsername.setText("@" + reporter.username());
        reporterId.setText("ID: " + reporter.id());

        MockUser reported = data.user(report.reportedUserId());
        reportedName.setText(reported.fullName());
        reportedUsername.setText("@" + reported.username());
        reportedId.setText("ID: " + reported.id());

        MockUser.Status userStatus = userStatuses.getOrDefault(reported.id(), reported.status());
        updateUserStatusBadge(reportedUserStatus, userStatus);

        reportReason.setText(report.reason());

        // Update Lock button state (A-11)
        boolean isLocked = userStatus == MockUser.Status.LOCKED;
        lockAccountButton.setDisable(isLocked);
        lockAccountButton.setText(isLocked ? "Tài khoản đã bị khóa" : "Khóa tài khoản bị báo cáo");
        lockIcon.setIconCode(isLocked ? Feather.CHECK : Feather.LOCK);

        // Update Review section (DEC-10)
        boolean isReviewed = currentStatus == MockSpamReport.Status.REVIEWED;
        pendingReviewBox.setVisible(!isReviewed);
        pendingReviewBox.setManaged(!isReviewed);
        reviewedBox.setVisible(isReviewed);
        reviewedBox.setManaged(isReviewed);

        reviewNoteField.clear();
        reviewNoteError.setVisible(false);
        reviewNoteError.setManaged(false);

        if (isReviewed) {
            Instant reviewedAt = reviewedAtMap.getOrDefault(report.id(), report.createdAt().plusSeconds(3600));
            reviewedHeader.setText("Đã xử lý bởi Admin hệ thống lúc " + DATE_TIME.format(reviewedAt));
            reviewedNoteText.setText("Ghi chú: " + reviewNotes.getOrDefault(report.id(), "Đã xem xét và xử lý."));
        }

        // Render chat preview evidence
        renderChatPreview(report);

        reportInspector.setVvalue(0);
    }

    private void renderChatPreview(MockSpamReport report) {
        chatPreviewList.getChildren().clear();
        List<MockSpamReport.Evidence> evidenceList = report.evidence();
        evidenceCountLabel.setText("Bằng chứng đính kèm (" + evidenceList.size() + " tin nhắn)");

        for (MockSpamReport.Evidence item : evidenceList) {
            MockMessage msg = item.message();
            MockUser sender = data.user(msg.senderId());
            boolean isReportedUser = sender.id().equals(report.reportedUserId());

            VBox bubbleCard = new VBox(4);
            bubbleCard.getStyleClass().add("admin-chat-bubble-card");
            if (item.selected()) {
                bubbleCard.getStyleClass().add("bubble-selected");
            } else {
                bubbleCard.getStyleClass().add("bubble-context");
            }

            // Top row: Sender Name + Timestamp + Badge
            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);

            Label senderLabel = new Label(sender.fullName() + " (@" + sender.username() + ")");
            senderLabel.getStyleClass().add("admin-bubble-sender");
            if (isReportedUser) {
                senderLabel.getStyleClass().add("sender-reported");
            }

            Label timeLabel = new Label(DATE_TIME.format(msg.sentAt()));
            timeLabel.getStyleClass().add("admin-bubble-time");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label tag = new Label();
            if (item.selected()) {
                tag.setText("Bị báo cáo");
                tag.getStyleClass().setAll("label", "admin-evidence-tag", "tag-selected");
            } else {
                tag.setText("Ngữ cảnh");
                tag.getStyleClass().setAll("label", "admin-evidence-tag", "tag-context");
            }

            header.getChildren().addAll(senderLabel, timeLabel, spacer, tag);

            // Message text
            Label content = new Label(msg.text());
            content.setWrapText(true);
            content.getStyleClass().add("admin-bubble-text");

            bubbleCard.getChildren().addAll(header, content);
            chatPreviewList.getChildren().add(bubbleCard);
        }
    }

    @FXML
    private void openLockConfirmation() {
        MockSpamReport report = reportsTable.getSelectionModel().getSelectedItem();
        if (report == null || navigator == null) {
            return;
        }
        MockUser user = data.user(report.reportedUserId());
        Dialog<ButtonType> dialog = navigator.showModal(AdminViews.CONFIRM_ACTION,
                new AdminViewInputs.ConfirmAction(AdminViewInputs.Action.LOCK_USER, user.id()));

        dialog.setOnHidden(event -> {
            ButtonType result = dialog.getResult();
            if (result != null && result.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                userStatuses.put(user.id(), MockUser.Status.LOCKED);
                showReportDetails(reportsTable.getSelectionModel().getSelectedItem());
            }
        });
    }

    @FXML
    private void markReviewed() {
        MockSpamReport report = reportsTable.getSelectionModel().getSelectedItem();
        if (report == null) {
            return;
        }
        String note = reviewNoteField.getText() == null ? "" : reviewNoteField.getText().trim();
        if (note.isEmpty()) {
            reviewNoteError.setText("Vui lòng nhập ghi chú xử lý (bắt buộc theo DEC-10)");
            reviewNoteError.setVisible(true);
            reviewNoteError.setManaged(true);
            return;
        }

        reportStatuses.put(report.id(), MockSpamReport.Status.REVIEWED);
        reviewNotes.put(report.id(), note);
        reviewedAtMap.put(report.id(), Instant.now());

        reportsTable.refresh();
        showReportDetails(report);
    }

    private static void updateStatusBadge(Label badge, MockSpamReport.Status status) {
        boolean isReviewed = status == MockSpamReport.Status.REVIEWED;
        badge.setText(isReviewed ? "Đã xử lý" : "Chờ xử lý");
        badge.getStyleClass().setAll("label", "admin-user-status",
                isReviewed ? "status-reviewed" : "status-pending");
        if (badge.getGraphic() == null) {
            var dot = new Region();
            dot.getStyleClass().add("admin-status-dot");
            badge.setGraphic(dot);
        }
    }

    private static void updateUserStatusBadge(Label badge, MockUser.Status status) {
        String text = switch (status) {
            case ACTIVE -> "Hoạt động";
            case LOCKED -> "Bị khóa";
            case DELETED -> "Xóa mềm";
        };
        badge.setText(text);
        badge.getStyleClass().setAll("label", "admin-user-status",
                "status-" + status.name().toLowerCase(Locale.ROOT));
        if (badge.getGraphic() == null) {
            var dot = new Region();
            dot.getStyleClass().add("admin-status-dot");
            badge.setGraphic(dot);
        }
    }

    private static final class UserDisplayCell extends TableCell<MockSpamReport, MockSpamReport> {
        private final java.util.function.Function<MockSpamReport, MockUser> userExtractor;
        private final Label name = new Label();
        private final Label handle = new Label();
        private final VBox layout = new VBox(2, name, handle);
        private final Tooltip tooltip = new Tooltip();

        private UserDisplayCell(java.util.function.Function<MockSpamReport, MockUser> userExtractor) {
            this.userExtractor = userExtractor;
            name.getStyleClass().add("admin-user-name");
            handle.getStyleClass().add("caption");
            layout.setMinWidth(0);
        }

        @Override
        protected void updateItem(MockSpamReport report, boolean empty) {
            super.updateItem(report, empty);
            if (empty || report == null) {
                setGraphic(null);
                setTooltip(null);
                setAccessibleText(null);
            } else {
                MockUser user = userExtractor.apply(report);
                name.setText(user.fullName());
                handle.setText("@" + user.username());
                tooltip.setText(user.fullName() + " (@" + user.username() + ")");
                setTooltip(tooltip);
                setAccessibleText(user.fullName() + " @" + user.username());
                setGraphic(layout);
            }
        }
    }

    private static final class StatusBadgeCell extends TableCell<MockSpamReport, MockSpamReport.Status> {
        private final Label badge = new Label();

        private StatusBadgeCell() {
            badge.getStyleClass().add("admin-user-status");
            var dot = new Region();
            dot.getStyleClass().add("admin-status-dot");
            badge.setGraphic(dot);
        }

        @Override
        protected void updateItem(MockSpamReport.Status status, boolean empty) {
            super.updateItem(status, empty);
            if (empty || status == null) {
                setGraphic(null);
            } else {
                updateStatusBadge(badge, status);
                setGraphic(badge);
            }
        }
    }
}
