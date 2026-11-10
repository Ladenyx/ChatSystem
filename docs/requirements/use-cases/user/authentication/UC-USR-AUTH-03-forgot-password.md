# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-AUTH-03
- **Use Case Name**: Khôi phục mật khẩu và đặt lại mật khẩu bắt buộc
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-04
- **Last Updated by / Date**: baonguyen1776 / 2026-11-08
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Dịch vụ gửi email (Email Service)

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng xác thực quyền sở hữu tài khoản qua email để nhận mật khẩu tạm ngẫu nhiên, sau đó sử dụng mật khẩu tạm để bắt buộc thiết lập mật khẩu mới.
- **Goal**: Khôi phục quyền truy cập tài khoản thông qua việc đặt mật khẩu mới an toàn và hợp lệ.
- **Trigger**: Người dùng chọn liên kết "Quên mật khẩu?" tại màn hình đăng nhập (`U-SCR-01`).

## Điều kiện (Conditions)

- **Pre-conditions**: Ứng dụng User Client đã kết nối được với máy chủ; tài khoản người dùng đã tồn tại và có gắn địa chỉ email hợp lệ; người dùng nhớ tên đăng nhập và có quyền truy cập hộp thư email liên quan.
- **Post-conditions**: Mật khẩu mới được lưu thành công trên máy chủ; mật khẩu cũ và mật khẩu tạm bị vô hiệu hóa; các phiên làm việc đang hoạt động của tài khoản bị thu hồi theo chính sách an toàn; người dùng được điều hướng về giao diện đăng nhập để đăng nhập lại bằng mật khẩu mới.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** chọn chức năng quên mật khẩu từ màn hình đăng nhập (`U-SCR-01`), cung cấp tên đăng nhập và gửi yêu cầu tiếp tục.
2. **Hệ thống** kiểm tra tên đăng nhập, xác định tài khoản tồn tại và đủ điều kiện khôi phục, sau đó phản hồi gợi ý email được che một phần (ví dụ `b***@gmail.com`).
3. **Người dùng** nhập lại địa chỉ email đầy đủ của tài khoản và gửi yêu cầu phát hành mật khẩu tạm.
4. **Hệ thống** đối chiếu email người dùng nhập với email đã đăng ký của tài khoản, kiểm tra giới hạn tần suất gửi, phát hành một mật khẩu tạm ngẫu nhiên có hiệu lực 15 phút và gửi mật khẩu này tới hộp thư email của tài khoản.
5. **Người dùng** kiểm tra hộp thư email, nhận mật khẩu tạm và chọn tiếp tục để mở màn hình đặt mật khẩu mới (`U-SCR-05`).
6. **Người dùng** cung cấp mật khẩu tạm vừa nhận và gửi yêu cầu xác nhận.
7. **Hệ thống** xác thực mật khẩu tạm, kiểm tra thời hạn hiệu lực và cấp quyền đặt mật khẩu mới trong phạm vi thời hạn còn lại của mật khẩu tạm.
8. **Người dùng** nhập mật khẩu mới, xác nhận lại mật khẩu mới và gửi yêu cầu lưu.
9. **Hệ thống** kiểm tra quy tắc an toàn mật khẩu, lưu mật khẩu mới, vô hiệu hóa mật khẩu tạm và mật khẩu cũ, thu hồi các phiên đang hoạt động của tài khoản và chuyển **Người dùng** về màn hình đăng nhập (`U-SCR-01`).

### Alternative Flows

**Alternative flow 1: Người dùng nhập mật khẩu tạm trực tiếp tại màn hình đăng nhập**
1. Tại bước #5 của Basic Flow, sau khi nhận mật khẩu tạm từ email, **Người dùng** quay về màn hình đăng nhập `U-SCR-01`, nhập tên đăng nhập cùng mật khẩu tạm để đăng nhập.
2. **Hệ thống** xác thực mật khẩu tạm hợp lệ, cấp quyền đặt mật khẩu mới và chuyển thẳng **Người dùng** sang màn hình `U-SCR-05` tại bước nhập mật khẩu mới mà không yêu cầu nhập lại mật khẩu tạm.
3. Tiếp tục bước #8 trong Basic Flow.

**Alternative flow 2: Yêu cầu gửi lại mật khẩu tạm**
1. Tại bước #4 hoặc #5 của Basic Flow, **Người dùng** chưa nhận được email hoặc mật khẩu tạm bị quá hạn và chọn yêu cầu gửi lại.
2. **Hệ thống** kiểm tra khoảng thời gian giãn cách tối thiểu (60 giây) và giới hạn số lượt gửi trong 1 giờ; nếu thỏa mãn thì hủy mật khẩu tạm cũ, phát hành mật khẩu tạm mới và gửi lại email.
3. Quay lại bước #5 trong Basic Flow.

**Alternative flow 3: Người dùng chủ động hủy hoặc quay về màn hình đăng nhập**
1. Tại bất kỳ bước nào trước bước #9 của Basic Flow, **Người dùng** chọn quay về màn hình đăng nhập (`U-SCR-01`).
2. **Hệ thống** hủy quy trình khôi phục trên giao diện, giữ nguyên mật khẩu cũ và các phiên hiện tại của tài khoản.
3. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Thiếu thông tin bắt buộc hoặc email sai định dạng**
1. Tại bước #1 hoặc #3 của Basic Flow, **Người dùng** bỏ trống tên đăng nhập, email hoặc nhập email không đúng định dạng.
2. **Hệ thống** từ chối gửi yêu cầu và thông báo trường thông tin cần hoàn thiện.
3. Quay lại bước tương ứng trong Basic Flow.

