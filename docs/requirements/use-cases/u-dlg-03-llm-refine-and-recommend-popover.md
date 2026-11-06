# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-DLG-03
- **Use Case Name**: Chỉnh sửa bản nháp và gợi ý tin nhắn bằng LLM
- **Author / Create by**: Kiet
- **Date Create**: 01/11/2026
- **Last Updated by** / Date: Kiet / 01/11/2026
- **Primary Actor**: User đã đăng nhập và muốn được hỗ trợ soạn tin.
- **Secondary Actor(s)**: Dịch vụ LLM (Muse API theo DEC-08), được gọi thông qua server ứng dụng.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Desciption)

- **Brief Description**: User mở popover từ một trong hai nút AI độc lập trên thanh soạn tin: **Chỉnh sửa (Refine)** để viết lại bản nháp, hoặc **Gợi ý (Recommend)** để tạo tin nhắn từ mô tả ý định (prompt) của User, các tin ngữ cảnh do User chọn hoặc kết hợp cả hai. Gợi ý hỗ trợ cả việc chủ động mở lời khi hội thoại chưa có tin nhắn. Trước mỗi yêu cầu, User được xem trước, sửa và xác nhận nội dung bản rõ gửi tới server ứng dụng và nhà cung cấp AI, kể cả trong nhóm E2EE. Kết quả chỉ được đưa vào ô soạn khi User chọn sử dụng.
- **Goal**: Có nội dung nháp phù hợp để User kiểm tra, chỉnh sửa và tự quyết định gửi trong hội thoại hiện tại.
- **Trigger**: User nhấn **Chỉnh sửa** hoặc **Gợi ý** trên thanh soạn tin của `U-SCR-07`.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - User đã đăng nhập bằng phiên hợp lệ và đang mở hội thoại cá nhân hoặc nhóm trong `U-SCR-07` có thanh soạn tin khả dụng.
  - Với Chỉnh sửa: ô soạn có nội dung văn bản, không rỗng hoặc chỉ gồm khoảng trắng.
  - Với Gợi ý: không bắt buộc hội thoại đã có tin nhắn hoặc ô soạn đã có bản nháp. User có thể nhập prompt mô tả ý định, chọn các tin văn bản được phép đọc làm ngữ cảnh hoặc kết hợp cả hai; trước khi gửi yêu cầu phải có ít nhất một nguồn đầu vào hợp lệ.
  - Luồng cơ bản có kết nối tới server và dịch vụ LLM đã được cấu hình, có quyền sử dụng và còn hạn mức.
  - Nội dung lấy từ nhóm E2EE phải được giải mã trên thiết bị của User trước khi đưa vào phần xem trước; không gửi khóa giải mã cho server hoặc dịch vụ LLM.
- **Post-conditions**:
  - Khi sinh nội dung thành công: popover hiển thị các phương án tương ứng với chế độ và nội dung đã xác nhận.
  - Khi User chọn **Sử dụng**: phương án được điền vào ô soạn của đúng hội thoại nguồn; chưa tạo hoặc gửi tin nhắn cho người nhận.
  - Bản nháp gốc được giữ nguyên cho đến khi User chủ động áp dụng kết quả; nếu bản nháp đã thay đổi trong lúc chờ, phải xử lý theo Alt 6b trước khi thay thế.
  - Hủy, lỗi, hết hạn mức hoặc đóng popover khi chưa áp dụng không thay đổi bản nháp và lịch sử hội thoại.
  - Không xác nhận gửi dữ liệu cho AI thì không gửi nội dung nháp/prompt/ngữ cảnh tới server hoặc nhà cung cấp AI qua chức năng này.
  - Đóng popover sau khi đã gửi yêu cầu không đồng nghĩa thu hồi được dữ liệu đã gửi; kết quả trả muộn không tự điền vào ô soạn.

## Luồng sự kiện (Flow of Events)

### Basic Flow

