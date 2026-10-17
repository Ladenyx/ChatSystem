package common.mock;

import common.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Static mock data for Version 1 UI development.
 * Includes Vietnamese names with diacritics to test font rendering.
 */
public class MockData {

    // ── USERS ─────────────────────────────────────────────────────────────────

    public static final List<User> USERS = new ArrayList<>();
    public static final List<Conversation> CONVERSATIONS = new ArrayList<>();
    public static final List<ChatGroup> GROUPS = new ArrayList<>();
    public static final List<FriendRequest> FRIEND_REQUESTS = new ArrayList<>();
    public static final List<SpamReport> SPAM_REPORTS = new ArrayList<>();
    public static final List<LoginRecord> LOGIN_RECORDS = new ArrayList<>();

    /** The currently logged-in user (user 1). */
    public static User CURRENT_USER;

    static {
        buildUsers();
        buildConversations();
        buildGroups();
        buildFriendRequests();
        buildSpamReports();
        buildLoginRecords();
    }

    // ── BUILD METHODS ─────────────────────────────────────────────────────────

    private static void buildUsers() {
        USERS.add(new User(1, "quan.nguyen", "Nguyễn Thế Quân",    "quan@gmail.com",   "123 Lý Thường Kiệt, Q.10, TP.HCM", LocalDate.of(2005, 3, 14), User.Gender.MALE,   User.Status.ACTIVE, LocalDateTime.of(2025, 9, 1,  8, 0), true));
        USERS.add(new User(2, "bao.nguyen",  "Nguyễn Phương Gia Bảo","bao@gmail.com",    "456 Điện Biên Phủ, Q.3, TP.HCM",  LocalDate.of(2005, 7, 22), User.Gender.MALE,   User.Status.ACTIVE, LocalDateTime.of(2025, 9, 1,  9, 0), true));
        USERS.add(new User(3, "kiet.thai",   "Thái Nguyễn Tuấn Kiệt","kiet@gmail.com",   "789 Nguyễn Trãi, Q.5, TP.HCM",   LocalDate.of(2005, 11, 5), User.Gender.MALE,   User.Status.ACTIVE, LocalDateTime.of(2025, 9, 2, 10, 0), true));
        USERS.add(new User(4, "linh.tran",   "Trần Thị Mỹ Linh",    "linh@gmail.com",   "12 Lê Lợi, Q.1, TP.HCM",          LocalDate.of(2005, 1, 18), User.Gender.FEMALE, User.Status.ACTIVE, LocalDateTime.of(2025, 9, 3, 11, 0), false));
        USERS.add(new User(5, "huong.le",    "Lê Thị Hương",        "huong@gmail.com",  "34 Trần Hưng Đạo, Q.1, TP.HCM",  LocalDate.of(2004, 6, 30), User.Gender.FEMALE, User.Status.ACTIVE, LocalDateTime.of(2025, 9, 4,  8, 30), true));
        USERS.add(new User(6, "minh.pham",   "Phạm Văn Minh",       "minh@gmail.com",   "56 Hai Bà Trưng, Q.1, TP.HCM",   LocalDate.of(2005, 4, 12), User.Gender.MALE,   User.Status.ACTIVE, LocalDateTime.of(2025, 9, 5,  9, 15), false));
        USERS.add(new User(7, "an.vo",       "Võ Thị Ánh",         "an@gmail.com",     "78 Cộng Hòa, Tân Bình, TP.HCM",  LocalDate.of(2004, 9, 25), User.Gender.FEMALE, User.Status.ACTIVE, LocalDateTime.of(2025, 9, 6, 14, 0), true));
        USERS.add(new User(8, "duc.hoang",   "Hoàng Minh Đức",      "duc@gmail.com",    "90 Phạm Hùng, Bình Chánh, TP.HCM",LocalDate.of(2005, 2, 8),  User.Gender.MALE,   User.Status.LOCKED, LocalDateTime.of(2025, 9, 7, 15, 30), false));
        USERS.add(new User(9, "thu.dang",    "Đặng Thị Thu",        "thu@gmail.com",    "22 Nguyễn Đình Chiểu, Q.3, TP.HCM",LocalDate.of(2004,12, 3), User.Gender.FEMALE, User.Status.ACTIVE, LocalDateTime.of(2025, 9, 8, 10, 45), false));
        USERS.add(new User(10,"khoa.nguyen", "Nguyễn Anh Khoa",     "khoa@gmail.com",   "44 Bạch Đằng, Bình Thạnh, TP.HCM",LocalDate.of(2005, 8, 19), User.Gender.MALE,   User.Status.ACTIVE, LocalDateTime.of(2025, 9, 9,  8,  0), true));
        USERS.add(new User(11,"nhi.truong",  "Trương Thị Nhị",      "nhi@gmail.com",    "66 Nguyễn Văn Cừ, Q.5, TP.HCM",  LocalDate.of(2004, 5, 14), User.Gender.FEMALE, User.Status.ACTIVE, LocalDateTime.of(2025, 9,10, 11, 0), true));
        USERS.add(new User(12,"tuan.bui",    "Bùi Minh Tuấn",       "tuan@gmail.com",   "88 Tôn Đức Thắng, Q.1, TP.HCM",  LocalDate.of(2005, 10,27), User.Gender.MALE,   User.Status.ACTIVE, LocalDateTime.of(2025, 9,11, 13, 0), false));
        USERS.add(new User(13,"mai.do",      "Đỗ Thị Mai",          "mai@gmail.com",    "100 Kha Vạn Cân, Thủ Đức, TP.HCM",LocalDate.of(2004, 3, 21), User.Gender.FEMALE, User.Status.ACTIVE, LocalDateTime.of(2025, 9,12,  9, 30), false));
        USERS.add(new User(14,"long.dinh",   "Đinh Quốc Long",      "long@gmail.com",   "112 Lê Văn Việt, Thủ Đức, TP.HCM",LocalDate.of(2005, 1, 5),  User.Gender.MALE,   User.Status.LOCKED, LocalDateTime.of(2025, 9,13, 16, 0), false));
        USERS.add(new User(15,"thao.ly",     "Lý Ngọc Thảo",        "thao@gmail.com",   "24 Trường Chinh, Tân Phú, TP.HCM",LocalDate.of(2004, 7, 16), User.Gender.FEMALE, User.Status.ACTIVE, LocalDateTime.of(2025, 9,14,  8, 0), true));
        USERS.add(new User(16,"phuc.cao",    "Cao Minh Phúc",       "phuc@gmail.com",   "36 Lũy Bán Bích, Tân Phú, TP.HCM",LocalDate.of(2005, 6, 9),  User.Gender.MALE,   User.Status.ACTIVE, LocalDateTime.of(2025, 9,15,  9, 45), false));
        USERS.add(new User(17,"yen.lam",     "Lâm Thị Yến",         "yen@gmail.com",    "48 Âu Cơ, Tân Bình, TP.HCM",     LocalDate.of(2004, 11,28), User.Gender.FEMALE, User.Status.ACTIVE, LocalDateTime.of(2025, 9,16, 12, 30), true));
        USERS.add(new User(18,"dat.ngo",     "Ngô Quốc Đạt",        "dat@gmail.com",    "60 Phan Văn Hớn, Q.12, TP.HCM",  LocalDate.of(2005, 4, 3),  User.Gender.MALE,   User.Status.ACTIVE, LocalDateTime.of(2025, 9,17,  7, 0), false));

        // Admin account (id=0 convention)
        USERS.add(new User(0, "admin", "System Administrator", "admin@chatsystem.local",
                "HCM City", LocalDate.of(2000, 1, 1), User.Gender.OTHER, User.Status.ACTIVE,
                LocalDateTime.of(2024, 1, 1, 0, 0), false));

        CURRENT_USER = USERS.get(0); // quan.nguyen is logged in
    }

