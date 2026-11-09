# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-FRD-05
- **Use Case Name**: Hủy kết bạn
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Người bạn bị hủy kết bạn.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng chấm dứt quan hệ bạn bè với một tài khoản.
- **Goal**: Hai tài khoản không còn là bạn bè.
- **Trigger**: Người dùng yêu cầu hủy kết bạn.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và đang là bạn với tài khoản được chọn.
- **Post-conditions**: Quan hệ bạn bè bị gỡ khỏi cả hai phía.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** chọn người bạn và xác nhận hủy kết bạn.
2. **Hệ thống** kiểm tra quan hệ hiện tại và gỡ quan hệ bạn bè.
3. **Hệ thống** cập nhật danh sách bạn bè của hai bên và thông báo hoàn tất.

### Alternative Flows

- **A1 — Hủy thao tác**: Tại bước 1, người dùng không xác nhận; kết thúc và giữ quan hệ.
- **A2 — Đã hủy kết bạn**: Tại bước 2, quan hệ đã bị gỡ. Hệ thống trả trạng thái hiện tại; kết thúc.

### Exception Flows

- **E1 — Không nhận được xác nhận**: Tại bước 2–3, hệ thống kiểm tra lại quan hệ trước khi thử lại.
- **E2 — Phiên hết hiệu lực**: Hệ thống từ chối yêu cầu và yêu cầu đăng nhập lại.

## Business Rules & Non-Functional Requirements
- **Business Rules (BR)**:
  - **BR-01**: Hủy kết bạn không cần sự đồng ý hoặc trạng thái online của đối phương.
  - **BR-02**: Hủy kết bạn không tạo quan hệ chặn; hai bên vẫn có thể chat riêng khi đủ điều kiện.
  - **BR-03**: Lịch sử chat, tư cách thành viên và quyền trong nhóm chung được giữ nguyên.
  - **BR-04**: Kết bạn lại cần gửi và chấp nhận lời mời mới.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Gỡ quan hệ nhất quán ở hai phía, kể cả khi có yêu cầu lặp hoặc đồng thời.
  - **NFR-02**: Giữ bản nháp và trạng thái chặn hiện có khi cập nhật quan hệ.
