# Kế hoạch triển khai Version 1 — ChatSystem

**Mục tiêu chung:** Hoàn thành mốc **Version 1 (trọng số 20%, dự kiến 22/11/2026)**

Hai trọng tâm cốt lõi của Version 1:
1. **Mã nguồn giao diện JavaFX chạy được (`src/`)** cho cả hai phân hệ **User Client** và **Admin App (Desktop GUI)** — chưa bắt buộc xử lý nghiệp vụ backend/socket, nhưng cần điều hướng qua lại và có dữ liệu mẫu để hiển thị và chụp màn hình báo cáo (không dùng mockup tĩnh thay thế).
2. **Thiết kế Cơ sở dữ liệu hoàn chỉnh (`script/`)** chạy được từ đầu trên database PostgreSQL trống, kèm tài liệu báo cáo và ảnh chụp lịch sử commit Git của từng thành viên (`git/`).

---

## Giai đoạn 1: Khởi tạo khung dự án & UI Shell dùng chung (1–2 ngày)

Mục tiêu là tạo "xương sống" để mọi thành viên có thể kéo code về và làm giao diện độc lập trên nhánh riêng mà không xung đột.

- [x] **1.1. Khởi tạo cấu trúc Maven & thư mục chuẩn**
  - Đã thiết lập Maven Wrapper (`mvnw`, `mvnw.cmd`, Maven 3.9.11) với cấu trúc multi-module gồm bốn module nghiệp vụ (`common`, `server`, `user`, `admin`) và thư viện JavaFX dùng chung `desktop-ui` trong `src/` theo [tech-stack.md](../../decisions/tech-stack.md) (`Eclipse Temurin JDK 25`, `JavaFX 25`, `AtlantaFX 2.0.1`, `Ikonli 12.4.0`), giữ các thư mục nộp bài (`src/`, `script/`, `git/`) cùng `report/`, `release/`.
  - POM cha quản lý phiên bản thư viện/plugin, kiểm tra JDK/Maven; mỗi module có thư mục nguồn, tài nguyên và kiểm thử chuẩn Maven. Xem lệnh build/test và phạm vi khung tại [README.md](../../../README.md).
  - Đã kiểm tra `./mvnw clean verify` cho toàn bộ reactor và `./mvnw -pl src/user -am verify` với Temurin JDK 25 trên Linux; `validate` với JDK 17 bị từ chối đúng yêu cầu. Chưa có test case hoặc lớp khởi chạy ứng dụng ở bước này.
