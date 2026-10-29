# Xóa, thu hồi và gửi lại tin

- Xóa phía mình hoặc toàn bộ lịch sử chỉ ẩn với người thao tác và loại khỏi tìm kiếm của họ; người khác giữ lịch sử. Trạng thái xóa riêng đồng bộ khi đổi máy/đăng nhập lại; tin mới vẫn xuất hiện.
- Chỉ người gửi thu hồi tin của mình, trong chat riêng và nhóm; admin nhóm không thu hồi tin người khác. Có dấu báo thu hồi, loại nội dung khỏi tìm kiếm và đồng bộ cho người offline. Việc thu hồi chỉ được phép trong **48h đầu tiên**
- Phương án retry: tin chờ tự gửi lại với cùng ID; server xử lý lặp an toàn, không tạo thêm tin/lượt hoạt động và kiểm tra quyền hiện tại.
- Phân biệt đang chờ, server đã lưu, người nhận đã nhận/đọc. Đăng ký không áp dụng tự retry khi chưa rõ kết quả.
