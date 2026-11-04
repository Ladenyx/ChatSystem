# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-FRD-08
- **Use Case Name**: Xem và lọc danh sách hội thoại
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
- **Primary Actor**: User đã đăng nhập muốn tra cứu các hội thoại của mình.
- **Secondary Actor(s)**: Người tham gia chat riêng và thành viên nhóm có trạng thái hiện diện được dùng để xác định Online/Offline của hội thoại.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User xem danh sách hội thoại cá nhân và nhóm E2EE được phép truy cập, giới hạn danh sách theo **Tất cả**, **Chưa đọc** hoặc **Nhóm**, kèm trạng thái Online/Offline. Đây là tra cứu hội thoại của User, không phải tra cứu danh sách bạn bè hay tìm nội dung tin.
- **Goal**: Nhận danh sách hội thoại đúng quyền và điều kiện lọc, đủ để xác định hội thoại cần quan tâm; có thể kết thúc mà không mở hoặc đọc tin nhắn.
- **Trigger**: User muốn xem các hội thoại của mình hoặc thay đổi phạm vi lọc danh sách đang xem.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - User có phiên hợp lệ và được phép xem danh sách hội thoại của chính mình.
  - Không bắt buộc đã có hội thoại, tin chưa đọc, bạn bè đang online hoặc khóa giải mã lịch sử nhóm sẵn sàng chỉ để xem metadata được phép cung cấp.
  - Luồng cơ bản có kết nối để đối chiếu danh sách và trạng thái; dữ liệu chưa hoàn tất hoặc mất kết nối được xử lý riêng, không giả định danh sách rỗng.
- **Post-conditions**:
  - Thành công: User nhận danh sách đúng bộ lọc trong tập hội thoại được phép liệt kê; mỗi hội thoại xuất hiện một lần theo định danh hội thoại, kể cả khi kết quả rỗng.
  - Thông tin loại hội thoại, chưa đọc và hiện diện phản ánh dữ liệu đã được xác nhận; nếu còn thiếu dữ liệu thì không khẳng định kết quả đã đầy đủ.
  - Xem hoặc đổi bộ lọc không đánh dấu tin đã đọc, gửi tin, tạo hội thoại, thay đổi quan hệ hoặc cấp thêm quyền xem lịch sử.
  - Kết thúc hoặc đổi bộ lọc không làm mất bản nháp của hội thoại đang mở; kết quả truy vấn cũ không được áp dụng cho tài khoản hoặc điều kiện mới.

## Luồng sự kiện (Flow of Events)

### Basic Flow

Luồng cơ bản: xem danh sách với phạm vi Tất cả.

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Xem danh sách hội thoại của mình. | Xác thực User, xác định tập hội thoại được phép liệt kê và trạng thái dữ liệu hiện có; chưa tải hay đánh dấu đọc toàn bộ tin chỉ vì danh sách được yêu cầu. |
| 2 | Chọn phạm vi Tất cả. | Lấy các hội thoại cá nhân và nhóm E2EE trong tập được phép; nhận diện theo định danh hội thoại và loại bỏ bản ghi trùng. |
| 3 | Tra cứu các hội thoại được cung cấp. | Cung cấp thông tin nhận diện để phân biệt chat riêng/nhóm, tình trạng chưa đọc và trạng thái hiện diện được phép biết. Chat riêng dùng hiện diện của đối phương. Nhóm có chỉ báo loại/E2EE và hiển thị **Online khi ít nhất một thành viên trong nhóm online**; chỉ hiển thị Offline khi đã xác nhận không có thành viên online theo BR-05. |
| 4 | Nhận được danh sách cần tra cứu và kết thúc xem. | Hoàn tất mục tiêu với danh sách đúng phạm vi đã xác nhận; không bắt buộc chọn hội thoại, đọc lịch sử hoặc gửi tin. |

### Alternative Flows

