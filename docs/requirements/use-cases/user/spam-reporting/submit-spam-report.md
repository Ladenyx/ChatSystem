# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-RPT-01
- **Use Case Name**: Gửi báo cáo spam
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 08/11/2026
- **Primary Actor**: User có tin cần báo cáo và lịch sử local được phép đọc trong hội thoại nguồn.
- **Secondary Actor(s)**: Admin hệ thống xem gói bằng chứng đã được tiếp nhận trong mục tiêu kiểm duyệt riêng.
- **Priority**: Must Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: User chọn tin của tài khoản cần báo cáo và nêu lý do. Client trích xuất tối đa 30 tin mới nhất trong cùng hội thoại từ local, gộp với các tin được chọn, rồi cho User xem trước và xác nhận gửi bản rõ tới server để Admin hiểu ngữ cảnh.
- **Goal**: Server tiếp nhận báo cáo cùng gói tin không trùng, phân biệt tin được report và tin ngữ cảnh; cấp định danh để Admin xem xét.
- **Trigger**: User chọn các tin nghi spam và yêu cầu gửi báo cáo tài khoản đã gửi chúng.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - User có phiên hợp lệ, quyền gửi báo cáo trong hội thoại nguồn và quyền đọc nội dung được dùng; với nhóm E2EE, nội dung được giải mã trên thiết bị.
  - Luồng cơ bản từ nhóm còn quyền thành viên/truy cập.
  - Có ít nhất một tin của tài khoản đích được User chọn và còn đủ điều kiện làm tin được report. Không bắt buộc hai tài khoản đã kết bạn hoặc đang online.
  - Client có thể đọc lịch sử local của đúng hội thoại. Không yêu cầu local có đủ 30 tin để được gửi báo cáo.
  - Có kết nối khi gửi; Admin không cần đang đăng nhập.
- **Post-conditions**:
  - Thành công: báo cáo có ID, chủ báo cáo từ phiên, tài khoản đích, hội thoại nguồn, lý do, gói tin đã xác nhận và thời điểm tiếp nhận; không ghi nhận một phần gói tin như thành công.
  - Mỗi ID tin chỉ có một bản trong gói; lưu nhãn tin được report, tin ngữ cảnh hoặc cả hai, cùng số lượng thực tế của từng tập và tổng số tin sau gộp.
  - Admin nhận đủ các tin User chọn và tối đa 30 tin ngữ cảnh local đã được xác nhận, kèm người gửi, thời gian và thứ tự hội thoại.
  - Thành công chỉ xác nhận tiếp nhận, chưa kết luận vi phạm hoặc áp dụng chế tài.
  - Hủy trước gửi không tạo báo cáo hoặc chia sẻ bản rõ; lỗi/chưa rõ kết quả giữ gói đã xác nhận để đối chiếu, không tự tạo yêu cầu mới.
  - Nháp, lịch sử và quan hệ cá nhân/nhóm không tự bị thay đổi.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Yêu cầu báo cáo từ hội thoại đang được phép đọc. | Ghi đúng hội thoại nguồn. Chat riêng dùng tài khoản đối phương; nhóm yêu cầu chọn một tài khoản có tin cần báo cáo. Chưa chia sẻ nội dung. |
