package com.chatsystem.admin.controller.dialogs;

import com.chatsystem.admin.mock.UserManagementFixtures;
import com.chatsystem.admin.navigation.AdminViewInputs;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalDateStringConverter;

public final class EditUserController implements ViewController {
    private static final String UNSPECIFIED = "Chưa cung cấp";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    @FXML private VBox userForm;
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    @FXML private Label usernameLabel;
    @FXML private TextField username;
    @FXML private TextField fullName;
    @FXML private TextField email;
    @FXML private TextField address;
    @FXML private DatePicker birthDate;
    @FXML private ComboBox<String> gender;

    @FXML
    private void initialize() {
        birthDate.setConverter(new LocalDateStringConverter(DATE_FORMAT, DATE_FORMAT));
        gender.getItems().setAll(UNSPECIFIED, "Nam", "Nữ", "Khác");
        gender.getSelectionModel().selectFirst();
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        boolean editing = input instanceof AdminViewInputs.EditUser;
        MockUser user = input instanceof AdminViewInputs.EditUser edit
                ? UserManagementFixtures.findUser(edit.userId()).orElse(null) : null;
        if (editing) {
            formTitle.setText("Sửa hồ sơ");
            usernameLabel.setText("Tên đăng nhập · chỉ đọc");
            username.setEditable(false);
            formSubtitle.setText(user == null ? "Tài khoản chưa có trong dữ liệu mẫu" : "ID " + user.id());
        }
        if (user != null) {
            username.setText(user.username());
            fullName.setText(user.fullName());
            email.setText(user.email());
            address.setText(user.address());
            birthDate.setValue(user.birthDate().orElse(null));
            gender.setValue(switch (user.gender()) {
                case MALE -> "Nam";
                case FEMALE -> "Nữ";
                case UNSPECIFIED -> UNSPECIFIED;
            });
        }

        if (userForm.getParent() instanceof DialogPane pane) {
            var submit = UserDialogButtons.install(pane, editing ? "Lưu hồ sơ" : "Tạo tài khoản", false);
            submit.disableProperty().bind(Bindings.createBooleanBinding(
                    () -> username.getText().isBlank() || fullName.getText().isBlank() || email.getText().isBlank(),
                    username.textProperty(), fullName.textProperty(), email.textProperty()));
        }
    }
}
