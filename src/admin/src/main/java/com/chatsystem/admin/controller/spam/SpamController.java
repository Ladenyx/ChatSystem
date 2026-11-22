package com.chatsystem.admin.controller.spam;

import com.chatsystem.admin.AdminViews;
import com.chatsystem.admin.mock.SpamModerationPreview;
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
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public final class SpamController implements ViewController {
    private static final int MAX_INLINE_REPORTED = 3;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm", Locale.ROOT)
            .withZone(MockData.DISPLAY_ZONE);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)
            .withZone(MockData.DISPLAY_ZONE);
    private static final DateTimeFormatter TABLE_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/uuuu'\n'HH:mm", Locale.ROOT)
            .withZone(MockData.DISPLAY_ZONE);
    private static final Collator VIETNAMESE_COLLATOR = Collator.getInstance(Locale.of("vi", "VN"));
    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
    private static final String DATE_ERROR = "Nhập ngày hợp lệ theo định dạng dd/mm/yyyy";

    @FXML private DatePicker startDateFilter;
    @FXML private DatePicker endDateFilter;
    @FXML private Label spamDateError;
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
    @FXML private Label reporterIdentity;

    @FXML private Label reportedName;
    @FXML private Label reportedIdentity;
    @FXML private Label reportedUserStatus;

    @FXML private Label reportReason;
    @FXML private Label evidenceCountLabel;
    @FXML private Button openEvidenceButton;
    @FXML private VBox chatPreviewList;

    @FXML private Button lockAccountButton;
    @FXML private FontIcon lockIcon;
    @FXML private Label lockAccountNotice;
    @FXML private VBox pendingReviewBox;
    @FXML private TextField reviewNoteField;
    @FXML private Label reviewNoteError;

    @FXML private VBox reviewedBox;
    @FXML private Label reviewedHeader;
    @FXML private Label reviewedNoteText;

    private final SpamModerationPreview preview = new SpamModerationPreview();
    private final FilteredList<MockSpamReport> filteredReports =
            new FilteredList<>(FXCollections.observableArrayList(preview.reports()));
    private final SortedList<MockSpamReport> sortedReports = new SortedList<>(filteredReports);
    private DateRange appliedDates = new DateRange(null, null);

    private ViewNavigator navigator;
    private Dialog<ButtonType> evidenceDialog;

    @FXML
    private void initialize() {
        configureTables();
        configureSortOptions();
        configureDatePicker(startDateFilter);
        configureDatePicker(endDateFilter);

        pendingReports.setUserData(MockSpamReport.Status.PENDING);
        reviewedReports.setUserData(MockSpamReport.Status.REVIEWED);

        reportedUserFilter.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
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
                .or(startDateFilter.getEditor().textProperty().isNotEmpty())
                .or(endDateFilter.getEditor().textProperty().isNotEmpty())
                .or(allReports.selectedProperty().not())
                .or(sortOrder.getSelectionModel().selectedIndexProperty().isNotEqualTo(0)));
        clearFilters.managedProperty().bind(clearFilters.visibleProperty());
        spamDateError.managedProperty().bind(spamDateError.visibleProperty());
        lockAccountNotice.managedProperty().bind(lockAccountNotice.visibleProperty());
        reviewNoteField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                reviewNoteField.pseudoClassStateChanged(INVALID, false);
                reviewNoteError.setVisible(false);
                reviewNoteError.setManaged(false);
            }
        });

        reportsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, selected) -> showReportDetails(selected));

        applyFilter();
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
    }

    @Override
    public void onLeave() {
        navigator = null;
        if (evidenceDialog != null) {
            evidenceDialog.close();
            evidenceDialog = null;
        }
    }

    private void configureSortOptions() {
        sortOrder.getItems().setAll(
                "Mới nhất",
                "Cũ nhất",
                "Username A–Z",
                "Username Z–A"
        );
        sortOrder.getSelectionModel().select(0);
    }

    private void configureTables() {
        reportedUserColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        reportedUserColumn.setCellFactory(col -> new ReportIdentityCell(preview));

        reporterColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        reporterColumn.setCellFactory(col -> new UserDisplayCell(report -> preview.user(report.reporterId())));
        // The inspector still shows the reporter when the directory does not have room for all four columns.
        double allColumnsWidth = reportsTable.getColumns().stream().mapToDouble(TableColumn::getMinWidth).sum();
        reporterColumn.visibleProperty().bind(reportsTable.widthProperty().greaterThanOrEqualTo(allColumnsWidth + 16));

        reportedAtColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().createdAt()));
        reportedAtColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Instant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setAccessibleText(null);
                } else {
                    setText(TABLE_DATE_TIME.format(item));
                    setAccessibleText(DATE_TIME.format(item));
                }
            }
        });

        statusColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(preview.status(cell.getValue())));
        statusColumn.setCellFactory(col -> new StatusBadgeCell());
        reportsTable.setItems(sortedReports);
    }

    private void configureDatePicker(DatePicker picker) {
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : DATE.format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                try {
                    return parseDate(text);
                } catch (DateTimeParseException invalid) {
                    // Keep the committed value when JavaFX commits on blur; the editor retains the invalid draft.
                    return picker.getValue();
                }
            }
        });

        picker.addEventFilter(KeyEvent.ANY, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    parseDate(picker.getEditor().getText());
                } catch (DateTimeParseException invalid) {
                    // Don't let the skin replace an invalid draft with the last committed value on Enter.
                    event.consume();
                }
            }
        });
        picker.getEditor().textProperty().addListener((obs, previous, current) -> applyDateRange());
        picker.valueProperty().addListener((obs, previous, current) -> {
            // Calendar choices and setValue need the same text-driven validation as typed dates.
            picker.getEditor().setText(picker.getConverter().toString(current));
        });
    }

    private static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String date = text.strip();
        if (!date.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) {
            throw new DateTimeParseException(DATE_ERROR, date, 0);
        }
        LocalDate parsed = LocalDate.parse(date, DATE);
        if (parsed.getYear() == 0) {
            throw new DateTimeParseException(DATE_ERROR, date, 6);
        }
        return parsed;
    }

    private void applyDateRange() {
        LocalDate start;
        LocalDate end;
        try {
            start = parseDate(startDateFilter.getEditor().getText());
        } catch (DateTimeParseException invalid) {
            showDateError(DATE_ERROR, true, false);
            return;
        }
        try {
            end = parseDate(endDateFilter.getEditor().getText());
        } catch (DateTimeParseException invalid) {
            showDateError(DATE_ERROR, false, true);
            return;
        }
        if (start != null && end != null && start.isAfter(end)) {
            showDateError("Từ ngày phải trước hoặc bằng Đến ngày", true, true);
            return;
        }
        showDateError("", false, false);
        appliedDates = new DateRange(start, end);
        applyFilter();
    }

    private void showDateError(String message, boolean invalidStart, boolean invalidEnd) {
        spamDateError.setText(message);
        spamDateError.setVisible(invalidStart || invalidEnd);
        startDateFilter.pseudoClassStateChanged(INVALID, invalidStart);
        endDateFilter.pseudoClassStateChanged(INVALID, invalidEnd);
    }

    private void applyFilter() {
        String query = reportedUserFilter.getText() == null ? ""
                : reportedUserFilter.getText().strip().toLowerCase(Locale.ROOT);
        String usernameQuery = query.startsWith("@") ? query.substring(1) : query;
        var selectedToggle = statusFilters.getSelectedToggle();
        MockSpamReport.Status targetStatus = selectedToggle == null ? null
                : (MockSpamReport.Status) selectedToggle.getUserData();
        var selection = reportsTable.getSelectionModel();
        MockSpamReport selected = selection.getSelectedItem();

        filteredReports.setPredicate(report ->
                (targetStatus == null || preview.status(report) == targetStatus)
                && preview.user(report.reportedUserId()).username().toLowerCase(Locale.ROOT).contains(usernameQuery)
                && appliedDates.includes(report.createdAt().atZone(MockData.DISPLAY_ZONE).toLocalDate()));

        Comparator<MockSpamReport> byUsername = Comparator.comparing(
                report -> preview.user(report.reportedUserId()).username(), VIETNAMESE_COLLATOR);
        sortedReports.setComparator(switch (sortOrder.getSelectionModel().getSelectedIndex()) {
            case 1 -> Comparator.comparing(MockSpamReport::createdAt);
            case 2 -> byUsername;
            case 3 -> byUsername.reversed();
            default -> Comparator.comparing(MockSpamReport::createdAt).reversed();
        });

        if (selected != null && sortedReports.contains(selected)) {
            selection.select(selected);
        } else {
            selection.clearSelection();
        }
    }

    private record DateRange(LocalDate start, LocalDate end) {
        boolean includes(LocalDate date) {
            return (start == null || !date.isBefore(start)) && (end == null || !date.isAfter(end));
        }
    }

    @FXML
    private void clearFilters() {
        reportedUserFilter.clear();
        startDateFilter.setValue(null);
        endDateFilter.setValue(null);
        startDateFilter.getEditor().clear();
        endDateFilter.getEditor().clear();
        allReports.setSelected(true);
        sortOrder.getSelectionModel().select(0);
    }

    private void showReportDetails(MockSpamReport report) {
        if (evidenceDialog != null) {
            evidenceDialog.close();
            evidenceDialog = null;
        }
        boolean hasSelection = report != null;
        unselectedReport.setVisible(!hasSelection);
        unselectedReport.setManaged(!hasSelection);
        reportInspector.setVisible(hasSelection);
        reportInspector.setManaged(hasSelection);

        if (!hasSelection) {
            return;
        }

        reportIdTitle.setText("Báo cáo #" + report.id());
        updateStatusBadge(reportStatusBadge, preview.status(report));
        var submitted = report.createdAt().atZone(MockData.DISPLAY_ZONE);
        reportTimestamp.setText("Gửi lúc " + TIME.format(submitted) + " ngày " + DATE.format(submitted));

        MockUser reporter = preview.user(report.reporterId());
        reporterName.setText(reporter.fullName());
        reporterIdentity.setText("@" + reporter.username() + " · ID " + reporter.id());

        MockUser reported = preview.user(report.reportedUserId());
        reportedName.setText(reported.fullName());
        reportedIdentity.setText("@" + reported.username() + " · ID " + reported.id());

        reportReason.setText(report.reason());
        updateLockAction(report);

        var review = preview.review(report);
        boolean isReviewed = review != null;
        pendingReviewBox.setVisible(!isReviewed);
        pendingReviewBox.setManaged(!isReviewed);
        reviewedBox.setVisible(isReviewed);
        reviewedBox.setManaged(isReviewed);

        reviewNoteField.clear();
        reviewNoteField.pseudoClassStateChanged(INVALID, false);
        reviewNoteError.setVisible(false);
        reviewNoteError.setManaged(false);

        if (isReviewed) {
            reviewedHeader.setText("Admin hệ thống đã xử lý lúc " + DATE_TIME.format(review.reviewedAt()));
            reviewedNoteText.setText("Ghi chú: " + review.note());
        }

        long selectedCount = report.evidence().stream().filter(MockSpamReport.Evidence::selected).count();
        evidenceCountLabel.setText(report.evidence().isEmpty() ? "Chưa có tin nhắn đính kèm"
                : selectedCount + " tin bị báo cáo · " + report.evidence().size() + " tin đính kèm");
        openEvidenceButton.setDisable(report.evidence().isEmpty());
        openEvidenceButton.setText(report.evidence().isEmpty() ? "Xem hội thoại"
                : "Xem hội thoại (" + report.evidence().size() + ")");
        renderReportedPreview(report);
        reportInspector.setVvalue(0);
    }

    private void updateLockAction(MockSpamReport report) {
        MockUser target = preview.user(report.reportedUserId());
        MockUser.Status status = preview.accountStatus(target);
        boolean locked = status == MockUser.Status.LOCKED;
        updateUserStatusBadge(reportedUserStatus, status);
        lockAccountButton.setDisable(!preview.canLockReportedAccount(report));
        lockAccountButton.setText(locked ? "Tài khoản đã bị khóa" : "Khóa tài khoản bị báo cáo");
        lockIcon.setIconCode(locked ? Feather.CHECK : Feather.LOCK);
        String notice = target.role() == MockUser.Role.ADMIN ? "Không khóa tài khoản Admin hệ thống từ báo cáo"
                : status == MockUser.Status.DELETED ? "Không khóa tài khoản đã xóa mềm" : "";
        lockAccountNotice.setText(notice);
        lockAccountNotice.setVisible(!notice.isEmpty());
    }

    private void renderReportedPreview(MockSpamReport report) {
        chatPreviewList.getChildren().clear();
        List<MockSpamReport.Evidence> selected = report.evidence().stream()
                .filter(MockSpamReport.Evidence::selected)
                .toList();
        int shown = Math.min(MAX_INLINE_REPORTED, selected.size());
        for (int index = 0; index < shown; index++) {
            chatPreviewList.getChildren().add(reportedCard(selected.get(index)));
        }
        if (selected.size() > MAX_INLINE_REPORTED) {
            int remaining = selected.size() - MAX_INLINE_REPORTED;
            Hyperlink more = new Hyperlink("+ " + remaining + " tin bị báo cáo khác — Xem trong hội thoại");
            more.setOnAction(event -> openEvidenceConversation());
            chatPreviewList.getChildren().add(more);
        }
    }

    private VBox reportedCard(MockSpamReport.Evidence item) {
        MockMessage message = item.message();
        MockUser sender = preview.user(message.senderId());

        Label flagged = new Label("Tin bị báo cáo", FontIcon.of(Feather.FLAG, 12));
        flagged.getStyleClass().add("tag-selected");
        flagged.setMinWidth(Region.USE_PREF_SIZE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label time = new Label(DATE_TIME.format(message.sentAt()));
        time.getStyleClass().add("admin-bubble-time");
        time.setMinWidth(Region.USE_PREF_SIZE);

        HBox topRow = new HBox(8, flagged, spacer, time);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label author = wrappingLabel(sender.fullName() + " · @" + sender.username(), "admin-bubble-sender");
        Label body = wrappingLabel(message.text(), "admin-bubble-text");

        Label cue = new Label("Nhấn để xem đầy đủ mạch hội thoại →");
        cue.getStyleClass().add("admin-reported-cue");

        VBox card = new VBox(6, topRow, author, body, cue);
        card.setMinWidth(0);
        card.getStyleClass().addAll("admin-reported-card", "bubble-selected");
        card.setFocusTraversable(true);
        card.setAccessibleText("Tin bị báo cáo từ " + sender.fullName() + ": " + message.text());
        card.setOnMouseClicked(event -> openEvidenceConversationFor(message.id()));
        card.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                openEvidenceConversationFor(message.id());
                event.consume();
            }
        });
        return card;
    }

    @FXML
    private void openEvidenceConversation() {
        openEvidenceConversationFor(null);
    }

    private void openEvidenceConversationFor(String focusMessageId) {
        MockSpamReport report = reportsTable.getSelectionModel().getSelectedItem();
        if (report == null || navigator == null || report.evidence().isEmpty()) {
            return;
        }
        if (evidenceDialog != null) {
            evidenceDialog.close();
            evidenceDialog = null;
        }
        Window owner = reportsTable.getScene() == null ? null : reportsTable.getScene().getWindow();
        Dialog<ButtonType> dialog = EvidenceConversationDialog.create(
                owner, navigator.themes(), preview, report, focusMessageId);
        dialog.addEventHandler(DialogEvent.DIALOG_HIDDEN, event -> {
            if (evidenceDialog == dialog) {
                evidenceDialog = null;
            }
        });
        evidenceDialog = dialog;
        dialog.show();
    }

    private static Label wrappingLabel(String text, String style) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.getStyleClass().add(style);
        return label;
    }

    @FXML
    private void openLockConfirmation() {
        MockSpamReport report = reportsTable.getSelectionModel().getSelectedItem();
        if (report == null || navigator == null || !preview.canLockReportedAccount(report)) {
            return;
        }
        Dialog<ButtonType> dialog = navigator.showModal(AdminViews.CONFIRM_ACTION,
                new AdminViewInputs.ConfirmAction(AdminViewInputs.Action.LOCK_REPORTED_USER, report.reportedUserId()));

        dialog.addEventHandler(DialogEvent.DIALOG_HIDDEN, event -> {
            ButtonType result = dialog.getResult();
            if (result != null && result.getButtonData() == ButtonBar.ButtonData.OK_DONE
                    && preview.lockReportedAccount(report)) {
                MockSpamReport selected = reportsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    updateLockAction(selected);
                }
            }
        });
    }

    @FXML
    private void markReviewed() {
        MockSpamReport report = reportsTable.getSelectionModel().getSelectedItem();
        if (report == null || preview.status(report) != MockSpamReport.Status.PENDING) {
            return;
        }
        String note = reviewNoteField.getText() == null ? "" : reviewNoteField.getText().strip();
        if (note.isEmpty()) {
            reviewNoteField.pseudoClassStateChanged(INVALID, true);
            reviewNoteError.setText("Nhập ghi chú trước khi đánh dấu đã xử lý");
            reviewNoteError.setVisible(true);
            reviewNoteError.setManaged(true);
            reviewNoteField.requestFocus();
            return;
        }

        preview.markReviewed(report, note);
        applyFilter();
        reportsTable.refresh();
        showReportDetails(reportsTable.getSelectionModel().getSelectedItem());
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

    private static final class ReportIdentityCell extends TableCell<MockSpamReport, MockSpamReport> {
        private final SpamModerationPreview preview;
        private final Label handle = new Label();
        private final Label reportId = new Label();
        private final VBox layout = new VBox(2, handle, reportId);
        private final Tooltip tooltip = new Tooltip();

        private ReportIdentityCell(SpamModerationPreview preview) {
            this.preview = preview;
            handle.getStyleClass().add("admin-user-name");
            reportId.getStyleClass().add("caption");
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
                MockUser target = preview.user(report.reportedUserId());
                handle.setText("@" + target.username());
                reportId.setText(report.id());
                String identity = target.fullName() + " (@" + target.username() + ") · " + report.id();
                tooltip.setText(identity);
                setTooltip(tooltip);
                setAccessibleText(identity);
                setGraphic(layout);
            }
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
            name.getStyleClass().add("admin-cell-secondary-name");
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
