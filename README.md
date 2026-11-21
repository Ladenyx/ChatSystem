# ChatSystem

Ứng dụng chat Java gồm **User Client** và **Admin App** dạng desktop GUI, kết nối đến Server. Kiến trúc và công nghệ đã chốt tại [tech-stack.md](docs/decisions/tech-stack.md); công việc Version 1 tại [version-1-plan.md](docs/management/plans/version-1-plan.md).

## Yêu cầu phát triển

- **Eclipse Temurin JDK 25**; đặt `JAVA_HOME` trỏ đến thư mục JDK và thêm `JAVA_HOME/bin` vào `PATH`.
- Không cần cài Maven riêng: Maven Wrapper tải **Maven 3.9.11** ở lần chạy đầu. Linux/macOS cần `curl` hoặc `wget` và `unzip`; Windows dùng PowerShell.
- Cần kết nối mạng lần đầu để tải Maven, plugin và thư viện từ Maven Central.
- Mở `pom.xml` ở thư mục gốc bằng IDE, chọn Project SDK và Maven Runner JDK là **25**.

Phiên bản thư viện quản lý tập trung trong POM cha: **JavaFX 25**, **AtlantaFX 2.0.1**, **Ikonli 12.4.0** (Feather icons), **JUnit Jupiter 5.13.4**. Maven Enforcer kiểm tra JDK 25 và Maven 3.9.x ngay tại bước `validate`.

## Cấu trúc thư mục

```text
ChatSystem/
├── pom.xml                         # POM cha và danh sách module
├── mvnw / mvnw.cmd                 # Maven Wrapper cho Unix / Windows
├── .mvn/wrapper/                   # Phiên bản Maven và checksum
├── compose.yaml                   # PostgreSQL 18 cho môi trường phát triển
├── .env.example                   # Mẫu cấu hình cục bộ, sao chép thành .env
├── src/
│   ├── common/                    # DTO và mô hình giao thức dùng chung
│   ├── server/                    # Nghiệp vụ, persistence và tích hợp dịch vụ
│   ├── desktop-ui/                # Điều hướng, theme và dữ liệu UI mẫu dùng chung
│   ├── user/                      # User Client — JavaFX desktop GUI
│   └── admin/                     # Admin App — JavaFX desktop GUI
├── script/                        # Script PostgreSQL
├── git/                           # Ảnh lịch sử commit từng thành viên
├── report/                        # Báo cáo, ERD và tài liệu nộp bài
├── release/                       # JAR chạy được và cấu hình ngoài (Version 2)
└── docs/
    ├── architecture/              # Kiến trúc và hướng dẫn tích hợp UI
    ├── requirements/
    ├── decisions/
    └── management/
```

Mỗi module trong `src/` giữ cấu trúc Maven chuẩn:

```text
<module>/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/chatsystem/<module>/
    │   └── resources/
    └── test/
        ├── java/com/chatsystem/<module>/
        └── resources/
```

Với `user` và `admin`, tài nguyên nằm trong `src/main/resources/com/chatsystem/<module>/`, gồm `fxml/`, `css/`, `images/`. Các file `.gitkeep` giữ thư mục trống trong Git. Đường dẫn tài nguyên tuyệt đối khi dùng `getResource` sẽ có dạng `/com/chatsystem/user/fxml/<view>.fxml`.

`server`, `user`, `admin` phụ thuộc vào `common`; `common` không phụ thuộc JavaFX hay framework server. `user` và `admin` dùng thêm thư viện `desktop-ui` cho điều hướng, overlay và theme; `server` không phụ thuộc thư viện này. Đây là các **Maven module**; chưa sử dụng Java Platform Module System (`module-info.java`).

## PostgreSQL cho nhóm phát triển

Cài Docker Engine kèm Docker Compose trên Linux, hoặc Docker Desktop trên Windows/macOS; khởi động Docker trước khi chạy.

Sao chép `.env.example` thành `.env` (`cp .env.example .env` trên Linux/macOS, `Copy-Item .env.example .env` trong PowerShell), rồi thay `POSTGRES_PASSWORD` bằng mật khẩu cục bộ của bạn. File `.env` đã được Git bỏ qua.

```sh
docker compose config --quiet
docker compose up -d --wait db
docker compose ps
```

Kết nối từ máy phát triển: host `127.0.0.1`, port `55432`, database/user `chatsystem`; mật khẩu lấy từ `.env`. Có thể đổi tên, user và port qua các biến `POSTGRES_*` trong file đó. Dữ liệu được giữ trong named volume khi chạy `docker compose down`.

Compose nạp các script SQL trong `script/` khi khởi tạo volume trống. Xem [cách dùng PostgreSQL và script](script/README.md) để mở `psql`, xem log và xử lý khi script thay đổi. JavaFX tiếp tục chạy trực tiếp trên máy phát triển.

## Build và kiểm tra

Chạy từ thư mục gốc trên Linux/macOS:

```sh
./mvnw --version
./mvnw validate
./mvnw clean verify
```

Trên Windows PowerShell:

```powershell
.\mvnw.cmd --version
.\mvnw.cmd validate
.\mvnw.cmd clean verify
```

Build một module cùng các module mà nó phụ thuộc (thay `src/user` bằng `src/admin` hoặc `src/server` khi cần):

```sh
./mvnw -pl src/user -am verify
```

Chạy kiểm thử JUnit 5:

```sh
./mvnw test
```

Build mặc định không cần màn hình và bỏ qua các lớp kiểm thử GUI. Kiểm thử JavaFX cần desktop session hoặc Xvfb trên Linux và phải bật profile riêng:

```sh
./mvnw -Pui-tests verify
```

