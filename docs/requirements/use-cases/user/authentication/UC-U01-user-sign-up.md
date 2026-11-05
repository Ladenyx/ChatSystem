# ĐẶC TẢ USE CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U01
- **Use Case Name**: Đăng ký tài khoản User Client
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-04
- **Last Updated by / Date**: baonguyen1776 / 2026-11-04
- **Primary Actor**: Người muốn tạo tài khoản User Client.
- **Secondary Actor(s)**: Dịch vụ gửi email.
- **Priority**: Must Have
- **Status**: Draft
- **Screen / Feature ID**: `U-SCR-02`, `U-SCR-03` / `U-01`

> Bản nháp áp dụng flow đăng ký nhiều bước mới: tạo tài khoản sau khi xác minh email và nhập thông tin cá nhân. [PR #7](https://github.com/Ladenyx/ChatSystem/pull/7) hiện mô tả thời điểm tạo tài khoản trước thay đổi này; cần nêu rõ khác biệt khi trình nhóm review.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng nhập lần lượt tên đăng nhập, mật khẩu, email; xác minh email; sau đó cung cấp thông tin cá nhân để hoàn tất đăng ký.
- **Goal**: Tạo tài khoản User Client với email đã được xác minh và thông tin cá nhân được nhập đầy đủ.
- **Trigger**: Người dùng chọn tạo tài khoản tại màn hình đăng nhập `U-SCR-01`.

## Điều kiện (Conditions)

- **Pre-conditions**: User Client đã có cấu hình địa chỉ Server và đang hiển thị bước đăng ký trên `U-SCR-02`.
- **Post-conditions**:
  - **Thành công**: Server tạo tài khoản `USER` bằng thông tin đã được kiểm tra; email đã xác minh. Người dùng trở về màn đăng nhập và tự đăng nhập; chưa cấp phiên User Client.
  - **Bỏ dở hoặc thất bại**: Không tạo tài khoản từ yêu cầu chưa hoàn tất; yêu cầu tạm được hủy hoặc hết hạn theo BR-06.
  - **Chưa xác định được kết quả**: Nếu mất phản hồi, Client chưa biết Server đã xử lý đến đâu và không khẳng định việc đăng ký đã hoàn tất.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Mở đăng ký, nhập tên đăng nhập và yêu cầu tiếp tục | Client kiểm tra dữ liệu; Server kiểm tra tên đăng nhập có thuộc tài khoản đã tạo hay không. Nếu hợp lệ, chuyển sang bước mật khẩu. |
| 2 | Nhập mật khẩu, xác nhận mật khẩu và yêu cầu tiếp tục | Client kiểm tra quy tắc mật khẩu và hai giá trị khớp nhau; nếu hợp lệ, chuyển sang bước email. |
| 3 | Nhập email và yêu cầu xác minh | Client kiểm tra định dạng; Server kiểm tra dữ liệu và email có thuộc tài khoản đã tạo hay không. Nếu hợp lệ, Server tạo yêu cầu đăng ký tạm và yêu cầu gửi mã đến email; Client chuyển sang `U-SCR-03`. |
| 4 | Nhập mã xác minh email | Server kiểm tra mã và hiệu lực của yêu cầu theo [luồng xác minh email](UC-U-VERIFY-EMAIL.md). Nếu hợp lệ, ghi nhận email đã được xác minh cho yêu cầu đó và chuyển sang bước thông tin cá nhân; chưa tạo tài khoản đăng nhập. |
| 5 | Nhập họ tên, địa chỉ, ngày sinh, giới tính và yêu cầu hoàn tất | Client kiểm tra dữ liệu; Server kiểm tra lại thông tin, quyền hoàn tất yêu cầu còn hiệu lực và tính duy nhất của username/email. |
| 6 | Chờ kết quả | Nếu hợp lệ, Server tạo tài khoản `USER` bằng thông tin của yêu cầu đã xác minh. Client thông báo kết quả và đưa người dùng về `U-SCR-01` để tự đăng nhập. → Kết thúc Use Case. |

### Alternative Flows

- **Alt 1a — Quay lại đăng nhập**: Trước khi bắt đầu bước xác minh, người dùng có thể rời đăng ký và quay về `U-SCR-01`; không tạo tài khoản.
- **Alt 1b — Tên đăng nhập đã thuộc tài khoản khác**: Tại bước 1, Server từ chối tên đã được dùng và Client thông báo để người dùng chọn tên khác. Tên thuộc yêu cầu tạm chưa được xem là đã đăng ký chỉ vì yêu cầu đó tồn tại.
- **Alt 2a — Dữ liệu chưa hợp lệ**: Tại bước 1–3 hoặc bước 5, Client thông báo các trường còn thiếu hoặc chưa hợp lệ; người dùng sửa trước khi tiếp tục hoặc gửi yêu cầu tương ứng.
- **Alt 3a — Đăng ký lại với email đang có yêu cầu tạm**: Tại bước 3, Server có thể tạo yêu cầu mới cùng email, kể cả dùng lại username của yêu cầu tạm cùng email. Mã mới gắn với yêu cầu mới; nhập trùng thông tin không tự hủy yêu cầu cũ. Việc hủy các yêu cầu tạm cũ chỉ thực hiện khi tạo tài khoản thành công bằng yêu cầu mới.
- **Alt 3b — Email đã được đăng ký**: Nếu email đã thuộc một tài khoản được tạo, Server không tạo yêu cầu đăng ký mới với email đó. Client thông báo và cho người dùng nhập email khác.
- **Alt 4a — Hủy hoặc rời luồng**: Sau khi có yêu cầu tạm nhưng trước khi tạo tài khoản, Client yêu cầu Server hủy yêu cầu tương ứng. Người dùng mở lại ứng dụng để bắt đầu đăng ký từ đầu. Nếu Server không nhận được yêu cầu hủy, yêu cầu tạm tự hết hạn theo BR-06; Client không khẳng định đã hủy khi chưa nhận xác nhận.
- **Alt 4b — Đổi email đang đăng ký**: Tại bước 4, người dùng chọn sửa email và tiếp tục đăng ký; Client giữ tên đăng nhập và mật khẩu trong phiên hiện tại. Server kiểm tra email mới chưa thuộc tài khoản đã tạo và xử lý cấp mã theo [luồng xác minh email](UC-U-VERIFY-EMAIL.md). Thao tác này không gia hạn yêu cầu đăng ký tạm.

### Exception Flows

- **Ex 1a — Server không xác nhận được tên đăng nhập**: Tại bước 1, nếu mất kết nối hoặc Server không phản hồi, Client thông báo chưa thể kiểm tra và cho người dùng chủ động thử lại; không kết luận tên đã được sử dụng hoặc còn trống.
- **Ex 3a — Không gửi được email**: Tại bước 3, nếu yêu cầu tạm đã tạo nhưng gửi mã thất bại, hệ thống thông báo và cho người dùng chuyển sang `U-SCR-03` để yêu cầu gửi lại khi có thể. Không báo tài khoản đã được tạo.
- **Ex 3b — Yêu cầu gửi mã bị giới hạn**: Server chưa gửi mã khi vượt giới hạn chống lạm dụng, kể cả khi người dùng tạo lượt đăng ký mới. Client thông báo và cho người dùng thử lại khi được phép; không báo đăng ký thành công.
- **Ex 4a — Mã xác minh không hợp lệ hoặc hết hạn**: Tại bước 4, Client thông báo để người dùng thử lại hoặc yêu cầu mã mới theo luồng `U-SCR-03`. Mã chỉ được chấp nhận khi yêu cầu tương ứng còn hiệu lực.
- **Ex 5a — Thông tin bị từ chối hoặc đã được sử dụng**: Tại bước 5–6, Server không tạo tài khoản nếu dữ liệu không hợp lệ, username/email đã thuộc tài khoản khác hoặc yêu cầu hết hạn. Client thông báo kết quả và hướng dẫn người dùng sửa hoặc bắt đầu đăng ký mới theo tình huống.
- **Ex 5b — Mất kết nối hoặc Server không phản hồi**: Khi gửi yêu cầu tại bước 3–6, Client thông báo chưa xác định được kết quả và không tự gửi lặp. Người dùng có thể chủ động đăng ký lại; hệ thống không ghi đè tài khoản đã tạo nếu lần trước đã thành công.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Username dài 6–30 ký tự; chỉ gồm chữ cái Latin, chữ số, dấu gạch dưới `_` và gạch ngang `-`. Server kiểm tra tính duy nhất khi hoàn tất tạo tài khoản.
  - **BR-02**: Mật khẩu dài 8–128 ký tự, có ít nhất một chữ hoa, một chữ thường, một chữ số và một ký tự đặc biệt; xác nhận mật khẩu phải khớp. Ký tự đặc biệt là ký hiệu ASCII in được không phải chữ hoặc số, không tính khoảng trắng; ví dụ `!`, `@`, `#`, `$`, `%`, `^`, `&`, `*`. Ví dụ `@Nggasd123` đáp ứng các điều kiện này.
  - **BR-03**: Email, họ tên, địa chỉ, ngày sinh và giới tính phải được nhập trong quá trình đăng ký; ngày sinh không được ở tương lai. Thông tin cá nhân được cung cấp trước khi hoàn tất tạo tài khoản; quy tắc dữ liệu theo [DEC-07](../../../../decisions/account-lifecycle.md).
  - **BR-04**: Đăng ký công khai chỉ tạo tài khoản `USER`; không cấp phiên sử dụng ứng dụng từ use case đăng ký.
  - **BR-05**: Mỗi email chỉ thuộc một tài khoản đã tạo. Server kiểm tra email khi gửi yêu cầu đăng ký hoặc đổi email đang đăng ký và kiểm tra lại khi hoàn tất; yêu cầu tạm chưa tạo tài khoản không được coi là tài khoản đã đăng ký. Email phải được xác minh trước khi tạo tài khoản. Quyền hoàn tất chỉ phục vụ yêu cầu đăng ký tương ứng, không cấp quyền chat.
  - **BR-06**: Mỗi yêu cầu tạm có hạn tối đa 30 phút từ khi Server tạo; mã xác minh có hạn 10 phút và chỉ hợp lệ khi yêu cầu tương ứng còn hiệu lực. Gửi lại mã không gia hạn yêu cầu. Hủy, hết hạn và đăng ký lại được xử lý nhất quán, không hủy hoặc ghi đè tài khoản đã tạo.
  - **BR-07**: Client kiểm tra dữ liệu và phản hồi trong khi người dùng nhập; Server kiểm tra lại và quyết định dữ liệu hợp lệ, tính duy nhất trước khi tạo tài khoản. Chi tiết hiển thị thuộc đặc tả `U-SCR-02`.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Truyền thông tin đăng ký qua kết nối được bảo vệ; Server không lưu trữ mật khẩu dạng rõ và không ghi mật khẩu/mã xác minh vào log, thông báo lỗi hay URL.
  - **NFR-02**: Thể hiện trạng thái xử lý, ngăn yêu cầu trùng và thông báo kết quả rõ ràng; không báo tài khoản được tạo trước khi Server xác nhận tạo thành công.

Chi tiết điều khiển, phản hồi khi nhập và thông báo được mô tả tại [đặc tả giao diện U-SCR-02](../../../screens/user/authentication/U-SCR-02-user-sign-up.md).
