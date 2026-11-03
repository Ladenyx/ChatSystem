# ĐẶC TẢ USE CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U02
- **Use Case Name**: Đăng nhập vào User Client
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-01
- **Last Updated by / Date**: baonguyen1776 / 2026-11-04
- **Primary Actor**: Người dùng sử dụng User Client
- **Secondary Actor(s)**: Không có tác nhân ngoài hệ thống trong bước đăng nhập.
- **Priority**: Must Have
- **Status**: Draft
- **Screen / Feature ID**: `U-SCR-01` / `U-02`

## Mô tả tổng quan (Summary / Desciption)

- **Brief Description**: Người dùng đăng nhập bằng tên đăng nhập và mật khẩu. Server xác thực và kiểm tra trạng thái tài khoản để thiết lập phiên User Client hoặc chuyển sang xác minh email/đặt mật khẩu mới.
- **Goal**: Thiết lập phiên hợp lệ và mở khung chính User Client.
- **Trigger**: Người dùng mở User Client hoặc quay về `U-SCR-01` sau khi đăng xuất, phiên hết hạn hay bị thu hồi.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**: User Client đã có cấu hình địa chỉ Server và đang hiển thị `U-SCR-01`. Tài khoản không tồn tại hoặc thông tin sai được xử lý tại Ex 3a.
- **Post-conditions**:
  - **Thành công**: Phiên User Client được thiết lập và mở khung chính với `U-SCR-07`. Các phiên User Client khác và phiên quản trị đang có không bị thu hồi bởi lần đăng nhập này.
  - **Chuyển tiếp**: Tài khoản cần xác minh email hoặc đặt mật khẩu mới được chuyển sang bước tương ứng; chưa cấp phiên sử dụng User Client.
  - **Thất bại**: Khi Server từ chối xác thực, không cấp phiên User Client mới. Khi mất phản hồi, Client chưa xác nhận được kết quả và không mở khung chính (Ex 4a). Lần đăng nhập thất bại không tự thu hồi phiên đang hoạt động; việc thu hồi do khóa quản trị/xóa tài khoản vẫn áp dụng theo DEC-07.

## Luồng sự kiện (Flow of Events)

### Basic Flow

Luồng chính áp dụng khi tài khoản hoạt động, không bị khóa, email đã xác minh và dùng mật khẩu thông thường hợp lệ.

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Mở User Client | Hiển thị màn hình đăng nhập `U-SCR-01`. |
| 2 | Nhập thông tin và yêu cầu đăng nhập | Client kiểm tra dữ liệu bắt buộc, gửi yêu cầu xác thực khi hợp lệ; thể hiện đang xử lý và ngăn gửi yêu cầu trùng. |
| 3 | Chờ kết quả xác thực | Server kiểm tra thông tin đăng nhập và điều kiện tài khoản theo bảng bên dưới, trước khi cấp phiên hoặc quyền chuyển tiếp. |
| 4 | Chờ thiết lập phiên | Server kiểm tra tài khoản vẫn đủ điều kiện và cấp một phiên User Client độc lập; không tự thu hồi phiên User Client hoặc phiên quản trị khác. |
| 5 | Bắt đầu sử dụng | Client nhận phiên hợp lệ và mở khung chính với `U-SCR-07`. → Kết thúc Use Case. |

### Điều kiện tài khoản tại bước 3

