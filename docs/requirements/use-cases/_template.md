# ĐẶC TẢ USE-CASE

> [!WARNING]
> Tránh việc viết những điều quá hiển nhiên / mô tả những gì dễ dàng nhận thấy từ UI.
> Tránh đi sâu vào đặc tả giao diện

## Thông tin chung (Metadata)

- **Use Case ID**: [Mã định danh. Ví dụ: UC-ADM-AUTH-01 (format: UC-`<phân hệ>`-`<module>`-`<use-case-id>`)]
- **Use Case Name**: [Tên Use Case, động từ + danh từ, ví dụ: Đăng nhập vào hệ thống]
- **Author / Create by**: [Tên người viết]
- **Date Create**: [Ngày tạo]
- **Last Updated by / Date**: [Người cập nhật cuối cùng & ngày]
- **Primary Actor**: [Tác nhân chính khởi tạo, ví dụ: Khách hàng, người dùng, None]
- **Secondary Actor(s)**: [Tác nhận phụ/hỗ trợ, ví dụ: Hệ thống thanh toán, SMS Gateway]

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: [Mô tả ngắn gọn mục đích của Use Case này giải quyết việc gì cho người dùng]
- **Goal**: [Kết quả mà actor mong muốn đạt được sau khi hoàn tất Use Case]
- **Trigger**: [Hành động hoặc điều kiện gì bắt đầu kích hoạt Use Case này? Ví dụ: Người dùng nhấn nút "Đăng nhập"]

## Điều kiện (Conditions)

- **Pre-conditions**: [Những điều kiện bắt buộc phải đúng/tồn tại trước khi thực hiện Use Case. Ví dụ: "User goes to homepage at www.hailua.com.vn"]
- **Post-conditions**: [Trạng thái của hệ thống hoặc dữ liệu sau khi Use Case kết thúc thành công. Ví dụ: "The user successfully adds new item to the cart or increases quantity of the existing item in the cart"]

## Luồng sự kiện (Flow of Events)

### Basic Flow

[Ví dụ:
1. At the homepage, **User** enters keywords on the ‘Search’ field
2. Users clicks on ‘Search’ button to start searching a product
3. System displays the products found
4. User clicks ‘Add to cart’ button on the expected product
5. System adds a new product to shopping cart
5. System displays the quantity of all products in the cart to the user
]

### Alternative Flows

- [Ví dụ:
**Alternative flow 1: User cannot find products searched**
1. From #1 of the basic flow, user enters another term
2. Continue step #2 in the basic flow

**Alternative flow 2: Shopping cart has already had the product**
1. From #5 of the basic flow, system increases the quantity of the product
2. Continue step #6

**Alternative flow 3: …**
]

### Exception Flows

> Tương tự với Alternative Flows

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**: [Các quy định nghiệp vụ liên quan, ví dụ: BR-05: Mật khẩu phải có tối thiểu 8 ký tự gồm chữ cái và số]
- **Non-Functional Requirements (NFR)**: [Yêu cầu phi chức năng, ví dụ: Thời gian phản hồi trang đăng nhập không quá 2 giây]
