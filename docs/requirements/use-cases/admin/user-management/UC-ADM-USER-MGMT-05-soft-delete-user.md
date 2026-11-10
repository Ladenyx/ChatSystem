# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-USER-MGMT-05
- **Use Case Name**: Xóa mềm tài khoản người dùng
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-29
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin đưa một tài khoản người dùng vào trạng thái xóa mềm có thời hạn lưu giữ 30 ngày, chấm dứt ngay quyền đăng nhập và nhận tin nhắn mới nhưng vẫn cho phép khôi phục trước khi hết hạn.
- **Goal**: Vô hiệu hóa tài khoản theo quy trình xóa có thời gian ân hạn 30 ngày trước khi xóa vĩnh viễn (purge).
- **Trigger**: Admin chọn thực hiện xóa mềm 30 ngày đối với tài khoản người dùng đang được Admin xem hồ sơ chi tiết ([UC-ADM-USER-MGMT-02](UC-ADM-USER-MGMT-02-view-user-profile.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin có phiên quản trị hợp lệ và quyền Admin hệ thống; tài khoản được chọn là tài khoản `USER` đang tồn tại và chưa ở trạng thái `DELETED`.
- **Post-conditions**: Tài khoản chuyển sang trạng thái `DELETED` kèm mốc thời gian xóa và hạn khôi phục 30 ngày theo giờ máy chủ (lưu giữ trạng thái trước khi xóa để phục vụ khôi phục); toàn bộ phiên đang hoạt động của tài khoản bị thu hồi ngay lập tức và tài khoản ngừng nhận tin nhắn mới.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** khởi tạo yêu cầu xóa mềm 30 ngày đối với tài khoản người dùng đang được chọn.
2. **Hệ thống** yêu cầu **Admin** xác nhận đối tượng tài khoản, hệ quả thu hồi phiên/ngừng sử dụng và thời hạn cho phép khôi phục trong 30 ngày.
3. **Admin** xác nhận thực hiện xóa mềm.
4. **Hệ thống** kiểm tra lại phiên, quyền Admin hệ thống, trạng thái hiện tại của tài khoản và ràng buộc quản trị nhóm chat của người dùng đó.
5. **Hệ thống** chuyển trạng thái tài khoản sang `DELETED`, ghi nhận mốc thời gian xóa cùng hạn khôi phục 30 ngày, đồng thời thu hồi toàn bộ phiên đang hoạt động và chặn đăng nhập/nhận tin nhắn mới.
6. **Hệ thống** thông báo xóa mềm thành công kèm thời hạn khôi phục quy đổi theo `Asia/Ho_Chi_Minh`.

### Alternative Flows

**Alternative flow 1: Admin hủy bỏ xác nhận xóa mềm**
1. Từ bước #2 của Basic Flow, **Admin** từ chối xác nhận thao tác.
2. **Hệ thống** hủy yêu cầu và giữ nguyên trạng thái hiện tại của tài khoản.
3. Kết thúc Use Case.

**Alternative flow 2: Tài khoản đã ở trạng thái xóa mềm từ thao tác khác**
1. Từ bước #4 của Basic Flow, **Hệ thống** phát hiện tài khoản đã được chuyển sang `DELETED` bởi một yêu cầu trước đó.
2. **Hệ thống** giữ nguyên mốc hạn 30 ngày ban đầu (không gia hạn lại 30 ngày mới) và trả về trạng thái `DELETED` hiện tại cho **Admin**.
3. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Người dùng đang là Admin duy nhất của nhóm chat còn thành viên khác**
1. Từ bước #4 của Basic Flow, **Hệ thống** phát hiện tài khoản đang là Admin nhóm duy nhất trong ít nhất một nhóm chat vẫn còn thành viên khác theo [DEC-05](../../../../decisions/group-permissions-and-e2ee.md) và [DEC-07](../../../../decisions/account-lifecycle.md).
2. **Hệ thống** từ chối xóa mềm và thông báo rõ điều kiện bảo vệ Admin nhóm cuối cùng chưa được giải quyết (riêng nhóm chỉ còn duy nhất người bị xóa thì được đóng và giữ lịch sử).
3. Kết thúc Use Case.

**Exception flow 2: Tài khoản không còn tồn tại hoặc không thuộc phạm vi xóa**
1. Từ bước #4 của Basic Flow, tài khoản đã bị purge hoặc không phải tài khoản `USER` hợp lệ để xóa qua chức năng này.
2. **Hệ thống** từ chối thao tác, thông báo đối tượng không hợp lệ và làm mới lại danh sách người dùng.
3. Kết thúc Use Case.

**Exception flow 3: Phiên quản trị hết hiệu lực, thiếu quyền hoặc lỗi kết nối/ghi dữ liệu**
1. Từ bước #4 hoặc #5 của Basic Flow, phiên quản trị không hợp lệ, lỗi lưu trạng thái hoặc mất phản hồi từ máy chủ.
2. **Hệ thống** không báo xóa thành công khi chưa xác nhận ghi hoàn tất; buộc xác thực lại nếu mất phiên hoặc yêu cầu đối chiếu lại trạng thái thực tế trên máy chủ trước khi thử lại.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Xóa mềm có thời hạn đúng **30 ngày** tính theo thời gian máy chủ ([DEC-07](../../../../decisions/account-lifecycle.md)). Trong thời gian `DELETED`, `username` và `email` chưa được giải phóng để đăng ký/tạo mới, và Admin có thể khôi phục tài khoản theo [UC-ADM-USER-MGMT-06](UC-ADM-USER-MGMT-06-restore-user.md).
  - **BR-02**: Khi hết hạn 30 ngày mà không được khôi phục, tiến trình hệ thống mới thực hiện purge hồ sơ, thông tin đăng nhập, quan hệ bạn bè và bản sao lưu khóa trên máy chủ; giữ lại thống kê tổng hợp và ẩn danh định danh theo [DEC-07](../../../../decisions/account-lifecycle.md).
  - **BR-03**: Xóa mềm thu hồi ngay mọi phiên hoạt động (bao gồm kết nối thời gian thực), nhưng hệ thống không giải mã nội dung nhóm E2EE và không cam kết xóa các bản sao lịch sử đã nằm cục bộ trên thiết bị của người dùng khác.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Việc chuyển sang trạng thái `DELETED` và vô hiệu hóa quyền sử dụng phiên phải có hiệu lực tức thời để không chấp nhận thêm bất kỳ yêu cầu nghiệp vụ nào từ phiên cũ.
  - **NFR-02**: Mốc thời gian xóa và thời hạn khôi phục hiển thị cho Admin theo múi giờ `Asia/Ho_Chi_Minh`.
