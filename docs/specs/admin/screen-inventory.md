# Kiểm kê Màn hình Version 1 — Phân hệ Admin App (Phạm vi Nguyễn Thế Quân)

- **Phụ trách:** Nguyễn Thế Quân (`@Ladenyx`)
- **Review:** Nguyễn Phương Gia Bảo (`@baonguyen1776`)
- **Phạm vi:** `AdminShell`, `A-SCR-01`, `A-SCR-02`, `A-SCR-03`, `A-DLG-01`, `A-DLG-02`, `A-SCR-06`, `A-SCR-07`, `A-SCR-08`, `A-SCR-09` (`A-01`–`A-07`, `A-12`–`A-16`).
- **Tài liệu liên quan:** [Danh mục màn hình](../../requirements/screen.md) · [Kiến trúc desktop](../../architecture/desktop-ui.md) · [Quy ước REST](../../architecture/contracts/rest-conventions.md) · [Giao thức WSS](../../architecture/contracts/websocket-protocol.md) · [OpenAPI](../../architecture/contracts/openapi.yaml) · [Schema SQL](../../../script/schema.sql).

*(Ghi chú ranh giới: `A-SCR-04` Quản lý nhóm do Kiệt nối logic; `A-SCR-05` Kiểm duyệt spam do Bảo nối logic. `A-SCR-02` có thẻ/bảng dẫn sang hai màn hình này và `A-SCR-05` tái sử dụng hộp thoại xác nhận `A-DLG-02` cho thao tác `LOCK_REPORTED_USER`.)*

---

## 1. Tổng hợp nguồn mã nguồn Version 1 đã kiểm kê

| Mã màn hình | FXML (`src/admin/src/main/resources/com/chatsystem/admin/fxml/...`) | Controller | Dữ liệu mẫu V1 | Feature ID / Use Case |
| --- | --- | --- | --- | --- |
| `AdminShell` | `shell.fxml` | `AdminShellController` | — | Khung điều hướng, theme, đăng xuất/thoát |
| `A-SCR-01` | `auth/sign-in.fxml` | `auth.SignInController` | — | `UC-ADM-AUTH-01` |
| `A-SCR-02` | `overview/overview.fxml` | `overview.OverviewController` | `MockData.create()` | `UC-ADM-OVW-01` |
| `A-SCR-03` | `users/users.fxml` | `users.UsersController` | `UserManagementFixtures` | `A-01`–`A-06` · `UC-ADM-USER-MGMT-01..11` |
| `A-DLG-01` | `dialogs/edit-user.fxml` | `dialogs.EditUserController` | `UserManagementFixtures` | `A-02` · `UC-ADM-USER-MGMT-03`, `04` |
| `A-DLG-02` | `dialogs/confirm-action.fxml` | `dialogs.ConfirmActionController` | `UserManagementFixtures` | `A-02`–`A-04`, `A-11` · `UC-ADM-USER-MGMT-05..09` |
| `A-SCR-06` | `logs/login-logs.fxml` | `logs.LoginLogsController` | `MockData.create()` | `A-07` · `UC-ADM-LOG-01` |
| `A-SCR-07` | `analytics/registrations.fxml` (+ `monthly-chart.fxml`, `filters.fxml`) | `analytics.RegistrationsController` (+ `MonthlyChartController`, `AnalyticsFiltersController`) | `MockData.create()` | `A-12`, `A-13` · `UC-ADM-ANL-01`, `02` |
| `A-SCR-08` | `analytics/activity.fxml` (+ `monthly-chart.fxml`, `filters.fxml`) | `analytics.ActivityController` | `MockData.create()` | `A-15`, `A-16` · `UC-ADM-ANL-03`, `04` |
| `A-SCR-09` | `analytics/friend-network.fxml` (+ `monthly-chart.fxml`, `filters.fxml`) | `analytics.FriendNetworkController` | `UserManagementFixtures`, `MockData` | `A-14` · `UC-ADM-ANL-05` |

---

## 2. Kiểm kê chi tiết từng thành phần theo màn hình

