# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-GRP-01
- **Use Case Name**: Tạo nhóm chat
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
- **Primary Actor**: User đã đăng nhập muốn lập nhóm.
- **Secondary Actor(s)**: Các tài khoản được chọn làm thành viên.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User xác định tên và thành viên để lập một nhóm chat dùng E2EE. Yêu cầu kết thúc khi nhóm, tư cách thành viên và vai trò ban đầu được ghi nhận; hoàn tất chia sẻ khóa là trạng thái riêng.

  > **NOTE:** Đề xuất chỉ chọn bạn bè không có quan hệ chặn ở bất kỳ chiều nào với người tạo, tên nhóm tối đa 75 ký tự và nhóm tối đa 250 thành viên, tính cả người tạo nhóm; cần xác nhận trước khi áp dụng. [nguồn tham khảo](https://www.facebook.com/fb-answers/is-there-a-limit-on-how-many-people-can-be-in-a-group-chat/).

- **Goal**: Có một nhóm với đúng tên và tập thành viên đã xác nhận; người tạo là Admin đầu tiên.
- **Trigger**: User muốn lập nhóm để trao đổi với các tài khoản được chọn.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên User hợp lệ; có thể liên hệ server.
  - Có ít nhất một tài khoản khác đủ điều kiện để được chọn; khóa cần thiết trên thiết bị người tạo sẵn sàng cho yêu cầu tạo nhóm E2EE.
  - Luồng cơ bản dùng các bạn bè còn đủ điều kiện và không có quan hệ chặn ở bất kỳ chiều nào với người tạo.

    > **NOTE:** Đề xuất chỉ chọn bạn bè còn đủ điều kiện và không có quan hệ chặn ở bất kỳ chiều nào với người tạo; cần xác nhận trước khi áp dụng.

- **Post-conditions**:
  - Thành công: nhóm có định danh, tên, người tạo là Admin và các thành viên thường đúng danh sách đã xác nhận; không tạo trùng cho cùng yêu cầu.
  - Nhóm mặc định E2EE. Việc tạo thành công không chứng minh mọi thành viên/thiết bị đã được chia sẻ khóa hay đọc được lịch sử.
  - Hủy trước khi gửi hoặc yêu cầu bị từ chối không tạo nhóm; mất xác nhận thì kết quả còn chưa rõ cho tới khi đối chiếu. Bản nháp hội thoại khác được giữ.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn tạo nhóm mới. | Xác định người tạo và cung cấp các tài khoản đủ điều kiện theo chính sách hiện hành. |
| 2 | Nhập tên nhóm và chọn ít nhất một tài khoản khác. | Kiểm tra tên không rỗng/chỉ có khoảng trắng, lựa chọn không trùng và không tính chính người tạo thay cho thành viên khác. |
| 3 | Kiểm tra danh sách thành viên và xác nhận lập nhóm. | Gắn yêu cầu với tên, danh sách đã xác nhận và định danh yêu cầu; chưa tự thay người không còn hợp lệ. |
| 4 | Xác nhận tạo nhóm. | Server xác thực phiên, kiểm tra lại toàn bộ lựa chọn, quan hệ và điều kiện nhóm/khóa; ghi nhận nhóm, Admin đầu tiên và thành viên nhất quán. |
| 5 | Nhận kết quả đã tạo nhóm. | Trả định danh nhóm và kết quả tạo đã được xác nhận; phân biệt tình trạng chia sẻ khóa chưa hoàn tất. |
| 6 | Tiếp nhận nhóm vừa tạo. | Kết thúc với nhóm tồn tại; đọc lịch sử/gửi tin về sau thuộc các UC nhắn tin, không kéo dài mục tiêu tạo nhóm. |

### Alternative Flows

- **Alt 1a — Có sẵn một thành viên dự kiến**: Tại bước 1–2, User có thể đưa một người bạn làm lựa chọn ban đầu rồi bổ sung/bỏ lựa chọn. Hệ thống vẫn kiểm tra điều kiện mới nhất; quay lại bước 2, không coi lựa chọn ban đầu là đã đồng ý tạo nhóm.

- **Alt 2a — Thay danh sách thành viên hoặc không có kết quả tìm**: Tại bước 2, thay tiêu chí tìm không tự bỏ lựa chọn còn hợp lệ. Không có kết quả khớp khác với không có bạn bè đủ điều kiện; User thay lựa chọn hoặc kết thúc không tạo nhóm.

- **Alt 2b — Những người được chọn chặn nhau**:

  > **NOTE:** Đề xuất cần chốt việc cho phép thêm những người đang có quan hệ chặn với nhau vào cùng nhóm; chưa tự chọn phương án cho phép hoặc từ chối.

- **Alt 4a — Thành viên offline**: Tại bước 4, không từ chối chỉ vì thành viên offline; ghi nhận tư cách thành viên theo yêu cầu. Nếu chia sẻ khóa chưa xong, áp dụng Alt 5a.

- **Alt 5a — Nhóm tồn tại nhưng khóa chưa hoàn tất**: Sau bước 4 thành công, tiếp tục xử lý khóa trên đúng nhóm đã có, không tạo nhóm khác để thử lại. Chỉ đọc/gửi khi quyền và khóa phù hợp, không gửi bản rõ thay thế.

- **Alt 3a — Hủy trước khi gửi**: Trước bước 4, User rút ý định tạo; không tạo nhóm hoặc thay quan hệ. Nếu yêu cầu đã gửi, việc rời giao diện không chứng minh đã hủy; áp dụng Ex 4b.

### Exception Flows

- **Ex 1a — Không lấy được dữ liệu lựa chọn**: Không coi lỗi tải là danh sách rỗng; giữ đầu vào còn phù hợp, cho đối chiếu/thử lại bước 1–2.

- **Ex 2a — Tên hoặc số thành viên không hợp lệ**: Không gửi yêu cầu cho tên rỗng/chỉ có khoảng trắng, thiếu người khác hoặc lựa chọn trùng/sai tài khoản.

  > **NOTE:** Đề xuất tên nhóm tối đa 75 ký tự và nhóm tối đa 250 thành viên, tính cả người tạo nhóm; cần xác nhận giới hạn và cách đếm tên. [nguồn tham khảo](https://www.facebook.com/fb-answers/is-there-a-limit-on-how-many-people-can-be-in-a-group-chat/).

- **Ex 4a — Quan hệ hoặc tài khoản thay đổi**: Từ chối toàn bộ yêu cầu khi một lựa chọn không còn hợp lệ; không âm thầm bỏ người rồi tạo nhóm khác danh sách xác nhận. User đối chiếu, chọn lại và xác nhận bước 3.

- **Ex 4b — Mất kết nối hoặc chưa rõ kết quả**: Đối chiếu bằng cùng định danh yêu cầu trước khi gửi lại. Đã tạo thì trả nhóm cũ; chỉ cho xác nhận thử lại khi biết chưa ghi nhận. Không đếm thêm nhóm/thành viên do reconnect.

- **Ex 4c — Phiên/khóa không hợp lệ hoặc lỗi ghi nhận**: Dừng yêu cầu cần xác thực; không tạo nhóm thiếu Admin/thành viên rồi báo thành công, không chuyển sang nhóm không E2EE. Giữ đầu vào còn hợp lệ; thiết lập/khôi phục khóa thuộc phân hệ E2EE riêng.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01 — Nhóm tối thiểu gồm người tạo và một tài khoản khác; người tạo là Admin đầu tiên, không tự thêm/bớt người ngoài tập xác nhận.
  - BR-02 — Tên không rỗng/chỉ gồm khoảng trắng.
  - BR-03 — Người tạo và người được chọn không có quan hệ chặn hai chiều
  - BR-04 — Tạo nhóm, ghi Admin đầu tiên và danh sách thành viên nhất quán; định danh yêu cầu xử lý lặp an toàn. User vẫn có thể chủ động tạo nhóm khác trùng tên sau đó.
  - BR-05 — server không giữ khả năng tự giải mã luồng nhóm; không đồng nhất tư cách thành viên với đã có khóa.
- **Non-Functional Requirements (NFR)**:
  - NFR-01 — Kiểm tra phiên và điều kiện tại server, không tin danh sách/quan hệ cũ từ client.
  - NFR-02 — Không ghi khóa bí mật hoặc nội dung nhạy cảm vào log/lỗi; không mất bản nháp hội thoại nguồn khi tạo/hủy nhóm.
  - NFR-03 — Phản hồi trả muộn gắn đúng yêu cầu/nhóm, không sửa dữ liệu hội thoại khác.

    > **NOTE:** Đề xuất cần thống nhất ngưỡng độ trễ tạo nhóm và điều kiện đo trước nghiệm thu.