Luồng cơ bản: chỉnh sửa bản nháp và sử dụng một phương án viết lại trong cùng hội thoại, khi bản nháp chưa thay đổi trong lúc chờ.

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Nhấn **Chỉnh sửa (Refine)** khi ô soạn có chữ. | Mở `U-DLG-03` ở chế độ Chỉnh sửa, lấy bản sao nội dung nháp và ghi nhận hội thoại nguồn. Ô soạn gốc vẫn giữ nguyên. |
| 2 | Chọn kiểu diễn đạt mong muốn, ví dụ tự nhiên, lịch sự hoặc ngắn gọn; xem và sửa nội dung đầu vào nếu cần. | Hiển thị bản xem trước nội dung sẽ gửi và kiểu diễn đạt đã chọn. Chỉ dùng bản nháp đã xem trước, không tự thêm lịch sử hội thoại. Việc sửa trong phần xem trước chưa thay đổi ô soạn gốc. |
| 3 | Đọc thông báo chia sẻ dữ liệu và nhấn **Xác nhận & tạo**. | Nêu rõ nội dung bản rõ sẽ được gửi tới server ứng dụng và nhà cung cấp AI; kể cả nhóm E2EE, phần nội dung này được chia sẻ ra ngoài luồng E2EE. Kiểm tra đầu vào hợp lệ rồi gửi đúng nội dung đã xác nhận, hiển thị đang xử lý và ngăn gửi lặp khi yêu cầu còn chờ. |
| 4 | Chờ kết quả. | Server xác thực phiên, kiểm tra đầu vào/hạn mức và gọi dịch vụ LLM bằng thông tin xác thực lưu ở server. Khi thành công, trả các phương án viết lại gắn với yêu cầu hiện tại; client hiển thị trong popover, chưa thay đổi ô soạn. |
| 5 | Xem các phương án và chọn một phương án muốn dùng. | Đánh dấu phương án đã chọn, cho đọc toàn bộ nội dung và nhắc User kiểm tra trước khi sử dụng. |
| 6 | Nhấn **Sử dụng**. | Kiểm tra popover còn thuộc hội thoại nguồn, thanh soạn vẫn khả dụng và bản nháp chưa thay đổi; thay nội dung văn bản trong ô soạn bằng phương án đã chọn, đóng popover và đưa con trỏ về ô soạn. |
| 7 | Kiểm tra, chỉnh sửa thêm nếu cần. | Giữ nội dung ở trạng thái nháp. Việc nhấn **Gửi** sau đó thuộc luồng gửi tin của `U-SCR-07` và vẫn chịu các kiểm tra quyền, quan hệ chặn, kết nối và E2EE tương ứng. |

### Alternative Flows

#### Alt 1a — Gợi ý trả lời từ các tin ngữ cảnh

Thay bước 1–2:

1. User nhấn **Gợi ý (Recommend)**; ô soạn có thể đang trống hoặc có bản nháp.
2. Hệ thống mở popover ở chế độ Gợi ý, có ô nhập prompt mô tả ý định và phần chọn tin ngữ cảnh; User chọn một hoặc nhiều tin văn bản trong hội thoại hiện tại mà mình được phép đọc.
3. User chọn các tin cần làm ngữ cảnh. Hệ thống hiển thị phần xem trước theo thứ tự hội thoại, phân biệt lượt nói để User hiểu nội dung sẽ gửi.
4. User có thể bỏ chọn tin, sửa phần văn bản trong bản xem trước hoặc nhập thêm prompt để nêu mục đích và giọng điệu mong muốn. Thao tác này không sửa tin nhắn trong lịch sử.
5. Hệ thống không tự đưa toàn bộ hội thoại hoặc bản nháp đang có vào yêu cầu Gợi ý. Chỉ nội dung ngữ cảnh được chọn và prompt do User nhập, đã được xem trước và xác nhận, mới được gửi.
6. User tiếp tục từ bước 3. Kết quả ở bước 4 là các câu trả lời gợi ý; bước 5–7 áp dụng tương tự luồng cơ bản.

