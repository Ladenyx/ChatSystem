# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-USER-MGMT-01
- **Use Case Name**: Tra cứu danh sách người dùng
- **Author / Create by**: ladenyx
- **Date Create**: 2026-11-03
- **Last Updated by / Date**: ladenyx / 2026-11-08
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin xem và tra cứu danh sách tài khoản người dùng với khả năng lọc theo tên (`họ tên`), tên đăng nhập (`username`), trạng thái tài khoản và sắp xếp kết quả theo tên (`họ tên`) hoặc ngày tạo.
- **Goal**: Xác định đúng tài khoản người dùng cần xem xét hoặc thực hiện nghiệp vụ quản trị.
- **Trigger**: Admin mở chức năng Quản lý người dùng hoặc được điều hướng sang từ trang Tổng quan.

## Điều kiện (Conditions)

- **Pre-conditions**: Admin đã có phiên quản trị hợp lệ và quyền Admin hệ thống ([UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md)).
- **Post-conditions**: Danh sách tài khoản người dùng khớp với điều kiện lọc và thứ tự sắp xếp được cung cấp; thông tin hồ sơ và trạng thái của mọi tài khoản không bị thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** mở chức năng Quản lý người dùng.
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống và trả về danh sách các tài khoản người dùng cùng trạng thái hiện tại theo thứ tự mặc định.
3. **Admin** nhập hoặc chọn một hay nhiều tiêu chí lọc gồm: **tên (`họ tên`)**, **tên đăng nhập (`username`)** và **trạng thái tài khoản**.
4. **Hệ thống** truy vấn và trả về danh sách các tài khoản thỏa mãn đồng thời các tiêu chí lọc đang áp dụng.
5. **Admin** chọn tiêu chí sắp xếp danh sách theo **tên (`họ tên`)** hoặc **ngày tạo** cùng chiều sắp xếp (tăng dần / giảm dần).
6. **Hệ thống** sắp xếp và cung cấp danh sách tài khoản theo tiêu chí và chiều sắp xếp đã chọn.
7. **Admin** chọn một tài khoản trong danh sách kết quả.
8. **Hệ thống** chuyển mã định danh tài khoản được chọn sang [UC-ADM-USER-MGMT-02](UC-ADM-USER-MGMT-02-view-user-profile.md) để tải hồ sơ chi tiết.

### Alternative Flows

**Alternative flow 1: Không sử dụng hoặc gỡ bỏ bớt tiêu chí lọc**
1. Từ bước #3 của Basic Flow, **Admin** để trống hoặc xóa bớt một/nhiều tiêu chí lọc (`tên`, `tên đăng nhập` hoặc đặt `trạng thái` về tất cả).
2. **Hệ thống** gỡ ràng buộc của tiêu chí đó, giữ nguyên các điều kiện lọc/sắp xếp còn lại và tiếp tục bước #4 trong Basic Flow.

**Alternative flow 2: Giữ nguyên thứ tự sắp xếp hiện tại hoặc chỉ tra cứu danh sách**
1. Từ bước #5 của Basic Flow, **Admin** không thay đổi tiêu chí sắp xếp.
2. Tiếp tục bước #7 trong Basic Flow để mở chi tiết người dùng, hoặc kết thúc Use Case nếu chỉ tra cứu danh sách.

**Alternative flow 3: Không có tài khoản thỏa mãn điều kiện lọc**
1. Từ bước #2 hoặc #4 của Basic Flow, truy vấn hoàn tất nhưng không có tài khoản nào khớp điều kiện.
2. **Hệ thống** thông báo không tìm thấy tài khoản phù hợp và giữ nguyên tiêu chí lọc hiện tại để **Admin** điều chỉnh.
3. Quay lại bước #3 trong Basic Flow hoặc kết thúc Use Case.

**Alternative flow 4: Khởi tạo thêm tài khoản người dùng mới**
1. Từ bước #2, #4 hoặc #6 của Basic Flow, **Admin** chọn thực hiện tạo người dùng mới thay vì chọn tài khoản có sẵn.
2. **Hệ thống** chuyển sang quy trình tạo tài khoản tại [UC-ADM-USER-MGMT-03](UC-ADM-USER-MGMT-03-create-user.md).

### Exception Flows

**Exception flow 1: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #2, #4 hoặc #6 của Basic Flow, **Hệ thống** phát hiện phiên không còn hợp lệ hoặc không có quyền Admin hệ thống.
2. **Hệ thống** từ chối yêu cầu tra cứu và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 2: Lỗi kết nối hoặc truy vấn dữ liệu thất bại**
1. Từ bước #2, #4 hoặc #6 của Basic Flow, quá trình truy xuất danh sách gặp sự cố.
2. **Hệ thống** thông báo lỗi truy vấn, giữ các tiêu chí lọc/sắp xếp hiện có để thử lại và phân biệt rõ lỗi tải với trường hợp không có kết quả.
3. Kết thúc Use Case.

**Exception flow 3: Tài khoản được chọn vừa bị xóa vĩnh viễn đồng thời**
1. Từ bước #8 của Basic Flow, tài khoản vừa chọn đã bị purge ngay trước thời điểm tải chi tiết.
2. **Hệ thống** thông báo tài khoản không còn tồn tại theo định danh gốc, làm mới danh sách và không mở nhầm sang tài khoản khác dùng lại cùng `username`.
3. Quay lại bước #6 trong Basic Flow.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Hệ thống hỗ trợ đủ **03 tiêu chí lọc** (áp dụng đơn lẻ hoặc kết hợp đồng thời):
    - **Lọc theo tên (`họ tên`)**: đối sánh chuỗi không phân biệt hoa thường sau khi cắt khoảng trắng đầu/cuối.
    - **Lọc theo tên đăng nhập (`username`)**: đối sánh chuỗi không phân biệt hoa thường sau khi cắt khoảng trắng đầu/cuối.
    - **Lọc theo trạng thái tài khoản**: xem tất cả hoặc lọc riêng theo từng trạng thái vòng đời tại [DEC-07](../../../../decisions/account-lifecycle.md) gồm đang hoạt động (`ACTIVE`), bị khóa (`LOCKED`) và xóa mềm trong hạn 30 ngày (`DELETED`). Hồ sơ đã xóa vĩnh viễn (purge) không còn trong danh sách tra cứu.
  - **BR-02**: Hệ thống hỗ trợ đủ **02 tiêu chí sắp xếp**: theo **tên (`họ tên`)** hoặc theo **ngày tạo tài khoản**, cho phép chọn chiều tăng dần hoặc giảm dần (kết hợp `Account ID` làm khóa phụ cố định khi các bản ghi trùng giá trị sắp xếp để đảm bảo thứ tự ổn định).
  - **BR-03**: Mỗi bản ghi kết quả phải gắn chặt với mã định danh tài khoản bất biến (`Account ID`) để các nghiệp vụ tiếp theo tác động đúng đối tượng, không dựa vào vị trí thứ tự hiển thị hay `username`.
  - **BR-04**: Các mốc ngày tạo tài khoản được quy đổi và hiển thị theo múi giờ `Asia/Ho_Chi_Minh`.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Dữ liệu trả về cho danh sách tra cứu tuyệt đối không chứa băm mật khẩu, mật khẩu tạm hay khóa mật mã E2EE.
  - **NFR-02**: Khi **Admin** thay đổi điều kiện lọc hoặc sắp xếp liên tiếp, phản hồi đến muộn của truy vấn cũ không được ghi đè kết quả của truy vấn mới nhất.
