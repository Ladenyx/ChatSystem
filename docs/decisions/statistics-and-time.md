# Thống kê và thời gian

| Chỉ số | Cách tính đề xuất |
| --- | --- |
| Tổng bạn mở rộng | Hợp bạn trực tiếp và người qua hai bước; loại mình, trùng, tài khoản đã xóa và người có quan hệ chặn với người gốc. Tài khoản bị khóa vẫn tính nếu còn quan hệ |
| Lượt mở app | Một lượt/tài khoản xác thực thành công/lần khởi chạy User Client; reconnect hoặc đăng nhập lại cùng lần chạy không tăng |
| Danh sách hoạt động | Người mở app hoặc gửi tin được server chấp nhận trong khoảng |
| Người/nhóm đã chat | Đích gửi thành công khác nhau; chỉ nhận tin không tăng |
| Biểu đồ hoạt động tháng | Người duy nhất mở app trong tháng; đủ 12 tháng, tháng trống bằng 0 |
| Bộ lọc số lượng | Chọn lượt mở app, số người hoặc số nhóm đã chat; áp dụng `=`, `<`, `>` |
| Thời gian | Lưu UTC theo server; lọc/hiển thị `Asia/Ho_Chi_Minh`; ngày cuối lấy đến trước 00:00 ngày kế tiếp |

Giữ thống kê sau xóa tài khoản; không cộng người duy nhất từng ngày để ra người duy nhất cả tháng. Xóa/thu hồi tin không đảo ngược sự kiện gửi. Mở app 31/10 rồi gửi tin 1/11 được tính hoạt động ngày 1/11 nhưng chưa có lượt mở app tháng 11.

**Giao diện hiển thị:** triển khai trực quan trên **Admin App (Desktop GUI)** gồm trang **Dashboard Tổng quan** (KPI toàn hệ thống) và các trang Dashboard thống kê chuyên sâu kết hợp thẻ KPI, biểu đồ đồ họa JavaFX (`BarChart` / `LineChart` 12 tháng) cùng bảng lọc tương tác trên cùng màn hình.

**Còn mở:** xác nhận công thức, thiết kế dữ liệu sau purge và bộ mẫu có kết quả tính tay.
