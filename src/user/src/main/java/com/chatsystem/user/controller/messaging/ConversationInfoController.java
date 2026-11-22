package com.chatsystem.user.controller.messaging;

import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockGroup;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewInput;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserApplication;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.control.SearchResultCard;
import com.chatsystem.user.controller.group.GroupFormController;
import com.chatsystem.user.messaging.ConversationCatalog;
import com.chatsystem.user.navigation.UserViewInputs;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public final class ConversationInfoController implements ViewController {
    private static final PseudoClass MENU_OPEN = PseudoClass.getPseudoClass("menu-open");
    private static final String TOKENS_CSS = Objects.requireNonNull(
            UserApplication.class.getResource("/com/chatsystem/user/css/chat-tokens.css")).toExternalForm();
    private static final String CHAT_CSS = Objects.requireNonNull(
            UserApplication.class.getResource("/com/chatsystem/user/css/chat.css")).toExternalForm();

    @FXML private BorderPane drawerLayout;
    @FXML private Button back;
    @FXML private Label heading;
    @FXML private ScrollPane infoPane;
    @FXML private VBox infoContent;
    @FXML private VBox searchPane;
    @FXML private TextField conversationSearchField;
    @FXML private Label resultCount;
    @FXML private Button previousResult;
    @FXML private Button nextResult;
    @FXML private VBox localResults;

    private final ConversationCatalog catalog = UserViews.CONVERSATIONS_DATA;
    private final Runnable catalogListener = this::onCatalogChanged;
    private final List<SearchResultCard> cards = new ArrayList<>();
    private ViewNavigator navigator;
    private UserViewInputs.MessageSearch search;
    private MockConversation currentChat;
    private List<MockMessage> matches = List.of();
    private int currentIndex = -1;
    private Popup memberActionPopup;
    private HBox activeMenuRow;
    private Dialog<ButtonType> activeDialog;

    @FXML
    private void initialize() {
        back.managedProperty().bind(back.visibleProperty());
        conversationSearchField.textProperty().addListener((observable, old, query) -> {
            if (search != null) {
                renderResults(query);
            }
        });
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view, ViewInput input) {
        this.navigator = navigator;
        catalog.addChangeListener(catalogListener);
        if (input instanceof UserViewInputs.MessageSearch details) {
            search = details;
            catalog.conversations().stream().filter(chat -> chat.id().equals(details.conversationId()))
                    .findFirst().ifPresent(this::renderInfo);
        }
        showInfo();
    }

    @Override
    public void onLeave() {
        closeMemberMenu();
        closeConfirmDialog();
        catalog.removeChangeListener(catalogListener);
        navigator = null;
    }

    private void onCatalogChanged() {
        if (navigator == null || search == null) {
            return;
        }
        catalog.conversations().stream()
                .filter(chat -> chat.id().equals(search.conversationId()))
                .findFirst()
                .ifPresent(this::renderInfo);
        if (drawerLayout.getCenter() == searchPane) {
            renderResults(conversationSearchField.getText());
        }
    }

    private void renderInfo(MockConversation chat) {
        closeMemberMenu();
        currentChat = chat;
        var avatar = new ConversationAvatar();
        avatar.getStyleClass().add("chat-info-avatar");
        avatar.show(chat, catalog);

        var title = new Label(chat.title());
        title.getStyleClass().add("chat-info-title");
        title.setWrapText(true);
        title.setTextAlignment(TextAlignment.CENTER);

        var status = new Label(catalog.statusText(chat));
        status.getStyleClass().add("chat-header-status");
        status.setGraphic(ConversationAvatar.statusGraphic(chat, catalog));
        status.setGraphicTextGap(6);

        var profile = new VBox(8, avatar, title, status);
        profile.setAlignment(Pos.CENTER);

        var identity = new VBox(12, profile,
                action("searchInConversation", "fth-search", "Tìm kiếm trong hội thoại", false, this::showSearch));
        identity.getStyleClass().addAll("chat-drawer-section", "chat-drawer-identity");
        infoContent.getChildren().setAll(identity);

        if (chat.encrypted()) {
            List<MockGroup.Member> groupMembers = resolveGroupMembers(chat);
            boolean currentUserIsAdmin = catalog.isCurrentUserGroupAdmin(chat.id());
            boolean hasEligibleSuccessor = groupMembers.stream()
                    .map(MockGroup.Member::userId)
                    .filter(id -> !catalog.currentUserId().equals(id))
                    .map(catalog::user)
                    .anyMatch(user -> user.status() == MockUser.Status.ACTIVE);

            var memberRows = new VBox(4);
            for (MockGroup.Member member : groupMembers) {
                MockUser user = catalog.user(member.userId());
                boolean isAdmin = member.role() == MockGroup.Role.ADMIN;
                boolean canManage = catalog.canManageGroupMember(chat.id(), member.userId());
                memberRows.getChildren().add(memberRow(chat, user, isAdmin, canManage));
            }

            Label membersHeading = sectionLabel("Thành viên (" + groupMembers.size() + ")");
            membersHeading.setId("groupMembersHeading");
            var members = new VBox(8, membersHeading,
                    action("addConversationMember", "fth-user-plus", "Thêm thành viên", false,
                            () -> openGroupForm(chat, GroupFormController.Mode.ADD)),
                    memberRows);
            members.getStyleClass().add("chat-drawer-section");
            infoContent.getChildren().add(members);

            var groupButtons = new VBox(4);
            if (currentUserIsAdmin) {
                groupButtons.getChildren().add(action("renameConversationGroup", "fth-edit-2", "Đổi tên nhóm", false,
                        () -> openGroupForm(chat, GroupFormController.Mode.RENAME)));
                groupButtons.getChildren().add(action("resignConversationAdmin", "fth-shield", "Chọn Admin kế nhiệm", false,
                        hasEligibleSuccessor ? () -> openGroupForm(chat, GroupFormController.Mode.SUCCESSOR) : null));
            }
            groupButtons.getChildren().addAll(
                    action("clearConversationHistory", "fth-trash-2", "Xóa lịch sử phía mình", false, null),
                    action("reportConversation", "fth-flag", "Báo cáo spam", true,
                            () -> navigator.showModal(UserViews.REPORT_SPAM,
                                    new UserViewInputs.ReportSpam(chat.id(), List.of()), true)),
                    action("leaveConversationGroup", "fth-log-out", "Rời nhóm", true, null));
            var groupOptions = new VBox(8, sectionLabel("Quản lý nhóm"), groupButtons);
            groupOptions.getStyleClass().add("chat-drawer-section");
            infoContent.getChildren().add(groupOptions);
        } else {
            var optionButtons = new VBox(4,
                    action("blockConversationPeer", "fth-slash", "Chặn người dùng", true, null),
                    action("clearConversationHistory", "fth-trash-2", "Xóa lịch sử phía mình", false, null),
                    action("reportConversation", "fth-flag", "Báo cáo spam", true,
                            () -> navigator.showModal(UserViews.REPORT_SPAM,
                                    new UserViewInputs.ReportSpam(chat.id(), List.of()), true)));
            var options = new VBox(8, sectionLabel("Tùy chọn hội thoại"), optionButtons);
            options.getStyleClass().add("chat-drawer-section");
            infoContent.getChildren().add(options);
        }
    }

    private List<MockGroup.Member> resolveGroupMembers(MockConversation chat) {
        List<MockGroup.Member> raw = catalog.groupMembers(chat.id());
        if (raw.isEmpty()) {
            return chat.participantIds().stream()
                    .map(id -> new MockGroup.Member(id, MockGroup.Role.MEMBER))
                    .toList();
        }
        return raw.stream()
                .sorted(Comparator.comparing(member -> member.role() != MockGroup.Role.ADMIN))
                .toList();
    }

    private void openGroupForm(MockConversation chat, GroupFormController.Mode mode) {
        closeMemberMenu();
        var route = mode == GroupFormController.Mode.SUCCESSOR
                ? UserViews.ASSIGN_SUCCESSOR_ADMIN
                : UserViews.CREATE_GROUP;
        navigator.showModal(route, new GroupFormController.Input(mode, chat.id()), true);
    }

    private static Label sectionLabel(String text) {
        var label = new Label(text);
        label.getStyleClass().add("chat-section-label");
        return label;
    }

    private Button action(String id, String iconLiteral, String text, boolean danger, Runnable handler) {
        var icon = new FontIcon(iconLiteral);
        icon.setIconSize(15);
        var button = new Button(text, icon);
        button.setId(id);
        button.setWrapText(true);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add("chat-drawer-action");
        if (danger) {
            button.getStyleClass().add("chat-drawer-danger");
        }
        if (handler == null) {
            button.setDisable(true);
        } else {
            button.setOnAction(event -> handler.run());
        }
        return button;
    }

    private HBox memberRow(MockConversation chat, MockUser member, boolean isAdmin, boolean canManage) {
        Label badge = new Label(ConversationAvatar.initials(member.fullName()));
        badge.getStyleClass().add("chat-info-member-avatar");

        StackPane avatarWrap = new StackPane(badge);
        avatarWrap.getStyleClass().add("chat-info-member-avatar-wrap");
        avatarWrap.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        if (member.presence() == MockUser.Presence.ONLINE) {
            Region onlineDot = new Region();
            onlineDot.getStyleClass().add("chat-info-member-online");
            StackPane.setAlignment(onlineDot, Pos.BOTTOM_RIGHT);
            avatarWrap.getChildren().add(onlineDot);
        }

        String displayName = catalog.currentUserId().equals(member.id())
                ? member.fullName() + " (Bạn)"
                : member.fullName();
        Label name = new Label(displayName);
        name.getStyleClass().add("chat-info-member-name");
        name.setTextOverrun(OverrunStyle.ELLIPSIS);
        name.setMinWidth(0);

        HBox nameRow = new HBox(6, name);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        if (isAdmin) {
            Label roleBadge = new Label("Admin");
            roleBadge.setId("member-role-" + member.id());
            roleBadge.getStyleClass().add("chat-info-role-badge");
            roleBadge.setMinWidth(Region.USE_PREF_SIZE);
            nameRow.getChildren().add(roleBadge);
        }

        Label handle = new Label("@" + member.username());
        handle.getStyleClass().add("chat-info-member-handle");
        handle.setTextOverrun(OverrunStyle.ELLIPSIS);

        VBox text = new VBox(2, nameRow, handle);
        text.setMinWidth(0);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox row = new HBox(8, avatarWrap, text);
        row.setId("member-row-" + member.id());
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("chat-info-member");

        if (canManage) {
            Button more = new Button();
            more.setId("member-menu-" + member.id());
            more.getStyleClass().add("chat-info-member-more");
            more.setGraphic(FontIcon.of(Feather.MORE_HORIZONTAL, 16));
            more.setAccessibleText("Quản lý thành viên " + member.fullName());
            more.setTooltip(new Tooltip("Tùy chọn thành viên"));
            more.setOnAction(event -> openMemberMenu(row, chat, member, isAdmin, more));
            row.getChildren().add(more);
        }
        return row;
    }

    private void openMemberMenu(HBox row, MockConversation chat, MockUser member, boolean isAdmin, Button anchor) {
        if (memberActionPopup != null && memberActionPopup.isShowing() && activeMenuRow == row) {
            closeMemberMenu();
            return;
        }
        closeMemberMenu();

        VBox menu = new VBox(2);
        menu.setId("memberActionMenu");
        menu.getStyleClass().add("chat-message-menu");
        menu.setFocusTraversable(true);
        navigator.themes().apply(menu);
        menu.getStylesheets().addAll(TOKENS_CSS, CHAT_CSS);

        if (isAdmin) {
            menu.getChildren().add(memberMenuItem("memberActionRevokeAdmin", "Thu hồi quyền Admin",
                    Feather.SHIELD_OFF, false, () -> confirmAdminRoleChange(chat, member, false)));
        } else {
            menu.getChildren().add(memberMenuItem("memberActionGrantAdmin", "Cấp quyền Admin",
                    Feather.SHIELD, false, () -> confirmAdminRoleChange(chat, member, true)));
        }
        menu.getChildren().add(memberMenuItem("memberActionRemove", "Xóa khỏi nhóm",
                Feather.USER_MINUS, true, () -> confirmRemoveMember(chat, member)));

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        popup.setAnchorLocation(AnchorLocation.WINDOW_TOP_LEFT);
        popup.getContent().add(menu);
        popup.setOnHidden(event -> {
            if (activeMenuRow == row) {
                row.pseudoClassStateChanged(MENU_OPEN, false);
                activeMenuRow = null;
            }
            if (memberActionPopup == popup) {
                memberActionPopup = null;
            }
        });

        menu.applyCss();
        menu.layout();
        var menuBounds = menu.getBoundsInParent();
        double shadowOffsetX = -menuBounds.getMinX();
        double shadowOffsetY = -menuBounds.getMinY();
        double cardWidth = menu.prefWidth(-1);
        double cardHeight = menu.prefHeight(-1);

        var anchorBounds = anchor.localToScreen(anchor.getBoundsInLocal());
        var drawerBounds = infoPane.localToScreen(infoPane.getBoundsInLocal());
        var ownerWindow = anchor.getScene() != null ? anchor.getScene().getWindow() : null;

        double cardX = anchorBounds.getMaxX() - cardWidth;
        if (drawerBounds != null) {
            double minX = drawerBounds.getMinX() + 8;
            double maxX = Math.max(minX, drawerBounds.getMaxX() - cardWidth - 8);
            cardX = Math.clamp(cardX, minX, maxX);
        } else if (ownerWindow != null) {
            double minX = ownerWindow.getX() + 8;
            double maxX = Math.max(minX, ownerWindow.getX() + ownerWindow.getWidth() - cardWidth - 8);
            cardX = Math.clamp(cardX, minX, maxX);
        }

        double cardY = anchorBounds.getMaxY() + 4;
        double areaTop = drawerBounds != null ? drawerBounds.getMinY() + 8
                : (ownerWindow != null ? ownerWindow.getY() + 8 : cardY);
        double areaBottom = drawerBounds != null ? drawerBounds.getMaxY() - 8
                : (ownerWindow != null ? ownerWindow.getY() + ownerWindow.getHeight() - 8 : cardY + cardHeight);
        if (cardY + cardHeight > areaBottom) {
            cardY = anchorBounds.getMinY() - cardHeight - 4;
        }
        cardY = Math.clamp(cardY, areaTop, Math.max(areaTop, areaBottom - cardHeight));

        memberActionPopup = popup;
        activeMenuRow = row;
        row.pseudoClassStateChanged(MENU_OPEN, true);
        popup.show(anchor, cardX - shadowOffsetX, cardY - shadowOffsetY);
        menu.requestFocus();
    }

    private Button memberMenuItem(String id, String text, Feather icon, boolean danger, Runnable handler) {
        Button button = new Button(text, FontIcon.of(icon, 14));
        button.setId(id);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("chat-message-menu-item");
        if (danger) {
            button.getStyleClass().add("chat-message-menu-danger");
        }
        button.setOnAction(event -> {
            closeMemberMenu();
            handler.run();
        });
        return button;
    }

    private void closeMemberMenu() {
        if (memberActionPopup != null) {
            Popup closing = memberActionPopup;
            memberActionPopup = null;
            closing.hide();
        }
        if (activeMenuRow != null) {
            activeMenuRow.pseudoClassStateChanged(MENU_OPEN, false);
            activeMenuRow = null;
        }
    }

    private void confirmAdminRoleChange(MockConversation chat, MockUser member, boolean grantAdmin) {
        String title = grantAdmin
                ? "Cấp quyền Admin cho " + member.fullName()
                : "Thu hồi quyền Admin của " + member.fullName();
        String bodyText = grantAdmin
                ? member.fullName() + " sẽ có quyền quản trị nhóm tương đương bạn, bao gồm đổi tên nhóm, thêm hoặc xóa thành viên và quản lý quyền Admin."
                : member.fullName() + " sẽ trở về thành viên thường và không còn quyền quản trị nhóm, nhưng vẫn tiếp tục tham gia hội thoại.";
        String confirmText = grantAdmin ? "Cấp quyền" : "Thu hồi quyền";
        showConfirmDialog(
                "memberRoleConfirmDialog",
                title,
                "memberRoleConfirmTitle",
                "memberRoleConfirmClose",
                bodyText,
                "memberRoleConfirmCancel",
                "Hủy",
                "memberRoleConfirmSubmit",
                confirmText,
                "chat-primary",
                () -> catalog.setGroupAdminRole(chat.id(), member.id(), grantAdmin));
    }

    private void confirmRemoveMember(MockConversation chat, MockUser member) {
        String title = "Xóa " + member.fullName() + " khỏi nhóm";
        String bodyText = member.fullName()
                + " sẽ bị loại khỏi nhóm và không thể gửi hay nhận tin nhắn mới. Họ vẫn có thể xem lịch sử trước đây trên thiết bị của mình.";
        showConfirmDialog(
                "memberRemoveConfirmDialog",
                title,
                "memberRemoveConfirmTitle",
                "memberRemoveConfirmClose",
                bodyText,
                "memberRemoveConfirmCancel",
                "Hủy",
                "memberRemoveConfirmSubmit",
                "Xóa khỏi nhóm",
                "chat-danger-solid",
                () -> catalog.removeGroupMember(chat.id(), member.id()));
    }

    private void showConfirmDialog(String dialogId, String titleText, String titleId, String closeButtonId,
                                   String detailText, String cancelId, String cancelText,
                                   String confirmId, String confirmText, String confirmStyleClass,
                                   Runnable onConfirm) {
        closeConfirmDialog();
        Dialog<ButtonType> dialog = new Dialog<>();
        Window owner = drawerLayout.getScene() != null ? drawerLayout.getScene().getWindow() : null;
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle(titleText);

        var pane = dialog.getDialogPane();
        pane.setId(dialogId);
        pane.getStyleClass().add("chat-message-dialog");
        pane.setPrefWidth(420);

        Label title = new Label(titleText);
        title.setId(titleId);
        title.getStyleClass().add("chat-message-dialog-title");
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);

        Button closeButton = new Button();
        closeButton.setId(closeButtonId);
        closeButton.getStyleClass().add("chat-icon-btn");
        closeButton.setGraphic(FontIcon.of(Feather.X, 16));
        closeButton.setAccessibleText("Đóng hộp thoại");
        closeButton.setTooltip(new Tooltip("Đóng (Esc)"));

        HBox header = new HBox(12, title, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("chat-message-dialog-header");

        Label detail = new Label(detailText);
        detail.getStyleClass().add("chat-message-dialog-detail");
        detail.setWrapText(true);

        VBox body = new VBox(detail);
        body.getStyleClass().add("chat-message-dialog-body");

        pane.setContent(new VBox(header, body));

        ButtonType cancelType = new ButtonType(cancelText, ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType confirmType = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().setAll(cancelType, confirmType);

        Button cancelButton = (Button) pane.lookupButton(cancelType);
        Button confirmButton = (Button) pane.lookupButton(confirmType);
        ButtonBar.setButtonUniformSize(cancelButton, false);
        ButtonBar.setButtonUniformSize(confirmButton, false);
        cancelButton.setId(cancelId);
        cancelButton.getStyleClass().add("chat-dialog-cancel");
        cancelButton.setDefaultButton(true);
        confirmButton.setId(confirmId);
        confirmButton.getStyleClass().add(confirmStyleClass);
        confirmButton.setDefaultButton(false);
        closeButton.setOnAction(event -> cancelButton.fire());

        if (navigator != null) {
            navigator.themes().apply(pane);
        }
        for (String sheet : List.of(TOKENS_CSS, CHAT_CSS)) {
            if (!pane.getStylesheets().contains(sheet)) {
                pane.getStylesheets().add(sheet);
            }
        }

        dialog.setOnHidden(event -> {
            if (activeDialog == dialog) {
                activeDialog = null;
            }
            if (dialog.getResult() == confirmType) {
                onConfirm.run();
            }
        });
        activeDialog = dialog;
        dialog.show();
        Platform.runLater(cancelButton::requestFocus);
    }

    private void closeConfirmDialog() {
        if (activeDialog != null) {
            Dialog<ButtonType> closing = activeDialog;
            activeDialog = null;
            ButtonType cancel = closing.getDialogPane().getButtonTypes().stream()
                    .filter(type -> type.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE)
                    .findFirst().orElse(null);
            if (cancel != null) {
                closing.setResult(cancel);
            }
            closing.close();
        }
    }

    @FXML
    private void showInfo() {
        closeMemberMenu();
        drawerLayout.setCenter(infoPane);
        boolean isGroup = currentChat != null
                && (currentChat.kind() == MockConversation.Kind.GROUP || currentChat.encrypted());
        heading.setText(isGroup ? "Thông tin nhóm" : "Thông tin đoạn chat");
        back.setVisible(false);
    }

    private void showSearch() {
        closeMemberMenu();
        drawerLayout.setCenter(searchPane);
        heading.setText("Tìm trong đoạn chat");
        back.setVisible(true);
        renderResults(conversationSearchField.getText());
        conversationSearchField.requestFocus();
    }

    private void renderResults(String query) {
        String value = query.strip().toLowerCase(Locale.ROOT);
        matches = value.isEmpty() ? List.of() : catalog.messagesFor(search.conversationId()).stream()
                .filter(message -> message.state() != MockMessage.State.RECALLED)
                .filter(message -> message.text().toLowerCase(Locale.ROOT).contains(value)).toList();
        localResults.getChildren().clear();
        cards.clear();
        for (int i = 0; i < matches.size(); i++) {
            int index = i;
            var message = matches.get(i);
            String senderName = message.senderId().equals(catalog.currentUserId())
                    ? "Bạn"
                    : catalog.user(message.senderId()).fullName();
            var result = new SearchResultCard(senderName, message.text(),
                    message.sentAt(), null, () -> reveal(index));
            cards.add(result);
        }
        localResults.getChildren().setAll(cards);
        currentIndex = -1;
        resultCount.setText(value.isEmpty() ? "Nhập từ khóa" : matches.size() + " kết quả");
        previousResult.setDisable(true);
        nextResult.setDisable(matches.isEmpty());
    }

    private void reveal(int index) {
        if (index < 0 || index >= matches.size()) {
            return;
        }
        currentIndex = index;
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setCurrent(i == index);
        }
        resultCount.setText("Kết quả " + (index + 1) + " / " + matches.size());
        previousResult.setDisable(index == 0);
        nextResult.setDisable(index == matches.size() - 1);
        search.revealMessage().accept(matches.get(index).id());
    }

    @FXML
    private void previous() {
        reveal(currentIndex - 1);
    }

    @FXML
    private void next() {
        reveal(currentIndex + 1);
    }

    @FXML
    private void close() {
        navigator.closeDrawer();
    }
}
