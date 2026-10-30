# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-SCR-07
- **Use Case Name**: Xem hội thoại và trao đổi tin nhắn
- **Author / Create by**: Kiet
- **Date Create**: 30/10/2026
- **Last Updated by** / Date: Kiet / 30/10/2026
- **Primary Actor**: User đã đăng nhập.
- **Secondary Actor(s)**: Người nhận tin nhắn cá nhân; các thành viên nhóm; nhà cung cấp AI (khi sử dụng trợ lý AI).
- **Priority**: Must Have
- **Status**: Reviewed

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User mở không gian Hội thoại & Khung Chat, chọn hội thoại cá nhân hoặc nhóm, xem lịch sử và trao đổi tin nhắn. User có thể lọc danh sách, tìm kiếm tổng hợp, xóa tin phía mình, thu hồi tin đã gửi và sử dụng AI để chỉnh sửa nháp hoặc gợi ý trả lời.
- **Goal**: User xem đúng hội thoại, đọc được lịch sử có quyền truy cập và gửi tin nhắn với trạng thái xử lý rõ ràng.
- **Trigger**: User chọn khung chat trên thanh điều hướng của khung làm việc chính.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - User đã đăng nhập bằng phiên hợp lệ.
  - Khi mở hội thoại, User có quyền truy cập hội thoại đó; khi gửi tin nhóm, User là thành viên hiện tại.
  - Khi đọc hoặc gửi tin nhóm E2EE, thiết bị có khóa hợp lệ tương ứng. Nếu khóa hoặc lịch sử chưa sẵn sàng, áp dụng luồng thay thế/lỗi bên dưới.
  - Luồng cơ bản giả định kết nối tới server hoạt động, có sẵn hội thoại cá nhân và không bên nào đang chặn bên còn lại. Các trường hợp có quan hệ chặn được mô tả ở Alt 2c và Alt 5b.
- **Post-conditions**:
  - Khi xem thành công: hội thoại được chọn, thông tin đầu khung và lịch sử được hiển thị theo quyền của User.
  - Khi gửi thành công: server xác nhận đã lưu tin; khung chat hiển thị tin cùng trạng thái gửi tương ứng. Trạng thái **Đã nhận/Đã xem** chỉ xuất hiện khi có xác nhận tương ứng.
  - Khi người nhận đọc tin: hệ thống ghi nhận xác nhận đã xem cho đúng tin nhắn và User đã đọc, cập nhật phía người gửi thành **Đã xem**. Trong nhóm, hiển thị những thành viên đã xem; không coi một người xem là toàn bộ nhóm đã xem.
  - Khi người nhận offline: tin vẫn được gửi và lưu ngay; hệ thống phát thông báo tin nhắn mới cho người nhận mà không chờ trạng thái Online. Việc nhận thông báo không đồng nghĩa đã nhận/đọc nội dung tin trong khung chat.
  - Khi xóa/thu hồi thành công: lịch sử và kết quả tìm kiếm được cập nhật đúng phạm vi thao tác.
  - Khi sử dụng AI: phương án được chọn chỉ điền vào ô soạn nháp; User vẫn phải chủ động gửi.
  - Khi thao tác thất bại hoặc bị hủy: không hiển thị như đã thành công; giữ nháp và cho phép tiếp tục phù hợp với quyền hiện tại.
  - Khi gửi tin trong lúc bị đối phương chặn: tin hiển thị trạng thái **Không thể gửi** ở phía gửi, không được chuyển hoặc phát thông báo cho đối phương và không tự gửi lại khi bỏ chặn.
  - Khi rời nhóm thành công: User không còn là thành viên và không được gửi/nhận hoặc giải mã tin nhóm mới. Nhóm còn thành viên phải còn ít nhất một Admin; nếu không còn ai, nhóm được đóng theo phương án đề xuất tại Alt 2e. Lịch sử User từng được đọc không bị tự động xóa.

## Luồng sự kiện (Flow of Events)

### Basic Flow

