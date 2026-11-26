# Feature Spec: Quản lý Người dùng & Vòng đời Tài khoản (`A-SCR-03`)

- **Phụ trách:** Nguyễn Thế Quân (`@Ladenyx`) · *(dùng API/service vòng đời tài khoản phối hợp với Nguyễn Phương Gia Bảo `@baonguyen1776`)*
- **Reviewer:** Nguyễn Phương Gia Bảo (`@baonguyen1776`)
- **Phạm vi:** `A-SCR-03` (`UsersController`), `A-DLG-01` (`EditUserController`), `A-DLG-02` (`ConfirmActionController`) — bao phủ `A-01` đến `A-06`.
- **Dẫn chiếu:**
  - Use Cases: [UC-ADM-USER-MGMT-01..11](../../requirements/use-cases/admin/user-management/UC-ADM-USER-MGMT-01-browse-users.md)
  - Decisions: [DEC-03 Bạn bè & chặn](../../decisions/blocking-and-friendship.md) · [DEC-05 Quyền nhóm](../../decisions/group-permissions-and-e2ee.md) · [DEC-06 Email & mật khẩu](../../decisions/password-reset-and-email.md) · [DEC-07 Vòng đời tài khoản](../../decisions/account-lifecycle.md)
  - Contracts: [rest-conventions.md](../../architecture/contracts/rest-conventions.md) · [websocket-protocol.md](../../architecture/contracts/websocket-protocol.md) · [openapi.yaml](../../architecture/contracts/openapi.yaml) · [admin-users.schema.json](../../architecture/contracts/schemas/admin-users.schema.json)
  - Màn hình & Kiểm kê: [screen.md](../../requirements/screen.md) · [screen-inventory.md](../admin/screen-inventory.md)

---

## 1. Người thao tác & Điều kiện tiên quyết

- **Tác nhân chính:** Admin hệ thống đã đăng nhập với phiên `ADMIN_APP` hợp lệ (`users.role = 'ADMIN'`, `users.status = 'ACTIVE'`).
- **Tác nhân phụ:** Dịch vụ gửi email (`Gmail SMTP` qua Server) khi tạo tài khoản mới (`A-02`) hoặc đặt lại mật khẩu (`A-04`).
- **Nguyên tắc Master–Detail:** Theo mục 1.3 của kế hoạch Version 2, dữ liệu bảng danh sách bên trái (`GET /api/v1/admin/users`) tách biệt hoàn toàn với hồ sơ chi tiết (`GET /api/v1/admin/users/{userId}`), tab lịch sử đăng nhập (`GET /api/v1/admin/users/{userId}/login-history`) và tab danh sách bạn bè (`GET /api/v1/admin/users/{userId}/friends`). Chọn một hàng ở bảng trái chỉ tải lại khung phải; bảng trái vẫn dùng được bình thường.

---

## 2. Phân quyền & Ma trận thao tác theo Trạng thái/Vai trò

| Thao tác | `USER` (`ACTIVE`) | `USER` (`LOCKED`) | `USER` (`DELETED`, còn hạn 30 ngày) | `ADMIN` (chính mình hoặc Admin khác) |
| --- | :---: | :---: | :---: | :---: |
| Xem danh sách / hồ sơ / login history / friends | Có | Có | Có (hiện hạn khôi phục) | Có (chỉ xem) |
| Sửa hồ sơ (`PATCH /admin/users/{id}`) | Có | Có | Không (nút disable + `409`) | Chỉ xem hoặc sửa hồ sơ không đổi vai trò |
| Khóa (`LOCK_USER`) | Có | Không (đã khóa) | Không | **Chặn (`403 FORBIDDEN`)** |
| Mở khóa (`UNLOCK_USER`) | Không | Có | Không | **Chặn (`403 FORBIDDEN`)** |
| Đặt lại mật khẩu (`RESET_PASSWORD`) | Có | Có *(không tự mở khóa)* | Không | **Chặn (`403 FORBIDDEN`)** |
| Xóa mềm 30 ngày (`SOFT_DELETE_USER`) | Có *(nếu không vi phạm Admin nhóm cuối)* | Có *(nếu không vi phạm Admin nhóm cuối)* | Không | **Chặn (`403 FORBIDDEN`)** |
| Khôi phục trước hạn (`RESTORE_USER`) | Không | Không | Có *(trước `deletedAt + 30d`)* | N/A |

- **Bảo mật dữ liệu:** Server không bao giờ trả `password_hash`, mật khẩu tạm, token phiên hay khóa/mã khôi phục E2EE cho Admin App.

---

## 3. Luồng nghiệp vụ chi tiết

