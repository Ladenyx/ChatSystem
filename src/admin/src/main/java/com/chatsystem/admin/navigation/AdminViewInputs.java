package com.chatsystem.admin.navigation;

import com.chatsystem.desktop.navigation.ViewInput;
import java.util.Objects;

public final class AdminViewInputs {
    private AdminViewInputs() {
    }

    public record EditUser(String userId) implements ViewInput {
        public EditUser {
            Objects.requireNonNull(userId, "userId");
        }
    }

    public record ConfirmAction(Action action, String userId) implements ViewInput {
        public ConfirmAction {
            Objects.requireNonNull(action, "action");
            Objects.requireNonNull(userId, "userId");
        }
    }

    public enum Action {
        LOCK_USER, UNLOCK_USER, SOFT_DELETE_USER, RESET_PASSWORD, RESTORE_USER
    }
}
