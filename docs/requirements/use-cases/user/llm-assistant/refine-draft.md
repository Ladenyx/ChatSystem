# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-AI-01
- **Use Case Name**: Chỉnh sửa bản nháp bằng AI
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
- **Primary Actor**: User có bản nháp hợp lệ và quyền soạn trong hội thoại.
- **Secondary Actor(s)**: Nhà cung cấp AI, được gọi qua server ứng dụng.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User yêu cầu AI cải thiện cách diễn đạt của nội dung đã soạn, xem các phương án và tự quyết định có thay bản nháp hay không. Nội dung chỉ được chia sẻ tới server/AI sau khi User xem trước và xác nhận.
- **Goal**: Có phương án viết lại phù hợp, bảo toàn ý định và quyền quyết định gửi tin của User.
- **Trigger**: User muốn cải thiện bản nháp hiện tại.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - User có phiên hợp lệ; hội thoại cá nhân hoặc nhóm còn cho phép soạn theo quyền hiện tại. Chat riêng có chặn ở bất kỳ chiều nào không đáp ứng điều kiện này theo DEC-03.
  - Bản nháp có văn bản không rỗng sau khi loại khoảng trắng đầu/cuối; thiết bị có quyền đọc nội dung được dùng.
  - Luồng cơ bản có kết nối và dịch vụ AI được cấu hình, được phép sử dụng, còn hạn mức.
- **Post-conditions**:
  - Thành công sinh nội dung: User nhận được phương án viết lại của đúng yêu cầu và hội thoại.
  - Nếu User xác nhận áp dụng: phương án thay nội dung nháp sau khi đối chiếu phiên bản nháp; chưa tạo tin trong lịch sử hoặc gửi cho người nhận.
  - Hủy, không áp dụng, lỗi hoặc hết hạn mức giữ nguyên nháp đã có. Không xác nhận chia sẻ thì không gửi nội dung qua chức năng này.
  - Sau khi đã gửi yêu cầu, rời phiên xử lý không bảo đảm thu hồi được dữ liệu hoặc phí dịch vụ; phản hồi cũ không tự đổi nháp.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Chọn cải thiện bản nháp hiện tại. | Ghi nhận hội thoại nguồn, phiên bản nháp và bản sao nội dung; chưa thay nháp hay chia sẻ dữ liệu. |
| 2 | Chọn kiểu diễn đạt mong muốn, kiểm tra/sửa nội dung dành cho AI và xác nhận tạo phương án. | Cho xem trước nội dung và phạm vi chia sẻ theo BR-02; kiểm tra dữ liệu, quyền và hạn mức rồi gửi đúng nội dung đã xác nhận, gắn định danh yêu cầu/hội thoại. Sửa đầu vào không sửa nháp gốc. |
| 3 | Nhận và xem các phương án viết lại. | Đối chiếu yêu cầu hiện hành rồi cung cấp kết quả sử dụng được; chưa chọn hay áp dụng thay User. |
| 4 | Chọn phương án và xác nhận dùng thay bản nháp. | Kiểm tra lại hội thoại, quyền soạn, phiên và phiên bản nháp. Khi vẫn hợp lệ và chưa đổi, thay nội dung nháp bằng phương án được chọn. |
| 5 | Kiểm tra nội dung đã được áp dụng. | Giữ nội dung ở dạng nháp. Gửi tin là mục tiêu riêng tại [UC-U-MSG-02](../messaging/send-message.md). |

### Alternative Flows

- **Alt 2a — Sửa đầu vào**: Tại bước 2, User sửa nội dung/kiểu diễn đạt. Hệ thống cập nhật bản xem trước, chưa gửi và phải lấy xác nhận cho phiên bản mới; tiếp tục bước 2.
- **Alt 2b — Không đồng ý chia sẻ**: Trước khi phát yêu cầu ở bước 2, User hủy. Không gửi dữ liệu, nháp giữ nguyên; kết thúc.
- **Alt 3a — Tạo lại**: Tại bước 3, User muốn phương án khác hoặc sửa đầu vào. Quay lại bước 2 và xác nhận yêu cầu mới chịu hạn mức hiện tại; không dùng kết quả cũ như kết quả của đầu vào mới. Lỗi lần tạo lại giữ nháp.
- **Alt 3b — Không dùng phương án**: Sau bước 3, User kết thúc mà không áp dụng; nháp gốc giữ nguyên, kết thúc thành công ở việc nhận phương án.
- **Alt 3c — Rời hội thoại hoặc ngừng chờ**: Tại bước 2–3, User rời ngữ cảnh nguồn. Hệ thống hủy xử lý nếu có thể; kết quả đến sau không tự mở lại phiên xử lý, áp dụng sang hội thoại khác hoặc thay nháp.
- **Alt 4a — Nháp đã thay đổi**: Tại bước 4, hệ thống đối chiếu thấy nháp khác phiên bản ghi nhận. User được kiểm tra cả nội dung hiện tại và phương án; chỉ thay thế khi xác nhận lại. Hủy giữ nháp hiện tại; nếu nháp lại đổi thì phải đối chiếu lại, không dùng xác nhận cũ.
- **Alt 1a — Muốn nhận nội dung gợi ý theo ý định/ngữ cảnh**: Đây là mục tiêu khác tại [UC-U-AI-02](recommend-message.md); không tự chuyển chế độ hay gửi dữ liệu của UC này như một yêu cầu gợi ý.

