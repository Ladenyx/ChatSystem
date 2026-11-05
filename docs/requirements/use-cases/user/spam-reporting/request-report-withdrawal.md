# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-RPT-03
- **Use Case Name**: Yêu cầu rút báo cáo
- **Author / Create by**: Kiet
- **Date Create**: 04/11/2026
- **Last Updated by** / Date: Kiet / 04/11/2026
- **Primary Actor**: Chủ báo cáo đã được tiếp nhận.
- **Secondary Actor(s)**: Admin hệ thống nhận yêu cầu để xử lý trong UC riêng.
- **Priority**: Should Have
- **Status**: Draft

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Chủ báo cáo đề nghị rút một báo cáo đã gửi nhưng chưa được xử lý và cung cấp lý do; server tiếp nhận để Admin quyết định.
- **Goal**: Yêu cầu rút hợp lệ được ghi nhận, chưa đồng nghĩa báo cáo được rút.
- **Trigger**: User muốn rút báo cáo đã gửi và chưa được xử lý.

  > **NOTE:** Đề xuất chức năng yêu cầu rút qua Admin, cho phép gửi yêu cầu trong tối đa 15 ngày kể từ khi server tiếp nhận báo cáo và lưu bằng chứng tối đa 30 ngày; các phạm vi và thời hạn này cần được xác nhận.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - Phiên hợp lệ, User là chủ báo cáo; báo cáo chưa được xử lý và chưa được rút thành công.
  - Thời gian server còn trước hạn lưu bằng chứng E; chưa có yêu cầu rút khác đang chờ cho hồ sơ.

    > **NOTE:** Đề xuất User chỉ được gửi yêu cầu rút trong tối đa 15 ngày kể từ khi server tiếp nhận báo cáo; cần xác nhận cách tính và thời điểm hết hạn gửi.

  - Có kết nối khi gửi.

- **Post-conditions**:
  - Thành công: ghi ID yêu cầu rút, báo cáo nguồn, chủ yêu cầu, lý do, thời điểm và trạng thái chờ xử lý.
  - Báo cáo/kết luận/chế tài không tự bị đảo ngược, bản rõ không tự bị xóa hoặc gia hạn chỉ vì đã gửi yêu cầu.
  - Hủy trước gửi không tạo yêu cầu; lỗi/chưa rõ kết quả giữ thông tin để đối chiếu, không tự gửi ID mới.

## Luồng sự kiện (Flow of Events)

### Basic Flow

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Xác định báo cáo muốn yêu cầu rút. | Kiểm tra quyền sở hữu, trạng thái chưa xử lý và các điều kiện rút; cung cấp thông tin đủ để User kiểm tra đúng hồ sơ. |
| 2 | Nêu lý do và kiểm tra ý định rút. | Chuẩn bị nội dung yêu cầu; nêu rõ cần Admin chấp thuận và việc gửi không tự xóa bằng chứng/đảo ngược chế tài. |
| 3 | Xác nhận gửi yêu cầu rút. | Gắn định danh yêu cầu; server xác thực phiên, kiểm tra lại quyền sở hữu, trạng thái xử lý của báo cáo, hạn lưu và trạng thái yêu cầu rút. Nếu báo cáo đã được xử lý, áp dụng Ex 1a. |
| 4 | Chờ xác nhận. | Ghi nhất quán yêu cầu ở trạng thái chờ, trả ID và thời điểm tiếp nhận; không ghi là đã rút. |
| 5 | Nhận xác nhận tiếp nhận yêu cầu. | Hoàn tất mục tiêu gửi yêu cầu. Việc xem xét và quyết định thuộc phân hệ Admin do teammate phụ trách; xem trạng thái thuộc [UC-U-RPT-02](track-spam-report.md). |

### Alternative Flows

- **Alt 2a — Sửa lý do**: Trước bước 3, User sửa; chuẩn bị lại nội dung và xác nhận phiên bản mới.
- **Alt 3a — Hủy trước gửi**: Không tạo yêu cầu hay thay báo cáo; kết thúc.
- **Alt 3b — Đã có yêu cầu chờ**: Trả đúng trạng thái yêu cầu hiện có; không tạo yêu cầu thứ hai hoặc đặt lại hạn xử lý.
- **Alt 3c — Gửi lặp cùng yêu cầu**: Nếu đã tiếp nhận thì trả kết quả cũ; cùng ID khác dữ liệu bị từ chối, không thay lý do cũ âm thầm.

- **Alt 4a — Ngừng chờ**: Không tự hủy yêu cầu đã phát. Kết quả trễ chỉ gắn với yêu cầu/hồ sơ nguồn.

