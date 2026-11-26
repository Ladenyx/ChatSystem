# Quy ước HTTPS REST API — ChatSystem (`/api/v1`)

Tài liệu này quy định chuẩn thiết kế, xác thực, định dạng dữ liệu, xử lý lỗi, phân trang, lọc/sắp xếp và idempotency cho toàn bộ REST API giữa **User Client**, **Admin App** và **Chat Server**.

Tham chiếu liên quan: [Tech stack](../../decisions/tech-stack.md) · [DEC-02](../../decisions/message-deletion.md) · [DEC-03](../../decisions/blocking-and-friendship.md) · [DEC-05](../../decisions/group-permissions-and-e2ee.md) · [DEC-06](../../decisions/password-reset-and-email.md) · [DEC-07](../../decisions/account-lifecycle.md) · [DEC-08](../../decisions/llm-assistant.md) · [DEC-10](../../decisions/spam-moderation-and-retention.md) · [Thống kê & thời gian](../../decisions/statistics-and-time.md).

---

## 1. Giao thức, Versioning và Headers

- **Giao thức:** HTTPS (`TLS 1.2+`); khi phát triển cục bộ có thể chạy HTTP loopback `http://127.0.0.1:8080`, triển khai VPS qua Reverse Proxy TLS.
- **Tiền tố đường dẫn:** `/api/v1`. Nhóm quản trị nằm dưới `/api/v1/admin/...`.
- **Định dạng thân gói tin:** `Content-Type: application/json; charset=utf-8` và `Accept: application/json`.

### Headers chuẩn

| Header | Chiều | Bắt buộc | Mục đích |
| --- | --- | --- | --- |
| `Authorization` | Client → Server | Có (trừ endpoint công khai) | `Bearer <opaque-session-token>` hoặc `Bearer <transition-token>` ở bước xác minh/đặt mật khẩu tạm |
| `X-Request-Id` | Hai chiều | Khuyến nghị (Client gửi UUID; Server tạo mới nếu thiếu và phản hồi lại) | Truy vết log, gắn vào `ApiError.requestId` và đối chiếu phản hồi về muộn phía UI |
| `X-Client-Device-Id` | Client → Server | Khi User Client đã khởi tạo thiết bị E2EE | Định danh thiết bị hiện tại (`e2ee_devices.id`) phục vụ cấp/kiểm tra envelope khóa nhóm |
| `Idempotency-Key` | Client → Server | Tùy chọn với lệnh có body không mang sẵn client UUID | Bảo vệ chống bấm gửi lặp trên thao tác ghi (các thao tác chính như gửi tin, lời mời kết bạn, báo cáo spam mang sẵn client UUID trong body) |

---

## 2. Xác thực phiên, Phạm vi quyền và Vòng đời token

### 2.1. Opaque Bearer Token (`user_sessions`)

- Khi đăng nhập thành công và đủ điều kiện cấp phiên, Server sinh token ngẫu nhiên mật mã (tối thiểu 256 bit entropy, mã hóa Base64URL), chỉ lưu `SHA-256` vào `user_sessions.token_hash` và trả token gốc một lần duy nhất trong phản hồi đăng nhập.
- **Không áp đặt TTL cứng** (khớp schema Version 1 `10-auth-and-friends.sql`); phiên chỉ hết hiệu lực khi:
  1. Người dùng đăng xuất (`POST /api/v1/auth/logout` — chỉ thu hồi phiên hiện tại).
  2. Đăng nhập mới hợp lệ trên **Admin App** (thu hồi ngay phiên `ADMIN_APP` cũ đang sống cùng `user_id`, kể cả REST và WSS).
  3. Tài khoản bị khóa quản trị (`LOCKED`), xóa mềm (`DELETED`) hoặc đổi/đặt lại mật khẩu thành công (thu hồi toàn bộ phiên đang sống trên mọi thiết bị).

### 2.2. Tách phạm vi phiên (`SessionScope`)

