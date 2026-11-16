# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-ADM-SPAM-02
- **Use Case Name**: Xem chi tiết và bằng chứng báo cáo spam
- **Author / Create by**: ladenyx
- **Date Create**: 2026-10-30
- **Last Updated by / Date**: ladenyx / 2026-11-07
- **Primary Actor**: Admin hệ thống
- **Secondary Actor(s)**: None

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Admin kiểm tra thông tin chi tiết của một báo cáo spam gồm người gửi báo cáo, tài khoản bị báo cáo, lý do báo cáo và bản chụp các tin nhắn bằng chứng do người báo cáo cung cấp.
- **Goal**: Có đủ ngữ cảnh và bằng chứng để đánh giá hành vi vi phạm trước khi quyết định có khóa tài khoản bị báo cáo hay không.
- **Trigger**: Admin chọn một báo cáo spam từ danh sách tra cứu ([UC-ADM-SPAM-01](UC-ADM-SPAM-01-browse-spam-reports.md)).

## Điều kiện (Conditions)

- **Pre-conditions**: Admin có phiên quản trị hợp lệ và quyền Admin hệ thống; định danh của báo cáo spam cần xem đã được xác định từ [UC-ADM-SPAM-01](UC-ADM-SPAM-01-browse-spam-reports.md).
- **Post-conditions**: Thông tin chi tiết và bằng chứng đính kèm của đúng báo cáo được cung cấp cho Admin; trạng thái báo cáo và trạng thái tài khoản liên quan không bị thay đổi bởi thao tác xem.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Admin** yêu cầu xem chi tiết một báo cáo spam đã chọn.
2. **Hệ thống** kiểm tra phiên, quyền Admin hệ thống và truy xuất hồ sơ báo cáo theo định danh đã chọn.
3. **Hệ thống** cung cấp thông tin định danh người báo cáo, tài khoản bị báo cáo (kèm trạng thái tài khoản hiện tại), thời điểm gửi, lý do báo cáo và tập tin nhắn bằng chứng đính kèm (gồm các tin nhắn bị báo cáo cùng tối đa 30 tin nhắn ngữ cảnh gần nhất được trích xuất từ phía người gửi báo cáo).
4. **Admin** xem xét nội dung bằng chứng và kết thúc kiểm tra hoặc chuyển sang xử lý khóa tài khoản theo [UC-ADM-SPAM-03](UC-ADM-SPAM-03-lock-reported-account.md).

### Alternative Flows

**Alternative flow 1: Cuộc trò chuyện có ít hơn 30 tin nhắn ngữ cảnh**
1. Từ bước #3 của Basic Flow, tổng số tin nhắn ngữ cảnh được đính kèm tại thời điểm gửi báo cáo ít hơn 30 tin nhắn.
2. **Hệ thống** cung cấp đúng số lượng tin nhắn thực tế có trong bản chụp báo cáo.
3. Tiếp tục bước #4 trong Basic Flow.