    private static void buildConversations() {
        User me = USERS.get(0); // quan.nguyen

        // Conversation with bao.nguyen
        Conversation c1 = new Conversation(1, USERS.get(1), LocalDateTime.of(2026, 10, 16, 9, 30));
        c1.getMessages().add(new Message(1,  2, "Nguyễn Phương Gia Bảo", 1, null, "Mày ơi tao đẩy code lên rồi, pull về coi thử", Message.Type.DIRECT, Message.Status.READ, LocalDateTime.of(2026, 10, 16, 9, 0)));
        c1.getMessages().add(new Message(2,  1, "Nguyễn Thế Quân",       2, null, "Ok, tao đang pull",                            Message.Type.DIRECT, Message.Status.READ, LocalDateTime.of(2026, 10, 16, 9, 5)));
        c1.getMessages().add(new Message(3,  2, "Nguyễn Phương Gia Bảo", 1, null, "Nhớ test màn hình Login trước nha",            Message.Type.DIRECT, Message.Status.READ, LocalDateTime.of(2026, 10, 16, 9, 20)));
        c1.getMessages().add(new Message(4,  1, "Nguyễn Thế Quân",       2, null, "Oki, bro! UI chạy ngon rồi 🎉",               Message.Type.DIRECT, Message.Status.DELIVERED, LocalDateTime.of(2026, 10, 16, 9, 30)));
        c1.setLastMessage(c1.getMessages().get(3));
        c1.setUnreadCount(0);
        CONVERSATIONS.add(c1);

        // Conversation with kiet.thai
        Conversation c2 = new Conversation(2, USERS.get(2), LocalDateTime.of(2026, 10, 16, 8, 45));
        c2.getMessages().add(new Message(5, 3, "Thái Nguyễn Tuấn Kiệt", 1, null, "Kiệt đây, bạn setup database chưa?",          Message.Type.DIRECT, Message.Status.READ,      LocalDateTime.of(2026, 10, 16, 8, 0)));
        c2.getMessages().add(new Message(6, 1, "Nguyễn Thế Quân",       3, null, "Chưa, tao đang làm UI trước",                  Message.Type.DIRECT, Message.Status.READ,      LocalDateTime.of(2026, 10, 16, 8, 10)));
        c2.getMessages().add(new Message(7, 3, "Thái Nguyễn Tuấn Kiệt", 1, null, "Oke, để tao lo phần SQL script",               Message.Type.DIRECT, Message.Status.DELIVERED,  LocalDateTime.of(2026, 10, 16, 8, 45)));
        c2.setLastMessage(c2.getMessages().get(2));
        c2.setUnreadCount(1);
        CONVERSATIONS.add(c2);

        // Conversation with linh.tran
        Conversation c3 = new Conversation(3, USERS.get(3), LocalDateTime.of(2026, 10, 15, 22, 0));
        c3.getMessages().add(new Message(8,  4, "Trần Thị Mỹ Linh", 1, null, "Mày biết cài AtlantaFX chưa?",                     Message.Type.DIRECT, Message.Status.READ,      LocalDateTime.of(2026, 10, 15, 21, 30)));
        c3.getMessages().add(new Message(9,  1, "Nguyễn Thế Quân",   4, null, "Biết rồi, thêm dependency vào pom.xml là xong",    Message.Type.DIRECT, Message.Status.READ,      LocalDateTime.of(2026, 10, 15, 21, 45)));
        c3.getMessages().add(new Message(10, 4, "Trần Thị Mỹ Linh", 1, null, "Cảm ơn bạn nhiều lắm! 😊",                        Message.Type.DIRECT, Message.Status.READ,      LocalDateTime.of(2026, 10, 15, 22, 0)));
        c3.setLastMessage(c3.getMessages().get(2));
        c3.setUnreadCount(0);
        CONVERSATIONS.add(c3);

        // Conversation with huong.le
        Conversation c4 = new Conversation(4, USERS.get(4), LocalDateTime.of(2026, 10, 14, 17, 0));
        c4.getMessages().add(new Message(11, 5, "Lê Thị Hương",  1, null, "Cho tao hỏi deadline nộp version 1 là khi nào vậy?", Message.Type.DIRECT, Message.Status.READ,     LocalDateTime.of(2026, 10, 14, 16, 50)));
        c4.getMessages().add(new Message(12, 1, "Nguyễn Thế Quân",5, null, "Chủ nhật 18/10 này nè",                              Message.Type.DIRECT, Message.Status.READ,     LocalDateTime.of(2026, 10, 14, 17, 0)));
        c4.setLastMessage(c4.getMessages().get(1));
        c4.setUnreadCount(0);
        CONVERSATIONS.add(c4);
    }

