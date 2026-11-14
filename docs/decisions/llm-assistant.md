# Chỉnh sửa nháp và gợi ý trả lời bằng LLM

**DEC-08:** sử dụng **Meta Model API (Muse API)** (`https://dev.meta.ai/docs/overview`), triển khai đầy đủ **chỉnh sửa câu nháp và gợi ý trả lời** với ngân sách mục tiêu **2 USD/tháng**, tách biệt với chi phí hosting.

- **Giao diện & Luồng thao tác (`U-27`, `U-DLG-03`):** thực hiện cả hai chức năng qua **hai nút riêng biệt** trên thanh soạn tin: **nút Chỉnh sửa (Refine)** viết lại/trau chuốt nội dung đang gõ ở ô nháp và **nút Gợi ý (Recommend)** sinh câu phản hồi theo ngữ cảnh hội thoại. Kết quả đều đưa vào bản nháp để người dùng tự kiểm tra và gửi.
- **Model & Giao thức API đã chốt:**
  - **Endpoint:** `POST https://api.meta.ai/v1/chat/completions` (xác thực bằng `Authorization: Bearer <MODEL_API_KEY>` tại Server).
  - **Model mặc định:** `muse-spark-1.3-contributor` (Pay-as-you-go Contributor mode: `0.10 USD / 1M input tokens`, `0.20 USD / 1M output tokens`, `0.002 USD / 1M cached input tokens`; giới hạn `150 RPM` và `3,000,000 TPM` mỗi team). Cho phép đổi model qua file cấu hình Server (ví dụ sang `muse-spark-1.3` hoặc `muse-ember-1.0` ở chế độ Standard khi cần).
  - **Định dạng phản hồi & tối ưu độ trễ:** sử dụng **Structured Output** (`response_format: { "type": "json_schema", ... }`) kết hợp `reasoning_effort: "minimal"` (hoặc `"low"`) để đảm bảo phản hồi luôn khớp JSON Schema (mảng 3 phương án văn bản) và giảm thiểu độ trễ.
- **Thông số vận hành đã chốt:**
  - **Số tin ngữ cảnh tối đa (`Recommend`):** tối đa **5 tin nhắn** văn bản hợp lệ do người dùng chọn.
  - **Số phương án trả về:** **3 phương án** cho mỗi lượt yêu cầu `Refine` hoặc `Recommend`.
  - **Timeout:** **15 giây** cho mỗi yêu cầu từ Client/Server tới Meta Model API.
- **Quyền riêng tư & Thông báo xác nhận (`U-DLG-03`):**
  - Chỉ gửi phần bản nháp hoặc các tin ngữ cảnh người dùng chọn sau khi người dùng đã xem trước/sửa và bấm xác nhận rõ ràng.
  - Hộp thoại xác nhận `U-DLG-03` phải nêu rõ: **(1)** nội dung bản rõ được gửi tới Server ứng dụng và nhà cung cấp Meta Model API (kể cả khi trích từ nhóm chat mã hóa E2EE), và **(2)** khi hệ thống vận hành với biến thể `muse-spark-1.3-contributor` (Contributor mode), nội dung gửi lên và phản hồi có thể được Meta sử dụng để huấn luyện các mô hình tương lai theo điều khoản của nhà cung cấp.
- **An toàn & Xử lý lỗi:** lỗi mạng, quá thời gian chờ (`15 giây`) hoặc vượt hạn mức (`HTTP 429`) thì thông báo rõ, giữ nguyên bản nháp hiện tại và cho phép tiếp tục chat thủ công. API key chỉ lưu tại cấu hình Server; không dùng cookie/token cá nhân làm API chung.
