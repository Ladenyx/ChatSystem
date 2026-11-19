# Khung SQL và kiểm thử theo luồng thành viên

Bộ khung dùng PostgreSQL 18, `psql` và Python 3 (standard library, không cần `pip`). Mỗi thành viên có **một cặp DDL + Test riêng**. Smoke test chung kiểm tra mỏ neo/hạ tầng; test từng luồng kiểm tra các ràng buộc dữ liệu do luồng đó định nghĩa.

## Phân chia file

| Thành viên | Phạm vi | DDL trong `script/sql/` | Test trong `script/tests/` |
| --- | --- | --- | --- |
| 2 | Auth, Sessions, Friends, Blocks | `10-auth-and-friends.sql` | `test-auth-friends.sql` |
| 1 | Conversations, Messages, E2EE, Spam | `20-messaging-e2ee-spam.sql` | `test-messaging-e2ee-spam.sql` |
| 3 | Login logs, App open events, Analytics, Indexes | `30-logs-and-analytics.sql` | `test-logs-analytics.sql` |

[`sql/00-core-users.sql`](sql/00-core-users.sql) là nơi duy nhất định nghĩa extension và bảng `users` chung. Các luồng dùng `public.users(id)`, không tạo stub/copy của bảng này. Thay đổi hợp đồng mỏ neo cần thống nhất giữa các thành viên.

`schema.sql` chỉ điều phối `\ir` theo thứ tự 00 → 10 → 20 → 30, trong một transaction. Các file con đặt trong `sql/` để Docker không chạy lại chúng ở cấp đầu. `tests/run.sql` điều phối assert, fixture user và các file test. Các entry point này chỉ được thiết lập một lần; phát triển hàng ngày diễn ra trong cặp file của từng người.

## Dựng PostgreSQL một lần

Từ thư mục gốc repo, sao chép `.env.example` thành `.env` (`cp` trên Linux/macOS hoặc `Copy-Item` trong PowerShell), thay placeholder `POSTGRES_PASSWORD` bằng mật khẩu cục bộ rồi chạy Docker Engine/Docker Desktop.

```sh
docker compose config --quiet
docker compose up -d --wait db
docker compose ps
```

Database phát triển mặc định là `chatsystem`, user `chatsystem`, host `127.0.0.1`, port `55432`. Các giá trị nằm trong `.env`; file này được Git bỏ qua.

`schema.sql` chỉ tự chạy khi volume còn trống. Với volume đã có từ PR Compose, vòng test bên dưới vẫn nạp bộ khung mới vào DB tạm, nên không cần reset volume phát triển để bắt đầu làm SQL.

## Sửa DDL → chạy lại test

Giữ service `db` chạy và chọn luồng của mình:

```sh
python3 script/test-db.py --flow auth-friends
python3 script/test-db.py --flow messaging-e2ee-spam
python3 script/test-db.py --flow logs-analytics
```

PowerShell có thể dùng `py -3` thay `python3`. Chạy tích hợp trước khi merge bằng:

```sh
python3 script/test-db.py --flow all
```

Một lượt riêng chỉ nạp `00-core-users.sql` cùng DDL/Test được chọn và smoke test mỏ neo. DDL hoặc test của người khác đang sửa dở không được nạp trong lượt đó. Chế độ `all` dùng nguyên `schema.sql` và `tests/run.sql` để phát hiện lỗi tích hợp.

Mỗi lượt tạo DB dạng `chatsystem_schema_test_<luồng>_<mã ngẫu nhiên>`, chụp bản SQL từ checkout hiện tại rồi chạy kiểm tra. Fixture được rollback; cuối lượt chỉ xóa **DB tạm và bản SQL do lượt đó tạo**. Không chạy `docker compose down -v`, không xóa/tạo lại database phát triển `chatsystem`.

Các terminal/worktree có DB và bản SQL riêng, có thể dùng chung server Compose đang chạy. Dùng `--project <tên>` nếu server được khởi chạy với Compose project khác. Vòng warm-Docker tận dụng server sẵn có; thời gian thực tế tùy máy, không gồm lần tải image/khởi động Docker đầu tiên.

## Kiểm thử native nhanh

Linux/macOS có PostgreSQL 18 trong `PATH` có thể chạy cụm tạm riêng, chỉ mở Unix socket:

```sh
python3 script/test-native.py --flow auth-friends
python3 script/test-native.py --flow messaging-e2ee-spam
python3 script/test-native.py --flow logs-analytics
python3 script/test-native.py --flow all
```

