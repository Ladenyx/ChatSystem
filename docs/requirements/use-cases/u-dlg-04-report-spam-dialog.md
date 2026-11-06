# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-DLG-04
- **Use Case Name**: Báo cáo spam trong chat riêng
- **Author / Create by**: Kiet
- **Date Create**: 01/11/2026
- **Last Updated by** / Date: Kiet / 02/11/2026
- **Primary Actor**: User đã đăng nhập và muốn báo cáo người gửi tin làm phiền trong chat riêng.
- **Secondary Actor(s)**: Admin hệ thống tiếp nhận và xem xét báo cáo trong `A-SCR-05`.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Desciption)

- **Brief Description**: User mở hộp thoại Báo cáo spam từ panel thông tin chat riêng, chọn một hoặc nhiều tin nhắn của đối phương làm bằng chứng và nhập lý do. Trước khi gửi, User xem trước và xác nhận chia sẻ bản rõ các tin đã chọn cho server ứng dụng và Admin hệ thống. Server ghi nhận báo cáo để Admin xem xét; không tự khóa tài khoản chỉ vì có báo cáo.
- **Goal**: Gửi báo cáo kèm bằng chứng và lý do tới Admin hệ thống để xem xét hành vi spam của đối phương trong chat riêng.
- **Trigger**: User chọn **Báo cáo spam** trong mục Quyền riêng tư & hỗ trợ của `U-SCR-09`, mở từ chat riêng tại `U-SCR-07`.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - User đã đăng nhập bằng phiên hợp lệ và đang xem chat riêng có chức năng Báo cáo spam.
  - Có ít nhất một tin văn bản của đối phương trong chat riêng mà User được phép đọc, còn hiển thị nội dung và có thể chọn làm bằng chứng.
  - Tài khoản bị báo cáo chưa bị khóa hoặc xóa tại thời điểm server tiếp nhận; tài khoản đã xóa mềm hoặc xóa vĩnh viễn đều không được tiếp nhận báo cáo mới.
  - Không bắt buộc User và người bị báo cáo đang là bạn bè, đang Online hoặc có thể gửi tin cho nhau; quan hệ chặn không tự ngăn báo cáo từ lịch sử còn được phép đọc.
  - Luồng gửi báo cáo có kết nối tới server. Admin không bắt buộc đang đăng nhập tại thời điểm User gửi.
- **Post-conditions**:
  - Khi thành công: server ghi nhận báo cáo có định danh, người báo cáo, tài khoản bị báo cáo là đối phương, chat riêng nguồn, lý do, các tin bằng chứng đã xác nhận và thời gian tiếp nhận.
  - Báo cáo có thể được Admin có quyền truy cập xem trong `A-SCR-05`; nội dung bằng chứng được thể hiện là bản rõ do người báo cáo cung cấp, lưu thành bản chụp tại thời điểm tiếp nhận tối đa 30 ngày theo BR-12.
  - User nhận xác nhận **Đã gửi báo cáo**, chưa đồng nghĩa tài khoản bị báo cáo đã vi phạm hoặc bị khóa.
  - Báo cáo không tự chặn/hủy kết bạn hoặc xóa/thu hồi tin. Bản nháp chat hiện tại được giữ nguyên.
  - Nếu hủy trước khi gửi, không tạo báo cáo và không gửi bản rõ bằng chứng qua chức năng này.
  - Nếu lỗi hoặc chưa rõ kết quả, giữ lý do và lựa chọn còn hợp lệ để đối chiếu/thử lại; không báo thành công khi chưa có xác nhận của server.

## Luồng sự kiện (Flow of Events)

### Basic Flow

