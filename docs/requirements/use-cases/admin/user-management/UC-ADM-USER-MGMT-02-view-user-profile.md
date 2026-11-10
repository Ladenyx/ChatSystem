# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-USER-MGMT-02
- **Use Case Name**: Xem hồ sơ chi tiết người dùng
- **Author / Create by**: ladenyx
- **Date Create**: 2026-11-04
- **Last Updated by / Date**: ladenyx / 2026-11-08
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin xem đầy đủ thông tin hồ sơ, trạng thái tài khoản hiện tại và danh mục các nghiệp vụ quản trị khả dụng đối với một người dùng cụ thể.
- **Goal**: Nắm chính xác thông tin định danh và trạng thái thực tế của tài khoản trước khi thực hiện tác vụ quản trị hoặc tra cứu dữ liệu liên quan.
- **Trigger**: Admin chọn một tài khoản người dùng từ danh sách tra cứu ([UC-ADM-USER-MGMT-01](UC-ADM-USER-MGMT-01-browse-users.md)) hoặc sau khi hoàn tất tạo/cập nhật tài khoản.

## Điều kiện (Conditions)

- **Pre-conditions**: Admin có phiên quản trị hợp lệ và quyền Admin hệ thống; đã xác định mã định danh của tài khoản người dùng cần xem.
- **Post-conditions**: Hồ sơ chi tiết và trạng thái mới nhất tại thời điểm truy vấn của đúng tài khoản được cung cấp cho Admin; dữ liệu hồ sơ và trạng thái tài khoản không bị thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** yêu cầu xem hồ sơ chi tiết của một tài khoản người dùng đã chọn.
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống và sự tồn tại của tài khoản theo mã định danh.
3. **Hệ thống** truy xuất và cung cấp các thuộc tính hồ sơ (`username`, `họ tên`, `địa chỉ`, `ngày sinh`, `giới tính`, `email`), trạng thái vòng đời (`ACTIVE`, `LOCKED`, `DELETED`), tình trạng xác minh email cùng các nghiệp vụ quản trị tương thích với trạng thái hiện tại.
4. **Admin** kiểm tra thông tin hồ sơ hoặc chọn chuyển tiếp sang một nghiệp vụ quản trị liên quan.

### Alternative Flows

**Alternative flow 1: Hồ sơ người dùng bỏ trống các thông tin tùy chọn**
1. Từ bước #3 của Basic Flow, người dùng chưa khai báo `địa chỉ`, `ngày sinh` hoặc `giới tính`.
2. **Hệ thống** thể hiện rõ trường thông tin chưa được cung cấp mà không tự sinh giá trị mặc định hay sửa đổi dữ liệu gốc.
3. Tiếp tục bước #4 trong Basic Flow.

**Alternative flow 2: Chuyển tiếp sang nghiệp vụ quản trị hoặc tra cứu phụ thuộc**
1. Từ bước #4 của Basic Flow, **Admin** chọn thực hiện một tác vụ trên tài khoản đang xem.
2. **Hệ thống** chuyển ngữ cảnh định danh tài khoản sang Use Case tương ứng: cập nhật hồ sơ ([UC-ADM-USER-MGMT-04](UC-ADM-USER-MGMT-04-update-user.md)), xóa mềm ([UC-ADM-USER-MGMT-05](UC-ADM-USER-MGMT-05-soft-delete-user.md)), khôi phục ([UC-ADM-USER-MGMT-06](UC-ADM-USER-MGMT-06-restore-user.md)), khóa ([UC-ADM-USER-MGMT-07](UC-ADM-USER-MGMT-07-lock-account.md)), mở khóa ([UC-ADM-USER-MGMT-08](UC-ADM-USER-MGMT-08-unlock-account.md)), đặt lại mật khẩu ([UC-ADM-USER-MGMT-09](UC-ADM-USER-MGMT-09-reset-user-password.md)), xem lịch sử đăng nhập ([UC-ADM-USER-MGMT-10](UC-ADM-USER-MGMT-10-view-user-login-history.md)) hoặc xem danh sách bạn bè ([UC-ADM-USER-MGMT-11](UC-ADM-USER-MGMT-11-view-user-friends.md)).

### Exception Flows

**Exception flow 1: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #2 của Basic Flow, phiên quản trị không còn hợp lệ hoặc không có quyền Admin hệ thống.
2. **Hệ thống** từ chối truy xuất hồ sơ và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 2: Tài khoản không còn tồn tại (đã purge)**
1. Từ bước #2 của Basic Flow, tài khoản tương ứng với mã định danh đã bị xóa vĩnh viễn sau thời hạn lưu giữ.
2. **Hệ thống** thông báo tài khoản không còn tồn tại, xóa ngữ cảnh đang chọn và không tự chuyển sang tài khoản mới dù trùng `username`.
3. Kết thúc Use Case.

**Exception flow 3: Lỗi kết nối hoặc không tải được hồ sơ**
1. Từ bước #2 của Basic Flow, quá trình truy vấn hồ sơ gặp sự cố kết nối hoặc lỗi máy chủ.
2. **Hệ thống** thông báo lỗi truy xuất, không giữ lại dữ liệu của tài khoản được xem trước đó dưới tên tài khoản mới và cho phép thử tải lại.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Đây là nghiệp vụ chỉ đọc; tập thao tác quản trị được phép khởi tạo phụ thuộc vào trạng thái hiện tại của tài khoản theo [DEC-07](../../../../decisions/account-lifecycle.md) và vẫn phải được máy chủ kiểm tra lại khi thực thi.
  - **BR-02**: Hệ thống tuyệt đối không cung cấp mật khẩu hiện tại, mật khẩu tạm, khóa bí mật hay mã khôi phục E2EE của người dùng cho Admin.
  - **BR-03**: Tài khoản ở trạng thái xóa mềm (`DELETED`) còn trong hạn 30 ngày vẫn cho phép xem hồ sơ và thời hạn để phục vụ quyết định khôi phục trước hạn.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Khi **Admin** chuyển lựa chọn giữa nhiều người dùng liên tiếp, dữ liệu phản hồi muộn của tài khoản cũ không được ghi đè thông tin của tài khoản đang chọn.
  - **NFR-02**: Ngày sinh và các mốc thời gian liên quan của tài khoản được hiển thị nhất quán theo múi giờ `Asia/Ho_Chi_Minh`.
