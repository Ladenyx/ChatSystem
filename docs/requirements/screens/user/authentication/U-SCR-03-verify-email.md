# ĐẶC TẢ GIAO DIỆN — U-SCR-03

- **Màn hình**: Xác minh email.
- **Use case**: [UC-U-VERIFY-EMAIL — Xác minh email](../../../use-cases/user/authentication/UC-U-VERIFY-EMAIL.md).
- **Người viết / cập nhật**: baonguyen1776 / 2026-11-04.
- **Trạng thái**: Draft.
- **Ngữ cảnh**: Đăng ký tài khoản, xác minh email mới khi cập nhật hồ sơ hoặc kích hoạt tài khoản do Admin tạo.

## Thành phần và thao tác

- Hiển thị hướng dẫn kiểm tra email, địa chỉ nhận được che một phần, **6 ô mã xác minh**, nút **Xác minh**, **Gửi lại mã** và **Quay lại**. Trong luồng đăng ký, có thêm **Đổi email** để sửa địa chỉ mà không rời đăng ký.
- Mỗi ô nhận một chữ số `0`–`9`; chấp nhận `0` ở đầu và cho phép dán mã 6 chữ số. Chưa nhập đủ 6 ô thì vô hiệu hóa **Xác minh** và không gửi bằng Enter. Khi đã nhập đủ, người dùng chọn **Xác minh** hoặc nhấn Enter; không tự gửi mã.
- Hiển thị hạn mã theo thông tin do Server cung cấp; thời gian trên Client chỉ mang tính hướng dẫn, Server quyết định mã còn hợp lệ. Sau 60 giây kể từ lần phát hành trước, người dùng có thể chủ động yêu cầu gửi lại nếu Server cho phép.
- Tab/Shift+Tab di chuyển giữa các điều khiển; lỗi được trình bày bằng chữ, không chỉ bằng màu sắc.

## Trạng thái giao diện

| Trạng thái | Hiển thị và thao tác |
| --- | --- |
| Mặc định | Hướng dẫn kiểm tra email; 6 ô mã trống, nút **Xác minh** bị vô hiệu hóa; **Gửi lại mã** thể hiện thời gian chờ khi chưa được phép. |
| Mã chưa đủ | Nút **Xác minh** vẫn bị vô hiệu hóa; không gửi yêu cầu lên Server. |
| Đang xác minh | Nút thể hiện **“Đang xác minh”**; vô hiệu hóa gửi xác minh lặp bằng nút hoặc Enter cho đến khi có kết quả. Cửa sổ vẫn phản hồi. |
| Mã không đúng | Hiển thị **“Mã xác thực không đúng”**. Cho phép sửa và gửi lại nếu mã vẫn còn hiệu lực. |
| Sai 5 lần | Hiển thị **“Mã không còn hiệu lực”**. Không cho dùng lại mã cũ; có thể yêu cầu mã mới khi hết thời gian chờ. Không thông báo tài khoản bị khóa. |
| Mã hết hạn hoặc bị thay thế | Hiển thị **“Mã đã hết hạn hoặc không còn hiệu lực”**. Chỉ cho gửi lại khi yêu cầu còn hiệu lực và Server cho phép. |
| Đang chờ gửi lại | Hiển thị thời gian chờ; chưa cho gửi lại. Đếm ngược chỉ phản ánh thời hạn do Server cung cấp. |
| Bị giới hạn gửi mã | Hiển thị **“Chưa thể gửi mã. Vui lòng thử lại sau”**; cho thử lại khi Server cho phép. |
| Đang gửi lại | Thể hiện đang gửi, ngăn yêu cầu gửi lại trùng. Khi Server xác nhận đã cấp mã mới, thông báo kiểm tra email và dùng mã mới. |
| Không gửi được email/mất kết nối | Hiển thị **“Đã có lỗi xảy ra. Vui lòng thử lại”**. Không tự gửi lặp; nếu chưa rõ kết quả cấp mã, không khẳng định mã cũ còn hiệu lực. |
| Yêu cầu hoặc quyền hết hạn | Hiển thị **“Yêu cầu đã hết hạn”**. Với đăng ký, quay về bắt đầu đăng ký; với đổi email, quay về hồ sơ nếu phiên còn hợp lệ hoặc về đăng nhập nếu phiên hết hiệu lực. Với tài khoản do Admin tạo, điều hướng thuộc luồng kích hoạt chưa được chốt. |
| Email mới không thể sử dụng | Trong luồng đổi email, hiển thị **“Không thể sử dụng email này”**. Không báo đã đổi thành công; quay về luồng hồ sơ. |
| Xác minh thành công | Với đăng ký, chuyển sang bước thông tin cá nhân của `U-SCR-02`; chưa báo tạo tài khoản thành công. Với đổi email, sau khi Server xác nhận cập nhật, thông báo thành công và trở về `U-DLG-01`. Với tài khoản do Admin tạo, bước tiếp theo thuộc luồng kích hoạt chưa được chốt. |

- **Quay lại**: với đăng ký, rời luồng và yêu cầu Server hủy đăng ký tạm; với đổi email, quay về hồ sơ mà chưa thay email cũ. Với tài khoản do Admin tạo, thao tác không xóa tài khoản; điều hướng thuộc luồng kích hoạt chưa được chốt. Nếu chưa nhận xác nhận hủy, không thông báo đã hủy thành công.
- **Đổi email khi đăng ký**: quay lại bước email, giữ tên đăng nhập và mật khẩu trong phiên đăng ký hiện tại. Người dùng được sửa email ngay và chờ đủ 30 giây từ lần Server phát hành mã gần nhất để gửi mã đến email khác; hiển thị thời gian còn lại nếu chưa đủ. Sau khi Server chấp nhận email mới, mã cũ hết hiệu lực; nhập mã gửi đến địa chỉ mới. Thời hạn 30 phút của yêu cầu đăng ký không được gia hạn.
- Sau kết quả hoặc lỗi mà vẫn ở màn xác minh, khôi phục các điều khiển phù hợp. Giới hạn và thời gian chờ được Server quản lý; đóng/mở ứng dụng không tự đặt lại.
- Đây là đặc tả trạng thái; chưa có mockup hoặc kiểm thử JavaFX. Thao tác tạo tài khoản và cập nhật hồ sơ thuộc luồng tương ứng, không gộp vào màn hình này.
