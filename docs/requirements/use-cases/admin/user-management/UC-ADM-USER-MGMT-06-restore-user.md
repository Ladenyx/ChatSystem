# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-USER-MGMT-06
- **Use Case Name**: Khôi phục tài khoản người dùng trước hạn
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-29
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin gỡ trạng thái xóa mềm cho một tài khoản người dùng đang trong thời gian lưu giữ 30 ngày để đưa tài khoản quay về trạng thái trước khi xóa.
- **Goal**: Phục hồi tài khoản cũ trên chính mã định danh gốc trước khi hết hạn xóa mềm mà không làm mất các ràng buộc bảo mật độc lập của tài khoản.
- **Trigger**: Admin chọn thực hiện khôi phục trước hạn đối với một tài khoản đang ở trạng thái xóa mềm ([UC-ADM-USER-MGMT-02](UC-ADM-USER-MGMT-02-view-user-profile.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin có phiên quản trị hợp lệ và quyền Admin hệ thống; tài khoản được chọn đang ở trạng thái `DELETED`, chưa vượt quá thời hạn 30 ngày theo giờ máy chủ và chưa bị purge.
- **Post-conditions**: Tài khoản thoát khỏi trạng thái `DELETED` trên chính `Account ID` cũ và trở về trạng thái trước khi xóa (`LOCKED` nếu trước đó bị khóa, ngược lại trở về `ACTIVE` theo [DEC-07](../../../../decisions/account-lifecycle.md)); các phiên cũ đã thu hồi và tư cách nhóm đã mất không tự động phục hồi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** gửi yêu cầu khôi phục trước hạn đối với tài khoản đang ở trạng thái xóa mềm.
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống, trạng thái `DELETED`, thời hạn 30 ngày theo giờ máy chủ và đảm bảo tài khoản chưa bước vào tiến trình purge.
3. **Hệ thống** gỡ trạng thái `DELETED` trên đúng định danh tài khoản gốc, trả tài khoản về trạng thái trước khi xóa (`ACTIVE` hoặc `LOCKED`) và giữ nguyên các điều kiện xác thực độc lập (như tình trạng xác minh email hoặc yêu cầu đặt mật khẩu mới).
4. **Hệ thống** thông báo khôi phục thành công kèm trạng thái hiện hành của tài khoản sau khôi phục.

### Alternative Flows

**Alternative flow 1: Tài khoản đã được khôi phục bởi một thao tác quản trị khác**
1. Từ bước #2 của Basic Flow, **Hệ thống** phát hiện tài khoản đã thoát khỏi trạng thái `DELETED` trước đó.
2. **Hệ thống** trả về trạng thái hiện tại của tài khoản mà không sinh tác động dư thừa và đồng bộ lại thông tin cho **Admin**.
3. Kết thúc Use Case.

**Alternative flow 2: Tài khoản sau khôi phục vẫn còn ràng buộc khóa hoặc chờ xác minh**
1. Từ bước #3 của Basic Flow, tài khoản trước khi xóa vốn đang bị khóa (`LOCKED`) hoặc chưa hoàn tất xác minh email.
2. **Hệ thống** khôi phục tài khoản về đúng trạng thái `LOCKED` (hoặc `ACTIVE` nhưng chưa xác minh email) và thông báo rõ các điều kiện còn lại trước khi người dùng có thể đăng nhập sử dụng.
3. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Đã hết thời hạn 30 ngày hoặc tài khoản đã bị purge**
1. Từ bước #2 của Basic Flow, thời điểm hiện tại trên máy chủ đã chạm/vượt hạn 30 ngày hoặc dữ liệu tài khoản đã bị xóa vĩnh viễn.
2. **Hệ thống** từ chối khôi phục (kể cả khi tác vụ dọn dẹp nền chưa kịp chạy), không tạo tài khoản mới thay thế và không tác động vào tài khoản mới nếu `username`/`email` đã được người khác đăng ký lại sau purge.
3. Kết thúc Use Case.

**Exception flow 2: Phiên quản trị hết hiệu lực, thiếu quyền hoặc lỗi kết nối/tranh chấp với tiến trình purge**
1. Từ bước #2 hoặc #3 của Basic Flow, phiên không còn hợp lệ, xảy ra xung đột đồng thời với tiến trình purge hoặc mất kết nối với máy chủ.
2. **Hệ thống** đảm bảo không để xảy ra trạng thái khôi phục dữ liệu đã bị xóa dở dang; từ chối yêu cầu (buộc xác thực lại nếu mất phiên) hoặc yêu cầu truy vấn lại trạng thái thực tế trước khi thử lại.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Chỉ được phép khôi phục khi thời gian máy chủ còn nằm trước mốc hết hạn 30 ngày kể từ lúc xóa mềm ([UC-ADM-USER-MGMT-05](UC-ADM-USER-MGMT-05-soft-delete-user.md)); đồng hồ trên máy trạm Admin không được dùng làm căn cứ xét hạn.
  - **BR-02**: Khôi phục chỉ gỡ trạng thái `DELETED` trên `Account ID` gốc: không tự mở khóa tài khoản vốn đang `LOCKED`, không tự đánh dấu email đã xác minh, không khôi phục các phiên đăng nhập đã thu hồi và không tự trả lại tư cách thành viên hay quyền Admin trong các nhóm chat ([DEC-07](../../../../decisions/account-lifecycle.md)).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Thao tác khôi phục và tiến trình purge tự động phải được đồng bộ loại trừ lẫn nhau trên máy chủ để đảm bảo toàn vẹn dữ liệu tài khoản.
  - **NFR-02**: Phản hồi khôi phục không chứa mật khẩu, mật khẩu tạm hoặc khóa E2EE của người dùng.
