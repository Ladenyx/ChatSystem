# Bản đồ Use Case — UC, File, Screen và Feature

Đây là tài liệu tổng quan để tìm, review và quản lý các đặc tả trong `use-cases/user/` và `use-cases/admin/`, dựa vào [screen.md](screen.md)

## Mục lục

- [1. Tổng quan danh mục](#1-tổng-quan-danh-mục)
- [2. Ánh xạ từ UC và file đặc tả](#2-ánh-xạ-từ-uc-và-file-đặc-tả)
  - [2.1. User — Nhắn tin](#21-user--nhắn-tin)
  - [2.2. User — Bạn bè và kết nối](#22-user--bạn-bè-và-kết-nối)
  - [2.3. User — Quản lý nhóm](#23-user--quản-lý-nhóm)
  - [2.4. User — Trợ lý AI](#24-user--trợ-lý-ai)
  - [2.5. User — Báo cáo spam](#25-user--báo-cáo-spam)
  - [2.6. Admin — Kiểm duyệt và yêu cầu rút](#26-admin--kiểm-duyệt-và-yêu-cầu-rút)
- [3. Tra ngược theo Screen ID](#3-tra-ngược-theo-screen-id)
- [4. Tra ngược theo Feature ID đã có trong screen.md](#4-tra-ngược-theo-feature-id-đã-có-trong-screenmd)
- [5. Khoảng trống và mã feature đề xuất](#5-khoảng-trống-và-mã-feature-đề-xuất)

## 1. Tổng quan danh mục

| Phân hệ | Module | Dải mã UC | Số file UC | Trạng thái tài liệu hiện tại |
| --- | --- | --- | --- | --- |
| User | `messaging` | UC-U-MSG-01 đến UC-U-MSG-06 | 6 | Draft |
| User | `friend-management` | UC-U-FRD-01 đến UC-U-FRD-08 | 8 | Draft |
| User | `group-management` | UC-U-GRP-01 đến UC-U-GRP-07 | 7 | Draft |
| User | `llm-assistant` | UC-U-AI-01 đến UC-U-AI-02 | 2 | Draft |
| User | `spam-reporting` | UC-U-RPT-01 đến UC-U-RPT-03 | 3 | Draft |
| Admin | `spam-reporting` | UC-A-RPT-01 đến UC-A-RPT-02 | 2 | Draft |
| **Tổng** | **26 User + 2 Admin** | **28 mã độc lập** | **28** | **28 Draft** |

## 2. Ánh xạ từ UC và file đặc tả

### 2.1. User — Nhắn tin

| UC ID | File đặc tả / Use Case Name | Screen ID | Feature ID từ screen.md | Trạng thái UC | Phạm vi / điểm cần quản lý |
| --- | --- | --- | --- | --- | --- |
| `UC-U-MSG-01` | [view-conversation-history.md](use-cases/user/messaging/view-conversation-history.md)<br>Xem hội thoại và lịch sử | U-SCR-07 | U-14; U-25 (ràng buộc nhóm) | Draft | Đọc lịch sử; việc thiết lập/khôi phục khóa là chức năng hỗ trợ khác. |
| `UC-U-MSG-02` | [send-message.md](use-cases/user/messaging/send-message.md)<br>Gửi tin nhắn | U-SCR-07 | U-12, U-13, U-24; U-25 (nhóm) | Draft | Gửi cá nhân/nhóm, online/offline; tối đa 2.000 ký tự/tin theo BR-06. Cách đếm Unicode và quyền chat chưa kết bạn còn CF-20/24. |
| `UC-U-MSG-03` | [search-messages.md](use-cases/user/messaging/search-messages.md)<br>Tìm tin nhắn | U-SCR-07, U-SCR-09 | U-17, U-18 | Draft | Một/tất cả hội thoại là phạm vi tìm; cách so khớp và phân trang còn CF-21. |
| `UC-U-MSG-04` | [delete-messages-for-self.md](use-cases/user/messaging/delete-messages-for-self.md)<br>Xóa tin phía mình | U-SCR-07 | U-16 | Draft | Xóa một/nhiều tin phía mình; xử lý một phần theo CF-23. |
| `UC-U-MSG-05` | [clear-conversation-history.md](use-cases/user/messaging/clear-conversation-history.md)<br>Xóa toàn bộ lịch sử phía mình | U-SCR-09 | U-15 | Draft | Dùng chung cho chat riêng và nhóm E2EE, chỉ xóa lịch sử phía User; mốc cắt khi có tin đồng thời theo CF-22. |
| `UC-U-MSG-06` | [recall-message.md](use-cases/user/messaging/recall-message.md)<br>Thu hồi tin đã gửi | U-SCR-07 | U-16 | Draft | Thu hồi được nêu ở thao tác tin trong U-SCR-07; khác xóa phía mình. |

### 2.2. User — Bạn bè và kết nối

| UC ID | File đặc tả / Use Case Name | Screen ID | Feature ID từ screen.md | Trạng thái UC | Phạm vi / điểm cần quản lý |
| --- | --- | --- | --- | --- | --- |
| `UC-U-FRD-01` | [view-friends.md](use-cases/user/friend-management/view-friends.md)<br>Xem danh sách bạn bè | U-SCR-08 | U-06, U-11 | Draft | Danh sách bạn bè và bộ lọc online cùng mục tiêu xem. |
| `UC-U-FRD-02` | [search-users.md](use-cases/user/friend-management/search-users.md)<br>Tìm người dùng | U-SCR-08 | U-07 | Draft | Ẩn hai chiều khi tìm người còn Proposed (CF-01); so khớp/phân trang CF-21. |
| `UC-U-FRD-03` | [send-friend-request.md](use-cases/user/friend-management/send-friend-request.md)<br>Gửi lời mời kết bạn | U-SCR-08 | U-07 | Draft | Gửi lời mời; chưa tạo quan hệ bạn bè. |
| `UC-U-FRD-04` | [process-friend-request.md](use-cases/user/friend-management/process-friend-request.md)<br>Xử lý lời mời kết bạn | U-SCR-08 | U-08 | Draft | Xử lý lời mời; chấp nhận/từ chối là hai nhánh cùng mục tiêu. |
| `UC-U-FRD-05` | [unfriend-user.md](use-cases/user/friend-management/unfriend-user.md)<br>Hủy kết bạn | U-SCR-08 | U-09 | Draft | Hủy kết bạn; không tự chặn hoặc rời nhóm. |
| `UC-U-FRD-06` | [block-user.md](use-cases/user/friend-management/block-user.md)<br>Chặn người dùng | U-SCR-08, U-SCR-09 | U-10 | Draft | Hai điểm chặn cùng một UC; U-SCR-07 phản ánh trạng thái ô nhập đã chọn ở C03. |
| `UC-U-FRD-07` | [unblock-user.md](use-cases/user/friend-management/unblock-user.md)<br>Bỏ chặn người dùng | U-SCR-08 | U-10 | Draft | Bỏ chặn tại mục Đã chặn; không tự khôi phục tình bạn hoặc gửi lại tin lỗi. |
| `UC-U-FRD-08` | [view-conversations.md](use-cases/user/friend-management/view-conversations.md)<br>Xem và lọc danh sách hội thoại | U-SCR-07 | Chưa có mã riêng; U-14 liên quan lịch sử, U-24/U-25 liên quan nhóm/E2EE | Draft | Tất cả/Chưa đọc/Nhóm, danh sách chat riêng/nhóm E2EE và hiện diện; nhóm Online khi ít nhất một thành viên online đã được xác nhận. Không phải danh sách bạn bè U-06/U-11. Feature riêng, việc tính chính User vào hiện diện nhóm, thứ tự/phân trang/ghi nhớ bộ lọc còn CF-16/27/28. |

### 2.3. User — Quản lý nhóm

| UC ID | File đặc tả / Use Case Name | Screen ID | Feature ID từ screen.md | Trạng thái UC | Phạm vi / điểm cần quản lý |
| --- | --- | --- | --- | --- | --- |
| `UC-U-GRP-01` | [create-group.md](use-cases/user/group-management/create-group.md)<br>Tạo nhóm chat | U-DLG-02; điểm vào U-SCR-07, U-SCR-08 | U-19, U-25 | Draft | Tạo nhóm; giới hạn 75/50 và quy tắc liên quan giữ đúng nhãn Proposed (CF-02/05/06). |
| `UC-U-GRP-02` | [leave-group.md](use-cases/user/group-management/leave-group.md)<br>Rời nhóm | U-SCR-09; U-DLG-05 khi cần kế nhiệm | Chưa có mã rời nhóm riêng; U-22/U-25 liên quan | Draft | Rời nhóm có trong screen map; kế nhiệm là nhánh. Đóng nhóm/lịch sử và feature riêng còn CF-03/06/16. |
| `UC-U-GRP-03` | [relinquish-group-admin.md](use-cases/user/group-management/relinquish-group-admin.md)<br>Bàn giao quyền và trở thành thành viên thường | U-DLG-05; ngữ cảnh U-SCR-09 | U-22 liên quan; chưa có mã riêng cho bàn giao bản thân | Draft | Phạm vi ở lại làm thành viên còn Proposed (CF-04/16); không gộp với rời nhóm. |
| `UC-U-GRP-04` | [rename-group.md](use-cases/user/group-management/rename-group.md)<br>Đổi tên nhóm | U-SCR-09 | U-20 | Draft | Đổi tên; quyền, giới hạn và xung đột đồng thời theo CF-02/05/18. |
| `UC-U-GRP-05` | [add-group-member.md](use-cases/user/group-management/add-group-member.md)<br>Thêm thành viên nhóm | U-SCR-09 | U-21; U-25 (khóa nhóm) | Draft | Thêm thành viên; ma trận quyền, E2EE và kết quả theo lô còn CF-02/06/19. |
| `UC-U-GRP-06` | [manage-group-admin-rights.md](use-cases/user/group-management/manage-group-admin-rights.md)<br>Quản lý quyền Admin của thành viên | U-SCR-09 | U-22 | Draft | Cấp/thu hồi quyền thành viên khác được nêu trong screen map; ma trận quyền CF-02. |
| `UC-U-GRP-07` | [remove-group-member.md](use-cases/user/group-management/remove-group-member.md)<br>Xóa thành viên khỏi nhóm | U-SCR-09 | U-23; U-25 (khóa nhóm) | Draft | Xóa thành viên; không cấp quyền Admin hệ thống; CF-02/03/06. |

### 2.4. User — Trợ lý AI

| UC ID | File đặc tả / Use Case Name | Screen ID | Feature ID từ screen.md | Trạng thái UC | Phạm vi / điểm cần quản lý |
| --- | --- | --- | --- | --- | --- |
| `UC-U-AI-01` | [refine-draft.md](use-cases/user/llm-assistant/refine-draft.md)<br>Chỉnh sửa bản nháp bằng AI | U-SCR-07, U-DLG-03 | U-27 | Draft | Cải thiện nháp; quota/model/giới hạn/nhãn AI còn CF-08/09. |
| `UC-U-AI-02` | [recommend-message.md](use-cases/user/llm-assistant/recommend-message.md)<br>Nhận gợi ý tin nhắn bằng AI | U-SCR-07, U-DLG-03 | U-27 | Draft | Gợi ý theo ngữ cảnh là nền; prompt kết hợp/prompt-only và các giới hạn còn CF-07–09. |

### 2.5. User — Báo cáo spam

| UC ID | File đặc tả / Use Case Name | Screen ID | Feature ID từ screen.md | Trạng thái UC | Phạm vi / điểm cần quản lý |
| --- | --- | --- | --- | --- | --- |
| `UC-U-RPT-01` | [submit-spam-report.md](use-cases/user/spam-reporting/submit-spam-report.md)<br>Gửi báo cáo spam | U-SCR-09, U-DLG-04; hội thoại nguồn U-SCR-07 | U-26; U-25 (bằng chứng nhóm E2EE) | Draft | Giữ chat riêng và nhóm; mục tiêu kết thúc ở tiếp nhận. Điều kiện/giới hạn/xác thực còn CF-13–15. |
| `UC-U-RPT-02` | [track-spam-report.md](use-cases/user/spam-reporting/track-spam-report.md)<br>Theo dõi và xem kết quả báo cáo | U-DLG-04 (mục theo dõi đề xuất) | U-26 liên quan; chưa có mã theo dõi riêng | Draft | Proposed: danh sách, điểm mở và quyền xem metadata/bản rõ cần CF-10/16. |
| `UC-U-RPT-03` | [request-report-withdrawal.md](use-cases/user/spam-reporting/request-report-withdrawal.md)<br>Yêu cầu rút báo cáo | U-DLG-04 (điểm mở đề xuất) | U-26 liên quan; chưa có mã rút riêng | Draft | Proposed chờ leader: rút qua Admin, kể cả sau kết luận; CF-11–13/16. |

### 2.6. Admin — Kiểm duyệt và yêu cầu rút

| UC ID | File đặc tả / Use Case Name | Screen ID | Feature ID từ screen.md | Trạng thái UC | Phạm vi / điểm cần quản lý |
| --- | --- | --- | --- | --- | --- |
| `UC-A-RPT-01` | [moderate-spam-report.md](use-cases/admin/spam-reporting/moderate-spam-report.md)<br>Kiểm duyệt báo cáo spam | A-SCR-05; A-DLG-02 chỉ khi dẫn sang khóa | A-10 (xem/lọc); A-11 (chế tài liên quan) | Draft | Chưa có mã riêng cho kết luận/phản hồi; A-11 không thay thế mục tiêu kết luận. CF-12/15/16/25/26. |
| `UC-A-RPT-02` | [process-report-withdrawal.md](use-cases/admin/spam-reporting/process-report-withdrawal.md)<br>Xử lý yêu cầu rút báo cáo | A-SCR-05 (phần xử lý rút đề xuất) | A-10 liên quan; chưa có mã xử lý rút riêng | Draft | Proposed cùng yêu cầu rút; quyết định, hệ quả, deadline/lưu và thông báo theo CF-11/12/16/26. |

## 3. Tra ngược theo Screen ID

Bảng này phân biệt điểm thực hiện đã có trong screen map với vùng/luồng được đề xuất trong đặc tả mới. Liên kết UI dẫn tới tài liệu trình bày, không thay cho UC nghiệp vụ.

| Screen ID | Đặc tả UI chi tiết | UC liên quan | Ý nghĩa quan hệ |
| --- | --- | --- | --- |
| U-SCR-07 | [Hội thoại và khung chat](screens/user/u-scr-07-conversations-and-chat.md) | UC-U-FRD-08, UC-U-MSG-01/02/03/04/06, UC-U-AI-01/02, UC-U-GRP-01 | Xem/lọc danh sách hội thoại, đọc/gửi/thao tác tin, điểm mở AI và tạo nhóm; phản ánh kết quả chặn/rời/xóa lịch sử từ panel |
| U-SCR-08 | [Bạn bè và kết nối](screens/user/u-scr-08-contacts-and-people.md) | UC-U-FRD-01 đến 07, UC-U-GRP-01 | Các mục tiêu quan hệ, điểm mở chat và tạo nhóm |
| U-SCR-09 | [Panel thông tin](screens/user/u-scr-09-conversation-and-group-drawer.md) | UC-U-MSG-03/05, UC-U-FRD-06, UC-U-GRP-02 đến 07, UC-U-RPT-01 | Tìm/xóa lịch sử, chặn, quản lý nhóm, báo cáo; bỏ chặn dẫn qua U-SCR-08 |
| U-DLG-02 | [Tạo nhóm](screens/user/u-dlg-02-create-group-dialog.md) | UC-U-GRP-01 | Giao diện của mục tiêu tạo nhóm |
| U-DLG-03 | [Trợ lý AI](screens/user/u-dlg-03-llm-refine-and-recommend-popover.md) | UC-U-AI-01/02 | Hai mục tiêu cùng dùng một popover; trường prompt mở rộng còn Proposed |
| U-DLG-04 | [Báo cáo spam](screens/user/u-dlg-04-report-spam-dialog.md) | UC-U-RPT-01; UC-U-RPT-02/03 là phần đề xuất | Gửi từ chat riêng/nhóm E2EE đã có trong nguồn; mục theo dõi/rút chưa chốt |
| U-DLG-05 | [Chọn Admin kế nhiệm](screens/user/u-dlg-05-assign-successor-admin-dialog.md) | UC-U-GRP-02/03 | Bước/nhánh hỗ trợ mục tiêu rời hoặc bàn giao; không tạo UC chỉ để chọn người |
| A-SCR-05 | [Kiểm duyệt báo cáo](screens/admin/a-scr-05-spam-moderation.md) | UC-A-RPT-01/02 | Xem/xử lý báo cáo; vùng xử lý rút và phản hồi mở rộng còn Proposed |
| A-DLG-02 | Có mã trong screen map, chưa tách UI riêng trong đợt này | UC-A-RPT-01 chỉ dẫn sang thao tác khóa | Giao diện xác nhận chế tài A-11; không phải mục tiêu kết luận kiểm duyệt |

U-SCR-06/U-DLG-01 là giao diện thiết lập/khôi phục khóa hỗ trợ các UC E2EE. Các file UC hiện tại chỉ dẫn ngữ cảnh đó, không đặc tả đầy đủ mục tiêu thiết lập/khôi phục khóa.

## 4. Tra ngược theo Feature ID đã có trong screen.md

| Feature ID | UC tương ứng trong danh mục hiện tại | Mức độ bao phủ |
| --- | --- | --- |
| U-06, U-11 | UC-U-FRD-01 | Xem bạn bè và lọc online |
| U-07 | UC-U-FRD-02/03 | Tìm người và gửi lời mời là hai mục tiêu |
| U-08 | UC-U-FRD-04 | Xử lý lời mời |
| U-09 | UC-U-FRD-05 | Hủy kết bạn |
| U-10 | UC-U-FRD-06/07; UC-U-MSG-02 chịu ràng buộc chặn | Chặn/bỏ chặn và hệ quả gửi tin |
| U-12, U-13 | UC-U-MSG-02 | Gửi tin online/offline |
| U-14 | UC-U-MSG-01; UC-U-FRD-08 liên quan điểm truy cập | Đọc lịch sử; chưa có Feature ID riêng cho xem/lọc danh sách hội thoại |
| U-15 | UC-U-MSG-05 | Xóa cả lịch sử phía mình trong chat riêng hoặc nhóm E2EE |
| U-16 | UC-U-MSG-04/06 | Xóa riêng và thu hồi là hai mục tiêu khác nhau; nguồn U-SCR-07 nêu cả hai |
| U-17, U-18 | UC-U-MSG-03 | Tìm một/tất cả hội thoại |
| U-19 | UC-U-GRP-01 | Tạo nhóm |
| U-20 | UC-U-GRP-04 | Đổi tên nhóm |
| U-21 | UC-U-GRP-05 | Thêm thành viên |
| U-22 | UC-U-GRP-06; UC-U-GRP-02/03 liên quan kế nhiệm/bàn giao | Cấp/thu hồi quyền; không suy ra đã có feature riêng cho mọi mục tiêu rời/bàn giao |
| U-23 | UC-U-GRP-07 | Xóa thành viên |
| U-24 | UC-U-MSG-02, UC-U-MSG-01 trong ngữ cảnh nhóm; UC-U-FRD-08 liên quan loại hội thoại | Chat nhóm; chưa thay thế feature riêng cho xem/lọc danh sách hoặc rời nhóm |
| U-25 | Các UC đọc/gửi/tìm và quản lý nhóm; UC-U-RPT-01, UC-U-AI-01/02 chịu ràng buộc dữ liệu E2EE; UC-U-FRD-08 liên quan loại nhóm E2EE | Bao phủ ràng buộc liên quan; liệt kê metadata không đồng nghĩa đã có khóa đọc lịch sử. Chưa có UC thiết lập/khôi phục khóa đầy đủ trong danh mục |
| U-26 | UC-U-RPT-01; UC-U-RPT-02/03 là mở rộng liên quan | Mã gửi báo cáo hiện có không tự phê duyệt theo dõi/rút |
| U-27 | UC-U-AI-01/02 | Refine và Recommend |
| A-10 | UC-A-RPT-01 về xem hồ sơ; UC-A-RPT-02 là mở rộng liên quan | Chưa có mã trực tiếp cho mọi mục tiêu kết luận/xử lý rút |
| A-11 | UC-A-RPT-01 dẫn sang luồng khóa tài khoản | Chế tài liên quan; chưa có UC khóa độc lập trong đợt này |

Danh mục này quản lý **28 UC hiện có**, không khẳng định toàn bộ feature của hệ thống đã có UC chi tiết. U-01–U-05, các mục tiêu độc lập về khóa U-25, A-01–A-09 và A-12–A-16 chưa được các file hiện tại đặc tả đầy đủ. Khi thêm đặc tả ở các phạm vi đó, bổ sung dòng UC tương ứng thay vì suy từ độ phủ màn hình thành độ phủ UC.

## 5. Khoảng trống và mã feature đề xuất

Các mã ở cột đề xuất **chưa có trong screen.md**, nên không dùng thay cho mã nguồn ở bảng mục 2. Đây là dữ liệu để review, chờ CF-16 và quyết định phạm vi; giữ rõ quan hệ hỗ trợ với mã hiện có.

| Điểm quản lý | UC | Mã có trong screen.md / khoảng trống | Mã đang đề xuất | Trạng thái cần chốt |
| --- | --- | --- | --- | --- |
| F01 — Thu hồi | UC-U-MSG-06 | U-16, thao tác thu hồi được nêu ở U-SCR-07 | Không cấp thêm | Đã đối chiếu nguồn; giữ riêng UC xóa/thu hồi |
| F02 — Rời nhóm | UC-U-GRP-02 | Có điểm mở rời ở U-SCR-09, U-22 hỗ trợ kế nhiệm; thiếu mã riêng của mục tiêu | U-28 | CF-03/06/16 |
| F03 — Bàn giao và ở lại | UC-U-GRP-03 | U-22 liên quan cơ chế thay vai trò; thiếu mã riêng | U-29 | CF-04/16 |
| F04 — Thu hồi quyền Admin | UC-U-GRP-06 | U-22; U-SCR-09 nêu cấp/thu hồi | Không cấp thêm | Mã đã có; ma trận quyền vẫn CF-02 |
| F05 — Theo dõi báo cáo | UC-U-RPT-02 | U-26 liên quan gửi báo cáo; thiếu mã theo dõi riêng | U-30 | CF-10/16 |
| F06 — Yêu cầu rút | UC-U-RPT-03 | Chưa có mã trực tiếp | U-31 | CF-11/12/16 |
| F07 — Kết luận kiểm duyệt | UC-A-RPT-01 | A-10 xem, A-11 khóa; thiếu mã riêng cho kết luận/phản hồi | A-17 | CF-12/15/16/26 |
| F08 — Admin xử lý rút | UC-A-RPT-02 | A-10 chỉ là ngữ cảnh liên quan | A-18 | CF-11/12/16/26 |
| F09 — Xem/lọc danh sách hội thoại | UC-U-FRD-08 | U-SCR-07 nêu danh sách và ba bộ lọc; U-14/U-24/U-25 chỉ liên quan, chưa có mã riêng | Chưa cấp; cần thống nhất mã phù hợp | CF-16/27/28 |
