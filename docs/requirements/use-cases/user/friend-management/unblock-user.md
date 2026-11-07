# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-FRD-07
- **Use Case Name**: Bỏ chặn người dùng
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 07/11/2026
- **Primary Actor**: User đã chủ động chặn tài khoản đích.
- **Secondary Actor(s)**: Tài khoản đích và các phiên nhận cập nhật quan hệ.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User gỡ chiều chặn do chính mình tạo. Bỏ chặn không tự khôi phục tình bạn hoặc gỡ chiều chặn của đối phương.
- **Goal**: Chiều chặn của actor được gỡ và quyền tương tác được đối chiếu theo quan hệ còn lại.
- **Trigger**: User muốn bỏ quan hệ chặn do mình tạo.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ; tài khoản đích thuộc quan hệ chặn của chính User.
  - User chỉ có quyền gỡ chiều do mình tạo.
- **Post-conditions**:
  - Thành công gỡ chiều chặn của User; chiều chặn của đối phương nếu có vẫn giữ.
  - Chat riêng chỉ có thể gửi mới khi không còn chiều chặn và các quyền khác hợp lệ.
  - Không tự khôi phục bạn bè/lời mời, gửi lại tin đã lỗi hoặc xóa lịch sử; lỗi/chưa rõ kết quả giữ trạng thái được xác nhận gần nhất.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Tra cứu các tài khoản mình đang chặn và xác định đích. | Lấy danh sách chiều chặn do actor tạo, không coi đó là danh sách những người chặn actor. |
| 2 | Yêu cầu gỡ chiều chặn của mình. | Server xác thực quyền, kiểm tra trạng thái mới nhất và gỡ đúng chiều do User tạo. |
| 3 | Nhận kết quả. | Cập nhật danh sách đã chặn và quan hệ liên quan, đối chiếu chiều còn lại để xác định quyền tương tác. |
| 4 | Kết thúc bỏ chặn. | Không phát sinh lời mời/tình bạn hoặc gửi tin tự động; User tự quyết định mục tiêu tiếp theo. |

### Alternative Flows

- **Alt 1a — Chưa chặn ai**: Tại bước 1, trả tập rỗng đã xác nhận; kết thúc không có thay đổi.
- **Alt 2a — Chiều chặn đã được gỡ**: Tại bước 2 hoặc đối chiếu, trả trạng thái hiện có, không ghi tác động trùng.
- **Alt 3a — Đối phương vẫn chặn User**: Tại bước 3, vẫn giữ hạn chế trao đổi riêng ở cả hai phía.
- **Alt 2b — Không tiếp tục yêu cầu**: Trước ghi nhận bước 2, User rời thao tác; không gỡ chiều chặn.

### Exception Flows

- **Ex 2a — Không đúng chủ chiều chặn**: từ chối, không gỡ quan hệ do người khác tạo.
- **Ex 2b — Mất xác nhận**: đối chiếu quan hệ, giữ hạn chế gửi khi chưa xác nhận đủ điều kiện.
- **Ex 2c — Phiên bị thu hồi hoặc tài khoản không còn được thao tác**: không báo đã gỡ trên trạng thái cũ, cung cấp kết quả phù hợp.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: Tập tài khoản đã chặn chỉ là nguồn chọn đích trong mục tiêu này; không dùng kết quả tìm người để vượt chính sách ẩn.
  - BR-02: Chặn hai chiều cần từng bên chủ động gỡ phần của mình; một lần bỏ chặn không thay quyết định bên còn lại.
  - BR-03: Sau khi User bỏ chặn, nếu đối phương vẫn chặn User thì User vẫn không tìm thấy đối phương trong kết quả tìm người dùng. Nếu không còn chiều chặn nào, áp dụng điều kiện tìm kiếm thông thường.
  - BR-04: Muốn kết bạn lại cần gửi/xử lý lời mời mới khi đủ điều kiện; không tự phát sinh từ bỏ chặn.
- **Non-Functional Requirements (NFR)**:
  - NFR-01: Server xác thực actor và chiều quan hệ; retry không gỡ nhầm tài khoản/chiều khác.
  - NFR-02: Cập nhật quyền gửi phải theo dữ liệu đã xác nhận; giữ nháp, không tự gửi lại tin lỗi.