package com.chatsystem.admin.controller.groups;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockGroup;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.ViewController;
import java.text.Collator;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

public final class GroupsController implements ViewController {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
            .withZone(MockData.DISPLAY_ZONE);
    private static final Collator VIETNAMESE_COLLATOR = Collator.getInstance(Locale.of("vi", "VN"));

    public record GroupMemberRow(String userId, String fullName, String username, MockGroup.Role role) {
    }

    @FXML private TextField nameFilter;
    @FXML private ComboBox<String> sortOrder;
    @FXML private TableView<MockGroup> groupsTable;
    @FXML private TableColumn<MockGroup, MockGroup> groupNameColumn;
    @FXML private TableColumn<MockGroup, Integer> memberCountColumn;
    @FXML private TableColumn<MockGroup, Instant> groupCreatedColumn;
    @FXML private Label groupCount;
    @FXML private Hyperlink clearFilter;

    @FXML private VBox unselectedGroup;
    @FXML private ScrollPane groupInspector;
    @FXML private Label groupInitials;
    @FXML private Label groupTitle;
    @FXML private Label groupId;
    @FXML private Label groupMemberCountBadge;
    @FXML private Label groupCreated;

    @FXML private Label adminCount;
    @FXML private TableView<GroupMemberRow> adminTable;
    @FXML private TableColumn<GroupMemberRow, GroupMemberRow> adminIdentityColumn;
    @FXML private TableColumn<GroupMemberRow, String> adminRoleColumn;

    @FXML private Label memberCount;
    @FXML private TableView<GroupMemberRow> membersTable;
    @FXML private TableColumn<GroupMemberRow, GroupMemberRow> memberIdentityColumn;
    @FXML private TableColumn<GroupMemberRow, String> memberRoleColumn;

    private final MockData data = MockData.create();
    private final FilteredList<MockGroup> filteredGroups =
            new FilteredList<>(FXCollections.observableArrayList(data.groups()));
    private final SortedList<MockGroup> sortedGroups = new SortedList<>(filteredGroups);

