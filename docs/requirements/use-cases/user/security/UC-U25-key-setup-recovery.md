# ĐẶC TẢ USE CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U25-KEYS
- **Use Case Name**: Thiết lập và khôi phục khóa E2EE
- **Author / Create by**: baonguyen1776
- **Date Create**: 2026-11-05
- **Last Updated by / Date**: baonguyen1776 / 2026-11-05
- **Primary Actor**: Người dùng đã đăng nhập User Client.
- **Secondary Actor(s)**: Không có tác nhân ngoài hệ thống.
- **Priority**: Must Have
- **Status**: Draft
- **Screen / Feature ID**: `U-SCR-06` / `U-25`

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng thiết lập hoặc nhập mã bảo mật 6 số để mở hoặc khôi phục khóa trên thiết bị, phục vụ sử dụng nhóm chat được mã hóa.
- **Goal**: Giúp thiết bị có khóa phù hợp để sử dụng các chức năng nhóm chat được mã hóa.
- **Trigger**: Người dùng chọn thiết lập hoặc khôi phục khóa sau khi đăng nhập.

> Đề bài yêu cầu mã hóa nhóm chat; cách sử dụng mã 6 số và khôi phục khóa là phương án đề xuất trong bản nháp của chúng ta.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng đã đăng nhập thành công vào User Client và có phiên hợp lệ. Việc đăng nhập không đồng nghĩa thiết bị đã có khóa E2EE.
- **Post-conditions**:
  - **Thiết lập thành công**: Thiết bị đã thiết lập khóa và Server đã xác nhận lưu bản sao lưu khóa được mã hóa. Người dùng có mã khôi phục để tự lưu.
  - **Khôi phục thành công**: Thiết bị đã khôi phục được khóa; chỉ phần lịch sử có đủ khóa và dữ liệu mới có thể đọc sau khi đồng bộ.
  - **Thiết lập lại khi quên mã**: Sau khi người dùng xác nhận cảnh báo và thiết lập thành công, hệ thống lưu bản sao lưu khóa mới thay cho bản cũ. Việc này không tự khôi phục khóa hay bảo đảm đọc được lịch sử mã hóa cũ.
  - **Chưa hoàn tất**: Phiên đăng nhập không kết thúc chỉ vì người dùng bỏ qua, hủy hoặc gặp lỗi. Bản sao lưu khóa hiện có không bị tự xóa hoặc ghi đè.
  - **Chưa rõ kết quả**: Nếu mất phản hồi khi lưu hoặc khôi phục, Client không khẳng định thao tác đã thành công hay chưa; không tự tạo hoặc ghi đè bản sao lưu khác.

## Luồng sự kiện (Flow of Events)

### Basic Flow — Thiết lập lần đầu

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Sau khi đăng nhập, người dùng chọn thiết lập khóa | Client mở `U-SCR-06` và kiểm tra trạng thái khóa trên thiết bị cùng trạng thái bản sao lưu. |
| 2 | Người dùng nhập mã khóa 6 số và nhập lại để xác nhận | Nếu xác định được chưa có bản sao lưu khóa, Client kiểm tra hai mã và chuẩn bị khóa E2EE. |
| 3 | Người dùng yêu cầu hoàn tất thiết lập | Client yêu cầu lưu bản sao lưu khóa được mã hóa và hướng dẫn người dùng ghi nhớ hoặc lưu mã để khôi phục khi cần. |
| 4 | Người dùng chờ kết quả | Khi thiết lập hoàn tất và Server xác nhận lưu bản sao lưu, Client thông báo thành công và trở về ứng dụng. → Kết thúc Use Case. |

### Alternative Flows

