package com.chatsystem.desktop.navigation;

public final class ViewLoadException extends RuntimeException {
    public ViewLoadException(String resource, Throwable cause) {
        super("Cannot load FXML view: " + resource, cause);
    }
}