Luồng cơ bản: mở hội thoại cá nhân và gửi một tin nhắn, không phụ thuộc người nhận đang Online hay Offline.

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn **Đoạn chat** trên thanh điều hướng. | Mở `U-SCR-07`. Cột trái hiển thị nút **Tạo nhóm mới**, ô tìm kiếm tổng hợp, các bộ lọc **Tất cả / Chưa đọc / Nhóm** và danh sách hội thoại cá nhân/nhóm kèm trạng thái phù hợp. |
| 2 | Chọn một hội thoại cá nhân trong danh sách. | Đánh dấu hội thoại đang chọn; hiển thị avatar, tên, trạng thái Online/Offline và nút **i** ở đầu khung giữa. |
| 3 | Xem các tin nhắn trong hội thoại. | Tải trang lịch sử gần nhất theo kích thước **50 tin/trang**; hiển thị nội dung và trạng thái tin. Với tin nhận từ người khác đã hiển thị trong vùng đọc của hội thoại đang mở, gửi xác nhận đã xem theo Alt 7c; không đánh dấu mọi tin vừa tải là đã xem. Nếu đang đồng bộ, hiển thị banner tiến độ. |
| 4 | Nhập nội dung vào ô soạn tin. | Giữ nội dung trong bản nháp; hiển thị riêng nút **Chỉnh sửa**, nút **Gợi ý** và nút **Gửi**. Nút Chỉnh sửa chỉ được kích hoạt khi nháp có chữ. |
| 5 | Nhấn **Gửi**. | Kiểm tra phiên, quyền gửi và nội dung; hiển thị tin ở trạng thái **Đang chờ** và gửi tới server. |
| 6 | Theo dõi kết quả gửi tin. | Server lưu tin, xác nhận gửi thành công cho người gửi và phát thông báo tin nhắn mới cho người nhận ngay. Khung chat chuyển sang **Server đã lưu** khi có xác nhận lưu; nội dung được chuyển tới client nhận khi có kết nối và cập nhật **Đã nhận** khi có xác nhận nhận tin. Khi người nhận đọc tin và có xác nhận đã xem, cập nhật thành **Đã xem** theo Alt 7c. |
| 7 | Tiếp tục theo dõi hội thoại. | Hiển thị tin đến theo thời gian thực. Tin chưa gửi thành công vẫn có trạng thái riêng để User nhận biết và thử lại khi phù hợp. |

### Alternative Flows

#### Alt 1a — Lọc danh sách hội thoại

Tại bước 1:

1. User chọn **Tất cả**, **Chưa đọc** hoặc **Nhóm**.
2. Hệ thống hiển thị các hội thoại phù hợp với bộ lọc; nếu không có, hiển thị trạng thái danh sách trống.
3. User chọn một hội thoại để tiếp tục bước 2 hoặc đổi bộ lọc.

#### Alt 1b — Tìm kiếm tổng hợp và nhảy tới tin nhắn (`U-18`)

Tại bước 1:

1. User nhập từ khóa vào ô tìm kiếm phía trên danh sách hội thoại.
2. Hệ thống trả về hai nhóm kết quả: **Đoạn chat khớp tên** User/nhóm và **Tin nhắn khớp nội dung trên tất cả hội thoại** mà User được phép tìm kiếm.
3. Nếu lịch sử/chỉ mục chưa đồng bộ đầy đủ, hệ thống hiển thị trạng thái đang đồng bộ; không trình bày kết quả hiện có như kết quả đầy đủ.
4. Nếu chọn kết quả tên hội thoại, hệ thống mở hội thoại và tiếp tục bước 3.
5. Nếu chọn kết quả tin nhắn, hệ thống mở hội thoại chứa tin, tải trang lịch sử liên quan, tự cuộn tới và highlight tin đích; sau đó User có thể tiếp tục bước 4.
6. Nếu không có kết quả, hệ thống hiển thị thông báo tương ứng với phạm vi đã tìm; User có thể đổi hoặc xóa từ khóa.

#### Alt 1c — Mở hộp thoại tạo nhóm (`U-19`)

Tại bước 1:

1. User nhấn **Tạo nhóm mới**.
2. Hệ thống mở `U-DLG-02`, cho nhập tên nhóm và chọn tối thiểu một thành viên từ bạn bè không có quan hệ chặn hai chiều với người mời.
3. Nếu tạo thành công, hệ thống cập nhật danh sách hội thoại, mở nhóm mới mặc định E2EE và xác định người tạo là Admin đầu tiên; tiếp tục theo Alt 2a.
4. Nếu User hủy, quay về danh sách hiện tại. Các bước kiểm tra và lỗi tạo nhóm thuộc đặc tả `U-DLG-02`.

#### Alt 1d — Chưa có hội thoại hoặc chỉ xem lịch sử

- Tại bước 1, nếu chưa có hội thoại, hệ thống hiển thị trạng thái trống; User có thể mở **Bạn bè & Kết nối** để bắt đầu nhắn tin hoặc chọn **Tạo nhóm mới**.
- Sau bước 3, nếu User chỉ đọc rồi chuyển hội thoại hoặc rời màn hình, use case kết thúc ở kết quả xem thành công, không yêu cầu gửi tin.
- Nếu hội thoại chưa có tin nhắn, hệ thống hiển thị lịch sử trống và cho phép soạn tin khi User có quyền gửi.

#### Alt 2a — Mở và nhắn tin trong nhóm E2EE (`U-24`, `U-25`)

Tại bước 2:

1. User chọn một nhóm đang tham gia.
2. Hệ thống hiển thị avatar, tên nhóm, nhãn E2EE và nút **i**; tải và giải mã lịch sử theo khóa/quyền của User. Nếu có thành viên đang bị User chặn hoặc đang chặn User, hiển thị banner theo Alt 2d.
3. User soạn và gửi tin theo các bước 4–5.
4. Client mã hóa tin trước khi gửi; server lưu/chuyển bản mã cho các thành viên có quyền nhận. Các client nhận giải mã để hiển thị.
5. Hệ thống cập nhật trạng thái dựa trên xác nhận thực tế; khi có thành viên đọc tin, ghi nhận **Đã xem** theo từng User và cho người gửi xem danh sách thành viên đã xem theo Alt 7c. Không suy diễn toàn bộ thành viên đã đọc chỉ từ việc server đã lưu tin hoặc một thành viên đã xem.

