# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-USER-MGMT-09
- **Use Case Name**: Đặt lại mật khẩu người dùng qua email
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-29
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: Dịch vụ gửi email (Email Service)

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin khởi tạo yêu cầu cấp mật khẩu tạm ngẫu nhiên qua email cho một tài khoản người dùng hiện có để chủ tài khoản tự đăng nhập vào quy trình đặt mật khẩu mới bắt buộc.
- **Goal**: Hỗ trợ người dùng khôi phục quyền xác thực qua email mà không để Admin biết hoặc tự đặt mật khẩu lâu dài của người dùng.
- **Trigger**: Admin chọn thực hiện đặt lại mật khẩu qua email từ thông tin chi tiết người dùng ([UC-ADM-USER-MGMT-02](UC-ADM-USER-MGMT-02-view-user-profile.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin có phiên quản trị hợp lệ và quyền Admin hệ thống; tài khoản được chọn là tài khoản `USER` đang tồn tại, không ở trạng thái `DELETED`, có địa chỉ email hợp lệ và dịch vụ gửi email đã được cấu hình.
- **Post-conditions**: Mật khẩu tạm ngẫu nhiên dùng một lần (hiệu lực 15 phút) được phát hành và chuyển giao cho dịch vụ gửi email; mật khẩu hiện tại và các phiên đang hoạt động của người dùng vẫn được giữ nguyên cho đến khi người dùng hoàn tất lưu mật khẩu mới.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** khởi tạo yêu cầu đặt lại mật khẩu qua email cho tài khoản người dùng đang chọn.
2. **Hệ thống** yêu cầu **Admin** kiểm tra và xác nhận đúng định danh tài khoản cùng địa chỉ email nhận mật khẩu tạm.
3. **Admin** xác nhận gửi yêu cầu đặt lại mật khẩu.
4. **Hệ thống** kiểm tra lại phiên, quyền Admin hệ thống, sự tồn tại/trạng thái hiện thời của tài khoản, tính khớp của địa chỉ email hiện tại và giới hạn tần suất gửi yêu cầu.
5. **Hệ thống** sinh mật khẩu tạm ngẫu nhiên dùng một lần có thời hạn 15 phút và yêu cầu **Dịch vụ gửi email** gửi tới hộp thư của tài khoản (không trả mật khẩu tạm về cho **Admin**).
6. **Hệ thống** thông báo dịch vụ gửi email đã tiếp nhận yêu cầu chuyển phát mật khẩu tạm.

### Alternative Flows

**Alternative flow 1: Admin hủy bỏ xác nhận đặt lại mật khẩu**
1. Từ bước #2 của Basic Flow, **Admin** từ chối xác nhận thao tác.
2. **Hệ thống** hủy yêu cầu, không phát hành mật khẩu tạm và giữ nguyên thông tin xác thực của tài khoản.
3. Kết thúc Use Case.

**Alternative flow 2: Tài khoản người dùng hiện đang bị khóa (`LOCKED`)**
1. Từ bước #4 của Basic Flow, tài khoản đang ở trạng thái `LOCKED`.
2. Việc đặt lại mật khẩu không tự động gỡ trạng thái `LOCKED`; theo [DEC-06](../../../../decisions/password-reset-and-email.md) và [DEC-07](../../../../decisions/account-lifecycle.md), tài khoản bị khóa vẫn bị chặn sử dụng mật khẩu tạm cho đến khi được mở khóa riêng theo [UC-ADM-USER-MGMT-08](UC-ADM-USER-MGMT-08-unlock-account.md).
3. Tiếp tục bước #5 trong Basic Flow (hoặc từ chối gửi nếu chính sách máy chủ chặn phát hành cho tài khoản đang khóa, kèm thông báo rõ trạng thái `LOCKED`).

### Exception Flows

**Exception flow 1: Vượt giới hạn giãn cách hoặc hạn mức yêu cầu**
1. Từ bước #4 của Basic Flow, tài khoản vừa được gửi mật khẩu tạm chưa đủ thời gian giãn cách (60 giây) hoặc đã vượt số lần tối đa (5 yêu cầu/giờ/tài khoản).
2. **Hệ thống** từ chối sinh mật khẩu tạm mới và thông báo thời gian cần chờ theo quy định của máy chủ.
3. Kết thúc Use Case.

**Exception flow 2: Tài khoản đã xóa hoặc địa chỉ email vừa thay đổi đồng thời**
1. Từ bước #4 của Basic Flow, tài khoản đã chuyển sang `DELETED`/purge hoặc địa chỉ email hiện tại trên máy chủ không còn khớp với địa chỉ mà **Admin** vừa xác nhận ở bước #2.
2. **Hệ thống** dừng thao tác gửi, không chuyển thư tới địa chỉ cũ và tải lại hồ sơ mới nhất để **Admin** kiểm tra lại.
3. Kết thúc Use Case.

**Exception flow 3: Dịch vụ gửi email báo lỗi hoặc mất kết nối**
1. Từ bước #5 của Basic Flow, **Dịch vụ gửi email** từ chối yêu cầu hoặc quá trình kết nối bị gián đoạn.
2. **Hệ thống** thông báo chưa gửi được email (hoặc kết quả chưa xác định khi mất phản hồi), giữ nguyên mật khẩu hiện tại lẫn các phiên đang hoạt động của người dùng để không làm gián đoạn quyền truy cập cũ.
3. Kết thúc Use Case.

**Exception flow 4: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #4 của Basic Flow, phiên quản trị không còn hợp lệ hoặc thiếu quyền Admin hệ thống.
2. **Hệ thống** từ chối yêu cầu và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Admin không được nhập hộ mật khẩu mới và không được xem mật khẩu tạm đã sinh.
  - **BR-02**: Theo [DEC-06](../../../../decisions/password-reset-and-email.md), mật khẩu tạm chỉ dùng một lần, hết hạn sau **15 phút kể từ khi máy chủ phát hành**, áp dụng giãn cách **60 giây** và tối đa **5 yêu cầu/giờ/tài khoản** (tính theo giờ máy chủ).
  - **BR-03**: Mật khẩu tạm chỉ cấp quyền giới hạn để người dùng tự đặt mật khẩu mới, chưa cấp phiên sử dụng chat. Mật khẩu cũ và các phiên đang hoạt động chỉ bị vô hiệu hóa/thu hồi sau khi người dùng lưu mật khẩu mới thành công.
  - **BR-04**: Đặt lại mật khẩu không mở khóa tài khoản `LOCKED` và không thay đổi hay giải mã mã khôi phục / bản sao lưu khóa E2EE ([DEC-05](../../../../decisions/group-permissions-and-e2ee.md)).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Tuyệt đối không ghi mật khẩu hiện tại hoặc mật khẩu tạm ở dạng bản rõ vào nhật ký hệ thống hay thông báo lỗi.
  - **NFR-02**: Hệ thống phải phân biệt rõ giữa việc dịch vụ gửi email đã tiếp nhận yêu cầu với lỗi gửi thư hoặc trạng thái mất kết nối chưa xác định.
