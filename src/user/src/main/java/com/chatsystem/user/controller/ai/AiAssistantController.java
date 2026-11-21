package com.chatsystem.user.controller.ai;

import atlantafx.base.controls.Popover;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.controller.ai.OfflineAiAssistantService.Mode;
import com.chatsystem.user.controller.ai.OfflineAiAssistantService.Request;
import com.chatsystem.user.controller.ai.OfflineAiAssistantService.Scenario;
import com.chatsystem.user.controller.ai.OfflineAiAssistantService.Tone;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public final class AiAssistantController implements ViewController {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm").withZone(MockData.DISPLAY_ZONE);

    /** A snapshot of the active composer, with live checks for access, draft revisions and context. */
    public record Input(Mode mode, String conversationTitle, String originalDraft,
                        Supplier<List<MockMessage>> readableHistory, Function<String, String> senderName,
                        BooleanSupplier isCurrent, Consumer<String> applyDraft) {}

    @FXML private Label heading;
    @FXML private ScrollPane scroll;
    @FXML private VBox body;
    @FXML private VBox disclosure;
    @FXML private HBox footer;

    private final OfflineAiAssistantService assistant = new OfflineAiAssistantService();
    private final Set<String> selectedIds = new LinkedHashSet<>();
    private final Map<String, String> contextCopies = new LinkedHashMap<>();
    private final PauseTransition pending = new PauseTransition(Duration.millis(650));
    private ViewNavigator navigator;
    private Input input;
    private String refineCopy;
    private Tone tone = Tone.CLEAR;
    private Scenario scenario = Scenario.SUCCESS;

    @FXML
    private void initialize() {
        disclosure.visibleProperty().bind(Bindings.isNotEmpty(disclosure.getChildren()));
        disclosure.managedProperty().bind(disclosure.visibleProperty());
    }

    public static void open(ViewNavigator navigator, Node anchor, Input input) {
        var properties = navigator.getRoot().getProperties();
        properties.put(Input.class, input);
        try {
            navigator.showPopover(UserViews.AI_ASSISTANT, anchor, popup -> {
                popup.setArrowLocation(Popover.ArrowLocation.BOTTOM_LEFT);
                popup.setHeaderAlwaysVisible(false);
                popup.setCloseButtonEnabled(false);
            });
        } finally {
            properties.remove(Input.class);
        }
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
        input = (Input) navigator.getRoot().getProperties().get(Input.class);
        refineCopy = input.originalDraft();
        showPreview("");
    }

    @Override
    public void onLeave() {
        pending.stop();
        pending.setOnFinished(null);
        input = null;
        navigator = null;
    }

    @FXML
    private void close() {
        navigator.closePopover();
    }

    private boolean isCurrent() {
        return input != null && input.isCurrent().getAsBoolean();
    }

    private void showPreview(String warning) {
        if (!isCurrent()) {
            showStale();
            return;
        }
        boolean refine = input.mode() == Mode.REFINE;
        reset(refine ? "Chỉnh sửa bản nháp" : "Gợi ý trả lời", refine ? 228 : 354);
        Button confirm = button("aiConfirm", refine ? "Chia sẻ và tạo phương án" : "Chia sẻ và tạo gợi ý", this::submit, true);
        if (refine) {
            showRefineInput(confirm);
        } else {
            showContextPicker(confirm);
        }
        disclosure.getChildren().add(text("Trong bản tích hợp, nội dung xem trước sẽ được gửi dưới dạng bản rõ tới server ứng dụng "
                + "và Meta Model API, kể cả trong nhóm E2EE.", "chat-row-name"));
        disclosure.getChildren().add(text("Ở chế độ Contributor, Meta có thể dùng nội dung và phản hồi để huấn luyện mô hình. "
                + "Bản demo chỉ xử lý cục bộ.", "chat-caption"));
        var scenarios = new ComboBox<Scenario>();
        scenarios.setId("aiScenario");
        scenarios.setAccessibleText("Tình huống demo");
        scenarios.getItems().setAll(Scenario.values());
        scenarios.setValue(scenario);
        scenarios.valueProperty().addListener((observable, old, next) -> scenario = next);
        var demo = new HBox(8, text("Tình huống demo", "chat-caption"), scenarios);
        demo.setAlignment(Pos.CENTER_LEFT);
        body.getChildren().add(demo);
        if (!warning.isBlank()) {
            disclosure.getChildren().add(text(warning, "chat-warning-text"));
        }
        footer.getChildren().addAll(button("aiCancel", "Hủy", this::close, false), confirm);
    }

    private void showRefineInput(Button confirm) {
        var copy = editor("aiRefineInput", refineCopy);
        copy.setAccessibleText("Nội dung gửi AI");
        confirm.setDisable(refineCopy.isBlank());
        copy.textProperty().addListener((observable, old, value) -> {
            refineCopy = value;
            confirm.setDisable(value.isBlank());
        });
        var tones = new ToggleGroup();
        var options = new HBox(8);
        for (Tone style : Tone.values()) {
            var option = new ToggleButton(style.toString());
            option.setId("aiTone-" + style.name());
            option.getStyleClass().add("chat-outline");
            option.setToggleGroup(tones);
            option.setSelected(style == tone);
            option.setOnAction(event -> {
                if (!option.isSelected()) {
                    option.setSelected(true);
                }
                tone = style;
            });
            options.getChildren().add(option);
        }
        body.getChildren().addAll(text("Nội dung gửi AI", "chat-result-title"), copy,
                text("Kiểu diễn đạt", "chat-result-title"), options,
                text("Chỉ dùng phần nháp trên và kiểu diễn đạt đã chọn.", "chat-caption"));
    }

    private void showContextPicker(Button confirm) {
        List<MockMessage> messages = input.readableHistory().get();
        var list = new VBox(8);
        list.setMinWidth(0);
        var summary = text("", "chat-caption");
        summary.setId("aiContextSummary");
        var edit = button("aiEditContext", "Chỉnh nội dung gửi AI", this::showContextEditor, false);
        edit.setMaxWidth(Double.MAX_VALUE);
        var choices = new ArrayList<CheckBox>();
        Runnable update = () -> {
            int count = selectedIds.size();
            summary.setText("Đã chọn " + count + " / " + OfflineAiAssistantService.MAX_CONTEXT_MESSAGES
                    + " tin · Theo thứ tự hội thoại");
            confirm.setDisable(count == 0);
            edit.setDisable(count == 0);
            for (var choice : choices) {
                choice.setDisable(count >= OfflineAiAssistantService.MAX_CONTEXT_MESSAGES && !choice.isSelected());
            }
        };
        for (var message : messages) {
            var choice = new CheckBox();
            choice.setId("aiContext-" + message.id());
            choice.setSelected(selectedIds.contains(message.id()));
            choice.setOnAction(event -> {
                if (choice.isSelected()) {
                    selectedIds.add(message.id());
                } else {
                    selectedIds.remove(message.id());
                }
                update.run();
            });
            choices.add(choice);
            list.getChildren().add(choiceCard(choice, messageTitle(message),
                    contextCopies.getOrDefault(message.id(), message.text())));
        }
        update.run();
        body.getChildren().add(text("Chọn tin trong đoạn chat " + input.conversationTitle(), "chat-result-title"));
        if (messages.isEmpty()) {
            body.getChildren().add(text("Không có tin nhắn ngữ cảnh hợp lệ.", "chat-caption"));
        } else {
            var history = new ScrollPane(list);
            history.setFitToWidth(true);
            history.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            history.setPrefViewportHeight(Math.min(190, messages.size() * 72));
            history.getStyleClass().add("chat-search-scroll");
            body.getChildren().add(history);
        }
        body.getChildren().addAll(summary, edit,
                text("Chỉ dùng các tin được chọn. Nháp và các tin khác không được kèm theo.", "chat-caption"));
    }

    private void showContextEditor() {
        if (!isCurrent()) {
            showStale();
            return;
        }
        reset("Chỉnh phần ngữ cảnh gửi AI", 318);
        body.getChildren().add(text("Bạn đang chỉnh bản sao. Tin nguồn và bản nháp chat giữ nguyên.", "chat-caption"));
        var fields = new LinkedHashMap<String, TextArea>();
        for (var message : selectedMessages()) {
            var field = editor("aiContextEdit-" + message.id(), contextCopies.getOrDefault(message.id(), message.text()));
            field.setAccessibleText(messageTitle(message));
            fields.put(message.id(), field);
            body.getChildren().addAll(text(messageTitle(message), "chat-result-title"), field);
        }
        if (fields.size() != selectedIds.size()) {
            checkContext();
            return;
        }
        var review = button("aiReviewContext", "Xem trước lại", () -> {
            fields.forEach((id, field) -> contextCopies.put(id, field.getText()));
            showPreview("");
        }, true);
        Runnable update = () -> review.setDisable(fields.values().stream().anyMatch(field -> field.getText().isBlank()));
        fields.values().forEach(field -> field.textProperty().addListener((observable, old, value) -> update.run()));
        update.run();
        footer.getChildren().addAll(button("aiCancelEdit", "Hủy chỉnh sửa", () -> showPreview(""), false), review);
    }

    private List<MockMessage> selectedMessages() {
        return input.readableHistory().get().stream().filter(message -> selectedIds.contains(message.id())).toList();
    }

    private boolean checkContext() {
        if (input.mode() == Mode.RECOMMEND && selectedMessages().size() != selectedIds.size()) {
            List<String> available = input.readableHistory().get().stream().map(MockMessage::id).toList();
            selectedIds.retainAll(available);
            contextCopies.keySet().retainAll(selectedIds);
            showPreview("Một số tin không còn khả dụng. Hãy xem lại nội dung và xác nhận lại.");
            return false;
        }
        return true;
    }

    private void submit() {
        if (!isCurrent()) {
            showStale();
            return;
        }
        if (!checkContext()) {
            return;
        }
        Request request;
        try {
            request = input.mode() == Mode.REFINE ? new OfflineAiAssistantService.Refine(refineCopy, tone)
                    : new OfflineAiAssistantService.Recommend(selectedMessages().stream()
                            .map(message -> contextCopies.getOrDefault(message.id(), message.text())).toList());
        } catch (IllegalArgumentException error) {
            showPreview(error.getMessage());
            return;
        }
        Scenario confirmedScenario = scenario;
        reset("Đang tạo phương án...", 96);
        var spinner = new ProgressIndicator();
        spinner.setId("aiLoading");
        spinner.setPrefSize(28, 28);
        body.getChildren().addAll(spinner, text("Đang xử lý phần nội dung bạn đã xác nhận.", "chat-caption"));
        footer.getChildren().add(button("aiStop", "Ngừng chờ", this::close, false));
        pending.setOnFinished(event -> {
            if (!isCurrent()) {
                if (input != null) {
                    showStale();
                }
                return;
            }
            if (!checkContext()) {
                return;
            }
            var result = assistant.generate(request, confirmedScenario);
            if (result.scenario() == Scenario.SUCCESS) {
                showResults(result.suggestions());
            } else {
                showError(result.scenario());
            }
        });
        pending.playFromStart();
    }

    private void showResults(List<String> suggestions) {
        reset("Chọn phương án", 284);
        body.getChildren().add(text("Kiểm tra nội dung trước khi dùng làm nháp.", "chat-caption"));
        var choices = new ArrayList<CheckBox>();
        var apply = button("aiApply", "Dùng làm nháp", () -> {
            if (!isCurrent()) {
                showStale();
            } else if (checkContext()) {
                choices.stream().filter(CheckBox::isSelected).findFirst().ifPresent(option -> {
                    input.applyDraft().accept((String) option.getUserData());
                    close();
                });
            }
        }, true);
        apply.setDisable(true);
        for (int i = 0; i < suggestions.size(); i++) {
            String content = suggestions.get(i);
            int number = i + 1;
            var choice = new CheckBox();
            choice.setId("aiOption-" + number);
            choice.setUserData(content);
            choice.setOnAction(event -> {
                if (choice.isSelected()) {
                    choices.stream().filter(other -> other != choice).forEach(other -> other.setSelected(false));
                }
                apply.setDisable(!choice.isSelected());
            });
            choices.add(choice);
            body.getChildren().add(choiceCard(choice, "Phương án " + number, content));
        }
        body.getChildren().add(text("Phương án chỉ thay nội dung ô soạn. Bạn vẫn cần nhấn Gửi để gửi tin.", "chat-caption"));
        footer.getChildren().addAll(button("aiReviewInput", "Xem lại đầu vào", () -> showPreview(""), false), apply);
    }

    private void showError(Scenario outcome) {
        reset("Chưa tạo được phương án", 96);
        String message = switch (outcome) {
            case ERROR -> "Không thể kết nối với trợ lý AI (mô phỏng).";
            case TIMEOUT -> "Đã quá thời gian chờ 15 giây (mô phỏng).";
            case QUOTA -> "Trợ lý AI đã hết hạn mức (mô phỏng).";
            case SUCCESS -> throw new IllegalArgumentException("Expected a simulated error");
        };
        var error = text(message, "chat-warning-text");
        error.setId("aiError");
        body.getChildren().addAll(error, text("Bản nháp vẫn được giữ nguyên.", "chat-caption"));
        footer.getChildren().addAll(button("aiReviewInput", "Xem lại trước khi thử", () -> showPreview(""), false),
                button("aiContinue", "Tiếp tục soạn", this::close, true));
    }

    private void showStale() {
        reset("Bản nháp hoặc quyền soạn đã đổi", 86);
        var warning = text("Phương án không còn phù hợp với phiên soạn này. Bản nháp hiện tại vẫn được giữ.",
                "chat-caption");
        warning.setId("aiStale");
        body.getChildren().add(warning);
        footer.getChildren().add(button("aiContinue", "Tiếp tục soạn", this::close, true));
    }

    private void reset(String title, double bodyHeight) {
        heading.setText(title);
        body.getChildren().clear();
        disclosure.getChildren().clear();
        footer.getChildren().clear();
        scroll.setPrefViewportHeight(bodyHeight);
        scroll.setVvalue(0);
    }

    private String messageTitle(MockMessage message) {
        return input.senderName().apply(message.senderId()) + " · " + TIME.format(message.sentAt());
    }

    private static HBox choiceCard(CheckBox choice, String title, String content) {
        choice.setAccessibleText(title + " · " + content);
        var labels = new VBox(4, text(title, "chat-result-title"), text(content, "chat-row-name"));
        labels.setMinWidth(0);
        labels.setPrefWidth(420);
        HBox.setHgrow(labels, Priority.ALWAYS);
        var card = new HBox(12, choice, labels);
        card.setMinWidth(0);
        card.setFillHeight(false);
        card.getStyleClass().add("chat-ai-choice");
        var selected = PseudoClass.getPseudoClass("selected");
        card.pseudoClassStateChanged(selected, choice.isSelected());
        choice.selectedProperty().addListener((observable, oldValue, newValue) ->
                card.pseudoClassStateChanged(selected, newValue));
        choice.focusedProperty().addListener((observable, oldValue, newValue) ->
                card.pseudoClassStateChanged(PseudoClass.getPseudoClass("choice-focused"), newValue));
        labels.setOnMouseClicked(event -> choice.fire());
        return card;
    }

    private static TextArea editor(String id, String content) {
        var edit = new TextArea(content);
        edit.setId(id);
        edit.setWrapText(true);
        edit.setPrefRowCount(2);
        return edit;
    }

    private static Label text(String content, String style) {
        var label = new Label(content);
        label.setWrapText(true);
        label.getStyleClass().add(style);
        return label;
    }

    private static Button button(String id, String text, Runnable action, boolean primary) {
        var button = new Button(text);
        button.setId(id);
        button.getStyleClass().add(primary ? "chat-primary" : "chat-outline");
        button.setOnAction(event -> action.run());
        return button;
    }
}