#### Alt 2b — Bật/tắt panel thông tin

Sau bước 2 hoặc 3:

1. User nhấn nút **i** trên header.
2. Hệ thống bật `U-SCR-09` ở bên phải, hiển thị thông tin của hội thoại đang chọn; nhấn lại **i** để tắt panel.
3. Nếu chọn tìm kiếm trong riêng hội thoại (`U-17`) từ panel, kết quả được điều hướng về khung giữa để tải trang, tự cuộn và highlight tin đích như Alt 1b.
4. User tiếp tục đọc hoặc soạn tin trong hội thoại hiện tại. Các thao tác quản trị và quyền riêng tư khác trong panel thuộc đặc tả `U-SCR-09`.

#### Alt 2c — Mở hội thoại với người mình đang chặn (`U-10`)

Tại bước 2–4 hoặc ngay sau khi User chủ động chặn đối phương:

1. Hệ thống giữ lịch sử hội thoại nhưng ẩn toàn bộ thanh soạn tin, gồm ô nhập, các nút AI và nút **Gửi**; thay bằng hai nút **Bỏ chặn** và **Tìm hiểu thêm**.
2. Nếu User chọn **Tìm hiểu thêm**, hệ thống hiển thị giải thích về việc chặn: không thể trao đổi tin riêng mới, lịch sử cũ vẫn được giữ và bỏ chặn không tự khôi phục quan hệ bạn bè. Thao tác này không thay đổi trạng thái chặn.
3. Nếu User chọn **Bỏ chặn**, hệ thống gửi yêu cầu bỏ chặn. Sau khi server xác nhận thành công, hệ thống hiển thị lại thanh soạn tin để User tiếp tục bước 4; nếu thất bại, áp dụng Ex 4a.
4. Bỏ chặn không tự gửi lại các tin trước đó đã báo **Không thể gửi**. Nếu đối phương vẫn đang chặn User, thanh soạn vẫn hiển thị nhưng thao tác gửi áp dụng Alt 5b.
5. Nếu cả hai bên cùng chặn nhau, mỗi bên thấy giao diện của người chủ động chặn cho đến khi tự bỏ chặn.

#### Alt 2d — Thông báo khi mở nhóm có quan hệ chặn (`U-10`, `U-24`)

Tại bước 2 của Alt 2a, mỗi khi User mở một nhóm:

1. Hệ thống kiểm tra quan hệ chặn hai chiều giữa User hiện tại và các thành viên còn lại của nhóm.
2. Nếu có ít nhất một thành viên mà User đã chặn hoặc đã chặn User, hệ thống hiển thị banner thông tin phía trên vùng lịch sử chat:

   > Trong nhóm có người bạn đã chặn hoặc đã chặn bạn. Các thành viên vẫn có thể xem và gửi tin nhắn trong nhóm.

3. Banner hiển thị cho User có quan hệ chặn liên quan, không phải tin nhắn được gửi vào lịch sử nhóm. Quan hệ chặn chỉ giữa các thành viên khác không làm xuất hiện banner cho User hiện tại.
4. User tiếp tục đọc và gửi tin bình thường theo Alt 2a; banner không yêu cầu xác nhận, không ẩn/vô hiệu hóa thanh soạn và không khiến tin nhóm báo **Không thể gửi** chỉ vì quan hệ chặn.
5. Nếu không còn quan hệ chặn liên quan đến User trong nhóm, hệ thống không hiển thị banner. Kiểm tra lại điều kiện khi mở nhóm và khi có cập nhật quan hệ chặn hoặc thành viên.

#### Alt 2e — Rời nhóm từ panel thông tin (`U-SCR-09`, `U-DLG-05`)

Sau khi mở nhóm và bật panel thông tin theo Alt 2b:

1. User chọn **Rời nhóm** trong `U-SCR-09`.
2. Hệ thống kiểm tra số thành viên và vai trò hiện tại để xử lý theo bảng dưới. Trường hợp chỉ còn một người được xét trước yêu cầu chọn Admin kế nhiệm.

   | Trường hợp | Xử lý trước khi rời |
   | --- | --- |
   | Thành viên thường, nhóm còn người khác | Cho xác nhận rời nhóm; không yêu cầu chọn Admin kế nhiệm. |
   | Admin, sau khi rời vẫn còn ít nhất một Admin khác | Cho xác nhận rời nhóm; không bắt buộc chỉ định thêm Admin. |
   | Admin duy nhất, nhóm còn thành viên khác | Mở `U-DLG-05`, bắt buộc chọn một thành viên hiện tại làm Admin kế nhiệm trước khi xác nhận rời. Không chọn hoặc hủy thì User vẫn ở trong nhóm, giữ quyền Admin. |
   | User là người cuối cùng trong nhóm | Không yêu cầu người kế nhiệm. Hiển thị xác nhận: **Bạn là thành viên cuối cùng. Rời nhóm sẽ đóng nhóm. Lịch sử cũ không bị tự động xóa.** |

