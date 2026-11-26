# Kế hoạch triển khai Version 2 (ChatSystem)

Hoàn thiện phần xử lý cho User Client và Admin Console, kết nối với Server cùng PostgreSQL trên nền giao diện và CSDL của Version 1. Phạm vi Version 2 (80%) gồm `U-01` đến `U-27`, `A-01` đến `A-16` và `X-02` đến `X-05`. Mốc Version 2 là **12/12/2026** theo DEC-09, còn các mốc từng giai đoạn dưới đây là đề xuất. [Roadmap](../../../ROADMAP.md) · [DEC-09](../../decisions/performance-and-acceptance.md) · [Đề bài, trang 4-5](../Final%20Project.pdf)

[Phân công cho ba thành viên](#phân-công-cho-ba-thành-viên)

---

## Giai đoạn 1: Rà UI, viết feature spec và chốt API contract đợt đầu (đến 25/11)

- [ ] **1.1. Kiểm kê các màn hình Version 1**
  - Rà soát FXML, controller và dữ liệu mẫu của 14 màn hình/hộp thoại User Client và 11 màn hình/hộp thoại Admin Console. Với từng ô nhập, nút, bảng, bộ lọc hay biểu đồ, ghi rõ mã màn hình, Feature ID và use case tương ứng, dữ liệu cần đọc/ghi, cách xử lý (cục bộ, REST hay WebSocket), trạng thái tải, trạng thái lỗi và ca nghiệm thu. [Danh mục màn hình](../../requirements/screen.md) · [Kiến trúc desktop](../../architecture/desktop-ui.md)
  - Đối chiếu nhu cầu dữ liệu với schema hiện tại để xác định phần cần bổ sung cho cơ chế gửi lại tin, trạng thái đã nhận/đã đọc và mốc xóa lịch sử. Khi đổi DDL, cập nhật script, ERD và test SQL trong cùng một PR. [Schema CSDL](../../../script/schema.sql) · [Quy trình SQL](../../databases/workflow.md)
- [ ] **1.2. Viết feature spec theo cụm chức năng**
  - Ưu tiên viết trước spec cho đăng nhập, tải màn hình đầu tiên và chat 1-1, đặt tại vị trí đề xuất `docs/specs/<nhom>/spec.md`. Mỗi spec nêu rõ người thao tác, điều kiện, luồng chính, ngoại lệ, phân quyền, trạng thái UI, tiêu chí chấp nhận và dẫn chiếu tới use case, DEC hiện có.
  - Chốt các mục còn mở trước khi duyệt spec liên quan, gồm quyền chat và mời nhóm khi một bên đã chặn, điều kiện gửi lại email và thao tác đồng thời trên tài khoản. Giao một người phụ trách và một người review cho từng cụm. [Bạn bè và chặn](../../decisions/blocking-and-friendship.md) · [Email và mật khẩu](../../decisions/password-reset-and-email.md) · [Vòng đời tài khoản](../../decisions/account-lifecycle.md)
- [ ] **1.3. Chốt API contract giữa client và Server**
  - Soạn OpenAPI cho HTTPS REST và tài liệu WebSocket kèm JSON schema cùng ví dụ payload tại vị trí đề xuất `docs/architecture/contracts/`. Với REST, chốt rõ request/response, quyền theo phiên, mã lỗi, quy tắc lọc/sắp xếp/phân trang, ID và cách xử lý khi gọi lặp. Với WebSocket, chốt loại sự kiện, phạm vi người nhận, thứ tự, ACK và cơ chế đồng bộ bù sau khi kết nối lại. Cả client lẫn Server dùng chung một bộ dữ liệu mẫu để kiểm thử. [Tech stack](../../decisions/tech-stack.md)
  - Lấy `U-SCR-07` làm mẫu truy vết: lịch sử cần phân trang 50 tin bằng cursor và hỗ trợ tải đoạn tin quanh `messageId` để nhảy tới kết quả tìm kiếm. Endpoint đề xuất là `GET /api/v1/conversations/{id}/messages?before=...&limit=50`, trả về `items` và `nextCursor` sau khi kiểm tra quyền đọc. Riêng `A-SCR-03`, tách dữ liệu bảng lọc/sắp xếp khỏi phần hồ sơ và các tab chi tiết theo `userId`. [Use case xem lịch sử](../../requirements/use-cases/user/messaging/UC-USR-MSG-01-view-conversation-history.md)
- [ ] **1.4. Bắt đầu thử E2EE, kết nối SMTP/LLM và build JAR trên Windows**

---

## Giai đoạn 2: Dựng Server, skeleton loading và luồng tích hợp đầu tiên (đến 29/11)

- [ ] **2.1. Khởi chạy Server với PostgreSQL (`X-02`, `X-03`)**
  - Dựng dự án Spring Boot, kết nối PostgreSQL qua file cấu hình ngoài, tách phạm vi phiên User/Admin và mở HTTPS REST cùng WSS. Server kiểm tra quyền cả khi nhận yêu cầu lẫn khi phát sự kiện.
  - Đặt DTO và mô hình giao thức trong `src/common/`. Nối controller JavaFX qua lớp service phía client để chuyển sang dữ liệu thật theo từng màn hình, nhưng vẫn giữ dữ liệu mẫu cho preview và kiểm thử.
- [ ] **2.2. Tạo skeleton loading dùng chung trong `src/desktop-ui/`**
  - **User Client:** Hiện skeleton khi tải lần đầu danh sách hội thoại, lịch sử tin nhắn, danh sách bạn và kết quả tìm kiếm. Khi tải thêm trang tin cũ, hiển thị chỉ báo ở đầu khung tin và giữ nguyên vị trí cuộn. Tin đang gửi hiện trạng thái chờ, kèm thao tác thử lại khi cần.
  - **Admin Console:** Hiện skeleton riêng cho thẻ KPI, bảng, biểu đồ và khung chi tiết. Khi chọn hàng khác, chỉ tải lại khung chi tiết trong lúc bảng bên trái vẫn dùng được.
  - **Biểu mẫu và hộp thoại:** Hiện trạng thái đang xử lý trên nút gửi để tránh bấm lặp. Popover AI hiện skeleton cho ba phương án, đồng bộ E2EE hiển thị tiến độ hoặc trạng thái chờ khóa, còn tìm kiếm thông báo khi lịch sử chưa đồng bộ đủ.
- [ ] **2.3. Xử lý trạng thái tải và phản hồi về muộn**
  - Phân tách các trạng thái đang tải, có dữ liệu, rỗng, lỗi có thể thử lại và mất kết nối. Khi làm mới trên cùng đối tượng và bộ lọc, giữ nguyên phần dữ liệu còn quyền xem kèm chỉ báo cập nhật. Skeleton cần bám theo bố cục hiện tại, dùng được với cả theme sáng lẫn tối và không nhận focus bàn phím.
  - Đưa toàn bộ I/O và xử lý nặng ra ngoài JavaFX Application Thread. Chỉ áp kết quả vào đúng phiên, hội thoại, hàng đang chọn hoặc bộ lọc lúc gửi yêu cầu, đồng thời hủy hay bỏ qua phản hồi cũ khi rời màn hình, đóng popover hoặc đăng xuất. [Vòng đời điều hướng](../../architecture/desktop-ui.md#điều-hướng-và-vòng-đời)
- [ ] **2.4. Chạy thử luồng thật và các tích hợp có rủi ro**
  - Chạy thử hai User Client đăng nhập, gửi tin 1-1, tải lịch sử và đồng bộ bù sau khi ngắt kết nối. Song song đó, đăng nhập Admin Console để mở một bảng dùng dữ liệu thật.
  - Ghi lại kết quả thử gửi bản mã và xoay khóa nhóm, kiểm tra kết nối SMTP/LLM và chạy thử bộ JAR trên Windows (`X-04`).

---

## Giai đoạn 3: Hoàn thiện tài khoản, bạn bè, chat 1-1 và quản trị tài khoản (đến 04/12)

- [ ] **3.1. Nối xác thực và hồ sơ User Client (`U-01` đến `U-05`)**
  - Triển khai đăng ký, xác thực OTP, đăng nhập, sửa hồ sơ, đổi mật khẩu và đặt lại mật khẩu qua email. Kiểm thử thêm các ca đăng ký trùng đồng thời, OTP hoặc mật khẩu tạm hết hạn và lỗi gửi email.
- [ ] **3.2. Nối danh sách bạn bè, tìm người dùng và chặn (`U-06` đến `U-11`)**
  - Nối dữ liệu thật cho danh sách bạn, trạng thái online/offline, tìm người dùng, gửi và xử lý lời mời, hủy kết bạn và chặn/bỏ chặn, đồng thời áp dụng quyền chat và mời nhóm đã chốt trong spec.
- [ ] **3.3. Hoàn thiện chat riêng (`U-12` đến `U-18`)**
  - Lưu tin nhắn vào CSDL trước khi trả ACK. Khi mất ACK, client gửi lại cùng ID đó và kiểm tra trong CSDL chỉ có một bản ghi tin nhắn và một sự kiện gửi. Đồng bộ tin cho người nhận offline và hiển thị trạng thái đã lưu, đã nhận hay đã đọc theo contract.
  - Xây dựng phân trang lịch sử, xóa một hay vài tin hoặc toàn bộ lịch sử phía mình, thu hồi trong 48 giờ, tìm kiếm trong một hội thoại lẫn trên các hội thoại rồi nhảy đúng tới tin tương ứng. [Xóa và gửi lại tin](../../decisions/message-deletion.md)
- [ ] **3.4. Nối quản lý người dùng và nhật ký trong Admin Console (`A-01` đến `A-07`)**
  - Hoàn thiện lọc/sắp xếp danh sách, tạo/sửa, khóa/mở khóa, xóa mềm/khôi phục, đặt lại mật khẩu, xem lịch sử đăng nhập và danh sách bạn. Kiểm tra rằng khi khóa tài khoản, Server thu hồi phiên API/WSS của tài khoản đó. Ghi riêng sự kiện tạo tài khoản và mở ứng dụng để dùng cho thống kê.

---

## Giai đoạn 4: Hoàn thiện nhóm E2EE, spam, AI và thống kê (đến 08/12)

Triển khai song song với Giai đoạn 3 sau khi có phiên đăng nhập và luồng gửi tin cơ bản. Spec và contract của từng cụm phải được chốt trước khi nối các màn hình thuộc cụm đó.

- [ ] **4.1. Xây dựng nhóm chat và mã hóa đầu cuối (`U-19` đến `U-25`, `A-08` và `A-09`)**
  - Triển khai tạo/đổi tên nhóm, thêm/xóa thành viên, cấp quyền Admin nhóm và chọn người kế nhiệm khi cần. Nối màn hình Admin Console để lọc danh sách nhóm và xem thành viên cùng Admin nhóm.
  - Xử lý xác nhận thiết bị mới, cấp và khôi phục khóa, xoay epoch khi thành viên thay đổi; bảo đảm người đã rời nhóm không nhận được khóa cho tin mới. Trên client, bổ sung xem lịch sử, xóa và tìm kiếm tin nhóm trong phạm vi có quyền đọc. [Quyền nhóm và E2EE](../../decisions/group-permissions-and-e2ee.md)
- [ ] **4.2. Hoàn thiện luồng báo cáo và xử lý spam (`U-26`, `A-10` và `A-11`)**
  - Trên User Client, cho phép chọn, xem trước và xác nhận gói bằng chứng. Phía Admin chỉ đọc phần bằng chứng được chia sẻ, tách riêng hai thao tác khóa tài khoản và đánh dấu đã xử lý, kèm kiểm thử hồ sơ hết hạn hoặc bị ẩn danh sau purge. [Kiểm duyệt spam](../../decisions/spam-moderation-and-retention.md)
- [ ] **4.3. Tích hợp hai nút AI trên thanh soạn tin (`U-27`)**
  - Tách nút hiện tại thành **Refine** và **Recommend**, dùng chung popover và một API phân biệt theo `mode` nếu phù hợp. Client gửi tối đa 50 tin hợp lệ qua Server đến nhà cung cấp LLM và hiển thị ba phương án; khi người dùng chọn một phương án, nội dung được điền vào ô nháp.
  - Ghi rõ trong spec về việc gửi bản rõ ngữ cảnh. Kiểm tra ô nháp vẫn giữ nguyên khi gặp timeout 15 giây, hết hạn mức, dữ liệu trả về sai định dạng hoặc người dùng đã đổi hội thoại. [Thanh soạn tin hiện tại](../../../src/user/src/main/resources/com/chatsystem/user/fxml/messaging/chat-history.fxml#L68-L85) · [DEC-08](../../decisions/llm-assistant.md)
- [ ] **4.4. Nối Dashboard Tổng quan và thống kê (`A-12` đến `A-16`)**
  - Nối số liệu thật cho bảng và biểu đồ đăng ký mới, hoạt động và mạng lưới bạn bè. Áp dụng bộ lọc và múi giờ `Asia/Ho_Chi_Minh`, hiển thị đủ 12 tháng (tháng không có dữ liệu ghi 0) và kiểm tra để việc gửi lại tin không làm tăng số liệu. [Thống kê và thời gian](../../decisions/statistics-and-time.md)
- [ ] **4.5. Chạy tác vụ dọn dữ liệu theo các DEC**
  - Dọn tài khoản, bản sao lưu khóa và bằng chứng spam theo thời hạn, sau đó kiểm tra lại quyền sau khi dọn và đối chiếu các số liệu thống kê cần giữ.

---

## Giai đoạn 5: Nghiệm thu, đóng gói và nộp Version 2 (10-12/12)

- [ ] **5.1. Kiểm thử trước phát hành (10/12)**
  - Chạy test SQL trên PostgreSQL trống, test tích hợp Server, test REST/WSS theo contract và test JavaFX trong các tình huống phản hồi chậm, rỗng, lỗi mạng, timeout và lệch thứ tự. Demo nhiều máy qua Server với các kịch bản kết nối lại, mất quyền giữa chừng, nhảy tới kết quả tìm kiếm, đổi khóa nhóm và giữ nguyên nháp khi AI gặp lỗi. [Kiểm thử SQL](../../../script/README.md) · [Kiểm thử JavaFX](../../architecture/desktop-ui.md#kiểm-thử)
  - Đo hiệu năng theo ngưỡng đề xuất trong DEC-09: p95 từ lúc gửi đến hiển thị tối đa 1 giây, tải 50 tin tối đa 2 giây, cùng thời gian tìm kiếm và đồng bộ E2EE. Ghi lại kết quả đo, môi trường chạy và số tin mất/trùng sau khi kết nối lại.
- [ ] **5.2. Đóng gói và chạy thử trên Windows sạch (`X-04`, `X-05`)**
  - Đóng gói `user.jar`, `admin.jar` và `server.jar` trong `release/` cùng file cấu hình kết nối CSDL chỉnh sửa được, kèm bản JavaFX native cho Windows x64. Sau khi có JDK 25 và tạo CSDL từ `script/`, chỉnh cấu hình ngoài rồi chạy trực tiếp cả ba JAR từ gói nộp.
  - Đặt cấu hình CSDL, SMTP và LLM ở Server, còn file mẫu chỉ dùng giá trị placeholder. Rà lại mã nguồn và gói nộp trước khi giao để loại bỏ thông tin đăng nhập và khóa thật. [Gói phát hành](../../../release/README.md)
- [ ] **5.3. Cập nhật Roadmap, báo cáo và ảnh commit**
  - Đánh dấu **Logic** khi tính năng chạy với PostgreSQL, đã qua review và được ghép vào nhánh chính, đồng thời lưu ảnh commit của từng thành viên vào `git/`. [Quy ước trạng thái](../../../ROADMAP.md#how-to-use-this-file)
  - Viết báo cáo gồm thông tin nhóm, phần việc từng thành viên, tỷ lệ đóng góp tổng 100%, tính năng/kỹ thuật nổi bật, thiết kế hệ thống và CSDL, hướng dẫn có ảnh chụp, link YouTube demo và link GitHub, rồi điền bản tự đánh giá theo mẫu.
- [ ] **5.4. Kiểm tra bản nén và nộp bài (12/12)**
  - Kiểm tra đủ `src/`, `release/`, `script/`, `git/`, báo cáo và bản tự đánh giá. Mở lại gói nén, chạy thử theo hướng dẫn và kiểm tra link demo trước khi nộp. [Checklist nộp bài](../../../ROADMAP.md#pre-submission-checks)

---

## Phân công cho ba thành viên

**Đề xuất Nguyễn Thế Quân làm leader** vì Quân phụ trách khung Maven, `desktop-ui` và Docker Compose ở Version 1. Mỗi người viết feature spec và API contract cho cụm mình phụ trách, triển khai từ giao diện đến Server và kiểm thử với CSDL thật. [Phần việc Version 1](../../../README.md#team-and-contributors)

### Nguyễn Thế Quân (`@Ladenyx`, leader đề xuất)

- **Giai đoạn 1-2.** Điều phối tiến độ, chốt quy ước REST/WSS và DTO dùng chung. Dựng Spring Boot, kết nối PostgreSQL, cấu hình ngoài và phần kết nối WebSocket, push sự kiện và presence (`X-02`, `X-03`). Làm skeleton loading trong `desktop-ui`, nối vào một bảng Admin App để thử với dữ liệu thật và chạy thử bản đóng gói trên Windows.
- **Giai đoạn 3-4.** Tích hợp đăng nhập Admin App, quản lý người dùng và nhật ký (`A-01` đến `A-07`), Dashboard Tổng quan và thống kê (`A-12` đến `A-16`). Dùng API vòng đời tài khoản của Bảo cho các thao tác khóa, xóa và khôi phục.
- **Giai đoạn 5.** Tích hợp các PR, chốt bản nghiệm thu, đóng gói ba JAR và cấu hình ngoài (`X-04`, `X-05`). Tổng hợp báo cáo, kiểm tra hồ sơ trước khi nộp.

### Nguyễn Phương Gia Bảo (`@baonguyen1776`)

- **Giai đoạn 1-3.** Phụ trách xác thực, phiên User/Admin, SMTP, vòng đời tài khoản và bạn bè (`U-01` đến `U-11`). Thử SMTP và phối hợp thử cấp khóa E2EE ngay từ đợt đầu. Nối đăng nhập User Client cho demo và cung cấp phần kiểm tra quyền, thu hồi phiên cho Quân và Kiệt. Áp dụng skeleton và trạng thái tải vào các màn phụ trách.
- **Giai đoạn 4.** Chủ trì `U-25` ở phần xác nhận thiết bị, cấp khóa, backup và khôi phục khóa. Làm luồng báo cáo spam từ User Client tới Admin App (`U-26`, `A-10` và `A-11`), gồm API, xem bằng chứng, khóa tài khoản và đánh dấu đã xử lý. Triển khai tác vụ dọn tài khoản, backup khóa và báo cáo theo thời hạn.
- **Giai đoạn 5.** Nghiệm thu xác thực qua email, khóa tài khoản và kiểm duyệt spam. Chuẩn bị ảnh và hướng dẫn sử dụng cho các luồng này.

### Thái Nguyễn Tuấn Kiệt (`@tkiet24`)

- **Giai đoạn 1-3.** Phụ trách gửi/nhận tin, lịch sử, tìm kiếm và đồng bộ chat 1-1 (`U-12` đến `U-18`). Nối gửi tin và tải lịch sử bằng hai User Client để demo đợt đầu. Thử gửi bản mã, xoay khóa nhóm và kết nối LLM sớm. Tích hợp skeleton vào danh sách hội thoại, khung chat và kết quả tìm kiếm.
- **Giai đoạn 4.** Làm quyền và thao tác nhóm (`U-19` đến `U-24`), xoay epoch khi đổi thành viên và tích hợp khóa của Bảo vào luồng gửi/nhận tin nhóm E2EE. Nối màn hình tra cứu nhóm trên Admin App (`A-08` và `A-09`). Làm hai nút Refine và Recommend cùng phần gọi LLM qua Server (`U-27`).
- **Giai đoạn 5.** Đo độ trễ hiển thị chat và kiểm tra đồng bộ sau reconnect. Chạy độc lập bộ JAR do Quân đóng gói trên Windows sạch và chuẩn bị demo chat, E2EE, AI.

### Các điểm bàn giao và review chéo

1. **Đăng nhập và phân quyền.** Bảo bàn giao phiên, định danh và thao tác thu hồi phiên. Quân nối vào Server và Admin App, Kiệt dùng cho kết nối chat và cập nhật quyền soạn tin.
2. **E2EE và bằng chứng spam.** Bảo và Kiệt thử chung ca thêm/xóa thành viên, thiết bị mới và khôi phục khóa. Kiệt nối thao tác mở báo cáo từ hội thoại vào luồng submit của Bảo và kiểm tra gói bằng chứng đã xác nhận được gửi đúng.
3. **Nguồn số liệu thống kê.** Bảo cung cấp sự kiện tạo tài khoản và mở ứng dụng. Kiệt ghi sự kiện gửi tin sau khi lưu thành công. Quân dùng các sự kiện này cho Dashboard và đối chiếu số liệu khi gửi lại tin hoặc sau purge.
4. **Review và hồ sơ.** Bảo review phần nền tảng và Admin của Quân, Kiệt review phần phiên, E2EE và spam của Bảo, Quân review chat, nhóm và AI của Kiệt. Mỗi người có commit cho phần mình, tự lưu ảnh commit và gửi phần hướng dẫn cùng kết quả kiểm thử cho leader.
