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
- Khi có khóa cục bộ hợp lệ của đúng tài khoản, thiết bị sử dụng khóa đó. Khi khôi phục thành công, chỉ hiển thị lịch sử mà thiết bị đã lấy được khóa giải mã và đồng bộ được.
- Khi bỏ qua khôi phục và chưa có khóa dùng được, luồng E2EE phải chuẩn bị bộ khóa cho thiết bị mới và hoàn tất việc cấp/chia sẻ khóa cho các hội thoại theo giao thức được chọn trước khi cho gửi/nhận tin E2EE mới. Lịch sử cũ vẫn chưa được coi là khôi phục. Trong lúc chưa đủ khóa, chỉ các thao tác tin nhắn E2EE liên quan bị hạn chế; không gửi bản rõ thay thế hoặc báo đã giải mã thành công.
- Không tự ghi đè/xóa backup khóa hiện có hoặc thay mã khôi phục khi bỏ qua/hủy. Lỗi đọc backup không được diễn giải thành không có backup; khóa mới của thiết bị phải được lưu tách biệt với bản sao lưu phục vụ lịch sử cũ.
- Chi tiết tạo khóa thiết bị, cấp khóa cho nhiều thiết bị và điều kiện gửi/nhận khi chưa khôi phục thuộc đặc tả `U-SCR-06`/`U-25`; không triển khai quy trình đó trong use case đăng nhập. Những điều kiện trên là ràng buộc cần đáp ứng khi chọn giao thức, chưa khẳng định đã có cơ chế mật mã chạy được.

**Còn mở:** giao thức/thư viện, prototype cấp lịch sử và cấp khóa nhiều thiết bị, đổi thành viên/khóa, tìm kiếm, archive và khôi phục. Bouncy Castle MLS chỉ là ứng viên; chia sẻ lại lịch sử khi mất toàn bộ khóa chưa được cam kết.
