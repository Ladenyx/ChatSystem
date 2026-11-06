# ĐẶC TẢ USE CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-VERIFY-EMAIL
- **Use Case Name**: Xác minh email
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-04
- **Last Updated by / Date**: baonguyen1776 / 2026-11-04
- **Primary Actor**: Người dùng cần xác minh email.
- **Secondary Actor(s)**: Dịch vụ gửi email.
- **Priority**: Must Have
- **Status**: Draft
- **Screen / Feature ID**: `U-SCR-03` / `U-01`, `U-03`

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng xác minh quyền truy cập email bằng mã do Server gửi.
- **Goal**: Xác minh email cho yêu cầu đang thực hiện.
- **Trigger**: Một luồng nghiệp vụ chuyển người dùng đến `U-SCR-03` để xác minh email.

## Điều kiện (Conditions)

- **Pre-conditions**: Server đã tạo yêu cầu xác minh. Người dùng có thể mở màn hình khi email chưa đến hoặc gửi mã thất bại.
- **Post-conditions**:
  - **Thành công**: Server ghi nhận email đã được xác minh cho đúng yêu cầu; người dùng tiếp tục luồng gọi chức năng này.
  - **Chưa hoàn tất**: Server không xác minh email bằng mã sai, hết hạn hoặc yêu cầu không hợp lệ.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Người dùng mở màn hình xác minh | Client hướng dẫn người dùng kiểm tra email. |
| 2 | Người dùng nhập mã nhận được và yêu cầu xác minh | Client kiểm tra định dạng, gửi yêu cầu và ngăn gửi lặp trong lúc chờ. |
| 3 | Người dùng chờ kết quả | Server kiểm tra mã, thời hạn và điều kiện của yêu cầu xác minh. |
| 4 | Người dùng tiếp tục luồng đang thực hiện | Nếu mã hợp lệ, Server ghi nhận email đã được xác minh; Client chuyển sang bước tiếp theo của luồng đó. → Kết thúc Use Case. |

Trong đăng ký, bước tiếp theo là nhập thông tin cá nhân; chưa tạo tài khoản. Chi tiết tại [UC-U01](UC-U01-user-sign-up.md). Trong luồng đổi email từ hồ sơ, email chỉ được cập nhật sau khi Server kiểm tra các điều kiện của luồng này. Màn hình cũng có thể được dùng khi kích hoạt tài khoản do Admin tạo; bước tiếp theo thuộc đặc tả luồng kích hoạt và chưa được chốt tại đây.

### Alternative Flows

- **Alt 2a — Mã chưa đủ hoặc sai định dạng**: Client không gửi yêu cầu và cho người dùng sửa. Khi đã nhập nhưng chưa đủ 6 chữ số, Client hướng dẫn nhập đủ và vô hiệu hóa **Xác minh**; dữ liệu không gửi lên Server không tính là một lần nhập sai.
- **Alt 2b — Gửi lại mã**: Người dùng yêu cầu mã mới đến cùng email. Khi Server cho phép theo BR-02, Server phát hành mã mới, vô hiệu mã cũ và gửi mã mới đến email. Người dùng tiếp tục từ bước 2.
- **Alt 2c — Sửa email khi đăng ký**: Người dùng chọn đổi email. Client giữ thông tin đã nhập trong phiên đăng ký; Server kiểm tra email mới theo [UC-U01](UC-U01-user-sign-up.md). Khi Server chấp nhận địa chỉ mới và cho phép gửi theo BR-02, mã cũ hết hiệu lực và mã mới được gửi đến email mới. Người dùng tiếp tục từ bước 2.
- **Alt 2d — Rời luồng xác minh**: Client trở về luồng gọi. Việc hủy đăng ký tạm theo [UC-U01](UC-U01-user-sign-up.md); với luồng đổi email trong hồ sơ, email cũ chưa bị thay đổi.

### Exception Flows

- **Ex 2a — Chưa được phép gửi mã**: Nếu còn thời gian chờ hoặc yêu cầu bị giới hạn gửi, Server chưa cấp mã mới; Client thông báo để người dùng thử lại khi được phép.
- **Ex 3a — Mã không đúng**: Server ghi nhận lần nhập sai và từ chối xác minh; Client hiển thị **“Mã xác thực không đúng”**. Người dùng có thể sửa và thử lại nếu mã còn hiệu lực.
- **Ex 3b — Mã hết hiệu lực**: Nếu nhập sai 5 lần, mã hết hạn hoặc bị thay thế, Server từ chối mã. Người dùng có thể yêu cầu mã mới khi được phép; nhập sai mã không tự khóa tài khoản.
- **Ex 3c — Yêu cầu hoặc quyền thực hiện hết hiệu lực**: Server từ chối xác minh; người dùng tiếp tục xử lý theo luồng gọi. Với đăng ký, người dùng bắt đầu lại từ đầu.
- **Ex 3d — Không gửi được email hoặc mất phản hồi**: Client thông báo chưa thể hoàn tất và không tự gửi lặp. Nếu kết quả đổi email hoặc cấp/gửi mã chưa rõ, Client không khẳng định email nào đang được dùng hay mã cũ còn hiệu lực.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Mã xác minh gồm 6 chữ số, dùng một lần và gắn với đúng yêu cầu. Email thông báo hạn mã; màn hình xác minh không hiển thị thời gian hết hạn mã. Quy tắc hiệu lực và số lần thử theo [DEC-06](../../../../decisions/password-reset-and-email.md#xác-minh-email).
  - **BR-02**: Thời gian chờ gửi lại, đổi email và giới hạn gửi mã theo [DEC-06](../../../../decisions/password-reset-and-email.md#xác-minh-email). Server quyết định khi nào yêu cầu được phép gửi.
  - **BR-03**: Xác minh thành công chỉ ghi nhận kết quả cho yêu cầu tương ứng; các bước tạo tài khoản, cập nhật hồ sơ hoặc kích hoạt do luồng gọi quyết định.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Mã xác minh được truyền qua kết nối được bảo vệ và không ghi vào log, URL hoặc thông báo lỗi.
  - **NFR-02**: Thể hiện trạng thái xử lý, ngăn gửi trùng và không báo thành công khi chưa có xác nhận từ Server.

Chi tiết điều khiển và thông báo được mô tả tại [đặc tả giao diện U-SCR-03](../../../screens/user/authentication/U-SCR-03-verify-email.md).
