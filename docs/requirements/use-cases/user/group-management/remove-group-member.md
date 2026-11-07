# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-GRP-07
- **Use Case Name**: Xóa thành viên khỏi nhóm
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 07/11/2026
- **Primary Actor**: Admin nhóm.
- **Secondary Actor(s)**: Thành viên bị xóa và các thành viên còn lại.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin chấm dứt tư cách thành viên của một người khác trong nhóm và ngăn truy cập tin mới theo quyền/khóa được cập nhật. Không tự đồng nhất xóa thành viên với xóa bản sao trên thiết bị.

- **Goal**: Thành viên đích không còn trong nhóm, mất quyền với tin nhóm mới; nhóm còn người vẫn có Admin.
- **Trigger**: Admin muốn xóa một thành viên khác khỏi nhóm đang quản trị.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ; User là Admin hiện tại của nhóm đang hoạt động.
  - Đích là thành viên hiện tại khác User; thao tác đáp ứng điều kiện quyền và bảo vệ Admin cuối cùng.
  - Có kết nối server để ghi nhận thay đổi.
- **Post-conditions**:
  - Thành công: tư cách thành viên và quyền Admin nhóm của đích nếu có bị gỡ; nhóm còn người vẫn có Admin.
  - Đích không được gửi/nhận hoặc giải mã tin nhóm mới; không được tiếp tục quản trị bằng quyền cũ.
  - Đích vẫn được xem lịch sử từng được đọc ở chế độ chỉ đọc; không cam kết xóa từ xa bản sao/khóa cũ hoặc khôi phục lịch sử mất khóa.

  - Hủy hoặc bị từ chối không xóa tư cách thành viên; chưa rõ kết quả phải đối chiếu trước thử lại.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Xác định thành viên cần loại khỏi nhóm. | Đối chiếu nhóm, quyền User và tư cách thành viên đích. |
| 2 | Yêu cầu đánh giá hậu quả loại thành viên. | Kiểm tra điều kiện bảo vệ Admin cuối cùng và làm rõ đích mất quyền tin mới, không đồng nghĩa xóa lịch sử trên thiết bị. |
| 3 | Xác nhận tài khoản đích và ý định loại. | Gắn đúng nhóm, đích và định danh yêu cầu. |
| 4 | Gửi yêu cầu. | Server xác thực quyền hiện tại và trạng thái thành viên/nhóm; gỡ tư cách và quyền Admin của đích khi hợp lệ. |
| 5 | Tiếp nhận kết quả. | Cập nhật thành viên/quyền và điều kiện khóa để ngăn truy cập tin mới; không báo một phần thay đổi là hoàn tất. |
| 6 | Hoàn tất loại thành viên. | Đích không tạo/gửi lại tin mới hoặc thao tác Admin bằng quyền cũ; các client đối chiếu lại trạng thái khi kết nối lại. |

### Alternative Flows

- **Alt 1a — User muốn loại chính mình**: Không thực hiện tự xóa qua UC này; kết thúc mục tiêu quản trị người khác và dẫn UC-U-GRP-02 với điều kiện rời/kế nhiệm và xác nhận riêng.

- **Alt 2a — Đích là Admin khác**: Tại bước 2, User được xóa Admin khác, kể cả người tạo nhóm, nếu nhóm vẫn còn ít nhất một Admin sau thay đổi. Không tự suy ra Admin hệ thống có cùng quy trình.

- **Alt 3a — Hủy**: Trước bước 4, User rút ý định; không thay tư cách/vai trò hoặc quan hệ bạn bè/chặn.

- **Alt 4a — Đích đã rời/bị xóa trước xử lý**: Đối chiếu trạng thái, không xóa lần nữa hoặc áp sang tài khoản khác. Nếu là cùng yêu cầu đã thành công, trả kết quả cũ; nếu thao tác khác đã thay trạng thái, cho biết không có thay đổi mới từ yêu cầu này.

- **Alt 5a — Xử lý khóa còn chờ**: Tư cách thành viên đã bị gỡ không được coi là cấp lại quyền cho đích. Giữ điều kiện ngăn đích truy cập tin mới; không gửi bản rõ hoặc báo đã hoàn tất ngăn truy cập nếu chưa bảo đảm.

### Exception Flows

- **Ex 4a — User/đích/nhóm không còn đủ điều kiện**: Từ chối yêu cầu mới nếu mất quyền, nhóm không hoạt động hoặc tham chiếu sai; không dùng dữ liệu client cũ để bỏ qua kiểm tra.

- **Ex 4b — Vi phạm điều kiện Admin cuối cùng**: Không ghi nhận thay đổi làm nhóm còn người thiếu Admin; không tự cấp quyền cho tài khoản khác để hợp thức hóa việc xóa.

- **Ex 4c — Lỗi hoặc chưa rõ kết quả**: Không báo xóa thành công khi chưa được xác nhận. Đối chiếu định danh yêu cầu trước thử lại, không tạo thêm sự kiện loại/quyền/khóa trùng do reconnect.

- **Ex 1a — Phiên không còn hợp lệ**: Dừng yêu cầu cần xác thực, không phục hồi phiên hoặc coi thao tác đã ghi nhận là tự hủy.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01 — Chỉ Admin nhóm được xóa thành viên khác, gồm cả Admin khác và người tạo nhóm; thành viên thường không có quyền này. Mọi thao tác phải giữ ít nhất một Admin nếu nhóm còn thành viên.

  - BR-02 — Tự rời là mục tiêu riêng UC-U-GRP-02. Chặn cá nhân/hủy kết bạn không tự xóa tư cách thành viên, không thay quyền Admin nhóm.
  - BR-03 — Gỡ tư cách và quyền Admin đích nếu có phải nhất quán với bảo vệ Admin cuối cùng; không xóa bản sao lịch sử trên thiết bị người khác.
  - BR-04 — Người bị xóa giữ quyền xem lịch sử từng được đọc ở chế độ chỉ đọc, không được gửi/nhận hoặc giải mã tin nhóm mới. Không gửi bản rõ thay thế khi việc cập nhật quyền truy cập chưa hoàn tất.

  - BR-05 — Định danh tài khoản/nhóm xác định đích; không áp kết quả cho tài khoản mới chỉ vì trùng username.
- **Non-Functional Requirements (NFR)**:
  - NFR-01 — Server kiểm tra quyền mới nhất, client đích không tiếp tục dùng quyền thành viên/Admin cũ; đồng bộ lại khi reconnect.
  - NFR-02 — Xử lý lặp an toàn; phản hồi cũ không tác động nhóm/đích khác.
  - NFR-03 — Không đưa khóa bí mật, bản rõ lịch sử hoặc token vào log/lỗi; bản nháp của thành viên khác không bị xóa.
