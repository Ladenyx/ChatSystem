# ĐẶC TẢ USE CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U02
- **Use Case Name**: Đăng nhập vào User Client
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-01
- **Last Updated by / Date**: baonguyen1776 / 2026-11-02
- **Primary Actor**: Người dùng sử dụng User Client
- **Secondary Actor(s)**: Không có tác nhân ngoài hệ thống trong bước đăng nhập.
- **Priority**: Must Have
- **Status**: Draft
- **Screen / Feature ID**: `U-SCR-01` / `U-02`

## Mô tả tổng quan (Summary / Desciption)

- **Brief Description**: Người dùng đăng nhập bằng tên đăng nhập và mật khẩu. Server xác thực và kiểm tra điều kiện truy cập; tài khoản cần xác minh email, đổi mật khẩu tạm hoặc thiết lập/khôi phục khóa được chuyển đến màn hình tương ứng.
- **Goal**: Thiết lập phiên hợp lệ và mở khung chính User Client sau khi đáp ứng các điều kiện khởi tạo.
- **Trigger**: Người dùng mở User Client hoặc quay về `U-SCR-01` sau khi đăng xuất, phiên hết hạn hay bị thu hồi.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**: User Client đã có cấu hình địa chỉ Server và đang hiển thị `U-SCR-01`. Tài khoản không tồn tại hoặc thông tin sai được xử lý tại Ex 3a.
- **Post-conditions**:
  - **Thành công**: Phiên đầy đủ được thiết lập, phiên cũ (nếu có) bị thu hồi; tối đa một phiên đầy đủ/tài khoản; mở khung chính với màn hình `U-SCR-07`.
  - **Chuyển tiếp**: Tài khoản còn bước xác minh email, đặt mật khẩu mới hoặc thiết lập/khôi phục khóa được chuyển đến màn hình tương ứng, chưa vào chat.
  - **Thất bại**: Không cấp phiên đầy đủ mới. Đăng nhập thất bại không làm thu hồi phiên đang hoạt động.

## Luồng sự kiện (Flow of Events)

### Basic Flow

Luồng chính áp dụng khi tài khoản đủ điều kiện sử dụng User Client, email đã xác minh, dùng mật khẩu thông thường và thiết bị đã có khóa E2EE hợp lệ.

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Mở User Client | Hiển thị form trống gồm Tên đăng nhập, Mật khẩu, nút hiện/ẩn mật khẩu, **Đăng nhập**, **Quên mật khẩu?** và **Tạo tài khoản**. Mật khẩu được che mặc định. |
| 2 | Nhập thông tin và bấm **Đăng nhập** hoặc nhấn Enter | Kiểm tra trường bắt buộc; nếu hợp lệ, gửi một yêu cầu xác thực. Đổi nút thành **Loading...**, vô hiệu hóa nút và chặn Enter gửi lặp khi đang chờ. |
| 3 | Chờ kết quả xác thực | Server kiểm tra thông tin đăng nhập, trạng thái tài khoản và quyền sử dụng User Client. |
| 4 | Chờ thiết lập phiên | Khi đủ điều kiện cấp phiên đầy đủ, Server thay phiên cũ (nếu có) và cấp phiên mới theo Alt 4a. |
| 5 | Chờ kiểm tra khóa trên thiết bị | Client kiểm tra khóa E2EE gắn với đúng tài khoản. Thiếu khóa chuyển Alt 5a/5b. |
| 6 | Bắt đầu sử dụng | Mở khung chính với `U-SCR-07`, xóa mật khẩu khỏi form. → Kết thúc Use Case. |

### Alternative Flows