Luồng cơ bản: báo cáo một tài khoản trong chat cá nhân bằng các tin văn bản còn được phép đọc.

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Trong hội thoại cá nhân, mở panel thông tin bằng nút **i** và chọn **Báo cáo spam**. | Mở `U-DLG-04`, hiển thị người bị báo cáo là đối phương, phần chọn tin bằng chứng, ô lý do và các nút **Tiếp tục / Hủy**. Ghi nhận hội thoại nguồn, chưa gửi bằng chứng. |
| 2 | Chọn một hoặc nhiều tin nhắn của người bị báo cáo. | Hiển thị các tin đủ điều kiện trong hội thoại nguồn; cập nhật danh sách đã chọn, không chọn trùng và không tự chọn toàn bộ lịch sử. Mỗi tin có nội dung, người gửi và thời gian gửi để User kiểm tra. |
| 3 | Nhập lý do báo cáo. | Giữ lý do trong hộp thoại; kiểm tra 1–20 tin hợp lệ, lý do 1–1.000 ký tự Unicode sau trim và tổng dữ liệu báo cáo không vượt 256 KiB theo BR-08 trước khi cho tiếp tục. |
| 4 | Nhấn **Tiếp tục**, xem lại người bị báo cáo, các tin đã chọn và lý do. | Hiển thị bản xem trước đầy đủ cùng thông báo: **Nội dung các tin nhắn đã chọn và lý do sẽ được gửi tới server ứng dụng để Admin hệ thống xem xét.** Có nút quay lại sửa lựa chọn và nút **Xác nhận & gửi báo cáo**. |
| 5 | Đồng ý chia sẻ nội dung và nhấn **Xác nhận & gửi báo cáo**. | Kiểm tra lại đầu vào, gửi đúng nội dung đã xem trước cùng định danh yêu cầu; hiển thị đang xử lý và ngăn nhấn gửi lặp. Không tự thêm tin mới đến hoặc nội dung khác vào bằng chứng. |
| 6 | Chờ kết quả gửi. | Server xác thực phiên; kiểm tra tài khoản đích chưa khóa/xóa theo BR-11, hội thoại nguồn là chat riêng của User và các tin bằng chứng thuộc chat riêng đó, do đối phương gửi, chưa bị thu hồi trước lúc tiếp nhận theo BR-13; kiểm tra giới hạn đầu vào, tần suất và trùng lặp theo BR-08, BR-09, BR-10. Ghi nhận báo cáo cùng bản chụp bằng chứng nhất quán, trả định danh, thời gian tiếp nhận và hạn lưu bằng chứng theo BR-12. Không chỉ tin nội dung/tham chiếu do client cung cấp. |
| 7 | Nhận thông báo **Đã gửi báo cáo**. | Đóng hộp thoại và trở lại hội thoại, giữ nguyên bản nháp chat. Báo cáo sẵn sàng cho Admin xem xét tại `A-SCR-05`; User có thể theo dõi và yêu cầu rút trong mục **Báo cáo đã gửi** của `U-DLG-04`. Quyết định khóa tài khoản thuộc luồng quản trị `A-11`, không thuộc thao tác gửi báo cáo. |

### Alternative Flows

#### Alt 2a — Chọn nhiều tin hoặc tìm thêm trong lịch sử

Tại bước 2:

1. User tải thêm lịch sử và chọn thêm tin của đối phương trong cùng chat riêng nguồn.
2. Hệ thống giữ các lựa chọn hợp lệ khi tải thêm, hiển thị số tin đã chọn và cho bỏ chọn từng tin.
3. Các tin trong bản xem trước được sắp theo thứ tự hội thoại. Tin mới đến không tự được chọn làm bằng chứng.
4. Nếu User bỏ chọn hết, nút Tiếp tục không khả dụng cho đến khi chọn lại ít nhất một tin hợp lệ.

#### Alt 4a — Quay lại sửa trước khi xác nhận

Tại bước 4–5, trước khi gửi:

1. User quay lại thay đổi lý do hoặc danh sách tin đã chọn; tài khoản đích vẫn là đối phương trong chat riêng nguồn.
2. Hệ thống cập nhật bản xem trước; chưa tạo báo cáo hoặc gửi bản rõ bằng chứng.
3. User phải xem và xác nhận nội dung mới. Không dùng xác nhận cho bản cũ để gửi dữ liệu đã thay đổi.

#### Alt 4b — Hủy hoặc không đồng ý chia sẻ bản rõ

Trước bước 5:

1. User nhấn **Hủy**, đóng hộp thoại hoặc nhấn Escape.
2. Hệ thống đóng hộp thoại, không gửi báo cáo và giữ nguyên hội thoại/bản nháp chat.
3. Việc đã chọn tin hoặc mở bản xem trước chưa được xem là đồng ý chia sẻ nội dung.

#### Alt 5a — Tin đã chọn không còn hợp lệ trước khi gửi

Tại bước 2–5:

1. Một tin được chọn vừa bị thu hồi, bị xóa phía mình hoặc không còn nội dung mà User được phép đọc.
2. Hệ thống thông báo thay đổi và loại nội dung đó khỏi phần lựa chọn/xem trước khi phát hiện; không dùng bản sao cũ để âm thầm gửi đi.
3. Nếu không còn tin hợp lệ, yêu cầu chọn lại. Nếu vẫn còn tin, User phải kiểm tra và xác nhận lại bằng chứng đã cập nhật.
4. Nếu server đã tiếp nhận báo cáo hợp lệ trước khi tin bị thu hồi, bản chụp bằng chứng vẫn giữ và hiển thị nội dung trước khi thu hồi; báo cáo vẫn hợp lệ trong thời hạn lưu 30 ngày. Nếu thu hồi xảy ra trước khi server tiếp nhận, từ chối bằng chứng đó và yêu cầu User kiểm tra lại. Thứ tự được xác định theo xử lý tại server, không theo đồng hồ client.

#### Alt 6a — Đóng hộp thoại hoặc chuyển hội thoại khi đã gửi yêu cầu

Tại bước 6:

