# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-RPT-01
- **Use Case Name**: Gửi báo cáo spam
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 07/11/2026
- **Primary Actor**: User có bằng chứng được phép đọc trong hội thoại nguồn.
- **Secondary Actor(s)**: Không bắt buộc actor phụ hoạt động đồng thời; Admin hệ thống xem hồ sơ trong UC kiểm duyệt riêng.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User báo cáo một tài khoản từ chat riêng hoặc nhóm E2EE, cung cấp các tin của tài khoản đó và lý do sau khi xem trước/xác nhận chia sẻ bản rõ.
- **Goal**: Server tiếp nhận đúng báo cáo và cấp định danh để xem xét.
- **Trigger**: User phát hiện nội dung nghi spam và muốn báo cáo tài khoản gửi.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - User có phiên hợp lệ, được phép truy cập hội thoại nguồn và đọc các tin văn bản làm bằng chứng; nhóm E2EE có bản rõ trên thiết bị, không cần server giải mã.
  - Luồng cơ bản từ nhóm còn quyền thành viên/truy cập.

  - Có ít nhất một tin của tài khoản đích còn nội dung được phép dùng. Không bắt buộc hai tài khoản đã kết bạn hoặc đang online.
  - Có kết nối khi gửi; Admin không cần đang đăng nhập.
- **Post-conditions**:
  - Thành công: báo cáo có ID, chủ báo cáo từ phiên, tài khoản đích, hội thoại nguồn, lý do, bản chụp bằng chứng đã xác nhận và thời điểm tiếp nhận; không ghi nhận một phần như thành công.
  - Thành công chỉ xác nhận tiếp nhận, chưa kết luận vi phạm hoặc áp dụng chế tài.
  - Hủy trước gửi không tạo báo cáo/tiết lộ bằng chứng qua chức năng này; lỗi/chưa rõ kết quả không tự tạo yêu cầu mới.
  - Nháp, lịch sử và quan hệ cá nhân/nhóm không tự bị thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Yêu cầu báo cáo từ hội thoại đang được phép đọc. | Ghi nguồn. Trong chat riêng, tài khoản đích là đối phương; trong nhóm, yêu cầu User chọn đúng một tài khoản gửi tin cần báo cáo. Chưa chia sẻ nội dung. |
| 2 | Xác định tài khoản đích, chọn một hoặc nhiều tin của tài khoản đó trong cùng hội thoại và nêu lý do. | Kiểm tra nguồn/người gửi/quyền đọc, loại lựa chọn trùng; không tự chọn toàn bộ lịch sử hoặc tin của tài khoản khác. |
| 3 | Kiểm tra tài khoản đích, nội dung từng tin và lý do sẽ được cung cấp. | Chuẩn bị bản xem trước đầy đủ; nêu rõ bản rõ được gửi tới server ứng dụng cho Admin hệ thống xem xét, kể cả từ nhóm E2EE. |
| 4 | Xác nhận chia sẻ đúng nội dung đã kiểm tra và gửi báo cáo. | Kiểm tra lại nội dung/quyền, gắn định danh yêu cầu; gửi đúng phần đã xác nhận, không bổ sung tin mới đến hoặc khóa E2EE. |
| 5 | Chờ xác nhận tiếp nhận. | Server xác thực chủ báo cáo, nguồn, tài khoản/người gửi từng tin và tham chiếu hợp lệ; kiểm tra đầu vào và hạn mức của hệ thống, đối chiếu yêu cầu lặp. Ghi nhận báo cáo và bằng chứng nhất quán, trả ID cùng thời điểm tiếp nhận. |
| 6 | Nhận xác nhận báo cáo được tiếp nhận. | Thông báo báo cáo đã được tiếp nhận; chưa kết luận vi phạm hoặc áp dụng chế tài. Hoàn tất mục tiêu gửi, việc kiểm duyệt do Admin hệ thống thực hiện. |

### Alternative Flows

- **Alt 2a — Đổi tài khoản đích trong nhóm**: Trước bước 4, User chọn tài khoản khác. Bỏ bằng chứng không còn đúng người gửi, yêu cầu chọn lại và xem/xác nhận bản cập nhật; quay về bước 2–3, không mang xác nhận của mục tiêu cũ sang hồ sơ mới.
- **Alt 2b — Chọn thêm/bớt bằng chứng**: User chọn tin khác trong cùng nguồn/người gửi, giữ lựa chọn hợp lệ khi tải thêm lịch sử. Quay bước 3; không tự chọn tin mới đến. Bỏ hết thì chưa được gửi.
- **Alt 3a — Sửa trước xác nhận**: User sửa lý do/lựa chọn, hệ thống chuẩn bị lại dữ liệu và yêu cầu xác nhận nội dung mới; quay bước 3.
- **Alt 4a — Hủy hoặc không đồng ý chia sẻ**: Trước khi phát yêu cầu, kết thúc không gửi báo cáo/bản rõ; nháp chat được giữ.
- **Alt 4b — Tin nguồn thay đổi**: Trước khi gửi, nếu tin đã thu hồi/xóa riêng/mất quyền đọc, cập nhật lựa chọn và yêu cầu xác nhận lại. Sau khi báo cáo đã được tiếp nhận, thay đổi lịch sử không tự tạo báo cáo mới hoặc khôi phục tin nguồn đã bị loại.
- **Alt 5a — Rời ngữ cảnh khi đang chờ**: Ngừng chờ không tự hủy báo cáo đã phát. Phản hồi gắn đúng yêu cầu; không áp dụng sang hồ sơ/hội thoại mới.
- **Alt 5b — Gửi lặp cùng yêu cầu**: Server đã tiếp nhận thì trả đúng báo cáo cũ, không tạo thêm hoặc tính thêm hạn mức. Cùng ID với dữ liệu khác bị từ chối; không thay nội dung báo cáo cũ.

