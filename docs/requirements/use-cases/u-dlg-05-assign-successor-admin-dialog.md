# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-DLG-05
- **Use Case Name**: Chọn Admin kế nhiệm
- **Author / Create by**: Kiet
- **Date Create**: 02/11/2026
- **Last Updated by** / Date: Kiet / 02/11/2026
- **Primary Actor**: User đang là Admin duy nhất của nhóm và muốn hạ quyền bản thân hoặc rời nhóm.
- **Secondary Actor(s)**: Thành viên được chọn làm Admin kế nhiệm; các thành viên còn lại nhận cập nhật vai trò/thành viên.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Desciption)

- **Brief Description**: Khi Admin duy nhất muốn hạ quyền bản thân hoặc rời nhóm vẫn còn thành viên khác, hệ thống yêu cầu chọn một thành viên hiện tại làm Admin kế nhiệm qua `U-DLG-05`. User chọn người kế nhiệm và xác nhận đúng thao tác ban đầu. Server cấp quyền cho người kế nhiệm và hạ quyền/cho User rời nhóm trong cùng một lần xử lý nhất quán, bảo đảm nhóm còn thành viên luôn có Admin.
- **Goal**: Hoàn tất việc hạ quyền hoặc rời nhóm của Admin hiện tại mà nhóm vẫn có người quản trị.
- **Trigger**: User chọn **Rời nhóm** hoặc thao tác **Chuyển quyền Admin** tại `U-SCR-09`, mở từ nhóm trong `U-SCR-07`; hệ thống xác định User là Admin duy nhất và nhóm vẫn còn thành viên khác.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - User đã đăng nhập bằng phiên hợp lệ, đang là thành viên và là Admin duy nhất của nhóm.
  - Nhóm đang hoạt động và còn ít nhất một thành viên khác ngoài User.
  - Hộp thoại được gắn với đúng định danh nhóm và thao tác ban đầu: **Rời nhóm** hoặc **Chuyển quyền Admin**.
  - Luồng cơ bản có kết nối tới server và có ít nhất một thành viên đủ điều kiện làm người kế nhiệm theo BR-03.
- **Post-conditions**:
  - Khi thành công: người được chọn trở thành Admin nhóm. Với luồng rời nhóm, User không còn là thành viên; với luồng hạ quyền, User vẫn là thành viên thường.
  - Cấp quyền kế nhiệm và thao tác ban đầu được ghi nhận cùng nhau; không báo thành công khi mới thực hiện một phần hoặc để nhóm còn thành viên nhưng không có Admin.
  - `U-SCR-07` và `U-SCR-09` cập nhật theo quyền/thành viên mới sau xác nhận của server.
  - Khi rời nhóm, User không được gửi/nhận hoặc giải mã tin mới; lịch sử từng được đọc được giữ ở chế độ chỉ đọc theo thiết kế hiện tại của `screen.md` và đề xuất DEC-05. Việc hạ quyền không làm User mất tư cách thành viên hoặc quyền đọc/gửi tin của thành viên thường.
  - Hủy trước khi gửi hoặc yêu cầu bị từ chối thì không đổi vai trò và không cho User rời nhóm qua yêu cầu đó.
  - Nếu chưa rõ kết quả do mất kết nối, giữ định danh yêu cầu để đối chiếu; không tự coi thao tác đã thành công hoặc thất bại.

## Luồng sự kiện (Flow of Events)

### Basic Flow