- **Alt 1a — Tạo tài khoản**: Tại bước 1, người dùng chọn **Tạo tài khoản** để mở `U-SCR-02`; không chuyển mật khẩu đang nhập sang màn đăng ký.
- **Alt 1b — Quên mật khẩu**: Tại bước 1, chọn **Quên mật khẩu?** để mở `U-SCR-04`. Có thể điền sẵn tên đăng nhập làm gợi ý; không tự gửi email khi điều hướng.
- **Alt 1c — Hiện/ẩn mật khẩu**: Tại bước 1–2, nút hiện/ẩn chỉ thay cách hiển thị, giữ nguyên giá trị mật khẩu. Khi rời màn hình, xóa mật khẩu và đặt lại chế độ che.
- **Alt 1d — Trở về sau đăng ký/xác minh/đổi mật khẩu**: Hiển thị `U-SCR-01` với cả hai ô trống; có thể kèm thông báo thành công phù hợp. Người dùng tự nhập thông tin để đăng nhập lại.
- **Alt 2a — Thiếu thông tin**: Tại bước 2, hiển thị lỗi dưới từng trường còn thiếu và focus trường lỗi đầu tiên; không gửi yêu cầu lên Server. Người dùng bổ sung rồi thực hiện lại bước 2.
- **Alt 3a — Email chưa xác minh**: Tại bước 3, sau khi thông tin xác thực hợp lệ, nếu tài khoản còn chờ xác minh, hiển thị **“Email của tài khoản chưa được xác minh.”** Cho chọn **Xác minh email** để mở `U-SCR-03`, hoặc **Quay lại đăng nhập**. Không tự gửi lại email; màn xác minh kiểm tra hạn mã và cho yêu cầu mã mới theo chính sách gửi lại.
- **Alt 3b — Mật khẩu tạm còn hiệu lực**: Tại bước 3, Server kiểm tra mật khẩu tạm còn hạn và chưa dùng; cấp quyền chỉ đặt mật khẩu mới tại `U-SCR-05`, chưa cho vào chat. Theo hướng DEC-06, mật khẩu tạm dùng một lần, hết hạn **15 phút**; bản nháp tính từ khi Server phát hành. Chỉ khi lưu mật khẩu mới thành công mới vô hiệu hóa mật khẩu cũ và thu hồi phiên hiện tại. Luồng tài khoản do Admin mới tạo cần đặc tả riêng vì chưa có mật khẩu cũ.
- **Alt 4a — Tài khoản đang có phiên hoạt động**: Tại bước 4, Server thu hồi trực tiếp phiên cũ và cấp phiên mới, không chờ phê duyệt từ thiết bị cũ. Phiên mới tiếp tục bước 5. Thiết bị cũ quay về `U-SCR-01`, xóa dữ liệu phiên và hiển thị **“Bạn đã được đăng xuất vì tài khoản được đăng nhập trên thiết bị khác.”** Nếu thiết bị cũ offline, phiên vẫn bị vô hiệu tại Server và bị từ chối khi kết nối lại.
- **Alt 5a — Chưa có khóa, chưa có bản sao lưu**: Tại bước 5, chuyển `U-SCR-06` ở chế độ **Thiết lập khóa**. Chỉ mở khung chính khi hoàn tất; không tạo khóa mới nếu chưa xác định được trạng thái bản sao lưu.
- **Alt 5b — Có bản sao lưu, thiếu khóa cục bộ**: Tại bước 5, chuyển `U-SCR-06` ở chế độ **Khôi phục khóa**. Xác thực tài khoản không tự giải mã được bản sao lưu. Lỗi đọc khóa/kiểm tra bản sao lưu được xử lý tại màn hình này, không coi là chưa có khóa để ghi đè.
- **Alt 5c — Hủy thiết lập/khôi phục khóa**: Kết thúc phiên mới, xóa dữ liệu nhạy cảm tạm và quay về login trống; không khôi phục phiên cũ đã bị thu hồi.

### Exception Flows