### 3.1. Bảng lọc, sắp xếp & phân trang (`A-01`)
- Bộ lọc kết hợp đồng thời (`AND`): `name` (trim, không phân biệt hoa thường), `username` (trim, không phân biệt hoa thường), `status` (`null` | `ACTIVE` | `LOCKED` | `DELETED`). Tài khoản đã purge không còn trong bảng.
- Sắp xếp hỗ trợ `sortBy = fullName | createdAt` và `sortDir = ASC | DESC`, luôn kèm tie-breaker `id ASC`.
- Phân trang `OffsetPage<AdminUserListItemDto>` (`page >= 0`, `size = 25` mặc định, tối đa `100`).
- **Debounce & chống ghi đè:** Thay đổi ô nhập lọc áp dụng debounce 250ms; đổi chip trạng thái hoặc tiêu chí sắp xếp đưa về `page = 0`. Kết quả về muộn của truy vấn cũ (khi đổi bộ lọc nhanh) bị bỏ qua.

### 3.2. Hồ sơ chi tiết & 2 tab phụ (`A-02`, `A-05`, `A-06`)
- Khi chọn `userId` trên bảng trái:
  1. Khung phải hiện skeleton riêng cho thẻ hồ sơ + hai tab (không chặn tương tác bảng trái).
  2. Gọi song song `GET /api/v1/admin/users/{userId}` (nhận `AdminUserDetailDto` + header `ETag`), `GET /api/v1/admin/users/{userId}/login-history?limit=50` và `GET /api/v1/admin/users/{userId}/friends?page=0&size=50`.
  3. **Kiểm tra chống trộn phản hồi (`UC-ADM-USER-MGMT-02` NFR-01 / EF-3):** Nếu Admin đổi sang hàng `userId` khác khi request trước chưa xong, phản hồi của `userId` cũ bị hủy/bỏ qua. Nếu tải hồ sơ thất bại (`5xx`/mạng) hoặc tài khoản vừa bị purge (`404`), **xóa sạch nội dung hồ sơ cũ**, hiển thị thông báo lỗi thử lại (hoặc thông báo tài khoản không còn tồn tại + làm mới bảng trái), tuyệt đối không giữ dữ liệu của user trước dưới tên user mới.
- **Tab Lịch sử đăng nhập (`A-05`):** Cursor `(loggedInAt DESC, id DESC)`, hiển thị giờ `Asia/Ho_Chi_Minh` và ứng dụng (`User Client` / `Admin App`). Có nút **Tải thêm** nếu `hasMore == true`.
- **Tab Danh sách bạn bè (`A-06`):** Chỉ đọc các quan hệ bạn bè trực tiếp hợp lệ trong `friendships` (loại tài khoản đã xóa hoặc đã hủy do chặn theo DEC-03).

### 3.3. Tạo tài khoản mới (`A-02` / `A-DLG-01`)
1. Admin bấm **Thêm người dùng**, điền `username` (6–30 ASCII `[A-Za-z0-9_-]`), `fullName` (1–100 sau trim), `email` (<= 254 đúng định dạng), và các trường tùy chọn (`address`, `dateOfBirth` không ở tương lai theo `Asia/Ho_Chi_Minh`, `gender`).
2. Client khóa nút gửi + đóng dialog khi đang gửi `POST /api/v1/admin/users`.
3. Server kiểm tra quyền, định dạng, tính duy nhất không phân biệt hoa thường của `username`/`email` (bao gồm tài khoản đang `DELETED` chưa purge và xử lý nguyên tử khi tạo đồng thời — lỗi trả `409 USERNAME_ALREADY_EXISTS` / `EMAIL_ALREADY_EXISTS` hoặc `400 VALIDATION_ERROR` kèm `FieldError`).
4. Server lưu `users` (`role = USER`, `status = ACTIVE`, `email_verified_at = NULL`, trigger tự ghi `account_registration_events`) + phát hành mật khẩu tạm ngẫu nhiên 15 phút (`INITIAL_SETUP` trong `password_reset_tokens`) trong cùng transaction, sau đó gửi email kích hoạt qua Gmail SMTP.
5. **Xử lý khi DB đã lưu nhưng SMTP lỗi (`UC-ADM-USER-MGMT-03` EF-3):** Server vẫn trả `201 Created` với `AdminCreateUserResponse (user, activationEmailSent = false, emailDeliveryError = "...")` để Admin App cập nhật bảng người dùng (không bấm gửi lại tạo trùng) và hướng dẫn Admin dùng chức năng **Đặt lại mật khẩu (`A-04`)** để gửi lại email kích hoạt.