- **Alt 2a — Chưa đọc**: Thay bước 2, User chọn Chưa đọc. Chỉ giữ hội thoại có ít nhất một tin từ người khác mà User chưa đọc theo trạng thái đã xác nhận, trong cùng tập được phép liệt kê; tiếp tục bước 3. Tin do chính User gửi không tự làm hội thoại thành chưa đọc.
- **Alt 2b — Nhóm**: Thay bước 2, User chọn Nhóm. Chỉ giữ hội thoại nhóm E2EE được phép liệt kê, không đưa chat riêng vào kết quả; tiếp tục bước 3. Không yêu cầu có tin chưa đọc hoặc thành viên khác đang online.
- **Alt 2c — Đổi bộ lọc**: User chuyển giữa Tất cả, Chưa đọc và Nhóm; xác định lại phạm vi từ tập hội thoại được phép, quay bước 2–3. Đổi điều kiện chỉ thay kết quả tra cứu, không sửa trạng thái đọc hay quan hệ. Ba giá trị là các nhánh của cùng mục tiêu, không là ba UC độc lập.
- **Alt 3a — Không có kết quả phù hợp**: Danh sách rỗng là kết quả hợp lệ khi phạm vi đã được xét đầy đủ. Không có tin chưa đọc hoặc không có nhóm không đồng nghĩa User không có hội thoại; User có thể đổi bộ lọc hoặc kết thúc.
- **Alt 3b — Có cập nhật khi đang xem**: Khi có tin mới, cập nhật mốc đọc, trạng thái hiện diện, thành viên nhóm hoặc quyền liệt kê, đối chiếu lại hội thoại bị ảnh hưởng và giữ điều kiện hiện tại. Trạng thái nhóm chuyển sang Online khi có ít nhất một thành viên online; chỉ chuyển sang Offline khi đã xác nhận không còn thành viên online. Với Chưa đọc, chỉ thêm/bớt mục theo trạng thái đọc đã xác nhận; không đánh dấu đã đọc vì mục vừa xuất hiện trong danh sách.
- **Alt 3c — Danh sách hoặc trạng thái chưa đồng bộ đủ**: Cho xem phần metadata được phép và đã có, nêu rõ phần chưa hoàn tất. Khi dữ liệu sẵn sàng, cập nhật theo cùng điều kiện; không coi trạng thái chưa biết là không có tin chưa đọc hay đối phương offline.
- **Alt 3d — Chọn hội thoại để đọc**: Sau khi nhận danh sách, User chọn một hội thoại; kết thúc tra cứu và dẫn sang [UC-U-MSG-01](../messaging/view-conversation-history.md). Quyền đọc, khóa E2EE và ghi nhận đã xem được kiểm tra trong mục tiêu đọc; xuất hiện hoặc được chọn trong danh sách không thay thế các điều kiện đó.

### Exception Flows