1. User rời hộp thoại hoặc chuyển sang hội thoại khác trong lúc chờ xác nhận.
2. Hệ thống giữ thông tin yêu cầu đang chờ gắn với hội thoại nguồn để đối chiếu kết quả; không coi việc đóng hộp thoại là đã hủy báo cáo trên server.
3. Phản hồi trả muộn chỉ cập nhật trạng thái của yêu cầu tương ứng, không mở lại hộp thoại hoặc thay lựa chọn/bản nháp ở hội thoại khác.
4. Nếu chưa rõ server đã tiếp nhận hay chưa, xử lý theo Ex 6b trước khi cho gửi lại cùng báo cáo.

#### Alt 7a — Theo dõi và yêu cầu rút báo cáo đã gửi

Sau khi báo cáo được tiếp nhận:

1. User mở `U-DLG-04` từ chat riêng và chọn **Báo cáo đã gửi**. Hệ thống chỉ liệt kê báo cáo của chính User trong chat riêng đó, gồm định danh, thời gian tiếp nhận, trạng thái, kết quả nếu có và hạn lưu bằng chứng.
2. Trong thời gian lưu bằng chứng, tối đa 30 ngày tính từ thời điểm tiếp nhận theo BR-12, User chọn **Yêu cầu rút báo cáo**, nhập lý do 1–1.000 ký tự Unicode sau trim và xác nhận gửi theo BR-14.
3. Server kiểm tra người gửi là chủ báo cáo và chưa hết hạn; lưu yêu cầu rút cùng lý do, thời gian, hiển thị **Đang chờ xử lý yêu cầu rút** và chuyển cho Admin hệ thống. Không tạo thêm yêu cầu nếu đã có một yêu cầu rút đang chờ.
4. Admin chấp nhận hoặc từ chối yêu cầu rút, ghi lý do và phản hồi cho User. Nếu chấp nhận, báo cáo được đánh dấu **Đã rút**; nếu từ chối, giữ trạng thái/kết quả báo cáo trước đó. Việc yêu cầu rút không tự xóa bằng chứng, kéo dài thời hạn lưu hoặc đảo ngược quyết định khóa tài khoản đã thực hiện.
5. Nếu báo cáo đã có kết quả, User vẫn được yêu cầu rút trong thời gian lưu bằng chứng, tối đa 30 ngày; giữ lịch sử quyết định và trạng thái rút để phân biệt hai sự kiện. Xử lý thay đổi chế tài, nếu có, thuộc luồng quản trị riêng.

#### Alt 7b — Nhận kết quả kiểm duyệt

1. Admin hệ thống kết luận **Có vi phạm** hoặc **Không đủ căn cứ xác định vi phạm**, ghi kết quả và lý do tại `A-SCR-05`.
2. Hệ thống phản hồi kết quả cho người gửi báo cáo, cho xem lại trong **Báo cáo đã gửi**; không xem thông báo tiếp nhận ban đầu là kết quả kiểm duyệt.
3. Nếu kết luận **Có vi phạm**, hệ thống thông báo cho người bị báo cáo về hành vi vi phạm và biện pháp đã áp dụng, nếu có. Không cung cấp danh tính, định danh tài khoản, lý do gốc của người báo cáo hoặc dữ liệu có thể trực tiếp nhận diện người báo cáo trong thông báo/API dành cho người bị báo cáo.
4. Thông báo được lưu để đọc sau nếu người nhận offline. Nếu tài khoản bị báo cáo đã bị khóa do xử lý, hiển thị thông báo kết quả qua màn hình trạng thái tài khoản khi đăng nhập, không yêu cầu mở lại quyền chat. Không gửi thông báo kết luận vi phạm cho người bị báo cáo khi chưa có kết luận **Có vi phạm**.

### Exception Flows