### 2.1. `AdminShell` (`shell.fxml` / `AdminShellController`)

| Thành phần (`fx:id` / `#id`) | Loại | Feature / UC | Dữ liệu đọc/ghi | Kênh xử lý | Trạng thái tải / lỗi | Ca nghiệm thu |
| --- | --- | --- | --- | --- | --- | --- |
| `overviewNav`, `managementNav`, `analyticsNav` (`#nav-A-SCR-*`) | Sidebar ToggleButtons (8 mục) | Điều hướng | `AdminViews.PAGES`, `currentViewProperty` | Cục bộ (`ViewNavigator.navigate`) | Hủy request cũ của trang rời đi trong `onLeave()` | Chuyển trang giữ nút chọn đúng, không để phản hồi trang trước ghi đè trang mới |
| `backButton` (`#goBack`) | Button (`Alt + ←`) | Điều hướng | Lịch sử `ViewNavigator` | Cục bộ (`navigator.goBack()`) | Ẩn khi stack rỗng | Quay lại khôi phục route + bộ lọc trước |
| `displayButton` (`#displayOptions`) | Button | Theme | `ThemeManager` (Primer Light/Dark, giảm chuyển động) | Cục bộ (`showPopover`) | — | Đổi theme áp ngay cho shell, dialog, skeleton |
| *(Bổ sung V2)* Nút **Đăng xuất** | Button | `UC-ADM-AUTH-01` | Thu hồi phiên `ADMIN_APP` hiện hành | REST `POST /api/v1/auth/logout` + đóng WSS (`1000`) | Vô hiệu hóa nút khi đang gửi; vẫn reset về `A-SCR-01` nếu phiên đã bị thu hồi | Sau đăng xuất token cũ trả `401`, cache/lịch sử trang bị xóa qua `navigator.reset(SIGN_IN)` |
| Nút `Thoát ứng dụng` (`#exit`) | Button | Vòng đời app | Đóng kết nối WSS và `Platform.exit()` | Cục bộ + dọn WSS | Không treo luồng nền | Đóng cửa sổ dọn thread pool I/O sạch |
| *(Bổ sung V2)* Listener `session.revoked` | Global WSS handler | `DEC-07` | `WsSessionRevokedPayload` (`ADMIN_NEW_LOGIN`, `ACCOUNT_LOCKED`, `ACCOUNT_DELETED`, `PASSWORD_CHANGED`) | WSS `session.revoked` / Close `4001`–`4004` | Dừng reconnect ngay, chuyển `navigator.reset(SIGN_IN, reason)` | Đăng nhập Admin ở máy khác đẩy phiên cũ về `A-SCR-01` kèm thông báo rõ |

### 2.2. `A-SCR-01` — Đăng nhập Quản trị (`auth/sign-in.fxml` / `SignInController`)

| Thành phần (`fx:id`) | Loại | Feature / UC | Dữ liệu đọc/ghi | Kênh xử lý | Trạng thái tải / lỗi | Ca nghiệm thu |
| --- | --- | --- | --- | --- | --- | --- |
| `signInUsername` | TextField | `UC-ADM-AUTH-01` | `LoginRequest.username` (trim) | Cục bộ → REST | Khóa nhập khi đang submit | Nút submit khóa khi rỗng |
| `signInPassword` | PasswordField | `UC-ADM-AUTH-01` | `LoginRequest.password` (giữ nguyên hoa/thường/khoảng trắng; xóa sạch trong `onLeave()`) | Cục bộ → REST | Khóa nhập khi đang submit | Không log mật khẩu; rời trang là xóa `PasswordField` |
| `signInSubmit` | Button (`defaultButton`) | `UC-ADM-AUTH-01` | Ghi `POST /api/v1/auth/login` (`scope = ADMIN_APP`, `launchId = null`), sau đó mở WSS `/ws` | REST + WSS handshake | Hiện trạng thái đang xử lý trên nút để chống bấm lặp | Thành công → lưu phiên `ADMIN_APP`, mở WSS, `navigator.reset(OVERVIEW)`; không tăng `app_open_events` |
| *(Bổ sung V2)* Banner lỗi / thông báo phiên | Label/Box | `UC-ADM-AUTH-01` | `ApiError` (`UNAUTHORIZED`, `FORBIDDEN`, `ACCOUNT_LOCKED`, `ACCOUNT_DELETED`, `RATE_LIMITED`) hoặc lý do bị thu hồi phiên | REST error / `ViewInput` | Phân biệt lỗi sai thông tin (không lộ tồn tại), không đủ quyền `ADMIN`, khóa tạm (kèm thời gian chờ), mất kết nối | Tài khoản `USER` đăng nhập Admin App bị từ chối rõ; mật khẩu sai không tiết lộ tài khoản tồn tại |

