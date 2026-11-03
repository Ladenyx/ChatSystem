# Mật khẩu và email

**DEC-06 — Đề xuất:** các quy tắc và con số chưa được kết luận chung toàn mục.

- Gửi mật khẩu tạm ngẫu nhiên qua email, dùng một lần, hết hạn **15 phút kể từ khi Server phát hành**; chỉ cho đặt mật khẩu mới, chưa vào chat. Quyền đặt mật khẩu mới dùng chung mốc hết hạn, không được gia hạn khi mở màn hình hoặc thao tác.
- Giữ mật khẩu cũ và phiên hiện tại đến khi lưu mật khẩu mới thành công; sau đó vô hiệu mật khẩu tạm, thu hồi phiên và yêu cầu đăng nhập lại.
- Email lỗi không làm mất khả năng đăng nhập cũ. Không ghi mật khẩu tạm/hiện tại vào log.
- Giãn cách **60 giây**, tối đa **5 yêu cầu/giờ/tài khoản**.
- Admin reset dùng cùng luồng email, không đặt mật khẩu lâu dài thay người dùng. Reset không mở khóa tài khoản bị khóa và không thay mã khôi phục E2EE.

## Quyền đặt mật khẩu mới

- Sau khi xác thực mật khẩu tạm hợp lệ và kiểm tra điều kiện tài khoản theo [DEC-07](account-lifecycle.md#điều-kiện-tài-khoản-khi-đăng-nhập), chỉ cấp quyền thực hiện bước đặt mật khẩu, không cấp phiên User Client hoặc phiên quản trị. Khóa/xóa tài khoản vẫn chặn quyền này.
- Khi lưu mật khẩu mới, Server kiểm tra lại quyền, hạn dùng và trạng thái tài khoản. Quyền đã hết hạn hoặc bị thu hồi không được tiếp tục; người dùng phải bắt đầu lại yêu cầu hợp lệ.
- Chỉ sau khi lưu thành công mới vô hiệu hóa mật khẩu cũ, mật khẩu tạm và quyền đặt mật khẩu, thu hồi các phiên của tài khoản và yêu cầu đăng nhập lại. Lỗi/hủy trước khi lưu thành công không tự thay mật khẩu hay thu hồi phiên đang hoạt động.
- Thoát ứng dụng không lưu quyền tạm để tự tiếp tục ở lần mở sau. Mật khẩu tạm chỉ còn dùng được trong hạn gốc và khi chưa hoàn tất việc đổi mật khẩu.

## Xác minh email

- Mã xác minh hết hạn **10 phút kể từ khi Server phát hành**; thời điểm mở ứng dụng hoặc đọc email không làm thay đổi hạn dùng. Mã đã hết hạn không được chấp nhận.
- Hết hạn thì người dùng chủ động yêu cầu mã mới theo chính sách gửi lại; không tự gửi email chỉ vì mở màn hình đăng nhập. Việc gửi lại và kiểm tra mã thuộc màn hình `U-SCR-03`.
- Chưa áp dụng tự xóa tài khoản chưa xác minh. Mã hết hạn không xóa tài khoản hoặc bắt người dùng đăng ký lại; sau đăng nhập hợp lệ có thể bắt đầu xác minh bằng mã mới.

**Còn mở:** SMTP, reset đồng thời, thư đến trễ, retry/outbox, cơ chế mã và giới hạn gửi lại mã xác minh. Luồng admin tạo tài khoản mới cần đặc tả riêng vì chưa có mật khẩu cũ.
