# Mật khẩu và email

**DEC-06 — Đề xuất:** các quy tắc và con số chưa được kết luận chung toàn mục.

- Gửi mật khẩu tạm ngẫu nhiên qua email, dùng một lần, hết hạn **15 phút**; chỉ cho đặt mật khẩu mới, chưa vào chat.
- Giữ mật khẩu cũ và phiên hiện tại đến khi lưu mật khẩu mới thành công; sau đó vô hiệu mật khẩu tạm, thu hồi phiên và yêu cầu đăng nhập lại.
- Email lỗi không làm mất khả năng đăng nhập cũ. Không ghi mật khẩu tạm/hiện tại vào log.
- Giãn cách **60 giây**, tối đa **5 yêu cầu/giờ/tài khoản**.
- Admin reset dùng cùng luồng email, không đặt mật khẩu lâu dài thay người dùng. Reset không mở khóa tài khoản bị khóa và không thay mã khôi phục E2EE.

**Còn mở:** SMTP, reset đồng thời, thư đến trễ, retry/outbox và phiên đặt mật khẩu hạn chế. Luồng admin tạo tài khoản mới cần đặc tả riêng vì chưa có mật khẩu cũ.
