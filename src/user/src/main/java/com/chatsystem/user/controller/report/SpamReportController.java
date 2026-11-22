package com.chatsystem.user.controller.report;

import com.chatsystem.desktop.navigation.DialogViewController;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserApplication;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.navigation.UserViewInputs;
import com.chatsystem.user.service.SpamReportService;
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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Controller for User Spam Reporting dialog (U-DLG-04 / P1 / REPORT-GROUP).
 */
public final class SpamReportController implements DialogViewController {
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
    private static final int MAX_SELECTED_MESSAGES = 20;

    public record ReportContext(String conversationId, String conversationTitle) {}

    // STEP 1 Controls
    @FXML private VBox pickPane;
    @FXML private Label pickHeading;
    @FXML private Button pickCloseButton;
    @FXML private Label pickDescription;
    @FXML private ComboBox<TargetUserOption> targetUserCombo;
    @FXML private TextField reasonField;
    @FXML private Label messagesSectionTitle;
    @FXML private Label selectionCountLabel;
    @FXML private ScrollPane messagesScroll;
    @FXML private VBox messagesList;
    @FXML private HBox pickFeedbackBox;
    @FXML private Label pickFeedback;
    @FXML private Button pickCancelButton;
    @FXML private Button previewButton;

    // STEP 2 Controls
    @FXML private VBox previewPane;
    @FXML private Label previewHeading;
    @FXML private Button previewCloseButton;
    @FXML private Label previewTargetValue;
    @FXML private Label previewReasonValue;
    @FXML private Label previewConsentNotice;
    @FXML private Label previewCountMeta;
    @FXML private ScrollPane evidenceScroll;
    @FXML private VBox evidenceList;
    @FXML private Button editPickButton;
    @FXML private Button submitReportButton;

    // STEP 3 Controls
    @FXML private VBox acceptedPane;
    @FXML private Button acceptedCloseTopButton;
    @FXML private Label receiptIdLabel;
    @FXML private Label receiptStatusLabel;
    @FXML private Label receiptConversationLabel;
    @FXML private Label receiptTargetLabel;
    @FXML private Label receiptEvidenceLabel;
    @FXML private Label receiptTimestampLabel;
    @FXML private Button acceptedDoneButton;

    private final Set<String> selectedMessageIds = new LinkedHashSet<>();
    private List<String> preselectedIds = List.of();
    private ViewNavigator navigator;
    private SpamReportService service;
    private ReportContext context;
    private ReportPreparation currentPreparation;
    private Dialog<ButtonType> currentDialog;
    private Dialog<ButtonType> confirmDialog;
    private Node pendingScrollTarget;

    public static void open(ViewNavigator navigator, String conversationId, String conversationTitle) {
        String id = conversationId != null ? conversationId : "";
        navigator.showModal(UserViews.REPORT_SPAM, new UserViewInputs.ReportSpam(id, List.of()), true);
    }

