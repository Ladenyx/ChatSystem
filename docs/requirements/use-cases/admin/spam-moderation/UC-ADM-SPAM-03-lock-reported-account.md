# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-SPAM-03
- **Use Case Name**: Khóa tài khoản từ báo cáo spam
- **Author / Create by**: ladenyx
- **Date Create**: 2026-11-01
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Từ ngữ cảnh báo cáo spam đang xem xét, Admin thực hiện khóa tài khoản người dùng bị báo cáo để chấm dứt ngay quyền truy cập của tài khoản vi phạm.
- **Goal**: Khóa đúng tài khoản người dùng bị báo cáo và thu hồi toàn bộ phiên đang hoạt động của tài khoản đó.
- **Trigger**: Admin chọn thực hiện khóa tài khoản bị báo cáo trong khi đang xem chi tiết báo cáo spam ([UC-ADM-SPAM-02](UC-ADM-SPAM-02-view-spam-report.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin có phiên quản trị hợp lệ và quyền Admin hệ thống; báo cáo spam cùng định danh tài khoản bị báo cáo đã được tải tại [UC-ADM-SPAM-02](UC-ADM-SPAM-02-view-spam-report.md).
- **Post-conditions**: Tài khoản người dùng bị báo cáo chuyển sang trạng thái `LOCKED`, mọi phiên đang hoạt động của tài khoản đó bị thu hồi ngay lập tức; dữ liệu báo cáo và bằng chứng vẫn được giữ nguyên.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** khởi tạo yêu cầu khóa đối với tài khoản bị báo cáo trong báo cáo spam đang xem.
2. **Hệ thống** yêu cầu **Admin** xác nhận rõ định danh tài khoản bị báo cáo và hệ quả thu hồi phiên ngay khi khóa.
3. **Admin** xác nhận thực hiện khóa tài khoản.
4. **Hệ thống** kiểm tra lại hiệu lực phiên, quyền Admin hệ thống, mối liên kết giữa báo cáo với định danh tài khoản bị báo cáo, sau đó thực thi nghiệp vụ khóa và thu hồi phiên theo quy tắc của `UC-ADM-USER-07` (Khóa tài khoản người dùng).
5. **Hệ thống** xác nhận hoàn tất, cập nhật trạng thái tài khoản bị báo cáo thành `LOCKED` trong ngữ cảnh báo cáo đang xem.

### Alternative Flows

**Alternative flow 1: Admin hủy bỏ xác nhận khóa**
1. Từ bước #2 của Basic Flow, **Admin** từ chối xác nhận thao tác khóa.
2. **Hệ thống** hủy thao tác, không gửi lệnh khóa và giữ nguyên trạng thái tài khoản lẫn báo cáo đang xem.
3. Kết thúc Use Case.

**Alternative flow 2: Tài khoản bị báo cáo đã bị khóa trước đó**
1. Từ bước #4 của Basic Flow, **Hệ thống** phát hiện tài khoản bị báo cáo đã ở trạng thái `LOCKED` do một thao tác quản trị khác.
2. **Hệ thống** trả về trạng thái `LOCKED` hiện tại mà không tạo tác động lặp hay tự mở khóa lại, đồng thời đồng bộ lại thông tin trạng thái cho **Admin**.
3. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #4 của Basic Flow, phiên của **Admin** không còn hợp lệ hoặc không có quyền Admin hệ thống.
2. **Hệ thống** từ chối yêu cầu khóa và yêu cầu xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 2: Tài khoản bị báo cáo không còn hợp lệ để khóa**
1. Từ bước #4 của Basic Flow, tài khoản bị báo cáo đã bị xóa mềm (`DELETED`), đã bị xóa vĩnh viễn (purge) hoặc không thuộc phạm vi tài khoản User được phép khóa.
2. **Hệ thống** từ chối thao tác khóa, không tự chuyển sang tài khoản khác có cùng `username`, thông báo trạng thái thực tế và làm mới lại dữ liệu báo cáo.
3. Kết thúc Use Case.

**Exception flow 3: Mất kết nối hoặc không xác định được kết quả phản hồi**
1. Từ bước #3 hoặc #4 của Basic Flow, kết nối gián đoạn trước hoặc sau khi yêu cầu khóa được gửi tới máy chủ.
2. **Hệ thống** thông báo kết quả chưa xác định, không tự kết luận đã khóa thành công và yêu cầu truy vấn đối chiếu lại trạng thái tài khoản trước khi thử lại.
3. Kết thúc Use Case.

**Exception flow 4: Lỗi hệ thống khi ghi nhận khóa hoặc thu hồi phiên**
1. Từ bước #4 của Basic Flow, quá trình cập nhật trạng thái `LOCKED` và vô hiệu hóa phiên không hoàn tất trọn vẹn.
2. **Hệ thống** đảm bảo không để phiên của tài khoản đã khóa tiếp tục được chấp nhận, trả thông báo lỗi xử lý và yêu cầu tải lại trạng thái mới nhất.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Đối tượng bị khóa bắt buộc là **tài khoản User bị báo cáo** gắn theo định danh bất biến trong báo cáo spam, tuyệt đối không tác động nhầm lên người gửi báo cáo; tư cách Admin nhóm chat của người bị báo cáo không miễn trừ thao tác khóa.
  - **BR-02**: Bắt buộc phải có bước **Admin** xác nhận tường minh trước khi thực thi khóa tài khoản.
  - **BR-03**: Hiệu lực khóa và cơ chế thu hồi toàn bộ phiên tuân thủ đồng nhất quy tắc nghiệp vụ của `UC-ADM-USER-07` và [DEC-07](../../../../decisions/account-lifecycle.md). Việc khóa tài khoản không xóa bản ghi báo cáo spam hay bằng chứng đính kèm.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Máy chủ phải xác minh lại phiên, quyền Admin hệ thống và định danh tài khoản đích trực tiếp từ dữ liệu gốc của báo cáo, không tin cậy đơn thuần tham số gửi từ phía ứng dụng khách.
  - **NFR-02**: Hệ thống ngăn chặn xử lý trùng lặp khi cùng một yêu cầu khóa bị gửi lặp trong lúc đang chờ phản hồi.
