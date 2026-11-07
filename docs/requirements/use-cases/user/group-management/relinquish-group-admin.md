# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-GRP-03
- **Use Case Name**: Bàn giao quyền và trở thành thành viên thường
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 07/11/2026
- **Primary Actor**: Admin nhóm muốn thôi quyền nhưng vẫn ở lại.
- **Secondary Actor(s)**: Thành viên nhận quyền kế nhiệm khi cần; các thành viên nhóm.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin chuyển mình về vai trò thành viên thường mà không rời nhóm; nếu là Admin cuối cùng thì cần bàn giao cho một thành viên đủ điều kiện trong cùng kết quả thay đổi.
- **Goal**: User không còn quyền quản trị nhưng vẫn là thành viên, nhóm còn Admin.
- **Trigger**: Admin muốn thôi vai trò quản trị mà vẫn ở lại nhóm.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ; User là Admin hiện tại của nhóm đang hoạt động.
  - Luồng cơ bản User là Admin cuối cùng và có ít nhất một thành viên khác đủ điều kiện kế nhiệm; trường hợp còn Admin khác tại Alt 2a.
- **Post-conditions**:
  - Thành công: User là thành viên thường, không thay số thành viên; người kế nhiệm khi cần có quyền Admin.
  - Cấp quyền kế nhiệm và hạ quyền User cùng thành công hoặc cùng không thay đổi; không để nhóm còn người không có Admin.
  - User giữ quyền đọc/gửi theo tư cách thành viên và trạng thái khóa hiện tại, không coi đổi vai trò là hoàn tất chia sẻ/khôi phục khóa.
  - Hủy hoặc bị từ chối trước thay đổi không đổi vai trò; chưa rõ kết quả thì đối chiếu theo yêu cầu cũ.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn hạ quyền nhưng ở lại đúng nhóm. | Xác định vai trò và tư cách thành viên hiện tại; không hiểu ý định là rời nhóm. |
| 2 | Xác định điều kiện bàn giao. | Kiểm tra số Admin và cung cấp ứng viên thành viên hiện tại đủ điều kiện nếu cần kế nhiệm. |
| 3 | Chọn người kế nhiệm và xác nhận hậu quả ở lại làm thành viên thường. | Gắn người nhận quyền, nhóm và loại thao tác với định danh yêu cầu; không ghi nhận quyền chỉ từ việc chọn ứng viên. |
| 4 | Gửi yêu cầu thay đổi. | Server kiểm tra lại quyền, số Admin và ứng viên, cấp quyền/hạ quyền nguyên tử theo điều kiện hiện tại. |
| 5 | Nhận kết quả. | Ghi nhận User là thành viên thường; người kế nhiệm nếu cần trở thành Admin, tư cách thành viên của User được giữ nguyên. |
| 6 | Tiếp tục tham gia nhóm. | Kết thúc mục tiêu bàn giao; các thao tác quản trị sau đó bị kiểm tra theo vai trò mới, không xóa lịch sử hay bản nháp. |

### Alternative Flows

- **Alt 2a — Đã có Admin khác**: Tại bước 2, không bắt buộc chọn kế nhiệm. User xác nhận hạ quyền bản thân, tiếp tục bước 4; không tự cấp thêm quyền cho một ứng viên từng được chọn.

- **Alt 3a — Đổi người kế nhiệm hoặc hủy**: Trước bước 4, đổi ứng viên thì xác nhận lại danh tính/hậu quả ở bước 3. Hủy giữ User là Admin; không thực hiện phần cấp quyền riêng.

- **Alt 4a — Vai trò/số Admin thay đổi**: Tại bước 4 trước ghi nhận, tải lại điều kiện và yêu cầu xác nhận đúng kết quả mới, không âm thầm chuyển thành rời nhóm hoặc bàn giao khác. Nếu yêu cầu cũ đã thành công, chỉ trả lại kết quả cũ.

- **Alt 3b — Muốn rời nhóm**: User thay mục tiêu sang rời thì kết thúc ý định hạ quyền và dẫn UC-U-GRP-02; cần xác nhận riêng, không dùng xác nhận ở lại để cho rời.

### Exception Flows

- **Ex 2a — Nhóm chỉ còn User hoặc không có ứng viên hợp lệ**: Không cho Admin cuối cùng tự hạ quyền rồi ở lại nhóm không có Admin. Nếu còn người khác nhưng không có ứng viên đủ điều kiện, giữ quyền User, cho đối chiếu/hủy; không tự chọn người ngoài nhóm.

- **Ex 4a — Người nhận quyền hoặc User mất điều kiện**: Từ chối toàn bộ yêu cầu nếu ứng viên rời nhóm/khóa/xóa, User mất quyền hoặc nhóm không còn hoạt động. Không cấp quyền một phần và không tự chuyển sang ứng viên khác.

- **Ex 4b — Timeout hoặc lỗi ghi nhận**: Không báo thành công khi thiếu xác nhận; đối chiếu cùng ID trước gửi lại. Cùng ID khác ứng viên/loại thao tác bị từ chối; không đổi lựa chọn khi kết quả trước chưa rõ.

- **Ex 1a — Phiên không hợp lệ**: Dừng yêu cầu mới; không tiếp tục bằng phiên cũ hoặc coi đã hủy một thay đổi server đã ghi nhận.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01 — Tự hạ quyền nhưng ở lại khác rời nhóm và khác quản lý quyền thành viên khác.

  - BR-02 — Admin cuối cùng muốn hạ quyền phải chọn một thành viên hiện tại khác User, có tài khoản không bị khóa/xóa, làm Admin kế nhiệm. Không bắt buộc ứng viên là bạn bè hoặc đang online, không tự loại vì chặn cá nhân. Nếu còn Admin khác, User được hạ quyền mà không cần chọn người kế nhiệm.

  - BR-03 — Người kế nhiệm trở thành Admin ngang quyền với các Admin khác; không tạo vai trò chủ sở hữu hay cấp quyền Admin hệ thống.

  - BR-04 — Cấp và hạ quyền cùng ghi nhận khi cần bàn giao; tư cách thành viên của User giữ nguyên. Đổi vai trò không tự đổi quyền giải mã lịch sử.
  - BR-05 — Hủy trước khi gửi không thay vai trò; rời giao diện sau khi gửi không tự thu hồi thay đổi server.
- **Non-Functional Requirements (NFR)**:
  - NFR-01 — Kiểm tra quyền/thành viên tại server và cập nhật quyền thực tế sau thay đổi; client cũ không tiếp tục thao tác bằng quyền Admin đã mất.
  - NFR-02 — Xử lý lặp an toàn và đối chiếu kết quả cả khi chính yêu cầu cũ đã làm User mất quyền Admin.
  - NFR-03 — Không đưa khóa bí mật/bản rõ tin nhóm vào yêu cầu đổi vai trò, log hoặc lỗi.