- **Ex 3a — Sai thông tin hoặc tài khoản không tồn tại**: Tại bước 3, Server từ chối xác thực. Hiển thị **“Tên đăng nhập hoặc mật khẩu không đúng.”** tại vùng lỗi chung; giữ tên đăng nhập, xóa mật khẩu và cho nhập lại. Focus thông báo lỗi chung, không đánh dấu riêng ô mật khẩu sai; không tự chuyển sang đăng ký.
- **Ex 3b — Tạm hạn chế thử đăng nhập**: Tại bước 3, nếu Server yêu cầu chờ do thử sai, hiển thị thời gian chờ được Server cung cấp và chặn gửi lại đến khi được phép. Không đồng nhất hạn chế này với khóa quản trị.
- **Ex 3c — Tài khoản không được phép đăng nhập**: Tại bước 3, tài khoản bị khóa, xóa hoặc không có quyền sử dụng User Client bị từ chối cấp phiên. Trước khi xác thực danh tính, dùng lỗi chung; sau xác thực, hiển thị lý do/hướng dẫn hỗ trợ theo chính sách nhóm. Khóa quản trị không tự mở theo thời gian chờ thử sai. → Kết thúc lần đăng nhập.
- **Ex 3d — Mật khẩu tạm hết hạn/đã dùng**: Không mở `U-SCR-05`. Khi Server xác định được ngữ cảnh khôi phục, hiển thị **“Mật khẩu tạm không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu lại.”** và cho chọn **Quên mật khẩu?**; nếu không, dùng lỗi chung Ex 3a.
- **Ex 3e — Quyền xác minh/đặt mật khẩu hết hạn**: Không tiếp tục bằng quyền tạm cũ. Quay về login trống với thông báo **“Yêu cầu đã hết hạn. Vui lòng bắt đầu lại.”** Người dùng chủ động đăng nhập để xác minh lại hoặc yêu cầu mật khẩu tạm mới.
- **Ex 4a — Mất kết nối hoặc Server không phản hồi**: Tại bước 2–4, dừng chờ sau timeout cấu hình, giữ tên đăng nhập, xóa mật khẩu và trả nút về **Đăng nhập**. Hiển thị **“Chưa thể xác nhận đăng nhập. Kiểm tra kết nối rồi thử lại.”** Không tự gửi lại hoặc khẳng định trạng thái phiên cũ khi chưa nhận được kết quả. Người dùng chủ động thử lại.
- **Ex 4b — Phiên hết hạn/bị thu hồi khi chuyển màn hình**: Tại bước 4–6, hủy điều hướng vào chat, xóa dữ liệu phiên và quay về login trống kèm lý do phù hợp. Không tự dùng lại mật khẩu để đăng nhập nền.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Đăng nhập bằng tên đăng nhập và mật khẩu theo `screen.md`. Chuẩn hóa tên đăng nhập theo DEC-07; giữ nguyên hoa thường và khoảng trắng của mật khẩu.
  - **BR-02**: Server quyết định quyền truy cập User Client và kiểm tra phiên trên các yêu cầu sau đăng nhập. Không có lựa chọn role trên form; quyền Admin nhóm không thay thế Admin hệ thống. Quyền dùng User Client của tài khoản Admin hệ thống còn chờ nhóm chốt.
  - **BR-03**: Tối đa một phiên đầy đủ/tài khoản; thay phiên phải bảo đảm điều này cả khi có nhiều yêu cầu đồng thời. Thu hồi áp dụng cho yêu cầu nghiệp vụ và kết nối thời gian thực; reconnect không giành lại phiên cũ.
  - **BR-04**: Quyền xác minh email/đặt mật khẩu chỉ phục vụ bước tương ứng, không cấp quyền chat. Hết hạn mã hoặc mật khẩu tạm không tự xóa tài khoản.
  - **BR-05**: Mở lại ứng dụng luôn bắt đầu ở login trống; không khôi phục mã, mật khẩu, quyền tạm hoặc focus của luồng đã bỏ dở. Server kiểm tra hạn khi cấp quyền và khi hoàn tất bước giới hạn; không gia hạn do mở app hay thao tác trên form.
  - **BR-06**: Khóa E2EE gắn với đúng tài khoản. Reset mật khẩu không thay mã khôi phục hoặc tự mở được lịch sử mã hóa.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Che mật khẩu mặc định; truyền thông tin xác thực qua kết nối được bảo vệ. Không ghi mật khẩu/token vào log, thông báo lỗi hay URL.
  - **NFR-02**: Dùng được bằng Tab, Shift+Tab và Enter; nhãn trường luôn hiện. Nút hiện/ẩn có tên truy cập; lỗi có chữ và focus phù hợp, không chỉ đổi màu.
  - **NFR-03**: Nút chờ ghi đúng **Loading...**, bị vô hiệu hóa và chặn Enter gửi lặp. Khi kết thúc chờ mà vẫn ở login, trả nút về **Đăng nhập**; không đóng băng cửa sổ hoặc báo thành công trước xác nhận từ Server.

