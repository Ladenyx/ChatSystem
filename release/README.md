# Gói phát hành

Thư mục dành cho Version 2: `user.jar`, `admin.jar`, `server.jar` chạy được và file cấu hình kết nối ngoài. Các JAR khung trong `target/` hiện chưa phải bản phát hành.

Cấu hình thật đặt trong `config/` (đã được Git bỏ qua). Mẫu cấu hình đưa vào Git phải dùng placeholder (`server.example.properties`, `client.example.properties`); database, SMTP và LLM key chỉ thuộc server.

## Khởi chạy Server mẫu (cấu hình ngoài)

```sh
java -jar src/server/target/server.jar --spring.config.additional-location=optional:file:./config/server.properties
```

## Nạp dữ liệu mẫu & chạy Admin App kết nối Server thật

Sau khi chạy `script/schema.sql` trên PostgreSQL 18, nạp thêm dữ liệu mẫu (`sysadmin` / `Admin@123`, `user01`..`user03` / `User@123`):

```sh
psql -h 127.0.0.1 -p 55432 -U chatsystem -d chatsystem -f script/seed.sql
```

Khi chạy `./mvnw -pl src/admin javafx:run`, Admin App mặc định kết nối Server thật (truyền `-Dchatsystem.mock=true` nếu chỉ muốn xem preview ngoại tuyến).
