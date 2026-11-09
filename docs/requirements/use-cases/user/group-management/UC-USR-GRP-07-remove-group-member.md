# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-GRP-07
- **Use Case Name**: Xóa thành viên khỏi nhóm
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Admin nhóm
- **Secondary Actor(s)**: Thành viên bị xóa và các thành viên còn lại.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin loại một thành viên khác khỏi nhóm và chấm dứt quyền truy cập tin mới của người đó.
- **Goal**: Người được chọn không còn là thành viên nhóm.
- **Trigger**: Admin yêu cầu xóa thành viên.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và là Admin của nhóm đang hoạt động; tài khoản đích là thành viên khác trong nhóm.
- **Post-conditions**: Gỡ tư cách thành viên và quyền Admin của tài khoản đích nếu có; người đó không được gửi, nhận hoặc giải mã tin nhóm mới. Nhóm vẫn còn Admin.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** chọn thành viên cần xóa.
2. **Hệ thống** kiểm tra quyền và nêu hệ quả đối với quyền tham gia, lịch sử.
3. **Admin** xác nhận xóa thành viên.
4. **Hệ thống** kiểm tra lại điều kiện, gỡ thành viên cùng quyền Admin nếu có và ngăn truy cập tin mới.

### Alternative Flows

- **A1 — Đã rời hoặc bị xóa**: Tại bước 4, tài khoản đích không còn trong nhóm. Hệ thống trả trạng thái hiện tại; kết thúc.
- **A2 — Hủy thao tác**: Tại bước 3, Admin không xác nhận; giữ tư cách thành viên.
- **A3 — Cập nhật khóa chưa hoàn tất**: Sau bước 4, duy trì việc ngăn người bị xóa truy cập tin mới trong khi tiếp tục xử lý khóa; không báo hoàn tất bảo vệ E2EE khi chưa bảo đảm.

### Exception Flows

- **E1 — Mất quyền hoặc nhóm không hoạt động**: Tại bước 4, hệ thống từ chối; yêu cầu đăng nhập lại nếu phiên hết hiệu lực.
- **E2 — Nhóm sẽ không còn Admin**: Tại bước 4, hệ thống từ chối xóa; không tự chọn người kế nhiệm.
- **E3 — Lỗi ghi nhận hoặc mất xác nhận**: Tại bước 4, kiểm tra kết quả trước khi thử lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Chỉ Admin nhóm được xóa thành viên khác, kể cả Admin khác hoặc người tạo nhóm, miễn nhóm vẫn còn Admin.
  - **BR-02**: Muốn tự rời nhóm thực hiện [UC-USR-GRP-02-leave-group](UC-USR-GRP-02-leave-group.md).
  - **BR-03**: Người bị xóa được xem lịch sử từng đọc ở chế độ chỉ đọc; hệ thống không xóa từ xa bản sao đã có trên thiết bị.
  - **BR-04**: Chặn hoặc hủy kết bạn không tự xóa thành viên nhóm theo [DEC-03 (đề xuất)](../../../../decisions/blocking-and-friendship.md).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Gỡ tư cách và quyền Admin nhất quán, bảo vệ Admin cuối cùng; yêu cầu lặp không tạo thêm lần xóa.
  - **NFR-02**: Quyền và khóa phải ngăn người bị xóa truy cập tin mới; không gửi bản rõ thay thế khi cập nhật khóa chưa xong.
