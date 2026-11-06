# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-GRP-05
- **Use Case Name**: Thêm thành viên nhóm
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 05/11/2026
- **Primary Actor**: Thành viên nhóm có quyền thêm theo chính sách nhóm.
- **Secondary Actor(s)**: Các tài khoản được thêm và các thành viên hiện tại.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User bổ sung các tài khoản đủ điều kiện vào nhóm đang tham gia. Tư cách thành viên được ghi nhận không có nghĩa đã hoàn tất cấp khóa.

  > **NOTE:** Đề xuất cho thành viên thường thêm bạn bè, kiểm tra điều kiện từng cặp người mời–người được mời và chia sẻ lịch sử E2EE

- **Goal**: Những tài khoản được xác nhận đủ điều kiện trở thành thành viên của đúng nhóm.
- **Trigger**: User muốn bổ sung người tham gia vào nhóm hiện có.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ; User còn là thành viên của nhóm đang hoạt động và có quyền thêm theo phương án được áp dụng.
  - Có ít nhất một tài khoản dự kiến chưa là thành viên, là bạn của người mời và không có chặn ở bất kỳ chiều nào với người mời.

    > **NOTE:** Đề xuất người được thêm là bạn của người mời và không có quan hệ chặn ở bất kỳ chiều nào với người mời

  - Có kết nối server; việc chuẩn bị/trao khóa theo giao thức E2EE được chốt.
- **Post-conditions**:
  - Thành công: các tài khoản đã xác nhận được thêm đúng nhóm một lần; không tự thêm tài khoản ngoài lựa chọn.
  - Tình trạng cấp khóa/lịch sử được phân biệt với tư cách thành viên.

    > **NOTE:** Đề xuất cho đọc lịch sử trước khi tham gia khi được chia sẻ khả năng giải mã hợp lệ, trừ tin đã thu hồi

  - Hủy trước khi gửi hoặc yêu cầu từ chối không thêm người; chưa rõ kết quả phải đối chiếu trước lặp. Không coi mất khóa là lý do gửi lịch sử bản rõ thay thế.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn thêm người vào nhóm. | Đối chiếu nhóm, tư cách và quyền thêm của User. |
| 2 | Chọn các tài khoản dự kiến. | Cung cấp lựa chọn đủ điều kiện; loại thành viên hiện có và tài khoản trùng khỏi tập thêm. |
| 3 | Kiểm tra danh sách và xác nhận. | Gắn đúng nhóm, tập tài khoản được duyệt và định danh yêu cầu. |
| 4 | Gửi yêu cầu thêm. | Server kiểm tra lại quyền, quan hệ và tư cách từng tài khoản; ghi nhận kết quả thêm theo tập đã được xác nhận. |
| 5 | Nhận kết quả thành viên. | Cập nhật danh sách thành viên; trả riêng tình trạng khả năng đọc E2EE khi việc chia sẻ chưa hoàn tất. |
| 6 | Hoàn tất mục tiêu thêm. | Các bước chia sẻ khóa tiếp tục trên nhóm/thành viên đã ghi nhận, không lặp việc thêm để thử lại phần khóa. |

### Alternative Flows

- **Alt 2a — Người được thêm offline**: Không từ chối chỉ vì offline; tiếp tục bước 3. Việc nhận khả năng giải mã có thể chờ theo giao thức, không giả định online là đã có khóa.

- **Alt 2b — Người được thêm có chặn với thành viên khác**: Xét chặn giữa người mời–người được mời; chặn với thành viên khác không tự ngăn thêm, không buộc bỏ chặn giữa các thành viên nhóm.

  > **NOTE:** Đề xuất chỉ xét quan hệ chặn giữa người mời và người được mời; quan hệ chặn với thành viên khác không tự ngăn việc thêm người. CẦN XÁC NHẬN CASE NÀY.

- **Alt 3a — Thay danh sách hoặc hủy**: Trước bước 4, sửa lựa chọn quay bước 2 và xác nhận lại; hủy không thêm người, không thay tình bạn/quan hệ chặn.

- **Alt 5a — Chia sẻ khóa chưa hoàn tất**: Nhóm và tư cách thành viên đã tồn tại; chưa báo được đọc toàn bộ lịch sử. Chỉ trao đổi theo quyền/khóa sẵn sàng, xử lý khóa trên đúng thành viên, không dùng server tự giải mã.

