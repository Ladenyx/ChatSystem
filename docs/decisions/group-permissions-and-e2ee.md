# Quyền nhóm và E2EE

**DEC-05 — Đề xuất:** chưa có kết luận chung toàn mục hoặc thư viện mật mã được chốt.

- Người tạo là admin đầu tiên; admin ngang quyền, được đổi tên, thêm/xóa thành viên, cấp/thu hồi admin; bảo vệ admin cuối cùng.
- Thành viên thường chỉ được thêm bạn bè theo điều kiện DEC-03, không đổi tên nhóm. Admin chỉ thu hồi tin của mình theo giới hạn DEC-02.
- Mọi nhóm dùng E2EE; server lưu/chuyển bản mã, không tự giải mã luồng chat nhóm.
- Người mới được đọc lịch sử trước khi tham gia, trừ tin đã thu hồi; cần thiết bị có quyền chia sẻ khả năng giải mã. Người được mời có thể offline; quyền đọc chỉ hoàn tất khi chia sẻ xong.
- Người bị xóa giữ lịch sử từng được đọc, không nhận/giải mã tin mới; không thể xóa bản đã nằm trên thiết bị của họ.
- User Client hỗ trợ nhiều phiên/thiết bị đồng thời; đăng nhập máy mới không kết thúc phiên chat hoặc phiên quản trị đang có. Giới hạn một phiên chỉ áp dụng cho phiên quản trị theo DEC-07. Xác thực tài khoản không đồng nghĩa thiết bị đã có khóa E2EE.
- Đổi máy cần chuyển/khôi phục khóa bằng mã khôi phục người dùng giữ và backup khóa đã mã hóa. Reset mật khẩu không mở khóa lịch sử; mất thiết bị giữ khóa lẫn mã thì không tự mở được backup.
- Gửi nội dung cho LLM hoặc báo cáo spam là tiết lộ bản rõ, có xem trước/xác nhận riêng.

## Thiết lập/khôi phục khóa sau đăng nhập

- Đăng nhập thành công mở khung chính trước. Người dùng có thể mở `U-SCR-06` để khôi phục lịch sử ngay hoặc để sau; hủy/bỏ qua khôi phục không đăng xuất, không thu hồi phiên và không cản sử dụng các chức năng không cần khóa.
- Một tài khoản dùng một mã khôi phục 6 số do người dùng thiết lập. Sau 5 lần xác minh sai liên tiếp được ghi nhận trên các thiết bị, Server áp dụng trạng thái chờ chung 5 phút trước lần thử mới trên mọi thiết bị của tài khoản. Thiết bị đã mở khóa không tự bị khóa lại; khi không kết nối được Server để kiểm tra trạng thái, không bắt đầu lần thử mã mới. Cách ghi nhận và đồng bộ an toàn chưa được nhóm kỹ thuật chốt.
- Khi có khóa cục bộ phù hợp và trạng thái mở khóa hợp lệ của đúng tài khoản, thiết bị sử dụng khóa đó. Đăng xuất làm khóa lại trên thiết bị đó; đăng nhập lại hoặc khôi phục trên thiết bị mới cần nhập mã. Nếu chỉ đóng và mở lại ứng dụng mà phiên đăng nhập và trạng thái mở khóa vẫn hợp lệ, không yêu cầu mã lần nữa. Khi khôi phục thành công, chỉ hiển thị lịch sử mà thiết bị đã lấy được khóa giải mã và đồng bộ được.
- Khi bỏ qua khôi phục và chưa có khóa dùng được, luồng E2EE phải chuẩn bị bộ khóa cho thiết bị mới và hoàn tất việc cấp/chia sẻ khóa cho các hội thoại theo giao thức được chọn trước khi cho gửi hoặc giải mã tin E2EE mới. Lịch sử cũ vẫn chưa được coi là khôi phục. Trong lúc nhóm đang khóa, Server vẫn lưu tin nhắn mã hóa theo quyền nhận và chính sách lưu trữ; ứng dụng có thể báo có tin mới nhưng không hiện nội dung. Chỉ đọc được sau khi mở khóa và có đủ dữ liệu, khóa. Không gửi bản rõ thay thế hoặc báo đã giải mã thành công.
- Không tự ghi đè/xóa backup khóa hiện có hoặc thay mã khôi phục khi bỏ qua/hủy. Lỗi đọc backup không được diễn giải thành không có backup; khóa mới của thiết bị phải được lưu tách biệt với bản sao lưu phục vụ lịch sử cũ.
- Phiên bản hiện tại không có luồng Quên mã để khôi phục hoặc thiết lập lại mã. Nếu người dùng không còn mã và không còn thiết bị đã mở khóa, không thể dùng mã để khôi phục bản sao lưu và có thể không đọc lại được lịch sử mã hóa; không tự xóa hay thay bản sao lưu đó. Nếu còn hoặc tìm lại được thiết bị đang mở khóa hợp lệ và có các khóa cần thiết, người dùng có thể đổi mã trên thiết bị đó theo `U-25`, không cần nhập lại mã cũ tại bước đổi.
- Chi tiết tạo khóa thiết bị, cấp khóa cho nhiều thiết bị và điều kiện gửi/nhận khi chưa khôi phục thuộc đặc tả `U-SCR-06`/`U-25`; không triển khai quy trình đó trong use case đăng nhập. Những điều kiện trên là ràng buộc cần đáp ứng khi chọn giao thức, chưa khẳng định đã có cơ chế mật mã chạy được.

