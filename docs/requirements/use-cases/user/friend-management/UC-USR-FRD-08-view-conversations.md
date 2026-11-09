# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-FRD-08
- **Use Case Name**: Xem và lọc danh sách hội thoại
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Không có.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng tra cứu hội thoại cá nhân và nhóm theo phạm vi Tất cả, Chưa đọc hoặc Nhóm.
- **Goal**: Xác định hội thoại cần theo dõi.
- **Trigger**: Người dùng yêu cầu xem danh sách hội thoại.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ.
- **Post-conditions**: Danh sách hội thoại đúng quyền truy cập và điều kiện lọc được cung cấp.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** yêu cầu xem các hội thoại của mình.
2. **Hệ thống** trả danh sách hội thoại được phép truy cập, kèm trạng thái chưa đọc và online/offline.
3. **Người dùng** chọn phạm vi Chưa đọc hoặc Nhóm để thu hẹp danh sách.
4. **Hệ thống** trả các hội thoại phù hợp với phạm vi đã chọn.

### Alternative Flows

- **A1 — Xem tất cả**: Sau bước 2, người dùng giữ phạm vi Tất cả; kết thúc use case.
- **A2 — Không có kết quả**: Tại bước 2 hoặc 4, danh sách rỗng. Người dùng đổi phạm vi ở bước 3 hoặc kết thúc.
- **A3 — Có cập nhật khi đang xem**: Tại bước 2 hoặc 4, có tin mới hoặc thay đổi trạng thái đọc, online hay quyền truy cập. Hệ thống cập nhật danh sách theo bộ lọc hiện tại.

### Exception Flows

- **E1 — Lỗi tải hoặc chưa đồng bộ đủ**: Tại bước 2 hoặc 4, hệ thống cung cấp phần dữ liệu đã có và còn được phép xem, nêu rõ chưa hoàn tất và cho phép thử lại.
- **E2 — Phiên hết hiệu lực**: Hệ thống dừng truy cập và yêu cầu đăng nhập lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Tất cả gồm chat riêng và nhóm; Chưa đọc gồm hội thoại có tin từ người khác mà người dùng chưa đọc; Nhóm chỉ gồm hội thoại nhóm.
  - **BR-02**: Danh sách chat riêng không giới hạn ở bạn bè. Chặn hoặc hủy kết bạn vẫn giữ lịch sử theo [DEC-03 (đề xuất)](../../../../decisions/blocking-and-friendship.md).
  - **BR-03**: Xem danh sách không đánh dấu tin đã đọc; tin nhóm chưa giải mã không tự được coi là đã đọc.
  - **BR-04**: Chat riêng lấy trạng thái online của đối phương. Nhóm online khi có ít nhất một thành viên khác online; chỉ xác định offline khi biết không có thành viên khác online.
  - **BR-05**: Hội thoại nhóm đã rời vẫn được liệt kê để đọc lịch sử trong phạm vi được phép trước khi rời. Người bị xóa khỏi nhóm chỉ giữ lịch sử từng được đọc theo [DEC-05 (đề xuất)](../../../../decisions/group-permissions-and-e2ee.md); cả hai trường hợp không nhận hoặc gửi tin nhóm mới.
  - **BR-06**: Xem danh sách không yêu cầu khóa giải mã nhóm; đọc nội dung E2EE cần khóa hợp lệ theo [DEC-05 (đề xuất)](../../../../decisions/group-permissions-and-e2ee.md).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Server kiểm tra quyền truy cập; không gửi khóa giải mã hoặc bản rõ tin nhóm lên server để lọc danh sách.
  - **NFR-02**: Phản hồi cũ không ghi đè bộ lọc mới; chưa đồng bộ đủ không được coi là danh sách rỗng hoặc trạng thái offline.
  - **NFR-03**: Giữ bản nháp của hội thoại đang mở khi xem hoặc đổi bộ lọc.
