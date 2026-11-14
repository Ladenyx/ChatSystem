# Kế hoạch triển khai Version 1 — ChatSystem

**Mục tiêu chung:** Hoàn thành mốc **Version 1 (trọng số 20%, dự kiến 22/11/2026)**

Hai trọng tâm cốt lõi của Version 1:
1. **Mã nguồn giao diện JavaFX chạy được (`src/`)** cho cả hai phân hệ **User Client** và **Admin App (Desktop GUI)** — chưa bắt buộc xử lý nghiệp vụ backend/socket, nhưng cần điều hướng qua lại và có dữ liệu mẫu để hiển thị và chụp màn hình báo cáo (không dùng mockup tĩnh thay thế).
2. **Thiết kế Cơ sở dữ liệu hoàn chỉnh (`script/`)** chạy được từ đầu trên database PostgreSQL trống, kèm tài liệu báo cáo và ảnh chụp lịch sử commit Git của từng thành viên (`git/`).

---

## Giai đoạn 1: Khởi tạo khung dự án & UI Shell dùng chung (1–2 ngày)

Mục tiêu là tạo "xương sống" để mọi thành viên có thể kéo code về và làm giao diện độc lập trên nhánh riêng mà không xung đột.

- [ ] **1.1. Khởi tạo cấu trúc Maven & thư mục chuẩn**
  - Thiết lập Maven Wrapper (`mvnw`) với cấu trúc multi-module (`common`, `server`, `user`, `admin`) theo [tech-stack.md](../decisions/tech-stack.md) (`Eclipse Temurin JDK 25`, `JavaFX 25`, `AtlantaFX 2.0.1`, `Ikonli`), đảm bảo giữ đúng cấu trúc thư mục nộp bài (`src/`, `script/`, `git/`).
- [ ] **1.2. Xây dựng bộ khung điều hướng (Navigation / Scene Manager) & Theme**
  - Khởi tạo class khởi chạy cho **User Client** và **Admin App**, nạp theme `Primer Light` / `Primer Dark` của AtlantaFX.
  - Tạo `ViewNavigator` cho phép chuyển đổi giữa các màn hình FXML và bật các hộp thoại Modal/Drawer/Popover để phục vụ demo luồng màn hình và chụp ảnh báo cáo.
- [ ] **1.3. Tạo bộ dữ liệu giả lập (UI Mock / Dummy Models)**
  - Tạo sẵn các class dữ liệu hiển thị (Record/ViewModel) chứa danh sách người dùng mẫu, tin nhắn mẫu, nhóm mẫu, báo cáo spam mẫu và số liệu 12 tháng để các bảng (`TableView`), danh sách (`ListView`) và biểu đồ (`BarChart`) hiển thị đầy đặn khi chạy UI.

---

## Giai đoạn 2: Thiết kế CSDL & Viết Script khởi tạo (`X-01` → `script/`) (2 ngày)

Thực hiện song song với Giai đoạn 1–3 để sớm chốt cấu trúc bảng đưa vào báo cáo.

