# Feature Spec: Dashboard Phân tích & Thống kê (`A-SCR-07`, `A-SCR-08`, `A-SCR-09`)

- **Phụ trách:** Nguyễn Thế Quân (`@Ladenyx`)
- **Reviewer:** Nguyễn Phương Gia Bảo (`@baonguyen1776`)
- **Phạm vi:** `A-SCR-07` (`A-12`, `A-13` — Đăng ký mới), `A-SCR-08` (`A-15`, `A-16` — Hoạt động người dùng), `A-SCR-09` (`A-14` — Mạng lưới bạn bè) cùng 2 thành phần dùng chung `MonthlyChartController` và `AnalyticsFiltersController`.
- **Dẫn chiếu:**
  - Use Cases: [UC-ADM-ANL-01..05](../../requirements/use-cases/admin/analytics/UC-ADM-ANL-01-browse-new-registrations.md)
  - Decisions: [Thống kê và thời gian](../../decisions/statistics-and-time.md) · [DEC-03 Bạn bè & chặn](../../decisions/blocking-and-friendship.md) · [DEC-07 Vòng đời tài khoản](../../decisions/account-lifecycle.md)
  - Contracts: [rest-conventions.md](../../architecture/contracts/rest-conventions.md) · [openapi.yaml](../../architecture/contracts/openapi.yaml) · [admin-analytics.schema.json](../../architecture/contracts/schemas/admin-analytics.schema.json)
  - Màn hình & Kiểm kê: [screen.md](../../requirements/screen.md) · [screen-inventory.md](../admin/screen-inventory.md)

---

## 1. Người thao tác & Quy tắc chung

- **Tác nhân chính:** Admin hệ thống có phiên `ADMIN_APP` hợp lệ (`role = ADMIN`, `status = ACTIVE`). Toàn bộ màn hình thống kê là **chỉ đọc**.
- **Múi giờ chuẩn (`Asia/Ho_Chi_Minh`):**
  - Mọi bộ lọc ngày `from` – `to` (`YYYY-MM-DD`) và ranh giới tháng/năm đều quy đổi theo `Asia/Ho_Chi_Minh` (`UTC+07:00`).
  - Khoảng truy vấn SQL nửa mở: `[from 00:00:00+07, (to + 1 day) 00:00:00+07)`.
  - Nếu `from > to` → chặn ngay trên UI (`filterError`) và trên Server trả `400 INVALID_DATE_RANGE`.
- **Chuẩn biểu đồ 12 tháng (`AdminMonthlySeriesDto`):**
  - Luôn trả đủ **12 phần tử** `month = 1..12` của `year` đã chọn; tháng không phát sinh dữ liệu có `count = 0`.
  - Kèm `asOfDate` (ngày hiện tại theo `Asia/Ho_Chi_Minh`) và `availableYears` để UI phân biệt tháng tương lai (`future` pseudo-class, tách nét trên `LineChart` của `A-SCR-08`).
  - **Không được** hiển thị lỗi truy vấn thành biểu đồ có 12 tháng bằng `0`.
- **Quyền riêng tư:** Thống kê chỉ đọc metadata (`account_registration_events`, `app_open_events`, `message_send_events`, `friendships`, `blocks`), tuyệt đối không đọc hay giải mã nội dung tin nhắn.

---

## 2. Luồng nghiệp vụ theo từng Dashboard

### 2.1. Dashboard Đăng ký mới (`A-SCR-07` — `A-12`, `A-13`)

#### Biểu đồ 12 tháng (`A-13`)
- Gọi `GET /api/v1/admin/analytics/registrations/monthly?year=<Y>`.
- Nguồn dữ liệu: `account_registration_events` (bao gồm cả tài khoản `USER`, `ADMIN` tạo mới, tài khoản do Admin tạo và Admin khởi tạo qua CLI; không tính `pending_registrations`).
- Tài khoản sau này bị khóa, xóa mềm hay purge vẫn giữ nguyên trong số liệu tháng/năm đã tạo; tài khoản mới dùng lại username/email sau purge tạo `account_key` mới và được tính là lượt đăng ký mới.
- Trên `A-SCR-07`, bấm vào một cột tháng `M` trên biểu đồ `BarChart` sẽ tự động đặt bộ lọc bảng dưới thành ngày đầu tháng `M` đến ngày cuối tháng `M` và gọi lại API bảng chi tiết (bấm lại cùng tháng sẽ khôi phục khoảng ngày trước đó).

