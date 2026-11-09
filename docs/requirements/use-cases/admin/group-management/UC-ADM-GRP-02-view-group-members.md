# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-GRP-02
- **Use Case Name**: Xem thành viên và Admin của nhóm chat
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-30
- **Last Updated by / Date**: ladenyx / 2026-11-08
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin hệ thống xem danh sách thành viên hiện tại của một nhóm chat đã chọn, được phân tách rõ giữa nhóm vai trò Admin nhóm và Thành viên thường.
- **Goal**: Xác định chính xác những tài khoản nào đang giữ quyền Admin nhóm và những tài khoản nào là thành viên thường của đúng nhóm đang kiểm tra.
- **Trigger**: Admin chọn một nhóm chat từ danh sách tra cứu nhóm ([UC-ADM-GRP-01](UC-ADM-GRP-01-browse-groups.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin có phiên quản trị hợp lệ và quyền Admin hệ thống; định danh nhóm chat cần kiểm tra đã được chọn từ [UC-ADM-GRP-01](UC-ADM-GRP-01-browse-groups.md).
- **Post-conditions**: Danh sách **Admin nhóm** và danh sách **Thành viên thường** hiện hành của đúng nhóm đã chọn được cung cấp cho Admin; tư cách thành viên, vai trò và dữ liệu của nhóm không bị thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** yêu cầu xem thông tin thành viên và vai trò của một nhóm chat đã chọn.
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống và sự tồn tại khả dụng của nhóm theo mã định danh.
3. **Hệ thống** truy xuất toàn bộ thành viên hiện tại của nhóm kèm vai trò tương ứng, tách thành hai tập độc lập gồm **Admin nhóm** và **Thành viên thường** (mặc định sắp xếp theo tên hiển thị A–Z trong từng tập) và trả kết quả gồm tên hiển thị (`họ tên`), `username` và vai trò cho **Admin**.
4. **Admin** đối chiếu danh sách Admin nhóm và thành viên của nhóm đang chọn.

### Alternative Flows

**Alternative flow 1: Chuyển sang kiểm tra một nhóm chat khác**
1. Từ bước #2 hoặc #4 của Basic Flow, **Admin** chọn sang một nhóm chat khác trong danh sách tại [UC-ADM-GRP-01](UC-ADM-GRP-01-browse-groups.md).
2. **Hệ thống** hủy bỏ hoặc bỏ qua phản hồi trễ của nhóm trước đó và thực hiện lại từ bước #1 của Basic Flow cho nhóm mới được chọn.

**Alternative flow 2: Một trong hai nhóm vai trò không có bản ghi**
1. Từ bước #3 của Basic Flow, tập **Thành viên thường** (ví dụ mọi thành viên trong nhóm đều là Admin nhóm) hoặc tập **Admin nhóm** không có bản ghi nào.
2. **Hệ thống** thông báo không có thành viên cho riêng nhóm vai trò trống đó và vẫn cung cấp đầy đủ dữ liệu của nhóm vai trò còn lại.
3. Tiếp tục bước #4 trong Basic Flow.

### Exception Flows

**Exception flow 1: Nhóm chat không còn khả dụng**
1. Từ bước #2 của Basic Flow, nhóm vừa chọn đã bị đóng hoặc không còn tồn tại trên máy chủ.
2. **Hệ thống** thông báo nhóm không còn khả dụng, xóa ngữ cảnh chi tiết cũ để không hiển thị nhầm dữ liệu nhóm trước đó và làm mới danh sách nhóm tại [UC-ADM-GRP-01](UC-ADM-GRP-01-browse-groups.md).
3. Kết thúc Use Case.

**Exception flow 2: Lỗi kết nối hoặc lỗi truy xuất danh sách thành viên**
1. Từ bước #2 hoặc #3 của Basic Flow, quá trình truy vấn thành viên nhóm thất bại.
2. **Hệ thống** thông báo lỗi tải chi tiết thành viên, cho phép thử tải lại đối với nhóm đang chọn và không thay thế lỗi bằng danh sách thành viên rỗng.
3. Kết thúc Use Case.

**Exception flow 3: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #2 của Basic Flow, phiên quản trị không còn hợp lệ hoặc không có quyền Admin hệ thống.
2. **Hệ thống** ngừng cung cấp dữ liệu chi tiết nhóm và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Nghiệp vụ này chỉ cho phép xem danh sách thành viên và vai trò trong nhóm; không cung cấp thao tác cấp/thu hồi quyền Admin nhóm, thêm/xóa thành viên, đổi tên nhóm, tham gia hoặc rời nhóm cho Admin hệ thống.
  - **BR-02**: Mỗi thành viên hiện tại của nhóm chỉ thuộc đúng một trong hai phân loại (**Admin nhóm** hoặc **Thành viên thường**, không trùng lặp ở cả hai tập) để tổng số bản ghi khớp với số lượng thành viên của nhóm tại [UC-ADM-GRP-01](UC-ADM-GRP-01-browse-groups.md).
  - **BR-03**: Quyền Admin hệ thống và quyền Admin nhóm hoàn toàn độc lập; thao tác xem thành viên không làm thay đổi tư cách thành viên hay quyền quản trị trong nhóm.
  - **BR-04**: Theo [DEC-05](../../../../decisions/group-permissions-and-e2ee.md) và [DEC-07](../../../../decisions/account-lifecycle.md), Admin hệ thống chỉ được tra cứu siêu dữ liệu quản trị nhóm (thông tin nhóm, thành viên, vai trò) và không có quyền xem nội dung hội thoại cá nhân hay nhóm kể cả khi không mã hóa E2EE (ngoại trừ tin nhắn bằng chứng do người dùng chủ động gửi kèm khi báo cáo spam); đồng thời với nhóm áp dụng E2EE, máy chủ không lưu khóa hay giải mã luồng chat nhóm.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Tối thiểu hóa dữ liệu & bảo mật — chỉ trả về thông tin định danh hiển thị (`họ tên`, `username`) và vai trò trong nhóm; không truyền thông tin nhạy cảm của tài khoản, khóa mật mã hay bản mã luồng chat nhóm.
  - **NFR-02**: Khi **Admin** chuyển đổi nhanh giữa nhiều nhóm, danh sách thành viên hiển thị phải luôn khớp đúng định danh nhóm đang chọn, không bị ghi đè bởi phản hồi trễ của nhóm cũ.
