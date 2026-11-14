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
├── src/
│   ├── common/                    # DTO và mô hình giao thức dùng chung
│   ├── server/                    # Nghiệp vụ, persistence và tích hợp dịch vụ
│   ├── desktop-ui/                # Điều hướng, overlay, theme dùng chung cho desktop
│   ├── user/                      # User Client — JavaFX desktop GUI
│   └── admin/                     # Admin App — JavaFX desktop GUI
├── script/                        # Script PostgreSQL
├── git/                           # Ảnh lịch sử commit từng thành viên
├── report/                        # Báo cáo, ERD và tài liệu nộp bài
├── release/                       # JAR chạy được và cấu hình ngoài (Version 2)
└── docs/
    ├── images/                    # Ảnh dùng trong tài liệu
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

Profile này kiểm tra điều hướng/quay lại, giữ trạng thái trang, reset phiên, lỗi FXML, theme, modal/drawer/popover và khởi chạy hai ứng dụng. Không bật profile thì các kiểm thử cần JavaFX được đánh dấu **skipped**. Đặt class tên `<ClassName>Test` trong `src/test/java` của module tương ứng. Xem [hướng dẫn kiểm thử UI](docs/architecture/desktop-ui.md#kiểm-thử).

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

Hai ứng dụng mở thẳng **bản xem giao diện ngoại tuyến**. Dùng rail/sidebar để chuyển trang, nút Quay lại hoặc **Alt + ←**, nút đổi sáng/tối và **Hiển thị** để mở popover. User Client có **Hồ sơ**, **Tạo nhóm mới** (modal), **Thông tin** (drawer); dùng nút **Đóng** hoặc **Esc** để đóng. **Thoát** đóng ứng dụng; chưa có phiên đăng nhập ở bước 1.2.

Các trang và hộp thoại hiện là khung rỗng để gắn FXML nghiệp vụ ở giai đoạn 3–4. Bộ dữ liệu mẫu thuộc mục 1.3. Xem [kiến trúc UI và cách thêm màn hình](docs/architecture/desktop-ui.md), [tham chiếu Figma](docs/architecture/figma_links.md) và ảnh chạy thực tế trong `docs/images/navigation-theme/`.

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
