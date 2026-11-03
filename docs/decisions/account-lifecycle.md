# Tài khoản và vòng đời dữ liệu

**DEC-07 — Đề xuất:** chưa có kết luận chung toàn mục.

- Username cố định, 3–30 ký tự ASCII chữ/số/`_`; username/email duy nhất không phân biệt hoa thường sau trim, kể cả đăng ký đồng thời.
- Mật khẩu 8–128 ký tự, giữ nguyên hoa thường/khoảng trắng, không chỉ gồm khoảng trắng. Họ tên bắt buộc, trim 1–100; email bắt buộc, trim tối đa 254 và đúng định dạng UC01.
- Địa chỉ tùy chọn, trim tối đa 255; ngày sinh hợp lệ không ở tương lai; giới tính tùy chọn Nam/Nữ/Khác, không tự gán mặc định.
- Đăng ký phải xác minh email, sau đó về đăng nhập. Đổi email cần mật khẩu hiện tại và xác minh email mới. Admin tạo user gửi mật khẩu tạm; user xác nhận email/đặt mật khẩu riêng trước khi chat.
- Khởi tạo/cấp quyền tài khoản admin hệ thống ban đầu qua CLI server, còn toàn bộ nghiệp vụ quản trị vận hành trên **Admin App (Desktop GUI)**; đăng ký công khai luôn là USER. Tách quyền admin hệ thống và admin nhóm; không có mật khẩu mặc định cố định trong mã.
- Phiên User Client và phiên quản trị được quản lý độc lập. User Client cho phép đăng nhập trên nhiều thiết bị; một lần đăng nhập mới không tự thu hồi các phiên User Client hoặc phiên quản trị khác. Chỉ phiên quản trị bị giới hạn tối đa một phiên/tài khoản; đăng nhập Admin App mới hợp lệ thu hồi phiên quản trị cũ, kể cả API/WSS, và reconnect không giành lại phiên đó.
- Khóa quản trị hoặc xóa tài khoản thu hồi các phiên của tài khoản và chặn cấp phiên/quyền chuyển tiếp mới. Mở khóa hoặc khôi phục tài khoản không tự khôi phục các phiên đã thu hồi; người dùng phải đăng nhập lại.
- Xóa mềm **30 ngày**, ngừng đăng nhập/nhận tin mới; admin hệ thống được khôi phục trước hạn. Khôi phục không tự trả thành viên hoặc quyền nhóm.
- Sau hạn, purge hồ sơ/thông tin đăng nhập/quan hệ/backup khóa server; giữ chat dưới nhãn người đã xóa, loại định danh khỏi log/báo cáo và giữ thống kê. Username/email được dùng lại sau purge; tài khoản mới có ID mới, không kế thừa dữ liệu/quyền.
- Xóa admin nhóm cuối cần chọn người kế nhiệm trước; nhóm chỉ còn người bị xóa được đóng, giữ lịch sử. Không cam kết xóa bản sao trên thiết bị khác.
- Spam report cho chọn tin và lý do, xem trước/xác nhận gửi bản rõ cho admin kể cả nhóm E2EE. Admin quyết định khóa thủ công; không mặc định server đã xác thực bản rõ do người báo cáo cung cấp.

## Phân tách tài khoản quản trị và tài khoản chat

