# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-GRP-02
- **Use Case Name**: Rời nhóm
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 05/11/2026
- **Primary Actor**: Thành viên nhóm, gồm cả Admin nhóm.
- **Secondary Actor(s)**: Người kế nhiệm khi cần; các thành viên còn lại.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Thành viên chấm dứt tư cách thành viên, đồng thời bảo đảm nhóm còn người vẫn có Admin. Đây là nguồn duy nhất của luồng rời nhóm, hợp nhất nội dung rời/kế nhiệm trong các đặc tả màn hình trước đây.

  > **NOTE:** Đề xuất phương án rời/kế nhiệm, đóng nhóm khi người cuối cùng rời và giữ lịch sử chỉ đọc

- **Goal**: User không còn là thành viên; quyền tin mới kết thúc và nhóm còn người vẫn có Admin.
- **Trigger**: User muốn chấm dứt tư cách thành viên của một nhóm.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên User hợp lệ; User còn là thành viên của nhóm đang hoạt động khi bắt đầu.
  - Luồng cơ bản có kết nối server và User không là Admin cuối cùng còn người khác; nhánh cần kế nhiệm tại Alt 2a.
- **Post-conditions**:
  - Thành công: User rời nhóm và mất quyền gửi/nhận hoặc giải mã tin nhóm mới; nhóm còn thành viên có ít nhất một Admin.
  - Nếu cần kế nhiệm, cấp quyền người kế nhiệm và rời nhóm cùng thành công hoặc cùng không thay đổi.
  - Lịch sử sau khi rời và trạng thái nhóm khi không còn thành viên tuân theo chính sách được duyệt; không cam kết xóa bản sao/khóa cũ trên thiết bị hoặc khôi phục lịch sử mất khóa.

    > **NOTE:** Đề xuất giữ lịch sử từng được đọc ở chế độ chỉ đọc và đóng nhóm khi người cuối cùng rời

  - Hủy trước khi gửi không thay tư cách/vai trò. Khi chưa rõ kết quả, không giả định đã rời hoặc còn quyền, phải đối chiếu.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn rời nhóm. | Xác định nhóm và tư cách thành viên hiện tại. |
| 2 | Yêu cầu đánh giá điều kiện rời. | Kiểm tra số thành viên, vai trò và số Admin; xác định có cần kế nhiệm hoặc có nhánh nhóm chỉ còn một người. |
| 3 | Kiểm tra hậu quả và xác nhận rời. | Ghi nhận đúng ý định rời và người kế nhiệm nếu nhánh yêu cầu, gắn một định danh yêu cầu. |
| 4 | Gửi yêu cầu đã xác nhận. | Server kiểm tra lại điều kiện mới nhất và thực hiện rời; nếu cần bàn giao thì ghi nhận cùng nhau, không để nhóm còn người thiếu Admin. |
| 5 | Tiếp nhận kết quả. | Trả kết quả đã ghi nhận; cập nhật quyền thành viên và điều kiện E2EE ngăn truy cập tin mới. |
| 6 | Kết thúc tư cách thành viên. | Ngừng tạo/gửi lại tin mới của nhóm từ User đã rời; việc đọc/xóa lịch sử riêng sau rời tuân chính sách còn quyền, không mở lại quyền thành viên. |

### Alternative Flows

- **Alt 2a — Admin duy nhất còn thành viên khác**: Tại bước 2, yêu cầu User chọn đúng một thành viên hiện tại đủ điều kiện làm Admin kế nhiệm; không tự chọn. User kiểm tra người nhận quyền rồi quay lại bước 3. Không có người hợp lệ thì áp dụng Ex 2a, không tách gửi cấp quyền và rời thành hai yêu cầu độc lập.

- **Alt 2b — Nhóm chỉ còn User**: Tại bước 2, xét trước yêu cầu kế nhiệm: không có người để bàn giao. User được xác nhận rõ rời đồng thời đóng nhóm; khi chấp nhận, bước 4 ghi nhận nhóm không còn thành viên và không nhận tin/lời mời mới, không tự xóa lịch sử.

  > **NOTE:** Đề xuất đóng nhóm khi người cuối cùng rời, giữ lịch sử nhóm chỉ đọc.

