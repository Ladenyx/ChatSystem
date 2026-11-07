# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-LOG-01
- **Use Case Name**: Xem nhật ký đăng nhập hệ thống
- **Author / Create by**: ladenyx
- **Date Create**: 2026-11-02
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin tra cứu nhật ký các lượt đăng nhập trên toàn hệ thống theo thứ tự thời gian với thông tin thời điểm đăng nhập, tên đăng nhập (`username`) và họ tên người dùng.
- **Goal**: Giám sát hoạt động đăng nhập ở phạm vi toàn hệ thống theo dòng thời gian.
- **Trigger**: Admin mở chức năng Nhật ký đăng nhập hệ thống hoặc điều hướng sang từ danh sách lượt đăng nhập gần đây trên trang Tổng quan.

## Điều kiện (Conditions)

- **Pre-conditions**: Admin đã có phiên quản trị hợp lệ và quyền Admin hệ thống ([UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md)).
- **Post-conditions**: Danh sách các sự kiện đăng nhập toàn hệ thống được cung cấp theo đúng thứ tự thời gian; dữ liệu nhật ký và tài khoản không bị sửa đổi hay xóa bỏ.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** mở chức năng Nhật ký đăng nhập hệ thống.
2. **Hệ thống** kiểm tra hiệu lực phiên và quyền Admin hệ thống, sau đó truy xuất danh sách các lượt đăng nhập đã ghi nhận theo thứ tự thời gian.
3. **Hệ thống** cung cấp kết quả nhật ký gồm ba thông tin cốt lõi cho mỗi bản ghi: `thời gian đăng nhập` (quy đổi theo `Asia/Ho_Chi_Minh`), `username` và `họ tên`.
4. **Admin** rà soát các sự kiện đăng nhập theo thứ tự thời gian.

### Alternative Flows

**Alternative flow 1: Hệ thống chưa có bản ghi đăng nhập nào**
1. Từ bước #2 của Basic Flow, truy vấn hoàn tất thành công nhưng chưa có sự kiện đăng nhập nào trong nhật ký.
2. **Hệ thống** thông báo chưa có dữ liệu nhật ký đăng nhập.
3. Kết thúc Use Case.

**Alternative flow 2: Bản ghi thuộc về tài khoản đã xóa vĩnh viễn (purge)**
1. Từ bước #3 của Basic Flow, một sự kiện đăng nhập trong quá khứ thuộc về tài khoản đã bị purge khỏi hệ thống theo vòng đời dữ liệu.
2. **Hệ thống** hiển thị nhãn định danh đã ẩn danh/đã xóa theo chính sách tại [DEC-07](../../../../decisions/account-lifecycle.md) và tuyệt đối không gán sự kiện cũ này cho tài khoản mới đăng ký lại cùng `username`.
3. Tiếp tục bước #4 trong Basic Flow.

### Exception Flows

**Exception flow 1: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #2 của Basic Flow, phiên quản trị không còn hợp lệ hoặc thiếu quyền Admin hệ thống.
2. **Hệ thống** từ chối truy vấn nhật ký và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 2: Lỗi kết nối hoặc lỗi truy xuất nhật ký**
1. Từ bước #2 của Basic Flow, máy chủ không phản hồi hoặc truy vấn dữ liệu gặp lỗi.
2. **Hệ thống** thông báo lỗi tải nhật ký, cho phép thử lại và không hiển thị lỗi như trạng thái chưa có lượt đăng nhập.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Nhật ký đăng nhập hệ thống phục vụ tra cứu tập trung toàn hệ thống theo thứ tự thời gian (phân biệt với lịch sử đăng nhập của riêng từng người dùng tại [UC-ADM-USER-10](../user-management/UC-ADM-USER-10-view-user-login-history.md)) và không cho phép Admin sửa hay xóa bản ghi nhật ký.
  - **BR-02**: Mỗi bản ghi nhật ký bắt buộc cung cấp đầy đủ `thời gian đăng nhập`, `username` và `họ tên` (trừ trường hợp định danh đã được ẩn danh sau purge theo [DEC-07](../../../../decisions/account-lifecycle.md)).
  - **BR-03**: Thời điểm đăng nhập được lưu chuẩn hóa theo giờ máy chủ (UTC) và hiển thị theo múi giờ `Asia/Ho_Chi_Minh`; sự kiện đăng nhập không đồng nhất với chỉ số "lượt mở ứng dụng" trong thống kê hoạt động ([Thống kê và thời gian](../../../../decisions/statistics-and-time.md)).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Dữ liệu nhật ký cung cấp cho Admin không được chứa mật khẩu, mã phiên (session token) hoặc dữ liệu xác thực nhạy cảm.
  - **NFR-02**: Quá trình tải dữ liệu phải bảo toàn thứ tự thời gian nhất quán, không làm trùng lặp hoặc bỏ sót bản ghi giữa các lần tải.
