# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-FRD-07
- **Use Case Name**: Bỏ chặn người dùng
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng đã chặn
- **Secondary Actor(s)**: Người được bỏ chặn.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng gỡ quan hệ chặn do mình tạo.
- **Goal**: Bỏ hạn chế do lần chặn của mình đối với tài khoản được chọn.
- **Trigger**: Người dùng yêu cầu bỏ chặn một tài khoản.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và đã chặn tài khoản đích.
- **Post-conditions**: Gỡ chiều chặn của người dùng; chiều chặn của đối phương, nếu có, vẫn còn hiệu lực.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** chọn tài khoản từ danh sách mình đã chặn và yêu cầu bỏ chặn.
2. **Hệ thống** kiểm tra quyền và gỡ chiều chặn do người dùng tạo.
3. **Hệ thống** cập nhật quyền tương tác theo quan hệ còn lại và thông báo hoàn tất.

### Alternative Flows

- **A1 — Đã bỏ chặn**: Tại bước 2, chiều chặn đã được gỡ. Tiếp tục bước 3 với trạng thái hiện tại.
- **A2 — Đối phương vẫn chặn**: Tại bước 3, bỏ chặn hoàn tất nhưng tin riêng và lời mời mới vẫn bị cấm ở cả hai chiều.

### Exception Flows

- **E1 — Sai quyền hoặc phiên hết hiệu lực**: Tại bước 2, hệ thống từ chối; yêu cầu đăng nhập lại nếu phiên hết hiệu lực.
- **E2 — Tài khoản đích không còn khả dụng**: Tại bước 2, hệ thống thông báo không thể xử lý.
- **E3 — Không nhận được xác nhận**: Tại bước 2–3, hệ thống kiểm tra trạng thái chặn trước khi thử lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Mỗi người chỉ được gỡ chiều chặn do mình tạo.
  - **BR-02**: Bỏ chặn không khôi phục tình bạn hoặc lời mời cũ; muốn kết bạn lại phải gửi lời mời mới.
  - **BR-03**: Chat riêng và lời mời mới chỉ hoạt động lại khi không còn bên nào chặn và tài khoản đủ điều kiện tương tác.
  - **BR-04**: Nếu đối phương vẫn chặn, người dùng vẫn không tìm thấy đối phương theo [quy tắc tìm người dùng](UC-USR-FRD-02-search-users.md).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Server kiểm tra chủ sở hữu chiều chặn; xử lý lặp không ảnh hưởng chiều chặn của đối phương.
  - **NFR-02**: Chỉ cập nhật quyền gửi sau khi có xác nhận; giữ bản nháp và không tự gửi lại tin đã bị từ chối.