3. User xác nhận rời nhóm. Nếu hủy, đóng hộp thoại và giữ nguyên tư cách thành viên, vai trò và dữ liệu.
4. Server kiểm tra lại thành viên/vai trò mới nhất. Với Admin duy nhất, việc cấp quyền cho người kế nhiệm và rời nhóm phải thành công cùng nhau; nếu nhóm còn người thì không được có thời điểm kết thúc xử lý mà nhóm thiếu Admin.
5. Nếu hợp lệ, server xóa User khỏi danh sách thành viên, thu hồi quyền với tin nhóm mới và cập nhật thông tin thành viên cho các bên liên quan. Với nhóm E2EE, việc cập nhật quyền/khóa phải bảo đảm người đã rời không giải mã được tin mới; giao thức cụ thể còn theo DEC-05.
6. Sau xác nhận thành công, khung chat phía User chuyển sang chỉ đọc lịch sử đã có quyền đọc, thay thanh soạn bằng nhãn **Bạn đã rời nhóm**. Không tự gửi lại tin chờ của nhóm sau khi rời; các yêu cầu bị từ chối hiển thị **Không thể gửi**.
7. Nếu User là người cuối cùng, hệ thống đồng thời đánh dấu **Nhóm đã đóng**; nhóm không còn thành viên và không nhận tin hoặc lời mời mới. Rời/đóng nhóm không đồng nghĩa xóa lịch sử hay thu hồi các bản sao đã có trên thiết bị.

#### Alt 3a — Xem lịch sử cũ và theo dõi đồng bộ (`U-14`)

Tại bước 3:

1. User cuộn lên để xem tin cũ hơn.
2. Hệ thống tải trang trước theo kích thước **50 tin/trang**; trang cuối có thể ít hơn 50 tin, tránh hiển thị trùng các tin đã tải.
3. Nếu lịch sử đang đồng bộ, hệ thống hiển thị tiến độ và cho xem phần đã sẵn sàng. Với nhóm E2EE, chỉ hiển thị bản rõ khi giải mã được bằng khóa hợp lệ.
4. Khi không còn lịch sử có thể tải, hệ thống thể hiện đã đến đầu lịch sử.

#### Alt 5a — Gửi tin khi người nhận offline (`U-13`)

Tại bước 5:

1. Hệ thống gửi tin tới server và chuyển sang **Server đã lưu** sau khi nhận xác nhận lưu.
2. Server phát thông báo tin nhắn mới cho người nhận ngay qua cơ chế thông báo của hệ thống, không chờ người nhận chuyển sang Online. Luồng gửi của người gửi hoàn tất sau xác nhận lưu, không giữ tin ở **Đang chờ** chỉ vì người nhận offline.
3. Khi client nhận có kết nối, hệ thống chuyển/đồng bộ nội dung vào hội thoại. Trạng thái **Đã nhận/Đã xem** chỉ được cập nhật khi có xác nhận tương ứng; thông báo đã được phát không thay thế các xác nhận này.
4. Nếu thiết bị người nhận mất kết nối hoàn toàn hoặc không thể nhận thông báo, áp dụng Ex 6a; tin vẫn được lưu thành công để đồng bộ khi thiết bị kết nối lại.

#### Alt 5b — Người bị chặn soạn và gửi tin (`U-10`)

Tại bước 4–6, khi đối phương đang chặn User và User không chủ động chặn đối phương:

1. Hệ thống vẫn hiển thị thanh soạn tin; ô nhập và nút **Gửi** không bị vô hiệu hóa do User bị chặn. User có thể nhập nội dung và nhấn **Gửi**.
2. Hệ thống gửi yêu cầu tới server; server kiểm tra quan hệ chặn và từ chối chuyển tin.
3. Tin vừa gửi được hiển thị tại phía User với trạng thái lỗi **Không thể gửi**. Tin không chuyển sang **Server đã lưu**, không được chuyển hoặc phát thông báo cho người chặn.
4. Hệ thống không tự retry tin bị từ chối do chặn, kể cả khi quan hệ chặn được gỡ bỏ sau đó. Thanh soạn vẫn sử dụng được; các lần gửi tiếp theo được kiểm tra quyền tại thời điểm gửi.

#### Alt 7a — Xóa một hoặc nhiều tin phía mình (`U-16`)

Sau bước 3 hoặc 7:

1. User mở thao tác của một tin và chọn **Xóa phía mình**, hoặc chọn nhiều dòng rồi thực hiện thao tác này.
2. Hệ thống ẩn các tin đã chọn khỏi lịch sử và kết quả tìm kiếm của người thao tác; đồng bộ trạng thái xóa riêng khi đăng nhập lại hoặc đổi máy.
3. Lịch sử của người khác không bị ảnh hưởng. Tin mới trong hội thoại vẫn được hiển thị.

