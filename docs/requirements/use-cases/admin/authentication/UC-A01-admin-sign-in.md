# ĐẶC TẢ USE CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-A01
- **Use Case Name**: Đăng nhập vào Admin App
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-29
- **Last Updated by / Date**: ladenyx / 2026-10-30
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: Không có tác nhân ngoài hệ thống.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Desciption)

- **Brief Description**: Admin hệ thống đăng nhập bằng tài khoản đã được khởi tạo từ Server để truy cập ứng dụng quản lí.
- **Goal**: Tạo phiên hợp lệ có quyền Admin hệ thống và mở Dashboard tổng quan.
- **Trigger**: Admin mở Admin App và chọn đăng nhập tại A-SCR-01.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**: Tài khoản Admin hệ thống đã được tạo.
- **Post-conditions**: Phiên Admin hợp lệ được thiết lập; số phiên hoạt động của tài khoản không vượt quá 1; A-SCR-02 được mở.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Admin mở app cho người quản trị | Hiển thị A-SCR-01 với ô Tên đăng nhập, mật khẩu và nút đăng nhập |
| 2 | Admin nhập thông tin và chọn đăng nhập | Kiểm tra dữ liệu và gửi yêu cầu xác thực đến Server |
| 3 | Chờ kết quả xác thực | Server kiểm tra thông tin đăng nhập, quyền Admin hệ thống và điều kiện được đăng nhập |
| 4 | Chờ thiết lập phiên | Server áp dụng chính sách một phiên/tài khoản và cấp phiên có quyền quản trị khi hợp lệ |
| 5 | Sử dụng ứng dụng | Admin App nhận phiên hợp lệ và mở A-SCR-02 |

### Alternative Flows

- **Alt 2a — Thiếu dữ liệu bắt buộc**: Tại bước 2, nếu Admin để trống `Tên đăng nhập` hoặc `Mật khẩu`, Admin App không gửi yêu cầu lên Server, đánh dấu và hiển thị thông báo tại trường còn thiếu. Admin bổ sung thông tin và thực hiện lại bước 2.
- **Alt 3a — Sai thông tin xác thực nhưng chưa vượt ngưỡng khóa (`≤ N` lần liên tiếp)**: Tại bước 3, nếu tên đăng nhập hoặc mật khẩu không chính xác và số lần nhập sai liên tiếp chưa vượt quá `N` lần quy định, Server ghi nhận tăng bộ đếm đăng nhập sai liên tiếp (nếu tài khoản tồn tại) và từ chối xác thực. Admin App xóa trống ô mật khẩu, hiển thị thông báo lỗi xác thực (kèm số lần thử còn lại nếu áp dụng) và quay về bước 2 để Admin nhập lại. *(Bộ đếm lỗi liên tiếp được đặt lại về `0` khi đăng nhập thành công).*
- **Alt 4a — Tài khoản đang có phiên hoạt động (yêu cầu phiên cũ phê duyệt)**: Tại bước 4, nếu tài khoản đã có phiên quản trị đang hoạt động ở thiết bị/cửa sổ khác, hệ thống bảo đảm tối đa 1 phiên/tài khoản bằng cơ chế xin xác nhận từ **phiên cũ**:
  - **Trên phiên mới (`A-SCR-01`)**: Hiển thị trạng thái chờ phiên đang hoạt động phê duyệt.
  - **Trên phiên cũ (đang hoạt động)**: Hiển thị hộp thoại xác nhận: *“Phát hiện yêu cầu đăng nhập mới vào tài khoản này từ nơi khác. Bạn có đồng ý đăng xuất phiên hiện tại và cho phép phiên mới tiếp tục không?”*
  - **Nhánh 4a.1 — Phiên cũ chọn Chấp nhận (`Accept` / `Yes`) hoặc hết thời gian chờ phản hồi (`Heartbeat Timeout`)**: Server thu hồi ngay lập tức phiên cũ (đăng xuất phiên cũ về `A-SCR-01`), đồng thời cấp phiên quản trị hợp lệ cho phiên mới và chuyển sang bước 5.
  - **Nhánh 4a.2 — Phiên cũ chọn Từ chối (`Reject` / `No`)**: Server giữ nguyên phiên cũ và từ chối yêu cầu đăng nhập mới; Admin App ở phiên mới hiển thị thông báo đăng nhập bị từ chối và quay về bước 2.

### Exception Flows

- **Ex 3a — Nhập sai mật khẩu quá `N` lần liên tiếp (khóa tài khoản tạm thời)**: Tại bước 3, nếu số lần nhập sai mật khẩu liên tiếp vượt quá ngưỡng `N` lần cho phép, Server tự động chuyển trạng thái tài khoản sang `LOCKED` (khóa tạm thời trong khoảng thời gian theo cấu hình hệ thống), ghi nhận sự kiện bảo mật và từ chối đăng nhập. Admin App hiển thị thông báo tài khoản đã bị khóa tạm thời kèm thời gian chờ mở khóa tự động. → **Kết thúc Use Case.**
- **Ex 3b — Tài khoản không có quyền Admin hệ thống**: Tại bước 3, nếu thông tin xác thực đúng nhưng tài khoản không mang vai trò Admin hệ thống (ví dụ: tài khoản `USER` thông thường hoặc chỉ là Admin nhóm chat), Server từ chối cấp phiên quản trị. Admin App hiển thị thông báo không có quyền truy cập ứng dụng quản trị. → **Kết thúc Use Case.**
- **Ex 3c — Trạng thái tài khoản không hợp lệ để đăng nhập**: Tại bước 3, nếu tài khoản đang ở trạng thái không được phép đăng nhập theo chính sách Server, hệ thống từ chối xác thực, không tạo phiên và không mở `A-SCR-02`; Admin App hiển thị lý do tương ứng. → **Kết thúc Use Case.**

  | Status | Ý nghĩa | Đăng nhập được? |
  | :--- | :--- | :---: |
  | `ACTIVE` | Hoạt động bình thường | Có |
  | `LOCKED` | Đang bị khóa (khóa quản trị hoặc khóa tạm thời do nhập sai mật khẩu quá `N` lần liên tiếp, tự mở sau một khoảng thời gian) | Không |

- **Ex 4a — Mất kết nối hoặc Server không phản hồi**: Tại bước 2–4, nếu kết nối mạng gặp sự cố hoặc không nhận được phản hồi xác nhận từ Server, Admin App dừng trạng thái chờ, không mở `A-SCR-02`, hiển thị thông báo lỗi kết nối để Admin thử lại sau. → **Kết thúc Use Case.**

## Business Rules & Non-Functional Requirements

- **Business Rule (BR)**:
    - BR-01: Chỉ tài khoản Admin hệ thống được vào Admin App; quyền Admin nhóm không thay thế quyền này. Không đăng ký Admin công khai trên A-SCR-01
    - BR-02: Tài khoản Admin chỉ được tạo qua script SQL lên database
    - BR-03: Tối đa một phiên hoạt động/tài khoản.
    - BR-04: Mọi yêu cầu quản trị sau đăng nhập đều phải được Server kiểm tra phiên và quyền, không chỉ dựa vào việc đã mở màn hình.
- **Non-Functional Requirements (NFR)**:
    - NFR-01: Mật không được che trên GUI; không ghi mật khẩu hoặc dữ liệu nhạy cảm vào thông báo lỗi hay tài liệu cấu hình.
    - NFR-02: Thể hiện trạng thái đang xác thực và lỗi kết nối rõ ràng; không báo đăng nhập thành công trước khi có xác nhận hợp lệ từ Server.
