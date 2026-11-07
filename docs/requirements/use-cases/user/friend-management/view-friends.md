# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-FRD-01
- **Use Case Name**: Xem danh sách bạn bè
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 07/11/2026
- **Primary Actor**: User đã đăng nhập.
- **Secondary Actor(s)**: Bạn bè của User có trạng thái hiện diện được phép xem.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User tra cứu danh sách bạn bè hiện tại theo tên hoặc trạng thái online. Đổi bộ lọc chỉ thay phạm vi đọc, không cập nhật quan hệ.
- **Goal**: Nhận danh sách bạn bè đúng quan hệ và điều kiện lọc, kể cả danh sách trống.
- **Trigger**: User muốn xem hoặc tìm một người trong danh sách bạn bè của mình.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ; có quyền đọc quan hệ bạn bè của chính mình.
  - Không bắt buộc đã có bạn bè hoặc có người đang online.
- **Post-conditions**:
  - Danh sách phản ánh bạn bè hiện tại trong phạm vi lọc và trạng thái dữ liệu đã xác nhận.
  - Xem danh sách không tự gửi tin, lời mời, tạo nhóm hoặc thay đổi quan hệ.
  - Lỗi dữ liệu không được kết luận thành không có bạn bè.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Yêu cầu xem bạn bè của mình. | Xác thực và lấy quan hệ bạn bè hiện tại trong phạm vi được phép. |
| 2 | Nêu điều kiện tên hoặc trạng thái hiện diện nếu cần. | Lọc trong tập bạn bè, không mở rộng sang toàn bộ tài khoản. |
| 3 | Đọc danh sách phù hợp. | Cung cấp tên/trạng thái hiện diện còn hợp lệ, phân biệt có dữ liệu hay không có kết quả trong phạm vi đã xét. |
| 4 | Kết thúc tra cứu hoặc đổi điều kiện. | Giữ quan hệ không đổi; khi đổi điều kiện quay lại bước 2. |

### Alternative Flows

- **Alt 2a — Chỉ xem bạn đang online**: Tại bước 2, giới hạn cùng danh sách bạn bè vào trạng thái online. Đây là bộ lọc của mục tiêu xem, không buộc người dùng online mới được liên hệ.
- **Alt 3a — Không có dữ liệu phù hợp**: Tại bước 3, trả danh sách rỗng đúng phạm vi, kết thúc xem thành công. Không suy không có bạn bè từ không khớp tên.
- **Alt 3b — Quan hệ/hiện diện thay đổi**: Tại bước 3, cập nhật khi có sự kiện hoặc tải lại; người đã hủy kết bạn không còn thuộc tập này. Không dùng ảnh chụp danh sách để cấp quyền thao tác.

### Exception Flows

- **Ex 1a — Lỗi tải**: giữ điều kiện và phần dữ liệu đã xác nhận, cho thử lại; không coi lỗi là danh sách trống.
- **Ex 1b — Phiên không còn hợp lệ**: dừng truy cập và không trả dữ liệu tài khoản khác.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: Chỉ bạn bè hiện tại xuất hiện trong danh sách bạn bè; trạng thái hiện diện không thay thế kiểm tra quan hệ/quyền.
  - BR-02: Các mục tiêu gửi tin, tạo nhóm, hủy kết bạn và chặn có UC riêng. Điều hướng từ một người bạn chưa hoàn thành những mục tiêu đó.
  - BR-03: Cho phép lọc bạn bè theo tên trong cả danh sách Tất cả bạn bè và Đang Online; bỏ điều kiện tên thì hiển thị lại danh sách trong phạm vi trạng thái đã chọn.
  - BR-04: Danh sách dài cho phép xem tiếp các bạn bè còn lại với cùng điều kiện lọc; kết quả đang tải chưa được coi là toàn bộ danh sách.
- **Non-Functional Requirements (NFR)**:
  - NFR-01: Phản hồi cũ không ghi đè điều kiện lọc mới; trạng thái quan hệ/hiện diện được cập nhật nhất quán.
  - NFR-02: Không mở quyền đọc danh bạ người khác từ dữ liệu client truyền.