- **Alt 4a — Tài khoản đã được người khác thêm vào nhóm**: Tại bước 4, khi server kiểm tra lại danh sách đã xác nhận, một hoặc nhiều tài khoản đã trở thành thành viên do yêu cầu khác được xử lý trước. Hệ thống ghi nhận trạng thái hiện tại, không thêm trùng hoặc tự thay bằng tài khoản khác; kết quả đối với các tài khoản còn lại cần được xử lý theo phương án được chốt.

  > **NOTE:** Đề xuất cần chốt một trong hai cách: (1) từ chối toàn bộ yêu cầu, không thêm các tài khoản còn lại và yêu cầu User kiểm tra, xác nhận lại danh sách; hoặc (2) tiếp tục thêm các tài khoản còn đủ điều kiện và thông báo riêng tài khoản đã là thành viên, tài khoản được thêm và tài khoản không được thêm. Không báo yêu cầu đã thêm thành công những tài khoản đã được thêm bởi yêu cầu khác.

### Exception Flows

- **Ex 2a — Không lấy được danh sách hoặc thiếu lựa chọn**: Phân biệt lỗi tải với không có tài khoản đủ điều kiện; không ghi nhận thêm nếu chưa có ít nhất một lựa chọn hợp lệ.

- **Ex 4a — Quyền/quan hệ/tài khoản thay đổi**: Không xác nhận toàn bộ thành công khi một lựa chọn không còn hợp lệ; không âm thầm thêm tập khác danh sách đã xác nhận. Đối chiếu kết quả và xác nhận lại trước yêu cầu mới.

  > **NOTE:** Đề xuất cần xác nhận cách ghi nhận toàn bộ hoặc một phần; chưa tự chọn phương án trong bản nháp.

- **Ex 4b — Vượt sức chứa**: Không tự bỏ bớt người để vừa giới hạn.

  > **NOTE:** Đề xuất mỗi nhóm tối đa 250 thành viên hiện tại, tính cả người tạo nhóm nếu vẫn là thành viên. [nguồn tham khảo](https://www.facebook.com/fb-answers/is-there-a-limit-on-how-many-people-can-be-in-a-group-chat/).

- **Ex 4c — Timeout/chưa rõ kết quả**: Đối chiếu định danh yêu cầu trước thử lại để không nhân đôi tư cách thành viên hoặc tiến trình cấp khóa; phản hồi phải chỉ rõ tài khoản nào đã được ghi nhận theo chính sách được chốt.

- **Ex 1a — Phiên hoặc nhóm không hợp lệ**: Dừng yêu cầu mới khi phiên hết hiệu lực, User mất quyền/thành viên hoặc nhóm đóng; không tự phục hồi quyền.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01 — Quyền thêm thành viên

    > **NOTE:** Đề xuất cho Admin và thành viên thường thêm bạn bè theo điều kiện; cần xác nhận ma trận quyền.

  - BR-02 — Xét bạn bè/chặn hai chiều ở từng cặp người mời–người được mời, không áp ràng buộc đó sang mọi cặp trong nhóm.

    > **NOTE:** Đề xuất kiểm tra bạn bè/chặn theo từng cặp người mời–người được mời; phạm vi này còn chờ xác nhận.

  - BR-03 — Không thêm trùng, không tự thêm mình hoặc người ngoài tập xác nhận. Một yêu cầu gửi lặp phải có cùng kết quả, không nhân đôi thành viên.
  - BR-04 — Sức chứa nhóm và kết quả thêm nhiều thành viên.

    > **NOTE:** Đề xuất mỗi nhóm tối đa 250 thành viên hiện tại, tính cả người tạo nhóm nếu vẫn là thành viên; cần chốt xử lý toàn bộ hay thành công một phần khi thêm nhiều người. [nguồn tham khảo](https://www.facebook.com/fb-answers/is-there-a-limit-on-how-many-people-can-be-in-a-group-chat/).

  - BR-05 — Quyền đọc lịch sử chỉ hoàn tất khi có khả năng giải mã hợp lệ; không mặc định server có khóa.

    > **NOTE:** Đề xuất chia sẻ khả năng đọc lịch sử trước khi tham gia; cần xác nhận chính sách và giao thức E2EE.

- **Non-Functional Requirements (NFR)**:
  - NFR-01 — Server kiểm tra điều kiện mới nhất, đồng bộ danh sách/quyền tới client; không chỉ dựa vào danh sách cũ.
  - NFR-02 — Giữ lựa chọn còn hợp lệ khi lỗi; kết quả cũ không áp sang nhóm khác.
  - NFR-03 — Không gửi khóa bí mật/bản rõ lịch sử tới server hoặc ghi vào log để khắc phục lỗi cấp khóa.