- **Ex 1a — Chưa có bằng chứng có thể chọn**: Hiển thị trạng thái trống và giải thích cần ít nhất một tin văn bản của đối phương còn được phép đọc trong chat riêng. Nếu lịch sử đang tải, hiển thị trạng thái tương ứng; không coi đó là kết luận hội thoại không có tin. Không gửi báo cáo chỉ có lý do trong phạm vi use case này.
- **Ex 2a — Không tải được tin nhắn**: Thông báo lỗi tải/đồng bộ, cho thử lại và giữ các lựa chọn còn hợp lệ. Không coi lỗi tải là lịch sử trống.
- **Ex 2b — Chưa chọn tin hoặc lựa chọn sai nguồn/người gửi**: Yêu cầu chọn ít nhất một tin của đối phương trong cùng chat riêng nguồn. Không chấp nhận tin của chính User, tin hệ thống, nội dung đã thu hồi hoặc tham chiếu thuộc hội thoại khác.
- **Ex 2c — Hội thoại hoặc tài khoản đích không hợp lệ**: Server từ chối nếu hội thoại nguồn không phải chat riêng của User hoặc tài khoản đích không phải đối phương. Không ghi nhận báo cáo từ nhóm chat qua chức năng này.
- **Ex 3a — Lý do không hợp lệ**: Nếu lý do sau trim rỗng hoặc vượt 1.000 ký tự Unicode, hiển thị lỗi tại ô lý do và không chuyển sang gửi báo cáo. Giữ các tin đã chọn còn hợp lệ. Áp dụng cùng giới hạn cho lý do yêu cầu rút báo cáo.
- **Ex 5a — Vượt giới hạn đầu vào**: Nếu chọn quá 20 tin hoặc tổng dữ liệu JSON UTF-8 vượt 256 KiB, hướng dẫn User điều chỉnh. Không tự cắt lý do, cắt nội dung tin hoặc bỏ bớt bằng chứng rồi gửi một báo cáo khác bản đã xác nhận.
- **Ex 5b — Vượt tần suất hoặc báo cáo trùng**: Server áp dụng BR-09 và BR-10; thông báo thời điểm được gửi lại nếu vượt hạn mức hoặc **Bạn đã báo cáo các tin nhắn này** nếu trùng. Giữ lý do/lựa chọn; không tạo báo cáo mới hoặc tính thêm hạn mức cho bản gửi lại của yêu cầu đã tiếp nhận.
- **Ex 6a — Server từ chối hoặc lỗi lưu báo cáo**: Thông báo lỗi phù hợp, không báo đã gửi thành công. Giữ lý do/lựa chọn còn hợp lệ; không ghi nhận báo cáo thiếu bằng chứng hoặc thiếu lý do như một báo cáo hoàn chỉnh.
- **Ex 6b — Mất kết nối hoặc chưa nhận xác nhận tiếp nhận**: Hiển thị chưa rõ kết quả, đối chiếu trạng thái bằng định danh yêu cầu. Nếu đã lưu, dùng đúng báo cáo đó; nếu xác định chưa lưu, cho User kiểm tra và xác nhận gửi lại. Dùng cùng định danh cho cùng lần gửi để không tạo báo cáo trùng do nhấn lặp/reconnect; không tự tạo yêu cầu mới khi chưa rõ kết quả cũ.
- **Ex 6c — Tài khoản bị báo cáo đã khóa/xóa**: Server từ chối báo cáo mới nếu tài khoản đích đã khóa, xóa mềm hoặc xóa vĩnh viễn tại thời điểm tiếp nhận. Hiển thị **Không thể báo cáo tài khoản đã bị khóa hoặc xóa**. Không tự đổi sang tài khoản mới có cùng username. Báo cáo đã tiếp nhận trước khi tài khoản bị khóa/xóa vẫn được giữ theo BR-11 và BR-12.
- **Ex 7a — Yêu cầu rút không hợp lệ**: Từ chối nếu không phải chủ báo cáo, đã hết thời hạn lưu bằng chứng theo BR-12, lý do không hợp lệ hoặc báo cáo đã được rút. Nếu đã có yêu cầu rút đang chờ, trả trạng thái yêu cầu đó; không tạo yêu cầu trùng.
- **Ex 1b — Phiên không còn hợp lệ**: Ở bất kỳ bước nào, nếu phiên bị thu hồi/tài khoản User bị khóa, dừng thao tác cần xác thực và chuyển về luồng đăng nhập. Không tự gửi lại bằng chứng bằng phiên mới; báo cáo đã được server tiếp nhận trước đó không được coi là đã hủy chỉ vì phiên kết thúc.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

#### BR-01 — Thành phần hộp thoại

- Có thông tin tài khoản bị báo cáo, phần chọn một hoặc nhiều tin bằng chứng, ô lý do, số tin đã chọn và các nút điều hướng/xác nhận/hủy.
- Chỉ mở từ chat riêng; tài khoản đích được xác định là đối phương, không có bộ chọn loại báo cáo hoặc danh sách nhiều tài khoản đích.
- Bản xem trước phải cho đọc đầy đủ tài khoản đích, nội dung từng tin, người gửi, thời gian gửi và lý do sẽ chia sẻ.
- Có mục **Báo cáo đã gửi** cho chính User trong chat riêng nguồn, hiển thị trạng thái/kết quả, hạn lưu bằng chứng và nút **Yêu cầu rút báo cáo** kèm ô lý do khi còn trong thời gian lưu bằng chứng, tối đa 30 ngày theo BR-12 và BR-14.

#### BR-02 — Đối tượng và bằng chứng

