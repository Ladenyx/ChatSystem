# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-FRD-03
- **Use Case Name**: Gửi lời mời kết bạn
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng gửi lời mời
- **Secondary Actor(s)**: Người nhận lời mời.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng đề nghị kết bạn với một tài khoản khác.
- **Goal**: Lời mời được ghi nhận để người nhận quyết định.
- **Trigger**: Người dùng yêu cầu gửi lời mời kết bạn.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ và đã xác định tài khoản muốn kết bạn.
- **Post-conditions**: Lời mời ở trạng thái chờ xử lý; quan hệ bạn bè chưa được tạo.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** gửi yêu cầu kết bạn tới tài khoản đã chọn.
2. **Hệ thống** kiểm tra tài khoản và quan hệ hiện tại theo BR-01.
3. **Hệ thống** ghi nhận lời mời chờ xử lý cho người nhận và thông báo kết quả cho người gửi.

### Alternative Flows

- **A1 — Đã gửi lời mời**: Tại bước 2, đã có lời mời cùng chiều đang chờ. Hệ thống trả trạng thái hiện tại; kết thúc.
- **A2 — Đã nhận lời mời từ đối phương**: Tại bước 2, hệ thống giữ lời mời nhận vào để người dùng [chấp nhận hoặc từ chối](UC-USR-FRD-04-process-friend-request.md); kết thúc yêu cầu gửi.
- **A3 — Đã là bạn bè**: Tại bước 2, hệ thống thông báo quan hệ hiện tại; kết thúc.

### Exception Flows

- **E1 — Không đủ điều kiện gửi**: Tại bước 2, người dùng tự gửi cho mình, một bên đang chặn hoặc tài khoản đích không còn khả dụng. Hệ thống từ chối lời mời.
- **E2 — Không nhận được xác nhận**: Tại bước 3, hệ thống kiểm tra trạng thái lời mời trước khi thử lại.
- **E3 — Phiên hết hiệu lực**: Hệ thống từ chối yêu cầu và yêu cầu đăng nhập lại.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Hai tài khoản phải khác nhau, được phép tương tác, chưa là bạn, không có quan hệ chặn ở bất kỳ chiều nào và không có lời mời đang chờ giữa hai bên.
  - **BR-02**: Có lời mời nhận vào thì chỉ được chấp nhận hoặc từ chối; không tạo lời mời ngược chiều hay tự chấp nhận.
  - **BR-03**: Người nhận không cần online để nhận lời mời.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Server kiểm tra điều kiện tại thời điểm ghi nhận; yêu cầu lặp hoặc đồng thời không tạo lời mời trùng.
  - **NFR-02**: Trạng thái lời mời gửi và nhận phải nhất quán giữa hai bên.