### 2.3. `A-SCR-02` — Dashboard Tổng quan (`overview/overview.fxml` / `OverviewController`)

| Thành phần (`fx:id`) | Loại | Feature / UC | Dữ liệu đọc/ghi | Kênh xử lý | Trạng thái tải / lỗi | Ca nghiệm thu |
| --- | --- | --- | --- | --- | --- | --- |
| `totalUsersCard` / `totalUsersValue` | Button KPI | `UC-ADM-OVW-01` | `AdminOverviewDto.totalUsers` | REST `GET /api/v1/admin/overview` + WSS `admin.kpi.updated` | Skeleton thẻ KPI; lỗi không thay bằng `0` | Bấm chuyển `A-SCR-03`; số liệu khớp DB |
| `onlineUsersCard` / `onlineUsersValue` / `onlineIndicator` | Button KPI | `UC-ADM-OVW-01` | `AdminOverviewDto.onlineUsers` (chỉ phiên `USER_CLIENT`) | REST + WSS `admin.kpi.updated` | Skeleton thẻ KPI | Bấm mở `A-SCR-03` mặc định không gắn nhãn lọc giả (`UC-ADM-OVW-01` AF-3) |
| `totalGroupsCard` / `totalGroupsValue` | Button KPI | `UC-ADM-OVW-01` | `AdminOverviewDto.totalGroups` | REST + WSS `admin.kpi.updated` | Skeleton thẻ KPI | Bấm chuyển `A-SCR-04` |
| `pendingSpamCard` / `pendingSpamValue` | Button KPI | `UC-ADM-OVW-01` | `AdminOverviewDto.pendingSpamReports` | REST + WSS `admin.kpi.updated` / `admin.spam_report.*` | Skeleton thẻ KPI | Bấm chuyển `A-SCR-05` |
| `trendTitle`, `monthlyTrend`, `#registrationsLink`, `#activityLink` | LineChart 12 tháng + 2 Hyperlink | `UC-ADM-OVW-01` | `monthlyRegistrations`, `monthlyActiveUsers` (`year`, `asOfDate` theo `Asia/Ho_Chi_Minh`, đủ 12 tháng) | REST `GET /api/v1/admin/overview` | Skeleton khung biểu đồ; lỗi hiển thị banner thử lại riêng | Tooltip hiện đủ 2 chỉ số từng tháng; link mở `A-SCR-07` / `A-SCR-08` |
| `recentReports` (`reportedAccount`, `reporterAccount`, `reportedAt`), `#allReportsLink` | TableView (top 3) + Hyperlink | `UC-ADM-OVW-01` | `AdminOverviewDto.recentSpamReports` (`AdminRecentSpamReportDto`, không có bản rõ tin nhắn) | REST + WSS `admin.spam_report.created` | Skeleton bảng; rỗng hiển thị `"Chưa có báo cáo spam"`; lỗi phân biệt với rỗng | Hiển thị đúng 3 báo cáo mới nhất còn hạn 180 ngày; link mở `A-SCR-05` |
| `recentLogins` (`loginUsername`, `loginFullName`, `loggedInAt`), `#allLoginsLink` | TableView (top 3) + Hyperlink | `UC-ADM-OVW-01` | `AdminOverviewDto.recentLogins` | REST | Skeleton bảng; rỗng hiển thị `"Chưa có lượt đăng nhập"` | Hiển thị thời gian `Asia/Ho_Chi_Minh`; tài khoản đã purge hiện nhãn đã xóa; link mở `A-SCR-06` |

