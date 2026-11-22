package com.chatsystem.admin.controller.dialogs;

import com.chatsystem.admin.mock.UserManagementFixtures;
import com.chatsystem.admin.navigation.AdminViewInputs;
import com.chatsystem.admin.navigation.AdminViewInputs.Action;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class ConfirmActionController implements ViewController {
    @FXML private VBox confirmation;
    @FXML private Label confirmationTitle;
    @FXML private Label account;
    @FXML private Label accountId;
    @FXML private Label consequences;
    @FXML private Label recipient;
    @FXML private Label preservationNotice;

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        if (!(input instanceof AdminViewInputs.ConfirmAction action)) {
            return;
        }
        var user = UserManagementFixtures.findUser(action.userId()).orElse(null);
        Prompt prompt = promptFor(action.action());
        confirmationTitle.setText(prompt.title());
        account.setText(user == null ? "Tài khoản chưa có trong dữ liệu mẫu" : "@" + user.username());
        accountId.setText("ID " + action.userId());
        consequences.setText(prompt.consequences());
        boolean reportedAccount = action.action() == Action.LOCK_REPORTED_USER;
        preservationNotice.setManaged(reportedAccount);
        preservationNotice.setVisible(reportedAccount);
        if (action.action() == Action.RESET_PASSWORD && user != null) {
            recipient.setText("Email nhận · " + user.email());
            recipient.setManaged(true);
            recipient.setVisible(true);
        }
        if (confirmation.getParent() instanceof DialogPane pane) {
            UserDialogButtons.install(pane, prompt.submitText(), prompt.destructive()).setDisable(user == null);
        }
    }

    private static Prompt promptFor(Action action) {
        return switch (action) {
            case LOCK_USER -> new Prompt("Khóa tài khoản?", "Xác nhận khóa",
                    "Các phiên đang hoạt động sẽ bị thu hồi ngay khi khóa", true);
            case LOCK_REPORTED_USER -> new Prompt("Xác nhận khóa tài khoản", "Xác nhận khóa",
                    "Khóa tài khoản sẽ thu hồi ngay toàn bộ phiên đang hoạt động của tài khoản này.", true);
            case UNLOCK_USER -> new Prompt("Mở khóa tài khoản?", "Xác nhận mở khóa",
                    "Người dùng có thể đăng nhập lại khi đủ điều kiện xác thực", false);
            case SOFT_DELETE_USER -> new Prompt("Xóa mềm tài khoản?", "Xóa mềm 30 ngày",
                    "Tài khoản sẽ ngừng nhận tin nhắn mới và bị thu hồi các phiên. Có thể khôi phục trong 30 ngày.", true);
            case RESTORE_USER -> new Prompt("Khôi phục tài khoản?", "Xác nhận khôi phục",
                    "Tài khoản trở về trạng thái trước khi xóa mềm", false);
            case RESET_PASSWORD -> new Prompt("Đặt lại mật khẩu?", "Xác nhận đặt lại",
                    "Mật khẩu tạm được gửi qua email và có hiệu lực 15 phút", false);
        };
    }

    private record Prompt(String title, String submitText, String consequences, boolean destructive) {
    }
}
