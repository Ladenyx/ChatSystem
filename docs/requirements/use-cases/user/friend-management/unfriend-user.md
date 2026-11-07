# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-FRD-05
- **Use Case Name**: Hủy kết bạn
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 07/11/2026
- **Primary Actor**: User đang có quan hệ bạn bè với tài khoản đích.
- **Secondary Actor(s)**: Người bạn bị hủy quan hệ.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User chấm dứt tình bạn đang có sau xác nhận. Quan hệ chặn, lịch sử và nhóm chung không tự thay đổi.
- **Goal**: Quan hệ bạn bè giữa hai tài khoản được gỡ nhất quán.
- **Trigger**: User muốn chấm dứt tình bạn với một người.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ; tài khoản đích khác User, quan hệ bạn bè tồn tại tại lúc xác nhận.
  - Không yêu cầu đối phương đang online.
- **Post-conditions**:
  - Thành công gỡ tình bạn hai chiều; người đó không còn thuộc danh sách bạn bè/online
  - Không tự chặn, xóa lịch sử hoặc rời nhóm; chưa xác nhận thì không coi đã hủy.
  - Hủy thao tác thì ghi nhận giữ nguyên quan hệ.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn người bạn muốn hủy quan hệ. | Kiểm tra tình bạn và giải thích phạm vi chỉ chấm dứt quan hệ bạn bè. |
| 2 | Xác nhận hủy kết bạn. | Server xác thực actor, kiểm tra quan hệ mới nhất và gỡ tình bạn của hai phía. |
| 3 | Nhận kết quả. | Cập nhật tập bạn bè liên quan theo quan hệ đã ghi nhận, giữ lịch sử và nhóm chung. |

### Alternative Flows

- **Alt 2a — Không xác nhận**: Tại bước 2, User hủy; không thay đổi tình bạn.
- **Alt 2b — Quan hệ đã bị gỡ**: Tại bước 2 hoặc khi đối chiếu, trả trạng thái mới thay vì báo tạo một tác động mới; không tự khôi phục quan hệ để tiếp tục.

### Exception Flows

- **Ex 2a — Phiên/quyền không hợp lệ**: từ chối, không cho thao tác thay tài khoản khác.
- **Ex 2b — Chưa rõ kết quả**: đối chiếu quan hệ trước thử lại, không suy thành công từ việc phía client vừa bỏ dữ liệu.
- **Ex 2c — Thay đổi đồng thời**: không làm mất trạng thái chặn đã được ghi bởi một yêu cầu khác; cung cấp trạng thái quan hệ hiện tại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: Hủy kết bạn và chặn là hai mục tiêu độc lập; không tự thêm quan hệ chặn.
  - BR-02: Hủy kết bạn không tự thay đổi tư cách thành viên/quyền nhóm, không thu hồi hoặc xóa tin.
  - BR-03: Sau khi hủy kết bạn, hai tài khoản vẫn được chat riêng nếu không có chiều chặn nào và các điều kiện phiên/quyền còn hợp lệ, chat riêng không bắt buộc là bạn bè.

  - BR-04: Kết bạn lại phải qua lời mời và quyết định hợp lệ, không được suy từ lịch sử tình bạn cũ.
- **Non-Functional Requirements (NFR)**:
  - NFR-01: Gỡ quan hệ hai phía nhất quán, retry an toàn và cập nhật danh sách đúng phạm vi.
  - NFR-02: Bảo toàn nháp/lịch sử trong khi quan hệ thay đổi.