- **Ex 1a — Phiên không hợp lệ**: Dừng truy vấn/đồng bộ cần xác thực; không trả danh sách dựa trên quyền của phiên cũ hoặc dùng kết quả đó cho người đăng nhập sau.
- **Ex 1b — Lỗi tải hoặc mất kết nối**: Giữ bộ lọc và phần dữ liệu đã xác nhận nếu vẫn được phép xem; thông báo chưa hoàn tất và cho đối chiếu/thử lại. Không kết luận không có hội thoại từ lỗi tải hoặc đổi toàn bộ trạng thái hiện diện thành Offline.
- **Ex 2a — Quyền hoặc nguồn truy vấn không hợp lệ**: Không cho User đọc danh sách của tài khoản khác bằng dữ liệu client truyền. Từ chối truy vấn không hợp lệ thay vì âm thầm chuyển sang một tài khoản/phạm vi khác.
- **Ex 3a — Hội thoại không còn được phép liệt kê**: Loại khỏi kết quả hoặc giới hạn thông tin theo quyền mới, không mở rộng quyền từ bản danh sách cũ. Nhóm đã rời/bị xóa/đóng tuân theo phạm vi giữ lịch sử còn Proposed tại DEC-05, không tự coi mọi metadata/lịch sử luôn được truy cập.
- **Ex 3b — Trạng thái hiện diện không xác định hoặc đã cũ**: Phân biệt chưa biết/chưa cập nhật với Offline; không suy ra đã nhận/đã xem tin hoặc quyền gửi từ trạng thái hiện diện. Với nhóm, chỉ cần một thành viên được xác nhận online là đủ hiển thị Online dù còn thiếu trạng thái của người khác. Nếu chưa xác nhận được người nào online nhưng vẫn còn trạng thái thành viên chưa biết/đã cũ, chưa kết luận nhóm Offline.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - BR-01 — Phạm vi danh sách là các hội thoại của chính User được phép liệt kê, gồm chat riêng và nhóm E2EE; không giới hạn vào các tài khoản đang là bạn bè.
  - BR-02 — Mỗi lần tra cứu áp dụng một phạm vi Tất cả, Chưa đọc hoặc Nhóm. Tất cả gồm cả hai loại hội thoại trong phạm vi quyền; Chưa đọc xét tin User nhận mà chưa đọc; Nhóm chỉ xét loại nhóm. Không tự đổi điều kiện khi không có kết quả.
  - BR-03 — Chưa đọc được xác định riêng theo User, không dùng số người chưa đọc chung của nhóm. Tin do User gửi, trạng thái online, nhận thông báo, tải metadata hay mở danh sách không làm phát sinh xác nhận đã xem. Tin nhóm chưa giải mã không được tự coi là đã đọc; trạng thái còn thiếu phải được ghi là chưa đủ dữ liệu. Việc cập nhật sau đọc thuộc UC-U-MSG-01 và được phản ánh lại vào danh sách.
  - BR-04 — Mỗi hội thoại có một mục theo định danh, không hợp nhất các hội thoại khác nhau chỉ vì trùng tên. Thông tin nhận diện và hiện diện không thay thế kiểm tra quyền tại lúc mở lịch sử hoặc gửi tin; không tải bản rõ nhóm hoặc yêu cầu khóa bí mật chỉ để lọc metadata danh sách.
  - BR-05 — Chat riêng dùng Online/Offline của đối phương khi có dữ liệu hiện diện hợp lệ. Nhóm hiển thị Online khi có ít nhất một thành viên trong nhóm online, không yêu cầu tất cả thành viên online; không tính người đã rời hoặc bị xóa khỏi nhóm. Nhóm chỉ hiển thị Offline khi đã xác nhận không có thành viên online; thiếu dữ liệu không đồng nghĩa Offline.
  - BR-06 — Thứ tự danh sách, cách xếp lại khi có hoạt động mới, phân trang/kích thước lô và hành vi ghi nhớ bộ lọc chưa được nguồn quy định.
  - BR-07 — Đọc lịch sử, gửi tin, tìm nội dung và tạo nhóm là các mục tiêu được dẫn chiếu, không lồng vào mục tiêu xem/lọc danh sách. Có thể kết thúc ngay sau khi nhận danh sách.
- **Non-Functional Requirements (NFR)**:
  - NFR-01 — Phản hồi bất đồng bộ gắn đúng User/phiên/phạm vi; phản hồi cũ không ghi đè bộ lọc mới hoặc đưa dữ liệu người khác vào danh sách. Giữ bản nháp hội thoại đang mở khi tra cứu/đổi phạm vi.
  - NFR-02 — Kiểm tra quyền liệt kê tại server; chỉ cung cấp metadata/hiện diện được phép. Không gửi khóa E2EE hoặc bản rõ tin nhóm cho server để phục vụ bộ lọc danh sách thông thường.
  - NFR-03 — Phân biệt tải/chưa hoàn tất/lỗi với danh sách thật sự rỗng, và trạng thái hiện diện chưa biết với Offline. Chưa có ngưỡng độ trễ riêng của danh sách hội thoại được chốt; không áp ngưỡng tìm tin hoặc tải lịch sử của DEC-09 như cam kết đã đo cho UC này.
