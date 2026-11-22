package com.chatsystem.admin.controller.logs;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockLoginLog;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

public final class LoginLogsController {
    private static final PseudoClass DAY_START = PseudoClass.getPseudoClass("day-start");
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(MockData.DISPLAY_ZONE);

    @FXML private Label loginCount;
    @FXML private TableView<MockLoginLog> loginEvents;
    @FXML private TableColumn<MockLoginLog, String> loggedInAt;
    @FXML private TableColumn<MockLoginLog, String> username;
    @FXML private TableColumn<MockLoginLog, String> fullName;

    @FXML
    private void initialize() {
        MockData data = MockData.create();
        loggedInAt.setCellValueFactory(row ->
                new ReadOnlyStringWrapper(DATE_TIME.format(row.getValue().loggedInAt())));
        username.setCellValueFactory(row ->
                new ReadOnlyStringWrapper(data.user(row.getValue().userId()).username()));
        fullName.setCellValueFactory(row ->
                new ReadOnlyStringWrapper(data.user(row.getValue().userId()).fullName()));
        loginEvents.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        loginEvents.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(MockLoginLog log, boolean empty) {
                super.updateItem(log, empty);
                int index = getIndex();
                boolean startsDay = !empty && (index == 0
                        || !displayDate(log).equals(displayDate(table.getItems().get(index - 1))));
                pseudoClassStateChanged(DAY_START, startsDay);
            }
        });
        loginEvents.getItems().setAll(data.loginLogs());
        loginCount.setText(loginEvents.getItems().size() + " lượt đăng nhập");
    }

    private static LocalDate displayDate(MockLoginLog log) {
        return log.loggedInAt().atZone(MockData.DISPLAY_ZONE).toLocalDate();
    }
}
