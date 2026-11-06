# ĐẶC TẢ GIAO DIỆN — U-SCR-06

- **Màn hình**: Thiết lập và khôi phục khóa E2EE.
- **Use case**: [UC-U25-KEYS — Thiết lập và khôi phục khóa E2EE](../../../use-cases/user/security/UC-U25-key-setup-recovery.md).
- **Người viết / cập nhật**: baonguyen1776 / 2026-11-05.
- **Trạng thái**: Draft.
- **Phạm vi**: Thiết lập, nhập và đổi mã khóa 6 số của tài khoản. Lớp phủ trong khung hội thoại là điểm mở màn hình này và cần được thống nhất với người phụ trách `U-SCR-07`.

## Thành phần và thao tác

- Người dùng có thể mở **Thiết lập mã khóa** từ menu **…** ở khung hội thoại. Vị trí menu cần được thống nhất trong thiết kế `U-SCR-07`.
- **Thiết lập lần đầu**: người dùng nhập mã 6 số và nhập lại mã để xác nhận, sau đó chọn **Thiết lập**. Mã do người dùng chọn dùng cho tài khoản, không phải mã riêng của từng nhóm.
- **Khôi phục trên thiết bị**: người dùng nhập mã đã thiết lập vào 6 ô; mỗi ô nhận một chữ số `0`–`9`, kể cả `0` ở đầu. Chưa nhập đủ 6 số thì nút **Xác nhận** bị vô hiệu hóa.
- **Đổi mã**: trên thiết bị còn trạng thái mở khóa hợp lệ và có các khóa cần thiết, người dùng chọn **Xóa mã** rồi thiết lập mã mới, không cần nhập lại mã cũ tại bước đổi. Việc đổi mã được thực hiện theo [UC-U25-KEYS](../../../use-cases/user/security/UC-U25-key-setup-recovery.md) để giữ khả năng đọc lịch sử cũ khi còn đủ khóa.
- Phiên bản hiện tại không có thao tác **Quên mã** hoặc thiết lập lại mã. Nếu không còn mã và không còn thiết bị đã mở khóa, người dùng không thể dùng mã để khôi phục bản sao lưu và có thể không đọc lại được lịch sử mã hóa. Nếu tìm lại thiết bị còn mở khóa hợp lệ, người dùng có thể đổi mã trên thiết bị đó theo luồng trên.
- Mã được che khi nhập. Nút hoặc phím Enter không gửi lặp khi hệ thống đang xử lý; Tab/Shift+Tab di chuyển giữa các điều khiển.
- Sau khi đăng xuất, khi đăng nhập lại người dùng cần nhập mã để mở khóa; thiết bị mới cũng cần nhập mã để khôi phục. Nếu chỉ đóng và mở lại ứng dụng mà phiên đăng nhập và trạng thái mở khóa vẫn hợp lệ trên thiết bị, không yêu cầu nhập lại. Đăng nhập trên thiết bị mới không tự khóa thiết bị khác.

## Lớp phủ tại khung hội thoại nhóm

Khi chưa mở khóa hoặc thiết bị chưa có đủ khóa để đọc cuộc trò chuyện mã hóa, che vùng nội dung tin nhắn và không cho gửi tin trong cuộc trò chuyện đó. Các phần ứng dụng không cần khóa vẫn sử dụng được.

Trong danh sách hội thoại, khi nhóm đang khóa hoặc thiết bị chưa có đủ khóa, dòng xem trước không hiển thị nội dung tin nhắn; thay bằng **“Tin nhắn được mã hóa”**. Sau khi mở khóa, chỉ hiển thị nội dung nếu thiết bị có đủ dữ liệu và khóa để đọc.

Nếu có tin mới trong nhóm mà người dùng còn quyền nhận, ứng dụng hiển thị chỉ báo có tin mới nhưng không hiển thị nội dung khi nhóm đang khóa. Tin được giữ dưới dạng mã hóa; sau khi mở khóa, người dùng đọc được những tin có đủ dữ liệu và khóa tương ứng.

