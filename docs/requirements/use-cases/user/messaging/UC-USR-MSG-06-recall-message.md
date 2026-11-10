# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-MSG-06
- **Use Case Name**: Thu hồi tin đã gửi
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người gửi tin
- **Secondary Actor(s)**: Các bên tham gia hội thoại.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người gửi thu hồi tin của mình trong thời hạn cho phép.
- **Goal**: Nội dung tin không còn được đọc/tìm trong hội thoại của các bên sau khi đồng bộ.
- **Trigger**: Người dùng yêu cầu thu hồi một tin đã gửi.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ; tin thuộc người dùng và còn trong 48 giờ đầu kể từ khi gửi.
- **Post-conditions**: Server ghi nhận thu hồi, giữ dấu báo tin đã thu hồi và đồng bộ việc loại nội dung tới các bên.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** chọn tin của mình và xác nhận thu hồi.
2. **Server** kiểm tra người gửi, quyền hiện tại và thời hạn theo thời điểm tin được ghi nhận.
3. **Hệ thống** ghi nhận thu hồi và loại nội dung khỏi lịch sử/tìm kiếm của các bên khi đồng bộ.

### Alternative Flows

- **A1 — Tin đã thu hồi**: Tại bước 2, trả trạng thái đã ghi nhận; kết thúc.
- **A2 — Người nhận offline**: Tại bước 3, giữ trạng thái thu hồi để đồng bộ khi người nhận kết nối lại.

### Exception Flows

- **E1 — Sai người gửi hoặc hết thời hạn**: Tại bước 2, từ chối thu hồi. Người dùng có thể chọn [xóa phía mình](UC-USR-MSG-04-delete-messages-for-self.md).
- **E2 — Mất quyền hoặc phiên hết hiệu lực**: Tại bước 2, hệ thống từ chối; yêu cầu đăng nhập lại nếu cần.
- **E3 — Lỗi ghi nhận hoặc mất xác nhận**: Tại bước 3, kiểm tra kết quả của tin/yêu cầu trước khi thử lại.
- **E4 — Đồng bộ thất bại**: Sau khi ghi nhận, tiếp tục đồng bộ; không khôi phục nội dung vì một thiết bị chưa nhận cập nhật.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Chỉ người gửi được thu hồi trong 48 giờ đầu, áp dụng cho chat riêng và nhóm; Admin nhóm không được thu hồi tin người khác.
  - **BR-02**: Xóa phía mình không mở rộng quyền hoặc thời hạn thu hồi.
  - **BR-03**: Thu hồi không tự rút báo cáo spam và không bảo đảm xóa bản sao đã lưu ngoài hệ thống.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Trạng thái thu hồi thống nhất giữa lịch sử, tìm kiếm và bộ nhớ đệm; xử lý lặp không tạo tác động trùng.
  - **NFR-02**: Thu hồi không yêu cầu cung cấp khóa giải mã nhóm cho server.