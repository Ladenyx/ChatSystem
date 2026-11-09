# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-AUTH-01
- **Use Case Name**: Đăng nhập vào phân hệ Quản trị
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-29
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin hệ thống xác thực bằng tài khoản đã được khởi tạo sẵn từ phía máy chủ để thiết lập phiên làm việc quản trị.
- **Goal**: Thiết lập phiên quản trị hợp lệ và truy cập không gian điều hành của Admin App.
- **Trigger**: Admin khởi tạo yêu cầu đăng nhập vào Admin App.

## Điều kiện (Conditions)

- **Pre-conditions**: Tài khoản Admin hệ thống đã được khởi tạo trên hệ thống; thiết bị hiện tại chưa có phiên quản trị hoạt động.
- **Post-conditions**: Một phiên quản trị hợp lệ được cấp cho Admin; phiên quản trị cũ của cùng tài khoản (nếu có) bị thu hồi ngay lập tức; các phiên User Client của tài khoản không bị ảnh hưởng.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** cung cấp tên đăng nhập, mật khẩu và gửi yêu cầu đăng nhập.
2. **Hệ thống** kiểm tra tính hợp lệ đầu vào, đối chiếu thông tin xác thực, vai trò Admin hệ thống và điều kiện trạng thái của tài khoản.
3. **Hệ thống** đặt lại bộ đếm thử sai liên tiếp về `0`, cấp phiên quản trị mới và đưa **Admin** vào màn hình tổng quan quản trị.

### Alternative Flows

**Alternative flow 1: Tài khoản đang có phiên quản trị hoạt động ở nơi khác**
1. Từ bước #2 của Basic Flow, **Hệ thống** phát hiện tài khoản đã có một phiên quản trị đang hoạt động ở cửa sổ hoặc thiết bị khác.
2. **Hệ thống** thu hồi trực tiếp phiên quản trị cũ (vô hiệu hóa cả yêu cầu nghiệp vụ lẫn kết nối thời gian thực đang mở của phiên cũ) mà không ảnh hưởng các phiên User Client.
3. Tiếp tục bước #3 trong Basic Flow để cấp phiên quản trị mới.

**Alternative flow 2: Sai thông tin xác thực nhưng chưa vượt ngưỡng khóa tạm**
1. Từ bước #2 của Basic Flow, tên đăng nhập hoặc mật khẩu không chính xác và số lần thử sai liên tiếp chưa vượt ngưỡng `N` theo cấu hình.
2. **Hệ thống** tăng bộ đếm đăng nhập sai liên tiếp (nếu tài khoản tồn tại), từ chối xác thực và trả thông báo lỗi chung mà không tiết lộ sự tồn tại của tài khoản.
3. Quay lại bước #1 trong Basic Flow.

### Exception Flows

**Exception flow 1: Nhập sai mật khẩu vượt ngưỡng cho phép (khóa đăng nhập tạm thời)**
1. Từ bước #2 của Basic Flow, số lần thử sai liên tiếp vượt ngưỡng `N` quy định.
2. **Hệ thống** áp dụng thời hạn khóa đăng nhập tạm thời, ghi nhận sự kiện bảo mật và từ chối yêu cầu kèm thời gian phải chờ.
3. Kết thúc Use Case.

**Exception flow 2: Tài khoản không có quyền Admin hệ thống hoặc trạng thái không được phép đăng nhập**
1. Từ bước #2 của Basic Flow, thông tin xác thực khớp nhưng tài khoản không mang vai trò Admin hệ thống (tài khoản `USER` hoặc chỉ là Admin nhóm chat), hoặc tài khoản đang ở trạng thái bị khóa (`LOCKED`) / xóa mềm (`DELETED`).
2. **Hệ thống** từ chối cấp phiên quản trị và thông báo lý do không được phép truy cập.
3. Kết thúc Use Case.

**Exception flow 3: Mất kết nối hoặc máy chủ không phản hồi**
1. Từ bước #1 hoặc #2 của Basic Flow, quá trình gửi yêu cầu hoặc nhận phản hồi vượt quá thời gian chờ kết nối.
2. **Hệ thống** hủy yêu cầu đang chờ, không thiết lập phiên quản trị và thông báo chưa thể xác nhận đăng nhập để **Admin** chủ động thử lại.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Chỉ tài khoản mang vai trò Admin hệ thống mới được truy cập Admin App; quyền Admin nhóm chat không thay thế quyền này. Hệ thống không hỗ trợ đăng ký tài khoản Admin công khai.
  - **BR-02**: Tài khoản Admin hệ thống chỉ được khởi tạo trực tiếp qua kịch bản cơ sở dữ liệu / máy chủ, không dùng mật khẩu mặc định cố định trong mã nguồn.
  - **BR-03**: Mỗi tài khoản chỉ có tối đa 01 phiên quản trị hoạt động tại một thời điểm; cơ chế tự kết nối lại từ phiên cũ đã bị thu hồi không giành lại được phiên. Các phiên User Client được quản lý độc lập theo [DEC-07](../../../../decisions/account-lifecycle.md).
  - **BR-04**: Hạn chế khóa tạm do thử sai được kiểm tra độc lập với trạng thái tài khoản; hết thời gian khóa tạm không tự mở khóa quản trị (`LOCKED`) hay phục hồi tài khoản đã xóa (`DELETED`).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Thông tin xác thực và mã phiên phải được truyền qua kênh bảo vệ; không ghi mật khẩu hoặc mã phiên vào nhật ký hệ thống, thông báo lỗi hay tệp cấu hình.
  - **NFR-02**: Mọi yêu cầu nghiệp vụ sau khi đăng nhập đều phải được máy chủ kiểm tra lại hiệu lực phiên và quyền Admin hệ thống trước khi thực thi.
