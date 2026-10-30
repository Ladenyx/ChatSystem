# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-SCR-09
- **Use Case Name**: Xem thông tin và quản lý đoạn chat
- **Author / Create by**: Kiet
- **Date Create**: 30/10/2026
- **Last Updated by** / Date: Kiet / 30/10/2026
- **Primary Actor**: User đã đăng nhập và đang mở một hội thoại; có thể là thành viên thường hoặc Admin nhóm.
- **Secondary Actor(s)**: Người tham gia chat cá nhân; các thành viên/Admin nhóm khác; Admin hệ thống tiếp nhận báo cáo spam.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User mở panel thông tin bên phải khung chat để xem thông tin hội thoại, tìm tin nhắn trong hội thoại, xóa toàn bộ lịch sử phía mình và báo cáo spam. Với chat cá nhân, User có thể chặn đối phương. Với nhóm, User xem thành viên, thông tin E2EE, thêm thành viên theo quyền và rời nhóm; Admin có thêm quyền đổi tên, cấp/thu hồi quyền Admin và xóa thành viên.
- **Goal**: User tra cứu và quản lý đúng hội thoại đang chọn, thực hiện các thao tác phù hợp với vai trò hiện tại mà không làm ảnh hưởng dữ liệu hoặc quyền ngoài phạm vi cho phép.
- **Trigger**: User nhấn nút **i** trên header của hội thoại đang mở trong `U-SCR-07`.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - User đã đăng nhập bằng phiên hợp lệ và đang chọn một hội thoại trong `U-SCR-07`.
  - User có quyền xem thông tin/lịch sử tương ứng của hội thoại đó.
  - Với thao tác quản trị hoặc rời nhóm, User còn là thành viên hiện tại; thao tác chỉ dành cho Admin yêu cầu vai trò Admin tại thời điểm server xử lý.
  - Tìm nội dung tin nhóm hoặc chọn bằng chứng báo cáo cần bản rõ đã được giải mã bằng khóa hợp lệ; không coi đăng nhập thành công là đã có khóa.
  - Luồng cơ bản giả định kết nối hoạt động và thông tin hội thoại tải được. Các trường hợp thiếu khóa, dữ liệu cũ hoặc mất quyền được xử lý ở luồng ngoại lệ.
- **Post-conditions**:
  - Khi xem thành công: panel hiển thị đúng thông tin và thao tác theo loại hội thoại/vai trò hiện tại.
  - Khi tìm kiếm thành công: khung giữa mở đúng trang lịch sử, cuộn tới và highlight tin đích được phép đọc.
  - Khi xóa lịch sử thành công: lịch sử cũ bị ẩn và loại khỏi tìm kiếm của riêng User; lịch sử của người khác và quyền thành viên không thay đổi.
  - Khi chặn thành công: tình bạn/lời mời giữa hai bên được hủy, danh sách đã chặn và giao diện chat cập nhật như `UC-U-SCR-07`/`UC-U-SCR-08`.
  - Khi đổi tên/thành viên/vai trò thành công: server ghi nhận thay đổi và cập nhật các giao diện liên quan; nhóm còn thành viên phải còn ít nhất một Admin.
  - Khi gửi báo cáo thành công: Admin hệ thống có báo cáo gồm tài khoản bị báo cáo, lý do và các bằng chứng bản rõ do User đã xác nhận cung cấp; không tự khóa tài khoản.
  - Khi rời nhóm thành công: User không còn quyền gửi/nhận hoặc giải mã tin mới; lịch sử từng được đọc được giữ theo quy tắc hiện tại. Người cuối cùng rời làm nhóm đóng theo đề xuất DEC-05.
  - Khi hủy/lỗi: không báo thao tác thành công hoặc áp dụng thay đổi một phần không được xác nhận; giữ nội dung nhập còn phù hợp và đối chiếu lại trạng thái nếu chưa rõ kết quả.

## Luồng sự kiện (Flow of Events)

### Basic Flow

