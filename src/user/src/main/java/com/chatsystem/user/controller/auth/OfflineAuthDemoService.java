package com.chatsystem.user.controller.auth;

/** Synthetic account responses for the offline preview; not an authentication backend. */
final class OfflineAuthDemoService {
    private static final String DEMO_USER = "demo";
    private static final String RESERVED_USERNAME = "demo01";
    private static final String DEMO_EMAIL = "demo@example.test";
    private static final String DEMO_PASSWORD = "Demo!1234";
    private static final String DEMO_TEMPORARY_PASSWORD = "Tam!1234";

    private OfflineAuthDemoService() {
    }

    static boolean matchesDemoUsername(String username) {
        return DEMO_USER.equalsIgnoreCase(username);
    }

    static boolean usernameInUse(String username) {
        return matchesDemoUsername(username) || RESERVED_USERNAME.equalsIgnoreCase(username);
    }

    static boolean emailInUse(String email) {
        return DEMO_EMAIL.equalsIgnoreCase(email);
    }

    static boolean matchesDemoCredentials(String username, String password) {
        return matchesDemoUsername(username) && DEMO_PASSWORD.equals(password);
    }

    static boolean matchesTemporaryPassword(String password) {
        return DEMO_TEMPORARY_PASSWORD.equals(password);
    }

    static String recoveryEmailHint(String username) {
        return matchesDemoUsername(username) ? AuthFormController.maskedEmail(DEMO_EMAIL) : "";
    }

    static boolean verifyRecoveryEmail(String username, String email) {
        return matchesDemoUsername(username) && emailInUse(email);
    }
}