| 2 | Chọn một hoặc nhiều tin của tài khoản đích và nêu lý do. | Xác định tập tin được report Selected, kiểm tra người gửi/quyền đọc, loại ID trùng. Các tin ngữ cảnh không thay thế yêu cầu phải có ít nhất một tin trong Selected. |
| 3 | Yêu cầu gửi và kiểm tra gói tin sẽ chia sẻ. | Client lấy tập Context gồm tối đa 30 tin mới nhất đủ điều kiện trong lịch sử local của cùng hội thoại tại lúc chuẩn bị bản xem trước; không lấy từ server. Gộp Evidence = Selected ∪ Context theo ID, xếp theo thứ tự hội thoại, giữ nhãn mỗi tin. Hiển thị đầy đủ tài khoản đích, lý do, nội dung, người gửi, thời gian và các số lượng; nếu local có ít hơn 30 tin thì nêu rõ số thực tế. |
| 4 | Xác nhận chia sẻ toàn bộ gói đã xem trước và gửi báo cáo. | Kiểm tra lại quyền/nội dung rồi gửi đúng gói Evidence đã xác nhận, gồm tin được report và tin ngữ cảnh, tới server ứng dụng cho Admin hệ thống xem xét. Nêu rõ đây là gửi bản rõ kể cả từ nhóm E2EE; không gửi khóa hoặc tự bổ sung tin mới đến. |
| 5 | Chờ xác nhận tiếp nhận. | Server kiểm tra chủ báo cáo, hội thoại và metadata từng tin; chỉ các tin trong Selected phải thuộc tài khoản đích, tin ngữ cảnh có thể thuộc người gửi khác trong cùng hội thoại. Kiểm tra đầu vào/hạn mức, đối chiếu yêu cầu lặp và lưu nhất quán gói đã nhận; không truy vấn lịch sử server để bổ sung hoặc thay thế 30 tin local. Trả ID và thời điểm tiếp nhận. |
| 6 | Nhận xác nhận báo cáo đã được tiếp nhận. | Xác nhận gói tin và số lượng đã lưu. Admin có quyền được xem gói không trùng cùng nhãn tin được report/ngữ cảnh; chưa kết luận vi phạm hoặc tự áp dụng chế tài. Hoàn tất mục tiêu gửi. |

### Alternative Flows

- **Alt 2a — Đổi tài khoản đích trong nhóm**: Trước bước 4, User đổi tài khoản đích; bỏ các lựa chọn trong Selected không thuộc tài khoản mới, yêu cầu chọn lại và chuẩn bị lại bản xem trước. Tin của người khác vẫn có thể nằm trong Context nhưng không tự trở thành tin được report.
- **Alt 2b — Chọn thêm/bớt tin được report**: Sửa Selected trong lịch sử local của cùng hội thoại, giữ lựa chọn còn hợp lệ và chuẩn bị lại gói ở bước 3. Bỏ hết Selected thì chưa được gửi dù Context vẫn có tin.
- **Alt 3a — Sửa lý do hoặc làm mới ngữ cảnh**: Chuẩn bị lại bản xem trước; nếu làm mới ngữ cảnh thì trích xuất lại Context từ local và gộp lại. Mọi thay đổi gói tin hoặc lý do đều cần User xác nhận lại trước khi gửi.
- **Alt 3b — Selected không giao Context**: Giữ tất cả tin trong Selected và Context, mỗi tin một lần. Nếu Context có 30 tin, tổng số tin gửi là |Selected| + 30.
- **Alt 3c — Selected giao một phần với Context**: Tin thuộc cả hai tập chỉ gửi một bản nhưng giữ cả hai nhãn. Tổng số tin gửi là |Selected| + |Context| − |Selected ∩ Context|.
- **Alt 3d — Selected nằm hoàn toàn trong Context**: Gói gửi chính là Context, các tin trong Selected được đánh dấu là tin được report. Nếu Context có 30 tin thì Admin nhận 30 tin, không nhận thêm bản sao của Selected.
- **Alt 3e — Local có ít hơn 30 tin đủ điều kiện**: Dùng toàn bộ tin local hiện có và đủ quyền đọc làm Context, vẫn cho gửi nếu Selected hợp lệ. Hiển thị rõ số tin ngữ cảnh và tổng số tin sau gộp; không tải từ server, tạo tin giả hoặc lặp tin cho đủ 30.
- **Alt 3f — Có tin mới sau bản xem trước**: Giữ nguyên gói đã chuẩn bị; không âm thầm đưa tin mới vào nội dung gửi. User muốn dùng ngữ cảnh mới thì chủ động làm mới và xác nhận lại theo Alt 3a.
- **Alt 4a — Hủy hoặc không đồng ý chia sẻ**: Trước khi gửi, kết thúc không chia sẻ gói tin/bản rõ; nháp chat được giữ.
- **Alt 4b — Tin nguồn thay đổi**: Trước khi gửi, tin bị thu hồi/xóa riêng/mất quyền đọc phải được loại khỏi lựa chọn hoặc ngữ cảnh; chuẩn bị lại gói local và yêu cầu xác nhận lại. Nếu Selected không còn tin hợp lệ thì không gửi. Sau khi báo cáo đã được tiếp nhận, thay đổi lịch sử không tự tạo báo cáo mới hoặc khôi phục tin nguồn đã bị loại.
- **Alt 5a — Rời ngữ cảnh khi đang chờ**: Ngừng chờ không tự hủy báo cáo đã gửi. Phản hồi gắn đúng yêu cầu, không áp dụng sang hồ sơ/hội thoại mới.
- **Alt 5b — Gửi lặp cùng yêu cầu**: Giữ nguyên Selected, Context và gói Evidence đã xác nhận; không lấy lại 30 tin mới cho cùng định danh yêu cầu. Đã tiếp nhận thì trả báo cáo cũ, không tạo thêm hoặc tính thêm hạn mức. Cùng ID với dữ liệu khác bị từ chối.

