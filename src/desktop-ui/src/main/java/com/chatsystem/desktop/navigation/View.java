package com.chatsystem.desktop.navigation;

import java.util.Objects;

/** A route's identity and FXML resource; resources may belong to either app or the UI library. */
public record View(String id, String title, Class<?> resourceOwner, String resource) {
    public View {
        if (id == null || id.isBlank() || title == null || title.isBlank()) {
            throw new IllegalArgumentException("View id and title must not be blank");
        }
        Objects.requireNonNull(resourceOwner, "resourceOwner");
        if (resource == null || !resource.startsWith("/")) {
            throw new IllegalArgumentException("Use an absolute classpath resource for FXML");
        }
    }

    public static View placeholder(String id, String title) {
        return new View(id, title, View.class, "/com/chatsystem/desktop/fxml/placeholder.fxml");
    }
}
