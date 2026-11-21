package com.chatsystem.user.control;

import com.chatsystem.user.UserApplication;
import java.net.URL;
import java.util.Objects;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/** Renders a Figma icon with the foreground color of its JavaFX component. */
public final class ChatIcon extends Region {
    private final ImageView mask = new ImageView();
    private String icon;

    public ChatIcon() {
        getStyleClass().add("chat-icon");
        mask.setPreserveRatio(true);
        mask.fitWidthProperty().bind(widthProperty());
        mask.fitHeightProperty().bind(heightProperty());
        setClip(mask);
        setMouseTransparent(true);
    }

    public ChatIcon(String name) {
        this();
        setIcon(name);
    }

    public void setIcon(String name) {
        URL resource = UserApplication.class.getResource("/com/chatsystem/user/images/mainshell/" + name + ".png");
        mask.setImage(new Image(Objects.requireNonNull(resource, name).toExternalForm()));
        icon = name;
    }

    public String getIcon() {
        return icon;
    }
}
