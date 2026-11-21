# Kế hoạch UI/UX Design — Main Workspace Shell

**Người giao việc:** PM · **Người thực hiện:** UI/UX Designer  
**Ngày lập:** 18/11/2026 · **Trạng thái:** Giai đoạn 1 đã được duyệt ngày 18/11/2026; PM-01/PM-02 còn mở  
**Đích bàn giao:** File Figma Chat System · **Ưu tiên visual:** Light mode; Dark làm sau

## 1. Mục tiêu và nguồn quyết định

Hoàn thiện khung làm việc để người dùng đọc/gửi tin, tìm lại nội dung, quản lý quan hệ và nhóm, dùng AI, báo cáo spam theo đúng quyền hiện tại. Designer phải thể hiện được thao tác bắt đầu, kết quả, hủy và phục hồi lỗi; developer/QA có thể lần từ use case tới frame và chạy prototype.

**Thứ tự bắt buộc: hi-fi wireframe → prototype → áp dụng design system vào wireframe.**

Trong kế hoạch này, hi-fi wireframe là bố cục chi tiết, dữ liệu/nội dung thật, control và trạng thái đầy đủ trong hình thức trung tính. Visual hi-fi Light hoàn thiện ở bước cuối. Có thể dùng component CS từ đầu ở mode Wireframe để tránh dựng lại, nhưng nghiệm thu nghiệp vụ/prototype trước khi áp visual Light.

### 1.1. Nguồn chuẩn

- [Screen Map](docs/requirements/screen.md), mục 1.2: U-SCR-07/08/09 và Icon Rail; mục 1.3: các hộp thoại liên quan.
- [Use case User](docs/requirements/use-cases/user/): nguồn cho điều kiện, Basic/Alternative/Exception Flow, BR và NFR. Các mã rút gọn MSG/FRD/GRP/AI/RPT trong tài liệu này tương ứng UC-USR-…; link nguồn ở cuối file.
- [Trang wireframe Figma](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=27-1054): nền để tái sử dụng và sửa các điểm đã rà soát.
- [Design system CS](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=7-2), [Foundations](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=19-71), [mẫu Chat Light](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=23-952): nguồn visual/component/token; không dùng mẫu DS để tự thêm tính năng.

Đường dẫn thực tế là **docs/requirements/screen.md**; dự án hiện không có docs/screen.md tại vị trí người dùng gọi ban đầu.

### 1.2. Phạm vi và nguyên tắc

Thiết kế U-SCR-07, U-SCR-08, U-SCR-09; U-DLG-02 tạo nhóm, U-DLG-03 AI, U-DLG-04 báo cáo, U-DLG-05 kế nhiệm. U-DLG-01 Hồ sơ & Bảo mật, U-SCR-06 khóa và U-SCR-01 đăng nhập dùng frame hiện có để nối điểm vào/ra, không mở rộng redesign cụm xác thực. Không làm Admin App.

Mọi quyết định nghiệp vụ phải có nguồn UC/Screen Map. Quy ước tên frame, layout, dữ liệu mẫu và trigger trình diễn là quyết định bàn giao thiết kế của PM, không phải business rule mới. Nếu nguồn chưa chốt thì ghi **CẦN PM CHỐT**, không để designer tự chọn hành vi.

RPT-02 theo dõi báo cáo và RPT-03 rút báo cáo ngoài phạm vi hiện tại theo ghi chú trong các tài liệu đó. Dark mode là backlog sau Light, không nhân đôi toàn bộ frame trong đợt này.

### 1.3. Hiện trạng cần sửa trước

Theo lần rà soát Figma trong phiên làm việc: có 52 frame WF-07 và 20 frame WF-08. WF-08 có nội dung nhưng chưa có reaction prototype; rail Bạn bè thiếu Đăng xuất. Drawer nhóm mới chỉ liệt kê thành viên. Gửi ở chat thường, một số action nhóm và Chặn ở drawer cá nhân chưa nối. Một số đường đổi sai hội thoại/vai trò: nhóm mới và nhóm chỉ đọc đều mở drawer Nhóm Java đang hoạt động. Nhánh lỗi AI/nháp xung đột có frame nhưng cần nối từ luồng review.

Đây là hiện trạng review; khi bắt đầu thiết kế cần ghi lại node mới/thay đổi vào bảng, không kết luận rằng cấu hình Figma chưa từng thay đổi.

## 2. Cách tổ chức và bàn giao cho designer

### 2.1. Ba giai đoạn và điều kiện qua bước

| Giai đoạn | Công việc | Sản phẩm | Điều kiện qua bước |
|---|---|---|---|
| 1. Hi-fi wireframe | Hoàn thiện 72 frame nền và các state bổ sung; dữ liệu, quyền, nội dung, layout chi tiết | Bộ frame trung tính có mã, nguồn UC và annotation | PM duyệt nội dung/quyền; không thiếu state bắt buộc; điểm nguồn còn mở có nhãn |
| 2. Prototype | Nối mọi hành động và nhánh scenario; đúng hội thoại/nháp/quyền | Flow chạy từ điểm bắt đầu; bảng interaction và scenario review | Không có control chính bị cụt; flow thành công/hủy/lỗi chạy được |
| 3. Apply DS | Dùng instance/style/token CS và mode Light, xử lý visual | Toàn bộ frame Light, component/token map, ảnh QA | Kiểm tra visual và chạy lại prototype sau đổi component |

### 2.2. Quy ước frame và state

- Giữ mã nền WF-07-xx, WF-08-xx để truy vết. Mã bổ sung ở mục 4 là mã giao việc, không phải mã screen sản phẩm mới.
- Các state dùng chung có thể là variant/overlay; phải có một hàng riêng trong danh mục và đường mở để reviewer xem được. Không buộc tạo một desktop trùng nhau cho mọi lỗi.
- Đặt section theo screen/flow, tách khu component và bảng Scenario Review khỏi viewport sản phẩm. Dùng cùng bộ frame qua ba bước, giữ lịch sử milestone/version để so sánh.
- Designer bổ sung cột Link cuối và Trạng thái (Chưa làm / Đang làm / Chờ PM / Sẵn sàng review / Đạt) khi bàn giao.
- Mỗi frame ghi annotation ngoài viewport: UC + bước/BR, điều kiện đầu vào, quyền, dữ liệu mẫu, control bị khóa và lý do, đường đi/đường về.
- Layout desktop nền 1440 × 900 theo khung hiện có; tiêu đề review đặt bên ngoài. Dùng auto-layout, kiểm tra tên/nội dung dài, drawer và overlay không che CTA. Đây là quy ước thiết kế, không tự suy ra breakpoint/min-size sản phẩm.
- Giữ một fixture cá nhân (Minh Anh), một nhóm đang hoạt động (Nhóm Java), một nhóm mới (Nhóm Ôn tập Java); tên, thành viên, vai trò và nháp phải đi theo đúng fixture trong toàn flow.

### 2.3. Hợp đồng mỗi task

Mỗi task có: **mục tiêu → nguồn UC/bước → frame nền/state bổ sung → nội dung UI → prototype → nhánh hủy/lỗi → tiêu chí đạt → link bàn giao**. Các bảng dưới là backlog giao việc; designer phải điền link kết quả chứ không thay mô tả bằng “đã thiết kế”.

Mỗi interaction phải ghi: **nguồn / control và trigger / điều kiện / đích / dữ liệu giữ hoặc đổi / đóng-quay lại / nguồn UC**. Prototype dùng fixture mô phỏng; các bảo đảm server/E2EE/chống trùng cần annotation cho dev và kiểm thử hệ thống riêng.

## 3. Backlog frame hiện có — từng frame cần hoàn thiện

Tất cả frame dưới đây đều phải qua bước 1, 2 và 3. “Sửa” bao gồm tái sử dụng nội dung đã đúng, bổ sung phần thiếu và chuẩn hóa Light ở giai đoạn cuối.

### 3.1. U-SCR-07 và hộp thoại liên quan