    private static void buildGroups() {
        // Group 1: Java Programming team
        ChatGroup g1 = new ChatGroup(1, "Nhóm Đồ Án Java", 1, LocalDateTime.of(2025, 9, 1, 10, 0), true);
        g1.getMembers().addAll(List.of(USERS.get(0), USERS.get(1), USERS.get(2)));
        g1.getAdmins().addAll(List.of(USERS.get(0)));
        g1.getMessages().add(new Message(100, 1, "Nguyễn Thế Quân",       null, 1, "Anh em ơi, hôm nay mình bắt đầu làm UI nha!",   Message.Type.GROUP, Message.Status.READ, LocalDateTime.of(2026, 10, 16, 7, 0)));
        g1.getMessages().add(new Message(101, 2, "Nguyễn Phương Gia Bảo", null, 1, "Okela! Tao sẽ lo phần database",                Message.Type.GROUP, Message.Status.READ, LocalDateTime.of(2026, 10, 16, 7, 5)));
        g1.getMessages().add(new Message(102, 3, "Thái Nguyễn Tuấn Kiệt", null, 1, "Tao viết server socket nha",                    Message.Type.GROUP, Message.Status.READ, LocalDateTime.of(2026, 10, 16, 7, 10)));
        GROUPS.add(g1);

        // Group 2: Study group
        ChatGroup g2 = new ChatGroup(2, "Học Thuật HK1", 4, LocalDateTime.of(2025, 9, 5, 14, 0), false);
        g2.getMembers().addAll(List.of(USERS.get(0), USERS.get(3), USERS.get(4), USERS.get(5)));
        g2.getAdmins().addAll(List.of(USERS.get(3)));
        g2.getMessages().add(new Message(103, 4, "Trần Thị Mỹ Linh", null, 2, "Ai làm bài tập giải tích chưa?",                   Message.Type.GROUP, Message.Status.READ, LocalDateTime.of(2026, 10, 15, 20, 0)));
        g2.getMessages().add(new Message(104, 1, "Nguyễn Thế Quân",   null, 2, "Tao làm rồi, nhưng câu 3 khó quá 😅",             Message.Type.GROUP, Message.Status.READ, LocalDateTime.of(2026, 10, 15, 20, 30)));
        GROUPS.add(g2);

        // Group 3: Friends hangout
        ChatGroup g3 = new ChatGroup(3, "Chill Weekend ☀️", 5, LocalDateTime.of(2025, 10, 1, 9, 0), false);
        g3.getMembers().addAll(List.of(USERS.get(0), USERS.get(4), USERS.get(6), USERS.get(9), USERS.get(10)));
        g3.getAdmins().addAll(List.of(USERS.get(4)));
        g3.getMessages().add(new Message(105, 5, "Lê Thị Hương",    null, 3, "Cuối tuần này đi cà phê không mọi người?",           Message.Type.GROUP, Message.Status.DELIVERED, LocalDateTime.of(2026, 10, 16, 6, 0)));
        GROUPS.add(g3);
    }

