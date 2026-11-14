# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-ANL-04
- **Use Case Name**: Xem số người dùng hoạt động theo tháng
- **Author / Create by**: ladenyx
- **Date Create**: 2026-11-14
- **Last Updated by / Date**: ladenyx / 2026-11-14
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin hệ thống xem biểu đồ số lượng người dùng duy nhất đã mở ứng dụng trong từng tháng của 12 tháng thuộc năm được chọn trên Dashboard Hoạt động Người dùng (`A-SCR-08` — `A-16`), và có thể chọn một tháng để lọc nhanh bảng hoạt động chi tiết theo tháng đó.
- **Goal**: Theo dõi biến động số lượng người dùng có mở ứng dụng theo từng tháng trong năm và chuyển nhanh sang bảng hoạt động chi tiết của tháng cần phân tích.
- **Trigger**: Admin mở Dashboard Hoạt động Người dùng (`A-SCR-08`), điều hướng từ trang Tổng quan ([UC-ADM-OVW-01](../overview/UC-ADM-OVW-01-view-overview.md)), hoặc chọn năm thống kê khác.

## Điều kiện (Conditions)

- **Pre-conditions**: Admin đã có phiên quản trị hợp lệ và quyền Admin hệ thống ([UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md)).
- **Post-conditions**: Biểu đồ 12 tháng phản ánh đúng số người dùng duy nhất (`distinct`) có mở ứng dụng trong từng tháng của năm đã chọn; nếu Admin chọn một tháng, khoảng ngày của tháng đó được áp dụng xuống bảng hoạt động chi tiết tại [UC-ADM-ANL-03](UC-ADM-ANL-03-browse-user-activity.md); dữ liệu hệ thống không bị thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** mở Dashboard Hoạt động Người dùng hoặc chọn `Năm` cần xem thống kê (mặc định là năm hiện tại theo `Asia/Ho_Chi_Minh`).
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống, tính hợp lệ của năm và đếm số lượng tài khoản duy nhất (`distinct Account ID`) có ít nhất một lượt mở ứng dụng trong từng tháng từ tháng 1 đến tháng 12 của năm đó.
3. **Hệ thống** cung cấp biểu đồ 12 tháng kèm số người dùng mở ứng dụng của từng tháng (tháng không có người mở ứng dụng hiển thị giá trị `0`).
4. **Admin** chọn một tháng cụ thể trên biểu đồ để kiểm tra chi tiết hoạt động trong tháng đó.
5. **Hệ thống** đặt khoảng thời gian từ ngày đầu tháng đến ngày cuối tháng đã chọn (theo `Asia/Ho_Chi_Minh`) và kích hoạt tra cứu bảng hoạt động chi tiết tại [UC-ADM-ANL-03](UC-ADM-ANL-03-browse-user-activity.md).

### Alternative Flows

**Alternative flow 1: Năm được chọn có tháng không phát sinh lượt mở ứng dụng**
1. Từ bước #2 của Basic Flow, truy vấn hoàn tất thành công nhưng một hoặc nhiều tháng không có người dùng nào mở ứng dụng.
2. **Hệ thống** vẫn trả về đủ 12 tháng với giá trị `0` tại các tháng trống.
3. Tiếp tục bước #3 trong Basic Flow.

**Alternative flow 2: Admin chỉ xem biểu đồ năm mà không chọn tháng**
1. Từ bước #3 của Basic Flow, **Admin** chỉ quan sát biểu đồ hoặc đổi sang năm khác mà không chọn tháng để lọc bảng dưới.
2. Kết thúc Use Case (hoặc quay lại bước #1 trong Basic Flow nếu **Admin** chọn năm khác).

**Alternative flow 3: Admin bỏ chọn tháng hoặc nhập khoảng ngày tùy chỉnh**
1. Từ bước #5 của Basic Flow, **Admin** bỏ chọn tháng trên biểu đồ hoặc chỉnh tay khoảng ngày ở bảng dưới.
2. **Hệ thống** gỡ chọn tháng trên biểu đồ và lọc bảng theo khoảng ngày tại [UC-ADM-ANL-03](UC-ADM-ANL-03-browse-user-activity.md).

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
1. Từ bước #2 của Basic Flow, truy vấn thống kê gặp sự cố.
2. **Hệ thống** thông báo lỗi tải biểu đồ, cho phép thử lại và không hiển thị lỗi truy vấn thành biểu đồ có các tháng bằng `0`.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Chỉ số mỗi tháng trên biểu đồ (`A-16`) là **số lượng người dùng duy nhất (`distinct Account ID`) có ít nhất 01 lượt mở ứng dụng** trong tháng đó; tuyệt đối không cộng dồn số người duy nhất của từng ngày để suy ra số người duy nhất của cả tháng ([Thống kê và thời gian](../../../../decisions/statistics-and-time.md)).
  - **BR-02**: Biểu đồ luôn trả về đủ 12 tháng của năm đã chọn theo múi giờ `Asia/Ho_Chi_Minh`; tháng không có dữ liệu có giá trị `0`. Thống kê được giữ nguyên sau khi tài khoản bị xóa mềm hoặc purge.
  - **BR-03**: Phân biệt chỉ số biểu đồ (`A-16`) và danh sách hoạt động (`A-15` — [UC-ADM-ANL-03](UC-ADM-ANL-03-browse-user-activity.md)): Biểu đồ `A-16` chỉ đếm người có sự kiện **mở ứng dụng** trong tháng, trong khi bảng `A-15` liệt kê người có **mở ứng dụng hoặc gửi tin nhắn**. Do đó, người mở ứng dụng ngày 31/10 rồi giữ phiên sang ngày 01/11 để gửi tin (nhưng chưa mở lại ứng dụng trong tháng 11) sẽ xuất hiện trong bảng `A-15` của tháng 11 (với `lượt mở ứng dụng = 0`), nhưng chưa được tính vào cột tháng 11 của biểu đồ `A-16` ([Thống kê và thời gian](../../../../decisions/statistics-and-time.md)).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Hệ thống phân biệt minh bạch giữa tháng có giá trị `0` thực tế và lỗi truy vấn dữ liệu.
  - **NFR-02**: Khi **Admin** đổi năm liên tiếp, phản hồi đến muộn của năm cũ không được ghi đè biểu đồ của năm vừa chọn.