Luồng cơ bản: Admin duy nhất chọn người kế nhiệm rồi rời nhóm vẫn còn thành viên khác.

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Trong `U-SCR-09` của nhóm, chọn **Rời nhóm**. | Kiểm tra vai trò/thành viên hiện tại. Nếu User là Admin duy nhất và nhóm còn người khác, mở `U-DLG-05`; giải thích cần chọn Admin kế nhiệm trước khi rời. Chưa thay đổi vai trò hoặc tư cách thành viên. |
| 2 | Xem danh sách thành viên có thể kế nhiệm. | Tải danh sách từ server, hiển thị tên nhóm, username/họ tên của từng ứng viên, vùng người được chọn và các nút **Chuyển quyền & rời nhóm / Hủy**. Không tự chọn sẵn người kế nhiệm; chưa chọn thì nút xác nhận không khả dụng. |
| 3 | Chọn một thành viên làm Admin kế nhiệm. | Đánh dấu đúng một lựa chọn; hiển thị lại danh tính người đó và thông tin người được chọn sẽ trở thành Admin, còn User sẽ rời nhóm. Chỉ cho xác nhận khi lựa chọn còn hợp lệ. |
| 4 | Kiểm tra người kế nhiệm và nhấn **Rời nhóm**. | Gửi định danh nhóm, người kế nhiệm, loại thao tác và định danh yêu cầu; hiển thị đang xử lý, ngăn nhấn xác nhận lặp. Không gửi thêm yêu cầu rời nhóm độc lập. |
| 5 | Chờ kết quả. | Server xác thực phiên, kiểm tra lại vai trò User, số Admin, tư cách thành viên và điều kiện người kế nhiệm. Trong cùng lần xử lý nhất quán, cấp quyền Admin cho người được chọn và cho User rời nhóm, bảo đảm nhóm còn người có ít nhất một Admin; trả kết quả xác nhận. |
| 6 | Nhận thông báo **Đã chuyển quyền và rời nhóm**. | Đóng hộp thoại; cập nhật quyền/thành viên tại các client liên quan. Khung chat của User chuyển sang lịch sử chỉ đọc với nhãn **Bạn đã rời nhóm**; không tự gửi lại tin đang chờ hoặc bản nháp sau khi rời. Việc truy cập tin mới tuân theo BR-08. |

### Alternative Flows

#### Alt 1a — Chuyển quyền rồi hạ quyền bản thân

Thay bước 1 và điều chỉnh bước 2–6:

1. User chọn **Chuyển quyền Admin** tại `U-SCR-09`. Khi User là Admin duy nhất và nhóm còn thành viên khác, mở `U-DLG-05` ở chế độ hạ quyền.
2. Hệ thống hiển thị rõ User sẽ ở lại nhóm với vai trò thành viên thường; nút xác nhận là **Chuyển quyền Admin**.
3. User chọn đúng một thành viên đủ điều kiện và xác nhận.
4. Server kiểm tra lại điều kiện, cấp quyền Admin cho người kế nhiệm và hạ quyền User trong cùng một lần xử lý nhất quán; không xóa User khỏi nhóm.
5. Sau xác nhận thành công, đóng hộp thoại, cập nhật nhãn vai trò và ẩn các thao tác chỉ dành cho Admin tại `U-SCR-09`. User tiếp tục đọc/gửi tin theo quyền thành viên thường; giữ nguyên bản nháp và vị trí lịch sử đang xem.

#### Alt 3a — Đổi người kế nhiệm trước khi gửi

Tại bước 3, trước bước 4:

1. User chọn thành viên khác.
2. Hệ thống bỏ đánh dấu người cũ, chỉ giữ một người mới và cập nhật phần xác nhận.
3. Không cấp quyền cho bất kỳ ai chỉ từ việc chọn hoặc đổi lựa chọn; User kiểm tra lại trước khi gửi.

#### Alt 4a — Hủy trước khi gửi

Trước bước 4:

1. User nhấn **Hủy**, đóng hộp thoại hoặc nhấn Escape.
2. Hệ thống đóng hộp thoại, dừng thao tác hạ quyền/rời nhóm ban đầu; User vẫn là Admin và thành viên của nhóm.
3. Giữ nguyên hội thoại, lịch sử và bản nháp chat. Không tự chọn người khác hoặc gửi lại thao tác sau khi hủy.

#### Alt 5a — Đã có Admin khác khi kiểm tra lại

Tại bước 1–5:

1. Dữ liệu mới nhất cho thấy nhóm đã có ít nhất một Admin khác ngoài User.
2. Hệ thống thông báo không còn bắt buộc chọn kế nhiệm và cập nhật lại vai trò; nếu chưa ghi nhận yêu cầu thì không tự cấp quyền thêm cho người đang được chọn.
3. Đóng luồng chọn kế nhiệm và cho User xác nhận lại thao tác hạ quyền/rời nhóm theo luồng trực tiếp tại `U-SCR-09`; không tự thực hiện thao tác khác với nội dung vừa xác nhận.
4. Nếu yêu cầu đã được xử lý thành công trước đó, trả đúng kết quả đã ghi nhận theo BR-07; không áp dụng lại trên trạng thái mới.

#### Alt 5b — Nhóm chỉ còn User

