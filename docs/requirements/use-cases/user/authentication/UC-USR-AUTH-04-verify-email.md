# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-AUTH-04
- **Use Case Name**: Xác minh email
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-04
- **Last Updated by / Date**: baonguyen1776 / 2026-11-08
- **Primary Actor**: Người dùng cần xác minh email
- **Secondary Actor(s)**: Dịch vụ gửi email (Email Service)

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng xác thực quyền sở hữu địa chỉ email bằng mã xác minh gồm 6 chữ số do máy chủ phát hành và gửi qua email.
- **Goal**: Xác minh thành công địa chỉ email cho quy trình nghiệp vụ đang thực hiện (Đăng ký tài khoản, Đổi email hồ sơ, Kích hoạt tài khoản do Admin tạo).
- **Trigger**: Hệ thống chuyển hướng người dùng tới màn hình xác minh email (`U-SCR-03`) từ một luồng nghiệp vụ yêu cầu xác minh.

## Điều kiện (Conditions)

- **Pre-conditions**: Máy chủ đã tiếp nhận yêu cầu và phát hành mã xác minh 6 chữ số tới địa chỉ email mục tiêu.
- **Post-conditions**:
  - **Thành công**: Máy chủ ghi nhận địa chỉ email đã được xác minh thành công cho đúng yêu cầu nghiệp vụ tương ứng; người dùng được chuyển tiếp sang bước tiếp theo của luồng gọi.
  - **Chưa hoàn tất**: Nếu mã sai, hết hạn hoặc người dùng hủy, địa chỉ email chưa được xác minh và không làm thay đổi trạng thái dữ liệu chính thức.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** tiếp cận màn hình xác minh email (`U-SCR-03`), xem địa chỉ email nhận mã được che một phần và kiểm tra hộp thư email.
2. **Người dùng** nhập đủ 6 chữ số của mã xác minh nhận được và gửi yêu cầu xác thực.
3. **Hệ thống** kiểm tra định dạng mã, đối chiếu mã với dữ liệu lưu trên máy chủ và kiểm tra thời hạn hiệu lực của mã (10 phút).
4. **Hệ thống** ghi nhận email đã được xác minh thành công cho yêu cầu hiện tại, vô hiệu hóa mã xác minh và điều hướng **Người dùng** sang bước kế tiếp của luồng nghiệp vụ gọi chức năng này (chuyển sang nhập thông tin cá nhân nếu đang Đăng ký, hoặc hoàn tất đổi email nếu đang ở Hồ sơ).

### Alternative Flows

**Alternative flow 1: Người dùng yêu cầu gửi lại mã xác minh**
1. Tại bước #2 của Basic Flow, **Người dùng** chưa nhận được email hoặc mã cũ đã hết hạn và chọn "Gửi lại mã".
2. **Hệ thống** kiểm tra thời gian chờ giãn cách (tối thiểu 60 giây từ lần gửi trước); nếu hợp lệ thì vô hiệu hóa mã cũ, tạo mã 6 số mới và gửi tới email.
3. Tiếp tục bước #2 trong Basic Flow với mã mới.

**Alternative flow 2: Người dùng sửa địa chỉ email (trong luồng Đăng ký)**
1. Tại bước #1 hoặc #2 của Basic Flow, **Người dùng** nhận thấy nhập sai email và chọn "Đổi email".
2. **Hệ thống** quay lại bước nhập email, giữ nguyên username và password đã nhập trước đó.
3. Sau khi **Người dùng** cung cấp email mới hợp lệ, **Hệ thống** hủy mã cũ và gửi mã mới tới địa chỉ email mới vừa nhập.
4. Quay lại bước #1 trong Basic Flow.

**Alternative flow 3: Người dùng rời luồng xác minh**
1. Tại bất kỳ bước nào trước khi xác minh thành công, **Người dùng** chọn quay lại hoặc đóng ứng dụng.
2. **Hệ thống** hủy quy trình xác minh trên giao diện; đối với đăng ký công khai thì gửi yêu cầu hủy đăng ký tạm theo quy định.
3. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Nhập chưa đủ 6 chữ số của mã xác minh**
1. Tại bước #2 của Basic Flow, người dùng chưa nhập đủ 6 ô số.
2. **Hệ thống** vô hiệu hóa nút gửi, hiển thị thông báo hướng dẫn nhập đủ 6 chữ số và không gửi yêu cầu lên máy chủ (không tính là lần thử sai).
3. Quay lại bước #2 trong Basic Flow.

**Exception flow 2: Nhập sai mã xác minh**
1. Từ bước #3 của Basic Flow, mã người dùng nhập không khớp với mã máy chủ đã phát hành.
2. **Hệ thống** ghi nhận một lần thử sai, từ chối xác thực và hiển thị thông báo mã không chính xác để người dùng sửa lại.
3. Quay lại bước #2 trong Basic Flow.

**Exception flow 3: Nhập sai quá 5 lần liên tiếp hoặc mã hết hạn (10 phút)**
1. Từ bước #3 của Basic Flow, số lần nhập sai vượt quá 5 lần hoặc thời gian kể từ lúc phát hành mã vượt quá 10 phút.
2. **Hệ thống** vô hiệu hóa mã xác minh hiện tại và yêu cầu người dùng gửi lại mã mới.
3. Kết thúc Use Case.

**Exception flow 4: Chưa đủ thời gian giãn cách gửi lại mã (Rate Limit)**
1. Từ bước #2 của Alternative Flow 1, người dùng bấm gửi lại khi chưa hết thời gian đếm ngược 60 giây.
2. **Hệ thống** từ chối phát hành mã mới và hiển thị thời gian còn lại cần chờ.
3. Quay lại bước #2 trong Basic Flow.

**Exception flow 5: Mất kết nối mạng hoặc lỗi gửi email**
1. Quá trình gửi mã hoặc xác thực gặp sự cố gián đoạn kết nối.
2. **Hệ thống** thông báo lỗi kết nối và cho phép người dùng thử lại khi mạng ổn định.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Mã xác minh gồm đúng 6 chữ số, dùng một lần và chỉ có hiệu lực tối đa 10 phút kể từ thời điểm máy chủ phát hành theo [DEC-06](../../../../decisions/password-reset-and-email.md#xác-minh-email).
  - **BR-02**: Nhập sai 5 lần liên tiếp sẽ làm mã hiện tại mất hiệu lực; người dùng phải yêu cầu mã mới. Nhập sai mã không tự động khóa tài khoản người dùng.
  - **BR-03**: Giãn cách gửi lại mã: tối thiểu 60 giây giữa hai lần gửi liên tiếp đến cùng một địa chỉ email.
  - **BR-04**: Xác minh email thành công chỉ là điều kiện xác thực trung gian; việc tạo tài khoản, đổi email hồ sơ hay kích hoạt tài khoản do luồng nghiệp vụ cha quyết định.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Mã xác minh phải được bảo vệ qua kết nối bảo mật; không lưu mã rõ trong nhật ký hệ thống.
  - **NFR-02**: Giao diện hỗ trợ dán (paste) chuỗi 6 chữ số vào các ô nhập liệu và tự động phân chia ký tự.
