# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-ANL-02
- **Use Case Name**: Xem số lượt đăng ký mới theo tháng
- **Author / Create by**: ladenyx
- **Date Create**: 2026-11-14
- **Last Updated by / Date**: ladenyx / 2026-11-14
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin hệ thống xem biểu đồ thống kê số lượt tài khoản đăng ký mới trong 12 tháng của năm được chọn trên Dashboard Đăng ký mới (`A-SCR-07` — `A-13`) và có thể chọn một tháng trên biểu đồ để lọc nhanh bảng danh sách chi tiết theo tháng đó.
- **Goal**: Quan sát xu hướng tăng trưởng người dùng mới theo từng tháng trong năm và chuyển nhanh sang danh sách tài khoản đăng ký của một tháng cụ thể khi cần đối chiếu.
- **Trigger**: Admin mở Dashboard Đăng ký mới (`A-SCR-07`), điều hướng từ trang Tổng quan ([UC-ADM-OVW-01](../overview/UC-ADM-OVW-01-view-overview.md)), hoặc chọn năm thống kê khác.

## Điều kiện (Conditions)

- **Pre-conditions**: Admin đã có phiên quản trị hợp lệ và quyền Admin hệ thống ([UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md)).
- **Post-conditions**: Số liệu đăng ký mới của đủ 12 tháng trong năm đã chọn được biểu diễn chính xác; nếu Admin chọn một tháng, khoảng ngày của tháng đó được áp dụng cho bảng chi tiết tại [UC-ADM-ANL-01](UC-ADM-ANL-01-browse-new-registrations.md); không làm thay đổi dữ liệu tài khoản.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** mở Dashboard Đăng ký mới hoặc chọn một `Năm` cần xem thống kê (mặc định là năm hiện tại theo `Asia/Ho_Chi_Minh`).
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống, tính hợp lệ của năm được chọn và tổng hợp số lượng tài khoản được tạo mới theo từng tháng từ tháng 1 đến tháng 12 của năm đó.
3. **Hệ thống** cung cấp biểu đồ 12 tháng kèm số liệu tổng hợp của từng tháng (các tháng không phát sinh đăng ký mới được điền giá trị `0`).
4. **Admin** chọn một tháng cụ thể trên biểu đồ để xem danh sách tài khoản đăng ký trong tháng đó.
5. **Hệ thống** xác định khoảng ngày từ ngày đầu tháng đến ngày cuối tháng đã chọn (theo `Asia/Ho_Chi_Minh`) và kích hoạt lọc bảng danh sách chi tiết tại [UC-ADM-ANL-01](UC-ADM-ANL-01-browse-new-registrations.md).

### Alternative Flows

**Alternative flow 1: Năm được chọn chưa có hoặc có những tháng không phát sinh lượt đăng ký mới**
1. Từ bước #2 của Basic Flow, truy vấn hoàn tất thành công nhưng một số hoặc cả 12 tháng không có tài khoản mới nào.
2. **Hệ thống** vẫn trả về đầy đủ 12 mốc tháng với giá trị `0` cho các tháng trống.
3. Tiếp tục bước #3 trong Basic Flow.

**Alternative flow 2: Admin chỉ theo dõi biểu đồ năm mà không chọn tháng**
1. Từ bước #3 của Basic Flow, **Admin** chỉ quan sát biểu đồ hoặc đổi sang năm khác mà không chọn tháng để lọc bảng dưới.
2. Kết thúc Use Case (hoặc quay lại bước #1 trong Basic Flow nếu **Admin** chọn năm khác).

**Alternative flow 3: Admin bỏ chọn tháng đang lọc trên biểu đồ**
1. Từ bước #5 của Basic Flow, **Admin** bỏ chọn tháng trên biểu đồ hoặc chủ động nhập khoảng ngày tùy chỉnh ở bảng chi tiết.
2. **Hệ thống** gỡ trạng thái lọc theo tháng đang chọn và thực hiện tra cứu theo khoảng ngày tại [UC-ADM-ANL-01](UC-ADM-ANL-01-browse-new-registrations.md).

### Exception Flows

**Exception flow 1: Năm được chọn không hợp lệ**
1. Từ bước #2 của Basic Flow, giá trị năm nằm ngoài phạm vi hợp lệ.
2. **Hệ thống** từ chối truy vấn, thông báo năm không hợp lệ và giữ nguyên kết quả hợp lệ trước đó.
3. Quay lại bước #1 trong Basic Flow.

**Exception flow 2: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #2 hoặc #5 của Basic Flow, phiên quản trị không còn hợp lệ hoặc không có quyền Admin hệ thống.
2. **Hệ thống** từ chối truy vấn và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 3: Lỗi kết nối hoặc lỗi truy xuất dữ liệu biểu đồ**
1. Từ bước #2 của Basic Flow, truy vấn thống kê tháng gặp sự cố.
2. **Hệ thống** thông báo lỗi tải biểu đồ, cho phép thử lại và tuyệt đối không hiển thị lỗi truy vấn thành biểu đồ có 12 tháng đều bằng `0`.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Biểu đồ luôn biểu diễn đầy đủ 12 tháng (tháng 1 đến tháng 12) của năm đã chọn; tháng không có lượt đăng ký mới phải có giá trị `0` ([Thống kê và thời gian](../../../../decisions/statistics-and-time.md)).
  - **BR-02**: Ranh giới từng tháng và năm được tính theo múi giờ `Asia/Ho_Chi_Minh` (từ `00:00:00` ngày 01 của tháng đến trước `00:00:00` ngày 01 của tháng kế tiếp).
  - **BR-03**: Tiêu chí tính một lượt đăng ký mới trên biểu đồ (`A-13`) đồng nhất hoàn toàn với bảng chi tiết (`A-12` — [UC-ADM-ANL-01](UC-ADM-ANL-01-browse-new-registrations.md)): tính mọi tài khoản tạo thành công (tự đăng ký đã xác minh email và Admin tạo), không tính yêu cầu đăng ký tạm, và giữ nguyên số liệu thống kê kể cả khi tài khoản sau đó bị xóa mềm hoặc purge.
  - **BR-04**: Khi chọn một tháng trên biểu đồ năm `Y`, tổng số bản ghi của bảng chi tiết ([UC-ADM-ANL-01](UC-ADM-ANL-01-browse-new-registrations.md)) trong tháng đó khi chưa nhập bộ lọc `họ tên` phải khớp nhất quán với con số hiển thị trên cột tháng tương ứng.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Hệ thống phải phân biệt rõ giữa năm/tháng có số lượt đăng ký bằng `0` thực tế với trạng thái truy vấn bị lỗi.
  - **NFR-02**: Khi **Admin** chuyển đổi nhanh giữa các năm hoặc các tháng, phản hồi đến muộn của lựa chọn cũ không được ghi đè biểu đồ hoặc khoảng ngày của lựa chọn mới nhất.
