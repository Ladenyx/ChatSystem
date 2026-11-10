# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-MSG-02
- **Use Case Name**: Gửi tin nhắn
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng gửi tin
- **Secondary Actor(s)**: Người nhận chat riêng hoặc các thành viên nhóm.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng gửi tin nhắn vào hội thoại cá nhân hoặc nhóm.
- **Goal**: Tin được server lưu và chuyển tới người nhận khi có thể.
- **Trigger**: Người dùng yêu cầu gửi nội dung đã soạn.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và quyền gửi; chat riêng không có chiều chặn nào, chat nhóm cần tư cách thành viên và khóa gửi phù hợp.
- **Post-conditions**: Server lưu tin và trả xác nhận cùng định danh tin; việc người nhận đã nhận/đọc được cập nhật sau.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** gửi nội dung vào hội thoại đã chọn.
2. **Hệ thống** kiểm tra nội dung, gán định danh tin và mã hóa trên thiết bị nếu là chat nhóm.
3. **Server** kiểm tra quyền hiện tại, lưu tin và trả xác nhận.
4. **Hệ thống** ghi nhận gửi thành công và chuyển tin tới người nhận đang online.

### Alternative Flows

- **A1 — Người nhận offline**: Tại bước 4, tin vẫn được xác nhận đã lưu và sẽ đồng bộ khi người nhận kết nối lại.
- **A2 — Mất kết nối hoặc chưa nhận xác nhận**: Tại bước 3, giữ yêu cầu chờ và thử lại với cùng ID khi kết nối lại. Nếu server đã lưu, trả kết quả cũ.

### Exception Flows

- **E1 — Nội dung không hợp lệ**: Tại bước 2, hệ thống từ chối gửi và giữ nháp để sửa; không tự cắt hoặc chia tin.
- **E2 — Thiếu khóa nhóm**: Tại bước 2, giữ nháp và yêu cầu thiết lập/khôi phục khóa.
- **E3 — Quyền thay đổi hoặc phiên hết hiệu lực**: Tại bước 3, hệ thống từ chối gửi mới; không chuyển tin, phát thông báo hoặc tự gửi lại khi quyền được khôi phục.
- **E4 — Chuyển tin hoặc thông báo thất bại**: Tại bước 4, giữ trạng thái đã lưu và tiếp tục đồng bộ; không coi tin là gửi thất bại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Phân biệt server đã lưu, thiết bị người nhận đã nhận và người nhận đã đọc.
  - **BR-02**: Gửi lại giữ nguyên ID; không tạo thêm tin hoặc lượt hoạt động. Yêu cầu chưa được lưu phải được kiểm tra quyền hiện tại
  - **BR-03**: Chat riêng không yêu cầu kết bạn nhưng bị cấm khi có một chiều chặn. Tin đã được chấp nhận trước khi chặn vẫn được đồng bộ, theo [DEC-03 (đề xuất)](../../../../decisions/blocking-and-friendship.md).
  - **BR-04**: Tin nhóm dùng E2EE; chặn cá nhân giữa thành viên không ngăn gửi tin nhóm, theo [DEC-05 (đề xuất)](../../../../decisions/group-permissions-and-e2ee.md).
  - **BR-05**: Nội dung do AI hỗ trợ vẫn là bản nháp; người dùng quyết định gửi.
  - **BR-06**: Nội dung phải tuân giới hạn độ dài của hệ thống, áp dụng cho cả chat riêng và nhóm.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Không mất/trùng tin server đã lưu; phản hồi lặp hoặc sai thứ tự không làm trạng thái lùi.
  - **NFR-02**: Không gửi bản rõ thay E2EE; giữ nháp/yêu cầu đúng hội thoại khi lỗi.
  - **NFR-03**: Mục tiêu đề xuất với người nhận online: p95 từ lúc gửi đến khi nhận và giải mã/hiển thị ≤1 giây
