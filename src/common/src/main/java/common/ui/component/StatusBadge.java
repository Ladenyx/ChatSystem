package common.ui.component;

import common.model.User;
import javafx.scene.control.Label;

/** A compact, theme-aware status label. */
public class StatusBadge extends Label {
    public StatusBadge(String status) {
        super(status == null ? "Unknown" : status);
        getStyleClass().add("status-badge");
        applyStatusStyle(status);
    }

    public StatusBadge(User.Status status) {
        this(status == null ? "Unknown" : status.name());
    }

    public static StatusBadge online(boolean online) {
        return new StatusBadge(online ? "Online" : "Offline");
    }

    public static StatusBadge accountStatus(User user) {
        return new StatusBadge(user != null && user.getStatus() != null ? user.getStatus().name() : "Active");
    }

    public static StatusBadge onlineStatus(User user) {
        return online(user != null && user.isOnline());
    }

    private void applyStatusStyle(String status) {
        String normalized = status == null ? "" : status.toLowerCase();
        if (normalized.equals("active") || normalized.equals("online") || normalized.equals("resolved")) {
            getStyleClass().add("badge-active");
        } else if (normalized.equals("locked") || normalized.equals("pending") || normalized.equals("offline")) {
            getStyleClass().add(normalized.equals("offline") ? "badge-offline" : "badge-locked");
        } else {
            getStyleClass().add("badge-offline");
        }
    }
}