Tại bước 1–5, khi chưa ghi nhận thay đổi:

1. Server xác định các thành viên khác đã rời/bị xóa và chỉ còn User.
2. Hệ thống bỏ yêu cầu chọn kế nhiệm; không cho chọn một người đã rời nhóm.
3. Nếu thao tác ban đầu là rời nhóm, quay về bước xác nhận rời của `U-SCR-09`; người cuối cùng được rời không cần kế nhiệm. Việc đóng nhóm và giữ lịch sử thuộc luồng rời nhóm hiện tại, không thực hiện âm thầm trong hộp thoại này.
4. Nếu thao tác ban đầu là hạ quyền, từ chối hạ quyền Admin cuối cùng khi User vẫn ở lại nhóm; User có thể hủy hoặc chủ động chọn rời nhóm qua luồng riêng.

#### Alt 5c — Đóng hộp thoại hoặc chuyển nhóm sau khi gửi

Sau bước 4, khi đang chờ:

1. User đóng hộp thoại hoặc chuyển sang hội thoại khác.
2. Hệ thống giữ yêu cầu đang chờ gắn với đúng nhóm, người kế nhiệm và loại thao tác; việc đóng hộp thoại không tự hủy yêu cầu trên server.
3. Phản hồi trả muộn chỉ cập nhật nhóm/yêu cầu tương ứng; không thay lựa chọn, bản nháp hoặc mở lại hộp thoại trong hội thoại mới.
4. Nếu chưa rõ kết quả, đối chiếu theo Ex 5c trước khi cho thử lại.

### Exception Flows

- **Ex 2a — Không tải được danh sách thành viên**: Hiển thị lỗi và nút tải lại; không coi lỗi tải là nhóm không có người kế nhiệm. Không gửi thao tác hạ quyền/rời nhóm khi chưa đối chiếu được dữ liệu cần thiết.
- **Ex 2b — Không có người kế nhiệm đủ điều kiện**: Hiển thị danh sách trống kèm lý do phù hợp; không tự chọn tài khoản ngoài nhóm. Nếu vẫn còn thành viên khác nhưng không có ứng viên hợp lệ, giữ User là Admin và cho hủy/tải lại. Nếu chỉ còn User, áp dụng Alt 5b.
- **Ex 3a — Chưa chọn hoặc chọn sai người**: Không cho gửi nếu chưa chọn đúng một người hợp lệ, chọn chính mình hoặc tham chiếu tài khoản không thuộc nhóm. Hiển thị lỗi và yêu cầu chọn lại.
- **Ex 5a — Người kế nhiệm không còn hợp lệ**: Nếu người được chọn đã rời/bị xóa khỏi nhóm, bị khóa hoặc xóa tài khoản trước lúc server xử lý, từ chối toàn bộ yêu cầu và tải lại danh sách. Không tự thay người hoặc chỉ thực hiện một phần yêu cầu; User giữ nguyên vai trò/tư cách thành viên đối với yêu cầu bị từ chối, chọn lại người và xác nhận lại.
- **Ex 5b — User hoặc nhóm không còn đủ điều kiện**: Nếu User đã mất quyền Admin, không còn là thành viên hoặc nhóm đã đóng/không tồn tại, từ chối yêu cầu mới và cập nhật giao diện theo trạng thái server. Không tự khôi phục quyền hoặc tư cách thành viên để tiếp tục. Nếu đây là lần gửi lại của yêu cầu đã thành công, trả kết quả cũ theo BR-07.
- **Ex 5c — Mất kết nối hoặc chưa nhận xác nhận**: Hiển thị chưa rõ kết quả, đối chiếu bằng định danh yêu cầu. Nếu đã hoàn tất, dùng đúng kết quả đó; nếu xác định chưa ghi nhận thay đổi, tải lại điều kiện và cho User xác nhận thử lại. Không tạo yêu cầu mới hoặc đổi người kế nhiệm khi kết quả yêu cầu cũ còn chưa rõ.
- **Ex 5d — Server từ chối hoặc lỗi ghi nhận**: Không báo thành công; cấp quyền và thao tác hạ quyền/rời nhóm phải cùng thành công hoặc cùng không thay đổi. Giữ lựa chọn còn hợp lệ để User kiểm tra và thử lại; không để nhóm còn người nhưng thiếu Admin do lỗi giữa chừng.
- **Ex 1a — Phiên không còn hợp lệ**: Ở bất kỳ bước nào, nếu phiên bị thu hồi/tài khoản User bị khóa, dừng thao tác cần xác thực và chuyển về đăng nhập. Không tự gửi lại bằng phiên mới; yêu cầu đã được server ghi nhận không bị coi là hủy chỉ vì phiên kết thúc.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

