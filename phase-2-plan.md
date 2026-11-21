# Kế hoạch giai đoạn 2 — Thành viên 3

Người thực hiện là bạn, đảm nhận Thành viên 3 trên Windows theo [bảng phân công](bang-phan-cong.md). Kế hoạch phục vụ mốc Version 1 dự kiến ngày 22/11/2026, với khoảng 2 ngày làm việc cho phần CSDL.

Mục tiêu là hoàn thành bảng nhật ký và lượt mở ứng dụng, index phục vụ thống kê, sơ đồ ERD toàn hệ thống và kiểm thử script PostgreSQL 18 trên Windows.

Kế hoạch lập ngày 16/11/2026, cập nhật ngày 17/11/2026 theo commit `d7c887d` vừa nhận từ `main` và xác nhận của bạn. Phần 3 chuẩn bị môi trường Windows đã hoàn thành. PostgreSQL 18.7 đã chạy trên cả Docker và bản cài trực tiếp; database `chatsystem_windows_test` dùng UTF8, tài khoản `postgres`.

Repository đã có `schema.sql` điều phối, bảng `public.users` dùng khóa UUID, cặp DDL/Test riêng cho từng thành viên và runner tạo database tạm. Hai file nghiệp vụ của Thành viên 3 đã triển khai `login_logs`, `app_open_events` và bảng ánh xạ khóa thống kê `app_open_accounts`. Ngày 18/11/2026, Docker runner riêng và tích hợp đạt 109 kiểm tra, gồm 43 ca truy vấn tham chiếu mục 5.1 và 8 ca index/lọc tên mục 5.2. Đã đo EXPLAIN trên hai kích thước dữ liệu, thêm index tài khoản/thời gian và dùng lại GIN tên; phép thử hai kết nối SQL đồng thời của mục 4.2 cũng đã đạt cho cả tài khoản mới và tài khoản đã có khóa thống kê. Phần xác thực/purge thực tế, adapter/index từ luồng 10/20 và ERD toàn hệ thống tiếp tục theo các mục còn lại.

## 0. Luồng thực hiện

```text
Đọc public.users và hợp đồng dữ liệu của nhóm
                    ↓
Vẽ ERD nháp, thiết kế hai bảng sự kiện
                    ↓
Viết DDL và test vào cặp file logs/analytics
                    ↓
Chạy --flow logs-analytics trên database tạm
                    ↓
Điều chỉnh SQL, test và ERD theo kết quả, chạy lại
                    ↓
Chạy --flow all, kiểm tra SQL trên PostgreSQL Windows
                    ↓
Đối chiếu ERD toàn hệ thống và bàn giao
```

ERD nháp có thể bắt đầu từ danh sách bảng trong kế hoạch Version 1. Khi chốt ERD toàn hệ thống, đối chiếu tên cột, kiểu khóa và hành vi xóa với SQL do cả ba thành viên đóng góp. Các mục dưới đây chia theo đầu ra để theo dõi tiến độ.

## 1. Phạm vi và đầu ra

| Phần việc | Kết quả cần bàn giao | Mã tính năng liên quan |
| --- | --- | --- |
| Nhật ký đăng nhập | Thiết kế và SQL tạo `login_logs`, dữ liệu mẫu, truy vấn kiểm tra | `A-05`, `A-07` |
| Lượt mở User Client | Thiết kế và SQL tạo `app_open_events`, ràng buộc chống ghi trùng | `A-15`, `A-16` |
| Index thống kê | Index phù hợp với truy vấn đăng ký, hoạt động và mạng lưới bạn bè | `A-12`–`A-16` |
| ERD | Sơ đồ toàn bộ CSDL khớp với schema đã tích hợp, có PK/FK và quan hệ | `X-01` |
| Kiểm thử Windows | Kết quả chạy từ database trống qua Docker và PostgreSQL cài trực tiếp | `X-01` |

Các bước này chuẩn bị cấu trúc dữ liệu và SQL cho Admin App. Phần giao diện và tích hợp Java với database được thực hiện ở giai đoạn phát triển tương ứng.

### Vị trí bàn giao dự kiến

Theo [workflow CSDL](docs/databases/workflow.md) và [script/README.md](script/README.md), Thành viên 3 phát triển trong một cặp DDL/Test. `schema.sql` điều phối thứ tự 00 → 10 → 20 → 30; `tests/run.sql` điều phối transaction và kiểm chứng chung.

| Vị trí | Nội dung do Thành viên 3 đóng góp |
| --- | --- |
| `script/sql/30-logs-and-analytics.sql` | Hai bảng sự kiện, ràng buộc và index thuộc luồng Logs/Analytics |
| `script/tests/test-logs-analytics.sql` | Fixture riêng, truy vấn kiểm chứng và case có kết quả mong đợi |
| `script/sql/00-core-users.sql` | Tham chiếu `public.users(id)` UUID và index chung đã có; phối hợp khi cần thay đổi |
| `script/tests/fixtures.sql`, `script/tests/assertions.sql` | Tái sử dụng user UUID cố định và helper kiểm chứng chung |
| `script/README.md` | Đối chiếu hướng dẫn runner, bổ sung cách nghiệm thu Windows khi cần |
| `docs/architecture/database.md` | Thiết kế bảng, quy tắc lưu dữ liệu, nguồn ERD và bảng kết quả tính tay |
| `docs/databases/erd/*.drawio.svg` | Mỗi nhóm một sơ đồ chỉnh sửa được và dùng làm ảnh báo cáo |
| Mục 8 của kế hoạch này | Ghi nhận kết quả kiểm thử Windows và phiên bản đã kiểm tra |

Schema và cặp file DDL/Test đã được nhóm chốt trong repository. Nguồn ERD và ảnh xuất vẫn dùng các vị trí dự kiến ở bảng trên. Fixture nghiệp vụ được đặt trong file test của mình và rollback cùng transaction do `tests/run.sql` quản lý.

Runner cho một luồng chỉ nạp core và DDL/Test của luồng đó. Dùng `pg_temp.assert_true` cho kết quả đúng, `pg_temp.expect_error` cho lỗi dự kiến và prefix nhãn `logs-analytics/` để tránh trùng. Transaction được quản lý tại entry point chung. Dữ liệu minh họa để giữ lại khi demo sẽ được thống nhất vị trí riêng nếu cần.

## 2. Thống nhất phần giao nhau với hai thành viên còn lại

- [x] Đã nhận và đọc `compose.yaml`, `.env.example` và hướng dẫn PostgreSQL từ `main` tại commit `8a599ec`. Service là `db`, cổng máy phát triển mặc định `55432`.
- [x] Đã đọc workflow mới tại commit `d7c887d`: mỗi lượt runner tạo database tạm riêng trên server Compose đang chạy, nạp SQL từ checkout và dọn database do lượt đó tạo.
- [x] Xác định hợp đồng `public.users(id)` dùng UUID tại `00-core-users.sql`, với `created_at`, `full_name`, `role`, `status`, `deleted_at` và index họ tên đã có.
- [ ] Với Thành viên 2, thống nhất biểu diễn `friendships`/`blocks` và phần mở rộng tài khoản, xác thực, purge dựa trên core hiện có.
- [ ] Với Thành viên 1, thống nhất siêu dữ liệu tin nhắn được server chấp nhận, người gửi, đích 1-1/nhóm, thời điểm gửi và ID dùng khi retry.
- [ ] Thống nhất quy tắc sau xóa mềm/purge để nhật ký và số liệu lịch sử được giữ lại.
- [ ] Với Thành viên 1, đối chiếu ERD E2EE theo DEC-05 và báo cáo spam theo DEC-10, gồm dữ liệu tin cậy thiết bị, revision backup, metadata archive và dấu chặn retry.
- [ ] Chốt tên cột, PK/FK và hợp đồng phụ thuộc chéo để luồng riêng và chế độ `all` đều chạy đúng. Core nằm ở file 00, Thành viên 2 ở 10, Thành viên 1 ở 20 và bạn ở 30.

