# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-A15
- **Use Case Name**: Xem thành viên và Admin của nhóm chat
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-30
- **Last Updated by / Date**: ladenyx / 2026-11-06
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: Không có tác nhân ngoài hệ thống
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin hệ thống xem danh sách thành viên và vai trò Admin nhóm trong vùng chi tiết của nhóm đã chọn.
- **Goal**: Xác định chính xác ai là Admin nhóm và ai là thành viên của đúng nhóm đang kiểm tra.
- **Trigger**: Admin chọn một nhóm trong danh sách tại màn hình `A-SCR-04` (từ `UC-A14`).

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**: Actor đã đăng nhập vào Admin Console với quyền Admin hệ thống, phiên làm việc còn hợp lệ; màn hình `A-SCR-04` đã hiển thị danh sách nhóm và nhóm được chọn còn khả dụng.
- **Post-conditions**: Vùng chi tiết hiển thị phân nhóm Admin nhóm nổi bật ở trên và phân nhóm Thành viên thường ở dưới cho đúng nhóm đã chọn; dữ liệu và quyền trong nhóm không bị thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn một nhóm trong danh sách tại màn hình `A-SCR-04` | Ghi nhận định danh nhóm được chọn, cập nhật tiêu đề nhóm đang kiểm tra và hiển thị trạng thái đang tải; kiểm tra quyền Admin hệ thống và truy xuất danh sách thành viên kèm vai trò hiện tại từ Server. |
| 2 | Xem thông tin thành viên và vai trò trong vùng chi tiết | Hiển thị tách biệt phân nhóm **Admin nhóm** nổi bật ở trên và phân nhóm **Thành viên thường** ở dưới (gồm tên hiển thị, `username` và nhãn vai trò), sắp xếp mặc định theo tên hiển thị A–Z trong từng phân nhóm. |

### Alternative Flows

- **Alt 1a/2a (Chuyển sang chọn nhóm khác)**: Tại bước 1 hoặc 2, Admin chọn một nhóm khác trong danh sách tại `A-SCR-04`: hệ thống bỏ qua phản hồi trễ (nếu có) của nhóm trước đó và thực hiện lại từ bước 1 cho nhóm mới được chọn.
- **Alt 2b (Một phân nhóm không có bản ghi)**: Tại bước 2, phân nhóm **Admin nhóm** hoặc phân nhóm **Thành viên thường** không có bản ghi theo kết quả từ Server: hiển thị trạng thái không có dữ liệu cục bộ ngay tại phân nhóm đó; vẫn hiển thị bình thường phân nhóm còn lại và không hiển thị thông báo rỗng/lỗi phủ toàn bộ nhóm.

### Exception Flows

- **Ex 1a (Nhóm không còn khả dụng)**: Nhóm vừa chọn đã bị giải tán hoặc không còn tồn tại trên Server: thông báo nhóm không còn khả dụng, không hiển thị danh sách cũ như danh sách hiện tại, đồng thời làm mới danh sách nhóm tại `A-SCR-04` và đặt lại trạng thái chưa chọn nhóm.
- **Ex 1b (Lỗi kết nối hoặc truy xuất dữ liệu)**: Hiển thị thông báo lỗi tải chi tiết tại chỗ và cho phép thử tải lại đối với nhóm đang chọn; không thay thế trạng thái lỗi bằng danh sách thành viên rỗng.
- **Ex 1c/2a (Phiên không hợp lệ hoặc không đủ quyền)**: Ngừng hiển thị dữ liệu chi tiết, thông báo phiên hết hạn/không có quyền truy cập và yêu cầu xác thực lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
    - **BR-01**: Phạm vi `UC-A15` (`A-09`) chỉ gồm xem danh sách thành viên và vai trò Admin nhóm; không cung cấp thao tác cấp/thu hồi quyền Admin nhóm, thêm/xóa thành viên, đổi tên nhóm, tham gia hoặc rời nhóm.
    - **BR-02**: Mỗi thành viên của nhóm chỉ xuất hiện ở đúng một phân nhóm tương ứng với vai trò hiện tại (**Admin nhóm** hoặc **Thành viên thường**, không lặp lại ở cả hai mục) để tổng số bản ghi khớp với quy mô thành viên tại `UC-A14`.
    - **BR-03**: Quyền Admin hệ thống và Admin nhóm là hai vai trò độc lập; việc xem chi tiết nhóm không làm thay đổi tư cách thành viên hay quyền quản trị nhóm.
    - **BR-04**: Admin hệ thống chỉ được tra cứu siêu dữ liệu quản trị nhóm (thông tin nhóm, thành viên, vai trò) và không có quyền xem nội dung hội thoại cá nhân hay nhóm kể cả khi không mã hóa E2EE (ngoại trừ các tin nhắn bằng chứng do người dùng chủ động gửi kèm khi báo cáo spam tại `UC-A-17`); đồng thời với nhóm áp dụng E2EE (`U-25`), hệ thống cũng không lưu khóa hay giải mã luồng chat nhóm.
- **Non-Functional Requirements (NFR)**:
    - **NFR-01 (Hiển thị trực quan & Đúng ngữ cảnh nhóm)**: Luôn hiển thị rõ tên nhóm đang được kiểm tra ở tiêu đề chi tiết; phân biệt trực quan giữa **Admin nhóm** và **Thành viên thường**; đảm bảo danh sách hiển thị luôn khớp với nhóm đang được đánh dấu chọn.
    - **NFR-02 (Tối thiểu hóa dữ liệu & Bảo mật)**: Dữ liệu trả về cho vùng chi tiết chỉ gồm thông tin định danh hiển thị (tên hiển thị, `username`) và vai trò trong nhóm; không tải dữ liệu nhạy cảm của tài khoản hay bản mã luồng chat nhóm.
    - **NFR-03 (Phản hồi giao diện Desktop GUI)**: Tác vụ tải chi tiết thành viên không làm đơ luồng giao diện (UI thread); phân biệt rõ các trạng thái: đang tải (`loading`), phân nhóm rỗng (`empty`), và lỗi tải chi tiết (`error`).