| Phạm vi | Endpoint đăng nhập | Quyền gọi API | Số phiên đồng thời |
| --- | --- | --- | --- |
| `USER_CLIENT` | `POST /api/v1/auth/login` (`scope = USER_CLIENT`, kèm `launchId`) | Mọi endpoint `/api/v1/...` ngoài `/api/v1/admin/...` | Nhiều thiết bị đồng thời |
| `ADMIN_APP` | `POST /api/v1/auth/login` (`scope = ADMIN_APP`) | Các endpoint `/api/v1/admin/...` và `/api/v1/auth/me`, `/api/v1/auth/logout` | Tối đa **1** phiên/tài khoản |

- Tài khoản có `role = ADMIN` vẫn được đăng nhập `USER_CLIENT`, nhưng phiên `USER_CLIENT` **không bao giờ** gọi được `/api/v1/admin/...` (trả `403 SESSION_SCOPE_MISMATCH`).
- Ngược lại, phiên `ADMIN_APP` **không** gọi được các endpoint chat/bạn bè người dùng và **không** được đọc nội dung tin nhắn ngoài bằng chứng spam trong `A-SCR-05` ([DEC-07](../../decisions/account-lifecycle.md)).
- Server kiểm tra tuần tự trên **mọi yêu cầu**: (1) token hợp lệ & chưa `revoked_at`, (2) `users.status == ACTIVE`, (3) `scope` khớp đường dẫn, (4) `users.role` khớp yêu cầu (với Admin là `ADMIN`), (5) quyền ở mức tài nguyên (thành viên nhóm, Admin nhóm, quan hệ chặn).

### 2.3. Quyền chuyển tiếp trước phiên (`AuthNextAction`)