### 3.4. Cập nhật hồ sơ (`A-02` / `A-DLG-01`)
1. Client mở `A-DLG-01` ở chế độ sửa (`username` chỉ đọc), điền dữ liệu từ `AdminUserDetailDto` hiện hành và giữ `ETag` của lần đọc đó.
2. Khi lưu, Client gửi `PATCH /api/v1/admin/users/{userId}` kèm header `If-Match: "<etag>"` và `AdminUpdateUserRequest`.
3. Server kiểm tra `If-Match`: nếu hồ sơ đã bị chỉnh sửa bởi chủ tài khoản hoặc một Admin khác, hoặc đã chuyển sang `DELETED`/purge → trả `409 CONFLICT` (`"Hồ sơ đã được cập nhật bởi thao tác khác"`), Client tải lại hồ sơ mới nhất để Admin xem lại (`UC-ADM-USER-MGMT-04` EF-2).
4. Nếu `email` thay đổi (không phân biệt hoa thường): kiểm tra trùng (`409 EMAIL_ALREADY_EXISTS`), lưu email mới và đặt `email_verified_at = NULL` để chủ tài khoản xác minh lại theo DEC-06/DEC-07 (không yêu cầu Admin nhập mật khẩu hiện tại của user).

### 3.5. Khóa / Mở khóa (`A-03` / `A-DLG-02`)
- Mọi thao tác bắt buộc xác nhận qua `A-DLG-02`.
- **Khóa (`POST /admin/users/{userId}/lock`):** Trong một transaction: kiểm tra `role == USER`, chưa `DELETED`; nếu đã `LOCKED` trả trạng thái hiện tại (idempotent); nếu `ACTIVE` chuyển sang `LOCKED`, thu hồi toàn bộ `user_sessions` của `userId`, phát `session.revoked` (`ACCOUNT_LOCKED`) + đóng WSS (`4003`), phát `admin.user.status_changed` và `admin.kpi.updated`.
- **Mở khóa (`POST /admin/users/{userId}/unlock`):** Nếu đang `LOCKED` chuyển sang `ACTIVE` (nếu đã `ACTIVE` trả idempotent; nếu `DELETED` trả `409 CONFLICT`). Không khôi phục phiên cũ, không tự xác minh email, không xóa khóa thử sai.

### 3.6. Xóa mềm 30 ngày & Khôi phục trước hạn (`A-02` / `A-DLG-02`)
- **Xóa mềm (`POST /admin/users/{userId}/soft-delete`):**
  1. Kiểm tra `role == USER`. Nếu đã `DELETED`, trả nguyên trạng thái hiện tại với `deletedAt` gốc (**không** gia hạn thêm 30 ngày).
  2. Kiểm tra điều kiện bảo vệ Admin nhóm cuối (`DEC-05`, `DEC-07`): nếu user là `ADMIN` duy nhất còn hiệu lực (`left_at IS NULL`) trong bất kỳ nhóm nào còn ít nhất 1 thành viên `left_at IS NULL` khác → từ chối bằng `409 LAST_GROUP_ADMIN_REQUIRED`.
  3. Đối với nhóm chỉ còn duy nhất user này, đánh dấu `closed_at = CURRENT_TIMESTAMP` (giữ lịch sử nhóm).
  4. Lưu `status_before_delete = status`, `status = 'DELETED'`, `deleted_at = CURRENT_TIMESTAMP` (`restoreDeadlineAt = deletedAt + 30 days`), thu hồi mọi phiên REST/WSS (`ACCOUNT_DELETED`, close `4003`).
- **Khôi phục (`POST /admin/users/{userId}/restore`):**
  1. Kiểm tra `status == DELETED` và thời điểm máy chủ hiện tại `< deleted_at + INTERVAL '30 days'` (đồng bộ khóa hàng `FOR UPDATE` với tiến trình purge). Nếu đã quá hạn hoặc đã purge → trả `410 RESOURCE_EXPIRED` / `404 NOT_FOUND`.
  2. Khôi phục `status = status_before_delete` (`ACTIVE` hoặc `LOCKED`), đặt `status_before_delete = NULL`, `deleted_at = NULL`. Không tự khôi phục phiên, email verified hoặc tư cách thành viên nhóm đã rời.

### 3.7. Đặt lại mật khẩu qua email (`A-04` / `A-DLG-02`)
1. `A-DLG-02` hiển thị rõ `recipient` (`Email nhận · <email>`).
2. Khi xác nhận, Client gửi `POST /api/v1/admin/users/{userId}/reset-password` với `AdminResetPasswordRequest(expectedEmail)`.
3. Server kiểm tra `role == USER`, `status != DELETED`, đối chiếu `email` hiện tại khớp `expectedEmail` (nếu user/Admin khác vừa đổi email đồng thời → trả `409 CONFLICT` theo `UC-ADM-USER-MGMT-09` EF-2), kiểm tra rate limit (**giãn cách 60 giây, tối đa 5 yêu cầu/giờ/tài khoản** → `429 RATE_LIMITED`).
4. Server phát hành mật khẩu tạm ngẫu nhiên dùng một lần (hiệu lực **15 phút**, `purpose = ADMIN_RESET`) và gửi email qua SMTP.
5. Nếu gửi SMTP lỗi → trả `502 EMAIL_DELIVERY_FAILED`, vô hiệu hóa bản ghi tạm vừa tạo và **giữ nguyên** mật khẩu hiện tại cùng các phiên đang sống của user. Đặt lại mật khẩu không mở khóa `LOCKED` và không thay đổi mã khôi phục E2EE.

