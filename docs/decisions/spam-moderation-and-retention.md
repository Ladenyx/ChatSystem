# Kiểm duyệt và vòng đời báo cáo spam

**DEC-10 — Đã chốt nghiệp vụ ngày 15/11/2026.** Áp dụng cho `U-26`, `A-10`, `A-11` và `A-SCR-05`, theo câu trả lời xác nhận của chủ dự án. Các tên cột dưới đây phục vụ ERD; quyết định này chưa khẳng định SQL/backend đã triển khai.

## Phạm vi và xử lý thủ công

- User Client chỉ gửi báo cáo. Theo dõi kết quả (`UC-USR-RPT-02`) và yêu cầu rút (`UC-USR-RPT-03`) nằm ngoài phạm vi đã chốt.
- Admin hệ thống được xem bằng chứng, khóa tài khoản bằng thao tác riêng có xác nhận, và **đánh dấu đã xử lý kèm ghi chú**. Quyền Admin nhóm không cấp quyền kiểm duyệt hệ thống.
- Trạng thái xử lý gồm `PENDING` khi tiếp nhận và `REVIEWED` sau khi Admin chủ động xác nhận đã xử lý. `REVIEWED` chỉ thể hiện đã ghi nhận việc xem xét; không tự khẳng định tài khoản vi phạm, đang bị khóa hoặc báo cáo được rút.
- Tra cứu, mở chi tiết hoặc khóa tài khoản **không tự chuyển** báo cáo sang `REVIEWED`. Đánh dấu đã xử lý **không tự khóa** tài khoản, thu hồi tin hay xóa bằng chứng.

### Đánh dấu đã xử lý

1. Admin hệ thống yêu cầu ghi nhận đã xử lý báo cáo đang còn dữ liệu được phép kiểm duyệt và nhập ghi chú có nội dung.
2. Server kiểm tra lại phiên quản trị, quyền Admin hệ thống, đúng mã hồ sơ, thời hạn lưu và trạng thái hiện tại.
3. Admin xác nhận thao tác; server kiểm tra lại các điều kiện tại thời điểm ghi và ghi cùng lúc `review_status = REVIEWED`, `reviewed_by_user_id`, `reviewed_at` theo thời gian server và `review_note`.
4. Hệ thống trả trạng thái đã ghi nhận. Không thay đổi trạng thái tài khoản hoặc nội dung gói bằng chứng.

Trong trạng thái `PENDING`, các trường người xử lý, thời điểm và ghi chú chưa có giá trị. Khi chuyển sang `REVIEWED`, ghi chú bắt buộc có nội dung và phải xác định được người xử lý/thời điểm. Sau đó chỉ đọc thông tin xử lý; phạm vi hiện tại không có sửa ghi chú, mở lại hay ghi đè kết quả của Admin khác. Gửi lặp yêu cầu đã hoàn tất trả kết quả cũ; một yêu cầu khác khi hồ sơ đã `REVIEWED` trả trạng thái hiện tại, không thay người xử lý hoặc ghi chú. Mất xác nhận phải đối chiếu trạng thái trước khi thử lại.

Thao tác bị từ chối nếu phiên/quyền không còn hợp lệ, ghi chú rỗng hoặc hồ sơ không còn được phép xử lý. Khi có thao tác đồng thời, chỉ một lần chuyển `PENDING → REVIEWED` được ghi nhận. Hồ sơ đã hết hạn chỉ còn metadata ẩn danh không được nhận thêm ghi chú hoặc khôi phục bằng chứng để tiếp tục thao tác này.

## Selected, Context và giới hạn dung lượng

| Điều kiện | Giá trị đã chốt |
|---|---|
| Số tin được chọn (`Selected`) | Từ 1 đến **20** tin hợp lệ của tài khoản đích |
| Số tin ngữ cảnh (`Context`) | Tối đa **30** tin mới nhất đủ điều kiện từ local của cùng hội thoại |
| Toàn gói báo cáo đã xác nhận, với Evidence sau loại trùng | Không vượt **1 MiB = 1.048.576 byte** trước nén, theo biểu diễn chuẩn bên dưới |

`Evidence = Selected ∪ Context` theo ID tin. Một tin có thể có cả hai nhãn và chỉ được lưu một lần. Giới hạn 30 áp dụng cho Context, không áp dụng cho tổng Evidence; 20 Selected nằm ngoài 30 Context có thể tạo 50 dòng nếu toàn gói vẫn trong hạn mức byte. Lấy ít hơn 30 Context khi local không có đủ dữ liệu hợp lệ; không tải bù từ server.

Khi vượt số lượng hoặc dung lượng, từ chối và để người báo cáo điều chỉnh, xem trước rồi xác nhận lại. Không tự cắt nội dung, bớt Context, bỏ Selected hoặc chia thành nhiều báo cáo. Các giá trị trên được kiểm tra ở server và được cung cấp cho client để hiển thị giới hạn thống nhất.

### Biểu diễn dùng để tính byte

