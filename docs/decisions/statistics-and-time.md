# Thống kê và thời gian

**Đã chốt ngày 20/11/2026:** phạm vi tài khoản trong thống kê tạo mới, thời hạn lưu dữ liệu thống kê và việc bảo toàn đích chat lịch sử sau purge/đóng nhóm. Các quyết định này là yêu cầu cho thiết kế ERD/SQL; việc kiểm chứng phần lưu trữ được thực hiện riêng.

| Chỉ số | Cách tính |
| --- | --- |
| Tài khoản tạo mới (`A-12`, `A-13`) | Mọi tài khoản `USER` và `ADMIN` được tạo thành công, kể cả Admin hệ thống khởi tạo qua CLI; yêu cầu đăng ký tạm chưa tạo tài khoản không được tính |
| Tổng bạn mở rộng | Hợp bạn trực tiếp và người qua hai bước; loại mình, trùng, tài khoản đã xóa và người có quan hệ chặn với người gốc. Tài khoản bị khóa vẫn tính nếu còn quan hệ |
| Lượt mở app | Một lượt/tài khoản xác thực thành công/lần khởi chạy User Client; reconnect hoặc đăng nhập lại cùng lần chạy không tăng |
| Danh sách hoạt động | Người mở app hoặc gửi tin được server chấp nhận trong khoảng |
| Người/nhóm đã chat | Đích gửi thành công khác nhau; chỉ nhận tin không tăng |
| Biểu đồ hoạt động tháng | Người duy nhất mở app trong tháng; đủ 12 tháng, tháng trống bằng 0 |
| Bộ lọc số lượng | Chọn lượt mở app, số người hoặc số nhóm đã chat; áp dụng `=`, `<`, `>` |
| Thời gian | Lưu UTC theo server; lọc/hiển thị `Asia/Ho_Chi_Minh`; ngày cuối lấy đến trước 00:00 ngày kế tiếp |

Giữ thống kê sau xóa tài khoản; không cộng người duy nhất từng ngày để ra người duy nhất cả tháng. Xóa/thu hồi tin không đảo ngược sự kiện gửi. Mở app 31/10 rồi gửi tin 1/11 được tính hoạt động ngày 1/11 nhưng chưa có lượt mở app tháng 11.

## Phạm vi tài khoản tạo mới

Danh sách đăng ký mới (`A-12`) và biểu đồ đăng ký theo tháng (`A-13`) dùng cùng một phạm vi: tính cả tài khoản `USER` và `ADMIN`, gồm tự đăng ký đã hoàn tất tạo tài khoản, tài khoản do Admin tạo và Admin hệ thống được khởi tạo qua CLI. Một tài khoản được tính một lần theo sự kiện tạo thành công; yêu cầu đăng ký tạm chưa tạo tài khoản không phát sinh lượt đăng ký mới.

Khóa, xóa mềm hoặc purge về sau không làm giảm số liệu tạo mới của khoảng thời gian trước đó. Tài khoản mới dùng lại username/email sau purge có định danh mới và được tính là một tài khoản mới, theo [vòng đời tài khoản](account-lifecycle.md).

## Thời hạn giữ dữ liệu thống kê

Dữ liệu thống kê tạo tài khoản, lượt mở ứng dụng và metadata gửi tin được giữ lâu dài, không tự hết hạn theo tuổi của sự kiện. Purge tài khoản không xóa phần dữ liệu thống kê cần để tra cứu lịch sử và tính đúng các chỉ số trong khoảng ngày được chọn.

Phạm vi giữ lâu dài là metadata phục vụ thống kê; thông tin định danh tài khoản sau purge được xử lý theo [DEC-07](account-lifecycle.md). Thời hạn của gói báo cáo spam theo [DEC-10](spam-moderation-and-retention.md) và backup E2EE theo [DEC-05](group-permissions-and-e2ee.md) giữ theo các chính sách riêng đã chốt.

## Bảo toàn đích chat trong thống kê lịch sử

Số người/nhóm đã chat được tính theo các đích gửi thành công khác nhau trong khoảng thời gian tra cứu. Người nhận bị purge hoặc nhóm đóng về sau vẫn được tính trong số liệu của khoảng thời gian đã phát sinh sự kiện gửi.

| Tình huống | Kết quả khi xem lại khoảng thời gian cũ |
| --- | --- |
| A đã gửi tin cho B và C trong tháng 10; B bị purge sau đó | Số người A đã chat trong tháng 10 vẫn là `2` |
| A đã gửi tin vào nhóm G trong tháng 10; G đóng sau đó | Nhóm G vẫn đóng góp `1` vào số nhóm A đã chat trong tháng 10 |
| Tin đã được server chấp nhận, sau đó bị xóa phía mình hoặc thu hồi | Sự kiện gửi vẫn được tính trong lịch sử |

Quy tắc này bảo toàn số liệu lịch sử; quyền đăng nhập, gửi/nhận tin và tư cách thành viên hiện tại vẫn theo vòng đời tài khoản và nhóm. Việc dùng lại username/email không gán các sự kiện cũ sang tài khoản mới.

**Giao diện hiển thị:** triển khai trực quan trên **Admin App (Desktop GUI)** gồm trang **Dashboard Tổng quan** (KPI toàn hệ thống) và các trang Dashboard thống kê chuyên sâu kết hợp thẻ KPI, biểu đồ đồ họa JavaFX (`BarChart` / `LineChart` 12 tháng) cùng bảng lọc tương tác trên cùng màn hình.

**Thiết kế và kiểm chứng:** mô hình lưu bền vững, liên kết khóa thống kê và hợp đồng ghi sự kiện tạo tài khoản/gửi tin cần đáp ứng các quyết định trên. DDL được quản lý trong [`30-logs-and-analytics.sql`](../../script/sql/30-logs-and-analytics.sql), cùng các kiểm tra trong [`test-logs-analytics.sql`](../../script/tests/test-logs-analytics.sql). Các ca trước/sau purge, đóng nhóm và dùng lại username/email phải đọc từ nguồn lưu trữ thật; bảng tạm chỉ dùng trong test không thay thế nguồn dữ liệu thống kê được triển khai.