| Frame và link hiện tại | Task | Nguồn | Yêu cầu cụ thể | Link cuối | Trạng thái |
|---|---|---|---|---|---|
| [WF-07-01 / Chưa chọn hội thoại](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-134) | UX-02 | [MSG-01] | Chọn người/nhóm mở đúng khung; khi chưa chọn giữ empty state. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-134) | GĐ1: đã duyệt |
| [WF-07-02 / Chat cá nhân — Bình thường](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-251) | UX-04 | [MSG-01], [MSG-02] | Nối Gửi, AI, Thao tác; không yêu cầu kết bạn/online để gửi. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-251) | GĐ1: đã duyệt |
| [WF-07-03 / Chat nhóm E2EE — Bình thường](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-418) | UX-04 | [MSG-01], [MSG-02] | Nối Gửi, hai nút AI, menu tin và drawer theo vai trò. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-418) | GĐ1: đã duyệt |
| [WF-07-04 / Tìm kiếm tổng hợp](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-609) | UX-03 | [MSG-03] | Hai nhóm kết quả; nối cả kết quả tên và tin; báo phạm vi chưa đồng bộ. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-609) | GĐ1: đã duyệt |
| [WF-07-05 / Nhảy tới tin nhắn đích](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-739) | UX-03 | [MSG-01], [MSG-03] | Highlight đúng tin, về tin mới nhất; chỉ giữ một control về tin mới nhất. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-739) | GĐ1: đã duyệt |
| [WF-07-06 / Mở panel thông tin](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-902) | UX-08 | [MSG-03], [MSG-05], [FRD-06] | Nối Chặn và các thao tác cá nhân; đóng không đổi hội thoại/nháp. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-902) | GĐ1: đã duyệt |
| [WF-07-07 / Chưa có hội thoại](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1085) | UX-02 | [MSG-01] | Đi Bạn bè hoặc Tạo nhóm; không dùng empty state này thay lỗi tải. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1085) | GĐ1: đã duyệt |
| [WF-07-08 / Hội thoại chưa có tin nhắn](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1152) | UX-02 | [MSG-01], [AI-01], [AI-02] | Refine tắt khi nháp rỗng; Recommend tắt khi chưa có tin đọc được. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1152) | GĐ1: đã duyệt |
| [WF-07-09 / Tìm kiếm không có kết quả](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1283) | UX-03 | [MSG-03] | Giữ từ khóa/phạm vi; xóa từ khóa về danh sách đúng ngữ cảnh. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1283) | GĐ1: đã duyệt |
| [WF-07-10 / Đang tải / đồng bộ lịch sử](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1400) | UX-03 | [MSG-01] | 50 tin/trang, phần đã giải mã và tiến độ; thêm đầu lịch sử. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1400) | GĐ1: đã duyệt |
| [WF-07-11 / Tải lịch sử thất bại](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1575) | UX-03 | [MSG-01] | Giữ dữ liệu cũ; thử lại trang thất bại của cùng hội thoại. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1575) | GĐ1: đã duyệt |
| [WF-07-12 / Tin đang chờ / cần thử lại](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1725) | UX-04 | [MSG-02] | Phân biệt chờ xác nhận với bị từ chối quyền; retry không sinh tin trùng. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1725) | GĐ1: đã duyệt |
| [WF-07-13 / Menu thao tác một tin nhắn](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1879) | UX-05 | [MSG-04], [MSG-06] | Thêm biến thể tin nhận/quá hạn; không cho Admin thu hồi tin người khác. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-1879) | GĐ1: đã duyệt |
| [WF-07-14 / Chọn nhiều tin để xóa](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-2047) | UX-05 | [MSG-04] | Chọn/bỏ chọn, số lượng, hủy, xác nhận; giữ đúng tập tin. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-2047) | GĐ1: đã duyệt |
| [WF-07-15 / Danh sách thành viên đã xem](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-2194) | UX-04 | [MSG-01] | Đúng tin, từng người nhận; đóng về nhóm nguồn. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-2194) | GĐ1: đã duyệt |
| [WF-07-16 / Mình chủ động chặn](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-2403) | UX-06 | [FRD-06], [FRD-07] | Tắt soạn/Gửi/AI; bỏ chặn chỉ gỡ chiều của mình. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-2403) | GĐ1: đã duyệt |
| [WF-07-17 / Bị đối phương chặn](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-2541) | UX-06 | [MSG-02], [FRD-07] | Không có nút gỡ chiều chặn của đối phương; giữ nháp. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-2541) | GĐ1: đã duyệt |
| [WF-07-18 / Nhóm có quan hệ chặn](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-2688) | UX-06 | [MSG-01], [MSG-02], [FRD-06] | Đọc/gửi nhóm tiếp tục; banner chỉ cho người liên quan. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-2688) | GĐ1: đã duyệt |
| [WF-07-19 / Đã rời nhóm — Chỉ đọc](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-2851) | UX-06 | [MSG-01], [GRP-02] | Giữ phạm vi lịch sử được đọc; drawer không mở lại quyền gửi. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-2851) | GĐ1: đã duyệt |
| [WF-07-20 / Thiếu khóa E2EE](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-3009) | UX-06 | [MSG-01], [MSG-02] | Nối U-SCR-06; thiếu khóa gửi giữ nháp, không gửi bản rõ thay thế. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-3009) | GĐ1: đã duyệt |
| [WF-07-21 / Tìm trong hội thoại hiện tại](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=321-9254) | UX-03 | [MSG-03] | Thêm nhiều kết quả, Trước/Sau và giới hạn đầu/cuối. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=321-9254) | GĐ1: đã duyệt |
| [WF-07-22 / Kết quả không còn khả dụng](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=321-9467) | UX-03 | [MSG-03] | Loại kết quả mất quyền/xóa/thu hồi; giữ từ khóa để chọn lại. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=321-9467) | GĐ1: đã duyệt |
| [WF-07-23 / Xác nhận xóa một tin](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=321-9600) | UX-05 | [MSG-04] | Định danh đúng tin; Hủy giữ nguyên, xác nhận mới gửi yêu cầu. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=321-9600) | GĐ1: đã duyệt |
| [WF-07-24 / Xác nhận xóa nhiều tin](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=321-9820) | UX-05 | [MSG-04] | Nêu số lượng/phạm vi; lựa chọn đổi cần kiểm tra lại. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=321-9820) | GĐ1: đã duyệt |
| [WF-07-25 / Đã xóa một tin phía mình](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=322-9777) | UX-05 | [MSG-04] | Tin biến mất phía mình; không hiển thị dấu thu hồi. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=322-9777) | GĐ1: đã duyệt |
| [WF-07-26 / Đã xóa hai tin phía mình](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=322-9983) | UX-05 | [MSG-04] | Chỉ loại tin xác nhận; tin không chọn/tin mới còn nguyên. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=322-9983) | GĐ1: đã duyệt |
| [WF-07-27 / Xác nhận thu hồi tin](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=322-10199) | UX-05 | [MSG-06] | Đúng người gửi, quyền hiện tại và thời hạn server. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=322-10199) | GĐ1: đã duyệt |
| [WF-07-28 / Tin đã được thu hồi](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=322-10420) | UX-05 | [MSG-06] | Dấu thu hồi thay nội dung; annotation đồng bộ lịch sử/tìm kiếm. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=322-10420) | GĐ1: đã duyệt |
| [WF-07-29 / Thu hồi đã hết thời hạn](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=323-10289) | UX-05 | [MSG-06] | Chỉ dẫn xóa phía mình; không cho retry thu hồi quá hạn. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=323-10289) | GĐ1: đã duyệt |
| [WF-07-30 / Bị xóa khỏi nhóm — Chỉ đọc](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=323-10463) | UX-06 | [MSG-01], [GRP-07] | Chỉ giữ lịch sử từng đọc; không nhận/gửi tin mới; drawer đúng trạng thái. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=323-10463) | GĐ1: đã duyệt |
| [WF-07-31 / Thiếu khóa cũ — Vẫn gửi tin mới](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=323-10608) | UX-06 | [MSG-01], [MSG-02] | Có khóa gửi hiện tại mới bật gửi; không dùng tin chưa giải mã cho AI. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=323-10608) | GĐ1: đã duyệt |
| [WF-07-32 / AI Chỉnh sửa — Xem trước chia sẻ](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=323-10790) | UX-10 | [AI-01] | Đầu vào chỉnh được, kiểu diễn đạt, xác nhận bản rõ; không gửi kèm lịch sử. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=323-10790) | GĐ1: đã duyệt |
| [WF-07-33 / AI Gợi ý — Chọn và xem trước ngữ cảnh](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=324-10828) | UX-10 | [AI-02] | Chọn/sửa ngữ cảnh cùng hội thoại; xem trước đúng tập tin theo thứ tự. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=324-10828) | GĐ1: đã duyệt |
| [WF-07-34 / AI — Chọn phương án làm nháp](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=324-11069) | UX-10 | [AI-01] | Danh sách phương án; chọn một, dùng làm nháp hoặc tạo lại. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=324-11069) | GĐ1: đã duyệt |
| [WF-07-35 / AI không khả dụng — Giữ nháp](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=324-11291) | UX-10 | [AI-01], [AI-02] | Biến thể theo chế độ; thử lại về đúng preview, không luôn về Refine. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=324-11291) | GĐ1: đã duyệt |
| [WF-07-36 / Nháp đã thay đổi — Xác nhận thay thế](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=324-11510) | UX-10 | [AI-01], [AI-02] | So sánh nháp mới và phương án; nối nhánh xung đột vào luồng chính. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=324-11510) | GĐ1: đã duyệt |
| [WF-07-37 / Xác nhận xóa lịch sử phía mình](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=325-11390) | UX-05 | [MSG-05] | Chốt phạm vi trước xác nhận; nêu lịch sử người khác vẫn giữ. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=325-11390) | GĐ1: đã duyệt |
| [WF-07-38 / Lịch sử đã xóa — Sẵn sàng nhận tin mới](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=325-11596) | UX-05 | [MSG-05] | Nhận tin mới ngoài phạm vi; không tải lại tin đã xóa từ cache. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=325-11596) | GĐ1: đã duyệt |
| [WF-07-39 / Đã áp dụng AI — Chưa gửi tin](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=325-11735) | UX-10 | [AI-01], [AI-02], [MSG-02] | Đúng nháp/hội thoại; Gửi là thao tác riêng. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=325-11735) | GĐ1: đã duyệt |
| [WF-07-40 / Giữ bản nháp mới nhất](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=325-11943) | UX-10 | [AI-01], [AI-02] | Giữ nguyên nháp mới khi hủy/đóng xác nhận. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=325-11943) | GĐ1: đã duyệt |
| [WF-07-41 / Bộ lọc Chưa đọc](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=326-12305) | UX-02 | [MSG-01] | Chỉ đổi danh sách; giữ khung chat và nháp hiện tại. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=326-12305) | GĐ1: đã duyệt |
| [WF-07-42 / Bộ lọc Nhóm](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=326-12509) | UX-02 | [MSG-01] | Sửa đường từ chat cá nhân: không tự chuyển khung giữa sang nhóm. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=326-12509) | GĐ1: đã duyệt |
| [WF-07-43 / Tạo nhóm — Xác nhận thành viên](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=326-12688) | UX-09 | [GRP-01] | Tên, tối thiểu một bạn; được chọn người offline; xác nhận trước tạo. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=326-12688) | GĐ1: đã duyệt |
| [WF-07-44 / Báo cáo spam — Xem trước bằng chứng](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=326-12920) | UX-11 | [RPT-01] | Hiện toàn bộ gói, nhãn, số lượng, lý do và bên nhận bản rõ. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=326-12920) | GĐ1: đã duyệt |
| [WF-07-45 / Gửi thành công — Server đã lưu](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=327-12411) | UX-04 | [MSG-02] | Đã lưu không đồng nghĩa đã nhận/đọc; nháp chỉ xóa khi gửi được giữ an toàn. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=327-12411) | GĐ1: đã duyệt |
| [WF-07-46 / AI đang xử lý](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=327-12625) | UX-10 | [AI-01], [AI-02] | Cả hai mode dùng state này; phản hồi cũ không áp dụng nhầm; nối lỗi/kết quả. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=327-12625) | GĐ1: đã duyệt |
| [WF-07-47 / Đã bỏ chặn — Kiểm tra lại quyền](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=327-12843) | UX-06 | [FRD-07] | Thêm nhánh đối phương vẫn chặn; không tự khôi phục tình bạn. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=327-12843) | GĐ1: đã duyệt |
| [WF-07-48 / Panel thông tin nhóm — Thành viên](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=327-13050) | UX-08 | [GRP-02], [GRP-03], [GRP-04], [GRP-05], [GRP-06], [GRP-07] | Bổ sung đủ control và variant vai trò/nhóm nguồn; bỏ panel cố định Nhóm Java. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=327-13050) | GĐ1: đã duyệt |
| [WF-07-49 / Nhóm vừa tạo — Chưa có tin](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=328-12963) | UX-09 | [GRP-01] | Nhóm Ôn tập Java, 2 người, mình Admin; drawer phải giữ đúng dữ liệu này. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=328-12963) | GĐ1: đã duyệt |
| [WF-07-50 / Báo cáo spam — Chọn tin và lý do](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=328-13145) | UX-11 | [RPT-01] | Chọn 1–20 tin hợp lệ của cùng tài khoản đích, lý do bắt buộc. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=328-13145) | GĐ1: đã duyệt |
| [WF-07-51 / Báo cáo đã được tiếp nhận](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=328-13379) | UX-11 | [RPT-01] | Mã hồ sơ và thời điểm server tiếp nhận; không tự chặn/xóa/rời nhóm. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=328-13379) | GĐ1: đã duyệt |
| [WF-07-52 / AI Gợi ý — Phương án trả lời](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=328-13586) | UX-10 | [AI-02] | Kết quả và tạo lại giữ mode Recommend; dùng làm nháp đúng hội thoại. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=328-13586) | GĐ1: đã duyệt |

