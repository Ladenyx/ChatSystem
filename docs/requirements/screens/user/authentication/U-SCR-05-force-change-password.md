# ĐẶC TẢ GIAO DIỆN — U-SCR-05

- **Màn hình**: Đặt mật khẩu mới.
- **Chức năng liên quan**: [UC-USR-AUTH-03 — Khôi phục mật khẩu và đặt lại mật khẩu bắt buộc](../../../use-cases/user/authentication/UC-USR-AUTH-03-forgot-password.md), tạo tài khoản hoặc đặt lại mật khẩu bởi Admin (`A-02`, `A-04`).
- **Người viết / cập nhật**: baonguyen1776 / 2026-11-08.
- **Trạng thái**: Draft.
- **Phạm vi**: Xác nhận mật khẩu tạm khi đến từ luồng quên mật khẩu và đặt mật khẩu mới. Nếu mật khẩu tạm đã được xác thực tại màn đăng nhập, người dùng bắt đầu tại bước đặt mật khẩu mới.

## Thành phần và thao tác

- Khi đến từ `U-SCR-04`, hiển thị ô **Mật khẩu tạm** và nút **Xác nhận**. Mật khẩu tạm được che mặc định bằng ký tự `*`; xóa khỏi ô sau khi xác nhận thành công. Server kiểm tra mật khẩu tạm với tài khoản trong lượt khôi phục, thời hạn và điều kiện tài khoản; chỉ khi hợp lệ mới cho tiếp tục đặt mật khẩu mới.
- Khi đến từ `U-SCR-01` sau khi Server đã xác thực mật khẩu tạm và cấp quyền đặt mật khẩu, bỏ qua bước nhập lại mật khẩu tạm.
- Ở bước đặt mật khẩu mới, hiển thị ô **Mật khẩu mới**, ô **Nhập lại mật khẩu** và nút **Lưu mật khẩu**. Có **Quay lại đăng nhập** để rời màn hình.
- Hai ô được che mặc định bằng ký tự `*`; người dùng có thể chọn hiện hoặc ẩn mật khẩu. Wireframe dùng icon mắt gạch chéo khi mật khẩu đang được che; thư viện icon cho ứng dụng chưa được chốt. Enter thực hiện cùng hành động với nút lưu; Tab/Shift+Tab di chuyển giữa các điều khiển.
- Hiển thị điều kiện mật khẩu và cập nhật phản hồi khi người dùng nhập. Kiểm tra hai ô khớp nhau; Client không gửi dữ liệu chưa hợp lệ. Server kiểm tra lại trước khi lưu.
- Mật khẩu có độ dài **8–128 ký tự**, có ít nhất một chữ hoa, một chữ thường, một chữ số và một ký tự đặc biệt. Hiển thị từng điều kiện và cập nhật khi người dùng nhập. Ký tự đặc biệt là ký hiệu ASCII in được không phải chữ hoặc số, không tính khoảng trắng; ví dụ `!`, `@`, `#`, `$`, `%`, `^`, `&`, `*`. Giữ nguyên hoa thường và khoảng trắng của mật khẩu theo DEC-07.
- Nếu hiển thị thời gian còn lại, thời gian này dựa trên hạn do Server cung cấp; Server quyết định quyền đặt mật khẩu còn hiệu lực hay không.

## Trạng thái giao diện

| Trạng thái | Hiển thị và thao tác |
| --- | --- |
| Nhập mật khẩu tạm | Với luồng từ `U-SCR-04`, ô mật khẩu tạm trống và được che; người dùng nhập rồi chọn **Xác nhận**. |
| Thiếu mật khẩu tạm | Hiển thị **“Nhập mật khẩu tạm”**; không gửi yêu cầu. |
| Đang xác nhận | Nút hiển thị **“Đang xác nhận...”**; chặn gửi lặp bằng nút hoặc Enter đến khi có kết quả. |
| Mật khẩu tạm không hợp lệ | Hiển thị **“Mật khẩu tạm không hợp lệ”**; chưa cho đặt mật khẩu mới. Người dùng có thể thử lại khi Server cho phép. |
| Mật khẩu tạm hết hạn | Hiển thị **“Mật khẩu tạm đã hết hạn”**; người dùng bắt đầu lại yêu cầu khôi phục phù hợp. |
| Đặt mật khẩu mới | Hai ô trống, mật khẩu được che; chỉ hiển thị sau khi Server xác thực mật khẩu tạm và cấp quyền đặt mật khẩu. |
| Thiếu mật khẩu | Hiển thị **“Nhập mật khẩu mới”** hoặc **“Nhập lại mật khẩu”** tại ô tương ứng. |
| Mật khẩu chưa hợp lệ | Hiển thị điều kiện chưa đạt dưới ô mật khẩu; người dùng sửa trước khi lưu. |
| Hai mật khẩu không khớp | Hiển thị **“Mật khẩu chưa khớp”** dưới ô nhập lại. |
| Đang lưu | Nút hiển thị **“Đang lưu...”**; chặn lưu lặp bằng nút hoặc Enter đến khi có kết quả. Cửa sổ vẫn phản hồi. |
| Quyền hết hạn hoặc bị thu hồi | Hiển thị **“Yêu cầu đã hết hạn”** hoặc thông báo phù hợp từ Server; không lưu mật khẩu mới. Người dùng bắt đầu lại luồng phù hợp. |
| Không thể xác nhận kết quả | Hiển thị **“Chưa xác nhận được kết quả”**; không tự gửi lại hoặc khẳng định mật khẩu đã hay chưa được thay đổi. |
| Lưu thành công | Chỉ thông báo thành công sau khi Server xác nhận; trở về `U-SCR-01` với hai ô đăng nhập trống. Người dùng tự đăng nhập bằng mật khẩu mới. |

- **Quay lại đăng nhập**: rời luồng đặt mật khẩu, xóa dữ liệu mật khẩu khỏi màn hình và trở về `U-SCR-01`. Nếu chưa lưu thành công, mật khẩu cũ và các phiên hiện tại không tự bị thay đổi hoặc thu hồi chỉ vì người dùng rời màn hình. Khi đang chờ kết quả lưu, không khẳng định kết quả nếu chưa có phản hồi.
- Khi đến từ luồng quên mật khẩu, Client giữ thông tin nhận diện lượt khôi phục trong phiên hiện tại để Server xác định yêu cầu; thông tin đó không tự cấp quyền đặt mật khẩu. Nếu rời ứng dụng rồi mở lại, người dùng bắt đầu lại luồng phù hợp hoặc nhập mật khẩu tạm còn hiệu lực tại màn đăng nhập.
- Mật khẩu tạm và quyền đặt mật khẩu dùng chung hạn 15 phút từ lúc Server phát hành mật khẩu tạm; mở màn hình không gia hạn. Nếu người dùng mở lại ứng dụng sau khi bỏ dở, không tự khôi phục form hoặc quyền tạm theo DEC-06.
- [Wireframe Figma](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=126-3550) là bản tĩnh; chưa kiểm thử JavaFX.
