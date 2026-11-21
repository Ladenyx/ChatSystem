package com.chatsystem.user.navigation;

import com.chatsystem.desktop.navigation.ViewInput;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class UserViewInputs {
    private UserViewInputs() {
    }

    public record DirectChat(String userId) implements ViewInput {
        public DirectChat {
            Objects.requireNonNull(userId, "userId");
        }
    }

    public record MessageLocation(String conversationId, String messageId) implements ViewInput {
        public MessageLocation {
            Objects.requireNonNull(conversationId, "conversationId");
            Objects.requireNonNull(messageId, "messageId");
        }
    }

    public record CreateGroup(List<String> preselectedUserIds) implements ViewInput {
        public CreateGroup {
            preselectedUserIds = List.copyOf(preselectedUserIds);
        }
    }

    public record Conversation(String conversationId) implements ViewInput {
        public Conversation {
            Objects.requireNonNull(conversationId, "conversationId");
        }
    }

    public record MessageSearch(String conversationId, Consumer<String> revealMessage) implements ViewInput {
        public MessageSearch {
            Objects.requireNonNull(conversationId, "conversationId");
            Objects.requireNonNull(revealMessage, "revealMessage");
        }
    }

    public record ReportSpam(String conversationId, List<String> selectedMessageIds) implements ViewInput {
        public ReportSpam {
            Objects.requireNonNull(conversationId, "conversationId");
            selectedMessageIds = List.copyOf(selectedMessageIds);
        }
    }

    public record AiAssistant(String conversationId, AiMode mode) implements ViewInput {
        public AiAssistant {
            Objects.requireNonNull(conversationId, "conversationId");
            Objects.requireNonNull(mode, "mode");
        }
    }

    public enum AiMode {
        REFINE, RECOMMEND
    }

    public enum VerifyEmail implements ViewInput {
        SIGN_UP, ADMIN_CREATED_ACCOUNT, CHANGE_EMAIL
    }

    public enum E2eeKeySetup implements ViewInput {
        SET_UP, RECOVER
    }
}
