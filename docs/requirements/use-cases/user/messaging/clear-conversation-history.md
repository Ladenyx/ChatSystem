# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-MSG-05
- **Use Case Name**: Xóa toàn bộ lịch sử phía mình
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
- **Primary Actor**: User có quyền thao tác lịch sử hội thoại của mình.
- **Secondary Actor(s)**: Không có.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User xóa toàn bộ lịch sử thuộc phạm vi xóa ở phía mình của một hội thoại chat riêng hoặc nhóm E2EE, sau khi được giải thích phạm vi tác động.
- **Goal**: Lịch sử cũ không còn trong phạm vi đọc/tìm của User; lịch sử người khác và tư cách thành viên không đổi.
- **Trigger**: User muốn xóa toàn bộ lịch sử phía mình của một hội thoại chat riêng hoặc nhóm E2EE.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ và hội thoại nguồn xác định là chat riêng hoặc nhóm E2EE; User có quyền cập nhật trạng thái xóa ở phía mình.
  - Phạm vi dữ liệu sẽ bị xóa được xác định để User xác nhận.
- **Post-conditions**:
  - Thành công khi trạng thái xóa riêng toàn lịch sử trong phạm vi đã xác nhận được ghi nhận và đồng bộ.
  - Không thu hồi tin, không rời nhóm/hủy bạn bè; tin mới tiếp tục xuất hiện theo quyền.
  - Hủy trước ghi nhận không thay đổi lịch sử; chưa rõ kết quả không được coi đã hoàn tất.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Yêu cầu xóa toàn bộ lịch sử phía mình của hội thoại chat riêng hoặc nhóm E2EE. | Xác định đúng hội thoại nguồn và giải thích lịch sử của đối phương/các thành viên khác vẫn giữ, quyền thành viên không đổi. |
| 2 | Kiểm tra phạm vi và xác nhận. | Kiểm tra quyền mới nhất rồi ghi nhận trạng thái xóa riêng cho phạm vi đã xác nhận. |
| 3 | Nhận kết quả. | Loại lịch sử đó khỏi đọc/tìm ở phía User, đồng bộ trạng thái khi đăng nhập lại hoặc đổi máy. |

### Alternative Flows

- **Alt 2a — Hủy**: Tại bước 2, User không xác nhận; kết thúc không thay đổi.
- **Alt 3a — Hội thoại có tin mới sau phạm vi xóa**: Tại bước 3 và sau đó, giữ tin mới theo quy tắc quyền; không tiếp tục xóa tự động những tin ngoài phạm vi đã xác nhận.
- **Alt 2b — Đối chiếu thao tác đã thành công**: Tại bước 2–3, nếu yêu cầu đã ghi nhận thì trả đúng kết quả, không chuyển thao tác sang hội thoại mới.

### Exception Flows

- **Ex 1a — Mất quyền/nguồn không tồn tại**: dừng thao tác, không xóa lịch sử hội thoại khác.
- **Ex 2a — Không nhận xác nhận**: đối chiếu kết quả trước thử lại; không tự suy diễn thành công.
- **Ex 2b — Server từ chối/phiên hết hạn**: không báo xóa thành công, chỉ giữ trạng thái đã xác nhận.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: Áp dụng cho cả chat riêng và nhóm E2EE.
  - BR-02: Phạm vi xóa chỉ là lịch sử phía User thực hiện.
  - BR-03: Lịch sử của đối phương trong chat riêng hoặc các thành viên khác trong nhóm không bị thay đổi.
  - BR-04: User không cần có vai trò Admin nhóm để xóa lịch sử phía mình trong nhóm.
  - BR-05: Xóa lịch sử nhóm không làm User rời nhóm.
  - BR-06: Xóa lịch sử phía mình không thu hồi tin nhắn với mọi người.
  - BR-07: Trạng thái xóa được đồng bộ tới phiên/thiết bị mới của User.
  - BR-08: Tìm kiếm của User không trả lại phần lịch sử đã xóa.
  - BR-09: Mốc bao gồm tin đến đồng thời lúc xác nhận chưa được nguồn mô tả chính xác.
- **Non-Functional Requirements (NFR)**:
  - NFR-01: Việc cập nhật lịch sử/chỉ mục/cache phải nhất quán và không làm mất lịch sử của người khác.
  - NFR-02: Phản hồi gắn đúng hội thoại/yêu cầu, không bị áp dụng sang hội thoại đang mở khác.
