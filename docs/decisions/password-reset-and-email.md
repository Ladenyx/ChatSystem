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
- Đối với đăng ký công khai, trước khi xác minh thành công chỉ có yêu cầu đăng ký tạm, chưa tạo tài khoản đăng nhập. Nếu người dùng chủ động hủy hoặc rời luồng trước khi hoàn tất, Client yêu cầu Server hủy; khi Server xác nhận hủy, các mã của yêu cầu bị vô hiệu và username/email được giải phóng. Lần sau người dùng bắt đầu đăng ký mới, không dùng login để khôi phục yêu cầu cũ.
- Việc đóng ứng dụng hoặc mất kết nối không bảo đảm Server nhận được yêu cầu hủy. Server tự hết hạn yêu cầu đăng ký tạm sau thời hạn chưa được nhóm chốt, vô hiệu mã và giải phóng username/email; nếu người dùng đăng ký lại trước khi Server hủy hoặc yêu cầu hết hạn, thông tin có thể vẫn tạm bị giữ. Client không khẳng định đã hủy khi chưa nhận xác nhận từ Server.
- Hạn của mã xác minh và hạn của yêu cầu đăng ký tạm là hai mốc riêng: mã hết hạn không tự hủy yêu cầu còn hiệu lực; người dùng có thể yêu cầu mã mới theo chính sách gửi lại. Khi yêu cầu đã bị hủy/hết hạn thì mã không còn sử dụng được. Server phải xử lý việc hủy và hoàn tất đăng ký nhất quán: yêu cầu đã hủy/hết hạn không thể tạo tài khoản; nếu xác minh đã hoàn tất trước, lệnh hủy không xóa tài khoản đã tạo.
- Quy tắc hủy yêu cầu đăng ký công khai không xóa tài khoản đã được tạo, bao gồm tài khoản do Admin tạo. Với tài khoản do Admin tạo chưa xác minh, người dùng xác thực hợp lệ để tiếp tục xác minh; mã hết hạn có thể yêu cầu lại theo chính sách gửi lại. Việc gửi lại và kiểm tra mã thuộc `U-SCR-03`.

**Còn mở:** thời hạn yêu cầu đăng ký tạm, SMTP, reset đồng thời, thư đến trễ, retry/outbox, cơ chế mã và giới hạn gửi lại mã xác minh. Luồng admin tạo tài khoản mới cần đặc tả riêng vì chưa có mật khẩu cũ.
