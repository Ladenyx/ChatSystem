# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-MSG-02
- **Use Case Name**: Gửi tin nhắn
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
- **Primary Actor**: User đã đăng nhập có quyền gửi vào hội thoại.
- **Secondary Actor(s)**: Người nhận chat riêng hoặc các thành viên nhóm.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User gửi nội dung đã soạn vào hội thoại cá nhân hoặc nhóm; hệ thống kiểm tra quyền và lưu tin hợp lệ.
- **Goal**: Tin nhắn được server lưu thành công, không tạo bản trùng khi gửi lại cùng yêu cầu.
- **Trigger**: User thực hiện gửi nội dung đã soạn vào hội thoại.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ, nội dung hợp lệ, hội thoại đích xác định; với nhóm thì User đang là thành viên và có khóa gửi phù hợp.
  - Chat riêng không có chiều chặn nào. Khi có chặn, không tạo yêu cầu gửi mới từ luồng soạn hiện hành.
- **Post-conditions**:
  - Thành công khi server lưu tin và client nhận được phản hồi lưu thành công kèm định danh tin; không cần chờ thiết bị người nhận nhận tin hoặc người nhận đọc.
  - Nếu chưa nhận được phản hồi lưu từ server, giữ nội dung/yêu cầu chờ để đối chiếu; chưa kết luận gửi thành công hay thất bại chỉ vì mất phản hồi. Nếu bị từ chối quyền, không chuyển tin hoặc phát thông báo và không tự gửi lại sau khi quyền được khôi phục.
  - Không tự gửi thêm nội dung từ AI hoặc nháp của hội thoại khác.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Xác định hội thoại và nội dung muốn gửi. | Kiểm tra đầu vào theo BR-06, quyền và khóa cần thiết; gắn nội dung với hội thoại nguồn và định danh tin. |
| 2 | Gửi nội dung đã soạn. | Client giữ yêu cầu để đối chiếu và chuyển tới server; với nhóm, mã hóa bằng khóa phù hợp trước khi chuyển. |
| 3 | Chờ kết quả gửi. | Server kiểm tra phiên/quyền hiện tại, lưu tin hợp lệ và trả phản hồi lưu thành công kèm định danh tin. Nếu cùng yêu cầu đã được lưu, trả kết quả đã có, không tạo bản tin thứ hai. |
| 4 | Nhận kết quả gửi. | Client ghi nhận tin đã được server lưu thành công; kết thúc mục tiêu gửi. Hệ thống chuyển/đồng bộ tin tới thiết bị người nhận khi có thể và phát thông báo phù hợp. |

### Alternative Flows

- **Alt 2a — Chat nhóm E2EE**: Tại bước 2, dùng khóa nhóm hợp lệ và kiểm tra thành viên hiện tại tại lúc xử lý; chặn cá nhân giữa thành viên không tự từ chối tin nhóm. Tiếp tục bước 3.
- **Alt 3a — Người nhận offline**: Tại bước 3–4, nếu thiết bị người nhận chưa kết nối, server vẫn lưu tin và trả kết quả gửi thành công cho client người gửi; phát thông báo theo khả năng kênh. Đồng bộ nội dung tới thiết bị người nhận khi kết nối lại.
- **Alt 3b — Chưa rõ kết quả vì mất kết nối**: Tại bước 3, đối chiếu/thử lại cùng ID tin sau reconnect, kiểm tra lại quyền và không tạo tin/lượt hoạt động trùng. Nếu server đã lưu, dùng kết quả cũ và chuyển bước 4.

### Exception Flows

- **Ex 1a — Nội dung không hợp lệ**: Nếu nội dung không hợp lệ hoặc vượt giới hạn tại BR-06, không gửi và giữ nguyên nháp để User sửa; thông báo lý do. Không tự cắt nội dung hoặc chia thành nhiều tin để gửi.
- **Ex 1b — Thiếu khóa nhóm**: không gửi bản rõ thay thế hoặc yêu cầu server giải mã; giữ nội dung và dẫn quy trình khóa hiện có.
- **Ex 3a — Chặn hoặc quyền thành viên thay đổi khi gửi**: server từ chối, không chuyển/phát thông báo. Nếu server đã chấp nhận trước thay đổi thì tiếp tục đồng bộ tin đã chấp nhận; không tự gửi lại tin đã bị từ chối.
- **Ex 3b — Phiên bị thu hồi/tài khoản bị khóa**: dừng yêu cầu cần xác thực; không tự gửi bằng phiên mới.
- **Ex 4a — Kênh nhận/thông báo lỗi**: giữ kết quả đã lưu, đồng bộ lại khi có thể; không lùi thành gửi thất bại và không suy ra đã nhận/đọc.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: 
    - Kết quả gửi thành công dựa trên phản hồi server đã lưu tin. 
    - Đã nhận dựa trên phản hồi của thiết bị người nhận; đã đọc dựa trên sự kiện đọc hợp lệ trong [UC-U-MSG-01](view-conversation-history.md).
    - Trạng thái nhận/đọc được cập nhật bất đồng bộ, không phải điều kiện hoàn tất UC gửi tin 
    - phản hồi lặp hoặc đến sai thứ tự không làm trạng thái lùi. Không suy ra đã nhận/đọc từ thông báo hoặc trạng thái Online.
  - BR-02: Mỗi lần gửi lại giữ ID tin. Cùng yêu cầu không được tạo thêm tin hoặc lượt hoạt động; phải kiểm tra quyền hiện tại.
  - BR-03: Chat riêng còn chặn ở bất kỳ chiều nào thì không gửi mới. Bỏ chặn một phía chưa đủ, không tự retry tin bị từ chối.
  - BR-04: Có thể chat riêng với người chưa kết bạn. User phải có quyền thực tế, không suy quyền từ kết quả tìm người cũ.
  - BR-05: AI chỉ cung cấp nháp theo UC-U-AI-01/02. Việc mở AI, áp dụng phương án hoặc đổi hội thoại không tự kích hoạt gửi.
  - BR-06 — Giới hạn độ dài tin nhắn: Mỗi tin nhắn được phép có tối đa **2.000 ký tự**, áp dụng cho chat cá nhân và nhóm. Kiểm tra độ dài trên nội dung văn bản trước khi mã hóa, không tính theo số byte hoặc độ dài bản mã. Nếu vượt giới hạn, từ chối gửi theo Ex 1a; không tự cắt hoặc chia nhỏ nội dung. 
  > Nguồn tham khảo: [PipeOne — Character limits per message](https://pipeone.me/help-center-english/character-limits-per-message)
- **Non-Functional Requirements (NFR)**:
  - NFR-01: Không mất/trùng tin server đã lưu, kể cả timeout/reconnect; giữ nháp/yêu cầu theo hội thoại nguồn.
  - NFR-02: Server không giữ khóa bí mật tự giải mã luồng nhóm. Không đưa nội dung/khóa nhạy cảm vào lỗi vận hành.
  - NFR-03: Mục tiêu p95 gửi tới GUI nhận giải mã/render ≤1 giây là đề xuất DEC-09, cần đo trong môi trường được ghi nhận.