    @FXML
    private void initialize() {
        configureTables();
        configureSortOptions();

        nameFilter.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        sortOrder.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        groupCount.textProperty().bind(Bindings.size(filteredGroups).asString("%d nhóm chat"));
        clearFilter.visibleProperty().bind(nameFilter.textProperty().isNotEmpty()
                .or(sortOrder.getSelectionModel().selectedIndexProperty().isNotEqualTo(0)));
        clearFilter.managedProperty().bind(clearFilter.visibleProperty());

        groupsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, selected) -> showGroupDetails(selected));

        applyFilter();
    }

    private void configureSortOptions() {
        sortOrder.getItems().setAll(
                "Mới tạo trước",
                "Cũ nhất trước",
                "Tên nhóm A–Z",
                "Tên nhóm Z–A"
        );
        sortOrder.getSelectionModel().select(0);
    }

    private void configureTables() {
        groupsTable.setItems(sortedGroups);
        groupNameColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        groupNameColumn.setCellFactory(col -> new GroupNameCell());

        memberCountColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().members().size()));

        groupCreatedColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().createdAt()));
        groupCreatedColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Instant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DATE_TIME.format(item));
                }
            }
        });

        adminIdentityColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        adminIdentityColumn.setCellFactory(col -> new MemberIdentityCell());
        adminRoleColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>("Admin nhóm"));
        adminRoleColumn.setCellFactory(col -> new RoleBadgeCell("badge-admin"));

        memberIdentityColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        memberIdentityColumn.setCellFactory(col -> new MemberIdentityCell());
        memberRoleColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>("Thành viên"));
        memberRoleColumn.setCellFactory(col -> new RoleBadgeCell("badge-member"));
    }

    private void applyFilter() {
        MockGroup selected = groupsTable.getSelectionModel().getSelectedItem();
        String query = nameFilter.getText() == null ? "" : nameFilter.getText().trim().toLowerCase(Locale.ROOT);
        filteredGroups.setPredicate(group -> group.name().toLowerCase(Locale.ROOT).contains(query));

        Comparator<MockGroup> comparator = switch (sortOrder.getSelectionModel().getSelectedIndex()) {
            case 1 -> Comparator.comparing(MockGroup::createdAt);
            case 2 -> (a, b) -> VIETNAMESE_COLLATOR.compare(a.name(), b.name());
            case 3 -> (a, b) -> VIETNAMESE_COLLATOR.compare(b.name(), a.name());
            default -> Comparator.comparing(MockGroup::createdAt).reversed();
        };

        sortedGroups.setComparator(comparator.thenComparing(MockGroup::id));

        if (selected == null || !sortedGroups.contains(selected)) {
            groupsTable.getSelectionModel().clearSelection();
        } else {
            groupsTable.getSelectionModel().select(selected);
        }
    }

    @FXML
    private void clearFilter() {
        nameFilter.clear();
        sortOrder.getSelectionModel().select(0);
    }

    private void showGroupDetails(MockGroup group) {
        boolean hasSelection = group != null;
        unselectedGroup.setVisible(!hasSelection);
        unselectedGroup.setManaged(!hasSelection);
        groupInspector.setVisible(hasSelection);
        groupInspector.setManaged(hasSelection);

        if (!hasSelection) {
            return;
        }

        groupInitials.setText(extractInitials(group.name()));
        groupTitle.setText(group.name());
        groupId.setText("Mã nhóm: " + group.id());
        groupMemberCountBadge.setText(group.members().size() + " thành viên");
        groupCreated.setText("Tạo ngày: " + DATE_TIME.format(group.createdAt()));

        List<GroupMemberRow> admins = new ArrayList<>();
        List<GroupMemberRow> regulars = new ArrayList<>();

        for (MockGroup.Member member : group.members()) {
            MockUser user = data.user(member.userId());
            GroupMemberRow row = new GroupMemberRow(user.id(), user.fullName(), user.username(), member.role());
            if (member.role() == MockGroup.Role.ADMIN) {
                admins.add(row);
            } else {
                regulars.add(row);
            }
        }

        // Sort members alphabetically by display name (UC-ADM-GRP-02 Step 3)
        admins.sort((a, b) -> VIETNAMESE_COLLATOR.compare(a.fullName(), b.fullName()));
        regulars.sort((a, b) -> VIETNAMESE_COLLATOR.compare(a.fullName(), b.fullName()));

        setRoleRows(adminTable, admins);
        adminCount.setText(admins.size() + " quản trị viên");

        setRoleRows(membersTable, regulars);
        memberCount.setText(regulars.size() + " thành viên thường");

        groupInspector.setVvalue(0);
    }

    private static void setRoleRows(TableView<GroupMemberRow> table, List<GroupMemberRow> rows) {
        table.getItems().setAll(rows);
        // Size for all mock rows and let the inspector, not the two tables, scroll.
        double height = 36 + table.getFixedCellSize() * Math.max(1, rows.size());
        table.setMinHeight(height);
        table.setPrefHeight(height);
        table.setMaxHeight(height);
        table.getSelectionModel().clearSelection();
    }

    private static String extractInitials(String name) {
        if (name == null || name.isBlank()) {
            return "GP";
        }
        String[] words = name.trim().split("\\s+");
        if (words.length == 1) {
            return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase(Locale.ROOT);
        }
        return (words[0].substring(0, 1) + words[words.length - 1].substring(0, 1)).toUpperCase(Locale.ROOT);
    }

    private static final class GroupNameCell extends TableCell<MockGroup, MockGroup> {
        private final Label name = new Label();
        private final Label count = new Label();
        private final VBox layout = new VBox(2, name, count);
        private final Tooltip tooltip = new Tooltip();

        private GroupNameCell() {
            name.getStyleClass().add("admin-user-name");
            count.getStyleClass().add("caption");
            layout.setMinWidth(0);
        }

        @Override
        protected void updateItem(MockGroup group, boolean empty) {
            super.updateItem(group, empty);
            if (empty || group == null) {
                setGraphic(null);
                setTooltip(null);
                setAccessibleText(null);
            } else {
                name.setText(group.name());
                count.setText("Mã: " + group.id());
                tooltip.setText(group.name() + " (" + group.id() + ")");
                setTooltip(tooltip);
                setAccessibleText(group.name());
                setGraphic(layout);
            }
        }
    }

    private static final class MemberIdentityCell extends TableCell<GroupMemberRow, GroupMemberRow> {
        private final Label name = new Label();
        private final Label username = new Label();
        private final VBox layout = new VBox(2, name, username);
        private final Tooltip tooltip = new Tooltip();

        private MemberIdentityCell() {
            name.getStyleClass().add("admin-member-name");
            username.getStyleClass().add("caption");
            layout.setMinWidth(0);
        }

        @Override
        protected void updateItem(GroupMemberRow item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setTooltip(null);
                setAccessibleText(null);
            } else {
                name.setText(item.fullName());
                username.setText("@" + item.username());
                tooltip.setText(item.fullName() + " (@" + item.username() + ")");
                setTooltip(tooltip);
                setAccessibleText(item.fullName() + " @" + item.username());
                setGraphic(layout);
            }
        }
    }

    private static final class RoleBadgeCell extends TableCell<GroupMemberRow, String> {
        private final Label badge = new Label();
        private RoleBadgeCell(String roleStyle) {
            badge.getStyleClass().addAll("admin-role-badge", roleStyle);
        }

        @Override
        protected void updateItem(String roleText, boolean empty) {
            super.updateItem(roleText, empty);
            if (empty || roleText == null) {
                setGraphic(null);
            } else {
                badge.setText(roleText);
                setGraphic(badge);
            }
        }
    }
}
