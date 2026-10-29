# Tài khoản và vòng đời dữ liệu

**DEC-07 — Đề xuất:** chưa có kết luận chung toàn mục.

- Username cố định, 3–30 ký tự ASCII chữ/số/`_`; username/email duy nhất không phân biệt hoa thường sau trim, kể cả đăng ký đồng thời.
- Mật khẩu 8–128 ký tự, giữ nguyên hoa thường/khoảng trắng, không chỉ gồm khoảng trắng. Họ tên bắt buộc, trim 1–100; email bắt buộc, trim tối đa 254 và đúng định dạng UC01.
- Địa chỉ tùy chọn, trim tối đa 255; ngày sinh hợp lệ không ở tương lai; giới tính tùy chọn Nam/Nữ/Khác, không tự gán mặc định.
- Đăng ký phải xác minh email, sau đó về đăng nhập. Đổi email cần mật khẩu hiện tại và xác minh email mới. Admin tạo user gửi mật khẩu tạm; user xác nhận email/đặt mật khẩu riêng trước khi chat.
- Khởi tạo/cấp quyền tài khoản admin hệ thống ban đầu qua CLI server, còn toàn bộ nghiệp vụ quản trị vận hành trên **Admin App (Desktop GUI)**; đăng ký công khai luôn là USER. Tách quyền admin hệ thống và admin nhóm; không có mật khẩu mặc định cố định trong mã.
- Một phiên/tài khoản; thu hồi cả API/WSS, reconnect không giành lại phiên cũ. Khóa thu hồi phiên ngay; mở khóa cần đăng nhập lại.
- Xóa mềm **30 ngày**, ngừng đăng nhập/nhận tin mới; admin hệ thống được khôi phục trước hạn. Khôi phục không tự trả thành viên hoặc quyền nhóm.
- Sau hạn, purge hồ sơ/thông tin đăng nhập/quan hệ/backup khóa server; giữ chat dưới nhãn người đã xóa, loại định danh khỏi log/báo cáo và giữ thống kê. Username/email được dùng lại sau purge; tài khoản mới có ID mới, không kế thừa dữ liệu/quyền.
- Xóa admin nhóm cuối cần chọn người kế nhiệm trước; nhóm chỉ còn người bị xóa được đóng, giữ lịch sử. Không cam kết xóa bản sao trên thiết bị khác.
- Spam report cho chọn tin và lý do, xem trước/xác nhận gửi bản rõ cho admin kể cả nhóm E2EE. Admin quyết định khóa thủ công; không mặc định server đã xác thực bản rõ do người báo cáo cung cấp.

**Còn mở:** băm mật khẩu (Argon2id mới là ứng viên), thời hạn phiên, bảo vệ admin hệ thống cuối, mã xác minh email và retention/purge dữ liệu.
