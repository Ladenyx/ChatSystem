# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-AI-01
- **Use Case Name**: Chỉnh sửa bản nháp bằng AI
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Người dùng
- **Secondary Actor(s)**: Nhà cung cấp AI qua server ứng dụng.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Người dùng nhờ AI viết lại nội dung đang soạn theo kiểu diễn đạt mong muốn.
- **Goal**: Có phương án diễn đạt phù hợp để sử dụng cho bản nháp.
- **Trigger**: Người dùng yêu cầu chỉnh sửa bản nháp.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ, quyền soạn trong hội thoại và bản nháp có ít nhất một ký tự không phải khoảng trắng.
- **Post-conditions**: Phương án viết lại được cung cấp; chỉ thay nháp khi người dùng xác nhận áp dụng.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** chọn kiểu diễn đạt và kiểm tra/sửa phần nháp muốn gửi AI.
2. **Hệ thống** cho xem trước nội dung, nêu rõ bản rõ sẽ tới server ứng dụng và nhà cung cấp AI, kể cả từ nhóm E2EE.
3. **Người dùng** xác nhận chia sẻ nội dung để tạo phương án.
4. **Hệ thống** kiểm tra quyền, đầu vào và hạn mức, gửi đúng nội dung đã xác nhận qua Muse API rồi trả các phương án.
5. **Người dùng** chọn một phương án và xác nhận dùng thay nháp.
6. **Hệ thống** kiểm tra quyền soạn và phiên bản nháp, áp dụng phương án vào đúng hội thoại.

### Alternative Flows

- **A1 — Hủy trước chia sẻ**: Tại bước 3, người dùng không xác nhận; kết thúc, không gửi dữ liệu.
- **A2 — Tạo lại**: Sau bước 4, người dùng muốn phương án khác; quay lại bước 1–3 để kiểm tra và xác nhận yêu cầu mới.
- **A3 — Không áp dụng**: Tại bước 5, người dùng kết thúc; giữ nháp hiện tại.
- **A4 — Nháp đã thay đổi**: Tại bước 6, hệ thống yêu cầu xác nhận thay thế nháp mới nhất; không đồng ý thì giữ nháp.

### Exception Flows

- **E1 — Đầu vào không hợp lệ hoặc vượt giới hạn**: Tại bước 4, yêu cầu sửa và xác nhận lại; không tự cắt nội dung để gửi.
- **E2 — Dịch vụ không khả dụng, hết hạn mức hoặc lỗi kết nối**: Tại bước 4, giữ nháp; không tự gọi lại AI. Thử lại theo A2.
- **E3 — Kết quả không dùng được**: Tại bước 4, AI từ chối hoặc trả dữ liệu rỗng/sai định dạng; không áp dụng kết quả.
- **E4 — Mất quyền hoặc phiên hết hiệu lực**: Tại bước 4 hoặc 6, dừng chia sẻ/áp dụng; yêu cầu đăng nhập lại nếu cần.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Chỉ gửi phần nháp, kiểu diễn đạt đã xác nhận và thông tin tối thiểu cho yêu cầu; không tự bổ sung lịch sử hoặc hồ sơ người nhận. Sửa đầu vào AI không sửa nháp gốc.
  - **BR-02**: Mỗi lần tạo hoặc thử lại cần xác nhận chia sẻ mới. Ngừng chờ sau khi đã gửi không bảo đảm thu hồi được dữ liệu hoặc chi phí.
  - **BR-03**: Kết quả chỉ là gợi ý; người dùng kiểm tra trước khi áp dụng và tự quyết định gửi tin.
  - **BR-04**: Chỉ áp dụng vào đúng hội thoại và phiên bản nháp đã xác nhận. Nếu nháp tiếp tục đổi, phải xác nhận lại; mất quyền soạn, kể cả do chặn, thì không áp dụng.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Giữ nháp khi hủy/lỗi; phản hồi cũ không ghi đè yêu cầu hoặc hội thoại mới. AI lỗi không cản soạn thủ công khi còn quyền.
  - **NFR-02**: Gọi Muse API qua server, khóa API chỉ lưu tại server; không dùng cookie/token cá nhân làm API chung.
  - **NFR-03**: Truyền bảo mật; không gửi khóa E2EE hoặc ghi nháp, kết quả, khóa vào log vận hành. Xử lý kết quả như văn bản, không thực thi chỉ dẫn.