Luồng cơ bản: mở panel, xem thông tin hội thoại hiện tại và đóng panel.

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Nhấn nút **i** trên header hội thoại trong `U-SCR-07`. | Mở panel `U-SCR-09` bên phải, giữ hội thoại và vùng lịch sử ở khung giữa. |
| 2 | Xem phần đầu panel. | Hiển thị avatar lớn, tên người/nhóm, trạng thái Online/Offline đối với chat cá nhân hoặc chỉ báo E2EE đối với nhóm; có nút tròn **Tìm kiếm** trong hội thoại hiện tại. |
| 3 | Xem các mục thao tác bên dưới. | Với chat cá nhân: **Xóa toàn bộ lịch sử phía mình**, **Báo cáo spam**, **Chặn người dùng** khi chưa chủ động chặn. Với nhóm: hiển thị tùy chỉnh nhóm, thành viên kèm nhãn Admin và quyền riêng tư/hỗ trợ; các thao tác thay đổi tuân theo vai trò hiện tại. |
| 4 | Tiếp tục xem thông tin hoặc chọn một thao tác. | Nếu chọn thao tác, thực hiện luồng thay thế tương ứng; chỉ cho biết thành công sau khi thao tác được xác nhận. Nếu chỉ xem, không thay đổi dữ liệu hội thoại. |
| 5 | Nhấn lại **i** để đóng panel. | Ẩn panel và giữ hội thoại hiện tại để User tiếp tục đọc/soạn tin theo quyền hiện tại. |

### Alternative Flows

#### Alt 2a — Tìm tin nhắn trong riêng hội thoại (`U-17`)

Tại bước 2:

1. User nhấn nút **Tìm kiếm** trong panel và nhập chuỗi cần tìm.
2. Hệ thống tìm nội dung trong riêng hội thoại hiện tại mà User có quyền đọc và chưa xóa phía mình; loại nội dung đã thu hồi khỏi kết quả.
3. Nếu lịch sử/chỉ mục còn đồng bộ, hiển thị tiến độ và cho biết kết quả hiện có có thể chưa đầy đủ. Tin nhóm được tìm sau khi giải mã, không gửi bản rõ lên server để tìm kiếm thông thường.
4. Hệ thống liệt kê kết quả và hỗ trợ điều hướng **Lên / Xuống** giữa các kết quả.
5. User chọn một kết quả hoặc dùng nút điều hướng; khung giữa tải trang lịch sử liên quan theo kích thước **50 tin/trang**, tự cuộn tới và highlight tin đích.
6. Nếu không có kết quả trong phần dữ liệu đã tìm, hiển thị thông báo phù hợp; khi tìm kiếm xong mới coi kết quả là đầy đủ. Xóa từ khóa không chuyển sang tìm trên tất cả hội thoại.

#### Alt 3a — Xóa toàn bộ lịch sử phía mình (`U-15`)

Tại bước 3, áp dụng cho chat cá nhân và nhóm:

1. User chọn **Xóa toàn bộ lịch sử phía mình**.
2. Hệ thống yêu cầu xác nhận, nêu rõ thao tác chỉ ẩn lịch sử với chính User, không xóa tin của những người khác và không làm User rời nhóm.
3. Nếu User xác nhận, server ghi nhận trạng thái xóa riêng; client ẩn lịch sử thuộc phạm vi đã xóa và loại nội dung tương ứng khỏi tìm kiếm của User.
4. Trạng thái xóa riêng được đồng bộ khi đăng nhập lại hoặc đổi máy; tin mới vẫn xuất hiện bình thường theo quyền của User.
5. Nếu User hủy, giữ nguyên lịch sử. Đây không phải thao tác **Thu hồi với mọi người** trong `UC-U-SCR-07`.

#### Alt 3b — Chặn người dùng trong chat cá nhân (`U-10`)

Tại bước 3 của chat cá nhân:

