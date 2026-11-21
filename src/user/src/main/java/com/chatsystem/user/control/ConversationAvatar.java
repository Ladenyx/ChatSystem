package com.chatsystem.user.control;

import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.user.messaging.ConversationCatalog;
import java.util.Locale;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

/** Shared 40 px avatar rendered in conversation rows, headers, and the info drawer. */
public final class ConversationAvatar extends StackPane {
    private final Label initials = new Label();
    private final StackPane presence = new StackPane();
    private final FontIcon lock = new FontIcon("fth-lock");

    public ConversationAvatar() {
        initials.getStyleClass().add("chat-avatar");
        lock.setIconSize(9);
        lock.getStyleClass().add("chat-lock");
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setAlignment(presence, Pos.BOTTOM_RIGHT);
        getChildren().setAll(initials, presence);
    }

    public void show(MockConversation chat, ConversationCatalog catalog) {
        initials.setText(initials(chat.title()));
        presence.getStyleClass().setAll("chat-presence");
        if (chat.encrypted()) {
            presence.setVisible(true);
            presence.setManaged(true);
            presence.getStyleClass().add("chat-presence-encrypted");
            presence.getChildren().setAll(lock);
        } else if (catalog.isOnline(chat)) {
            presence.setVisible(true);
            presence.setManaged(true);
            presence.getStyleClass().add("chat-presence-online");
            presence.getChildren().clear();
        } else {
            presence.setVisible(false);
            presence.setManaged(false);
            presence.getStyleClass().add("chat-presence-offline");
            presence.getChildren().clear();
        }
    }

    /** Returns up to two uppercase initials for a user or conversation title. */
    public static String initials(String title) {
        String[] words = title.strip().split("\\s+");
        return (words[0].substring(0, 1) + (words.length > 1 ? words[words.length - 1].substring(0, 1) : ""))
                .toUpperCase(Locale.ROOT);
    }

    /** Builds the compact status indicator graphic (E2EE lock or presence dot) for headers and drawers. */
    public static Node statusGraphic(MockConversation chat, ConversationCatalog catalog) {
        if (chat.encrypted()) {
            FontIcon icon = new FontIcon("fth-lock");
            icon.setIconSize(12);
            icon.getStyleClass().add("chat-status-icon");
            return icon;
        }
        Region dot = new Region();
        dot.getStyleClass().add("chat-status-dot");
        if (catalog.isOnline(chat)) {
            dot.getStyleClass().add("status-online");
        }
        return dot;
    }
}
