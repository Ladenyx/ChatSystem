# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-AUTH-01
- **Use Case Name**: Đăng ký tài khoản User Client
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-04
- **Last Updated by / Date**: baonguyen1776 / 2026-11-08
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Dịch vụ gửi email (Email Service)

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng cung cấp tên đăng nhập, mật khẩu, email; hoàn tất xác minh mã bảo mật qua email; sau đó nhập thông tin cá nhân bắt buộc để hoàn tất tạo tài khoản User Client mới trên hệ thống.
- **Goal**: Khởi tạo thành công tài khoản người dùng với email đã xác thực và thông tin cá nhân hợp lệ.
- **Trigger**: Người dùng chọn liên kết "Tạo tài khoản" tại màn hình đăng nhập (`U-SCR-01`).

## Điều kiện (Conditions)

- **Pre-conditions**: Ứng dụng User Client đã kết nối được với máy chủ; người dùng chưa có tài khoản hoặc muốn tạo tài khoản mới.
- **Post-conditions**:
  - **Thành công**: Tài khoản `USER` mới được tạo trong cơ sở dữ liệu với trạng thái hoạt động (`ACTIVE`) và email đã được đánh dấu xác minh; người dùng được chuyển về màn hình đăng nhập (`U-SCR-01`) để tự đăng nhập (chưa cấp phiên sử dụng ứng dụng ngay).
  - **Hủy hoặc thất bại**: Yêu cầu đăng ký tạm bị hủy hoặc hết hạn; không tạo tài khoản rác trong hệ thống.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** chọn tạo tài khoản từ màn hình đăng nhập (`U-SCR-01`), cung cấp tên đăng nhập mong muốn và gửi yêu cầu kiểm tra.
2. **Hệ thống** kiểm tra định dạng tên đăng nhập (6–30 ký tự, chữ cái, số, `_`, `-`) và xác nhận tên đăng nhập chưa bị trùng với tài khoản đã tồn tại.
3. **Người dùng** nhập mật khẩu, xác nhận lại mật khẩu và chuyển sang bước cung cấp email.
4. **Hệ thống** kiểm tra mật khẩu đạt chuẩn an toàn (8–128 ký tự, đủ chữ hoa, thường, số, ký tự đặc biệt) và hai ô mật khẩu khớp nhau.
5. **Người dùng** nhập địa chỉ email và gửi yêu cầu xác minh.
6. **Hệ thống** kiểm tra định dạng email, xác nhận email chưa gắn với tài khoản đã tạo, tạo một yêu cầu đăng ký tạm (hiệu lực 30 phút), phát hành mã xác minh 6 chữ số (hiệu lực 10 phút) gửi tới email và chuyển **Người dùng** sang màn hình nhập mã xác minh (`U-SCR-03`).
7. **Người dùng** nhập mã xác minh nhận được từ hộp thư email và gửi yêu cầu xác nhận.
8. **Hệ thống** đối chiếu mã xác minh theo quy trình xác minh email (`UC-USR-AUTH-04`), ghi nhận email đã được xác minh cho yêu cầu tạm và chuyển **Người dùng** sang bước hoàn thiện thông tin cá nhân.
9. **Người dùng** cung cấp họ tên (bắt buộc) và có thể bổ sung ngày sinh, giới tính (tùy chọn Nam/Nữ/Khác), địa chỉ (tùy chọn), sau đó gửi yêu cầu hoàn tất đăng ký.
10. **Hệ thống** kiểm tra lại toàn bộ dữ liệu, xác nhận yêu cầu tạm còn hiệu lực và tính duy nhất của username/email, tiến hành tạo tài khoản `USER` mới và chuyển **Người dùng** về màn hình đăng nhập (`U-SCR-01`) kèm thông báo đăng ký thành công.

### Alternative Flows

**Alternative flow 1: Người dùng chỉnh sửa lại địa chỉ email trong lúc xác minh**
1. Tại bước #7 của Basic Flow, **Người dùng** chọn "Đổi email" trên màn hình `U-SCR-03` để sửa địa chỉ email.
2. **Hệ thống** cho phép người dùng nhập địa chỉ email mới, giữ nguyên tên đăng nhập và mật khẩu đã nhập trước đó.
3. **Hệ thống** kiểm tra email mới, hủy mã cũ, phát hành mã mới tới địa chỉ mới và khởi động lại bộ đếm gửi mã (thời hạn 30 phút của yêu cầu đăng ký tạm không đổi).
4. Tiếp tục bước #7 trong Basic Flow.

**Alternative flow 2: Yêu cầu gửi lại mã xác minh email**
1. Tại bước #7 của Basic Flow, **Người dùng** chưa nhận được mã hoặc mã bị hết hạn và chọn yêu cầu gửi lại.
2. **Hệ thống** kiểm tra khoảng cách giãn cách tối thiểu (60 giây); nếu hợp lệ thì hủy mã cũ, phát hành mã mới và gửi lại email.
3. Tiếp tục bước #7 trong Basic Flow.

**Alternative flow 3: Đăng ký lại bằng email đang có yêu cầu tạm chưa hoàn tất**
1. Tại bước #5 của Basic Flow, email người dùng nhập đang gắn với một yêu cầu đăng ký tạm cũ chưa hoàn tất (chưa thành tài khoản chính thức).
2. **Hệ thống** cho phép tạo một yêu cầu đăng ký tạm mới thay thế, không chặn việc đăng ký của người dùng.
3. Tiếp tục bước #6 trong Basic Flow.