1. User chọn **Chặn người dùng** trong mục **Quyền riêng tư & hỗ trợ**.
2. Hệ thống yêu cầu xác nhận và giải thích việc chặn sẽ hủy tình bạn/lời mời chờ, ngăn trao đổi tin riêng mới nhưng giữ lịch sử cũ và nhóm chung.
3. Sau xác nhận, server ghi nhận chặn và hủy tình bạn/các lời mời chờ giữa hai bên; cập nhật **Bạn bè & Kết nối** tương ứng.
4. Khung chat phía User ẩn thanh soạn và hiển thị **Bỏ chặn / Tìm hiểu thêm**. Phía chỉ bị chặn vẫn có thể soạn/nhấn Gửi, nhưng tin bị từ chối và hiển thị **Không thể gửi**; không chuyển tin hoặc phát thông báo cho người chặn.
5. Hai bên không xuất hiện trong kết quả **Tìm người dùng** của nhau. User xem tài khoản mình chặn trong mục **Đã chặn** của `U-SCR-08`.
6. Nếu đã chủ động chặn từ trước, không thực hiện chặn lặp; hiển thị trạng thái đang chặn và dùng **Bỏ chặn** ở khung chat hoặc mục **Đã chặn** để bỏ chặn theo các use case liên quan.
7. Thao tác có cùng kết quả với nút **Chặn** trong danh sách bạn bè; không tự rời nhóm chung hoặc gửi lại các tin đã lỗi khi bỏ chặn.

#### Alt 3c — Gửi báo cáo spam (`U-26`, `U-DLG-04`)

Tại bước 3, áp dụng cho chat cá nhân và nhóm:

1. User chọn **Báo cáo spam**; hệ thống mở `U-DLG-04` cho hội thoại hiện tại.
2. User xác định tài khoản cần báo cáo, chọn một hoặc nhiều tin vi phạm của tài khoản đó và nhập lý do.
3. Hệ thống cho xem trước các tin/lý do sẽ gửi, thông báo rõ bản rõ bằng chứng sẽ được cung cấp cho Admin hệ thống, kể cả từ nhóm E2EE.
4. Chỉ sau khi User xác nhận, hệ thống gửi phần bằng chứng và lý do đã được duyệt; không tự gửi toàn bộ lịch sử nhóm hoặc khóa E2EE.
5. Khi server xác nhận tiếp nhận, hiển thị báo cáo đã gửi. Admin hệ thống xem xét xử lý; gửi báo cáo không tự khóa/chặn tài khoản hoặc xóa tin.
6. Nếu User hủy trước khi xác nhận gửi, đóng hộp thoại và không gửi bằng chứng. Nếu lỗi gửi, xử lý theo Ex 3g.

#### Alt 3d — Đổi tên nhóm (`U-20`)

Tại bước 3 của nhóm, User là Admin:

1. User chọn **Đổi tên nhóm** trong mục **Tùy chỉnh nhóm**.
2. Hệ thống hiển thị tên hiện tại để sửa; User nhập tên mới và xác nhận.
3. Server kiểm tra vai trò Admin hiện tại và tên hợp lệ, không rỗng/không chỉ có khoảng trắng; không tự áp dụng giới hạn độ dài chưa chốt.
4. Sau xác nhận thành công, cập nhật tên ở panel, header và danh sách hội thoại cho các thành viên liên quan.
5. Nếu hủy hoặc tên không thay đổi, không tạo thay đổi tên nhóm. Thành viên thường không được thực hiện thao tác này.

#### Alt 3e — Thêm thành viên (`U-21`)

Tại bước 3 của nhóm, User là thành viên có quyền thêm theo DEC-05:

1. User nhấn **Thêm thành viên**; hệ thống cho tìm và chọn từ bạn bè của User chưa thuộc nhóm.
2. User chọn ít nhất một người và xác nhận; người được mời không bắt buộc Online.
3. Server kiểm tra lại tư cách thành viên/quyền thêm của User, tình bạn và chặn hai chiều giữa User với từng người được mời. Không thêm trùng người đã thuộc nhóm.
4. Quan hệ chặn giữa người được mời và thành viên khác không ngăn lời mời nếu cặp người mời–người được mời vẫn đủ điều kiện.
5. Khi thành công, cập nhật danh sách thành viên. Với E2EE, trạng thái chia sẻ khóa/lịch sử phải được thể hiện nếu chưa hoàn tất; không coi thêm thành viên thành công là đã giải mã được lịch sử.
6. Theo đề xuất DEC-05, thành viên mới được đọc lịch sử trước khi tham gia trừ tin đã thu hồi khi được chia sẻ khả năng giải mã hợp lệ. Không để server tự giải mã thay hoặc cam kết mở được lịch sử khi mất toàn bộ khóa.
7. Khi User có quan hệ chặn với thành viên trong nhóm, banner của `U-SCR-07` được cập nhật theo quy tắc hiện tại. Hủy lựa chọn không thêm ai.

