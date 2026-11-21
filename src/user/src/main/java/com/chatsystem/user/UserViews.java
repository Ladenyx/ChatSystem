package com.chatsystem.user;

import com.chatsystem.desktop.navigation.View;

public final class UserViews {
    public static final View CONVERSATIONS = new View("U-SCR-07", "Hội thoại",
            UserApplication.class, "/com/chatsystem/user/fxml/conversations.fxml");
    public static final View CONTACTS = View.placeholder("U-SCR-08", "Bạn bè & Kết nối");
    public static final View PROFILE = View.placeholder("U-DLG-01", "Hồ sơ & Bảo mật");
    public static final View CREATE_GROUP = View.placeholder("U-DLG-02", "Tạo nhóm chat mới");
    public static final View CONVERSATION_INFO = new View("U-SCR-09", "Thông tin đoạn chat",
            UserApplication.class, "/com/chatsystem/user/fxml/conversation-info.fxml");

    private UserViews() {
    }
}
