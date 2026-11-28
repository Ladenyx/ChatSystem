# Gói phát hành

Thư mục dành cho Version 2: `server.jar`, `user.jar`, `admin.jar` chạy độc lập và các mẫu file cấu hình ngoài (`server.example.properties`, `client.example.properties`).

Thông tin kết nối cơ sở dữ liệu, SMTP và LLM key đặt trong file cấu hình ngoài (không commit file chứa mật khẩu thật lên Git).

## 1. Đóng gói 3 fat-JAR đa nền tảng (Windows, Linux, macOS Apple Silicon)

Từ thư mục gốc repo (với JDK 25):

```sh
# Linux / macOS
./mvnw -Prelease clean package -DskipTests
```

```powershell
# Windows
.\mvnw.cmd -Prelease clean package -DskipTests
```

Sau khi build xong, 3 file JAR độc lập nằm tại:

- `src/server/target/server.jar`
- `src/user/target/user.jar`
- `src/admin/target/admin.jar`

## 2. Chuẩn bị file cấu hình ngoài

Sao chép các file mẫu thành cấu hình cục bộ đặt cùng thư mục với JAR (hoặc trong `./config/` / `./release/config/`):

- `server.example.properties` -> `server.properties` (cập nhật thông tin PostgreSQL 18, Gmail SMTP, LLM key)
- `client.example.properties` -> `client.properties` (cập nhật `chatsystem.server.base-url`)

## 3. Khởi tạo PostgreSQL 18 & nạp dữ liệu mẫu

Sau khi chạy `script/schema.sql` trên PostgreSQL 18, nạp dữ liệu mẫu (`sysadmin` / `Admin@123`, `user01`..`user03` / `User@123`):

```sh
psql -h 127.0.0.1 -p 55432 -U chatsystem -d chatsystem -f script/seed/demo-seed.sql
```

## 4. Khởi chạy từ JAR

```sh
# 1. Server (tự đọc ./server.properties, ./config/server.properties hoặc ./release/config/server.properties)
java -jar server.jar

# 2. Admin App (tự đọc ./client.properties, ./config/client.properties hoặc ./release/config/client.properties)
java --enable-native-access=ALL-UNNAMED -jar admin.jar

# 3. User Client
java --enable-native-access=ALL-UNNAMED -jar user.jar
```

Truyền `-Dchatsystem.mock=true` cho `admin.jar` nếu chỉ muốn xem preview giao diện ngoại tuyến không cần kết nối Server. Khi phát triển cục bộ bằng Maven, vẫn dùng `./mvnw -pl src/admin javafx:run` hoặc `./mvnw -pl src/user javafx:run`.
