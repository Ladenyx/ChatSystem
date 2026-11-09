# ĐẶC TẢ USE-CASE

> Chức năng đề xuất, chưa được chốt trong yêu cầu hiện tại.

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-RPT-02
- **Use Case Name**: Theo dõi và xem kết quả báo cáo
- **Author / Create by**: tkiet24
- **Date Create**: 05/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Chủ báo cáo
- **Secondary Actor(s)**: Không có.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Chủ báo cáo tra cứu trạng thái xử lý và kết quả được phép xem của hồ sơ đã gửi.
- **Goal**: Biết báo cáo đã được xử lý đến đâu và có kết luận hay chưa.
- **Trigger**: Người dùng yêu cầu xem báo cáo của mình.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ; chỉ truy cập các hồ sơ do mình gửi.
- **Post-conditions**: Trạng thái và kết quả mới nhất được phép truy cập được cung cấp.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** yêu cầu xem các báo cáo đã gửi.
2. **Hệ thống** xác thực và trả danh sách hồ sơ thuộc người dùng.
3. **Người dùng** chọn một báo cáo.
4. **Hệ thống** kiểm tra quyền, cung cấp mã hồ sơ, thời điểm tiếp nhận, trạng thái xử lý và kết luận nếu có.

### Alternative Flows

- **A1 — Không có báo cáo**: Tại bước 2, danh sách rỗng; kết thúc.
- **A2 — Chưa có kết luận**: Tại bước 4, cung cấp trạng thái hiện tại; người dùng có thể tra cứu lại sau.
- **A3 — Một phần dữ liệu không còn khả dụng**: Tại bước 4, chỉ cung cấp phần còn được lưu và được phép xem, nêu rõ phần thiếu.
- **A4 — Muốn yêu cầu rút**: Sau bước 4, người dùng thực hiện [UC-USR-RPT-03](UC-USR-RPT-03-request-report-withdrawal.md).

### Exception Flows

- **E1 — Hồ sơ không thuộc người dùng hoặc không còn khả dụng**: Tại bước 4, từ chối truy cập; không tiết lộ dữ liệu của người khác.
- **E2 — Lỗi tải**: Tại bước 2 hoặc 4, thông báo chưa tải được dữ liệu và cho thử lại; không coi dữ liệu cũ là kết quả mới.
- **E3 — Phiên hết hiệu lực**: Dừng truy cập và yêu cầu đăng nhập lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Chủ báo cáo chỉ xem hồ sơ của mình; người bị báo cáo không có quyền xem hồ sơ gốc chỉ vì là tài khoản đích.
  - **BR-02**: Tiếp nhận, kết luận kiểm duyệt, trạng thái yêu cầu rút và thời hạn lưu bằng chứng được xác định riêng. Hết hạn lưu không đồng nghĩa đã rút hoặc không vi phạm.
  - **BR-03**: Quyền xem trạng thái không tự cấp quyền đọc bản rõ bằng chứng. Tra cứu không thay đổi kết luận, chế tài hoặc gia hạn lưu dữ liệu.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Server kiểm tra quyền sở hữu ở mọi lần truy cập; không lộ dữ liệu nhạy cảm qua lỗi/log.
  - **NFR-02**: Phản hồi gắn đúng hồ sơ/phiên; không khôi phục dữ liệu đã xóa từ bộ nhớ đệm.
