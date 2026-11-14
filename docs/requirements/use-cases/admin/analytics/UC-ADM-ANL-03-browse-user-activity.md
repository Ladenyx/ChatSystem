# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-ANL-03
- **Use Case Name**: Tra cứu hoạt động của người dùng theo khoảng thời gian
- **Author / Create by**: ladenyx
- **Date Create**: 2026-11-14
- **Last Updated by / Date**: ladenyx / 2026-11-14
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin hệ thống tra cứu danh sách người dùng có hoạt động trong khoảng thời gian đã chọn trên Dashboard Hoạt động Người dùng (`A-SCR-08` — `A-15`) kèm ba chỉ số (*lượt mở ứng dụng*, *số người đã chat*, *số nhóm đã chat*), kết hợp lọc theo họ tên, lọc so sánh (`=`, `<`, `>`) theo giá trị của một chỉ số và sắp xếp theo họ tên hoặc ngày tạo tài khoản.
- **Goal**: Đánh giá mức độ tương tác chi tiết của từng người dùng hoạt động trong khoảng ngày quan tâm và lọc ra nhóm người dùng theo ngưỡng hoạt động cụ thể.
- **Trigger**: Admin mở Dashboard Hoạt động Người dùng (`A-SCR-08`), thay đổi khoảng thời gian/bộ lọc/sắp xếp, hoặc chọn một tháng từ biểu đồ hoạt động theo tháng ([UC-ADM-ANL-04](UC-ADM-ANL-04-view-monthly-active-users-chart.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin đã có phiên quản trị hợp lệ và quyền Admin hệ thống ([UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md)).
- **Post-conditions**: Bảng chi tiết các người dùng có hoạt động trong khoảng ngày hợp lệ cùng 3 chỉ số, khớp bộ lọc họ tên, bộ lọc ngưỡng chỉ số và thứ tự sắp xếp được cung cấp; không làm thay đổi dữ liệu hoạt động hay hồ sơ người dùng.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** mở Dashboard Hoạt động Người dùng hoặc chọn khoảng thời gian (`Từ ngày – Đến ngày`) cần tra cứu.
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống, tính hợp lệ của khoảng ngày và tổng hợp danh sách những người dùng có phát sinh hoạt động trong khoảng thời gian đó kèm 3 chỉ số: `lượt mở ứng dụng`, `số người đã chat` và `số nhóm đã chat`.
3. **Admin** nhập từ khóa `họ tên` và/hoặc thiết lập bộ lọc chỉ số hoạt động (chọn 01 trong 03 chỉ số, chọn toán tử so sánh `=`, `<` hoặc `>` và nhập giá trị ngưỡng số nguyên) cùng tiêu chí sắp xếp (`họ tên` hoặc `ngày tạo tài khoản`, tăng/giảm).
4. **Hệ thống** kiểm tra tính hợp lệ của điều kiện lọc, áp dụng đồng thời bộ lọc `họ tên`, bộ lọc so sánh chỉ số trên tập người dùng có hoạt động trong khoảng ngày, sắp xếp theo tiêu chí đã chọn và cung cấp bảng kết quả.
5. **Admin** rà soát bảng chi tiết hoạt động của từng người dùng.

### Alternative Flows

**Alternative flow 1: Lọc nhanh khoảng ngày theo tháng được chọn từ biểu đồ hoạt động năm**
1. Từ bước #1 của Basic Flow, **Admin** chọn một tháng trên biểu đồ tại [UC-ADM-ANL-04](UC-ADM-ANL-04-view-monthly-active-users-chart.md).
2. **Hệ thống** tự động cập nhật khoảng ngày từ đầu tháng đến cuối tháng đó (theo `Asia/Ho_Chi_Minh`).
3. Tiếp tục bước #2 trong Basic Flow.

**Alternative flow 2: Không sử dụng hoặc gỡ bỏ bộ lọc họ tên / bộ lọc chỉ số**
1. Từ bước #3 của Basic Flow, **Admin** để trống hoặc xóa từ khóa `họ tên` và/hoặc xóa điều kiện lọc ngưỡng chỉ số.
2. **Hệ thống** gỡ ràng buộc tương ứng, giữ khoảng thời gian cùng thứ tự sắp xếp hiện hành.
3. Tiếp tục bước #4 trong Basic Flow.

**Alternative flow 3: Không có người dùng hoạt động nào thỏa mãn điều kiện**
1. Từ bước #2 hoặc #4 của Basic Flow, truy vấn hoàn tất nhưng không có người dùng nào phát sinh hoạt động trong khoảng ngày hoặc không có bản ghi nào khớp tổ hợp lọc.
2. **Hệ thống** thông báo không có dữ liệu phù hợp và giữ nguyên tiêu chí để **Admin** điều chỉnh.
3. Quay lại bước #1 hoặc #3 trong Basic Flow, hoặc kết thúc Use Case.

**Alternative flow 4: Hoạt động thuộc tài khoản đã bị xóa vĩnh viễn (purge)**
1. Từ bước #2 hoặc #4 của Basic Flow, người dùng có phát sinh hoạt động trong khoảng ngày tra cứu nhưng tài khoản đã bị purge sau đó.
2. **Hệ thống** giữ nguyên số liệu hoạt động lịch sử dưới nhãn định danh đã ẩn danh/đã xóa theo [DEC-07](../../../../decisions/account-lifecycle.md) và [Thống kê và thời gian](../../../../decisions/statistics-and-time.md).
3. Tiếp tục bước #5 trong Basic Flow.

### Exception Flows

**Exception flow 1: Khoảng thời gian hoặc giá trị lọc chỉ số không hợp lệ**
1. Từ bước #2 hoặc #4 của Basic Flow, ngày bắt đầu lớn hơn ngày kết thúc, hoặc giá trị ngưỡng so sánh không phải là số nguyên không âm hợp lệ.
2. **Hệ thống** từ chối truy vấn, thông báo lỗi điều kiện lọc và yêu cầu **Admin** điều chỉnh.
3. Quay lại bước #1 hoặc #3 trong Basic Flow.

**Exception flow 2: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #2 hoặc #4 của Basic Flow, phiên quản trị không còn hợp lệ hoặc không có quyền Admin hệ thống.
2. **Hệ thống** từ chối truy vấn và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 3: Lỗi kết nối hoặc lỗi truy vấn dữ liệu**
1. Từ bước #2 hoặc #4 của Basic Flow, quá trình tổng hợp dữ liệu hoạt động thất bại.
2. **Hệ thống** thông báo lỗi truy xuất, giữ lại tiêu chí lọc hiện có để thử lại và không hiển thị lỗi như trạng thái không có người dùng hoạt động.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: **Điều kiện xuất hiện trong danh sách hoạt động**: Bảng chỉ bao gồm những người dùng có ít nhất một lượt mở ứng dụng được xác thực thành công **hoặc** ít nhất một tin nhắn gửi đi được máy chủ chấp nhận trong khoảng thời gian đã chọn ([Thống kê và thời gian](../../../../decisions/statistics-and-time.md)); người dùng không có bất kỳ hoạt động nào trong khoảng ngày (cả 3 chỉ số đều bằng `0` và không mở app/gửi tin) không nằm trong danh sách này.
  - **BR-02**: **Cách tính 03 chỉ số hoạt động** theo [Thống kê và thời gian](../../../../decisions/statistics-and-time.md):
    - *Lượt mở ứng dụng*: tính 01 lượt cho mỗi lần khởi chạy User Client có xác thực tài khoản thành công; tự kết nối lại (`reconnect`) hoặc đăng nhập lại trong cùng một lần chạy ứng dụng không làm tăng số lượt.
    - *Số người đã chat*: số lượng người nhận cá nhân duy nhất (`distinct`) mà người dùng đã gửi tin nhắn 1-1 được máy chủ chấp nhận trong khoảng thời gian; việc chỉ nhận tin nhắn không làm tăng chỉ số này.
    - *Số nhóm đã chat*: số lượng nhóm chat duy nhất (`distinct`) mà người dùng đã gửi tin nhắn được máy chủ chấp nhận trong khoảng thời gian; việc chỉ nhận tin nhắn nhóm không làm tăng chỉ số này.
    - Việc xóa lịch sử phía mình hoặc thu hồi tin nhắn sau đó không đảo ngược sự kiện gửi đã được máy chủ chấp nhận. Người mở ứng dụng vào cuối ngày/tháng trước (ví dụ 31/10) rồi giữ phiên sang ngày 01/11 để gửi tin nhắn được tính là có hoạt động trong ngày 01/11 (với `lượt mở ứng dụng = 0` nếu tra cứu từ 01/11).
  - **BR-03**: **Quy tắc lọc và sắp xếp**:
    - Lọc `họ tên`: đối sánh chuỗi con không phân biệt hoa thường sau khi cắt khoảng trắng thừa hai đầu (`trim`).
    - Lọc theo chỉ số: chọn 01 chỉ số (*Lượt mở ứng dụng*, *Số người đã chat* hoặc *Số nhóm đã chat*) kết hợp toán tử `=`, `<`, `>` trên tập người dùng đã thỏa điều kiện hoạt động tại **BR-01**.
    - Sắp xếp: hỗ trợ sắp xếp theo `họ tên` hoặc `ngày tạo tài khoản` (tăng/giảm, dùng `Account ID` làm khóa phụ cố định khi trùng giá trị).
  - **BR-04**: Mọi mốc thời gian lọc và hiển thị đều tính theo múi giờ `Asia/Ho_Chi_Minh` (ngày kết thúc lấy đến trước `00:00` ngày kế tiếp). Dữ liệu thống kê được giữ lại sau khi xóa tài khoản.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Quá trình thống kê hoạt động chỉ dựa trên siêu dữ liệu sự kiện (lượt mở app, đích gửi tin nhắn), tuyệt đối không đọc hay giải mã nội dung tin nhắn cá nhân hoặc nhóm E2EE.
  - **NFR-02**: Khi **Admin** thay đổi điều kiện lọc/sắp xếp liên tiếp, phản hồi đến muộn của truy vấn cũ không được ghi đè kết quả mới nhất.