#### Bảng chi tiết theo khoảng thời gian (`A-12`)
- Gọi `GET /api/v1/admin/analytics/registrations?from=&to=&name=&sortBy=fullName|createdAt&sortDir=ASC|DESC&page=0&size=25`.
- Lọc `name` theo chuỗi con không phân biệt hoa thường sau trim trên `users.full_name`.
- Bản ghi thuộc tài khoản đã purge (`user_id IS NULL`) vẫn hiển thị `accountKey` và `registeredAt` với nhãn ẩn danh `"Người dùng đã xóa"` cho `username`/`fullName` (nếu chưa nhập từ khóa `name`).
- **Tính nhất quán:** Khi chưa lọc `name` và chọn đúng khoảng ngày của tháng `M` trong năm `Y`, `totalItems` của bảng phải bằng đúng giá trị `count` của tháng `M` trên biểu đồ `A-13`.

### 2.2. Dashboard Hoạt động Người dùng (`A-SCR-08` — `A-15`, `A-16`)

#### Biểu đồ người dùng mở ứng dụng theo tháng (`A-16`)
- Gọi `GET /api/v1/admin/analytics/activity/monthly?year=<Y>`.
- Đếm `COUNT(DISTINCT account_key)` trong `app_open_events` theo từng tháng của năm `Y` (theo `Asia/Ho_Chi_Minh`). **Không** cộng dồn số người duy nhất của từng ngày để suy ra cả tháng.
- Bấm vào một điểm tháng trên `LineChart` tự động đặt khoảng ngày bảng dưới về tháng đó.

#### Bảng chi tiết hoạt động (`A-15`)
- Gọi `GET /api/v1/admin/analytics/activity?from=&to=&name=&metric=APP_OPENS|PEOPLE_CHATTED|GROUPS_CHATTED&metricOp=EQ|LT|GT&metricValue=&sortBy=fullName|createdAt&sortDir=&page=0&size=25`.
- **Điều kiện xuất hiện:** Tài khoản (`account_key`) có ít nhất 1 sự kiện trong khoảng ngày thuộc `app_open_events` **hoặc** `message_send_events`.
- **Công thức 3 chỉ số trong khoảng ngày:**
  1. `appOpens`: số dòng `app_open_events` của `account_key` trong khoảng ngày (mỗi lần khởi chạy User Client xác thực thành công = 1; reconnect/re-login cùng `launchId` không tăng).
  2. `peopleChattedWith`: `COUNT(DISTINCT recipient_key)` trong `message_send_events` với `recipient_key IS NOT NULL` (chỉ nhận tin không tăng; thu hồi/xóa tin hoặc purge người nhận về sau không làm giảm số liệu).
  3. `groupsChattedIn`: `COUNT(DISTINCT group_id)` trong `message_send_events` với `group_id IS NOT NULL` (đóng nhóm về sau không làm giảm số liệu).
- **Trường hợp xuyên mốc tháng:** Người mở app ngày 31/10 rồi giữ phiên sang ngày 01/11 để gửi tin vẫn xuất hiện trong bảng tháng 11 với `appOpens = 0` nhưng không cộng vào điểm tháng 11 trên biểu đồ `A-16`.
- Lọc số lượng `metricOp` + `metricValue` (số nguyên không âm `>= 0`) áp dụng trên tập các tài khoản có phát sinh hoạt động trong khoảng ngày.

### 2.3. Phân tích Mạng lưới Bạn bè (`A-SCR-09` — `A-14`)

#### Biểu đồ lượt chấp nhận kết bạn theo tháng
- Gọi `GET /api/v1/admin/analytics/friend-network/monthly?year=<Y>`.
- Đếm số lượt `friend_requests` có `status = 'ACCEPTED'` theo `resolved_at` trong từng tháng của năm `Y` (theo `Asia/Ho_Chi_Minh`), bao gồm cả lượt chấp nhận khi hai người kết bạn lại sau khi từng hủy.

#### Bảng mạng lưới bạn bè hiện tại & KPI
- Gọi `GET /api/v1/admin/analytics/friend-network?name=&directFriendsOp=EQ|LT|GT&directFriendsValue=&sortBy=fullName|createdAt&sortDir=&page=0&size=25`.
- Trả về `AdminFriendNetworkResponse(currentAccounts, connectedAccounts, page)`:
  - `currentAccounts`: tổng tài khoản chưa xóa (`status IN ('ACTIVE', 'LOCKED')`).
  - `connectedAccounts`: số tài khoản trong tập trên có `directFriends > 0`.