---

## 4. Trạng thái UI & Xử lý bất đồng bộ

- **5 trạng thái độc lập cho bảng trái và khung phải:**
  - *Đang tải:* Skeleton bảng trái (hàng bảng) tách biệt hoàn toàn với skeleton khung phải (avatar, thông tin, nút thao tác, bảng tab). Làm mới cùng lựa chọn giữ dữ liệu cũ kèm chỉ báo cập nhật nhỏ.
  - *Có dữ liệu / Rỗng:* Bảng trái hiện `"Không tìm thấy tài khoản"`; khung phải chưa chọn hiện `"Chọn người dùng để xem hồ sơ"`; tab lịch sử/bạn bè hiện placeholder riêng.
  - *Lỗi có thể thử lại / Mất kết nối:* Bảng trái hoặc khung phải hiện banner lỗi + nút **Thử lại** độc lập; lỗi khung phải xóa bỏ dữ liệu của tài khoản chọn trước đó.
- **Ngoài JavaFX Application Thread:** Toàn bộ cuộc gọi REST qua `CompletableFuture`/executor nền, cập nhật UI qua `Platform.runLater` kèm kiểm tra `selectedUserId` và `requestToken` hiện tại.

---

## 5. Các mục còn mở đã chốt

| Mục còn mở | Quyết định áp dụng |
| --- | --- |
| Chống ghi đè đồng thời khi sửa hồ sơ (`UC-ADM-USER-MGMT-04` EF-2) | Dùng header `ETag` trên `GET /admin/users/{userId}` và `If-Match` trên `PATCH /admin/users/{userId}`, trả `409 CONFLICT` nếu lệch. |
| Tạo user thành công nhưng SMTP lỗi (`UC-ADM-USER-MGMT-03` EF-3) | Trả `201 Created` với `AdminCreateUserResponse (activationEmailSent = false, emailDeliveryError)` để không tạo trùng và hướng dẫn dùng `A-04`. |
| Đổi email đồng thời trước khi xác nhận reset password (`UC-ADM-USER-MGMT-09` EF-2) | Gửi `AdminResetPasswordRequest(expectedEmail)`; nếu email DB đã đổi, Server từ chối bằng `409 CONFLICT` để Admin tải lại hồ sơ trước khi gửi. |
| Bảo vệ tài khoản `ADMIN` trên `A-SCR-03` | Admin hệ thống không khóa/xóa/reset password lẫn nhau qua `A-SCR-03`; các nút thao tác khóa/xóa/reset bị vô hiệu hóa với tài khoản `ADMIN` và Server chặn bằng `403`. |

---

## 6. Tiêu chí chấp nhận (Acceptance Criteria)

1. Lọc kết hợp `name` + `username` + `status` trên bảng trái trả đúng kết quả phân trang ổn định; chọn hàng ở bảng trái chỉ tải lại khung phải.
2. Đổi nhanh giữa nhiều tài khoản hoặc mất kết nối khi tải khung phải không làm hiển thị nhầm hồ sơ/tab của người trước dưới tên người sau.
3. Tạo user mới hợp lệ trả `201`, không lộ mật khẩu tạm cho Admin; trường hợp SMTP lỗi vẫn không tạo bản ghi thứ hai khi thử lại.
4. Sửa hồ sơ kèm `If-Match` cũ trả `409 CONFLICT`; sửa email đặt lại trạng thái chưa xác minh email (`emailVerified = false`).
5. Khóa hoặc xóa mềm tài khoản `USER` thu hồi mọi phiên REST + WSS ngay lập tức; xóa mềm Admin nhóm cuối còn thành viên khác bị chặn bằng `409 LAST_GROUP_ADMIN_REQUIRED`.
6. Khôi phục trước hạn 30 ngày đưa tài khoản về đúng `statusBeforeDelete` (`ACTIVE` hoặc `LOCKED`) và giữ nguyên tình trạng thu hồi phiên/nhóm; quá 30 ngày bị từ chối.
7. Reset mật khẩu tôn trọng giãn cách 60s / tối đa 5 lần/giờ; khi SMTP lỗi không làm mất mật khẩu cũ hoặc phiên đang dùng của user.