Profile này kiểm tra điều hướng/quay lại, giữ trạng thái trang, reset phiên, lỗi FXML, theme, modal/drawer/popover, trang dữ liệu mẫu và các màn hình User Client. Không bật profile thì các kiểm thử GUI được bỏ qua; 7 kiểm thử dữ liệu/series vẫn chạy. Để chạy riêng phần User Client và xuất thêm ảnh giao diện vào `src/user/target/screenshots/`:

```sh
./mvnw -pl src/user -am -Pui-tests -Dchatsystem.capture=true verify
```

Đặt class tên `<ClassName>Test` trong `src/test/java` của module tương ứng. Xem [hướng dẫn kiểm thử UI](docs/architecture/desktop-ui.md#kiểm-thử).

## Chạy User Client và Admin App

Cài các module dùng chung vào Maven local repository trước lần chạy đầu và sau khi sửa chúng:

```sh
./mvnw install
```

Mở hai terminal riêng từ thư mục gốc:

```sh
./mvnw -pl src/user javafx:run
./mvnw -pl src/admin javafx:run
```

Windows PowerShell dùng `.\mvnw.cmd install`, `.\mvnw.cmd -pl src/user javafx:run` và `.\mvnw.cmd -pl src/admin javafx:run`. Các lệnh này cần JDK 25 và môi trường đồ họa. Chạy `javafx:run` trên module ứng dụng, không thêm `-am` vào lệnh launch.

Cả hai ứng dụng chạy **ngoại tuyến với dữ liệu mẫu**. User Client mở màn đăng nhập trước; nút **Đăng nhập** chỉ chuyển sang giao diện hội thoại để xem trước, không xác thực tài khoản. Các nút chuyển bước trong màn đăng ký, xác minh email và khôi phục mật khẩu chỉ phục vụ xem giao diện: không gửi email, không tạo tài khoản và không đổi mật khẩu. Không nhập thông tin thật. Admin App vẫn mở thẳng khung xem trước.

Trong workspace, dùng rail/sidebar để chuyển trang, nút Quay lại hoặc **Alt + ←**, nút đổi sáng/tối và **Hiển thị** để mở popover. User Client có các khung **Hồ sơ**, **Tạo nhóm mới** (modal), **Thông tin** (drawer); dùng nút **Đóng** hoặc **Esc** để đóng.

Nút **hình cơ sở dữ liệu** trên toolbar (tooltip **Xem dữ liệu mẫu**) mở trang xem bộ mẫu của mục 1.3: 20 tài khoản, 9 hội thoại/126 tin nhắn, 4 nhóm, 6 báo cáo spam và biểu đồ đủ 12 tháng cho năm 2025/2026. Dữ liệu hư cấu cố định, dùng chung trong `desktop-ui`, có thêm nhật ký đăng nhập và chỉ số hoạt động. Xem [API, cách gắn TableView/ListView/BarChart và kiểm thử](docs/architecture/ui-mock-data.md), [ảnh trang dữ liệu mẫu trong lịch sử Git](https://github.com/Ladenyx/ChatSystem/blob/47e3662b15611c6715c62e79295a83ef4a29d03d/docs/images/ui-mock-data/README.md).

User Client có năm màn xác thực `U-SCR-01`–`05`. Các trang và hộp thoại nghiệp vụ ngoài phạm vi này vẫn là khung để gắn FXML ở giai đoạn 3–4. Xem [kiến trúc UI và cách thêm màn hình](docs/architecture/desktop-ui.md), [tham chiếu Figma](docs/architecture/figma_links.md) và [ảnh khung điều hướng trong lịch sử Git](https://github.com/Ladenyx/ChatSystem/blob/47e3662b15611c6715c62e79295a83ef4a29d03d/docs/images/navigation-theme/README.md).

Figma hiện gồm wireframe và tài liệu tham khảo: **wireframe Admin đã hoàn thành, wireframe User chưa hoàn thành**. Các lựa chọn trực quan của khung 1.2 chưa được coi là thiết kế cuối cùng đã chốt của nhóm.

Kết quả build nằm trong `target/` của từng module; `user`, `admin`, `server` tạo lần lượt `user.jar`, `admin.jar`, `server.jar`. Hiện chạy desktop qua Maven/IDE; đóng gói fat-JAR chạy bằng `java -jar` và Spring Boot server thuộc công việc tiếp theo.

## Cấu hình và đóng gói

- `script/` dành cho schema, index và dữ liệu mẫu PostgreSQL 18; việc tạo và kiểm thử SQL thuộc mục 2.2.
- `release/` dành cho JAR và cấu hình ngoài của Version 2; cấu hình thật đặt trong `release/config/` (đã được Git bỏ qua). Chỉ đưa mẫu không chứa bí mật vào Git, ví dụ `server.example.properties`.
- Database, SMTP và API key chỉ thuộc server; không đưa vào tài nguyên của User Client hoặc Admin App.
- Gói nộp Version 1 cần `src/`, `script/`, `git/` và báo cáo. Giữ kèm POM cha, `mvnw`, `mvnw.cmd`, `.mvn/` khi giao mã nguồn Maven để có thể build từ đầu.

Theo dõi tiến độ chức năng và yêu cầu nộp bài tại [ROADMAP.md](ROADMAP.md).

## Quy ước đóng góp

Dùng nhánh `feature/<ten-cong-viec>`, `fix/<ten-loi>` hoặc `docs/<ten-tai-lieu>`. Commit theo Conventional Commits, ví dụ `build: initialize Maven multi-module project` hoặc `docs(U-01): clarify signup rules`. Giữ các mã tính năng `U-01`, `A-01`, … khi cập nhật yêu cầu và roadmap; mỗi thành viên cần có commit của mình và PR cần một đồng đội review.
