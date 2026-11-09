# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-RPT-01
- **Use Case Name**: Gửi báo cáo spam
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng báo cáo
- **Secondary Actor(s)**: Admin hệ thống tiếp nhận để kiểm duyệt.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng gửi tin nghi spam, lý do và ngữ cảnh hội thoại để Admin xem xét.
- **Goal**: Báo cáo cùng gói bằng chứng được tiếp nhận và cấp mã hồ sơ.
- **Trigger**: Người dùng yêu cầu báo cáo một tài khoản từ hội thoại.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ, quyền gửi báo cáo và đọc lịch sử local của hội thoại; có ít nhất một tin hợp lệ của tài khoản đích, đã giải mã nếu thuộc nhóm E2EE.
- **Post-conditions**: Báo cáo và toàn bộ gói đã xác nhận được lưu cùng mã hồ sơ, chủ báo cáo, tài khoản đích, hội thoại, lý do và thời điểm tiếp nhận; chưa có kết luận vi phạm.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** xác định tài khoản bị báo cáo, chọn tin nghi spam và nhập lý do.
2. **Hệ thống** lấy tối đa 30 tin mới nhất đủ điều kiện từ local của cùng hội thoại, gộp với các tin được chọn và loại trùng theo ID.
3. **Hệ thống** cho xem trước toàn bộ gói, gồm nội dung, người gửi, thời gian, nhãn tin được báo cáo/ngữ cảnh và số lượng; nêu rõ bản rõ sẽ được gửi tới server cho Admin xem, kể cả từ nhóm E2EE.
4. **Người dùng** xác nhận chia sẻ gói bằng chứng và lý do.
5. **Server** kiểm tra phiên, quyền, metadata và giới hạn sử dụng, lưu toàn bộ báo cáo rồi trả mã hồ sơ cùng thời điểm tiếp nhận.

### Alternative Flows

- **A1 — Local có ít hơn 30 tin**: Tại bước 2, lấy toàn bộ tin local hợp lệ hiện có; vẫn cho gửi khi có tin được báo cáo hợp lệ.
- **A2 — Đổi tài khoản, lựa chọn, lý do hoặc làm mới ngữ cảnh**: Trước bước 4, kiểm tra lại tin được chọn và chuẩn bị lại gói; người dùng xem trước, xác nhận lại.
- **A3 — Có tin mới sau bản xem trước**: Tại bước 3–5, giữ nguyên gói đã chuẩn bị; muốn bổ sung thì thực hiện A2.
- **A4 — Hủy trước gửi**: Tại bước 4, người dùng không xác nhận; không chia sẻ dữ liệu hoặc tạo hồ sơ.
- **A5 — Yêu cầu đã được tiếp nhận**: Tại bước 5, trả báo cáo cũ cho cùng yêu cầu và nội dung; không tạo hồ sơ hoặc tính hạn mức lần nữa.

### Exception Flows

- **E1 — Không còn tin hợp lệ**: Trước khi gửi, tin bị thu hồi, xóa riêng, mất quyền đọc hoặc chưa giải mã phải bị loại. Chuẩn bị lại gói và xác nhận lại; không còn tin được báo cáo thì không gửi.
- **E2 — Không đọc được local**: Tại bước 2, giữ lựa chọn/lý do để thử lại; không coi lỗi là thiếu tin hoặc lấy lịch sử server thay thế.
- **E3 — Đầu vào không hợp lệ hoặc vượt hạn mức**: Tại bước 5, từ chối và nêu điều kiện cần điều chỉnh; không tự cắt nội dung, giảm ngữ cảnh hoặc chia báo cáo.
- **E4 — Mất xác nhận**: Tại bước 5, đối chiếu cùng ID và gói đã gửi trước khi thử lại; không trích xuất lại ngữ cảnh hoặc tự tạo ID mới.
- **E5 — Phiên/quyền hết hiệu lực**: Hệ thống dừng yêu cầu; yêu cầu đăng nhập lại nếu cần, không tự gửi bằng phiên mới.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Một báo cáo có một chủ, một tài khoản đích và một hội thoại. Selected gồm ít nhất một tin của tài khoản đích, không dùng tin của chính người báo cáo hoặc tin hệ thống. Không bắt buộc kết bạn hoặc online.
  - **BR-02**: Context gồm tối đa 30 tin mới nhất đủ quyền đọc trong local tại lúc chuẩn bị bản xem trước, không phải 30 tin quanh tin được chọn. Có thể gồm tin của người báo cáo và thành viên khác; không tải bù từ server.
  - **BR-03**: Evidence = Selected ∪ Context theo ID; giữ đủ tin được chọn dù nằm ngoài Context. Tin giao chỉ có một bản với cả hai nhãn. Giữ thứ tự hội thoại và số lượng tin được chọn, ngữ cảnh, giao nhau, tổng ID duy nhất.
  - **BR-04**: Lý do bắt buộc có nội dung. Giới hạn số tin được chọn áp dụng cho Selected; giới hạn dung lượng áp dụng cho toàn bộ Evidence sau loại trùng.
  - **BR-05**: Chỉ gửi đúng gói và lý do đã xác nhận; không sửa nội dung tin nguồn. Ngừng chờ sau khi gửi không đồng nghĩa hủy báo cáo.
  - **BR-06**: Admin kiểm duyệt được xem bằng chứng đã tiếp nhận; bản rõ do người báo cáo cung cấp không mặc nhiên được xác thực mật mã. Tiếp nhận không tự khóa/chặn tài khoản, xóa tin hoặc rời nhóm.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Lưu hồ sơ và gói bằng chứng cùng thành công hoặc không thay đổi; cùng ID khác nội dung bị từ chối.
  - **NFR-02**: Kiểm tra chủ báo cáo và quyền truy cập; truyền bảo mật, không gửi khóa E2EE, ghi nội dung/lý do vào log hoặc thực thi nội dung bằng chứng.
  - **NFR-03**: Lỗi báo cáo giữ nháp chat và dữ liệu còn hợp lệ; phản hồi gắn đúng yêu cầu/hội thoại.