### 2.4. `A-SCR-03` — Quản lý Người dùng (`users/users.fxml` / `UsersController`)

| Thành phần (`fx:id`) | Loại | Feature / UC | Dữ liệu đọc/ghi | Kênh xử lý | Trạng thái tải / lỗi | Ca nghiệm thu |
| --- | --- | --- | --- | --- | --- | --- |
| `#createUser` | Button | `A-02` / `UC-ADM-USER-MGMT-03` | Mở `A-DLG-01` (`ViewInput.NONE`) | Cục bộ → Modal | — | Tạo xong làm mới bảng trái + chọn user mới |
| `nameFilter`, `usernameFilter` | TextField (debounce 250ms) | `A-01` / `UC-ADM-USER-MGMT-01` | Query `name`, `username` | REST `GET /api/v1/admin/users` | Skeleton bảng trái; hủy request cũ khi gõ tiếp | Kết hợp đồng thời `name`, `username`, `status`; trim không phân biệt hoa thường |
| `statusFilters` (`allUsers`, `activeUsers`, `lockedUsers`, `deletedUsers`) | ToggleButton chips | `A-01` / `UC-ADM-USER-MGMT-01` | Query `status` (`null`, `ACTIVE`, `LOCKED`, `DELETED`) | REST `GET /api/v1/admin/users` | Giữ bảng cũ kèm chỉ báo refresh khi đang tải lại | Luôn có đúng 1 chip được chọn; đổi chip đưa `page=0` |
| `clearFilters` | Hyperlink | `A-01` | Đặt lại bộ lọc về mặc định | Cục bộ + REST | Ẩn khi không có bộ lọc | Xóa sạch bộ lọc và tải lại trang 0 |
| `userTable` (`userIdentity`, `userStatus`, `userCreated`), `userCount`, *(bổ sung V2: thanh phân trang)* | TableView + Pagination | `A-01` / `UC-ADM-USER-MGMT-01` | `OffsetPage<AdminUserListItemDto>` (`sortBy=fullName\|createdAt`, `sortDir`, `page`, `size`) | REST `GET /api/v1/admin/users` + WSS `admin.user.status_changed` | Skeleton bảng trái; tách rỗng (`"Không tìm thấy tài khoản"`) khỏi lỗi thử lại | Chọn hàng chỉ tải lại khung phải; giữ bảng trái tương tác bình thường |
| `unselectedProfile` / `userInspector` (`profileName`, `profileHandle`, `profileIdentity`, `profileStatus`, `deletionNotice`, `profileEmail`, `profileAddress`, `profileBirthDate`, `profileGender`) | SplitPane phải — Inspector | `A-02` / `UC-ADM-USER-MGMT-02` | `AdminUserDetailDto` + header `ETag` | REST `GET /api/v1/admin/users/{userId}` | Skeleton riêng khung phải; đổi hàng nhanh bỏ qua phản hồi của `userId` cũ | Khi `DELETED` hiện mốc xóa + hạn khôi phục (+30 ngày, `Asia/Ho_Chi_Minh`); lỗi không giữ dữ liệu của user trước dưới tên user mới (`UC-ADM-USER-MGMT-02` EF-3) |
| `editUser` | Button | `A-02` / `UC-ADM-USER-MGMT-04` | Mở `A-DLG-01` (`EditUser(userId)`) | Cục bộ → Modal | Vô hiệu hóa khi `status == DELETED` | Lưu xong cập nhật khung phải + dòng tương ứng bảng trái |
| `toggleLock` | Button | `A-03` / `UC-ADM-USER-MGMT-07`, `08` | Mở `A-DLG-02` (`LOCK_USER` / `UNLOCK_USER`) | Cục bộ → Modal → REST | Vô hiệu hóa khi `DELETED` hoặc `role == ADMIN` | Khóa thu hồi toàn bộ phiên user ngay lập tức |
| `resetPassword` | Button | `A-04` / `UC-ADM-USER-MGMT-09` | Mở `A-DLG-02` (`RESET_PASSWORD`) | Cục bộ → Modal → REST | Vô hiệu hóa khi `DELETED` hoặc `role == ADMIN` | Hiển thị email nhận trong dialog xác nhận; không bao giờ lộ mật khẩu tạm |
| `deleteOrRestore` | Button | `A-02` / `UC-ADM-USER-MGMT-05`, `06` | Mở `A-DLG-02` (`SOFT_DELETE_USER` / `RESTORE_USER`) | Cục bộ → Modal → REST | Vô hiệu hóa khi `role == ADMIN` | Xóa mềm bị chặn nếu là Admin nhóm cuối cùng còn thành viên (`LAST_GROUP_ADMIN_REQUIRED`) |
| Tab `Lịch sử đăng nhập` (`loginHistory`, `#systemLoginLogs`) | Tab + TableView + Hyperlink | `A-05` / `UC-ADM-USER-MGMT-10` | `CursorPage<AdminLoginHistoryItemDto>` | REST `GET /api/v1/admin/users/{userId}/login-history` | Skeleton riêng trong tab; hỗ trợ "Tải thêm" nếu `hasMore` | Gắn theo `userId` bất biến; đổi user nhanh không trộn dữ liệu; link mở `A-SCR-06` |
| Tab `Danh sách bạn bè` (`friends`, `friendCount`) | Tab + TableView | `A-06` / `UC-ADM-USER-MGMT-11` | `OffsetPage<AdminUserFriendDto>` | REST `GET /api/v1/admin/users/{userId}/friends` | Skeleton riêng trong tab | Chỉ hiện bạn bè trực tiếp đang hiệu lực (không gồm chặn, lời mời, xóa mềm) |