#### Alt 3f — Cấp hoặc thu hồi quyền Admin (`U-22`)

Tại bước 3 của nhóm, User là Admin:

1. User mở menu của một thành viên và chọn **Cấp quyền Admin** hoặc **Thu hồi quyền Admin** phù hợp với vai trò hiện tại của người đó.
2. Hệ thống yêu cầu xác nhận; server kiểm tra người thao tác và người được thay đổi vẫn thuộc nhóm, vai trò hiện tại còn hợp lệ.
3. Khi cấp quyền thành công, thêm nhãn Admin và cập nhật quyền thao tác cho thành viên được cấp.
4. Khi thu hồi quyền thành công, bỏ nhãn Admin và cập nhật quyền của thành viên; người đó vẫn ở trong nhóm.
5. Nếu thao tác làm nhóm đang có thành viên không còn Admin, không cho thu hồi trực tiếp. Khi Admin duy nhất tự hạ quyền và còn thành viên khác, mở `U-DLG-05` để chọn Admin kế nhiệm; chuyển quyền và hạ quyền phải thành công cùng nhau.
6. Nếu nhóm chỉ còn một người, không có người kế nhiệm để tự hạ quyền; giữ vai trò Admin. User vẫn có thể chọn **Rời nhóm** theo Alt 3i.
7. Nếu User hủy hoặc không chọn người kế nhiệm hợp lệ, giữ vai trò hiện tại. Quy tắc Admin ngang quyền theo DEC-05 không trao quyền thu hồi tin nhắn của người khác.

#### Alt 3g — Xóa thành viên khỏi nhóm (`U-23`)

Tại bước 3 của nhóm, User là Admin:

1. User mở menu của thành viên khác và chọn **Xóa thành viên**; nếu muốn tự rời thì dùng **Rời nhóm** theo Alt 3i.
2. Hệ thống yêu cầu xác nhận, giải thích người bị xóa không nhận/giải mã tin nhóm mới nhưng bản lịch sử đã có trên thiết bị của họ không bị xóa từ xa.
3. Server kiểm tra quyền Admin, thành viên đích và điều kiện nhóm còn người phải còn Admin, theo đề xuất Admin ngang quyền của DEC-05.
4. Nếu hợp lệ, xóa tư cách thành viên và quyền Admin của người bị xóa nếu có; cập nhật danh sách và quyền/khóa E2EE để ngăn truy cập tin mới.
5. Phía người bị xóa hiển thị trạng thái **Bạn đã bị xóa khỏi nhóm**, không cho gửi hoặc tiếp tục thao tác quản trị bằng quyền cũ; giữ lịch sử từng được đọc theo DEC-05.
6. Chặn cá nhân không tự thực hiện thao tác xóa thành viên này. Nếu User hủy, giữ nguyên tư cách thành viên.

#### Alt 3h — Xem thông tin E2EE (`U-25`)

Tại bước 3 của nhóm:

1. User mở mục thông tin mã hóa E2EE trong **Quyền riêng tư & hỗ trợ**.
2. Hệ thống hiển thị nhóm dùng E2EE và trạng thái khóa/đồng bộ của thiết bị liên quan đến khả năng đọc lịch sử; server lưu/chuyển bản mã, không tự giải mã luồng tin nhóm.
3. Nếu thiếu khóa hoặc cần khôi phục, hướng tới `U-SCR-06`/tab khóa E2EE của `U-DLG-01` theo chức năng tương ứng; không hiển thị khóa bí mật hoặc mã khôi phục trong panel nhóm.
4. Việc xem thông tin không tự xuất khóa, gửi bản rõ ra ngoài hoặc thay đổi thành viên. Báo cáo spam vẫn cần xem trước/xác nhận riêng theo Alt 3c.

#### Alt 3i — Rời nhóm và chọn Admin kế nhiệm

Tại bước 3 của nhóm:

1. User chọn **Rời nhóm**.
2. Hệ thống xét theo số thành viên/vai trò mới nhất:

   | Trường hợp | Xử lý |
   | --- | --- |
   | Thành viên thường, còn người khác | Yêu cầu xác nhận rời, không chọn kế nhiệm. |
   | Admin, vẫn còn Admin khác sau khi rời | Yêu cầu xác nhận rời, không bắt buộc chọn thêm Admin. |
   | Admin duy nhất, còn thành viên khác | Bắt buộc mở `U-DLG-05` chọn thành viên hiện tại làm Admin kế nhiệm trước khi xác nhận. |
   | Người cuối cùng trong nhóm | Không yêu cầu người kế nhiệm; xác nhận rời và đóng nhóm, giữ lịch sử theo đề xuất DEC-05. |

3. Sau xác nhận, server kiểm tra lại điều kiện; khi cần kế nhiệm thì cấp quyền và rời nhóm phải thành công cùng nhau. Hủy hoặc chưa chọn người hợp lệ thì User vẫn ở nhóm và giữ vai trò.
4. Khi thành công, cập nhật thành viên, thu hồi quyền gửi/nhận và giải mã tin mới của User; ngừng retry các tin chưa được chấp nhận từ User đã rời.
5. Phía User giữ lịch sử từng được đọc ở chế độ chỉ đọc và thay thanh soạn bằng **Bạn đã rời nhóm**. Các thao tác quản trị trong panel không còn khả dụng.
6. Nếu người cuối cùng rời, nhóm có nhãn **Nhóm đã đóng**, không nhận tin/lời mời mới; không tự xóa lịch sử hoặc bản sao trên thiết bị.
7. Luồng này thống nhất với Alt 2e của `UC-U-SCR-07`; phương án đóng nhóm/giữ lịch sử chỉ đọc vẫn là đề xuất, không cam kết khôi phục lịch sử khi mất khóa.

#### Alt 2b — Đổi hội thoại hoặc xem nhóm đã rời

Tại bước 2–4:

1. Nếu User chuyển hội thoại khi panel đang mở, hệ thống cập nhật panel theo hội thoại mới và loại các kết quả tìm kiếm/lựa chọn không còn thuộc ngữ cảnh hiện tại.
2. Không tự áp dụng tên, thành viên, lựa chọn xóa hoặc bằng chứng báo cáo của hội thoại trước cho hội thoại mới. Thao tác đang chờ phải gắn đúng hội thoại ban đầu.
3. Nếu xem hội thoại cũ của nhóm đã rời/bị xóa/đã đóng, panel thể hiện trạng thái tương ứng và không cho thao tác cần tư cách thành viên hiện tại. Chỉ cho xem/tìm/xóa lịch sử riêng trong phạm vi còn quyền; báo cáo chỉ sử dụng bằng chứng còn đọc được và quyền đã được server kiểm tra.

### Exception Flows

