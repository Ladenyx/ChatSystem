# Gói phát hành

Thư mục dành cho Version 2: `user.jar`, `admin.jar`, `server.jar` chạy được và file cấu hình kết nối ngoài. Các JAR khung trong `target/` hiện chưa phải bản phát hành.

Cấu hình thật đặt trong `config/` (đã được Git bỏ qua). Mẫu cấu hình đưa vào Git phải dùng placeholder (`server.example.properties`, `client.example.properties`); database, SMTP và LLM key chỉ thuộc server.

## Khởi chạy Server mẫu (cấu hình ngoài)

```sh
java -jar src/server/target/server.jar --spring.config.additional-location=optional:file:./config/server.properties
```
