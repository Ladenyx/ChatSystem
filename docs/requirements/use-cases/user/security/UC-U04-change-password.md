# ĐẶC TẢ USE CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U04
- **Use Case Name**: Đổi mật khẩu
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-05
- **Last Updated by / Date**: baonguyen1776 / 2026-11-05
- **Primary Actor**: Người dùng đã đăng nhập User Client.
- **Secondary Actor(s)**: Không có tác nhân ngoài hệ thống.
- **Priority**: Must Have
- **Status**: Draft
- **Screen / Feature ID**: `U-DLG-01` / `U-04`

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng xác nhận mật khẩu hiện tại và đặt mật khẩu mới cho tài khoản đang đăng nhập.
- **Goal**: Đổi mật khẩu tài khoản sau khi Server xác thực và kiểm tra dữ liệu hợp lệ.
- **Trigger**: Người dùng chọn **Cài đặt → Hồ sơ & Bảo mật**, sau đó mở tab Đổi mật khẩu trong `U-DLG-01`.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng đã đăng nhập bằng phiên User Client hợp lệ và biết mật khẩu hiện tại.
- **Post-conditions**:
  - **Thành công**: Server lưu mật khẩu mới, giữ phiên hiện tại và thu hồi các phiên trên thiết bị khác; Client chỉ thông báo thành công khi có xác nhận từ Server.
  - **Chưa hoàn tất**: Nếu Server chưa lưu mật khẩu mới, thao tác hủy hoặc lỗi không tự thay mật khẩu cũ.
  - **Chưa rõ kết quả**: Nếu không nhận được phản hồi, Client không khẳng định mật khẩu đã hay chưa được thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Người dùng mở tab Đổi mật khẩu | Client hiển thị các ô mật khẩu hiện tại, mật khẩu mới và xác nhận mật khẩu mới. |
| 2 | Người dùng nhập thông tin và chọn lưu | Client kiểm tra trường bắt buộc, quy tắc mật khẩu mới và hai ô mật khẩu mới khớp nhau. |
| 3 | Người dùng chờ kết quả | Nếu dữ liệu hợp lệ, Client gửi yêu cầu và ngăn gửi trùng. Server kiểm tra phiên, trạng thái tài khoản, mật khẩu hiện tại và mật khẩu mới. |
| 4 | Người dùng nhận kết quả | Nếu hợp lệ, Server lưu mật khẩu mới, giữ phiên hiện tại và thu hồi các phiên trên thiết bị khác; Client chỉ báo thành công sau khi được xác nhận. → Kết thúc Use Case. |

### Alternative Flows

- **Alt 2a — Hủy**: Người dùng rời thao tác trước khi gửi; mật khẩu tài khoản không thay đổi.
- **Alt 2b — Thiếu hoặc sai dữ liệu**: Client thông báo tại trường cần sửa; không gửi dữ liệu chưa hợp lệ lên Server.
- **Alt 2c — Quên mật khẩu hiện tại**: Người dùng có thể rời thao tác và thực hiện [luồng quên mật khẩu](../authentication/UC-U05-user-password-recovery.md); không tự thay mật khẩu từ use case này.

### Exception Flows

- **Ex 3a — Mật khẩu hiện tại không đúng**: Server từ chối yêu cầu; mật khẩu cũ không thay đổi. Client thông báo để người dùng kiểm tra lại.
- **Ex 3b — Mật khẩu mới không hợp lệ**: Server từ chối lưu; Client thông báo để người dùng sửa.
- **Ex 3c — Phiên hoặc tài khoản không còn hợp lệ**: Server từ chối đổi mật khẩu; Client yêu cầu đăng nhập lại khi phù hợp.
- **Ex 3d — Không xác nhận được kết quả**: Nếu mất kết nối hoặc Server không phản hồi, Client thông báo chưa xác định được kết quả và không tự gửi lặp.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Người dùng chỉ đổi mật khẩu của tài khoản gắn với phiên hợp lệ; Server phải kiểm tra mật khẩu hiện tại trước khi lưu mật khẩu mới.
  - **BR-02**: Mật khẩu mới áp dụng cùng quy tắc đăng ký: 8–128 ký tự, có chữ hoa, chữ thường, chữ số và ký tự đặc biệt; xác nhận mật khẩu phải khớp. Ký tự đặc biệt theo [DEC-07](../../../../decisions/account-lifecycle.md). Client kiểm tra trước khi gửi; Server kiểm tra lại trước khi lưu.
  - **BR-03**: Sau khi đổi mật khẩu thành công, giữ phiên hiện tại và thu hồi các phiên trên thiết bị khác; đổi mật khẩu không tự khôi phục khóa E2EE hoặc lịch sử mã hóa.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Mật khẩu được truyền qua kết nối được bảo vệ; không ghi mật khẩu vào log, URL hoặc thông báo lỗi.
  - **NFR-02**: Thể hiện trạng thái xử lý, ngăn yêu cầu trùng và không báo thành công trước xác nhận từ Server.