### Exception Flows

- **Ex 2a — Đầu vào không hợp lệ/vượt giới hạn**: Tại bước 2, không gửi nội dung rỗng hoặc không đáp ứng cấu hình đã được duyệt; yêu cầu User điều chỉnh, không tự cắt và gửi bản khác với bản đã xác nhận.
- **Ex 2b — Mạng/timeout/chưa cấu hình/hết hạn mức**: Không coi là có kết quả, giữ nháp và đầu vào phù hợp; không tự gọi lại AI. Thử lại là yêu cầu mới sau khi User kiểm tra/xác nhận. Phản hồi trễ không ghi đè kết quả yêu cầu mới.
- **Ex 3a — Dịch vụ từ chối, trả rỗng hoặc sai định dạng**: Không dùng dữ liệu lỗi làm nội dung nháp; User có thể điều chỉnh và quay lại bước 2 hoặc soạn thủ công.
- **Ex 4a — Mất quyền hoặc đổi phiên**: Ở bước 2–4, nếu phiên hết hiệu lực, User rời/bị xóa khỏi nhóm, nhóm đóng hoặc chat riêng phát sinh chặn, dừng thao tác cần quyền và không áp dụng phản hồi vào ngữ cảnh/phiên khác. Việc đã gửi dữ liệu trước đó không được giả định đã hủy.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

- **BR-01 — Dữ liệu tối thiểu**: Chỉ bản nháp đã xem trước, kiểu diễn đạt và metadata tối thiểu cho yêu cầu; không tự bổ sung lịch sử, danh bạ hay hồ sơ người nhận.
- **BR-02 — Đồng ý chia sẻ**: Mỗi lần tạo/thử lại phải được xác nhận sau khi User xem trước nội dung và biết phạm vi chia sẻ tới server ứng dụng, nhà cung cấp AI. Với nội dung nhóm E2EE, giải thích đây là chia sẻ bản rõ ra ngoài luồng E2EE. Không gửi khóa bí mật hoặc cho server/AI khả năng giải mã lịch sử nhóm; HTTPS không thay thế việc xác nhận bản rõ.
- **BR-03 — Quyền áp dụng**: Kết quả chỉ là gợi ý, User có cơ hội kiểm tra/sửa. Áp dụng chỉ thay nháp đúng hội thoại và phiên bản đã xác nhận; không tự gửi hay tạo sự kiện chat cho đối phương.
- **BR-04 — Bất đồng bộ**: Gắn phản hồi với định danh yêu cầu, hội thoại và phiên; kết quả cũ không thay thế yêu cầu mới. Ngừng chờ không cam kết nhà cung cấp chưa xử lý hoặc chưa tính phí.
- **BR-05 — Phần cần xác nhận**: Model, đầu vào/đầu ra, quota, thời gian chờ và nhãn AI theo DEC-08; các giá trị 5/3/15 giây không là BR đã duyệt. Không tự thêm watermark vào tin.
- **BR-06 — Dịch vụ**: Theo DEC-08, tích hợp Muse API qua server; khóa API ở server. Không dùng cookie/token web hoặc token cá nhân làm API chung.

### Non-Functional Requirements (NFR)

- **NFR-01**: Xử lý AI không làm chặn các thao tác hợp lệ khác; lỗi AI không làm mất nháp hoặc ngăn soạn thủ công khi còn quyền.
- **NFR-02**: Truyền bảo mật; không ghi khóa API/E2EE, nháp hay kết quả vào log vận hành thông thường. Xử lý đầu ra như văn bản, không thực thi mã hoặc thao tác tài khoản từ chỉ dẫn trong kết quả.
- **NFR-03**: Đánh giá khả năng giữ ý và diễn đạt tiếng Việt theo kịch bản được duyệt; không cam kết đầu ra luôn đúng. Ngưỡng nghiệm thu định lượng còn chờ xác nhận.
