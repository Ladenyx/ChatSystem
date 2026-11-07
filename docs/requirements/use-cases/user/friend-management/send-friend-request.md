# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-FRD-03
- **Use Case Name**: Gửi lời mời kết bạn
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
- **Primary Actor**: User muốn kết bạn với tài khoản khác.
- **Secondary Actor(s)**: Người nhận lời mời.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User đề nghị kết bạn với một tài khoản đủ điều kiện; server ghi nhận lời mời chờ, chưa tạo tình bạn.
- **Goal**: Lời mời kết bạn hợp lệ được ghi nhận đúng một lần.
- **Trigger**: User muốn đề nghị kết bạn với tài khoản đã xác định.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ, tài khoản đích khác chính User và được phép tương tác.
  - Hai bên chưa là bạn và không có chiều chặn nào; kiểm tra lại tại server khi tiếp nhận.
  - Không có lời mời nhận vào đang chờ từ tài khoản đích. Nếu đã có, User chỉ được xử lý lời mời theo [UC-U-FRD-04](process-friend-request.md), không gửi lời mời ngược chiều.
- **Post-conditions**:
  - Thành công tạo/đối chiếu lời mời chờ tới đúng người nhận, chưa tạo tình bạn.
  - Hủy hoặc từ chối không tự tạo tình bạn, chặn hay gửi tin.
  - Khi chưa rõ kết quả, đối chiếu trước khi tạo lời mời mới.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn người muốn đề nghị kết bạn. | Lấy trạng thái quan hệ hiện tại, không suy từ kết quả tìm cũ. |
| 2 | Xác nhận gửi đề nghị. | Server kiểm tra phiên/tài khoản, quan hệ bạn bè, chặn và lời mời đang có. |
| 3 | Nhận kết quả tiếp nhận. | Ghi nhận một lời mời chờ tới người nhận, trả trạng thái đã xác nhận và đồng bộ quan hệ liên quan. |

### Alternative Flows

- **Alt 2a — Đã có lời mời gửi đi**: Tại bước 2, trả lời mời đang chờ thay vì tạo trùng; kết thúc với trạng thái đã có.
- **Alt 2b — Đã có lời mời nhận vào**: Tại bước 1–2, nếu có lời mời từ tài khoản đích đang chờ, trả trạng thái chờ chấp nhận/từ chối và không cho gửi lời mời ngược chiều. Nếu yêu cầu gửi vẫn tới server do trạng thái cũ hoặc thao tác đồng thời, server từ chối tạo lời mời mới và trả trạng thái hiện tại. Dẫn User sang [UC-U-FRD-04](process-friend-request.md) để tự quyết định; không tự chấp nhận lời mời đã nhận.
- **Alt 2c — Hai bên đã là bạn**: Tại bước 2, trả quan hệ hiện tại, không tạo lời mời; kết thúc không có thay đổi.
- **Alt 2d — Hủy ý định trước tiếp nhận**: Trước ghi nhận ở bước 2, User hủy; không tạo yêu cầu mới.

### Exception Flows

- **Ex 2a — Một bên vừa chặn hoặc tài khoản không còn khả dụng**: từ chối tiếp nhận, cập nhật trạng thái được phép biết.
- **Ex 2b — Mất xác nhận**: đối chiếu trạng thái hiện tại trước thử lại; server xử lý lặp an toàn, không tạo nhiều lời mời cho cùng cặp.
- **Ex 1a — Tự gửi cho mình hoặc phiên không hợp lệ**: từ chối, không cập nhật quan hệ.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: Chỉ lời mời được người nhận chấp nhận mới tạo tình bạn; gửi không tự đồng ý, kể cả có lời mời ngược chiều.
  - BR-02: Không tạo tình bạn/lời mời chờ trùng cho cùng cặp; kiểm tra quyền trên dữ liệu mới nhất, kể cả các yêu cầu đồng thời.
  - BR-03: Người nhận không cần online; online là trạng thái hiện diện, không phải điều kiện tiếp nhận.
  - BR-04: Gửi lời mời không tự mở quyền chat, tạo nhóm hoặc khôi phục quan hệ từng bị hủy.
  - BR-05: Có lời mời nhận vào đang chờ thì chức năng gửi lời mời cho cùng tài khoản không khả dụng; chỉ người nhận được chấp nhận/từ chối theo UC-U-FRD-04. Quy tắc được kiểm tra tại server, không chỉ dựa vào trạng thái điều khiển trên UI. Sau khi lời mời kết thúc, đối chiếu quan hệ và các điều kiện hiện tại; không tự gửi lời mời mới.
- **Non-Functional Requirements (NFR)**:
  - NFR-01: Kết quả cập nhật nhất quán giữa lời mời gửi/nhận; phản hồi lặp không tạo thêm thực thể.
  - NFR-02: Không dùng tài khoản đích do client truyền để gửi thay người khác; xác thực actor tại server.