    private static void buildFriendRequests() {
        FRIEND_REQUESTS.add(new FriendRequest(1, USERS.get(5),  USERS.get(0), FriendRequest.Status.PENDING, LocalDateTime.of(2026, 10, 16, 8, 0)));
        FRIEND_REQUESTS.add(new FriendRequest(2, USERS.get(8),  USERS.get(0), FriendRequest.Status.PENDING, LocalDateTime.of(2026, 10, 15, 21, 0)));
        FRIEND_REQUESTS.add(new FriendRequest(3, USERS.get(11), USERS.get(0), FriendRequest.Status.PENDING, LocalDateTime.of(2026, 10, 15, 16, 30)));
    }

    private static void buildSpamReports() {
        SPAM_REPORTS.add(new SpamReport(1, USERS.get(0),  USERS.get(7),  "Spam tin nhắn quảng cáo",  SpamReport.Status.PENDING,  LocalDateTime.of(2026, 10, 16, 9, 0)));
        SPAM_REPORTS.add(new SpamReport(2, USERS.get(3),  USERS.get(13), "Gửi nội dung không phù hợp",SpamReport.Status.PENDING,  LocalDateTime.of(2026, 10, 15, 20, 0)));
        SPAM_REPORTS.add(new SpamReport(3, USERS.get(4),  USERS.get(7),  "Xúc phạm cá nhân",         SpamReport.Status.RESOLVED, LocalDateTime.of(2026, 10, 14, 14, 0)));
        SPAM_REPORTS.add(new SpamReport(4, USERS.get(9),  USERS.get(13), "Kêu gọi lừa đảo",          SpamReport.Status.PENDING,  LocalDateTime.of(2026, 10, 13, 11, 0)));
    }

