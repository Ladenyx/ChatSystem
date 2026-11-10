# ĐẶC TẢ USE-CASE

> Chức năng đề xuất, chưa được chốt trong yêu cầu hiện tại.

## Thông tin chung (Metadata)

- **Use Case ID**: UC-USR-RPT-03
- **Use Case Name**: Yêu cầu rút báo cáo
- **Author / Create by**: tkiet24
- **Date Create**: 04/11/2026
- **Last Updated by / Date**: tkiet24 / 09/11/2026
- **Primary Actor**: Chủ báo cáo
- **Secondary Actor(s)**: Admin hệ thống xét yêu cầu rút.

## Mô tả tổng quan (Summary / Description)

- **Brief Description**: Chủ báo cáo gửi đề nghị rút kèm lý do để Admin xem xét.
- **Goal**: Yêu cầu rút hợp lệ được tiếp nhận ở trạng thái chờ xử lý.
- **Trigger**: Người dùng muốn rút một báo cáo đã gửi.

## Điều kiện (Conditions)

- **Pre-conditions**: Người dùng có phiên User Client hợp lệ, là chủ báo cáo đã được tiếp nhận và còn đáp ứng điều kiện gửi yêu cầu rút.
- **Post-conditions**: Yêu cầu rút được lưu với mã yêu cầu, báo cáo nguồn, chủ yêu cầu, lý do và thời điểm tiếp nhận; chưa đồng nghĩa báo cáo đã được rút.

## Luồng sự kiện (Flow of Events)

### Basic Flow

1. **Người dùng** chọn báo cáo muốn rút và nêu lý do.
2. **Hệ thống** kiểm tra quyền sở hữu, trạng thái báo cáo và thời hạn gửi theo chính sách được chốt; nêu rõ cần Admin xét duyệt.
3. **Người dùng** xác nhận gửi yêu cầu.
4. **Hệ thống** kiểm tra lại điều kiện, ghi nhận yêu cầu chờ xử lý và trả mã cùng thời điểm tiếp nhận.

### Alternative Flows

- **A1 — Hủy trước gửi**: Tại bước 3, người dùng không xác nhận; kết thúc.
- **A2 — Đã có yêu cầu chờ**: Tại bước 2, cung cấp trạng thái yêu cầu hiện có; chưa kết luận báo cáo đã được rút.
- **A3 — Gửi lại cùng yêu cầu**: Tại bước 4, đã tiếp nhận thì trả kết quả cũ; cùng ID khác nội dung bị từ chối.

### Exception Flows

- **E1 — Không đủ quyền hoặc điều kiện rút**: Tại bước 2 hoặc 4, sai chủ sở hữu, hết thời hạn hoặc trạng thái không còn phù hợp; từ chối yêu cầu mới.
- **E2 — Lý do không hợp lệ**: Tại bước 2, yêu cầu sửa và xác nhận lại.
- **E3 — Lỗi ghi nhận hoặc mất xác nhận**: Tại bước 4, đối chiếu yêu cầu cũ trước khi thử lại; không tạo ID mới khi chưa rõ kết quả.
- **E4 — Phiên hết hiệu lực**: Dừng yêu cầu và yêu cầu đăng nhập lại; không tự gửi bằng phiên mới.

## Business Rules & Non-Functional Requirements

- **Business Rules (BR)**:
  - **BR-01**: Chỉ chủ báo cáo được yêu cầu rút; lý do phải có nội dung và tuân giới hạn của hệ thống.
  - **BR-02**: Gửi yêu cầu không tự xóa bằng chứng, đảo ngược kết luận/chế tài hoặc gia hạn lưu dữ liệu. Admin quyết định chấp thuận hay từ chối.
  - **BR-03**: Thời hạn được kiểm tra tại server theo chính sách được chốt; ngừng chờ không hủy yêu cầu đã tiếp nhận.
- **Non-Functional Requirements (NFR)**:
  - **NFR-01**: Kiểm tra quyền và điều kiện mới nhất; gửi lại cùng yêu cầu không tạo bản ghi trùng hoặc đặt lại thời hạn.
  - **NFR-02**: Bảo vệ lý do và danh tính, chỉ cung cấp cho người có quyền; không ghi nội dung nhạy cảm vào log.
  - **NFR-03**: Mốc thời gian theo server, hiển thị theo Asia/Ho_Chi_Minh. Timeout gửi yêu cầu không phải thời hạn Admin xét duyệt.
