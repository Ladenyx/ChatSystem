# Ảnh chạy thực tế — Khung điều hướng & Theme

Ảnh được render từ ứng dụng JavaFX khi chạy kiểm thử trên Linux, Temurin JDK 25, JavaFX 25, AtlantaFX 2.0.1 và màn hình ảo Xvfb. Đây là **khung UI của mục 1.2**. Figma hiện chủ yếu là wireframe và tài liệu tham khảo: wireframe Admin đã hoàn thành, User chưa hoàn thành; các ảnh này không đại diện cho bản triển khai đầy đủ hoặc thiết kế cuối cùng đã được nhóm chốt.

| Ảnh | Nội dung |
| --- | --- |
| [User Light](user-light.png) | Rail và khung hội thoại, Primer Light, 1280 × 720 |
| [User Dark](user-dark.png) | Đổi Primer Dark trực tiếp trên cùng trang |
| [User Drawer](user-drawer.png) | Drawer thông tin bên phải, vùng nội dung co lại |
| [User Modal](user-modal.png) | Dialog có owner, nội dung khung Hồ sơ & Bảo mật |
| [User Popover](user-popover.png) | Chọn sáng/tối và giảm chuyển động |
| [User Small](user-small.png) | User Client ở kích thước nội dung 1024 × 640 |
| [Admin Light](admin-light.png) | Sidebar phân nhóm và trang tổng quan, Primer Light |
| [Admin Dark](admin-dark.png) | Chuyển route hoạt động và đổi Primer Dark |
| [Admin Small](admin-small.png) | Admin App ở kích thước nội dung 1024 × 640 |

Ảnh modal/popover chỉ chụp vùng nội dung cửa sổ tương ứng. Tạo lại bằng `./mvnw -Pui-tests -Dchatsystem.capture=true verify`; ảnh mới nằm trong `target/screenshots/` của từng ứng dụng. Hướng dẫn chi tiết tại [desktop-ui.md](../../architecture/desktop-ui.md#kiểm-thử).