**Exception flow 2: Tên đăng nhập không tồn tại hoặc email không khớp**
1. Từ bước #2 hoặc #4 của Basic Flow, tên đăng nhập không tồn tại trong hệ thống hoặc email nhập vào không khớp với email gắn với tài khoản.
2. **Hệ thống** từ chối phát hành mật khẩu tạm và trả thông báo lỗi chung mà không tiết lộ chi tiết tài khoản hay thông tin bảo mật.
3. Kết thúc Use Case.

**Exception flow 3: Vượt quá giới hạn yêu cầu gửi mật khẩu tạm (Rate Limit)**
1. Từ bước #4 của Basic Flow, người dùng gửi yêu cầu khi chưa hết 60 giây giãn cách hoặc vượt quá 5 lần/giờ/tài khoản.
2. **Hệ thống** từ chối phát hành mật khẩu tạm mới và thông báo thời gian người dùng cần chờ trước khi thử lại.
3. Kết thúc Use Case.

**Exception flow 4: Mật khẩu tạm không hợp lệ hoặc đã hết hạn**
1. Từ bước #6 của Basic Flow, mật khẩu tạm đã hết hạn 15 phút hoặc nhập sai.
2. **Hệ thống** từ chối cấp quyền đặt mật khẩu và thông báo mật khẩu tạm không hợp lệ để người dùng yêu cầu lại.
3. Kết thúc Use Case.

**Exception flow 5: Tài khoản đang bị khóa quản trị hoặc đã bị xóa mềm**
1. Từ bước #2, #4 hoặc #7 của Basic Flow, tài khoản ở trạng thái bị khóa (`LOCKED`) hoặc xóa mềm (`DELETED`).
2. **Hệ thống** từ chối thực hiện quy trình khôi phục mật khẩu và thông báo tài khoản không đủ điều kiện thao tác.
3. Kết thúc Use Case.

**Exception flow 6: Mật khẩu mới không đạt chuẩn hoặc không khớp**
1. Tại bước #8 của Basic Flow, mật khẩu mới không đạt độ dài 8–128 ký tự, thiếu các thành phần bắt buộc (chữ hoa, chữ thường, số, ký tự đặc biệt) hoặc hai ô mật khẩu không khớp nhau.
2. **Hệ thống** từ chối lưu và hiển thị chi tiết các tiêu chí chưa đạt để người dùng chỉnh sửa.
3. Quay lại bước #8 trong Basic Flow.

**Exception flow 7: Mất kết nối mạng hoặc lỗi gửi dịch vụ email**
1. Từ bước #4 hoặc #9 của Basic Flow, kết nối mạng bị gián đoạn hoặc dịch vụ email SMTP gặp sự cố.
2. **Hệ thống** hủy thao tác đang chờ, không khẳng định trạng thái đã thành công và hướng dẫn người dùng thử lại sau.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Mật khẩu tạm có hiệu lực tối đa 15 phút kể từ thời điểm máy chủ phát hành; quyền đặt mật khẩu mới dùng chung thời hạn 15 phút này, việc mở màn hình không gia hạn thêm thời gian.
  - **BR-02**: Giới hạn gửi mật khẩu tạm: giãn cách tối thiểu 60 giây giữa 2 lần yêu cầu liên tiếp và tối đa 5 yêu cầu trong vòng 1 giờ cho mỗi tài khoản theo [DEC-06](../../../../decisions/password-reset-and-email.md).
  - **BR-03**: Gợi ý email chỉ hiển thị ký tự đầu tiên và phần tên miền (ví dụ: `u***@example.com`); máy chủ không gửi email đầy đủ về phía client trước khi người dùng xác thực.
  - **BR-04**: Mật khẩu tạm chỉ có quyền dùng một lần và chỉ dùng để đặt mật khẩu mới, không cấp quyền truy cập tin nhắn hay các nghiệp vụ người dùng khác.
  - **BR-05**: Khi lưu mật khẩu mới thành công, máy chủ vô hiệu hóa mật khẩu cũ và mật khẩu tạm, đồng thời thu hồi toàn bộ các phiên làm việc đang hoạt động của tài khoản (yêu cầu người dùng đăng nhập lại từ đầu).
  - **BR-06**: Mật khẩu mới bắt buộc có độ dài từ 8 đến 128 ký tự, có ít nhất một chữ hoa, một chữ thường, một chữ số và một ký tự đặc biệt theo quy định tại [DEC-07](../../../../decisions/account-lifecycle.md).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Mật khẩu tạm và mật khẩu mới phải được truyền qua kênh bảo mật mã hóa; không bao giờ lưu mật khẩu dưới dạng văn bản rõ (plain-text) trong nhật ký hệ thống hoặc thông báo lỗi.
  - **NFR-02**: Giao diện phải vô hiệu hóa nút gửi trong khi đang xử lý để ngăn chặn việc người dùng click nhiều lần gửi yêu cầu trùng lặp.
