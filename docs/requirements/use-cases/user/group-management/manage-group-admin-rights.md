# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-GRP-06
- **Use Case Name**: Quản lý quyền Admin của thành viên
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 05/11/2026
- **Primary Actor**: Admin nhóm.
- **Secondary Actor(s)**: Thành viên được cấp/thu hồi quyền và các thành viên nhóm.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin thay đổi quyền quản trị của một thành viên khác đang trong nhóm. Cấp và thu hồi là hai quyết định trong cùng mục tiêu quản lý quyền; không bao gồm ý định tự thôi quyền hay rời nhóm.

  > **NOTE:** Cần chốt mô hình các Admin ngang quyền. Đề xuất người tạo nhóm mặc định là Admin và các Admin khác không thể thu hồi quyền Admin của người tạo nhóm.

- **Goal**: Vai trò của thành viên đích được cập nhật đúng quyết định, nhóm còn người vẫn có Admin.
- **Trigger**: Admin muốn cấp hoặc thu hồi quyền quản trị của một thành viên khác.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên User hợp lệ; User là Admin hiện tại của nhóm đang hoạt động.
  - Tài khoản đích là thành viên hiện tại khác User; vai trò phù hợp thao tác được đề nghị.
  - Luồng cơ bản có kết nối server và thao tác không vi phạm bảo vệ Admin cuối cùng.
- **Post-conditions**:
  - Thành công: vai trò thành viên đích thay đổi đúng quyết định; tư cách thành viên không bị xóa.
  - Không tạo vai trò chủ sở hữu hoặc quyền Admin hệ thống; không cấp quyền thu hồi tin người khác.
  - Hủy hoặc từ chối không đổi quyền; timeout/chưa rõ kết quả phải đối chiếu trước gửi lại.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn thành viên cần thay đổi vai trò. | Đối chiếu quyền Admin của User, tư cách và vai trò đích. |
| 2 | Chọn quyết định cấp hoặc thu hồi. | Kiểm tra vai trò đích và điều kiện bảo vệ Admin cuối cùng; nêu hậu quả vai trò tương ứng. |
| 3 | Xác nhận tài khoản và quyết định. | Gắn nhóm, đích, vai trò mong muốn và định danh yêu cầu. |
| 4 | Gửi yêu cầu đổi quyền. | Server kiểm tra lại phiên, quyền, tư cách và số Admin hiện tại; ghi nhận thay đổi hợp lệ. |
| 5 | Nhận kết quả. | Cập nhật vai trò và quyền thao tác của thành viên đích, giữ tư cách thành viên; kết thúc UC. |

### Alternative Flows

- **Alt 2a — Thu hồi thay cho cấp**: Tại bước 2, nếu đích đang là Admin thì đề nghị chuyển về thành viên thường; vẫn tiếp tục bước 3–5, không thực hiện xóa thành viên.

- **Alt 2b — Vai trò đã đúng trạng thái mong muốn**: Tại bước 2/4, đối chiếu trạng thái hiện tại; không ghi thêm thay đổi trùng hoặc đảo ngược trạng thái chỉ để đáp ứng yêu cầu cũ.

- **Alt 2c — Ý định thay đổi vai trò chính mình**: Kết thúc mục tiêu thay đổi người khác và dẫn [UC-U-GRP-03](relinquish-group-admin.md) với xác nhận riêng; tự rời thuộc [UC-U-GRP-02](leave-group.md). Không dùng UC này để bỏ qua điều kiện kế nhiệm.

- **Alt 3a — Hủy trước gửi**: Trước bước 4, hủy thì không đổi quyền, không thêm/xóa thành viên.

- **Alt 4a — Quyền hoặc số Admin đổi đồng thời**: Tại bước 4 trước ghi nhận, kiểm tra lại và yêu cầu đối chiếu/xác nhận khi kết quả có thể khác; không để nhóm còn người thiếu Admin hoặc dùng quyền Admin đã bị thu hồi.

### Exception Flows

- **Ex 2a — Đích/loại thao tác không hợp lệ**: Không cho đổi quyền người ngoài nhóm hoặc lấy yêu cầu cấp quyền như bằng chứng chia sẻ khóa đã xong; User sửa lựa chọn, quay bước 1.

- **Ex 4a — User mất quyền hoặc đích không còn thành viên**: Server từ chối yêu cầu mới, cập nhật dữ liệu theo trạng thái hiện tại; không phục hồi quyền để tiếp tục.

- **Ex 4b — Thay đổi vi phạm Admin cuối cùng**: Không thu hồi nếu làm nhóm còn người không có Admin; việc bàn giao bản thân phải theo UC-U-GRP-03, không tự chọn người kế nhiệm.

- **Ex 4c — Lỗi ghi nhận/chưa rõ kết quả**: Không báo đổi quyền thành công khi chưa có xác nhận. Đối chiếu cùng ID, nhóm, đích và quyết định trước thử lại; không áp yêu cầu cũ lần nữa hoặc chuyển sang đích khác.

- **Ex 1a — Phiên không còn hợp lệ**: Dừng thao tác mới cần xác thực; kết quả server đã ghi nhận không bị coi là hủy chỉ vì phiên kết thúc.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01 — Quyền quản trị

    > **NOTE:** Đề xuất các Admin ngang quyền và được thu hồi quyền Admin khác theo điều kiện, ngoại trừ người tạo nhóm không thể thu hồi quyền admin.

  - BR-02 — Cấp/thu hồi chỉ áp dụng thành viên hiện tại. Quan hệ bạn bè/chặn dùng cho mời người mới không tự loại một thành viên hiện có khỏi việc đổi quyền.
  - BR-03 — Giữ điều kiện nhóm còn thành viên có ít nhất một Admin dưới các yêu cầu đồng thời; kiểm tra lại tại thời điểm ghi nhận.
  - BR-04 — Đổi quyền không tự thay khóa/quyền đọc lịch sử, không rời nhóm hoặc thu hồi tin của người khác.
- **Non-Functional Requirements (NFR)**:
  - NFR-01 — Kiểm tra quyền tại server, client cập nhật quyền thực tế sau xác nhận; không để client cũ tiếp tục dùng quyền đã mất.
  - NFR-02 — Xử lý lặp an toàn và cập nhật các bên liên quan; phản hồi muộn chỉ thuộc nhóm/yêu cầu nguồn.
  - NFR-03 — Không lộ thông tin xác thực, khóa E2EE hoặc nội dung tin trong yêu cầu/log đổi vai trò.
