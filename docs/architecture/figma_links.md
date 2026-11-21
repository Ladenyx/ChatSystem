# Tham chiếu Figma — ChatSystem

[File Figma của nhóm](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System).

Các frame dưới đây là điểm bắt đầu để tham chiếu bố cục và trạng thái UI. Dùng [đặc tả màn hình](../requirements/screen.md) để xác định hành vi và các trạng thái bắt buộc khi triển khai. Kiểm tra các biến thể lân cận trong Figma khi màn hình có nhiều trạng thái.

## Nền tảng dùng chung

| Phần | Node |
| --- | --- |
| Màu sáng, Inter, khoảng cách và bo góc | [Light Foundations](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=19-71) |
| Màu tối và trạng thái | [Dark Foundations](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=543-1053) |
| Điều khiển và mẫu giao diện lặp lại | [Components](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=465-20511) |
| Thông báo trạng thái sáng | [Notice](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=23-832) |
| Rail User Client | [WF / Rail — 35:2](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=35-2) |
| Sidebar phân nhóm Admin App | [Sidebar — 192:1076](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=192-1076) |

Ánh xạ các màu sang CSS JavaFX và quy ước khoảng cách nằm trong [kiến trúc UI](desktop-ui.md#theme-và-liên-hệ-với-figma).

## User Client

| Mã | Màn hình | Frame tham chiếu |
| --- | --- | --- |
| `U-SCR-01` | Đăng nhập | [Mặc định](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=119-3552) |
| `U-SCR-02` | Đăng ký | [Bước tên đăng nhập](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=102-3552) |
| `U-SCR-03` | Xác minh email | [Chưa nhập mã](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=116-3552) |
| `U-SCR-04` | Quên mật khẩu | [Nhập tên đăng nhập](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=122-3552) |
| `U-SCR-05` | Đặt mật khẩu bắt buộc | [Nhập mật khẩu tạm](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=126-3552) |
| `U-SCR-06` | Thiết lập và khôi phục khóa E2EE | [Các trạng thái thiết lập và khôi phục](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=133-6122) |
| `U-SCR-07` | Hội thoại | [Chat cá nhân](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-251), [chat nhóm](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=36-418), [preview sáng/tối](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=552-25101) |
| `U-SCR-08` | Bạn bè và kết nối | [Tất cả bạn bè](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=176-7612) |
| `U-SCR-09` | Thông tin đoạn chat | [Drawer nhóm](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=427-14064), [drawer cá nhân](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=427-14199) |
| `U-DLG-01` | Hồ sơ và bảo mật | [Tab hồ sơ](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=149-5684) |
| `U-DLG-02` | Tạo nhóm | [Hộp thoại tạo nhóm E2EE](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=673-43182) |
| `U-DLG-03` | Trợ lý AI | [Refine](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=434-15052), [Recommend](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=434-15073) |
| `U-DLG-04` | Báo cáo spam | [Chọn tin và lý do](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=328-13145), [xem trước bằng chứng](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=326-12920) |
| `U-DLG-05` | Chọn Admin kế nhiệm | [Chưa chọn người kế nhiệm](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=669-28562) |

## Admin App

| Mã | Màn hình | Frame tham chiếu |
| --- | --- | --- |
| `A-SCR-01` | Đăng nhập quản trị | [Form đăng nhập](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=364-10701) |
| `A-SCR-02` | Tổng quan | [Dashboard](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=192-1067) |
| `A-SCR-03` | Quản lý người dùng | [Preview sáng](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=552-34298), [preview tối](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=552-34372), [trạng thái chưa chọn](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=253-1268) |
| `A-SCR-04` | Quản lý nhóm chat | [Danh sách và thành viên nhóm](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=214-1189) |
| `A-SCR-05` | Kiểm duyệt spam | [Danh sách và bằng chứng](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=225-1379) |
| `A-SCR-06` | Nhật ký đăng nhập | [Nhật ký hệ thống](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=229-1525) |
| `A-SCR-07` | Thống kê đăng ký mới | [Bố cục](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=362-9464), [preview sáng](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=556-11040), [preview tối](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=556-11232) |
| `A-SCR-08` | Thống kê hoạt động | [Biểu đồ và bảng chi tiết](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=362-9622) |
| `A-SCR-09` | Mạng lưới bạn bè | [Bảng phân tích](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=362-9780) |
| `A-DLG-01` | Thêm hoặc cập nhật người dùng | [Thêm người dùng](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=249-1497), [sửa hồ sơ](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=257-4714) |
| `A-DLG-02` | Xác nhận thao tác | [Xác nhận khóa tài khoản](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=225-1724) (lớp ẩn mặc định trong màn hình kiểm duyệt spam) |
