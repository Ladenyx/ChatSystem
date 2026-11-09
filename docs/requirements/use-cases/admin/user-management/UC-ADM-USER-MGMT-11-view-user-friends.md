# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-USER-MGMT-11
- **Use Case Name**: Xem danh sách bạn bè của một người dùng
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-29
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin xem danh sách các tài khoản hiện đang có quan hệ bạn bè trực tiếp với người dùng được chọn.
- **Goal**: Nắm bắt các kết nối bạn bè trực tiếp hiện hành của một người dùng để phục vụ công tác kiểm tra và quản trị.
- **Trigger**: Admin chọn xem danh sách bạn bè từ thông tin chi tiết của một người dùng ([UC-ADM-USER-MGMT-02](UC-ADM-USER-MGMT-02-view-user-profile.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin có phiên quản trị hợp lệ và quyền Admin hệ thống; đã xác định mã định danh của một tài khoản người dùng còn trong phạm vi lưu giữ hồ sơ.
- **Post-conditions**: Danh sách bạn bè trực tiếp hiện tại của đúng tài khoản được cung cấp cho Admin; không tạo mới, hủy bỏ hay thay đổi bất kỳ quan hệ bạn bè hoặc quan hệ chặn nào.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** yêu cầu xem danh sách bạn bè của tài khoản người dùng đang chọn.
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống và sự tồn tại của hồ sơ tài khoản theo mã định danh.
3. **Hệ thống** truy vấn tập các quan hệ bạn bè trực tiếp đang còn hiệu lực của tài khoản đó.
4. **Hệ thống** cung cấp danh sách các người bạn trực tiếp kèm thông tin định danh hiển thị của từng tài khoản bạn bè.

### Alternative Flows

**Alternative flow 1: Người dùng hiện không có bạn bè trực tiếp**
1. Từ bước #3 của Basic Flow, truy vấn hoàn tất thành công nhưng tài khoản chưa kết bạn hoặc không còn quan hệ bạn bè trực tiếp nào.
2. **Hệ thống** thông báo danh sách bạn bè hiện đang trống (phân biệt rõ với lỗi truy vấn).
3. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #2 của Basic Flow, phiên quản trị không còn hợp lệ hoặc thiếu quyền Admin hệ thống.
2. **Hệ thống** từ chối truy vấn và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 2: Tài khoản gốc đã bị xóa vĩnh viễn (purge)**
1. Từ bước #2 của Basic Flow, hồ sơ của người dùng đang kiểm tra đã bị purge.
2. **Hệ thống** thông báo tài khoản không còn tồn tại và không dùng `username` để truy vấn nhầm quan hệ sang tài khoản mới trùng tên.
3. Kết thúc Use Case.

**Exception flow 3: Lỗi kết nối hoặc lỗi truy vấn dữ liệu**
1. Từ bước #2 hoặc #3 của Basic Flow, quá trình truy xuất dữ liệu quan hệ bạn bè gặp sự cố.
2. **Hệ thống** thông báo lỗi truy vấn, cho phép thử lại và không suy diễn thành người dùng không có bạn bè.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Danh sách chỉ bao gồm **quan hệ bạn bè trực tiếp đang có hiệu lực**; không bao gồm lời mời kết bạn đang chờ xử lý, danh sách chặn hay mạng lưới "bạn của bạn" (phần thống kê mở rộng thuộc chức năng phân tích mạng lưới bạn bè riêng).
  - **BR-02**: Theo [DEC-03](../../../../decisions/blocking-and-friendship.md), khi một bên chặn bên kia thì quan hệ bạn bè đã bị hủy ngay tại thời điểm chặn (và bỏ chặn không tự khôi phục tình bạn), nên các cặp đã chặn nhau không còn nằm trong danh sách bạn bè trực tiếp.
  - **BR-03**: Đây là nghiệp vụ chỉ đọc dành cho Admin; hệ thống không cung cấp thao tác kết bạn, hủy kết bạn, chặn hay nhắn tin thay mặt người dùng.
  - **BR-04**: Quan hệ bạn bè gắn theo `Account ID`; tài khoản mới dùng lại `username`/`email` sau khi tài khoản cũ đã purge không kế thừa bất kỳ quan hệ bạn bè nào của tài khoản cũ ([DEC-07](../../../../decisions/account-lifecycle.md)).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Khi **Admin** chuyển đổi nhanh giữa các tài khoản người dùng, kết quả danh sách bạn bè phản hồi muộn của tài khoản trước không được hiển thị nhầm sang tài khoản đang chọn.
  - **NFR-02**: Phản hồi danh sách bạn bè chỉ bao gồm thông tin nhận diện hồ sơ được phép xem, không bao gồm mật khẩu, mã phiên hay khóa E2EE.
