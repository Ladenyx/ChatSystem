# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-MSG-01
- **Use Case Name**: Xem hội thoại và lịch sử
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
- **Primary Actor**: User đã đăng nhập có quyền truy cập hội thoại.
- **Secondary Actor(s)**: Các thành viên có quyền đọc hội thoại.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User đọc các tin đã được phép truy cập trong hội thoại cá nhân hoặc nhóm, tải thêm lịch sử khi cần và tiếp tục theo dõi tin đến.
- **Goal**: Đọc đúng lịch sử theo quyền, không bắt buộc gửi hoặc thay đổi dữ liệu hội thoại.
- **Trigger**: User muốn đọc hoặc đọc lại một hội thoại.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ và có quyền xem hội thoại nguồn.
  - Với nội dung nhóm E2EE, thiết bị có khóa tương ứng để đọc phần lịch sử đó; đăng nhập không tự đồng nghĩa có khóa.
- **Post-conditions**:
  - Khi thành công, User đọc được phần lịch sử còn quyền truy cập; chỉ ghi nhận đã xem đối với tin thực sự đọc.
  - Hủy hoặc mất quyền không mở rộng phạm vi lịch sử; lỗi tải không được coi là lịch sử trống.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn một hội thoại. | Kiểm tra quyền hiện tại và lấy phần lịch sử gần nhất còn được phép đọc. |
| 2 | Đọc các tin đã nhận. | Giải mã nội dung nhóm trên thiết bị khi đủ khóa; ghi nhận đã xem đối với tin từ người khác thực sự được đọc. |
| 3 | Yêu cầu xem phần lịch sử cũ hơn. | Lấy trang trước, bảo toàn thứ tự và loại trùng; tiếp tục đọc trong cùng hội thoại. |
| 4 | Kết thúc đọc hoặc tiếp tục theo dõi hội thoại. | Giữ mốc dữ liệu đã xác nhận; tin đến được cung cấp theo quyền hiện tại, không bắt buộc gửi tin. |

### Alternative Flows

- **Alt 1a — Không có lịch sử**: Tại bước 1, nếu chưa có tin trong phạm vi được đọc, trả kết quả rỗng và kết thúc xem thành công; không tạo tin nhắn.
- **Alt 2a — Chỉ có một phần lịch sử sẵn sàng**: Tại bước 2, cho đọc phần đã đồng bộ/giải mã, ghi rõ phần chưa hoàn tất; khi có thêm dữ liệu, quay lại bước 2 hoặc 3.
- **Alt 2b — Ghi nhận đã xem khi offline**: Tại bước 2, lưu xác nhận đọc để đồng bộ khi kết nối và quyền cho phép. Ghi nhận theo tin và tài khoản, không lặp người đọc hoặc làm trạng thái lùi.
- **Alt 3a — Chuyển hội thoại**: Tại bước 3, kết thúc phiên đọc nguồn; phản hồi cũ không được đưa vào hội thoại mới.

### Exception Flows

- **Ex 1a — Không có quyền hoặc phiên hết hạn**: dừng đọc phần không còn được phép, không trả dữ liệu dựa trên quyền cũ.
- **Ex 1b — Lỗi tải/đồng bộ**: giữ phần đã đọc và cho thử lại; không trả lỗi dưới dạng không có lịch sử.
- **Ex 2a — Thiếu khóa/không giải mã được**: không coi bản mã là nội dung đọc thành công; dẫn quy trình khóa hiện có, không tự giải mã tại server hoặc hứa khôi phục khi mất toàn bộ khóa.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: Trang lịch sử có kích thước 50 tin theo nguồn; trang cuối có thể ít hơn. Quyền xóa riêng/thu hồi phải được áp dụng khi đọc và tìm.
  - BR-02: Chỉ ghi nhận đã xem khi nội dung thực sự được đọc trong hội thoại đang xem, với E2EE đã giải mã. Online, nhận thông báo, tải nền hoặc nhận bản mã chưa đủ.
  - BR-03: Xác nhận đọc gắn đúng tin/người đọc; không tính người gửi là người nhận đã xem. Trong nhóm, một người đọc không đại diện toàn nhóm.
  - BR-04: Chặn cá nhân giữ lịch sử cũ và không tự hạn chế nhóm chung. Truy cập sau khi rời/bị xóa nhóm theo đề xuất DEC-05, chưa tự phê duyệt giữ lịch sử.
- **Non-Functional Requirements (NFR)**:
  - NFR-01: Không mất/trùng tin đã được server xác nhận sau reconnect; phản hồi cũ không ghi đè hội thoại hiện tại.
  - NFR-02: Không truyền bản rõ nhóm cho server để đọc/tìm thông thường. Không suy ra khóa từ phiên đăng nhập.
  - NFR-03: Mục tiêu tải trang 50 tin ≤2 giây và đồng bộ 10.000 tin ≤30 giây là đề xuất DEC-09, chưa là kết quả đã đo.
