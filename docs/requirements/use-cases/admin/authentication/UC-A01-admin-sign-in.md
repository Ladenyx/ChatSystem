# ĐẶC TẢ USE CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-A01
- **Use Case Name**: Đăng nhập vào Admin App
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-29
- **Last Updated by / Date**: baonguyen1776 / 2026-11-03
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
- **Post-conditions**: Phiên Admin hợp lệ được thiết lập; phiên quản trị cũ (nếu có) đã bị thu hồi; số phiên quản trị hoạt động của tài khoản không vượt quá 1; các phiên User Client không bị ảnh hưởng bởi lần đăng nhập này; A-SCR-02 được mở.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Admin mở app cho người quản trị | Hiển thị A-SCR-01 với ô Tên đăng nhập, mật khẩu và nút đăng nhập |
| 2 | Admin nhập thông tin và chọn đăng nhập | Kiểm tra dữ liệu và gửi yêu cầu xác thực đến Server |
| 3 | Chờ kết quả xác thực | Server kiểm tra thông tin đăng nhập, quyền Admin hệ thống và điều kiện được đăng nhập |
| 4 | Chờ thiết lập phiên | Server áp dụng chính sách một phiên quản trị/tài khoản (thu hồi trực tiếp phiên quản trị cũ nếu đang tồn tại) và cấp phiên quản trị mới; không thay các phiên User Client |
| 5 | Sử dụng ứng dụng | Admin App nhận phiên hợp lệ và mở A-SCR-02 |

### Alternative Flows

- **Alt 2a — Thiếu dữ liệu bắt buộc**: Tại bước 2, nếu Admin để trống `Tên đăng nhập` hoặc `Mật khẩu`, Admin App không gửi yêu cầu lên Server, đánh dấu và hiển thị thông báo tại trường còn thiếu. Admin bổ sung thông tin và thực hiện lại bước 2.
- **Alt 3a — Sai thông tin xác thực nhưng chưa vượt ngưỡng khóa (`≤ N` lần liên tiếp)**: Tại bước 3, nếu tên đăng nhập hoặc mật khẩu không chính xác và số lần nhập sai liên tiếp chưa vượt quá `N` lần quy định, Server ghi nhận tăng bộ đếm đăng nhập sai liên tiếp (nếu tài khoản tồn tại) và từ chối xác thực. Admin App xóa trống ô mật khẩu, hiển thị thông báo lỗi xác thực (kèm số lần thử còn lại nếu áp dụng) và quay về bước 2 để Admin nhập lại. *(Bộ đếm lỗi liên tiếp được đặt lại về `0` khi đăng nhập thành công).*
- **Alt 4a — Tài khoản đang có phiên quản trị hoạt động (thu hồi trực tiếp phiên quản trị cũ)**: Tại bước 4, nếu tài khoản đã có phiên quản trị đang hoạt động ở thiết bị/cửa sổ khác, hệ thống bảo đảm tối đa 1 phiên quản trị/tài khoản bằng cách thu hồi trực tiếp phiên quản trị cũ mà không cần chờ xác nhận; các phiên User Client không bị ảnh hưởng:
  - **Trên Server**: Thu hồi ngay lập tức phiên cũ (vô hiệu hóa cả yêu cầu nghiệp vụ lẫn kết nối thời gian thực đang mở; tự động kết nối lại từ phiên cũ không giành lại được phiên) và cấp phiên quản trị hợp lệ cho phiên mới.
  - **Trên phiên cũ (đang hoạt động)**: Nhận sự kiện phiên bị thu hồi (hoặc bị từ chối ở yêu cầu tiếp theo), tự động đăng xuất về `A-SCR-01` và hiển thị thông báo: *“Phiên đăng nhập đã bị thu hồi do tài khoản vừa đăng nhập ở thiết bị/cửa sổ khác.”*
  - **Trên phiên mới**: Nhận phiên quản trị hợp lệ và chuyển sang bước 5 để mở `A-SCR-02`.

### Exception Flows

- **Ex 3a — Nhập sai mật khẩu quá `N` lần liên tiếp (khóa đăng nhập tạm thời)**: Tại bước 3, nếu số lần nhập sai mật khẩu liên tiếp vượt quá ngưỡng `N` lần cho phép, Server áp dụng hạn khóa tạm theo cấu hình hệ thống, ghi nhận sự kiện bảo mật và từ chối đăng nhập. Admin App thông báo thời gian chờ; hết hạn chỉ bỏ hạn chế thử sai, không tự mở khóa quản trị đang có. → **Kết thúc Use Case.**
- **Ex 3b — Tài khoản không có quyền Admin hệ thống**: Tại bước 3, nếu thông tin xác thực đúng nhưng tài khoản không mang vai trò Admin hệ thống (ví dụ: tài khoản `USER` thông thường hoặc chỉ là Admin nhóm chat), Server từ chối cấp phiên quản trị. Admin App hiển thị thông báo không có quyền truy cập ứng dụng quản trị. → **Kết thúc Use Case.**
- **Ex 3c — Trạng thái tài khoản không hợp lệ để đăng nhập**: Tại bước 3, nếu tài khoản đang ở trạng thái không được phép đăng nhập theo chính sách Server, hệ thống từ chối xác thực, không tạo phiên và không mở `A-SCR-02`; Admin App hiển thị lý do tương ứng. → **Kết thúc Use Case.**

  | Status | Ý nghĩa | Đăng nhập được? |
  | :--- | :--- | :---: |
  | `ACTIVE` | Hoạt động bình thường | Có |
  | `LOCKED` | Bị khóa quản trị; cần được mở khóa theo nghiệp vụ quản trị | Không |

  Khóa do thử sai được kiểm tra độc lập với trạng thái tài khoản theo [DEC-07](../../../../decisions/account-lifecycle.md#điều-kiện-tài-khoản-khi-đăng-nhập). Tài khoản `ACTIVE` đang trong hạn khóa tạm vẫn bị từ chối; hết hạn khóa tạm không thay đổi `LOCKED` hoặc phục hồi tài khoản đã xóa.

- **Ex 4a — Mất kết nối hoặc Server không phản hồi**: Tại bước 2–4, nếu kết nối mạng gặp sự cố hoặc không nhận được phản hồi xác nhận từ Server, Admin App dừng trạng thái chờ, không mở `A-SCR-02`, hiển thị thông báo lỗi kết nối để Admin thử lại sau. → **Kết thúc Use Case.**

## Business Rules & Non-Functional Requirements

- **Business Rule (BR)**:
    - BR-01: Chỉ tài khoản Admin hệ thống được vào Admin App; quyền Admin nhóm không thay thế quyền này. Không đăng ký Admin công khai trên A-SCR-01
    - BR-02: Tài khoản Admin chỉ được tạo qua script SQL lên database
    - BR-03: Tối đa một phiên quản trị hoạt động/tài khoản; đăng nhập Admin App mới hợp lệ chỉ thu hồi phiên quản trị cũ, reconnect không giành lại phiên đó. Các phiên User Client được quản lý độc lập theo DEC-07.
    - BR-04: Mọi yêu cầu quản trị sau đăng nhập đều phải được Server kiểm tra phiên và quyền, không chỉ dựa vào việc đã mở màn hình.
- **Non-Functional Requirements (NFR)**:
    - NFR-01: Mật khẩu được che trên GUI; không ghi mật khẩu hoặc dữ liệu nhạy cảm vào thông báo lỗi hay tài liệu cấu hình.
    - NFR-02: Thể hiện trạng thái đang xác thực và lỗi kết nối rõ ràng; không báo đăng nhập thành công trước khi có xác nhận hợp lệ từ Server.
