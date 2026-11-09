# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-PROF-01
- **Use Case Name**: Cập nhật hồ sơ tài khoản
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-05
- **Last Updated by / Date**: baonguyen1776 / 2026-11-08
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Dịch vụ gửi email (Email Service)

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng xem và cập nhật thông tin hồ sơ cá nhân (họ tên, ngày sinh, giới tính, địa chỉ) hoặc thực hiện đổi địa chỉ email liên kết với tài khoản qua bước xác thực mật khẩu và xác minh email mới.
- **Goal**: Lưu thông tin cá nhân mới hợp lệ hoặc cập nhật thành công địa chỉ email đã được xác minh trên hệ thống.
- **Trigger**: Người dùng mở mục Hồ sơ & Bảo mật (`U-DLG-01`) từ thanh điều hướng chính của User Client.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng đã đăng nhập bằng phiên User Client hợp lệ; tài khoản đang ở trạng thái hoạt động (`ACTIVE`).
- **Post-conditions**:
  - **Lưu hồ sơ thành công**: Thông tin cá nhân hợp lệ được lưu vào cơ sở dữ liệu máy chủ và cập nhật trên giao diện người dùng.
  - **Đổi email thành công**: Địa chỉ email mới được cập nhật trên tài khoản sau khi hoàn tất xác minh mã; email cũ bị thay thế; các thông tin cá nhân khác không bị ảnh hưởng.
  - **Thao tác chưa hoàn tất**: Nếu người dùng hủy hoặc xảy ra lỗi, thông tin cũ và email cũ được giữ nguyên vẹn.

## Luồng sự kiện (Flow of Events)

### Basic Flow — Cập nhật thông tin cá nhân

1. **Người dùng** mở tab Hồ sơ tài khoản trong hộp thoại Hồ sơ & Bảo mật (`U-DLG-01`), xem thông tin hiện tại (tên đăng nhập hiển thị chỉ đọc).
2. **Người dùng** chỉnh sửa một hoặc nhiều thông tin cá nhân gồm họ tên, ngày sinh, giới tính, địa chỉ và gửi yêu cầu lưu hồ sơ.
3. **Hệ thống** kiểm tra tính hợp lệ của dữ liệu nhập (họ tên từ 1 đến 100 ký tự, ngày sinh hợp lệ không ở tương lai, giới tính Nam/Nữ, địa chỉ tối đa 255 ký tự).
4. **Hệ thống** kiểm tra hiệu lực phiên User Client, lưu thông tin hồ sơ mới vào cơ sở dữ liệu và thông báo cập nhật thành công cho **Người dùng**.

### Alternative Flows

**Alternative flow 1: Người dùng thực hiện đổi địa chỉ email**
1. Tại bước #2 của Basic Flow, **Người dùng** chọn chức năng đổi email, cung cấp địa chỉ email mới và mật khẩu đăng nhập hiện tại.
2. **Hệ thống** kiểm tra mật khẩu hiện tại, định dạng email mới và tính duy nhất của email mới trên toàn hệ thống (không trùng với tài khoản khác).
3. **Hệ thống** phát hành mã xác minh 6 chữ số (hiệu lực 10 phút), gửi tới địa chỉ email mới và chuyển **Người dùng** sang màn hình xác minh email (`U-SCR-03`).
4. **Người dùng** kiểm tra hộp thư mới, nhập mã xác minh và gửi yêu cầu xác nhận.
5. **Hệ thống** xác thực mã hợp lệ, cập nhật email mới cho tài khoản, vô hiệu hóa mã xác minh và thông báo đổi email thành công (email cũ được giữ nguyên cho tới khi bước này hoàn tất).
6. Kết thúc Use Case.

**Alternative flow 2: Người dùng hủy thay đổi hoặc đóng hộp thoại**
1. Tại bất kỳ bước nào trước khi yêu cầu lưu được xác nhận, **Người dùng** đóng hộp thoại hoặc chọn hủy thao tác.
2. **Hệ thống** hủy các dữ liệu đang chỉnh sửa trên giao diện, giữ nguyên toàn bộ thông tin hồ sơ và email đã lưu trước đó.
3. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Dữ liệu thông tin cá nhân không hợp lệ**
1. Tại bước #2 của Basic Flow, họ tên để trống hoặc vượt quá 100 ký tự, ngày sinh ở tương lai, hoặc địa chỉ vượt quá 255 ký tự.
2. **Hệ thống** từ chối lưu và hiển thị thông báo lỗi cụ thể tại từng trường thông tin không hợp lệ để người dùng chỉnh sửa.
3. Quay lại bước #2 trong Basic Flow.