Khi tìm kiếm trong nhóm hoặc trên toàn bộ hội thoại, không tìm hoặc hiển thị kết quả theo nội dung tin nhắn của nhóm đang khóa; vẫn cho tìm nhóm theo tên. Nếu nhóm bị khóa khi kết quả nội dung của nhóm đó đang hiển thị, ẩn các kết quả ấy mà không xóa tin nhắn. Khi tìm kiếm chung có nhóm đang khóa, hiển thị **“Chưa tìm trong nhóm đang khóa”**.

| Trạng thái | Hiển thị và thao tác |
| --- | --- |
| Chưa từng thiết lập mã | Hiển thị biểu tượng ổ khóa, **“Thiết lập mã để xem tin nhắn”** và nút **Thiết lập mã**. |
| Đã có mã, cần mở khóa hoặc khôi phục | Hiển thị biểu tượng ổ khóa, **“Tin nhắn đã được mã hóa. Nhập mã để xem”**, 6 ô nhập và nút **Xác nhận**. |
| Đã xác nhận mã nhưng chưa đủ khóa của nhóm | Hiển thị **“Chưa có khóa nhóm”**; không báo đã đọc được lịch sử hoặc yêu cầu nhập lại mã như thể mã sai. |

## Trạng thái màn hình

| Trạng thái | Hiển thị và thao tác |
| --- | --- |
| Mã chưa đủ 6 số | Chưa cho xác nhận. |
| Hai mã thiết lập chưa khớp | Hiển thị **“Mã chưa khớp”**; chưa hoàn tất thiết lập. |
| Đang xử lý | Hiển thị **“Đang xử lý...”**; chặn gửi lặp. |
| Mã sai | Hiển thị **“Mã không đúng”** khi xác định được mã sai; cho thử lại nếu chưa bị khóa tạm. |
| Sai 5 lần | Tạm khóa thao tác nhập mã trên mọi thiết bị của tài khoản **5 phút**, hiển thị thời gian chờ từ Server; hết thời gian cho phép bắt đầu đợt thử mới. Thiết bị đã mở khóa không tự bị khóa lại; không khóa đăng nhập tài khoản. |
| Mất kết nối khi cần nhập mã | Hiển thị **“Cần kết nối mạng”**; chưa cho bắt đầu lần thử mới vì không kiểm tra được trạng thái khóa chung. Lỗi kết nối không tính là nhập sai. |
| Không kiểm tra được bản sao lưu hoặc lỗi kết nối khác | Hiển thị **“Chưa thể hoàn tất”** và cho thử lại; không tự tạo hoặc ghi đè bản sao lưu. |
| Thiết lập hoặc khôi phục thành công | Thông báo kết quả khi hệ thống xác nhận. Chỉ hiển thị nội dung các tin nhắn đã có đủ dữ liệu và khóa để giải mã. |
| Để sau hoặc hủy | Đóng màn hình, giữ phiên đăng nhập và bản sao lưu hiện có. Nếu vẫn thiếu khóa, lớp phủ tại cuộc trò chuyện liên quan vẫn hiển thị. |

- Server quản lý trạng thái chờ chung theo tài khoản; Client cần kết nối để kiểm tra trước mỗi lần thử. Cách ghi nhận và đồng bộ lần thử mà không gửi mã dạng rõ cần nhóm kỹ thuật xác định. Giới hạn trong ứng dụng không ngăn việc dò mã ngoài ứng dụng nếu bản sao lưu mã hóa bị lộ.
- [Wireframe Figma](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=128-3550) là bản tĩnh, phát triển trên bản sao màn hội thoại của `U-SCR-07`; vị trí menu và lớp phủ cần thống nhất với người phụ trách màn hội thoại. Chưa kiểm thử JavaFX.
