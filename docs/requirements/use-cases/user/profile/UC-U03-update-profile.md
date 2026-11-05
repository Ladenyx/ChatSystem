# ĐẶC TẢ USE CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U03
- **Use Case Name**: Cập nhật hồ sơ tài khoản
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-05
- **Last Updated by / Date**: baonguyen1776 / 2026-11-05
- **Primary Actor**: Người dùng đã đăng nhập User Client.
- **Secondary Actor(s)**: Dịch vụ gửi email.
- **Priority**: Must Have
- **Status**: Draft
- **Screen / Feature ID**: `U-DLG-01`, `U-SCR-03` / `U-03`

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng xem và cập nhật thông tin hồ sơ; đổi email là thao tác riêng có xác thực và xác minh email mới.
- **Goal**: Lưu thông tin cá nhân hợp lệ hoặc cập nhật email mới đã được xác minh.
- **Trigger**: Người dùng chọn **Cài đặt → Hồ sơ & Bảo mật**, sau đó mở tab Hồ sơ trong `U-DLG-01`.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng đã đăng nhập bằng phiên User Client hợp lệ.
- **Post-conditions**:
  - **Lưu hồ sơ thành công**: Server lưu thông tin cá nhân hợp lệ của người dùng và Client hiển thị kết quả đã lưu.
  - **Đổi email thành công**: Server cập nhật email mới sau khi xác minh và kiểm tra các điều kiện liên quan.
  - **Chưa hoàn tất**: Thông tin chưa được lưu không tự cập nhật; email cũ không bị thay chỉ vì đã yêu cầu đổi hoặc gửi mã.
  - **Chưa rõ kết quả**: Khi không nhận được phản hồi, Client không khẳng định Server đã lưu hay chưa.

## Luồng sự kiện (Flow of Events)

### Basic Flow — Cập nhật thông tin cá nhân

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Người dùng mở tab Hồ sơ | Client hiển thị thông tin tài khoản; tên đăng nhập chỉ đọc. |
| 2 | Người dùng chỉnh sửa họ tên, địa chỉ, ngày sinh hoặc giới tính | Client cho chọn ngày sinh bằng lịch; kiểm tra dữ liệu và phản hồi tại trường cần sửa. |
| 3 | Người dùng chọn lưu hồ sơ | Client kiểm tra dữ liệu, gửi yêu cầu hợp lệ và ngăn gửi trùng trong lúc chờ. |
| 4 | Người dùng chờ kết quả | Server kiểm tra phiên, quyền và dữ liệu; nếu hợp lệ, lưu hồ sơ. Client chỉ thông báo thành công khi Server xác nhận. → Kết thúc Use Case. |

### Alternative Flows

- **Alt 2a — Không lưu hoặc hủy thay đổi**: Người dùng rời thao tác mà không lưu; các thay đổi chưa gửi không tự cập nhật hồ sơ đã lưu.
- **Alt 2b — Đổi email**: Người dùng chọn đổi email, nhập email mới và mật khẩu hiện tại. Server kiểm tra phiên, mật khẩu, định dạng và tính duy nhất của email; nếu hợp lệ, tạo yêu cầu xác minh và gửi mã đến email mới. Client mở `U-SCR-03` theo [luồng xác minh email](../authentication/UC-U-VERIFY-EMAIL.md). Khi mã hợp lệ, Server kiểm tra lại điều kiện và cập nhật email; Client trở về tab Hồ sơ. Email cũ được giữ đến khi cập nhật thành công. Đổi email không tự lưu các thay đổi hồ sơ khác.
- **Alt 2c — Hủy đổi email**: Người dùng rời luồng xác minh trước khi hoàn tất; email cũ không bị thay đổi. Việc xác minh mã được xử lý theo `UC-U-VERIFY-EMAIL`.

### Exception Flows

- **Ex 2a — Mật khẩu hiện tại không đúng**: Server không chấp nhận yêu cầu đổi email; Client thông báo để người dùng kiểm tra lại.
- **Ex 2b — Email mới không hợp lệ hoặc không thể sử dụng**: Không cập nhật email; Client thông báo để người dùng sửa hoặc chọn email khác. Server kiểm tra lại tính duy nhất trước khi hoàn tất.
- **Ex 2c — Xác minh email chưa thành công**: Mã sai, hết hạn, gửi thất bại hoặc yêu cầu không còn hiệu lực được xử lý theo `UC-U-VERIFY-EMAIL`; email cũ không bị thay nếu chưa cập nhật thành công.
- **Ex 3a — Dữ liệu hồ sơ không hợp lệ**: Client thông báo tại trường cần sửa và không gửi dữ liệu chưa hợp lệ. Server kiểm tra lại khi nhận yêu cầu.
- **Ex 4a — Phiên hoặc quyền không còn hợp lệ**: Server từ chối cập nhật; Client yêu cầu người dùng đăng nhập lại khi phù hợp.
- **Ex 4b — Không xác nhận được kết quả**: Nếu mất kết nối hoặc Server không phản hồi, Client thông báo chưa xác định được kết quả và không tự gửi lặp.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Người dùng chỉ cập nhật hồ sơ của tài khoản gắn với phiên hợp lệ. Username là thông tin chỉ đọc tại màn hình này.
  - **BR-02**: Họ tên, ngày sinh và giới tính bắt buộc; địa chỉ tùy chọn. Kiểm tra theo [DEC-07](../../../../decisions/account-lifecycle.md): họ tên sau trim từ 1 đến 100 ký tự; địa chỉ sau trim tối đa 255 ký tự nếu được nhập; ngày sinh hợp lệ, không ở tương lai; giới tính là Nam hoặc Nữ. Client hỗ trợ phản hồi khi nhập; Server kiểm tra lại trước khi lưu.
  - **BR-03**: Lưu thông tin cá nhân và đổi email là hai thao tác riêng. Thay email yêu cầu mật khẩu hiện tại và xác minh địa chỉ mới bằng mã gắn với tài khoản, yêu cầu tương ứng; Server chỉ cập nhật khi các điều kiện đều hợp lệ.
  - **BR-04**: Email của tài khoản phải duy nhất theo quy tắc tại DEC-07. Mã xác minh, thời hạn và giới hạn gửi theo [DEC-06](../../../../decisions/password-reset-and-email.md#xác-minh-email).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Thông tin tài khoản và mật khẩu được truyền qua kết nối được bảo vệ; không ghi mật khẩu hoặc mã xác minh vào log, URL hay thông báo lỗi.
  - **NFR-02**: Thể hiện trạng thái xử lý, ngăn gửi trùng và không báo thành công trước khi Server xác nhận.