- **Phạm vi hàng & công thức:**
  - Liệt kê các tài khoản `status IN ('ACTIVE', 'LOCKED')` (loại `DELETED` và đã purge).
  - `directFriends` của `U`: số bạn trực tiếp trong `friendships` mà đối phương có `status IN ('ACTIVE', 'LOCKED')`. Tài khoản `LOCKED` vẫn được tính nếu còn quan hệ bạn bè hợp lệ; cặp đã chặn đã bị hủy bạn theo `DEC-03`.
  - `extendedFriends` của `U`: lực lượng tập hợp `F1(U) ∪ F2(U)` loại trừ: (1) chính `U`, (2) trùng lặp, (3) tài khoản `DELETED`/purge, (4) mọi tài khoản đang có quan hệ chặn 1 chiều hoặc 2 chiều trong `blocks` với `U`. Luôn thỏa `extendedFriends >= directFriends`.
- Bộ lọc `directFriendsOp` (`EQ`/`LT`/`GT`) + `directFriendsValue` (`>= 0`) chỉ áp dụng trên cột `directFriends`.

---

## 3. Trạng thái UI & Xử lý phản hồi về muộn

- **Phân tách 2 vùng tải độc lập trên mỗi màn hình:** Khối **Biểu đồ 12 tháng** (`MonthlyChartController`) và khối **Bảng chi tiết** có skeleton loading, trạng thái lỗi + nút **Thử lại** và bộ đếm `requestSeq` độc lập.
  - Khi Admin đổi năm trên biểu đồ, chỉ biểu đồ hiện skeleton/tải lại; bảng dưới vẫn giữ nguyên (trừ khi bấm chọn tháng mới).
  - Khi Admin đổi bộ lọc bảng (debounce 250ms cho ô `name` và `threshold`), chỉ bảng hiện skeleton/cập nhật; biểu đồ giữ nguyên.
  - Đổi bộ lọc hoặc đổi năm nhanh liên tiếp: hủy/bỏ qua mọi phản hồi về muộn của request trước ngoài JavaFX Application Thread.

---

## 4. Các mục còn mở đã chốt

| Mục còn mở | Quyết định áp dụng |
| --- | --- |
| Hợp đồng dữ liệu biểu đồ 12 tháng | Dùng `AdminMonthlySeriesDto(year, asOfDate, availableYears, points[12])` thay vì mảng trần để client biết `asOfDate` và `availableYears` (vẽ `future` trên LineChart `A-SCR-08`). |
| Hợp đồng 2 KPI tổng mạng lưới trên `A-SCR-09` | Dùng `AdminFriendNetworkResponse(currentAccounts, connectedAccounts, page)` để 2 KPI phản ánh toàn hệ thống chứ không bị cắt theo trang `OffsetPage`. |
| Lịch sử `friend_requests` khi tài khoản bị purge | Trong phạm vi schema V1, bảng `friend_requests` xóa cascade khi purge tài khoản; ghi nhận tại `screen-inventory.md` để bổ sung bảng sự kiện bất biến nếu cần bảo toàn sau purge. |

---

## 5. Tiêu chí chấp nhận (Acceptance Criteria)

1. Biểu đồ trên `A-SCR-07`, `A-SCR-08`, `A-SCR-09` luôn trả đủ 12 tháng `T1..T12`, tháng trống có `count = 0`; lỗi API hiển thị banner thử lại chứ không vẽ thành 12 tháng bằng `0`.
2. Trên `A-SCR-07`, chọn tháng `M` trên biểu đồ lọc bảng dưới đúng tháng `M` theo `Asia/Ho_Chi_Minh`, và `totalItems` khi chưa lọc `name` khớp đúng cột tháng `M`.
3. Tài khoản bị xóa mềm hoặc purge vẫn giữ nguyên trong số liệu lịch sử của `A-12`, `A-13`, `A-15`, `A-16`, không gán sự kiện cũ sang tài khoản mới dùng lại `username`.
4. Trên `A-SCR-08`, người chỉ gửi tin trong khoảng ngày (chưa mở lại app trong khoảng đó) vẫn hiện trong bảng `A-15` với `appOpens = 0` và không làm tăng số người mở app tháng đó của `A-16`.
5. Trên `A-SCR-09`, `extendedFriends` luôn `>= directFriends`, loại bỏ chính mình, phần tử trùng, tài khoản `DELETED` và người có quan hệ chặn với `U` nhưng giữ tài khoản `LOCKED` còn bạn bè; bộ lọc `=`, `<`, `>` chỉ lọc theo `directFriends`.
6. Mọi bộ lọc ngày `from > to` hoặc giá trị ngưỡng âm bị chặn ở UI và trả `400` ở Server; phản hồi về muộn khi đổi bộ lọc nhanh không ghi đè kết quả mới nhất.
