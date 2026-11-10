# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-GRP-03
- **Use Case Name**: Thôi quyền Admin và ở lại nhóm
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Admin nhóm
- **Secondary Actor(s)**: Người kế nhiệm khi cần.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin tự chuyển thành thành viên thường; bàn giao cho người kế nhiệm nếu là Admin duy nhất.
- **Goal**: Người dùng thôi quyền quản trị, vẫn tham gia nhóm và nhóm còn Admin.
- **Trigger**: Admin yêu cầu thôi quyền nhưng ở lại nhóm.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và là Admin của nhóm đang hoạt động.
- **Post-conditions**: Người dùng trở thành thành viên thường; người kế nhiệm được cấp quyền nếu cần. Nhóm vẫn có ít nhất một Admin.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** yêu cầu thôi quyền quản trị.
2. **Hệ thống** xác định đây là Admin duy nhất và cung cấp các thành viên đủ điều kiện kế nhiệm.
3. **Admin** chọn người kế nhiệm và xác nhận.
4. **Hệ thống** kiểm tra lại điều kiện, cấp quyền cho người kế nhiệm và chuyển Admin hiện tại thành thành viên thường.

### Alternative Flows

- **A1 — Còn Admin khác**: Tại bước 2, không cần chọn kế nhiệm. Người dùng xác nhận; bước 4 chỉ hạ quyền của người yêu cầu.
- **A2 — Hủy thao tác**: Tại bước 3, người dùng không xác nhận; giữ nguyên các vai trò.

### Exception Flows

- **E1 — Không có người kế nhiệm hợp lệ**: Tại bước 2, nhóm chỉ còn người yêu cầu hoặc không có ứng viên đủ điều kiện. Hệ thống từ chối hạ quyền.
- **E2 — Điều kiện bàn giao thay đổi**: Tại bước 4, người kế nhiệm hoặc số Admin thay đổi. Hệ thống chưa cập nhật quyền; quay lại bước 2 để xác nhận lại.
- **E3 — Mất quyền hoặc nhóm không hoạt động**: Tại bước 4, hệ thống từ chối; yêu cầu đăng nhập lại nếu phiên hết hiệu lực.
- **E4 — Lỗi ghi nhận hoặc mất xác nhận**: Tại bước 4, kiểm tra kết quả trước khi thử lại, kể cả khi yêu cầu trước đã làm người dùng mất quyền Admin.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Người kế nhiệm là thành viên khác, tài khoản không bị khóa/xóa; không cần là bạn bè hoặc online, không bị loại vì chặn cá nhân.
  - **BR-02**: Admin trong nhóm ngang quyền; bàn giao không tạo quyền Admin hệ thống.
  - **BR-03**: Thôi quyền không thay tư cách thành viên hoặc quyền đọc/gửi theo khóa hiện có. Muốn rời nhóm thực hiện [UC-USR-GRP-02-leave-group](UC-USR-GRP-02-leave-group.md).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Cấp quyền kế nhiệm và hạ quyền cùng thành công hoặc không thay đổi; luôn giữ ít nhất một Admin.
  - **NFR-02**: Server kiểm tra vai trò mới nhất; người đã thôi quyền không tiếp tục quản trị bằng quyền cũ.
  - **NFR-03**: Gửi lại yêu cầu không cấp quyền hoặc bàn giao thêm lần nữa.