#### Alt 1b — Gợi ý mở lời từ prompt khi chưa có tin nhắn

Thay bước 1–2:

1. User mở hội thoại chưa có tin nhắn và nhấn **Gợi ý (Recommend)**; nút vẫn khả dụng khi ô soạn trống.
2. Hệ thống mở popover với ô **Mô tả điều bạn muốn nhắn** và thông báo chưa có tin ngữ cảnh; không bắt buộc chọn tin nhắn để tiếp tục.
3. User nhập prompt nêu đối tượng, ý định, các ý cần nói và giọng điệu mong muốn. Ví dụ: **Tôi là sinh viên lớp Java, muốn chủ động nhắn cho giảng viên để xin lịch trao đổi về đồ án. Hãy gợi ý lời mở đầu lịch sự, ngắn gọn.**
4. Hệ thống hiển thị bản xem trước prompt để User kiểm tra và sửa. Khi prompt có nội dung và không chỉ gồm khoảng trắng, cho phép xác nhận tạo gợi ý mà không cần tin ngữ cảnh.
5. User tiếp tục từ bước 3 của luồng cơ bản. Hệ thống chỉ gửi prompt đã xác nhận cùng chế độ/kiểu diễn đạt cần thiết; không tự bổ sung dữ liệu hồ sơ người nhận hoặc lịch sử khác.
6. Kết quả ở bước 4 là các phương án mở lời theo ý định đã mô tả; bước 5–7 áp dụng tương tự luồng cơ bản.
7. Luồng dùng prompt cũng khả dụng khi hội thoại đã có tin nhắn nhưng User không muốn chọn tin làm ngữ cảnh.

#### Alt 2a — Đổi chế độ hoặc sửa đầu vào trước khi xác nhận

Tại bước 1–3:

1. User quay lại thay đổi nội dung xem trước, prompt, lựa chọn ngữ cảnh hoặc kiểu diễn đạt; nếu chọn nút AI còn lại thì chuyển sang chế độ tương ứng.
2. Hệ thống cập nhật bản xem trước theo chế độ hiện tại, không tự gửi dữ liệu.
3. User phải xác nhận nội dung mới trước khi tạo kết quả. Kết quả của đầu vào hoặc chế độ cũ, nếu có, không được coi là kết quả cho lựa chọn mới.

#### Alt 3a — Không đồng ý chia sẻ nội dung hoặc đóng popover

Trước khi gửi yêu cầu ở bước 3:

1. User nhấn **Hủy**, đóng popover hoặc nhấn Escape.
2. Hệ thống đóng popover, không gửi nội dung tới server/AI và giữ nguyên ô soạn.

#### Alt 4a — Đóng popover hoặc chuyển hội thoại khi đang chờ

Tại bước 4:

1. User đóng popover hoặc chuyển sang hội thoại khác.
2. Hệ thống ngừng hiển thị yêu cầu đó trong popover hiện tại, giữ bản nháp theo hội thoại nguồn và hủy xử lý đang chờ nếu có thể.
3. Không cam kết dữ liệu đã gửi được thu hồi hoặc lời gọi AI chắc chắn chưa bị tính hạn mức.
4. Kết quả trả về sau đó không tự mở lại popover, thay bản nháp hoặc điền vào hội thoại khác.

#### Alt 5a — Tạo lại khi kết quả chưa phù hợp

Tại bước 5:

1. User chọn **Tạo lại**, hoặc quay lại sửa đầu vào/prompt/kiểu diễn đạt/ngữ cảnh.
2. Hệ thống cho xem lại nội dung sẽ gửi và thông báo đây là yêu cầu AI mới, chịu hạn mức hiện tại.
3. User xác nhận lại trước khi gửi. Trong lúc chờ, không cho áp dụng kết quả cũ như thể đó là kết quả của yêu cầu mới.
4. Nếu lần tạo lại thất bại, bản nháp trong ô soạn vẫn giữ nguyên; User có thể đóng popover và tiếp tục soạn thủ công.