- Mỗi báo cáo nhắm tới đúng một tài khoản là đối phương trong chat riêng nguồn, kèm 1–20 tin văn bản của tài khoản đó trong cùng chat riêng.
- Chỉ chọn tin đã có định danh server, User được phép đọc và còn hiển thị nội dung; một tin chỉ xuất hiện một lần trong danh sách bằng chứng.
- Không tự chọn toàn bộ lịch sử, bổ sung hội thoại khác hoặc gửi các tin mới đến sau khi xác nhận.
- User được sửa lý do và bỏ/chọn lại tin; không cung cấp thao tác viết lại nội dung tin gốc trong bản xem trước bằng chứng.
- Không chọn tin của chính người báo cáo, tin hệ thống hoặc tin từ nhóm chat làm bằng chứng.
- Tin đã thu hồi không được chọn làm bằng chứng mới. Tin bị thu hồi sau khi báo cáo được server tiếp nhận vẫn hiển thị nội dung trong bản chụp bằng chứng, không thay bằng nhãn thu hồi; không vì thế làm báo cáo đã gửi mất hiệu lực.

#### BR-03 — Lý do báo cáo

- Lý do là bắt buộc, dài 1–1.000 ký tự Unicode sau khi bỏ khoảng trắng đầu/cuối; không chấp nhận chỉ có khoảng trắng. Đếm theo Unicode code point thống nhất giữa client/server.
- Lý do thể hiện nhận định của người báo cáo, không tự được coi là kết luận vi phạm.
- Dùng ô nhập lý do tự do; giới hạn báo cáo áp dụng theo BR-08, độc lập với chức năng LLM.

#### BR-04 — Xem trước và xác nhận chia sẻ bằng chứng

- Mở hộp thoại, chọn tin hoặc nhập lý do chưa đồng nghĩa đồng ý gửi dữ liệu.
- User phải xem trước và xác nhận rõ ràng việc gửi bản rõ các tin đã chọn cùng lý do tới server ứng dụng để Admin hệ thống xem xét.
- Chỉ gửi dữ liệu bằng chứng đã xác nhận và metadata cần thiết để gắn với người báo cáo, tài khoản đích, chat riêng và tin nguồn. Không tự đính kèm danh bạ/hồ sơ không liên quan.
- Admin chỉ được xem bằng chứng trong báo cáo qua chức năng kiểm duyệt, không tự được truy cập toàn bộ lịch sử chat riêng qua thao tác này.

#### BR-05 — Tiếp nhận và xử lý báo cáo

- Server kiểm tra phiên, hội thoại nguồn là chat riêng, tài khoản đích, người gửi từng tin và các tham chiếu/quyền theo dữ liệu hiện có; không chỉ tin thông tin do client cung cấp.
- Không tiếp nhận báo cáo mới cho tài khoản đích đã khóa/xóa. Kiểm tra báo cáo gửi lại theo định danh yêu cầu trước: nếu đã tiếp nhận thì trả đúng kết quả cũ, không từ chối bản gửi lại chỉ vì tài khoản đích vừa bị khóa/xóa sau đó.
- Admin xem bằng chứng với lưu ý **Bản rõ do người báo cáo cung cấp**, theo DEC-07 và `A-SCR-05`.
- Admin hệ thống quyết định khóa tài khoản thủ công qua luồng `A-11`/`A-DLG-02`; không tự khóa theo một báo cáo hoặc theo số lượng báo cáo trong use case này.
- Thông báo thành công chỉ xác nhận đã tiếp nhận. Kết quả kiểm duyệt và thông báo cho các bên áp dụng theo BR-16; thời hạn xử lý tối đa là 15 ngày theo BR-15, không xem việc quá hạn là kết luận có/không vi phạm.

#### BR-06 — Tách báo cáo khỏi thao tác hội thoại

- Gửi báo cáo không tự chặn, hủy kết bạn hoặc xóa/thu hồi tin. Các thao tác đó thuộc luồng riêng.
- Không bắt buộc bỏ chặn hoặc kết bạn lại để báo cáo bằng các tin lịch sử còn được phép đọc.
- Không tự gửi tin nhắn vào hội thoại khi báo cáo. Danh tính người báo cáo được ẩn với người bị báo cáo; server và Admin có quyền kiểm duyệt vẫn biết danh tính để xử lý, kiểm tra quyền và chống lạm dụng. Thông báo kết quả tuân theo BR-16.
- Bản nháp chat và vị trí hội thoại đang xem không bị thay thế bằng nội dung biểu mẫu hoặc phản hồi gửi báo cáo.

#### BR-07 — Nhất quán khi gửi và thử lại

- Chỉ hiển thị đã gửi thành công khi server xác nhận tiếp nhận báo cáo hoàn chỉnh.
- Cùng một yêu cầu gửi phải được xử lý lặp an toàn; đối chiếu theo người báo cáo và định danh yêu cầu, không tạo nhiều báo cáo hoặc tính thêm hạn mức do nhấn lặp/reconnect. Cùng định danh nhưng dữ liệu khác phải bị từ chối; giữ kết quả đối chiếu ít nhất 30 ngày, không giữ thêm bản rõ ngoài thời hạn BR-12.
- Nếu User sửa lý do hoặc bằng chứng sau một lần gửi đã có kết quả, đó là nội dung cho yêu cầu mới, cần xem trước/xác nhận lại. Không tái sử dụng định danh cũ cho dữ liệu khác.
- Không tự coi báo cáo là trùng chỉ vì cùng tài khoản đích; đối chiếu người báo cáo, tài khoản đích và tập ID tin bằng chứng trong 24 giờ theo BR-10.

