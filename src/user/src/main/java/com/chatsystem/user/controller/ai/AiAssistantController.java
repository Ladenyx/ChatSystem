package com.chatsystem.user.controller.ai;

import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.navigation.UserViewInputs;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class AiAssistantController implements ViewController {
    @FXML private Label heading;
    @FXML private VBox body;
    @FXML private VBox disclosure;
    @FXML private HBox footer;

    private ViewNavigator navigator;

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        this.navigator = navigator;
        if (!(input instanceof UserViewInputs.AiAssistant preview)) {
            heading.setText(view.title());
            return;
        }
        boolean refine = preview.mode() == UserViewInputs.AiMode.REFINE;
        heading.setText(refine ? "Chỉnh sửa bản nháp" : "Gợi ý trả lời");

        if (refine) {
            var draft = new TextArea(preview.draft());
            draft.setWrapText(true);
            draft.setPrefRowCount(4);
            draft.setAccessibleText("Bản sao bản nháp");
            var tone = new ComboBox<>(FXCollections.observableArrayList("Rõ ràng", "Thân thiện", "Trang trọng"));
            tone.getSelectionModel().selectFirst();
            tone.setAccessibleText("Giọng văn");
            body.getChildren().setAll(new Label("Xem trước bản nháp"), draft, new Label("Giọng văn"), tone);
        } else {
            showContext(preview.conversationId());
        }

        var dataNotice = new Label("Khi tích hợp AI, nội dung đã chọn có thể được chia sẻ dưới dạng bản rõ.");
        dataNotice.getStyleClass().add("chat-caption");
        dataNotice.setWrapText(true);
        var consent = new CheckBox("Tôi đồng ý chia sẻ nội dung để dùng AI");
        consent.setWrapText(true);
        disclosure.getChildren().setAll(dataNotice, consent);

        var generate = new Button(refine ? "Tạo đề xuất" : "Gợi ý trả lời");
        generate.getStyleClass().add("chat-primary");
        generate.setDisable(true);
        var cancel = new Button("Hủy");
        cancel.setOnAction(event -> close());
        footer.getChildren().setAll(cancel, generate);
    }

    private void showContext(String conversationId) {
        body.getChildren().setAll(new Label("Chọn tối đa 5 tin nhắn làm ngữ cảnh"));
        var catalog = UserViews.CONVERSATIONS_DATA;
        catalog.messagesFor(conversationId).reversed().stream()
                .filter(message -> message.state() != MockMessage.State.RECALLED)
                .limit(5)
                .forEach(message -> {
                    var row = new CheckBox(catalog.user(message.senderId()).fullName() + " · " + message.text());
                    row.setWrapText(true);
                    body.getChildren().add(row);
                });
        if (body.getChildren().size() == 1) {
            body.getChildren().add(new Label("Chưa có tin nhắn"));
        }
    }

    @FXML
    private void close() {
        navigator.closePopover();
    }
}