- **Ex 1a — Không tải được panel**: Tại bước 1–3, thông báo lỗi và cho tải lại; không diễn giải lỗi thành nhóm không có thành viên hoặc User không có quyền. Khung chat giữ dữ liệu đã tải phù hợp.
- **Ex 2a — Tìm kiếm chưa sẵn sàng**: Tại Alt 2a, nếu đang đồng bộ thì thể hiện tiến độ; nếu thiếu khóa thì thông báo cần thiết lập/khôi phục. Không trả kết quả đầy đủ giả, gửi bản rõ nhóm lên server để tìm hoặc coi bản mã là nội dung có thể đọc.
- **Ex 2b — Tin đích không còn khả dụng**: Nếu tin đã thu hồi, xóa phía mình hoặc không còn quyền truy cập sau khi kết quả được tải, thông báo không thể mở và cập nhật kết quả; không hiển thị lại nội dung đã bị loại.
- **Ex 3a — Mất quyền/thành viên thay đổi**: Với đổi tên, thêm/xóa thành viên, đổi vai trò hoặc rời nhóm, server kiểm tra lại trạng thái tại thời điểm xử lý. Nếu User không còn đủ quyền, từ chối, cập nhật panel và không dùng quyền cũ để tiếp tục.
- **Ex 3b — Dữ liệu nhập không hợp lệ**: Nếu tên nhóm rỗng, chưa chọn người để thêm, chưa chọn tin báo cáo hoặc chưa nhập lý do, không gửi thao tác chưa hợp lệ; thông báo tại vị trí phù hợp và giữ nội dung để sửa. Không tự đặt giới hạn độ dài/số tin khi chưa chốt.
- **Ex 3c — Người được thêm không còn đủ điều kiện**: Nếu không còn tình bạn, có chặn hai chiều với người mời, đã thuộc nhóm hoặc tài khoản không còn khả dụng, cập nhật danh sách và yêu cầu User xác nhận lại lựa chọn hợp lệ; không tự thêm một phần ngoài danh sách User đã xác nhận.
- **Ex 3d — Admin kế nhiệm không còn hợp lệ**: Nếu người được chọn đã rời/bị xóa hoặc số thành viên/vai trò đổi trong lúc xác nhận, kiểm tra lại và yêu cầu chọn/xác nhận theo trường hợp mới. Không chuyển quyền hoặc hạ quyền/rời nhóm một phần làm nhóm còn người nhưng không có Admin.
- **Ex 3e — Cập nhật thành viên/khóa E2EE lỗi**: Không báo đã hoàn tất quyền đọc lịch sử khi chia sẻ khóa còn chờ/lỗi; hiển thị trạng thái cụ thể. Với xóa/rời nhóm, chặn quyền truy cập tin mới theo trạng thái đã xác nhận và không tiếp tục phân phối tin bằng khóa cho phép người đã bị loại giải mã; giao thức xử lý theo DEC-05 còn cần chốt.
- **Ex 3f — Thao tác lỗi hoặc mất xác nhận**: Với xóa lịch sử, chặn hoặc quản trị nhóm, thông báo lỗi khi server từ chối. Nếu chưa rõ kết quả do mất kết nối, đối chiếu trạng thái trước khi cho thực hiện lại; không tự báo thành công, nhân đôi thay đổi hoặc giả định User vẫn còn quyền gửi/quản trị.
- **Ex 3g — Báo cáo chưa gửi được**: Giữ lý do và phần bằng chứng còn hợp lệ để User xem lại; không báo đã gửi khi chưa có xác nhận. Nếu chưa rõ server đã tiếp nhận hay chưa, kiểm tra trước khi gửi lại để tránh báo cáo trùng; không tự bổ sung bản rõ ngoài phần đã được xác nhận.
- **Ex 1b — Phiên không còn hợp lệ**: Dừng thao tác cần xác thực, thông báo và chuyển về đăng nhập; không tiếp tục gửi bằng chứng, thay đổi nhóm hoặc cập nhật quyền bằng phiên cũ.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

#### BR-01 — Ngữ cảnh panel

- Panel được mở/đóng bằng nút **i** trong `U-SCR-07` và luôn gắn với một hội thoại cụ thể.
- Phần đầu có avatar, tên và trạng thái phù hợp; phần dưới thay đổi theo chat cá nhân/nhóm và vai trò hiện tại.
- Đổi hội thoại không được làm thao tác đang chờ áp dụng nhầm sang hội thoại khác.

#### BR-02 — Tìm trong hội thoại

- `U-17` chỉ tìm trong hội thoại hiện tại, khác tìm tổng hợp `U-18` ở cột trái của `U-SCR-07`.
- Chỉ tìm nội dung được phép đọc/giải mã, chưa xóa phía mình và chưa thu hồi.
- Hỗ trợ danh sách kết quả và **Lên / Xuống**; tải trang **50 tin/trang**, cuộn và highlight đúng tin.
- Thể hiện lịch sử/chỉ mục đang đồng bộ; không coi kết quả chưa tải đủ là đầy đủ.

#### BR-03 — Xóa toàn bộ lịch sử phía mình

- Chỉ ẩn lịch sử với User thao tác và loại khỏi tìm kiếm của họ; người khác giữ lịch sử.
- Trạng thái xóa riêng đồng bộ khi đăng nhập lại/đổi máy; tin mới vẫn xuất hiện.
- Không hủy quan hệ bạn bè, tự rời nhóm hoặc thu hồi tin với mọi người.

#### BR-04 — Chặn và bỏ chặn