    private static void buildLoginRecords() {
        for (User u : USERS) {
            if (u.getId() == 0) continue;
            LOGIN_RECORDS.add(new LoginRecord(
                u.getId() * 10,
                u.getId(), u.getUsername(), u.getFullName(),
                u.getCreatedAt().plusDays(1)
            ));
        }
        // Add some recent logins
        LOGIN_RECORDS.add(new LoginRecord(200, 1, "quan.nguyen",  "Nguyễn Thế Quân",        LocalDateTime.of(2026, 10, 16, 7, 0)));
        LOGIN_RECORDS.add(new LoginRecord(201, 2, "bao.nguyen",   "Nguyễn Phương Gia Bảo",  LocalDateTime.of(2026, 10, 16, 7, 30)));
        LOGIN_RECORDS.add(new LoginRecord(202, 3, "kiet.thai",    "Thái Nguyễn Tuấn Kiệt",  LocalDateTime.of(2026, 10, 16, 8, 0)));
        LOGIN_RECORDS.add(new LoginRecord(203, 5, "huong.le",     "Lê Thị Hương",            LocalDateTime.of(2026, 10, 16, 8, 15)));
        LOGIN_RECORDS.add(new LoginRecord(204, 7, "an.vo",        "Võ Thị Ánh",             LocalDateTime.of(2026, 10, 16, 8, 30)));
    }

    // ── FRIEND & BLOCK RELATIONSHIPS ──────────────────────────────────────────
    public static final List<Integer> FRIEND_IDS = new ArrayList<>(List.of(2, 3, 4, 5, 6, 7, 10, 11, 15, 17));
    public static final List<Integer> BLOCKED_IDS = new ArrayList<>(List.of(8, 14));
    public static final List<Integer> OUTGOING_REQUEST_IDS = new ArrayList<>(List.of(12));

    // ── HELPERS ───────────────────────────────────────────────────────────────

