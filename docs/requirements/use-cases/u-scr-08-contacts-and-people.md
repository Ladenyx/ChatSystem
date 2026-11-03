# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-SCR-08
- **Use Case Name**: Quản lý bạn bè và kết nối người dùng
- **Author / Create by**: Kiet
- **Date Create**: 30/10/2026
- **Last Updated by** / Date: Kiet / 30/10/2026
- **Primary Actor**: User đã đăng nhập.
- **Secondary Actor(s)**: Người được tìm kiếm; người gửi/nhận lời mời kết bạn; bạn bè được chọn để nhắn tin hoặc tạo nhóm.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User xem và lọc danh sách bạn bè, xem bạn bè đang Online, tìm người dùng, gửi và xử lý lời mời kết bạn, hủy kết bạn, chặn và quản lý danh sách đã chặn/bỏ chặn. Từ danh sách phù hợp, User có thể mở chat cá nhân hoặc hộp thoại tạo nhóm; thao tác chặn có thể thực hiện trực tiếp trong danh sách bạn bè hoặc trong panel thông tin mở bằng nút **i** của khung chat.
- **Goal**: User quản lý đúng quan hệ bạn bè/chặn và kết nối với người dùng khác theo quyền hiện tại.
- **Trigger**: User chọn **Bạn bè & Kết nối** trên thanh điều hướng của khung làm việc chính.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - User đã đăng nhập bằng phiên hợp lệ và được phép vào khung làm việc chính.
  - Kết nối tới server hoạt động đối với các thao tác tìm kiếm, cập nhật quan hệ hoặc tạo nhóm.
  - Luồng cơ bản giả định User có ít nhất một người bạn, hai bên không có quan hệ chặn và người bạn vẫn có thể được liên hệ.
  - Các trường hợp danh sách trống, chưa kết bạn, có quan hệ chặn hoặc dữ liệu thay đổi được xử lý ở các luồng thay thế/ngoại lệ.
- **Post-conditions**:
  - Khi xem thành công: phân mục được chọn hiển thị danh sách đúng phạm vi và điều kiện lọc, kèm trạng thái quan hệ/thao tác phù hợp.
  - Khi gửi lời mời thành công: lời mời ở trạng thái chờ xử lý; hai bên chưa trở thành bạn bè.
  - Khi đồng ý lời mời thành công: hai bên trở thành bạn bè; lời mời không còn trong danh sách chờ.
  - Khi từ chối lời mời thành công: lời mời được xử lý và loại khỏi danh sách chờ; không tạo quan hệ bạn bè.
  - Khi hủy kết bạn thành công: quan hệ bạn bè bị gỡ ở cả hai phía; không tự chặn, xóa lịch sử chat hoặc rời nhóm chung.
  - Khi chặn thành công: quan hệ chặn được ghi nhận, tình bạn và lời mời đang chờ giữa hai bên được hủy; các danh sách liên quan cập nhật tương ứng.
  - Khi bỏ chặn thành công: chỉ gỡ quan hệ chặn do chính User tạo; không tự khôi phục tình bạn/lời mời hoặc gửi lại các tin đã lỗi do chặn.
  - Khi chọn Nhắn tin/Tạo nhóm: chuyển tới hội thoại hoặc hộp thoại tương ứng; chưa coi tin đã gửi hoặc nhóm đã tạo chỉ từ thao tác điều hướng.
  - Nếu thao tác bị hủy/thất bại: không báo thành công hoặc giả định quan hệ đã đổi; trạng thái được đối chiếu lại với server khi chưa rõ kết quả.

## Luồng sự kiện (Flow of Events)

### Basic Flow

