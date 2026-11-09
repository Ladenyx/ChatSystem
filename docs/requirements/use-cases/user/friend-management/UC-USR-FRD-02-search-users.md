# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-FRD-02
- **Use Case Name**: Tìm người dùng
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Không có.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng tìm tài khoản theo tên đăng nhập hoặc họ tên.
- **Goal**: Xác định tài khoản muốn liên hệ.
- **Trigger**: Người dùng gửi yêu cầu tìm kiếm.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ.
- **Post-conditions**: Các tài khoản phù hợp và được phép xuất hiện trong kết quả được cung cấp.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** cung cấp tên đăng nhập hoặc họ tên cần tìm.
2. **Hệ thống** kiểm tra từ khóa và tìm các tài khoản phù hợp, áp dụng quy tắc ẩn theo quan hệ chặn.
3. **Hệ thống** trả kết quả cùng tên đăng nhập, họ tên và quan hệ hiện tại với người dùng.

### Alternative Flows

- **A1 — Không có kết quả**: Tại bước 3, hệ thống thông báo không tìm thấy tài khoản phù hợp. Người dùng có thể đổi từ khóa ở bước 1 hoặc kết thúc.

### Exception Flows

- **E1 — Từ khóa rỗng**: Tại bước 2, hệ thống yêu cầu nhập từ khóa; quay lại bước 1.
- **E2 — Tìm kiếm thất bại**: Tại bước 2, hệ thống thông báo lỗi và giữ từ khóa để thử lại.
- **E3 — Phiên hết hiệu lực**: Hệ thống dừng tìm kiếm và yêu cầu đăng nhập lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Tìm theo tên đăng nhập hoặc họ tên với từ khóa không rỗng.
  - **BR-02**: Ẩn tài khoản đang chặn người tìm, kể cả khi nhập đúng tên đăng nhập; không tiết lộ lý do bị ẩn.
  - **BR-03**: Người chưa kết bạn vẫn có thể tìm nhau và chat riêng; mời vào nhóm cần quan hệ bạn bè theo [DEC-03 (đề xuất)](../../../../decisions/blocking-and-friendship.md).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Server kiểm tra quyền xuất hiện trong kết quả theo quan hệ chặn hiện tại.
  - **NFR-02**: Phản hồi của từ khóa cũ không ghi đè kết quả tìm kiếm mới.