### Exception Flows

- **Ex 2a — Không có tin được report hợp lệ**: Không gửi báo cáo chỉ có lý do hoặc tin ngữ cảnh. Selected không dùng tin của mình, tin hệ thống, khác tài khoản đích/hội thoại hoặc chưa giải mã. Quy tắc tài khoản đích chỉ áp dụng Selected; Context được chứa tin của User và thành viên khác đủ quyền trong cùng hội thoại.
- **Ex 2b — Mất quyền hoặc nguồn không khớp**: Từ chối tin khác hội thoại hoặc tài khoản đích không đúng người gửi của Selected; không sửa âm thầm thành báo cáo người khác.
- **Ex 3a — Không đọc được lịch sử local**: Thông báo lỗi và cho thử lại, giữ lựa chọn/lý do còn hợp lệ. Không coi lỗi đọc local là có ít hơn 30 tin hoặc thay bằng lịch sử server.
- **Ex 4a — Đầu vào không hợp lệ/vượt giới hạn**: Kiểm tra trên toàn bộ gói Evidence sau loại trùng. Thông báo điều kiện không đạt; không tự cắt nội dung, bỏ tin được report hoặc giảm ngữ cảnh để gửi một gói khác bản xác nhận.
- **Ex 5a — Timeout/mất xác nhận**: Giữ định danh và đúng gói đã gửi để đối chiếu. Chỉ sau khi xác định chưa tiếp nhận mới cho User xác nhận thử lại; không tự tạo ID mới hoặc thay Context bằng ngữ cảnh mới.
- **Ex 5b — Bị từ chối hoặc vượt hạn mức**: Không tạo hồ sơ mới, giữ dữ liệu còn hợp lệ để User điều chỉnh. Chỉ nêu thời điểm thử lại nếu xác định được; không tự gửi sau thời gian chờ.
- **Ex 1a — Phiên không hợp lệ**: Dừng thao tác cần xác thực; không tự gửi lại bằng phiên mới. Yêu cầu cũ đã được tiếp nhận không tự bị hủy.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

