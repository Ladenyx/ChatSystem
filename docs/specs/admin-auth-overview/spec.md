# Feature Spec: Xác thực Admin, Dashboard Tổng quan & Nhật ký Đăng nhập

- **Phụ trách:** Nguyễn Thế Quân (`@Ladenyx`)
- **Reviewer:** Nguyễn Phương Gia Bảo (`@baonguyen1776`)
- **Phạm vi:** `A-SCR-01` (Đăng nhập quản trị), `AdminShell` (đăng xuất & thu hồi phiên WSS), `A-SCR-02` (Dashboard Tổng quan), `A-SCR-06` / `A-07` (Nhật ký đăng nhập hệ thống).
- **Dẫn chiếu:**
  - Use Cases: [UC-ADM-AUTH-01](../../requirements/use-cases/admin/authentication/UC-ADM-AUTH-01-admin-sign-in.md) · [UC-ADM-OVW-01](../../requirements/use-cases/admin/overview/UC-ADM-OVW-01-view-overview.md) · [UC-ADM-LOG-01](../../requirements/use-cases/admin/login-logs/UC-ADM-LOG-01-view-system-login-log.md)
  - Decisions: [DEC-07 Vòng đời tài khoản](../../decisions/account-lifecycle.md) · [DEC-10 Kiểm duyệt spam](../../decisions/spam-moderation-and-retention.md) · [Thống kê & thời gian](../../decisions/statistics-and-time.md)
  - Contracts: [rest-conventions.md](../../architecture/contracts/rest-conventions.md) · [websocket-protocol.md](../../architecture/contracts/websocket-protocol.md) · [openapi.yaml](../../architecture/contracts/openapi.yaml)
  - Màn hình & Kiểm kê: [screen.md](../../requirements/screen.md) · [screen-inventory.md](../admin/screen-inventory.md)

---

## 1. Người thao tác & Điều kiện tiên quyết

- **Tác nhân chính:** Admin hệ thống (`users.role = 'ADMIN'`, `users.status = 'ACTIVE'`).
- **Khởi tạo tài khoản Admin:** Chỉ tạo qua CLI/script phía máy chủ; không có đăng ký công khai vai trò `ADMIN` và không có mật khẩu mặc định trong mã nguồn.
- **Môi trường:** Mọi mốc thời gian lọc/hiển thị dùng múi giờ `Asia/Ho_Chi_Minh` (lưu UTC trên PostgreSQL).

---

## 2. Phân quyền & Phạm vi phiên (`ADMIN_APP`)

1. **Giới hạn 1 phiên quản trị (`DEC-07`):** Mỗi tài khoản `ADMIN` chỉ có tối đa **01** phiên `ADMIN_APP` đang sống (`user_sessions_one_live_admin_uq`).
2. **Độc lập với `USER_CLIENT`:** Đăng nhập `ADMIN_APP` mới không thu hồi phiên `USER_CLIENT` và ngược lại; kết nối WSS của `ADMIN_APP` **không** làm tài khoản xuất hiện `ONLINE` trên danh sách bạn bè và **không** gọi `record_app_open`.
3. **Quyền riêng tư:** `A-SCR-02` và `A-SCR-06` là giao diện **chỉ đọc**, tuyệt đối không chứa nội dung tin nhắn cá nhân hay nhóm E2EE (bảng spam trên `A-SCR-02` chỉ trả metadata `AdminRecentSpamReportDto`, không trả `evidence`).
4. **Kiểm tra mỗi yêu cầu:** Server kiểm tra lại token chưa `revoked_at`, `scope == ADMIN_APP`, `users.role == ADMIN` và `users.status == ACTIVE` trước mọi REST response và mọi khung WSS push (`admin.kpi.updated`, `admin.spam_report.*`, `admin.user.status_changed`).

---

## 3. Luồng nghiệp vụ chi tiết

### 3.1. Đăng nhập & Thu hồi phiên (`A-SCR-01` & `AdminShell`)

#### Luồng chính
1. Admin nhập `username` (trim), `password` (giữ nguyên ký tự) rồi bấm **Đăng nhập**.
2. Client vô hiệu hóa nút gửi + các ô nhập và gọi `POST /api/v1/auth/login` với `scope = ADMIN_APP`, `launchId = null`.
3. Server kiểm tra tuần tự:
   - Khóa tạm do thử sai (nếu đang bị khóa → từ chối `429 RATE_LIMITED` kèm thời gian chờ, độc lập với trạng thái tài khoản).
   - Tài khoản tồn tại và đúng mật khẩu Argon2id (nếu sai → tăng bộ đếm thử sai; trả `401 UNAUTHORIZED` chung không lộ tài khoản tồn tại hay không).
   - `users.status` phải là `ACTIVE` (`LOCKED` → `403 ACCOUNT_LOCKED`; `DELETED` → `403 ACCOUNT_DELETED`).
   - `users.role` phải là `ADMIN` (`USER` → `403 FORBIDDEN`).
