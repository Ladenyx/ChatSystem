# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-GRP-01
- **Use Case Name**: Tạo nhóm chat
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Các thành viên được thêm vào nhóm.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng tạo nhóm chat E2EE với những người bạn được chọn.
- **Goal**: Có nhóm chat mới do người tạo quản trị.
- **Trigger**: Người dùng yêu cầu tạo nhóm.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ, có ít nhất một người bạn đủ điều kiện tham gia và khóa cần thiết để tạo nhóm E2EE.
- **Post-conditions**: Nhóm được tạo với tên và danh sách đã xác nhận; người tạo là Admin đầu tiên, những người được thêm là thành viên thường.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** nhập tên nhóm và chọn ít nhất một người bạn.
2. **Hệ thống** kiểm tra tên, số thành viên và điều kiện tham gia.
3. **Người dùng** xác nhận tạo nhóm.
4. **Hệ thống** kiểm tra lại điều kiện, tạo nhóm cùng các vai trò ban đầu và trả kết quả.

### Alternative Flows

- **A1 — Hủy tạo nhóm**: Tại bước 3, người dùng không xác nhận; kết thúc.
- **A2 — Chưa chia sẻ khóa xong**: Sau bước 4, nhóm đã tồn tại nhưng một số thành viên chưa có khóa. Tiếp tục chia sẻ khóa trên nhóm vừa tạo; chỉ cho đọc/gửi khi đủ điều kiện E2EE.

### Exception Flows

- **E1 — Tên hoặc danh sách không hợp lệ**: Tại bước 2, tên rỗng, vượt giới hạn độ dài hoặc số thành viên không hợp lệ. Người dùng sửa lại ở bước 1.
- **E2 — Thành viên không còn đủ điều kiện**: Tại bước 4, một lựa chọn không còn hợp lệ. Hệ thống từ chối toàn bộ yêu cầu; người dùng chọn và xác nhận lại.
- **E3 — Phiên hoặc khóa không hợp lệ**: Tại bước 4, hệ thống từ chối tạo nhóm; yêu cầu đăng nhập hoặc thiết lập khóa phù hợp.
- **E4 — Lỗi ghi nhận hoặc mất xác nhận**: Tại bước 4, kiểm tra kết quả trước khi thử lại; nếu đã tạo, trả lại nhóm đó.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Nhóm có tối thiểu hai người, gồm người tạo; tổng thành viên không vượt giới hạn của hệ thống.
  - **BR-02**: Tên nhóm không rỗng hoặc chỉ có khoảng trắng, không vượt giới hạn độ dài của hệ thống.
  - **BR-03**: Mỗi người được thêm phải là bạn của người tạo và không có chiều chặn nào với người tạo. Chặn giữa những người được thêm không ngăn họ cùng tham gia; người được thêm có thể offline.
  - **BR-04**: Mọi nhóm dùng E2EE; tạo nhóm thành công không đồng nghĩa mọi thành viên đã nhận đủ khóa.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Ghi nhận nhóm, Admin và thành viên cùng thành công hoặc không thay đổi; gửi lại cùng yêu cầu không tạo nhóm trùng.
  - **NFR-02**: Không gửi bản rõ thay E2EE hoặc ghi khóa bí mật vào log; giữ bản nháp hội thoại đang mở.
