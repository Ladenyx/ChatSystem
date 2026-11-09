# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-MSG-01
- **Use Case Name**: Xem hội thoại và lịch sử
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Người gửi nhận cập nhật đã đọc.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng đọc tin nhắn trong hội thoại cá nhân hoặc nhóm.
- **Goal**: Xem nội dung trao đổi trong phạm vi được phép truy cập.
- **Trigger**: Người dùng chọn một hội thoại để đọc.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và quyền xem hội thoại; nội dung nhóm E2EE cần khóa giải mã tương ứng trên thiết bị.
- **Post-conditions**: Phần lịch sử được phép đọc được cung cấp; ghi nhận đã đọc đối với tin thực sự được xem.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** yêu cầu xem một hội thoại.
2. **Hệ thống** kiểm tra quyền, tải lịch sử gần nhất và giải mã tin nhóm trên thiết bị.
3. **Người dùng** đọc tin nhắn.
4. **Hệ thống** ghi nhận đã đọc đối với các tin nhận được xem và cập nhật tin mới theo quyền hiện tại.

### Alternative Flows

- **A1 — Xem tin cũ hơn**: Sau bước 3, người dùng yêu cầu lịch sử trước đó; hệ thống tải trang tiếp theo rồi quay lại bước 3.
- **A2 — Không có lịch sử**: Tại bước 2, không có tin trong phạm vi được đọc; kết thúc.
- **A3 — Chỉ một phần dữ liệu sẵn sàng**: Tại bước 2, cung cấp phần đã đồng bộ và giải mã, nêu rõ phần chưa hoàn tất.
- **A4 — Đọc khi mất kết nối**: Tại bước 3–4, lưu xác nhận đọc của dữ liệu còn được phép xem để đồng bộ khi kết nối lại.

### Exception Flows

- **E1 — Mất quyền hoặc phiên hết hiệu lực**: Tại bước 2 hoặc 4, dừng truy cập phần không còn được phép; yêu cầu đăng nhập lại nếu cần.
- **E2 — Lỗi tải lịch sử**: Tại bước 2, giữ dữ liệu đã có và cho phép thử lại.
- **E3 — Thiếu khóa hoặc giải mã thất bại**: Tại bước 2, yêu cầu thiết lập/khôi phục khóa; không ghi nhận tin chưa giải mã là đã đọc.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Tối đa 50 tin/trang; áp dụng trạng thái xóa phía mình và thu hồi khi tải lịch sử
  - **BR-02**: Chỉ ghi nhận đã đọc khi người nhận xem được nội dung; tải nền hoặc nhận thông báo chưa đủ. Xác nhận gắn với từng tin/người nhận, không đại diện toàn nhóm.
  - **BR-03**: Chặn cá nhân vẫn giữ lịch sử cũ và không ngăn đọc tin trong nhóm chung, theo [DEC-03 (đề xuất)](../../../../decisions/blocking-and-friendship.md).
  - **BR-04**: Người tự rời nhóm chỉ đọc lịch sử được phép trước khi rời; người bị xóa giữ lịch sử từng đọc. Cả hai không nhận tin nhóm mới
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Đồng bộ không làm mất/trùng tin hoặc lùi trạng thái đã đọc; phản hồi cũ không đưa dữ liệu sang hội thoại khác.
  - **NFR-02**: Giải mã nhóm trên thiết bị, không gửi bản rõ hoặc khóa bí mật cho server.
  - **NFR-03**: Mục tiêu đề xuất: tải trang 50 tin trong tối đa 2 giây theo [DEC-09](../../../../decisions/performance-and-acceptance.md).