4. Server đặt lại bộ đếm thử sai về `0`, thu hồi phiên `ADMIN_APP` cũ (nếu có) và đẩy `session.revoked` (`reason = ADMIN_NEW_LOGIN`) + đóng WSS cũ bằng mã `4004`, tạo phiên `ADMIN_APP` mới trong `user_sessions`, ghi 1 dòng vào `login_logs` (`session_scope = ADMIN_APP`) — **không** ghi `app_open_events` — và trả `LoginResponse (SESSION_GRANTED)`.
5. Client lưu token phiên trong bộ nhớ, mở kết nối WSS `/ws` kèm header `Authorization: Bearer <token>`, xóa sạch `PasswordField` và gọi `navigator.reset(AdminViews.OVERVIEW)`.

#### Đăng xuất & Thu hồi phiên đột xuất
- Bấm **Đăng xuất** trên sidebar → vô hiệu hóa nút, gọi `POST /api/v1/auth/logout`, đóng WSS (`1000`), xóa token/trạng thái phiên và `navigator.reset(AdminViews.SIGN_IN)`.
- Khi nhận khung `session.revoked` hoặc close code `4001`–`4004`, Client dừng reconnect WSS ngay, xóa cache điều hướng bằng `navigator.reset(AdminViews.SIGN_IN, reason)` và hiển thị thông báo lý do trên `A-SCR-01`.

### 3.2. Dashboard Tổng quan (`A-SCR-02`)

#### Luồng chính
1. Sau khi vào `A-SCR-02`, Client hiển thị **skeleton riêng** cho 4 thẻ KPI, khối biểu đồ xu hướng và 2 bảng rút gọn, rồi gọi `GET /api/v1/admin/overview` (ngoài JavaFX Application Thread).
2. Server xác định năm hiện tại theo `Asia/Ho_Chi_Minh` (`asOfDate`) và truy vấn trong PostgreSQL + bộ nhớ presence:
   - `totalUsers`: tổng tài khoản còn lưu trong `users` (không gồm tài khoản đã purge).
   - `onlineUsers`: số `userId` duy nhất đang có ít nhất 1 kết nối WSS `USER_CLIENT` hoạt động.
   - `totalGroups`: tổng hội thoại `type = 'GROUP'` trong `conversations`.
   - `pendingSpamReports`: số bản ghi `spam_reports` có `review_status = 'PENDING'` và còn trong hạn **180 ngày** (`received_at + INTERVAL '4320 hours' > CURRENT_TIMESTAMP`).
   - `monthlyRegistrations` & `monthlyActiveUsers`: đủ 12 tháng `T1..T12` của năm `year` (tháng trống = `0`).
   - `recentSpamReports`: tối đa 3 báo cáo mới nhất theo `(received_at DESC, id DESC)` còn trong hạn 180 ngày.
   - `recentLogins`: tối đa 3 lượt đăng nhập mới nhất theo `(login_at DESC, id DESC)`; nếu `user_id IS NULL` (đã purge) thì hiển thị nhãn ẩn danh `"Người dùng đã xóa"`.
3. Khi nhận sự kiện WSS `admin.kpi.updated` hoặc `admin.spam_report.created` / `admin.spam_report.reviewed`, UI cập nhật chỉ số KPI và danh sách tóm tắt tương ứng (chỉ khi đang ở phiên hợp lệ).
4. Bấm vào thẻ KPI hoặc liên kết điều hướng sang `A-SCR-03`, `A-SCR-04`, `A-SCR-05`, `A-SCR-06`, `A-SCR-07`, `A-SCR-08`. Bấm `onlineUsersCard` mở `A-SCR-03` ở trạng thái mặc định (không gắn bộ lọc giả).

#### Ngoại lệ dữ liệu
- Nếu truy vấn tổng quan thất bại hoặc trả lỗi, UI hiển thị trạng thái lỗi kèm nút **Thử lại** — **tuyệt đối không** thay lỗi truy vấn bằng `0` hay danh sách rỗng (`UC-ADM-OVW-01` NFR-01).

### 3.3. Nhật ký Đăng nhập Hệ thống (`A-SCR-06` / `A-07`)

