# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-USER-MGMT-04
- **Use Case Name**: Cập nhật hồ sơ người dùng
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-29
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: Dịch vụ gửi email (khi thay đổi địa chỉ email cần xác minh lại)

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin chỉnh sửa các thông tin hồ sơ được phép thay đổi của một người dùng hiện có mà vẫn bảo toàn định danh tài khoản (`Account ID` và `username`).
- **Goal**: Cập nhật chính xác thông tin hồ sơ người dùng một cách nhất quán, không ghi đè mù quáng lên các thay đổi đồng thời.
- **Trigger**: Admin chọn chỉnh sửa hồ sơ từ thông tin chi tiết của người dùng ([UC-ADM-USER-MGMT-02](UC-ADM-USER-MGMT-02-view-user-profile.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin có phiên quản trị hợp lệ và quyền Admin hệ thống; tài khoản người dùng được chọn đang tồn tại và chưa bị xóa mềm (`DELETED`) hoặc xóa vĩnh viễn.
- **Post-conditions**: Các trường hồ sơ hợp lệ được lưu cập nhật đồng bộ trên cùng mã định danh tài khoản; `username`, mật khẩu, vai trò và trạng thái khóa/xóa của tài khoản không bị thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** mở chức năng chỉnh sửa hồ sơ của người dùng đang chọn, điều chỉnh các thông tin (`họ tên`, `địa chỉ`, `ngày sinh`, `giới tính`, `email`) và gửi yêu cầu lưu.
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống, trạng thái tồn tại hiện thời của tài khoản, tính hợp lệ của dữ liệu và phát hiện xung đột cập nhật đồng thời.
3. **Hệ thống** kiểm tra tính duy nhất nếu `email` thay đổi, lưu nhất quán toàn bộ thông tin hồ sơ mới và trả về dữ liệu hồ sơ sau cập nhật.
4. **Hệ thống** thông báo lưu thành công và làm mới thông tin chi tiết người dùng tại [UC-ADM-USER-MGMT-02](UC-ADM-USER-MGMT-02-view-user-profile.md).

### Alternative Flows

**Alternative flow 1: Xóa trống các thông tin tùy chọn hoặc không có thay đổi thực tế**
1. Từ bước #1 của Basic Flow, **Admin** xóa trống `địa chỉ`, `ngày sinh`, `giới tính` hoặc giữ nguyên toàn bộ giá trị như ban đầu.
2. Nếu không có trường nào thay đổi, **Hệ thống** kết thúc thao tác mà không phát sinh ghi dữ liệu dư thừa; nếu có xóa trường tùy chọn, **Hệ thống** ghi nhận trạng thái chưa cung cấp và tiếp tục bước #2 trong Basic Flow.

**Alternative flow 2: Thay đổi địa chỉ `email` của tài khoản**
1. Từ bước #3 của Basic Flow, **Admin** cập nhật sang một địa chỉ `email` mới chưa trùng với bất kỳ tài khoản nào khác.
2. **Hệ thống** lưu địa chỉ `email` mới và đánh dấu email cần được xác minh lại theo quy định tại [DEC-06](../../../../decisions/password-reset-and-email.md) và [DEC-07](../../../../decisions/account-lifecycle.md) (không yêu cầu **Admin** nhập mật khẩu hiện tại của người dùng).
3. Tiếp tục bước #4 trong Basic Flow.

**Alternative flow 3: Admin hủy bỏ chỉnh sửa**
1. Từ bước #1 của Basic Flow, **Admin** hủy thao tác trước khi xác nhận lưu.
2. **Hệ thống** giữ nguyên hồ sơ hiện có và quay về [UC-ADM-USER-MGMT-02](UC-ADM-USER-MGMT-02-view-user-profile.md).

### Exception Flows

**Exception flow 1: Dữ liệu cập nhật sai định dạng hoặc trùng `email`**
1. Từ bước #2 hoặc #3 của Basic Flow, dữ liệu vi phạm ràng buộc độ dài/định dạng hoặc địa chỉ `email` mới đã thuộc về tài khoản khác.
2. **Hệ thống** từ chối cập nhật toàn bộ yêu cầu (không lưu một phần hồ sơ) và thông báo rõ nguyên nhân lỗi để **Admin** chỉnh sửa.
3. Quay lại bước #1 trong Basic Flow.

**Exception flow 2: Xung đột cập nhật đồng thời hoặc tài khoản đã chuyển sang xóa mềm/purge**
1. Từ bước #2 của Basic Flow, **Hệ thống** phát hiện hồ sơ đã bị thay đổi bởi chủ tài khoản / một phiên quản trị khác kể từ lúc mở dữ liệu, hoặc tài khoản đã bị xóa.
2. **Hệ thống** từ chối ghi đè mù quáng, thông báo xung đột và tải lại dữ liệu hồ sơ mới nhất để **Admin** kiểm tra lại.
3. Kết thúc Use Case.

**Exception flow 3: Phiên quản trị hết hiệu lực, thiếu quyền hoặc mất kết nối khi lưu**
1. Từ bước #2 hoặc #3 của Basic Flow, phiên quản trị không còn hợp lệ hoặc mất phản hồi từ máy chủ.
2. **Hệ thống** từ chối thao tác (buộc xác thực lại nếu mất phiên) hoặc báo kết quả chưa xác định và yêu cầu đọc lại hồ sơ trước khi thử lưu lại.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: `Account ID` và `username` là bất biến sau khi tạo tài khoản; chỉ cho phép cập nhật `họ tên`, `địa chỉ`, `ngày sinh`, `giới tính` và `email` theo cùng ràng buộc dữ liệu tại [DEC-07](../../../../decisions/account-lifecycle.md).
  - **BR-02**: Việc cập nhật hồ sơ không được làm thay đổi mật khẩu, quyền hạn hệ thống, trạng thái khóa (`LOCKED`) hay trạng thái xóa mềm (`DELETED`) của tài khoản.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Thao tác ghi hồ sơ phải đảm bảo tính nguyên tử (thành công toàn bộ hoặc hủy bỏ toàn bộ) và kiểm tra tính duy nhất của `email` ngay tại thời điểm ghi để chống tranh chấp đồng thời.
  - **NFR-02**: Không tiết lộ mật khẩu hoặc khóa E2EE của người dùng trong quá trình đọc/ghi hồ sơ hay trong nhật ký lỗi.
