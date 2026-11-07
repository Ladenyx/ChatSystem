# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-RPT-03
- **Use Case Name**: Yêu cầu rút báo cáo
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 07/11/2026
- **Primary Actor**: Chủ báo cáo đã được tiếp nhận.
- **Secondary Actor(s)**: Admin hệ thống nhận yêu cầu để xử lý trong UC riêng.
- **Priority**: Should Have
- **Status**: Proposed

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Chủ báo cáo gửi đề nghị rút kèm lý do; server tiếp nhận để Admin hệ thống xem xét và quyết định.
- **Goal**: Yêu cầu rút hợp lệ được ghi nhận, chưa đồng nghĩa báo cáo được rút.
- **Trigger**: User muốn đề nghị rút một báo cáo đã gửi.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ, User là chủ báo cáo; hồ sơ và User còn đáp ứng điều kiện gửi yêu cầu rút.
  - Yêu cầu còn trong thời hạn gửi của hệ thống; trạng thái báo cáo và yêu cầu rút hiện có đã được đối chiếu.

  - Có kết nối khi gửi.

- **Post-conditions**:
  - Thành công: ghi ID yêu cầu rút, báo cáo nguồn, chủ yêu cầu, lý do, thời điểm và trạng thái chờ xử lý.
  - Báo cáo/kết luận/chế tài không tự bị đảo ngược, bản rõ không tự bị xóa hoặc gia hạn chỉ vì đã gửi yêu cầu.
  - Hủy trước gửi không tạo yêu cầu; lỗi/chưa rõ kết quả giữ thông tin để đối chiếu, không tự gửi ID mới.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Xác định báo cáo muốn yêu cầu rút. | Kiểm tra quyền sở hữu, trạng thái báo cáo và các điều kiện rút; cung cấp thông tin đủ để User kiểm tra đúng hồ sơ. |
| 2 | Nêu lý do và kiểm tra ý định rút. | Chuẩn bị nội dung yêu cầu; nêu rõ cần Admin chấp thuận và việc gửi không tự xóa bằng chứng/đảo ngược chế tài. |
| 3 | Xác nhận gửi yêu cầu rút. | Gắn định danh yêu cầu; server xác thực phiên, kiểm tra lại quyền sở hữu, điều kiện trạng thái báo cáo, thời hạn gửi và yêu cầu rút hiện có. |
| 4 | Chờ xác nhận. | Ghi nhất quán yêu cầu ở trạng thái chờ, trả ID và thời điểm tiếp nhận; không ghi là đã rút. |
| 5 | Nhận xác nhận tiếp nhận yêu cầu. | Thông báo yêu cầu đã được tiếp nhận, chưa đồng nghĩa báo cáo đã được rút. Việc xem xét và quyết định do Admin hệ thống thực hiện. |

### Alternative Flows

- **Alt 2a — Sửa lý do**: Trước bước 3, User sửa; chuẩn bị lại nội dung và xác nhận phiên bản mới.
- **Alt 3a — Hủy trước gửi**: Không tạo yêu cầu hay thay báo cáo; kết thúc.
- **Alt 3b — Đã có yêu cầu chờ**: Hiển thị trạng thái yêu cầu hiện có; không coi việc yêu cầu đang chờ là báo cáo đã được rút.
- **Alt 3c — Gửi lặp cùng yêu cầu**: Nếu đã tiếp nhận thì trả kết quả cũ; cùng ID khác dữ liệu bị từ chối, không thay lý do cũ âm thầm.

- **Alt 4a — Ngừng chờ**: Không tự hủy yêu cầu đã phát. Kết quả trễ chỉ gắn với yêu cầu/hồ sơ nguồn.

### Exception Flows

- **Ex 1a — Sai chủ sở hữu hoặc hồ sơ không còn đủ điều kiện**: Từ chối tạo yêu cầu rút mới, thông báo trạng thái phù hợp và không lộ hồ sơ của người khác.
- **Ex 2a — Lý do không hợp lệ**: Không gửi, thông báo điều kiện không đạt và yêu cầu sửa theo giới hạn của hệ thống.

- **Ex 3a — Hết hạn gửi hoặc điều kiện đổi**: Đối chiếu điều kiện và thời hạn hiện tại tại server; nếu đã hết thời hạn được phép gửi thì không tạo yêu cầu rút mới. Không sử dụng điều kiện client cũ.

- **Ex 4a — Mạng/timeout/chưa rõ kết quả**: Đối chiếu ID cũ trước thử lại; không suy ra chắc chắn chưa tiếp nhận. Không tạo thêm yêu cầu trong khi kết quả cũ chưa xác định.
- **Ex 1b — Phiên hết hiệu lực**: Dừng thao tác cần xác thực; yêu cầu đã tiếp nhận không bị coi hủy, không tự gửi bằng phiên mới.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

- **BR-01 — Chủ thể**: Chỉ chủ báo cáo có quyền gửi yêu cầu rút cho đúng hồ sơ; không suy quyền từ việc biết ID báo cáo hoặc trạng thái online của người bị báo cáo.
- **BR-02 — Hệ quả**: Gửi yêu cầu không đồng nghĩa Admin đã chấp thuận, không tự xóa bằng chứng hoặc thay đổi chế tài.
- **BR-03 — Thời hạn gửi**: Chỉ tiếp nhận khi còn trong thời hạn gửi của hệ thống; yêu cầu rút không tự gia hạn xử lý báo cáo gốc hoặc thời hạn lưu dữ liệu.
- **BR-04 — Lý do**: Lý do bắt buộc có nội dung và nằm trong giới hạn của hệ thống; không tự sửa lý do hoặc nội dung báo cáo gốc.
- **BR-05**: Hủy ý định trước gửi khác rút báo cáo đã gửi và khác Admin xử lý yêu cầu rút; ba hành động không gộp thành một Alternative Flow của UC gửi báo cáo.

### Non-Functional Requirements (NFR)

- **NFR-01**: Xác thực chủ yêu cầu, kiểm tra đồng thời và idempotency; không tạo thêm yêu cầu/hạn mới do reconnect.
- **NFR-02**: Bảo vệ lý do và danh tính, chỉ cung cấp cho người có quyền; không ghi nội dung nhạy cảm vào log vận hành.
- **NFR-03**: Thời gian xét hạn dựa server, hiển thị theo Asia/Ho_Chi_Minh; timeout API không là deadline xử lý Admin.