Đây là quy ước kỹ thuật cho ERD/API tiếp theo, nhằm đo cùng một nội dung như nhau ở client và server:

- Tính số byte UTF-8 **trước nén** của toàn object JSON báo cáo đã xác nhận. Object chỉ có các khóa, theo đúng thứ tự: `id`, `reporter_user_id`, `reported_user_id`, `conversation_id`, `reason`, `evidence`. Chủ báo cáo được server xác định từ phiên hợp lệ; không tin một giá trị tùy ý do client khai.
- `evidence` là mảng đã loại trùng, sắp theo `display_order` tăng dần. Mỗi phần tử chỉ có các khóa, theo đúng thứ tự: `source_message_id`, `sender_user_id`, `message_created_at`, `content_plain`, `is_selected`, `is_context`, `display_order`.
- Biểu diễn không có BOM, khoảng trắng thừa hoặc xuống dòng ngoài nội dung chuỗi. UUID dùng dạng chuẩn chữ thường có dấu gạch nối. Timestamp dùng UTC với hậu tố `Z` và sáu chữ số thập phân của giây, thống nhất với độ chính xác thời gian PostgreSQL được dùng cho tin nguồn. Boolean là `true`/`false`; thứ tự là số nguyên từ 1.
- Chuỗi giữ nguyên nội dung, không trim, chuẩn hóa Unicode hoặc đổi xuống dòng. JSON escape dấu nháy kép thành `\"`, dấu gạch chéo ngược thành `\\`; dùng `\b`, `\t`, `\n`, `\f`, `\r` cho năm ký tự điều khiển tương ứng, các ký tự U+0000–U+001F còn lại dùng `\u00xx` với chữ số hex thường. Không escape `/`; ký tự Unicode khác được mã hóa UTF-8 trực tiếp, từ chối chuỗi Unicode không hợp lệ. Tính cả byte dấu ngoặc, tên khóa, dấu phân cách và escape của biểu diễn này.
- Hạn mức **gồm lý do, các ID/metadata ở object ngoài và toàn bộ Evidence**. Không cộng token phiên, header HTTP, overhead giao thức hoặc dữ liệu điều khiển truyền tải; việc nén không làm giảm số byte dùng để kiểm tra hạn mức.

Fingerprint xử lý retry là **SHA-256 do server tự tính trên chính toàn bộ canonical bytes nói trên**. Cùng một biểu diễn được dùng cho hai mục đích riêng: đếm byte giới hạn toàn gói và đối chiếu idempotency; fingerprint không chứng minh bản rõ E2EE là thật. Cùng ID nhưng khác nội dung phải bị từ chối, không nhận fingerprint do client gửi như nguồn quyết định.

## Lưu nội dung 180 ngày và metadata ẩn danh

- Lý do, bản rõ bằng chứng và ghi chú xử lý được giữ tối đa **180 ngày kể từ `received_at` do server ghi nhận**, tương đương 180 × 24 giờ. Đánh dấu xử lý, khóa tài khoản, xem lại hay retry không gia hạn mốc này.
- Từ thời điểm hết hạn, không trả lại nội dung/lý do/ghi chú qua API, UI, cache hoặc retry, kể cả khi tác vụ dọn dữ liệu vật lý chưa chạy. Server kiểm tra hạn trước mọi lần đọc hay ghi xử lý; tác vụ lưu trữ phải xóa phần dữ liệu quá hạn tương ứng.
- Sau hạn, xóa bản rõ, lý do, ghi chú, fingerprint của payload và các liên kết định danh người báo cáo, tài khoản đích, người xử lý, người gửi, tin nguồn, hội thoại. Mã hồ sơ/yêu cầu cũ hoặc ánh xạ tới nó cũng không được giữ trong metadata ẩn danh nếu còn dùng để lần ngược về hồ sơ gốc.
- Chỉ giữ metadata không lưu liên kết tới người/hội thoại: trạng thái xử lý, số Selected/Context/giao/tổng và thống kê theo ngày khi cần. Bản lưu dùng ID nội bộ mới, không lưu ánh xạ về hồ sơ gốc. Giảm độ chi tiết timestamp về ngày hoặc số liệu tổng hợp; không giữ thời điểm chi tiết như một khóa đối chiếu gián tiếp. Đây là biện pháp hạn chế liên kết dữ liệu của thiết kế, không phải khẳng định một bảo đảm ẩn danh pháp lý tuyệt đối.
- Không gọi một bản ghi đã ẩn danh chỉ vì đặt các FK user thành NULL trong khi vẫn giữ source ID, mã hồ sơ cũ, nội dung tự do hoặc fingerprint có thể liên kết. Chính sách này áp dụng cả dữ liệu được sao chép vào nhật ký, cache và bản xuất phục vụ vận hành.

Việc xóa/ẩn danh không chuyển `PENDING` thành `REVIEWED`, không có nghĩa chấp nhận yêu cầu rút, không đảo ngược chế tài và không đặt lại thống kê tiếp nhận. Metadata ẩn danh chỉ phục vụ thống kê/đối chiếu vận hành trong phạm vi không còn dữ liệu người dùng; không dùng để tra cứu hồ sơ gốc hoặc tìm một tài khoản mới trùng username.

