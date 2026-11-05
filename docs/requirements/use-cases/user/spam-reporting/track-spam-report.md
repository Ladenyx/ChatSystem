# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-RPT-02
- **Use Case Name**: Theo dõi và xem kết quả báo cáo
- **Author / Create by**: Kiet
- **Date Create**: 05/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
- **Primary Actor**: Chủ báo cáo đã gửi.
- **Secondary Actor(s)**: Không bắt buộc actor phụ hoạt động đồng thời.
- **Priority**: Should Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Chủ báo cáo xem trạng thái tiếp nhận/xử lý và kết quả của hồ sơ mình gửi mà không tạo yêu cầu xử lý mới.
- **Goal**: Biết trạng thái và kết quả được phép xem của báo cáo.
- **Trigger**: User muốn kiểm tra báo cáo đã gửi hoặc đọc kết quả được thông báo.

  > **NOTE:** Đề xuất chức năng theo dõi và xem lại báo cáo; cần xác nhận vị trí mở, nội dung được xem và Feature ID. Toàn bộ use case là bản nháp phương án chưa được duyệt.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - User có phiên hợp lệ; chỉ yêu cầu danh sách/hồ sơ do chính mình gửi.
  - Hồ sơ đã được tiếp nhận hoặc User có thông tin yêu cầu để đối chiếu; có kết nối trong luồng cơ bản.
  - Không suy quyền xem bản rõ của chủ báo cáo từ quyền Admin xem bằng chứng.

    > **NOTE:** Đề xuất cần chốt chủ báo cáo được xem lại bản rõ đã gửi hay chỉ metadata/kết quả.

- **Post-conditions**:
  - User nhận thông tin trạng thái/kết quả mới nhất được phép truy cập, kể cả chưa có kết luận hoặc danh sách trống.
  - Không thay đổi báo cáo, tạo yêu cầu rút, đảo ngược chế tài hay gia hạn lưu bằng chứng.
  - Thất bại không làm lộ dữ liệu người khác hoặc trình bày dữ liệu cũ như dữ liệu vừa được xác nhận.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Yêu cầu xem các báo cáo mình đã gửi trong phạm vi đang được phép truy cập. | Xác thực User và chỉ truy xuất hồ sơ có quyền sở hữu phù hợp. |
| 2 | Chọn hồ sơ cần theo dõi. | Đối chiếu quyền tại thời điểm truy xuất và cung cấp ID, mốc tiếp nhận, trạng thái xử lý, kết luận nếu có và thông tin lưu giữ được phép xem. |
| 3 | Xem trạng thái/kết quả hoặc lịch sử yêu cầu rút liên quan. | Phân biệt chưa kiểm duyệt, kết luận đã ghi, yêu cầu rút chờ/đã xử lý và tình trạng bằng chứng; không suy diễn từ một trạng thái sang trạng thái khác. |
| 4 | Kết thúc việc tra cứu. | Không thay đổi hồ sơ; hoàn tất mục tiêu xem. |

### Alternative Flows

- **Alt 1a — Không có báo cáo**: Tại bước 1, trả danh sách trống đúng phạm vi; kết thúc xem thành công, không tạo hồ sơ.
- **Alt 2a — Chưa có kết luận**: Tại bước 2–3, cung cấp trạng thái đã tiếp nhận/đang xử lý theo dữ liệu thực; không biến xác nhận tiếp nhận thành kết quả vi phạm.
- **Alt 2b — Nhận thông báo kết quả**: User mở thông báo có tham chiếu hồ sơ, hệ thống kiểm tra lại chủ sở hữu rồi tiếp tục bước 2–3. Nội dung thông báo không thay kiểm tra quyền.
- **Alt 3a — Bằng chứng hết hạn**: Theo hạn lưu sau khi được chốt, chỉ cung cấp metadata/kết quả tối thiểu còn được phép giữ; không tải lại bản rõ đã xóa từ lịch sử hoặc cache.

  > **NOTE:** Đề xuất giữ metadata/kết quả tối thiểu sau hết hạn bằng chứng; cần xác nhận hạn lưu và tập dữ liệu còn được xem.

- **Alt 3b — Cập nhật kết quả**: User tra cứu lại; hệ thống cung cấp phiên bản mới đã xác nhận, không giữ trạng thái cũ như kết luận hiện tại.
- **Alt 3c — Muốn yêu cầu rút**: Kết thúc tra cứu và dẫn tới mục tiêu riêng [UC-U-RPT-03](request-report-withdrawal.md); việc mở xem không tự tạo yêu cầu rút.

  > **NOTE:** Đề xuất chức năng yêu cầu rút báo cáo; cần xác nhận phạm vi và thời hạn xử lý.

### Exception Flows

- **Ex 2a — Không thuộc quyền sở hữu hoặc hồ sơ không khả dụng**: Không trả bằng chứng/trạng thái của người khác; không dùng ID do client gửi để bỏ kiểm tra quyền.
- **Ex 1a — Mạng/lỗi tải/chưa rõ đối chiếu**: Phân biệt lỗi tải với danh sách trống và chưa có kết luận; dữ liệu đang có phải thể hiện chưa được làm mới, không giả báo có kết quả.
- **Ex 2b — Phiên/quyền hết hiệu lực**: Dừng truy xuất và dẫn luồng xác thực hiện có; không lưu nội dung hồ sơ để hiển thị ở phiên khác.
- **Ex 3a — Yêu cầu rút đang chờ hoặc kết luận vừa đổi**: Chỉ hiển thị bản đã xác nhận và trạng thái riêng từng phần; không tự hủy, chấp thuận hay tạo thêm yêu cầu rút.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

- **BR-01**: Chủ báo cáo chỉ xem hồ sơ mình gửi; bị báo cáo không tạo quyền truy cập hồ sơ gốc. Admin xem tại UC quản trị riêng.
- **BR-02**: Kết luận, trạng thái rút và thời hạn bằng chứng là độc lập. Xem lại không làm mới hạn xử lý/lưu giữ; hết lưu không đồng nghĩa đã rút hoặc không vi phạm.
- **BR-03**: Use case này chỉ mô tả User đọc thông tin và kết quả được phép. Việc tạo thông báo thuộc phân hệ Admin do teammate phụ trách; nội dung phản hồi cần được hai bên thống nhất.
- **BR-04 — Phạm vi thông tin được xem**: Basic Flow chỉ cung cấp metadata/kết quả được phép; hồ sơ gốc/bằng chứng bản rõ hiện dành cho Admin có quyền theo nguồn, không tự mở rộng cho User khi tách mục tiêu xem.

  > **NOTE:** Đề xuất cần chốt phạm vi theo hội thoại hay toàn bộ báo cáo, truy cập sau rời nhóm, quyền xem lại bản rõ, điểm mở, hạn lưu 30 ngày và Feature ID theo dõi báo cáo.

- **BR-05**: Yêu cầu rút hoặc xem bằng chứng cần quyền riêng; không mặc định việc có bản ghi trong danh sách cho phép truy cập dữ liệu đã hết hạn.

### Non-Functional Requirements (NFR)

- **NFR-01**: Truyền bảo mật và kiểm tra sở hữu ở mọi lần truy xuất; không lộ nội dung báo cáo khác trong lỗi/log.
- **NFR-02**: Phản hồi gắn đúng hồ sơ/phiên; không để kết quả tải cũ thay hồ sơ User vừa chọn.
- **NFR-03**: Giữ khả năng tra cứu metadata/kết quả được phép khi bản rõ hết hạn theo chính sách được duyệt; không phục hồi dữ liệu đã xóa qua bản sao/cache.
