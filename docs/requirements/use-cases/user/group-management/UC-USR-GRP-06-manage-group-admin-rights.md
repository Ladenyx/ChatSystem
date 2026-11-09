# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-GRP-06
- **Use Case Name**: Quản lý quyền Admin của thành viên
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Admin nhóm
- **Secondary Actor(s)**: Thành viên được cấp hoặc thu hồi quyền.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin cấp hoặc thu hồi quyền quản trị của một thành viên khác.
- **Goal**: Vai trò của thành viên được cập nhật theo quyết định.
- **Trigger**: Admin yêu cầu thay đổi quyền quản trị của thành viên.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và là Admin của nhóm đang hoạt động; tài khoản đích là thành viên khác trong nhóm.
- **Post-conditions**: Vai trò của tài khoản đích được cập nhật; người đó vẫn là thành viên và nhóm còn ít nhất một Admin.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** chọn một thành viên thường để cấp quyền.
2. **Hệ thống** kiểm tra quyền người yêu cầu và vai trò hiện tại của thành viên.
3. **Admin** xác nhận cấp quyền.
4. **Hệ thống** kiểm tra lại điều kiện, cấp quyền Admin và cập nhật quyền thao tác của thành viên.

### Alternative Flows

- **A1 — Thu hồi quyền**: Thay bước 1, Admin chọn một Admin khác để thu hồi quyền. Sau xác nhận ở bước 3, bước 4 chuyển người đó về thành viên thường.
- **A2 — Vai trò đã đúng yêu cầu**: Tại bước 2 hoặc 4, hệ thống trả vai trò hiện tại; kết thúc.
- **A3 — Hủy thao tác**: Tại bước 3, Admin không xác nhận; giữ nguyên vai trò.

### Exception Flows

- **E1 — Người yêu cầu hoặc tài khoản đích mất điều kiện**: Tại bước 4, Admin mất quyền, người được chọn không còn thuộc nhóm hoặc nhóm ngừng hoạt động. Hệ thống từ chối.
- **E2 — Nhóm sẽ không còn Admin**: Tại bước 4, hệ thống từ chối thay đổi vi phạm điều kiện Admin cuối cùng.
- **E3 — Lỗi ghi nhận hoặc mất xác nhận**: Tại bước 4, kiểm tra kết quả trước khi thử lại.
- **E4 — Phiên hết hiệu lực**: Hệ thống từ chối yêu cầu và yêu cầu đăng nhập lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Các Admin ngang quyền; có thể thu hồi quyền của Admin khác, kể cả người tạo nhóm, miễn nhóm còn Admin.
  - **BR-02**: Quan hệ bạn bè hoặc chặn cá nhân không quyết định quyền quản trị của thành viên hiện có.
  - **BR-03**: Thôi quyền của chính mình thực hiện [UC-USR-GRP-03-relinquish-group-admin](UC-USR-GRP-03-relinquish-group-admin.md).
  - **BR-04**: Quyền Admin nhóm không cấp quyền Admin hệ thống, thu hồi tin của người khác hoặc tự thay đổi khả năng giải mã lịch sử.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Server kiểm tra vai trò mới nhất và bảo vệ Admin cuối cùng, kể cả khi có yêu cầu đồng thời.
  - **NFR-02**: Người bị thu hồi quyền không tiếp tục quản trị bằng quyền cũ; gửi lại yêu cầu không tạo thay đổi trùng.
