# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-RPT-01
- **Use Case Name**: Gửi báo cáo spam
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
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

    > **NOTE:** Đề xuất cho phép báo cáo từ lịch sử sau khi rời nhóm theo BR-14; cần chốt điều kiện truy cập và trường hợp bị xóa khỏi nhóm, chưa tự coi quyền này đã được duyệt.

  - Có ít nhất một tin của tài khoản đích còn nội dung được phép dùng. Không bắt buộc đã kết bạn, online hay được gửi tin riêng; chặn không tự ngăn báo cáo lịch sử được phép đọc.
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
| 5 | Chờ xác nhận tiếp nhận. | Server xác thực chủ báo cáo, nguồn, tài khoản/người gửi từng tin và tham chiếu hợp lệ; kiểm tra đầu vào/hạn mức theo chính sách được chốt, đối chiếu yêu cầu lặp. Ghi nhận báo cáo và bằng chứng nhất quán, trả ID cùng thời điểm tiếp nhận. |
| 6 | Nhận xác nhận báo cáo được tiếp nhận. | Hoàn tất mục tiêu gửi. Mục tiêu xem lại thuộc [UC-U-RPT-02](track-spam-report.md); việc kiểm duyệt thuộc phân hệ Admin do teammate phụ trách, ngoài phạm vi use case này. |

### Alternative Flows

- **Alt 2a — Đổi tài khoản đích trong nhóm**: Trước bước 4, User chọn tài khoản khác. Bỏ bằng chứng không còn đúng người gửi, yêu cầu chọn lại và xem/xác nhận bản cập nhật; quay về bước 2–3, không mang xác nhận của mục tiêu cũ sang hồ sơ mới.
- **Alt 2b — Chọn thêm/bớt bằng chứng**: User chọn tin khác trong cùng nguồn/người gửi, giữ lựa chọn hợp lệ khi tải thêm lịch sử. Quay bước 3; không tự chọn tin mới đến. Bỏ hết thì chưa được gửi.
- **Alt 3a — Sửa trước xác nhận**: User sửa lý do/lựa chọn, hệ thống chuẩn bị lại dữ liệu và yêu cầu xác nhận nội dung mới; quay bước 3.
- **Alt 4a — Hủy hoặc không đồng ý chia sẻ**: Trước khi phát yêu cầu, kết thúc không gửi báo cáo/bản rõ; nháp chat được giữ.
- **Alt 4b — Tin nguồn đổi trước tiếp nhận**: Nếu tin đã thu hồi/xóa riêng/mất quyền đọc trước gửi, cập nhật lựa chọn và yêu cầu xác nhận lại. Nếu server đã tiếp nhận trước khi xử lý thu hồi, bản chụp giữ theo chính sách báo cáo; không sửa bản chụp theo lịch sử chat sau đó.
- **Alt 5a — Rời ngữ cảnh khi đang chờ**: Ngừng chờ không tự hủy báo cáo đã phát. Phản hồi gắn đúng yêu cầu; không áp dụng sang hồ sơ/hội thoại mới.
- **Alt 5b — Gửi lặp cùng yêu cầu**: Server đã tiếp nhận thì trả đúng báo cáo cũ, không tạo thêm hoặc tính thêm hạn mức. Cùng ID với dữ liệu khác bị từ chối; không thay nội dung báo cáo cũ.

### Exception Flows

- **Ex 2a — Không có bằng chứng hợp lệ**: Phân biệt đang tải, lỗi tải và thực sự không có tin đủ quyền. Không gửi báo cáo chỉ có lý do; không dùng tin của mình, tin hệ thống, khác người gửi/nguồn hoặc chưa giải mã.
- **Ex 2b — Mất quyền hoặc tài khoản/nguồn không khớp**: Từ chối tham chiếu giả, tài khoản đích không là đối phương chat riêng hoặc không đúng người gửi trong nhóm; không sửa âm thầm thành báo cáo một người khác.
- **Ex 4a — Đầu vào không hợp lệ/vượt giới hạn**: Yêu cầu sửa theo cấu hình đã duyệt; không cắt nội dung hay bỏ bằng chứng rồi gửi báo cáo khác bản xác nhận.
- **Ex 5a — Timeout/mất xác nhận**: Giữ định danh và dữ liệu yêu cầu để đối chiếu. Chỉ sau khi xác định chưa tiếp nhận mới cho xác nhận thử lại; không báo thất bại chắc chắn từ timeout hoặc gửi ID mới khi còn chưa rõ.
- **Ex 5b — Từ chối, trùng hoặc quá tần suất**: Không tạo hồ sơ mới; giữ dữ liệu còn hợp lệ cho User điều chỉnh. Khi quá tần suất, chỉ nêu thời điểm thử lại nếu xác định được, không tự gửi sau thời gian chờ.
- **Ex 1a — Phiên không hợp lệ**: Dừng thao tác cần xác thực và dẫn luồng đăng nhập hiện có. Không tự gửi lại bằng phiên mới; yêu cầu cũ đã được tiếp nhận không tự bị hủy.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

