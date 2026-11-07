# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-AI-02
- **Use Case Name**: Nhận gợi ý tin nhắn bằng AI
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 07/11/2026
- **Primary Actor**: User cần gợi ý trả lời theo ngữ cảnh hội thoại.
- **Secondary Actor(s)**: Nhà cung cấp AI, được gọi qua server ứng dụng.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User chọn ngữ cảnh được phép đọc, xem trước và xác nhận phần bản rõ được chia sẻ để AI gợi ý nội dung. User tự quyết định có đưa phương án vào nháp hay không.
- **Goal**: Nhận gợi ý tin nhắn phù hợp để tiếp tục tự soạn và/hoặc quyết định gửi.
- **Trigger**: User muốn được gợi ý nội dung liên hệ trong hội thoại hiện tại.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ, hội thoại nguồn và quyền soạn còn khả dụng; có kết nối và dịch vụ AI sẵn sàng trong luồng cơ bản.
  - Luồng cơ bản có ít nhất một tin văn bản được phép đọc trong chính hội thoại, đã giải mã được nếu từ nhóm E2EE. Không bắt buộc có bản nháp hoặc người nhận đang online.
- **Post-conditions**:
  - User nhận được phương án tương ứng với nội dung đã xác nhận, gắn đúng hội thoại/yêu cầu.
  - Chỉ khi User chủ động xác nhận áp dụng và phiên bản nháp còn phù hợp, phương án mới thay nháp; chưa có tin được gửi.
  - Không áp dụng, hủy hoặc lỗi không sửa lịch sử/nguyên nháp. Không xác nhận chia sẻ thì không gửi dữ liệu; kết quả trả muộn không tự áp dụng.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Yêu cầu gợi ý nội dung cho hội thoại hiện tại. | Ghi hội thoại/phiên nguồn và phiên bản nháp hiện tại để bảo vệ khi áp dụng; chưa chia sẻ dữ liệu. |
| 2 | Chọn các tin văn bản làm ngữ cảnh. | Chỉ cho chọn tin còn được phép đọc, chưa bị thu hồi/xóa phía mình. Chuẩn bị bản xem trước theo thứ tự lượt nói, chỉ gồm nội dung User chọn. Cho điều chỉnh phần văn bản dành cho yêu cầu, không sửa tin nguồn. |
| 3 | Kiểm tra và xác nhận chia sẻ bản rõ tới server ứng dụng và nhà cung cấp AI. | Kiểm tra lại quyền đọc, trạng thái thu hồi/xóa phía mình, đầu vào và hạn mức trước khi chia sẻ; gửi đúng nội dung đã xác nhận, không tự bổ sung nháp hiện có hoặc tin mới đến. Nêu rõ bản rõ nhóm E2EE đi ra ngoài luồng E2EE. |
| 4 | Nhận các phương án. | Chỉ cung cấp kết quả sử dụng được của đúng yêu cầu; chưa thay nháp hoặc tự chọn phương án. |
| 5 | Chọn và xác nhận áp dụng một phương án. | Kiểm tra lại phiên, hội thoại, quyền soạn và phiên bản nháp; khi phù hợp, thay nháp bằng phương án được chọn. |
| 6 | Kiểm tra nội dung nháp. | Giữ nội dung chờ User tự quyết định; gửi tin thực tế thuộc UC-U-MSG-02. |

### Alternative Flows

- **Alt 2a — Ngữ cảnh đổi trước chia sẻ**:
  - Tại bước 2–3, khi mới chọn/xem trước mà chưa chia sẻ bản rõ tới server ứng dụng, tin bị thu hồi/xóa phía mình không còn được chọn làm ngữ cảnh, không gửi lại bản sao cũ của tin đã bị loại.
  - Nếu vẫn còn ngữ cảnh hợp lệ, tiếp tục bước 3.
  - Nếu không còn tin hợp lệ, yêu cầu chọn lại ngữ cảnh hoặc kết thúc không gửi yêu cầu AI.
- **Alt 2b — Sửa đầu vào**: Quay lại bước 2 và lấy xác nhận cho nội dung mới; lựa chọn cũ không cho phép gửi bản đã thay đổi.