## Purge tài khoản và retry

[DEC-07](account-lifecycle.md) vẫn yêu cầu purge tài khoản sau 30 ngày xóa mềm. **Bổ sung đã chốt ngày 16/11/2026:** Khi purge bất kỳ tài khoản liên quan đến báo cáo — người báo cáo, tài khoản đích, người gửi tin bằng chứng (kể cả tin chỉ có nhãn Context) hoặc Admin xử lý — chuyển **toàn bộ báo cáo** đó ngay thành metadata archive ẩn danh và ghi dấu chặn retry, kể cả chưa đủ 180 ngày. Xóa toàn bộ hồ sơ/evidence hoạt động, lý do, bản rõ, ghi chú, fingerprint và liên kết gốc; không chỉ đặt FK thành NULL hay giữ một phần bằng chứng để tiếp tục kiểm duyệt. Quy trình chuyển metadata, ghi tombstone và xóa hồ sơ phải cùng thành công như khi hết hạn; API/UI/cache không được tiếp tục trả hồ sơ gốc hoặc retry để tạo lại dữ liệu sau purge. Việc này chỉ loại snapshot báo cáo, không CASCADE làm mất lịch sử chat còn được phép giữ của thành viên khác.

Trước hạn và trước xử lý ẩn danh, report cùng toàn bộ evidence được ghi cùng thành công; retry cùng ID/cùng gói trả kết quả đã tiếp nhận. Xóa riêng/thu hồi tin nguồn sau tiếp nhận không tự rút hoặc xóa snapshot, trừ xử lý vòng đời theo chính sách này. Admin chỉ xem bản rõ người báo cáo chủ động chia sẻ, không được mở toàn hội thoại hoặc yêu cầu khóa E2EE.

Sau khi ẩn danh hoặc hết hạn, không được nạp payload cũ từ request, cache, log hay `messages` để tạo lại hồ sơ. Yêu cầu retry cũ không có quyền gia hạn hay tái lập dữ liệu đã xóa; cùng ID phải bị từ chối tiếp nhận lại, không trả lại nội dung hoặc liên kết hồ sơ đã loại.

Đề xuất lưu kết quả vòng đời theo hai phần độc lập:

- `spam_report_archives`: ID mới, ngày tiếp nhận/ngày xử lý nếu có/ngày lưu metadata, trạng thái và số Selected/Context/giao/tổng. Không giữ FK, ID gốc, ánh xạ gốc, nội dung hoặc fingerprint payload.
- `spam_report_request_tombstones`: chỉ giữ digest để chặn tái sử dụng ID yêu cầu cũ, không có FK, archive ID, user ID, nội dung hoặc timestamp. Digest SHA-256 tính trên tiền tố miền cố định `ChatSystem:spam-report-request:v1` + byte `0x00` + 16 byte UUID của ID gốc; không chứa định danh người dùng hay payload. Bản này tồn tại để từ chối tái tiếp nhận cùng ID, không cung cấp API tra cứu hồ sơ hoặc nội dung đã xóa.

Chuyển metadata, ghi dấu chặn retry và xóa report/evidence hoạt động phải cùng thành công, không để hồ sơ nửa xóa. Khi đọc/retry đúng lúc quá hạn mà tác vụ định kỳ chưa chạy, server áp dụng cùng xử lý rồi từ chối mở hồ sơ gốc theo mã cũ; không được trả payload quá hạn hoặc ánh xạ mã cũ sang archive. Archive chỉ là nguồn metadata/thống kê riêng. Quy tắc 180 ngày không phụ thuộc tốc độ chạy của tác vụ dọn.

## Tài liệu liên quan

- [UC-USR-RPT-01 — Gửi báo cáo spam](../requirements/use-cases/user/spam-reporting/UC-USR-RPT-01-submit-spam-report.md).
- [UC-ADM-SPAM-01 — Tra cứu báo cáo](../requirements/use-cases/admin/spam-moderation/UC-ADM-SPAM-01-browse-spam-reports.md).
- [UC-ADM-SPAM-02 — Xem bằng chứng](../requirements/use-cases/admin/spam-moderation/UC-ADM-SPAM-02-view-spam-report.md).
- [UC-ADM-SPAM-03 — Khóa tài khoản bị báo cáo](../requirements/use-cases/admin/spam-moderation/UC-ADM-SPAM-03-lock-reported-account.md).
- [DEC-05 — Quyền nhóm và E2EE](group-permissions-and-e2ee.md).
- [Thời gian và thống kê](statistics-and-time.md).

Các chính sách nêu trên đã chốt. Việc thực thi lịch dọn, cấu trúc metadata ẩn danh, transaction, giới hạn tiếp nhận request cũ và định dạng thông điệp E2EE còn cần được thiết kế/kiểm thử ở giai đoạn SQL/backend; chúng không mở lại thời hạn 180 ngày, phạm vi thao tác hoặc hạn mức đã xác nhận.