### 3.2. U-SCR-08 — Bạn bè & Kết nối

Task UX-07 phụ trách cả 20 frame; UX-01 phụ trách rail dùng chung. Nối từng sub-tab và action, không chỉ nối các frame vào một bản đồ đọc.

| Frame và link hiện tại | Nguồn | Yêu cầu cụ thể | Link cuối | Trạng thái |
|---|---|---|---|---|
| [WF-08-01 / Tất cả bạn bè](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=176-7612) | [FRD-01], [FRD-05], [FRD-06] | Lọc tên, Nhắn tin, Hủy kết bạn, Chặn; thống nhất rail. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=176-7612) | GĐ1: đã duyệt |
| [WF-08-02 / Bạn bè đang Online](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=176-7765) | [FRD-01], [GRP-01] | Lọc tên; tạo nhóm với người đang chọn được điền sẵn. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=176-7765) | GĐ1: đã duyệt |
| [WF-08-03 / Lời mời kết bạn](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=176-7880) | [FRD-04] | Lọc tên người gửi; Đồng ý/Từ chối cập nhật đúng lời mời. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=176-7880) | GĐ1: đã duyệt |
| [WF-08-04 / Tìm người dùng — Có kết quả](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=176-7995) | [FRD-02], [FRD-03] | Hành động theo quan hệ; chưa kết bạn vẫn nhắn riêng; tạo nhóm cần là bạn. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=176-7995) | GĐ1: đã duyệt |
| [WF-08-05 / Danh sách đã chặn](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=176-8134) | [FRD-07] | Chỉ tài khoản mình chặn; bỏ chặn không khôi phục tình bạn. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=176-8134) | GĐ1: đã duyệt |
| [WF-08-06 / Tìm người dùng — Chưa nhập từ khóa](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8102) | [FRD-02] | Không truy vấn toàn bộ tài khoản khi chuỗi rỗng. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8102) | GĐ1: đã duyệt |
| [WF-08-07 / Đã gửi lời mời kết bạn](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8178) | [FRD-03] | Chỉ báo sau xác nhận; không tạo quan hệ bạn bè ngay. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8178) | GĐ1: đã duyệt |
| [WF-08-08 / Kết quả tìm có lời mời nhận vào](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8275) | [FRD-03], [FRD-04] | Nối Xem lời mời; không gửi ngược hoặc tự chấp nhận. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8275) | GĐ1: đã duyệt |
| [WF-08-09 / Xác nhận hủy kết bạn](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8372) | [FRD-05] | Giữ lịch sử và nhóm chung; nối Hủy và Xác nhận. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8372) | GĐ1: đã duyệt |
| [WF-08-10 / Xác nhận chặn người dùng](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8538) | [FRD-06] | Nêu hệ quả tình bạn/lời mời/chat riêng; cùng flow với drawer. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8538) | GĐ1: đã duyệt |
| [WF-08-11 / Chưa có bạn bè](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8703) | [FRD-01] | CTA sang tìm người; chỉ dùng sau tải thành công. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8703) | GĐ1: đã duyệt |
| [WF-08-12 / Không có bạn bè đang Online](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8780) | [FRD-01] | Về Tất cả; không ngăn nhắn người offline. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8780) | GĐ1: đã duyệt |
| [WF-08-13 / Không có lời mời kết bạn](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8857) | [FRD-04] | Phân biệt rỗng với lỗi tải. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8857) | GĐ1: đã duyệt |
| [WF-08-14 / Chưa chặn người dùng nào](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8931) | [FRD-07] | Không suy ra ai đang chặn mình. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-8931) | GĐ1: đã duyệt |
| [WF-08-15 / Không khớp bộ lọc tên](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-9005) | [FRD-01] | Xóa bộ lọc giữ đúng phân mục. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-9005) | GĐ1: đã duyệt |
| [WF-08-16 / Tìm người dùng — Không có kết quả](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-9087) | [FRD-02] | Không tiết lộ tài khoản bị ẩn hoặc lý do chặn. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-9087) | GĐ1: đã duyệt |
| [WF-08-17 / Đang tải danh sách](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-9171) | [FRD-01] | Giữ phân mục, điều kiện và dữ liệu đã có. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-9171) | GĐ1: đã duyệt |
| [WF-08-18 / Tìm kiếm thất bại](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-9279) | [FRD-02] | Giữ từ khóa/kết quả; retry cùng truy vấn, kiểm tra quyền hiện tại. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-9279) | GĐ1: đã duyệt |
| [WF-08-19 / Đang xử lý thao tác quan hệ](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-9379) | [FRD-03], [FRD-04], [FRD-05], [FRD-06], [FRD-07] | Ngăn lặp yêu cầu; label/loading theo đúng hành động. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-9379) | GĐ1: đã duyệt |
| [WF-08-20 / Cần đối chiếu kết quả thao tác](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-9497) | [FRD-03], [FRD-04], [FRD-05], [FRD-06], [FRD-07] | Kiểm tra lại trạng thái trước retry; không báo thành công giả. | [Mở frame](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=177-9497) | GĐ1: đã duyệt |

## 4. Frame/state cần design bổ sung

Task ưu tiên: **UX-01 Shell, UX-08 Panel và UX-06 quyền** làm trước, sau đó các flow chat/bạn bè/nhóm; UX-10 AI và UX-11 báo cáo hoàn thiện trước nghiệm thu prototype. Đây là thứ tự giao việc, không cắt phạm vi.

Các mã dưới đây là deliverable bắt buộc. Dùng variant cho các lỗi cùng cấu trúc nhưng nội dung và đường phục hồi phải riêng theo thao tác.

| Mã bổ sung / task | State cần thể hiện và nội dung bắt buộc | Nguồn nghiệm thu |
|---|---|---|
| SHELL-01 / UX-01 | Rail thống nhất: Đoạn chat, Bạn bè & Kết nối, Hồ sơ & Bảo mật, Đăng xuất; selected đúng screen | Screen Map 1.2 |
| CHAT-LIST-01 / UX-02 | Loading, lỗi danh sách, lọc Chưa đọc rỗng, lọc Nhóm rỗng; giữ hội thoại đang mở | Screen Map 1.2, [MSG-01] |
| CHAT-HISTORY-01 / UX-03 | Tải trang cũ, đã đến đầu lịch sử, đọc offline phần đã có; giữ vị trí đọc | [MSG-01] A1/A4, BR-01 |
| CHAT-SEARCH-01 / UX-03 | Nhiều kết quả để chạy Trước/Sau; đầu/cuối disabled; không kết quả trong phạm vi | [MSG-03] Basic/A1 |
| CHAT-SEARCH-02 / UX-03 | Lỗi tìm, thiếu khóa/chỉ một phần dữ liệu; thử lại/khôi phục khóa, giữ query | [MSG-03] A2/E2 |
| CHAT-SEND-01 / UX-04 | Nội dung rỗng/không hợp lệ/vượt giới hạn; lỗi gắn input, nháp giữ nguyên | [MSG-02] E1, BR-06 |
| CHAT-SEND-02 / UX-04 | Quyền đổi khi gửi; không thể gửi và không tự retry khi quyền phục hồi | [MSG-02] E3 |
| CHAT-MENU-01 / UX-05 | Menu tin nhận, tin của mình quá hạn; chỉ có thao tác được phép | [MSG-04] BR-01, [MSG-06] BR-01 |
| CHAT-DELETE-01 / UX-05 | Chọn/bỏ chọn; phần lựa chọn không hợp lệ; đối chiếu và xác nhận lại | [MSG-04] E1 |
| CHAT-MUTATION-01 / UX-05 | Đang xóa/thu hồi; mất xác nhận; đã ghi nhận trước đó; đồng bộ chưa xong | [MSG-04] A2/E2, [MSG-05] A3/E2, [MSG-06] A1/E3/E4 |
| PEOPLE-RESULT-01 / UX-07 | Đồng ý thành công, Từ chối thành công; loại đúng lời mời, quan hệ đúng kết quả | [FRD-04] Basic/A1 |
| PEOPLE-RESULT-02 / UX-07 | Hủy kết bạn thành công, Chặn thành công, Bỏ chặn thành công | [FRD-05], [FRD-06], [FRD-07] Basic |
| PEOPLE-RESULT-03 / UX-07 | Bỏ chặn nhưng chiều đối phương còn; không tự có lại tình bạn | [FRD-07] A2/BR-02/03 |
| PEOPLE-ERROR-01 / UX-07 | Lời mời đã xử lý, không đủ điều kiện, tài khoản không khả dụng; kết quả đối chiếu | [FRD-03/04/05/06/07] Exception |
| PEOPLE-ERROR-02 / UX-07 | Lỗi tải danh sách ở từng phân mục; giữ filter và dữ liệu đã có | [FRD-01] E1, [FRD-04] E3 |
| DRAWER-01 / UX-08 | Cá nhân thường/đang chặn: avatar lớn, tên, trạng thái, Tìm kiếm; xóa lịch sử, báo cáo, chặn hoặc gỡ chiều chặn của mình | Screen Map U-SCR-09, [FRD-06/07] |
| DRAWER-02 / UX-08 | Nhóm thành viên: avatar/tên/E2EE, tìm kiếm, danh sách có nhãn Admin, thêm người, thông tin E2EE, xóa lịch sử, báo cáo, rời nhóm | Screen Map U-SCR-09, [GRP-05] BR-01 |
| DRAWER-03 / UX-08 | Nhóm Admin: thêm đổi tên, menu cấp/thu hồi Admin, xóa thành viên và thôi quyền | [GRP-03/04/06/07] |
| DRAWER-04 / UX-08 | Nhóm mới: đúng tên/số người/mình Admin; nhóm đã rời/bị xóa: đúng trạng thái chỉ đọc | [GRP-01/02/07], [MSG-01] BR-04 |
| GROUP-RENAME-01 / UX-08 | Form tên hiện tại/mới; invalid; lưu; thành công; mất quyền hoặc cần đối chiếu | [GRP-04] Basic/A1/E1–E3 |
| GROUP-ADD-01 / UX-08 | Chọn một/nhiều bạn, tìm tên, số chọn; không chọn/vượt sức chứa; danh sách không còn hợp lệ | [GRP-05] Basic/E1/E2 |
| GROUP-ADD-02 / UX-08 | Kết quả từng người: đã thêm/đã là thành viên/không được thêm; chờ khóa riêng với tư cách thành viên | [GRP-05] A1/A2/E4, BR-03; PM-01 còn mở |
| GROUP-ROLE-01 / UX-08 | Xác nhận cấp Admin, thu hồi Admin khác; kết quả vai trò, điều kiện đổi/không còn Admin | [GRP-06] Basic/A1/E1/E2 |
| GROUP-REMOVE-01 / UX-08 | Xác nhận xóa người và hệ quả lịch sử; đã xóa/đã rời/mất quyền; cập nhật khóa chưa xong | [GRP-07] Basic/A1/A3/E1/E2 |
| GROUP-LEAVE-01 / UX-08 | Rời khi còn Admin khác; Admin duy nhất cần kế nhiệm; người cuối cùng rời, nhóm không còn thành viên | [GRP-02] Basic/A1/A2 |
| GROUP-SUCCESSOR-01 / UX-08 | U-DLG-05: danh sách ứng viên, chọn/xác nhận, không có ứng viên, điều kiện đổi cần xác nhận lại | [GRP-02] E1/E2, [GRP-03] Basic/E1/E2 |
| GROUP-RELINQUISH-01 / UX-08 | Thôi quyền khi còn Admin khác hoặc cần kế nhiệm; kết quả vẫn là thành viên, không phải đã rời | [GRP-03] Basic/A1 |
| GROUP-MUTATION-01 / UX-08 | Đang thực hiện, quyền đổi, nhóm không hoạt động, mất xác nhận; kiểm tra lại thay vì lặp tác động | [GRP-02…07] Exception |
| GROUP-CREATE-01 / UX-09 | Tên/danh sách invalid, thành viên đổi điều kiện, khóa không hợp lệ, đang tạo, mất xác nhận | [GRP-01] E1–E4 |
| GROUP-CREATE-02 / UX-09 | Nhóm tồn tại, một số người chưa đủ khóa; không báo mọi người đã đọc/gửi được | [GRP-01] A2 |
| AI-INPUT-01 / UX-10 | Đầu vào invalid/vượt giới hạn; sửa lại, xem trước/xác nhận mới | [AI-01/02] E1 |
| AI-CONTEXT-01 / UX-10 | Tin bị thu hồi/xóa/mất quyền trước chia sẻ; loại, chuẩn bị lại; không còn tin hợp lệ thì chọn lại | [AI-02] A1/E1 |
| AI-ERROR-01 / UX-10 | Hết hạn mức, lỗi dịch vụ/kết nối, kết quả rỗng/từ chối/sai định dạng; giữ nháp, retry có consent mới | [AI-01/02] E2/E3 |
| AI-PERMISSION-01 / UX-10 | Mất quyền trước chia sẻ/áp dụng; không gửi hoặc không điền kết quả; phiên hết hiệu lực | [AI-01/02] E4 |
| REPORT-INPUT-01 / UX-11 | Thiếu tin/lý do, chọn hơn 20 tin, gói vượt 1 MiB/hạn mức; chỉ rõ phần cần chỉnh | [RPT-01] E3/BR-04 |
| REPORT-CONTEXT-01 / UX-11 | Tin không còn hợp lệ; chuẩn bị lại/xác nhận lại; không còn tin được báo cáo thì khóa gửi | [RPT-01] E1 |
| REPORT-LOCAL-01 / UX-11 | Không đọc được local, giữ lựa chọn/lý do và cho thử lại; không thay bằng lấy lịch sử server | [RPT-01] E2 |
| REPORT-SUBMIT-01 / UX-11 | Đang gửi, mất xác nhận, đối chiếu cùng gói/ID; quyền/phiên hết hiệu lực; tiếp nhận có thời điểm | [RPT-01] A5/E4/E5 |
| SESSION-01 / dùng chung | Phiên hết hiệu lực: dừng thao tác, hướng về đăng nhập hiện có; không tự gửi hành động nhạy cảm bằng phiên mới | Exception phiên trong các UC |

