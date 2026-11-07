# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-FRD-02
- **Use Case Name**: Tìm người dùng
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 07/11/2026
- **Primary Actor**: User đã đăng nhập.
- **Secondary Actor(s)**: Tài khoản được phép xuất hiện trong kết quả tìm.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User tìm tài khoản theo username hoặc họ tên để xác định người muốn liên hệ. 
- **Goal**: Nhận kết quả tài khoản được phép hiển thị hoặc biết không có kết quả phù hợp.
- **Trigger**: User muốn tìm một tài khoản để kết nối hoặc liên hệ.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ, chuỗi tìm không rỗng và có thể kết nối server để kiểm tra quyền hiển thị.
  - Chính sách ẩn tài khoản theo quan hệ chặn phải được áp dụng tại server, không chỉ che kết quả phía client.
- **Post-conditions**:
  - Kết quả đúng tiêu chí/quyền và chỉ phản ánh trạng thái hiện tại đã xác nhận.
  - Không tạo lời mời, tình bạn, tin nhắn hay nhóm chỉ bằng việc tìm hoặc chọn kết quả.
  - Không hiển thị tài khoản đang chặn User trong kết quả tìm kiếm.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Cung cấp username hoặc họ tên muốn tìm. | Kiểm tra chuỗi đầu vào và xác định User truy vấn. |
| 2 | Yêu cầu tìm tài khoản. | Server tìm theo tiêu chí và loại tài khoản không được phép hiển thị theo chính sách đã áp dụng. |
| 3 | Đọc kết quả. | Cung cấp thông tin định danh đủ phân biệt và quan hệ đã xác nhận; không suy quyền thao tác từ việc xuất hiện. |
| 4 | Kết thúc tra cứu hoặc sửa tiêu chí. | Không thay đổi quan hệ; sửa tiêu chí quay lại bước 1. |

### Alternative Flows

- **Alt 2a — Không có kết quả được phép hiển thị**: Tại bước 2–3, trả kết quả rỗng theo phạm vi; không tiết lộ danh tính tài khoản bị loại hoặc lý do người đó chặn User.
- **Alt 2b — Tài khoản đích đang chặn User**: Tại bước 2, loại tài khoản đang chặn User khỏi kết quả, kể cả khi nhập chính xác username; trả kết quả còn lại như bước 3 và không tiết lộ lý do tài khoản bị ẩn.
- **Alt 3a — Kết quả đã thay đổi**: Tại bước 3, nếu tài khoản/quan hệ đổi, cập nhật hoặc bỏ kết quả không còn được phép; phản hồi của tiêu chí cũ không thay kết quả mới.

### Exception Flows

- **Ex 1a — Chuỗi rỗng**: không truy vấn toàn bộ tài khoản; yêu cầu nêu tiêu chí rồi tiếp tục bước 1.
- **Ex 2a — Mạng/server lỗi**: giữ tiêu chí để thử lại, không coi lỗi là không tồn tại tài khoản.
- **Ex 2b — Phiên bị thu hồi**: dừng và không trả thông tin dựa trên phiên cũ.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: Tìm theo username hoặc họ tên với từ khóa không rỗng. Danh sách dài cho phép xem tiếp kết quả trong cùng tiêu chí và phạm vi được phép hiển thị.
  - BR-02: Ẩn tài khoản đang chặn User dù nhập chính xác username.
  - BR-03: Gửi lời mời, mở hội thoại/gửi tin và tạo nhóm là các mục tiêu được dẫn chiếu, không là nhánh cập nhật quan hệ trong UC tìm.
- **Non-Functional Requirements (NFR)**:
  - NFR-01: Kiểm tra quyền hiển thị tại server, làm mới kết quả khi quan hệ chặn thay đổi.
  - NFR-02: Trong khi tìm kiếm, hiển thị trạng thái đang xử lý và cho phép User thay đổi tiêu chí; chỉ thông báo không có kết quả sau khi tìm xong trong phạm vi được phép.
