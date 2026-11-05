# Quyền nhóm và E2EE

**DEC-05 — Đề xuất:** chưa có kết luận chung toàn mục hoặc thư viện mật mã được chốt.

- Người tạo là admin đầu tiên; admin ngang quyền, được đổi tên, thêm/xóa thành viên, cấp/thu hồi admin; bảo vệ admin cuối cùng.
- Thành viên thường chỉ được thêm bạn bè theo điều kiện DEC-03, không đổi tên nhóm. Admin chỉ thu hồi tin của mình theo giới hạn DEC-02.
- Mọi nhóm dùng E2EE; server lưu/chuyển bản mã, không tự giải mã luồng chat nhóm.
- Người mới được đọc lịch sử trước khi tham gia, trừ tin đã thu hồi; cần thiết bị có quyền chia sẻ khả năng giải mã. Người được mời có thể offline; quyền đọc chỉ hoàn tất khi chia sẻ xong.
- Người bị xóa giữ lịch sử từng được đọc, không nhận/giải mã tin mới; không thể xóa bản đã nằm trên thiết bị của họ.
- User Client hỗ trợ nhiều phiên/thiết bị đồng thời; đăng nhập máy mới không kết thúc phiên chat hoặc phiên quản trị đang có. Giới hạn một phiên chỉ áp dụng cho phiên quản trị theo DEC-07. Xác thực tài khoản không đồng nghĩa thiết bị đã có khóa E2EE.
- Đổi máy cần chuyển/khôi phục khóa bằng mã khôi phục người dùng giữ và backup khóa đã mã hóa. Reset mật khẩu không mở khóa lịch sử; mất thiết bị giữ khóa lẫn mã thì không tự mở được backup.
- Gửi nội dung cho LLM hoặc báo cáo spam là tiết lộ bản rõ, có xem trước/xác nhận riêng.

## Thiết lập/khôi phục khóa sau đăng nhập

- Đăng nhập thành công mở khung chính trước. Người dùng có thể mở `U-SCR-06` để khôi phục lịch sử ngay hoặc để sau; hủy/bỏ qua khôi phục không đăng xuất, không thu hồi phiên và không cản sử dụng các chức năng không cần khóa.
- Một tài khoản dùng một mã khôi phục 6 số do người dùng thiết lập. Sau 5 lần xác minh sai liên tiếp được ghi nhận trên các thiết bị, Server áp dụng trạng thái chờ chung 5 phút trước lần thử mới trên mọi thiết bị của tài khoản. Thiết bị đã mở khóa không tự bị khóa lại; khi không kết nối được Server để kiểm tra trạng thái, không bắt đầu lần thử mã mới. Cách ghi nhận và đồng bộ an toàn chưa được nhóm kỹ thuật chốt.
- Khi có khóa cục bộ phù hợp và trạng thái mở khóa hợp lệ của đúng tài khoản, thiết bị sử dụng khóa đó. Đăng xuất làm khóa lại trên thiết bị đó; đăng nhập lại hoặc khôi phục trên thiết bị mới cần nhập mã. Nếu chỉ đóng và mở lại ứng dụng mà phiên đăng nhập và trạng thái mở khóa vẫn hợp lệ, không yêu cầu mã lần nữa. Khi khôi phục thành công, chỉ hiển thị lịch sử mà thiết bị đã lấy được khóa giải mã và đồng bộ được.
- Khi bỏ qua khôi phục và chưa có khóa dùng được, luồng E2EE phải chuẩn bị bộ khóa cho thiết bị mới và hoàn tất việc cấp/chia sẻ khóa cho các hội thoại theo giao thức được chọn trước khi cho gửi hoặc giải mã tin E2EE mới. Lịch sử cũ vẫn chưa được coi là khôi phục. Trong lúc nhóm đang khóa, Server vẫn lưu tin nhắn mã hóa theo quyền nhận và chính sách lưu trữ; ứng dụng có thể báo có tin mới nhưng không hiện nội dung. Chỉ đọc được sau khi mở khóa và có đủ dữ liệu, khóa. Không gửi bản rõ thay thế hoặc báo đã giải mã thành công.
- Không tự ghi đè/xóa backup khóa hiện có hoặc thay mã khôi phục khi bỏ qua/hủy. Lỗi đọc backup không được diễn giải thành không có backup; khóa mới của thiết bị phải được lưu tách biệt với bản sao lưu phục vụ lịch sử cũ.
- Nếu người dùng quên mã và không còn thiết bị đã mở khóa, có thể chọn thiết lập lại sau khi được cảnh báo bản sao lưu khóa cũ sẽ được thay thế và lịch sử cũ có thể không đọc lại được. Chỉ thay bản cũ khi người dùng xác nhận và bản mới đã được lưu thành công; không tự xóa tin nhắn. Thiết lập lại không khôi phục khóa cũ và không bảo đảm thiết bị đã có đủ khóa cho các nhóm.
- Chi tiết tạo khóa thiết bị, cấp khóa cho nhiều thiết bị và điều kiện gửi/nhận khi chưa khôi phục thuộc đặc tả `U-SCR-06`/`U-25`; không triển khai quy trình đó trong use case đăng nhập. Những điều kiện trên là ràng buộc cần đáp ứng khi chọn giao thức, chưa khẳng định đã có cơ chế mật mã chạy được.

**Còn mở:** giao thức/thư viện, prototype cấp lịch sử và cấp khóa nhiều thiết bị, đổi thành viên/khóa, tìm kiếm, archive và khôi phục. Bouncy Castle MLS chỉ là ứng viên; chia sẻ lại lịch sử khi mất toàn bộ khóa chưa được cam kết.
