# ĐẶC TẢ GIAO DIỆN — U-SCR-04

- **Màn hình**: Quên mật khẩu.
- **Use case**: [UC-U05 — Khôi phục mật khẩu](../../../use-cases/user/authentication/UC-U05-user-password-recovery.md).
- **Người viết / cập nhật**: baonguyen1776 / 2026-11-04.
- **Trạng thái**: Draft.
- **Phạm vi**: Xác nhận thông tin và yêu cầu gửi mật khẩu tạm; xác nhận mật khẩu tạm và đặt mật khẩu mới thuộc màn hình tiếp theo.

## Thành phần và thao tác

1. **Tên đăng nhập:** nhập tên đăng nhập và chọn **Tiếp tục**.
2. **Email:** xem gợi ý email được che một phần, nhập lại email đầy đủ và chọn **Gửi**.
3. **Kiểm tra email:** sau khi Server tiếp nhận, hướng dẫn người dùng kiểm tra email; có hai nút **Tiếp tục** và **Gửi lại**. Người dùng chọn **Tiếp tục** để mở bước nhập mật khẩu tạm tại `U-SCR-05`.

- Hiển thị **Quay lại đăng nhập** để rời luồng. Enter thực hiện hành động chính của bước hiện tại; Tab/Shift+Tab di chuyển giữa các điều khiển. Không tự gửi yêu cầu khi mở màn hình.
- Email gợi ý do Server che trước khi gửi đến Client: hiển thị ký tự đầu của phần trước `@`, thay phần còn lại bằng `***`, giữ tên miền để người dùng nhận ra, ví dụ `b***@gmail.com`. Email đầy đủ người dùng phải tự nhập; Server kiểm tra email và chỉ gửi mật khẩu tạm đến email gắn với tài khoản khi yêu cầu đủ điều kiện.

## Trạng thái giao diện

| Trạng thái | Hiển thị và thao tác |
| --- | --- |
| Nhập tên đăng nhập | Hiển thị ô **Tên đăng nhập** và nút **Tiếp tục**. |
| Thiếu tên đăng nhập | Hiển thị **“Nhập tên đăng nhập”**; không gửi yêu cầu. |
| Nhập lại email | Hiển thị email gợi ý được che một phần, ô **Email** và nút **Gửi**. |
| Thiếu hoặc sai định dạng email | Hiển thị **“Nhập email hợp lệ”**; không gửi yêu cầu. |
| Không thể tiếp tục hoặc thông tin không khớp | Hiển thị **“Thông tin chưa đúng”**; không báo đã gửi mật khẩu tạm. |
| Bị giới hạn tra cứu hoặc thử email | Hiển thị **“Thử lại sau”**; chỉ cho thử lại khi Server cho phép. |
| Đang xử lý | Nút chính thể hiện đang xử lý; chặn gửi lặp bằng nút hoặc Enter đến khi có kết quả. Cửa sổ vẫn phản hồi. |
| Kiểm tra email | Hiển thị **“Kiểm tra email”**, cùng hai nút **Tiếp tục** và **Gửi lại**. Không khẳng định email đã đến hộp thư. |
| Chờ gửi lại | Hiển thị thời gian chờ, ví dụ **“Gửi lại sau 00:45”**; chưa cho gửi lại và không tự gửi. Server quyết định khả năng gửi theo DEC-06, kể cả khi đóng/mở ứng dụng. |
| Không thể hoàn tất | Hiển thị **“Có lỗi xảy ra”** khi Client không nhận được phản hồi phù hợp; không tự gửi lặp hoặc khẳng định Server chưa xử lý. |
| Quay lại | Về `U-SCR-01`; người dùng tự nhập thông tin đăng nhập. |

- Client chỉ mở `U-SCR-05` khi người dùng chọn **Tiếp tục**; việc mở màn hình không tự xác thực mật khẩu tạm hoặc cấp quyền đặt mật khẩu. Người dùng vẫn có thể quay về `U-SCR-01` và đăng nhập bằng tên đăng nhập cùng mật khẩu tạm.
- Quy tắc thời gian và giới hạn gửi theo [DEC-06](../../../../decisions/password-reset-and-email.md). Bộ đếm trên Client không thay thế kiểm tra của Server.
- [Wireframe Figma](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=122-3550) là bản tĩnh; chưa kiểm thử JavaFX.