**Alternative flow 2: Tài khoản liên quan bị purge trong lúc mở báo cáo**
1. Từ bước #2 hoặc #3 của Basic Flow, bất kỳ tài khoản liên quan — người báo cáo, tài khoản đích, người gửi tin bằng chứng (kể cả chỉ là Context) hoặc Admin xử lý — bị xóa vĩnh viễn theo chính sách vòng đời dữ liệu.
2. **Hệ thống** chuyển toàn bộ hồ sơ sang metadata archive ẩn danh và ghi tombstone theo quyết định bổ sung ngày 16/11/2026 tại [DEC-10](../../../../decisions/spam-moderation-and-retention.md#purge-tài-khoản-và-retry), kể cả chưa đủ 180 ngày; không trả phần bằng chứng còn lại, ánh xạ mã cũ sang archive hoặc giữ chi tiết cũ trên giao diện/cache.
3. **Hệ thống** thông báo hồ sơ không còn khả dụng, hủy ngữ cảnh chi tiết và quay về danh sách tại [UC-ADM-SPAM-01](UC-ADM-SPAM-01-browse-spam-reports.md). Kết thúc Use Case.

### Exception Flows

**Exception flow 1: Báo cáo không còn tồn tại**
1. Từ bước #2 của Basic Flow, định danh báo cáo không còn hợp lệ hoặc bản ghi báo cáo không còn khả dụng.
2. **Hệ thống** thông báo không tìm thấy báo cáo, hủy ngữ cảnh chi tiết cũ để tránh nhầm với báo cáo trước đó và quay về danh sách tại [UC-ADM-SPAM-01](UC-ADM-SPAM-01-browse-spam-reports.md).
3. Kết thúc Use Case.

**Exception flow 2: Lỗi kết nối hoặc lỗi tải dữ liệu bằng chứng**
1. Từ bước #2 hoặc #3 của Basic Flow, quá trình tải chi tiết báo cáo hoặc tập tin nhắn bằng chứng gặp lỗi.
2. **Hệ thống** thông báo lỗi truy xuất dữ liệu, cho phép thử tải lại và không khẳng định bằng chứng của báo cáo đã đầy đủ.
3. Kết thúc Use Case.

**Exception flow 3: Phiên quản trị hết hiệu lực hoặc không đủ quyền**
1. Từ bước #2 của Basic Flow, **Hệ thống** phát hiện phiên quản trị không hợp lệ hoặc không có quyền Admin hệ thống.
2. **Hệ thống** từ chối cung cấp chi tiết báo cáo và yêu cầu xác thực lại theo [UC-ADM-AUTH-01](../authentication/UC-ADM-AUTH-01-admin-sign-in.md).
3. Kết thúc Use Case.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Admin hệ thống không có quyền truy cập toàn bộ lịch sử hội thoại riêng tư hay tự giải mã nhóm chat E2EE trên máy chủ. Toàn bộ nội dung tin nhắn xem được trong báo cáo chỉ đến từ bản rõ do người gửi báo cáo chủ động xác nhận cung cấp (tin vi phạm kèm tối đa 30 tin nhắn gần nhất) theo [DEC-05](../../../../decisions/group-permissions-and-e2ee.md) và [DEC-07](../../../../decisions/account-lifecycle.md).
  - **BR-02**: Hệ thống phải ghi chú rõ bằng chứng bản rõ là dữ liệu do người báo cáo gửi kèm để Admin tự thẩm định, không mặc định máy chủ đã xác thực mật mã nội dung E2EE gốc.
  - **BR-03**: Hệ thống phải phân biệt tách bạch vai trò giữa **người gửi báo cáo** và **tài khoản bị báo cáo** để tránh nhầm đối tượng khi chuyển sang bước xử lý khóa tài khoản.
  - **BR-04**: Sau khi xem, Admin có thể khởi tạo thao tác riêng **Đánh dấu đã xử lý kèm ghi chú** theo [DEC-10](../../../../decisions/spam-moderation-and-retention.md#đánh-dấu-đã-xử-lý). Bản thân thao tác xem không chuyển `PENDING` thành `REVIEWED` và không sửa ghi chú.
  - **BR-05**: Sau 180 ngày từ tiếp nhận, hồ sơ gốc không còn được mở bằng mã cũ; từ chối tải chi tiết và làm mới danh sách theo [DEC-10](../../../../decisions/spam-moderation-and-retention.md#lưu-nội-dung-180-ngày-và-metadata-ẩn-danh). Không trả lại bản rõ/lý do/ghi chú hoặc định danh đã loại và không ánh xạ mã cũ sang archive. Archive là nguồn metadata/thống kê ẩn danh riêng, không phải hồ sơ thay thế trong thao tác xem này.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Khi **Admin** chuyển nhanh sang báo cáo khác, dữ liệu phản hồi trễ của báo cáo cũ không được hiển thị chồng lên báo cáo mới.
  - **NFR-02**: Mốc thời gian của báo cáo và tin nhắn bằng chứng được quy đổi nhất quán theo múi giờ `Asia/Ho_Chi_Minh`.
