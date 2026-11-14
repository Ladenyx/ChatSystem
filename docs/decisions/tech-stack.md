# Tech stack — ChatSystem

| Thành phần | Công nghệ / quyết định đã chốt |
| --- | --- |
| Java | Eclipse Temurin JDK 25 |
| Desktop UI | JavaFX 25, FXML/CSS cho cả **User Client** và **Admin App** (đều là ứng dụng Desktop GUI trực quan, không dùng CLI/Console cho phân hệ quản trị); AtlantaFX 2.0.1 (theme Primer Light / Primer Dark) + Ikonli (bộ icon đồng bộ) |
| Backend | Spring Boot 4.1.x |
| Database | PostgreSQL 18; JDBC/JdbcTemplate (kết hợp In-Memory state `ConcurrentHashMap` / `Caffeine` trên Server cho trạng thái Online/Offline và phiên WebSocket; `pg_trgm` + `unaccent` cho tìm kiếm văn bản) |
| Build & Đóng gói | Maven 3.9 qua Maven Wrapper (`mvnw`); bốn module nghiệp vụ (`common`, `server`, `user`, `admin`) và thư viện JavaFX dùng chung `desktop-ui`; mục tiêu đóng gói 3 fat-JAR độc lập (`user.jar`, `admin.jar`, `server.jar`) đọc file cấu hình ngoài trong `release/` |
| Giao tiếp | HTTPS REST + WSS, dữ liệu JSON |
| Bảo mật & Mật khẩu | Băm mật khẩu bằng **Argon2id** qua `Spring Security Crypto` + `Bouncy Castle` theo chuẩn OWASP (`m=19456 KiB` / 19 MiB, `t=2`, `p=1`) |
| Mã hóa nhóm (E2EE) | **Hybrid E2EE** trên JDK JCA + Bouncy Castle (`AES-256-GCM` cho tin nhắn/lịch sử + `X25519`/`Ed25519` bọc khóa thành viên và xoay khóa; `PBKDF2`/`Argon2` cho bản sao lưu khóa khôi phục) |
| Email | **Gmail SMTP** (App Password, cổng `587` STARTTLS / `465` SMTPS) qua `Spring Mail` (`JavaMailSender`) cho xác minh email và khôi phục mật khẩu, cấu hình qua file ngoài |
| AI | **Meta Model API (Muse API)** (`POST https://api.meta.ai/v1/chat/completions`, model mặc định `muse-spark-1.3-contributor`, `response_format: json_schema`, `reasoning_effort: "minimal"`/`"low"`) cho cả **chỉnh sửa câu nháp và gợi ý trả lời** (2 nút riêng trên thanh soạn tin); ngân sách mục tiêu 2 USD/tháng, cấu hình qua file server |
| Kiểm thử (Testing) | JUnit 5 (Jupiter) + AssertJ + Mockito + Spring Boot Test (kiểm thử tích hợp trên PostgreSQL / Testcontainers) |
| Triển khai | VPS Linux (Hetzner / DigitalOcean, 4–6 USD/tháng, mở cổng SMTP `587`/`465`), chạy Docker Compose (Spring Boot Server + PostgreSQL 18 + Reverse Proxy tự cấp TLS cho HTTPS/WSS) |

**Kiến trúc:** User Client / Admin App (Desktop GUI) → Server → PostgreSQL, Gmail SMTP, LLM API. Thông tin đăng nhập database, SMTP và API key dịch vụ chỉ ở server (hoặc file cấu hình theo yêu cầu gói nộp `release/`).

**Nền tảng & Cấu hình máy tối thiểu:**
- **Hệ điều hành:** ưu tiên nghiệm thu Windows Intel x64; Arch Linux x64 và macOS Apple Silicon arm64 phục vụ phát triển. Phát hành đủ ba OS là phạm vi bổ sung.
- **Cấu hình máy tối thiểu:** CPU 2 vCPU, RAM 4 GB, cài sẵn JDK 25, độ phân giải màn hình tối thiểu `1366x768`.
- **Gói nộp (`release/`):** giữ các file JAR chạy được (`user.jar`, `admin.jar`, kèm `server.jar`) và file cấu hình kết nối ngoài.

**Khung build/UI đã khởi tạo (Version 1, mục 1.1–1.2):** Maven Wrapper 3.9.11 (`mvnw`, `mvnw.cmd`), các module trong `src/`, POM cha quản lý JavaFX 25, AtlantaFX 2.0.1, Ikonli 12.4.0 và JUnit 5.13.4. `UserApplication` / `AdminApplication` chạy bằng `javafx:run`, dùng thư viện `desktop-ui` cho điều hướng FXML, overlay và Primer Light/Dark. Xem [lệnh build/test/launch](../../README.md#chạy-user-client-và-admin-app) và [kiến trúc desktop](../architecture/desktop-ui.md). Spring Boot và fat-JAR sẽ được triển khai ở giai đoạn tiếp theo.
