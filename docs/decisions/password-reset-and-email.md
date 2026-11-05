# Mật khẩu và email

**DEC-06 — Đề xuất:** các quy tắc và con số chưa được kết luận chung toàn mục.

- Gửi mật khẩu tạm ngẫu nhiên qua email, dùng một lần, hết hạn **15 phút kể từ khi Server phát hành**; chỉ cho đặt mật khẩu mới, chưa vào chat. Quyền đặt mật khẩu mới dùng chung mốc hết hạn, không được gia hạn khi mở màn hình hoặc thao tác.
- Giữ mật khẩu cũ và phiên hiện tại đến khi lưu mật khẩu mới thành công; sau đó vô hiệu mật khẩu tạm, thu hồi phiên và yêu cầu đăng nhập lại.
- Email lỗi không làm mất khả năng đăng nhập cũ. Không ghi mật khẩu tạm/hiện tại vào log.
- Yêu cầu gửi mật khẩu tạm giãn cách **60 giây**, tối đa **5 yêu cầu/giờ/tài khoản**.
- Admin reset dùng cùng luồng email, không đặt mật khẩu lâu dài thay người dùng. Reset không mở khóa tài khoản bị khóa và không thay mã khôi phục E2EE.
- Với yêu cầu quên mật khẩu từ User Client, người dùng nhập tên đăng nhập, xem email gợi ý đã che và nhập lại email đầy đủ. Server kiểm tra email khớp trước khi gửi mật khẩu tạm đến email đã đăng ký; giới hạn tra cứu và thử email theo cấu hình Server. Chi tiết tại [UC-U05](../requirements/use-cases/user/authentication/UC-U05-user-password-recovery.md).

## Quyền đặt mật khẩu mới

