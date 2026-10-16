package common.ui.component;

import common.model.User;
import javafx.scene.control.Label;

/** Rounded avatar tile showing a user's initials. */
public class AvatarLabel extends Label {
    public AvatarLabel(User user) {
        this(user == null ? "?" : user.getInitials());
    }

    public AvatarLabel(String initials) {
        super(initials == null || initials.isBlank() ? "?" : initials.trim());
        getStyleClass().addAll("avatar", "avatar-user");
        setMinSize(40, 40);
        setPrefSize(40, 40);
        setMaxSize(40, 40);
    }
}
