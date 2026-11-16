# Làm SQL theo miền mà không chặn nhau

Bộ khung tuân theo ba nguyên tắc: mỏ neo dùng chung, file thuộc từng miền, và database kiểm thử riêng cho mỗi lượt. PostgreSQL 18 có thể chạy lâu dài bằng Compose; vòng sửa SQL không phụ thuộc việc tạo lại container/volume.

## Hợp đồng chung

- `script/anchors/users.sql` là định nghĩa `users` duy nhất; `id` có kiểu UUID và là PK. Bảng hiện chứa hồ sơ cơ bản, chưa phải toàn bộ nhóm Tài khoản/Xác thực hay cơ chế đăng nhập.
- `script/anchors/conversations.sql` giữ hợp đồng hội thoại dùng chung: ID, loại DIRECT/GROUP, tên nhóm, người tạo và thời gian. E2EE và Spam chỉ cần hợp đồng này, không nạp file Messaging đang phát triển.
- Mỗi miền tham chiếu hai mỏ neo này. Không sao chép `CREATE TABLE users`, không thay khóa gốc để chạy riêng một bài test.
- Thay đổi hợp đồng mỏ neo cần phối hợp trước và cập nhật common smoke test. Thành viên triển khai nghiệp vụ thường chỉ sửa DDL, fixture và case của miền mình.

## Tách file

```text
script/
├── schema.sql                  # Entry point cho khởi tạo toàn bộ
├── common/
│   ├── bootstrap.sql           # Transaction, chọn miền và \ir
│   ├── environment.sql         # PostgreSQL 18, pg_trgm, unaccent
│   └── selection.sql           # all / messaging / e2ee / spam
├── anchors/
│   ├── users.sql               # Một định nghĩa users dùng chung
│   └── conversations.sql       # Hợp đồng tham chiếu hội thoại
├── domains/
│   ├── messaging.sql
│   ├── e2ee.sql
│   └── spam.sql
├── tests/
│   ├── run.sql                 # Guard DB thử, test, rollback, kiểm tra sạch
│   ├── common/                 # Helper, fixture và smoke test của mỏ neo
│   ├── messaging/              # fixtures.sql + cases.sql
│   ├── e2ee/                   # fixtures.sql + cases.sql
│   └── spam/                   # fixtures.sql + cases.sql
├── test-db.py                  # Warm Docker hoặc native qua --native
└── test-native.py              # Entry point native tiện dùng
```

`schema.sql` nạp tất cả miền. Chế độ từng miền nạp cùng mỏ neo nhưng chỉ DDL/test của miền được chọn. Các file của miền khác có thể đang sửa dở mà không chặn vòng test riêng. Chạy `--domain all` trước khi tích hợp để phát hiện va chạm tên bảng/hàm/index và các phụ thuộc phát sinh giữa miền.

Các file DDL/fixture/case miền trong PR khung đang để sẵn vị trí, chưa chứa nghiệp vụ. Smoke test chung kiểm tra UUID PK của users, tính duy nhất username/email, NULL, FK người tạo, loại/tên hội thoại, trạng thái xóa và giữ hội thoại khi bỏ người tạo.

## Vòng sửa/test

1. Khởi động Compose một lần; chọn lệnh `--domain` của mình theo [hướng dẫn](../../script/README.md).
2. Sửa file DDL và case tương ứng.
3. Runner tạo DB ngẫu nhiên có prefix `chatsystem_schema_test`, nạp schema từ checkout hiện tại và chạy case.
4. Lỗi SQL/assertion trả exit code khác 0; thành công báo số check/thời gian. Fixture được rollback, DB thử và bản sao SQL riêng được dọn.
5. Lặp lại lệnh sau mỗi thay đổi. Database phát triển và volume vẫn được giữ.

Tên DB và thư mục SQL có suffix riêng nên các tiến trình không drop/truncate dữ liệu của nhau. Docker runner chỉ gọi `dropdb` cho DB đã được chính lượt đó tạo thành công. Native runner dùng cả cụm/socket/thư mục riêng và không mở TCP. Không có script reset database phát triển trong bộ khung.

Các bài test miền về sau cần giữ fixture trong transaction của `run.sql`, không tự commit. Kiểm tra phân quyền, giao thức E2EE và luồng xử lý Server được bổ sung theo đặc tả; 12 smoke check của khung không được coi là các tính năng đó đã hoàn tất.
