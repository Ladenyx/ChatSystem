# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-GRP-05
- **Use Case Name**: Thêm thành viên nhóm
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Thành viên nhóm, gồm cả Admin
- **Secondary Actor(s)**: Các tài khoản được thêm.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Thành viên thêm bạn bè vào nhóm đang tham gia.
- **Goal**: Những người được chọn và đủ điều kiện trở thành thành viên nhóm.
- **Trigger**: Người dùng yêu cầu thêm thành viên.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và là thành viên của nhóm đang hoạt động.
- **Post-conditions**: Các tài khoản được xác nhận thêm thành công trở thành thành viên; khả năng đọc lịch sử E2EE phụ thuộc việc chia sẻ khóa.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** chọn một hoặc nhiều người bạn để thêm.
2. **Hệ thống** kiểm tra quan hệ, tư cách thành viên và giới hạn số người trong nhóm.
3. **Người dùng** xác nhận danh sách.
4. **Hệ thống** xác nhận toàn bộ lựa chọn còn hợp lệ, thêm thành viên theo danh sách đã xác nhận và trả kết quả từng người.

### Alternative Flows

- **A1 — Đã là thành viên**: Tại bước 4, một tài khoản đã được thêm bởi yêu cầu khác. Hệ thống trả trạng thái hiện tại, không thêm trùng.
- **A2 — Chưa chia sẻ khóa xong**: Sau bước 4, tiếp tục chia sẻ khóa cho thành viên đã được thêm; chỉ cho đọc/gửi khi có quyền và khóa phù hợp.
- **A3 — Hủy thao tác**: Tại bước 3, người dùng không xác nhận; kết thúc.

### Exception Flows

- **E1 — Chưa chọn người hoặc vượt sức chứa**: Tại bước 2 hoặc 4, hệ thống yêu cầu điều chỉnh danh sách; không tự bỏ bớt người để vừa giới hạn.
- **E2 — Tài khoản hoặc quan hệ thay đổi**: Tại bước 4, có người không còn đủ điều kiện. Hệ thống báo kết quả từng người; người dùng đối chiếu và xác nhận lại trước yêu cầu mới.
- **E3 — Mất tư cách thành viên hoặc nhóm không hoạt động**: Tại bước 4, hệ thống từ chối; yêu cầu đăng nhập lại nếu phiên hết hiệu lực.
- **E4 — Lỗi ghi nhận hoặc mất xác nhận**: Tại bước 4, kiểm tra kết quả từng tài khoản trước khi thử lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Admin và thành viên thường đều được thêm bạn bè; mỗi người được thêm phải chưa thuộc nhóm và không có chiều chặn nào với người mời.
  - **BR-02**: Chặn với thành viên khác không ngăn việc thêm vào nhóm; người được thêm có thể offline.
  - **BR-03**: Tổng thành viên không vượt giới hạn của hệ thống; kết quả phải nêu rõ ai được thêm, đã là thành viên hoặc không được thêm.
  - **BR-04**: Người mới được đọc lịch sử trước khi tham gia, trừ tin đã thu hồi; quyền đọc chỉ hoàn tất khi được chia sẻ khả năng giải mã.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Server kiểm tra điều kiện hiện tại; yêu cầu lặp hoặc đồng thời không tạo thành viên trùng.
  - **NFR-02**: Lỗi chia sẻ khóa không làm lặp việc thêm thành viên; không gửi khóa bí mật hoặc bản rõ lịch sử cho server.

**Còn mở**: Khi thêm nhiều người mà có lựa chọn không hợp lệ hoặc đã là thành viên, cần chốt từ chối toàn bộ hay chỉ thêm những người còn hợp lệ.
