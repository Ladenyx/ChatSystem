# Ảnh chạy bộ dữ liệu UI mẫu — mục 1.3

Ảnh chụp JavaFX thật trên Linux, Temurin JDK 25 + Xvfb, profile `ui-tests` và `-Dchatsystem.capture=true`. Đây là trang xem dữ liệu hư cấu dùng chung trong hai ứng dụng, không phải các màn hình nghiệp vụ giai đoạn 3–4 hay thiết kế Figma cuối cùng.

Figma hiện có wireframe Admin đã hoàn thành; wireframe User chưa hoàn thành. Trang kiểm tra dữ liệu dùng theme Primer của khung 1.2.

| Ảnh | Nội dung | Kích thước scene |
| --- | --- | --- |
| [user-mock-messages.png](user-mock-messages.png) | ListView hội thoại nhóm có 56 tin, tên tiếng Việt và trạng thái gửi | 1280 × 720 |
| [user-mock-chart-dark.png](user-mock-chart-dark.png) | BarChart 12 tháng số người mở app duy nhất, bảng giá trị tương ứng, Primer Dark | 1280 × 720 |
| [admin-mock-users-small.png](admin-mock-users-small.png) | TableView người dùng và chi tiết hồ sơ đang chọn | 1024 × 640 |
| [admin-mock-spam-small.png](admin-mock-spam-small.png) | Báo cáo nhóm có 31 bằng chứng: 1 Selected cũ + 30 Context | 1024 × 640 |
| [admin-mock-chart-small-dark.png](admin-mock-chart-small-dark.png) | Đăng ký mới đủ 12 tháng, tháng trống bằng 0 và bảng cuộn | 1024 × 640 |

Tái tạo từ repository root với JDK 25 và desktop session/Xvfb:

```sh
./mvnw -Pui-tests -Dchatsystem.capture=true verify
```

Ảnh gốc nằm trong `src/user/target/screenshots/` và `src/admin/target/screenshots/`. Xem [API và cách dùng bộ mẫu](../../architecture/ui-mock-data.md).
