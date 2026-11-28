package com.chatsystem.user;

/** Non-Application entry point required to launch JavaFX cleanly from a standalone fat-JAR. */
public final class UserLauncher {
    private UserLauncher() {
    }

    public static void main(String[] args) {
        UserApplication.main(args);
    }
}