- User Client chỉ chấp nhận tài khoản có vai trò `USER`; Admin App chỉ chấp nhận tài khoản Admin hệ thống. Server kiểm tra vai trò trước khi cấp phiên hoặc quyền chuyển tiếp, không dựa vào lựa chọn trên giao diện.
- Người quản trị muốn tham gia chat sử dụng tài khoản `USER` riêng với thông tin đăng nhập riêng. Tài khoản Admin hệ thống không dùng để chat; tài khoản `USER` không được gọi nghiệp vụ quản trị hệ thống. Quyền Admin nhóm được xác định theo từng nhóm, không cấp quyền Admin hệ thống.
- Lý do lựa chọn: tách tài khoản có quyền khóa/xóa tài khoản và reset mật khẩu khỏi hoạt động chat thường ngày. Đổi lại, người quản trị cần quản lý hai tài khoản nếu muốn chat. Việc tách tài khoản không thay thế kiểm tra quyền và phiên trên Server.
- Đây là chính sách thiết kế của dự án, không phải ràng buộc nêu trực tiếp trong đề bài. Tham khảo khuyến nghị dùng tài khoản riêng cho công việc hằng ngày của [Google Workspace](https://knowledge.workspace.google.com/admin/users/security-best-practices-for-administrator-accounts) và [Microsoft 365](https://learn.microsoft.com/en-gb/microsoft-365/admin/security-and-compliance/m365b-account-security-admins?view=o365-worldwide); các khuyến nghị này không khẳng định mọi sản phẩm đều chặn Admin đăng nhập ứng dụng người dùng.

## Điều kiện tài khoản khi đăng nhập

Các trạng thái nghiệp vụ dưới đây tổng hợp điều kiện đã có trong screen map. `ACTIVE` và `LOCKED` giữ cách gọi của UC-A01; chờ xác minh/xóa mềm chưa ấn định tên enum hoặc cấu trúc lưu trữ. Vai trò tài khoản được kiểm tra riêng theo use case của từng ứng dụng.

| Điều kiện | Phiên sử dụng ứng dụng | Quyền chuyển tiếp xác minh/đặt mật khẩu |
| --- | --- | --- |
| Không tồn tại, đã xóa mềm hoặc đã xóa vĩnh viễn | Từ chối | Không cấp |
| Bị khóa quản trị (`LOCKED`) | Từ chối | Không cấp, kể cả mật khẩu tạm còn hạn |
| Đang trong thời gian khóa do thử sai | Từ chối đến khi hết hạn khóa tạm | Không cấp trong thời gian khóa tạm |
| Chờ xác minh email, không bị khóa/xóa | Chưa cấp phiên User Client | Sau xác thực hợp lệ, chuyển sang xác minh email; chưa cấp quyền đặt mật khẩu từ nhánh login này |
| Hoạt động (`ACTIVE`), dùng mật khẩu tạm hợp lệ | Chưa cấp phiên sử dụng ứng dụng | Chỉ cấp quyền đặt mật khẩu mới theo DEC-06 |
| Hoạt động (`ACTIVE`), dùng mật khẩu thông thường hợp lệ | Cấp phiên nếu đáp ứng quyền truy cập ứng dụng | Không cần bước chuyển tiếp |

- Kiểm tra các điều kiện từ trên xuống, trước khi cấp bất kỳ phiên hoặc quyền chuyển tiếp nào. Mật khẩu thông thường hoặc mật khẩu tạm đúng không bỏ qua được khóa/xóa tài khoản. Trước khi xác thực danh tính, thông báo không tiết lộ tài khoản có tồn tại hay không.
- Khóa quản trị, hạn khóa do thử sai và trạng thái xác minh email là các điều kiện độc lập, có thể cùng tồn tại. Hết hạn khóa tạm chỉ bỏ hạn chế thử sai; không tự mở khóa quản trị, phục hồi tài khoản đã xóa hoặc xác minh email. Mọi lần cấp quyền phải kiểm tra lại toàn bộ điều kiện hiện tại.
- Trong User Client, đợt thử đầu sai 5 lần liên tiếp thì khóa đăng nhập 3 phút; đợt tiếp theo giảm số lượt được thử. Lịch giảm lượt, thời gian khóa các đợt sau và điều kiện đặt lại bộ đếm theo cấu hình Server; đóng/mở app không đặt lại giới hạn. Không dùng bộ đếm/thời gian phía client làm nguồn quyết định. Khóa do thử sai không tự thu hồi các phiên đang hoạt động.
- Đăng xuất hoặc thu hồi một phiên User Client chỉ kết thúc phiên được chỉ định. Các thao tác thu hồi toàn bộ phiên như khóa quản trị/xóa tài khoản hoặc đổi mật khẩu theo DEC-06 vẫn có hiệu lực trên các thiết bị.

**Còn mở:** băm mật khẩu (Argon2id mới là ứng viên), thời hạn phiên, bảo vệ admin hệ thống cuối, cơ chế mã xác minh email và retention/purge dữ liệu.
