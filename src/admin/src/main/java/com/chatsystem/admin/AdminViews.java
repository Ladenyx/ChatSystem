package com.chatsystem.admin;

import com.chatsystem.desktop.navigation.View;
import java.util.List;

public final class AdminViews {
    public static final View OVERVIEW = View.placeholder("A-SCR-02", "Dashboard Tổng quan");
    public static final View USERS = View.placeholder("A-SCR-03", "Quản lý Người dùng");
    public static final View GROUPS = View.placeholder("A-SCR-04", "Quản lý Nhóm Chat");
    public static final View SPAM = View.placeholder("A-SCR-05", "Kiểm duyệt Báo cáo Spam");
    public static final View LOGIN_LOGS = View.placeholder("A-SCR-06", "Nhật ký Đăng nhập");
    public static final View REGISTRATIONS = View.placeholder("A-SCR-07", "Dashboard Đăng ký mới");
    public static final View ACTIVITY = View.placeholder("A-SCR-08", "Dashboard Hoạt động Người dùng");
    public static final View FRIEND_NETWORK = View.placeholder("A-SCR-09", "Phân tích Mạng lưới Bạn bè");
    public static final View EDIT_USER = View.placeholder("A-DLG-01", "Thêm / Cập nhật Người dùng");
    public static final List<View> PAGES = List.of(OVERVIEW, USERS, GROUPS, SPAM, LOGIN_LOGS,
            REGISTRATIONS, ACTIVITY, FRIEND_NETWORK);

    private AdminViews() {
    }
}
