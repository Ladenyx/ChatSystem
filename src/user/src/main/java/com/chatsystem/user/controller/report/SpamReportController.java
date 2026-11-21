package com.chatsystem.user.controller.report;

import com.chatsystem.desktop.navigation.DialogViewController;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserApplication;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.service.SpamReportService;
import com.chatsystem.user.service.model.ReportEvidenceItem;
import com.chatsystem.user.service.model.ReportMessageItem;
import com.chatsystem.user.service.model.ReportPreparation;
import com.chatsystem.user.service.model.ReportReceipt;
import com.chatsystem.user.service.model.TargetUserOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Controller for User Spam Reporting dialog (U-DLG-04 / P1 / REPORT-GROUP).
 * Complies with strict architectural rule: imports ONLY SpamReportService interface
 * and its models; does not import any Demo or Offline class.
 */
public final class SpamReportController implements DialogViewController {
    public record ReportContext(String conversationId, String conversationTitle) {}

    // STEP 1 Controls
    @FXML private VBox pickPane;
    @FXML private Label pickHeading;
    @FXML private Button pickCloseButton;
    @FXML private Label pickDescription;
    @FXML private ComboBox<TargetUserOption> targetUserCombo;
    @FXML private TextField reasonField;
    @FXML private Label messagesSectionTitle;
    @FXML private ScrollPane messagesScroll;
    @FXML private VBox messagesList;
    @FXML private Label pickFeedback;
    @FXML private Button pickCancelButton;
    @FXML private Button previewButton;

    // STEP 2 Controls
    @FXML private VBox previewPane;
    @FXML private Label previewHeading;
    @FXML private Button previewCloseButton;
    @FXML private Label previewAccountAndReason;
    @FXML private Label previewStats;
    @FXML private Label previewConsentNotice;
    @FXML private ScrollPane evidenceScroll;
    @FXML private VBox evidenceList;
    @FXML private Button editPickButton;
    @FXML private Button submitReportButton;

    // STEP 3 Controls
    @FXML private VBox acceptedPane;
    @FXML private Button acceptedCloseTopButton;
    @FXML private Label receiptIdLabel;
    @FXML private Label receiptTimestampLabel;
    @FXML private Button acceptedDoneButton;

    private final Set<String> selectedMessageIds = new LinkedHashSet<>();
    private SpamReportService service;
    private ReportContext context;
    private ReportPreparation currentPreparation;
    private Dialog<ButtonType> currentDialog;

    public static void open(ViewNavigator navigator, String conversationId, String conversationTitle) {
        var properties = navigator.getRoot().getProperties();
        properties.put(ReportContext.class, new ReportContext(conversationId, conversationTitle));
        try {
            navigator.showModal(UserViews.SPAM_REPORT, true);
        } finally {
            properties.remove(ReportContext.class);
        }
    }

    @FXML
    private void initialize() {
        pickFeedback.managedProperty().bind(pickFeedback.visibleProperty());
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        service = UserApplication.getSpamReportService();
        Object ctx = navigator.getRoot().getProperties().get(ReportContext.class);
        if (ctx instanceof ReportContext rc) {
            context = rc;
        } else {
            context = new ReportContext("conv-group-java", "Nhóm Java");
        }

        String title = context.conversationTitle() != null && !context.conversationTitle().isBlank()
                ? context.conversationTitle() : "Nhóm Java";
        pickHeading.setText("Báo cáo spam — " + title);
        previewHeading.setText("Xem trước báo cáo — " + title);

        setupTargetUsers();
        showStep(pickPane);
    }

    @Override
    public void configureDialog(Dialog<ButtonType> dialog) {
        this.currentDialog = dialog;
        dialog.setTitle(pickHeading.getText());

        var pane = dialog.getDialogPane();
        pane.getStyleClass().add("chat-report-dialog");
        for (String file : List.of("chat-tokens.css", "chat.css", "report.css")) {
            pane.getStylesheets().add(UserApplication.class.getResource("/com/chatsystem/user/css/" + file).toExternalForm());
        }

        // Hide default JavaFX dialog button bar so custom styled action buttons inside FXML take effect
        var buttonBar = pane.lookup(".button-bar");
        if (buttonBar != null) {
            buttonBar.setVisible(false);
            buttonBar.setManaged(false);
        }

        // Hook up dialog close buttons
        pickCloseButton.setOnAction(event -> dialog.close());
        pickCancelButton.setOnAction(event -> dialog.close());
        previewCloseButton.setOnAction(event -> dialog.close());
        acceptedCloseTopButton.setOnAction(event -> dialog.close());
        acceptedDoneButton.setOnAction(event -> dialog.close());

        // Step 1 -> Step 2
        previewButton.setOnAction(event -> onPreviewEvidence());

        // Step 2 -> Step 1
        editPickButton.setOnAction(event -> showStep(pickPane));

        // Step 2 -> Step 3
        submitReportButton.setOnAction(event -> onSubmitReport());
    }