- **BR-01 — Phạm vi một hồ sơ**: Một chủ báo cáo, một tài khoản bị báo cáo và một hội thoại nguồn. Selected chỉ chứa tin của tài khoản đích; Context là ngữ cảnh của cùng hội thoại, có thể gồm tin của người báo cáo và các thành viên khác. Có mặt trong Context không tự làm người gửi trở thành tài khoản bị báo cáo.
- **BR-02 — Đồng ý và giới hạn dữ liệu**: User xem trước và xác nhận toàn bộ Evidence cùng lý do trước khi chia sẻ bản rõ. Không chỉnh sửa nội dung tin gốc hoặc tự gửi danh bạ, hồ sơ, lịch sử hội thoại khác hay nội dung ngoài Selected ∪ Context.
- **BR-03 — Mức xác thực**: Server kiểm tra phiên, metadata/quyền/tham chiếu có thể kiểm tra, nhưng không tự chứng minh bản rõ khớp bản mã E2EE chỉ từ ID. Admin được biết đây là bản chụp local do người báo cáo cung cấp.
- **BR-04 — Tách mục tiêu**: Tiếp nhận báo cáo không tự khóa/chặn/hủy kết bạn/xóa tin hoặc rời nhóm. Admin hệ thống xem xét và quyết định xử lý thủ công.
- **BR-05 — Quyền và riêng tư**: Chủ báo cáo xác định từ phiên; bản rõ bằng chứng và ngữ cảnh chỉ cung cấp cho người có quyền kiểm duyệt. Không tiết lộ hồ sơ/danh tính qua lỗi hoặc truy vấn không có quyền.
- **BR-06 — Bằng chứng**: Selected có ít nhất một tin hợp lệ của tài khoản đích. Giới hạn số tin User được chọn áp dụng cho Selected; Context không thay thế tin được report và không phải 30 tin người dùng phải chọn thêm.
- **BR-07 — Lý do**: Lý do bắt buộc có nội dung, tuân giới hạn độ dài của hệ thống; không tự cắt hoặc thay nội dung đã xác nhận.
- **BR-08 — Kích thước và hạn mức**: Kiểm tra dung lượng trên toàn bộ Evidence, tính mỗi ID một lần. Tần suất/số báo cáo tuân hạn mức hệ thống; không tự chia báo cáo, cắt gói hoặc giảm 30 tin ngữ cảnh khi local có đủ tin hợp lệ để vừa giới hạn đầu vào.
- **BR-09 — Yêu cầu lặp**: Cùng định danh yêu cầu sử dụng đúng gói đã xác nhận và trả kết quả cũ nếu đã tiếp nhận. Context không tự được trích xuất lại khi thử lại hoặc reconnect.
- **BR-10 — Quyền báo cáo**: Kiểm tra quyền gửi báo cáo và đọc từng tin tại thời điểm gửi; không suy quyền từ dữ liệu hội thoại cũ. Không lấy tin đã thu hồi/xóa riêng hoặc chưa giải mã làm nội dung bản rõ.
- **BR-11 — Lưu giữ bằng chứng**: Lưu nội dung, người gửi, thời gian, ID, nhãn được report/ngữ cảnh và số lượng thực tế của gói đã tiếp nhận một cách nhất quán. Bảo vệ bản rõ như dữ liệu riêng tư phục vụ kiểm duyệt, không đưa vào log hoặc tự khôi phục từ lịch sử đã bị loại.
- **BR-12 — Nguồn và mốc ngữ cảnh**: Context gồm tối đa 30 tin mới nhất đủ quyền đọc và có nội dung sẵn sàng trong lịch sử local của cùng hội thoại tại lúc chuẩn bị bản xem trước. Đây là 30 tin mới nhất của hội thoại local, không phải 30 tin quanh vị trí tin được report. Không truy vấn, tải bù hoặc trích xuất lịch sử từ server để tạo Context.
- **BR-13 — Gộp và loại trùng**: Evidence = Selected ∪ Context theo ID tin, |Evidence| = |Selected| + |Context| − |Selected ∩ Context|. Tin giao chỉ có một bản với cả hai nhãn; giữ đủ Selected kể cả khi các tin đó nằm ngoài 30 tin mới nhất. Trình bày Evidence theo thứ tự hội thoại.
- **BR-14 — Không đủ 30 tin local**: Nếu local chỉ có n < 30 tin đủ điều kiện thì Context gồm toàn bộ n tin đó. Vẫn cho báo cáo khi Selected hợp lệ, hiển thị rõ n và tổng số tin sau gộp; không cam kết Admin luôn có ít nhất 30 tin trong trường hợp này.
- **BR-15 — Bản xem trước và gói gửi**: Hiển thị số tin được report, số tin trong Context, số tin giao và tổng số ID duy nhất. Tin mới đến sau bản xem trước không tự thay gói gửi; làm mới hoặc sửa gói phải xem trước/xác nhận lại.

### Non-Functional Requirements (NFR)

- **NFR-01**: Trích xuất local, gộp và kiểm tra gói không làm treo giao diện. Lưu báo cáo/gói tin nhất quán; phản hồi mất hoặc đến chậm không tạo thêm hồ sơ hoặc tin trùng.
- **NFR-02**: Truyền bảo mật, bảo vệ bản rõ bằng chứng và ngữ cảnh; không ghi nội dung/lý do, token hoặc khóa vào log vận hành. Không gửi khóa E2EE hoặc thực thi mã/liên kết trong gói tin.
- **NFR-03**: Lỗi báo cáo không làm mất nháp hoặc thay đổi quyền chat. Kết quả và gói tin gắn đúng hội thoại/yêu cầu; không lẫn dữ liệu khi chuyển hội thoại.