- [ ] **2.1. Thiết kế sơ đồ thực thể kết hợp (ERD) & Bảng vật lý (PostgreSQL 18)**
  - **Nhóm Tài khoản & Xác thực (`U-01`–`U-05`, `A-01`–`A-05`, `A-07`):** `users` (trạng thái `ACTIVE`/`LOCKED`/`DELETED`, thời điểm xóa mềm), `pending_registrations` (TTL 30 phút), `email_verification_codes` (TTL 10 phút), `password_reset_tokens` (mật khẩu tạm TTL 15 phút), `user_sessions` (tách phạm vi phiên User và Admin), `login_logs`, `app_open_events`.
  - **Nhóm Quan hệ Bạn bè & Chặn (`U-06`–`U-11`, `A-06`, `A-14`):** `friend_requests`, `friendships`, `blocks`.
  - **Nhóm Hội thoại, Nhóm chat & Tin nhắn (`U-12`–`U-24`, `A-08`, `A-09`, `A-15`):** `conversations` (1-1 và nhóm), `conversation_members` (vai trò `ADMIN`/`MEMBER`, thời điểm tham gia/rời), `messages` (hỗ trợ bản rõ 1-1, bản mã E2EE, trạng thái thu hồi 48h), `message_deletions` (xóa phía mình theo từng user).
  - **Nhóm Khóa E2EE (`U-25`):** `e2ee_devices` (khóa công khai `X25519`/`Ed25519`), `e2ee_key_backups` (bản sao lưu khóa mã hóa bằng mã khôi phục), `e2ee_group_key_envelopes` (khóa phiên nhóm bọc cho từng thành viên/thiết bị).
  - **Nhóm Báo cáo Spam (`U-26`, `A-10`, `A-11`):** `spam_reports`, `spam_report_evidences` (lưu gói bằng chứng Selected + tối đa 30 tin Context theo [account-lifecycle.md](../decisions/account-lifecycle.md#L59-L67)).
- [ ] **2.2. Viết và kiểm thử `script/`**
  - Viết file SQL khởi tạo trong `script/` (ví dụ `script/schema.sql` và `script/seed.sql` hoặc gộp theo quy ước nhóm) kèm các `INDEX` quan trọng (như `(conversation_id, created_at DESC)`, `pg_trgm` + `unaccent` cho tìm kiếm tên/tin nhắn).
  - **Kiểm tra bắt buộc:** Chạy script từ đầu trên một database PostgreSQL 18 trống để chắc chắn không có lỗi cú pháp hay thứ tự khóa ngoại.

---

## Giai đoạn 3: Xây dựng Giao diện `User Client` (3–4 ngày)

Bám sát đặc tả tại [screen.md](../requirements/screen.md#L52-L84), chia thành 3 cụm màn hình để triển khai FXML + Controller UI:

- [ ] **3.1. Cụm 1 — Xác thực & Khởi tạo khóa (`U-SCR-01` → `U-SCR-06`)**
  - `U-SCR-01`: Đăng nhập (`Sign In View`).
  - `U-SCR-02`: Đăng ký (`Sign Up View`).
  - `U-SCR-03`: Xác minh Email (`Email Verification View`, có bộ đếm ngược gửi lại mã).
  - `U-SCR-04`: Quên mật khẩu (`Forgot Password View`).
  - `U-SCR-05`: Đặt mật khẩu bắt buộc (`Force Change Password View`, khi đăng nhập bằng mật khẩu tạm).
  - `U-SCR-06`: Thiết lập / Khôi phục khóa E2EE (`E2EE Key Setup & Recovery View`).
- [ ] **3.2. Cụm 2 — Khung chính (`MainShell`), Hội thoại (`U-SCR-07`) & Drawer thông tin (`U-SCR-09`)**
  - Thanh điều hướng dọc (Icon Rail) bên trái cùng.
  - `U-SCR-07`: Cột trái (nút Tạo nhóm mới, ô tìm kiếm tổng hợp `U-18`, bộ lọc *Tất cả/Chưa đọc/Nhóm*, danh sách chat); Khung giữa (header hội thoại, luồng tin nhắn phân trang, trạng thái gửi, menu xóa 1/nhiều dòng & thu hồi 48h, thanh soạn thảo có **2 nút AI riêng biệt: `Chỉnh sửa (Refine)` và `Gợi ý (Recommend)`**).
  - `U-SCR-09`: Panel trượt bên phải (tìm kiếm trong hội thoại `U-17`, quản lý thành viên/Admin nhóm `U-20`–`U-23`, nút xóa lịch sử `U-15`, báo cáo spam, chặn/rời nhóm).
- [ ] **3.3. Cụm 3 — Bạn bè (`U-SCR-08`) & 5 Hộp thoại (`U-DLG-01` → `U-DLG-05`)**
  - `U-SCR-08`: 5 phân mục (sub-tab): *Tất cả bạn bè*, *Đang Online*, *Lời mời kết bạn*, *Tìm người dùng*, *Đã chặn*.
  - `U-DLG-01`: Hồ sơ & Bảo mật Tài khoản (Hồ sơ, Đổi mật khẩu, Quản lý khóa E2EE).
  - `U-DLG-02`: Tạo nhóm chat mới.
  - `U-DLG-03`: Popover Trợ lý AI (`Refine` & `Recommend`, kèm bước xem trước/xác nhận gửi bản rõ).
  - `U-DLG-04`: Báo cáo Spam (chọn tin vi phạm, lý do và xem trước gói bằng chứng).
  - `U-DLG-05`: Chọn Admin kế nhiệm.

---

## Giai đoạn 4: Xây dựng Giao diện `Admin App` (Desktop GUI) (3–4 ngày)

Thực hiện song song với Giai đoạn 3 theo đặc tả tại [screen.md](../requirements/screen.md#L87-L118):

- [ ] **4.1. Cụm 1 — Khung Sidebar, Tổng quan & Quản trị Master–Detail (`A-SCR-01` → `A-SCR-06`, `A-DLG-01`..`02`)**
  - `A-SCR-01`: Đăng nhập Quản trị (`Admin Sign In View`) và khung chính có Sidebar phân nhóm.
  - `A-SCR-02`: Dashboard Tổng quan (4 thẻ KPI + biểu đồ xu hướng nhanh + danh sách tóm tắt).
  - `A-SCR-03`: Quản lý Người dùng dạng `SplitPane` (bảng lọc/sắp xếp bên trái + User Inspector cố định bên phải kèm 2 tab *Lịch sử đăng nhập* `A-05` và *Danh sách bạn bè* `A-06`).
  - `A-SCR-04`: Quản lý Nhóm Chat (danh sách nhóm bên trái + danh sách Admin/Thành viên bên phải).
  - `A-SCR-05`: Kiểm duyệt Báo cáo Spam (danh sách báo cáo bên trái + khung xem bằng chứng tin nhắn dạng bong bóng chat và nút Khóa tài khoản bên phải).
  - `A-SCR-06`: Nhật ký Đăng nhập Hệ thống theo thời gian (`A-07`).
  - `A-DLG-01`: Modal Thêm / Cập nhật Người dùng.
  - `A-DLG-02`: Modal Xác nhận Thao tác Quản trị.
- [ ] **4.2. Cụm 2 — 3 Trang Dashboard Thống kê & Biểu đồ (`A-SCR-07` → `A-SCR-09`)**
  - `A-SCR-07`: Dashboard Đăng ký mới (`BarChart` 12 tháng theo năm chọn `A-13` ở trên + bảng lọc theo khoảng ngày/họ tên `A-12` ở dưới).
  - `A-SCR-08`: Dashboard Hoạt động Người dùng (`BarChart` 12 tháng `A-16` ở trên + bảng chi tiết 3 chỉ số *Lượt mở app / Số người đã chat / Số nhóm đã chat* kèm bộ lọc toán tử `=`, `<`, `>` `A-15` ở dưới).
  - `A-SCR-09`: Phân tích Mạng lưới Bạn bè (`A-14`: bảng hiển thị *Bạn trực tiếp* và *Tổng bạn mở rộng (tính cả bạn của bạn)* kèm bộ lọc toán tử `=`, `<`, `>`).

---

## Giai đoạn 5: Nghiệm thu nội bộ, Viết Báo cáo & Đóng gói nộp Version 1 (1–2 ngày cuối)

Theo bảng **Version 1 deliverables** và **Pre-submission checks** tại [ROADMAP.md](../../ROADMAP.md#L55-L71):

- [ ] **5.1. Cập nhật trạng thái [ROADMAP.md](../../ROADMAP.md)**
  - Đánh dấu `✅` vào cột **UI** cho các tính năng `U-01`–`U-27`, `A-01`–`A-16` và `X-01`.
  - Điền GitHub handle của từng thành viên vào cột **Owner**.
- [ ] **5.2. Thu thập ảnh chụp Commit (`git/`)**
  - Đảm bảo **mọi thành viên trong nhóm đều có commit thực tế trên Git** (theo quy định môn học: thành viên không có commit nhận 0 điểm).
  - Chụp ảnh lịch sử commit của từng thành viên lưu vào thư mục `git/`.
- [ ] **5.3. Soạn thảo Báo cáo Version 1 (`report/`)**
  - Thông tin nhóm, danh sách công việc của từng thành viên và bảng tỷ lệ đóng góp (tổng cộng đúng **100%**).
  - Thiết kế cơ sở dữ liệu (sơ đồ ERD + giải thích các bảng/ràng buộc chính).
  - Danh sách toàn bộ màn hình đã thiết kế kèm **ảnh chụp màn hình thực tế chạy từ ứng dụng JavaFX**.
- [ ] **5.4. Kiểm tra cấu trúc gói nộp cuối cùng**
  - Đảm bảo file nén chứa đúng `src/`, `script/`, `git/` và file Báo cáo theo đúng quy định của đề bài.
  - Kiểm tra không commit hoặc đóng gói kèm mật khẩu, API key hay thông tin nhạy cảm thật.