- **BR-01 — Phạm vi một hồ sơ**: Một chủ báo cáo, một tài khoản bị báo cáo, một hội thoại nguồn và tập tin của tài khoản đó. Chat riêng và nhóm E2EE là hai ngữ cảnh của cùng mục tiêu gửi.
- **BR-02 — Đồng ý và giới hạn dữ liệu**: Chỉ nội dung đã xem trước/xác nhận cùng metadata tối thiểu; không chỉnh sửa bản rõ tin gốc trong bằng chứng, không tự gửi hồ sơ/danh bạ/lịch sử khác. Thay dữ liệu phải xác nhận lại.
- **BR-03 — Mức xác thực**: Server kiểm tra phiên, metadata/quyền/tham chiếu theo dữ liệu có thể kiểm tra, nhưng không tự chứng minh bản rõ User cung cấp khớp bản mã E2EE chỉ từ ID. Admin được biết đây là bản rõ do người báo cáo cung cấp.

  > **NOTE:** Đề xuất cần thống nhất cơ chế kiểm chứng nội dung bản rõ E2EE và mức tin cậy của bằng chứng.

- **BR-04 — Tách mục tiêu**:Không tự khóa/chặn/hủy kết bạn/xóa tin hoặc rời nhóm.
- **BR-05 — Quyền và riêng tư**: Danh tính chủ báo cáo lấy từ phiên; Admin hệ thống được cấp quyền mới xem bằng chứng. Người bị báo cáo không được truy cập hồ sơ gốc/danh tính người báo cáo.
- **BR-06 — Số tin bằng chứng**:

  > **NOTE:** Đề xuất từ 1 đến 20 tin bằng chứng trong một lần báo cáo.

- **BR-07 — Độ dài lý do**:

  > **NOTE:** Đề xuất lý do có từ 10 đến 1.000 ký tự sau khi loại khoảng trắng đầu/cuối.

- **BR-08 — Kích thước dữ liệu**:

  > **NOTE:** Đề xuất JSON UTF-8 trước nén của báo cáo không vượt quá 256 KB, gồm nội dung, lý do và metadata; không tính header giao thức.

- **BR-09 — Giãn cách gửi**:

  > **NOTE:** Đề xuất hai báo cáo liên tiếp của cùng User cách nhau ít nhất 60 giây.

- **BR-10 — Hạn mức theo giờ**:

  > **NOTE:** Đề xuất mỗi User gửi tối đa 5 báo cáo trong một giờ.

- **BR-11 — Hạn mức theo 24 giờ**:

  > **NOTE:** Đề xuất mỗi User gửi tối đa 20 báo cáo trong 24 giờ.

- **BR-12 — Cửa sổ chống trùng**:

  > **NOTE:** Đề xuất áp dụng cửa sổ chống trùng báo cáo là 24 giờ.

- **BR-13 — Trạng thái tài khoản đích**:

  > **NOTE:** Đề xuất tài khoản đã chặn/chưa kết bạn vẫn có thể áp dụng báo cáo

- **BR-14 — Báo cáo sau khi rời nhóm**:

  > **NOTE:** Đề xuất User vẫn có quyền báo cáo sau khi rời nhóm

- **BR-15 — Thời hạn lưu bản rõ**:

  > **NOTE:** Đề xuất lưu bản rõ bằng chứng tối đa 30 ngày.

- **BR-16 — Nhận diện báo cáo trùng**:

  > **NOTE:** Đề xuất trong cửa sổ tại BR-12, báo cáo có cùng người gửi, tài khoản đích, hội thoại nguồn và tập ID tin được xem là trùng, kể cả khi đổi lý do hoặc thứ tự tin.

- **BR-17 — Bằng chứng cho báo cáo tiếp theo**:

  > **NOTE:** Đề xuất báo cáo tiếp theo trong cửa sổ chống trùng cần có tin bằng chứng mới.

### Non-Functional Requirements (NFR)

- **NFR-01**: Ghi hồ sơ/bằng chứng nhất quán, idempotency với cùng yêu cầu và thứ tự tiếp nhận/thu hồi do server xác định; không tin đồng hồ client.
- **NFR-02**: Truyền bảo mật, bảo vệ bản rõ lưu cho kiểm duyệt; không ghi nội dung/lý do, token hoặc khóa vào log vận hành. Không thực thi mã/liên kết trong bằng chứng.
- **NFR-03**: Lỗi báo cáo không làm mất nháp hoặc thay đổi quyền chat hiện tại; đối chiếu bất đồng bộ không cập nhật nhầm hội thoại.

  > **NOTE:** Đề xuất hạn kiểm duyệt 15 ngày thuộc UC quản trị, còn chờ xác nhận; không dùng mốc này làm timeout gửi báo cáo.
