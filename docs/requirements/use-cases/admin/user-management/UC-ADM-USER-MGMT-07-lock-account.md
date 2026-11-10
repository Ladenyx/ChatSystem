# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-USER-MGMT-07
- **Use Case Name**: Khóa tài khoản người dùng
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-29
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin thực hiện khóa quản trị đối với một tài khoản người dùng để đình chỉ ngay quyền truy cập và thu hồi toàn bộ phiên đang hoạt động mà không xóa hồ sơ của người dùng.
- **Goal**: Chấm dứt khả năng đăng nhập và sử dụng hệ thống của tài khoản người dùng được chọn.
- **Trigger**: Admin chọn thực hiện khóa tài khoản từ thông tin chi tiết người dùng ([UC-ADM-USER-MGMT-02](UC-ADM-USER-MGMT-02-view-user-profile.md)) hoặc được ủy quyền gọi từ nghiệp vụ xử lý báo cáo spam ([UC-ADM-SPAM-03](../spam-moderation/UC-ADM-SPAM-03-lock-reported-account.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin có phiên quản trị hợp lệ và quyền Admin hệ thống; tài khoản được chọn là tài khoản `USER` đang ở trạng thái `ACTIVE` (chưa bị khóa và chưa bị xóa mềm/purge).
- **Post-conditions**: Tài khoản chuyển sang trạng thái `LOCKED`; mọi phiên đang hoạt động của tài khoản đó bị thu hồi ngay lập tức và mọi yêu cầu đăng nhập hoặc cấp quyền chuyển tiếp tiếp theo của tài khoản đều bị từ chối.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** khởi tạo yêu cầu khóa đối với tài khoản người dùng đã chọn.
2. **Hệ thống** yêu cầu **Admin** xác nhận rõ tài khoản chịu tác động và hệ quả thu hồi ngay các phiên đang hoạt động.
3. **Admin** xác nhận thực hiện khóa tài khoản.
4. **Hệ thống** kiểm tra lại phiên, quyền Admin hệ thống và trạng thái hiện tại của tài khoản theo mã định danh.
5. **Hệ thống** chuyển trạng thái tài khoản sang `LOCKED`, đồng thời thu hồi toàn bộ phiên đang hoạt động (bao gồm cả kết nối thời gian thực) và từ chối mọi yêu cầu xác thực mới của tài khoản.
6. **Hệ thống** thông báo khóa thành công và cập nhật thông tin trạng thái của tài khoản.

### Alternative Flows

**Alternative flow 1: Admin hủy bỏ xác nhận khóa**
1. Từ bước #2 của Basic Flow, **Admin** từ chối xác nhận thao tác.
2. **Hệ thống** hủy yêu cầu và giữ nguyên trạng thái tài khoản cùng các phiên hiện có của người dùng.
3. Kết thúc Use Case.

**Alternative flow 2: Tài khoản đã bị khóa bởi một thao tác khác**
1. Từ bước #4 của Basic Flow, **Hệ thống** phát hiện tài khoản đã ở trạng thái `LOCKED`.
2. **Hệ thống** trả về trạng thái `LOCKED` hiện tại mà không sinh tác động lặp và đồng bộ lại thông tin cho **Admin**.
3. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #4 của Basic Flow, phiên quản trị không còn hợp lệ hoặc không có quyền Admin hệ thống.
2. **Hệ thống** từ chối yêu cầu khóa và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 2: Đối tượng tài khoản không còn hợp lệ để khóa**
1. Từ bước #4 của Basic Flow, tài khoản đã chuyển sang xóa mềm (`DELETED`), đã bị purge hoặc không thuộc phạm vi tài khoản `USER` được phép khóa.
2. **Hệ thống** từ chối thao tác khóa, thông báo trạng thái thực tế của tài khoản và cập nhật lại hồ sơ.
3. Kết thúc Use Case.

**Exception flow 3: Lỗi hệ thống hoặc mất kết nối khi áp dụng khóa**
1. Từ bước #4 hoặc #5 của Basic Flow, xảy ra lỗi ghi nhận trạng thái/thu hồi phiên hoặc mất phản hồi kết nối.
2. **Hệ thống** không thông báo thành công khi chưa xác nhận hiệu lực khóa và thu hồi phiên; đảm bảo không chấp nhận phiên của tài khoản đã khóa và yêu cầu đối chiếu lại trạng thái thực tế trước khi thử lại.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Mọi thao tác khóa tài khoản đều bắt buộc có xác nhận từ Admin và được kiểm tra quyền Admin hệ thống trên máy chủ tại thời điểm ghi.
  - **BR-02**: Khi tài khoản ở trạng thái `LOCKED`, hệ thống chặn cấp cả phiên sử dụng ứng dụng lẫn quyền chuyển tiếp xác minh email / đặt mật khẩu mới (kể cả khi mật khẩu tạm còn hạn), đồng thời ngăn phiên cũ tự kết nối lại để giành lại quyền truy cập theo [DEC-07](../../../../decisions/account-lifecycle.md).
  - **BR-03**: Khóa tài khoản chỉ đình chỉ quyền truy cập (`ACTIVE` → `LOCKED`); không xóa hồ sơ, không đặt lại mật khẩu và không tự động xóa người dùng khỏi các nhóm chat. Để gỡ khóa phải thực hiện riêng theo [UC-ADM-USER-MGMT-08](UC-ADM-USER-MGMT-08-unlock-account.md).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Quá trình chuyển trạng thái `LOCKED` và vô hiệu hóa phiên phải nhất quán trước các yêu cầu đồng thời để phiên cũ không thể tiếp tục gửi/nhận dữ liệu sau khi khóa có hiệu lực.
  - **NFR-02**: Không bao gồm mật khẩu hoặc khóa E2EE trong dữ liệu yêu cầu, phản hồi hay nhật ký lỗi của nghiệp vụ khóa.
