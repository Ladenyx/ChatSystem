# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-AI-02
- **Use Case Name**: Nhận gợi ý tin nhắn bằng AI
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Nhà cung cấp AI qua server ứng dụng.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng chọn các tin ngữ cảnh để AI gợi ý câu trả lời.
- **Goal**: Có phương án trả lời phù hợp để tiếp tục soạn tin.
- **Trigger**: Người dùng yêu cầu gợi ý trả lời trong hội thoại.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ, quyền soạn và ít nhất một tin văn bản được phép đọc trong hội thoại; tin nhóm cần được giải mã.
- **Post-conditions**: Các phương án tương ứng với ngữ cảnh đã xác nhận được cung cấp; chỉ thay nháp khi người dùng xác nhận áp dụng.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** chọn các tin làm ngữ cảnh và kiểm tra/sửa phần nội dung gửi AI.
2. **Hệ thống** cho xem trước ngữ cảnh theo thứ tự trao đổi, nêu rõ bản rõ sẽ tới server ứng dụng và nhà cung cấp AI, kể cả từ nhóm E2EE.
3. **Người dùng** xác nhận chia sẻ ngữ cảnh.
4. **Hệ thống** kiểm tra lại quyền, trạng thái tin và hạn mức, gửi đúng ngữ cảnh đã xác nhận qua Muse API rồi trả các phương án.
5. **Người dùng** chọn một phương án và xác nhận dùng làm nháp.
6. **Hệ thống** kiểm tra quyền soạn và phiên bản nháp, áp dụng phương án vào đúng hội thoại.

### Alternative Flows

- **A1 — Ngữ cảnh đổi trước chia sẻ**: Tại bước 1–4, tin bị thu hồi/xóa riêng hoặc mất quyền đọc. Loại tin đó khỏi phần gửi và yêu cầu kiểm tra, xác nhận lại; không còn tin hợp lệ thì chọn lại hoặc kết thúc.
- **A2 — Hủy hoặc không dùng kết quả**: Tại bước 3, hủy thì không chia sẻ dữ liệu; tại bước 5, không áp dụng thì giữ nháp.
- **A3 — Tạo lại**: Sau bước 4, quay lại bước 1–3; kiểm tra lại ngữ cảnh và xác nhận yêu cầu mới.
- **A4 — Nháp đã thay đổi**: Tại bước 6, chỉ thay nháp mới nhất sau khi người dùng xác nhận lại.

### Exception Flows

- **E1 — Không có ngữ cảnh hợp lệ hoặc vượt giới hạn**: Tại bước 1 hoặc 4, yêu cầu điều chỉnh lựa chọn; không tự lấy thêm lịch sử hoặc cắt nội dung.
- **E2 — Dịch vụ không khả dụng, hết hạn mức hoặc lỗi kết nối**: Tại bước 4, giữ nháp; không tự gọi lại AI. Thử lại theo A3.
- **E3 — Kết quả không dùng được**: Tại bước 4, AI từ chối hoặc trả dữ liệu rỗng/sai định dạng; không áp dụng kết quả.
- **E4 — Mất quyền hoặc phiên hết hiệu lực**: Tại bước 4 hoặc 6, dừng chia sẻ/áp dụng; yêu cầu đăng nhập lại nếu cần.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Chỉ dùng tin văn bản cùng hội thoại, còn quyền đọc, chưa thu hồi/xóa riêng và đã giải mã. Không cần có sẵn nháp hoặc người nhận online.
  - **BR-02**: Mỗi yêu cầu chỉ gửi nội dung đã xem trước và xác nhận; không tự thêm nháp, tin mới hoặc toàn bộ lịch sử. Sửa đầu vào AI không sửa tin nguồn hay nháp.
  - **BR-03**: Tin bị xóa/thu hồi sau khi chia sẻ không bảo đảm thu hồi được bản rõ đã gửi; không dùng lại tin đó cho yêu cầu mới. Ngừng chờ không bảo đảm hủy chi phí đã phát sinh.
  - **BR-04**: Kết quả chỉ là gợi ý, không tự gửi tin. Chỉ áp dụng vào đúng hội thoại, phiên và nháp đã xác nhận; mất quyền soạn, kể cả do chặn, thì không áp dụng.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Giữ nháp khi hủy/lỗi; phản hồi cũ không ghi đè yêu cầu mới hoặc tự áp dụng sau khi rời hội thoại. AI lỗi không cản soạn thủ công khi còn quyền.
  - **NFR-02**: Muse API được gọi qua server, khóa API chỉ lưu tại server; không dùng cookie/token cá nhân làm API chung.
  - **NFR-03**: Truyền bảo mật; không gửi khóa E2EE hoặc ghi ngữ cảnh, nháp, kết quả, khóa vào log vận hành. Không thực thi chỉ dẫn trong ngữ cảnh/kết quả.
