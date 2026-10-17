package common.ui.component;

import common.model.User;
import javafx.scene.control.Label;

/** Rounded avatar tile showing a user's or group's initials. */
public class AvatarLabel extends Label {

    public AvatarLabel(User user) {
        this(user == null ? "?" : user.getInitials(), false);
    }

    public AvatarLabel(String initials) {
        this(initials, false);
    }

    public AvatarLabel(String initials, boolean isGroup) {
        this(initials, isGroup, false);
    }

    public AvatarLabel(String initials, boolean isGroup, boolean small) {
        super(initials == null || initials.isBlank() ? "?" : initials.trim());
        getStyleClass().addAll("avatar", isGroup ? "avatar-group" : "avatar-user");
        if (small) {
            getStyleClass().add("avatar-sm");
            setMinSize(32, 32);
            setPrefSize(32, 32);
            setMaxSize(32, 32);
        } else {
            setMinSize(40, 40);
            setPrefSize(40, 40);
            setMaxSize(40, 40);
        }
    }
}
