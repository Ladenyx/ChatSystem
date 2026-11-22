package com.chatsystem.user.controller.report;

import com.chatsystem.desktop.navigation.DialogViewController;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserApplication;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.navigation.UserViewInputs;
import com.chatsystem.user.service.SpamReportService;
import com.chatsystem.user.service.model.ReportEvidenceItem;
import com.chatsystem.user.service.model.ReportMessageItem;
import com.chatsystem.user.service.model.ReportPreparation;
import com.chatsystem.user.service.model.ReportReceipt;
import com.chatsystem.user.service.model.TargetUserOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
 */
public final class SpamReportController implements DialogViewController {
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

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
    @FXML private Label receiptStatusLabel;
    @FXML private Label receiptTimestampLabel;
    @FXML private Button acceptedDoneButton;

    private final Set<String> selectedMessageIds = new LinkedHashSet<>();
    private List<String> preselectedIds = List.of();
    private SpamReportService service;
    private ReportContext context;
    private ReportPreparation currentPreparation;
    private Dialog<ButtonType> currentDialog;

    public static void open(ViewNavigator navigator, String conversationId, String conversationTitle) {
        String id = conversationId != null ? conversationId : "";
        navigator.showModal(UserViews.REPORT_SPAM, new UserViewInputs.ReportSpam(id, List.of()), true);
    }