### Exception Flows

- **Ex 2a — Không có bằng chứng hợp lệ**: Phân biệt đang tải, lỗi tải và thực sự không có tin đủ quyền. Không gửi báo cáo chỉ có lý do; không dùng tin của mình, tin hệ thống, khác người gửi/nguồn hoặc chưa giải mã.
- **Ex 2b — Mất quyền hoặc tài khoản/nguồn không khớp**: Từ chối tham chiếu giả, tài khoản đích không là đối phương chat riêng hoặc không đúng người gửi trong nhóm; không sửa âm thầm thành báo cáo một người khác.
- **Ex 4a — Đầu vào không hợp lệ/vượt giới hạn**: Thông báo đầu vào vượt giới hạn của hệ thống và yêu cầu User sửa; không cắt nội dung hay bỏ bằng chứng rồi gửi báo cáo khác bản xác nhận.
- **Ex 5a — Timeout/mất xác nhận**: Giữ định danh và dữ liệu yêu cầu để đối chiếu. Chỉ sau khi xác định chưa tiếp nhận mới cho xác nhận thử lại; không báo thất bại chắc chắn từ timeout hoặc gửi ID mới khi còn chưa rõ.
- **Ex 5b — Bị từ chối hoặc vượt hạn mức**: Không tạo hồ sơ mới; giữ dữ liệu còn hợp lệ cho User điều chỉnh. Khi quá tần suất, chỉ nêu thời điểm thử lại nếu xác định được, không tự gửi sau thời gian chờ.
- **Ex 1a — Phiên không hợp lệ**: Dừng thao tác cần xác thực và dẫn luồng đăng nhập hiện có. Không tự gửi lại bằng phiên mới; yêu cầu cũ đã được tiếp nhận không tự bị hủy.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

- **BR-01 — Phạm vi một hồ sơ**: Một chủ báo cáo, một tài khoản bị báo cáo, một hội thoại nguồn và tập tin của tài khoản đó. Chat riêng và nhóm E2EE là hai ngữ cảnh của cùng mục tiêu gửi.
- **BR-02 — Đồng ý và giới hạn dữ liệu**: Chỉ nội dung đã xem trước/xác nhận cùng metadata tối thiểu; không chỉnh sửa bản rõ tin gốc trong bằng chứng, không tự gửi hồ sơ/danh bạ/lịch sử khác. Thay dữ liệu phải xác nhận lại.
- **BR-03 — Mức xác thực**: Server kiểm tra phiên, metadata/quyền/tham chiếu theo dữ liệu có thể kiểm tra, nhưng không tự chứng minh bản rõ User cung cấp khớp bản mã E2EE chỉ từ ID. Admin được biết đây là bản rõ do người báo cáo cung cấp.

- **BR-04 — Tách mục tiêu**: Tiếp nhận báo cáo không tự khóa/chặn/hủy kết bạn/xóa tin hoặc rời nhóm. Admin hệ thống xem xét và quyết định xử lý thủ công.
- **BR-05 — Quyền và riêng tư**: Danh tính chủ báo cáo lấy từ phiên; bản rõ bằng chứng chỉ cung cấp cho người có quyền kiểm duyệt. Không tiết lộ hồ sơ hoặc danh tính qua lỗi hay truy vấn không có quyền.
- **BR-06 — Bằng chứng**: Chọn ít nhất một tin của tài khoản đích trong cùng hội thoại; số tin được chọn phải nằm trong giới hạn của hệ thống.
- **BR-07 — Lý do**: Lý do bắt buộc có nội dung và tuân giới hạn độ dài của hệ thống; không tự cắt hoặc thay nội dung User đã xác nhận.
- **BR-08 — Kích thước và hạn mức**: Dữ liệu báo cáo, tần suất và số lần gửi tuân giới hạn của hệ thống. Khi đạt ngưỡng, thông báo để User điều chỉnh hoặc thử lại khi đủ điều kiện; không tự chia báo cáo hay gửi lại sau thời gian chờ.
- **BR-09 — Yêu cầu lặp**: Cùng một yêu cầu đã được tiếp nhận chỉ trả lại kết quả cũ, không tạo thêm hồ sơ. Đổi nội dung không được ghi đè báo cáo đã tiếp nhận.
- **BR-10 — Quyền báo cáo**: Kiểm tra quyền gửi báo cáo và quyền sử dụng bằng chứng tại thời điểm gửi; không suy quyền từ dữ liệu hội thoại cũ.
- **BR-11 — Lưu giữ bằng chứng**: Bản rõ bằng chứng được bảo vệ như dữ liệu riêng tư dùng cho kiểm duyệt; không đưa vào log hoặc tự khôi phục từ tin đã bị loại khỏi lịch sử.

### Non-Functional Requirements (NFR)

- **NFR-01**: Ghi hồ sơ và bằng chứng nhất quán, xử lý lặp an toàn với cùng yêu cầu; phản hồi mất hoặc đến chậm không tạo thêm báo cáo.
- **NFR-02**: Truyền bảo mật, bảo vệ bản rõ lưu cho kiểm duyệt; không ghi nội dung/lý do, token hoặc khóa vào log vận hành. Không thực thi mã/liên kết trong bằng chứng.
- **NFR-03**: Lỗi báo cáo không làm mất nháp hoặc thay đổi quyền chat hiện tại; đối chiếu bất đồng bộ không cập nhật nhầm hội thoại.
