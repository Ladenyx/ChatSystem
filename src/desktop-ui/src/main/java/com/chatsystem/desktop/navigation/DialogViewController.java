package com.chatsystem.desktop.navigation;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/** Optional hook for a modal view to configure its buttons before it is shown. */
public interface DialogViewController extends ViewController {
    void configureDialog(Dialog<ButtonType> dialog);
}