    @FXML
    private void initialize() {
        for (Node node : List.of(pickDescription, pickFeedback)) {
            node.managedProperty().bind(node.visibleProperty());
        }
        targetUserCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (context != null && service != null) {
                loadMessagesForTargetUser(newVal != null ? newVal.id() : null);
            }
        });
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        service = UserApplication.getSpamReportService();
        if (input instanceof UserViewInputs.ReportSpam reportInput) {
            preselectedIds = reportInput.selectedMessageIds();
            String id = reportInput.conversationId();
            String title = UserViews.CONVERSATIONS_DATA.conversations().stream()
                    .filter(chat -> chat.id().equals(id))
                    .map(chat -> chat.title())
                    .findFirst()
                    .orElse("Hội thoại");
            context = new ReportContext(id, title);
        } else if (navigator.getRoot().getProperties().get(ReportContext.class) instanceof ReportContext rc) {
            preselectedIds = List.of();
            context = rc;
        } else {
            preselectedIds = List.of();
            context = new ReportContext("conv-group-java", "Nhóm Java");
        }

        String title = context.conversationTitle() != null && !context.conversationTitle().isBlank()
                ? context.conversationTitle() : "Hội thoại";
        pickHeading.setText("Báo cáo spam · " + title);
        previewHeading.setText("Xem trước bằng chứng · " + title);

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
            String sheet = UserApplication.class.getResource("/com/chatsystem/user/css/" + file).toExternalForm();
            if (!pane.getStylesheets().contains(sheet)) {
                pane.getStylesheets().add(sheet);
            }
        }

        var buttonBar = pane.lookup(".button-bar");
        if (buttonBar != null) {
            buttonBar.setVisible(false);
            buttonBar.setManaged(false);
        }

        pickCloseButton.setOnAction(event -> dialog.close());
        pickCancelButton.setOnAction(event -> dialog.close());
        previewCloseButton.setOnAction(event -> dialog.close());
        acceptedCloseTopButton.setOnAction(event -> dialog.close());
        acceptedDoneButton.setOnAction(event -> dialog.close());

        previewButton.setOnAction(event -> onPreviewEvidence());
        editPickButton.setOnAction(event -> showStep(pickPane));
        submitReportButton.setOnAction(event -> onSubmitReport());
    }

    private void setupTargetUsers() {
        targetUserCombo.getItems().clear();
        List<TargetUserOption> options = service.getTargetUserOptions(context.conversationId());
        targetUserCombo.getItems().addAll(options);
        if (!options.isEmpty()) {
            targetUserCombo.getSelectionModel().selectFirst();
        } else {
            loadMessagesForTargetUser(null);
        }
    }

    private void loadMessagesForTargetUser(String targetUserId) {
        selectedMessageIds.clear();
        messagesList.getChildren().clear();
        pickFeedback.setVisible(false);

        if (targetUserId == null || targetUserId.isBlank()) {
            return;
        }
        List<ReportMessageItem> messages = service.getEligibleMessages(context.conversationId(), targetUserId);
        for (int i = 0; i < messages.size(); i++) {
            ReportMessageItem msg = messages.get(i);
            boolean initiallySelected = !preselectedIds.isEmpty()
                    ? preselectedIds.contains(msg.id())
                    : (i == 0);
            if (initiallySelected) {
                selectedMessageIds.add(msg.id());
            }
            messagesList.getChildren().add(createMessageExcerptRow(msg, initiallySelected));
        }
        if (messages.isEmpty()) {
            Label empty = new Label("Không có tin nhắn hợp lệ nào từ thành viên này.");
            empty.getStyleClass().add("chat-caption");
            empty.setWrapText(true);
            messagesList.getChildren().add(empty);
        }
        messagesScroll.setVvalue(0);
    }

    private HBox createMessageExcerptRow(ReportMessageItem item, boolean initiallyChecked) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(initiallyChecked);
        checkBox.setAccessibleText("Chọn tin nhắn của " + item.senderName());

        VBox detailsBox = buildDetailsBox(item.senderName(), item.formattedTime(),
                null, item.content());

        HBox card = new HBox(12, checkBox, detailsBox);
        card.getStyleClass().add("chat-report-excerpt-card");
        card.pseudoClassStateChanged(SELECTED, initiallyChecked);

        checkBox.setOnAction(event -> {
            boolean checked = checkBox.isSelected();
            if (checked) {
                selectedMessageIds.add(item.id());
            } else {
                selectedMessageIds.remove(item.id());
            }
            card.pseudoClassStateChanged(SELECTED, checked);
            pickFeedback.setVisible(false);
        });
        card.setOnMouseClicked(event -> {
            for (Node node = event.getTarget() instanceof Node n ? n : null;
                 node != null && node != card;
                 node = node.getParent()) {
                if (node == checkBox) {
                    return;
                }
            }
            checkBox.fire();
        });
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
                    selectedMessageIds);

            previewAccountAndReason.setText("Tài khoản: " + currentPreparation.targetUserName()
                    + " · Lý do: " + currentPreparation.reason());
            previewStats.setText(currentPreparation.selectedCount() + " tin báo cáo · "
                    + currentPreparation.contextCount() + " tin ngữ cảnh · Giao "
                    + currentPreparation.intersectionCount() + " · Tổng "
                    + currentPreparation.totalUniqueCount() + " tin duy nhất");
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
            VBox detailsBox = buildDetailsBox(item.senderName(), item.formattedTime(),
                    item.classification(), item.content());
            HBox card = new HBox(detailsBox);
            card.getStyleClass().add("chat-report-excerpt-card");
            if (!"Ngữ cảnh".equals(item.classification())) {
                card.pseudoClassStateChanged(SELECTED, true);
            }
            evidenceList.getChildren().add(card);
        }
        evidenceScroll.setVvalue(0);
    }

    private static VBox buildDetailsBox(String sender, String formattedTime,
                                        String classification, String content) {
        Label senderLabel = new Label(sender);
        senderLabel.getStyleClass().add("chat-result-title");
        senderLabel.setMinWidth(0);
        HBox.setHgrow(senderLabel, Priority.ALWAYS);

        Label timeLabel = new Label(formattedTime);
        timeLabel.getStyleClass().addAll("chat-caption", "chat-result-date");

        HBox senderTimeBox = new HBox(8, senderLabel, timeLabel);
        senderTimeBox.setAlignment(Pos.CENTER_LEFT);

        VBox detailsBox = new VBox(4, senderTimeBox);
        if (classification != null && !classification.isBlank()) {
            Label classLabel = new Label(classification);
            classLabel.getStyleClass().add("chat-report-badge");
            if (!"Ngữ cảnh".equals(classification)) {
                classLabel.getStyleClass().add("is-reported");
            }
            detailsBox.getChildren().add(classLabel);
        }

        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("chat-report-content");
        contentLabel.setWrapText(true);
        detailsBox.getChildren().add(contentLabel);

        detailsBox.setAlignment(Pos.CENTER_LEFT);
        detailsBox.setMinWidth(0);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);
        return detailsBox;
    }

    private void onSubmitReport() {
        if (currentPreparation == null) {
            return;
        }
        ReportReceipt receipt = service.submitReport(currentPreparation);
        receiptIdLabel.setText("Mã hồ sơ: " + receipt.reportId());
        receiptStatusLabel.setText(receipt.status());
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

        active.requestFocus();

        if (currentDialog != null) {
            String title = (active == pickPane) ? pickHeading.getText()
                    : (active == previewPane) ? previewHeading.getText()
                    : "Báo cáo đã được tiếp nhận";
            currentDialog.setTitle(title);
            Platform.runLater(() -> {
                if (currentDialog.getDialogPane().getScene() != null
                        && currentDialog.getDialogPane().getScene().getWindow() != null) {
                    currentDialog.getDialogPane().getScene().getWindow().sizeToScene();
                }
            });
        }
    }
}