- **Alt 2c — Còn Admin khác**: Tại bước 2, User rời trực tiếp sau xác nhận, không tự cấp quyền cho ứng viên đã chọn ở lần đánh giá trước. Tiếp tục bước 3.

- **Alt 3a — Hủy hoặc đổi ý định**: Trước bước 4, hủy thì giữ nguyên tư cách/vai trò. Nếu User muốn chỉ thôi quyền nhưng ở lại, kết thúc ý định rời và dẫn tới [UC-U-GRP-03](relinquish-group-admin.md) với xác nhận riêng.

- **Alt 4a — Điều kiện đổi trước ghi nhận**: Tại bước 4, số Admin/thành viên hoặc người kế nhiệm đã đổi: tải lại điều kiện, không tự đổi ứng viên/cấp quyền thêm hoặc thực hiện hậu quả khác nội dung xác nhận; quay lại bước 2–3 khi xác định chưa ghi nhận.

- **Alt 5a — User rời giao diện trong lúc chờ**: Yêu cầu đã gửi vẫn gắn nhóm nguồn; việc đóng giao diện không tự hủy thao tác server. Kết quả muộn chỉ cập nhật đúng yêu cầu, không mở lại hay sửa hội thoại khác.

### Exception Flows

- **Ex 2a — Không có người kế nhiệm đủ điều kiện**: Nhóm còn người nhưng không có ứng viên hợp lệ: không rời hoặc hạ quyền một phần, giữ tư cách/vai trò, cho đối chiếu lại hoặc hủy. Lỗi tải dữ liệu không được coi là không có ứng viên.

- **Ex 4a — Mất quyền/nhóm không còn hoạt động**: Từ chối yêu cầu mới khi User không còn là thành viên, nhóm đóng/không tồn tại hoặc phiên không hợp lệ; không phục hồi quyền để tiếp tục. Nếu đối chiếu cùng yêu cầu đã thành công, trả kết quả cũ mà không cấp lại quyền nhóm.

- **Ex 4b — Lỗi ghi nhận hoặc người kế nhiệm mất điều kiện**: Không thực hiện một phần thay đổi. User chọn lại/xác nhận lại sau khi biết yêu cầu chưa thành công; không để nhóm thiếu Admin.

- **Ex 4c — Timeout/chưa nhận xác nhận**: Đối chiếu định danh yêu cầu trước thử lại; không tạo yêu cầu mới/đổi ứng viên khi kết quả cũ còn chưa rõ. Cùng ID và cùng nội dung trả kết quả cũ, không rời lần hai.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01 — Chính sách rời nhóm

    > **NOTE:** Đề xuất đóng nhóm khi người cuối cùng rời, giữ lịch sử chỉ đọc và dùng mã U-28 cho rời nhóm

  - BR-02 — Người kế nhiệm là thành viên hiện tại khác User, tài khoản không khóa/xóa theo phương án nguồn; không cần là bạn bè/online. Quan hệ chặn không tự loại ứng viên đã là thành viên.
  - BR-03 — Giữ điều kiện nhóm còn người có Admin sau mọi thay đổi đồng thời; bàn giao và rời là một kết quả nhất quán.
  - BR-04 — Rời nhóm không tự thu hồi tin đã gửi, xóa lịch sử người khác hay rút báo cáo; các mục tiêu đó nằm ở UC riêng.
  - BR-05 — Quyền/khóa phải ngăn người đã rời truy cập tin mới. Không gửi bản rõ thay thế khi xử lý khóa chưa hoàn tất.

    > **NOTE:** Đề xuất cần thống nhất giao thức E2EE và cơ chế đổi khóa khi rời nhóm.

- **Non-Functional Requirements (NFR)**:
  - NFR-01 — Server xác thực chủ yêu cầu và kiểm tra lại vai trò/thành viên, không dựa vào dữ liệu client.
  - NFR-02 — Bảo toàn bản nháp và lịch sử theo quyền còn lại; không tự phát tin chờ sau khi rời.
  - NFR-03 — Phản hồi yêu cầu cũ không ghi đè trạng thái nhóm/hội thoại khác; log không lộ khóa hoặc nội dung tin.
