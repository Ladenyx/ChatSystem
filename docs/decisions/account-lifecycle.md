# Tài khoản và vòng đời dữ liệu

**DEC-07 — Đề xuất:** chưa có kết luận chung toàn mục.

- Username cố định, 3–30 ký tự ASCII chữ/số/`_`; username/email của tài khoản phải duy nhất không phân biệt hoa thường sau trim, kể cả khi hoàn tất đăng ký đồng thời. Các yêu cầu đăng ký tạm có cùng email có thể dùng lại email và username của yêu cầu tạm đó theo [DEC-06](password-reset-and-email.md#xác-minh-email); Server kiểm tra lại tính duy nhất trước khi tạo tài khoản.
- Mật khẩu 8–128 ký tự, giữ nguyên hoa thường/khoảng trắng, không chỉ gồm khoảng trắng. Họ tên bắt buộc, trim 1–100; email bắt buộc, trim tối đa 254 và đúng định dạng UC01.
- Địa chỉ tùy chọn, trim tối đa 255; ngày sinh hợp lệ không ở tương lai; giới tính tùy chọn Nam/Nữ/Khác, không tự gán mặc định.
- Đăng ký phải xác minh email, sau đó về đăng nhập. Đổi email cần mật khẩu hiện tại và xác minh email mới. Admin tạo user gửi mật khẩu tạm; user xác nhận email/đặt mật khẩu riêng trước khi chat.
- Khởi tạo/cấp quyền tài khoản admin hệ thống ban đầu qua CLI server, còn toàn bộ nghiệp vụ quản trị vận hành trên **Admin App (Desktop GUI)**; đăng ký công khai luôn là USER. Tách quyền admin hệ thống và admin nhóm; không có mật khẩu mặc định cố định trong mã.
- Phiên User Client và phiên quản trị được quản lý độc lập. User Client cho phép đăng nhập trên nhiều thiết bị; một lần đăng nhập mới không tự thu hồi các phiên User Client hoặc phiên quản trị khác. Chỉ phiên quản trị bị giới hạn tối đa một phiên/tài khoản; đăng nhập Admin App mới hợp lệ thu hồi phiên quản trị cũ, kể cả API/WSS, và reconnect không giành lại phiên đó.
- Khóa quản trị hoặc xóa tài khoản thu hồi các phiên của tài khoản và chặn cấp phiên/quyền chuyển tiếp mới. Mở khóa hoặc khôi phục tài khoản không tự khôi phục các phiên đã thu hồi; người dùng phải đăng nhập lại.
- Xóa mềm **30 ngày**, ngừng đăng nhập/nhận tin mới; admin hệ thống được khôi phục trước hạn. Khôi phục không tự trả thành viên hoặc quyền nhóm.
- Sau hạn, purge hồ sơ/thông tin đăng nhập/quan hệ/backup khóa server; giữ chat dưới nhãn người đã xóa, loại định danh khỏi log/báo cáo và giữ thống kê. Username/email được dùng lại sau purge; tài khoản mới có ID mới, không kế thừa dữ liệu/quyền.
- Xóa admin nhóm cuối cần chọn người kế nhiệm trước; nhóm chỉ còn người bị xóa được đóng, giữ lịch sử. Không cam kết xóa bản sao trên thiết bị khác.
- Theo chính sách quyền riêng tư, Admin hệ thống mặc định không có quyền xem nội dung tin nhắn cá nhân (1-1) hay nhóm chat, kể cả khi hội thoại không mã hóa E2EE; ngoại lệ duy nhất là xem các tin nhắn bằng chứng trong báo cáo spam. Spam report cho chọn tin và lý do, xem trước/xác nhận gửi bản rõ làm bằng chứng cho admin (áp dụng cho cả chat 1-1 lẫn nhóm E2EE). Admin quyết định khóa thủ công; không mặc định server đã xác thực bản rõ do người báo cáo cung cấp.

## Vai trò và phạm vi phiên

- User Client cho phép tài khoản thuộc mọi vai trò đăng nhập khi đáp ứng điều kiện xác thực và trạng thái tài khoản. Admin App chỉ chấp nhận tài khoản có quyền Admin hệ thống.
- Phiên User Client chỉ có phạm vi sử dụng chức năng người dùng. Nghiệp vụ quản trị yêu cầu phiên quản trị và quyền Admin hệ thống; Server kiểm tra quyền tài khoản và phạm vi phiên ở từng yêu cầu. Quyền Admin nhóm được xác định riêng theo từng nhóm.
- Khuyến nghị người quản trị sử dụng tài khoản riêng cho hoạt động thường ngày theo hướng dẫn của [Google Workspace](https://knowledge.workspace.google.com/admin/users/security-best-practices-for-administrator-accounts) và [Microsoft 365](https://learn.microsoft.com/en-gb/microsoft-365/admin/security-and-compliance/m365b-account-security-admins?view=o365-worldwide). Đây là khuyến nghị vận hành, không phải điều kiện chặn vai trò Admin khỏi User Client.

## Trạng thái tài khoản

Đề xuất dùng ba trạng thái cho tài khoản đã được tạo:

| Trạng thái | Ý nghĩa | Điều kiện đăng nhập |
| --- | --- | --- |
| `ACTIVE` | Tài khoản không bị khóa quản trị hoặc xóa mềm | Chỉ cấp phiên khi thông tin xác thực, email và các điều kiện khác đều hợp lệ |
| `LOCKED` | Tài khoản bị Admin khóa | Không cấp phiên/quyền chuyển tiếp; thu hồi các phiên đang hoạt động |
| `DELETED` | Tài khoản đã xóa mềm, còn trong thời hạn khôi phục | Không cấp phiên/quyền chuyển tiếp; thu hồi phiên và ngừng nhận tin mới |

- Chuyển trạng thái theo nghiệp vụ quản trị:
  - `ACTIVE` → `LOCKED` khi Admin khóa; `LOCKED` → `ACTIVE` khi Admin mở khóa.
  - `ACTIVE` hoặc `LOCKED` → `DELETED` khi xóa mềm. Khi khôi phục trong thời hạn cho phép, tài khoản trở về trạng thái trước khi xóa: đã khóa thì trở về `LOCKED`, còn lại trở về `ACTIVE`.
  - Sau thời hạn xóa mềm, tài khoản `DELETED` bị purge; không còn tài khoản để đăng nhập hoặc khôi phục.
- Mở khóa hoặc khôi phục không tự khôi phục phiên cũ. Trở về `ACTIVE` không tự xác minh email hoặc bỏ hạn chế thử sai còn hiệu lực.
- Không dùng `PENDING` làm trạng thái tài khoản. Dữ liệu đăng ký công khai chưa hoàn tất xác minh là yêu cầu tạm theo [DEC-06](password-reset-and-email.md#xác-minh-email), chưa phải tài khoản đăng nhập. Việc email đã xác minh hay chưa là điều kiện kiểm tra riêng, kể cả với tài khoản do Admin tạo.
- Hạn chế đăng nhập do thử sai được quản lý độc lập với trạng thái tài khoản; hết hạn chờ không tự thay đổi trạng thái hay xác minh email.

## Điều kiện tài khoản khi đăng nhập

Áp dụng các trạng thái trên cùng điều kiện xác thực, xác minh email và hạn chế thử sai. Quyền truy cập Admin App được kiểm tra riêng.

| Điều kiện | Phiên sử dụng ứng dụng | Quyền chuyển tiếp xác minh/đặt mật khẩu |
| --- | --- | --- |
| Không tồn tại, đã xóa mềm hoặc đã xóa vĩnh viễn | Từ chối | Không cấp |
| Bị khóa quản trị (`LOCKED`) | Từ chối | Không cấp, kể cả mật khẩu tạm còn hạn |
| Đang trong thời gian khóa do thử sai | Từ chối đến khi hết hạn khóa tạm | Không cấp trong thời gian khóa tạm |
| Tài khoản do Admin tạo, chờ xác minh email, không bị khóa/xóa | Chưa cấp phiên User Client | Sau xác thực hợp lệ, chuyển sang xác minh email; chưa cấp quyền đặt mật khẩu từ nhánh login này |
| Hoạt động (`ACTIVE`), dùng mật khẩu tạm hợp lệ | Chưa cấp phiên sử dụng ứng dụng | Chỉ cấp quyền đặt mật khẩu mới theo DEC-06 |
| Hoạt động (`ACTIVE`), dùng mật khẩu thông thường hợp lệ | Cấp phiên nếu đáp ứng quyền truy cập ứng dụng | Không cần bước chuyển tiếp |

- Kiểm tra các điều kiện từ trên xuống, trước khi cấp bất kỳ phiên hoặc quyền chuyển tiếp nào. Mật khẩu thông thường hoặc mật khẩu tạm đúng không bỏ qua được khóa/xóa tài khoản. Trước khi xác thực danh tính, thông báo không tiết lộ tài khoản có tồn tại hay không.
- Đăng ký công khai chưa hoàn tất xác minh chỉ là yêu cầu đăng ký tạm, không phải tài khoản đăng nhập. Việc hủy hoặc hết hạn yêu cầu theo [DEC-06](password-reset-and-email.md#xác-minh-email) không xóa tài khoản đã tạo, bao gồm tài khoản do Admin tạo.
- Khóa quản trị, hạn khóa do thử sai và trạng thái xác minh email là các điều kiện độc lập, có thể cùng tồn tại. Hết hạn khóa tạm chỉ bỏ hạn chế thử sai; không tự mở khóa quản trị, phục hồi tài khoản đã xóa hoặc xác minh email. Mọi lần cấp quyền phải kiểm tra lại toàn bộ điều kiện hiện tại.
- Trong User Client, đợt thử đầu sai 5 lần liên tiếp thì khóa đăng nhập 3 phút; đợt tiếp theo giảm số lượt được thử. Lịch giảm lượt, thời gian khóa các đợt sau và điều kiện đặt lại bộ đếm theo cấu hình Server; đóng/mở app không đặt lại giới hạn. Không dùng bộ đếm/thời gian phía client làm nguồn quyết định. Khóa do thử sai không tự thu hồi các phiên đang hoạt động.
- Đăng xuất hoặc thu hồi một phiên User Client chỉ kết thúc phiên được chỉ định. Các thao tác thu hồi toàn bộ phiên như khóa quản trị/xóa tài khoản hoặc đổi mật khẩu theo DEC-06 vẫn có hiệu lực trên các thiết bị.

**Còn mở:** băm mật khẩu (Argon2id mới là ứng viên), thời hạn phiên, bảo vệ admin hệ thống cuối, cơ chế mã xác minh email và retention/purge dữ liệu.