#### Alt 6a — Không sử dụng phương án nào

Tại bước 5–6:

1. User xem kết quả nhưng chọn **Hủy** hoặc đóng popover.
2. Hệ thống giữ nguyên bản nháp gốc, không tạo tin nhắn trong lịch sử.

#### Alt 6b — Bản nháp đã thay đổi trong lúc chờ

Tại bước 6:

1. User đã sửa nội dung ô soạn sau khi mở popover hoặc gửi yêu cầu AI.
2. Khi User nhấn **Sử dụng**, hệ thống không tự ghi đè bản nháp hiện tại; hiển thị nội dung hiện tại và phương án đã chọn, yêu cầu xác nhận thay thế.
3. Nếu User xác nhận, thay nội dung văn bản bằng phương án đã chọn. Nếu hủy, giữ nguyên bản nháp hiện tại và cho tiếp tục xem kết quả.
4. Nếu bản nháp lại thay đổi trước khi xác nhận thay thế, phải đối chiếu lại; không dùng xác nhận cũ để ghi đè nội dung mới.

#### Alt 2b — Ngữ cảnh đã chọn thay đổi trước khi gửi

Tại bước 2–3 của chế độ Gợi ý:

1. Tin đã chọn vừa bị thu hồi, bị xóa phía mình hoặc không còn được phép đọc tại thời điểm chuẩn bị gửi.
2. Hệ thống loại nội dung không còn hợp lệ khỏi bản xem trước và thông báo cho User kiểm tra lại lựa chọn.
3. Nếu không còn ngữ cảnh hợp lệ nhưng prompt vẫn hợp lệ, User có thể tiếp tục chỉ với prompt sau khi xem và xác nhận lại. Nếu cả prompt và ngữ cảnh đều không hợp lệ, không gửi yêu cầu và hướng dẫn nhập prompt hoặc chọn lại tin; mọi nội dung đã cập nhật phải được xem và xác nhận lại trước khi gửi.
4. Việc thu hồi sau khi yêu cầu đã gửi không bảo đảm xóa được bản rõ đã chuyển tới dịch vụ AI.

### Exception Flows

