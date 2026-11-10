# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-USER-MGMT-03
- **Use Case Name**: Tạo tài khoản người dùng
- **Author / Create by**: ladenyx
- **Date Create**: 2026-11-04
- **Last Updated by / Date**: ladenyx / 2026-11-08
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: Dịch vụ gửi email (Email Service)

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin khởi tạo mới một tài khoản người dùng thông qua việc nhập thông tin hồ sơ; hệ thống tự động sinh mật khẩu tạm và gửi qua email để chủ tài khoản xác minh và tự đặt mật khẩu riêng.
- **Goal**: Tạo thành công tài khoản người dùng mới không trùng định danh và chuyển phát thông tin kích hoạt tới hộp thư của người dùng.
- **Trigger**: Admin chọn thực hiện thêm người dùng mới từ chức năng Quản lý người dùng ([UC-ADM-USER-MGMT-01](UC-ADM-USER-MGMT-01-browse-users.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin có phiên quản trị hợp lệ và quyền Admin hệ thống; dịch vụ gửi email đã được cấu hình trên máy chủ.
- **Post-conditions**: Tài khoản người dùng mới được lưu trữ với vai trò `USER`, gắn trạng thái cần xác minh email và đặt mật khẩu riêng trước khi sử dụng chức năng chat; yêu cầu gửi email chứa mật khẩu tạm đã được dịch vụ gửi email tiếp nhận.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** cung cấp các thông tin bắt buộc (`username`, `họ tên`, `email`), các thông tin tùy chọn (`địa chỉ`, `ngày sinh`, `giới tính`) và gửi yêu cầu tạo tài khoản.
2. **Hệ thống** kiểm tra phiên, quyền Admin hệ thống, định dạng các trường dữ liệu và tính duy nhất của `username` cùng `email` tại thời điểm ghi.
3. **Hệ thống** sinh mật khẩu tạm ngẫu nhiên dùng một lần, tạo bản ghi tài khoản `USER` kèm thông tin đối chiếu mật khẩu tạm trong một giao dịch nhất quán.
4. **Hệ thống** yêu cầu **Dịch vụ gửi email** chuyển phát thông tin kích hoạt và mật khẩu tạm tới địa chỉ `email` của tài khoản vừa tạo (không hiển thị mật khẩu tạm cho **Admin**).
5. **Hệ thống** thông báo tạo tài khoản và gửi email kích hoạt thành công, đồng thời cập nhật danh sách người dùng.

### Alternative Flows

**Alternative flow 1: Bỏ trống các trường hồ sơ tùy chọn**
1. Từ bước #1 của Basic Flow, **Admin** không nhập `địa chỉ`, `ngày sinh` hoặc `giới tính`.
2. **Hệ thống** ghi nhận các trường này ở trạng thái chưa cung cấp mà không tự gán giá trị mặc định, sau đó tiếp tục bước #2 trong Basic Flow.

**Alternative flow 2: Admin hủy tạo tài khoản trước khi gửi yêu cầu**
1. Từ bước #1 của Basic Flow, **Admin** chủ động hủy bỏ thao tác thêm mới.
2. **Hệ thống** không lưu dữ liệu và quay về danh sách tại [UC-ADM-USER-MGMT-01](UC-ADM-USER-MGMT-01-browse-users.md).

### Exception Flows

**Exception flow 1: Dữ liệu đầu vào thiếu hoặc sai định dạng**
1. Từ bước #2 của Basic Flow, một hoặc nhiều trường vi phạm quy tắc định dạng, độ dài hoặc ngày sinh nằm ở tương lai.
2. **Hệ thống** từ chối tạo tài khoản và trả về chi tiết các trường không hợp lệ để **Admin** chỉnh sửa.
3. Quay lại bước #1 trong Basic Flow.

**Exception flow 2: Trùng `username` hoặc `email`**
1. Từ bước #2 hoặc #3 của Basic Flow, `username` hoặc `email` đã thuộc về một tài khoản hiện có (bao gồm tài khoản đang xóa mềm chưa purge hoặc yêu cầu tạo đồng thời).
2. **Hệ thống** từ chối yêu cầu tạo trùng, không sinh hay gửi mật khẩu tạm tới tài khoản đang tồn tại và thông báo trường bị trùng.
3. Quay lại bước #1 trong Basic Flow.

**Exception flow 3: Tạo tài khoản thành công nhưng gửi email thất bại**
1. Từ bước #4 của Basic Flow, bản ghi tài khoản đã được lưu nhưng **Dịch vụ gửi email** từ chối hoặc gặp sự cố kết nối.
2. **Hệ thống** thông báo rõ tài khoản đã được tạo nhưng email kích hoạt chưa gửi được (không báo kích hoạt hoàn tất và không tạo bản ghi tài khoản thứ hai) để **Admin** có thể gửi lại qua nghiệp vụ đặt lại mật khẩu ([UC-ADM-USER-MGMT-09](UC-ADM-USER-MGMT-09-reset-user-password.md)).
3. Kết thúc Use Case.

**Exception flow 4: Phiên quản trị hết hiệu lực, thiếu quyền hoặc mất kết nối trước khi xác nhận kết quả**
1. Từ bước #2 hoặc #3 của Basic Flow, phiên quản trị không hợp lệ hoặc mất kết nối với máy chủ.
2. **Hệ thống** từ chối thao tác (buộc xác thực lại nếu mất phiên) hoặc báo trạng thái chưa xác định để **Admin** tra cứu đối chiếu lại danh sách trước khi gửi lại yêu cầu nhằm tránh tạo trùng.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Nghiệp vụ này chỉ tạo tài khoản mang vai trò `USER`; không cấp quyền Admin hệ thống qua chức năng quản lý người dùng.
  - **BR-02**: Quy tắc trường theo [DEC-07](../../../../decisions/account-lifecycle.md): `username` cố định 3–30 ký tự ASCII chữ/số/`_`; `họ tên` bắt buộc (1–100 ký tự sau trim); `email` bắt buộc (tối đa 254 ký tự, đúng định dạng); `địa chỉ` tùy chọn (tối đa 255 ký tự); `ngày sinh` hợp lệ không ở tương lai; `giới tính` tùy chọn (`Nam` / `Nữ` / `Khác`). `username` và `email` là duy nhất không phân biệt hoa thường sau khi cắt khoảng trắng đầu/cuối.
  - **BR-03**: Admin không đặt và không được xem mật khẩu của người dùng. Mật khẩu tạm có hiệu lực 15 phút kể từ khi máy chủ phát hành theo [DEC-06](../../../../decisions/password-reset-and-email.md); người dùng bắt buộc xác minh email và tự đặt mật khẩu riêng trước khi được cấp phiên sử dụng chat.
  - **BR-04**: `username` và `email` của tài khoản đang ở trạng thái xóa mềm (`DELETED`) chưa được phép tái sử dụng cho đến khi hoàn tất purge.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Kiểm tra tính duy nhất và lưu tài khoản phải đảm bảo tính nguyên tử trên máy chủ để chống tạo trùng khi có nhiều yêu cầu đăng ký/tạo mới đồng thời.
  - **NFR-02**: Không ghi mật khẩu tạm ở dạng bản rõ vào nhật ký hệ thống, thông báo lỗi hay phản hồi trả về cho Admin App.
