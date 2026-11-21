package com.chatsystem.admin;

import com.chatsystem.desktop.navigation.View;
import java.util.List;

public final class AdminViews {
    public static final View SIGN_IN = view("A-SCR-01", "Đăng nhập quản trị", "auth/sign-in.fxml");
    public static final View OVERVIEW = view("A-SCR-02", "Tổng quan", "overview/overview.fxml");
    public static final View USERS = view("A-SCR-03", "Quản lý người dùng", "users/users.fxml");
    public static final View GROUPS = view("A-SCR-04", "Quản lý Nhóm Chat", "groups/groups.fxml");
    public static final View SPAM = view("A-SCR-05", "Kiểm duyệt Báo cáo Spam", "spam/spam.fxml");
    public static final View LOGIN_LOGS = view("A-SCR-06", "Nhật ký đăng nhập", "logs/login-logs.fxml");
    public static final View REGISTRATIONS = view("A-SCR-07", "Dashboard Đăng ký mới",
            "analytics/registrations.fxml");
    public static final View ACTIVITY = view("A-SCR-08", "Dashboard Hoạt động Người dùng",
            "analytics/activity.fxml");
    public static final View FRIEND_NETWORK = view("A-SCR-09", "Phân tích Mạng lưới Bạn bè",
            "analytics/friend-network.fxml");

    public static final View EDIT_USER = view("A-DLG-01", "Hồ sơ người dùng", "dialogs/edit-user.fxml");
    public static final View CONFIRM_ACTION = view("A-DLG-02", "Xác nhận thao tác",
            "dialogs/confirm-action.fxml");

    public static final List<View> ALL = List.of(SIGN_IN, OVERVIEW, USERS, GROUPS, SPAM, LOGIN_LOGS,
            REGISTRATIONS, ACTIVITY, FRIEND_NETWORK, EDIT_USER, CONFIRM_ACTION);
    public static final List<View> PAGES = List.of(OVERVIEW, USERS, GROUPS, SPAM, LOGIN_LOGS,
            REGISTRATIONS, ACTIVITY, FRIEND_NETWORK);

    private AdminViews() {
    }

    private static View view(String id, String title, String path) {
        return new View(id, title, AdminApplication.class, "/com/chatsystem/admin/fxml/" + path);
    }
}
