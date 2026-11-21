package com.chatsystem.desktop.navigation;

/** Optional lifecycle for FXML controllers. Stop timers/subscriptions in onLeave(). */
public interface ViewController {
    default void onEnter(ViewNavigator navigator, View view) {
    }

    /** Receives the input for this activation, including when returning through history. */
    default void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        onEnter(navigator, view);
    }

    default void onLeave() {
    }
}
