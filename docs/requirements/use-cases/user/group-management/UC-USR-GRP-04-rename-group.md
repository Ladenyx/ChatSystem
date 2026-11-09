# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-GRP-04
- **Use Case Name**: Đổi tên nhóm
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Admin nhóm
- **Secondary Actor(s)**: Các thành viên nhóm.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin cập nhật tên của nhóm đang quản trị.
- **Goal**: Nhóm sử dụng tên mới hợp lệ.
- **Trigger**: Admin yêu cầu đổi tên nhóm.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và là Admin của nhóm đang hoạt động.
- **Post-conditions**: Tên mới được ghi nhận và đồng bộ cho các thành viên.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** nhập tên mới và xác nhận đổi tên.
2. **Hệ thống** kiểm tra quyền Admin và tính hợp lệ của tên.
3. **Hệ thống** lưu tên mới và thông báo kết quả cho các thành viên.

### Alternative Flows

- **A1 — Tên không thay đổi**: Tại bước 2, tên đề nghị giống tên hiện tại; kết thúc và giữ nguyên tên.

### Exception Flows

- **E1 — Tên không hợp lệ**: Tại bước 2, tên rỗng, chỉ có khoảng trắng hoặc vượt giới hạn độ dài. Hệ thống yêu cầu sửa tại bước 1.
- **E2 — Mất quyền hoặc nhóm không hoạt động**: Tại bước 2, hệ thống từ chối; yêu cầu đăng nhập lại nếu phiên hết hiệu lực.
- **E3 — Lỗi ghi nhận hoặc mất xác nhận**: Tại bước 3, kiểm tra tên và kết quả yêu cầu trước khi thử lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Chỉ Admin nhóm được đổi tên; các Admin có quyền ngang nhau.
  - **BR-02**: Tên phải tuân cùng quy tắc và giới hạn độ dài như khi tạo nhóm.
  - **BR-03**: Đổi tên giữ nguyên định danh nhóm, thành viên và lịch sử.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Server kiểm tra quyền tại thời điểm ghi nhận và đồng bộ tên đã lưu cho các thành viên.
  - **NFR-02**: Phản hồi trả muộn chỉ cập nhật đúng nhóm; giữ tên đang đề nghị nếu yêu cầu thất bại.
