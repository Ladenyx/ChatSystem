# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-USER-MGMT-10
- **Use Case Name**: Xem lịch sử đăng nhập của một người dùng
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-29
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin tra cứu danh sách các lượt đăng nhập gắn với một tài khoản người dùng cụ thể theo thứ tự thời gian.
- **Goal**: Kiểm tra lịch sử truy cập riêng của tài khoản đang xem xét mà không bị lẫn với nhật ký đăng nhập của các tài khoản khác trong hệ thống.
- **Trigger**: Admin chọn xem lịch sử đăng nhập từ thông tin chi tiết của một người dùng ([UC-ADM-USER-MGMT-02](UC-ADM-USER-MGMT-02-view-user-profile.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin có phiên quản trị hợp lệ và quyền Admin hệ thống; đã xác định mã định danh của một tài khoản người dùng còn trong phạm vi lưu giữ hồ sơ.
- **Post-conditions**: Danh sách các bản ghi đăng nhập thuộc đúng tài khoản được cung cấp theo thứ tự thời gian; không phát sinh bản ghi đăng nhập mới và không làm thay đổi dữ liệu nhật ký hay hồ sơ tài khoản.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** yêu cầu xem lịch sử đăng nhập của tài khoản người dùng đang chọn.
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống và sự tồn tại của hồ sơ tài khoản theo mã định danh.
3. **Hệ thống** truy xuất các bản ghi đăng nhập gắn với đúng mã định danh tài khoản đó và sắp xếp theo thứ tự thời gian.
4. **Hệ thống** cung cấp danh sách lịch sử đăng nhập kèm mốc thời gian quy đổi theo múi giờ `Asia/Ho_Chi_Minh`.

### Alternative Flows

**Alternative flow 1: Tài khoản chưa có bản ghi đăng nhập nào**
1. Từ bước #3 của Basic Flow, truy vấn hoàn tất thành công nhưng tài khoản chưa phát sinh lượt đăng nhập nào được lưu nhận.
2. **Hệ thống** thông báo tài khoản chưa có lịch sử đăng nhập (phân biệt với lỗi truy vấn).
3. Kết thúc Use Case.

**Alternative flow 2: Admin chuyển sang xem nhật ký đăng nhập toàn hệ thống**
1. Từ bước #4 của Basic Flow, **Admin** muốn đối chiếu các sự kiện đăng nhập trên phạm vi toàn hệ thống thay vì riêng một người dùng.
2. **Admin** chuyển sang thực hiện [UC-ADM-LOG-01](../login-logs/UC-ADM-LOG-01-view-system-login-log.md).

### Exception Flows

**Exception flow 1: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #2 của Basic Flow, phiên quản trị không còn hợp lệ hoặc thiếu quyền Admin hệ thống.
2. **Hệ thống** từ chối truy vấn và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 2: Tài khoản đã bị xóa vĩnh viễn (purge)**
1. Từ bước #2 của Basic Flow, hồ sơ gốc của tài khoản đã bị purge.
2. **Hệ thống** thông báo hồ sơ không còn tồn tại và không dùng `username` để ghép nhầm nhật ký cũ sang một tài khoản mới dùng lại cùng `username`.
3. Kết thúc Use Case.

**Exception flow 3: Lỗi kết nối hoặc lỗi truy vấn dữ liệu**
1. Từ bước #2 hoặc #3 của Basic Flow, quá trình truy xuất lịch sử đăng nhập thất bại.
2. **Hệ thống** thông báo lỗi truy xuất dữ liệu, cho phép thử lại và không trình bày lỗi như lịch sử trống.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Lịch sử đăng nhập luôn được liên kết theo mã định danh tài khoản bất biến (`Account ID`), không liên kết đơn thuần theo chuỗi `username` có thể được tái sử dụng sau purge ([DEC-07](../../../../decisions/account-lifecycle.md)).
  - **BR-02**: Nghiệp vụ này chỉ phạm vi hóa cho 01 người dùng đang chọn (khác với nhật ký toàn hệ thống tại [UC-ADM-LOG-01](../login-logs/UC-ADM-LOG-01-view-system-login-log.md)) và không đồng nhất với chỉ số "lượt mở ứng dụng" trong báo cáo thống kê hoạt động ([Thống kê và thời gian](../../../../decisions/statistics-and-time.md)).
  - **BR-03**: Thời điểm sự kiện được lưu chuẩn hóa theo giờ máy chủ (UTC) và quy đổi sang múi giờ `Asia/Ho_Chi_Minh` khi cung cấp cho Admin.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Dữ liệu lịch sử đăng nhập không được chứa mật khẩu, mã phiên (session token) hay thông tin bí mật xác thực.
  - **NFR-02**: Khi **Admin** đổi nhanh sang tài khoản người dùng khác, phản hồi lịch sử đến muộn của tài khoản trước không được hiển thị nhầm cho tài khoản mới.