- **Alt 1a — Thiết bị đã mở khóa**: Nếu thiết bị có khóa phù hợp và trạng thái mở khóa còn hợp lệ, Client sử dụng khóa đó; không yêu cầu nhập mã lại hoặc tự thay bản sao lưu. → Kết thúc Use Case.
- **Alt 1b — Đổi mã khóa**: Sau khi đã mở khóa bằng mã hiện tại, người dùng chọn xóa mã cũ và thiết lập mã mới. Người dùng nhập mã mới 6 số và xác nhận; hệ thống dùng mã mới để bảo vệ lại các khóa hiện có, nhằm giữ khả năng đọc phần lịch sử đã có đủ khóa. Chỉ thay bản sao lưu sau khi thiết lập mới thành công. Nếu hủy hoặc có lỗi trước khi thay thành công, giữ bản sao lưu và mã cũ; nếu mất phản hồi khi thay, không khẳng định mã nào đang có hiệu lực.
- **Alt 2a — Đã có bản sao lưu, cần khôi phục**: Thay vì tạo bản sao lưu mới, người dùng chọn khôi phục và nhập mã khóa 6 số đã thiết lập. Client dùng mã để khôi phục khóa từ bản sao lưu được mã hóa. Nếu thành công, Client thông báo kết quả và đồng bộ phần lịch sử có đủ dữ liệu và khóa để đọc. → Kết thúc Use Case.
- **Alt 2b — Để sau hoặc hủy**: Người dùng rời màn hình mà không hoàn tất. Client trở về ứng dụng; phiên đăng nhập vẫn hợp lệ và bản sao lưu hiện có được giữ nguyên. Người dùng có thể mở lại màn hình sau.
- **Alt 2c — Quên mã, không còn thiết bị đã mở khóa**: Người dùng chọn thiết lập lại mã. Client cảnh báo bản sao lưu khóa cũ sẽ được thay thế và lịch sử mã hóa cũ có thể không đọc lại được. Nếu người dùng xác nhận, nhập mã mới và nhập lại để xác nhận, hệ thống chuẩn bị khóa mới và lưu bản sao lưu mới. Chỉ thay bản sao lưu cũ khi bản mới đã được lưu thành công; không tự xóa tin nhắn. Người dùng chỉ sử dụng được từng cuộc chat mã hóa khi thiết bị có đủ khóa tương ứng.
- **Alt 2d — Không xác nhận thiết lập lại**: Người dùng chọn hủy tại cảnh báo. Client giữ nguyên bản sao lưu hiện có và trở về bước trước; không tạo mã hoặc khóa mới.

### Exception Flows

