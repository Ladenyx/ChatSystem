package admin.ui;

import common.mock.MockData;
import common.model.SpamReport;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.ConfirmDialog;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;

/**
 * Screen #10 (A-10, A-11): Spam Reports moderation screen.
 * Supports filtering by time and username, sorting, resolving, dismissing, and locking reported accounts (A-11).
 */
public class SpamReportsView extends VBox {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    private final ObservableList<SpamReport> masterReports = FXCollections.observableArrayList();
    private final FilteredList<SpamReport> filteredReports = new FilteredList<>(masterReports, p -> true);
    private final SortedList<SpamReport> sortedReports = new SortedList<>(filteredReports);
    private final TableView<SpamReport> table = new TableView<>(sortedReports);

    private final TextField searchField = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final ComboBox<String> sortCombo = new ComboBox<>();

    private final Button lockAccountBtn = new Button("Lock Reported Account");
    private final Button resolveBtn = new Button("Mark Resolved");
    private final Button dismissBtn = new Button("Dismiss Report");

    public SpamReportsView() {
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("admin-content");

        buildHeader();
        buildFilterBar();
        buildTable();
        buildActionBar();

        reloadReports();
        sortedReports.comparatorProperty().bind(table.comparatorProperty());
    }

    private void buildHeader() {
        VBox titleBlock = new VBox(4);
        Label heading = new Label("Spam & Abuse Reports");
        heading.getStyleClass().add("admin-heading");
        Label sub = new Label("Review flagged content and take punitive moderation actions against reported offenders.");
        sub.getStyleClass().add("text-muted");
        titleBlock.getChildren().addAll(heading, sub);
        getChildren().add(titleBlock);
    }

    private void buildFilterBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Filter by reporter, offender or reason...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(280);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        statusFilter.setItems(FXCollections.observableArrayList("All Status", "PENDING", "RESOLVED", "DISMISSED"));
        statusFilter.setValue("All Status");
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        sortCombo.setItems(FXCollections.observableArrayList(
                "Reported: Newest",
                "Reported: Oldest",
                "Offender (A → Z)",
                "Reporter (A → Z)"
        ));
        sortCombo.setValue("Reported: Newest");
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySort(newVal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-ghost");
        refreshBtn.setOnAction(e -> reloadReports());

        bar.getChildren().addAll(searchField, statusFilter, sortCombo, spacer, refreshBtn);
        getChildren().add(bar);
    }

    private void buildTable() {
        table.getStyleClass().add("table-view-clean");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ID column
        TableColumn<SpamReport, Number> idCol = new TableColumn<>("Report ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        idCol.setPrefWidth(75);

        // Reported Time column
        TableColumn<SpamReport, String> timeCol = new TableColumn<>("Reported At");
        timeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getReportedAt() != null ? c.getValue().getReportedAt().format(DATE_FMT) : "—"));
        timeCol.setPrefWidth(150);