Luồng cơ bản: xem danh sách bạn bè, lọc theo tên và mở hội thoại cá nhân.

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn **Bạn bè & Kết nối** trên thanh điều hướng. | Mở `U-SCR-08`, hiển thị 5 phân mục: **Tất cả bạn bè**, **Đang Online**, **Lời mời kết bạn**, **Tìm người dùng**, **Đã chặn**. |
| 2 | Chọn **Tất cả bạn bè**. | Tải danh sách bạn bè hiện tại, hiển thị tên, trạng thái Online/Offline và các thao tác **Nhắn tin**, **Hủy kết bạn**, **Chặn**. |
| 3 | Nhập tên vào ô lọc của danh sách bạn bè. | Hiển thị các bạn bè khớp tên trong phân mục hiện tại; không tìm người ngoài danh sách bạn bè. |
| 4 | Chọn **Nhắn tin** ở một người bạn. | Mở `U-SCR-07` với hội thoại cá nhân tương ứng; nếu đã có hội thoại thì dùng hội thoại đó. Việc người bạn Offline không ngăn mở chat. |
| 5 | Tiếp tục xem hoặc soạn tin trong hội thoại. | Chuyển sang use case `UC-U-SCR-07`; việc gửi tin và trạng thái gửi tuân theo quyền thực tế tại thời điểm xử lý. |

### Alternative Flows

#### Alt 2a — Xem bạn bè đang Online (`U-11`)

Tại bước 1–2:

1. User chọn **Đang Online**.
2. Hệ thống chỉ hiển thị những người đang là bạn bè và có trạng thái Online, kèm **Nhắn tin** và **Tạo nhóm**.
3. User nhập tên để lọc trong danh sách Online, chọn **Nhắn tin** để tiếp tục bước 4 hoặc **Tạo nhóm** để chuyển sang Alt 4b.
4. Khi trạng thái Online thay đổi, hệ thống cập nhật danh sách. Nếu người được chọn vừa Offline nhưng vẫn đủ quyền liên hệ, không từ chối mở chat/tạo nhóm chỉ vì thay đổi trạng thái hiện diện.

#### Alt 2b — Tìm người dùng và gửi lời mời kết bạn (`U-07`)

Tại bước 1–2:

1. User chọn **Tìm người dùng**, nhập `username` hoặc `họ tên`.
2. Hệ thống tìm theo tiêu chí đã nhập; không trả về tài khoản User đang chủ động chặn hoặc tài khoản đang chặn User. Các thao tác trên kết quả phản ánh quan hệ hiện tại.
3. Với người chưa kết bạn, chưa có lời mời đang chờ và không có quan hệ chặn hai chiều, User nhấn **Kết bạn**.
4. Server kiểm tra lại điều kiện và tạo lời mời chờ xử lý. Hệ thống hiển thị **Đã gửi lời mời** thay cho thao tác gửi lại; lời mời xuất hiện ở phân mục **Lời mời kết bạn** của người nhận.
5. Hai bên chỉ trở thành bạn bè khi người nhận đồng ý theo Alt 2c. Gửi lời mời không yêu cầu người nhận đang Online.
6. Nếu đã là bạn bè, không tạo lời mời mới. Nếu đã có lời mời gửi đi, giữ trạng thái chờ; nếu có lời mời nhận vào từ người đó, cho User chuyển sang **Lời mời kết bạn** để đồng ý hoặc từ chối, không tự đồng ý chỉ vì User nhấn Kết bạn.
7. Nếu User đang chủ động chặn người được tìm, tài khoản đó không xuất hiện trong kết quả **Tìm người dùng**, kể cả khi nhập chính xác `username`; không hiển thị dòng kết quả kèm nút Kết bạn hoặc Bỏ chặn. Trong `U-SCR-08`, User phải vào mục **Đã chặn** để xem tài khoản này và bỏ chặn nếu muốn. Nếu không có kết quả phù hợp, hiển thị trạng thái không tìm thấy theo Ex 2b.

#### Alt 2c — Đồng ý lời mời kết bạn (`U-08`)

Tại bước 1–2:

1. User chọn **Lời mời kết bạn**.
2. Hệ thống hiển thị các lời mời User đã nhận và còn chờ xử lý, kèm **Đồng ý** và **Từ chối**; User có thể lọc người gửi theo tên.
3. User chọn **Đồng ý** ở một lời mời.
4. Server xác nhận User là người nhận, lời mời còn hiệu lực và hai bên không có quan hệ chặn.
5. Hệ thống tạo quan hệ bạn bè cho hai bên, loại lời mời khỏi danh sách chờ và cập nhật **Tất cả bạn bè**; người bạn mới cũng xuất hiện trong **Đang Online** nếu đang trực tuyến.

#### Alt 2d — Từ chối lời mời kết bạn (`U-08`)

Tại bước 3 của Alt 2c:

1. User chọn **Từ chối**.
2. Server kiểm tra quyền và trạng thái lời mời, ghi nhận từ chối rồi loại lời mời khỏi danh sách chờ.
3. Hệ thống không tạo tình bạn hoặc quan hệ chặn. Hai bên vẫn có thể chat riêng nếu đủ điều kiện; từ chối lời mời không đồng nghĩa chặn.

#### Alt 4a — Nhắn tin với người chưa kết bạn

Tại phân mục **Tìm người dùng**:

1. User chọn **Nhắn tin riêng** ở một kết quả hợp lệ, kể cả khi chưa kết bạn hoặc lời mời còn chờ xử lý.
2. Hệ thống mở hội thoại cá nhân tương ứng trong `U-SCR-07`; không buộc User phải kết bạn trước.
3. Nếu quan hệ chặn phát sinh sau khi kết quả được tải, cập nhật và xử lý theo Ex 4a; việc mở hội thoại không bảo đảm gửi tin thành công.

#### Alt 4b — Mở hộp thoại tạo nhóm với bạn bè (`U-19`)

Tại phân mục **Đang Online** hoặc kết quả **Tìm người dùng** đang là bạn bè:

1. User chọn **Tạo nhóm** với một người bạn.
2. Hệ thống mở `U-DLG-02` và đưa người bạn được chọn vào danh sách thành viên dự kiến. User nhập tên nhóm và có thể chọn thêm bạn bè.
3. Chỉ cho tạo nhóm khi có tối thiểu một thành viên khác được chọn; mỗi người được chọn phải đang là bạn bè và không có quan hệ chặn hai chiều với người tạo tại thời điểm server xử lý. Online không phải điều kiện bắt buộc của thành viên.
4. Khi tạo thành công, nhóm mặc định E2EE, người tạo là Admin đầu tiên; hệ thống mở hội thoại nhóm ở `U-SCR-07`. Việc chia sẻ khóa/lịch sử tuân theo DEC-05, không coi thành viên đã giải mã được lịch sử chỉ từ việc nhóm được tạo.
5. Quan hệ chặn giữa các thành viên được mời với nhau không ngăn người tạo đủ điều kiện mời họ. Khi mở nhóm có quan hệ chặn liên quan đến User hiện tại, áp dụng banner tại Alt 2d của `UC-U-SCR-07`.
6. Nếu User hủy hộp thoại, quay lại màn hình hiện tại và không tạo nhóm. Các bước xử lý chi tiết của hộp thoại thuộc `U-DLG-02`.

#### Alt 4c — Hủy kết bạn (`U-09`)

Tại bước 2–3, trong **Tất cả bạn bè**:

1. User chọn **Hủy kết bạn** ở một người bạn.
2. Hệ thống yêu cầu xác nhận; nếu User hủy, giữ nguyên quan hệ.
3. Khi User xác nhận, server kiểm tra quan hệ hiện tại và gỡ tình bạn ở cả hai phía.
4. Hệ thống loại người đó khỏi **Tất cả bạn bè** và **Đang Online**. Hội thoại/lịch sử cũ và tư cách thành viên nhóm chung được giữ; chat riêng vẫn hoạt động khi không có quan hệ chặn.