#### Alt 7b — Thu hồi tin với mọi người (`U-16`)

Sau bước 3 hoặc 7:

1. User mở thao tác trên tin do chính mình gửi và chọn **Thu hồi với mọi người** trong **48 giờ đầu**.
2. Hệ thống kiểm tra người gửi và thời hạn tại thời điểm xử lý yêu cầu.
3. Nếu hợp lệ, hệ thống thay nội dung bằng dấu báo thu hồi, loại nội dung khỏi tìm kiếm và đồng bộ cho các bên liên quan, kể cả người đang offline.
4. Nếu không hợp lệ, xử lý theo Ex 7b; User vẫn có thể chọn **Xóa phía mình**.

#### Alt 7c — Ghi nhận và hiển thị User đã xem tin nhắn

Tại bước 3 hoặc sau bước 6–7, áp dụng cho chat cá nhân và nhóm:

1. Người nhận mở hội thoại và đọc tin do người khác gửi. Client chỉ ghi nhận đã xem khi nội dung tin đã hiển thị trong vùng đọc của hội thoại đang được người nhận xem; với nhóm E2EE, tin phải được giải mã thành công.
2. Client gửi xác nhận đã xem gắn với tin nhắn và tài khoản người đọc; server xác thực quyền truy cập, ghi nhận và cập nhật cho phía người gửi.
3. Với chat cá nhân, người gửi thấy nhãn **Đã xem** trên tin khi người nhận đã đọc. Trạng thái Online, việc nhận thông báo, nhận tin về thiết bị hoặc tải lịch sử nền không đủ để chuyển sang **Đã xem**.
4. Với chat nhóm, người gửi có thể chọn chỉ báo **Đã xem** của tin để xem danh sách các thành viên đã đọc, ví dụ **Đã xem bởi: An, Bình**. Chỉ liệt kê người có xác nhận đã xem tin đó; không tính người gửi là người nhận đã xem và không hiển thị như cả nhóm đã xem khi chỉ một số thành viên đọc.
5. Nếu người gửi đang Offline, trạng thái đã xem được đồng bộ khi người gửi kết nối lại hoặc mở lại hội thoại. Xác nhận trùng không tạo thêm người đã xem và không làm trạng thái lùi từ **Đã xem** về **Đã nhận**.

#### Alt 4a — Chỉnh sửa bản nháp bằng AI (`U-27`)

Tại bước 4:

1. Khi ô nháp có chữ, User nhấn **Chỉnh sửa (Refine)**.
2. Hệ thống mở `U-DLG-03` ở chế độ **Refine Draft**, lấy nội dung nháp và cho chọn kiểu diễn đạt mong muốn.
3. Hệ thống cho xem trước/chỉnh sửa nội dung sẽ gửi và thông báo rõ bản rõ sẽ đi tới server ứng dụng cùng nhà cung cấp AI, kể cả trong nhóm E2EE.
4. Chỉ sau khi User xác nhận, hệ thống gửi nội dung được duyệt để sinh các phương án viết lại.
5. User chọn một phương án; hệ thống điền vào ô soạn nháp, không tự gửi. User kiểm tra/chỉnh sửa rồi có thể tiếp tục bước 5.
6. Nếu User hủy hoặc đóng popover mà chưa chọn phương án, hệ thống giữ nguyên nháp trước đó.

#### Alt 4b — Gợi ý trả lời bằng AI (`U-27`)

Tại bước 4 hoặc sau bước 3:

1. User nhấn **Gợi ý (Recommend)**, độc lập với nút Chỉnh sửa.
2. Hệ thống mở `U-DLG-03` ở chế độ **Recommend Reply**, cho User chọn các tin nhắn ngữ cảnh trong hội thoại hiện tại.
3. User xem trước/chỉnh sửa phần ngữ cảnh được chọn và xác nhận gửi bản rõ tới server ứng dụng cùng nhà cung cấp AI.
4. Hệ thống chỉ gửi phần được xác nhận và hiển thị các câu trả lời gợi ý.
5. User chọn một câu; hệ thống điền vào ô soạn nháp để User tự kiểm tra và nhấn **Gửi** khi muốn.
6. Nếu User hủy hoặc chưa chọn ngữ cảnh, hệ thống không gửi yêu cầu AI và giữ nháp hiện tại.

### Exception Flows