### 2.5. `A-DLG-01` — Thêm / Cập nhật Người dùng (`dialogs/edit-user.fxml` / `EditUserController`)

| Thành phần (`fx:id`) | Loại | Feature / UC | Dữ liệu đọc/ghi | Kênh xử lý | Trạng thái tải / lỗi | Ca nghiệm thu |
| --- | --- | --- | --- | --- | --- | --- |
| `username` | TextField | `A-02` | `AdminCreateUserRequest.username` (chỉ đọc khi sửa) | REST `POST /api/v1/admin/users` | Hiển thị lỗi trường từ `ApiError.details` | 6–30 ký tự ASCII `[A-Za-z0-9_-]`; trùng trả `409 USERNAME_ALREADY_EXISTS` |
| `fullName`, `email`, `address`, `birthDate`, `gender` | Inputs | `A-02` | `AdminCreateUserRequest` / `AdminUpdateUserRequest` | REST `POST` / `PATCH` (kèm `If-Match` khi sửa) | Lỗi từng trường inline; ngày sinh `dd/MM/uuuu` không ở tương lai | `gender` map `"Chưa cung cấp"` ↔ `null`, `"Nam"` ↔ `MALE`, `"Nữ"` ↔ `FEMALE`, `"Khác"` ↔ `OTHER` |
| Nút `Tạo tài khoản` / `Lưu hồ sơ` (`UserDialogButtons`) | Dialog Button | `A-02` | Ghi tạo mới (`201 AdminCreateUserResponse`) hoặc cập nhật (`200 AdminUserDetailDto`) | REST | Hiện trạng thái đang xử lý trên nút, khóa đóng nhầm; khi `activationEmailSent == false` hiện cảnh báo rõ tài khoản đã tạo nhưng email chưa gửi | Xung đột đồng thời khi sửa trả `409 CONFLICT` và tải lại hồ sơ mới nhất |

### 2.6. `A-DLG-02` — Xác nhận Thao tác Quản trị (`dialogs/confirm-action.fxml` / `ConfirmActionController`)