Thêm `--pg-bin /path/to/postgresql/18/bin` nếu cần. Cụm native được dừng và xóa sau lượt; không kết nối server hiện có. Trên Windows dùng Docker runner. Bộ kiểm tra native đã đo dưới 2 giây/lượt trên Linux hiện tại; số đo Docker cần nghiệm thu khi có daemon chạy.

## Viết case

`tests/assertions.sql` cung cấp `pg_temp.assert_true(label, boolean)` và `pg_temp.expect_error(label, SQL, SQLSTATE[, constraint])`. `tests/fixtures.sql` có UUID user cố định: suffix 1=Alice, 2=Bob, 3=Carol, 4=Admin; đây là hồ sơ DB thử, không phải credential đăng nhập. `test-core-users.sql` giữ smoke test của mỏ neo.

Mỗi người thêm fixture riêng và case vào file test của mình. Dùng nhãn có prefix như `messaging/...` để tránh trùng khi chạy `all`. Không tự `BEGIN`/`COMMIT`, reset schema hoặc định nghĩa lại `users` trong cặp file nghiệp vụ. Runner phụ trách transaction và kiểm tra dữ liệu được rollback sạch.

## Mô hình dữ liệu hội thoại, E2EE và Spam

[`sql/20-messaging-e2ee-spam.sql`](sql/20-messaging-e2ee-spam.sql) tạo 12 bảng của luồng Thành viên 1, chỉ phụ thuộc `users(id)` trong core. ERD chỉnh sửa được nằm ở [Hội thoại](../docs/databases/erd/messaging.drawio.svg), [E2EE](../docs/databases/erd/e2ee.drawio.svg) và [Spam](../docs/databases/erd/spam.drawio.svg). Khi đổi mô hình, cập nhật cả SVG và kỳ vọng catalog trong [`test-messaging-e2ee-spam.sql`](tests/test-messaging-e2ee-spam.sql).

- Hội thoại: PK ghép cho thành viên và dấu xóa riêng; tin 1–1 lưu bản rõ, tin nhóm lưu bản mã. Thu hồi giữ hàng tin, xóa payload và kiểm tra timestamp trong 48 giờ. Loại hội thoại bất biến để giữ nhất quán cách lưu payload.
- E2EE: thiết bị có thể chờ tin cậy; FK ghép bảo đảm thiết bị phê duyệt thuộc cùng tài khoản. Revision backup duy nhất theo user, tối đa một backup hiện hành. Epoch/envelope lịch sử độc lập với vòng đời backup.
- Spam: snapshot không FK đến tin nguồn, nên thu hồi/xóa tin không làm mất bằng chứng đã tiếp nhận. Archive chỉ giữ ID mới, ngày, trạng thái và số lượng; tombstone chỉ giữ digest, không liên kết archive hay hồ sơ gốc, theo [DEC-10](../docs/decisions/spam-moderation-and-retention.md).
- Purge: giữ hội thoại/tin của người khác bằng `SET NULL` cho người tạo/người gửi; dữ liệu riêng của user được `CASCADE`. User còn xuất hiện trong report/evidence hoạt động bị `RESTRICT` để server chuyển toàn bộ hồ sơ sang archive, ghi tombstone và xóa snapshot cùng transaction trước khi purge.

Fixture riêng dùng suffix UUID 101–109 và được dọn trong transaction; bốn user mẫu chung được giữ nguyên để các luồng chạy tiếp. Test bao gồm đối chiếu cột/kiểu/NULL, PK, đích FK và hành vi xóa, cùng các trường hợp dữ liệu hợp lệ/không hợp lệ.

Các ràng buộc trên bảo vệ cấu trúc lưu trữ. Backend/client vẫn phải kiểm tra phiên và quyền, admin cuối nhóm, số người trong chat 1–1, quyền đọc lịch sử/thu hồi theo đồng hồ server, xác minh proof và cấp/xoay khóa theo [DEC-05](../docs/decisions/group-permissions-and-e2ee.md). Hạn mức Selected 1–20, Context tối đa 30, canonical payload tối đa 1 MiB, idempotency và job giữ/xóa dữ liệu theo DEC-10 cũng cần phần backend; byte fixture không phải dữ liệu mật mã đã xác minh.

Để khởi tạo thủ công trên database mới, trống:

```sh
psql -X -v ON_ERROR_STOP=1 -d ten_database_moi -f script/schema.sql
```

`\ir` giải đường dẫn theo file đang include. Xem [quy trình phối hợp SQL](../docs/databases/workflow.md) và [kế hoạch Version 1](../docs/management/plans/version-1-plan.md).