Áp dụng trạng thái và điều kiện đăng nhập theo [DEC-07](../../../../decisions/account-lifecycle.md#trạng-thái-tài-khoản), xét bảng dưới theo thứ tự ưu tiên từ trên xuống. Các nhánh cấp phiên/chuyển tiếp chỉ được thực hiện sau khi thông tin xác thực hợp lệ.

| Điều kiện tài khoản | Kết quả | Luồng |
| --- | --- | --- |
| Không tồn tại hoặc đã xóa | Không cấp phiên hay quyền chuyển tiếp | Ex 3a/3c |
| Bị khóa quản trị | Từ chối, kể cả khi mật khẩu thông thường/mật khẩu tạm đúng | Ex 3c |
| Đang bị khóa tạm do thử sai | Từ chối đến khi hết hạn khóa tạm và các điều kiện khác đều hợp lệ | Ex 3b |
| Tài khoản do Admin tạo, chờ xác minh email, không bị khóa/xóa | Chỉ chuyển sang xác minh email, chưa cấp phiên User Client | Alt 3a |
| Hoạt động, email đã xác minh, dùng mật khẩu tạm hợp lệ | Chỉ chuyển sang đặt mật khẩu mới, chưa cấp phiên User Client | Alt 3b |
| Hoạt động, email đã xác minh, dùng mật khẩu thông thường hợp lệ | Tiếp tục thiết lập phiên User Client | Bước 4 |

Khóa quản trị, khóa tạm do thử sai và trạng thái xác minh được kiểm tra độc lập. Hết thời gian khóa tạm không tự mở khóa quản trị, phục hồi tài khoản đã xóa hoặc xác minh email.

Đăng ký công khai chưa xác minh là yêu cầu đăng ký tạm, chưa phải tài khoản đăng nhập. Nếu người dùng bỏ dở, đăng nhập không khôi phục yêu cầu này; người dùng bắt đầu đăng ký lại theo [DEC-06](../../../../decisions/password-reset-and-email.md#xác-minh-email).

### Alternative Flows

- **Alt 1a — Tạo tài khoản**: Tại bước 1, người dùng chọn đăng ký để chuyển sang use case tương ứng tại `U-SCR-02`. Quy trình đăng ký không thuộc use case đăng nhập.
- **Alt 1b — Quên mật khẩu**: Tại bước 1, người dùng chọn khôi phục mật khẩu để chuyển sang use case tương ứng tại `U-SCR-04`.
- **Alt 2a — Thiếu thông tin**: Tại bước 2, Client không gửi yêu cầu, thông báo dữ liệu còn thiếu và cho người dùng bổ sung rồi thực hiện lại bước 2.
- **Alt 3a — Tài khoản do Admin tạo chưa xác minh email**: Tại bước 3, sau xác thực hợp lệ, tài khoản do Admin tạo nhưng chưa xác minh email và không bị khóa/xóa được chuyển sang xác minh tại `U-SCR-03` nếu người dùng chọn tiếp tục. Chưa cấp phiên User Client. Quy tắc mã và xử lý hết hạn theo [DEC-06](../../../../decisions/password-reset-and-email.md#xác-minh-email).
- **Alt 3b — Mật khẩu tạm còn hiệu lực**: Tại bước 3, tài khoản đã xác minh email, không bị khóa/xóa và sử dụng mật khẩu tạm hợp lệ được chuyển sang `U-SCR-05`; chưa cấp phiên User Client. Cấp quyền giới hạn và hoàn tất đặt mật khẩu được đặc tả tại [DEC-06](../../../../decisions/password-reset-and-email.md#quyền-đặt-mật-khẩu-mới).
- **Alt 4a — Đã có phiên User Client khác**: Tại bước 4, Server vẫn cấp phiên User Client mới độc lập. Các phiên đang hoạt động tiếp tục sử dụng bình thường; không yêu cầu thiết bị cũ phê duyệt và không áp dụng cơ chế thay phiên quản trị.

### Exception Flows

- **Ex 3a — Sai thông tin hoặc tài khoản không tồn tại**: Tại bước 3, Server từ chối và trả lỗi xác thực chung, không tiết lộ tài khoản có tồn tại hay không. Với tài khoản tồn tại, ghi nhận lần thử sai theo DEC-07; người dùng được thử lại khi chưa bị khóa tạm. Không tự chuyển sang đăng ký.
- **Ex 3b — Khóa tạm do thử sai**: Server áp dụng giới hạn thử sai theo [DEC-07](../../../../decisions/account-lifecycle.md#điều-kiện-tài-khoản-khi-đăng-nhập), từ chối đăng nhập và quyền chuyển tiếp trong thời gian khóa tạm. Hết thời gian này vẫn phải kiểm tra lại khóa quản trị, trạng thái tài khoản và thông tin xác thực.
- **Ex 3c — Tài khoản không được phép đăng nhập**: Tại bước 3, tài khoản bị khóa quản trị hoặc đã xóa bị từ chối cấp phiên/quyền chuyển tiếp. Chỉ công bố lý do tài khoản theo chính sách xác thực; trước đó dùng lỗi chung. → Kết thúc lần đăng nhập.
- **Ex 3d — Mật khẩu tạm không hợp lệ**: Tại bước 3, không cấp quyền chuyển tiếp bằng mật khẩu tạm đã hết hạn/đã dùng. Khi xác định được ngữ cảnh khôi phục, cho người dùng chuyển sang `U-SCR-04` để yêu cầu lại; nếu không, dùng lỗi chung Ex 3a. Chính sách hiệu lực theo DEC-06.
- **Ex 4a — Mất kết nối hoặc Server không phản hồi**: Tại bước 2–4, dừng chờ theo timeout cấu hình và thông báo chưa thể xác nhận đăng nhập. Không tự gửi lại, không mở khung chính khi chưa nhận được phiên hợp lệ; người dùng chủ động thử lại. Nếu kết quả chưa rõ, không khẳng định Server chưa tạo phiên.
- **Ex 4b — Phiên mới hết hạn/bị thu hồi khi chuyển màn hình**: Tại bước 4–5, hủy điều hướng, xóa dữ liệu của phiên không còn hợp lệ và quay về đăng nhập. Không tự dùng lại mật khẩu để đăng nhập nền.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Đăng nhập bằng tên đăng nhập và mật khẩu theo `screen.md`. Chuẩn hóa tên đăng nhập theo DEC-07; giữ nguyên hoa thường và khoảng trắng của mật khẩu.
  - **BR-02**: User Client cho phép tài khoản thuộc mọi vai trò đăng nhập khi đáp ứng điều kiện xác thực và trạng thái tài khoản. Phiên User Client chỉ cấp quyền sử dụng chức năng người dùng; nghiệp vụ quản trị yêu cầu phiên quản trị và quyền Admin hệ thống. Server kiểm tra quyền tài khoản và phạm vi phiên ở từng yêu cầu. Quyền Admin nhóm được xác định riêng theo từng nhóm.
  - **BR-03**: Các phiên User Client độc lập và có thể hoạt động đồng thời. Đăng nhập User Client không tự thu hồi phiên chat hay phiên quản trị khác. Giới hạn một phiên chỉ áp dụng cho phiên quản trị theo DEC-07.
  - **BR-04**: Khóa quản trị/xóa tài khoản chặn cấp cả phiên sử dụng ứng dụng lẫn quyền chuyển tiếp, đồng thời thu hồi các phiên theo DEC-07. Khóa tạm do thử sai chặn cấp mới trong thời gian khóa, không tự thu hồi phiên đang hoạt động. Xác minh email và đặt mật khẩu mới chỉ được cấp quyền phục vụ bước tương ứng theo DEC-06.
  - **BR-05**: Khôi phục E2EE không phải điều kiện đăng nhập thành công. Sau khi vào khung chính, người dùng có thể khôi phục ngay hoặc để sau; hủy/bỏ qua không kết thúc phiên. Chuẩn bị khóa để gửi/nhận tin mới và bảo toàn backup thuộc luồng E2EE theo [DEC-05](../../../../decisions/group-permissions-and-e2ee.md#thiết-lậpkhôi-phục-khóa-sau-đăng-nhập).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Truyền thông tin xác thực qua kết nối được bảo vệ; không ghi mật khẩu/token vào log, thông báo lỗi hoặc URL.
  - **NFR-02**: Thể hiện trạng thái xử lý, ngăn yêu cầu trùng và thông báo kết quả rõ ràng; không báo thành công trước xác nhận hợp lệ từ Server.

Chi tiết nhãn nút, trạng thái ô nhập, thông báo và focus được mô tả tại [đặc tả giao diện U-SCR-01](../../../screens/user/authentication/U-SCR-01-user-sign-in.md).
