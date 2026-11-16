# Script cơ sở dữ liệu

Đặt script PostgreSQL 18 tại đây. Theo mục 2.2 của [kế hoạch Version 1](../docs/management/plans/version-1-plan.md), dự kiến dùng `schema.sql` cho bảng, ràng buộc và index; `seed.sql` cho dữ liệu mẫu không chứa thông tin thật.

## Dựng PostgreSQL cho team

Từ thư mục gốc repo, sao chép `.env.example` thành `.env` và thay placeholder `POSTGRES_PASSWORD` bằng mật khẩu cục bộ. Cần Docker Engine + Compose trên Linux hoặc Docker Desktop trên Windows/macOS đang chạy.

```sh
docker compose config --quiet
docker compose up -d --wait db
docker compose ps
docker compose logs --tail=50 db
docker compose exec db psql -U chatsystem -d chatsystem
```

Hai tham số `-U` và `-d` ở trên theo giá trị mẫu; đổi tương ứng nếu bạn đã sửa `POSTGRES_USER` hoặc `POSTGRES_DB`. Trong `psql`, dùng `\q` để thoát.

Từ công cụ SQL trên máy phát triển, dùng host `127.0.0.1`, port mặc định `55432`, user/database `chatsystem` và mật khẩu đã đặt trong `.env`. Port này có thể đổi bằng `POSTGRES_PORT`.

`docker compose down` dừng và gỡ container/network của project; named volume vẫn giữ dữ liệu. Đường dẫn volume là `/var/lib/postgresql`, theo layout của [PostgreSQL Docker Official Image 18+](https://hub.docker.com/_/postgres).

## Khi bổ sung SQL

`./script` được mount chỉ đọc vào `/docker-entrypoint-initdb.d`. PostgreSQL chạy các file `.sql` ở cấp đầu của thư mục này theo thứ tự tên, **chỉ khi thư mục dữ liệu còn trống**. Hiện thư mục có tài liệu hướng dẫn; schema và bộ kiểm thử được bổ sung ở nhánh SQL riêng. Team có thể dựng database trống ngay bằng Compose.

Việc sửa/thêm SQL hoặc đổi các biến khởi tạo `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` trong `.env` không tự cập nhật database đã tồn tại trong volume. SQL mới cần được áp dụng rõ ràng hoặc kiểm thử trên volume trống riêng theo hướng dẫn đi kèm script. Nếu lần khởi tạo SQL bị lỗi, kiểm tra log; restart container không tự chạy lại toàn bộ script trên dữ liệu đã khởi tạo một phần.

Các lệnh trên chạy được trong shell Linux/macOS và PowerShell. Kiểm tra cấu hình mà chưa khởi chạy container bằng `docker compose --env-file .env.example config --quiet`.
