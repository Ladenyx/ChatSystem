# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-USER-MGMT-08
- **Use Case Name**: Mở khóa tài khoản người dùng
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-29
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin gỡ trạng thái khóa quản trị (`LOCKED`) cho một tài khoản người dùng để cho phép chủ tài khoản thực hiện đăng nhập lại theo các điều kiện xác thực hiện hành.
- **Goal**: Gỡ bỏ lệnh khóa quản trị trên tài khoản người dùng được chọn mà không tự khôi phục phiên cũ hay bỏ qua các điều kiện bảo mật độc lập khác.
- **Trigger**: Admin chọn thực hiện mở khóa đối với một tài khoản đang bị khóa ([UC-ADM-USER-MGMT-02](UC-ADM-USER-MGMT-02-view-user-profile.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin có phiên quản trị hợp lệ và quyền Admin hệ thống; tài khoản được chọn là tài khoản `USER` đang ở trạng thái `LOCKED` và chưa bị xóa mềm/purge.
- **Post-conditions**: Tài khoản chuyển từ trạng thái `LOCKED` sang `ACTIVE`; các ràng buộc độc lập (như chưa xác minh email, bắt buộc đổi mật khẩu tạm hoặc hạn khóa tạm do thử sai) vẫn được bảo toàn; các phiên cũ đã thu hồi trước đó không tự hoạt động trở lại.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** khởi tạo yêu cầu mở khóa đối với tài khoản người dùng đang chọn.
2. **Hệ thống** yêu cầu **Admin** xác nhận rõ đối tượng tài khoản sẽ được gỡ trạng thái khóa.
3. **Admin** xác nhận thực hiện mở khóa.
4. **Hệ thống** kiểm tra lại phiên, quyền Admin hệ thống và trạng thái `LOCKED` hiện tại của tài khoản theo mã định danh.
5. **Hệ thống** chuyển trạng thái tài khoản sang `ACTIVE` mà không tái kích hoạt bất kỳ phiên cũ nào đã bị thu hồi trước đó.
6. **Hệ thống** thông báo mở khóa thành công (người dùng cần chủ động đăng nhập lại) và cập nhật thông tin trạng thái của tài khoản.

### Alternative Flows

**Alternative flow 1: Admin hủy bỏ xác nhận mở khóa**
1. Từ bước #2 của Basic Flow, **Admin** từ chối xác nhận thao tác.
2. **Hệ thống** hủy yêu cầu và giữ nguyên trạng thái `LOCKED` của tài khoản.
3. Kết thúc Use Case.

**Alternative flow 2: Tài khoản đã được mở khóa bởi thao tác khác**
1. Từ bước #4 của Basic Flow, **Hệ thống** phát hiện tài khoản đã được chuyển về `ACTIVE` trước đó.
2. **Hệ thống** trả về trạng thái hiện tại mà không tạo tác động lặp và đồng bộ lại thông tin cho **Admin**.
3. Kết thúc Use Case.

**Alternative flow 3: Tài khoản sau mở khóa vẫn còn điều kiện xác thực độc lập**
1. Từ bước #5 của Basic Flow, tài khoản đã thoát trạng thái `LOCKED` nhưng chưa xác minh email hoặc đang dùng mật khẩu tạm cần đổi.
2. **Hệ thống** giữ nguyên các điều kiện độc lập đó và phản ánh rõ tình trạng hiện có trong bước #6 của Basic Flow.

### Exception Flows

**Exception flow 1: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #4 của Basic Flow, phiên quản trị không còn hợp lệ hoặc không có quyền Admin hệ thống.
2. **Hệ thống** từ chối yêu cầu mở khóa và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 2: Tài khoản đã chuyển sang xóa mềm hoặc đã bị purge**
1. Từ bước #4 của Basic Flow, tài khoản đang ở trạng thái `DELETED` hoặc không còn tồn tại.
2. **Hệ thống** từ chối gỡ khóa (việc khôi phục tài khoản xóa mềm phải thực hiện riêng qua [UC-ADM-USER-MGMT-06](UC-ADM-USER-MGMT-06-restore-user.md)) và làm mới lại thông tin tài khoản.
3. Kết thúc Use Case.

**Exception flow 3: Lỗi ghi dữ liệu hoặc mất phản hồi từ máy chủ**
1. Từ bước #4 hoặc #5 của Basic Flow, quá trình cập nhật trạng thái gặp lỗi hoặc mất kết nối.
2. **Hệ thống** không báo mở khóa thành công khi chưa nhận được xác nhận từ máy chủ và yêu cầu đọc lại trạng thái thực tế trước khi thử lại.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Mọi yêu cầu mở khóa đều bắt buộc qua bước xác nhận của Admin và kiểm tra quyền Admin hệ thống trên máy chủ.
  - **BR-02**: Mở khóa chỉ thực hiện chuyển đổi `LOCKED` → `ACTIVE` theo [DEC-07](../../../../decisions/account-lifecycle.md); không tự xác minh email, không đặt lại mật khẩu, không khôi phục tài khoản `DELETED` và không tự cấp phiên đăng nhập cho người dùng.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Các yêu cầu khóa, mở khóa hoặc xóa mềm diễn ra đồng thời trên cùng một tài khoản phải được xử lý tuần tự hóa nhất quán trên máy chủ.
  - **NFR-02**: Nghiệp vụ mở khóa không đọc hoặc trả về mật khẩu hay khóa E2EE của người dùng.