#### Alt 4d — Chặn người dùng từ danh sách bạn bè hoặc khung chat (`U-10`)

Tại bước 2–3 trong **Tất cả bạn bè**, hoặc sau bước 4 khi đã mở hội thoại cá nhân trong `U-SCR-07`:

1. User chọn **Chặn** ngay tại tài khoản trong danh sách bạn bè. Hoặc, từ hội thoại cá nhân, User nhấn **i** để mở panel `U-SCR-09`, rồi chọn **Chặn người dùng** trong mục **Quyền riêng tư & hỗ trợ**.
2. Với cả hai điểm thao tác, hệ thống yêu cầu xác nhận, giải thích chặn sẽ hủy tình bạn/lời mời đang chờ và ngăn trao đổi tin riêng mới; lịch sử cũ và nhóm chung được giữ. Nếu User hủy, không thay đổi quan hệ.
3. Sau xác nhận, server ghi nhận chặn, đồng thời hủy tình bạn và các lời mời đang chờ giữa hai bên.
4. Hệ thống loại người đó khỏi danh sách bạn bè/Online và kết quả **Tìm người dùng**, cập nhật danh sách lời mời và thêm vào **Đã chặn** của User trong `U-SCR-08`. Khi một trong hai bên tìm người dùng, tài khoản của bên còn lại không xuất hiện trong kết quả. Nếu chặn từ danh sách bạn bè, User tiếp tục ở danh sách đó, không bắt buộc mở khung chat.
5. Nếu mở chat riêng: phía chủ động chặn thấy **Bỏ chặn / Tìm hiểu thêm** thay cho thanh soạn; phía chỉ bị chặn vẫn soạn và nhấn Gửi được nhưng tin báo **Không thể gửi**. Tin bị từ chối không được chuyển hoặc phát thông báo cho người chặn, không tự gửi lại khi bỏ chặn.
6. Trong nhóm chung, hai bên vẫn đọc/gửi tin; hiển thị banner cho User có quan hệ chặn liên quan theo `UC-U-SCR-07`. Không tự rời hoặc xóa thành viên khỏi nhóm chỉ vì thao tác chặn.

#### Alt 2e — Xem danh sách đã chặn và bỏ chặn (`U-10`)

Tại bước 1–2:

1. User chọn **Đã chặn**.
2. Hệ thống hiển thị các tài khoản do chính User đang chặn, kèm nút **Bỏ chặn**. Đây không phải danh sách những người đã chặn User.
3. User nhấn **Bỏ chặn** ở một tài khoản; server gỡ quan hệ chặn do User tạo.
4. Sau xác nhận thành công, hệ thống loại tài khoản đó khỏi **Đã chặn** và cập nhật các màn hình liên quan. Tài khoản có thể xuất hiện lại trong **Tìm người dùng** nếu đối phương không còn chặn User và đáp ứng các điều kiện tìm kiếm khác. Nếu đang mở chat riêng, thanh soạn xuất hiện lại.
5. Không tự khôi phục tình bạn/lời mời cũ hoặc tự gửi lại tin lỗi do chặn. Muốn kết bạn lại phải gửi và chấp nhận lời mời mới khi đủ điều kiện.
6. Nếu đối phương vẫn chặn User, quan hệ chặn ở phía đối phương được giữ: tài khoản đối phương vẫn bị ẩn khỏi tìm kiếm của User; trong hội thoại cũ, User có thể soạn nhưng gửi báo **Không thể gửi**.

#### Alt 3a — Xóa bộ lọc hoặc chỉ xem danh sách

Tại bước 3 hoặc các phân mục có ô lọc:

1. User xóa nội dung lọc; hệ thống khôi phục danh sách trong phân mục hiện tại, không trộn với các phân mục khác.
2. User có thể đổi phân mục hoặc rời màn hình sau khi xem; không bắt buộc gửi lời mời, nhắn tin hay cập nhật quan hệ để kết thúc use case.