- Chat cá nhân có **Chặn người dùng** trong panel; danh sách bạn bè cũng có **Chặn** với cùng quy trình xác nhận.
- Chặn hủy tình bạn/lời mời đang chờ, ngăn lời mời và chuyển tin riêng mới hai chiều; giữ lịch sử cũ.
- Người chủ động chặn thấy **Bỏ chặn / Tìm hiểu thêm** thay thanh soạn; người chỉ bị chặn vẫn soạn nhưng gửi báo **Không thể gửi**.
- **Tìm người dùng** loại cả tài khoản mình chặn và tài khoản đang chặn mình; mục **Đã chặn** quản lý tài khoản do mình chặn.
- Bỏ chặn không tự khôi phục tình bạn/lời mời hoặc tự gửi lại tin lỗi; nhóm chung vẫn đọc/gửi tin và có banner khi liên quan đến User hiện tại.

#### BR-05 — Quyền nhóm theo DEC-05 đang đề xuất

- Admin nhóm ngang quyền, được đổi tên, thêm/xóa thành viên và cấp/thu hồi quyền Admin.
- Thành viên thường được thêm bạn bè theo điều kiện mời, không được đổi tên, xóa thành viên hoặc đổi quyền Admin.
- Mọi thành viên hiện tại được xem danh sách thành viên và yêu cầu rời nhóm.
- Vai trò Admin nhóm không đồng nghĩa Admin hệ thống và không cho thu hồi tin của người khác.
- Server kiểm tra quyền hiện tại; ẩn/vô hiệu hóa menu trên client không thay thế kiểm tra quyền.

#### BR-06 — Đổi tên nhóm

- Chỉ Admin hiện tại được đổi tên; tên không rỗng hoặc chỉ gồm khoảng trắng.
- Cập nhật tên ở panel, header và danh sách hội thoại sau xác nhận thành công.
- Giới hạn độ dài/chuẩn hóa tên cụ thể chưa chốt, không tự thêm trong use case này.

#### BR-07 — Thêm thành viên

- Người được mời phải là bạn bè của người mời và không có quan hệ chặn hai chiều với người mời.
- Không mời trùng thành viên đang ở nhóm; Online không phải điều kiện bắt buộc.
- Quan hệ chặn với thành viên khác không ngăn lời mời đủ điều kiện giữa người mời–người được mời.
- Việc chia sẻ khả năng giải mã lịch sử trước khi tham gia theo DEC-05 là đề xuất, cần trạng thái chờ/lỗi khi chưa hoàn tất.

#### BR-08 — Cấp/thu hồi Admin và kế nhiệm

- Cấp quyền chỉ cho thành viên hiện tại; thu hồi quyền không tự xóa tư cách thành viên.
- Nhóm còn thành viên phải còn ít nhất một Admin sau thao tác.
- Admin duy nhất tự hạ quyền/rời khi còn người khác phải chọn kế nhiệm qua `U-DLG-05`; chuyển quyền và thao tác phải thành công cùng nhau.
- Nếu chỉ còn một người thì không có người để chọn kế nhiệm; không tự hạ quyền thành nhóm không có Admin, nhưng được rời theo Alt 3i.

#### BR-09 — Xóa thành viên và E2EE

- Chỉ Admin được xóa thành viên khác; tự rời dùng luồng **Rời nhóm**.
- Người bị xóa giữ lịch sử từng được đọc, không được gửi/nhận hoặc giải mã tin mới.
- Không cam kết xóa bản sao đã có trên thiết bị của người bị xóa.
- Chặn cá nhân không tự xóa thành viên hoặc thay đổi quyền nhóm.

#### BR-10 — Rời/đóng nhóm

- Thành viên thường và Admin khi còn Admin khác được xác nhận rời mà không chọn thêm người kế nhiệm.
- Admin duy nhất còn thành viên khác phải chọn kế nhiệm; hủy/chưa chọn thì chưa rời và chưa đổi vai trò.
- Người cuối cùng được rời không cần kế nhiệm; theo đề xuất DEC-05, nhóm được đóng, không nhận tin/lời mời mới và giữ lịch sử cũ chỉ đọc.
- Phía đã rời hiển thị **Bạn đã rời nhóm**; phía bị xóa hiển thị **Bạn đã bị xóa khỏi nhóm**; nhóm hết thành viên có nhãn **Nhóm đã đóng**.
- Không tự gửi lại tin chờ sau khi mất quyền thành viên; không tự xóa lịch sử hoặc cam kết khôi phục lịch sử khi mất khóa.

