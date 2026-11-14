# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-ANL-05
- **Use Case Name**: Xem quy mô mạng lưới bạn bè của người dùng
- **Author / Create by**: ladenyx
- **Date Create**: 2026-11-14
- **Last Updated by / Date**: ladenyx / 2026-11-14
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin hệ thống xem bảng thống kê so sánh số lượng bạn bè trực tiếp và tổng số bạn mở rộng (tính cả "bạn của bạn") của từng người dùng trên màn hình Phân tích Mạng lưới Bạn bè (`A-SCR-09` — `A-14`), kết hợp lọc theo họ tên, lọc so sánh (`=`, `<`, `>`) theo số bạn trực tiếp và sắp xếp theo họ tên hoặc ngày tạo tài khoản.
- **Goal**: Nắm bắt quy mô kết nối trực tiếp và độ phủ mạng lưới bạn bè bậc hai của từng người dùng trong hệ thống.
- **Trigger**: Admin mở chức năng Phân tích Mạng lưới Bạn bè (`A-SCR-09`) hoặc thay đổi tiêu chí lọc/sắp xếp trên bảng.

## Điều kiện (Conditions)

- **Pre-conditions**: Admin đã có phiên quản trị hợp lệ và quyền Admin hệ thống ([UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md)).
- **Post-conditions**: Bảng so sánh `số bạn trực tiếp` và `tổng số bạn mở rộng` của các tài khoản thỏa điều kiện lọc và thứ tự sắp xếp được cung cấp chính xác; không làm thay đổi bất kỳ quan hệ bạn bè hay quan hệ chặn nào.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** mở chức năng Phân tích Mạng lưới Bạn bè.
2. **Hệ thống** kiểm tra hiệu lực phiên, quyền Admin hệ thống và tính toán cho từng tài khoản người dùng hai chỉ số: `số bạn trực tiếp` và `tổng số bạn mở rộng` theo thứ tự mặc định.
3. **Admin** nhập từ khóa `họ tên` cần lọc, và/hoặc thiết lập điều kiện lọc theo `số bạn trực tiếp` (chọn toán tử `=`, `<` hoặc `>` kèm giá trị số nguyên), và/hoặc chọn tiêu chí sắp xếp theo `họ tên` hoặc `ngày tạo tài khoản` (tăng/giảm).
4. **Hệ thống** kiểm tra tính hợp lệ của điều kiện lọc, truy vấn các tài khoản thỏa mãn đồng thời bộ lọc `họ tên` và bộ lọc `số bạn trực tiếp`, sắp xếp theo tiêu chí đã chọn và cung cấp bảng kết quả so sánh hai chỉ số cho **Admin**.
5. **Admin** rà soát quy mô mạng lưới bạn bè trực tiếp và mở rộng của các người dùng.

### Alternative Flows

**Alternative flow 1: Không sử dụng hoặc gỡ bỏ bộ lọc họ tên / bộ lọc số bạn trực tiếp**
1. Từ bước #3 của Basic Flow, **Admin** xóa trống từ khóa `họ tên` và/hoặc gỡ điều kiện so sánh `số bạn trực tiếp`.
2. **Hệ thống** bỏ ràng buộc lọc tương ứng, giữ tiêu chí còn lại.
3. Tiếp tục bước #4 trong Basic Flow.

**Alternative flow 2: Không có người dùng nào thỏa mãn điều kiện lọc**
1. Từ bước #2 hoặc #4 của Basic Flow, truy vấn hoàn tất nhưng không có tài khoản nào khớp từ khóa `họ tên` và điều kiện so sánh `số bạn trực tiếp`.
2. **Hệ thống** thông báo không tìm thấy người dùng phù hợp và giữ nguyên bộ lọc để **Admin** điều chỉnh.
3. Quay lại bước #3 trong Basic Flow hoặc kết thúc Use Case.

**Alternative flow 3: Người dùng chưa có bạn trực tiếp hoặc không có thêm bạn của bạn**
1. Từ bước #2 hoặc #4 của Basic Flow, tài khoản có `số bạn trực tiếp = 0` (kéo theo `tổng số bạn mở rộng = 0`) hoặc mọi người bạn của bạn đều đã là bạn trực tiếp (`tổng số bạn mở rộng = số bạn trực tiếp`).
2. **Hệ thống** hiển thị đúng giá trị số học tương ứng mà không coi là lỗi dữ liệu.
3. Tiếp tục bước #5 trong Basic Flow.

### Exception Flows

