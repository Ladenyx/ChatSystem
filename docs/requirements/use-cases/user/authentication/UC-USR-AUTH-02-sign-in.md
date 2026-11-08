# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-AUTH-02
- **Use Case Name**: Đăng nhập vào User Client
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-01
- **Last Updated by / Date**: baonguyen1776 / 2026-11-08
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng xác thực bằng tên đăng nhập và mật khẩu qua User Client để thiết lập phiên làm việc sử dụng ứng dụng, hoặc nhận quyền chuyển tiếp sang bước xác minh email / đặt mật khẩu mới bắt buộc.
- **Goal**: Thiết lập một phiên User Client hợp lệ và truy cập không gian hội thoại chính (`U-SCR-07`).
- **Trigger**: Người dùng gửi yêu cầu đăng nhập từ giao diện User Client.

## Điều kiện (Conditions)

- **Pre-conditions**: Ứng dụng User Client đã kết nối được với máy chủ; tài khoản người dùng đã tồn tại trong hệ thống.
- **Post-conditions**: Một phiên User Client độc lập được thiết lập và đưa người dùng vào khung chính với `U-SCR-07`; các phiên User Client khác và phiên quản trị đang có của tài khoản không bị ảnh hưởng. Nếu thuộc diện cần xác minh email hoặc đổi mật khẩu tạm, tài khoản nhận quyền chuyển tiếp tương ứng mà chưa được cấp phiên chat.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** cung cấp tên đăng nhập, mật khẩu và gửi yêu cầu đăng nhập.
2. **Hệ thống** kiểm tra tính hợp lệ dữ liệu nhập, đối chiếu thông tin xác thực, vai trò tài khoản và các điều kiện trạng thái hoạt động theo quy định.
3. **Hệ thống** đặt lại bộ đếm số lần thử sai liên tiếp về `0`, cấp một phiên User Client mới độc lập và điều hướng **Người dùng** vào màn hình chính hội thoại (`U-SCR-07`).

### Alternative Flows

**Alternative flow 1: Tài khoản đã có phiên User Client khác đang hoạt động**
1. Từ bước #2 của Basic Flow, **Hệ thống** phát hiện tài khoản đã có một hoặc nhiều phiên User Client đang hoạt động trên cùng máy hoặc thiết bị khác.
2. **Hệ thống** vẫn cấp một phiên làm việc mới hoàn toàn độc lập mà không thu hồi các phiên đang có, không yêu cầu thiết bị cũ phê duyệt và không ảnh hưởng đến phiên quản trị (nếu có).
3. Tiếp tục bước #3 trong Basic Flow.

**Alternative flow 2: Tài khoản do Admin tạo chưa xác minh email**
1. Từ bước #2 của Basic Flow, thông tin xác thực hợp lệ nhưng tài khoản được khởi tạo bởi Admin hệ thống và chưa hoàn tất xác minh email.
2. **Hệ thống** từ chối cấp phiên User Client, cấp quyền tạm thời hạn 10 phút và chuyển hướng **Người dùng** sang màn hình xác minh email (`U-SCR-03`) theo [DEC-06](../../../../decisions/password-reset-and-email.md).
3. Kết thúc Use Case.

**Alternative flow 3: Đăng nhập bằng mật khẩu tạm còn hiệu lực**
1. Từ bước #2 của Basic Flow, tài khoản đã xác minh email, không bị khóa/xóa và thông tin xác thực khớp với mật khẩu tạm được cấp (do khôi phục mật khẩu hoặc Admin reset).
2. **Hệ thống** từ chối cấp phiên User Client, cấp quyền tạm có thời hạn và chuyển **Người dùng** sang màn hình bắt buộc đặt mật khẩu mới (`U-SCR-05`) theo [DEC-06](../../../../decisions/password-reset-and-email.md).
3. Kết thúc Use Case.

**Alternative flow 4: Người dùng chuyển sang màn hình Đăng ký hoặc Quên mật khẩu**
1. Tại bước #1 của Basic Flow, **Người dùng** lựa chọn chuyển hướng sang luồng Đăng ký tài khoản (`U-SCR-02`) hoặc Quên mật khẩu (`U-SCR-04`).
2. **Hệ thống** chuyển sang Use Case tương ứng mà không lưu giữ hay chuyển tiếp mật khẩu đang nhập sang màn hình mới.
3. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Thiếu thông tin đăng nhập bắt buộc**
1. Tại bước #1 của Basic Flow, **Người dùng** để trống tên đăng nhập hoặc mật khẩu khi gửi yêu cầu.
2. **Hệ thống** từ chối gửi yêu cầu tới máy chủ và phản hồi thông báo trường dữ liệu còn thiếu để **Người dùng** bổ sung.
3. Quay lại bước #1 trong Basic Flow.

**Exception flow 2: Sai thông tin xác thực nhưng chưa vượt ngưỡng khóa tạm**
1. Từ bước #2 của Basic Flow, tên đăng nhập hoặc mật khẩu không chính xác và số lần thử sai liên tiếp chưa vượt ngưỡng quy định.
2. **Hệ thống** tăng bộ đếm số lần đăng nhập sai (nếu tài khoản tồn tại), từ chối xác thực và trả về thông báo lỗi chung mà không tiết lộ sự tồn tại của tài khoản trong hệ thống.
3. Quay lại bước #1 trong Basic Flow.