- **Ex 1a — Chưa có bản nháp hợp lệ**: Nút Chỉnh sửa không khả dụng khi ô soạn rỗng/chỉ có khoảng trắng. Nếu đầu vào trở thành rỗng sau khi sửa bản xem trước, hiển thị lỗi tại phần nhập và không gửi yêu cầu.
- **Ex 1b — Chưa có đầu vào Gợi ý hợp lệ**: Chỉ ngăn gửi yêu cầu khi prompt rỗng/chỉ gồm khoảng trắng và không có tin ngữ cảnh hợp lệ; hướng dẫn User nhập mô tả ý định hoặc chọn ít nhất một tin văn bản có thể đọc. Không dùng tin đã thu hồi, bị xóa phía mình, chưa giải mã được hoặc nội dung của hội thoại khác. Nếu lịch sử đang tải, phân biệt với trường hợp thực sự chưa có tin; User vẫn có thể nhập prompt và tạo gợi ý mà không cần chờ lịch sử tải xong.
- **Ex 3a — Đầu vào vượt giới hạn**: Client hướng dẫn rút gọn bản xem trước hoặc bớt tin ngữ cảnh; server kiểm tra lại theo cấu hình thực tế. Không tự cắt nội dung và gửi một bản khác với bản User đã xác nhận.
- **Ex 3b — Mất kết nối, lỗi mạng hoặc hết thời gian chờ**: Thông báo không lấy được kết quả, giữ bản nháp và đầu vào xem trước còn phù hợp. Không tự gọi lại AI; User có thể thử lại sau khi xem và xác nhận. Kết quả của yêu cầu đã hết thời gian chờ không thay thế kết quả yêu cầu mới.
- **Ex 4a — Hết hạn mức hoặc dịch vụ giới hạn tần suất**: Thông báo AI tạm thời không khả dụng, chỉ hiển thị thời điểm thử lại nếu dịch vụ cung cấp. Giữ nguyên bản nháp và cho tiếp tục chat thủ công.
- **Ex 4b — Chưa cấu hình dịch vụ hoặc nhà cung cấp gặp lỗi**: Thông báo chức năng AI chưa sẵn sàng hoặc đang lỗi; không hiển thị API key hay chi tiết xác thực. Không chuyển sang dùng cookie/token cá nhân để thay thế.
- **Ex 4c — Không có kết quả sử dụng được**: Nếu dịch vụ từ chối, trả rỗng hoặc dữ liệu sai định dạng, hiển thị thông báo phù hợp; không đưa thông báo lỗi vào ô soạn như một câu trả lời. User có thể sửa đầu vào và xác nhận tạo lại hoặc tiếp tục soạn thủ công.
- **Ex 6a — Hội thoại hoặc thanh soạn không còn khả dụng**: Nếu User đã rời/bị xóa khỏi nhóm, nhóm đã đóng hoặc thanh soạn bị thay bằng thao tác Bỏ chặn, không điền kết quả vào ô soạn. Thông báo trạng thái đã thay đổi, không áp dụng sang hội thoại khác.
- **Ex 1c — Phiên không còn hợp lệ**: Ở bất kỳ bước nào, nếu phiên bị thu hồi hoặc tài khoản bị khóa, dừng yêu cầu cần xác thực và chuyển về luồng đăng nhập; kết quả trả muộn không tự áp dụng vào phiên mới.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

#### BR-01 — Hai chế độ độc lập

- Có hai nút riêng trên thanh soạn tin: **Chỉnh sửa (Refine)** và **Gợi ý (Recommend)**, mở đúng chế độ của `U-DLG-03`.
- Chỉnh sửa dùng bản nháp và kiểu diễn đạt đã chọn; Gợi ý dùng prompt mô tả ý định, các tin ngữ cảnh do User chọn trong hội thoại hiện tại hoặc kết hợp cả hai.
- Nút Gợi ý vẫn khả dụng khi hội thoại và ô soạn đều trống. Trước khi gửi yêu cầu, phải có prompt không rỗng/không chỉ gồm khoảng trắng hoặc ít nhất một tin ngữ cảnh hợp lệ; không bắt buộc đồng thời có cả hai.
- Cả hai chế độ áp dụng cho chat cá nhân và nhóm có thanh soạn khả dụng. Không mặc định cần quyền Admin nhóm.

#### BR-02 — Phạm vi nội dung gửi AI

- Chỉ gửi nội dung của bản nháp ở chế độ Chỉnh sửa, hoặc prompt và/hoặc phần ngữ cảnh ở chế độ Gợi ý, đã được xem trước, sửa và xác nhận, cùng chế độ/kiểu diễn đạt cần thiết.
- Không tự gửi toàn bộ lịch sử, danh bạ, hồ sơ tài khoản hoặc các hội thoại khác. Không tự bổ sung tin mới đến sau khi User xác nhận.
- Phân biệt lượt nói bằng thông tin tối thiểu cần thiết; không tự đính kèm username, email hoặc định danh tài khoản không cần cho việc sinh câu trả lời.
- Sửa bản xem trước chỉ sửa dữ liệu cho yêu cầu AI, không sửa lịch sử hay bản nháp gốc.

#### BR-03 — Xem trước và xác nhận chia sẻ bản rõ