    private void setupTargetUsers() {
        targetUserCombo.getItems().clear();
        List<TargetUserOption> options = service.getTargetUserOptions(context.conversationId());
        targetUserCombo.getItems().addAll(options);
        if (!options.isEmpty()) {
            targetUserCombo.getSelectionModel().selectFirst();
        }
        targetUserCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            loadMessagesForTargetUser(newVal != null ? newVal.id() : null);
        });

        loadMessagesForTargetUser(targetUserCombo.getValue() != null ? targetUserCombo.getValue().id() : null);
    }

    private void loadMessagesForTargetUser(String targetUserId) {
        selectedMessageIds.clear();
        messagesList.getChildren().clear();
        pickFeedback.setVisible(false);

        List<ReportMessageItem> messages = service.getEligibleMessages(context.conversationId(), targetUserId);
        for (int i = 0; i < messages.size(); i++) {
            ReportMessageItem msg = messages.get(i);
            boolean initiallySelected = (i == 0); // Pre-select first message matching Figma spec
            if (initiallySelected) {
                selectedMessageIds.add(msg.id());
            }
            messagesList.getChildren().add(createMessageExcerptRow(msg, initiallySelected));
        }
    }

    private HBox createMessageExcerptRow(ReportMessageItem item, boolean initiallyChecked) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(initiallyChecked);
        checkBox.setOnAction(event -> {
            if (checkBox.isSelected()) {
                selectedMessageIds.add(item.id());
            } else {
                selectedMessageIds.remove(item.id());
            }
            pickFeedback.setVisible(false);
        });

        Label senderLabel = new Label(item.senderName());
        senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: -chat-text;");

        Label timeLabel = new Label(" · " + item.formattedTime());
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: -chat-muted;");

        HBox senderTimeBox = new HBox(senderLabel, timeLabel);
        senderTimeBox.setAlignment(Pos.CENTER_LEFT);

        Label classLabel = new Label("Tin được báo cáo");
        classLabel.getStyleClass().add("chat-report-classification");

        Label contentLabel = new Label(item.content());
        contentLabel.getStyleClass().add("chat-report-content");
        contentLabel.setWrapText(true);

        VBox detailsBox = new VBox(2, senderTimeBox, classLabel, contentLabel);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);

        HBox card = new HBox(12, checkBox, detailsBox);
        card.getStyleClass().add("chat-report-excerpt-card");
        return card;
    }

    private void onPreviewEvidence() {
        TargetUserOption target = targetUserCombo.getValue();
        String targetId = target != null ? target.id() : "";
        String targetName = target != null ? target.displayName() : "Tài khoản";
        String reason = reasonField.getText();

        try {
            currentPreparation = service.prepareReport(
                    context.conversationId(),
                    context.conversationTitle(),
                    targetId,
                    targetName,
                    reason,
                    selectedMessageIds
            );

            // Populate Step 2 preview
            previewAccountAndReason.setText("Tài khoản: " + currentPreparation.targetUserName()
                    + ". Lý do: " + currentPreparation.reason());
            previewStats.setText(currentPreparation.selectedCount() + " tin báo cáo · "
                    + currentPreparation.contextCount() + " tin ngữ cảnh · Giao "
                    + currentPreparation.intersectionCount() + " · Tổng "
                    + currentPreparation.totalUniqueCount() + " tin duy nhất.");
            previewConsentNotice.setText(currentPreparation.consentNotice());

            renderEvidenceList(currentPreparation.evidence());
            showStep(previewPane);
        } catch (IllegalArgumentException ex) {
            pickFeedback.setText(ex.getMessage());
            pickFeedback.setVisible(true);
        }
    }

    private void renderEvidenceList(List<ReportEvidenceItem> evidence) {
        evidenceList.getChildren().clear();
        for (ReportEvidenceItem item : evidence) {
            Label senderLabel = new Label(item.senderName());
            senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: -chat-text;");

            Label timeLabel = new Label(" · " + item.formattedTime());
            timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: -chat-muted;");

            HBox senderTimeBox = new HBox(senderLabel, timeLabel);
            senderTimeBox.setAlignment(Pos.CENTER_LEFT);

            Label classLabel = new Label(item.classification());
            classLabel.getStyleClass().add("chat-report-classification");

            Label contentLabel = new Label(item.content());
            contentLabel.getStyleClass().add("chat-report-content");
            contentLabel.setWrapText(true);

            VBox detailsBox = new VBox(2, senderTimeBox, classLabel, contentLabel);
            detailsBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(detailsBox, Priority.ALWAYS);

            HBox card = new HBox(detailsBox);
            card.getStyleClass().add("chat-report-excerpt-card");
            evidenceList.getChildren().add(card);
        }
    }

    private void onSubmitReport() {
        if (currentPreparation == null) {
            return;
        }
        ReportReceipt receipt = service.submitReport(currentPreparation);
        receiptIdLabel.setText("Mã hồ sơ: " + receipt.reportId());
        receiptTimestampLabel.setText("Tiếp nhận: " + receipt.timestamp());
        showStep(acceptedPane);
    }

    private void showStep(VBox active) {
        pickPane.setVisible(active == pickPane);
        pickPane.setManaged(active == pickPane);

        previewPane.setVisible(active == previewPane);
        previewPane.setManaged(active == previewPane);

        acceptedPane.setVisible(active == acceptedPane);
        acceptedPane.setManaged(active == acceptedPane);

        if (currentDialog != null) {
            String title = (active == pickPane) ? pickHeading.getText()
                    : (active == previewPane) ? previewHeading.getText()
                    : "Báo cáo đã được tiếp nhận";
            currentDialog.setTitle(title);
        }
    }
}