### Exception Flows

- **Ex 1a — Không tải được dữ liệu**: Tại bước 1–3 hoặc khi chuyển phân mục/tìm kiếm, hệ thống thông báo lỗi và cho thử lại; giữ điều kiện lọc và dữ liệu đã tải khi có thể. Không hiển thị lỗi tải thành kết quả rỗng hoặc giả định người dùng đã bị hủy kết bạn/chặn.
- **Ex 2a — Danh sách trống**: Khi không có bạn bè, không có bạn Online, không có lời mời chờ hoặc chưa chặn ai, hệ thống hiển thị thông báo đúng phân mục. Nếu chỉ không khớp bộ lọc thì thông báo không có kết quả phù hợp; không khẳng định danh sách gốc trống.
- **Ex 2b — Không tìm thấy người dùng**: Tại Alt 2b, hiển thị **Không tìm thấy người dùng phù hợp** khi không có kết quả được phép hiển thị, kể cả khi từ khóa khớp tài khoản User đang chặn hoặc đang chặn User. Không hiển thị tài khoản bị loại kèm lý do chặn/bị chặn hoặc nút Bỏ chặn trong kết quả tìm kiếm; việc xem và bỏ chặn tài khoản do User chặn nằm ở mục **Đã chặn**. Chuỗi tìm kiếm rỗng không gửi yêu cầu tìm kiếm và không hiển thị toàn bộ người dùng hệ thống.
- **Ex 2c — Lời mời/quan hệ đã thay đổi**: Tại Alt 2b/2c/2d, nếu lời mời đã được xử lý/hủy, hai bên đã là bạn hoặc một bên vừa chặn, server kiểm tra lại và không tạo lời mời/tình bạn trùng. Hệ thống cập nhật trạng thái hiện tại, không báo đã kết bạn khi lời mời không còn hợp lệ. Trường hợp có lời mời ngược chiều được xử lý theo bước 6 của Alt 2b.
- **Ex 2d — Không có quyền thao tác**: Không cho tự gửi lời mời cho mình, xử lý lời mời không gửi tới mình hoặc bỏ chặn thay người khác. Server xác thực người thao tác và điều kiện quan hệ; giao diện không phải nơi kiểm tra quyền duy nhất.
- **Ex 4a — Quan hệ chặn thay đổi khi mở chat**: Nếu User vừa chặn đối phương, chat riêng hiển thị **Bỏ chặn / Tìm hiểu thêm**. Nếu User vừa bị đối phương chặn, cập nhật kết quả tìm kiếm nhưng vẫn giữ hành vi chat cũ: không vô hiệu hóa thanh soạn chỉ vì bị chặn; yêu cầu gửi bị server từ chối và tin báo **Không thể gửi**.
- **Ex 4b — Thành viên dự kiến không còn đủ điều kiện**: Tại Alt 4b, nếu một người không còn là bạn hoặc có quan hệ chặn với người tạo, server từ chối tạo nhóm theo danh sách đó, thông báo cần cập nhật lựa chọn và giữ tên nhóm/các lựa chọn còn hợp lệ để User sửa. Không báo tạo nhóm thành công khi chưa có xác nhận; không tự loại người rồi tạo nhóm ngoài danh sách User đã xác nhận.
- **Ex 4c — Cập nhật quan hệ thất bại hoặc chưa rõ kết quả**: Với gửi/xử lý lời mời, hủy kết bạn, chặn/bỏ chặn, nếu server từ chối thì thông báo và giữ trạng thái đã được xác nhận. Nếu mất kết nối sau khi gửi yêu cầu, đối chiếu trạng thái với server trước khi thực hiện lại; không tự tạo thêm lời mời hoặc giả định đã chặn/bỏ chặn thành công.
- **Ex 4d — Tài khoản đích không còn khả dụng**: Nếu tài khoản vừa bị khóa/xóa hoặc không còn được phép tương tác, server từ chối thao tác, hệ thống thông báo không thể thực hiện và tải lại danh sách phù hợp. Không dùng kết quả tìm kiếm cũ để bỏ qua trạng thái tài khoản hiện tại.
- **Ex 1b — Phiên không còn hợp lệ**: Ở bất kỳ bước nào, nếu phiên bị thu hồi hoặc tài khoản bị khóa, hệ thống dừng các thao tác cần xác thực, thông báo và chuyển về màn hình đăng nhập; không tiếp tục cập nhật quan hệ bằng phiên cũ.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