Theo [DEC-06](../../decisions/password-reset-and-email.md) và [DEC-07](../../decisions/account-lifecycle.md#điều-kiện-tài-khoản-khi-đăng-nhập), `POST /api/v1/auth/login` trả về trường `nextAction`:

| `nextAction` | Ý nghĩa | Token trả về | Quyền sử dụng |
| --- | --- | --- | --- |
| `SESSION_GRANTED` | Đăng nhập thành công | `sessionToken` (`USER_CLIENT` hoặc `ADMIN_APP`) | Vào khung chính |
| `EMAIL_VERIFICATION_REQUIRED` | Tài khoản do Admin tạo chưa xác minh email | `transitionToken` (ngắn hạn, không lưu `user_sessions`) | Chỉ gọi xác minh/gửi lại mã email (`U-SCR-03`), không cấp phiên ứng dụng |
| `FORCE_PASSWORD_CHANGE_REQUIRED` | Đăng nhập bằng mật khẩu tạm còn hạn 15 phút | `transitionToken` dùng chung mốc `expiresAt` của `password_reset_tokens` | Chỉ gọi `POST /api/v1/auth/force-password-change` (`U-SCR-05`), không gia hạn khi thao tác |

### 2.4. Ghi nhận lượt mở ứng dụng (`launchId`)

- Khi tiến trình `User Client` khởi động, ứng dụng sinh một `launchId` (`UUID`) lưu trong bộ nhớ suốt vòng đời tiến trình.
- Khi gọi `POST /api/v1/auth/login` với `scope = USER_CLIENT`, client gửi kèm `launchId`. Sau khi cấp phiên thành công, Server gọi `public.record_app_open(user_id, launch_id, 'USER_CLIENT')` trong cùng transaction cấp phiên.
- Reconnect WSS hoặc đăng nhập lại cùng tiến trình tái sử dụng cùng `launchId`, không làm tăng số lượt mở ứng dụng ([Thống kê và thời gian](../../decisions/statistics-and-time.md)).

---

## 3. Quy ước JSON, Kiểu dữ liệu và Thời gian

| Kiểu logic | Quy ước JSON | Kiểu Java (`src/common`) | Ví dụ |
| --- | --- | --- | --- |
| Tên trường | `camelCase` | Tên component `record` | `fullName`, `nextCursor`, `createdAt` |
| Định danh | UUID chữ thường có gạch nối (RFC 4122) | `java.util.UUID` | `"550e8400-e29b-41d4-a716-446655440000"` |
| Thời điểm | ISO-8601 UTC có hậu tố `Z` | `java.time.Instant` | `"2026-11-15T03:00:00Z"` |
| Ngày lịch / Ngày sinh | ISO-8601 `YYYY-MM-DD` | `java.time.LocalDate` | `"2026-11-15"` |
| Bộ lọc khoảng ngày (`from`, `to`) | `YYYY-MM-DD` theo múi giờ `Asia/Ho_Chi_Minh` | `java.time.LocalDate` | `from=2026-11-01&to=2026-11-15` → `[2026-11-01 00:00+07, 2026-11-16 00:00+07)` |
| Tháng trong biểu đồ 12 tháng | Số nguyên `1..12` kèm `year` | `int` | `{ "month": 11, "count": 42 }` |
| Dữ liệu nhị phân (E2EE) | Base64 chuẩn RFC 4648 (có padding) | `String` (Base64) | `"AAECAwQFBgc..."` |
| Enum | `UPPER_SNAKE_CASE` khớp CHECK SQL | Java `enum` | `"ACTIVE"`, `"USER_CLIENT"`, `"REVIEWED"` |
| Giới tính (`gender`) | Enum `MALE` / `FEMALE` / `OTHER` (hoặc `null` nếu chưa cung cấp); Server ánh xạ `'Nam'` / `'Nữ'` / `'Khác'` khi lưu DB | `Gender` (nullable) | `"FEMALE"` ↔ SQL `'Nữ'` |
| Giá trị vắng mặt | `null` (không dùng chuỗi rỗng `""` cho ngày sinh, địa chỉ, con trỏ) | Nullable field | `"deletedAt": null` |

> **Ngoại lệ DEC-10 (Báo cáo spam):** Request HTTP `POST /api/v1/spam-reports` vẫn dùng `camelCase` thống nhất như các API khác, nhưng cả Client và Server đều dùng `CanonicalSpamReportEncoder` trong `src/common` để xuất đúng **canonical bytes `snake_case`** theo thứ tự khóa của [DEC-10](../../decisions/spam-moderation-and-retention.md#biểu-diễn-dùng-để-tính-byte) khi kiểm tra giới hạn **1.048.576 byte (1 MiB)** và tính **SHA-256 fingerprint** / **tombstone digest**.

---

## 4. Định dạng phản hồi thành công & Lỗi chuẩn (`ApiError`)

### 4.1. Phản hồi thành công (2xx)

Trả thẳng payload JSON (không bọc lớp `{ "data": ... }`):
- `200 OK`: Đọc dữ liệu (`GET`), cập nhật (`PATCH`/`PUT`), lệnh có kết quả trả về (`POST` đăng nhập, thao tác quản trị, AI).
- `201 Created`: Tạo tài nguyên mới (`POST` đăng ký tạm, tạo user, tạo nhóm, gửi tin/báo cáo mới). Nếu retry idempotent gặp bản ghi đã tồn tại hợp lệ, trả `200 OK` (hoặc giữ nguyên body đã lưu).
- `204 No Content`: Đăng xuất, hủy đăng ký tạm, xóa không cần body.

### 4.2. Phản hồi lỗi (`ApiError`)

Mọi lỗi `4xx` và `5xx` đều trả về đúng một cấu trúc JSON (`com.chatsystem.common.api.ApiError`):

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu đầu vào không hợp lệ.",
  "status": 400,
  "timestamp": "2026-11-15T03:00:00Z",
  "path": "/api/v1/admin/users",
  "requestId": "9f3c2d8a-74b2-4e7b-9d90-2b3d1e6d2c11",
  "retryable": false,
  "details": [
    {
      "field": "username",
      "code": "INVALID_FORMAT",
      "message": "Tên đăng nhập phải dài 6–30 ký tự ASCII (chữ, số, _ hoặc -)."
    }
  ]
}
```

- `retryable = true` báo hiệu Desktop UI có thể hiển thị trạng thái **Lỗi có thể thử lại** (mục 2.3 của kế hoạch Version 2).
- `retryable = false` là lỗi nghiệp vụ/quyền/dữ liệu, không tự động thử lại.

### 4.3. Danh mục `ErrorCode` chuẩn

| Mã lỗi (`ErrorCode`) | HTTP | `retryable` | Ngữ cảnh sử dụng |
| --- | :---: | :---: | --- |
| `VALIDATION_ERROR` | `400` | `false` | Dữ liệu không hợp lệ, kèm mảng `details` theo từng trường |
| `INVALID_CURSOR` | `400` | `false` | Con trỏ phân trang sai định dạng hoặc không hợp lệ |
| `INVALID_DATE_RANGE` | `400` | `false` | `from > to` hoặc định dạng ngày sai |
| `UNAUTHORIZED` | `401` | `false` | Thiếu token, token không hợp lệ, sai thông tin đăng nhập (không lộ tài khoản tồn tại hay không) |
| `SESSION_REVOKED` | `401` | `false` | Phiên đã bị thu hồi (đăng xuất, đăng nhập Admin mới, đổi mật khẩu, khóa/xóa tài khoản) |
| `TRANSITION_TOKEN_EXPIRED` | `401` | `false` | Quyền tạm đặt mật khẩu mới / xác minh đã hết hạn |
| `FORBIDDEN` | `403` | `false` | Không có quyền thao tác trên tài nguyên |
| `SESSION_SCOPE_MISMATCH` | `403` | `false` | Dùng phiên `USER_CLIENT` gọi `/api/v1/admin/...` hoặc ngược lại |
| `ACCOUNT_LOCKED` | `403` | `false` | Tài khoản đang ở trạng thái `LOCKED` |
| `ACCOUNT_DELETED` | `403` | `false` | Tài khoản đang ở trạng thái `DELETED` (xóa mềm) |
| `EMAIL_NOT_VERIFIED` | `403` | `false` | Tài khoản chưa xác minh email |
| `BLOCKED_BY_RELATIONSHIP` | `403` | `false` | Hai bên có quan hệ chặn (cấm chat 1-1, mời bạn, mời nhóm theo DEC-03) |
| `NOT_CONVERSATION_MEMBER` | `403` | `false` | Không còn là thành viên hội thoại / nhóm |
| `GROUP_ADMIN_REQUIRED` | `403` | `false` | Thao tác yêu cầu quyền Admin nhóm (`U-20`, `U-22`, `U-23`) |
| `E2EE_DEVICE_UNTRUSTED` | `403` | `false` | Thiết bị mới chưa được xác nhận tin cậy, chưa được cấp khóa nhóm (DEC-05) |
| `NOT_FOUND` | `404` | `false` | Không tìm thấy tài nguyên (hoặc ẩn tài khoản đã chặn/xóa theo quy tắc bảo mật) |
| `CONFLICT` | `409` | `false` | Xung đột trạng thái nghiệp vụ chung |
| `USERNAME_ALREADY_EXISTS` | `409` | `false` | Username đã thuộc một tài khoản khác (không phân biệt hoa thường) |
| `EMAIL_ALREADY_EXISTS` | `409` | `false` | Email đã thuộc một tài khoản khác (không phân biệt hoa thường) |
| `IDEMPOTENCY_CONFLICT` | `409` | `false` | Trùng client UUID (`messageId`, `reportId`, `requestId`) nhưng payload/đích khác bản đã lưu |
| `LAST_GROUP_ADMIN_REQUIRED` | `409` | `false` | Admin nhóm cuối cùng phải chỉ định người kế nhiệm trước khi hạ quyền/rời nhóm (`U-DLG-05`) |
| `GROUP_CLOSED` | `409` | `false` | Nhóm đã đóng (`closed_at IS NOT NULL`), chỉ còn đọc lịch sử |
| `RECALL_WINDOW_EXPIRED` | `409` | `false` | Tin nhắn đã quá hạn thu hồi 48 giờ (DEC-02) |
| `ALREADY_REVIEWED` | `409` | `false` | Báo cáo spam đã ở trạng thái `REVIEWED`, không cho sửa/ghi đè (DEC-10) |
| `E2EE_EPOCH_STALE` | `409` | `true` | Client gửi tin nhóm bằng `epochNo` cũ đã `RETIRED`; cần đồng bộ epoch mới |
| `E2EE_BACKUP_REVISION_CONFLICT` | `409` | `false` | Xung đột revision khi thay bản sao lưu khóa E2EE (DEC-05) |
| `RESOURCE_EXPIRED` | `410` | `false` | Yêu cầu đăng ký tạm quá 30 phút, mã OTP quá 10 phút, mật khẩu tạm quá 15 phút, backup quá 30 ngày, hoặc báo cáo spam đã hết hạn 180 ngày / bị purge (DEC-10 tombstone) |
| `PAYLOAD_TOO_LARGE` | `413` | `false` | Vượt giới hạn dung lượng (ví dụ gói báo cáo spam > 1.048.576 byte hoặc > 20 Selected / > 30 Context) |
| `RATE_LIMITED` | `429` | `true` | Khóa tạm do thử sai đăng nhập, giãn cách gửi email 60 giây / 5 lần/giờ, hoặc hết hạn mức LLM (kèm `Retry-After` header khi xác định được) |
| `INTERNAL_ERROR` | `500` | `true` | Lỗi nội bộ Server ngoài dự kiến |
| `EMAIL_DELIVERY_FAILED` | `502` | `true` | Lỗi gửi thư qua Gmail SMTP (không xóa mật khẩu cũ/phiên đang có theo DEC-06) |
| `AI_PROVIDER_ERROR` | `502` | `true` | Lỗi phản hồi hoặc sai định dạng JSON Schema từ Meta Model API (DEC-08) |
| `SERVICE_UNAVAILABLE` | `503` | `true` | Dịch vụ tạm thời không sẵn sàng / lỗi kết nối PostgreSQL |
| `AI_PROVIDER_TIMEOUT` | `504` | `true` | Quá thời gian chờ 15 giây khi gọi LLM (DEC-08; Client giữ nguyên ô nháp) |

---

## 5. Phân trang, Lọc và Sắp xếp

Hệ thống dùng **hai chuẩn phân trang rõ ràng** theo đặc thù truy cập:

### 5.1. Cursor Pagination (`CursorPage<T>`) — Luồng thời gian thực & nhật ký

Dùng cho: lịch sử tin nhắn (`U-SCR-07`), danh sách hội thoại, kết quả tìm tin nhắn, nhật ký đăng nhập hệ thống (`A-SCR-06`), lịch sử đăng nhập của một người dùng (`A-05`), đồng bộ bù sự kiện.

```json
{
  "items": [ ... ],
  "nextCursor": "eyJjcmVhdGVkQXQiOiIyMDI2LTExLTE1VDAyOjMwOjAwWiIsImlkIjoiLi4uIn0",
  "hasMore": true
}
```

- Con trỏ `nextCursor` là chuỗi **Base64URL không padding (opaque)** do Server mã hóa từ cặp khóa ổn định `(timestamp, uuid)`. Client không tự tạo hay phân tích con trỏ.
- Quy định `limit`: mặc định `50`, tối đa `100` (riêng lịch sử tin nhắn `U-SCR-07` khóa tối đa `50` theo `UC-USR-MSG-01` BR-01).

#### Mẫu truy vết chuẩn cho `U-SCR-07` (Lịch sử & nhảy tới kết quả tìm kiếm)

Theo mục 1.3 của [kế hoạch Version 2](../../management/plans/version-2-plan.md#giai-đoạn-1-rà-ui-viết-feature-spec-và-chốt-api-contract-đợt-đầu-đến-2511):

- **Tải trang mới nhất / cuộn lên tải tin cũ hơn:**
  - `GET /api/v1/conversations/{conversationId}/messages?limit=50`
  - `GET /api/v1/conversations/{conversationId}/messages?before=<cursor>&limit=50`
- **Tải tin mới hơn (khi nhảy về cuối từ đoạn tin cũ):**
  - `GET /api/v1/conversations/{conversationId}/messages?after=<cursor>&limit=50`
- **Nhảy tới tin đích từ kết quả tìm kiếm (`U-17`, `U-18`):**
  - `GET /api/v1/conversations/{conversationId}/messages?aroundMessageId=<uuid>&limit=50`
- Các tham số `before`, `after`, `aroundMessageId` **loại trừ lẫn nhau** (gửi nhiều hơn một tham số trả `400 VALIDATION_ERROR`).
- Phản hồi dùng `MessageWindowResponse`:
  - `items`: luôn sắp xếp theo **thời gian tăng dần `(createdAt ASC, id ASC)`** để khớp trực tiếp hướng hiển thị từ trên xuống dưới của khung chat.
  - `nextCursor` (con trỏ tải tin **cũ hơn** khi cuộn lên đầu, tương ứng `before`), `hasMoreOlder`.
  - `newerCursor` (con trỏ tải tin **mới hơn** khi đang xem đoạn lịch sử giữa chừng, tương ứng `after`), `hasMoreNewer`.
  - `anchorMessageId`: UUID tin đích khi truy vấn `aroundMessageId`, ngược lại `null`.
  - Áp dụng sẵn lọc xóa phía mình (`message_deletions` và mốc xóa toàn bộ lịch sử `U-15`) và ẩn nội dung tin đã `RECALLED`.

### 5.2. Offset Pagination (`OffsetPage<T>`) — Bảng quản trị & thống kê Admin

Dùng cho: Quản lý người dùng (`A-SCR-03`), Quản lý nhóm (`A-SCR-04`), Báo cáo spam (`A-SCR-05`), Bảng đăng ký mới (`A-SCR-07`), Bảng hoạt động (`A-SCR-08`), Bảng mạng lưới bạn bè (`A-SCR-09`).

- Tham số query chuẩn: `page` (bắt đầu từ `0`, mặc định `0`), `size` (mặc định `25`, tối đa `100`), `sortBy` (whitelist theo từng endpoint), `sortDir` (`ASC` | `DESC`).
- Luôn có tie-breaker phụ `id ASC` trên SQL để thứ tự ổn định giữa các trang.
- Sắp xếp tên tiếng Việt ưu tiên collation phù hợp hoặc nhất quán giữa các trang theo spec của cụm.

```json
{
  "items": [ ... ],
  "page": 0,
  "size": 25,
  "totalItems": 148,
  "totalPages": 6
}
```

#### Mẫu truy vết chuẩn cho `A-SCR-03` (Tách Master–Detail theo `userId`)

Theo mục 1.3 và mục 2.2 của kế hoạch Version 2, bảng danh sách bên trái và khung chi tiết bên phải **tách endpoint độc lập** để chọn hàng khác chỉ tải lại khung phải (kèm skeleton riêng) trong lúc bảng trái vẫn tương tác bình thường:

1. **Bảng lọc/sắp xếp bên trái (`A-01`):** `GET /api/v1/admin/users?name=&username=&status=ACTIVE|LOCKED|DELETED&sortBy=fullName|createdAt&sortDir=ASC|DESC&page=0&size=25` → trả `OffsetPage<AdminUserListItemDto>` (chỉ chứa cột cần thiết của bảng: `id`, `username`, `fullName`, `role`, `status`, `createdAt`).
2. **Hồ sơ chi tiết khung phải (`A-02`):** `GET /api/v1/admin/users/{userId}` → trả `AdminUserDetailDto` (đầy đủ `email`, `address`, `dateOfBirth`, `gender`, `emailVerified`, `deletedAt`, `restoreDeadlineAt`, `directFriendCount`, v.v.).
3. **Tab Lịch sử đăng nhập (`A-05`):** `GET /api/v1/admin/users/{userId}/login-history?before=&limit=50` → trả `CursorPage<AdminLoginHistoryItemDto>`.
4. **Tab Danh sách bạn bè (`A-06`):** `GET /api/v1/admin/users/{userId}/friends?page=0&size=50` → trả `OffsetPage<AdminUserFriendDto>`.

### 5.3. Quy ước tham số lọc số lượng (`A-14`, `A-15`)

- Toán tử so sánh (`ComparisonOperator`): `EQ` (`=`), `LT` (`<`), `GT` (`>`).
- Với `A-15` (Dashboard Hoạt động): `metric=APP_OPENS|PEOPLE_CHATTED|GROUPS_CHATTED`, `metricOp=EQ|LT|GT`, `metricValue=<non-negative integer>`.
- Với `A-14` (Mạng lưới Bạn bè): `directFriendsOp=EQ|LT|GT`, `directFriendsValue=<non-negative integer>`.

---

## 6. Định danh (ID) và Xử lý gọi lặp (Idempotency)

| Thao tác | Nguồn ID | Hành vi khi gọi lặp (Retry) | Xung đột khác nội dung |
| --- | --- | --- | --- |
| Gửi tin nhắn (`U-12`, `U-13`, `U-24`) | Client tạo UUID (`messageId`) | Trả lại tin đã lưu (`createdAt` gốc); **không** tạo tin thứ hai và **không** tăng `message_send_events` | `409 IDEMPOTENCY_CONFLICT` |
| Gửi lời mời kết bạn (`U-07`) | Client tạo UUID (`requestId`) | Trả lại lời mời đã tạo với `requestId` đó | `409 IDEMPOTENCY_CONFLICT` |
| Gửi báo cáo spam (`U-26`) | Client tạo UUID (`reportId`) | Đối chiếu `SHA-256` trên DEC-10 canonical bytes; khớp và chưa hết hạn/purge thì trả biên nhận cũ | Khác fingerprint → `409 IDEMPOTENCY_CONFLICT`; đã vào tombstone → `410 RESOURCE_EXPIRED` |
| Đánh dấu đã xử lý spam (`A-10`) | `reportId` | Cùng admin lặp lại trả trạng thái `REVIEWED` hiện tại; admin khác gửi lên bản đã `REVIEWED` trả `409 ALREADY_REVIEWED` | Không cho ghi đè ghi chú/người xử lý |
| Ghi nhận mở app (`U-02`) | Client tạo UUID (`launchId`) | `(account_key, launch_id)` duy nhất trong `app_open_events`; gọi lại giữ nguyên `opened_at` gốc | N/A |
| Đăng ký tài khoản (`U-01`) & Quên mật khẩu (`U-05`) | Server tạo UUID | **Không tự động retry** khi chưa rõ kết quả (DEC-02); mỗi yêu cầu mới chịu kiểm tra giới hạn tốc độ & quy tắc DEC-06 | N/A |

---

## 7. Bản đồ Endpoint toàn hệ thống

Chi tiết schema request/response cho Đợt 1 nằm tại [openapi.yaml](openapi.yaml).

### Đợt 1 — Ưu tiên chốt trước cho Giai đoạn 2 (Demo tích hợp đầu tiên)

| Cụm | Method & Path | Scope | Feature | Phụ trách chính |
| --- | --- | --- | --- | --- |
| Auth | `POST /api/v1/auth/login` | Public | `U-02`, `A-SCR-01` | Bảo |
| Auth | `POST /api/v1/auth/logout` | `USER_CLIENT` \| `ADMIN_APP` | `U-02`, `A-SCR-01` | Bảo |
| Auth | `GET /api/v1/auth/me` | `USER_CLIENT` \| `ADMIN_APP` | `U-02`, `A-SCR-01` | Bảo |
| Auth | `POST /api/v1/auth/registrations` | Public | `U-01` | Bảo |
| Auth | `POST /api/v1/auth/registrations/{registrationId}/verify` | Public | `U-01` | Bảo |
| Auth | `POST /api/v1/auth/registrations/{registrationId}/resend-code` | Public | `U-01` | Bảo |
| Auth | `DELETE /api/v1/auth/registrations/{registrationId}` | Public | `U-01` | Bảo |
| Auth | `POST /api/v1/auth/password-resets` | Public | `U-05` | Bảo |
| Auth | `POST /api/v1/auth/force-password-change` | Transition token | `U-05`, `A-04` | Bảo |
| Messaging | `GET /api/v1/conversations` | `USER_CLIENT` | `U-14`, `U-SCR-07` | Kiệt |
| Messaging | `POST /api/v1/conversations/direct` | `USER_CLIENT` | `U-12` | Kiệt |
| Messaging | `GET /api/v1/conversations/{conversationId}/messages` | `USER_CLIENT` | `U-14`, `U-17`, `U-18` | Kiệt |
| Messaging | `POST /api/v1/conversations/{conversationId}/messages` | `USER_CLIENT` | `U-12`, `U-13`, `U-24` | Kiệt |
| Messaging | `POST /api/v1/conversations/{conversationId}/messages/{messageId}/recall` | `USER_CLIENT` | `U-16` | Kiệt |
| Messaging | `POST /api/v1/conversations/{conversationId}/messages/delete-for-self` | `USER_CLIENT` | `U-16` | Kiệt |
| Messaging | `DELETE /api/v1/conversations/{conversationId}/history` | `USER_CLIENT` | `U-15` | Kiệt |
| Messaging | `GET /api/v1/conversations/{conversationId}/messages/search` | `USER_CLIENT` | `U-17` | Kiệt |
| Messaging | `GET /api/v1/messages/search` | `USER_CLIENT` | `U-18` | Kiệt |
| Sync | `GET /api/v1/sync/events` | `USER_CLIENT` | `X-02`, `U-13` | Quân |
| Admin Users | `GET /api/v1/admin/overview` | `ADMIN_APP` | `A-SCR-02` | Quân |
| Admin Users | `GET /api/v1/admin/users`, `POST /api/v1/admin/users` | `ADMIN_APP` | `A-01`, `A-02` | Quân |
| Admin Users | `GET /api/v1/admin/users/{userId}`, `PATCH /api/v1/admin/users/{userId}` | `ADMIN_APP` | `A-02` | Quân |
| Admin Users | `POST /api/v1/admin/users/{userId}/lock`, `/unlock`, `/soft-delete`, `/restore`, `/reset-password` | `ADMIN_APP` | `A-02`, `A-03`, `A-04` | Quân (dùng dịch vụ vòng đời của Bảo) |
| Admin Users | `GET /api/v1/admin/users/{userId}/login-history`, `/friends` | `ADMIN_APP` | `A-05`, `A-06` | Quân |
| Admin Logs | `GET /api/v1/admin/login-logs` | `ADMIN_APP` | `A-07` | Quân |
| Admin Analytics | `GET /api/v1/admin/analytics/registrations` & `/monthly` | `ADMIN_APP` | `A-12`, `A-13` | Quân |
| Admin Analytics | `GET /api/v1/admin/analytics/activity` & `/monthly` | `ADMIN_APP` | `A-15`, `A-16` | Quân |
| Admin Analytics | `GET /api/v1/admin/analytics/friend-network` & `/monthly` | `ADMIN_APP` | `A-14` | Quân |

### Đợt 2 — Khung đường dẫn đã đặt trước (hoàn thiện chi tiết theo spec từng cụm)

| Cụm | Đường dẫn chuẩn | Scope | Feature | Phụ trách |
| --- | --- | --- | --- | --- |
| Profile | `GET/PATCH /api/v1/profile`, `POST /api/v1/profile/password`, `POST /api/v1/profile/email-change`, `POST /api/v1/profile/email-change/verify` | `USER_CLIENT` | `U-03`, `U-04` | Bảo |
| Contacts | `GET /api/v1/friends`, `DELETE /api/v1/friends/{friendId}`, `GET /api/v1/users/search`, `GET/POST /api/v1/friend-requests`, `POST /api/v1/friend-requests/{id}/accept\|reject\|cancel`, `GET/POST/DELETE /api/v1/blocks` | `USER_CLIENT` | `U-06`–`U-11` | Bảo |
| Groups | `POST /api/v1/groups`, `PATCH /api/v1/groups/{groupId}`, `POST/DELETE /api/v1/groups/{groupId}/members`, `PATCH /api/v1/groups/{groupId}/members/{userId}/role`, `POST /api/v1/groups/{groupId}/leave` | `USER_CLIENT` | `U-19`–`U-23` | Kiệt |
| E2EE | `POST/GET /api/v1/e2ee/devices`, `POST /api/v1/e2ee/devices/{deviceId}/trust\|revoke`, `GET/PUT /api/v1/e2ee/backups`, `GET/POST /api/v1/groups/{groupId}/epochs`, `GET/POST /api/v1/groups/{groupId}/epochs/{epochId}/envelopes` | `USER_CLIENT` | `U-25` | Bảo & Kiệt |
| Spam | `POST /api/v1/spam-reports`, `GET /api/v1/admin/spam-reports`, `GET /api/v1/admin/spam-reports/{reportId}`, `POST /api/v1/admin/spam-reports/{reportId}/review` | `USER_CLIENT` / `ADMIN_APP` | `U-26`, `A-10`, `A-11` | Bảo |
| Admin Groups | `GET /api/v1/admin/groups`, `GET /api/v1/admin/groups/{groupId}` | `ADMIN_APP` | `A-08`, `A-09` | Kiệt |
| AI Assistant | `POST /api/v1/ai/assist` (`mode: REFINE \| RECOMMEND`, tối đa 50 tin ngữ cảnh, trả đúng 3 phương án) | `USER_CLIENT` | `U-27` | Kiệt |