- Mở popover, nhập prompt hoặc chọn tin ngữ cảnh chưa được xem là đồng ý gửi dữ liệu.
- Trước mỗi yêu cầu, kể cả tạo lại/thử lại, phải có bước xem trước và xác nhận rõ ràng nội dung gửi tới **server ứng dụng và nhà cung cấp AI**.
- Với nhóm E2EE, phải nêu rõ phần nội dung được chọn sẽ được chia sẻ dưới dạng bản rõ ra ngoài luồng E2EE; không mô tả yêu cầu AI như vẫn chỉ có các thành viên nhóm đọc được.
- Nếu đầu vào thay đổi sau khi xem trước, phải cập nhật phần xem trước và lấy xác nhận cho nội dung mới trước khi gửi.
- Không gửi khóa bí mật hoặc cung cấp khả năng giải mã lịch sử cho server/AI.

#### BR-04 — Sử dụng kết quả

- Kết quả AI là nội dung gợi ý, không được coi là thông tin đã xác minh; User phải có cơ hội đọc và sửa trước khi gửi.
- Chỉ khi User chọn một phương án và nhấn **Sử dụng** thì hệ thống mới thay nội dung văn bản trong ô soạn; không tự nối thêm hoặc tự chọn phương án đầu tiên.
- Không tự gửi tin, tạo dòng lịch sử chat hoặc thông báo cho người nhận khi mở popover, tạo kết quả hay áp dụng vào bản nháp.
- Nội dung sau khi áp dụng vẫn chịu quy tắc kiểm tra và mã hóa của luồng gửi tin; có kết quả AI không đồng nghĩa tin chắc chắn gửi được.

#### BR-05 — Bảo toàn bản nháp và hội thoại nguồn

- Gắn mỗi yêu cầu/kết quả với đúng hội thoại nguồn, chế độ và nội dung đã xác nhận.
- Không tự ghi đè bản nháp khi đang chờ, khi lỗi hoặc khi kết quả trả muộn. Bản nháp đã đổi phải được xác nhận thay thế theo Alt 6b.
- Đóng popover, chuyển hội thoại hoặc kết thúc phiên làm mất hiệu lực áp dụng tự động của kết quả đang chờ; không chuyển nội dung sang ô soạn khác.
- Quan hệ chặn giữa các thành viên nhóm không tự vô hiệu hóa AI nếu thanh soạn nhóm vẫn khả dụng. Trong chat cá nhân, AI không làm thay đổi hành vi chặn của `U-SCR-07`.

#### BR-06 — Dịch vụ, hạn mức và thử lại

- Theo DEC-08, server tích hợp Muse API cho cả hai chế độ; API key chỉ ở server, không nằm trong User Client hoặc Admin App.
- Server kiểm tra phiên, đầu vào và hạn mức trước khi gọi dịch vụ; client ngăn thao tác gửi lặp khi một yêu cầu đang chờ trong popover.
- Không tự thử lại lời gọi AI sau lỗi/timeout. Một lần thử lại do User xác nhận có thể phát sinh chi phí mới; không cam kết nhà cung cấp chưa xử lý yêu cầu cũ.
- Lỗi AI hoặc hết hạn mức không chặn chức năng soạn và gửi tin thủ công theo quyền hiện tại.
- Không dùng cookie/token ChatGPT web hoặc token Codex cá nhân làm API chung.

#### BR-07 — Các thông số chưa chốt

- Giới hạn **5 tin ngữ cảnh**, **3 gợi ý**, **timeout 15 giây** và quota trong DEC-08 hiện là đề xuất, chưa phải quy tắc đã được xác nhận.
- Ngân sách mục tiêu **2 USD/tháng**, tách hosting, không đồng nghĩa hạn mức cố định cho từng User hoặc cam kết số yêu cầu được phục vụ.
- Model cụ thể, quyền truy cập, billing, chất lượng tiếng Việt, giới hạn đầu vào/đầu ra, chính sách sử dụng/lưu dữ liệu và hạn mức thực tế cần được chốt theo DEC-08 trước khi nghiệm thu tích hợp thật.

#### BR-08 — Lưu ý chưa chốt: đánh dấu nội dung có sự hỗ trợ từ AI

