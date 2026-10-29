# Chỉnh sửa nháp và gợi ý trả lời bằng LLM

**DEC-08:** giữ **Muse API**, triển khai đầy đủ **chỉnh sửa câu nháp và gợi ý trả lời** với ngân sách mục tiêu **2 USD/tháng**, tách hosting.

- U-27 thực hiện cả hai chức năng qua **hai nút riêng biệt** trên thanh soạn tin: **nút Chỉnh sửa (Refine)** viết lại/trau chuốt nội dung đang gõ ở ô nháp và **nút Gợi ý (Recommend)** sinh câu phản hồi theo ngữ cảnh hội thoại. Kết quả đều đưa vào bản nháp để người dùng tự kiểm tra và gửi.
- Chỉ gửi phần bản nháp hoặc các tin ngữ cảnh người dùng chọn, có xem trước/sửa và xác nhận rõ ràng. Nêu rõ bản rõ đi tới server ứng dụng và nhà cung cấp AI, kể cả từ nhóm E2EE.
- Lỗi/hết hạn mức thì thông báo, giữ bản nháp và tiếp tục chat thủ công. API key chỉ ở server.
- Không dùng cookie/token ChatGPT web hoặc token Codex cá nhân làm API chung. Sign in with ChatGPT chỉ là hướng nghiên cứu bổ sung.

**Còn mở:** tài khoản, quyền dùng, model, billing, chất lượng tiếng Việt, độ trễ và hạn mức thực tế. Tên model `muse-spark-1.3-contributor` và điều kiện Contributor trong thảo luận chưa được xác minh; cần kiểm tra thông báo sử dụng dữ liệu. Giới hạn 5 tin ngữ cảnh, 3 gợi ý, timeout 15 giây và quota mới là đề xuất.
