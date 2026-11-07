# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-MSG-06
- **Use Case Name**: Thu hồi tin đã gửi
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 07/11/2026
- **Primary Actor**: Người gửi tin còn đủ điều kiện thu hồi.
- **Secondary Actor(s)**: Người tham gia hội thoại nhận cập nhật thu hồi.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người gửi yêu cầu thu hồi tin của mình trong thời hạn; nội dung bị loại khỏi khả năng đọc/tìm của các bên theo cập nhật thu hồi.
- **Goal**: Tin đủ điều kiện được ghi nhận đã thu hồi với các bên, khác xóa phía mình.
- **Trigger**: Người gửi muốn thu hồi nội dung mình đã gửi.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ; tin nguồn và người gửi xác định.
  - Tin thuộc chính actor và yêu cầu trong **48 giờ đầu** theo quy tắc hiện có; server kiểm tra lại khi xử lý.
- **Post-conditions**:
  - Thành công khi server ghi nhận thu hồi; nội dung bị loại khỏi tìm kiếm và đồng bộ tới các bên, gồm người offline.
  - Có thông tin thay thế cho biết tin đã thu hồi; không cam kết xóa bản sao từng được lưu trên thiết bị ngoài kiểm soát hệ thống.
  - Thất bại do quyền/thời hạn không thu hồi; xóa riêng chỉ thực hiện nếu User phát sinh ý định khác.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn tin của mình muốn thu hồi. | Xác định người gửi, hội thoại và thời điểm server đã ghi nhận tin. |
| 2 | Xác nhận yêu cầu thu hồi. | Server xác thực actor, quyền hiện tại và thời hạn, xử lý trên trạng thái mới nhất. |
| 3 | Nhận kết quả. | Ghi nhận thu hồi; cập nhật nội dung và kết quả tìm của các bên, chuyển sự kiện cho người chưa kết nối khi họ đồng bộ. |

### Alternative Flows

- **Alt 2a — Hủy**: Trước ghi nhận ở bước 2, User hủy; không thu hồi.
- **Alt 2b — Tin đã thu hồi**: Tại bước 2 hoặc khi thử lại, trả trạng thái đã có, không tạo thêm sự kiện/tác động trùng.
- **Alt 3a — Người nhận offline**: Tại bước 3, giữ trạng thái thu hồi để đồng bộ khi họ kết nối, không chờ họ online để xác nhận yêu cầu đã ghi nhận.

### Exception Flows

- **Ex 2a — Tin của người khác hoặc quá 48 giờ**: từ chối thu hồi; Admin nhóm không có ngoại lệ. Nếu User muốn xóa riêng thì dẫn UC-U-MSG-04, không tự thực hiện thay.
- **Ex 2b — Mất quyền/phiên không hợp lệ**: không dùng tham chiếu cũ để thu hồi, không suy quyền từ khả năng xem menu.
- **Ex 2c — Timeout/chưa rõ kết quả**: đối chiếu đúng ID tin/yêu cầu; không tạo bản tin mới hoặc báo thu hồi thành công khi chưa xác nhận.
- **Ex 3a — Đồng bộ thất bại**: giữ kết quả thu hồi đã ghi nhận và tiếp tục đồng bộ phù hợp, không phục hồi nội dung chỉ vì một client chưa nhận cập nhật.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: Chỉ người gửi được thu hồi tin của mình trong 48 giờ đầu kể từ khi gửi; Admin nhóm không có đặc quyền thu hồi tin của người khác.
  - BR-02: Xóa riêng không thay đổi quyền thu hồi; một tin đã bị xóa riêng không được dùng để mở rộng quyền còn lại.
  - BR-03: Thu hồi cập nhật trạng thái tin trong hội thoại và loại nội dung khỏi khả năng đọc/tìm của các bên; không tự rút báo cáo spam.
- **Non-Functional Requirements (NFR)**:
  - NFR-01: Thu hồi/lập kết quả tìm/đồng bộ phải thống nhất, không trả lại nội dung đã thu hồi từ cache.
  - NFR-02: Không đưa khóa nhóm cho server để thu hồi; xử lý trạng thái tin không tạo quyền giải mã mới.
