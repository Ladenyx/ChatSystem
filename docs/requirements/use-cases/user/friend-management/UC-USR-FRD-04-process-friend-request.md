# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-FRD-04
- **Use Case Name**: Xử lý lời mời kết bạn
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người nhận lời mời
- **Secondary Actor(s)**: Người gửi lời mời.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người nhận chấp nhận hoặc từ chối lời mời kết bạn.
- **Goal**: Ghi nhận quyết định và cập nhật quan hệ giữa hai bên.
- **Trigger**: Người dùng yêu cầu xem các lời mời kết bạn đã nhận.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ.
- **Post-conditions**: Lời mời được xử lý; chấp nhận tạo quan hệ bạn bè hai chiều, từ chối không tạo quan hệ bạn bè.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Hệ thống** cung cấp các lời mời nhận vào đang chờ; người dùng có thể lọc theo tên người gửi.
2. **Người dùng** chọn một lời mời và chấp nhận.
3. **Hệ thống** kiểm tra người nhận và hiệu lực lời mời; nếu chấp nhận, kiểm tra thêm điều kiện kết bạn.
4. **Hệ thống** ghi nhận chấp nhận, tạo quan hệ bạn bè và cập nhật trạng thái cho hai bên.

### Alternative Flows

- **A1 — Từ chối**: Thay bước 2, người dùng từ chối. Sau kiểm tra ở bước 3, hệ thống ghi nhận từ chối và kết thúc lời mời.
- **A2 — Không có lời mời phù hợp**: Tại bước 1, danh sách rỗng. Người dùng đổi điều kiện lọc hoặc kết thúc.

### Exception Flows

- **E1 — Lời mời không còn hiệu lực**: Tại bước 3, lời mời đã được xử lý hoặc hủy. Hệ thống trả trạng thái hiện tại; kết thúc.
- **E2 — Không thể chấp nhận**: Tại bước 3, một bên đang chặn hoặc tài khoản không còn đủ điều kiện kết bạn. Hệ thống từ chối tạo quan hệ.
- **E3 — Lỗi kết nối**: Tại bước 1, cho phép tải lại; tại bước 4, kiểm tra kết quả đã ghi nhận trước khi thử lại.
- **E4 — Sai người nhận hoặc phiên hết hiệu lực**: Hệ thống từ chối xử lý; yêu cầu đăng nhập lại nếu phiên hết hiệu lực.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Chỉ người nhận được chấp nhận hoặc từ chối lời mời.
  - **BR-02**: Chấp nhận yêu cầu lời mời còn chờ, hai tài khoản được phép kết bạn và không có chiều chặn nào; người gửi không cần online.
  - **BR-03**: Từ chối không chặn người gửi. Muốn kết bạn sau đó cần lời mời mới.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Ghi nhận quyết định và cập nhật tình bạn phải hoàn tất cùng nhau; xử lý lặp không tạo quan hệ trùng hoặc ghi đè quyết định đã có.
  - **NFR-02**: Server kiểm tra quyền và trạng thái mới nhất khi xử lý.
