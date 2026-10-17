package user.ui;

import common.mock.MockData;
import common.model.Message;
import common.model.SpamReport;
import common.model.User;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;

/**
 * Dialog for submitting a spam / moderation report against a user (Roadmap U-26).
 */
public class ReportSpamDialog extends Dialog<Boolean> {

    public ReportSpamDialog(User targetUser, Message messageContext) {
        setTitle("Report for Spam");
        setHeaderText(null);

        DialogPane pane = getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        Button submitBtn = (Button) pane.lookupButton(ButtonType.OK);
        submitBtn.setText("Submit Report");
        submitBtn.getStyleClass().add("btn-warn");

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        content.setPrefWidth(420);

        Label title = new Label("Report User");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label("Report " + (targetUser != null ? targetUser.getFullName() : "this user") + " for violating community standards.");
        subtitle.getStyleClass().add("text-muted");

        // Reported User & Message info
        VBox contextBox = new VBox(6);
        contextBox.setStyle("-fx-background-color: #f9fafc; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #ebebfc; -fx-border-radius: 8;");

        Label userLabel = new Label("Reported account: @" + (targetUser != null ? targetUser.getUsername() : "unknown"));
        userLabel.getStyleClass().add("text-bold");

        contextBox.getChildren().add(userLabel);
        if (messageContext != null) {
            Label msgLabel = new Label("Flagged message: \"" + messageContext.getContent() + "\"");
            msgLabel.getStyleClass().add("text-small");
            msgLabel.setStyle("-fx-text-fill: #8a8a95;");
            contextBox.getChildren().add(msgLabel);
        }

        // Reason selector
        Label reasonLbl = new Label("Select reason:");
        reasonLbl.getStyleClass().add("text-bold");

        ComboBox<String> reasonCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Unsolicited spam or advertising",
                "Harassment, hate speech, or abuse",
                "Scam, phishing, or fraud attempt",
                "Malicious links or phishing",
                "Inappropriate or offensive content",
                "Other violations"
        ));
        reasonCombo.setValue("Unsolicited spam or advertising");
        reasonCombo.setMaxWidth(Double.MAX_VALUE);

        // Additional comments
        Label commentsLbl = new Label("Additional notes (optional):");
        commentsLbl.getStyleClass().add("text-bold");

        TextArea commentsArea = new TextArea();
        commentsArea.setPromptText("Describe what happened or provide context...");
        commentsArea.setPrefRowCount(3);
        commentsArea.setWrapText(true);

        content.getChildren().addAll(title, subtitle, contextBox, reasonLbl, reasonCombo, commentsLbl, commentsArea);
        pane.setContent(content);

        setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String fullReason = reasonCombo.getValue();
                if (!commentsArea.getText().isBlank()) {
                    fullReason += ": " + commentsArea.getText().trim();
                }

                User reporter = MockData.CURRENT_USER;
                User reported = targetUser != null ? targetUser : MockData.USERS.get(1);

                SpamReport report = new SpamReport(
                        MockData.SPAM_REPORTS.size() + 1,
                        reporter,
                        reported,
                        fullReason,
                        SpamReport.Status.PENDING,
                        LocalDateTime.now()
                );
                MockData.SPAM_REPORTS.add(report);

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Report Submitted");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Thank you for your report. Administrators will review the activity shortly.");
                successAlert.showAndWait();

                return true;
            }
            return false;
        });
    }

    public static void show(User targetUser, Message messageContext) {
        new ReportSpamDialog(targetUser, messageContext).showAndWait();
    }
}

