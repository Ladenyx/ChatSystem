# Quyền nhóm và E2EE

**DEC-05:** đã chốt chính sách tin cậy thiết bị mới và thời hạn giữ backup đã thay ngày **15/11/2026**. Hướng **Hybrid E2EE** trên JDK JCA + Bouncy Castle (`AES-256-GCM`, `X25519`, `Ed25519`) theo [tech stack](tech-stack.md); chi tiết giao thức cấp khóa, bằng chứng tin cậy và định dạng mật mã cần được thiết kế, thử nghiệm trước khi triển khai.

- Người tạo là admin đầu tiên; admin ngang quyền, được đổi tên, thêm/xóa thành viên, cấp/thu hồi admin; bảo vệ admin cuối cùng.
- Thành viên thường chỉ được thêm bạn bè theo điều kiện DEC-03, không đổi tên nhóm. Admin chỉ thu hồi tin của mình theo giới hạn DEC-02.
- Mọi nhóm dùng E2EE; server lưu/chuyển bản mã, không tự giải mã luồng chat nhóm. Bảo đảm mật mã này bổ sung cho chính sách quyền riêng tư tại [DEC-07](account-lifecycle.md) (vốn đã cấm Admin hệ thống xem nội dung hội thoại cá nhân/nhóm kể cả khi không mã hóa E2EE, ngoại trừ tin nhắn bằng chứng trong báo cáo spam).
- Người mới được đọc lịch sử trước khi tham gia, trừ tin đã thu hồi; cần thiết bị có quyền chia sẻ khả năng giải mã. Người được mời có thể offline; quyền đọc chỉ hoàn tất khi chia sẻ xong.
- Người bị xóa giữ lịch sử từng được đọc, không nhận/giải mã tin mới; không thể xóa bản đã nằm trên thiết bị của họ.
- User Client hỗ trợ nhiều phiên/thiết bị đồng thời; đăng nhập máy mới không kết thúc phiên chat hoặc phiên quản trị đang có. Giới hạn một phiên chỉ áp dụng cho phiên quản trị theo DEC-07. Xác thực tài khoản không đồng nghĩa thiết bị đã có khóa E2EE.
- Đổi máy cần chuyển/khôi phục khóa bằng mã khôi phục người dùng giữ và backup khóa đã mã hóa. Reset mật khẩu không mở khóa lịch sử; mất thiết bị giữ khóa lẫn mã thì không tự mở được backup.
- Gửi nội dung cho LLM hoặc đính kèm tin nhắn khi báo cáo spam là tiết lộ bản rõ do người dùng chủ động thực hiện (cấp quyền cho Admin xem các tin bằng chứng đối với cả chat 1-1 không E2EE lẫn nhóm E2EE), có xem trước/xác nhận riêng.

## Thiết lập/khôi phục khóa sau đăng nhập

- Đăng nhập thành công mở khung chính trước. Người dùng có thể mở `U-SCR-06` để khôi phục lịch sử ngay hoặc để sau; hủy/bỏ qua khôi phục không đăng xuất, không thu hồi phiên và không cản sử dụng các chức năng không cần khóa.
- Khi có khóa cục bộ hợp lệ của đúng tài khoản, thiết bị sử dụng khóa đó. Khi khôi phục thành công, chỉ hiển thị lịch sử mà thiết bị đã lấy được khóa giải mã và đồng bộ được.
- Khi bỏ qua khôi phục và chưa có khóa dùng được, luồng E2EE phải chuẩn bị bộ khóa cho thiết bị mới và hoàn tất việc cấp/chia sẻ khóa cho các hội thoại theo giao thức được chọn trước khi cho gửi/nhận tin E2EE mới. Lịch sử cũ vẫn chưa được coi là khôi phục. Trong lúc chưa đủ khóa, chỉ các thao tác tin nhắn E2EE liên quan bị hạn chế; không gửi bản rõ thay thế hoặc báo đã giải mã thành công.
- Không tự ghi đè/xóa backup khóa hiện có hoặc thay mã khôi phục khi bỏ qua/hủy. Lỗi đọc backup không được diễn giải thành không có backup; khóa mới của thiết bị phải được lưu tách biệt với bản sao lưu phục vụ lịch sử cũ.
- Chi tiết tạo khóa thiết bị, cấp khóa cho nhiều thiết bị và điều kiện gửi/nhận khi chưa khôi phục thuộc đặc tả `U-SCR-06`/`U-25`; không triển khai quy trình đó trong use case đăng nhập. Những điều kiện trên là ràng buộc cần đáp ứng khi chọn giao thức, chưa khẳng định đã có cơ chế mật mã chạy được.

