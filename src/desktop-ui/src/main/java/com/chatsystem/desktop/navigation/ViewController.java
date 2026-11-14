package com.chatsystem.desktop.navigation;

/** Optional lifecycle for FXML controllers. Stop timers/subscriptions in onLeave(). */
public interface ViewController {
    default void onEnter(ViewNavigator navigator, View view) {
    }

    default void onLeave() {
    }
}