Ký hiệu [FRD-03/04/05/06/07], [GRP-02…07], [AI-01/02] trong mô tả là nhóm mã; tra từng nguồn riêng trong mục 10. Không tạo mã UC mới từ các ký hiệu nhóm này.

## 5. Giai đoạn 2 — Prototype chi tiết theo screen/flow

### 5.1. Quy ước interaction và dữ liệu

- Click control chính; nhập liệu phải minh họa được thay đổi giá trị hợp lệ/không hợp lệ. Có thể dùng interactive component/variable/fixture cho prototype, ghi rõ giá trị demo.
- Menu mở cạnh đúng tin/thành viên; popover AI ở khu soạn; dialog xác nhận đặt trên screen nguồn; drawer mở bên phải. Đây là quy ước UX của bản giao việc.
- Hủy/Đóng trước xác nhận chỉ đóng lớp UI, trả focus về control đã mở và giữ dữ liệu nguồn. Escape tương đương đóng nếu chưa gửi; dùng hành động Ngừng chờ sau khi đã gửi AI. Không gắn đóng overlay thành undo thao tác server.
- Navigate chỉ khi đổi không gian/hội thoại; state cục bộ dùng variant/overlay khi khả thi. Overlay/modal không để action nền gửi thao tác lặp.
- Các state lỗi dữ liệu/server/role race mở từ Scenario Review ngoài viewport. Reviewer chọn scenario rồi thực hiện control thật; không rải nút “tạo lỗi” trong giao diện sản phẩm.
- Không đặt timeout demo thành cam kết SLA. Nếu mô phỏng server bằng delay, annotation ghi “độ trễ demo”.
- Mỗi flow giữ định danh hội thoại, người/tin được chọn, role, nháp và mode AI. Mọi đích ở bảng là cùng fixture nguồn trừ khi dòng chỉ rõ đổi hội thoại.
- Khi phiên hết hiệu lực, dừng đọc/gửi phần không còn quyền; đi SESSION-01 và đăng nhập. Không tự chia sẻ lại ngữ cảnh/báo cáo hoặc tự gửi tin bị từ chối khi đăng nhập lại.
- Đầu ra mỗi task gồm frame, bảng nối đã điền node, scenario và trạng thái review; không dùng “Back” cố định nếu có thể đưa về sai hội thoại/role.

### 5.2. UX-01 — Shell; UX-02 — Danh sách

**Mục tiêu:** luôn biết đang ở không gian nào và quay lại đúng công việc đang làm. **Nguồn:** Screen Map 1.2, [MSG-01], [FRD-01].

| Nguồn / control / trigger | Điều kiện → đích | Dữ liệu và đường về | Nghiệm thu |
|---|---|---|---|
| Mọi shell / Đoạn chat / click | Sang U-SCR-07 | Theo fixture đã chọn; lần đầu chưa chọn dùng 07-01 | Rail selected Đoạn chat, không đổi nhầm role |
| Mọi shell / Bạn bè / click | Sang 08-01 hoặc phân mục fixture đang review | Lưu nháp hội thoại trong bộ demo; rail Bạn bè | Từ Bạn bè bấm Đoạn chat quay lại được |
| Rail / Hồ sơ & Bảo mật / click | Mở U-DLG-01 hiện có | Đóng về shell nguồn; không xóa nháp | Label/đích giống nhau trên Chat và Bạn bè |
| Rail / Đăng xuất / click | Về U-SCR-01 hiện có | Kết thúc flow phiên; không giả tự đăng nhập | Có trên mọi shell |
| Sidebar / hội thoại / click | Người → 07-02; nhóm → 07-03 hoặc state quyền tương ứng | Header, lịch sử, drawer, badge và nháp cùng fixture | Không dùng cùng một đích cho mọi người |
| Sidebar / Tất cả, Chưa đọc, Nhóm / click | Danh sách lọc hoặc CHAT-LIST-01 rỗng | Giữ chat/nháp đang mở; xóa lọc trở lại tập đầy đủ | Từ Minh Anh bấm Nhóm vẫn giữ khung Minh Anh |
| 07-07 / Bạn bè hoặc Tạo nhóm / click | 08 hoặc U-DLG-02 | Hủy tạo nhóm quay lại 07-07 | Phân biệt rỗng với lỗi |

### 5.3. UX-03 — Lịch sử và tìm kiếm

**Mục tiêu:** đọc lịch sử được phép và tìm đúng tin mà không hiểu nhầm dữ liệu chưa đồng bộ là không tồn tại. **Nguồn:** [MSG-01] Basic/A1–A4/E1–E3/BR-01–04; [MSG-03] toàn flow.

| Nguồn / control / trigger | Điều kiện → đích | Dữ liệu và đường về | Nghiệm thu |
|---|---|---|---|
| Khung lịch sử / cuộn lên | Trang trước → 07-10 / CHAT-HISTORY-01 → lịch sử cũ hơn | Giữ vị trí tương đối và phần đã có; 50 tin/trang | Không trùng tin, không kéo xuống mới nhất vô cớ |
| 07-11 / Thử lại | Cùng trang thất bại → loading → dữ liệu hoặc lỗi | Giữ lịch sử cũ và hội thoại | Không dùng empty state thay lỗi |
| Sidebar / nhập query | 07-04; rỗng trở lại danh sách; không khớp → 07-09 | Giữ query và phạm vi tổng hợp | Có hai nhóm kết quả rõ ràng |
| Kết quả tên hội thoại / click | Mở hội thoại được chọn | Chọn đúng người/nhóm; xóa tìm qua control rõ ràng | Không nhảy tới tin khi chỉ chọn tên |
| Kết quả tin / click | Tải trang chứa tin → 07-05 | Đúng hội thoại, highlight tin đích; Về tin mới nhất quay cuối lịch sử | Dùng được cả cá nhân và nhóm đã giải mã |
| Drawer / Tìm kiếm / click | 07-21 trong hội thoại nguồn | Query riêng, tên/phạm vi hiển thị; Đóng về drawer nguồn | Đổi query không mở rộng sang tất cả |
| 07-21 / Trước-Sau / click | Kết quả kế/cũ trong cùng query | Đổi vị trí highlight; ở biên disabled | Fixture có ít nhất hai kết quả để chạy |
| Đang đồng bộ / kết quả hiện có | 07-04 hoặc CHAT-SEARCH-02 | Hiển thị kết quả một phần và phần chưa sẵn sàng | Không khẳng định “không có” trên toàn bộ lịch sử |
| Mở kết quả / tin không còn quyền | 07-22 | Loại kết quả; giữ query, chọn tin khác | Không để mở lại nội dung đã thu hồi/xóa riêng |
| Search lỗi/thiếu khóa / Thử lại-Khôi phục | CHAT-SEARCH-02 → retry hoặc U-SCR-06 | Trở lại đúng query/hội thoại; hủy khôi phục vẫn ở shell | Không gửi bản rõ nhóm để server tìm |

### 5.4. UX-04 — Gửi và trạng thái; UX-06 — quyền/E2EE

**Mục tiêu:** phân biệt gửi được với người khác đã nhận/đọc; bảo toàn nháp khi chưa gửi được. **Nguồn:** [MSG-01/02], [FRD-06/07], [GRP-02/07].

