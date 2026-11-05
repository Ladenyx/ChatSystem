# ĐẶC TẢ GIAO DIỆN — U-SCR-02

- **Màn hình**: Đăng ký tài khoản User Client.
- **Use case**: [UC-U01 — Đăng ký tài khoản](../../../use-cases/user/authentication/UC-U01-user-sign-up.md).
- **Người viết / cập nhật**: baonguyen1776 / 2026-11-04.
- **Trạng thái**: Draft.
- **Phạm vi**: Các bước nhập liệu đăng ký. Nhập mã và xử lý mã xác minh thuộc `U-SCR-03`.

## Trình tự các bước

| Bước | Thành phần chính | Tiếp tục khi |
| --- | --- | --- |
| 1 — Tên đăng nhập | Ô **Tên đăng nhập**, **Tiếp tục**, **Quay lại đăng nhập** | Username đúng định dạng và Server xác nhận có thể tiếp tục |
| 2 — Mật khẩu | Ô **Mật khẩu**, **Nhập lại mật khẩu**, điều khiển hiện/ẩn, **Quay lại**, **Tiếp tục** | Mật khẩu đáp ứng các điều kiện và hai ô khớp nhau |
| 3 — Email | Ô **Email**, **Quay lại**, **Gửi mã xác minh** | Email hợp lệ; Server chấp nhận yêu cầu đăng ký tạm để chuyển sang `U-SCR-03` |
| Xác minh email | Nhập mã trên `U-SCR-03` | Khi Server xác nhận email hợp lệ, chuyển sang bước thông tin cá nhân; chưa tạo tài khoản |
| 4 — Thông tin cá nhân | **Họ tên**, **Địa chỉ**, **Ngày sinh**, **Giới tính**, **Hoàn tất đăng ký** | Đã nhập các trường bắt buộc và dữ liệu hợp lệ; Server xác nhận tạo tài khoản thành công |

- Họ tên, ngày sinh và giới tính bắt buộc trước khi hoàn tất tạo tài khoản; địa chỉ tùy chọn. Việc cập nhật hồ sơ sau đăng nhập thuộc chức năng khác; bản này không quy định cấm sửa hồ sơ.
- Mật khẩu được che mặc định bằng ký tự `*`; điều khiển hiện/ẩn chỉ thay đổi cách hiển thị. Wireframe dùng icon mắt gạch chéo khi mật khẩu đang được che; thư viện icon cho ứng dụng chưa được chốt. Không hiển thị lại mật khẩu trong thông báo lỗi.
- Người dùng có thể quay lại bước nhập liệu trước khi xác minh. Khi đang xác minh, chọn **Đổi email** để sửa email mà vẫn tiếp tục đăng ký; hủy hoặc rời luồng sau khi tạo yêu cầu tạm được xử lý theo UC-U01. Không tự khôi phục form đang bỏ dở khi mở lại ứng dụng.
- Khi sửa email trong lúc xác minh, cho phép nhập ngay và hiển thị thời gian còn lại nếu chưa đủ 30 giây kể từ lần Server phát hành mã gần nhất. Nếu giữ nguyên địa chỉ, gửi lại mã theo thời gian chờ 60 giây.

## Phản hồi trong khi nhập

Các điều kiện và lỗi hiển thị ngay bên dưới trường tương ứng, cập nhật khi người dùng thay đổi dữ liệu. Điều kiện chưa đáp ứng được mô tả bằng chữ; điều kiện đã đáp ứng được nhận biết rõ, không chỉ dựa vào màu.