- **Ex 1a — Không tải được danh sách/lịch sử**: Tại bước 1–3 hoặc Alt 3a, khi lỗi kết nối/tải dữ liệu, hệ thống thông báo lỗi và cho thử lại. Phần dữ liệu đã tải vẫn được giữ; không diễn giải lỗi thành danh sách/lịch sử trống.
- **Ex 2a — Thiếu khóa hoặc chưa giải mã được E2EE**: Tại Alt 2a/3a, hệ thống thông báo tình trạng khóa/đồng bộ và hướng User tới quy trình thiết lập/khôi phục khóa `U-SCR-06` khi phù hợp. Không hiển thị bản mã như nội dung đã giải mã hoặc cho gửi tin nhóm khi chưa có khóa cần thiết. Reset mật khẩu không tự mở khóa lịch sử; khả năng khôi phục khi mất toàn bộ khóa còn phụ thuộc quyết định E2EE.
- **Ex 4a — Bỏ chặn thất bại**: Tại Alt 2c, nếu yêu cầu bỏ chặn lỗi hoặc chưa được server xác nhận, hệ thống thông báo lỗi, giữ hai nút **Bỏ chặn** và **Tìm hiểu thêm**, cho phép thử lại; chưa hiển thị lại thanh soạn tin như đã bỏ chặn thành công.
- **Ex 2e — Người kế nhiệm hoặc vai trò thay đổi**: Tại Alt 2e, nếu thành viên được chọn đã rời/bị xóa, hoặc vai trò/số thành viên thay đổi trong lúc xác nhận, server kiểm tra lại điều kiện. Nếu lựa chọn không còn hợp lệ, không cấp quyền/rời nhóm một phần; hệ thống cập nhật danh sách và yêu cầu chọn lại hoặc xác nhận theo trường hợp mới. Nếu User đã rời do thao tác khác, cập nhật giao diện đã rời thay vì tiếp tục thao tác với quyền cũ.
- **Ex 2f — Rời nhóm lỗi hoặc mất xác nhận**: Tại Alt 2e, nếu server từ chối thao tác, thông báo và giữ trạng thái thành viên/vai trò đã được xác nhận. Nếu mất kết nối khi chưa biết kết quả, không báo rời thành công hoặc giả định vẫn còn quyền; tải lại trạng thái trước khi cho gửi tin, rời lại hoặc chọn người kế nhiệm. Không tạo thay đổi vai trò trùng khi thử lại.
- **Ex 5a — Nội dung không hợp lệ**: Tại bước 5, nếu nội dung trống hoặc không đáp ứng kiểm tra hợp lệ, hệ thống không gửi, thông báo lý do và giữ nháp để sửa. Các giới hạn độ dài cụ thể cần được chốt riêng, không tự đặt trong use case này.
- **Ex 5b — Mất kết nối hoặc chưa nhận xác nhận lưu**: Tại bước 5–6, hệ thống giữ tin ở **Đang chờ/Thử lại**. Tin chờ được gửi lại với cùng ID để server xử lý lặp an toàn, không sinh tin hoặc lượt hoạt động trùng. Mỗi lần gửi lại phải kiểm tra quyền hiện tại; chỉ chuyển sang **Server đã lưu** sau xác nhận.
- **Ex 6a — Không chuyển được thông báo tới thiết bị nhận**: Tại bước 6 hoặc Alt 5a, nếu thiết bị nhận mất kết nối hoàn toàn hoặc kênh thông báo không khả dụng, hệ thống giữ tin đã lưu để đồng bộ khi có kết nối. Không đổi tin đã được server xác nhận về **Đang chờ/Thử lại** hoặc yêu cầu người gửi gửi lại chỉ vì thông báo chưa tới; chưa ghi nhận **Đã nhận/Đã xem** khi thiếu xác nhận.
- **Ex 7c — Chưa đồng bộ được xác nhận đã xem**: Tại Alt 7c, nếu mất kết nối sau khi người nhận đọc tin, client giữ xác nhận để đồng bộ lại khi có kết nối và quyền phù hợp. Phía người gửi giữ trạng thái đã được xác nhận gần nhất; không tự suy diễn **Đã xem** hoặc đổi tin đã gửi thành **Không thể gửi** chỉ vì chưa nhận được xác nhận đã xem.
- **Ex 5c — Quyền gửi thay đổi**: Nếu quan hệ chặn phát sinh trong lúc gửi tin riêng hoặc người gửi không còn là thành viên nhóm, server từ chối yêu cầu và tin hiển thị **Không thể gửi**. Với chat cá nhân, giao diện chuyển theo Alt 2c nếu User chủ động chặn, hoặc giữ thanh soạn theo Alt 5b nếu chỉ bị đối phương chặn. Quan hệ chặn trong nhóm chỉ kích hoạt banner theo Alt 2d, không phải lý do từ chối tin nhóm. Tin bị từ chối không được chuyển hoặc phát thông báo cho người nhận và không tự retry; tin server đã chấp nhận trước khi chặn vẫn được đồng bộ.
- **Ex 1b — Tin đích không còn khả dụng**: Tại Alt 1b/2b, nếu tin đã bị xóa phía mình, thu hồi hoặc không còn quyền truy cập, hệ thống thông báo không thể mở tin, cập nhật kết quả tìm kiếm và không hiển thị nội dung đã bị loại.
- **Ex 7a — Xóa/thu hồi không được xác nhận**: Tại Alt 7a/7b, hệ thống thông báo lỗi và cho thử lại khi phù hợp; không báo thao tác thành công nếu chưa được xác nhận.
- **Ex 7b — Không đủ điều kiện thu hồi**: Tại Alt 7b, nếu tin thuộc người khác hoặc đã quá 48 giờ, hệ thống không thực hiện thu hồi và thông báo lý do. Quyền Admin nhóm không cho phép thu hồi tin của thành viên khác.
- **Ex 4b — AI lỗi, timeout hoặc hết hạn mức**: Tại Alt 4a/4b, hệ thống thông báo không thể tạo kết quả, giữ nguyên bản nháp và cho User tiếp tục chat thủ công. Không tự gửi lại bản rõ khi User chưa xác nhận một yêu cầu mới.
- **Ex 1c — Phiên không còn hợp lệ**: Ở bất kỳ bước nào, nếu tài khoản bị khóa hoặc phiên bị thu hồi, hệ thống dừng thao tác cần xác thực, thông báo và chuyển về màn hình đăng nhập. Không tiếp tục gửi từ phiên cũ.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

