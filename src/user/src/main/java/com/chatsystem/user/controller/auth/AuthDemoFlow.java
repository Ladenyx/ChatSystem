package com.chatsystem.user.controller.auth;

import com.chatsystem.desktop.navigation.ViewNavigator;
import java.time.Duration;
import java.time.Instant;

/** Window-local navigation state for the offline preview; never stores a password. */
public final class AuthDemoFlow {
    static final String DEMO_CODE = "123456";

    enum SignUpStep { USERNAME, PASSWORD, EMAIL, PROFILE }
    enum TemporaryPasswordStatus { INVALID, EXPIRED, VALID }

    SignUpStep signUpStep = SignUpStep.USERNAME;
    String signUpName = "";
    String signUpEmail = "";
    Instant signUpExpires;
    Instant codeExpires;
    Instant codeIssued;
    int codeFailures;
    boolean emailVerified;
    String loginNotice = "";
    String recoveryName = "";
    String recoveryRequestedFor = "";
    Instant recoveryIssued;
    boolean temporaryAccepted;

    private AuthDemoFlow() {
    }

    static AuthDemoFlow get(ViewNavigator navigator) {
        return (AuthDemoFlow) navigator.getRoot().getProperties().computeIfAbsent(
                AuthDemoFlow.class, key -> new AuthDemoFlow());
    }

    public static void clear(ViewNavigator navigator) {
        navigator.getRoot().getProperties().remove(AuthDemoFlow.class);
    }

    void issueCode() {
        codeIssued = Instant.now();
        if (signUpExpires == null) {
            signUpExpires = codeIssued.plus(Duration.ofMinutes(30));
        }
        codeExpires = codeIssued.plus(Duration.ofMinutes(10));
        emailVerified = false;
        codeFailures = 0;
    }

    boolean signUpActive() {
        return activeUntil(signUpExpires);
    }

    boolean codeActive() {
        return signUpActive() && activeUntil(codeExpires) && codeFailures < 5;
    }

    void issueTemporaryPassword() {
        recoveryRequestedFor = recoveryName;
        recoveryIssued = Instant.now();
        temporaryAccepted = false;
    }

    boolean recoveryActive() {
        return recoveryIssued != null && activeUntil(recoveryIssued.plus(Duration.ofMinutes(15)));
    }

    TemporaryPasswordStatus temporaryPasswordStatus(String username, String password) {
        if (recoveryIssued == null || !recoveryRequestedFor.equalsIgnoreCase(username)
                || !OfflineAuthDemoService.matchesTemporaryPassword(password)) {
            return TemporaryPasswordStatus.INVALID;
        }
        return recoveryActive() ? TemporaryPasswordStatus.VALID : TemporaryPasswordStatus.EXPIRED;
    }

    static long secondsUntil(Instant issued, long wait) {
        if (issued == null) {
            return 0;
        }
        long millis = Duration.between(Instant.now(), issued.plusSeconds(wait)).toMillis();
        return Math.max(0, (millis + 999) / 1000);
    }

    private static boolean activeUntil(Instant deadline) {
        return deadline != null && Instant.now().isBefore(deadline);
    }
}
