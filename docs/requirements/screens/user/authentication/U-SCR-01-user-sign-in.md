# ĐẶC TẢ GIAO DIỆN — U-SCR-01

- **Màn hình**: Đăng nhập vào User Client.
- **Use case**: [UC-USR-AUTH-02 — Đăng nhập vào User Client](../../../use-cases/user/authentication/UC-USR-AUTH-02-sign-in.md).
- **Người viết / cập nhật**: baonguyen1776 / 2026-11-04.
- **Trạng thái**: Draft.
- **Mockup**: [Framer — Bao → U-SCR-01](https://framer.com/projects/Early-Persimmon--1Tzz5NWFPHgQOJcXaGB4-jirMF?node=MUWi3e89o).

## Thành phần và thao tác

- Form gồm **Tên đăng nhập**, **Mật khẩu**, nút hiện/ẩn mật khẩu, **Đăng nhập**, **Quên mật khẩu?** và **Tạo tài khoản**. Nhãn trường luôn hiện; mật khẩu được che mặc định.
- Tab/Shift+Tab di chuyển qua các điều khiển; Enter trong form thực hiện cùng hành vi với nút Đăng nhập. Nút mắt có tên truy cập **Hiện mật khẩu**/**Ẩn mật khẩu**, chỉ thay cách hiển thị, không thay giá trị. Không chặn paste mật khẩu.
- Khi mở mới, đăng xuất, quay về sau đăng ký/xác minh/đổi mật khẩu thành công hoặc mở lại sau khi bỏ dở, cả hai ô để trống. Không khôi phục mật khẩu, quyền tạm hoặc focus của màn hình đã bỏ dở.
- Chọn Tạo tài khoản mở `U-SCR-02`, không mang mật khẩu sang form đăng ký. Chọn Quên mật khẩu mở `U-SCR-04`, có thể mang tên đăng nhập làm gợi ý; không tự gửi email khi chỉ điều hướng.

## Trạng thái giao diện

| Trạng thái | Hiển thị và dữ liệu form | Thao tác tiếp theo |
| --- | --- | --- |
| Mặc định | Hai ô trống; mật khẩu che; nút **Đăng nhập** | Nhập thông tin và gửi |
| Thiếu thông tin | **“Vui lòng nhập tên đăng nhập.”** / **“Vui lòng nhập mật khẩu.”** dưới từng ô còn thiếu; focus ô lỗi đầu tiên | Bổ sung thông tin; chưa gửi yêu cầu lên Server |
| Đang xác thực | Nút đổi thành **Loading...** và bị vô hiệu hóa; chặn cả bấm nút lẫn Enter gửi lặp. Tạm vô hiệu hóa sửa trường, nút mắt và điều hướng đăng ký/quên mật khẩu trong lúc chờ | Chờ kết quả; cửa sổ vẫn phản hồi, không đóng băng ứng dụng |
| Sai thông tin | **“Tên đăng nhập hoặc mật khẩu không đúng.”** tại vùng lỗi chung; giữ tên đăng nhập, xóa mật khẩu | Focus vùng lỗi chung để công cụ hỗ trợ đọc được; người dùng tự chọn ô cần sửa, không tô lỗi/focus riêng mật khẩu và không tự chuyển đăng ký |
| Bị khóa tạm do thử sai | Thông báo phải chờ, kèm thời gian còn lại từ mốc khóa do Server cung cấp; vô hiệu hóa gửi lại | Cho thử lại khi được phép; hết đếm ngược không được tự coi tài khoản đã thoát mọi điều kiện khóa |
| Không được phép đăng nhập | Các trạng thái khóa/xóa chỉ hiển thị lý do được Server cho phép công bố | Không mở khung chính hoặc tự chuyển sang bước xác minh/đặt mật khẩu |
| Mất kết nối/timeout | **“Chưa thể xác nhận đăng nhập. Kiểm tra kết nối rồi thử lại.”**; giữ tên đăng nhập, xóa mật khẩu | Trả nút về **Đăng nhập**, cho chủ động thử lại; không tự gửi lại yêu cầu |
| Phiên hiện tại hết hạn/bị thu hồi | Quay về login với hai ô trống và lý do phù hợp do Server cung cấp | Đăng nhập lại chủ động; không tự điền mật khẩu hoặc giành lại phiên |
| Trở về sau thao tác thành công | Thông báo đăng ký/xác minh/đổi mật khẩu thành công theo đúng ngữ cảnh; cả hai ô trống | Người dùng tự đăng nhập; không đồng thời hiển thị banner thành công và lỗi trái ngược |
| Tài khoản do Admin tạo chưa xác minh email | **“Email của tài khoản chưa được xác minh.”**, lựa chọn **Xác minh email** / **Quay lại đăng nhập** | Chỉ cho tiếp tục theo kết quả xác thực và kiểm tra trạng thái từ Server; chọn xác minh mở `U-SCR-03` |
| Mật khẩu tạm không hợp lệ | Khi Server xác định được ngữ cảnh: **“Mật khẩu tạm không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu lại.”**; nếu không, dùng lỗi xác thực chung | Cho chủ động chọn Quên mật khẩu; không mở màn đặt mật khẩu bằng quyền không hợp lệ |
| Quyền chuyển tiếp hết hạn | Khi được đưa về từ luồng liên quan: **“Yêu cầu đã hết hạn. Vui lòng bắt đầu lại.”**, cả hai ô trống | Người dùng chủ động bắt đầu yêu cầu mới |

- Sau khi có kết quả hoặc timeout mà vẫn ở login, trả nhãn **Đăng nhập** và khôi phục các điều khiển phù hợp. Riêng gửi lại tiếp tục bị chặn khi Server còn áp dụng khóa tạm.
- Thông báo có chữ và trạng thái focus rõ, không chỉ dùng màu sắc. Vùng lỗi xác thực chung và lỗi từng trường được phân biệt theo bảng trên.
- Thời gian hiển thị chỉ phản ánh quyết định của Server. Giới hạn thử sai theo [DEC-07](../../../../decisions/account-lifecycle.md#điều-kiện-tài-khoản-khi-đăng-nhập); thời hạn mã/mật khẩu tạm và quyền chuyển tiếp theo [DEC-06](../../../../decisions/password-reset-and-email.md).
- Đăng nhập User Client trên thiết bị khác không làm hiện banner “bị đăng xuất vì đăng nhập nơi khác”. Phiên User Client hiện tại chỉ quay về login khi chính phiên đó thực sự hết hạn/bị thu hồi.

## Sau đăng nhập

- Nhận phiên User Client hợp lệ thì mở khung chính với `U-SCR-07`; không giữ người dùng ở login để chờ khôi phục E2EE.
- Từ khung chính, người dùng có thể khôi phục lịch sử ngay hoặc để sau. Hủy/bỏ qua tại `U-SCR-06` giữ phiên đăng nhập; không tự xóa/ghi đè backup hoặc thông báo lịch sử đã được khôi phục.
- Trạng thái sẵn sàng gửi/nhận tin mã hóa và lịch sử chưa khôi phục thuộc màn hình chat/E2EE theo [DEC-05](../../../../decisions/group-permissions-and-e2ee.md#thiết-lậpkhôi-phục-khóa-sau-đăng-nhập), không được diễn giải thành đăng nhập thất bại.

## Phạm vi mockup hiện có

Framer hiện có ba bản tĩnh bằng layer chỉnh sửa được: **Mặc định**, **Loading** và **Sai thông tin**. Các trạng thái còn lại trong bảng chưa dựng mockup; các bản hiện có chưa nối tương tác hoặc Server.

Thiết kế tham chiếu JavaFX/FXML/CSS + AtlantaFX theo DEC-01; không phải kết quả kiểm thử control JavaFX chạy thật. Các quy tắc bàn phím, gửi lặp, focus và timeout ở trên là yêu cầu triển khai.
