# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-SEC-01
- **Use Case Name**: Đổi mật khẩu tài khoản
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-05
- **Last Updated by / Date**: baonguyen1776 / 2026-11-08
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng xác nhận mật khẩu hiện tại và chủ động thiết lập mật khẩu mới cho tài khoản đang đăng nhập trong hộp thoại Cài đặt.
- **Goal**: Cập nhật mật khẩu mới an toàn cho tài khoản; duy trì phiên hiện tại và đăng xuất các phiên khác trên thiết bị khác để bảo vệ tài khoản.
- **Trigger**: Người dùng chọn tab Đổi mật khẩu trong hộp thoại Hồ sơ & Bảo mật (`U-DLG-01`).

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng đã đăng nhập bằng một phiên User Client hợp lệ; tài khoản ở trạng thái hoạt động (`ACTIVE`) và người dùng nhớ mật khẩu hiện tại.
- **Post-conditions**:
  - **Thành công**: Mật khẩu mới được lưu thành công trên máy chủ; mật khẩu cũ bị vô hiệu hóa; phiên User Client hiện tại được giữ nguyên, toàn bộ các phiên User Client khác trên các thiết bị khác bị thu hồi.
  - **Thất bại hoặc hủy**: Nếu máy chủ chưa xác nhận lưu thành công, mật khẩu cũ và mọi phiên hoạt động được giữ nguyên vẹn.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** mở tab Đổi mật khẩu trong hộp thoại Hồ sơ & Bảo mật (`U-DLG-01`).
2. **Người dùng** cung cấp mật khẩu hiện tại, nhập mật khẩu mới, xác nhận lại mật khẩu mới và gửi yêu cầu lưu.
3. **Hệ thống** kiểm tra tính hợp lệ đầu vào: các trường không được để trống, mật khẩu mới tuân thủ quy tắc độ phức tạp và hai ô mật khẩu mới phải khớp nhau.
4. **Hệ thống** đối chiếu mật khẩu hiện tại với cơ sở dữ liệu và kiểm tra trạng thái tài khoản.
5. **Hệ thống** lưu mật khẩu mới, duy trì phiên User Client đang thao tác, đồng thời thu hồi toàn bộ các phiên hoạt động khác của tài khoản trên các thiết bị khác và thông báo đổi mật khẩu thành công cho **Người dùng**.

### Alternative Flows

**Alternative flow 1: Người dùng hủy thao tác hoặc đóng hộp thoại**
1. Tại bước #2 của Basic Flow, **Người dùng** đóng hộp thoại hoặc chọn hủy thao tác.
2. **Hệ thống** hủy dữ liệu đang nhập trên giao diện, giữ nguyên mật khẩu cũ và duy trì bình thường các phiên đang chạy.
3. Kết thúc Use Case.

**Alternative flow 2: Người dùng quên mật khẩu hiện tại**
1. Tại bước #1 hoặc #2 của Basic Flow, **Người dùng** không nhớ mật khẩu hiện tại để thực hiện đổi mật khẩu.
2. **Người dùng** rời hộp thoại và chuyển sang quy trình Khôi phục mật khẩu (`UC-USR-AUTH-03`).
3. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Mật khẩu hiện tại không chính xác**
1. Từ bước #4 của Basic Flow, mật khẩu hiện tại người dùng nhập không khớp với mật khẩu đang lưu trên máy chủ.
2. **Hệ thống** từ chối lưu mật khẩu mới, giữ nguyên mật khẩu cũ và hiển thị thông báo lỗi để người dùng kiểm tra lại.
3. Quay lại bước #2 trong Basic Flow.

**Exception flow 2: Mật khẩu mới không đạt chuẩn an toàn hoặc không khớp**
1. Tại bước #3 của Basic Flow, mật khẩu mới không đạt độ dài 8–128 ký tự, thiếu các thành phần bắt buộc (chữ hoa, chữ thường, số, ký tự đặc biệt) hoặc hai ô mật khẩu mới không khớp nhau.
2. **Hệ thống** từ chối gửi yêu cầu tới máy chủ và hiển thị chi tiết các điều kiện chưa đạt dưới các ô nhập tương ứng.
3. Quay lại bước #2 trong Basic Flow.

**Exception flow 3: Phiên làm việc hết hạn hoặc bị thu hồi**
1. Từ bước #4 của Basic Flow, phiên User Client hiện tại đã hết hạn hoặc tài khoản bị khóa quản trị.
2. **Hệ thống** từ chối yêu cầu đổi mật khẩu và chuyển người dùng về màn hình đăng nhập.
3. Kết thúc Use Case.

**Exception flow 4: Mất kết nối mạng hoặc lỗi máy chủ (Timeout)**
1. Quá trình gửi yêu cầu hoặc nhận phản hồi vượt quá thời gian timeout quy định.
2. **Hệ thống** hủy yêu cầu đang chờ, không xác nhận trạng thái và thông báo lỗi kết nối để người dùng thử lại.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Máy chủ bắt buộc phải xác thực chính xác mật khẩu hiện tại trước khi cho phép cập nhật mật khẩu mới.
  - **BR-02**: Mật khẩu mới bắt buộc phải tuân theo tiêu chuẩn an toàn: độ dài từ 8 đến 128 ký tự, có ít nhất một chữ hoa, một chữ thường, một chữ số và một ký tự đặc biệt theo quy định tại [DEC-07](../../../../decisions/account-lifecycle.md).
  - **BR-03**: Chính sách phiên khi đổi mật khẩu chủ động: Sau khi lưu mật khẩu mới thành công, hệ thống **giữ nguyên phiên User Client hiện tại** và **thu hồi toàn bộ các phiên User Client khác trên các thiết bị khác** của cùng tài khoản theo [DEC-06](../../../../decisions/password-reset-and-email.md) và [DEC-07](../../../../decisions/account-lifecycle.md).
  - **BR-04**: Thao tác đổi mật khẩu tài khoản hoàn toàn độc lập với luồng khóa mã hóa E2EE, không làm thay đổi hay khôi phục khóa E2EE của nhóm chat.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Mật khẩu hiện tại và mật khẩu mới phải được truyền tải qua kết nối bảo mật mã hóa; không bao giờ lưu mật khẩu dưới dạng văn bản rõ (plain-text) trong nhật ký hệ thống.
  - **NFR-02**: Giao diện phải hỗ trợ nút hiện/ẩn mật khẩu và vô hiệu hóa nút gửi trong khi đang chờ phản hồi từ máy chủ.