#### BR-11 — Thông tin E2EE

- Mọi nhóm mặc định E2EE; server chỉ lưu/chuyển bản mã của luồng chat nhóm.
- Thông tin thiết bị/đồng bộ phải phân biệt đã có quyền thành viên với đã có khóa đọc được lịch sử.
- Thiết lập/khôi phục khóa thuộc `U-SCR-06` và `U-DLG-01`, không công khai khóa bí mật/mã khôi phục trong panel nhóm.
- Giao thức/thư viện, cấp lịch sử và đổi khóa vẫn theo DEC-05 chưa chốt.

#### BR-12 — Báo cáo spam

- Cần một hoặc nhiều tin vi phạm, tài khoản bị báo cáo và lý do.
- Cho xem trước và yêu cầu xác nhận riêng trước khi cung cấp bản rõ cho Admin hệ thống, kể cả nhóm E2EE.
- Chỉ gửi phần đã được xác nhận; không gửi toàn bộ lịch sử hoặc khóa giải mã nhóm.
- Bằng chứng là bản rõ User cung cấp; không mặc định server đã xác thực nội dung bản rõ đó.
- Admin hệ thống xử lý thủ công; báo cáo không tự khóa/chặn người dùng hoặc xóa tin.

### Non-Functional Requirements (NFR)

#### NFR-01 — Phản hồi giao diện

- Tải panel/thành viên, tìm kiếm và cập nhật nhóm không làm đóng băng JavaFX.
- Phân biệt đang tải, đang đồng bộ, dữ liệu trống và lỗi.
- Khi mất quyền, cập nhật thao tác của panel và khung chat, không giữ menu quản trị dùng được bằng quyền cũ.

#### NFR-02 — Quyền truy cập và riêng tư

- Server kiểm tra phiên, quyền hội thoại, vai trò và thành viên tại thời điểm xử lý.
- Tìm kiếm thường không gửi bản rõ nhóm lên server.
- Gửi bằng chứng báo cáo cần xác nhận riêng, không dùng việc mở panel hoặc xem nhãn E2EE làm sự đồng ý cung cấp bản rõ.
- Dữ liệu tìm kiếm/nháp báo cáo không được trộn giữa các hội thoại.

#### NFR-03 — Nhất quán và thao tác lặp

- Cập nhật đồng bộ tên, thành viên, vai trò và chặn giữa panel, khung chat và danh sách liên quan.
- Chuyển quyền kế nhiệm và hạ quyền/rời nhóm phải thành công cùng nhau.
- Ngăn nhấn lặp khi đang xử lý; đối chiếu trạng thái khi mất xác nhận để không tạo thay đổi hoặc báo cáo trùng.
- Không hiển thị thao tác hoàn tất chỉ dựa trên cập nhật tạm ở client.

#### NFR-04 — Bảo toàn dữ liệu

- Hủy/lỗi giữ dữ liệu nhập và lựa chọn còn hợp lệ để User sửa hoặc thử lại.
- Xóa lịch sử riêng, chặn, đổi tên hoặc rời nhóm không tự làm mất nháp của hội thoại khác.
- Xóa/rời nhóm không cam kết xóa lịch sử đã có trên thiết bị thành viên cũ.

#### NFR-05 — Hiệu năng đề xuất theo DEC-09

- Mục tiêu tải trang lịch sử **50 tin ≤ 2 giây**.
- Mục tiêu tìm nội dung **≤ 2 giây** khi lịch sử/chỉ mục đã sẵn sàng.
- Đây là mục tiêu chưa đo nghiệm thu; cần ghi môi trường, mạng, dữ liệu và lỗi khi kiểm chứng.
- Không tự áp dụng các ngưỡng này cho mọi thao tác quản trị hoặc chia sẻ khóa khi chưa có yêu cầu chốt.