**Còn mở:** giao thức/thư viện, prototype cấp lịch sử và cấp khóa nhiều thiết bị, đổi thành viên/khóa, tìm kiếm, archive và khôi phục. Bouncy Castle MLS chỉ là ứng viên; chia sẻ lại lịch sử khi mất toàn bộ khóa chưa được cam kết.

## Thuyết minh phương án và giới hạn

### Phạm vi và các khái niệm

Đề bài yêu cầu mã hóa nhóm chat. Bản nháp chọn bổ sung mã 6 số để người dùng mở hoặc khôi phục khóa trên thiết bị; đây là phương án của nhóm, không phải chi tiết đề bài bắt buộc. Mã này là phương thức khôi phục được mô tả trong phiên bản hiện tại. U-25 mô tả **nhóm chat**; việc áp dụng E2EE cho toàn bộ chat cá nhân chưa được chốt trong phạm vi này.

| Thành phần | Vai trò trong phương án |
| --- | --- |
| **Mật khẩu đăng nhập** | Xác thực tài khoản để vào User Client. Đổi mật khẩu đăng nhập không tự mở hoặc khôi phục khóa E2EE. |
| **Mã khóa 6 số** | Mã do người dùng chọn, dùng chung cho tài khoản để mở hoặc khôi phục khóa; không phải một mã riêng cho mỗi nhóm. |
| **Khóa E2EE** | Dữ liệu mật mã thiết bị cần có để giải mã hoặc gửi tin trong nhóm tương ứng. Mã 6 số không đồng nghĩa các nhóm dùng cùng một khóa mã hóa. |
| **Bản sao lưu khóa** | Bản lưu các khóa được bảo vệ dưới dạng mã hóa để có thể khôi phục trên thiết bị. Server không nhận mã khôi phục hay khóa bí mật dạng rõ. |

Đổi mã 6 số là thay cách bảo vệ bản sao lưu các khóa hiện có. Đó không đồng nghĩa hệ thống đã tự thay hoặc xoay khóa mã hóa của từng nhóm. Việc cấp, chia sẻ và thay khóa nhóm khi thành viên thay đổi thuộc phương án kỹ thuật E2EE, hiện chưa chốt.

### Các tình huống sử dụng

Trong các ví dụ dưới đây, **máy A** là thiết bị cũ và **máy B** là thiết bị mới của cùng một tài khoản.

