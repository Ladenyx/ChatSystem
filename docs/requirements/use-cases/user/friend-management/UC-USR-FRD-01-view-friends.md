# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-FRD-01
- **Use Case Name**: Xem danh sách bạn bè
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Không có.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng tra cứu bạn bè theo tên và trạng thái online/offline.
- **Goal**: Tìm được người bạn cần liên hệ hoặc biết ai đang online.
- **Trigger**: Người dùng yêu cầu xem danh sách bạn bè.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ.
- **Post-conditions**: Danh sách bạn bè phù hợp với điều kiện lọc được cung cấp.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** yêu cầu xem bạn bè của mình.
2. **Hệ thống** trả danh sách bạn bè hiện tại cùng trạng thái online/offline.
3. **Người dùng** nhập tên cần tìm và/hoặc giới hạn danh sách ở những người đang online.
4. **Hệ thống** trả danh sách phù hợp.

### Alternative Flows

- **A1 — Xem toàn bộ bạn bè**: Sau bước 2, người dùng không cần lọc thêm; kết thúc use case.
- **A2 — Không có kết quả**: Tại bước 2 hoặc 4, danh sách rỗng. Người dùng có thể đổi điều kiện ở bước 3 hoặc kết thúc.

### Exception Flows

- **E1 — Không tải được danh sách**: Tại bước 2 hoặc 4, hệ thống thông báo lỗi và cho phép thử lại với điều kiện hiện tại.
- **E2 — Phiên hết hiệu lực**: Hệ thống dừng truy cập và yêu cầu đăng nhập lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Chỉ lấy các quan hệ bạn bè còn hiệu lực của người dùng.
  - **BR-02**: Lọc theo tên áp dụng cho cả toàn bộ bạn bè và bạn bè đang online.
  - **BR-03**: Khi quan hệ hoặc trạng thái online thay đổi, danh sách được cập nhật theo điều kiện lọc hiện tại.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Chỉ cho phép truy cập danh sách bạn bè của chính tài khoản đang đăng nhập.
  - **NFR-02**: Phản hồi truy vấn cũ không ghi đè kết quả lọc mới; trạng thái chưa xác định không được coi là offline.