#### BR-01 — Điều kiện bắt buộc chọn kế nhiệm

- Hộp thoại dùng khi Admin duy nhất muốn hạ quyền bản thân hoặc rời nhóm đang có thành viên khác.
- Không bắt buộc khi còn Admin khác; người cuối cùng rời nhóm không cần kế nhiệm.
- Không cho Admin cuối cùng tự hạ quyền rồi ở lại nhóm mà không có người thay thế.
- Đây là bước hỗ trợ thao tác hạ quyền/rời nhóm; việc cấp quyền Admin thông thường cho thành viên không cần đi qua hộp thoại này.

#### BR-02 — Thành phần hộp thoại

- Hiển thị tên nhóm, lý do phải chọn kế nhiệm, danh sách ứng viên và người đang được chọn.
- Chọn đúng một người, không mặc định chọn sẵn. Danh tính người kế nhiệm phải đủ rõ để phân biệt tài khoản trùng họ tên.
- Nút xác nhận thể hiện đúng kết quả: **Rời nhóm** hoặc **Chuyển quyền Admin**; có nút **Hủy** và trạng thái đang xử lý.
- Không dùng lựa chọn của nhóm/thao tác trước cho một nhóm/thao tác mới.

#### BR-03 — Người kế nhiệm đủ điều kiện

- Là thành viên hiện tại của đúng nhóm, khác User, có tài khoản chưa bị khóa hoặc xóa; kiểm tra lại tại server khi xử lý.
- Chọn từ thành viên nhóm, không phải danh sách tất cả người dùng hoặc danh sách bạn bè. Không yêu cầu kết bạn hoặc đang Online để được cấp quyền.
- Quan hệ chặn không tự loại một thành viên hiện tại khỏi việc kế nhiệm; quy tắc chọn bạn bè/không chặn của luồng mời người mới không được áp sang việc đổi vai trò thành viên.
- Không có bước người kế nhiệm chấp nhận lời mời quyền trong use case này; quyền được ghi nhận sau khi Admin hiện tại xác nhận và server xử lý thành công.

#### BR-04 — Kết quả theo loại thao tác

- **Rời nhóm**: người kế nhiệm trở thành Admin, User rời nhóm và mất mọi quyền thành viên/Admin của nhóm.
- **Chuyển quyền Admin**: người kế nhiệm trở thành Admin, User ở lại làm thành viên thường; không thay đổi số thành viên.
- Người kế nhiệm nhận quyền Admin nhóm theo mô hình Admin ngang quyền hiện tại; không tạo vai trò chủ sở hữu hoặc cấp quyền Admin hệ thống.

#### BR-05 — Bảo vệ Admin cuối cùng và xử lý đồng thời

- Server kiểm tra vai trò, số Admin, thành viên và ứng viên trên trạng thái mới nhất trong cùng lần xử lý thay đổi.
- Cấp quyền cho người kế nhiệm và hạ quyền/cho User rời nhóm phải thành công cùng nhau. Không tách thành hai yêu cầu độc lập có thể chỉ hoàn tất một phần.
- Mọi thao tác đồng thời thay đổi vai trò/thành viên phải giữ điều kiện nhóm còn người luôn có ít nhất một Admin; không chỉ kiểm tra ở client trước khi gửi.
- Nếu điều kiện thay đổi, trả trạng thái phù hợp để cập nhật và xác nhận lại; không tự thay ứng viên hoặc chuyển sang thao tác khác.

#### BR-06 — Hủy và điều hướng

- Hủy trước khi gửi thì hủy cả ý định hạ quyền/rời nhóm đang chờ; không đổi quyền hoặc thành viên.
- Đóng hộp thoại sau khi gửi không đồng nghĩa đã hủy trên server. Chỉ cập nhật thành công sau xác nhận hoặc đối chiếu được kết quả.
- Hạ quyền thành công giữ User trong hội thoại, cập nhật thao tác theo quyền thành viên thường. Rời nhóm thành công chuyển khung chat sang trạng thái đã rời, không tự xóa lịch sử từng được đọc.