**Exception flow 1: Giá trị lọc số bạn trực tiếp không hợp lệ**
1. Từ bước #4 của Basic Flow, giá trị nhập vào bộ lọc so sánh không phải là số nguyên không âm hợp lệ.
2. **Hệ thống** từ chối truy vấn, thông báo lỗi giá trị lọc và yêu cầu **Admin** nhập lại.
3. Quay lại bước #3 trong Basic Flow.

**Exception flow 2: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #2 hoặc #4 của Basic Flow, phiên quản trị không còn hợp lệ hoặc không có quyền Admin hệ thống.
2. **Hệ thống** từ chối truy vấn và buộc xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

**Exception flow 3: Lỗi kết nối hoặc lỗi truy vấn dữ liệu**
1. Từ bước #2 hoặc #4 của Basic Flow, quá trình tính toán mạng lưới bạn bè gặp sự cố.
2. **Hệ thống** thông báo lỗi truy xuất dữ liệu, cho phép thử lại và không hiển thị lỗi thành bảng rỗng hay số bạn bằng `0`.
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: **Phạm vi tài khoản gốc trong bảng**: Bảng liệt kê các tài khoản người dùng chưa bị xóa (`ACTIVE` và `LOCKED`, bao gồm cả tài khoản có `0` bạn trực tiếp); không liệt kê tài khoản đang xóa mềm (`DELETED`), đã xóa vĩnh viễn (`purge`) hoặc yêu cầu đăng ký tạm chưa hoàn tất.
  - **BR-02**: **Công thức tính `Số bạn trực tiếp` và `Tổng số bạn mở rộng`** theo [Thống kê và thời gian](../../../../decisions/statistics-and-time.md) và [DEC-03](../../../../decisions/blocking-and-friendship.md):
    - *Số bạn trực tiếp* của người dùng `U`: số lượng tài khoản đang có quan hệ bạn bè trực tiếp hợp lệ với `U`, loại trừ các tài khoản đã xóa (`DELETED`/`purge`). Tài khoản đang bị khóa (`LOCKED`) vẫn được tính nếu còn quan hệ bạn bè. (Khi hai bên chặn nhau, quan hệ bạn bè trực tiếp đã bị hủy ngay tại thời điểm chặn theo [DEC-03](../../../../decisions/blocking-and-friendship.md)).
    - *Tổng số bạn mở rộng* của người dùng `U`: lực lượng của tập hợp hợp nhất giữa tập **bạn trực tiếp (bậc 1)** của `U` và tập **bạn trực tiếp của những người bạn đó (bậc 2)**, sau khi:
      1. Loại trừ chính người dùng gốc `U`;
      2. Loại bỏ phần tử trùng lặp (một người vừa là bạn bậc 1 vừa là bạn của một người bạn khác hoặc xuất hiện qua nhiều người bạn trung gian chỉ được đếm **01 lần duy nhất**);
      3. Loại trừ các tài khoản đã xóa (`DELETED` hoặc đã `purge`);
      4. Loại trừ những tài khoản đang có quan hệ chặn (một chiều hoặc hai chiều) với người dùng gốc `U`. Tài khoản `LOCKED` vẫn được tính nếu còn quan hệ bạn bè hợp lệ. Do tập mở rộng chứa tập bạn trực tiếp nên luôn có `Tổng số bạn mở rộng >= Số bạn trực tiếp`.
  - **BR-03**: **Quy tắc lọc và sắp xếp**:
    - Bộ lọc số lượng với toán tử `=`, `<`, `>` chỉ áp dụng trên chỉ số **Số bạn trực tiếp**.
    - Lọc theo `họ tên` thực hiện đối sánh chuỗi con không phân biệt hoa thường sau khi cắt khoảng trắng thừa hai đầu (`trim`).
    - Sắp xếp hỗ trợ 02 tiêu chí: theo `họ tên` hoặc theo `ngày tạo tài khoản` (tăng/giảm, dùng `Account ID` làm khóa phụ cố định khi trùng giá trị; mốc ngày tạo quy đổi theo `Asia/Ho_Chi_Minh`).
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Phép tính hợp nhất bạn bậc 1 và bậc 2 phải được thực hiện nhất quán trên máy chủ theo `Account ID`, đảm bảo không đếm trùng lặp và không bao gồm dữ liệu nhạy cảm của tài khoản.
  - **NFR-02**: Khi **Admin** thay đổi điều kiện lọc hoặc sắp xếp liên tiếp, phản hồi đến muộn của truy vấn cũ không được ghi đè kết quả mới nhất.