#### BR-01 — Phạm vi phân mục

- Có đúng 5 phân mục theo `U-SCR-08`.
- **Tất cả bạn bè** và **Đang Online** chỉ chứa bạn bè hiện tại;
- **Lời mời kết bạn** chứa lời mời nhận vào còn chờ;
- **Đã chặn** chứa các tài khoản do chính User chặn.

#### BR-02 — Lọc và tìm kiếm

- Lọc theo tên trong danh sách bạn bè/Online/lời mời không mở rộng phạm vi danh sách.
- **Tìm người dùng** tìm theo `username` hoặc `họ tên`, loại khỏi kết quả cả tài khoản User chủ động chặn và tài khoản đang chặn User, kể cả khi tìm chính xác username.
- Trong `U-SCR-08`, muốn xem/bỏ chặn tài khoản do mình chặn phải vào **Đã chặn**.
- Quy tắc ẩn khỏi tìm người dùng không xóa hội thoại/lịch sử cũ hoặc ẩn tin trong nhóm chung.
- Không tự đặt ngưỡng ký tự, quy tắc so khớp dấu/hoa thường, kích thước trang hay thời gian phản hồi khi chưa có yêu cầu chốt.

#### BR-03 — Lời mời và tình bạn

- Gửi lời mời không tự tạo tình bạn;
- chỉ người nhận được đồng ý/từ chối.
- Không tạo lời mời đang chờ hoặc tình bạn trùng cho cùng cặp tài khoản.
- Khi có lời mời ngược chiều, hướng người dùng xử lý lời mời nhận vào, không tự đồng ý.

#### BR-04 — Hủy kết bạn

- Gỡ tình bạn hai chiều nhưng không tự chặn, xóa lịch sử hoặc rời nhóm chung.
- Theo DEC-03 đang ở mức đề xuất, chat riêng không bắt buộc đã kết bạn.

#### BR-05 — Chặn

- User có thể chọn **Chặn** trực tiếp trong danh sách **Tất cả bạn bè** của `U-SCR-08` hoặc chọn **Chặn người dùng** trong mục **Quyền riêng tư & hỗ trợ** của panel `U-SCR-09`, mở bằng nút **i** trong chat cá nhân `U-SCR-07`.
- Cả hai điểm thao tác dùng cùng quy trình xác nhận và cập nhật quan hệ.
- Chặn hủy tình bạn và lời mời đang chờ hai chiều, ngăn lời mời mới và chuyển tin riêng mới;
- giữ lịch sử cũ.
- Phải áp dụng thay đổi quan hệ nhất quán, không để vừa chặn thành công vừa còn tình bạn/lời mời chờ của cùng cặp người dùng.

#### BR-06 — Hai phía trong chat riêng

- Người chủ động chặn thấy hai nút **Bỏ chặn / Tìm hiểu thêm** thay thanh soạn.
- Người chỉ bị chặn vẫn soạn/nhấn Gửi được nhưng tin báo **Không thể gửi**.
- Nếu cả hai cùng chặn, mỗi bên thấy giao diện người chủ động chặn cho đến khi tự bỏ chặn.
- Chi tiết tại `UC-U-SCR-07`.

#### BR-07 — Bỏ chặn

- Chỉ gỡ quan hệ chặn do User tạo;
- không tự khôi phục bạn bè/lời mời hoặc gửi lại tin đã lỗi.
- Chat riêng chỉ gửi thành công khi không còn bên nào chặn bên còn lại.