## Thiết kế màn hình U-SCR-01

[Framer — Bao → U-SCR-01 · Đăng nhập người dùng](https://framer.com/projects/Early-Persimmon--1Tzz5NWFPHgQOJcXaGB4-jirMF?node=MUWi3e89o).

Mockup tiếng Việt dùng các layer chỉnh sửa được của Framer, tham chiếu JavaFX/FXML/CSS + AtlantaFX. Các bản được đặt cạnh nhau để trình bày flow; chưa nối tương tác hoặc Server.

| Trạng thái | Nội dung thể hiện | Mockup |
| --- | --- | --- |
| Mặc định | Hai ô trống, mật khẩu che; liên kết đăng ký/quên mật khẩu | Đã dựng |
| Đang xác thực | **Loading...**, nút và ô nhập thể hiện bị khóa | Đã dựng |
| Sai thông tin | Lỗi chung, giữ tên đăng nhập, mật khẩu trống | Đã dựng |
| Thiếu thông tin | Lỗi dưới trường thiếu, focus trường đầu tiên | Chưa dựng |
| Mất kết nối/timeout | Thông báo chưa thể xác nhận, cho thử lại | Chưa dựng |
| Phiên hết hạn/bị thay | Login trống kèm lý do kết thúc phiên | Chưa dựng |
| Trở về sau thao tác thành công | Banner phù hợp, hai ô trống | Chưa dựng |
| Tạm hạn chế thử đăng nhập | Thời gian chờ theo Server | Chưa dựng |
| Không được phép đăng nhập | Lý do/hướng dẫn được phép công bố | Chưa dựng |
| Mật khẩu tạm/quyền giới hạn hết hạn | Thông báo cần bắt đầu lại | Chưa dựng |
| Email chưa xác minh | Thông báo và lựa chọn sang `U-SCR-03` | Chưa dựng |

Mở lại sau khi bỏ dở dùng lại trạng thái Mặc định. Nhánh mật khẩu tạm còn hạn và thiết lập/khôi phục khóa chuyển sang `U-SCR-05`/`U-SCR-06`; thiết kế chi tiết thuộc các màn hình đó.

## Điểm cần nhóm chốt

DEC-05/06/07 hiện là đề xuất. Bản nháp dùng hướng trong các tài liệu này và còn các điểm sau cần review:

- Tài khoản Admin hệ thống có được sử dụng User Client hay không.
- Ngưỡng thử sai, thời gian hạn chế, thời hạn phiên và timeout đăng nhập.
- Thời hạn mã xác minh, thời gian lưu đăng ký chưa hoàn tất; thứ tự xử lý khi tài khoản vừa chưa xác minh vừa dùng mật khẩu tạm.
- Hạn quyền đặt mật khẩu: đề xuất không vượt quá hạn yêu cầu khôi phục gốc, kể cả khi đã mở `U-SCR-05` trước hạn; không tự kéo dài thêm 15 phút.
- Thời điểm thay phiên khi máy mới thiếu khóa: bản nháp cấp phiên mới trước bước thiết lập/khôi phục khóa; hủy bước này không trả lại phiên cũ.

## Tài liệu tham chiếu

- [Mẫu đặc tả](../../_template.md) và [UC-A01 — đăng nhập Admin](../../admin/authentication/UC-A01-admin-sign-in.md).
- [Đề bài gốc](../../../../../Final%20Project.pdf) và [Screen map — mục 1.1](../../../screen.md).
- [DEC-06 — mật khẩu và email](../../../../decisions/password-reset-and-email.md).
- [DEC-07 — tài khoản và vòng đời dữ liệu](../../../../decisions/account-lifecycle.md).
- [DEC-05 — quyền nhóm và E2EE](../../../../decisions/group-permissions-and-e2ee.md).
- [DEC-01 — công nghệ](../../../../decisions/tech-stack.md).