### Exception Flows

- **Ex 1a — Sai chủ sở hữu, báo cáo đã rút hoặc đã được xử lý**: Từ chối tạo yêu cầu rút mới, thông báo trạng thái phù hợp và không lộ hồ sơ của người khác. Nếu báo cáo được xử lý trước khi server tiếp nhận yêu cầu, không ghi nhận yêu cầu mới.
- **Ex 2a — Lý do không hợp lệ**: Không gửi, yêu cầu sửa theo giới hạn được chốt.

  > **NOTE:** Đề xuất lý do rút có từ 1 đến 1.000 Unicode code point sau khi loại khoảng trắng đầu/cuối; cần chốt riêng giới hạn này, chưa coi đã duyệt.

- **Ex 3a — Hết hạn gửi hoặc điều kiện đổi**: Kiểm tra bằng giờ server theo chính sách được duyệt; nếu đã hết thời hạn được phép gửi thì không tạo yêu cầu rút mới. Không sử dụng điều kiện client cũ.

  > **NOTE:** Đề xuất thời hạn gửi yêu cầu rút tối đa 15 ngày kể từ khi server tiếp nhận báo cáo; cần xác nhận cách tính ngày và xử lý tại thời điểm hết hạn gửi.

- **Ex 4a — Mạng/timeout/chưa rõ kết quả**: Đối chiếu ID cũ trước thử lại; không suy ra chắc chắn chưa tiếp nhận. Không tạo thêm yêu cầu trong khi kết quả cũ chưa xác định.
- **Ex 1b — Phiên hết hiệu lực**: Dừng thao tác cần xác thực; yêu cầu đã tiếp nhận không bị coi hủy, không tự gửi bằng phiên mới.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

- **BR-01 — Chủ thể và điều kiện yêu cầu**: Chỉ chủ báo cáo được yêu cầu; một yêu cầu chờ cho mỗi báo cáo, không phụ thuộc người bị báo cáo đang online. Tài khoản đích khóa/xóa sau tiếp nhận báo cáo không tự hủy quyền yêu cầu theo phương án gốc.

  > **NOTE:** Đề xuất giới hạn một yêu cầu đang chờ cho mỗi báo cáo và giữ quyền yêu cầu khi tài khoản đích khóa/xóa sau tiếp nhận; cần xác nhận.

- **BR-02 — Phạm vi và hệ quả yêu cầu rút**: Chỉ được gửi yêu cầu rút khi báo cáo chưa được xử lý. Gửi yêu cầu không đồng nghĩa chấp thuận; không tự xóa bằng chứng, đặt lại hạn lưu hoặc thay chế tài.

  > **NOTE:** Đề xuất cần thống nhất cách xử lý yêu cầu rút đã tiếp nhận nhưng còn đang chờ khi báo cáo được xử lý.

- **BR-03 — Thời hạn gửi yêu cầu rút**: Yêu cầu rút không gia hạn thời hạn xử lý báo cáo gốc.

  > **NOTE:** Đề xuất thời hạn gửi yêu cầu rút tối đa 15 ngày kể từ khi server tiếp nhận báo cáo. Cần xác nhận cách tính thời hạn, quan hệ với hạn lưu bằng chứng và cách xử lý yêu cầu đã tiếp nhận nhưng còn chờ.

- **BR-04 — Xử lý các trường hợp chưa có chính sách**: Không tự quyết định kết quả khi quá hạn.

  > **NOTE:** Đề xuất cần chốt cách tính thời hạn gửi 15 ngày từ khi tiếp nhận báo cáo, lý do rút, trạng thái yêu cầu đã tiếp nhận khi báo cáo được xử lý hoặc thời hạn gửi kết thúc, quan hệ với hạn lưu bằng chứng và Feature ID.

- **BR-05**: Hủy ý định trước gửi khác rút báo cáo đã gửi và khác Admin xử lý yêu cầu rút; ba hành động không gộp thành một Alternative Flow của UC gửi báo cáo.

### Non-Functional Requirements (NFR)

- **NFR-01**: Xác thực chủ yêu cầu, kiểm tra đồng thời và idempotency; không tạo thêm yêu cầu/hạn mới do reconnect.
- **NFR-02**: Bảo vệ lý do/danh tính, không ghi nội dung vào log vận hành; lưu/xóa theo chính sách sau khi chốt, không sao chép lý do sang nơi giữ vượt E.
- **NFR-03**: Thời gian xét hạn dựa server, hiển thị theo Asia/Ho_Chi_Minh; timeout API không là deadline xử lý Admin.
