# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-MSG-04
  - **Ánh xạ theo [screen.md](../../../screen.md)**: Mã màn hình: [U-SCR-07](../../../screen.md#12-khung-làm-việc-chính-main-workspace-shell); Feature ID: [U-16](../../../screen.md#12-khung-làm-việc-chính-main-workspace-shell). Xem [bảng mapping](../../../use-case-map.md#21-user--nhắn-tin).
- **Use Case Name**: Xóa tin phía mình
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
- **Primary Actor**: User có quyền đọc hội thoại.
- **Secondary Actor(s)**: Không có.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User xóa một hoặc nhiều tin khỏi phạm vi lịch sử của chính mình; người tham gia khác giữ lịch sử của họ.
- **Goal**: Các tin được chọn không còn được đọc/tìm trong lịch sử riêng của User theo quy tắc xóa riêng.
- **Trigger**: User muốn bỏ một hoặc nhiều tin khỏi lịch sử của mình.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ; tin được chọn thuộc lịch sử User có quyền thao tác.
  - Phạm vi chọn xác định, không dựa vào dữ liệu của hội thoại khác.
- **Post-conditions**:
  - Thành công khi trạng thái xóa riêng được ghi nhận và đồng bộ; không thu hồi tin với người khác.
  - Tin mới vẫn được cung cấp theo quyền, tình bạn và thành viên nhóm không đổi.
  - Hủy/từ chối không thực hiện xóa một tập tin khác ngoài lựa chọn đã xác nhận; chưa rõ kết quả phải đối chiếu.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn một hoặc nhiều tin muốn xóa phía mình. | Kiểm tra tin thuộc phạm vi lịch sử của User và xác định đúng tập tin nguồn. |
| 2 | Xác nhận xóa phía mình. | Kiểm tra lại quyền và ghi nhận trạng thái xóa riêng của User cho tập tin đã xác nhận. |
| 3 | Nhận kết quả. | Loại nội dung khỏi lịch sử và tìm kiếm riêng, đồng bộ trạng thái khi đăng nhập lại/đổi máy. |

### Alternative Flows

- **Alt 1a — Chọn nhiều tin**: Tại bước 1, chọn nhiều tin cùng phạm vi được phép; kiểm tra không trùng. Số lượng là nhánh của cùng mục tiêu xóa riêng, tiếp tục bước 2.
- **Alt 2a — Hủy trước ghi nhận**: Tại bước 2, User hủy; không ghi xóa và kết thúc không thay đổi lịch sử.
- **Alt 2b — Trạng thái xóa đã được ghi nhận**: Tại bước 2–3 hoặc khi đối chiếu lại, dùng kết quả đã có; không tạo tác động lên lịch sử người khác.

### Exception Flows

- **Ex 1a — Tin không thuộc phạm vi/quyền đã mất**: không thực hiện trên dữ liệu cũ; tải lại phạm vi và yêu cầu xác nhận lựa chọn hợp lệ nếu còn.
- **Ex 2a — Mất kết nối/chưa rõ kết quả**: giữ thông tin lựa chọn/yêu cầu để đối chiếu, không báo thành công khi chưa xác nhận.
- **Ex 2b — Server từ chối hoặc phiên hết hạn**: không ghi nhận xóa thay người khác, bảo toàn trạng thái đã xác nhận.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: Có thể xóa phía mình với tin nhận hoặc tin gửi; không suy quyền thu hồi từ quyền xóa riêng.
  - BR-02: Trạng thái xóa riêng đồng bộ khi đổi máy/đăng nhập lại và loại tin khỏi kết quả tìm của User; người khác giữ nội dung.
  - BR-03: Xóa riêng không xóa toàn bộ hội thoại, không hủy kết bạn, rời nhóm hay thay đổi các bản chụp bằng chứng đã tiếp nhận.
  - BR-04: Giới hạn số tin và chính sách thành công một phần khi lựa chọn thay đổi chưa có kết luận riêng trong nguồn; cần confirm CF-23 nếu triển khai xử lý từng phần, không tự báo toàn bộ đã xóa.
- **Non-Functional Requirements (NFR)**:
  - NFR-01: Kết quả phải phản ánh trạng thái server/đồng bộ, không để cache hoặc chỉ mục trả lại nội dung đã xóa riêng.
  - NFR-02: Xử lý yêu cầu lặp an toàn và không mở rộng tập tin do phản hồi/đổi hội thoại.
