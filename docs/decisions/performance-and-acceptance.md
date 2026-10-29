# Hiệu năng và nghiệm thu

**DEC-09:** ưu tiên nghiệm thu **Windows**, demo nhiều máy qua server Internet. macOS/Arch Linux phục vụ phát triển; phát hành đủ ba OS là bổ sung. Thay yêu cầu cũ phải có GUI trên cả ba OS cùng đợt nghiệm thu.

| Hạng mục | Mục tiêu đề xuất, chưa đo đạt |
| --- | --- |
| Tải | 20 client/tài khoản; tổng 4 tin/giây trong 30 phút |
| Dữ liệu | 1.000 user, 100 nhóm, 100.000 tin ban đầu |
| Chat | p95 bấm gửi → GUI người nhận giải mã/render ≤1 giây |
| Lịch sử | Trang 50 tin ≤2 giây |
| Tìm kiếm | ≤2 giây khi lịch sử/chỉ mục đã sẵn sàng |
| Đồng bộ E2EE lần đầu | 10.000 tin được phép đọc ≤30 giây sau khi có khóa hợp lệ |
| Toàn vẹn | Không mất/hiển thị trùng tin server đã xác nhận, kể cả reconnect |

Ghi môi trường, phiên bản, mạng, dữ liệu, sai số đồng hồ, lỗi và tin chưa tới. Client giả lập không thay số đo render GUI; UI cần tiến độ khi đồng bộ và chưa coi kết quả đang tải là đầy đủ.

**Còn mở:** cấu hình Windows/server, số GUI thật/client giả lập, kích thước tin (1 KiB mới là đề xuất), giới hạn CPU/RAM sau đo. Ưu tiên Windows không tự giảm tải hoặc ngưỡng. V1 **22/11/2026**; V2 **12/12/2026**; owner/reviewer và hạn cá nhân TBD.
