# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-FRD-04
- **Use Case Name**: Xử lý lời mời kết bạn
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
- **Primary Actor**: User là người nhận của lời mời đang chờ.
- **Secondary Actor(s)**: Người gửi lời mời.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người nhận quyết định chấp nhận hoặc từ chối một lời mời. Hai quyết định là nhánh của cùng mục tiêu xử lý, khác gửi lời mời.
- **Goal**: Quyết định đối với lời mời được ghi nhận; chỉ chấp nhận tạo tình bạn.
- **Trigger**: User muốn quyết định một lời mời kết bạn đã nhận.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ; User là đúng người nhận.
  - Lời mời tồn tại và còn chờ; điều kiện tài khoản/quan hệ được kiểm tra lại khi xử lý.
  - Khi có lời mời nhận vào đang chờ từ một tài khoản, User chỉ được chấp nhận hoặc từ chối lời mời đó, không được gửi lời mời ngược chiều cho cùng tài khoản.
- **Post-conditions**:
  - Chấp nhận thành công tạo tình bạn hai chiều và kết thúc lời mời chờ.
  - Từ chối thành công kết thúc lời mời mà không tạo tình bạn/chặn; không tự xóa lịch sử hoặc rời nhóm chung.
  - Yêu cầu sai quyền hoặc đã thay đổi không được ghi như quyết định thành công mới.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Xem lời mời kết bạn. | Xác minh đúng người nhận và lấy trạng thái lời mời hiện tại. Khi lời mời còn chờ, chỉ cho phép chấp nhận/từ chối, không cho gửi lời mời ngược chiều. |
| 2 | Quyết định chấp nhận. | Server kiểm tra lời mời còn hiệu lực, điều kiện tài khoản và không có chiều chặn; ghi nhận chấp nhận và tình bạn nhất quán. |
| 3 | Nhận kết quả. | Kết thúc lời mời chờ, cung cấp quan hệ bạn bè đã xác nhận cho hai bên; hiện diện online không ảnh hưởng thành công. |

### Alternative Flows

- **Alt 2a — Từ chối**: Thay bước 2, User quyết định từ chối. Server kiểm tra quyền/trạng thái rồi ghi nhận từ chối, kết thúc lời mời; bước 3 trả trạng thái không tạo tình bạn.
- **Alt 2b — Chưa quyết định**: Tại bước 2, User rời việc xử lý trước ghi nhận; lời mời vẫn chờ nếu không có sự kiện khác.
- **Alt 1a — Tra cứu/lọc lời mời nhận vào**: Trước bước 1, User tra cứu tập lời mời nhận vào chờ theo tên; danh sách rỗng không buộc tạo hoặc xử lý một lời mời. Tiếp tục bước 1 khi chọn được hồ sơ.

### Exception Flows

- **Ex 2a — Lời mời vừa được xử lý/hủy hoặc quan hệ đổi**: kiểm tra trạng thái mới; không tạo tình bạn trùng hoặc ghi đè quyết định khác, trả kết quả đối chiếu.
- **Ex 2b — Chặn/tài khoản không đủ điều kiện**: không tạo tình bạn từ dữ liệu cũ; cung cấp trạng thái phù hợp.
- **Ex 2c — Chưa rõ kết quả do mạng**: đối chiếu lời mời/quan hệ trước thử lại; không phát sinh nhiều quan hệ khi yêu cầu lặp.
- **Ex 1b — Không phải người nhận hoặc phiên hết hạn**: từ chối, không cho quyết định thay người khác.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: Chỉ người nhận được quyết định; lời mời gửi đi hoặc mục tìm kiếm không phải quyền xử lý thay người nhận.
  - BR-02: Chấp nhận/từ chối là hai kết quả cùng mục tiêu. Muốn kết bạn lại sau từ chối cần một lời mời hợp lệ mới, không tự chuyển thành chặn.
  - BR-03: Ghi quyết định và thay đổi tình bạn phải nhất quán với xử lý đồng thời/chặn; nguồn quyền và dữ liệu là server.
  - BR-04: Khi đối phương gửi lời mời và lời mời còn chờ, trạng thái quan hệ của User với đối phương tự chuyển sang chờ chấp nhận/từ chối; User không được gửi lời mời ngược chiều.
- **Non-Functional Requirements (NFR)**:
  - NFR-01: Cập nhật danh sách lời mời và tình bạn theo cùng kết quả đã xác nhận; retry không tạo tình bạn trùng.
  - NFR-02: Phân biệt không có lời mời với lỗi tải/không có quyền; không tiết lộ dữ liệu lời mời không gửi tới actor.