| Thành phần (`fx:id`) | Loại | Feature / UC | Dữ liệu đọc/ghi | Kênh xử lý | Trạng thái tải / lỗi | Ca nghiệm thu |
| --- | --- | --- | --- | --- | --- | --- |
| `confirmationTitle`, `account`, `accountId`, `recipient`, `consequences`, `preservationNotice` | Labels | `A-02`, `A-03`, `A-04`, `A-11` | `AdminViewInputs.ConfirmAction(action, userId)` + hồ sơ hiện tại | Cục bộ (hiển thị) | Nếu `user == null` khóa nút xác nhận | `RESET_PASSWORD` hiện `recipient` (`Email nhận · ...`); `LOCK_REPORTED_USER` hiện `preservationNotice` |
| Nút xác nhận (`UserDialogButtons`) | Dialog Button | `A-02`, `A-03`, `A-04`, `A-11` | Gọi endpoint tương ứng: `/lock`, `/unlock`, `/soft-delete`, `/restore`, `/reset-password` (kèm `AdminResetPasswordRequest.expectedEmail`) | REST `POST` | Hiện trạng thái đang xử lý trên nút; lỗi hiển thị banner ngay trong dialog (`LAST_GROUP_ADMIN_REQUIRED`, `RATE_LIMITED`, `CONFLICT`, `EMAIL_DELIVERY_FAILED`) | Hủy dialog giữ nguyên trạng thái; thành công trả `OK_DONE` và đồng bộ bảng + inspector |

### 2.7. `A-SCR-06` — Nhật ký Đăng nhập Hệ thống (`logs/login-logs.fxml` / `LoginLogsController`)

| Thành phần (`fx:id`) | Loại | Feature / UC | Dữ liệu đọc/ghi | Kênh xử lý | Trạng thái tải / lỗi | Ca nghiệm thu |
| --- | --- | --- | --- | --- | --- | --- |
| `loginCount`, `loginEvents` (`loggedInAt`, `username`, `fullName`), *(bổ sung V2: nút "Tải thêm" / cuộn cursor)* | TableView chỉ đọc | `A-07` / `UC-ADM-LOG-01` | `CursorPage<AdminLoginHistoryItemDto>` | REST `GET /api/v1/admin/login-logs?before=&limit=50` | Skeleton bảng; giữ đường phân tách ngày (`day-start`) theo `Asia/Ho_Chi_Minh`; tách rỗng khỏi lỗi | Không trùng/sót khi tải trang tiếp; tài khoản đã purge hiện nhãn ẩn danh, không gắn sang tài khoản mới cùng `username` |

### 2.8. `A-SCR-07`, `A-SCR-08`, `A-SCR-09` — Cụm Dashboard Thống kê (`analytics/*.fxml`)

