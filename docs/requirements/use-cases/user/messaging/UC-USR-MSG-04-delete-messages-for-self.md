# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-MSG-04
- **Use Case Name**: Xóa tin phía mình
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Không có.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng xóa một hoặc nhiều tin khỏi lịch sử của mình.
- **Goal**: Các tin được chọn không còn xuất hiện trong lịch sử và tìm kiếm riêng.
- **Trigger**: Người dùng yêu cầu xóa các tin đã chọn.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và quyền thao tác các tin được chọn trong hội thoại.
- **Post-conditions**: Trạng thái xóa riêng được ghi nhận cho các tin đã xác nhận; lịch sử của người khác được giữ nguyên.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** chọn một hoặc nhiều tin và yêu cầu xóa phía mình.
2. **Hệ thống** kiểm tra phạm vi, quyền và số lượng tin được chọn.
3. **Người dùng** xác nhận xóa.
4. **Hệ thống** kiểm tra lại điều kiện, ghi nhận xóa riêng và loại các tin đó khỏi lịch sử/tìm kiếm của người dùng.

### Alternative Flows

- **A1 — Hủy thao tác**: Tại bước 3, người dùng không xác nhận; kết thúc.
- **A2 — Đã xóa trước đó**: Tại bước 4, hệ thống trả trạng thái đã ghi nhận cho các tin đã xóa.

### Exception Flows

- **E1 — Lựa chọn không hợp lệ**: Tại bước 2 hoặc 4, vượt giới hạn số tin hoặc có tin không còn thuộc phạm vi được thao tác. Hệ thống báo phần không hợp lệ để người dùng đối chiếu và xác nhận lại.
- **E2 — Lỗi ghi nhận hoặc mất xác nhận**: Tại bước 4, kiểm tra kết quả của yêu cầu trước khi thử lại.
- **E3 — Phiên hết hiệu lực**: Hệ thống từ chối và yêu cầu đăng nhập lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Có thể xóa tin gửi hoặc nhận; chỉ tác động lịch sử của người thao tác.
  - **BR-02**: Trạng thái xóa được đồng bộ khi đăng nhập lại hoặc đổi thiết bị.
  - **BR-03**: Chỉ xóa các tin đã xác nhận; tin mới tiếp tục xuất hiện theo quyền.
  - **BR-04**: Số tin được chọn không vượt giới hạn của hệ thống; chỉ báo hoàn tất đối với các tin đã được xác nhận xóa.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Lịch sử, bộ nhớ đệm và kết quả tìm kiếm phải thống nhất, không đưa lại nội dung đã xóa riêng.
  - **NFR-02**: Gửi lại yêu cầu không mở rộng tập tin hoặc tác động sang hội thoại khác.