- **Câu hỏi cần thảo luận**: Nội dung được Gợi ý hoặc Chỉnh sửa bằng AI có nên mang nhãn/watermark **Có sự hỗ trợ từ AI** hay không? Cần phân biệt nội dung AI đề xuất với bản nháp do User viết và được AI hỗ trợ chỉnh sửa.
- Cần chốt phạm vi hiển thị: chỉ trên kết quả trong popover, trên bản nháp để người soạn nhận biết, hay kèm tin nhắn đã gửi để người nhận cũng thấy. Phân biệt nhãn giao diện với watermark/chữ ký được chèn trực tiếp vào nội dung tin nhắn.
- Nếu áp dụng, cần chốt nhãn bắt buộc hay tùy chọn, User có thể gỡ nhãn không và cách xử lý khi User tiếp tục sửa nội dung sau khi sử dụng kết quả AI.
- Đây là lưu ý cho quyết định thiết kế tiếp theo, chưa phải yêu cầu bắt buộc gắn watermark hoặc tự thêm chữ vào tin nhắn.

### Non-Functional Requirements (NFR)

#### NFR-01 — Phản hồi giao diện

- Gọi AI không làm đóng băng JavaFX; User có thể đóng popover và tiếp tục thao tác trong ứng dụng.
- Phân biệt các trạng thái: chuẩn bị đầu vào, chờ xác nhận, đang xử lý, có kết quả, không có kết quả, lỗi và hết hạn mức.
- Cho đọc toàn bộ bản xem trước và từng phương án, kể cả nội dung dài; không che phần dữ liệu sẽ gửi sau một bản tóm tắt không thể mở xem.

#### NFR-02 — Bảo mật và quyền riêng tư

- Truyền dữ liệu qua kết nối bảo mật theo kiến trúc HTTPS của hệ thống; việc dùng HTTPS không thay thế thông báo chia sẻ bản rõ tới server/AI.
- Không ghi API key, khóa E2EE hoặc nội dung nháp/prompt/ngữ cảnh/kết quả vào log vận hành thông thường. Thông báo lỗi không làm lộ thông tin xác thực.
- Chỉ đưa nội dung User đang được phép đọc vào phần chọn ngữ cảnh; không nhờ server tự giải mã hoặc tự lấy thêm lịch sử để gửi AI.
- Hiển thị đầu ra AI như văn bản; không thực thi mã, mở liên kết hoặc thực hiện thao tác tài khoản chỉ vì có chỉ dẫn trong ngữ cảnh hay kết quả.

#### NFR-03 — Nhất quán khi xử lý bất đồng bộ

- Đối chiếu yêu cầu hiện tại trước khi hiển thị kết quả; phản hồi cũ không được ghi đè phản hồi của yêu cầu mới.
- Kiểm tra hội thoại nguồn, phiên, trạng thái thanh soạn và phiên bản bản nháp trước khi áp dụng.
- Hủy/đóng popover và timeout phải kết thúc trạng thái chờ trên giao diện, kể cả khi nhà cung cấp chưa dừng xử lý.

#### NFR-04 — Chất lượng và thời gian phản hồi

- Kiểm tra khả năng chỉnh sửa và gợi ý tiếng Việt theo ngữ cảnh; nội dung lỗi, rỗng hoặc sai định dạng không được trình bày như kết quả thành công.
- Chỉnh sửa cần giữ ý chính của bản nháp theo kiểu diễn đạt được chọn; Gợi ý cần phù hợp prompt và/hoặc các tin ngữ cảnh đã xác nhận, kể cả khi chỉ có prompt để chủ động mở lời. Không cam kết mọi kết quả AI luôn chính xác.
- Thời gian chờ và giới hạn sử dụng phải có cấu hình và thông báo phù hợp; ngưỡng 15 giây vẫn là đề xuất theo DEC-08, chưa coi là số đo hoặc cam kết nghiệm thu.
