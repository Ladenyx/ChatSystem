# API Contracts & Shared Protocol — ChatSystem (Version 2)

Bộ tài liệu này chốt quy ước giao tiếp **HTTPS REST**, **Raw JSON over WSS** và **DTO dùng chung** giữa **User Client**, **Admin App** và **Chat Server** (`Spring Boot 4.1.x` + `PostgreSQL 18`). Toàn bộ mô hình DTO/enum/envelope tương ứng nằm trong module [`src/common`](../../../src/common/src/main/java/com/chatsystem/common/package-info.java) (thuần JDK 25, không phụ thuộc JavaFX hay Spring).

## Mục lục tài liệu

| Tài liệu | Nội dung chính | Đối tượng sử dụng |
| --- | --- | --- |
| [rest-conventions.md](rest-conventions.md) | Base URL, xác thực Opaque Bearer Token, tách phiên User/Admin, quy ước `camelCase`, `ApiError` + danh mục `ErrorCode`, phân trang Cursor/Offset, lọc/sắp xếp, idempotency, bản đồ endpoint Đợt 1 & Đợt 2 | Cả nhóm |
| [websocket-protocol.md](websocket-protocol.md) | Bắt tay `wss://<host>/ws`, envelope `WsCommand` / `WsEvent`, heartbeat & presence, thứ tự `seq`, ACK gửi tin bền vững, biên nhận `DELIVERED`/`READ`, đồng bộ bù sau reconnect, danh mục sự kiện theo phạm vi quyền và mã đóng kết nối | Cả nhóm |
| [openapi.yaml](openapi.yaml) | Đặc tả OpenAPI 3.1.0 cho các endpoint Đợt 1 (Auth, Messaging 1-1 & Sync, Admin Overview/Users/Logs/Analytics) kèm khung chuẩn cho các cụm Đợt 2 (Contacts, Groups/E2EE, Spam, AI) | Client & Server |
| [schemas/](schemas/) | JSON Schema (Draft 2020-12) kiểm chứng `ApiError`, `WsFrame`, `MessageWindowResponse` (`U-SCR-07`), `AdminUser` (`A-SCR-03`) và `AdminOverview/Analytics` (`A-SCR-02`, `A-SCR-07`–`09`) | Kiểm thử contract |
| [fixtures/](fixtures/) | Dữ liệu JSON mẫu dùng chung giữa Client và Server cho kiểm thử tích hợp & đối chiếu DEC-10 canonical bytes | Kiểm thử tích hợp |
| [Kiểm kê & Spec cụm Admin](../../specs/admin/screen-inventory.md) | Kiểm kê màn hình V1, đối chiếu DDL và đặc tả [Auth/Overview/Logs](../../specs/admin-auth-overview/spec.md), [Users](../../specs/admin-users/spec.md), [Analytics](../../specs/admin-analytics/spec.md) | Quân & Bảo |

## Quyết định kiến trúc đã chốt (Giai đoạn 1)