| Thành phần dùng chung / riêng | Loại | Feature / UC | Dữ liệu đọc/ghi | Kênh xử lý | Trạng thái tải / lỗi | Ca nghiệm thu |
| --- | --- | --- | --- | --- | --- | --- |
| `MonthlyChartController` (`chartYear`, `primaryValue`, `monthsValue`, `chartUnit`, `monthHint`, `monthlyChart`) | Biểu đồ 12 tháng (`BarChart` cho `A-SCR-07`/`A-SCR-09`, `LineChart` cho `A-SCR-08`) | `A-13`, `A-16`, `A-14` / `UC-ADM-ANL-02`, `04` | `AdminMonthlySeriesDto` (`year`, `asOfDate`, `availableYears`, 12 `MonthlyCountPointDto`) | REST `GET /api/v1/admin/analytics/{registrations\|activity\|friend-network}/monthly?year=` | Skeleton riêng cho khối biểu đồ; lỗi không được vẽ thành 12 cột/điểm `0`; đổi năm nhanh bỏ qua phản hồi cũ | Đủ 12 tháng `T1..T12`, tháng trống = `0`; tháng tương lai so với `asOfDate` gắn pseudo-class `future` và tách nét ở `LineChart`; bấm cột/điểm tháng trên `A-SCR-07`/`A-SCR-08` cập nhật khoảng ngày bảng dưới |
| `AnalyticsFiltersController` (`fromDate`, `toDate`, `nameFilter`, `accountSort`, `activityMetric`, `comparison` `=`/`<`/`>`, `threshold`, `#clearAnalyticsFilters`, `filterError`) | Thanh bộ lọc dùng chung | `A-12`, `A-14`, `A-15` | Query `from`, `to`, `name`, `sortBy`, `sortDir`, `metric`, `metricOp`, `metricValue` / `directFriendsOp`, `directFriendsValue` | Cục bộ (validate) → REST | Chặn gửi request khi `from > to`; hiện `filterError` inline; bỏ qua phản hồi về muộn | Chọn `Bỏ bộ lọc` khôi phục khoảng mặc định và tải lại; lọc số chỉ chấp nhận số nguyên không âm |
| `A-SCR-07` `registrationRows` (`accountId`, `fullName`, `email`, `status`, `createdAt`) | TableView + Pagination | `A-12` / `UC-ADM-ANL-01` | `OffsetPage<AdminRegistrationRowDto>` | REST `GET /api/v1/admin/analytics/registrations` | Skeleton riêng bảng; lỗi thử lại | Bao gồm `USER` & `ADMIN` đã tạo (kể cả đã xóa mềm/purge), không tính đăng ký tạm; khớp tổng cột tháng khi chưa lọc tên |
| `A-SCR-08` `activityRows` (`accountId`, `fullName`, `createdAt`, `appOpens`, `peopleChattedWith`, `groupsChattedIn`) | TableView + Pagination | `A-15` / `UC-ADM-ANL-03` | `OffsetPage<AdminActivityRowDto>` | REST `GET /api/v1/admin/analytics/activity` | Skeleton riêng bảng; lỗi thử lại | Chỉ hiện tài khoản có `appOpens > 0` hoặc gửi tin được chấp nhận trong khoảng ngày; `appOpens` có thể `= 0` nếu mở app tháng trước rồi gửi tin tháng này |
| `A-SCR-09` `currentAccounts`, `connectedAccounts`, `networkRows` (`accountId`, `fullName`, `createdAt`, `directFriends`, `extendedFriends` kèm `ProgressBar`) | KPI inline + TableView + Pagination | `A-14` / `UC-ADM-ANL-05` | `AdminFriendNetworkResponse` (`currentAccounts`, `connectedAccounts`, `OffsetPage<AdminFriendNetworkRowDto>`) | REST `GET /api/v1/admin/analytics/friend-network` | Skeleton riêng KPI + bảng; lỗi thử lại | Chỉ gồm tài khoản `ACTIVE`/`LOCKED` (loại `DELETED`/purge); `extendedFriends >= directFriends`; lọc `=`/`<`/`>` chỉ áp dụng trên `directFriends` |

---

## 3. Khoảng trống UI Version 1 cần bổ sung khi nối logic (Giai đoạn 2–3)

1. **`shell.fxml` / `AdminShellController`:** Bổ sung nút **Đăng xuất** riêng bên cạnh **Thoát ứng dụng**, và gắn listener thu hồi phiên WSS (`session.revoked` / close code `4001`–`4004`).
2. **`auth/sign-in.fxml` / `SignInController`:** Bổ sung vùng thông báo lỗi / lý do phiên bị thu hồi (truyền qua `ViewInput` khi reset từ shell) và trạng thái bận trên nút `signInSubmit`.
3. **Phân trang & tải thêm:** `A-SCR-03`, `A-SCR-07`, `A-SCR-08`, `A-SCR-09` cần thanh phân trang `OffsetPage` (trang trước/sau, tổng số bản ghi); `A-SCR-06` và tab `Lịch sử đăng nhập` của `A-SCR-03` cần nút **Tải thêm** theo `nextCursor`.
4. **Skeleton loading & 5 trạng thái (`desktop-ui` — mục 2.2–2.3):** Thêm skeleton độc lập cho thẻ KPI, bảng, biểu đồ và khung inspector phải; thêm trạng thái lỗi có nút **Thử lại** và trạng thái mất kết nối.
5. **Dialog phản hồi server (`A-DLG-01`, `A-DLG-02`):** Thêm nhãn lỗi từng trường / lỗi chung từ `ApiError` và vô hiệu hóa nút gửi trong lúc gọi REST.