- **Alt 3a — Hủy trước chia sẻ**: User không đồng ý, kết thúc mà không gửi dữ liệu và giữ nháp.
- **Alt 3b — Tin nguồn bị thu hồi/xóa sau chia sẻ**: Sau bước 3, một hoặc nhiều tin nguồn bị thu hồi/xóa phía mình. Không khôi phục tin nguồn hoặc tự gửi lại nội dung đã bị loại cho một yêu cầu mới. Thay đổi tin nguồn không bảo đảm thu hồi được bản rõ đã gửi; kết quả AI không tự áp dụng vào nháp.
- **Alt 4a — Tạo lại**: User muốn phương án khác, quay lại bước 2–3; đây là yêu cầu AI mới, phải kiểm tra lại tin còn được phép chọn, xác nhận lại và có thể tiêu thụ hạn mức. Không tái sử dụng tin đã thu hồi/xóa phía mình từ bản chụp của yêu cầu trước làm ngữ cảnh cho yêu cầu mới.
- **Alt 4b — Không áp dụng**: Kết thúc sau khi xem, không đổi nháp hoặc tạo tin.
- **Alt 4c — Rời hội thoại/ngừng chờ**: Hủy xử lý nếu có thể; không bảo đảm thu hồi dữ liệu đã gửi hoặc không tính phí. Kết quả trễ không tự áp dụng/mở lại ngữ cảnh đã rời.
- **Alt 5a — Nháp đã đổi**: Đối chiếu nháp hiện tại với phương án; chỉ thay khi User xác nhận lại. Hủy giữ nháp; nếu nháp lại đổi thì xác nhận cũ mất hiệu lực.
- **Alt 1a — Muốn cải thiện nháp có sẵn**: Dẫn tới mục tiêu riêng UC-U-AI-01; không lồng luồng Refine như một nhánh tạo nội dung gợi ý.

### Exception Flows

- **Ex 2a — Không có đầu vào hợp lệ**: Khi chọn ngữ cảnh hoặc chuẩn bị chia sẻ cho yêu cầu mới, không dùng tin thuộc hội thoại khác, tin hệ thống không phù hợp, đã thu hồi/xóa phía mình hoặc chưa giải mã. Phân biệt lịch sử chưa tải xong với hội thoại trống. Không tự lấy toàn bộ lịch sử thay User chọn. Tin nguồn thay đổi sau khi chia sẻ được xử lý tại Alt 3b.
- **Ex 3a — Vượt giới hạn**: Thông báo đầu vào vượt giới hạn của hệ thống và yêu cầu User điều chỉnh, không cắt nội dung rồi gửi bản khác.
- **Ex 3b — Mạng/timeout/hết hạn mức/chưa cấu hình**: Giữ nháp và đầu vào phù hợp, không tự gọi lại AI; User kiểm tra/xác nhận mới trước thử lại. Phản hồi cũ không ghi đè yêu cầu mới.
- **Ex 4a — Dịch vụ từ chối/rỗng/sai định dạng**: Không đưa dữ liệu lỗi vào nháp; User có thể sửa đầu vào hoặc tiếp tục soạn thủ công.
- **Ex 5a — Mất quyền hoặc hết phiên**: Không áp dụng nếu hội thoại mất quyền soạn, nhóm đã rời/đóng hoặc chat riêng có chặn ở bất kỳ chiều nào. Không áp dụng sang hội thoại/phiên mới hoặc tự khôi phục quyền để hoàn tất.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

- **BR-01**: Chỉ dùng các tin ngữ cảnh đã xem trước và xác nhận; không tự đưa nháp đang có, toàn bộ hội thoại, danh bạ hoặc thông tin nhận diện không cần thiết vào lời gọi AI.
- **BR-02 — Phạm vi ngữ cảnh**: Sửa bản xem trước chỉ sửa dữ liệu gửi AI, không sửa lịch sử hay nháp gốc. Mỗi yêu cầu mới phải kiểm tra lại quyền đọc và chỉ chọn tin chưa bị thu hồi/xóa phía mình, đã giải mã nếu từ nhóm E2EE. Chỉ gửi đúng phần nội dung đã xác nhận; không tự thêm tin mới đến hoặc khôi phục tin nguồn đã bị loại.
- **BR-03**: Mỗi lần tạo/thử lại cần đồng ý chia sẻ mới. Không gửi khóa E2EE hoặc cho server/AI tự giải mã lịch sử; kết quả chỉ về nháp sau xác nhận áp dụng.
- **BR-04**: Gắn yêu cầu/kết quả với hội thoại, phiên và đầu vào; không tự dùng kết quả cũ cho đầu vào mới. Không tự gửi tin cho đối phương hoặc đánh dấu nội dung AI đã được xác minh.
- **BR-05 — Giới hạn sử dụng**: Số tin ngữ cảnh, độ dài đầu vào và số phương án tuân giới hạn của hệ thống. Khi vượt ngưỡng đầu vào, yêu cầu User điều chỉnh; khi hết quota, hết thời gian chờ hoặc dịch vụ không sẵn sàng, thông báo và giữ nguyên nháp.

- **BR-06**: Muse API tích hợp qua server; khóa API không lưu ở client. Không dùng token/cookie cá nhân thay API chung.

### Non-Functional Requirements (NFR)

- **NFR-01**: Bảo toàn nháp, phản hồi đúng yêu cầu và quyền hiện tại; AI lỗi không ngăn chat thủ công khi còn quyền.
- **NFR-02**: Truyền bảo mật, không ghi nháp/ngữ cảnh/kết quả hoặc khóa vào log vận hành; đầu ra được xử lý như văn bản, không thực thi chỉ dẫn trong ngữ cảnh/kết quả.
- **NFR-03**: Trình bày gợi ý để User kiểm tra mức phù hợp với ngữ cảnh và cách diễn đạt tiếng Việt trước khi áp dụng; không tự coi kết quả AI là nội dung đã được xác minh.