#### BR-08 — Giới hạn đầu vào

| Hạng mục | Quy tắc |
| --- | --- |
| Tin bằng chứng | Từ **1 đến 20 tin/báo cáo**, cùng đối phương và cùng chat riêng nguồn; không chọn trùng ID tin. |
| Lý do báo cáo | **1–1.000 ký tự Unicode** sau trim, đếm theo code point; áp dụng cùng giới hạn cho lý do yêu cầu rút. |
| Tổng dung lượng | Tối đa **256 KiB (262.144 byte)** cho toàn bộ dữ liệu JSON báo cáo mã hóa UTF-8, gồm nội dung tin, lý do và metadata; tính trước nén, không gồm header giao thức. |

- Client hỗ trợ kiểm tra và hiển thị số tin/số ký tự; server thực thi đầy đủ các giới hạn. Không tự cắt lý do, nội dung tin hoặc bỏ bằng chứng; nếu vượt ngưỡng, giữ biểu mẫu và hướng dẫn User điều chỉnh.

#### BR-09 — Tần suất gửi báo cáo

- Ít nhất **60 giây** giữa hai báo cáo mới được server tiếp nhận của cùng tài khoản.
- Tối đa **5 báo cáo trong 1 giờ** và **20 báo cáo trong 24 giờ** theo cửa sổ trượt, tính trên tài khoản người báo cáo. Đăng nhập lại không đặt lại hạn mức; các lượt tiếp nhận đồng thời phải được kiểm tra nhất quán.
- Chỉ báo cáo mới được tiếp nhận mới tiêu thụ hạn mức nghiệp vụ; gửi lại cùng yêu cầu, báo cáo trùng bị từ chối, yêu cầu không hợp lệ hoặc yêu cầu rút không tính thành báo cáo mới.
- Nếu vượt hạn mức, giữ lý do/lựa chọn và thông báo thời điểm được gửi lại; không tự gửi khi hết thời gian chờ.

#### BR-10 — Chống báo cáo lặp và lạm dụng

- Gửi lại do lỗi mạng dùng cùng định danh yêu cầu theo BR-07; nếu đã tiếp nhận thì trả đúng báo cáo cũ. Cùng định danh với nội dung khác bị từ chối.
- Trong **24 giờ** tính từ lần tiếp nhận, cùng người báo cáo, cùng tài khoản đích và cùng tập ID tin bằng chứng được xem là báo cáo trùng, kể cả đổi lý do hoặc thứ tự tin. Trả **Bạn đã báo cáo các tin nhắn này**, không tạo báo cáo mới.
- Với cùng đối phương trong cửa sổ 24 giờ, chỉ tiếp nhận báo cáo tiếp theo khi có ít nhất một tin bằng chứng chưa xuất hiện trong các báo cáo của người đó về đối phương trong cửa sổ này; vẫn áp dụng toàn bộ giới hạn tần suất. Việc rút báo cáo không đặt lại hạn mức hoặc xóa dấu vết chống trùng trong thời hạn này.
- Server kiểm tra tài khoản đích, quyền đọc, nguồn tin, người gửi và trạng thái thu hồi; không chỉ tin ID/nội dung do client cung cấp. Số lượng báo cáo không tự làm căn cứ khóa người bị báo cáo; Admin đánh giá bằng chứng trước khi quyết định.

#### BR-11 — Tài khoản đích đã khóa/xóa

- Không tiếp nhận báo cáo mới đối với tài khoản đã bị khóa, xóa mềm hoặc xóa vĩnh viễn tại thời điểm xử lý tiếp nhận. Server kiểm tra bằng định danh tài khoản, không chuyển báo cáo sang tài khoản mới có cùng username.
- Việc từ chối báo cáo mới không ngăn người gửi xem trạng thái/kết quả hoặc yêu cầu rút báo cáo đã tiếp nhận của chính mình trong thời hạn cho phép; các thao tác này chỉ yêu cầu phiên người gửi còn hợp lệ và đúng quyền sở hữu báo cáo.
- Báo cáo đã được tiếp nhận trước khi tài khoản bị khóa/xóa không bị hủy chỉ vì thay đổi trạng thái đó; giữ bản chụp bằng chứng theo thời hạn tối đa 30 ngày tại BR-12. Việc purge hồ sơ không chuyển quyền/dữ liệu sang tài khoản mới; định danh và metadata tài khoản vẫn tuân theo chính sách purge của DEC-07, bản chụp nội dung không được gia hạn lưu vì purge.

