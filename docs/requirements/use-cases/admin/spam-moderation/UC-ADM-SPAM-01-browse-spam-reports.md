# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-SPAM-01
- **Use Case Name**: Tra cứu danh sách báo cáo spam
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-30
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin tra cứu các báo cáo spam trong hệ thống thông qua tiêu chí lọc theo khoảng thời gian, tên đăng nhập của tài khoản bị báo cáo và tiêu chí sắp xếp để xác định báo cáo cần kiểm duyệt.
- **Goal**: Tìm được đúng báo cáo spam cần xem xét bằng chứng mà chưa làm thay đổi trạng thái báo cáo hay tài khoản liên quan.
- **Trigger**: Admin mở chức năng Kiểm duyệt báo cáo spam hoặc điều hướng sang từ chỉ số báo cáo spam trên trang Tổng quan.

## Điều kiện (Conditions)

- **Pre-conditions**: Admin đã có phiên quản trị hợp lệ và quyền Admin hệ thống ([UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md)).
- **Post-conditions**: Danh sách báo cáo spam khớp với điều kiện lọc và thứ tự sắp xếp được cung cấp; trạng thái báo cáo và trạng thái tài khoản người dùng không bị thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** mở chức năng tra cứu báo cáo spam.
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống và trả về danh sách các báo cáo spam hiện có theo thứ tự mặc định.
3. **Admin** nhập điều kiện lọc (khoảng thời gian gửi báo cáo và/hoặc `username` của người bị báo cáo) và chọn tiêu chí sắp xếp (theo thời gian hoặc theo `username`).
4. **Hệ thống** kiểm tra tính hợp lệ của khoảng thời gian, truy vấn và trả về danh sách báo cáo thỏa mãn tổ hợp lọc và sắp xếp đã chọn.
5. **Admin** chọn một báo cáo cụ thể trong danh sách kết quả để kiểm tra chi tiết.
6. **Hệ thống** chuyển ngữ cảnh định danh báo cáo sang [UC-ADM-SPAM-02](UC-ADM-SPAM-02-view-spam-report.md) để tải chi tiết và bằng chứng.

### Alternative Flows

**Alternative flow 1: Bỏ bớt tiêu chí lọc hoặc giữ nguyên thứ tự sắp xếp**
1. Từ bước #3 của Basic Flow, **Admin** xóa một điều kiện lọc hoặc không thay đổi tiêu chí sắp xếp.
2. **Hệ thống** gỡ ràng buộc của bộ lọc vừa xóa, giữ nguyên các điều kiện còn lại và tiếp tục bước #4 trong Basic Flow.

**Alternative flow 2: Không có báo cáo spam thỏa mãn điều kiện**
1. Từ bước #2 hoặc #4 của Basic Flow, truy vấn hoàn tất nhưng không có báo cáo nào khớp tiêu chí.
2. **Hệ thống** thông báo không tìm thấy báo cáo phù hợp và giữ nguyên tiêu chí lọc hiện tại.
3. Quay lại bước #3 trong Basic Flow hoặc kết thúc Use Case.

**Alternative flow 3: Admin chỉ tra cứu danh sách mà không mở chi tiết báo cáo**
1. Từ bước #2 hoặc #4 của Basic Flow, **Admin** hoàn tất việc rà soát danh sách mà không chọn báo cáo cụ thể.
2. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Khoảng thời gian lọc không hợp lệ**
1. Từ bước #4 của Basic Flow, ngày bắt đầu lớn hơn ngày kết thúc hoặc giá trị ngày không hợp lệ.
2. **Hệ thống** từ chối thực thi truy vấn với khoảng ngày sai và thông báo lỗi điều kiện lọc.
3. Quay lại bước #3 trong Basic Flow.

**Exception flow 2: Lỗi kết nối hoặc lỗi truy vấn dữ liệu**
1. Từ bước #2 hoặc #4 của Basic Flow, máy chủ không phản hồi hoặc truy vấn thất bại.
2. **Hệ thống** thông báo lỗi truy xuất dữ liệu, giữ lại tiêu chí lọc hiện có để thử lại và không trình bày lỗi như danh sách rỗng.
3. Kết thúc Use Case.

**Exception flow 3: Phiên quản trị hết hiệu lực hoặc thiếu quyền**
1. Từ bước #2, #4 hoặc #6 của Basic Flow, phiên quản trị không còn hợp lệ hoặc không đủ quyền Admin hệ thống.
2. **Hệ thống** từ chối yêu cầu và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 4: Báo cáo được chọn không còn khả dụng**
1. Từ bước #6 của Basic Flow, báo cáo vừa chọn không còn tồn tại tại thời điểm mở chi tiết.
2. **Hệ thống** thông báo báo cáo không còn khả dụng, làm mới lại danh sách và không mở báo cáo khác thay thế.
3. Quay lại bước #4 trong Basic Flow.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Tiêu chí lọc và sắp xếp theo `username` áp dụng trên `username` của **tài khoản bị báo cáo**, không phải người gửi báo cáo.
  - **BR-02**: Khoảng thời gian lọc được tính theo múi giờ `Asia/Ho_Chi_Minh` (ngày kết thúc tính đến trước `00:00` của ngày kế tiếp theo [Thống kê và thời gian](../../../../decisions/statistics-and-time.md)).
  - **BR-03**: Nghiệp vụ tra cứu danh sách chỉ đọc dữ liệu; không tự động khóa tài khoản bị báo cáo và không tự đánh dấu báo cáo đã xử lý.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Mọi yêu cầu tra cứu báo cáo spam đều phải được kiểm tra quyền Admin hệ thống trên máy chủ trước khi trả dữ liệu.
  - **NFR-02**: Khi **Admin** thay đổi bộ lọc liên tiếp, kết quả trả về muộn của truy vấn cũ không được ghi đè kết quả của truy vấn mới nhất.
