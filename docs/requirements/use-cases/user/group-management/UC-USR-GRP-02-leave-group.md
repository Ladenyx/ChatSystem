# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-GRP-02
- **Use Case Name**: Rời nhóm
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Thành viên nhóm, gồm cả Admin
- **Secondary Actor(s)**: Người kế nhiệm khi cần và các thành viên còn lại.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Thành viên rời nhóm; Admin duy nhất bàn giao trước khi rời nếu nhóm còn người khác.
- **Goal**: Chấm dứt tư cách thành viên và quyền đối với tin nhóm mới.
- **Trigger**: Người dùng yêu cầu rời nhóm.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và là thành viên của nhóm đang hoạt động.
- **Post-conditions**: Người dùng không còn trong nhóm; nhóm còn thành viên vẫn có Admin. Người đã rời chỉ được đọc lịch sử trong phạm vi được phép trước khi rời.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** yêu cầu rời nhóm.
2. **Hệ thống** kiểm tra vai trò và số Admin; nhóm vẫn còn Admin sau khi người dùng rời.
3. **Người dùng** xác nhận rời nhóm.
4. **Hệ thống** kiểm tra lại điều kiện, gỡ tư cách thành viên và quyền Admin nếu có, đồng thời ngăn truy cập tin mới.

### Alternative Flows

- **A1 — Admin duy nhất, còn người khác**: Tại bước 2, người dùng chọn một thành viên đủ điều kiện kế nhiệm rồi tiếp tục bước 3. Bước 4 cấp quyền kế nhiệm và cho người dùng rời trong cùng một lần cập nhật.
- **A2 — Thành viên cuối cùng**: Tại bước 2, không cần kế nhiệm. Tiếp tục bước 3–4; nhóm không còn thành viên.
- **A3 — Hủy rời nhóm**: Tại bước 3, người dùng không xác nhận; giữ tư cách và vai trò.

### Exception Flows

- **E1 — Không có người kế nhiệm hợp lệ**: Trong A1, hệ thống không cho Admin duy nhất rời khi nhóm còn người khác; giữ nguyên vai trò.
- **E2 — Điều kiện thay đổi**: Tại bước 4, số Admin hoặc người kế nhiệm thay đổi. Hệ thống chưa ghi nhận yêu cầu; quay lại bước 2 để chọn và xác nhận lại.
- **E3 — Phiên, thành viên hoặc nhóm không hợp lệ**: Tại bước 4, hệ thống từ chối yêu cầu mới; yêu cầu đăng nhập lại nếu phiên hết hiệu lực.
- **E4 — Lỗi ghi nhận hoặc mất xác nhận**: Tại bước 4, kiểm tra kết quả trước khi thử lại; không thực hiện riêng phần bàn giao hoặc phần rời nhóm.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Người kế nhiệm là thành viên khác, tài khoản không bị khóa/xóa; không bắt buộc là bạn bè hoặc online. Chặn cá nhân không loại ứng viên.
  - **BR-02**: Còn Admin khác thì không cần chọn người kế nhiệm.
  - **BR-03**: Người đã rời không được gửi, nhận hoặc giải mã tin nhóm mới; lịch sử được phép xem ở chế độ chỉ đọc.
  - **BR-04**: Muốn thôi quyền nhưng vẫn ở lại thực hiện [UC-USR-GRP-03-relinquish-group-admin](UC-USR-GRP-03-relinquish-group-admin.md).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Bàn giao và rời nhóm cùng thành công hoặc không thay đổi, kể cả khi có yêu cầu đồng thời.
  - **NFR-02**: Quyền và khóa phải ngăn truy cập tin mới; không gửi bản rõ thay thế trong lúc cập nhật khóa.
  - **NFR-03**: Giữ bản nháp và lịch sử còn được phép xem; không tự gửi lại tin đang chờ sau khi rời.