Ghi các lựa chọn vào `docs/architecture/database.md` để các thành viên có cùng một bản thiết kế.

| Điểm cần chốt | Hướng xử lý dự kiến |
| --- | --- |
| Định danh sau purge | Giữ khóa thống kê ổn định cho mỗi tài khoản cũ, loại dữ liệu nhận diện theo chính sách purge. Tài khoản đăng ký lại có khóa mới |
| Liên kết với `users` | Chọn FK và hành vi xóa phù hợp với cách lưu định danh lịch sử. Giữ khả năng phân biệt từng tài khoản khi tính `DISTINCT` |
| Đăng ký mới sau purge | Phối hợp Thành viên 2 giữ thời điểm tạo và định danh thống kê để bảng `A-12` và biểu đồ `A-13` vẫn khớp |
| Hoạt động chat sau xóa/thu hồi | Phối hợp Thành viên 1 giữ siêu dữ liệu lần gửi thành công, gồm người gửi, đích và thời gian |
| Phạm vi nhật ký | Ghi rõ phạm vi User/Admin cho sự kiện đăng nhập và thời điểm được coi là đăng nhập thành công |
| Khóa cho một lần khởi chạy | Mỗi lần chạy User Client có một mã riêng. Cùng tài khoản và cùng mã này chỉ tạo một lượt mở app |
| Tin cậy thiết bị và backup | Thành viên 1 chủ trì SQL E2EE. Bạn đối chiếu khóa tham chiếu thiết bị cùng tài khoản, metadata tin cậy, revision và mốc `replaced_at` trên ERD |
| Vòng đời báo cáo spam | Thành viên 1 chủ trì SQL báo cáo. Bạn đối chiếu trạng thái xử lý, quan hệ evidence, archive ẩn danh và dấu chặn retry trên ERD, phối hợp kiểm thử SQL tích hợp |

Core đã có username 6–30 ký tự ASCII chữ/số/`_`/`-`, tính duy nhất username/email theo chữ thường và ba trạng thái `ACTIVE`/`LOCKED`/`DELETED`. Các use-case vừa được đồng bộ quy tắc username và giá trị giới tính `Nam/Nữ/Khác`. Core hiện chứa hồ sơ cơ bản; các trường hồ sơ mở rộng và auth tiếp tục được phối hợp với Thành viên 2.

Công thức thống kê và chi tiết lưu lịch sử sau purge còn phần cần hoàn thiện thiết kế. Dùng quy tắc trong use-case làm cơ sở triển khai, ghi rõ lựa chọn và kiểm chứng việc giữ số liệu. Chính sách tin cậy thiết bị, thời hạn backup đã thay và vòng đời báo cáo spam đã được chốt theo DEC-05/DEC-10. [Thống kê và thời gian](docs/decisions/statistics-and-time.md), [vòng đời tài khoản](docs/decisions/account-lifecycle.md), [E2EE](docs/decisions/group-permissions-and-e2ee.md), [báo cáo spam](docs/decisions/spam-moderation-and-retention.md)

## 3. Chuẩn bị môi trường Windows

**Đã hoàn thành theo xác nhận của bạn ngày 17/11/2026.** Việc chạy test SQL mới được theo dõi tại mục 7–8.

- [x] Kiểm tra Docker Desktop và Docker Compose hoạt động bằng `docker version` và `docker compose version`. Theo output bạn cung cấp ngày 17/11/2026, Docker Compose `v5.7.0`, Docker Client/Engine `29.9.3`, Docker Desktop `4.99.0`, context `desktop-linux`; Client đã kết nối được tới Server.
- [x] Đã có `.env` cục bộ trong thư mục gốc dự án.
- [x] Điền tên database, tài khoản và mật khẩu của môi trường kiểm thử. `.gitignore` hiện đã bỏ qua `.env` và cho phép `.env.example`.
- [x] Kiểm tra cấu hình bằng `docker compose config --quiet`, dựng PostgreSQL bằng `docker compose up -d --wait db`, rồi xem `docker compose ps` và `docker compose logs --tail=50 db`. Output bạn cung cấp xác nhận PostgreSQL 18.7 đã khởi tạo, container `chatsystem-db-1` healthy, cổng `127.0.0.1:55432` chuyển tới `5432` trong container.
- [x] Mở `psql` bằng `docker compose exec db psql -U chatsystem -d chatsystem`. Điều chỉnh `-U` và `-d` theo `.env` nếu đã đổi giá trị mẫu.
- [x] Chuẩn bị PostgreSQL 18 cài trực tiếp trên Windows và công cụ `psql` cho lượt kiểm tra độc lập với Docker. Output bạn cung cấp xác nhận server PostgreSQL 18.7 trên `x86_64-windows`, database `chatsystem_windows_test`, user `postgres` và `server_encoding = UTF8`.
- [x] Dùng cổng riêng cho hai môi trường. Docker kết nối từ Windows qua `127.0.0.1:55432`; PostgreSQL cài trực tiếp kết nối qua `127.0.0.1:5432` theo lệnh bạn đã chạy.
- [x] Lưu SQL ở UTF-8 để kiểm tra dữ liệu họ tên tiếng Việt, theo xác nhận hoàn thành phần 3 của bạn.

### Cấu hình đã có trong repository

| Thành phần | Giá trị hiện tại |
| --- | --- |
| Image và service | `postgres:18`, service `db` |
| Kết nối từ công cụ SQL trên Windows | Host `127.0.0.1`, port `55432` theo `POSTGRES_PORT` |
| Database và tài khoản mặc định | `chatsystem`, cấu hình qua `POSTGRES_DB` và `POSTGRES_USER` |
| Mật khẩu | Thay placeholder `POSTGRES_PASSWORD` trong `.env` bằng mật khẩu cục bộ |
| Cổng PostgreSQL trong container | `5432` |
| Volume dữ liệu | `postgres-data` gắn vào `/var/lib/postgresql` |
| Script khởi tạo | `./script` gắn chỉ đọc vào `/docker-entrypoint-initdb.d` |
| Kiểm tra sẵn sàng | Healthcheck dùng `pg_isready`, lệnh `up --wait db` chờ service sẵn sàng |

Mỗi thành viên dùng database local trên máy mình. Compose hiện công bố cổng ở địa chỉ loopback `127.0.0.1`. `docker compose down` giữ volume dữ liệu để dùng lại. Khi volume đã được khởi tạo, việc đổi các biến khởi tạo trong `.env` cần đi kèm thao tác cập nhật database tương ứng hoặc một volume kiểm thử mới.

