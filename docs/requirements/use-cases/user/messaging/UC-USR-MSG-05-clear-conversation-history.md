# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-MSG-05
- **Use Case Name**: Xóa toàn bộ lịch sử phía mình
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Không có.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng xóa lịch sử phía mình của một hội thoại cá nhân hoặc nhóm.
- **Goal**: Lịch sử trong phạm vi đã xác nhận không còn được đọc hoặc tìm kiếm ở phía người dùng.
- **Trigger**: Người dùng yêu cầu xóa toàn bộ lịch sử của một hội thoại.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và quyền xóa lịch sử phía mình của hội thoại.
- **Post-conditions**: Trạng thái xóa được ghi nhận cho phạm vi đã xác nhận và đồng bộ tới các thiết bị của người dùng.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** yêu cầu xóa toàn bộ lịch sử phía mình của hội thoại đã chọn.
2. **Hệ thống** xác định phạm vi xóa và nêu rõ chỉ ảnh hưởng lịch sử của người dùng.
3. **Người dùng** xác nhận.
4. **Hệ thống** kiểm tra quyền, ghi nhận xóa riêng và loại lịch sử thuộc phạm vi đó khỏi đọc/tìm kiếm.

### Alternative Flows

- **A1 — Hủy thao tác**: Tại bước 3, người dùng không xác nhận; kết thúc.
- **A2 — Có tin mới ngoài phạm vi xóa**: Tại bước 4 hoặc sau đó, giữ tin mới để người dùng tiếp tục đọc.
- **A3 — Yêu cầu đã hoàn tất**: Tại bước 4, hệ thống trả kết quả cũ; không mở rộng phạm vi xóa.

### Exception Flows

- **E1 — Mất quyền hoặc hội thoại không còn khả dụng**: Tại bước 2 hoặc 4, hệ thống từ chối yêu cầu.
- **E2 — Lỗi ghi nhận hoặc mất xác nhận**: Tại bước 4, kiểm tra kết quả trước khi thử lại.
- **E3 — Phiên hết hiệu lực**: Hệ thống từ chối và yêu cầu đăng nhập lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Áp dụng cho chat riêng và nhóm E2EE; thành viên không cần quyền Admin để xóa lịch sử của mình.
  - **BR-02**: Xóa phía mình không thu hồi tin với người khác, hủy kết bạn hoặc làm người dùng rời nhóm.
  - **BR-03**: Chỉ xóa phạm vi đã xác nhận; tin mới ngoài phạm vi đó vẫn xuất hiện, kể cả khi gửi lại yêu cầu xóa.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Đồng bộ trạng thái xóa giữa các thiết bị, lịch sử và tìm kiếm; dữ liệu đã xóa không xuất hiện lại từ bộ nhớ đệm.
  - **NFR-02**: Yêu cầu và kết quả xóa luôn gắn với hội thoại ban đầu.