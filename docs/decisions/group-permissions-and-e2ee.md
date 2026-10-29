# Quyền nhóm và E2EE

**DEC-05 — Đề xuất:** chưa có kết luận chung toàn mục hoặc thư viện mật mã được chốt.

- Người tạo là admin đầu tiên; admin ngang quyền, được đổi tên, thêm/xóa thành viên, cấp/thu hồi admin; bảo vệ admin cuối cùng.
- Thành viên thường chỉ được thêm bạn bè theo điều kiện DEC-03, không đổi tên nhóm. Admin chỉ thu hồi tin của mình theo giới hạn DEC-02.
- Mọi nhóm dùng E2EE; server lưu/chuyển bản mã, không tự giải mã luồng chat nhóm.
- Người mới được đọc lịch sử trước khi tham gia, trừ tin đã thu hồi; cần thiết bị có quyền chia sẻ khả năng giải mã. Người được mời có thể offline; quyền đọc chỉ hoàn tất khi chia sẻ xong.
- Người bị xóa giữ lịch sử từng được đọc, không nhận/giải mã tin mới; không thể xóa bản đã nằm trên thiết bị của họ.
- Một tài khoản có một phiên; đăng nhập máy mới kết thúc phiên cũ. Đăng nhập không đồng nghĩa đã có khóa.
- Đổi máy cần chuyển/khôi phục khóa bằng mã khôi phục người dùng giữ và backup khóa đã mã hóa. Reset mật khẩu không mở khóa lịch sử; mất thiết bị giữ khóa lẫn mã thì không tự mở được backup.
- Gửi nội dung cho LLM hoặc báo cáo spam là tiết lộ bản rõ, có xem trước/xác nhận riêng.

**Còn mở:** giao thức/thư viện, prototype cấp lịch sử, đổi thành viên/khóa, tìm kiếm, archive và khôi phục. Bouncy Castle MLS chỉ là ứng viên; chia sẻ lại lịch sử khi mất toàn bộ khóa chưa được cam kết.