#### BR-08 — Tạo nhóm

- Cần ít nhất một người khác đang là bạn bè, không có quan hệ chặn hai chiều với người tạo.
- Chỉ kiểm tra cặp người tạo–người được mời;
- chặn giữa những người được mời với nhau không tự ngăn tạo nhóm.
- Nhóm mặc định E2EE, người tạo là Admin đầu tiên.
- Các quy tắc chi tiết về nhóm/khóa vẫn theo DEC-03/DEC-05 đang đề xuất.

#### BR-09 — Nhóm chung

- Chặn/hủy kết bạn không thay đổi tư cách thành viên hoặc quyền Admin nhóm.
- Hai bên vẫn thấy và gửi tin trong nhóm;
- banner xuất hiện cho User có quan hệ chặn liên quan theo `UC-U-SCR-07`.

#### BR-10 — Trạng thái hiện diện

- Online/Offline là thông tin hiện diện, không thay thế kiểm tra tình bạn/chặn.
- Người nhận lời mời, người được nhắn tin hoặc người được mời vào nhóm không bắt buộc Online;
- mục **Đang Online** chỉ là bộ lọc bạn bè trực tuyến.

#### BR-11 — Kiểm tra tại server

- Mọi thao tác cập nhật quan hệ phải kiểm tra phiên, quyền và dữ liệu mới nhất.
- Không suy ra quyền từ việc nút đang hiển thị hoặc từ danh sách tải trước đó;
- đối chiếu lại trạng thái khi chưa rõ kết quả thao tác.

### Non-Functional Requirements (NFR)

#### NFR-01 — Phản hồi giao diện

- Tải/lọc/tìm kiếm và cập nhật quan hệ có trạng thái chờ phù hợp, không làm đóng băng JavaFX.
- Phân biệt rõ đang tải, danh sách trống, không khớp bộ lọc và lỗi.

#### NFR-02 — Nhất quán dữ liệu

- Sau xác nhận thành công, cập nhật các phân mục liên quan và giao diện chat đang mở.
- Thay đổi từ phía đối phương được cập nhật khi nhận sự kiện hoặc tải lại;
- kết quả tìm kiếm cũ không được ghi đè điều kiện tìm mới.

#### NFR-03 — Chống thao tác lặp

- Ngăn nhấn lặp khi yêu cầu cùng thao tác đang xử lý;
- server xử lý lặp an toàn, không tạo lời mời/tình bạn trùng.
- Nếu chưa rõ kết quả, tải lại trạng thái trước khi cho thực hiện lại.

#### NFR-04 — Quyền truy cập

- Server kiểm soát việc tìm kiếm và thay đổi quan hệ.
- Kết quả tìm người dùng phải loại tài khoản có quan hệ chặn với User theo cả hai chiều ngay tại server, không chỉ ẩn trên giao diện trong khi vẫn trả tài khoản đó trong dữ liệu tìm kiếm.
- Khi quan hệ chặn thay đổi, cập nhật kết quả đã tải để không giữ lại tài khoản vừa bị loại.

#### NFR-05 — Bảo toàn dữ liệu

- Hủy kết bạn/chặn/bỏ chặn không tự xóa lịch sử, mất nháp đang soạn hoặc gửi lại các tin đã bị từ chối.
- Khi thao tác thất bại, giữ điều kiện lọc và dữ liệu nhập còn phù hợp để User tiếp tục.

#### NFR-06 — Khả năng nhận biết

- Trạng thái Online/Offline, lời mời chờ và kết quả thao tác cần có nhãn văn bản rõ ràng, không chỉ phân biệt bằng màu sắc.
- Các mục tiêu độ trễ riêng của màn hình này chưa được chốt, không mặc định áp dụng ngưỡng tìm nội dung tin nhắn của DEC-09 cho tìm người dùng.
