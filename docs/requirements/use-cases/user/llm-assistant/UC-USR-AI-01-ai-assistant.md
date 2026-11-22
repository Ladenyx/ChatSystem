# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-AI-01
- **Use Case Name**: Soạn và chỉnh sửa tin nhắn bằng AI
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 22/11/2026
- **Primary Actor**: Người dùng đang soạn tin trong hội thoại
- **Secondary Actor(s)**: Nhà cung cấp AI (Meta Model API / Muse API) qua server ứng dụng

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Khi soạn tin trong hội thoại cá nhân hoặc nhóm, người dùng kích hoạt trợ lý AI để nhận 3 phương án gợi ý câu trả lời hoặc viết lại bản nháp hiện có theo đúng ngữ cảnh và văn phong của cuộc trò chuyện, dựa trên tối đa 50 tin nhắn hợp lệ gần nhất lưu tại máy khách.
- **Goal**: Chọn được một phương án diễn đạt hoặc câu trả lời khớp ngữ cảnh và văn phong cuộc trò chuyện để đưa vào ô soạn nháp.
- **Trigger**: Người dùng nhấn nút trợ lý AI trên thanh soạn tin của hội thoại.

## Điều kiện (Conditions)

- **Pre-conditions**:
  1. Người dùng có phiên User Client hợp lệ và còn quyền gửi tin trong hội thoại hiện tại.
  2. Hội thoại có ít nhất một tin nhắn văn bản hợp lệ đã lưu cục bộ trên máy khách (đã giải mã nếu là nhóm E2EE, chưa bị thu hồi hoặc xóa phía mình) hoặc ô soạn nháp đang có ít nhất một ký tự khác khoảng trắng.
- **Post-conditions**:
  1. Hệ thống hiển thị 3 phương án văn bản phù hợp với ngữ cảnh và văn phong cuộc trò chuyện.
  2. Khi người dùng chọn một phương án, nội dung đó được điền vào ô soạn nháp của hội thoại hiện tại (tin nhắn chưa tự động gửi đi).

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** kích hoạt trợ lý AI trên thanh soạn tin của hội thoại đang mở.
2. **Hệ thống** trích xuất tối đa 50 tin nhắn văn bản hợp lệ gần nhất của hội thoại từ bộ nhớ cục bộ trên máy khách theo thứ tự thời gian (kèm nội dung bản nháp hiện tại nếu có) và gửi yêu cầu tới dịch vụ AI qua server ứng dụng.
3. **Hệ thống** phân tích ngữ cảnh, văn phong cuộc trò chuyện và hiển thị 3 phương án văn bản trên popover trợ lý AI (`U-DLG-03`).
4. **Người dùng** chọn một trong 3 phương án.
5. **Hệ thống** kiểm tra người dùng vẫn đang ở đúng hội thoại và bản nháp chưa bị sửa đổi trong lúc chờ, sau đó điền phương án đã chọn vào ô soạn nháp để người dùng kiểm tra hoặc chỉnh sửa trước khi gửi.

### Alternative Flows

- **A1: Ô soạn nháp đang trống (Chế độ gợi ý câu trả lời - Recommend)**: Tại bước 2, nếu ô soạn nháp chưa có chữ, hệ thống chỉ gửi tối đa 50 tin nhắn văn bản hợp lệ gần nhất ở máy khách để AI sinh 3 phương án trả lời tiếp nối mạch trao đổi và văn phong của hội thoại; tiếp tục từ bước 3.
- **A2: Ô soạn nháp đã có nội dung (Chế độ viết lại bản nháp - Refine)**: Tại bước 2, nếu ô soạn nháp đã có chữ, hệ thống gửi bản nháp cùng tối đa 50 tin nhắn văn bản hợp lệ gần nhất ở máy khách để AI viết lại bản nháp thành 3 phương án theo văn phong của hội thoại (nếu hội thoại chưa có lịch sử tin nhắn cục bộ thì AI viết lại dựa trên bản nháp); tiếp tục từ bước 3.
- **A3: Đóng trợ lý AI mà không chọn phương án**: Tại bước 4, người dùng đóng popover trợ lý AI hoặc nhấn ra ngoài; hệ thống giữ nguyên nội dung đang có trong ô soạn nháp và kết thúc Use Case.
- **A4: Yêu cầu tạo lại bộ 3 phương án mới**: Sau bước 3, người dùng đóng và kích hoạt lại trợ lý AI; hệ thống thực hiện lại từ bước 2 với ngữ cảnh cục bộ và bản nháp hiện hành.
- **A5: Bản nháp hoặc hội thoại đã thay đổi trước khi chọn phương án**: Tại bước 5, nếu người dùng đã chuyển sang hội thoại khác hoặc nội dung ô nháp đã thay đổi so với thời điểm mở trợ lý AI, hệ thống không ghi đè phương án lên ô nháp mới.

