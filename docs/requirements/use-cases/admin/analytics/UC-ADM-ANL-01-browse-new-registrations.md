# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-ANL-01
- **Use Case Name**: Tra cứu người dùng đăng ký mới theo khoảng thời gian
- **Author / Create by**: ladenyx
- **Date Create**: 2026-11-14
- **Last Updated by / Date**: ladenyx / 2026-11-14
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin hệ thống tra cứu danh sách các tài khoản người dùng được tạo mới trong khoảng thời gian đã chọn trên Dashboard Đăng ký mới (`A-SCR-07` — `A-12`), kết hợp lọc theo họ tên và sắp xếp theo họ tên hoặc ngày tạo.
- **Goal**: Nắm được danh sách chi tiết các tài khoản đăng ký mới trong khoảng ngày quan tâm theo đúng tiêu chí lọc và thứ tự sắp xếp.
- **Trigger**: Admin mở chức năng Dashboard Đăng ký mới (`A-SCR-07`), thay đổi khoảng thời gian/điều kiện lọc/sắp xếp, hoặc chọn một tháng từ biểu đồ đăng ký mới theo tháng ([UC-ADM-ANL-02](UC-ADM-ANL-02-view-monthly-registrations-chart.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin đã có phiên quản trị hợp lệ và quyền Admin hệ thống ([UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md)).
- **Post-conditions**: Danh sách tài khoản đăng ký mới trong khoảng thời gian hợp lệ, khớp điều kiện lọc họ tên và thứ tự sắp xếp được cung cấp cho Admin; dữ liệu hồ sơ và thống kê hệ thống không bị thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** mở chức năng tra cứu đăng ký mới hoặc xác định khoảng ngày (`Từ ngày – Đến ngày`) cần tra cứu.
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống, tính hợp lệ của khoảng ngày và truy xuất danh sách các tài khoản được tạo trong khoảng thời gian đó theo thứ tự mặc định (thời gian tạo mới nhất trước).
3. **Admin** nhập từ khóa `họ tên` cần lọc và/hoặc chọn tiêu chí sắp xếp theo `họ tên` hoặc `ngày tạo` (tăng dần / giảm dần).
4. **Hệ thống** chuẩn hóa từ khóa, lọc và sắp xếp các bản ghi đăng ký mới trong khoảng ngày đã chọn rồi cung cấp danh sách kết quả (gồm thông tin định danh tài khoản, họ tên, email/trạng thái hiển thị và thời điểm tạo quy đổi theo `Asia/Ho_Chi_Minh`).
5. **Admin** rà soát danh sách các tài khoản đăng ký mới.

### Alternative Flows

**Alternative flow 1: Lọc nhanh khoảng ngày theo tháng được chọn từ biểu đồ năm**
1. Từ bước #1 của Basic Flow, **Admin** chọn một tháng cụ thể trên biểu đồ tại [UC-ADM-ANL-02](UC-ADM-ANL-02-view-monthly-registrations-chart.md).
2. **Hệ thống** tự động đặt khoảng thời gian từ đầu ngày đầu tháng đến cuối ngày cuối tháng đó (theo `Asia/Ho_Chi_Minh`).
3. Tiếp tục bước #2 trong Basic Flow.

**Alternative flow 2: Bỏ điều kiện lọc họ tên hoặc giữ nguyên thứ tự sắp xếp**
1. Từ bước #3 của Basic Flow, **Admin** xóa trống từ khóa `họ tên` hoặc không đổi tiêu chí sắp xếp.
2. **Hệ thống** gỡ ràng buộc lọc họ tên, giữ khoảng ngày cùng thứ tự sắp xếp hiện hành.
3. Tiếp tục bước #4 trong Basic Flow.

**Alternative flow 3: Không có tài khoản đăng ký mới thỏa mãn điều kiện**
1. Từ bước #2 hoặc #4 của Basic Flow, truy vấn hoàn tất thành công nhưng không có bản ghi nào khớp khoảng ngày và từ khóa `họ tên`.
2. **Hệ thống** thông báo không có dữ liệu phù hợp trong khoảng thời gian đã chọn và giữ nguyên bộ lọc để **Admin** điều chỉnh.
3. Quay lại bước #1 hoặc #3 trong Basic Flow, hoặc kết thúc Use Case.

**Alternative flow 4: Bản ghi đăng ký thuộc tài khoản đã bị xóa vĩnh viễn (purge)**
1. Từ bước #2 hoặc #4 của Basic Flow, trong khoảng thời gian tra cứu có bản ghi tạo tài khoản của người dùng đã bị purge sau hạn xóa mềm.
2. **Hệ thống** vẫn giữ sự kiện tạo tài khoản trong thống kê lịch sử với nhãn định danh đã ẩn danh/đã xóa theo [DEC-07](../../../../decisions/account-lifecycle.md) và không ghép nhầm sang tài khoản mới dùng lại cùng `username`.
3. Tiếp tục bước #5 trong Basic Flow.

### Exception Flows

**Exception flow 1: Khoảng thời gian tra cứu không hợp lệ**
1. Từ bước #2 của Basic Flow, ngày bắt đầu lớn hơn ngày kết thúc hoặc định dạng ngày không hợp lệ.
2. **Hệ thống** từ chối thực thi truy vấn và thông báo lỗi khoảng thời gian.
3. Quay lại bước #1 trong Basic Flow.

**Exception flow 2: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #2 hoặc #4 của Basic Flow, phiên quản trị không còn hợp lệ hoặc không có quyền Admin hệ thống.
2. **Hệ thống** từ chối truy vấn và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 3: Lỗi kết nối hoặc lỗi truy vấn dữ liệu**
1. Từ bước #2 hoặc #4 của Basic Flow, máy chủ không phản hồi hoặc truy vấn dữ liệu thất bại.
2. **Hệ thống** thông báo lỗi truy xuất dữ liệu, giữ nguyên tiêu chí lọc hiện có để thử lại và phân biệt rõ lỗi hệ thống với trường hợp không có lượt đăng ký mới.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Nguồn tính "đăng ký mới" bao gồm mọi tài khoản người dùng được tạo thành công trong khoảng thời gian chọn (gồm tài khoản người dùng tự đăng ký công khai đã hoàn tất xác minh email và tài khoản do Admin tạo mới); các yêu cầu đăng ký tạm chưa hoàn tất xác minh email theo [DEC-06](../../../../decisions/password-reset-and-email.md) và [DEC-07](../../../../decisions/account-lifecycle.md) chưa phải là tài khoản nên không được tính.
  - **BR-02**: Khoảng thời gian lọc và mốc ngày tạo được tính theo múi giờ `Asia/Ho_Chi_Minh` (lưu chuẩn UTC trên máy chủ; ngày kết thúc tính đến trước `00:00` của ngày kế tiếp theo [Thống kê và thời gian](../../../../decisions/statistics-and-time.md)).
  - **BR-03**: Lọc theo `họ tên` thực hiện đối sánh chuỗi con không phân biệt hoa thường sau khi cắt khoảng trắng thừa hai đầu (`trim`). Sắp xếp hỗ trợ 02 tiêu chí: theo `họ tên` hoặc theo `ngày tạo` (tăng/giảm, dùng `Account ID` làm khóa phụ cố định khi trùng giá trị).
  - **BR-04**: Thống kê đăng ký mới được bảo toàn sau khi tài khoản bị xóa mềm (`DELETED`) hoặc xóa vĩnh viễn (`purge`) theo [Thống kê và thời gian](../../../../decisions/statistics-and-time.md) và [DEC-07](../../../../decisions/account-lifecycle.md).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Phản hồi danh sách không chứa mật khẩu, mã xác minh hay khóa mật mã E2EE.
  - **NFR-02**: Khi **Admin** đổi nhanh khoảng ngày, từ khóa hoặc tiêu chí sắp xếp, kết quả trả về muộn của truy vấn cũ không được ghi đè kết quả của truy vấn mới nhất.