---

## 4. Đối chiếu nhu cầu dữ liệu cụm Admin với `script/schema.sql`

| Nghiệp vụ / Màn hình | Bảng & Index hiện có trong `script/schema.sql` | Đánh giá & kiến nghị DDL |
| --- | --- | --- |
| `A-SCR-01` Đăng nhập Admin & 1 phiên duy nhất | `users`, `user_sessions` (có `user_sessions_one_live_admin_uq`), `login_logs` | **Đủ cho phiên.** Cần bổ sung bảng/cơ chế lưu bộ đếm thử sai đăng nhập bền vững (`auth_login_throttles`) để khóa tạm không bị đặt lại khi khởi động lại Server (`DEC-07`). |
| `A-SCR-02` Tổng quan KPI & bảng gần đây | `users`, `conversations`, `spam_reports`, `login_logs`, `account_registration_events`, `app_open_events` + in-memory WSS presence | **Đủ.** Chú ý `pendingSpamReports` và `recentSpamReports` phải lọc thêm điều kiện hạn 180 ngày (`received_at + INTERVAL '4320 hours' > CURRENT_TIMESTAMP`) theo `DEC-10`. |
| `A-SCR-03` Quản lý người dùng (`A-01`–`A-06`) | `users` (`users_username_uq`, `users_email_uq`, `users_name_trgm_idx`), `login_logs` (`login_logs_user_id_login_at_id_idx`), `friendships`, `password_reset_tokens` | **Đủ cốt lõi.** Kiến nghị bổ sung trong PR DDL Giai đoạn 2: (1) index `GIN (lower(username) gin_trgm_ops)` cho bộ lọc `username` trên `A-01`; (2) tùy chọn thêm cột `version BIGINT NOT NULL DEFAULT 1` / `updated_at TIMESTAMPTZ` trên `users` ngoài giải pháp `ETag`/`If-Match` chạy được ngay trên schema V1. |
| `A-SCR-06` Nhật ký đăng nhập (`A-07`) | `login_logs` (`login_logs_login_at_id_idx` trên `(login_at DESC, id DESC)`, `user_id ON DELETE SET NULL`) | **Đủ 100%** cho cursor pagination ổn định và ẩn danh sau purge. |
| `A-SCR-07` Đăng ký mới (`A-12`, `A-13`) | `app_open_accounts`, `account_registration_events` (`account_registration_events_time_key_idx`), trigger `users_record_registration` | **Đủ 100%**, bảo toàn sau purge và đếm cả `USER`/`ADMIN`. |
| `A-SCR-08` Hoạt động (`A-15`, `A-16`) | `app_open_events` (`opened_at, account_key` và `account_key, opened_at`), `message_send_events` (`sender_key, accepted_at` và `accepted_at, sender_key`), `record_app_open`, `record_message_send` | **Đủ 100%**, bảo toàn đích chat sau purge/đóng nhóm và chống đếm trùng retry. |
| `A-SCR-09` Mạng lưới bạn bè (`A-14`) | `friendships` (`PK (user_low_id, user_high_id)`, `friendships_user_high_idx`), `blocks`, `friend_requests` (`status`, `resolved_at`) | **Đủ cho bảng mạng lưới hiện tại.** Kiến nghị bổ sung partial index `friend_requests (resolved_at) WHERE status = 'ACCEPTED'` cho biểu đồ `A-SCR-09`; đồng thời ghi nhận `friend_requests` đang dùng `ON DELETE CASCADE` khi purge tài khoản (nếu cần bảo toàn lịch sử lượt chấp nhận sau purge như `A-13`/`A-16`, cần tách bảng sự kiện bất biến ở PR DDL sau). |