#### Luồng chính
1. Mở `A-SCR-06`, Client hiển thị skeleton bảng và gọi `GET /api/v1/admin/login-logs?limit=50`.
2. Server truy vấn `login_logs LEFT JOIN users` theo thứ tự `(login_at DESC, id DESC)` với con trỏ opaque `before` (Base64URL của `(loginAt, id)`), bảo đảm không trùng/sót khi có nhiều lượt đăng nhập mới phát sinh giữa các trang.
3. UI hiển thị `loggedInAt` theo `Asia/Ho_Chi_Minh`, `username` (hoặc nhãn ẩn danh nếu `userId == null`), `fullName`, giữ nguyên vạch phân tách ngày (`DAY_START`).
4. Nếu `hasMore == true`, hiển thị nút **Tải thêm** để tải trang tiếp theo bằng `nextCursor`, giữ vị trí cuộn hiện tại.

---

## 4. Trạng thái UI & Xử lý phản hồi về muộn

| Trạng thái | `A-SCR-01` | `A-SCR-02` | `A-SCR-06` |
| --- | --- | --- | --- |
| **Đang tải** | Nút submit hiện trạng thái đang xử lý, khóa input | Skeleton riêng cho 4 thẻ KPI, LineChart, 2 bảng | Skeleton bảng; giữ hàng cũ + spinner ở nút **Tải thêm** khi phân trang |
| **Có dữ liệu** | Chuyển vào `A-SCR-02` | Hiển thị số liệu, tooltip tháng, 3 dòng gần đây | Hiển thị danh sách theo ngày `Asia/Ho_Chi_Minh` |
| **Rỗng** | — | KPI bằng `0` hợp lệ; bảng hiện `"Chưa có báo cáo spam"` / `"Chưa có lượt đăng nhập"` | Hiện placeholder `"Chưa có lượt đăng nhập"` |
| **Lỗi thử lại được** | Banner lỗi + giữ `username`, cho bấm lại | Banner lỗi kèm nút **Thử lại** (không hiển thị `0` giả) | Banner lỗi kèm nút **Thử lại** |
| **Mất kết nối** | Thông báo không kết nối được máy chủ | Banner mất kết nối + giữ dữ liệu lần tải trước (nếu có) | Banner mất kết nối + giữ các dòng đã tải |

- **Chống ghi đè & vòng đời (`onLeave`):** Mọi lời gọi REST/WSS chạy ngoài JavaFX Application Thread, gắn `requestSeq` theo màn hình và hủy/bỏ qua khi rời màn hình, đóng ứng dụng hoặc đăng xuất.

---

## 5. Các mục còn mở đã chốt

| Mục còn mở | Quyết định áp dụng |
| --- | --- |
| Nút Đăng xuất trên `AdminShell` | Bổ sung nút Đăng xuất tách biệt với Thoát ứng dụng (`POST /api/v1/auth/logout` + đóng WSS + `navigator.reset(SIGN_IN)`). |
| Chi tiết `AdminOverviewDto` cho bảng spam gần đây | Bổ sung `asOfDate` và `recentSpamReports: AdminRecentSpamReportDto[]` vào `AdminOverviewDto` (chỉ metadata, không có `evidence`). |
| Nguồn đếm `onlineUsers` | Đếm user duy nhất có kết nối WSS `USER_CLIENT` đang mở; `ADMIN_APP` không cộng vào chỉ số này. |

---

## 6. Tiêu chí chấp nhận (Acceptance Criteria)

1. Tài khoản `USER` hoặc `LOCKED` hoặc `DELETED` đăng nhập vào `A-SCR-01` đều bị từ chối với mã lỗi tương ứng (`403`), không cấp phiên và không lộ tồn tại tài khoản khi sai mật khẩu.
2. Đăng nhập `ADMIN_APP` trên máy B làm phiên `ADMIN_APP` trên máy A nhận `session.revoked` (`ADMIN_NEW_LOGIN`, mã đóng `4004`), mất quyền gọi API ngay lập tức, trong khi phiên `USER_CLIENT` của cùng tài khoản vẫn hoạt động bình thường.
3. Đăng nhập `ADMIN_APP` tạo 1 bản ghi trong `login_logs` (`scope = ADMIN_APP`) và **không** tạo bản ghi trong `app_open_events`.
4. `A-SCR-02` hiển thị đúng 4 KPI, đủ 12 tháng `T1..T12` theo `Asia/Ho_Chi_Minh`, tối đa 3 spam report còn hạn 180 ngày và tối đa 3 login log mới nhất; lỗi mạng không hiển thị `0`.
5. `A-SCR-06` tải trang 50 dòng bằng cursor không trùng/sót bản ghi khi có lượt đăng nhập mới xen vào; tài khoản đã purge hiển thị nhãn ẩn danh và không gán nhầm cho tài khoản mới trùng `username`.