#### BR-12 — Thời hạn lưu bản rõ bằng chứng

- Bản rõ bằng chứng được lưu **tối đa 30 ngày liên tục (30 × 24 giờ)** tính từ thời điểm server tiếp nhận báo cáo; hạn lưu được ghi nhận khi tiếp nhận và không muộn hơn mốc này. Chỉ Admin hệ thống có quyền kiểm duyệt được xem bằng chứng qua chức năng quản trị; người bị báo cáo không được truy cập hồ sơ báo cáo gốc.
- Khi hết hạn lưu đã ghi nhận, chậm nhất sau 30 ngày kể từ lúc tiếp nhận, xóa bản rõ tin bằng chứng, lý do báo cáo và lý do yêu cầu rút khỏi hồ sơ lưu giữ; giao diện hiển thị **Bằng chứng đã hết thời hạn lưu**. Chỉ giữ metadata tối thiểu về định danh báo cáo, mốc thời gian, trạng thái và mã kết quả phục vụ đối chiếu; không lưu lại nội dung đã xóa trong ghi chú/thông báo/log. Bản sao lưu và cache phải tuân theo hạn xóa; khôi phục dữ liệu không được làm bằng chứng hết hạn xuất hiện trở lại.
- Ghi chú/lý do quyết định của Admin có chứa bản rõ tin nguồn hoặc nội dung lý do gốc cũng thuộc phạm vi xóa sau 30 ngày; không sao chép các nội dung này sang phần metadata được giữ lại.
- Thời hạn 30 ngày không bắt đầu lại khi có kết quả, yêu cầu rút hoặc thu hồi tin nguồn. Hết hạn không tự đổi kết luận kiểm duyệt hoặc tự mở khóa tài khoản đã bị xử lý.

#### BR-13 — Bằng chứng và tin nhắn bị thu hồi

- Lưu bản chụp độc lập của đúng nội dung đã được xác nhận tại lúc tiếp nhận, gắn ID tin, người gửi và thời gian nguồn. Tin đã bị thu hồi trước khi tiếp nhận không được dùng làm bằng chứng; client loại tin đó khi phát hiện, server kiểm tra lại khi ghi nhận.
- Nếu server tiếp nhận báo cáo hợp lệ trước khi xử lý thu hồi tin, bản chụp vẫn hiển thị nội dung trước thu hồi và báo cáo vẫn hợp lệ. Thu hồi/xóa lịch sử sau đó không sửa hoặc xóa bản chụp trong báo cáo; bản chụp vẫn tuân theo hạn lưu tối đa 30 ngày tại BR-12.
- Thứ tự tiếp nhận và thu hồi được xác định nhất quán tại server, không lấy thời gian do client khai báo làm căn cứ.

#### BR-14 — Yêu cầu rút báo cáo

- Trong thời gian lưu bản rõ bằng chứng theo BR-12, tối đa 30 ngày kể từ khi tiếp nhận, chính người gửi được **yêu cầu rút báo cáo và nêu lý do**, kể cả báo cáo đã có kết quả. Chỉ chấp nhận yêu cầu khi thời gian server còn trước hạn hết lưu; hết hạn thì không cho gửi yêu cầu rút mới.
- Yêu cầu phải được xác nhận; lưu người yêu cầu, thời gian, lý do và trạng thái để Admin xem xét. Tối đa một yêu cầu rút đang chờ cho mỗi báo cáo; gửi lặp cùng yêu cầu trả kết quả cũ.
- Admin chấp nhận hoặc từ chối, ghi lý do và phản hồi cho người gửi. Yêu cầu rút không đồng nghĩa đã rút thành công, không tự xóa bằng chứng hoặc đảo ngược chế tài. Khi chấp nhận, đánh dấu **Đã rút** và giữ lịch sử quyết định đến hạn lưu; kết luận/chế tài đã thực hiện chỉ được thay đổi qua thao tác quản trị riêng.

#### BR-15 — Thời hạn xử lý báo cáo và yêu cầu rút

- Thời hạn xử lý báo cáo tối đa **15 ngày liên tục (15 × 24 giờ)** từ thời điểm server tiếp nhận để Admin kết luận và hệ thống ghi nhận phản hồi kết quả cho người gửi.
- Yêu cầu rút phải được xử lý trong tối đa **15 ngày** kể từ lúc tiếp nhận yêu cầu và trước hạn hết lưu bằng chứng; lấy mốc đến trước làm hạn xử lý. Yêu cầu rút không đặt lại hạn xử lý báo cáo gốc hoặc hạn lưu tối đa 30 ngày.
- Hiển thị hạn xử lý và đánh dấu quá hạn/nhắc Admin nếu chưa có kết quả; không tự kết luận có hoặc không vi phạm khi hết 15 ngày. Thời gian tính theo server; hiển thị theo `Asia/Ho_Chi_Minh`.