#### BR-01 — Phạm vi hội thoại

- Danh sách gồm hội thoại cá nhân và nhóm mà User có quyền truy cập;
- bộ lọc nhanh gồm **Tất cả / Chưa đọc / Nhóm**.
- Theo đề xuất DEC-03, chat riêng không bắt buộc đã kết bạn.

#### BR-02 — Gửi, thông báo và trạng thái tin

- Phân biệt **Đang chờ/Thử lại**, **Server đã lưu**, **Đã nhận**, **Đã xem** và trạng thái lỗi **Không thể gửi**.
- **Đã xem** là nhãn hiển thị cho trạng thái người nhận đã đọc tin, không phải trạng thái riêng sau khi đã đọc.
- Tin hợp lệ được gửi, lưu và phát thông báo ngay, không chờ người nhận Online.
- Tin bị từ chối do chặn hiển thị **Không thể gửi** và không phát thông báo cho người nhận.
- Trạng thái hiện diện Offline không đồng nghĩa thiết bị mất kết nối hoàn toàn;
- khả năng nhận thông báo phụ thuộc kết nối và cơ chế thông báo.
- Không coi xác nhận lưu hoặc việc phát thông báo là xác nhận nhận/đọc nội dung tin.
- Tin đã lưu vẫn được coi là gửi thành công dù thiết bị nhận chưa nhận được thông báo.

#### BR-03 — Retry

- Giữ nguyên ID tin khi gửi lại;
- server xử lý lặp an toàn, kiểm tra quyền hiện tại, không tạo thêm tin hoặc lượt hoạt động cho cùng một lần gửi.

#### BR-04 — Lịch sử và tìm kiếm

- Lịch sử phân trang **50 tin/trang**.
- Tìm kiếm tổng hợp trả hai nhóm kết quả theo tên hội thoại và nội dung tin;
- chọn tin phải mở đúng hội thoại, tải trang cần thiết, tự cuộn và highlight.
- Kết quả chỉ bao gồm nội dung User có quyền đọc và có thể tìm kiếm sau khi giải mã.

#### BR-05 — Xóa phía mình

- Cho phép xóa một hoặc nhiều tin;
- chỉ ẩn với người thao tác, loại khỏi tìm kiếm của họ và đồng bộ trạng thái xóa riêng khi đổi máy/đăng nhập lại.
- Xóa toàn bộ lịch sử (`U-15`) được khởi tạo từ `U-SCR-09`.

#### BR-06 — Thu hồi

- Chỉ người gửi được thu hồi tin của mình trong **48 giờ đầu**, áp dụng cho chat cá nhân và nhóm.
- Có dấu báo thu hồi;
- nội dung bị loại khỏi tìm kiếm và cập nhật cho cả người offline.
- Admin nhóm không có ngoại lệ thu hồi tin người khác.

#### BR-07 — Chặn

- Trong chat cá nhân, người chủ động chặn không thấy thanh soạn tin mà thấy hai nút **Bỏ chặn** và **Tìm hiểu thêm**.
- Người chỉ bị đối phương chặn vẫn có thể soạn và nhấn **Gửi**, nhưng tin bị server từ chối và hiển thị **Không thể gửi**.
- Nếu cả hai bên cùng chặn, giao diện người chủ động chặn được áp dụng ở mỗi bên.
- Chỉ khôi phục thanh soạn sau khi yêu cầu bỏ chặn của chính User được xác nhận thành công;
- việc gửi thành công còn phụ thuộc quan hệ chặn ở phía đối phương.
- Giữ lịch sử cũ, không tự gửi lại tin lỗi khi bỏ chặn và không áp dụng hạn chế tin riêng này cho nhóm chung.

#### BR-08 — Nhóm E2EE

- Nhóm mặc định E2EE;
- server lưu/chuyển bản mã, không tự giải mã luồng chat nhóm.
- Việc cấp lịch sử, quản lý khóa khi đổi thành viên và khôi phục khóa tuân theo DEC-05 đang ở mức đề xuất, chưa chốt giao thức/thư viện.

#### BR-09 — Tạo nhóm từ header

- Nút **Tạo nhóm mới** mở `U-DLG-02`;
- tối thiểu một thành viên được chọn từ bạn bè không có quan hệ chặn hai chiều với người mời.
- Người tạo là Admin đầu tiên.

