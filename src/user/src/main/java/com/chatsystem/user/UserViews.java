package com.chatsystem.user;

import com.chatsystem.desktop.navigation.View;
import java.util.List;
import java.util.Set;

public final class UserViews {
    public static final View SIGN_IN = view("U-SCR-01", "Đăng nhập", "auth/sign-in.fxml");
    public static final View SIGN_UP = view("U-SCR-02", "Đăng ký", "auth/sign-up.fxml");
    public static final View VERIFY_EMAIL = view("U-SCR-03", "Xác minh email", "auth/verify-email.fxml");
    public static final View FORGOT_PASSWORD = view("U-SCR-04", "Quên mật khẩu", "auth/forgot-password.fxml");
    public static final View FORCE_CHANGE_PASSWORD = view("U-SCR-05", "Đặt mật khẩu bắt buộc",
            "auth/force-change-password.fxml");
    public static final View E2EE_KEY_SETUP = view("U-SCR-06", "Thiết lập và khôi phục khóa E2EE",
            "auth/e2ee-key-setup.fxml");
    public static final View CONVERSATIONS = view("U-SCR-07", "Hội thoại", "messaging/conversations.fxml");
    public static final View CONTACTS = view("U-SCR-08", "Bạn bè & Kết nối", "contacts/contacts.fxml");
    public static final View CONVERSATION_INFO = view("U-SCR-09", "Thông tin đoạn chat",
            "messaging/conversation-info.fxml");

    public static final View PROFILE = view("U-DLG-01", "Hồ sơ & Bảo mật", "profile/profile.fxml");
    public static final View CREATE_GROUP = view("U-DLG-02", "Tạo nhóm chat mới", "messaging/create-group.fxml");
    public static final View AI_ASSISTANT = view("U-DLG-03", "Trợ lý AI", "messaging/ai-assistant.fxml");
    public static final View REPORT_SPAM = view("U-DLG-04", "Báo cáo spam", "messaging/report-spam.fxml");
    public static final View ASSIGN_SUCCESSOR_ADMIN = view("U-DLG-05", "Chọn Admin kế nhiệm",
            "messaging/assign-successor-admin.fxml");

    public static final List<View> ALL = List.of(SIGN_IN, SIGN_UP, VERIFY_EMAIL, FORGOT_PASSWORD,
            FORCE_CHANGE_PASSWORD, E2EE_KEY_SETUP, CONVERSATIONS, CONTACTS, CONVERSATION_INFO,
            PROFILE, CREATE_GROUP, AI_ASSISTANT, REPORT_SPAM, ASSIGN_SUCCESSOR_ADMIN);

    private static final Set<View> AUTH_VIEWS = Set.of(SIGN_IN, SIGN_UP, VERIFY_EMAIL, FORGOT_PASSWORD,
            FORCE_CHANGE_PASSWORD, E2EE_KEY_SETUP);

    public static boolean isAuthView(View view) {
        return view != null && AUTH_VIEWS.contains(view);
    }

    private UserViews() {
    }

    private static View view(String id, String title, String path) {
        return new View(id, title, UserApplication.class, "/com/chatsystem/user/fxml/" + path);
    }
}