## Tin cậy thiết bị mới — đã chốt

- Thiết bị đầu tiên của **tài khoản mới** được khởi tạo bộ khóa và thiết lập tin cậy ban đầu. Không dùng lại nhánh này cho tài khoản đã có dữ liệu E2EE chỉ vì thiết bị cũ đang offline, đã bị thu hồi hoặc không đọc được backup.
- Với tài khoản đã có khóa E2EE, thiết bị đăng nhập mới phải được **thiết bị đã tin cậy xác nhận** hoặc xác nhận bằng **bằng chứng khôi phục** trước khi nhận khóa nhóm. Đăng nhập đúng mật khẩu và có phiên hợp lệ chưa đủ để tự cấp khóa nhóm.
- Thiết bị chưa được xác nhận vẫn mở khung chính sau khi đăng nhập; hiển thị rõ đang chờ xác nhận/cấp khóa và chỉ hạn chế thao tác E2EE liên quan. Không tự cấp khóa, dùng bản rõ thay E2EE hoặc ghi đè backup để bỏ qua bước này.
- Việc xác nhận phải gắn với đúng tài khoản, định danh thiết bị và bộ khóa công khai mới. Thiết bị xác nhận phải thuộc cùng tài khoản, đã tin cậy và chưa bị thu hồi tại thời điểm xác nhận; Server kiểm tra điều kiện hiện tại. Một FK hoặc cờ do client tự gửi không thay thế kiểm tra bằng chứng.
- Đề xuất dữ liệu tin cậy trên `e2ee_devices`: `trusted_at`, `trust_method` (`INITIAL`, `TRUSTED_DEVICE`, `RECOVERY`), `approved_by_device_id` và `trust_proof`. `approved_by_device_id` là tham chiếu thiết bị cùng tài khoản, chỉ dùng với `TRUSTED_DEVICE`; `RECOVERY` không giả tạo một thiết bị phê duyệt. Thiết bị chờ xác nhận chưa có các giá trị tin cậy này.
- Với `TRUSTED_DEVICE` hoặc `RECOVERY`, cần bằng chứng có thể xác minh theo giao thức được chọn, gắn với bộ khóa mới và chống phát lại. `trust_proof` chỉ chứa bằng chứng/chữ ký công khai cần cho kiểm tra; không gửi mã khôi phục, private key hoặc khóa nhóm bản rõ lên Server. Cách tạo/kiểm tra bằng chứng khôi phục và dữ liệu kiểm chứng công khai phải được đặc tả trước khi triển khai, không coi việc client báo giải mã thành công là bằng chứng.
- Xác nhận thiết bị không đồng nghĩa đã có mọi khóa hội thoại/lịch sử. Sau xác nhận vẫn kiểm tra tư cách thành viên và cấp đủ envelope cho từng hội thoại được phép. Khi không còn thiết bị tin cậy lẫn mã khôi phục, không tự cấp lại khóa hoặc cam kết mở lịch sử cũ.

## Vòng đời backup và khóa lịch sử — đã chốt