**Exception flow 2: Nhập sai mật khẩu hiện tại khi đổi email**
1. Từ bước #2 của Alternative Flow 1, mật khẩu người dùng cung cấp không khớp với mật khẩu tài khoản hiện tại.
2. **Hệ thống** từ chối phát hành mã xác minh và thông báo mật khẩu không chính xác.
3. Quay lại bước #1 của Alternative Flow 1.

**Exception flow 3: Địa chỉ email mới không hợp lệ hoặc đã được sử dụng**
1. Từ bước #2 của Alternative Flow 1, email mới sai định dạng hoặc đã được gắn với một tài khoản khác trong hệ thống.
2. **Hệ thống** từ chối tạo yêu cầu và thông báo email không thể sử dụng.
3. Quay lại bước #1 của Alternative Flow 1.

**Exception flow 4: Mã xác minh email mới không đúng hoặc đã hết hạn**
1. Từ bước #5 của Alternative Flow 1, người dùng nhập sai mã xác minh hoặc mã đã quá thời hạn 10 phút.
2. **Hệ thống** từ chối cập nhật email mới, giữ nguyên email cũ và thông báo lỗi để người dùng kiểm tra lại hoặc yêu cầu gửi mã mới.
3. Kết thúc Use Case.

**Exception flow 5: Phiên làm việc hết hạn hoặc bị thu hồi**
1. Từ bước #4 của Basic Flow hoặc bước #5 của Alternative Flow 1, phiên User Client đã hết hạn hoặc bị thu hồi từ nơi khác.
2. **Hệ thống** từ chối cập nhật dữ liệu và yêu cầu người dùng đăng nhập lại.
3. Kết thúc Use Case.

**Exception flow 6: Mất kết nối mạng hoặc lỗi máy chủ (Timeout)**
1. Quá trình gửi yêu cầu hoặc nhận phản hồi vượt quá thời gian timeout quy định.
2. **Hệ thống** hủy yêu cầu đang chờ, không xác nhận trạng thái và thông báo lỗi kết nối để người dùng thử lại.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Tên đăng nhập (`username`) là thông tin định danh cố định, chỉ hiển thị ở chế độ đọc và không được phép chỉnh sửa tại giao diện này theo [DEC-07](../../../../decisions/account-lifecycle.md).
  - **BR-02**: Ràng buộc dữ liệu hồ sơ cá nhân: Họ tên bắt buộc (1–100 ký tự sau khi cắt khoảng trắng thừa); ngày sinh hợp lệ và không ở tương lai; giới tính bắt buộc (Nam hoặc Nữ); địa chỉ là tùy chọn (tối đa 255 ký tự sau khi cắt khoảng trắng thừa) theo [DEC-07](../../../../decisions/account-lifecycle.md).
  - **BR-03**: Cập nhật thông tin cá nhân và đổi địa chỉ email là hai quy trình nghiệp vụ độc lập. Thao tác đổi email bắt buộc phải xác thực mật khẩu hiện tại và hoàn tất xác minh mã 6 chữ số gửi tới email mới theo [DEC-06](../../../../decisions/password-reset-and-email.md).
  - **BR-04**: Địa chỉ email của tài khoản phải là duy nhất trên toàn hệ thống (không phân biệt chữ hoa, chữ thường sau khi cắt khoảng trắng thừa). Email cũ chỉ bị thay thế sau khi máy chủ xác nhận email mới đã được xác minh thành công.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Thông tin cá nhân, mật khẩu và mã xác minh phải được bảo vệ qua kết nối mã hóa; không bao giờ ghi mật khẩu hoặc mã xác thực vào nhật ký hệ thống hay hiển thị trên URL.
  - **NFR-02**: Giao diện phải vô hiệu hóa các nút hành động (Lưu, Xác nhận) trong lúc hệ thống đang xử lý yêu cầu để ngăn việc người dùng gửi lặp dữ liệu.