1. **Giao tiếp:** HTTPS REST (`/api/v1/...`) + Raw JSON WebSocket (`wss://<host>/ws`) dùng trực tiếp `java.net.http.HttpClient` và `java.net.http.WebSocket` của JDK 25 (không dùng STOMP).
2. **Đặt tên JSON & phản hồi REST:** Dùng `camelCase` thống nhất trên toàn bộ REST và WSS. Response thành công trả thẳng body JSON (không bọc `{ "data": ... }`); mọi lỗi 4xx/5xx trả thống nhất đối tượng `ApiError`.
   - **Ngoại lệ đã chốt theo [DEC-10](../../decisions/spam-moderation-and-retention.md#biểu-diễn-dùng-để-tính-byte):** Biểu diễn canonical bytes để tính hạn mức **1 MiB** và **SHA-256 fingerprint** của báo cáo spam giữ nguyên `snake_case` và đúng thứ tự khóa trong `CanonicalSpamReportEncoder` (`src/common`).
3. **Xác thực phiên:** Opaque Bearer Token lưu băm trong `user_sessions.token_hash`, gửi qua header `Authorization: Bearer <token>` trên cả REST và HTTP Upgrade handshake của WSS. Chưa áp đặt TTL phiên cố định (khớp schema Version 1); phiên chỉ kết thúc khi đăng xuất hoặc bị thu hồi (đăng nhập Admin App mới, khóa tài khoản, xóa mềm, đổi/đặt lại mật khẩu).
4. **Module `src/common`:** Không phụ thuộc JavaFX hay Spring/Jackson; dùng Java 25 `record`, `enum`, `UUID`, `Instant`, `LocalDate` để Jackson 3 trên Spring Boot 4.1 và trình ánh xạ JSON phía Desktop Client đọc/ghi trực tiếp qua canonical constructor.

## Phân công sử dụng và mở rộng contract

- **Nguyễn Thế Quân (`@Ladenyx`):** Duy trì quy ước nền REST/WSS, `src/common`, `openapi.yaml` phần nền tảng và cụm Admin (`A-01`–`A-07`, `A-12`–`A-16`).
- **Nguyễn Phương Gia Bảo (`@baonguyen1776`):** Mở rộng chi tiết payload cho cụm Auth, Profile, Friends/Blocks (`U-01`–`U-11`), thiết bị/backup E2EE (`U-25`) và Spam Moderation (`U-26`, `A-10`, `A-11`).
- **Thái Nguyễn Tuấn Kiệt (`@tkiet24`):** Mở rộng chi tiết payload cho cụm Messaging 1-1 (`U-12`–`U-18`), Group Chat/Epoch (`U-19`–`U-25`, `A-08`, `A-09`) và AI Assistant (`U-27`).

## Hạng mục DDL cần phối hợp bổ sung (đối chiếu `script/schema.sql`)

Schema Version 1 ([`script/schema.sql`](../../../script/schema.sql)) đã phủ phần lớn bảng cốt lõi. Khi chuyển sang Giai đoạn 2 (theo mục 1.1 của [kế hoạch Version 2](../../management/plans/version-2-plan.md#giai-đoạn-1-rà-ui-viết-feature-spec-và-chốt-api-contract-đợt-đầu-đến-2511)), cần bổ sung các bảng/cột sau kèm cập nhật ERD và test SQL trong cùng PR:

| Nhu cầu trong contract | Hiện trạng trong `script/schema.sql` | Đề xuất bổ sung DDL |
| --- | --- | --- |
| Trạng thái `DELIVERED` / `READ` theo từng người nhận ([DEC-02](../../decisions/message-deletion.md), `UC-USR-MSG-01`) | `messages` chưa lưu biên nhận nhận/đọc | Bảng `message_receipts` hoặc `conversation_member_watermarks` (`last_delivered_message_id`, `last_read_message_id`, `updated_at`) |
| Mốc xóa toàn bộ lịch sử phía mình (`U-15`, `DELETE /conversations/{id}/history`) | Chỉ có `message_deletions` theo từng tin | Cột `cleared_before TIMESTAMPTZ` (hoặc `(cleared_at, cleared_message_id)`) trên `conversation_members` để tránh ghi hàng nghìn dòng vào `message_deletions` và đảm bảo tin mới vẫn hiện |
| Khóa đồng bộ bù WebSocket sau reconnect (`seq` tăng đơn điệu) | Chưa có bảng sự kiện đồng bộ | Sequence/bảng `user_sync_events (seq BIGSERIAL, user_id, event_id, event_type, payload, created_at)` có thời hạn giữ ngắn (ví dụ 7 ngày) hoặc tổng hợp từ `updated_at` kèm `fullResyncRequired` |
| Giới hạn gửi lại OTP / reset password & khóa thử sai đăng nhập ([DEC-06](../../decisions/password-reset-and-email.md), [DEC-07](../../decisions/account-lifecycle.md)) | Chưa lưu bộ đếm thử sai bền vững | Bảng `auth_login_throttles` / `email_rate_limits` (hoặc kết hợp PostgreSQL + Caffeine theo chính sách không đặt lại khi đóng/mở app) |
| Trạng thái đồng ý/chặn theo thời điểm cho nhóm hoặc yêu cầu gửi lại tin chưa ACK | `messages.id` đã là `UUID PRIMARY KEY` (nhận ID do client gửi) | Giữ `messages.id` do client cung cấp; kiểm tra trùng payload/hội thoại khi retry |
