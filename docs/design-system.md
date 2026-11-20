# Chat System — Design System

Nguồn: [tab design-system trên Figma](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=7-2), trích xuất 20/11/2026. Figma là nguồn thiết kế chính; [ảnh tham khảo](design-system/README.md). Các giá trị là pixel thiết kế, không khẳng định đã áp dụng đầy đủ trong JavaFX.

## Màu ngữ nghĩa

| Token Figma | Light | Dark |
|---|---|---|
| `color/bg/canvas` | `#F5F5F5` | `#141414` |
| `color/bg/surface` | `#FFFFFF` | `#1F1F1F` |
| `color/bg/subtle` | `#FAFAFA` | `#292929` |
| `color/bg/hover` | `#F0F0F0` | `#333333` |
| `color/bg/pressed` | `#E0E0E0` | `#424242` |
| `color/bg/disabled` | `#F0F0F0` | `#333333` |
| `color/text/primary` | `#1F1F1F` | `#FFFFFF` |
| `color/text/secondary` | `#616161` | `#D1D1D1` |
| `color/text/placeholder` | `#616161` | `#9E9E9E` |
| `color/text/disabled` | `#616161` | `#9E9E9E` |
| `color/text/on-brand` | `#FFFFFF` | `#FFFFFF` |
| `color/text/link` | `#115EA3` | `#77B7F7` |
| `color/border/default` | `#D1D1D1` | `#424242` |
| `color/border/strong` | `#707070` | `#9E9E9E` |
| `color/border/focus` | `#0F6CBD` | `#479EF5` |
| `color/brand/default` | `#0F6CBD` | `#0F6CBD` |
| `color/brand/hover` | `#115EA3` | `#115EA3` |
| `color/brand/pressed` | `#0F548C` | `#0F548C` |
| `color/brand/subtle` | `#EBF3FC` | `#0C3B5E` |
| `color/message/outgoing` | `#0F6CBD` | `#0F6CBD` |
| `color/message/incoming` | `#F0F0F0` | `#333333` |
| `color/message/text-outgoing` | `#FFFFFF` | `#FFFFFF` |
| `color/status/success` | `#107C10` | `#54B054` |
| `color/status/success-bg` | `#F1FAF1` | `#152C1C` |
| `color/status/danger` | `#B10E1C` | `#F1707B` |
| `color/status/danger-bg` | `#FDF3F4` | `#361C24` |
| `color/status/warning` | `#8A3707` | `#FCE100` |
| `color/status/warning-bg` | `#FFF9F5` | `#332717` |
| `color/presence/online` | `#107C10` | `#54B054` |
| `color/presence/away` | `#8A3707` | `#FCE100` |
| `color/presence/busy` | `#B10E1C` | `#F1707B` |
| `color/presence/offline` | `#707070` | `#9E9E9E` |
| `color/text/on-danger` | `#FFFFFF` | `#000000` |

## Typography

Font **Inter**; letter spacing 0%. Các style dưới đây lấy từ Figma, line-height `auto` được giữ nguyên.

| Style | Weight | Size | Line height |
|---|---:|---:|---:|
| Caption | 400 | 12px | 16px |
| Caption strong | 600 | 12px | 16px |
| Body | 400 | 14px | 20px |
| Body strong | 600 | 14px | 20px |
| Subtitle | 600 | 16px | 22px |
| Title | 600 | 20px | 28px |
| Heading | 600 | 28px | 36px |
| Display | 700 | 40px | 48px |
| KPI compact | 600 | 20px | auto |
| KPI large | 600 | 26px | auto |

## Kích thước và hiệu ứng

- Spacing: `0, 2, 4, 6, 8, 12, 16, 20, 24, 32, 40, 48, 64px`.
- Radius: `0, 4, 8, 12, 16, 999px`; stroke `1, 2px`; size `24, 32, 40, 44, 48px`.
- Shadow 2: `0 1px 2px 0 rgba(0,0,0,0.12)`; Shadow 8: `0 4px 8px 0 rgba(0,0,0,0.14)`; Shadow 16: `0 8px 16px 0 rgba(0,0,0,0.16)`.

## Components và cách dùng

Đây là design system giao diện chat desktop, **không phải thư viện slide**; các ví dụ slide hay code block chưa được xác nhận là component Figma.

- **Button:** hành động chính dùng Primary; hành động phụ dùng Secondary. Có kích thước 32/40 và các trạng thái default, hover, pressed, focus, disabled, loading.
- **Icon button:** thao tác ngắn bằng icon, kích thước 32/40, có trạng thái active; cần nhãn truy cập.
- **Field:** trường nhập có nhãn, nội dung, helper và phản hồi lỗi; dùng viền cùng chữ mô tả lỗi thay vì chỉ đổi màu.
- **Chat patterns:** tái sử dụng màu `message/outgoing`, `message/incoming` và trạng thái `presence/*` cho bong bóng tin nhắn và trạng thái người dùng; tham khảo [Light](design-system/03-chat-patterns-light.png) và [Dark](design-system/08-chat-patterns-dark.png).
- **Bề mặt và nội dung:** dùng `bg/canvas` cho nền tổng, `bg/surface` cho bề mặt, `text/primary` và `text/secondary` cho phân cấp; `status/*` cho thông báo có ngữ nghĩa.

[CSS variables cho HTML](design-system/tokens.css) là bản chuyển đổi tham khảo; JavaFX dùng token riêng trong [desktop.css](../src/desktop-ui/src/main/resources/com/chatsystem/desktop/css/desktop.css), không đọc file CSS HTML này.