| Nguồn / control / trigger | Điều kiện → đích | Dữ liệu và đường về | Nghiệm thu |
|---|---|---|---|
| 07-02/03 / Gửi / click hoặc Enter | Nháp hợp lệ, có quyền, nhóm có khóa gửi → pending → 07-45 | Cùng hội thoại; annotation gán ID và giữ yêu cầu an toàn | Nối được Gửi ở cả cá nhân và nhóm |
| Ô soạn / Shift+Enter | Xuống dòng | Giữ nháp, không gửi | Theo keyboard hint hiện có; ghi là quy ước UX |
| Gửi / invalid | CHAT-SEND-01 | Giữ nguyên nháp để sửa, không tự cắt/chia tin | Không tự đặt giới hạn ký tự chưa có trong nguồn |
| Gửi / mất kết nối hoặc thiếu xác nhận | 07-12; reconnect/Thử lại → đối chiếu → đã lưu hoặc pending | Cùng ID theo annotation; không tạo hai bubble | Người offline vẫn có thể nhận trạng thái server đã lưu |
| Tin đã lưu / receiver update | Đã nhận rồi Đã đọc theo fixture | Chỉ đổi status; không gửi lại tin | Không gọi “Failed” chỉ vì thông báo/chuyển tin chậm |
| Status nhóm đã xem / click | 07-15 | Đúng tin và từng người nhận; Đóng về chat nguồn | Không tính người gửi; chưa giải mã không tính đã đọc |
| Quyền thay đổi trước server lưu | CHAT-SEND-02 hoặc 07-17/30 | Giữ nháp/yêu cầu bị từ chối; không tự retry khi quyền trở lại | Không có nút retry như lỗi mạng |
| Thiếu khóa gửi / Khôi phục | 07-20 → U-SCR-06 hiện có | Hủy quay shell thiếu khóa, nháp giữ nguyên | Không gửi bản rõ thay E2EE |
| Chỉ thiếu khóa lịch sử | 07-31 | Tin cũ placeholder; nếu có khóa gửi hiện tại vẫn được soạn/gửi | Tách thiếu khóa cũ với thiếu khóa gửi |
| Nhóm có chặn cá nhân | 07-18 | Đọc/gửi nhóm bình thường; quan hệ chặn không thay tư cách nhóm | Chặn một-một không khóa nhóm |
| Đã rời/bị xóa / Thông tin | DRAWER-04 | Giữ chỉ đọc khi mở/đóng panel | Tự rời giữ lịch sử có quyền trước rời; bị xóa giữ lịch sử từng đọc |

### 5.5. UX-05 — Xóa tin, xóa lịch sử và thu hồi

**Nguồn:** [MSG-04] Basic/A1/A2/E1–E3; [MSG-05] Basic/A1–A3/E1–E3; [MSG-06] Basic/A1/A2/E1–E4.

| Nguồn / control / trigger | Điều kiện → đích | Dữ liệu và đường về | Nghiệm thu |
|---|---|---|---|
| Tin / Thao tác / click | 07-13 hoặc CHAT-MENU-01 | Menu gắn đúng tin; ngoài menu/Đóng giữ nguyên dữ liệu | Tin nhận không có quyền thu hồi |
| Menu / Xóa phía mình | 07-23 → đang xử lý → 07-25 | Xác nhận đúng tin; Hủy về tin/menu nguồn | Tin bị loại phía mình, không thay bằng dấu thu hồi |
| Menu / Chọn nhiều | 07-14; chọn/bỏ chọn | Số lượng đổi; Hủy bỏ mode chọn | Thể hiện được thay đổi lựa chọn |
| Đã chọn / Xóa | 07-24 → xử lý → 07-26 | Chỉ tập tin đã xác nhận; tin mới ngoài tập vẫn giữ | Lựa chọn invalid → CHAT-DELETE-01, kiểm tra/xác nhận lại |
| Menu / Thu hồi | Tin của mình trong 48 giờ → 07-27 → 07-28 | Hủy giữ tin; xác nhận thay nội dung bằng dấu thu hồi | Admin không được thu hồi tin người khác |
| Thu hồi / quá hạn tại lúc xác nhận | 07-29 → Xóa phía mình → 07-23 | Giữ tin gốc đến khi xóa riêng được ghi nhận | Không retry thu hồi quá hạn |
| Drawer / Xóa lịch sử | 07-37 → xử lý → 07-38 | Chốt phạm vi trước consent; Hủy về drawer | Không hủy kết bạn/rời nhóm/thu hồi của người khác |
| Mất xác nhận thao tác | CHAT-MUTATION-01 → Kiểm tra lại | Đối chiếu đúng yêu cầu/tập tin/phạm vi | Retry không mở rộng sang tin mới |
| Thu hồi đã ghi nhận, thiết bị khác offline | 07-28 + annotation đồng bộ | Tiếp tục đồng bộ, không khôi phục nội dung | Thu hồi không tự rút báo cáo spam |

### 5.6. UX-07 — Bạn bè & Kết nối

**Mục tiêu:** hành động theo đúng quan hệ hiện tại, không yêu cầu online để kết bạn/chat riêng. **Nguồn:** [FRD-01…07], Screen Map U-SCR-08.

| Nguồn / control / trigger | Điều kiện → đích | Dữ liệu và đường về | Nghiệm thu |
|---|---|---|---|
| Menu trái / năm phân mục | 08-01/02/03/04/05 | Selected đúng; giữ điều kiện của fixture từng mục | Cả năm mục đều bấm được |
| Bạn bè/Online / nhập tên | Lọc tập bạn bè đúng phân mục → kết quả hoặc 08-15 | Xóa lọc về tập hiện tại | Online và tất cả đều lọc được |
| Tìm người / nhập username-họ tên / Tìm | 08-04; rỗng → 08-06, không có → 08-16 | Query giữ khi lỗi; không tiết lộ vì sao người khác bị ẩn | Không tự thêm minimum query length chưa chốt |
| Hàng người / Nhắn tin | Có quyền tương tác → đúng chat cá nhân | Chưa kết bạn/offline vẫn mở được; quay Bạn bè qua rail | Đúng tên, đúng hội thoại, không luôn Minh Anh |
| Online/bạn trong kết quả / Tạo nhóm | U-DLG-02 / 07-43, chọn sẵn người đó | Hủy quay đúng phân mục; không tự tạo nhóm ngay | Chỉ bạn đủ điều kiện mới được chọn |
| Chưa là bạn / Kết bạn | 08-19 → 08-07 | Chỉ sau xác nhận mới hiện đã gửi | Lặp yêu cầu không nhân lời mời |
| Đã có lời mời nhận vào / Xem lời mời | 08-08 → 08-03 | Không tự tạo lời mời ngược; chọn đúng người | Nút Kết bạn không tiếp tục gửi |
| Lời mời / Đồng ý | 08-19 → PEOPLE-RESULT-01 | Tạo tình bạn, loại lời mời; dữ liệu bạn bè cập nhật | Lời mời hết hiệu lực → PEOPLE-ERROR-01 |
| Lời mời / Từ chối | 08-19 → PEOPLE-RESULT-01 | Chỉ kết thúc lời mời; không tạo chặn | Có đường về danh sách hiện tại |
| Bạn bè / Hủy kết bạn | 08-09 → 08-19 → PEOPLE-RESULT-02 | Hủy dialog giữ nguyên; thành công bỏ tình bạn | Lịch sử/nhóm chung còn; vẫn chat nếu không chặn |
| Người dùng hoặc drawer / Chặn | 08-10 → 08-19 → PEOPLE-RESULT-02 và 07-16 khi mở chat | Nêu hủy tình bạn/lời mời, cấm chat riêng mới, giữ lịch sử/nhóm chung | Hai điểm vào dùng cùng nội dung/quy tắc |
| Đã chặn hoặc chat / Bỏ chặn | Xử lý → 07-47 / PEOPLE-RESULT-02 hoặc 03 | Chỉ gỡ chiều mình; chiều kia còn thì quyền vẫn hạn chế | Không tự kết bạn/lấy lại lời mời/gửi tin bị từ chối |
| Quan hệ / mất xác nhận | 08-20 → Kiểm tra lại → kết quả hiện tại | Giữ đối tượng/hành động; chưa rõ thì không báo thành công | Không tạo trùng hoặc ghi đè quyết định đã có |
| Danh sách/tìm lỗi / Thử lại | 08-18 hoặc PEOPLE-ERROR-02 → loading → kết quả | Giữ query/filter/phần đã tải | Tách tải lỗi và không có dữ liệu |

### 5.7. UX-08/09 — Drawer, tạo và quản lý nhóm

**Mục tiêu:** thao tác đúng nhóm và đúng vai trò; Admin không có đặc quyền thu hồi tin hoặc giải mã lịch sử ngoài quyền đang có. **Nguồn:** Screen Map U-SCR-09/U-DLG-02/05, [GRP-01…07].

| Nguồn / control / trigger | Điều kiện → đích | Dữ liệu và đường về | Nghiệm thu |
|---|---|---|---|
| Header / i / click | DRAWER-01/02/03/04 theo chat/role | i/Đóng tắt drawer; giữ nguồn/nháp | Avatar lớn, tên, online/E2EE và Tìm kiếm dùng chung |
| Drawer thành viên | Hiện Thêm người, lịch sử, báo cáo, E2EE, rời | Không hiện quyền Admin như control khả dụng | Thành viên thường được thêm bạn theo GRP-05 BR-01 |
| Drawer Admin / menu thành viên | GROUP-ROLE-01 hoặc GROUP-REMOVE-01 | Đúng tài khoản; Cancel giữ vai trò/tư cách | Admin ngang quyền, kể cả người tạo nhóm |
| Header sidebar / Tạo nhóm | 07-43 | Tên + ít nhất một bạn không có chiều chặn với người tạo | Bạn được thêm có thể offline |
| Tạo nhóm / xác nhận | GROUP-CREATE-01 loading → 07-49 | Người tạo Admin, nhóm E2EE, đúng số/tên người chọn | Invalid giữ form; có một người mất điều kiện từ chối toàn yêu cầu theo GRP-01 E2 |
| Tạo nhóm / mất xác nhận | GROUP-CREATE-01 → Kiểm tra lại | Nếu đã tồn tại mở lại nhóm đó | Không tạo nhóm trùng |
| Tạo nhóm xong / chưa đủ khóa | GROUP-CREATE-02 hoặc 07-20 theo khóa thiết bị | Nhóm vẫn tồn tại; quyền đọc/gửi theo khóa thực tế | Không coi lỗi khóa là tạo nhóm thất bại |
| 07-49 / Thông tin | DRAWER-04 nhóm mới với vai trò Admin | Đúng Nhóm Ôn tập Java, 2 thành viên | Không mở Nhóm Java 3 người/mình thành viên |
| Admin / Đổi tên | GROUP-RENAME-01 → lưu → drawer/header tên mới | Hủy giữ tên; tên không đổi kết thúc, không tạo thay đổi giả | Invalid giữ input; mất quyền từ chối; ID/thành viên/lịch sử không đổi |
| Thành viên/Admin / Thêm người | GROUP-ADD-01 → xác nhận → GROUP-ADD-02 | Chọn bạn mình, chưa thuộc nhóm, không chiều chặn với mình; giữ dữ liệu để đối chiếu | Kết quả từng người; nhánh nhiều người lỗi gắn PM-01 |
| Thêm xong / chờ khóa | GROUP-ADD-02 chờ cấp khóa | Tư cách đã thêm tách quyền giải mã; không lặp yêu cầu thêm | Người mới được đọc lịch sử trước tham gia khi đủ quyền/khóa, trừ thu hồi |
| Admin / Cấp hoặc thu hồi Admin khác | GROUP-ROLE-01 → xác nhận → vai trò mới | Hủy giữ role; kiểm tra lại lúc xác nhận | Không làm nhóm còn người nhưng không còn Admin |
| Admin / Xóa thành viên khác | GROUP-REMOVE-01 → xác nhận → kết quả | Cả quyền Admin của đích nếu có bị gỡ; không dùng flow này tự rời | Đã rời trả trạng thái hiện tại; cập nhật khóa chưa xong có nhãn riêng |
| Thành viên / Rời nhóm | GROUP-LEAVE-01 → xác nhận → 07-19 | Hủy giữ tư cách; không nhận/gửi/giải mã tin mới | Còn Admin khác không cần chọn kế nhiệm |
| Admin duy nhất / Rời khi còn người khác | GROUP-SUCCESSOR-01 → chọn → xác nhận → 07-19 | Ghi chú bàn giao+rời cùng một cập nhật | Không tự chọn người kế nhiệm; không ứng viên thì không rời |
| Người cuối cùng / Rời | GROUP-LEAVE-01 xác nhận → nhóm không còn thành viên | Không yêu cầu kế nhiệm; lịch sử theo quyền | Không tiếp tục nhận tin mới |
| Admin / Thôi quyền | Còn Admin khác → xác nhận; duy nhất → GROUP-SUCCESSOR-01 | Hoàn tất thành thành viên thường, vẫn trong nhóm | Không chuyển sang “Bạn đã rời nhóm” |
| Chọn kế nhiệm / role-ứng viên đổi | GROUP-SUCCESSOR-01 kiểm tra/chọn lại | Chưa ghi nhận thay đổi, cần xác nhận lại | Ứng viên không phải là bạn/online; chặn cá nhân không loại ứng viên |
| Quản trị nhóm / lỗi hoặc mất xác nhận | GROUP-MUTATION-01 → Kiểm tra lại | Đúng yêu cầu/nhóm/tài khoản; quyền mới quyết định control | Không tách riêng bàn giao và rời thành hai kết quả giả độc lập |

