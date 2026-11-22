# Chỉnh sửa nháp và gợi ý trả lời bằng LLM

**DEC-08:** sử dụng **Meta Model API (Muse API)** (`https://dev.meta.ai/docs/overview`), triển khai đầy đủ **chỉnh sửa câu nháp và gợi ý trả lời** với ngân sách mục tiêu **2 USD/tháng**, tách biệt với chi phí hosting.

- **Giao diện & Luồng thao tác (`U-27`, `U-DLG-03`):** thực hiện cả hai chức năng qua **hai nút riêng biệt** trên thanh soạn tin: **nút Chỉnh sửa (Refine)** viết lại/trau chuốt nội dung đang gõ ở ô nháp và **nút Gợi ý (Recommend)** sinh câu phản hồi theo ngữ cảnh hội thoại. Kết quả đều đưa vào bản nháp để người dùng tự kiểm tra và gửi.
- **Model & Giao thức API đã chốt:**
  - **Endpoint:** `POST https://api.meta.ai/v1/chat/completions` (xác thực bằng `Authorization: Bearer <MODEL_API_KEY>` tại Server).
  - **Model mặc định:** `muse-spark-1.3-contributor` (Pay-as-you-go Contributor mode: `0.10 USD / 1M input tokens`, `0.20 USD / 1M output tokens`, `0.002 USD / 1M cached input tokens`; giới hạn `150 RPM` và `3,000,000 TPM` mỗi team). Cho phép đổi model qua file cấu hình Server (ví dụ sang `muse-spark-1.3` hoặc `muse-ember-1.0` ở chế độ Standard khi cần).
  - **Định dạng phản hồi & tối ưu độ trễ:** sử dụng **Structured Output** (`response_format: { "type": "json_schema", ... }`) kết hợp `reasoning_effort: "minimal"` (hoặc `"low"`) để đảm bảo phản hồi luôn khớp JSON Schema (mảng 3 phương án văn bản) và giảm thiểu độ trễ.
- **Thông số vận hành đã chốt:**
  - **Số tin ngữ cảnh tối đa:** tự động trích xuất tối đa **50 tin nhắn** văn bản hợp lệ gần nhất đang lưu tại máy khách (local client) trong hội thoại hiện tại (đã giải mã nếu là nhóm E2EE, bỏ qua tin đã thu hồi hoặc xóa phía mình) theo thứ tự thời gian để AI nắm ngữ cảnh và bắt đúng văn phong/phong cách cuộc trò chuyện.
  - **Số phương án trả về:** **3 phương án** hiển thị ngay trên popover `U-DLG-03` cho mỗi lượt nhấn yêu cầu (viết lại bản nháp khi đang có chữ hoặc gợi ý câu trả lời khi cần phản hồi).
  - **Timeout:** **15 giây** cho mỗi yêu cầu từ Client/Server tới Meta Model API.
- **Quyền riêng tư & Luồng hiển thị (`U-DLG-03`):**
  - Khi người dùng nhấn kích hoạt trợ lý AI, hệ thống tự động gửi ngữ cảnh tối đa **50 tin nhắn** hợp lệ gần nhất từ máy khách (kèm bản nháp hiện tại nếu có) và hiển thị ngay **3 phương án** kết quả trên popover `U-DLG-03` mà không bắt người dùng tích chọn thủ công từng tin hay qua bước xem trước/xác nhận trung gian trước mỗi lần gọi.
  - Tài liệu và chính sách tính năng phải nêu rõ cơ chế dữ liệu: **(1)** bản rõ của tối đa 50 tin nhắn ngữ cảnh cục bộ và bản nháp được gửi qua Server ứng dụng tới nhà cung cấp Meta Model API (kể cả khi trích xuất từ nhóm chat mã hóa E2EE), và **(2)** khi hệ thống vận hành với biến thể `muse-spark-1.3-contributor` (Contributor mode), nội dung gửi lên và phản hồi có thể được Meta sử dụng để huấn luyện các mô hình tương lai theo điều khoản của nhà cung cấp.
- **An toàn & Xử lý lỗi:** lỗi mạng, quá thời gian chờ (`15 giây`) hoặc vượt hạn mức (`HTTP 429`) thì thông báo rõ, giữ nguyên bản nháp hiện tại và cho phép tiếp tục chat thủ công. API key chỉ lưu tại cấu hình Server; không dùng cookie/token cá nhân làm API chung.
