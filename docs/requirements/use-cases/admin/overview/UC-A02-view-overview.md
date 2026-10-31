# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-A02
- **Use Case Name**: Xem Dashboard Tổng quan
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-31
- **Last Updated by / Date**: ladenyx / 2026-10-31
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: Không có tác nhân ngoài hệ thống
- **Priority**: Must have
- **Status**: Draft

## Mô tả tổng quan (Summary / Desciption)

- **Brief Description**: Hiển thị tình hình hệ thống bằng bốn thể KPI, biểu đồ xu hướng năm hiện tại và các danh sách hoạt động gần đây.
- **Goal**: Admin nắm tổng quan và mở nhanh màn hình quản trị liên quan từ các thẻ KPI.
- **Trigger**: Admin đăng nhập thành công hoặc chọn Dashboard trong Sidebar.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**: Actor có phiên hợp lệ và quyền Admin hệ thống; đã hoàn tất đăng nhập.
- **Post-conditions**: Các phần dữ liệu tải thành công được hiển thị với phạm vị thời gian rõ ràng; Admin có thể điều hướng tới màn hình liên quan. Không thay đổi dữ liệu nghiệp vụ.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Admin mở A-SCR-02 sau đăng nhập hoặc từ Sidebar | Hiển thị bố cục Dashboard và trạng thái đang tải |
| 2 | Chờ dữ liệu tổng quan | Server kiểm tra quyền sau đó trả dữ liệu cho bốn thẻ (Tổng người dùng, Đang Online, Tổng nhóm chat, Báo cáo Spam chờ xửa lý)|
| 3 | Xem khối xu hướng và hoạt động gần đây | Hiển thị biểu đồ xu hướng nhanh trong năm hiện tại, danh sách rút gọn báo cáo spam mới nhất và lượt đang nhập gần đây theo dữ liệu khả dụng |
| 4 | Chọn một thẻ KPI | Mở màn hình tương ứng: người dùng/Online tại A-SCR-03, nhóm tại A-SCR-04, báo cáo spam tại A-SCR-05; nếu mang theo bộ lọc thì bộ lọc phải được hiển thị rõ ở màn hình đích |

### Alternative Flows

- Alt 3a - Tại bước 3, một danh sách không có bản ghi: hiển thị trạng thái rỗng tương ứng. Chỉ hiện thị KPI bằng 0 khi truy vấn thành công và kết quả xác nhận bằng 0.
- Alt 4a - Tại bước 4, Admin chưa chọn thẻ KPI: tiếp tục xem Dashboard, không bắt buộc chuyển màn hình.
- Alt 4b - Tại bước 4, màn hình đích chưa hỗ trợ bộ lọc tương ứng (Ví dụ Online): vẫn mở màn hình quản trị liên quan; không tuyên bố danh sách đã lọc.<!--  -->

### Exception Flows

- Ex 2a/3a - Tại bước 2 hoặc bước 3, một nguồn dữ liệu lỗi: hiển thị lỗi ngay ở khối bị ảnh hưởng; các khối đã tải thành công vẫn hiện đúng trạng thái. Không thay lỗi bằng số 0.
- Ex 2b/4c - Tại bước 2 hoặc 4, phiên hết hiệu lực hoặc thiếu quyền: dừng truy cập và yêu cầu đăng nhập lại (chuyển đến màn hình A-SCR-01).
- Ex 4d - Tại bước 4, màn hình đích không tải được: thông báo lỗi điều hướng/tải dữ liệu; không hiển thị kết quả của màn hình khác dưới nhãn đích đã chọn

## Business Rules & Non-Functional Requirements
d.
    - NFR-03
- **Business Rules (BR)**:
    - BR-01: Dashboard gồm đúng 4 loại KPI và các khối tóm tắt; không phát sinh nghiệp vụ xử lý spam chỉ từ thẻ KPI.
    - BR-02: Thời gian hiển thị và năm hiện theo tính theo Asia/Ho_Chi_Minh.
    - BR-03: Đây là use case xem và điều hướng; việc xem danh sách đầy đủ/chi tiết tuân theo use case của màn hình đích.
- **Non-Functional Requirements (NFR)**: 
    - NFR-01: Trạng thái đang tải, rỗng và lỗi phải được phân biệt.
    - NFR-02: Chỉ tải dữ liệu sau kiểm tra quyền Admin; không hiện thị nội dung tin nhắn chat riêng hoặc giải mã E2EE trong Dashboard.