Có thể bắt đầu ERD nháp, thiết kế hai bảng và chuẩn bị dữ liệu tính tay trong lúc các thành viên khác hoàn thiện bảng nền. Hướng dẫn chạy đã có tại [README](README.md#postgresql-cho-nhóm-phát-triển) và [script/README.md](script/README.md).

## 4. Thiết kế hai bảng sự kiện

Đối chiếu ngày 18/11/2026: core đã định nghĩa `public.users(id UUID)`. Mục 4.1 và 4.2 đã có DDL và test nghiệp vụ trên nền `ab918e4`. DDL nằm trong [file 30](script/sql/30-logs-and-analytics.sql) và ca kiểm chứng trong [test Logs/Analytics](script/tests/test-logs-analytics.sql). Luồng riêng chỉ nạp core và file 30, vì vậy thiết kế hai bảng chạy được trước khi có bảng phiên của Thành viên 2.

### 4.1. Bảng `login_logs`

Bảng này lưu các lần đăng nhập thành công để Admin xem lịch sử của một người hoặc toàn hệ thống.

- [x] Ghi ID sự kiện, FK kiểu UUID tới `public.users(id)`, thời điểm do server ghi bằng `TIMESTAMPTZ` và phạm vi phiên thực tế được cấp. Một tài khoản có `users.role = 'ADMIN'` vẫn có thể đăng nhập User Client. Dùng phạm vi phiên để phân biệt hai ứng dụng.
- [x] Lấy tên hiển thị từ `users.username` và `users.full_name`; kiểm chứng `ON DELETE SET NULL` giữ log dưới nhãn người đã xóa sau purge và tài khoản mới cùng username chỉ có lịch sử theo UUID mới. Hợp đồng FK được ghi trong tài liệu thiết kế để Thành viên 2 đối chiếu khi tích hợp.
- [x] Giới hạn dữ liệu log ở metadata cần cho tra cứu. Dữ liệu xác thực bí mật thuộc cơ chế xác thực riêng.
- [x] Kiểm tra thứ tự bằng thời gian và ID làm khóa phụ khi hai bản ghi cùng thời điểm.

**Kết quả mục 4.1 ngày 18/11/2026:** `login_logs` có bốn cột `id`, `user_id`, `login_at`, `session_scope`; phạm vi phiên là `USER_CLIENT`/`ADMIN_APP`, thời gian mặc định `clock_timestamp()`. Hai index phục vụ nhật ký toàn hệ thống và lịch sử theo tài khoản. Có 20 kiểm tra riêng, cùng 9 kiểm tra core: tại thời điểm hoàn thành mục 4.1, cả `py -3 script/test-db.py --flow logs-analytics` và `--flow all` đạt 29/29, xác nhận rollback sạch và dọn database tạm. Thiết kế, truy vấn và dữ liệu tính tay tại [database.md](docs/architecture/database.md). Chưa có xác nhận phối hợp từ Thành viên 2; quy trình cấp phiên/purge thực tế và phân quyền ghi/đọc nhật ký cần nghiệm thu khi file 10 được triển khai. Các phần thống kê đầy đủ vẫn chưa hoàn thành.

Tham chiếu [lịch sử đăng nhập cá nhân](docs/requirements/use-cases/admin/user-management/UC-ADM-USER-MGMT-10-view-user-login-history.md) và [nhật ký hệ thống](docs/requirements/use-cases/admin/login-logs/UC-ADM-LOG-01-view-system-login-log.md).

### 4.2. Bảng `app_open_events`

Bảng này lưu một lượt cho mỗi tài khoản xác thực thành công trong một lần khởi chạy User Client.

- [x] Thiết kế các trường tối thiểu gồm ID sự kiện, định danh tài khoản/khóa lịch sử, mã lần khởi chạy và thời điểm mở app được ghi nhận.
- [x] Khi ghi nhận lượt mở, cả định danh tài khoản và mã lần khởi chạy phải có giá trị. Đặt ràng buộc duy nhất cho cặp này. Dùng thời điểm xác thực do server ghi bằng `TIMESTAMPTZ` để xếp sự kiện vào ngày/tháng theo `Asia/Ho_Chi_Minh`.
- [x] Kiểm tra việc ghi lại cùng sự kiện do retry/reconnect giữ nguyên một bản ghi, kể cả hai yêu cầu ghi đồng thời.
- [x] Một lần chạy mới với cùng tài khoản tạo lượt mới. Hai tài khoản lần lượt xác thực trong cùng lần chạy được tính theo từng tài khoản.
- [x] Dùng user mẫu `admin04` làm hai ca đối chiếu: một lần khởi chạy và đăng nhập User Client thành công tạo 1 lượt mở, đăng nhập Admin App tạo 0 lượt mở. Đăng nhập lại thành công trong cùng lần chạy User Client thêm một dòng nhật ký và giữ số lượt mở ở 1.
- [ ] Xác nhận với Thành viên 2 hợp đồng purge đã triển khai: `app_open_accounts` giữ khóa thống kê ngẫu nhiên, FK `user_id` đặt NULL khi xóa tài khoản; `app_open_events` giữ `account_key` NOT NULL và đếm người theo khóa này. Ca purge hai tài khoản trong cùng tháng đã đạt kết quả vẫn là 2; ERD cục bộ và phương án đã ghi tại [database.md](docs/architecture/database.md). Chưa có xác nhận phối hợp hoặc tác vụ purge tích hợp. [Quy tắc thống kê](docs/decisions/statistics-and-time.md)

**Kết quả SQL mục 4.2 ngày 18/11/2026:** bảng `app_open_events` có `id`, `account_key`, `launch_id`, `opened_at`; UNIQUE trên `(account_key, launch_id)`, index `(opened_at, account_key)`. Hàm `record_app_open` chỉ ghi lượt cho `USER_CLIENT`, trả lại cùng ID/thời điểm khi retry và không phụ thuộc bảng phiên. Bổ sung 29 kiểm tra SQL: hai flow `logs-analytics`/`all` đạt **58/58** (9 core + 20 login logs + 29 app opens), rollback sạch và dọn database tạm. Lệnh `py -3 script/test-app-open-concurrency.py` đạt cả hai ca tranh chấp khóa thực tế, xác nhận một sự kiện cho mỗi lần chạy và giữ nguyên ID/thời điểm; database riêng đã được dọn. Trạng thái xác thực/quyền cấp phiên thực tế và hợp đồng purge vẫn chờ tích hợp với Thành viên 2; thống kê và ERD toàn hệ thống tiếp tục ở bước 5–6.

**Tiêu chí hoàn thành bước 4** — Luồng `logs-analytics` tạo được hai bảng trên core và chạy các ca nghiệp vụ ở trên với fixture chung. Đối chiếu hành vi purge qua `--flow all` khi phần xử lý tài khoản được tích hợp.

## 5. Chuẩn bị truy vấn, dữ liệu mẫu và index

### 5.1. Xác định đúng kết quả trước khi tối ưu

- [x] Nhật ký `A-05`/`A-07` trả đúng tài khoản và sắp xếp ổn định theo thời gian.
- [x] Đăng ký mới `A-12`/`A-13` tính tài khoản tạo thành công, gồm cả Admin tạo. Yêu cầu đăng ký tạm được xử lý riêng.
- [x] Biểu đồ đăng ký và hoạt động trả đủ 12 tháng, các tháng trống bằng 0.
- [x] Hoạt động `A-15` lấy hợp các tài khoản có mở app hoặc có tin gửi được server chấp nhận trong khoảng.
- [x] Ba chỉ số của `A-15` gồm số lượt mở app, số người nhận 1-1 khác nhau và số nhóm nhận khác nhau. Tổng hợp mỗi nguồn trước khi ghép để tránh nhân số dòng.
- [x] Biểu đồ `A-16` đếm tài khoản duy nhất có mở app trong từng tháng.
- [x] Mạng lưới `A-14` hợp nhất bạn trực tiếp và bạn qua hai bước, loại bản thân, trùng lặp, tài khoản đã xóa và quan hệ chặn hai chiều với người gốc. Tài khoản khóa còn tình bạn hợp lệ vẫn được tính.
- [x] `A-14` giữ người có 0 bạn trong danh sách. Bộ lọc `=`, `<`, `>` áp dụng lên số bạn trực tiếp.
- [x] Các bảng thống kê hỗ trợ lọc họ tên theo chuỗi con không phân biệt hoa thường sau trim, sắp xếp theo họ tên/ngày tạo và dùng định danh làm khóa phụ.
- [x] Quy đổi khoảng ngày theo `Asia/Ho_Chi_Minh`, dùng khoảng từ đầu ngày bắt đầu đến trước đầu ngày kế tiếp của ngày kết thúc.
- [x] Tính thống kê chat bằng metadata của sự kiện gửi thành công.

**Phạm vi đánh dấu mục 5.1:** đã xác định và kiểm chứng công thức/kết quả trên bộ truy vấn tham chiếu chạy được tại [analytics-reference.sql](script/queries/analytics-reference.sql), gồm TEMP view và các đầu vào tạm được tạo trong test của luồng. Nguồn lượt mở/log đọc từ bảng thật trên core; nguồn đăng ký được giữ sau purge, metadata gửi tin và quan hệ bạn bè/chặn dùng hợp đồng đầu vào mẫu vì file 10/20 chưa có schema nghiệp vụ. Chưa đánh dấu adapter dữ liệu thật, retention đăng ký hoặc quy trình auth/purge tích hợp hoàn tất. Không tạo bản sao `users` hoặc bảng nghiệp vụ của thành viên khác.

**Kết quả ngày 18/11/2026 khi hoàn thành 5.1:** thêm 43 kiểm tra, tổng **101/101** cho cả `logs-analytics` (7,34 giây) và `all` (7,63 giây), rollback sạch và đã dọn database tạm. Bộ mẫu 2030: đăng ký tháng 10/11 = 3/9; người mở app tháng 10/11/12 = 1/5/1; tháng 11 có 7 hàng hoạt động với các bộ chỉ số tính tay. Mạng A có 2 bạn trực tiếp và 4 bạn mở rộng, F/L có 0; bao phủ khóa/xóa, chặn hai chiều, cạnh trùng, bậc 3 và các toán tử lọc. Có kiểm tra tên tiếng Việt, khóa phụ khi trùng tên/ngày, biên ngày/tháng/năm và bảo toàn kết quả trên prototype nguồn đăng ký sau purge. Chi tiết nguồn đầu vào, truy vấn và bảng số liệu tại [database.md](docs/architecture/database.md#kết-quả-tham-chiếu-trước-tối-ưu--mục-51). Kết quả index/EXPLAIN được bổ sung ở mục 5.2 bên dưới.

Tham chiếu [đăng ký mới](docs/requirements/use-cases/admin/analytics/UC-ADM-ANL-01-browse-new-registrations.md), [biểu đồ đăng ký](docs/requirements/use-cases/admin/analytics/UC-ADM-ANL-02-view-monthly-registrations-chart.md), [hoạt động](docs/requirements/use-cases/admin/analytics/UC-ADM-ANL-03-browse-user-activity.md), [biểu đồ hoạt động](docs/requirements/use-cases/admin/analytics/UC-ADM-ANL-04-view-monthly-active-users-chart.md) và [mạng lưới bạn bè](docs/requirements/use-cases/admin/analytics/UC-ADM-ANL-05-view-friend-network-scale.md).

### 5.2. Index dự kiến

Đây là danh sách ứng viên để kiểm tra trên schema và truy vấn thực tế. PK/UNIQUE đã tạo index thì tái sử dụng index đó.

Core đã có `users_name_trgm_idx` trên `lower(full_name)` và hai unique index cho username/email. Index trên bảng do luồng 10/20 tạo cần phối hợp với người sở hữu để đặt cùng DDL tạo bảng đó và được kiểm tra tích hợp. Luồng `logs-analytics` chạy riêng chỉ có core và file 30, nên cần xác định phụ thuộc trước khi đưa truy vấn hoặc DDL tham chiếu `messages`, `friendships`, `blocks` vào luồng này.

| Truy vấn | Cột hoặc tổ hợp cột cần xem xét |
| --- | --- |
| Nhật ký toàn hệ thống | `login_logs(login_at DESC, id DESC)` |
| Lịch sử một tài khoản | `login_logs(user_id, login_at DESC, id DESC)`, điều chỉnh theo khóa lịch sử đã chọn |
| Hoạt động theo khoảng thời gian/tháng | `app_open_events(opened_at, account_key)` |
| Lượt mở của một tài khoản | Đã thêm `app_open_events(account_key, opened_at)` sau đo EXPLAIN; UNIQUE theo launch không lọc trực tiếp được thời gian |
| Đăng ký theo khoảng ngày | Ngày tạo và định danh trên nguồn dữ liệu đăng ký được giữ sau purge |
| Gửi tin theo người và thời gian | Người gửi, thời điểm server chấp nhận trên nguồn metadata tin nhắn |
| Tra cứu bạn bè từ hai phía | Index bắt đầu từ mỗi đầu của quan hệ bạn bè, theo biểu diễn nhóm đã chọn |
| Kiểm tra chặn hai chiều | Cặp người chặn/người bị chặn và chiều tra cứu cần thiết |
| Lọc họ tên | Phối hợp index tìm kiếm dùng chung với Thành viên 2, khớp biểu thức truy vấn thực tế |

- [x] Kiểm tra index hiện có trước khi thêm để tránh trùng lặp.
- [x] Dùng `EXPLAIN (ANALYZE, BUFFERS)` với các truy vấn đọc để xem cách PostgreSQL truy cập dữ liệu.
- [x] Ghi lý do chọn index. Với bảng mẫu nhỏ, đánh giá cả tính đúng và điều kiện lọc vì PostgreSQL có thể chọn quét toàn bảng.
- [ ] Đối chiếu index trên bảng do Thành viên 1/2 phụ trách trước khi tích hợp vào schema chung.

**Kết quả mục 5.2 ngày 18/11/2026:** đã kiểm kê PK/UNIQUE và index của core/file 30; giữ các index log và index lượt mở bắt đầu bằng thời gian, thêm `app_open_events_account_key_opened_at_idx` trong file 30. Truy vấn đếm lượt một tài khoản/tháng giảm từ 23 xuống 4 shared buffer accesses trong fixture 400.000 lượt mở. Chuyển bộ lọc tên tham chiếu sang LIKE escape ký tự thường, tách nhánh hồ sơ còn tồn tại/nhãn purge để dùng lại `users_name_trgm_idx`; A-12 lọc tên chọn lọc giảm từ 918 xuống 33 buffer accesses trong phép đo. Thời gian chỉ là số đo warm trên máy hiện tại, không phải SLA hoặc cam kết cho mọi truy vấn.

Đã thêm [script benchmark](script/explain-logs-analytics.py) và [fixture riêng](script/tests/fixtures-logs-analytics-indexes.sql). Script tạo DB tạm mới, chạy VACUUM ANALYZE và 8 EXPLAIN JSON với planner mặc định; không nạp fixture này vào runner test thường. Mẫu lớn có 20.000 users/ánh xạ và 400.000 dòng mỗi bảng sự kiện; mẫu nhỏ có 50/100. [Bằng chứng trước](docs/architecture/benchmarks/logs-analytics-indexes-before.json), [sau](docs/architecture/benchmarks/logs-analytics-indexes-after.json) và [mẫu nhỏ](docs/architecture/benchmarks/logs-analytics-indexes-small.json) giữ SQL, cây plan, buffer/thời gian và xác nhận dọn DB. Mẫu nhỏ có Seq Scan hợp lý; test không ép node planner hoặc ngưỡng thời gian. [Thiết kế database](docs/architecture/database.md#đánh-giá-index--mục-52) ghi lý do chọn/giữ index, chi phí lưu trữ và danh sách ứng viên của teammate.

Bổ sung 8 kiểm tra index/ngữ nghĩa tìm tên, cả `logs-analytics` và `all` hiện đạt **109/109**, rollback sạch và đã dọn DB. `script/README.md` giữ nguyên. Mục đối chiếu teammate vẫn mở: file 10/20 chưa có bảng bạn bè/chặn/metadata gửi tin và nguồn đăng ký được giữ sau purge, nên chưa tạo hoặc tuyên bố nghiệm thu các index đó.

### 5.3. Bộ dữ liệu có kết quả tính tay

**Hoàn thành ngày 19/11/2026.** Bộ mẫu dùng UUID, thời điểm và hồ sơ giả cố định, tái sử dụng các tài khoản chung và helper kiểm chứng. Bảng tính tay ghi rõ tập đăng ký, 12 tháng của hai biểu đồ, ba chỉ số cho từng tài khoản hoạt động, hai tầng bạn bè và kết quả từng bộ lọc. [SQL và fixture](script/tests/test-logs-analytics.sql), [bảng kết quả tính tay](docs/architecture/database.md#bộ-dữ-liệu-tính-tay-mục-53)

Checkout hiện tại đã có schema 10/20. `--flow all` dùng bảng phiên, đăng ký tạm, tin nhắn, thành viên hội thoại, bạn bè và chặn để chạy các ca tích hợp. `--flow logs-analytics` chạy riêng trên core và file 30 với cùng kỳ vọng tính tay. Dữ kiện đăng ký và gửi tin sau purge được mô phỏng bằng bảng tạm. Tác vụ purge của backend và nơi lưu lâu dài hai loại dữ kiện này vẫn chờ triển khai.

| Ca kiểm tra | Kết quả mong đợi |
| --- | --- |
| Cùng tài khoản đăng nhập lại trong cùng lần chạy | Log phản ánh các lần đăng nhập thành công, lượt mở app vẫn là 1 |
| Ghi trùng một sự kiện mở app, kể cả hai phiên SQL đồng thời | Chỉ có một bản ghi cho tài khoản và lần chạy đó |
| Cùng tài khoản mở app ở hai lần chạy trong một tháng | 2 lượt mở, 1 người duy nhất trên biểu đồ tháng |
| Chỉ có mở Admin App | Chỉ số mở User Client giữ nguyên |
| Năm có nhiều tháng trống | Biểu đồ vẫn có 12 dòng và điền 0 |
| Mở app 31/10, gửi tin 01/11, chưa mở lại | Có trong bảng hoạt động tháng 11 với 0 lượt mở, chưa tăng người mở app tháng 11 |
| Gửi nhiều tin tới cùng người/nhóm, gửi lại cùng ID | Mỗi đích chỉ tính một lần trong khoảng, retry giữ nguyên sự kiện |
| Chỉ nhận tin, không mở app hay gửi tin trong khoảng | Chưa thuộc tập hoạt động của khoảng đó |
| Xóa phía mình hoặc thu hồi tin đã gửi thành công | Số liệu gửi trước đó giữ nguyên |
| Tự đăng ký thành công, Admin tạo user, yêu cầu đăng ký tạm | Hai tài khoản tạo thành công được tính, yêu cầu tạm chưa tạo lượt đăng ký |
| Xóa mềm rồi purge tài khoản, tạo tài khoản mới cùng username | Số liệu lịch sử còn đủ, log cũ được ẩn danh, tài khoản mới có lịch sử riêng |
| Biên ngày/tháng/năm theo giờ Việt Nam | Sự kiện ngay đầu kỳ được tính, sự kiện đúng đầu kỳ kế tiếp thuộc kỳ sau |
| Bạn bè có nhiều đường đi tới cùng một người | Tổng bạn mở rộng đếm người đó một lần |
| Bạn bè có tài khoản khóa, đã xóa, chặn từ mỗi chiều | Kết quả đúng quy tắc ở mục 5.1, gồm người có 0 bạn |
| Bộ lọc số lượng `=`, `<`, `>` | Mỗi toán tử trả đúng tập tính tay, gồm ngưỡng 0 |

**Kết quả kiểm tra mục 5.3 trên PostgreSQL 18.6 native (Linux)** — `logs-analytics` đạt **110 kiểm tra SQL**, `all` đạt **347 kiểm tra SQL**. Mỗi luồng đạt thêm **2 ca ghi đồng thời**, xác nhận hai kết nối trả cùng ID/thời điểm và chỉ lưu một dòng cho cùng tài khoản/lần chạy. Fixture SQL được rollback sạch. Ca ghi đồng thời dùng commit trong DB tạm, sau đó runner dọn cụm tạm.

## 6. Vẽ ERD toàn hệ thống

- [x] Rà lại các ERD theo luồng của Thành viên 1/2 và đối chiếu từng sơ đồ với DDL hiện tại.
- [x] Lấy schema tích hợp của cả ba thành viên làm nguồn đối chiếu.
- [x] Vẽ đủ các nhóm tài khoản/xác thực, bạn bè/chặn, hội thoại/tin nhắn, E2EE, spam và hai bảng sự kiện cùng `app_open_accounts`.
- [x] Thể hiện các cấu trúc lưu lịch sử đã có, gồm khóa thống kê lượt mở, archive và tombstone. Ghi rõ nguồn đăng ký và gửi tin sau purge còn dùng fixture tạm.
- [x] Thể hiện tên bảng, PK, FK, bội số, kiểu dữ liệu và cột được phép rỗng.
- [x] Giải thích các ràng buộc quan trọng, hành vi xóa và vai trò của hai bảng sự kiện.
- [x] Đối chiếu DEC-05/DEC-10 với DDL, ERD và ca kiểm chứng của Thành viên 1. Ghi rõ phần thực thi còn thuộc backend.
- [x] Dẫn sáu sơ đồ `.drawio.svg` từ tài liệu thiết kế. Mỗi tệp chứa nguồn Draw.io và ảnh của một nhóm.
- [x] Đối chiếu lại tệp bàn giao với catalog PostgreSQL sau khi nạp schema tích hợp từ đầu.

**Kết quả ngày 19/11/2026.** Sáu sơ đồ theo nhóm khớp **23 bảng, 151 cột, 23 PK và 33 FK** trên PostgreSQL 18.6. Draw.io Integration được dùng để xuất sơ đồ nhật ký và lượt mở theo định dạng SVG có nhúng nguồn. Lượt kiểm thử native từ database trống đạt **347 kiểm tra SQL**, gồm **146 ca lưu trữ của Thành viên 1**, và rollback sạch. [Sơ đồ nhật ký và lượt mở](docs/databases/erd/logs-and-analytics.drawio.svg), [danh mục sơ đồ và thiết kế](docs/architecture/database.md#erd-theo-nhóm-nghiệp-vụ-mục-6)

### Các chính sách mới cần đối chiếu trên ERD

| Nhóm dữ liệu | Nội dung thiết kế cần thể hiện |
| --- | --- |
| `e2ee_devices` | DEC-05 đề xuất `trusted_at`, `trust_method` (`INITIAL`, `TRUSTED_DEVICE`, `RECOVERY`), `approved_by_device_id`, `trust_proof`. Thiết bị phê duyệt thuộc cùng tài khoản; thiết bị chờ xác nhận có trạng thái tương ứng. Bằng chứng lưu theo giao thức được nhóm chọn |
| `e2ee_key_backups` | Revision hiện hành và backup đã thay, mốc `replaced_at`, thời hạn 30 ngày từ lúc thay. Purge tài khoản có hiệu lực sớm hơn. Việc thay revision và retry cần nhất quán |
| Epoch và envelope | Giữ dữ liệu cần cho lịch sử được phép đọc, kiểm tra quan hệ và hành vi xóa khi thu hồi thiết bị hoặc thay backup |
| `spam_reports` | `PENDING`/`REVIEWED`, thời điểm tiếp nhận, thông tin Admin xử lý và ghi chú. Chuyển trạng thái và ghi thông tin xử lý cùng thành công; thao tác khóa tài khoản được quản lý riêng |
| `spam_report_evidences` | Mỗi tin nguồn một dòng trong báo cáo, giữ cả nhãn Selected/Context khi giao nhau. Selected 1–20, Context tối đa 30, Evidence có thể tới 50 tin. Toàn gói tối đa 1.048.576 byte UTF-8 trước nén theo biểu diễn DEC-10 |
| Archive và dấu chặn retry | DEC-10 đề xuất `spam_report_archives` và `spam_report_request_tombstones`. Archive dùng ID mới, ngày và số lượng/trạng thái đã ẩn danh. Tombstone giữ digest ID yêu cầu theo DEC-10. Hai cấu trúc lưu độc lập, mỗi phần đúng tập dữ liệu được chính sách cho phép |
| Hết hạn và purge báo cáo | Sau 180 × 24 giờ từ `received_at`, hoặc sớm hơn khi purge bất kỳ tài khoản liên quan, chuyển toàn bộ báo cáo sang metadata ẩn danh, ghi dấu chặn retry và xóa dữ liệu gốc trong cùng giao dịch |

Các tên bảng/cột bổ sung trong quyết định là đề xuất để nhóm hoàn thiện schema. Thành viên 1 chủ trì triển khai SQL E2EE/spam theo phân công. Bạn tổng hợp ERD và phối hợp kiểm chứng ràng buộc, giao dịch và hành vi xóa ở mức SQL. Các phần xác minh bằng chứng mật mã, canonical JSON và kiểm tra API được ghi vào tài liệu thiết kế để thực hiện ở bước backend. [DEC-05](docs/decisions/group-permissions-and-e2ee.md), [DEC-10](docs/decisions/spam-moderation-and-retention.md)

## 7. Kiểm thử từ đầu trên Windows

Máy đang sử dụng là Linux. Đã chạy đối chiếu từ database trống trên PostgreSQL 18.6 qua Unix socket và TCP loopback, đồng thời kiểm tra nhánh Docker Desktop trên Linux với PostgreSQL 18.7. Hai lượt Windows vẫn chờ máy hoặc VM Windows. Kết quả thực tế được ghi ở mục 8.

### 7.1. Vòng kiểm thử qua Docker runner trên Windows

- [ ] Mở Docker Desktop và xác nhận server hoạt động. Giữ service `db` chạy bằng `docker compose up -d --wait db`.
- [ ] Chạy `py -3 script/test-db.py --flow logs-analytics` trước khi sửa để xác nhận khung hoạt động. Python 3 đã khởi chạy được runner trong phiên kiểm tra; kết nối Docker đang chờ xác minh lại.
- [ ] Thêm DDL, fixture và case vào cặp file của bạn rồi chạy lại cùng lệnh.
- [ ] Kiểm tra tên bảng, PK/FK, UNIQUE, CHECK, index và các kết quả tính tay ở mục 5 bằng helper chung.
- [ ] Kiểm tra các lỗi FK/UNIQUE/NOT NULL dự kiến bằng `pg_temp.expect_error` để lượt test tiếp tục trong transaction chung.
- [ ] Chạy `py -3 script/test-db.py --flow all` khi nhận đủ SQL liên quan và trước tích hợp/merge.
- [ ] Ghi commit, flow, kết quả và thời gian thực tế vào mục 8. Kiểm tra thông báo rollback và dọn database tạm sau lượt.

```powershell
# Chạy từ thư mục gốc repo, sau khi Docker Desktop đã hoạt động
docker compose up -d --wait db
py -3 script/test-db.py --flow logs-analytics

# Kiểm tra tích hợp các luồng
py -3 script/test-db.py --flow all
```

Mỗi lượt runner chụp bộ SQL từ checkout, tạo database `chatsystem_schema_test_<flow>_<suffix>` trên server đang chạy, nạp schema và test, rollback fixture rồi dọn database tạm. Khi dùng project Compose khác, truyền `--project <tên>` cho runner. Vòng làm việc này tái sử dụng server và volume phát triển đang có.

`schema.sql` là entry point gồm bảy dòng điều phối và transaction 00 → 10 → 20 → 30. Docker tự nạp file này khi volume khởi tạo lần đầu. Volume của bạn đã có từ trước khi nhận schema; runner vẫn nạp schema mới vào database tạm cho mỗi lượt test.

File 10, 20 và 30 đều có DDL nghiệp vụ. Truy vấn tham chiếu ở ngay trong `tests/test-logs-analytics.sql`. Khi chạy `all`, test đọc dữ liệu mẫu từ các bảng thật của file 10/20 và sử dụng dữ kiện tạm cho lịch sử đăng ký/gửi tin sau purge.

Trên Docker Desktop Linux, service `db` khởi tạo volume trống thành công với 23 bảng, UTF-8, `pg_trgm` và `unaccent`. Cả `logs-analytics` và `all` đạt kết quả như trên PostgreSQL native Linux. Catalog và container xác nhận không còn database kiểm thử hay bản SQL tạm. Sau lượt chạy, project, mạng và volume dùng riêng cho test đã được dọn.

### 7.2. Nghiệm thu thủ công trên PostgreSQL cài trực tiếp trên Windows

`script/test-native.py` dùng cụm tạm và Unix socket trên Linux/macOS. Chế độ `--local-server` của `script/test-db.py` dành cho server PostgreSQL 18 cài trực tiếp và gọi các công cụ `.exe` trên Windows. Runner tạo database tạm trên máy hiện tại, rồi tự dọn khi chạy xong hoặc gặp lỗi.

- [ ] Tạo một database mới trên server Windows cổng 5432. Dùng tên `chatsystem_schema_test` hoặc dạng suffix mà `tests/run.sql` cho phép. Database `chatsystem_windows_test` đã tạo dùng cho kiểm tra kết nối; entry point test có điều kiện kiểm tra tên database riêng.
- [ ] Nạp `script/schema.sql` bằng `psql.exe -X -v ON_ERROR_STOP=1 -f script/schema.sql` với host, port, user và database được chỉ định rõ.
- [ ] Chạy `script/tests/run.sql` trên cùng database, cùng tùy chọn dừng khi lỗi. Entry point mặc định dùng `flow=all`, nạp helper/fixture/test và rollback.
- [ ] Kiểm tra extension `pg_trgm`/`unaccent`, ràng buộc và số liệu nghiệp vụ, đối chiếu kết quả tích hợp với lượt Docker.
- [ ] Kiểm tra tiếng Việt, đường dẫn có khoảng trắng và mốc thời gian gần nửa đêm.
- [ ] Ghi lệnh thực tế, phiên bản server/client, mã thoát và kết quả vào mục 8. Lượt nghiệm thu tiếp theo dùng một database trống mới.

Chạy từ thư mục gốc repo. Runner dùng host `127.0.0.1`, cổng `5432` và tài khoản `postgres` mặc định. Nếu cần mật khẩu, cấu hình file mật khẩu libpq cho tài khoản thử. [Hướng dẫn kết nối và ghi kết quả](script/README.md#nghiệm-thu-trên-windows)

```powershell
$pgBin = 'C:\Program Files\PostgreSQL\18\bin'
py -3 script/test-db.py --local-server --pg-bin $pgBin --flow all
```

Runner gọi `createdb.exe` với `template0` và UTF-8, sau đó gọi `psql.exe -X -v ON_ERROR_STOP=1` để nạp `script/schema.sql` trước `script/tests/run.sql`. Lượt Linux qua TCP đã xác nhận UTF-8, hai extension, đường dẫn có khoảng trắng, SQL lưu dạng CRLF và việc dọn database khi cố ý gây lỗi ở bản sao test.

**Tiêu chí hoàn thành bước 7.** Cả Docker Desktop và PostgreSQL cài trực tiếp trên Windows nạp thành công cùng bộ SQL từ database trống, đạt kết quả tương ứng và dọn database tạm.

### 7.3. Phối hợp kiểm chứng SQL theo DEC-05/DEC-10

Các ca lưu trữ gốc của Thành viên 1 và ca đồng thời trong runner đã chạy trên Linux. Lượt Windows chờ chạy lại toàn bộ `--flow all`. Các quy tắc cần service thật được ghi ở [tài liệu thiết kế](docs/architecture/database.md#kiểm-chứng-dec-05-và-dec-10-mục-73).

- [x] Trên Linux, kiểm tra trạng thái tin cậy thiết bị và FK phê duyệt cùng tài khoản. Ca thay revision và purge thiết bị xác nhận giữ được epoch hoặc envelope thuộc lịch sử còn hợp lệ.
- [x] Trên Linux, kiểm tra tính duy nhất của revision, tối đa một bản hiện hành và thứ tự thời điểm `replaced_at`.
- [x] Trên Linux, kiểm tra khóa evidence, hai nhãn Selected/Context và các giới hạn số đếm của archive.
- [x] Trên Linux, kiểm tra chuyển `PENDING → REVIEWED` và ca SQL tham chiếu với hai kết nối. Ghi nhận đầu tiên giữ nguyên Admin, thời điểm và ghi chú.
- [x] Trên Linux, kiểm tra từ chối ghi xử lý đúng ngưỡng 180 × 24 giờ, cùng FK chặn purge sớm cho người báo cáo, tài khoản đích, người gửi Context và Admin xử lý.
- [x] Trên Linux, kiểm tra đường thành công khi chuyển archive, giữ tombstone và xóa báo cáo cùng evidence.
- [ ] Đối chiếu rollback khi retry trùng revision, purge backup theo chủ tài khoản, biên 30 ngày, ca ngay trước mốc 180 ngày và rollback toàn chuỗi archive khi lỗi.
- [ ] Chạy lại các ca SQL trên hai môi trường Windows.

Quyền Admin, lịch dọn, canonical JSON/giới hạn byte, retry API và tác vụ archive/purge thuộc phần nghiệm thu backend theo DEC-05/DEC-10.

## 8. Theo dõi nghiệm thu và bàn giao

| Nội dung | Phiên bản/commit kiểm tra | Kết quả | Bằng chứng hoặc lỗi cần xử lý |
| --- | --- | --- | --- |
| Dựng PostgreSQL trống bằng Docker | PostgreSQL 18.7, image `postgres:18` | Đạt theo output bạn cung cấp trước cập nhật | Volume `chatsystem_postgres-data`, container `chatsystem-db-1` healthy tại lúc dựng môi trường |
| Chuẩn bị môi trường Windows, mục 3 | Xác nhận của bạn ngày 17/11/2026 | Hoàn thành | Gồm hai PostgreSQL, psql, cổng riêng và UTF-8 |
| Runner riêng `logs-analytics` | Working tree trên nền `342e77f`, 18/11/2026, gồm mục 5.2 | Đạt 109/109 (9 core + 20 login logs + 29 app opens + 43 truy vấn tham chiếu + 8 index/tên), 4,25 giây | DB tạm `chatsystem_schema_test_logs_analytics_f0a5d784500b`; rollback sạch và đã dọn DB |
| Schema và test nghiệp vụ qua Docker | Working tree trên nền `342e77f`, 18/11/2026, gồm mục 5.2 | `--flow all` đạt 109/109, 4,26 giây | DB tạm `chatsystem_schema_test_all_bacfcb7a711a`; rollback sạch và đã dọn DB. File 10/20 còn scaffold; nguồn chat/bạn bè/retention đăng ký là hợp đồng đầu vào mẫu, chưa nghiệm thu auth/purge và adapter tích hợp |
| Dựng PostgreSQL 18 cài trực tiếp | PostgreSQL 18.7, x86_64-windows | Đạt theo output bạn cung cấp | Kết nối cổng 5432, tạo `chatsystem_windows_test` từ `template0`, kiểm tra user `postgres` và encoding UTF8 |
| Schema và test nghiệp vụ trên PostgreSQL cài trực tiếp ở Windows | Chưa có lượt nghiệm thu cho checkout hiện tại | Chờ máy Windows | Dùng `py -3 script/test-db.py --local-server --pg-bin $pgBin --flow all` |
| Đối chiếu mục 7 trên Linux qua Unix socket | Working tree trên nền `7b91aa6`, PostgreSQL và psql 18.6 | `auth-friends` 95 SQL, `logs-analytics` 110 SQL + 2 ca đồng thời. Sau khi khôi phục bộ test luồng 20, `messaging-e2ee-spam` đạt 156 SQL + 1 ca đồng thời (0,68 giây), `all` đạt 347 SQL + 3 ca đồng thời (0,91 giây) | `python3 script/test-db.py --native --pg-bin "$pgBin" --flow <flow>`. Mã thoát 0, rollback sạch |
| Đối chiếu mục 7 trên Linux qua server TCP loopback | Working tree trên nền `7b91aa6`, PostgreSQL và psql 18.6, UTF-8 | `all` 347 SQL + 3 ca đồng thời (0,47 giây), `logs-analytics` 110 SQL + 2 ca đồng thời (0,31 giây) | `python3 script/test-db.py --local-server --pg-bin "$pgBin" --pg-port "$pgPort" --pg-user chatsystem_validation --flow <flow>`. Hai lượt thoát 0, `pg_trgm` và `unaccent` có mặt. Bản sao CRLF chạy qua. Ca giả lập lỗi thoát khác 0, catalog xác nhận đã dọn DB tạm |
| Đối chiếu mục 7 qua Docker Desktop trên Linux | Working tree trên nền `7b91aa6`, Docker CLI 29.10.0, engine 29.9.3, Compose v5.7.1, PostgreSQL và psql 18.7 | Volume mới nạp đủ 23 bảng, UTF-8, `pg_trgm`/`unaccent`. `logs-analytics` đạt 110 SQL + 2 ca đồng thời (4,28 giây). `all` đạt 347 SQL + 3 ca đồng thời (4,78 giây) | Mã thoát 0. `docker compose -p chatsystem-step7-linux-977158800f0e up -d --wait db` và `python3 script/test-db.py --flow <flow> --project chatsystem-step7-linux-977158800f0e`. Hai DB tạm có suffix `1a0f84969f49` và `9c708935bf81`. Đã xác nhận rollback và dọn DB, bản SQL tạm, container, volume và mạng riêng. Image `postgres@sha256:e754b839f447639b7004895f4d8a4bfcb4e9dbe3a1bc035f27f646562a16fd61` |
| Docker Desktop trên Windows với checkout hiện tại | Chưa có lượt nghiệm thu | Chờ máy Windows | Chạy hai luồng ở mục 7.1 |
| Ràng buộc hai bảng và ghi trùng đồng thời | Working tree trên nền `ab918e4`, 18/11/2026 | SQL ràng buộc đạt; hai ca hai kết nối đồng thời đạt, 2,80 giây | `test-app-open-concurrency.py`; DB tạm `chatsystem_schema_test_logs_analytics_20eba1357875` đã dọn; xác nhận chờ khóa và cùng ID/thời điểm |
| Thống kê, timezone, xóa/thu hồi và purge | Working tree, 18/11/2026, mục 4–5.1 | Đạt SQL lượt mở app và công thức tham chiếu A-12–A-16; có biên năm Việt Nam, recall/ẩn tin, purge và username dùng lại | Purge mô phỏng bằng DELETE users; nguồn đăng ký được giữ và metadata chat/bạn bè là prototype đầu vào, còn chờ adapter/quy trình tích hợp |
| Index và kế hoạch thực thi truy vấn | Working tree trên nền `342e77f`, 18/11/2026, PostgreSQL 18.7 | Đạt phạm vi core/file 30 và đường đọc A-12 tham chiếu; 8 EXPLAIN trên mẫu lớn/nhỏ, có trước/sau | `benchmarks/logs-analytics-indexes-*.json`; DB lớn sau thay đổi `chatsystem_schema_test_logs_analytics_8957a4e4df80` và DB nhỏ `chatsystem_schema_test_logs_analytics_bce016ee8252` đã dọn. Index teammate còn chờ schema/đối chiếu |
| ERD khớp schema tích hợp | Working tree trên nền `7b91aa6`, 19/11/2026, PostgreSQL 18.6 | Đạt 23 bảng, 151 cột, 23 PK, 33 FK | [Sáu sơ đồ Draw.io SVG theo nhóm](docs/architecture/database.md#erd-theo-nhóm-nghiệp-vụ-mục-6) |
| SQL E2EE/spam tích hợp theo DEC-05/DEC-10 | Working tree trên nền `7b91aa6`, PostgreSQL 18.6 native | 146 kiểm tra lưu trữ trong bộ 347 kiểm tra SQL, cộng 1 ca đồng thời bằng SQL tham chiếu | Nghiệm thu Windows và các nghiệp vụ backend theo mục 7.3 |

- [ ] Hai bảng và index thuộc phần việc Thành viên 3 đã được tích hợp.
- [x] Dữ liệu mẫu, truy vấn kiểm chứng và kết quả tính tay đã đủ để thành viên khác chạy lại.
- [ ] Thiết kế giữ dữ liệu lịch sử sau purge đã có SQL và ca kiểm chứng tương ứng.
- [x] ERD nguồn và ảnh xuất đã được kiểm tra.
- [x] ERD đã đối chiếu DEC-05/DEC-10, các ca SQL trên PostgreSQL native được ghi kết quả và phần phụ thuộc backend được nêu trong tài liệu bàn giao.
- [ ] Hai lượt Windows ở mục 7 đạt yêu cầu, hướng dẫn chạy đã cập nhật.
- [ ] Có commit thể hiện phần đóng góp để dùng làm bằng chứng ở giai đoạn nộp bài.
- [ ] Cập nhật tiến độ phần CSDL `X-01` theo kết quả thực tế. Mốc hoàn thành giai đoạn 2 chung được đối chiếu với phần việc của cả ba thành viên.

### Thứ tự thực hiện dự kiến trong 2 ngày

| Thời điểm | Công việc | Kết quả đến cuối buổi |
| --- | --- | --- |
| Ngày 1, buổi đầu | Đọc core/workflow, thống nhất dữ liệu giao nhau và vẽ ERD nháp | Quy ước UUID/thời gian/purge, ERD nháp và thiết kế hai bảng |
| Ngày 1, buổi sau | Viết cặp DDL/Test của bạn, chạy `--flow logs-analytics` sau mỗi lần sửa | SQL Thành viên 3, kết quả tính tay và test riêng |
| Ngày 2, buổi đầu | Tích hợp schema, đối chiếu ERD và DEC-05/DEC-10, chạy `--flow all` | ERD tích hợp và danh sách lỗi sau kiểm thử |
| Ngày 2, buổi sau | Chạy PostgreSQL cài trực tiếp, điều chỉnh SQL/ERD, chạy lại kiểm chứng và bàn giao | SQL đã kiểm tra ở hai môi trường, ERD và kết quả nghiệm thu |

Thời lượng trên phụ thuộc vào thời điểm nhận hợp đồng và SQL nghiệp vụ từ các thành viên. Môi trường Windows đã chuẩn bị xong; core UUID và cặp DDL/Test của bạn đã có để bắt đầu luồng riêng. Trong lúc chờ tích hợp có thể hoàn thiện thiết kế, fixture, truy vấn và nguồn ERD theo quy ước đã thống nhất.

## Tài liệu đối chiếu

- [Kế hoạch Version 1, giai đoạn 2](docs/management/plans/version-1-plan.md)
- [Bảng phân công](bang-phan-cong.md)
- [ROADMAP](ROADMAP.md)
- [Tech stack](docs/decisions/tech-stack.md)
- [Thống kê và thời gian](docs/decisions/statistics-and-time.md)
- [Vòng đời tài khoản](docs/decisions/account-lifecycle.md)
- [Bạn bè và chặn](docs/decisions/blocking-and-friendship.md)
- [Xóa, thu hồi và gửi lại tin](docs/decisions/message-deletion.md)
- [Compose phát triển](compose.yaml)
- [Mẫu cấu hình local](.env.example)
- [Hướng dẫn PostgreSQL và script](script/README.md)
- [Workflow SQL theo ba luồng thành viên](docs/databases/workflow.md)
- [Core users dùng chung](script/sql/00-core-users.sql)
- [DDL Thành viên 3](script/sql/30-logs-and-analytics.sql)
- [Test Thành viên 3](script/tests/test-logs-analytics.sql)
- [Runner kiểm thử database tạm](script/test-db.py)
- [Tin cậy thiết bị và vòng đời backup E2EE](docs/decisions/group-permissions-and-e2ee.md)
- [Kiểm duyệt và vòng đời báo cáo spam](docs/decisions/spam-moderation-and-retention.md)