### Exception Flows

- **E1: Không có ngữ cảnh hợp lệ và ô nháp trống**: Tại bước 2, ô soạn nháp đang trống và bộ nhớ cục bộ của máy khách chưa có tin nhắn văn bản hợp lệ nào (hoặc tin nhắn nhóm E2EE chưa được giải mã); hệ thống không gửi yêu cầu lên server, thông báo chưa có ngữ cảnh để gợi ý và giữ nguyên trạng thái soạn tin.
- **E2: Dịch vụ AI không khả dụng, quá thời gian chờ (15 giây) hoặc vượt hạn mức**: Tại bước 2 hoặc bước 3, yêu cầu tới server ứng dụng hoặc Muse API gặp lỗi mạng, quá hạn chờ 15 giây hoặc bị giới hạn tần suất (`HTTP 429`); hệ thống thông báo lỗi, giữ nguyên ô soạn nháp để người dùng soạn thủ công và không tự động gọi lại ngầm.
- **E3: Kết quả AI rỗng hoặc sai định dạng**: Tại bước 3, dịch vụ AI từ chối xử lý hoặc không trả về danh sách 3 phương án văn bản hợp lệ theo cấu trúc quy định; hệ thống thông báo không tạo được gợi ý và giữ nguyên ô soạn nháp.
- **E4: Mất quyền soạn tin hoặc phiên hết hiệu lực**: Tại bước 2 hoặc bước 5, phiên đăng nhập hết hạn hoặc người dùng mất quyền gửi tin trong hội thoại (do bị chặn trong chat 1-1 hoặc không còn là thành viên nhóm); hệ thống hủy thao tác AI và cập nhật trạng thái khung soạn tin.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Ngữ cảnh gửi cho AI được lấy tự động từ bộ nhớ cục bộ của máy khách (local client), gồm tối đa 50 tin nhắn văn bản hợp lệ gần nhất của chính hội thoại đó theo thứ tự thời gian. Chỉ lấy các tin mà người dùng còn quyền đọc, chưa bị thu hồi, chưa xóa phía mình và đã được giải mã thành bản rõ trên thiết bị (đối với nhóm E2EE).
  - **BR-02**: Mỗi lượt kích hoạt trả về đúng 3 phương án văn bản phản ánh ngữ cảnh và văn phong/phong cách giao tiếp của cuộc trò chuyện: gợi ý câu trả lời tiếp theo khi muốn phản hồi (`Recommend`) hoặc viết lại câu chữ khi đang có bản nháp (`Refine`).
  - **BR-03**: Kết quả AI chỉ là gợi ý hỗ trợ soạn thảo, tuyệt đối không tự động gửi thành tin nhắn trong hội thoại. Phương án người dùng chọn chỉ được điền vào ô soạn nháp khi hội thoại đích và bản nháp gốc chưa bị thay đổi trong thời gian chờ.
  - **BR-04**: Khi người dùng sử dụng tính năng AI, các tin nhắn ngữ cảnh cục bộ (tối đa 50 tin gần nhất) và bản nháp được gửi dưới dạng bản rõ tới server ứng dụng và nhà cung cấp Meta Model API theo [DEC-08](../../../../decisions/llm-assistant.md), kể cả khi trích xuất từ nhóm chat mã hóa đầu cuối (E2EE).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Mọi lỗi từ phía AI hoặc mạng không được làm mất bản nháp hiện tại hay cản trở việc gõ và gửi tin nhắn thủ công. Phản hồi trễ từ yêu cầu cũ không được ghi đè lên bản nháp mới hoặc hội thoại khác.
  - **NFR-02**: Cuộc gọi tới Muse API phải đi qua server ứng dụng với thời gian chờ tối đa 15 giây; khóa API (`MODEL_API_KEY`) chỉ lưu trong cấu hình server, không nhúng vào máy khách.
  - **NFR-03**: Không gửi khóa giải mã E2EE lên server hoặc nhà cung cấp AI; không ghi nội dung bản nháp, ngữ cảnh hội thoại hay phương án trả về vào nhật ký vận hành (operational logs) của server. Toàn bộ phương án trả về từ AI chỉ được xử lý như văn bản thuần túy.
