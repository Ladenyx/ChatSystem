# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-FRD-06
- **Use Case Name**: Chặn người dùng
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
- **Primary Actor**: User muốn chặn một tài khoản.
- **Secondary Actor(s)**: Tài khoản bị chặn; các phiên liên quan được đồng bộ quan hệ.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User ghi nhận quan hệ chặn do mình tạo. Hai điểm thao tác danh sách bạn bè và thông tin chat dùng cùng quy trình.
- **Goal**: Quan hệ chặn và hệ quả đối với tình bạn/lời mời/tin riêng được ghi nhận nhất quán.
- **Trigger**: User muốn ngăn trao đổi riêng với một tài khoản xác định.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ; tài khoản đích khác User và được phép thao tác.
  - Server xác định actor từ phiên, không nhận quyền chặn thay tài khoản khác từ client.
- **Post-conditions**:
  - Thành công ghi chiều chặn của User, hủy tình bạn và lời mời chờ giữa cặp theo quy tắc nguồn.
  - Trong chat riêng còn chặn bất kỳ chiều nào thì không được gửi mới; server từ chối tin tới sau thời điểm chặn và không phát thông báo.
  - Giữ lịch sử, không rời/xóa thành viên nhóm chung. Hủy trước xác nhận không đổi quan hệ.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn tài khoản cần chặn. | Kiểm tra tài khoản/quan hệ và giải thích hệ quả đối với tình bạn, lời mời và trao đổi riêng. |
| 2 | Xác nhận chặn. | Server kiểm tra phiên/trạng thái mới nhất, ghi chiều chặn và hủy tình bạn/lời mời chờ nhất quán. |
| 3 | Nhận kết quả. | Cập nhật quan hệ của các bên; ngăn trao đổi riêng mới theo chính sách C03 và giữ nguyên quyền nhóm không liên quan. |
| 4 | Kết thúc thao tác. | Lưu kết quả đã xác nhận; không tự gửi lại tin từng bị từ chối hoặc chuyển sang mục tiêu bỏ chặn. |

### Alternative Flows

- **Alt 1a — Điểm vào khác**: Tại bước 1, tài khoản có thể được xác định từ bạn bè hoặc thông tin hội thoại riêng; cả hai tiếp tục cùng bước 2, không có hai UC chặn.
- **Alt 2a — Hủy**: Trước ghi nhận ở bước 2, User hủy; không cập nhật quan hệ.
- **Alt 2b — User đã chặn trước đó**: Tại bước 2, trả trạng thái đã chặn, không tạo tác động lặp hoặc đảo chiều chặn của đối phương.

### Exception Flows

- **Ex 2a — Mất xác nhận**: đối chiếu trạng thái với server trước thử lại, không báo đã chặn chỉ từ cập nhật tạm tại client.
- **Ex 2b — Phiên không hợp lệ/tài khoản đích không còn được thao tác**: từ chối theo trạng thái mới, không dùng dữ liệu cũ để vượt quyền.
- **Ex 2c — Gửi tin/quan hệ đổi đồng thời**: xác định theo thứ tự server. Tin đã được tiếp nhận trước chặn tiếp tục đồng bộ; tin đến sau chặn bị từ chối và không tự retry.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: Chat riêng còn một chiều chặn cũng không cho gửi mới ở cả hai phía.
  - BR-02:Người chủ động chặn thấy hai button;người bị chặn bị vô hiệu hóa thanh nhập tin nhắn.
  - BR-03: Chặn không thu hồi nội dung cũ hoặc tự rời nhóm. Quan hệ chặn giữa thành viên không tự cấm gửi/đọc trong nhóm chung.
  - BR-04: Hệ quả tìm người dùng ẩn cả hai chiều.
  - BR-05: Chỉ tạo chiều chặn của actor; không cho actor gỡ chiều do người khác tạo.
- **Non-Functional Requirements (NFR)**:
  - NFR-01: Ghi chặn/hủy tình bạn/lời mời nhất quán; các yêu cầu đồng thời không để vừa chặn thành công vừa còn quan hệ bị cấm.
  - NFR-02: Server kiểm tra khi gửi tin/tìm kiếm/cập nhật quan hệ, không dựa vào việc client đã đổi UI.
  - NFR-03: Không mất nháp/lịch sử do cập nhật quan hệ.