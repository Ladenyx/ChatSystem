package user.ui;

import common.model.Message;
import common.model.User;
import common.ui.component.AvatarLabel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Visual component representing a single message bubble in a conversation.
 * Supports:
 * - Sent (right-aligned, purple accent) vs Received (left-aligned, light grey) styling.
 * - Sender name display in group chats.
 * - Selection checkbox for multi-message deletion (U-16).
 * - Highlighting when matched in in-chat search (U-17).
 * - Context menu: Copy, Delete (U-16), Report as spam (U-26).
 */
public class MessageBubbleView extends HBox {

    private final Message message;
    private final boolean isMine;
    private final CheckBox selectCheckBox = new CheckBox();
    private final BooleanProperty selectedProperty = new SimpleBooleanProperty(false);
    private final VBox bubbleBox = new VBox(3);
    private final Label contentLabel = new Label();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Consumer<Message> onDeleteSingle;
    private final Consumer<Message> onReportSpam;

    public MessageBubbleView(Message message, boolean isMine, boolean isGroup,
                             Consumer<Message> onDeleteSingle,
                             Consumer<Message> onReportSpam) {
        this.message = message;
        this.isMine = isMine;
        this.onDeleteSingle = onDeleteSingle;
        this.onReportSpam = onReportSpam;

        setSpacing(8);
        setPadding(new Insets(4, 12, 4, 12));
        setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        // Selection CheckBox (initially hidden)
        selectCheckBox.setVisible(false);
        selectCheckBox.setManaged(false);
        selectCheckBox.selectedProperty().bindBidirectional(selectedProperty);

        buildBubbleUI(isGroup);
        setupContextMenu();
    }

    private void buildBubbleUI(boolean isGroup) {
        contentLabel.setText(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(380);
        contentLabel.getStyleClass().add(isMine ? "bubble-sent" : "bubble-received");

        // Timestamp & status indicators
        String timeStr = message.getSentAt() != null ? message.getSentAt().format(TIME_FORMATTER) : "";
        String statusStr = "";
        if (isMine) {
            statusStr = switch (message.getStatus()) {
                case READ -> " ✓✓";
                case DELIVERED -> " ✓";
                default -> "";
            };
        }
        Label timeLabel = new Label(timeStr + statusStr);
        timeLabel.getStyleClass().add("bubble-time");

        // Layout bubbleBox
        bubbleBox.getChildren().clear();

        // If in group chat and received, show sender name
        if (isGroup && !isMine && message.getSenderName() != null && !message.getSenderName().isBlank()) {
            Label senderLabel = new Label(message.getSenderName());
            senderLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #7678ed; -fx-padding: 0 0 2 0;");
            bubbleBox.getChildren().add(senderLabel);
        }

        bubbleBox.getChildren().addAll(contentLabel, timeLabel);
        bubbleBox.setAlignment(isMine ? Pos.BOTTOM_RIGHT : Pos.BOTTOM_LEFT);

        if (isMine) {
            getChildren().addAll(selectCheckBox, bubbleBox);
        } else {
            // Received: avatar on the left
            AvatarLabel avatar = new AvatarLabel(message.getSenderName() != null ? extractInitials(message.getSenderName()) : "?");
            getChildren().addAll(selectCheckBox, avatar, bubbleBox);
        }
    }

    private void setupContextMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem copyItem = new MenuItem("Copy message");
        copyItem.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(message.getContent());
            clipboard.setContent(content);
        });

        MenuItem deleteItem = new MenuItem("Delete message");
        deleteItem.setOnAction(e -> {
            if (onDeleteSingle != null) onDeleteSingle.accept(message);
        });

        menu.getItems().addAll(copyItem, deleteItem);

        // Can report spam on received messages
        if (!isMine) {
            MenuItem reportItem = new MenuItem("Report as spam");
            reportItem.setOnAction(e -> {
                if (onReportSpam != null) onReportSpam.accept(message);
            });
            menu.getItems().add(reportItem);
        }

        setOnContextMenuRequested(e -> menu.show(this, e.getScreenX(), e.getScreenY()));
    }

    public void setSelectionMode(boolean enabled) {
        selectCheckBox.setVisible(enabled);
        selectCheckBox.setManaged(enabled);
        if (!enabled) {
            setSelected(false);
        }
    }

    public boolean isSelected() {
        return selectedProperty.get();
    }

    public void setSelected(boolean val) {
        selectedProperty.set(val);
    }

    public BooleanProperty selectedProperty() {
        return selectedProperty;
    }

    public Message getMessage() {
        return message;
    }

    /**
     * Highlights the bubble when matched in search.
     */
    public void setHighlighted(boolean highlight) {
        if (highlight) {
            contentLabel.setStyle(isMine
                    ? "-fx-background-color: #ff7a55; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(255,122,85,0.4), 8, 0, 0, 2);"
                    : "-fx-background-color: #ffeaa7; -fx-text-fill: #202022; -fx-effect: dropshadow(gaussian, rgba(253,203,110,0.4), 8, 0, 0, 2);");
        } else {
            contentLabel.setStyle(null); // revert to CSS classes
        }
    }

    private String extractInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
