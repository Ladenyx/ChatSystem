# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-FRD-06
- **Use Case Name**: Chặn người dùng
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Người bị chặn.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng chặn một tài khoản để ngừng trao đổi riêng và lời mời kết bạn.
- **Goal**: Quan hệ chặn có hiệu lực với tài khoản được chọn.
- **Trigger**: Người dùng yêu cầu chặn một tài khoản.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ; tài khoản đích tồn tại và khác chính mình.
- **Post-conditions**: Ghi nhận chặn, hủy tình bạn và mọi lời mời đang chờ giữa hai bên; ngăn tin riêng và lời mời mới ở cả hai chiều.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** xác định tài khoản cần chặn.
2. **Hệ thống** nêu hệ quả đối với tình bạn, lời mời và tin nhắn riêng.
3. **Người dùng** xác nhận chặn.
4. **Hệ thống** ghi nhận chặn, hủy tình bạn/lời mời đang chờ, ngăn trao đổi riêng mới và thông báo hoàn tất.

### Alternative Flows

- **A1 — Hủy thao tác**: Tại bước 3, người dùng không xác nhận; kết thúc và giữ quan hệ.
- **A2 — Đã chặn**: Tại bước 4, người dùng đã chặn tài khoản này. Hệ thống trả trạng thái hiện tại; kết thúc.

### Exception Flows

- **E1 — Không còn đủ điều kiện**: Tại bước 4, phiên hết hiệu lực hoặc tài khoản đích không còn khả dụng. Hệ thống từ chối; yêu cầu đăng nhập lại nếu cần.
- **E2 — Không nhận được xác nhận**: Tại bước 4, hệ thống kiểm tra trạng thái chặn trước khi thử lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Có thể chặn cả người chưa kết bạn. Chỉ cần một bên chặn thì cả hai bên không được gửi tin riêng hoặc lời mời kết bạn mới.
  - **BR-02**: Người bị chặn không tìm thấy người chặn, kể cả khi nhập đúng tên đăng nhập.
  - **BR-03**: Giữ lịch sử chat và tư cách thành viên nhóm chung; các thành viên cùng nhóm vẫn thấy tin nhau.
  - **BR-04**: Tin được server chấp nhận trước khi chặn vẫn được đồng bộ. Tin gửi sau khi chặn bị từ chối và không tự gửi lại khi bỏ chặn.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Ghi chặn, hủy tình bạn và hủy lời mời phải hoàn tất cùng nhau, kể cả khi có yêu cầu đồng thời.
  - **NFR-02**: Server kiểm tra quan hệ chặn khi gửi tin, gửi lời mời và tìm kiếm.
  - **NFR-03**: Cập nhật quan hệ không làm mất bản nháp.