### 5.8. UX-10 — AI Refine và Recommend

**Nguồn:** [AI-01] Basic 1–6, A1–A4, E1–E4, BR-01–04; [AI-02] Basic 1–6, A1–A4, E1–E4, BR-01–04.

| Nguồn / control / trigger | Điều kiện → đích | Dữ liệu và đường về | Nghiệm thu |
|---|---|---|---|
| Composer / Chỉnh sửa | Có quyền soạn, nháp có ký tự không phải khoảng trắng → 07-32 | Mode Refine; input sửa được nhưng nháp gốc giữ nguyên | Không tự thêm lịch sử/hồ sơ người nhận |
| Composer / Gợi ý | Có quyền soạn và ít nhất một tin văn bản đọc được → 07-33 | Mode Recommend; chọn/sửa phần ngữ cảnh cùng hội thoại | Không yêu cầu có sẵn nháp/receiver online |
| Preview / Chia sẻ và tạo | Đầu vào hợp lệ → 07-46 theo mode | Nêu rõ bản rõ tới server và nhà cung cấp AI, cả E2EE | Không gọi trước consent |
| Preview / Hủy-Đóng | Về composer nguồn | Nháp gốc nguyên; chưa gửi dữ liệu | Cả cá nhân và nhóm giữ đúng nguồn |
| Tin ngữ cảnh thay đổi trước chia sẻ | AI-CONTEXT-01 → chọn/preview lại | Loại tin không hợp lệ; không còn thì không gửi | Không âm thầm bổ sung tin mới/nháp/toàn bộ lịch sử |
| Đang xử lý / response | Refine → 07-34; Recommend → 07-52; lỗi → 07-35/AI-ERROR-01 | Giữ mode, hội thoại, request; bỏ phản hồi cũ khi đã rời flow | Có scenario lỗi từ cả hai chế độ |
| Đang xử lý / Ngừng chờ | Về composer, giữ nháp | Cảnh báo ngừng chờ không thu hồi dữ liệu/chi phí đã phát sinh | Không tự áp dụng phản hồi đến sau |
| Kết quả / chọn phương án-Dùng làm nháp | Còn quyền, nháp không đổi → 07-39 | Chỉ thay nháp đúng hội thoại; chưa gửi | Thể hiện danh sách và lựa chọn phương án |
| Kết quả / nháp đã thay đổi | 07-36 | So sánh nháp mới và phương án AI | Có nhánh chạy được từ thao tác áp dụng |
| 07-36 / Giữ hoặc Thay | Giữ/Đóng → 07-40; đồng ý thay → 07-39 | Kiểm tra lại phiên bản; tiếp tục đổi thì xác nhận lại | Không âm thầm ghi đè |
| Kết quả / Xem lại đầu vào-Tạo lại | Về 07-32 hoặc 33 theo mode | Kiểm tra/sửa và consent mới | Không reuse tin đã thu hồi/xóa cho yêu cầu mới |
| Lỗi/hết hạn mức/kết quả rỗng | 07-35 / AI-ERROR-01 | Giữ nháp; soạn thủ công vẫn dùng khi còn quyền | Không tự gọi lại AI |
| Mất quyền trước chia sẻ/áp dụng | AI-PERMISSION-01 / SESSION-01 | Dừng chia sẻ hoặc áp dụng; giữ nháp phù hợp | Không áp dụng sau chặn/mất quyền |
| 07-39 / Gửi | Flow MSG-02 | Kiểm tra quyền, nội dung, khóa lại như tin tự soạn | Áp dụng AI không tự gửi |

### 5.9. UX-11 — Báo cáo spam

**Nguồn:** [RPT-01] Basic 1–5, A1–A5, E1–E5, BR-01–08.

| Nguồn / control / trigger | Điều kiện → đích | Dữ liệu và đường về | Nghiệm thu |
|---|---|---|---|
| Drawer cá nhân/nhóm / Báo cáo | 07-50; trong nhóm phải chọn được tài khoản đích | Một tài khoản đích, một hội thoại; chọn tin/lý do | Chỉ Selected là tin của đích; không Selected tin của mình/tin hệ thống |
| Chọn tin/lý do / Xem trước | 1–20 tin hợp lệ + lý do có nội dung → chuẩn bị → 07-44 | Context tối đa 30 tin local mới nhất đủ điều kiện, không tải bù | Có ít hơn 30 vẫn được gửi khi Selected hợp lệ |
| Chuẩn bị gói / lỗi local | REPORT-LOCAL-01 → Thử lại | Giữ lựa chọn/lý do | Không coi lỗi local là không có tin |
| Preview | Hiển thị toàn bộ Selected ∪ Context loại trùng ID | Nội dung, người gửi, thời gian, nhãn Selected/Context/cả hai và các số đếm | Selected ngoài Context vẫn có; theo thứ tự trao đổi |
| Preview / Sửa lựa chọn-lý do | Về 07-50 → chuẩn bị/preview lại | Gói mới phải consent lại | Sửa dữ liệu AI/báo cáo không sửa tin nguồn |
| Preview / Hủy-Đóng trước gửi | Về drawer/hội thoại nguồn | Không chia sẻ/tạo hồ sơ; nháp chat giữ nguyên | Hủy không đồng nghĩa rút hồ sơ đã gửi |
| Preview / tin mới đến | Giữ nguyên gói đã chuẩn bị | Chỉ bổ sung khi chủ động làm mới và consent lại | Không âm thầm thay Evidence |
| Preview / Chia sẻ và gửi | Đủ điều kiện/gói ≤1 MiB → REPORT-SUBMIT-01 → 07-51 | Bản rõ tới server để Admin xem, kể cả nhóm E2EE | Đếm byte theo UC; không tự cắt nội dung, giảm Context hay chia báo cáo |
| Tin mất quyền/xóa/thu hồi/chưa giải mã | REPORT-CONTEXT-01 → chuẩn bị và xác nhận lại | Loại tin invalid; không còn Selected thì khóa gửi | Không chia sẻ tin đã không còn được đọc |
| Đầu vào/hạn mức sai | REPORT-INPUT-01 | Chỉ rõ phần cần sửa, giữ dữ liệu hợp lệ | Không tự đặt số hạn mức ngoài nguồn |
| Mất xác nhận sau gửi | REPORT-SUBMIT-01 → đối chiếu cùng ID/gói | Nếu đã tiếp nhận trả hồ sơ cũ; không trích xuất Context mới | Không tạo lại ID/lặp hồ sơ/tính quota lần nữa |
| Server tiếp nhận | 07-51 | Mã hồ sơ + thời điểm; không tự chặn/xóa/rời nhóm | Không thêm UI theo dõi/rút báo cáo trong đợt này |
| Phiên/quyền hết hiệu lực | SESSION-01 | Không tự gửi lại bằng phiên mới | Nháp chat và dữ liệu còn hợp lệ được giữ theo RPT-01 NFR-03 |

## 6. Giai đoạn 3 — Áp dụng design system và redesign Light

### 6.1. Hệ thống đã có và cách sử dụng

Figma có CS / Primitives, CS / Color (Light, Dark, Wireframe), CS / Metrics. Chọn **Light** cho toàn bộ frame sản phẩm sau khi prototype được duyệt. Không vẽ lại bảng màu hoặc đổi semantic token thành màu literal. WF / Interaction state và Prototype / … là biến tương tác, không phải token visual.

| Khu vực | Component/style nền | Việc designer phải làm |
|---|---|---|
| Rail/sub-tab/filter | CS / Navigation item, Button, Icon button | Thống nhất selected/hover/focus/disabled; label và icon đúng hành động |
| Danh sách người/chat | CS / Avatar, Presence, Badge, Conversation item | Một main component/hàng lặp, instance với nội dung/quan hệ/role riêng |
| Form/search/select | CS / Field, Search box, Checkbox | Error/helper/filled/focus/disabled/loading theo flow; input giữ dữ liệu khi lỗi |
| Tin nhắn/composer | CS / Message bubble, Message composer | Sáu trạng thái delivery phù hợp; composer giữ hai AI độc lập và Gửi |
| Notice/dialog/empty | CS / Notice, Empty state, Dialog | Phân biệt success/warning/error; destructive confirmation nêu đối tượng/phạm vi |
| Text | CS / Title, Subtitle, Body, Body strong, Caption, Caption strong | Bind text style, font Inter theo Foundations; giữ tiếng Việt/wrap |
| Pattern mới | Drawer, Member row/menu, Search result, AI candidate/context, Report evidence | Ghép primitive CS, component hóa phần lặp và bind token |

