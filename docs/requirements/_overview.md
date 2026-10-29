# Tổng quan yêu cầu — Hệ thống Chat

Tài liệu tóm tắt yêu cầu ban đầu từ đề bài đồ án cuối kỳ môn **Lập trình ứng dụng Java** (BM. Công nghệ phần mềm – Khoa CNTT, Trường ĐH KHTN TP.HCM). Hệ thống gồm **2 ứng dụng Desktop giao diện đồ họa (GUI)** dành cho **Người quản trị (Admin App)** và **Người sử dụng (User Client)**.

---

## 1. Phân hệ Người quản trị (Admin App — Desktop GUI)

Ứng dụng giao diện đồ họa trực quan tập trung vào quản trị tài khoản, giám sát hoạt động và biểu diễn thống kê/biểu đồ hệ thống:

| Nhóm chức năng | Yêu cầu chi tiết |
| :--- | :--- |
| **Quản lý người dùng** | Quản lý hồ sơ (`tên đăng nhập`, `họ tên`, `địa chỉ`, `ngày sinh`, `giới tính`, `email`); lọc theo tên/tên đăng nhập/trạng thái; sắp xếp theo tên/ngày tạo; thêm, cập nhật, xóa, khóa/mở khóa tài khoản, cập nhật mật khẩu; xem lịch sử đăng nhập và danh sách bạn bè. |
| **Lịch sử đăng nhập** | Xem danh sách các lượt đăng nhập theo thứ tự thời gian (`thời gian`, `tên đăng nhập`, `họ tên`). |
| **Quản lý nhóm chat** | Xem danh sách nhóm (lọc theo tên, sắp xếp theo tên/thời gian tạo); xem danh sách thành viên và danh sách admin của từng nhóm. |
| **Xử lý báo cáo spam** | Xem danh sách báo cáo spam (lọc theo thời gian/tên đăng nhập, sắp xếp theo thời gian/tên đăng nhập) và khóa tài khoản vi phạm. |
| **Thống kê đăng ký mới** | Lọc người dùng đăng ký mới theo khoảng thời gian (lọc theo tên, sắp xếp theo tên/thời gian tạo); vẽ biểu đồ số lượng đăng ký mới theo 12 tháng của năm được chọn. |
| **Thống kê mạng lưới bạn bè** | Xem danh sách người dùng kèm số bạn trực tiếp và tổng số bạn tính cả "bạn của bạn"; sắp xếp theo tên/thời gian tạo; lọc theo tên hoặc so sánh (`=`, `<`, `>`) số bạn trực tiếp. |
| **Thống kê người dùng hoạt động** | Xem người dùng hoạt động trong khoảng thời gian cùng số liệu mở ứng dụng, số người đã chat, số nhóm đã chat (sắp xếp theo tên/thời gian tạo; lọc theo tên hoặc so sánh `=`, `<`, `>` số lượng hoạt động); vẽ biểu đồ số người có mở ứng dụng theo 12 tháng của năm được chọn. |

---

## 2. Phân hệ Người sử dụng (User)

Cung cấp trải nghiệm nhắn tin cá nhân, nhóm và kết nối bạn bè:

- **Tài khoản & Xác thực**:
  - Đăng ký tài khoản, đăng nhập.
  - Cập nhật thông tin cá nhân, đổi mật khẩu.
  - Khôi phục mật khẩu (tạo mật khẩu ngẫu nhiên và gửi qua email).
- **Bạn bè & Tìm kiếm**:
  - Quản lý danh sách bạn bè: lọc theo tên, xem trạng thái online/offline, hủy kết bạn, hoặc block kèm hủy kết bạn (chặn gửi lại lời mời).
  - Yêu cầu kết bạn: tìm theo tên để gửi lời mời; lọc danh sách lời mời chờ xử lý và đồng ý/từ chối.
  - Xem danh sách bạn bè đang online (lọc theo tên, bắt đầu chat hoặc tạo nhóm).
  - Tìm người dùng theo tên đăng nhập hoặc họ tên (ẩn người đang block mình) để chat hoặc tạo nhóm.
  - Báo cáo spam khi bị làm phiền qua tin nhắn.
- **Chat cá nhân (1-1)**:
  - Nhắn tin thời gian thực khi người nhận online; lưu tin nhắn chờ khi người nhận offline.
  - Xem lịch sử chat; xóa một/vài dòng hoặc xóa toàn bộ lịch sử chat.
  - Tìm kiếm chuỗi trong lịch sử chat với 1 người hoặc với tất cả mọi người, cho phép di chuyển đến đúng đoạn hội thoại tìm thấy.
  - Tích hợp **LLM** với **2 nút thao tác riêng**: **1 nút chỉnh sửa** nội dung đang soạn nháp và **1 nút gợi ý (recommend)** câu trả lời theo ngữ cảnh hội thoại.
- **Nhóm chat**:
  - Tạo nhóm (thêm tối thiểu 1 thành viên theo tên đăng nhập), đổi tên nhóm, chat nhóm.
  - Quản trị thành viên: thêm thành viên, gán quyền admin nhóm, xóa thành viên (chỉ admin mới có quyền xóa).
  - Hỗ trợ **mã hóa nhóm chat** (tham khảo tính năng nhóm chat được mã hóa của Facebook).

---

## 3. Yêu cầu chung & Ràng buộc bắt buộc

- **Quản lý mã nguồn**: Bắt buộc sử dụng Git; thành viên không có commit sẽ nhận **0 điểm**.
- **Đầy đủ thành phần**: Nộp toàn bộ mã nguồn của cả 2 phân hệ kèm script tạo cơ sở dữ liệu.
- **Quy định chấm điểm (các trường hợp 0 điểm)**:
  - Sai cấu trúc tập tin nén khi nộp bài.
  - Tập tin báo cáo thiếu nội dung theo yêu cầu.
  - Thiếu báo cáo tự đánh giá theo mẫu ở giai đoạn cuối.

---

## 4. Lộ trình bàn giao (Deadlines)

| Giai đoạn | Trọng số | Phạm vi chính | Thành phần nộp trong tập tin nén |
| :--- | :---: | :--- | :--- |
| **Phiên bản 1** | **20%** | Giao diện (UI) & Cơ sở dữ liệu | - `src/`: Mã nguồn giao diện các màn hình của 2 phân hệ (chưa cần xử lý sự kiện)<br>- `script/`: Script tạo CSDL<br>- `git/`: Ảnh chụp commit của từng thành viên<br>- **Báo cáo**: Thông tin nhóm, phân công, bảng đóng góp (tổng 100), thiết kế CSDL, danh sách màn hình kèm ảnh chụp |
| **Phiên bản 2** | **80%** | Hoàn thiện toàn bộ hệ thống | - `src/`: Mã nguồn chạy được của 2 phân hệ<br>- `release/`: Ít nhất 2 file `.jar` (quản lý, người dùng) + 1 file cấu hình kết nối CSDL (sửa cấu hình là chạy được `.jar`)<br>- `script/`: Script tạo CSDL<br>- `git/`: Ảnh chụp commit của từng thành viên<br>- **Báo cáo**: Thông tin nhóm, phân công, bảng đóng góp (tổng 100), tính năng/kỹ thuật nổi bật hoặc mở rộng, thiết kế hệ thống & CSDL, hướng dẫn sử dụng (có hình), link YouTube demo, link GitHub<br>- **Báo cáo tự đánh giá** (theo mẫu) |