- **Ex 1a — Không xác định được trạng thái bản sao lưu**: Nếu không nhận được trạng thái đáng tin cậy, Client thông báo và cho thử lại. Không coi lỗi kiểm tra là không có bản sao lưu, không tạo bản mới để ghi đè.
- **Ex 3a — Không hoàn tất thiết lập hoặc lưu bản sao lưu**: Client thông báo chưa hoàn tất và cho người dùng thử lại khi phù hợp; không báo đã thiết lập thành công khi chưa xác nhận được kết quả.
- **Ex 3b — Bản sao lưu xuất hiện trong lúc thiết lập**: Nếu một thiết bị khác đã tạo bản sao lưu trước khi yêu cầu lưu được xử lý, Server không ghi đè bản hiện có. Client thông báo và hướng dẫn người dùng kiểm tra hoặc khôi phục.
- **Ex 2a — Mã khôi phục sai**: Client không báo đã khôi phục; thông báo để người dùng kiểm tra mã và thử lại. Sau 5 lần sai liên tiếp được hệ thống ghi nhận trên các thiết bị của tài khoản, tạm khóa nhập mã trên tất cả thiết bị trong 5 phút; hết thời gian bắt đầu đợt thử mới. Các thiết bị đã mở khóa không tự bị khóa lại hoặc đăng xuất. Không tự thay mã hoặc xóa bản sao lưu.
- **Ex 2d — Mã thiết lập chưa hợp lệ hoặc xác nhận không khớp**: Client yêu cầu người dùng sửa; không hoàn tất thiết lập và không tính là lần nhập sai khi khôi phục.
- **Ex 2b — Lỗi kết nối hoặc không đọc được bản sao lưu**: Client thông báo không thể hoàn tất và cho thử lại. Khi không kiểm tra được trạng thái khóa chung từ Server, không bắt đầu lần xác minh mã mới; lỗi kết nối không tự tính là nhập sai. Không coi bản sao lưu lỗi hoặc không truy cập được là chưa tồn tại.
- **Ex 2c — Phiên đăng nhập hết hạn hoặc bị thu hồi**: Hệ thống dừng thao tác theo chính sách phiên và yêu cầu người dùng đăng nhập lại. Nếu kết quả đang xử lý chưa rõ, Client không khẳng định bản sao lưu đã hoặc chưa được lưu.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Mỗi tài khoản có một mã khóa gồm 6 chữ số do người dùng thiết lập để khôi phục các khóa nhóm của tài khoản. Đây không phải mật khẩu đăng nhập hay mã xác minh email. Đổi hoặc khôi phục mật khẩu đăng nhập không tự khôi phục khóa hoặc lịch sử mã hóa.
  - **BR-02**: Chỉ thiết lập bản sao lưu lần đầu khi đã xác định chưa có bản sao lưu và bảo đảm không ghi đè nếu trạng thái thay đổi đồng thời. Bỏ qua, hủy hoặc lỗi khôi phục không tự xóa hay thay bản sao lưu hiện có. Đổi mã sau khi đã mở khóa phải giữ các khóa cần thiết để tiếp tục đọc phần lịch sử người dùng đã có quyền đọc. Thiết lập lại khi quên mã là luồng riêng: chỉ thay bản sao lưu cũ sau khi người dùng xác nhận cảnh báo và bản mới được lưu thành công.
  - **BR-03**: Đăng nhập và khôi phục khóa là hai việc riêng. Chọn để sau không đăng xuất; các chức năng không cần khóa vẫn có thể sử dụng.
  - **BR-04**: Thiết bị chỉ giải mã, hiển thị hoặc gửi tin E2EE khi đã có đủ khóa tương ứng. Không thay tin nhắn mã hóa bằng bản rõ. Cách cấp và chia sẻ khóa thuộc phương án E2EE theo [DEC-05](../../../../decisions/group-permissions-and-e2ee.md).
  - **BR-05**: Thiết lập mã mới khi quên mã không khôi phục khóa cũ. Nếu không còn mã và khóa cần thiết, không thể cam kết đọc lại lịch sử mã hóa cũ. Việc thiết lập lại mã không tự xóa tin nhắn.
  - **BR-06**: Server quản lý trạng thái và thời gian chờ chung cho tài khoản. Sau 5 lần xác minh mã sai liên tiếp được ghi nhận trên các thiết bị, tạm không cho bắt đầu lần thử mã mới trên mọi thiết bị của tài khoản trong 5 phút; hết thời gian bắt đầu đợt thử mới. Cần kết nối Server để kiểm tra trạng thái trước khi thử. Giới hạn này không khóa đăng nhập hoặc tự khóa lại thiết bị đã mở khóa.
  - **BR-07**: Sau khi người dùng đăng xuất, khóa lại trạng thái sử dụng khóa E2EE trên thiết bị đó; khi đăng nhập lại, người dùng cần nhập mã để mở. Thiết bị mới cần nhập mã để khôi phục khóa. Sau khi mở khóa, không yêu cầu nhập lại mã cho từng nhóm nếu thiết bị đã có đủ khóa tương ứng. Đăng nhập trên thiết bị mới không tự đăng xuất hoặc khóa các phiên trên thiết bị khác. Đóng và mở lại ứng dụng không tự yêu cầu mã nếu phiên đăng nhập và trạng thái mở khóa trên thiết bị vẫn hợp lệ; nếu khóa không còn khả dụng, người dùng cần nhập mã để khôi phục.
  - **BR-08**: Khi nhóm đang khóa hoặc thiết bị thiếu khóa cần thiết, không hiển thị nội dung tin nhắn của nhóm tại vùng xem trước; không tìm hoặc hiển thị kết quả tìm kiếm theo nội dung tin nhắn của nhóm đó. Người dùng vẫn có thể tìm nhóm theo tên. Khi nhóm chuyển sang trạng thái khóa, không tiếp tục hiển thị kết quả nội dung của nhóm đã tìm trước đó; việc này không xóa tin nhắn.
  - **BR-09**: Nếu người dùng còn quyền nhận tin của nhóm khi thiết bị đang khóa, Server tiếp tục lưu tin nhắn dưới dạng mã hóa theo chính sách lưu trữ; không loại bỏ tin chỉ vì thiết bị đang khóa. Ứng dụng chỉ báo có tin mới, không hiển thị nội dung. Sau khi mở khóa, người dùng đọc được những tin có đủ dữ liệu và khóa tương ứng.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Mã khôi phục và khóa bí mật không được ghi vào log, URL hoặc thông báo lỗi. Bản sao lưu khóa gửi lên Server phải được mã hóa; không gửi mã khôi phục hoặc khóa bí mật dạng rõ lên Server.
  - **NFR-02**: Hiển thị trạng thái xử lý và kết quả rõ ràng; không báo khôi phục hoặc thiết lập thành công khi chưa xác nhận được kết quả.

**Phạm vi bản nháp:** Mã 6 số là mã khôi phục duy nhất theo lựa chọn hiện tại. Nếu bản sao lưu mã hóa bị lấy, giới hạn thử trong ứng dụng không ngăn được việc dò mã ngoài ứng dụng. Nhóm kỹ thuật cần xác định cách ghi nhận và đồng bộ lần thử giữa các thiết bị mà không gửi mã hay khóa bí mật dạng rõ lên Server, kể cả khi có yêu cầu đồng thời. Giao thức E2EE, thư viện và cách cấp khóa cho thiết bị mới chưa được nhóm chốt; các luồng trên chưa khẳng định hệ thống đã triển khai các cơ chế đó.