Token bắt buộc dùng theo vai trò có sẵn: color/bg/canvas, surface, subtle, hover, pressed, disabled; color/text/primary, secondary, placeholder, disabled, on-brand; color/border/default, strong, focus; color/brand/*; color/message/incoming, outgoing, text-outgoing; color/status/* và color/presence/*.

Metrics dùng space/*, radius/*, size/*, stroke/* hiện có. Foundations quy định lưới 4 px, control desktop 32 px, radius 4 cho control/12 cho bubble/999 cho avatar, focus stroke 2 px, bubble khoảng tối đa 70% chiều rộng. Dùng token tương ứng; geometry không có token chỉ ghi literal có chủ đích, không tự sinh hệ token mới.

### 6.2. Yêu cầu chất lượng visual

- Layer editable, instance còn liên kết; không flatten cả UI, không detach để sửa màu.
- Control trạng thái phải có nhãn/icon đi kèm; không dùng màu làm cách phân biệt duy nhất.
- Avatar lớn và nút Tìm kiếm ở đầu drawer phải có trên cả cá nhân/nhóm theo Screen Map.
- Composer nền DS chưa có hai nút AI thì tạo wrapper/pattern tương thích, không làm mất Refine/Recommend.
- Có fixture tên dài, tin nhiều dòng, danh sách dài, bằng chứng báo cáo dài để kiểm tra scroll/wrap và CTA luôn truy cập được.
- Dùng icon vector CS phù hợp; nếu thiếu ghi dependency component mới, không thay bằng emoji.
- CS có Attachment/Typing indicator hoặc status Away/Busy không đồng nghĩa phải bật những tính năng đó; chỉ hiển thị các chức năng được nguồn nghiệp vụ trong phạm vi yêu cầu.
- Kiểm tra drawer mở làm hẹp chat, selected sidebar còn đúng, modal không cắt footer và không có text màu Dark literal trên Light.
- Sau thay component/mode, chạy lại mọi flow; bảo toàn reactions, label, điều kiện disabled và đích frame.
- Bàn giao component/token mapping; Dark ghi backlog riêng, giữ binding sẵn có để làm sau.

## 7. Scenario review và checklist nghiệm thu

Mỗi scenario có điểm bắt đầu, fixture/role, thao tác, kết quả mong đợi, link frame và kết quả Đạt/Không đạt. Designer đặt bộ chọn scenario bên ngoài UI sản phẩm. Dùng scenario mô phỏng kiểm chứng phản hồi UI, không kết luận đã kiểm thử mã hóa/server qua Figma.

| ID | Kịch bản chạy thử | Kết quả bắt buộc |
|---|---|---|
| QA-01 | Chat → Bạn bè → Hồ sơ đóng → Chat; Đăng xuất | Đúng rail và đích, có đường về; logout có ở cả shell |
| QA-02 | Minh Anh có nháp → lọc Nhóm/Chưa đọc | Danh sách đổi, hội thoại/nháp không bị thay |
| QA-03 | Tìm toàn bộ → tên nhóm; tìm nội dung → tin đích → về mới nhất | Hai loại kết quả có đích đúng |
| QA-04 | Tìm trong nhóm → nhiều kết quả → Trước/Sau; tin vừa thu hồi | Không mở rộng phạm vi; loại kết quả mất quyền |
| QA-05 | Cuộn lịch sử → tải lỗi → retry; offline đọc dữ liệu đã có | Dữ liệu cũ và vị trí không mất; không giả trống |
| QA-06 | Gửi cá nhân khi receiver offline; mất xác nhận/retry | Đã lưu độc lập đã nhận/đọc, không tạo hai tin |
| QA-07 | Gửi nhóm thiếu khóa hiện tại vs chỉ thiếu khóa lịch sử | Trường hợp đầu khóa gửi; trường hợp sau vẫn gửi khi đủ khóa mới |
| QA-08 | Chặn hai chiều, bỏ một chiều; vào nhóm chung | Không tự khôi phục tình bạn/quyền khi chiều kia còn; nhóm vẫn trao đổi |
| QA-09 | Xóa một/nhiều/lịch sử; hủy; tin mới tới trong lúc xác nhận | Chỉ phạm vi xác nhận bị xóa phía mình |
| QA-10 | Thu hồi đúng người trong hạn; tin người khác/quá 48 giờ | Quyền menu và kết quả đúng, có dấu thu hồi |
| QA-11 | Nhắn tin với người chưa là bạn; gửi lời mời; Đồng ý/Từ chối | Quan hệ/CTA nhất quán, không tạo lời mời ngược |
| QA-12 | Tạo nhóm từ bạn Online; mất xác nhận; mở drawer nhóm mới | Người chọn sẵn, không nhóm trùng, đúng tên/role/số người |
| QA-13 | Thành viên thường thêm bạn; chờ cấp khóa | Có quyền mời, không báo đọc/gửi hoàn tất khi chưa đủ khóa |
| QA-14 | Admin đổi tên/cấp/thu hồi/xóa; role đổi trước xác nhận | Kiểm tra lại quyền; không còn control Admin khi mất quyền |
| QA-15 | Admin cuối cùng rời/thôi quyền; không ứng viên; người cuối rời | Bàn giao đúng điều kiện, không tự chọn, phân biệt rời với thôi quyền |
| QA-16 | Đã rời/bị xóa → mở/đóng drawer | Không bật lại composer; phạm vi lịch sử đúng từng trường hợp |
| QA-17 | Refine và Recommend từ cả chat cá nhân/nhóm | Preview consent rõ bản rõ; giữ đúng mode/ngữ cảnh |
| QA-18 | AI lỗi, quota, kết quả rỗng; nháp mới trước áp dụng; hủy thay | Giữ nháp, retry consent mới, không ghi đè/tự gửi |
| QA-19 | Tin ngữ cảnh mất quyền, đổi chat khi AI đang xử lý | Yêu cầu kiểm tra lại; phản hồi cũ không áp vào chat khác |
| QA-20 | Báo cáo trong nhóm; Selected ngoài Context; local <30 | Đúng tài khoản, union loại trùng, xem đủ gói không tải bù |
| QA-21 | Báo cáo >20 Selected/>1 MiB, lỗi local, tin đổi trước consent | Lỗi rõ và giữ dữ liệu; không tự cắt/gửi gói mới |
| QA-22 | Tin mới sau preview, mất xác nhận báo cáo, phiên hết hiệu lực | Giữ gói; đối chiếu cùng ID; không tự gửi bằng phiên mới |
| QA-23 | Apply Light xong chạy lại các flow và nội dung dài | Không đứt prototype/clip chữ; token, focus, disabled đúng |

### 7.1. Gate hi-fi wireframe

- [x] Từng frame ở mục 3 có yêu cầu sửa và link kết quả; từng state mục 4 có variant/overlay/frame để review.
- [x] Mỗi task có annotation UC và bước/BR; đủ thành công/hủy/lỗi.
- [x] Vai trò nhóm, lịch sử chỉ đọc, consent AI/báo cáo được PM review.
- [x] Chưa có con số giới hạn thì dùng thông báo điều kiện, không tự điền số.
- [x] PM-01/02 ở mục 9 được ghi rõ tình trạng và người xử lý.

### 7.2. Gate prototype

- [ ] Mọi nút hành động chính, năm phân mục, rail, close/cancel/back và retry có interaction hoặc lý do disabled.
- [ ] Bảng flow mục 5 được điền node ID đích cuối; không chỉ dùng bản đồ hyperlink để thay flow.
- [ ] Scenario ở mục 7 có đường vào/ra; không có nút demo lỗi trong viewport sản phẩm.
- [ ] Đúng context và snapshot selection; không dùng cố định Minh Anh/Nhóm Java cho mọi đích.
- [ ] Hành động destructive chưa ghi nhận không giả thành công; mất xác nhận có đường đối chiếu.

### 7.3. Gate apply design system và bàn giao

- [ ] Toàn bộ frame trong phạm vi dùng Light; instance/style/token còn liên kết.
- [ ] Component lặp tái sử dụng, layer editable, auto-layout và nội dung dài đạt.
- [ ] Không thêm tính năng ngoài nguồn chỉ vì DS có component.
- [ ] Có ảnh QA các screen chính/dialog/error sau apply DS; ghi lỗi, sửa và xác nhận ảnh cuối.
- [ ] Chạy lại QA-01…23; ghi link flow và kết quả.
- [ ] Có danh mục frame cuối, component/token map và annotation cho dev/QA.
- [ ] Nhánh nghiệp vụ còn mở chưa được đánh dấu “đã nghiệm thu” bằng giả định của designer.

## 8. Ma trận truy vết giao việc

Designer tách thêm một hàng cho từng Alternative/Exception/BR khi điền kết quả để kiểm tra đủ coverage. Cột “kết quả” bắt đầu Chưa review; PM cập nhật Đạt/Không đạt/Chờ quyết định kèm link.

| UC | Phần cần kiểm tra | Task/frame hoặc state nền | Flow / QA |
|---|---|---|---|
| [MSG-01] | Basic 1–4; A1–A4; E1–E3; BR-01–04 | UX-02/03/04/06; 07-01…11/15/19/20/30/31 | 5.2–5.4; QA-02/05/07/16 |
| [MSG-02] | Basic 1–4; A1/A2; E1–E4; BR-01–06 | UX-04/06; 07-02/03/12/16…20/45 | 5.4; QA-06/07/08 |
| [MSG-03] | Basic 1–4; A1/A2; E1–E4; BR-01–03 | UX-03; 07-04/05/09/21/22 | 5.3; QA-03/04 |
| [MSG-04] | Basic 1–4; A1/A2; E1–E3; BR-01–04 | UX-05; 07-13/14/23…26 | 5.5; QA-09 |
| [MSG-05] | Basic 1–4; A1–A3; E1–E3; BR-01–03 | UX-05; 07-37/38 | 5.5; QA-09 |
| [MSG-06] | Basic 1–3; A1/A2; E1–E4; BR-01–03 | UX-05; 07-27…29 | 5.5; QA-10 |
| [FRD-01] | Basic/A1/A2/E1/E2/BR-01–03 | UX-07; 08-01/02/11/12/15/17 | 5.6; QA-11 |
| [FRD-02] | Basic/A1/E1–E3/BR-01–03 | UX-07; 08-04/06/16/18 | 5.6; QA-11 |
| [FRD-03] | Basic/A1–A3/E1–E3/BR-01–03 | UX-07; 08-07/08/19/20 | 5.6; QA-11 |
| [FRD-04] | Basic/A1/A2/E1–E4/BR-01–03 | UX-07; 08-03/13/19/20 | 5.6; QA-11 |
| [FRD-05] | Basic/A1/A2/E1/E2/BR-01–04 | UX-07; 08-09/19/20 + result | 5.6; QA-11 |
| [FRD-06] | Basic/A1/A2/E1/E2/BR-01–04 | UX-06/07/08; 08-10, 07-16/18 | 5.4/5.6; QA-08 |
| [FRD-07] | Basic/A1/A2/E1–E3/BR-01–04 | UX-06/07; 08-05, 07-47 + result | 5.6; QA-08 |
| [GRP-01] | Basic/A1/A2/E1–E4/BR-01–04 | UX-09; 07-43/49, GROUP-CREATE | 5.7; QA-12 |
| [GRP-02] | Basic/A1–A3/E1–E4/BR-01–04 | UX-08; LEAVE/SUCCESSOR, 07-19 | 5.7; QA-15/16 |
| [GRP-03] | Basic/A1/A2/E1–E4/BR-01–03 | UX-08; RELINQUISH/SUCCESSOR | 5.7; QA-15 |
| [GRP-04] | Basic/A1/E1–E3/BR-01–03 | UX-08; RENAME | 5.7; QA-14 |
| [GRP-05] | Basic/A1–A3/E1–E4/BR-01–04; còn mở | UX-08; ADD; PM-01 | 5.7; QA-13 |
| [GRP-06] | Basic/A1–A3/E1–E4/BR-01–04 | UX-08; ROLE | 5.7; QA-14 |
| [GRP-07] | Basic/A1–A3/E1–E3/BR-01–04 | UX-08/06; REMOVE, 07-30 | 5.7; QA-14/16 |
| [AI-01] | Basic 1–6/A1–A4/E1–E4/BR-01–04 | UX-10; 07-32/34…36/39/40/46 | 5.8; QA-17…19 |
| [AI-02] | Basic 1–6/A1–A4/E1–E4/BR-01–04 | UX-10; 07-33/35/36/39/40/46/52 | 5.8; QA-17…19 |
| [RPT-01] | Basic 1–5/A1–A5/E1–E5/BR-01–08 | UX-11; 07-44/50/51 + REPORT | 5.9; QA-20…22 |

NFR về lưu/đồng bộ, bảo mật, không ghi log nháp/bản rõ/khóa, chống trùng và API được đưa vào annotation handoff cho developer/QA; prototype chỉ chứng minh phản hồi UI dự kiến. Mục tiêu hiệu năng còn ghi đề xuất trong nguồn không tự chuyển thành SLA sản phẩm.

## 9. Các điểm cần PM chốt và quy tắc xử lý

| Mã | Nguồn / vấn đề | Giao việc cho designer | Điều kiện nghiệm thu |
|---|---|---|---|
| PM-01 | [GRP-05], dòng “Còn mở”: batch có người invalid/đã là thành viên — từ chối toàn bộ hay thêm phần hợp lệ | Thiết kế selection, confirmation, kết quả từng người và state cần đối chiếu; annotation hai hướng tác động, không khóa một hướng thành quy tắc đã chốt | PM cập nhật quyết định vào nguồn, sau đó nối nhánh commit cuối và review lại; chỉ nhánh này chờ, các phần khác tiếp tục |
| PM-02 | Các UC chỉ nói giới hạn hệ thống mà chưa có số: độ dài tin/tên nhóm, số thành viên, số tin xóa, giới hạn/hạn mức AI | Form/error phải nêu điều kiện cần điều chỉnh; dùng placeholder trong annotation cho tham số, không đưa con số tự đặt vào UI | PM dẫn nguồn giá trị trước khi chốt copy có số; 50 tin/trang, 48 giờ và giới hạn báo cáo đã có nguồn vẫn dùng đúng |
| PM-03 | Nhu cầu Dark sau Light | Giữ semantic binding và ghi backlog Dark; chưa yêu cầu duplicate toàn bộ frame | Light là phạm vi nghiệm thu đợt này |

Các quyết định UX được chọn cho bàn giao: cùng fixture xuyên flow, overlay giữ context, focus về control nguồn, scenario ngoài viewport, bố cục desktop theo frame hiện có. Nếu quyết định UX làm thay quyền, phạm vi dữ liệu hay điều kiện UC thì chuyển lại PM, không tự áp dụng.

## 10. Link nguồn use case

Các link dưới đây là đường dẫn tương đối từ thư mục gốc ChatSystem để file Markdown dùng được khi mở từ repository.

- [UC-USR-MSG-01](docs/requirements/use-cases/user/messaging/UC-USR-MSG-01-view-conversation-history.md)
- [UC-USR-MSG-02](docs/requirements/use-cases/user/messaging/UC-USR-MSG-02-send-message.md)
- [UC-USR-MSG-03](docs/requirements/use-cases/user/messaging/UC-USR-MSG-03-search-messages.md)
- [UC-USR-MSG-04](docs/requirements/use-cases/user/messaging/UC-USR-MSG-04-delete-messages-for-self.md)
- [UC-USR-MSG-05](docs/requirements/use-cases/user/messaging/UC-USR-MSG-05-clear-conversation-history.md)
- [UC-USR-MSG-06](docs/requirements/use-cases/user/messaging/UC-USR-MSG-06-recall-message.md)
- [UC-USR-FRD-01](docs/requirements/use-cases/user/friend-management/UC-USR-FRD-01-view-friends.md)
- [UC-USR-FRD-02](docs/requirements/use-cases/user/friend-management/UC-USR-FRD-02-search-users.md)
- [UC-USR-FRD-03](docs/requirements/use-cases/user/friend-management/UC-USR-FRD-03-send-friend-request.md)
- [UC-USR-FRD-04](docs/requirements/use-cases/user/friend-management/UC-USR-FRD-04-process-friend-request.md)
- [UC-USR-FRD-05](docs/requirements/use-cases/user/friend-management/UC-USR-FRD-05-unfriend-user.md)
- [UC-USR-FRD-06](docs/requirements/use-cases/user/friend-management/UC-USR-FRD-06-block-user.md)
- [UC-USR-FRD-07](docs/requirements/use-cases/user/friend-management/UC-USR-FRD-07-unblock-user.md)
- [UC-USR-GRP-01](docs/requirements/use-cases/user/group-management/UC-USR-GRP-01-create-group.md)
- [UC-USR-GRP-02](docs/requirements/use-cases/user/group-management/UC-USR-GRP-02-leave-group.md)
- [UC-USR-GRP-03](docs/requirements/use-cases/user/group-management/UC-USR-GRP-03-relinquish-group-admin.md)
- [UC-USR-GRP-04](docs/requirements/use-cases/user/group-management/UC-USR-GRP-04-rename-group.md)
- [UC-USR-GRP-05](docs/requirements/use-cases/user/group-management/UC-USR-GRP-05-add-group-member.md)
- [UC-USR-GRP-06](docs/requirements/use-cases/user/group-management/UC-USR-GRP-06-manage-group-admin-rights.md)
- [UC-USR-GRP-07](docs/requirements/use-cases/user/group-management/UC-USR-GRP-07-remove-group-member.md)
- [UC-USR-AI-01](docs/requirements/use-cases/user/llm-assistant/UC-USR-AI-01-refine-draft.md)
- [UC-USR-AI-02](docs/requirements/use-cases/user/llm-assistant/UC-USR-AI-02-recommend-message.md)
- [UC-USR-RPT-01](docs/requirements/use-cases/user/spam-reporting/UC-USR-RPT-01-submit-spam-report.md)

### Ngoài phạm vi hiện tại

- [RPT-02 — Theo dõi báo cáo](docs/requirements/use-cases/user/spam-reporting/UC-USR-RPT-02-track-spam-report.md): đề xuất tương lai, không giao thiết kế trong đợt này.
- [RPT-03 — Rút báo cáo](docs/requirements/use-cases/user/spam-reporting/UC-USR-RPT-03-request-report-withdrawal.md): đề xuất tương lai, không giao thiết kế trong đợt này.

[MSG-01]: docs/requirements/use-cases/user/messaging/UC-USR-MSG-01-view-conversation-history.md
[MSG-02]: docs/requirements/use-cases/user/messaging/UC-USR-MSG-02-send-message.md
[MSG-03]: docs/requirements/use-cases/user/messaging/UC-USR-MSG-03-search-messages.md
[MSG-04]: docs/requirements/use-cases/user/messaging/UC-USR-MSG-04-delete-messages-for-self.md
[MSG-05]: docs/requirements/use-cases/user/messaging/UC-USR-MSG-05-clear-conversation-history.md
[MSG-06]: docs/requirements/use-cases/user/messaging/UC-USR-MSG-06-recall-message.md
[FRD-01]: docs/requirements/use-cases/user/friend-management/UC-USR-FRD-01-view-friends.md
[FRD-02]: docs/requirements/use-cases/user/friend-management/UC-USR-FRD-02-search-users.md
[FRD-03]: docs/requirements/use-cases/user/friend-management/UC-USR-FRD-03-send-friend-request.md
[FRD-04]: docs/requirements/use-cases/user/friend-management/UC-USR-FRD-04-process-friend-request.md
[FRD-05]: docs/requirements/use-cases/user/friend-management/UC-USR-FRD-05-unfriend-user.md
[FRD-06]: docs/requirements/use-cases/user/friend-management/UC-USR-FRD-06-block-user.md
[FRD-07]: docs/requirements/use-cases/user/friend-management/UC-USR-FRD-07-unblock-user.md
[GRP-01]: docs/requirements/use-cases/user/group-management/UC-USR-GRP-01-create-group.md
[GRP-02]: docs/requirements/use-cases/user/group-management/UC-USR-GRP-02-leave-group.md
[GRP-03]: docs/requirements/use-cases/user/group-management/UC-USR-GRP-03-relinquish-group-admin.md
[GRP-04]: docs/requirements/use-cases/user/group-management/UC-USR-GRP-04-rename-group.md
[GRP-05]: docs/requirements/use-cases/user/group-management/UC-USR-GRP-05-add-group-member.md
[GRP-06]: docs/requirements/use-cases/user/group-management/UC-USR-GRP-06-manage-group-admin-rights.md
[GRP-07]: docs/requirements/use-cases/user/group-management/UC-USR-GRP-07-remove-group-member.md
[AI-01]: docs/requirements/use-cases/user/llm-assistant/UC-USR-AI-01-refine-draft.md
[AI-02]: docs/requirements/use-cases/user/llm-assistant/UC-USR-AI-02-recommend-message.md
[RPT-01]: docs/requirements/use-cases/user/spam-reporting/UC-USR-RPT-01-submit-spam-report.md

## 11. Bàn giao giai đoạn 1 — 18/11/2026

**Trạng thái:** Giai đoạn 1 đã được người dùng duyệt ngày 18/11/2026; giữ PM-01/PM-02 còn mở. Giai đoạn 2/3 chưa được nghiệm thu.

- [Mục lục Figma — 193 frame/state](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=451-20257).
- [Bản bàn giao chi tiết và link cuối](ui-ux-phase-1-handoff.md): 72 frame nền, 121 state bổ sung, coverage 39 nhóm và kết quả audit.
- Đã đọc lại 193/193 frame, kiểm tra mode Wireframe và annotation; 118 viewport desktop đúng 1440 × 900; không phát hiện tràn nội dung hiển thị qua kiểm tra bounds.
- 27/27 kiểm tra UI trọng yếu đạt, kèm ảnh review đại diện và các lỗi đã sửa.
- PM-01 vẫn chờ quyết định commit batch thêm người; PM-02 chưa tự bổ sung giá trị số ngoài nguồn.
- Gate 7.1 đã được người dùng duyệt ngày 18/11/2026: nội dung, vai trò nhóm, lịch sử chỉ đọc và consent AI/báo cáo.
