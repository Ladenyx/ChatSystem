# ĐẶC TẢ USE CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U05
- **Use Case Name**: Khôi phục mật khẩu
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-04
- **Last Updated by / Date**: baonguyen1776 / 2026-11-04
- **Primary Actor**: Người dùng muốn khôi phục mật khẩu.
- **Secondary Actor(s)**: Dịch vụ gửi email.
- **Priority**: Must Have
- **Status**: Draft
- **Screen / Feature ID**: `U-SCR-04`, `U-SCR-05` / `U-05`

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng nhập tên đăng nhập, cung cấp lại email của tài khoản và yêu cầu gửi mật khẩu tạm; sau đó dùng mật khẩu tạm để đặt mật khẩu mới.
- **Goal**: Đặt mật khẩu mới để có thể đăng nhập lại khi tài khoản đủ điều kiện.
- **Trigger**: Người dùng chọn **Quên mật khẩu** tại màn hình đăng nhập `U-SCR-01`.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có thể mở `U-SCR-04`; việc yêu cầu không đòi hỏi đã đăng nhập. Để hoàn tất, tài khoản phải đủ điều kiện, người dùng biết tên đăng nhập và có thể truy cập email gắn với tài khoản.
- **Post-conditions**:
  - **Thành công**: Server lưu mật khẩu mới, vô hiệu mật khẩu cũ và mật khẩu tạm, thu hồi các phiên theo DEC-06. Người dùng trở về đăng nhập bằng mật khẩu mới.
  - **Chưa hoàn tất**: Nếu chưa lưu mật khẩu mới, việc yêu cầu khôi phục không tự thay mật khẩu cũ hoặc thu hồi phiên đang hoạt động.
  - **Chưa rõ kết quả**: Khi mất phản hồi, Client không khẳng định email đã được gửi hoặc mật khẩu đã được thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Người dùng chọn **Quên mật khẩu** tại `U-SCR-01` | Client mở `U-SCR-04`. |
| 2 | Người dùng nhập tên đăng nhập và yêu cầu tiếp tục | Server kiểm tra yêu cầu; nếu có thể tiếp tục, Client hiển thị email gắn với tài khoản ở dạng che một phần và yêu cầu nhập lại email đầy đủ. |
| 3 | Người dùng nhập email đầy đủ và yêu cầu gửi | Client kiểm tra dữ liệu bắt buộc; Server kiểm tra email có khớp với tài khoản, điều kiện xử lý và giới hạn gửi. Chỉ khi email khớp và yêu cầu được phép, Server phát hành mật khẩu tạm và yêu cầu gửi đến email đã đăng ký. |
| 4 | Người dùng kiểm tra email và chọn tiếp tục | Client mở `U-SCR-05` để nhập mật khẩu tạm. Người dùng cũng có thể chủ động yêu cầu gửi lại khi được phép; Client không khẳng định email đã đến hộp thư. |
| 5 | Người dùng nhập và xác nhận mật khẩu tạm | Server kiểm tra mật khẩu tạm với tài khoản trong lượt khôi phục, thời hạn và điều kiện tài khoản. Nếu hợp lệ, Server chỉ cấp quyền đặt mật khẩu; Client hiển thị bước nhập mật khẩu mới. Chưa cấp phiên sử dụng ứng dụng. |
| 6 | Người dùng đặt mật khẩu mới tại `U-SCR-05` | Server kiểm tra quyền, thời hạn và trạng thái tài khoản theo DEC-06; nếu hợp lệ, lưu mật khẩu mới, vô hiệu thông tin cũ và thu hồi phiên theo chính sách. Client chuyển về `U-SCR-01` để người dùng tự đăng nhập. → Kết thúc Use Case. |

### Alternative Flows

- **Alt 2a — Thiếu tên đăng nhập**: Client yêu cầu bổ sung, không gửi yêu cầu kiểm tra lên Server.
- **Alt 3a — Thiếu email hoặc email sai định dạng**: Client yêu cầu người dùng sửa, không gửi dữ liệu không hợp lệ lên Server.
- **Alt 4a — Yêu cầu gửi lại**: Người dùng chủ động yêu cầu gửi lại khi được phép. Server áp dụng giới hạn theo BR-01 và kiểm tra lại điều kiện trước khi gửi đến email đã đăng ký.
- **Alt 4b — Quay về đăng nhập**: Người dùng có thể rời `U-SCR-04` để trở về `U-SCR-01`; không tự đăng nhập hoặc tự yêu cầu gửi lại.
- **Alt 5a — Nhập mật khẩu tạm tại màn đăng nhập**: Người dùng nhập tên đăng nhập và mật khẩu tạm tại `U-SCR-01`. Nếu Server xác thực hợp lệ và cấp quyền đặt mật khẩu theo luồng đăng nhập, Client mở `U-SCR-05` tại bước nhập mật khẩu mới; không yêu cầu nhập lại mật khẩu tạm. Tiếp tục bước 6.

