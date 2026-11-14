package com.chatsystem.desktop.mock;

import com.chatsystem.desktop.mock.model.MockActivity;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockGroup;
import com.chatsystem.desktop.mock.model.MockLoginLog;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockMonthlyStatistics;
import com.chatsystem.desktop.mock.model.MockSpamReport;
import com.chatsystem.desktop.mock.model.MockUser;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A fresh, immutable fixture snapshot on every create(). No clock, random numbers, I/O,
 * JavaFX toolkit, or credentials are needed. Lists retain a deterministic display order.
 */
public final class MockData {
    public static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final Instant SNAPSHOT_AT = Instant.parse("2026-11-15T03:00:00Z");
    public static final String PREVIEW_USER_ID = "usr-01";

    private final List<MockUser> users;
    private final Map<String, MockUser> usersById;
    private final List<MockGroup> groups;
    private final List<MockConversation> conversations;
    private final List<MockMessage> messages;
    private final List<MockSpamReport> spamReports;
    private final List<AppOpen> appOpens;
    private final List<MockLoginLog> loginLogs;
    private final List<MockActivity> activities;

    private MockData() {
        users = createUsers();
        usersById = users.stream().collect(Collectors.toUnmodifiableMap(MockUser::id, Function.identity()));
        groups = createGroups();
        conversations = createConversations();
        messages = createMessages();
        spamReports = createReports();
        appOpens = createAppOpens();
        loginLogs = createLoginLogs();
        activities = createActivities();
    }

    public static MockData create() {
        return new MockData();
    }

    public List<MockUser> users() { return users; }
    public List<MockGroup> groups() { return groups; }
    public List<MockConversation> conversations() { return conversations; }
    public List<MockMessage> messages() { return messages; }
    public List<MockSpamReport> spamReports() { return spamReports; }
    public List<MockLoginLog> loginLogs() { return loginLogs; }
    public List<MockActivity> activities() { return activities; }
    public List<Integer> availableYears() { return List.of(2025, 2026); }
    public LocalDate activityFrom() { return LocalDate.of(2026, 1, 1); }
    public LocalDate snapshotDate() { return SNAPSHOT_AT.atZone(DISPLAY_ZONE).toLocalDate(); }
    public MockUser previewUser() { return user(PREVIEW_USER_ID); }

    public MockUser user(String id) {
        MockUser user = usersById.get(id);
        if (user == null) {
            throw new IllegalArgumentException("Unknown mock user: " + id);
        }
        return user;
    }

    /** Oldest first, suitable for a chat ListView. An unknown ID returns an empty list. */
    public List<MockMessage> messagesFor(String conversationId) {
        return messages.stream().filter(message -> message.conversationId().equals(conversationId)).toList();
    }