**Exception flow 3: Bị khóa đăng nhập tạm thời do vượt ngưỡng thử sai**
1. Từ bước #2 của Basic Flow, số lần thử sai liên tiếp của tài khoản vượt ngưỡng `N` quy định theo [DEC-07](../../../../decisions/account-lifecycle.md).
2. **Hệ thống** áp dụng thời hạn khóa đăng nhập tạm thời, từ chối mọi yêu cầu xác thực hoặc chuyển tiếp và thông báo khoảng thời gian phải chờ.
3. Kết thúc Use Case.

**Exception flow 4: Tài khoản bị khóa quản trị hoặc đã bị xóa mềm**
1. Từ bước #2 của Basic Flow, thông tin xác thực chính xác nhưng tài khoản đang ở trạng thái bị khóa (`LOCKED`) hoặc đã bị xóa mềm (`DELETED`).
2. **Hệ thống** từ chối cấp phiên làm việc cũng như quyền chuyển tiếp, đồng thời thu hồi ngay lập tức mọi phiên đang hoạt động của tài khoản theo [DEC-07](../../../../decisions/account-lifecycle.md).
3. Kết thúc Use Case.

**Exception flow 5: Mật khẩu tạm không hợp lệ hoặc đã hết hạn**
1. Từ bước #2 của Basic Flow, người dùng đăng nhập bằng mật khẩu tạm đã hết hạn sử dụng hoặc đã bị vô hiệu hóa trước đó.
2. **Hệ thống** từ chối cấp quyền chuyển tiếp; nếu nhận diện được ngữ cảnh khôi phục thì hướng dẫn **Người dùng** quay về `U-SCR-04` để yêu cầu mã mới, ngược lại trả thông báo lỗi xác thực chung.
3. Kết thúc Use Case.

**Exception flow 6: Mất kết nối hoặc máy chủ không phản hồi (Timeout)**
1. Từ bước #1 hoặc #2 của Basic Flow, quá trình gửi yêu cầu hoặc chờ máy chủ phản hồi vượt quá thời gian timeout quy định.
2. **Hệ thống** hủy yêu cầu đang chờ, không thiết lập phiên và thông báo lỗi kết nối để **Người dùng** chủ động kiểm tra mạng và thử lại.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Tên đăng nhập được chuẩn hóa trước khi đối chiếu (loại bỏ khoảng trắng thừa hai đầu); mật khẩu giữ nguyên phân biệt chữ hoa, chữ thường và ký tự đặc biệt theo [DEC-07](../../../../decisions/account-lifecycle.md).
  - **BR-02**: Tài khoản thuộc bất kỳ vai trò nào (`USER`, `ADMIN`) đều được phép đăng nhập vào User Client nếu thỏa mãn điều kiện trạng thái; phiên User Client chỉ cấp quyền sử dụng các tính năng người dùng và không thể thực thi các tác vụ quản trị.
  - **BR-03**: Hệ thống cho phép nhiều phiên User Client hoạt động đồng thời trên cùng tài khoản. Đăng nhập phiên User Client mới không làm mất phiên cũ hay phiên quản trị đang mở.
  - **BR-04**: Trạng thái khóa quản trị (`LOCKED`) và xóa mềm (`DELETED`) có mức ưu tiên cao nhất: chặn cả phiên đăng nhập lẫn quyền chuyển tiếp và thu hồi toàn bộ phiên đang chạy. Trạng thái khóa tạm do thử sai chỉ chặn cấp phiên mới trong thời hạn phạt, không tự thu hồi các phiên đang hoạt động bình thường.
  - **BR-05**: Hết thời hạn khóa tạm do thử sai không tự động mở khóa cho tài khoản đang bị khóa quản trị hoặc phục hồi tài khoản đã xóa.
  - **BR-06**: Việc thiết lập và khôi phục khóa mã hóa E2EE không phải là điều kiện ràng buộc để đăng nhập thành công. Người dùng có thể bỏ qua hoặc hoãn khôi phục E2EE để vào giao diện chính; thao tác này không làm kết thúc phiên đăng nhập theo [DEC-05](../../../../decisions/group-permissions-and-e2ee.md).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Thông tin xác thực và mã phiên phải được truyền tải qua kênh bảo mật mã hóa; không bao giờ lưu mật khẩu, mã phiên hoặc khóa E2EE vào log hệ thống, URL hay thông báo lỗi.
  - **NFR-02**: Giao diện phải phản hồi trạng thái xử lý rõ ràng, tạm thời khóa thao tác gửi lặp trong lúc chờ máy chủ phản hồi để tránh gửi yêu cầu trùng lặp.
  - **NFR-03**: Thời gian phản hồi xử lý đăng nhập từ máy chủ không vượt quá 2 giây trong điều kiện kết nối mạng tiêu chuẩn.