- [x] **1.2. Xây dựng bộ khung điều hướng (Navigation / Scene Manager) & Theme**
  - Khởi tạo class khởi chạy cho **User Client** và **Admin App**, nạp theme `Primer Light` / `Primer Dark` của AtlantaFX.
  - Tạo `ViewNavigator` cho phép chuyển đổi giữa các màn hình FXML và bật các hộp thoại Modal/Drawer/Popover để phục vụ demo luồng màn hình và chụp ảnh báo cáo.
  - Đã có `UserApplication` / `AdminApplication`, rail/sidebar tham chiếu Figma, đổi Primer Light/Dark trực tiếp, cache trang, lịch sử quay lại, reset phiên và quản lý focus/đóng overlay. Thêm thư viện `desktop-ui` để chia sẻ UI mà giữ `common`/`server` độc lập với JavaFX.
  - Đã kiểm tra 10 test GUI trên Linux với Temurin JDK 25 + Xvfb, bao gồm kích thước nội dung 1280 × 720 và 1024 × 640, cùng lệnh launch Maven cho cả hai ứng dụng. Xem [kiến trúc và cách gắn FXML](../../architecture/desktop-ui.md), [ảnh trong lịch sử Git](https://github.com/Ladenyx/ChatSystem/blob/47e3662b15611c6715c62e79295a83ef4a29d03d/docs/images/navigation-theme/README.md) và [lệnh launch](../../../README.md#chạy-user-client-và-admin-app).
  - Các route nghiệp vụ hiện dùng nội dung khung; bộ dữ liệu mẫu và các màn hình/biểu mẫu đầy đủ vẫn thuộc mục 1.3 và giai đoạn 3–4.
  - Trạng thái Figma theo cập nhật của nhóm: wireframe Admin đã hoàn thành, wireframe User chưa hoàn thành; file còn có các thành phần/tài liệu tham khảo, chưa phải bộ giao diện cuối cùng đã chốt. Khung 1.2 tham khảo phần đang có và không đồng nghĩa hoàn tất triển khai các wireframe.
- [x] **1.3. Tạo bộ dữ liệu giả lập (UI Mock / Dummy Models)**
  - Tạo sẵn các class dữ liệu hiển thị (Record/ViewModel) chứa danh sách người dùng mẫu, tin nhắn mẫu, nhóm mẫu, báo cáo spam mẫu và số liệu 12 tháng để các bảng (`TableView`), danh sách (`ListView`) và biểu đồ (`BarChart`) hiển thị đầy đặn khi chạy UI.
  - Đã có record và `MockData.create()` dùng chung trong `desktop-ui`: 20 hồ sơ, 9 hội thoại/126 tin, 4 nhóm, 6 báo cáo kèm bằng chứng Selected/Context, 37 lượt đăng nhập, 20 dòng hoạt động và đủ 12 tháng năm 2025/2026. Dữ liệu bất biến, ID/thời gian cố định, email hư cấu `@example.test`; `common`/`server` không phụ thuộc bộ mock.
  - Hai ứng dụng có nút **Xem dữ liệu mẫu** trên toolbar để kiểm tra bảng/danh sách/biểu đồ; màn hình nghiệp vụ vẫn thuộc giai đoạn 3–4. Đã kiểm tra 7 test dữ liệu không cần display và 11 test GUI trên Linux với Temurin JDK 25 + Xvfb. Xem [hướng dẫn sử dụng](../../architecture/ui-mock-data.md) và [ảnh trong lịch sử Git](https://github.com/Ladenyx/ChatSystem/blob/47e3662b15611c6715c62e79295a83ef4a29d03d/docs/images/ui-mock-data/README.md).

---

## Giai đoạn 2: Thiết kế CSDL & Viết Script khởi tạo (`X-01` → `script/`) (2 ngày)

Thực hiện song song với Giai đoạn 1–3 để sớm chốt cấu trúc bảng đưa vào báo cáo.

- [x] **2.1. Thiết kế sơ đồ thực thể kết hợp (ERD) & Bảng vật lý (PostgreSQL 18)**
  - **[Nhóm Tài khoản & Xác thực](../../databases/erd/authentication.drawio.svg) (`U-01`–`U-05`, `A-01`–`A-05`, `A-07`):** `users` (trạng thái `ACTIVE`/`LOCKED`/`DELETED`, thời điểm xóa mềm), `pending_registrations` (TTL 30 phút), `email_verification_codes` (TTL 10 phút), `password_reset_tokens` (mật khẩu tạm TTL 15 phút), `user_sessions` (tách phạm vi phiên User và Admin).
  - **[Nhóm Quan hệ Bạn bè & Chặn](../../databases/erd/friends-and-blocks.drawio.svg) (`U-06`–`U-11`, `A-06`, `A-14`):** `friend_requests`, `friendships`, `blocks`.
  - **[Nhóm Hội thoại, Nhóm chat & Tin nhắn](../../databases/erd/messaging.drawio.svg) (`U-12`–`U-24`, `A-08`, `A-09`, `A-15`):** `conversations` (1-1 và nhóm), `conversation_members` (vai trò `ADMIN`/`MEMBER`, thời điểm tham gia/rời), `messages` (hỗ trợ bản rõ 1-1, bản mã E2EE, trạng thái thu hồi 48h), `message_deletions` (xóa phía mình theo từng user).
  - **[Nhóm Khóa E2EE](../../databases/erd/e2ee.drawio.svg) (`U-25`):** `e2ee_devices` (khóa công khai `X25519`/`Ed25519`), `e2ee_key_backups` (bản sao lưu khóa mã hóa bằng mã khôi phục), `e2ee_group_key_epochs` (các epoch khóa nhóm), `e2ee_group_key_envelopes` (khóa phiên nhóm bọc cho từng thành viên/thiết bị).
  - **[Nhóm Báo cáo Spam](../../databases/erd/spam.drawio.svg) (`U-26`, `A-10`, `A-11`):** `spam_reports`, `spam_report_evidences` (snapshot bằng chứng Selected/Context), `spam_report_archives` (số liệu hồ sơ đã lưu trữ), `spam_report_request_tombstones` (digest yêu cầu đã tiếp nhận), theo [DEC-10](../../decisions/spam-moderation-and-retention.md).
  - **[Nhóm Nhật ký & Thống kê](../../databases/erd/logs-and-analytics.drawio.svg) (`A-05`, `A-07`, `A-12`–`A-16`):** `login_logs`, `app_open_accounts`, `account_registration_events`, `app_open_events`, `message_send_events`. Lịch sử đăng ký và metadata gửi tin dùng khóa thống kê độc lập để giữ số liệu sau purge theo [quyết định thống kê](../../decisions/statistics-and-time.md).
  - Sáu ERD chỉnh sửa được trong draw.io đã được đối chiếu với bảng vật lý: **25 bảng, 158 cột, 36 khóa ngoại**, gồm kiểu dữ liệu, NULL, PK, FK và các đầu quan hệ.
- [x] **2.2. Viết và kiểm thử `script/` (kèm `compose.yaml`)**
  - [`script/schema.sql`](../../../script/schema.sql) khởi tạo schema trong một transaction, điều phối `\ir` theo thứ tự 00 → 10 → 20 → 30. Đã có DDL và test cho cả ba luồng, dùng chung mỏ neo `users`; runner tạo database tạm riêng, rollback fixture và dọn tài nguyên từng lượt theo [quy trình SQL song song](../../databases/workflow.md).
  - Đã có index lịch sử tin nhắn `(conversation_id, created_at DESC, id DESC)`, index phục vụ các luồng nghiệp vụ/thống kê, extension `pg_trgm` và `unaccent`. [`compose.yaml`](../../../compose.yaml) dùng PostgreSQL 18, mount `./script` vào `/docker-entrypoint-initdb.d:ro` để team dùng chung cách khởi tạo trên Arch Linux, macOS và Windows.
  - **Đã nghiệm thu trên database trống:** [CI trên `main`](https://github.com/Ladenyx/ChatSystem/actions/runs/44610003728) đạt cả Docker Compose với volume mới và PostgreSQL **18.7 cài trực tiếp trên Windows Server 2025**. Mỗi môi trường đạt **381 kiểm tra tích hợp**, cùng ba bộ riêng: Auth/Friends **95**, Messaging/E2EE/Spam **156**, Logs/Analytics **140**; các ca ghi đồng thời và kiểm tra rollback/dọn tài nguyên đều đạt.
  - Workflow [PostgreSQL 18 acceptance](../../../.github/workflows/database.yml) kiểm tra UTF-8/tiếng Việt, extension và database khởi tạo trống trước/sau các lượt; tự chạy lại khi SQL, Compose hoặc workflow thay đổi. Xem [hướng dẫn chạy và nghiệm thu](../../../script/README.md#nghiệm-thu-tự-động-trên-docker-và-windows). Phần tích hợp backend, kiểm tra quyền, giao thức E2EE và job xử lý dữ liệu tiếp tục ở Version 2 theo các đặc tả nghiệp vụ.

---

## Giai đoạn 3: Xây dựng Giao diện `User Client` (3–4 ngày)

Bám sát đặc tả tại [screen.md](../../requirements/screen.md), chia thành 3 cụm màn hình để triển khai FXML + Controller UI:

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
  - `U-DLG-03`: Popover Trợ lý AI (`Refine` & `Recommend` theo [llm-assistant.md](../../decisions/llm-assistant.md)): tự động lấy tối đa **50 tin nhắn** hợp lệ gần nhất từ local client để AI hiểu ngữ cảnh và văn phong cuộc trò chuyện, hiển thị ngay **3 phương án** (`Refine` bản nháp hoặc `Recommend` câu trả lời) để chọn điền vào ô nháp (bản rõ ngữ cảnh cục bộ và bản nháp được gửi qua Server tới Meta Model API).
  - `U-DLG-04`: Báo cáo Spam (chọn tin vi phạm, lý do và xem trước gói bằng chứng).
  - `U-DLG-05`: Chọn Admin kế nhiệm.

---

## Giai đoạn 4: Xây dựng Giao diện `Admin App` (Desktop GUI) (3–4 ngày)

Thực hiện song song với Giai đoạn 3 theo đặc tả tại [screen.md](../../requirements/screen.md):

- [ ] **4.1. Cụm 1 — Khung Sidebar, Tổng quan & Quản trị Master–Detail (`A-SCR-01` → `A-SCR-06`, `A-DLG-01`..`02`)**
  - `A-SCR-01`: Đăng nhập Quản trị (`Admin Sign In View`) và khung chính có Sidebar phân nhóm.
  - `A-SCR-02`: Dashboard Tổng quan (4 thẻ KPI + biểu đồ xu hướng nhanh + danh sách tóm tắt).
  - `A-SCR-03`: Quản lý Người dùng dạng `SplitPane` (bảng lọc/sắp xếp bên trái + User Inspector cố định bên phải kèm 2 tab *Lịch sử đăng nhập* `A-05` và *Danh sách bạn bè* `A-06`).
  - `A-SCR-04`: Quản lý Nhóm Chat (danh sách nhóm bên trái + danh sách Admin/Thành viên bên phải).
  - `A-SCR-05`: Kiểm duyệt Báo cáo Spam (danh sách báo cáo bên trái + khung xem bằng chứng tin nhắn dạng bong bóng chat, nút Khóa tài khoản riêng và thao tác Đánh dấu đã xử lý kèm ghi chú theo DEC-10 bên phải).
  - `A-SCR-06`: Nhật ký Đăng nhập Hệ thống theo thời gian (`A-07`).
  - `A-DLG-01`: Modal Thêm / Cập nhật Người dùng.
  - `A-DLG-02`: Modal Xác nhận Thao tác Quản trị.
- [ ] **4.2. Cụm 2 — 3 Trang Dashboard Thống kê & Biểu đồ (`A-SCR-07` → `A-SCR-09`)**
  - `A-SCR-07`: Dashboard Đăng ký mới (`BarChart` 12 tháng theo năm chọn `A-13` ở trên + bảng lọc theo khoảng ngày/họ tên `A-12` ở dưới).
  - `A-SCR-08`: Dashboard Hoạt động Người dùng (`BarChart` 12 tháng `A-16` ở trên + bảng chi tiết 3 chỉ số *Lượt mở app / Số người đã chat / Số nhóm đã chat* kèm bộ lọc toán tử `=`, `<`, `>` `A-15` ở dưới).
  - `A-SCR-09`: Phân tích Mạng lưới Bạn bè (`A-14`: bảng hiển thị *Bạn trực tiếp* và *Tổng bạn mở rộng (tính cả bạn của bạn)* kèm bộ lọc toán tử `=`, `<`, `>`).

---

## Giai đoạn 5: Nghiệm thu nội bộ, Viết Báo cáo & Đóng gói nộp Version 1 (1–2 ngày cuối)

Theo bảng **Version 1 deliverables** và **Pre-submission checks** tại [ROADMAP.md](../../../ROADMAP.md#version-1-deliverables):

- [ ] **5.1. Cập nhật trạng thái [ROADMAP.md](../../../ROADMAP.md)**
  - Đánh dấu `✅` vào cột **UI** cho các tính năng `U-01`–`U-27`, `A-01`–`A-16` và `X-01`.
  - Điền GitHub handle của từng thành viên vào cột **Owner**.
- [ ] **5.2. Thu thập ảnh chụp Commit (`git/`)**
  - Đảm bảo **mọi thành viên trong nhóm đều có commit thực tế trên Git** (theo quy định môn học: thành viên không có commit nhận 0 điểm).
  - Chụp ảnh lịch sử commit của từng thành viên lưu vào thư mục `git/`.
- [ ] **5.3. Soạn thảo Báo cáo Version 1 (`report/`)**
  - Thông tin nhóm, danh sách công việc của từng thành viên và bảng tỷ lệ đóng góp (tổng cộng đúng **100%**).
  - Thiết kế cơ sở dữ liệu (sơ đồ ERD + giải thích các bảng/ràng buộc chính).
  - Danh sách toàn bộ màn hình đã thiết kế kèm **ảnh chụp màn hình thực tế chạy từ ứng dụng JavaFX**.
- [ ] **5.4. Kiểm thử chéo trên Windows & kiểm tra cấu trúc gói nộp cuối cùng**
  - Chạy kiểm thử giao diện trực tiếp trên máy **Windows** (`.\mvnw.cmd -pl src/user javafx:run` và `.\mvnw.cmd -pl src/admin javafx:run` ở độ phân giải tối thiểu `1366x768`) để xác nhận không lỗi font tiếng Việt, không lệch/tràn bố cục so với máy Arch Linux và macOS trước khi chụp ảnh đưa vào báo cáo.
  - Đảm bảo file nén chứa đúng `src/`, `script/`, `git/` và file Báo cáo theo đúng quy định của đề bài (kèm POM cha, `mvnw`, `mvnw.cmd`, `.mvn/` để build được từ đầu).
  - Kiểm tra không commit hoặc đóng gói kèm mật khẩu, API key hay thông tin nhạy cảm thật.