    /** Exactly January through December; an absent year contains twelve zero rows. */
    public List<MockMonthlyStatistics> monthlyStatistics(int year) {
        List<MockMonthlyStatistics> rows = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            YearMonth period = YearMonth.of(year, month);
            int registrations = (int) users.stream()
                    .filter(user -> YearMonth.from(user.createdAt().atZone(DISPLAY_ZONE)).equals(period)).count();
            int activeUsers = (int) appOpens.stream()
                    .filter(event -> YearMonth.from(event.at().atZone(DISPLAY_ZONE)).equals(period))
                    .map(AppOpen::userId).distinct().count();
            rows.add(new MockMonthlyStatistics(period, registrations, activeUsers));
        }
        return List.copyOf(rows);
    }

    private static List<MockUser> createUsers() {
        String[] names = {"Nguyễn Minh An", "Trần Bảo Châu", "Lê Hoàng Duy", "Phạm Ngọc Hà",
                "Võ Gia Hân", "Đặng Tuấn Huy", "Bùi Khánh Linh", "Đỗ Quang Minh",
                "Hồ Thảo My", "Ngô Đức Nam", "Dương Yến Nhi", "Lý Hải Phong",
                "Mai Thu Phương", "Tạ Quốc Quân", "Đinh Ngọc Thảo", "Cao Anh Thư",
                "Vũ Thanh Tùng", "Huỳnh Bảo Trâm", "Phan Nhật Vy", "Trịnh Hải Yến"};
        String[] usernames = {"minhan", "baochau", "hoangduy", "ngocha", "giahan", "tuanhuy",
                "khanhlinh", "quangminh", "thaomy", "ducnam", "yennhi", "haiphong",
                "thuphuong", "quocquan", "ngocthao", "anhthu", "thanhtung", "baotram", "nhatvy", "haiyen"};
        int[] months = {1, 2, 3, 5, 7, 8, 10, 12, 1, 2, 3, 3, 5, 7, 8, 8, 9, 10, 10, 11};
        List<MockUser> rows = new ArrayList<>();
        for (int index = 0; index < names.length; index++) {
            MockUser.Status status = index >= 18 ? MockUser.Status.DELETED
                    : index >= 16 ? MockUser.Status.LOCKED : MockUser.Status.ACTIVE;
            Optional<Instant> deletedAt = status == MockUser.Status.DELETED
                    ? Optional.of(SNAPSHOT_AT.minus(Duration.ofDays(index == 18 ? 10 : 1))) : Optional.empty();
            Instant createdAt = LocalDate.of(index < 8 ? 2025 : 2026, months[index], 3)
                    .atTime(9, 0).atZone(DISPLAY_ZONE).toInstant();
            rows.add(new MockUser(userId(index + 1), usernames[index], names[index],
                    usernames[index] + "@example.test", index % 5 == 0 ? "" : "Quận mẫu " + (index % 4 + 1),
                    index % 5 == 0 ? Optional.empty() : Optional.of(LocalDate.of(2004 + index % 3, index % 12 + 1, 12)),
                    index % 5 == 0 ? MockUser.Gender.UNSPECIFIED
                            : index % 2 == 0 ? MockUser.Gender.MALE : MockUser.Gender.FEMALE,
                    index == 1 ? MockUser.Role.ADMIN : MockUser.Role.USER, status,
                    status == MockUser.Status.ACTIVE && index % 3 == 0
                            ? MockUser.Presence.ONLINE : MockUser.Presence.OFFLINE,
                    createdAt, deletedAt));
        }
        return List.copyOf(rows);
    }

    private static List<MockGroup> createGroups() {
        return List.of(
                group("grp-01", "Nhóm đồ án Java", 1, 2, 3, 4, 5, 6, 7, 8),
                group("grp-02", "Thiết kế giao diện", 1, 2, 9, 10),
                group("grp-03", "Cơ sở dữ liệu", 1, 4, 11, 12, 13),
                group("grp-04", "Câu lạc bộ sách", 1, 3, 6, 14, 15, 16));
    }

    private static MockGroup group(String id, String name, int... memberNumbers) {
        List<MockGroup.Member> members = new ArrayList<>();
        for (int index = 0; index < memberNumbers.length; index++) {
            members.add(new MockGroup.Member(userId(memberNumbers[index]),
                    index < 2 ? MockGroup.Role.ADMIN : MockGroup.Role.MEMBER));
        }
        return new MockGroup(id, name, Instant.parse("2026-11-12T02:00:00Z"), members);
    }

    private List<MockConversation> createConversations() {
        List<MockConversation> rows = new ArrayList<>();
        for (int index = 1; index <= 5; index++) {
            MockUser peer = user(userId(index + 1));
            rows.add(new MockConversation("direct-0" + index, peer.fullName(), MockConversation.Kind.DIRECT,
                    List.of(PREVIEW_USER_ID, peer.id()), index % 3));
        }
        for (MockGroup group : groups) {
            rows.add(new MockConversation(group.id(), group.name(), MockConversation.Kind.GROUP,
                    group.members().stream().map(MockGroup.Member::userId).toList(), group.id().equals("grp-01") ? 4 : 0));
        }
        return List.copyOf(rows);
    }

    private List<MockMessage> createMessages() {
        String[] texts = {"Chào mọi người, mình đang chuẩn bị bản demo Version 1.",
                "Mình đã cập nhật tài liệu, bạn xem giúp nhé.", "Buổi trao đổi bắt đầu lúc 14:00 nhé!",
                "Nội dung quảng bá lặp lại (dữ liệu giả lập để xem bằng chứng spam).",
                "Đã nhận được, cảm ơn bạn.", "Mình gửi ghi chú:\n1. Kiểm tra giao diện\n2. Chụp ảnh báo cáo",
                "Thống nhất kiểm tra cả giao diện sáng và tối trước khi chụp ảnh báo cáo Version 1. "
                        + "Nếu tiêu đề dài, nội dung cần xuống dòng để vẫn đọc đủ trên cửa sổ nhỏ.",
                "Mình sẽ kiểm tra lại trước buổi họp."};
        List<MockMessage> rows = new ArrayList<>();
        for (int chatIndex = 0; chatIndex < conversations.size(); chatIndex++) {
            MockConversation chat = conversations.get(chatIndex);
            int count = chat.id().equals("grp-01") ? 56 : chat.kind() == MockConversation.Kind.GROUP ? 10 : 8;
            Instant start = SNAPSHOT_AT.minus(Duration.ofHours(46 - chatIndex * 3L));
            for (int index = 0; index < count; index++) {
                String sender = chat.participantIds().get(index % chat.participantIds().size());
                MockMessage.State state = switch (index % 3) {
                    case 0 -> MockMessage.State.STORED;
                    case 1 -> MockMessage.State.DELIVERED;
                    default -> MockMessage.State.READ;
                };
                if (chat.id().equals("direct-01") && index == 4) {
                    state = MockMessage.State.RECALLED;
                } else if (chat.id().equals("direct-02") && index == 6) {
                    state = MockMessage.State.PENDING;
                } else if (chat.id().equals("direct-03") && index == 6) {
                    state = MockMessage.State.FAILED;
                }
                rows.add(new MockMessage("msg-" + chat.id() + "-" + (index + 1), chat.id(), sender,
                        state == MockMessage.State.RECALLED ? "" : texts[index % texts.length],
                        start.plusSeconds(index * 60L), state));
            }
        }
        rows.sort(Comparator.comparing(MockMessage::sentAt).thenComparing(MockMessage::id));
        return List.copyOf(rows);
    }

    private List<MockSpamReport> createReports() {
        List<MockSpamReport> rows = new ArrayList<>();
        for (int index = 0; index < 6; index++) {
            MockConversation chat = conversations.get(index);
            List<MockMessage> eligible = messagesFor(chat.id()).stream()
                    .filter(message -> message.acceptedByServer() && message.state() != MockMessage.State.RECALLED).toList();
            // Select an older message so grp-01 also demonstrates Selected outside the latest 30 Context.
            MockMessage selected = eligible.stream().filter(message -> !message.senderId().equals(PREVIEW_USER_ID))
                    .filter(message -> message.text().contains("quảng bá")).findFirst().orElseThrow();
            Map<String, MockSpamReport.Evidence> evidence = new LinkedHashMap<>();
            evidence.put(selected.id(), new MockSpamReport.Evidence(selected, true, false));
            for (MockMessage message : eligible.subList(Math.max(0, eligible.size() - 30), eligible.size())) {
                evidence.put(message.id(), new MockSpamReport.Evidence(message, message.id().equals(selected.id()), true));
            }
            List<MockSpamReport.Evidence> snapshot = evidence.values().stream()
                    .sorted(Comparator.comparing(item -> item.message().sentAt())).toList();
            rows.add(new MockSpamReport("rpt-0" + (index + 1), PREVIEW_USER_ID, selected.senderId(), chat.id(),
                    "Gửi nội dung quảng bá lặp lại — tình huống giả lập " + (index + 1),
                    eligible.getLast().sentAt().plusSeconds(180),
                    index % 2 == 0 ? MockSpamReport.Status.PENDING : MockSpamReport.Status.REVIEWED, snapshot));
        }
        rows.sort(Comparator.comparing(MockSpamReport::createdAt).reversed());
        return List.copyOf(rows);
    }

    private List<AppOpen> createAppOpens() {
        List<AppOpen> rows = new ArrayList<>();
        for (int year : availableYears()) {
            for (int month = 1; month <= 12; month++) {
                Instant at = LocalDate.of(year, month, 10).atTime(9, 0).atZone(DISPLAY_ZONE).toInstant();
                for (int index = 0; index < users.size(); index++) {
                    MockUser user = users.get(index);
                    if (at.isBefore(user.createdAt()) || at.isAfter(SNAPSHOT_AT)
                            || user.deletedAt().filter(deleted -> !at.isBefore(deleted)).isPresent()) {
                        continue;
                    }
                    rows.add(new AppOpen(user.id(), at));
                    if ((index + month) % 2 == 0) {
                        // A second launch increases app opens but not monthly distinct users.
                        rows.add(new AppOpen(user.id(), at.plusSeconds(7200)));
                    }
                }
            }
        }
        return List.copyOf(rows);
    }

    private List<MockLoginLog> createLoginLogs() {
        List<MockLoginLog> rows = new ArrayList<>();
        List<AppOpen> recent = appOpens.stream().sorted(Comparator.comparing(AppOpen::at).reversed()).limit(36).toList();
        for (int index = 0; index < recent.size(); index++) {
            AppOpen event = recent.get(index);
            rows.add(new MockLoginLog("login-" + (index + 1), event.userId(), event.at(), MockLoginLog.Scope.USER_CLIENT));
        }
        rows.add(new MockLoginLog("login-admin-1", "usr-02", SNAPSHOT_AT.minusSeconds(3600), MockLoginLog.Scope.ADMIN_APP));
        rows.sort(Comparator.comparing(MockLoginLog::loggedInAt).reversed().thenComparing(MockLoginLog::id));
        return List.copyOf(rows);
    }

    private List<MockActivity> createActivities() {
        Instant from = activityFrom().atStartOfDay(DISPLAY_ZONE).toInstant();
        List<MockActivity> rows = new ArrayList<>();
        for (MockUser user : users) {
            int opens = (int) appOpens.stream().filter(event -> event.userId().equals(user.id()))
                    .filter(event -> !event.at().isBefore(from)).count();
            var people = new HashSet<String>();
            var chattedGroups = new HashSet<String>();
            for (MockConversation chat : conversations) {
                boolean sent = messagesFor(chat.id()).stream().anyMatch(message -> message.senderId().equals(user.id())
                        && message.acceptedByServer() && !message.sentAt().isBefore(from));
                if (sent && chat.kind() == MockConversation.Kind.GROUP) {
                    chattedGroups.add(chat.id());
                } else if (sent) {
                    chat.participantIds().stream().filter(id -> !id.equals(user.id())).forEach(people::add);
                }
            }
            rows.add(new MockActivity(user.id(), opens, people.size(), chattedGroups.size()));
        }
        return List.copyOf(rows);
    }

    private static String userId(int number) {
        return "usr-" + (number < 10 ? "0" : "") + number;
    }

    private record AppOpen(String userId, Instant at) {
    }
}
