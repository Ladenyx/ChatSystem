# Tech stack — ChatSystem

| Thành phần | Công nghệ / hướng lựa chọn |
| --- | --- |
| Java | Eclipse Temurin JDK 25 |
| Desktop UI | JavaFX 25, FXML/CSS cho cả **User Client** và **Admin App** (đều là ứng dụng Desktop GUI trực quan, không dùng CLI/Console cho phân hệ quản trị); bổ sung AtlantaFX, phiên bản theme còn chờ xác nhận |
| Backend | Spring Boot 4.1.x |
| Database | PostgreSQL 18; JDBC/JdbcTemplate |
| Build | Maven 3.9 qua Maven Wrapper |
| Giao tiếp | HTTPS REST + WSS, dữ liệu JSON |
| Mã hóa nhóm | E2EE; chưa chọn giao thức/thư viện mật mã |
| Email | SMTP cho xác minh email và khôi phục mật khẩu; chưa chọn nhà cung cấp |
| AI | Muse API cho cả **chỉnh sửa câu nháp và gợi ý trả lời** (2 nút riêng trên thanh soạn tin); model/quyền truy cập cần xác minh, ngân sách mục tiêu 2 USD/tháng |
| Triển khai | Server trên Internet; hosting/tên miền chưa chọn, ngân sách dự kiến 5–10 USD/tháng |

**Kiến trúc:** User Client / Admin App (Desktop GUI) → Server → PostgreSQL, SMTP, LLM API. Thông tin đăng nhập database và API key dịch vụ chỉ ở server.

**Nền tảng:** ưu tiên nghiệm thu Windows Intel x64; Arch Linux x64 và macOS Apple Silicon arm64 phục vụ phát triển. Phát hành đủ ba OS là phạm vi bổ sung. Gói nộp giữ JAR user/admin, cấu hình và thành phần server cần thiết.

**Còn cần chốt:** cặp JavaFX/AtlantaFX, phiên bản dependency cụ thể, framework test, cấu hình máy tối thiểu, hosting/SMTP, giao thức E2EE và thuật toán/tham số băm mật khẩu. Chưa cấu hình lệnh build/test/launch.
