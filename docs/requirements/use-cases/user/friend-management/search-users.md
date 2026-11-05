# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-FRD-02
- **Use Case Name**: Tìm người dùng
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
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
  - Không hiển thị người mình chặn và người chặn mình

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
- **Alt 2b — Ẩn hai chiều khi có chặn**: Tại bước 2, phương án UC nguồn loại cả người đang chặn User và người User chủ động chặn, kể cả username chính xác.
- **Alt 3a — Kết quả đã thay đổi**: Tại bước 3, nếu tài khoản/quan hệ đổi, cập nhật hoặc bỏ kết quả không còn được phép; phản hồi của tiêu chí cũ không thay kết quả mới.

### Exception Flows

- **Ex 1a — Chuỗi rỗng**: không truy vấn toàn bộ tài khoản; yêu cầu nêu tiêu chí rồi tiếp tục bước 1.
- **Ex 2a — Mạng/server lỗi**: giữ tiêu chí để thử lại, không coi lỗi là không tồn tại tài khoản.
- **Ex 2b — Phiên bị thu hồi**: dừng và không trả thông tin dựa trên phiên cũ.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: Tìm theo username hoặc họ tên
  > **NOTE:** Cần chốt đặt độ dài tối thiểu, phương pháp so khớp dấu/hoa/thường, kích thước trang
  - BR-02: Ẩn người User chặn và người chặn User dù nhập chính xác username.

    > **NOTE:** Đề xuất ẩn kết quả tìm kiếm theo cả hai chiều chặn

  - BR-03: Gửi lời mời, mở hội thoại/gửi tin và tạo nhóm là các mục tiêu được dẫn chiếu, không là nhánh cập nhật quan hệ trong UC tìm.
- **Non-Functional Requirements (NFR)**:
  - NFR-01: Kiểm tra quyền hiển thị tại server, làm mới kết quả khi quan hệ chặn thay đổi.
  - NFR-02: Thời gian tìm người dùng cần được xác định và đo riêng; không tự áp ngưỡng tìm nội dung tin nhắn.

    > **NOTE:** Đề xuất cần chốt ngưỡng độ trễ và điều kiện đo cho tìm người dùng trước khi đưa vào nghiệm thu.