    @FXML
    private void initialize() {
        for (Node node : List.of(pickDescription, pickFeedback)) {
            node.managedProperty().bind(node.visibleProperty());
        }
        pickFeedbackBox.visibleProperty().bind(pickFeedback.visibleProperty());
        pickFeedbackBox.managedProperty().bind(pickFeedback.visibleProperty());

        targetUserCombo.setCellFactory(list -> createTargetUserCell());
        targetUserCombo.setButtonCell(createTargetUserCell());
        targetUserCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (context != null && service != null) {
                loadMessagesForTargetUser(newVal != null ? newVal.id() : null);
            }
        });

        reasonField.textProperty().addListener((obs, oldText, newText) -> {
            reasonField.pseudoClassStateChanged(INVALID, false);
            if (pickFeedback.isVisible()) {
                pickFeedback.setVisible(false);
                resizeDialog();
            }
        });
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        this.navigator = navigator;
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

        currentPreparation = null;
        pendingScrollTarget = null;
        reasonField.clear();
        pickFeedback.setVisible(false);
        reasonField.pseudoClassStateChanged(INVALID, false);
        messagesScroll.pseudoClassStateChanged(INVALID, false);
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
        submitReportButton.setOnAction(event -> confirmSubmitReport());

        dialog.setOnShown(event -> {
            pane.applyCss();
            pane.layout();
            if (pane.getScene() != null && pane.getScene().getWindow() != null) {
                pane.getScene().getWindow().sizeToScene();
            }
            if (pendingScrollTarget != null) {
                scrollToExcerpt(pendingScrollTarget);
                pendingScrollTarget = null;
            }
        });
        resizeDialog();
    }

    @Override
    public void onLeave() {
        if (confirmDialog != null) {
            confirmDialog.close();
            confirmDialog = null;
        }
        navigator = null;
    }

    private static ListCell<TargetUserOption> createTargetUserCell() {
        return new ListCell<>() {
            private final Label avatar = new Label();
            private final Label name = new Label();
            private final HBox box = new HBox(8, avatar, name);

            {
                avatar.getStyleClass().add("chat-report-user-avatar");
                name.getStyleClass().add("chat-report-user-name");
                box.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(TargetUserOption item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    avatar.setText(ConversationAvatar.initials(item.displayName()));
                    name.setText(item.displayName());
                    setText(null);
                    setGraphic(box);
                }
            }
        };
    }

    private void setupTargetUsers() {
        targetUserCombo.getItems().clear();
        List<TargetUserOption> options = service.getTargetUserOptions(context.conversationId());
        targetUserCombo.getItems().addAll(options);
        if (!options.isEmpty()) {
            var preferredTargetId = service.resolveTargetUserId(context.conversationId(), preselectedIds);
            TargetUserOption initial = preferredTargetId
                    .flatMap(id -> options.stream().filter(opt -> opt.id().equals(id)).findFirst())
                    .orElse(options.getFirst());
            targetUserCombo.getSelectionModel().select(initial);
        } else {
            loadMessagesForTargetUser(null);
        }
    }

    private void loadMessagesForTargetUser(String targetUserId) {
        selectedMessageIds.clear();
        messagesList.getChildren().clear();
        messagesScroll.pseudoClassStateChanged(INVALID, false);
        if (pickFeedback.isVisible()) {
            pickFeedback.setVisible(false);
            resizeDialog();
        }

        if (targetUserId == null || targetUserId.isBlank()) {
            updateSelectionCounter();
            return;
        }
        List<ReportMessageItem> messages = service.getEligibleMessages(context.conversationId(), targetUserId);
        boolean hasPreselectedMatch = !preselectedIds.isEmpty()
                && messages.stream().anyMatch(msg -> preselectedIds.contains(msg.id()));
        HBox firstSelectedCard = null;
        for (int i = 0; i < messages.size(); i++) {
            ReportMessageItem msg = messages.get(i);
            boolean initiallySelected = hasPreselectedMatch
                    ? preselectedIds.contains(msg.id())
                    : (i == 0);
            if (initiallySelected) {
                selectedMessageIds.add(msg.id());
            }
            HBox card = createMessageExcerptRow(msg, initiallySelected);
            if (initiallySelected && firstSelectedCard == null) {
                firstSelectedCard = card;
            }
            messagesList.getChildren().add(card);
        }
        if (hasPreselectedMatch) {
            preselectedIds = List.of();
        }
        if (messages.isEmpty()) {
            Label empty = new Label("Không có tin nhắn hợp lệ từ thành viên này");
            empty.getStyleClass().add("chat-caption");
            empty.setWrapText(true);
            messagesList.getChildren().add(empty);
        }
        if (firstSelectedCard != null && hasPreselectedMatch) {
            final HBox targetCard = firstSelectedCard;
            pendingScrollTarget = targetCard;
            Platform.runLater(() -> scrollToExcerpt(targetCard));
        } else {
            messagesScroll.setVvalue(0);
        }
        updateSelectionCounter();
    }

    private void scrollToExcerpt(Node card) {
        messagesScroll.applyCss();
        messagesScroll.layout();
        messagesList.applyCss();
        messagesList.layout();
        double viewportHeight = messagesScroll.getViewportBounds().getHeight();
        double contentHeight = messagesList.getBoundsInLocal().getHeight();
        double scrollable = contentHeight - viewportHeight;
        if (scrollable > 0) {
            double targetY = card.getBoundsInParent().getMinY() - viewportHeight * 0.25;
            messagesScroll.setVvalue(Math.clamp(targetY / scrollable, 0.0, 1.0));
        } else {
            messagesScroll.setVvalue(0);
        }
    }

    private void updateSelectionCounter() {
        int count = selectedMessageIds.size();
        selectionCountLabel.setText(count + " / " + MAX_SELECTED_MESSAGES + " đã chọn");
        boolean overLimit = count > MAX_SELECTED_MESSAGES;
        if (overLimit && !selectionCountLabel.getStyleClass().contains("is-over-limit")) {
            selectionCountLabel.getStyleClass().add("is-over-limit");
        } else if (!overLimit) {
            selectionCountLabel.getStyleClass().remove("is-over-limit");
        }
        if (count >= 1 && !overLimit) {
            messagesScroll.pseudoClassStateChanged(INVALID, false);
        }
    }

    private HBox createMessageExcerptRow(ReportMessageItem item, boolean initiallyChecked) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(initiallyChecked);
        checkBox.setAccessibleText("Chọn tin nhắn của " + item.senderName());

        VBox detailsBox = buildDetailsBox(item.senderName(), item.formattedTime(), item.content());

        HBox card = new HBox(12, checkBox, detailsBox);
        card.getStyleClass().addAll("chat-report-excerpt-card", "is-selectable");
        card.pseudoClassStateChanged(SELECTED, initiallyChecked);

        checkBox.setOnAction(event -> {
            boolean checked = checkBox.isSelected();
            if (checked) {
                selectedMessageIds.add(item.id());
            } else {
                selectedMessageIds.remove(item.id());
            }
            card.pseudoClassStateChanged(SELECTED, checked);
            updateSelectionCounter();
            if (pickFeedback.isVisible()) {
                pickFeedback.setVisible(false);
                resizeDialog();
            }
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
        reasonField.pseudoClassStateChanged(INVALID, false);
        messagesScroll.pseudoClassStateChanged(INVALID, false);

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

            pickFeedback.setVisible(false);
            previewTargetValue.setText(currentPreparation.targetUserName());
            previewReasonValue.setText(currentPreparation.reason());
            previewCountMeta.setText(currentPreparation.selectedCount() + " tin nhắn");
            previewConsentNotice.setText(currentPreparation.selectedCount()
                    + " tin nhắn đã chọn và lý do báo cáo sẽ được gửi dạng bản rõ để quản trị viên"
                    + " hệ thống xem xét, kể cả trong nhóm mã hóa đầu cuối (E2EE).");

            renderSelectedEvidenceList(currentPreparation.selectedMessages());
            showStep(previewPane);
        } catch (IllegalArgumentException ex) {
            boolean invalidReason = reason == null || reason.isBlank();
            boolean invalidSelection = selectedMessageIds.isEmpty()
                    || selectedMessageIds.size() > MAX_SELECTED_MESSAGES;
            reasonField.pseudoClassStateChanged(INVALID, invalidReason);
            messagesScroll.pseudoClassStateChanged(INVALID, invalidSelection);
            pickFeedback.setText(ex.getMessage());
            pickFeedback.setVisible(true);
            if (invalidReason) {
                reasonField.requestFocus();
            }
            resizeDialog();
        }
    }

    private void renderSelectedEvidenceList(List<ReportMessageItem> selectedMessages) {
        evidenceList.getChildren().clear();
        for (ReportMessageItem item : selectedMessages) {
            VBox detailsBox = buildDetailsBox(item.senderName(), item.formattedTime(), item.content());
            HBox card = new HBox(detailsBox);
            card.getStyleClass().addAll("chat-report-excerpt-card", "is-readonly", "is-reported-preview");
            evidenceList.getChildren().add(card);
        }
        evidenceScroll.setVvalue(0);
    }

    private static VBox buildDetailsBox(String sender, String formattedTime, String content) {
        Label senderLabel = new Label(sender);
        senderLabel.getStyleClass().add("chat-result-title");
        senderLabel.setMinWidth(0);
        senderLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(senderLabel, Priority.ALWAYS);

        Label timeLabel = new Label(formattedTime);
        timeLabel.getStyleClass().addAll("chat-caption", "chat-result-date");
        timeLabel.setMinWidth(Region.USE_PREF_SIZE);

        HBox headerRow = new HBox(8, senderLabel, timeLabel);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("chat-report-content");
        contentLabel.setWrapText(true);

        VBox detailsBox = new VBox(4, headerRow, contentLabel);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        detailsBox.setMinWidth(0);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);
        return detailsBox;
    }

    private void confirmSubmitReport() {
        if (currentPreparation == null) {
            return;
        }
        if (confirmDialog != null) {
            confirmDialog.close();
            confirmDialog = null;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        Window owner = currentDialog != null && currentDialog.getDialogPane().getScene() != null
                ? currentDialog.getDialogPane().getScene().getWindow()
                : null;
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Xác nhận gửi báo cáo?");

        var pane = dialog.getDialogPane();
        pane.setId("reportConfirmDialog");
        pane.getStyleClass().add("chat-report-confirm");
        pane.setPrefWidth(400);

        Label title = new Label("Xác nhận gửi báo cáo?");
        title.setId("reportConfirmTitle");
        title.getStyleClass().add("chat-report-confirm-title");
        title.setWrapText(true);

        Label detail = new Label("Gửi báo cáo spam đối với " + currentPreparation.targetUserName()
                + " kèm " + currentPreparation.selectedCount()
                + " tin nhắn đã chọn để quản trị viên xem xét? Nội dung đã chọn và lý do sẽ được gửi"
                + " dạng bản rõ, kể cả trong nhóm E2EE.");
        detail.getStyleClass().add("chat-report-confirm-detail");
        detail.setWrapText(true);
        pane.setContent(new VBox(8, title, detail));

        ButtonType cancelType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType confirmType = new ButtonType("Gửi báo cáo", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().setAll(cancelType, confirmType);

        Button cancelButton = (Button) pane.lookupButton(cancelType);
        Button confirmButton = (Button) pane.lookupButton(confirmType);
        ButtonBar.setButtonUniformSize(cancelButton, false);
        ButtonBar.setButtonUniformSize(confirmButton, false);
        cancelButton.setId("reportConfirmCancel");
        cancelButton.getStyleClass().add("chat-dialog-cancel");
        cancelButton.setDefaultButton(true);
        confirmButton.setId("reportConfirmSubmit");
        confirmButton.getStyleClass().add("chat-primary");
        confirmButton.setDefaultButton(false);

        if (navigator != null) {
            navigator.themes().apply(pane);
        }
        for (String file : List.of("chat-tokens.css", "chat.css", "report.css")) {
            String sheet = UserApplication.class.getResource("/com/chatsystem/user/css/" + file).toExternalForm();
            if (!pane.getStylesheets().contains(sheet)) {
                pane.getStylesheets().add(sheet);
            }
        }

        dialog.setOnHidden(event -> {
            if (confirmDialog == dialog) {
                confirmDialog = null;
            }
            if (dialog.getResult() == confirmType) {
                executeSubmitReport();
            }
        });
        confirmDialog = dialog;
        dialog.show();
        cancelButton.requestFocus();
    }

    private void executeSubmitReport() {
        if (currentPreparation == null) {
            return;
        }
        ReportReceipt receipt = service.submitReport(currentPreparation);
        receiptIdLabel.setText(receipt.reportId());
        receiptStatusLabel.setText("PENDING".equals(receipt.status()) ? "Chờ xử lý" : receipt.status());
        receiptConversationLabel.setText(currentPreparation.conversationTitle());
        receiptTargetLabel.setText(currentPreparation.targetUserName());
        receiptEvidenceLabel.setText(currentPreparation.selectedCount() + " tin nhắn đã báo cáo");
        receiptTimestampLabel.setText(receipt.timestamp());
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
            resizeDialog();
        }
    }

    private void resizeDialog() {
        if (currentDialog == null) {
            return;
        }
        Platform.runLater(() -> {
            if (currentDialog.getDialogPane().getScene() != null
                    && currentDialog.getDialogPane().getScene().getWindow() != null) {
                currentDialog.getDialogPane().getScene().getWindow().sizeToScene();
            }
        });
    }
}