- Mật khẩu tạm có thể được xác thực tại `U-SCR-01` hoặc tại bước xác nhận trên `U-SCR-05` sau yêu cầu quên mật khẩu. Sau khi xác thực hợp lệ và kiểm tra điều kiện tài khoản theo [DEC-07](account-lifecycle.md#điều-kiện-tài-khoản-khi-đăng-nhập), chỉ cấp quyền thực hiện bước đặt mật khẩu, không cấp phiên User Client hoặc phiên quản trị. Khóa/xóa tài khoản vẫn chặn quyền này.
- Khi lưu mật khẩu mới, Server kiểm tra lại quyền, hạn dùng và trạng thái tài khoản. Quyền đã hết hạn hoặc bị thu hồi không được tiếp tục; người dùng phải bắt đầu lại yêu cầu hợp lệ.
- Chỉ sau khi lưu thành công mới vô hiệu hóa mật khẩu cũ, mật khẩu tạm và quyền đặt mật khẩu, thu hồi các phiên của tài khoản và yêu cầu đăng nhập lại. Lỗi/hủy trước khi lưu thành công không tự thay mật khẩu hay thu hồi phiên đang hoạt động.
- Thoát ứng dụng không lưu quyền tạm để tự tiếp tục ở lần mở sau. Mật khẩu tạm chỉ còn dùng được trong hạn gốc và khi chưa hoàn tất việc đổi mật khẩu.

## Đăng ký công khai

- Người dùng nhập username, mật khẩu, email; xác minh email; sau đó nhập thông tin cá nhân và yêu cầu hoàn tất. Server chỉ tạo tài khoản sau khi kiểm tra đầy đủ thông tin và kết quả xác minh còn hiệu lực; xác minh email không tự tạo tài khoản hoặc phiên chat.
- Trước khi tạo tài khoản, Server quản lý yêu cầu đăng ký tạm với hạn tối đa **30 phút kể từ khi tạo**. Mã xác minh có hạn **10 phút** và chỉ được dùng khi yêu cầu tương ứng còn hiệu lực; gửi lại mã không gia hạn yêu cầu. Sau xác minh, quyền hoàn tất chỉ phục vụ yêu cầu đó và không vượt quá hạn 30 phút.
- Khi đang xác minh đăng ký, người dùng có thể sửa email mà không rời luồng hoặc gia hạn yêu cầu tạm; chi tiết tại [UC-U01](../requirements/use-cases/user/authentication/UC-U01-user-sign-up.md) và [luồng xác minh email](../requirements/use-cases/user/authentication/UC-U-VERIFY-EMAIL.md).
- Khi người dùng hủy hoặc rời luồng, Client yêu cầu Server hủy yêu cầu tạm. Khi Server xác nhận hủy, mã của yêu cầu đó hết hiệu lực. Nếu Server không nhận được yêu cầu, dữ liệu tạm hết hạn theo mốc 30 phút; Client không khẳng định đã hủy khi chưa nhận xác nhận. Sau khi hủy hoặc hết hạn, bắt đầu đăng ký lại từ đầu.
- Nếu email đang gắn với yêu cầu tạm còn hạn, người dùng có thể gửi yêu cầu mới cùng email, kể cả dùng lại username của yêu cầu tạm cùng email. Mã mới gắn với yêu cầu mới; chỉ nhập trùng không hủy yêu cầu cũ. Khi tạo tài khoản thành công, Server hủy các yêu cầu tạm cũ cùng email và vô hiệu mã liên quan.
- Server kiểm tra email chưa thuộc tài khoản đã tạo khi bắt đầu đăng ký hoặc sửa email đang đăng ký; nếu đã dùng, người dùng cần nhập email khác. Server kiểm tra lại tính duy nhất khi hoàn tất; chỉ một yêu cầu được tạo tài khoản với cùng username/email. Không ghi đè tài khoản đã tạo; việc hủy hoặc hết hạn yêu cầu tạm không xóa tài khoản đã tạo. Kiểm tra/hủy/hoàn tất đồng thời phải bảo đảm các quy tắc này.

## Xác minh email

- Mã xác minh gồm **6 chữ số** `0`–`9`, gắn với đúng yêu cầu, dùng một lần và hết hạn sau **10 phút kể từ khi Server phát hành**. Email gửi mã thông báo thời hạn này; màn hình xác minh không hiển thị hạn mã. Với đăng ký, mã chỉ có hiệu lực khi yêu cầu tạm còn hiệu lực, nên thời gian sử dụng thực tế có thể ngắn hơn.
- Người dùng có thể yêu cầu gửi lại đến cùng email sau tối thiểu **60 giây kể từ lần phát hành trước**. Trong đăng ký, khi đổi sang email khác, áp dụng thời gian chờ **30 giây** theo quy tắc trên. Khi Server phát hành mã mới, mã cũ của yêu cầu đó hết hiệu lực; gửi lại không kéo dài thời hạn yêu cầu đăng ký tạm.
- Sau **5 lần nhập sai** được Server ghi nhận, mã hiện tại hết hiệu lực. Người dùng có thể yêu cầu mã mới khi Server cho phép; không tự khóa tài khoản vì nhập sai mã xác minh. Mã sai định dạng bị Client từ chối trước khi gửi không tính vào bộ đếm Server.
- Server quản lý hạn mã, số lần thử và thời gian gửi lại; đóng/mở ứng dụng không đặt lại các giới hạn. Server giới hạn việc gửi mã theo cấu hình để ngăn lạm dụng, áp dụng cả khi tạo nhiều yêu cầu đăng ký mới cho cùng email hoặc từ cùng nguồn gửi; số lần và thời gian giới hạn chưa được chốt. Đăng ký lại vẫn được phép, nhưng gửi mã có thể phải chờ nếu chạm giới hạn này.
- Khi đổi email, chỉ cập nhật email mới sau khi xác minh thành công và kiểm tra điều kiện tài khoản; hủy hoặc thất bại trước khi cập nhật không thay email cũ.

**Còn mở:** SMTP, reset đồng thời, thư đến trễ, retry/outbox và phiên đặt mật khẩu hạn chế. Luồng admin tạo tài khoản mới cần đặc tả riêng vì chưa có mật khẩu cũ.
