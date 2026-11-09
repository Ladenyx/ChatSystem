# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-MSG-03
- **Use Case Name**: Tìm tin nhắn
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Không có.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng tìm nội dung tin trong một hoặc tất cả hội thoại được phép đọc.
- **Goal**: Tìm được tin cần xem và mở đúng vị trí trong lịch sử.
- **Trigger**: Người dùng yêu cầu tìm nội dung đã trao đổi.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và quyền đọc dữ liệu cần tìm; nội dung nhóm cần khóa giải mã trên thiết bị.
- **Post-conditions**: Kết quả phù hợp được cung cấp; tin được chọn được mở trong hội thoại nguồn nếu còn quyền truy cập.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** nhập chuỗi cần tìm và chọn phạm vi một hoặc tất cả hội thoại.
2. **Hệ thống** tìm trong lịch sử được phép đọc, loại nội dung đã xóa phía mình hoặc thu hồi.
3. **Người dùng** chọn một kết quả.
4. **Hệ thống** kiểm tra lại quyền và trạng thái tin, mở đúng vị trí theo [UC-USR-MSG-01](UC-USR-MSG-01-view-conversation-history.md).

### Alternative Flows

- **A1 — Không có kết quả**: Tại bước 2, người dùng có thể đổi từ khóa/phạm vi ở bước 1 hoặc kết thúc.
- **A2 — Chưa đồng bộ hết lịch sử**: Tại bước 2, trả kết quả đã tìm được và nêu rõ phạm vi chưa hoàn tất; tiếp tục khi dữ liệu sẵn sàng.

### Exception Flows

- **E1 — Từ khóa rỗng**: Tại bước 1, yêu cầu nhập từ khóa.
- **E2 — Lỗi tìm kiếm hoặc thiếu khóa**: Tại bước 2, thông báo phần dữ liệu chưa tìm được; cho thử lại hoặc khôi phục khóa.
- **E3 — Tin không còn được đọc**: Tại bước 4, tin đã thu hồi, xóa riêng hoặc mất quyền. Loại kết quả đó và cho chọn tin khác.
- **E4 — Phiên hết hiệu lực**: Hệ thống dừng truy cập và yêu cầu đăng nhập lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Tìm chuỗi văn bản trong đúng phạm vi đã chọn; đổi từ khóa không tự mở rộng phạm vi.
  - **BR-02**: Trạng thái xóa riêng/thu hồi phải được áp dụng cả khi tìm và mở kết quả.
  - **BR-03**: Nội dung nhóm chỉ được tìm trong phần lịch sử thiết bị được phép giải mã, theo [DEC-05 (đề xuất)](../../../../decisions/group-permissions-and-e2ee.md).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Không gửi bản rõ nhóm hoặc khóa bí mật cho server để tìm kiếm.
  - **NFR-02**: Kết quả cũ không ghi đè truy vấn mới; chưa tìm hết dữ liệu không được coi là không có kết quả.
  - **NFR-03**: Mục tiêu đề xuất: tìm kiếm ≤2 giây khi lịch sử/chỉ mục đã sẵn sàng
