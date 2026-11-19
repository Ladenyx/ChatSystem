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

Thêm `--pg-bin /path/to/postgresql/18/bin` nếu cần. Cụm native được dừng và xóa sau lượt; không kết nối server hiện có. Thời gian gồm khởi tạo cụm mới và các ca ghi đồng thời của luồng đã chọn; phụ thuộc I/O của máy. Docker runner tận dụng server đang chạy để lặp DDL/Test mà không khởi tạo lại cả cụm.

## Kiểm thử với PostgreSQL cài trực tiếp

Khi PostgreSQL 18 đã chạy trên máy, dùng `--local-server` để tạo DB kiểm thử riêng trên server đó. Role kết nối cần quyền `CREATEDB`; cấu hình mật khẩu cục bộ qua cơ chế libpq (`pgpass`/`PGPASSFILE`), giữ file mật khẩu ngoài repo. Nếu terminal đã đặt `PGPASSWORD`, gỡ biến này khi chuyển sang passfile vì libpq ưu tiên mật khẩu được chỉ định sẵn. Runner không hỏi mật khẩu tương tác và không dùng thông tin kết nối trong `.env` của Compose.

Ví dụ PowerShell với PostgreSQL được cài ở đường dẫn mặc định; điều chỉnh đường dẫn hoặc role theo máy:

```powershell
py -3 script/test-db.py --local-server --flow all --pg-user postgres --pg-bin "C:\Program Files\PostgreSQL\18\bin"
```

Trên Linux/macOS có thể dùng `python3` và bỏ `--pg-bin` nếu các chương trình PostgreSQL 18 có trong `PATH`. Server luôn được kết nối qua `127.0.0.1`; thêm `--pg-port` nếu khác cổng `5432`. Runner kiểm tra phiên bản client/server, tạo DB trống với UTF-8, chạy DDL/Test và chỉ xóa DB do chính lượt đó tạo. Đây là cách nghiệm thu SQL trên PostgreSQL cài trực tiếp ở Windows; chế độ `--native` tạo Unix socket không dùng trên Windows.

## Nghiệm thu tự động trên Docker và Windows

Workflow [PostgreSQL 18 acceptance](../.github/workflows/database.yml) chạy khi PR hoặc commit vào `main` thay đổi `script/`, Compose hay chính workflow; cũng có thể chạy thủ công từ GitHub Actions. Hai job độc lập kiểm tra:

- Ubuntu: Compose project/volume mới cho mỗi lượt, khởi tạo qua entrypoint của image `postgres:18` và `script/schema.sql` được mount chỉ đọc.
- Windows Server 2025: bản PostgreSQL 18.7-1 chính thức từ [EDB](https://www.enterprisedb.com/download-postgresql-binaries), đối chiếu SHA-256, chạy trực tiếp `postgres.exe` trên loopback và nạp `schema.sql` bằng `psql.exe`. Role kiểm thử có `CREATEDB`, không có quyền superuser.

Cả hai job xác nhận phiên bản, UTF-8, extension, dữ liệu tiếng Việt và database khởi tạo trống bằng [`tests/check-bootstrap.sql`](tests/check-bootstrap.sql), rồi chạy `all` và cả ba luồng riêng. Các ca ghi đồng thời và kiểm tra rollback/dọn dữ liệu nằm trong runner hiện có. Database khởi tạo được kiểm tra lại sau toàn bộ lượt để phát hiện việc runner ghi nhầm vào đó.

Compose chỉ dọn project/volume riêng của lượt CI; Windows dừng server tạm và xóa passfile. Mật khẩu được sinh khi chạy, không dùng credential của team. Log GitHub Actions ghi phiên bản, số lượng bảng/cột/FK, số assertion và kết quả kiểm tra đồng thời; đây là bằng chứng nghiệm thu có thể chạy lại sau mỗi thay đổi SQL. Khi nâng bản Windows, cập nhật đồng thời URL và checksum trong workflow từ nguồn EDB.

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

## Mô hình dữ liệu thống kê bền vững

[`sql/30-logs-and-analytics.sql`](sql/30-logs-and-analytics.sql) chỉ phụ thuộc `users` trong core. [ERD Logs/Analytics](../docs/databases/erd/logs-and-analytics.drawio.svg) chứa bảng, hàng, ô và quan hệ draw.io native, có thể mở SVG để chỉnh sửa trực tiếp. Năm bảng của luồng này gồm:

| Bảng | Vai trò |
| --- | --- |
| `login_logs` | Metadata cấp phiên thành công, phân biệt User Client và Admin App |
| `app_open_accounts` | Khóa thống kê độc lập dùng chung cho đăng ký, mở app và gửi tin; giữ tên bảng hiện có |
| `account_registration_events` | Một sự kiện tạo thành công cho mỗi tài khoản `USER`/`ADMIN`, gồm cả Admin khởi tạo qua CLI |
| `app_open_events` | Một sự kiện mở User Client cho mỗi tài khoản/lần khởi chạy |
| `message_send_events` | Metadata gửi tin được server chấp nhận: khóa thống kê người gửi/nhận, ID nhóm và thời điểm chấp nhận |

Trigger `users_record_registration` cấp khóa thống kê và ghi `users.created_at` ngay khi tạo tài khoản, kể cả tài khoản chưa đăng nhập. Yêu cầu đăng ký tạm chưa tạo `users` không phát sinh sự kiện. Dữ liệu tạo tài khoản được giữ riêng nên thay đổi hồ sơ và purge không làm mất thời điểm tạo gốc.

Backend gọi `record_app_open` trong transaction cấp phiên User Client. Với tin nhắn, backend phải kiểm tra phiên, hội thoại, thành viên và payload, rồi gọi `record_message_send(message_id, sender_user_id, recipient_user_id, group_id, accepted_at)` trong **cùng transaction chấp nhận/lưu tin**. Chỉ một trong `recipient_user_id` hoặc `group_id` có giá trị; thời điểm chấp nhận do server xác định. Helper này ghi metadata và không tự cấp quyền gửi tin. Luồng standalone dùng được helper mà không cần nạp DDL Messaging.

Retry cùng `message_id` và cùng người gửi/đích giữ sự kiện cùng thời điểm chấp nhận đầu tiên. Tái sử dụng ID cho người gửi/đích khác bị từ chối. `message_id` và `group_id` trong ledger là ID snapshot, không FK tới tin/nhóm đang tồn tại; xóa hoặc thu hồi tin, đóng/xóa nhóm không giảm số liệu lịch sử. Hai ledger mới chặn sửa các sự kiện đã ghi.

Khi purge, `app_open_accounts.user_id` được đặt `NULL`; các sự kiện và khóa thống kê vẫn tồn tại. Khóa thống kê bất biến, liên kết chỉ được gỡ khi tài khoản đã purge và không được gán lại cho tài khoản khác. Username/email dùng lại thuộc tài khoản và khóa thống kê mới. Số liệu được giữ lâu dài theo [quyết định thống kê](../docs/decisions/statistics-and-time.md), với metadata riêng cho từng tài khoản/đích để đếm `distinct` chính xác trong khoảng ngày được chọn.

Test đọc trực tiếp các ledger bền vững, gồm các trường hợp purge hai phía, dùng lại username/email, retry đổi mốc tháng, rollback, thu hồi và đóng/xóa nhóm. Các bảng tạm còn lại phục vụ tham số/fixture; không giữ bản sao lịch sử thay cho bảng thật. Chế độ `all` còn kiểm tra transaction cùng bảng Messaging/Auth/Friends thực tế; runner dùng hai kết nối riêng để kiểm tra retry đồng thời của lượt mở và metadata gửi tin.

Để khởi tạo thủ công trên database mới, trống:

```sh
psql -X -v ON_ERROR_STOP=1 -d ten_database_moi -f script/schema.sql
```

`\ir` giải đường dẫn theo file đang include. Xem [quy trình phối hợp SQL](../docs/databases/workflow.md) và [kế hoạch Version 1](../docs/management/plans/version-1-plan.md).
