# Khung SQL và vòng kiểm thử độc lập

Bộ khung dùng PostgreSQL 18, `psql` và Python 3 (standard library, không cần `pip`). Đây là hạ tầng để các thành viên xây SQL và kiểm thử từng miền song song. Các file miền hiện là placeholder; 12 ca smoke test kiểm chứng mỏ neo/hạ tầng, chưa đại diện cho kiểm thử nghiệp vụ hoàn chỉnh.

## Dựng PostgreSQL một lần

Từ thư mục gốc repo, sao chép `.env.example` thành `.env` (`cp` trên Linux/macOS hoặc `Copy-Item` trong PowerShell), thay placeholder `POSTGRES_PASSWORD` bằng mật khẩu cục bộ rồi chạy Docker Engine/Docker Desktop.

```sh
docker compose config --quiet
docker compose up -d --wait db
docker compose ps
```

Database phát triển mặc định là `chatsystem`, user `chatsystem`, host `127.0.0.1`, port `55432`. Các giá trị cấu hình nằm trong `.env`; file này được Git bỏ qua.

`docker compose down` dừng container và giữ named volume. `schema.sql` chỉ tự chạy khi volume còn trống. Với volume đã có từ PR Compose, vòng test bên dưới vẫn nạp bộ khung mới vào DB tạm; không cần reset volume phát triển để bắt đầu làm SQL.

## Sửa SQL → chạy lại test

Giữ service `db` chạy và chọn miền của mình:

```sh
python3 script/test-db.py --domain messaging
python3 script/test-db.py --domain e2ee
python3 script/test-db.py --domain spam
```

PowerShell có thể dùng `py -3` thay `python3`. Chạy tích hợp các miền bằng:

```sh
python3 script/test-db.py --domain all
```

Mỗi lượt tạo DB mới dạng `chatsystem_schema_test_<miền>_<mã ngẫu nhiên>`, nạp mỏ neo cùng đúng miền cần kiểm tra, chạy test rồi xóa **DB tạm do lượt đó tạo**. Các fixture được rollback trước khi dọn. Không chạy `docker compose down -v`, không xóa/tạo lại database phát triển `chatsystem`.

Mỗi lượt cũng sao chép SQL từ checkout hiện tại vào một thư mục tạm riêng trong container. Vì vậy nhiều terminal/worktree dùng cùng service DB vẫn chạy trên đúng phiên bản file của mình, không ghi đè file test của nhau. Dùng `--project <tên>` nếu service DB được khởi chạy với một Compose project khác.

Vòng warm-Docker này tận dụng server đang chạy; thời gian thực tế tùy máy. Bộ kiểm tra native bên dưới đã đo dưới 2 giây/lượt trên môi trường Linux hiện tại. Lần tải image/khởi động Docker đầu tiên không nằm trong vòng sửa/test nhanh.

## Kiểm tra khi không dùng Docker

Linux/macOS có PostgreSQL 18 trong `PATH` có thể dùng cụm tạm chỉ mở Unix socket, tách khỏi server và dữ liệu hiện có:

```sh
python3 script/test-native.py --domain messaging
python3 script/test-native.py --domain e2ee
python3 script/test-native.py --domain spam
python3 script/test-native.py --domain all
```

Có thể thêm `--pg-bin /path/to/postgresql/18/bin`. Cụm native được dừng và xóa sau mỗi lượt; các lượt chạy đồng thời không dùng chung socket hay thư mục dữ liệu. Trên Windows, dùng Docker runner.

## File mỗi thành viên cần sửa

| Miền | DDL | Fixture và kiểm tra riêng |
| --- | --- | --- |
| Hội thoại/Tin nhắn | `domains/messaging.sql` | `tests/messaging/fixtures.sql`, `tests/messaging/cases.sql` |
| E2EE | `domains/e2ee.sql` | `tests/e2ee/fixtures.sql`, `tests/e2ee/cases.sql` |
| Spam | `domains/spam.sql` | `tests/spam/fixtures.sql`, `tests/spam/cases.sql` |

Các đường dẫn trong bảng tính từ `script/`. File miền được include trong transaction chung; không đặt `BEGIN`/`COMMIT`, `DROP SCHEMA` hoặc định nghĩa lại bảng mỏ neo trong file miền.

[`anchors/users.sql`](anchors/users.sql) là định nghĩa `users` duy nhất. [`anchors/conversations.sql`](anchors/conversations.sql) là hợp đồng hội thoại chung để E2EE/Spam tham chiếu độc lập. Thay đổi mỏ neo cần thống nhất giữa các thành viên; miền nghiệp vụ tham chiếu `public.users(id)`/`public.conversations(id)` thay vì tạo stub riêng.

`common/bootstrap.sql` nạp môi trường, mỏ neo và miền được chọn. `tests/run.sql` nạp fixture/test tương ứng qua `\ir`, bật `ON_ERROR_STOP`, rollback và kiểm tra không còn dữ liệu thử. Dùng `pg_temp.assert_true(label, boolean)` hoặc `pg_temp.expect_error(label, SQL, SQLSTATE[, constraint])` trong file `cases.sql`. Đặt nhãn case có prefix miền (ví dụ `e2ee/current-backup`) để các bộ test không trùng nhãn khi chạy `all`.

Chạy toàn bộ script khởi tạo trực tiếp trên database mới, trống bằng:

```sh
psql -X -v ON_ERROR_STOP=1 -d ten_database_moi -f script/schema.sql
```

`\ir` giải đường dẫn theo file đang được include, không phụ thuộc thư mục làm việc. Xem [quy ước phối hợp SQL](../docs/databases/workflow.md) và [kế hoạch Version 1](../docs/management/plans/version-1-plan.md).