### Exception Flows

- **Ex 2a — Không thể tiếp tục với tên đăng nhập**: Server không cung cấp thông tin email đầy đủ hoặc mật khẩu tạm; Client thông báo không thể tiếp tục. Việc tra cứu và phản hồi cần hạn chế nguy cơ dò tài khoản theo BR-02.
- **Ex 3a — Email không khớp**: Server không phát hành hoặc gửi mật khẩu tạm cho yêu cầu đó; Client thông báo để người dùng kiểm tra thông tin. Server giới hạn việc thử theo BR-02.
- **Ex 3b — Yêu cầu gửi bị giới hạn**: Server không cấp thêm mật khẩu tạm khi chưa hết thời gian chờ hoặc vượt giới hạn gửi; người dùng thử lại khi được phép.
- **Ex 3c — Gửi email thất bại hoặc mất phản hồi**: Client không khẳng định email đã được gửi và không tự gửi lặp. Mật khẩu cũ và phiên hiện tại không tự mất hiệu lực chỉ vì gửi email thất bại.
- **Ex 5a — Mật khẩu tạm không hợp lệ hoặc hết hạn**: Server không cấp quyền đặt mật khẩu. Người dùng có thể bắt đầu lại yêu cầu khôi phục theo chính sách gửi.
- **Ex 5b — Tài khoản bị khóa hoặc đã xóa**: Server không cấp phiên hay quyền đặt mật khẩu, kể cả khi mật khẩu tạm đúng. Khôi phục mật khẩu không tự mở khóa tài khoản.
- **Ex 6a — Quyền đặt mật khẩu hết hạn hoặc bị thu hồi**: Server không lưu mật khẩu mới. Người dùng cần bắt đầu lại yêu cầu hợp lệ theo DEC-06.
- **Ex 6b — Không thể xác nhận kết quả lưu**: Nếu Client mất phản hồi khi lưu mật khẩu mới, Client thông báo chưa xác định được kết quả; không khẳng định mật khẩu đã hoặc chưa thay đổi và không tự gửi lặp.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Mật khẩu tạm dùng một lần và có hạn 15 phút từ khi Server phát hành; quyền đặt mật khẩu dùng chung hạn này. Yêu cầu gửi cách nhau ít nhất 60 giây và tối đa 5 yêu cầu/giờ/tài khoản theo [DEC-06](../../../../decisions/password-reset-and-email.md).
  - **BR-02**: Server chỉ gửi mật khẩu tạm đến email đã gắn với tài khoản sau khi kiểm tra email người dùng nhập khớp với email đó theo quy tắc so sánh tại [DEC-07](../../../../decisions/account-lifecycle.md); Client không nhận email đầy đủ từ bước tra cứu. Server giới hạn việc tra cứu tên đăng nhập và thử email theo tài khoản/tên đăng nhập và nguồn yêu cầu; ngưỡng theo cấu hình Server. Gợi ý trước xác thực có thể làm lộ một phần thông tin và cho biết tên đăng nhập tồn tại; giới hạn thử chỉ giúp giảm rủi ro này.
  - **BR-03**: Mật khẩu tạm chỉ phục vụ đặt mật khẩu mới; không cấp quyền chat hoặc quản trị. Mật khẩu cũ và phiên hiện tại chỉ bị xử lý sau khi lưu mật khẩu mới thành công, theo [DEC-06](../../../../decisions/password-reset-and-email.md#quyền-đặt-mật-khẩu-mới).
  - **BR-04**: Khôi phục mật khẩu không tự khôi phục lịch sử E2EE hoặc thay mã khôi phục khóa.
  - **BR-05**: Mật khẩu mới đặt bằng quyền từ mật khẩu tạm áp dụng cùng quy tắc với đăng ký công khai theo [DEC-07](../../../../decisions/account-lifecycle.md); Server kiểm tra lại trước khi lưu.
  - **BR-06**: Thông tin nhận diện lượt khôi phục chỉ giúp Server xác định yêu cầu, không tự cấp quyền đặt mật khẩu. Server chỉ cấp quyền sau khi xác thực mật khẩu tạm và kiểm tra điều kiện tài khoản; quyền này không được lưu để tự tiếp tục khi mở lại ứng dụng.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Thông tin nhạy cảm được truyền qua kết nối được bảo vệ; không ghi mật khẩu tạm hoặc mật khẩu mới vào log, URL hay thông báo lỗi.
  - **NFR-02**: Hiển thị trạng thái xử lý và lỗi rõ ràng; ngăn gửi trùng và không báo thành công khi chưa có xác nhận phù hợp từ Server.

Chi tiết giao diện tại [U-SCR-04](../../../screens/user/authentication/U-SCR-04-forgot-password.md) và [U-SCR-05](../../../screens/user/authentication/U-SCR-05-force-change-password.md).