#### BR-07 — Nhất quán khi gửi lại

- Mỗi lần xác nhận gắn với định danh yêu cầu, nhóm, người kế nhiệm và loại thao tác. Server xử lý lặp an toàn, không cấp quyền hoặc cho rời/hạ quyền lần thứ hai do nhấn lặp/reconnect.
- Nếu đã ghi nhận thành công, lần gửi lại trả đúng kết quả cũ, kể cả User đã mất quyền Admin hoặc rời nhóm do chính yêu cầu đó. Chỉ chủ yêu cầu được đối chiếu kết quả; không vì vậy cấp lại quyền đọc nhóm.
- Cùng định danh nhưng người kế nhiệm/loại thao tác khác phải bị từ chối. Khi chưa rõ kết quả, đối chiếu yêu cầu cũ trước khi cho thay lựa chọn hoặc tạo yêu cầu mới.

#### BR-08 — Quyền truy cập tin và E2EE

- Hạ quyền Admin không xóa User khỏi nhóm hoặc tự tước khả năng đọc/gửi tin của thành viên thường.
- Khi rời nhóm, cập nhật quyền và trạng thái khóa/thành viên phải ngăn User gửi/nhận hoặc giải mã tin mới; không tự gửi lại tin đang chờ sau khi rời.
- Nếu phần khóa của nhóm cần xử lý tiếp, hiển thị trạng thái tương ứng và chỉ cho trao đổi tin theo quyền/khóa đã sẵn sàng; không gửi bản rõ để thay thế. Không coi việc cấp quyền Admin là đã chia sẻ hoặc khôi phục xong khóa.
- Không cam kết xóa được lịch sử/khóa cũ đã có trên thiết bị người rời. Giao thức và cơ chế đổi khóa cụ thể vẫn theo DEC-05 chưa chốt; use case không tự chọn thư viện mật mã.

### Non-Functional Requirements (NFR)

#### NFR-01 — Phản hồi giao diện

- Tải danh sách, gửi yêu cầu và cập nhật quyền/thành viên không làm đóng băng JavaFX.
- Phân biệt đang tải, không có ứng viên hợp lệ, chưa chọn, đang xử lý, thất bại và chưa rõ kết quả.
- Hiển thị rõ người kế nhiệm và kết quả của thao tác trước khi xác nhận; ngăn gửi lặp khi đang chờ.

#### NFR-02 — Phân quyền và bảo mật

- Server lấy danh tính User từ phiên và kiểm tra quyền ở mỗi thao tác; không chỉ tin ID, vai trò hoặc danh sách do client gửi.
- Không để client cũ tiếp tục dùng quyền Admin sau khi bị hạ quyền hoặc quyền thành viên sau khi rời nhóm.
- Không gửi khóa bí mật/bản rõ tin nhóm trong yêu cầu chuyển quyền hoặc ghi token/khóa vào log và thông báo lỗi.

#### NFR-03 — Toàn vẹn và cập nhật bất đồng bộ

- Kết quả thay đổi vai trò và thành viên phải nhất quán trước khi trả thành công; kiểm soát các yêu cầu đồng thời theo BR-05.
- Client liên quan cập nhật nhãn vai trò, danh sách thành viên và quyền thao tác theo xác nhận của server; khi reconnect phải đồng bộ trạng thái mới nhất.
- Phản hồi trả muộn gắn với đúng nhóm/yêu cầu, không thay lựa chọn hoặc bản nháp ở nhóm khác.

#### NFR-04 — Bảo toàn lựa chọn và dữ liệu chat

- Lỗi giữ lựa chọn còn có thể đối chiếu; khi dữ liệu thay đổi phải kiểm tra lại và yêu cầu xác nhận, không tự dùng lựa chọn cũ như vẫn hợp lệ.
- Mở/hủy hộp thoại không làm mất bản nháp chat. Sau khi rời, bản nháp/tin đang chờ không được tự phát đi; không cam kết khôi phục khả năng gửi khi User đã mất tư cách thành viên.
- Chưa có ngưỡng độ trễ riêng cho chuyển quyền được chốt; hiển thị tiến độ/trạng thái chờ và cơ chế đối chiếu sau timeout phù hợp.
