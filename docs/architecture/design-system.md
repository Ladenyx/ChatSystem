# Design system ChatSystem

Dùng [trang design-system trong Figma](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=7-2) làm nguồn thiết kế và [bản xuất JSON](design-system.tokens.json) để tra giá trị khi triển khai User Client và Admin App. Bản xuất ghi 95 biến dùng chung, 10 kiểu chữ và 3 kiểu đổ bóng. Các biến mô phỏng trạng thái của từng màn hình thuộc prototype tương ứng.

## Token

| Collection | Số biến | Mode | Dùng cho |
| --- | ---: | --- | --- |
| `CS / Primitives` | 36 | `Value` | Bảng màu cơ sở để các màu ngữ nghĩa tham chiếu. |
| `CS / Color` | 33 | `Light`, `Dark`, `Wireframe` | Màu theo vai trò như nền, chữ, viền, focus, trạng thái, presence và bong bóng chat. |
| `CS / Metrics` | 26 | `Value` | Khoảng cách, bo góc, kích thước và độ dày nét. |

Tên biến, ID và mode trong JSON theo Figma. Mỗi giá trị tham chiếu có `alias` là ID của biến gốc và `resolved` là giá trị đã tra. Ví dụ, `color/brand/default` trỏ tới màu `#0f6cbd` trong Light và Dark, còn Wireframe dùng `#d1d1d1`. Tra mode theo tên trong `values` khi đọc bản xuất.

| Nhóm metric | Giá trị theo px |
| --- | --- |
| `space/*` | 0, 2, 4, 6, 8, 12, 16, 20, 24, 32, 40, 48, 64 |
| `radius/*` | 0, 4, 8, 12, 16, 999 |
| `size/*` | 24, 32, 40, 44, 48 |
| `stroke/*` | 1, 2 |

Xem trực tiếp [Light Foundations](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=19-71) và [Dark Foundations](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=543-1053) để thấy cách kết hợp các token.

## Chữ và độ nổi

Các kiểu chữ dưới đây dùng Inter. Cỡ chữ và chiều cao dòng trong bảng là giá trị thiết kế Figma.

| Kiểu chữ | Độ đậm | Cỡ chữ (px) | Chiều cao dòng (px) |
| --- | ---: | ---: | --- |
| `CS / Caption` | 400 | 12 | 16 |
| `CS / Caption strong` | 600 | 12 | 16 |
| `CS / Body` | 400 | 14 | 20 |
| `CS / Body strong` | 600 | 14 | 20 |
| `CS / Subtitle` | 600 | 16 | 22 |
| `CS / Title` | 600 | 20 | 28 |
| `CS / Heading` | 600 | 28 | 36 |
| `CS / Display` | 700 | 40 | 48 |
| `CS / KPI compact` | 600 | 20 | Tự động |
| `CS / KPI large` | 600 | 26 | Tự động |

| Kiểu đổ bóng | Lệch X / Y (px) | Bán kính (px) | Độ mờ màu đen |
| --- | --- | ---: | ---: |
| `CS/Shadow 2` | 0 / 1 | 2 | 12% |
| `CS / Shadow 8` | 0 / 4 | 8 | 14% |
| `CS / Shadow 16` | 0 / 8 | 16 | 16% |

## Component và mẫu giao diện

Các link trỏ đến mẫu và biến thể trong [trang Components](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=465-20511). Khi chọn mẫu cho một màn hình cụ thể, đối chiếu bố cục và trạng thái theo [mã màn hình](figma_links.md).

| Nhóm | Mẫu trong Figma |
| --- | --- |
| [Navigation & Sidebars](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=470-20412) | [Navigation item](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=18-337), [Rail](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=475-1464), [Sidebar](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=475-1465), [Conversation action row](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=591-24011), [Conversation info panel](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=591-24014), [Conversation search panel](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=606-24310) |
| [People & Search](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=470-20413) | [People item](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=174-7573) |
| [Feedback & Dialogs](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=470-20414) | [Notice](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=22-661), [Empty state](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=22-692), [Dialog](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=22-719) |
| [Icons & Identity](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=490-1426) | [Presence](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=18-63), [Avatar](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=18-104), [Badge](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=18-111) |
| [Core Controls](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=490-1427) | [Button](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=18-160), [Icon button](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=18-209), [Field](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=18-240), [Search box](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=18-265), [Checkbox](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=18-293), [Switch](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=18-318), [Code input cell](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=524-1053) |
| [Messaging](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=490-1428) | [Chat header](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=473-1418), [Conversation item](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=22-409), [Message bubble](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=22-457), [Message composer](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=22-596), [Attachment](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=22-490), [Typing indicator](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=22-619), [Message row](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=606-24237), [Message context menu](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=606-24252) |
| [AI & Reports](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=606-24110) | [Message excerpt](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=606-24302), [AI suggestion option](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=606-24343) |

Xem thêm [Core controls](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=19-170), [Chat patterns](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=23-441) và [Dark mode / Chat patterns](https://www.figma.com/design/4kSG28zCQIaiT5JpAoxAKb/Chat-System?node-id=543-1545) để đối chiếu các trạng thái của control ở từng theme.

## Áp dụng vào JavaFX

- Dùng điều khiển JavaFX và theme AtlantaFX Primer cho các mẫu control. Dùng CSS của cụm để áp bố cục và trạng thái cần cho màn hình tương ứng.
- Tra [bảng ánh xạ JavaFX CSS](desktop-ui.md#theme-và-liên-hệ-với-figma) trước khi thêm màu. Hai app đọc bộ alias `-cs-*` trong [desktop.css](../../src/desktop-ui/src/main/resources/com/chatsystem/desktop/css/desktop.css). JSON lưu bộ token Figma để tra cứu, còn stylesheet định nghĩa các alias đang dùng trong app.
- Khi cần thêm alias dùng chung, tra tên và giá trị Light/Dark từ bản xuất rồi cập nhật stylesheet cùng tài liệu ánh xạ. Mỗi cụm có thể dùng số px từ thang metric trong FXML/CSS của mình.
- Khi Figma đổi token hoặc component, cập nhật bản xuất và liên kết bị ảnh hưởng cùng thay đổi liên quan trong code. Xác minh cả hai theme trước khi dùng alias mới.