| Tình huống | Cách xử lý và kết quả |
| --- | --- |
| **Thiết lập lần đầu** | Khi xác định chưa có bản sao lưu, người dùng chọn mã 6 số và nhập lại. Client chuẩn bị khóa và yêu cầu lưu bản sao lưu đã mã hóa. Chỉ báo hoàn tất khi Server xác nhận lưu thành công. Nếu không xác định được trạng thái bản sao lưu, không tự tạo bản mới để ghi đè. |
| **Nhớ mã trên máy B** | Người dùng đăng nhập, nhập mã đã tạo và yêu cầu khôi phục. Nếu khôi phục thành công, máy B chỉ đọc được những tin có đủ dữ liệu và khóa tương ứng; đăng nhập trên máy B không tự đăng xuất hoặc khóa máy A. |
| **Quên mã nhưng máy A vẫn mở khóa** | Trên máy A, người dùng chọn đổi mã, nhập và xác nhận mã mới mà không cần nhập lại mã cũ tại bước đổi. Sau khi Server xác nhận bản sao lưu mới đã được lưu, người dùng có thể dùng mã mới để khôi phục trên máy B. Khả năng đọc lịch sử còn phụ thuộc máy A có đủ dữ liệu và khóa cần thiết. |
| **Quên mã; tìm lại máy A đã đăng xuất** | Việc tìm lại máy A không tự mở khóa. Theo luồng hiện tại, sau khi đăng xuất, người dùng cần mã để mở lại; nếu không nhớ mã, chưa thể dùng máy A theo luồng đổi mã ở trên. Nếu còn một thiết bị khác đang mở khóa hợp lệ và có khóa cần thiết, có thể đổi mã trên thiết bị đó. |
| **Quên mã và không còn thiết bị đã mở khóa** | Phiên bản này không cung cấp chức năng **Quên mã** hay thiết lập lại mã. Người dùng không thể dùng mã để mở bản sao lưu cũ và có thể không đọc lại được lịch sử mã hóa; hệ thống không tự xóa hoặc thay bản sao lưu. Người dùng vẫn có thể đăng nhập và dùng các chức năng không cần khóa. |
| **Đóng rồi mở lại máy A** | Nếu phiên đăng nhập và trạng thái mở khóa trên máy A vẫn hợp lệ, không yêu cầu nhập mã lại. Nếu đã đăng xuất hoặc trạng thái mở khóa không còn hợp lệ, xử lý theo điều kiện mở khóa hiện tại. |

Khi quên mã và không còn thiết bị đã mở khóa, khả năng tiếp tục gửi hoặc đọc **tin nhóm mã hóa mới** phụ thuộc vào cách cấp khóa cho thiết bị theo giao thức nhóm lựa chọn. Hiện chưa thể cam kết chỉ cần đăng nhập hoặc tạo khóa thiết bị là dùng được nhóm. Không thay tin mã hóa bằng bản rõ để vượt qua điều kiện này.

### Nhập sai, mất kết nối và dữ liệu cũ

- Sau 5 lần xác minh mã sai liên tiếp được ghi nhận chung cho tài khoản, các thiết bị chưa mở khóa phải chờ 5 phút trước lần thử tiếp theo. Thiết bị đã mở khóa không tự bị khóa lại hoặc đăng xuất.
- Khi không kết nối được Server để kiểm tra trạng thái chờ chung, không bắt đầu lần thử mã mới; lỗi kết nối không tính là nhập sai.
- Đổi mã thành công không tự xóa tin nhắn. Nếu thao tác đổi mã thất bại trước khi bản sao lưu mới được lưu thành công, giữ mã và bản sao lưu cũ. Nếu mất phản hồi, không tự khẳng định bản nào đang có hiệu lực.
- Không có luồng tự thiết lập mã mới sau khi quên. Trường hợp giả định đã thiết lập mã mới khi mất mã rồi tìm lại máy cũ nằm ngoài phiên bản này; chưa có cam kết chuyển hoặc hợp nhất khóa cũ từ máy đó.

### Điểm cần thực hiện khi triển khai

Các luồng trên là **yêu cầu đề xuất**, chưa phải chức năng đã chạy. Nhóm kỹ thuật cần lựa chọn và kiểm thử cách bảo vệ bản sao lưu, cấp và đồng bộ khóa giữa thiết bị, xử lý thay đổi thành viên nhóm và ghi nhận số lần thử an toàn mà không gửi mã dạng rõ, kể cả khi các yêu cầu xảy ra đồng thời. Quy tắc nhập sai trong ứng dụng không ngăn việc dò mã ngoài ứng dụng nếu bản sao lưu bị lộ.

Wireframe tham khảo: [U-SCR-06 trên Figma](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=133-6122). Lớp phủ và vị trí menu cần thống nhất với người phụ trách màn hội thoại.
