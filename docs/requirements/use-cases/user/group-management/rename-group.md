# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-GRP-04
- **Use Case Name**: Đổi tên nhóm
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 07/11/2026
- **Primary Actor**: Admin nhóm.
- **Secondary Actor(s)**: Các thành viên nhóm nhận thay đổi tên.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin cập nhật tên hiển thị của một nhóm.

- **Goal**: Tên nhóm mới hợp lệ được ghi nhận và các thành viên nhận cùng tên đã xác nhận.
- **Trigger**: Admin muốn thay tên của nhóm đang quản trị.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên User hợp lệ, User là Admin hiện tại của nhóm đang hoạt động.
  - Có kết nối server để ghi nhận thay đổi; tên hiện tại đã được đối chiếu.
- **Post-conditions**:
  - Thành công: tên mới hợp lệ được gắn đúng nhóm; thành viên/vai trò/lịch sử không đổi.
  - Hủy hoặc lỗi không ghi nhận thì giữ tên trước đó; nếu chưa rõ kết quả, đối chiếu trạng thái trước khi thử lại.
  - Tên cập nhật không tạo nhóm/hội thoại mới và không tác động dữ liệu nhóm khác.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn đổi tên nhóm. | Đối chiếu nhóm, vai trò Admin và tên hiện tại. |
| 2 | Nhập tên mới. | Kiểm tra tên không rỗng/chỉ có khoảng trắng và không vượt giới hạn độ dài tên nhóm của hệ thống. |
| 3 | Kiểm tra và xác nhận tên muốn áp dụng. | Gắn tên đích với đúng nhóm và yêu cầu thay đổi. |
| 4 | Gửi yêu cầu. | Server xác thực User, kiểm tra quyền hiện tại và tên; ghi nhận tên mới khi hợp lệ. |
| 5 | Nhận kết quả đổi tên. | Trả tên đã ghi nhận và đồng bộ thay đổi tới các client liên quan; kết thúc UC. |

### Alternative Flows

- **Alt 2a — Tên không thay đổi**: Tại bước 2–3, tên đề nghị không thay đổi so với tên hiện tại: kết thúc không thay đổi dữ liệu, không báo đã tạo nhóm mới.

- **Alt 3a — Sửa lại hoặc hủy**: Trước bước 4, User sửa tên thì quay bước 2 và xác nhận lại; hủy giữ tên cũ, không gửi thay đổi.

- **Alt 4a — Tên thay đổi bởi Admin khác**: Tại bước 4 trước ghi nhận, đối chiếu tên/trạng thái hiện tại; nếu cần xác nhận lại thì không tự áp dụng xác nhận cũ cho nội dung khác.

### Exception Flows

- **Ex 2a — Tên không hợp lệ**: Không ghi tên rỗng/chỉ có khoảng trắng hoặc vượt giới hạn độ dài tên nhóm của hệ thống; giữ nội dung để User sửa. Không tự cắt nội dung.

- **Ex 4a — Mất quyền hoặc nhóm không hoạt động**: Từ chối nếu User không còn là Admin/thành viên hoặc nhóm đóng/không tồn tại; không phục hồi quyền để hoàn tất đổi tên.

- **Ex 4b — Lỗi kết nối/chưa rõ kết quả**: Không báo đổi thành công khi chưa có xác nhận; đối chiếu đúng nhóm/yêu cầu. Nếu đã ghi nhận, dùng kết quả đó, nếu chưa thì tải lại điều kiện và cho xác nhận thử lại.

- **Ex 1a — Phiên không còn hợp lệ**: Dừng thay đổi cần xác thực, không tiếp tục bằng phiên cũ.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01 — Chỉ Admin hiện tại được đổi tên; các Admin có quyền ngang nhau, thành viên thường không có quyền này.

  - BR-02 — Tên bắt buộc không rỗng/chỉ có khoảng trắng và tuân cùng giới hạn độ dài với tên khi tạo nhóm.

  - BR-03 — Đổi tên chỉ thay dữ liệu tên, không thay định danh nhóm, tư cách thành viên, khóa hoặc quyền lịch sử.
  - BR-04 — Nhóm được xác định bằng định danh; tên hiển thị không thay thế định danh khi ghi nhận hoặc đồng bộ thay đổi.

- **Non-Functional Requirements (NFR)**:
  - NFR-01 — Server kiểm tra quyền và đầu vào hiện tại; thay đổi được trả về nhất quán cho các client.
  - NFR-02 — Phản hồi muộn chỉ cập nhật đúng nhóm/yêu cầu, không ghi đè dữ liệu đang soạn của nhóm khác.
  - NFR-03 — Lỗi bảo toàn tên đang đề nghị còn phù hợp; không lộ dữ liệu xác thực trong thông báo.
