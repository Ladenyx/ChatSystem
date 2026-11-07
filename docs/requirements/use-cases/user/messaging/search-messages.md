# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-MSG-03
- **Use Case Name**: Tìm tin nhắn
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 07/11/2026
- **Primary Actor**: User có quyền đọc dữ liệu cần tìm.
- **Secondary Actor(s)**: Không có.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User tìm chuỗi trong một hoặc tất cả hội thoại có quyền đọc và mở đúng tin nguồn. Việc chọn phạm vi không tạo mục tiêu nghiệp vụ khác.
- **Goal**: Nhận kết quả đúng quyền và truy cập đúng vị trí tin, hoặc biết không có kết quả trong phạm vi đã tìm.
- **Trigger**: User muốn tìm nội dung tin nhắn đã trao đổi.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ; phạm vi tìm xác định.
  - Tin nhóm cần khóa giải mã phù hợp trên thiết bị để đưa nội dung vào phạm vi tìm.
- **Post-conditions**:
  - Kết quả chỉ gồm nội dung còn quyền đọc, chưa bị xóa phía mình hoặc thu hồi; có thể kết thúc với không có kết quả.
  - Mở tin, không gửi tin hoặc thay đổi quan hệ; nếu dữ liệu chưa đủ thì không khẳng định đã tìm hết.
  - Kết quả cũ không được thay thế kết quả của điều kiện tìm mới.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Nhập tin nhắn văn bản cần tìm | Kiểm tra điều kiện đầu vào và xác định dữ liệu hiện được phép tìm. |
| 2 | Yêu cầu thực hiện tìm kiếm. | Tìm trên phần lịch sử đã sẵn sàng, giải mã nhóm phía client; loại nội dung bị xóa/thu hồi hoặc không còn quyền. |
| 3 | Xem các kết quả phù hợp. | Cung cấp nội dung và nguồn đủ để phân biệt tin; ghi rõ mức hoàn tất của dữ liệu tìm. |
| 4 | Chọn một tin cần xem. | Kiểm tra lại quyền/trạng thái tin và lấy đúng phần lịch sử nguồn để đọc. |

### Alternative Flows

- **Alt 1a — Một hội thoại**: Tại bước 1, chỉ tìm trong hội thoại đã chỉ định. Xóa/đổi chuỗi không tự mở rộng ra tất cả hội thoại; quay lại bước 1 khi thay điều kiện.
- **Alt 1b — Tất cả hội thoại**: Tại bước 1, tìm trên mọi hội thoại User được phép truy cập. Kết quả tên hội thoại là hỗ trợ chọn nguồn đọc UC-U-MSG-01, không được trộn thành kết quả nội dung tin.
- **Alt 2a — Chưa đồng bộ hết lịch sử/chỉ mục**: Tại bước 2–3, trả phần tìm được kèm phạm vi chưa hoàn tất, tiếp tục khi dữ liệu sẵn sàng. Không coi không có kết quả tạm thời là kết luận toàn bộ lịch sử.
- **Alt 3a — Không có kết quả**: Tại bước 3, kết thúc với không có kết quả trong phạm vi đã tìm; User có thể sửa điều kiện để quay lại bước 1.
- **Alt 4a — Xem nhiều kết quả**: Sau bước 4, User chọn tin tiếp theo; lặp kiểm tra quyền và mở đúng tin, không tạo tác động ngoài mục tiêu tìm.

### Exception Flows

- **Ex 1a — Chuỗi rỗng**: không gửi yêu cầu tìm và không trả toàn bộ nội dung như một kết quả tìm kiếm.
- **Ex 2a — Lỗi tải/chỉ mục**: báo không hoàn tất phạm vi, giữ điều kiện để thử lại; không coi lỗi là kết quả rỗng.
- **Ex 2b — Thiếu khóa**: không tìm bản rõ chưa được giải mã; không gửi bản rõ nhóm hoặc khóa tới server để tìm thông thường.
- **Ex 4a — Tin vừa thu hồi/xóa riêng/mất quyền**: không mở nội dung cũ, cập nhật kết quả và cho chọn tin khác.
- **Ex 1b — Phiên không hợp lệ**: dừng truy cập và không trả dữ liệu dựa trên quyền cũ.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01: Cùng một kiểm tra quyền áp dụng khi lập kết quả và khi mở tin; chọn kết quả không bảo đảm quyền vẫn còn.
  - BR-02: Xóa phía mình và thu hồi phải được phản ánh trong chỉ mục/kết quả; không làm nội dung đã mất quyền xuất hiện lại từ cache.
  - BR-03: Tìm thường không chuyển bản rõ nhóm cho server. Lịch sử/chỉ mục nhóm phải phản ánh dữ liệu được phép giải mã.
  - BR-04: Tìm theo chuỗi văn bản không rỗng trong một hoặc tất cả hội thoại được phép. Danh sách dài cho phép xem tiếp kết quả theo cùng từ khóa và phạm vi; chưa tìm xong thì không kết luận đã hết kết quả.

- **Non-Functional Requirements (NFR)**:
  - NFR-01: Phản hồi bất đồng bộ gắn với điều kiện/phạm vi tìm; không ghi đè kết quả mới bằng phản hồi cũ.
  - NFR-02: Tìm kiếm không làm treo giao diện; hiển thị trạng thái đang tìm và phân biệt kết quả chưa đầy đủ với không có kết quả.