**Alternative flow 4: Người dùng hủy đăng ký hoặc quay về màn hình đăng nhập**
1. Tại bất kỳ bước nào trước bước #10 của Basic Flow, **Người dùng** chọn hủy hoặc quay về màn hình đăng nhập `U-SCR-01`.
2. **Hệ thống** gửi yêu cầu hủy yêu cầu đăng ký tạm tới máy chủ và xóa sạch dữ liệu form đang nhập trên máy khách.
3. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Tên đăng nhập đã được sử dụng hoặc sai định dạng**
1. Tại bước #1 hoặc #2 của Basic Flow, tên đăng nhập sai quy cách hoặc đã thuộc về một tài khoản đang hoạt động / bị khóa / xóa mềm trong hệ thống.
2. **Hệ thống** từ chối và hiển thị thông báo để người dùng chọn tên đăng nhập khác.
3. Quay lại bước #1 trong Basic Flow.

**Exception flow 2: Mật khẩu không đạt chuẩn an toàn hoặc hai mật khẩu không khớp**
1. Tại bước #3 hoặc #4 của Basic Flow, mật khẩu thiếu điều kiện quy định hoặc ô nhập lại không khớp.
2. **Hệ thống** từ chối chuyển bước và hiển thị chi tiết các tiêu chí chưa đạt.
3. Quay lại bước #3 trong Basic Flow.

**Exception flow 3: Địa chỉ email đã được đăng ký cho tài khoản khác**
1. Tại bước #5 hoặc #6 của Basic Flow, địa chỉ email đã thuộc về một tài khoản chính thức trong hệ thống.
2. **Hệ thống** từ chối và thông báo email đã được đăng ký để người dùng sử dụng email khác.
3. Quay lại bước #5 trong Basic Flow.

**Exception flow 4: Nhập sai mã xác minh 5 lần liên tiếp hoặc mã hết hạn**
1. Từ bước #8 của Basic Flow, người dùng nhập sai mã 5 lần hoặc mã đã hết hạn 10 phút.
2. **Hệ thống** vô hiệu hóa mã xác minh hiện tại và yêu cầu người dùng gửi lại mã mới (hoặc đăng ký lại nếu yêu cầu tạm quá 30 phút).
3. Kết thúc Use Case.

**Exception flow 5: Yêu cầu đăng ký tạm hết hạn (Timeout 30 phút)**
1. Từ bước #6 đến bước #10 của Basic Flow, tổng thời gian từ lúc tạo yêu cầu tạm vượt quá 30 phút.
2. **Hệ thống** hủy yêu cầu đăng ký tạm, từ chối tạo tài khoản và yêu cầu người dùng bắt đầu lại từ đầu.
3. Kết thúc Use Case.

**Exception flow 6: Thông tin cá nhân không hợp lệ khi hoàn tất**
1. Tại bước #9 của Basic Flow, họ tên để trống hoặc vượt quá 100 ký tự, ngày sinh ở tương lai, hoặc địa chỉ vượt quá 255 ký tự.
2. **Hệ thống** từ chối hoàn tất và hiển thị lỗi tại các trường chưa đạt.
3. Quay lại bước #9 trong Basic Flow.

**Exception flow 7: Mất kết nối mạng hoặc lỗi gửi dịch vụ email**
1. Quá trình gửi mã hoặc tạo tài khoản vượt quá thời gian timeout.
2. **Hệ thống** thông báo lỗi kết nối và hướng dẫn người dùng thử lại mà không báo đăng ký thành công.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Tên đăng nhập (`username`) có độ dài từ 6 đến 30 ký tự; chỉ bao gồm chữ cái Latin, chữ số, dấu gạch dưới `_` và gạch ngang `-` theo [DEC-07](../../../../decisions/account-lifecycle.md).
  - **BR-02**: Mật khẩu bắt buộc có độ dài từ 8 đến 128 ký tự, có ít nhất một chữ hoa, một chữ thường, một chữ số và một ký tự đặc biệt (`!@#$%^&*...`) theo [DEC-07](../../../../decisions/account-lifecycle.md).
  - **BR-03**: Thông tin cá nhân: Họ tên bắt buộc (1–100 ký tự sau khi cắt khoảng trắng thừa); Ngày sinh, Giới tính (tùy chọn Nam/Nữ/Khác, không tự gán mặc định) và Địa chỉ (tùy chọn, tối đa 255 ký tự sau khi cắt khoảng trắng thừa) theo [DEC-07](../../../../decisions/account-lifecycle.md). Nếu cung cấp ngày sinh, ngày sinh không được ở tương lai.
  - **BR-04**: Thứ tự quy trình đăng ký: Nhập tên đăng nhập ➔ Mật khẩu ➔ Email ➔ Xác minh mã qua email ➔ Nhập thông tin cá nhân ➔ Máy chủ chính thức tạo tài khoản. Không tạo tài khoản trước khi hoàn tất xác minh email.
  - **BR-05**: Yêu cầu đăng ký tạm có thời hạn tối đa 30 phút; mã xác minh gửi qua email có thời hạn 10 phút. Việc gửi lại mã không kéo dài thời hạn 30 phút của yêu cầu tạm theo [DEC-06](../../../../decisions/password-reset-and-email.md).
  - **BR-06**: Tài khoản tạo qua kênh công khai luôn được gán vai trò `USER` với trạng thái `ACTIVE`; không tự động đăng nhập hay cấp phiên User Client từ use-case này.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Toàn bộ dữ liệu nhạy cảm (mật khẩu, mã xác minh) phải được mã hóa khi truyền tải; không lưu mật khẩu dạng rõ và không ghi thông tin nhạy cảm vào nhật ký hệ thống.
  - **NFR-02**: Giao diện phải phản hồi tức thì trạng thái kiểm tra (loading), vô hiệu hóa nút gửi lặp trong khi đang xử lý yêu cầu.
