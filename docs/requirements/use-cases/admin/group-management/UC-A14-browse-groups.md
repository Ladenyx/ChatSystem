# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-A14
- **Use Case Name**: Tra cứu danh sách nhóm
- **Author / Create by**: ladenyx
- **Date Create**: 2026-11-02
- **Last Updated by / Date**: ladenyx / 2026-11-02
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: Không có tác nhân ngoài hệ thống
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin hệ thống xem danh sách nhóm chat, lọc nhóm theo tên và sắp xếp danh sách theo tên nhóm hoặc thời gian tạo.
- **Goal**: Tìm đúng nhóm cần kiểm tra và nắm quy mô thành viên trước khi xem chi tiết nhóm.
- **Trigger**: Admin truy cập màn hình A-SCR-04

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**: Actor đã đăng nhập vào Admin Console với quyền Admin hệ thống; phiên làm việc còn hợp lệ.
- **Post-conditions**: Danh sách nhóm (hoặc trạng thái rỗng) được hiển thị đúng theo tiêu chí lọc và sắp xếp hiện hành; nếu Admin chọn một nhóm thì chuyển tiếp sang `UC-A15`; dữ liệu hệ thống không bị thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Truy cập màn hình A-SCR-04 | Kiểm tra quyền và tải danh sách nhóm (gồm tên nhóm, thời gian tạo, quy mô thành viên) theo thứ tự mặc định: thời gian tạo mới nhất trước. |
| 2 | Nhập từ khóa tên nhóm cần tìm | Chuẩn hóa từ khóa, lọc danh sách nhóm khớp tên và hiển thị các nhóm phù hợp. |
| 3 | Chọn tiêu chí sắp xếp (tên nhóm hoặc thời gian tạo) và chiều sắp xếp (tăng/giảm) | Sắp xếp lại danh sách đang hiển thị theo tiêu chí và chiều sắp xếp đã chọn. |
| 4 | Chọn một nhóm trong danh sách | Đánh dấu nhóm được chọn và kích hoạt `UC-A15` để tải chi tiết thành viên. |

### Alternative Flows

- **Alt 1a (Danh sách hệ thống rỗng)**: Tại bước 1, hệ thống chưa có nhóm chat nào: hiển thị trạng thái chưa có nhóm nào trong hệ thống; kết thúc luồng tra cứu.
- **Alt 2a (Không lọc hoặc xóa bộ lọc)**: Tại bước 2, Admin bỏ qua bước nhập từ khóa hoặc xóa trắng ô tìm kiếm: hiển thị toàn bộ danh sách nhóm không giới hạn theo tên, giữ nguyên tiêu chí sắp xếp hiện hành.
- **Alt 2b (Không có kết quả khớp bộ lọc)**: Tại bước 2, không có nhóm nào phù hợp với từ khóa: hiển thị trạng thái không tìm thấy kết quả; cho phép Admin sửa hoặc xóa từ khóa để thực hiện lại bước 2.
- **Alt 3a (Giữ nguyên sắp xếp)**: Tại bước 3, Admin không thay đổi tiêu chí hoặc chiều sắp xếp: giữ nguyên thứ tự sắp xếp đang áp dụng.
- **Alt 4a (Chỉ tra cứu danh sách)**: Tại bước 4, Admin chỉ xem danh sách (hoặc lặp lại bước 2–3 để lọc/sắp xếp tiếp) mà không chọn nhóm cụ thể: giữ khung chi tiết ở trạng thái chờ chọn nhóm và kết thúc ở trạng thái tra cứu.

### Exception Flows

- **Ex-01 (Bước 1–3 — Lỗi kết nối hoặc truy xuất dữ liệu)**: Hiển thị thông báo lỗi tải dữ liệu tại chỗ, giữ nguyên tiêu chí lọc/sắp xếp đã chọn để Admin có thể thử tải lại; không coi dữ liệu cũ trước đó là kết quả mới.
- **Ex-02 (Bước 1–4 — Phiên không hợp lệ hoặc không đủ quyền)**: Từ chối thao tác, thông báo phiên hết hạn/không có quyền truy cập và yêu cầu xác thực lại.
- **Ex-03 (Bước 4 — Nhóm không còn khả dụng)**: Nhóm vừa chọn đã bị giải tán hoặc không còn tồn tại trên Server: thông báo nhóm không còn khả dụng, làm mới lại danh sách nhóm và đặt lại trạng thái chưa chọn nhóm (không tự mở nhóm khác thay thế).

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
    - **BR-01**: Phạm vi `A-08` chỉ gồm xem danh sách, lọc theo tên nhóm và sắp xếp theo tên nhóm hoặc thời gian tạo; không cung cấp thao tác tạo, sửa hoặc xóa nhóm trên màn hình `A-SCR-04`.
    - **BR-02**: Quyền Admin hệ thống và Admin nhóm là hai vai trò độc lập; việc tra cứu nhóm không tự động thêm Admin hệ thống làm thành viên hay cấp quyền Admin nhóm.
    - **BR-03**: Bộ lọc tên nhóm so khớp chuỗi con không phân biệt chữ hoa/thường sau khi cắt khoảng trắng thừa ở đầu và cuối từ khóa (`trim`).
    - **BR-04**: Admin hệ thống chỉ được tra cứu siêu dữ liệu quản trị nhóm (thông tin nhóm, thành viên, vai trò) và không có quyền xem nội dung hội thoại cá nhân hay nhóm kể cả khi không mã hóa E2EE (ngoại trừ các tin nhắn bằng chứng do người dùng chủ động gửi kèm khi báo cáo spam tại `UC-A-17`); đồng thời với nhóm áp dụng E2EE (`U-25`), hệ thống cũng không lưu khóa hay giải mã luồng chat nhóm.
- **Non-Functional Requirements (NFR)**:
    - **NFR-01 (Tối thiểu hóa dữ liệu & Bảo mật)**: Dữ liệu trả về cho màn hình `A-SCR-04` chỉ gồm siêu dữ liệu nhóm (mã nhóm, tên nhóm, thời gian tạo, số lượng thành viên); không tải bản mã hay nội dung luồng tin nhắn nhóm.
    - **NFR-02 (Phản hồi giao diện Desktop GUI)**: Tác vụ tải/lọc không làm đơ luồng giao diện (UI thread); giao diện phân biệt rõ các trạng thái: đang tải (`loading`), hệ thống chưa có nhóm / không có kết quả lọc (`empty`), và lỗi truy xuất (`error`).

