# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-GRP-01
- **Use Case Name**: Tra cứu danh sách nhóm chat
- **Author / Create by**: ladenyx
- **Date Create**: 2026-11-02
- **Last Updated by / Date**: ladenyx / 2026-11-08
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin hệ thống tra cứu danh sách các nhóm chat trong hệ thống, kết hợp lọc theo tên nhóm và sắp xếp theo tên nhóm hoặc thời gian tạo.
- **Goal**: Xác định đúng nhóm chat cần kiểm tra và nắm quy mô thành viên trước khi xem chi tiết danh sách thành viên và vai trò của nhóm.
- **Trigger**: Admin mở chức năng Quản lý nhóm chat hoặc điều hướng sang từ chỉ số tổng nhóm chat trên trang Tổng quan ([UC-ADM-OVW-01](../overview/UC-ADM-OVW-01-view-overview.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin đã có phiên quản trị hợp lệ và quyền Admin hệ thống ([UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md)).
- **Post-conditions**: Danh sách siêu dữ liệu nhóm chat khớp với tiêu chí lọc và thứ tự sắp xếp được cung cấp; nếu Admin chọn một nhóm cụ thể thì ngữ cảnh định danh nhóm được chuyển sang [UC-ADM-GRP-02](UC-ADM-GRP-02-view-group-members.md); dữ liệu và quyền trong các nhóm không bị thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** mở chức năng Quản lý nhóm chat.
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống và trả về danh sách các nhóm chat (gồm mã nhóm, tên nhóm, thời gian tạo, số lượng thành viên) theo thứ tự mặc định là thời gian tạo mới nhất trước.
3. **Admin** nhập từ khóa tên nhóm cần lọc và/hoặc chọn tiêu chí sắp xếp (`tên nhóm` hoặc `thời gian tạo`, tăng hoặc giảm).
4. **Hệ thống** chuẩn hóa từ khóa, truy vấn và trả về danh sách các nhóm thỏa mãn điều kiện lọc cùng thứ tự sắp xếp đã chọn.
5. **Admin** chọn một nhóm chat trong danh sách kết quả.
6. **Hệ thống** chuyển định danh nhóm được chọn sang [UC-ADM-GRP-02](UC-ADM-GRP-02-view-group-members.md) để tải danh sách thành viên và vai trò.

### Alternative Flows

**Alternative flow 1: Hệ thống chưa có nhóm chat nào**
1. Từ bước #2 của Basic Flow, truy vấn hoàn tất thành công nhưng toàn hệ thống chưa phát sinh nhóm chat nào.
2. **Hệ thống** thông báo hiện chưa có nhóm chat nào trong hệ thống.
3. Kết thúc Use Case.

**Alternative flow 2: Bỏ điều kiện lọc tên nhóm**
1. Từ bước #3 của Basic Flow, **Admin** xóa trống từ khóa tên nhóm.
2. **Hệ thống** gỡ bỏ ràng buộc lọc theo tên, giữ nguyên tiêu chí sắp xếp đang áp dụng và tiếp tục bước #4 trong Basic Flow.

**Alternative flow 3: Không có nhóm khớp từ khóa lọc**
1. Từ bước #4 của Basic Flow, không có nhóm chat nào khớp với từ khóa đã nhập.
2. **Hệ thống** thông báo không tìm thấy nhóm phù hợp và giữ nguyên tiêu chí hiện tại để **Admin** điều chỉnh.
3. Quay lại bước #3 trong Basic Flow hoặc kết thúc Use Case.

**Alternative flow 4: Admin chỉ tra cứu danh sách nhóm**
1. Từ bước #2 hoặc #4 của Basic Flow, **Admin** hoàn tất việc rà soát danh sách mà không chọn nhóm cụ thể để xem chi tiết.
2. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Lỗi kết nối hoặc lỗi truy xuất dữ liệu**
1. Từ bước #2 hoặc #4 của Basic Flow, quá trình truy vấn danh sách nhóm thất bại.
2. **Hệ thống** thông báo lỗi truy xuất dữ liệu, giữ lại tiêu chí lọc/sắp xếp để thử lại và không trình bày dữ liệu cũ như kết quả của truy vấn mới.
3. Kết thúc Use Case.

**Exception flow 2: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #2, #4 hoặc #6 của Basic Flow, phiên quản trị không còn hợp lệ hoặc không có quyền Admin hệ thống.
2. **Hệ thống** từ chối thao tác và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 3: Nhóm được chọn không còn khả dụng**
1. Từ bước #6 của Basic Flow, nhóm vừa chọn đã bị đóng hoặc không còn tồn tại trên máy chủ tại thời điểm tải chi tiết.
2. **Hệ thống** thông báo nhóm không còn khả dụng, làm mới lại danh sách nhóm và không tự mở nhóm khác thay thế.
3. Quay lại bước #4 trong Basic Flow.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Nghiệp vụ này chỉ cho phép xem danh sách, lọc theo tên nhóm và sắp xếp theo tên nhóm hoặc thời gian tạo; không cung cấp thao tác tạo mới, đổi tên hay xóa nhóm chat cho Admin hệ thống.
  - **BR-02**: Quyền Admin hệ thống và quyền Admin nhóm là hai vai trò độc lập; việc tra cứu danh sách nhóm không tự động thêm Admin hệ thống làm thành viên hay cấp quyền Admin nhóm.
  - **BR-03**: Điều kiện lọc theo tên nhóm thực hiện so khớp chuỗi con không phân biệt chữ hoa/thường sau khi cắt khoảng trắng thừa ở hai đầu từ khóa (`trim`). Mốc `thời gian tạo` hiển thị theo múi giờ `Asia/Ho_Chi_Minh` ([Thống kê và thời gian](../../../../decisions/statistics-and-time.md)).
  - **BR-04**: Theo chính sách quyền riêng tư quản trị và mã hóa tại [DEC-05](../../../../decisions/group-permissions-and-e2ee.md) và [DEC-07](../../../../decisions/account-lifecycle.md), Admin hệ thống chỉ được tra cứu siêu dữ liệu quản trị nhóm (thông tin nhóm, thành viên, vai trò) và mặc định không có quyền xem nội dung hội thoại cá nhân hay nhóm kể cả khi không mã hóa E2EE (ngoại trừ tin nhắn bằng chứng do người dùng chủ động gửi kèm trong báo cáo spam); đồng thời với nhóm áp dụng E2EE, máy chủ không lưu khóa giải mã luồng chat nhóm.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Tối thiểu hóa dữ liệu — phản hồi danh sách chỉ bao gồm siêu dữ liệu nhóm (mã nhóm, tên nhóm, thời gian tạo, số lượng thành viên), tuyệt đối không tải bản mã hay nội dung luồng tin nhắn nhóm.
  - **NFR-02**: Hệ thống phân biệt rõ trạng thái danh sách rỗng thực tế với lỗi truy vấn, và không để phản hồi muộn của truy vấn cũ ghi đè kết quả lọc/sắp xếp mới nhất.
