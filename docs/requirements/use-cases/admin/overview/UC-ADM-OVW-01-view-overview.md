# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-OVW-01
- **Use Case Name**: Xem tổng quan hệ thống
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-31
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Hệ thống tổng hợp và cung cấp cho Admin bức tranh chung về tình trạng vận hành gồm các chỉ số tổng quan (KPI), xu hướng trong năm hiện tại và các hoạt động phát sinh gần đây.
- **Goal**: Nắm bắt nhanh trạng thái vận hành toàn hệ thống và chuyển tiếp trực tiếp sang chức năng quản trị chi tiết khi cần kiểm tra.
- **Trigger**: Admin hoàn tất đăng nhập vào Admin App hoặc chủ động mở mục Tổng quan.

## Điều kiện (Conditions)

- **Pre-conditions**: Admin đã có phiên quản trị hợp lệ và quyền Admin hệ thống ([UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md)).
- **Post-conditions**: Các chỉ số tổng quan và dữ liệu hoạt động tải thành công được cung cấp đúng phạm vi thời gian; không làm thay đổi dữ liệu nghiệp vụ của hệ thống.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** yêu cầu xem tổng quan hệ thống.
2. **Hệ thống** kiểm tra hiệu lực phiên và quyền Admin hệ thống, sau đó truy xuất bốn chỉ số KPI gồm: tổng số người dùng, số người dùng đang trực tuyến, tổng số nhóm chat và số báo cáo spam chờ xử lý.
3. **Hệ thống** truy xuất dữ liệu xu hướng theo tháng của năm hiện tại cùng danh sách rút gọn các báo cáo spam mới nhất và các lượt đăng nhập gần đây.
4. **Hệ thống** cung cấp kết quả tổng quan kèm phạm vi thời gian tương ứng cho **Admin**.
5. **Admin** chọn một chỉ số tổng quan để đi tới chức năng quản trị liên quan.
6. **Hệ thống** chuyển **Admin** sang chức năng quản trị tương ứng (kèm điều kiện lọc liên quan nếu chức năng đích có hỗ trợ).

### Alternative Flows

**Alternative flow 1: Chưa có dữ liệu hoạt động phát sinh**
1. Từ bước #2 hoặc #3 của Basic Flow, truy vấn hoàn tất thành công nhưng một danh sách tóm tắt chưa có bản ghi hoặc chỉ số đạt giá trị `0`.
2. **Hệ thống** phản ánh đúng giá trị `0` hoặc trạng thái chưa có hoạt động cho phần dữ liệu đó.
3. Tiếp tục bước #4 trong Basic Flow.

**Alternative flow 2: Admin chỉ theo dõi tổng quan, không chuyển sang chức năng khác**
1. Từ bước #4 của Basic Flow, **Admin** kết thúc việc xem tổng quan mà không chọn chỉ số để điều hướng.
2. Kết thúc Use Case.

**Alternative flow 3: Chức năng đích không hỗ trợ bộ lọc tương ứng với chỉ số KPI**
1. Từ bước #5 của Basic Flow, **Admin** chọn một chỉ số mà chức năng quản trị đích không có bộ lọc chuyên biệt tương ứng (ví dụ chỉ số đang trực tuyến).
2. **Hệ thống** mở chức năng quản trị liên quan ở chế độ mặc định mà không gắn nhãn đã lọc dữ liệu.
3. Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Lỗi truy xuất một phần nguồn dữ liệu tổng quan**
1. Từ bước #2 hoặc #3 của Basic Flow, việc truy vấn một hoặc một số khối dữ liệu thất bại.
2. **Hệ thống** báo lỗi riêng cho khối dữ liệu không tải được, giữ nguyên các khối khác đã truy xuất thành công và không thay thế lỗi truy vấn bằng giá trị `0`.
3. Kết thúc Use Case.

**Exception flow 2: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #2 hoặc #6 của Basic Flow, **Hệ thống** xác định phiên không còn hợp lệ hoặc tài khoản không có quyền Admin hệ thống.
2. **Hệ thống** từ chối truy cập dữ liệu và yêu cầu **Admin** xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 3: Không thể chuyển sang chức năng quản trị đích**
1. Từ bước #6 của Basic Flow, chức năng quản trị đích gặp lỗi khi khởi tạo dữ liệu.
2. **Hệ thống** thông báo lỗi tải dữ liệu và không trả về dữ liệu của chức năng khác dưới tên mục đã chọn.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Tổng quan hệ thống là nghiệp vụ chỉ đọc và điều hướng nhanh; việc xem tổng quan hoặc chọn chỉ số KPI không tự thực hiện khóa tài khoản hay thay đổi trạng thái báo cáo spam.
  - **BR-02**: Năm hiện tại và mọi mốc thời gian thống kê/hiển thị đều được xác định theo múi giờ `Asia/Ho_Chi_Minh` ([Thống kê và thời gian](../../../../decisions/statistics-and-time.md)).
  - **BR-03**: Việc tra cứu đầy đủ, lọc chuyên sâu hoặc thực thi tác vụ quản trị tuân theo đặc tả Use Case riêng của từng chức năng đích.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Hệ thống phải phân biệt minh bạch giữa dữ liệu bằng `0`/danh sách rỗng thực tế và trạng thái truy vấn bị lỗi.
  - **NFR-02**: Dữ liệu tổng quan không bao giờ bao gồm nội dung tin nhắn riêng tư ngoài phạm vi báo cáo spam và không giải mã luồng hội thoại nhóm E2EE.