        // Reporter column
        TableColumn<SpamReport, SpamReport> reporterCol = new TableColumn<>("Reporter");
        reporterCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        reporterCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(SpamReport item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getReporter() == null) {
                    setGraphic(null);
                } else {
                    User r = item.getReporter();
                    AvatarLabel av = new AvatarLabel(r.getInitials(), false, true);
                    VBox box = new VBox(1, new Label(r.getFullName()), new Label("@" + r.getUsername()));
                    box.getChildren().get(0).getStyleClass().add("text-semibold");
                    box.getChildren().get(1).getStyleClass().add("text-muted");
                    HBox row = new HBox(8, av, box);
                    row.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(row);
                }
            }
        });
        reporterCol.setPrefWidth(180);

        // Reported User (Offender) column
        TableColumn<SpamReport, SpamReport> reportedCol = new TableColumn<>("Reported User");
        reportedCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        reportedCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(SpamReport item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getReported() == null) {
                    setGraphic(null);
                } else {
                    User off = item.getReported();
                    AvatarLabel av = new AvatarLabel(off.getInitials(), false, true);
                    VBox box = new VBox(1, new Label(off.getFullName()), new Label("@" + off.getUsername()));
                    box.getChildren().get(0).getStyleClass().add("text-semibold");
                    box.getChildren().get(1).getStyleClass().add("text-muted");
                    HBox row = new HBox(8, av, box);
                    row.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(row);
                }
            }
        });
        reportedCol.setPrefWidth(180);

        // Reason column
        TableColumn<SpamReport, String> reasonCol = new TableColumn<>("Reason / Allegation");
        reasonCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getReason()));
        reasonCol.setPrefWidth(220);

        // Status column
        TableColumn<SpamReport, SpamReport.Status> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(SpamReport.Status item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.name());
                    if (item == SpamReport.Status.PENDING) badge.getStyleClass().add("badge-unread");
                    else if (item == SpamReport.Status.RESOLVED) badge.getStyleClass().add("badge-active");
                    else badge.getStyleClass().add("badge-offline");
                    setGraphic(badge);
                }
            }
        });
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(idCol, timeCol, reporterCol, reportedCol, reasonCol, statusCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean has = newSel != null;
            lockAccountBtn.setDisable(!has);
            resolveBtn.setDisable(!has);
            dismissBtn.setDisable(!has);
        });

        getChildren().add(table);
    }

    private void buildActionBar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);

        lockAccountBtn.getStyleClass().add("btn-warn");
        lockAccountBtn.setDisable(true);
        lockAccountBtn.setOnAction(e -> handleLockAccount());

        resolveBtn.getStyleClass().add("btn-accent");
        resolveBtn.setDisable(true);
        resolveBtn.setOnAction(e -> handleResolve());

        dismissBtn.getStyleClass().add("btn-ghost");
        dismissBtn.setDisable(true);
        dismissBtn.setOnAction(e -> handleDismiss());

        bar.getChildren().addAll(lockAccountBtn, resolveBtn, dismissBtn);
        getChildren().add(bar);
    }

    private void reloadReports() {
        masterReports.setAll(MockData.SPAM_REPORTS);
        applyFilters();
    }

    private void applyFilters() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String st = statusFilter.getValue();

        filteredReports.setPredicate(report -> {
            boolean matchQuery = q.isEmpty()
                    || (report.getReason() != null && report.getReason().toLowerCase().contains(q))
                    || (report.getReporter() != null && (report.getReporter().getFullName().toLowerCase().contains(q) || report.getReporter().getUsername().toLowerCase().contains(q)))
                    || (report.getReported() != null && (report.getReported().getFullName().toLowerCase().contains(q) || report.getReported().getUsername().toLowerCase().contains(q)));

            boolean matchStatus = true;
            if (st != null && !"All Status".equals(st)) {
                matchStatus = report.getStatus() != null && report.getStatus().name().equalsIgnoreCase(st);
            }

            return matchQuery && matchStatus;
        });
    }

    private void applySort(String criteria) {
        if (criteria == null) return;
        switch (criteria) {
            case "Reported: Newest" -> masterReports.sort((a, b) -> {
                if (a.getReportedAt() == null) return 1;
                if (b.getReportedAt() == null) return -1;
                return b.getReportedAt().compareTo(a.getReportedAt());
            });
            case "Reported: Oldest" -> masterReports.sort((a, b) -> {
                if (a.getReportedAt() == null) return 1;
                if (b.getReportedAt() == null) return -1;
                return a.getReportedAt().compareTo(b.getReportedAt());
            });
            case "Offender (A → Z)" -> masterReports.sort((a, b) -> {
                String u1 = a.getReported() != null ? a.getReported().getFullName() : "";
                String u2 = b.getReported() != null ? b.getReported().getFullName() : "";
                return u1.compareToIgnoreCase(u2);
            });
            case "Reporter (A → Z)" -> masterReports.sort((a, b) -> {
                String u1 = a.getReporter() != null ? a.getReporter().getFullName() : "";
                String u2 = b.getReporter() != null ? b.getReporter().getFullName() : "";
                return u1.compareToIgnoreCase(u2);
            });
        }
    }

    private void handleLockAccount() {
        SpamReport sel = table.getSelectionModel().getSelectedItem();
        if (sel == null || sel.getReported() == null) return;
        User reported = sel.getReported();

        boolean ok = ConfirmDialog.confirm("Lock Reported Account", "Are you sure you want to lock the account of \""
                + reported.getFullName() + "\" (@" + reported.getUsername() + ") and mark this report as Resolved?");
        if (ok) {
            MockData.lockReportedUser(sel.getId());
            table.refresh();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Account Locked");
            alert.setHeaderText(null);
            alert.setContentText("User @" + reported.getUsername() + " has been locked. The spam report has been resolved.");
            alert.showAndWait();
        }
    }

    private void handleResolve() {
        SpamReport sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        MockData.resolveSpamReport(sel.getId());
        table.refresh();
    }

    private void handleDismiss() {
        SpamReport sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        MockData.dismissSpamReport(sel.getId());
        table.refresh();
    }
}

