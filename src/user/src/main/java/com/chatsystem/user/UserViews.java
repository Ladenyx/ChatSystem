package com.chatsystem.user;

import com.chatsystem.desktop.navigation.View;
import java.util.Set;

public final class UserViews {
    public static final View SIGN_IN = authView("U-SCR-01", "Đăng nhập", "auth-sign-in.fxml");
    public static final View SIGN_UP = authView("U-SCR-02", "Đăng ký", "auth-sign-up.fxml");
    public static final View VERIFY_EMAIL = authView("U-SCR-03", "Xác minh email", "auth-verify-email.fxml");
    public static final View FORGOT_PASSWORD = authView("U-SCR-04", "Quên mật khẩu", "auth-forgot-password.fxml");
    public static final View FORCE_CHANGE_PASSWORD = authView(
            "U-SCR-05", "Đặt mật khẩu mới", "auth-force-change-password.fxml");
    public static final View CONVERSATIONS = new View("U-SCR-07", "Hội thoại",
            UserApplication.class, "/com/chatsystem/user/fxml/conversations.fxml");
    public static final View CONTACTS = View.placeholder("U-SCR-08", "Bạn bè & Kết nối");
    public static final View PROFILE = View.placeholder("U-DLG-01", "Hồ sơ & Bảo mật");
    public static final View CREATE_GROUP = View.placeholder("U-DLG-02", "Tạo nhóm chat mới");
    public static final View CONVERSATION_INFO = View.placeholder("U-SCR-09", "Thông tin đoạn chat");

    private static final Set<View> AUTH_VIEWS = Set.of(
            SIGN_IN, SIGN_UP, VERIFY_EMAIL, FORGOT_PASSWORD, FORCE_CHANGE_PASSWORD);

    private UserViews() {
    }

    public static boolean isAuthView(View view) {
        return view != null && AUTH_VIEWS.contains(view);
    }

    private static View authView(String id, String title, String file) {
        return new View(id, title, UserApplication.class, "/com/chatsystem/user/fxml/" + file);
    }
}