| Trường | Điều kiện và phản hồi |
| --- | --- |
| Tên đăng nhập | Bắt buộc, dài 6–30 ký tự; chỉ gồm chữ cái Latin, chữ số, `_`, `-`. Lỗi: **“Vui lòng nhập tên đăng nhập.”**, **“Tên đăng nhập phải có từ 6 đến 30 ký tự.”**, **“Chỉ dùng chữ cái không dấu, chữ số, dấu _ và dấu -.”** |
| Tên đăng nhập đã dùng | Khi định dạng hợp lệ, Client yêu cầu Server kiểm tra; kết quả mới nhất được hiển thị dưới ô. Nếu username đã thuộc tài khoản được tạo: **“Tên đăng nhập đã được đăng ký.”** Không tự kết luận đã đăng ký khi chỉ có yêu cầu tạm. |
| Mật khẩu | Bắt buộc; hiển thị các điều kiện: **8–128 ký tự**, **có chữ hoa**, **có chữ thường**, **có chữ số**, **có ký tự đặc biệt**. Ký tự đặc biệt là ký hiệu ASCII in được không phải chữ hoặc số, không tính khoảng trắng; ví dụ `!`, `@`, `#`, `$`, `%`, `^`, `&`, `*`. Cập nhật từng điều kiện khi nhập; nếu bỏ trống, hiển thị **“Vui lòng nhập mật khẩu.”** |
| Nhập lại mật khẩu | Bắt buộc; nếu bỏ trống, hiển thị **“Vui lòng nhập lại mật khẩu.”** Nếu chưa khớp, hiển thị **“Mật khẩu nhập lại chưa khớp.”** Cập nhật kết quả khi một trong hai ô thay đổi. |
| Email | Bắt buộc; đúng định dạng, tối đa 254 ký tự sau khi bỏ khoảng trắng đầu/cuối. Lỗi: **“Vui lòng nhập email.”**, **“Vui lòng nhập email hợp lệ.”**, **“Email không được vượt quá 254 ký tự.”** |
| Email đã đăng ký | Khi gửi yêu cầu hoặc đổi email đang đăng ký, Server kiểm tra email. Nếu email đã thuộc tài khoản được tạo, hiển thị **“Email đã được đăng ký”** và cho nhập email khác. Nếu chỉ có yêu cầu tạm, áp dụng luồng đăng ký lại trong UC-U01. |
| Họ tên | Bắt buộc, 1–100 ký tự sau khi bỏ khoảng trắng đầu/cuối. Lỗi: **“Vui lòng nhập họ tên.”**, **“Họ tên không được vượt quá 100 ký tự.”** |
| Địa chỉ | Tùy chọn; nếu nhập, tối đa 255 ký tự sau khi bỏ khoảng trắng đầu/cuối. Lỗi: **“Địa chỉ không được vượt quá 255 ký tự.”** |
| Ngày sinh | Bắt buộc; có thể chọn ngày, tháng, năm bằng lịch. Ngày phải hợp lệ và không ở tương lai. Lỗi: **“Vui lòng nhập ngày sinh.”**, **“Ngày sinh không hợp lệ.”**, **“Ngày sinh không được ở tương lai.”** |
| Giới tính | Bắt buộc chọn một trong hai lựa chọn **Nam** hoặc **Nữ**; không tự chọn mặc định. Nếu chưa chọn, hiển thị **“Vui lòng chọn giới tính.”** |

- Kiểm tra định dạng tại Client giúp người dùng sửa ngay. Kết quả username đã dùng cần Server; khi mất kết nối phải thông báo chưa kiểm tra được, không báo tên đã dùng hoặc còn trống. Server kiểm tra lại trước khi tạo tài khoản.
- Chỉ sử dụng kết quả kiểm tra tương ứng với giá trị username hiện tại; kết quả trả về chậm của giá trị cũ không được ghi đè kết quả mới.
- Các giới hạn của email và thông tin cá nhân theo [DEC-07](../../../../decisions/account-lifecycle.md). Client và Server kiểm tra cùng quy tắc; mật khẩu được giữ nguyên, không tự bỏ khoảng trắng.

## Trạng thái và điều hướng

| Trạng thái | Hành vi |
| --- | --- |
| Thiếu hoặc sai dữ liệu | Hiển thị lỗi cạnh trường cần sửa; chưa chuyển bước hoặc gửi dữ liệu không hợp lệ. |
| Đang chờ Server | Thể hiện đang xử lý và ngăn gửi trùng; không báo thành công khi chưa có xác nhận. |
| Không gửi được email | Thông báo chưa gửi được mã; nếu yêu cầu tạm đã được tạo, cho người dùng sang `U-SCR-03` để yêu cầu gửi lại. |
| Yêu cầu đăng ký hết hạn | Thông báo yêu cầu đã hết hạn; người dùng bắt đầu đăng ký lại từ đầu. |
| Username hoặc email không còn hợp lệ khi hoàn tất | Không báo tạo tài khoản thành công; thông báo thông tin không còn sử dụng được và hướng dẫn người dùng đăng nhập hoặc đăng ký lại. |
| Mất kết nối hoặc chưa rõ kết quả | Thông báo chưa xác định được kết quả và không tự gửi lặp; người dùng chủ động thử lại. |
| Hoàn tất thành công | Chỉ báo thành công khi Server xác nhận tạo tài khoản; về `U-SCR-01` với form đăng nhập trống. |

Mỗi yêu cầu tạm có hạn 30 phút; mã xác minh có hạn 10 phút và không còn hợp lệ nếu yêu cầu tương ứng hết hạn. Thứ tự tạo tài khoản sau bước thông tin cá nhân là yêu cầu mới cần đồng bộ với tài liệu chung.

[Wireframe Figma](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=102-3550) là bản tĩnh; chưa kiểm thử JavaFX.