- Backup hiện hành được giữ đến khi có bản thay thế hợp lệ hoặc đến lúc purge tài khoản theo [DEC-07](account-lifecycle.md), tùy điều kiện nào đến trước. Bỏ qua/hủy khôi phục, lỗi đọc backup và đổi/reset mật khẩu không tự thay, xóa hoặc đặt lại thời hạn của backup.
- Backup đã được thay được giữ thêm **30 ngày kể từ `replaced_at`**, sau đó purge bản mã và metadata khôi phục của bản đó. Purge tài khoản có hiệu lực sớm hơn thì thực hiện ngay, không chờ đủ 30 ngày của từng backup.
- Trước khi thay bản hiện hành, client phải kiểm tra bản mới giải mã được và bao phủ các khóa cần khôi phục lịch sử được phép tại thời điểm tạo. Chỉ chuyển revision hiện hành sau thao tác được xác nhận và ghi thành công; dùng kiểm tra revision/transaction để tránh ghi đè đồng thời. Server không kiểm tra nội dung khóa bản rõ trong gói mã hóa.
- Muốn quay về bản backup cũ **còn hạn**, phải thực hiện thao tác được xác nhận và tạo revision hiện hành mới; không xóa hoặc đặt lại `replaced_at` của bản cũ để gia hạn thời gian giữ. Retry không tạo revision mới hoặc khôi phục bản đã purge.
- Epoch và envelope cũ được giữ khi còn cần cho **lịch sử được phép đọc**; không áp dụng thời hạn 30 ngày của backup đã thay cho chúng. Epoch `RETIRED` chỉ ngừng dùng để gửi tin mới, không tự xóa khả năng đọc lịch sử hợp lệ. Purge dữ liệu liên quan vẫn phải giữ lịch sử của các thành viên khác và tuân quyền đọc, không CASCADE xóa tin theo việc thay backup/thu hồi thiết bị.
- Khi rời hoặc bị xóa khỏi nhóm, các thiết bị của tài khoản đó không được cấp khóa bảo vệ tin mới. Việc xoay epoch và cấp envelope phải nhất quán với thay đổi thành viên; chưa bảo đảm điều kiện này thì giữ chặn thao tác E2EE liên quan.

## Tham khảo sản phẩm và phần kỹ thuật còn cần hoàn thiện

Messenger là nguồn tham khảo về nhiều thiết bị và quản lý đầu cuối trong [Meta Engineering (2023)](https://engineering.fb.com/2023/12/06/security/building-end-to-end-security-for-messenger/), cùng lưu trữ/khôi phục lịch sử mã hóa trong [Secure Storage (2024)](https://about.fb.com/news/2024/03/end-to-end-encryption-on-messenger-explained/). Cách xác nhận thiết bị và thời hạn 30 ngày nêu trên là quyết định của **ChatSystem**, không khẳng định là quy trình hay schema nội bộ của Messenger.

- Các chính sách thiết bị/backup ở trên đã chốt; phần còn cần thiết kế và kiểm chứng là transcript cấp khóa, cấu trúc metadata/bằng chứng, nguồn kiểm chứng khôi phục, xác thực/thu hồi bộ khóa, tham số KDF, chống phát lại, prototype xoay khóa và chia sẻ lịch sử nhiều thiết bị, tìm kiếm và archive. Những việc này không mở lại quyết định về quyền nhận khóa hoặc thời hạn backup; chưa được coi là giao thức mật mã đã chạy được.
- **Độ mạnh bí mật khôi phục khóa E2EE (thống nhất theo Issue #82):**
  - Ở Giai đoạn 1.4 (thử nghiệm rủi ro sớm): thử sinh khóa thiết bị (`X25519`/`Ed25519`), bọc khóa nhóm (`AES-256-GCM`), xác nhận từ thiết bị tin cậy (`TRUSTED_DEVICE`) và thử đóng gói backup với khóa/dữ liệu giả lập.
  - Trước khi triển khai chính thức `U-25` ở Giai đoạn 4: bắt buộc dùng **mã khôi phục ngẫu nhiên có entropy cao (tối thiểu 128 bit)** hoặc xác nhận từ thiết bị tin cậy (`TRUSTED_DEVICE`) để bảo vệ `e2ee_key_backups`; **không** dùng PIN 6 chữ số làm bí mật duy nhất bảo vệ backup khóa thật (các nhãn UI Version 1 trên `U-SCR-06` và `U-DLG-01` sẽ được cập nhật tương ứng ở Giai đoạn 4).