#### BR-10 — Hai chế độ AI

- Có hai nút độc lập.
- **Refine** cần nháp có chữ;
- **Recommend** dùng ngữ cảnh do User chọn.
- Chỉ gửi nháp/ngữ cảnh đã xem trước và xác nhận;
- nêu rõ việc tiết lộ bản rõ tới server ứng dụng và nhà cung cấp AI, kể cả trong nhóm E2EE.

#### BR-11 — Kiểm soát kết quả AI

- Kết quả được chọn chỉ điền vào bản nháp;
- User quyết định gửi.
- Hủy, lỗi hoặc hết hạn mức không làm mất nháp cũ.
- Các giới hạn số tin ngữ cảnh, số gợi ý, timeout và quota chưa được chốt.

#### BR-12 — Banner quan hệ chặn trong nhóm

- Khi mở nhóm có ít nhất một thành viên mà User đã chặn hoặc đã chặn User, hiển thị banner thông tin theo Alt 2d ở phía trên vùng lịch sử chat.
- Chỉ xét quan hệ chặn liên quan đến User hiện tại;
- banner không được gửi thành tin nhóm, không yêu cầu xác nhận và không thay đổi quyền đọc/gửi tin nhóm.
- Cập nhật banner khi điều kiện quan hệ chặn hoặc thành viên thay đổi.

#### BR-13 — Rời nhóm và Admin cuối cùng

- Mọi thành viên có thể yêu cầu rời nhóm từ `U-SCR-09`.
- Nếu còn người khác nhưng không còn Admin nào sau khi rời, bắt buộc chọn Admin kế nhiệm qua `U-DLG-05`;
- chuyển quyền và rời nhóm phải thành công cùng nhau.
- Nếu đã còn Admin khác, không bắt buộc chọn thêm.
- Người cuối cùng được rời mà không chọn kế nhiệm;
- theo đề xuất DEC-05, nhóm sau đó được đóng và lịch sử cũ giữ ở chế độ chỉ đọc.
- Rời nhóm không tự xóa lịch sử, không cấp quyền đọc tin mới và không bảo đảm khôi phục lịch sử nếu mất khóa E2EE.

#### BR-14 — Xác nhận đã xem

- Chỉ ghi nhận **Đã xem** khi người nhận có quyền đọc và nội dung tin đã hiển thị trong vùng đọc của hội thoại đang được xem;
- không đánh dấu đã xem chỉ vì Online, nhận thông báo, tải lịch sử hoặc nhận bản mã chưa giải mã.
- Ghi nhận theo từng tin và từng người nhận, kiểm tra tài khoản gửi xác nhận;
- không tính người gửi vào danh sách người nhận đã xem.
- Chat nhóm cho xem danh sách thành viên đã đọc theo Alt 7c.
- Trạng thái được đồng bộ khi kết nối lại, xử lý lặp an toàn và không bị lùi do xác nhận đến sai thứ tự.

### Non-Functional Requirements (NFR)

#### NFR-01 — Phản hồi giao diện

- Tải lịch sử, đồng bộ, tìm kiếm và gọi AI phải có trạng thái chờ/tiến độ phù hợp, không làm đóng băng giao diện JavaFX.
- Khi chưa đồng bộ xong phải thể hiện rõ kết quả tìm kiếm có thể chưa đầy đủ.

#### NFR-02 — Toàn vẹn

- Không mất hoặc hiển thị trùng tin server đã xác nhận, kể cả sau reconnect.
- Các trạng thái gửi phải phản ánh xác nhận thực tế.

#### NFR-03 — Riêng tư và bảo mật

- Không gửi bản rõ tin nhóm cho server để tìm kiếm thông thường.
- Luồng AI chỉ gửi phần User đã xác nhận;
- API key nằm ở server, không nhúng vào User Client.

#### NFR-04 — Mục tiêu độ trễ đề xuất (DEC-09)

- p95 từ bấm gửi đến GUI người nhận giải mã/render **≤ 1 giây**;
- tải một trang **50 tin ≤ 2 giây**;
- tìm kiếm **≤ 2 giây** khi lịch sử/chỉ mục đã sẵn sàng.

#### NFR-05 — Mục tiêu đồng bộ đề xuất (DEC-09)

- Đồng bộ E2EE lần đầu **10.000 tin có quyền đọc ≤ 30 giây** sau khi có khóa hợp lệ;
- hiển thị tiến độ trong thời gian chờ.

#### NFR-06 — Điều kiện đo đề xuất (DEC-09)

- Ưu tiên nghiệm thu Windows, demo nhiều máy qua server Internet;
- tải 20 client/tài khoản với tổng 4 tin/giây trong 30 phút, dữ liệu ban đầu 1.000 User, 100 nhóm và 100.000 tin.
- Ghi rõ môi trường, phiên bản, mạng, dữ liệu và lỗi;
- số đo phải có giải mã/render GUI thực tế.
- Các mục tiêu này cần được xác nhận bằng đo đạc.