    /** Returns a user by id, or null if not found. */
    public static User getUserById(int id) {
        return USERS.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    /** Returns whether a user is currently a friend of the current user. */
    public static boolean isFriend(int userId) {
        return FRIEND_IDS.contains(userId);
    }

    /** Returns whether a user is blocked by or has blocked the current user. */
    public static boolean isBlocked(int userId) {
        return BLOCKED_IDS.contains(userId);
    }

    /** Returns the friends of the current user. */
    public static List<User> getCurrentUserFriends() {
        return USERS.stream()
                .filter(u -> FRIEND_IDS.contains(u.getId()))
                .toList();
    }

    /** Returns online friends of the current user. */
    public static List<User> getOnlineFriends() {
        return getCurrentUserFriends().stream().filter(User::isOnline).toList();
    }

    /** Removes friend relationship (Unfriend). */
    public static void unfriend(int userId) {
        FRIEND_IDS.remove(Integer.valueOf(userId));
    }

    /** Blocks a user (removes from friends, adds to blocked list). */
    public static void blockUser(int userId) {
        FRIEND_IDS.remove(Integer.valueOf(userId));
        if (!BLOCKED_IDS.contains(userId)) {
            BLOCKED_IDS.add(userId);
        }
    }

    /** Accepts a friend request: adds sender to friends, marks request accepted. */
    public static void acceptFriendRequest(FriendRequest request) {
        if (request != null) {
            request.setStatus(FriendRequest.Status.ACCEPTED);
            if (!FRIEND_IDS.contains(request.getSender().getId())) {
                FRIEND_IDS.add(request.getSender().getId());
            }
            FRIEND_REQUESTS.remove(request);
        }
    }

    /** Declines a friend request: marks request declined and removes from pending. */
    public static void declineFriendRequest(FriendRequest request) {
        if (request != null) {
            request.setStatus(FriendRequest.Status.DECLINED);
            FRIEND_REQUESTS.remove(request);
        }
    }

    /** Sends a friend request to target user. */
    public static void sendFriendRequest(int targetUserId) {
        if (!OUTGOING_REQUEST_IDS.contains(targetUserId)) {
            OUTGOING_REQUEST_IDS.add(targetUserId);
        }
    }

    /** Checks if an outgoing friend request is pending for target user. */
    public static boolean hasOutgoingRequest(int targetUserId) {
        return OUTGOING_REQUEST_IDS.contains(targetUserId);
    }

    // ── ADMIN HELPERS & DATA STRUCTURES ────────────────────────────────────────

    public static final java.util.Map<Integer, List<Integer>> USER_FRIENDS_MAP = new java.util.HashMap<>();

    static {
        USER_FRIENDS_MAP.put(1,  new ArrayList<>(List.of(2, 3, 4, 5, 6, 7, 10, 11, 15, 17)));
        USER_FRIENDS_MAP.put(2,  new ArrayList<>(List.of(1, 3, 4, 6, 8, 9, 12)));
        USER_FRIENDS_MAP.put(3,  new ArrayList<>(List.of(1, 2, 5, 7, 10, 13)));
        USER_FRIENDS_MAP.put(4,  new ArrayList<>(List.of(1, 2, 5, 9, 11, 14)));
        USER_FRIENDS_MAP.put(5,  new ArrayList<>(List.of(1, 3, 4, 6, 7, 15)));
        USER_FRIENDS_MAP.put(6,  new ArrayList<>(List.of(1, 2, 5, 8, 12, 16)));
        USER_FRIENDS_MAP.put(7,  new ArrayList<>(List.of(1, 3, 5, 10, 17, 18)));
        USER_FRIENDS_MAP.put(8,  new ArrayList<>(List.of(2, 6, 12, 14)));
        USER_FRIENDS_MAP.put(9,  new ArrayList<>(List.of(2, 4, 11, 13)));
        USER_FRIENDS_MAP.put(10, new ArrayList<>(List.of(1, 3, 7, 15, 17)));
        USER_FRIENDS_MAP.put(11, new ArrayList<>(List.of(1, 4, 9, 13, 16)));
        USER_FRIENDS_MAP.put(12, new ArrayList<>(List.of(2, 6, 8, 18)));
        USER_FRIENDS_MAP.put(13, new ArrayList<>(List.of(3, 9, 11, 15)));
        USER_FRIENDS_MAP.put(14, new ArrayList<>(List.of(4, 8, 16)));
        USER_FRIENDS_MAP.put(15, new ArrayList<>(List.of(1, 5, 10, 13, 17)));
        USER_FRIENDS_MAP.put(16, new ArrayList<>(List.of(6, 11, 14, 18)));
        USER_FRIENDS_MAP.put(17, new ArrayList<>(List.of(1, 7, 10, 15)));
        USER_FRIENDS_MAP.put(18, new ArrayList<>(List.of(7, 12, 16)));
    }

    /** Returns direct friends of any specified user. */
    public static List<User> getUserFriends(int userId) {
        List<Integer> fids = USER_FRIENDS_MAP.getOrDefault(userId, List.of());
        return USERS.stream()
                .filter(u -> fids.contains(u.getId()))
                .toList();
    }

    /** Returns [directFriendsCount, friendsOfFriendsCount] for a user. */
    public static int[] getFriendCounts(int userId) {
        List<Integer> direct = USER_FRIENDS_MAP.getOrDefault(userId, List.of());
        int directCount = direct.size();

        java.util.Set<Integer> fofSet = new java.util.HashSet<>();
        for (int fid : direct) {
            List<Integer> secondary = USER_FRIENDS_MAP.getOrDefault(fid, List.of());
            for (int sfid : secondary) {
                if (sfid != userId && !direct.contains(sfid)) {
                    fofSet.add(sfid);
                }
            }
        }
        return new int[]{directCount, fofSet.size()};
    }

    /** Returns the login records for a specific user. */
    public static List<LoginRecord> getUserLoginHistory(int userId) {
        return LOGIN_RECORDS.stream()
                .filter(r -> r.getUserId() == userId)
                .sorted((a, b) -> b.getLoginAt().compareTo(a.getLoginAt()))
                .toList();
    }

    /** Adds a new user account (Admin A-02). */
    public static void addUser(User user) {
        if (user.getId() == 0) {
            int maxId = USERS.stream().mapToInt(User::getId).max().orElse(0);
            user.setId(maxId + 1);
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        USERS.add(user);
        USER_FRIENDS_MAP.put(user.getId(), new ArrayList<>());
        LOGIN_RECORDS.add(new LoginRecord(
                (int) (System.currentTimeMillis() % 100000),
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                LocalDateTime.now()
        ));
    }

    /** Updates an existing user profile (Admin A-02). */
    public static void updateUser(User updated) {
        for (int i = 0; i < USERS.size(); i++) {
            if (USERS.get(i).getId() == updated.getId()) {
                USERS.set(i, updated);
                break;
            }
        }
    }

    /** Deletes a user by ID (Admin A-02). */
    public static void deleteUser(int userId) {
        USERS.removeIf(u -> u.getId() == userId);
        USER_FRIENDS_MAP.remove(userId);
        for (List<Integer> list : USER_FRIENDS_MAP.values()) {
            list.remove(Integer.valueOf(userId));
        }
    }

    /** Toggles lock/unlock status of a user (Admin A-03). */
    public static void toggleUserStatus(int userId) {
        User u = getUserById(userId);
        if (u != null) {
            if (u.getStatus() == User.Status.ACTIVE) {
                u.setStatus(User.Status.LOCKED);
            } else {
                u.setStatus(User.Status.ACTIVE);
            }
        }
    }

    /** Resets password for user and returns the new plain temporary password (Admin A-04). */
    public static String resetUserPassword(int userId) {
        User u = getUserById(userId);
        if (u != null) {
            int randSuffix = 100 + (int) (Math.random() * 900);
            return "Pass@" + randSuffix;
        }
        return "Chat#2026";
    }

    /** Locks the reported user account and resolves the spam report (Admin A-11). */
    public static void lockReportedUser(int reportId) {
        for (SpamReport report : SPAM_REPORTS) {
            if (report.getId() == reportId) {
                if (report.getReported() != null) {
                    report.getReported().setStatus(User.Status.LOCKED);
                }
                report.setStatus(SpamReport.Status.RESOLVED);
                break;
            }
        }
    }

    /** Resolves a spam report. */
    public static void resolveSpamReport(int reportId) {
        for (SpamReport report : SPAM_REPORTS) {
            if (report.getId() == reportId) {
                report.setStatus(SpamReport.Status.RESOLVED);
                break;
            }
        }
    }

    /** Dismisses a spam report. */
    public static void dismissSpamReport(int reportId) {
        for (SpamReport report : SPAM_REPORTS) {
            if (report.getId() == reportId) {
                report.setStatus(SpamReport.Status.DISMISSED);
                break;
            }
        }
    }

    /** Activity record representation for Admin screen A-15. */
    public static class UserActivity {
        private final User user;
        private final int appOpens;
        private final int peopleChattedWith;
        private final int groupsChattedIn;
        private final LocalDateTime lastActive;

        public UserActivity(User user, int appOpens, int peopleChattedWith, int groupsChattedIn, LocalDateTime lastActive) {
            this.user = user;
            this.appOpens = appOpens;
            this.peopleChattedWith = peopleChattedWith;
            this.groupsChattedIn = groupsChattedIn;
            this.lastActive = lastActive;
        }

        public User getUser() { return user; }
        public int getAppOpens() { return appOpens; }
        public int getPeopleChattedWith() { return peopleChattedWith; }
        public int getGroupsChattedIn() { return groupsChattedIn; }
        public LocalDateTime getLastActive() { return lastActive; }
    }

    /** Returns active user statistics in the given date range (Admin A-15). */
    public static List<UserActivity> getActiveUserData(LocalDate from, LocalDate to) {
        List<UserActivity> list = new ArrayList<>();
        // Mock activity data seeded deterministically for all non-admin users
        int[] opens =  { 58, 45, 42, 31, 27, 21, 19, 3,  14, 25, 22, 11, 8,  2, 16, 12, 18, 5 };
        int[] people = {  9,  7,  6,  4,  5,  3,  4, 0,   2,  4,  3,  2, 1,  0,  3,  2,  3, 1 };
        int[] groups = {  3,  2,  2,  2,  2,  1,  1, 0,   1,  2,  1,  1, 0,  0,  1,  1,  1, 0 };

        List<User> regularUsers = USERS.stream().filter(u -> u.getId() != 0).toList();
        for (int i = 0; i < regularUsers.size(); i++) {
            User u = regularUsers.get(i);
            int idx = i % opens.length;
            LocalDateTime activeTime = LocalDateTime.of(2026, 10, 16, 8, 30).minusHours(i * 3 + 1);
            list.add(new UserActivity(u, opens[idx], people[idx], groups[idx], activeTime));
        }
        return list;
    }

    /** Returns monthly registration counts for selected year (Admin A-13). */
    public static java.util.Map<Integer, Long> getMonthlyRegistrations(int year) {
        java.util.Map<Integer, Long> map = new java.util.LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            map.put(m, 0L);
        }
        // Count actual registrations in MockData
        for (User u : USERS) {
            if (u.getId() != 0 && u.getCreatedAt() != null && u.getCreatedAt().getYear() == year) {
                int month = u.getCreatedAt().getMonthValue();
                map.put(month, map.get(month) + 1);
            }
        }
        // If year is 2025 or 2026, supplement with realistic curve so chart looks rich
        if (year == 2025) {
            int[] baseline = { 3, 4, 6, 5, 8, 7, 9, 11, 18, 12, 14, 15 };
            for (int m = 1; m <= 12; m++) {
                map.put(m, Math.max(map.get(m), (long) baseline[m - 1]));
            }
        } else if (year == 2026) {
            int[] baseline = { 12, 15, 14, 18, 20, 22, 25, 24, 28, 30, 26, 32 };
            for (int m = 1; m <= 12; m++) {
                map.put(m, Math.max(map.get(m), (long) baseline[m - 1]));
            }
        }
        return map;
    }

    /** Returns monthly active users for selected year (Admin A-16). */
    public static java.util.Map<Integer, Long> getMonthlyActiveUsers(int year) {
        java.util.Map<Integer, Long> map = new java.util.LinkedHashMap<>();
        int[] baseline2026 = { 14, 15, 16, 15, 17, 18, 17, 19, 21, 24, 22, 25 };
        int[] baseline2025 = { 8, 9, 10, 9, 11, 12, 13, 14, 16, 15, 17, 18 };
        int[] baseline = (year == 2025) ? baseline2025 : baseline2026;
        for (int m = 1; m <= 12; m++) {
            map.put(m, (long) baseline[m - 1]);
        }
        return map;
    }
}