#### BR-16 — Thông báo kết quả và ẩn danh

- Khi có kết luận kiểm duyệt, phản hồi **Có vi phạm** hoặc **Không đủ căn cứ xác định vi phạm** cùng giải thích ngắn cho người gửi; kết quả/yêu cầu rút được xem lại trong **Báo cáo đã gửi**. Giải thích không sao chép bản rõ bằng chứng hoặc lý do gốc sang một nơi lưu giữ vượt 30 ngày.
- Nếu kết luận **Có vi phạm**, thông báo cho người bị báo cáo về hành vi và biện pháp đã áp dụng, nếu có; không thông báo kết luận vi phạm chỉ từ việc có báo cáo mới.
- Danh tính người báo cáo được ẩn với người bị báo cáo: không trả tên, username, ID, thông tin liên hệ hoặc hồ sơ báo cáo gốc qua thông báo/API của bên bị báo cáo. Server và Admin có quyền kiểm duyệt vẫn truy cập được danh tính cần thiết cho xử lý và chống lạm dụng.
- Thông báo được lưu để người nhận đọc sau khi offline, không yêu cầu hai bên online cùng lúc. Với tài khoản bị khóa, kết quả vi phạm được hiển thị qua luồng trạng thái tài khoản khi đăng nhập, không cấp lại quyền chat chỉ để đọc thông báo.

### Non-Functional Requirements (NFR)

#### NFR-01 — Phản hồi giao diện

- Tải tin, chuẩn bị bằng chứng và gửi báo cáo không làm đóng băng JavaFX.
- Phân biệt đang tải lịch sử, không có tin phù hợp, lỗi tải, đang gửi, đã tiếp nhận và chưa rõ kết quả.
- Thông báo lỗi tại vị trí nhập/lựa chọn; giữ lý do và bằng chứng còn hợp lệ để User sửa mà không mất bản nháp chat.

#### NFR-02 — Bảo mật và quyền truy cập

- Truyền báo cáo qua HTTPS theo kiến trúc hệ thống; bảo vệ quyền truy cập bản rõ bằng chứng lưu cho mục đích kiểm duyệt.
- Server lấy danh tính người báo cáo từ phiên, kiểm tra quyền ở mỗi thao tác; không chỉ tin dữ liệu tài khoản/quyền do client gửi lên.
- Chỉ Admin hệ thống có quyền kiểm duyệt mới được xem bằng chứng qua chức năng quản trị; không công khai nội dung báo cáo cho người dùng khác.
- Không ghi token phiên hoặc bản rõ bằng chứng/lý do vào log vận hành thông thường. Lỗi không làm lộ dữ liệu báo cáo của người khác.
- Kiểm tra chủ báo cáo ở mọi thao tác xem trạng thái/yêu cầu rút theo BR-14; thông báo và API cho người bị báo cáo phải loại thông tin nhận diện người báo cáo theo BR-16. Việc xóa dữ liệu hết hạn tuân theo BR-12, áp dụng cho cả bản sao/cache do hệ thống quản lý.
- Hiển thị bằng chứng và lý do như văn bản; không thực thi mã hoặc tự mở liên kết có trong nội dung báo cáo.

#### NFR-03 — Toàn vẹn và xử lý bất đồng bộ

- Lưu báo cáo và danh sách bằng chứng nhất quán; không trả thành công khi mới lưu một phần.
- Nội dung gửi phải khớp bản xem trước đã xác nhận; nếu lựa chọn thay đổi trước khi gửi, yêu cầu kiểm tra/xác nhận lại.
- Phản hồi trả muộn gắn đúng yêu cầu/hội thoại nguồn, không thay biểu mẫu của báo cáo mới hoặc nội dung chat khác.
- Việc đối chiếu trạng thái sau lỗi mạng phải phân biệt đã tiếp nhận, chưa tiếp nhận và chưa thể xác định; không tự suy ra gửi thất bại chỉ từ timeout.

#### NFR-04 — Thời gian và khả năng tiếp nhận

- Admin có thể tiếp nhận/xem báo cáo sau khi đăng nhập, không yêu cầu phiên Admin hoạt động đồng thời với người báo cáo.
- Thời hạn xử lý báo cáo/yêu cầu rút tối đa 15 ngày theo BR-15; yêu cầu rút còn phải được xử lý trước hạn hết lưu bằng chứng. Quá hạn phải được hiển thị và nhắc Admin, không tự tạo kết luận kiểm duyệt. Thời gian chờ phản hồi API khi gửi là cấu hình kỹ thuật riêng, không đồng nhất với thời hạn kiểm duyệt.
- Lỗi báo cáo spam không tự làm mất khả năng đọc/soạn/gửi tin theo quyền hiện tại của hội thoại.
