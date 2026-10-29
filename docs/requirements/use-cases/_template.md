> File format: 

# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: [Mã định danh, ví dụ: UC-01]
- **Use Case Name**: [Tên Use Case, động từ + danh từ, ví dụ: Đăng nhập vào hệ thống]
- **Author / Create by**: [Tên người viết]
- **Date Create**: [Ngày tạo]
- **Last Updated by / Date**: [Người cập nhật cuối cùng & ngày]
- **Primary Actor**: [Tác nhân chính khởi tạo, ví dụ: Khách hàng, người dùng]
- **Secondary Actor(s)**: [Tác nhận phụ/hỗ trợ, ví dụ: Hệ thống thanh toán, SMS Gateway]
- **Priority**: [Must Have / Should Have / Could Have]
- **Status**: [Draft / Review / Approved]

## Mô tả tổng quan (Summary / Desciption)

- **Brief Description**: [Mô tả ngắn gọn mục đích của Use Case này giải quyết việc gì cho người dùng]
- **Goal**: [Kết quả mà actor mong muốn đạt được sau khi hoàn tất Use Case]
- **Trigger**: [Hành động hoặc điều kiện gì bắt đầu kích hoạt Use Case này? Ví dụ: Người dùng nhấn nút "Đăng nhập"]

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**: [Những điều kiện bắt buộc phải đúng/tồn tại trước khi thực hiện Use Case. Ví dụ: Người dùng đã có tài khoản và trang web đang hoạt động]
- **Post-conditions**: [Trạng thái của hệ thống hoặc dữ liệu sau khi Use Case kết thúc thành công. Ví dụ: Phiên làm việc (session) được tạo và chuyển hướng về trang chủ]

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |

### Alternative Flows

- [Ví dụ: Alt 2a: Tại bước 2, nếu người dùng chọn "Quên mật khẩu", hệ thống chuyển sang quy trình khôi phục mật khẩu]

### Exception Flows

- [Ví dụ: Ex 2a: Tại bước 2, nếu người dùng nhập sai mật khẩu quá 3 lần, hệ thống hiển thị thông báo khóa tài khoản tạm thời. => Kết thúc Use Case]

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**: [Các quy định nghiệp vụ liên quan, ví dụ: BR-05: Mật khẩu phải có tối thiểu 8 ký tự gồm chữ cái và số]
- **Non-Functional Requirements (NFR)**: [Yêu cầu phi chức năng, ví dụ: Thời gian phản hồi trang đăng nhập không quá 2 giây]
