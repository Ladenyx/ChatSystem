# Khung SQL cho ba luồng thành viên

Ba nguyên tắc: giữ `users` làm mỏ neo chung, mỗi thành viên có một cặp DDL/Test, và mỗi lượt kiểm thử dùng một database tạm riêng. Team dựng PostgreSQL một lần rồi lặp sửa/test mà không reset volume.

## Cấu trúc

```text
script/
├── schema.sql                         # Điều phối \ir theo thứ tự
├── sql/
│   ├── 00-core-users.sql              # Extension + users cơ sở dùng chung
│   ├── 10-auth-and-friends.sql        # Thành viên 2
│   ├── 20-messaging-e2ee-spam.sql      # Thành viên 1
│   └── 30-logs-and-analytics.sql       # Thành viên 3
├── tests/
│   ├── assertions.sql                 # Helper assert dùng chung
│   ├── fixtures.sql                   # User mẫu UUID cố định
│   ├── test-core-users.sql            # Smoke test mỏ neo dùng chung
│   ├── test-auth-friends.sql          # Thành viên 2
│   ├── test-messaging-e2ee-spam.sql    # Thành viên 1
│   ├── test-logs-analytics.sql         # Thành viên 3
│   └── run.sql                        # Gọi tất cả test khi tích hợp
├── test-db.py                         # Chọn luồng, DB tạm trên Docker/native
└── test-native.py                     # Entry point native tiện dùng
```

`schema.sql` giữ đúng bảy dòng điều phối: bật `ON_ERROR_STOP`, `BEGIN`, include bốn file 00/10/20/30 và `COMMIT`. Các DDL con nằm trong thư mục con để Docker chỉ chạy một entry point ở cấp đầu, không chạy trùng DDL.

`00-core-users.sql` là định nghĩa `users` duy nhất; PK `id` có kiểu UUID. Bảng hiện chứa hồ sơ cơ bản để làm hợp đồng FK, chưa phải toàn bộ nhóm Tài khoản/Xác thực hay cơ chế đăng nhập. Thay đổi hợp đồng mỏ neo cần phối hợp; không tạo `users` stub ở mỗi luồng.

Mỗi người phát triển DDL và test nghiệp vụ trong cặp file của mình, giữ entry point và helper chung ổn định. DDL/Test có phụ thuộc chéo giữa các luồng cần hợp đồng dữ liệu rõ và được kiểm tra bằng chế độ `all` trước khi tích hợp. Với luồng `messaging-e2ee-spam`, xem [mô hình và phạm vi kiểm thử](../../script/README.md#mô-hình-dữ-liệu-hội-thoại-e2ee-và-spam).

## Vòng kiểm thử độc lập

1. Chạy Compose một lần theo [hướng dẫn](../../script/README.md).
2. Sửa DDL và test thuộc luồng của mình.
3. Gọi `test-db.py --flow auth-friends`, `messaging-e2ee-spam` hoặc `logs-analytics`.
4. Runner tạo DB mới có prefix `chatsystem_schema_test`, nạp core + đúng DDL/Test đã chọn. Các file không được chọn không chạy.
5. Lỗi SQL/assertion trả exit code khác 0. Fixture được rollback; DB và bản SQL tạm được dọn. Lặp lại lệnh sau mỗi lần sửa.

Tên DB có suffix riêng để nhiều terminal/worktree không drop/truncate dữ liệu của nhau. SQL của từng checkout được chụp thành bản riêng trong thư mục tạm; Docker runner không phụ thuộc source mount của một worktree khác. Runner chỉ `dropdb` sau khi chính lượt đó đã tạo DB thành công, không thao tác volume hay database phát triển.

Native runner dùng cả cụm/socket/thư mục riêng và không mở TCP; đây cũng là phương án kiểm thử khi Docker daemon chưa chạy. Trên Windows, dùng Docker runner. Mục tiêu vòng nhanh 1–2 giây được đo bằng native trên Linux; thời gian Docker cần được xác nhận trên môi trường có daemon.

`tests/run.sql` quản lý transaction, helper, fixture, smoke test chung và các test thành viên. Khi chọn một luồng, runner tạo entry point được lọc trong thư mục tạm; không sửa file `run.sql` trong checkout. Chế độ `all` chạy nguyên các entry point gốc.

Các case nghiệp vụ cần giữ fixture trong transaction của `run.sql`, không tự commit. Phiên, phân quyền, giao thức E2EE và xử lý Server được bổ sung theo đặc tả; smoke test của khung không đánh dấu các chức năng đó hoàn tất.
