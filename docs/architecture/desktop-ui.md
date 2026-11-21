# Kiến trúc giao diện desktop

`UserApplication` và `AdminApplication` là hai JavaFX `Application` độc lập. Mỗi ứng dụng nạp `shell.fxml`, tạo `ViewNavigator` và mở một cửa sổ desktop. Các trang nghiệp vụ chưa hoàn thiện; bước này cung cấp nơi gắn FXML và kiểm tra luồng điều hướng.

## Phân chia module

| Module | Trách nhiệm |
| --- | --- |
| `common` | DTO/giao thức; không phụ thuộc JavaFX |
| `server` | Nghiệp vụ/tích hợp backend; không phụ thuộc UI |
| `desktop-ui` | `View`, `ViewController`, `ViewNavigator`, `ThemeManager`, CSS, font và dữ liệu UI mẫu dùng chung |
| `user` | Rail, route và FXML của User Client |
| `admin` | Sidebar phân nhóm, route và FXML của Admin App |

`desktop-ui` được thêm ở mục 1.2 để chia sẻ mã UI mà không đưa JavaFX vào `common` hoặc tạo phụ thuộc giữa hai ứng dụng. Bốn module nghiệp vụ của bước 1.1 giữ nguyên trách nhiệm.

## Cấu trúc thư mục theo cụm màn hình

Trong `src/user/` và `src/admin/`, dùng cùng tên cụm cho controller, tài nguyên FXML, CSS và kiểm thử của màn hình. Các đường dẫn cho màn hình Bạn bè trong `src/user/` có dạng sau.

```text
src/main/java/com/chatsystem/user/controller/contacts/ContactsController.java
src/main/resources/com/chatsystem/user/fxml/contacts/contacts.fxml
src/main/resources/com/chatsystem/user/css/contacts.css
src/test/java/com/chatsystem/user/controller/contacts/ContactsControllerTest.java
```

Khi cần CSS riêng, gắn stylesheet vào FXML của cụm, dùng tên lớp có tiền tố theo ứng dụng và cụm (ví dụ `.user-contacts`) và lấy màu từ theme chung. Các cụm dùng snapshot bất biến từ `MockData`. Trạng thái demo riêng của một màn hình, như bộ lọc hoặc lựa chọn đang hiển thị, thuộc cụm đó.

Mỗi thành viên phụ trách các file trong cụm được giao. Bảng phân công Version 1 dùng mã trong [danh mục màn hình](../requirements/screen.md).

| Module | Cụm | Màn hình / hộp thoại | Phụ trách |
| --- | --- | --- | --- |
| `user` | `auth/` | `U-SCR-01`–`U-SCR-06` | Thành viên 2 |
| `user` | `contacts/` | `U-SCR-08` | Thành viên 2 |
| `user` | `profile/` | `U-DLG-01` | Thành viên 2 |
| `user` | `messaging/` | `U-SCR-07`, `U-SCR-09`, `U-DLG-02`–`U-DLG-05` | Thành viên 3 |
| `admin` | `auth/`, `overview/`, `users/` | `A-SCR-01`, `A-SCR-02`, `A-SCR-03`, tương ứng | Thành viên 1 |
| `admin` | `groups/`, `spam/` | `A-SCR-04`, `A-SCR-05`, tương ứng | Thành viên 2 |
| `admin` | `logs/` | `A-SCR-06` | Thành viên 1 |
| `admin` | `analytics/` | `A-SCR-07`–`A-SCR-09` | Thành viên 1 |
| `admin` | `dialogs/` | `A-DLG-01`, `A-DLG-02` | Thành viên 1 |

Thành viên 3 là đầu mối tích hợp User Client, gồm `UserApplication`, `UserViews` và shell. Thành viên 1 phụ trách tương ứng `AdminApplication`, `AdminViews` và shell của Admin App. Khi một luồng đi qua hai cụm, người phụ trách thống nhất dữ liệu cần truyền và cách mở màn hình qua lớp điều hướng. Ví dụ, Bạn bè mở hội thoại theo ID người dùng, còn màn hình Kiểm duyệt Spam mở hộp thoại xác nhận thao tác quản trị `A-DLG-02`. Phối hợp trước khi sửa các thành phần trong `desktop-ui` vì cả hai ứng dụng cùng sử dụng module này.

## Điều hướng và vòng đời

- `View` định danh route bằng mã màn hình, tiêu đề, class sở hữu tài nguyên và đường dẫn FXML tuyệt đối. Route được khai báo trong `UserViews` hoặc `AdminViews`.
- `navigate(view)` nạp FXML ở lần đầu, giữ trang trong cache của cửa sổ, lưu lịch sử và đổi nội dung của `viewHost`. Đi lại giữa các trang giữ trạng thái điều khiển.
- `goBack()` dùng cùng cache; nút Quay lại và Alt + ← được nối sẵn. Chọn lại trang hiện tại không thêm một bước lịch sử.
- `reset(view)` nạp mới trang và bỏ toàn bộ cache/lịch sử. Dùng khi đổi tài khoản hoặc kết thúc phiên để không giữ nháp/dữ liệu của người trước.
- FXML được nạp trước khi thay đổi trang/lịch sử; tài nguyên thiếu hoặc FXML sai gây `ViewLoadException` có đường dẫn và nguyên nhân, giữ trang cũ. Không dùng exception này như thông báo lỗi nghiệp vụ cho người dùng.
- Controller có thể implement `ViewController`: `onEnter(navigator, view)` chạy mỗi lần mở; `onLeave()` chạy khi rời trang hoặc đóng overlay. Dừng timer, listener và tác vụ theo trang tại `onLeave()`.
- Chuyển trang đóng modal/drawer/popover đang mở. Đóng cửa sổ gọi `navigator.close()` để dọn cache và vòng đời trang.

Tất cả API điều hướng/theme phải gọi trên JavaFX Application Thread. Công việc I/O sau này chạy ngoài thread này và trả kết quả bằng `Platform.runLater`. Không đưa truy vấn DB, socket hoặc API vào controller điều hướng.

## Gắn màn hình mới

Tạo FXML và controller trong module phụ trách. Ví dụ thay màn hình khung Bạn bè bằng tài nguyên thật:

```java
public static final View CONTACTS = new View(
        "U-SCR-08", "Bạn bè & Kết nối", UserApplication.class,
        "/com/chatsystem/user/fxml/contacts/contacts.fxml");
```

Controller cần điều hướng sử dụng lifecycle thay vì truy cập biến toàn cục:

```java
public final class ContactsController implements ViewController {
    private ViewNavigator navigator;

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
    }

    @FXML
    private void openConversation() {
        navigator.navigate(UserViews.CONVERSATIONS);
    }
}
```

FXML khai báo `fx:controller` và các handler như bình thường. `initialize()` chỉ nên khởi tạo điều khiển; `navigator` được cung cấp sau khi FXMLLoader hoàn tất. Mỗi FXML root phải là `Parent`. Các route dùng `View.placeholder(...)` hiện tại phải được thay bằng tài nguyên thật khi làm giai đoạn 3–4.

## Modal, drawer và popover

```java
navigator.showModal(UserViews.PROFILE);
navigator.showDrawer(UserViews.CONVERSATION_INFO);
navigator.showPopover(DisplayOptionsController.VIEW, anchorButton);
```

- Modal dùng JavaFX `Dialog<ButtonType>`, có owner là cửa sổ đang mở, `WINDOW_MODAL` và nút Đóng/Cancel; trả về `Dialog` để màn hình sau này gắn xử lý kết quả.
- Drawer rộng 320 px ở bên phải, co vùng nội dung còn lại, có nút Đóng và Esc. Drawer không khóa phần còn lại của ứng dụng.
- Popover dùng AtlantaFX, bám vào control đang hiển thị, đóng bằng nút, Esc hoặc bấm ra ngoài. Không cho detach thành cửa sổ khác.
- Đóng overlay trả focus về control mở nó nếu control vẫn nằm trong cửa sổ. Chỉ một overlay được mở cùng lúc; mở overlay mới sẽ đóng overlay cũ.

Các route modal hiện dùng nội dung khung. Profile, tạo nhóm, thông tin hội thoại và thêm user chưa có biểu mẫu nghiệp vụ. Không đánh dấu các tính năng tương ứng hoàn tất chỉ vì đã có route.

## Theme và liên hệ với Figma

`ThemeManager` mặc định Primer Light, đổi sang Primer Dark mà không nạp lại trang. Theme áp dụng cả root của cửa sổ, DialogPane và Popover. Font Inter 4.1 Regular/SemiBold/Bold được nạp từ tài nguyên cục bộ, kèm SIL Open Font License.

CSS chung ở `src/desktop-ui/src/main/resources/com/chatsystem/desktop/css/desktop.css`. Dùng các màu ngữ nghĩa `-cs-canvas`, `-cs-surface`, `-cs-text`, `-cs-muted`, `-cs-border`, `-cs-brand`, … thay vì gán màu rải rác vào từng FXML. Màu xanh nút chính giữ độ tương phản với chữ trắng trong cả hai theme.

File Figma hiện gồm wireframe và các tài liệu/thành phần tham khảo: **wireframe Admin đã hoàn thành, wireframe User chưa hoàn thành**. Đây chưa phải bộ giao diện trực quan cuối cùng đã chốt.

Khung 1.2 tham khảo [các node đang có](figma_links.md): Foundations (`19:71`), User rail (`35:2`), Admin sidebar (`192:1076`). Các lựa chọn hiện tại gồm Inter, body 14 px, spacing theo lưới 4 px, radius 4 px, màu brand sáng `#0F6CBD`, rail 72 px và sidebar 224 px; không coi đây là xác nhận thiết kế cuối cùng của nhóm. Icon dùng **Ikonli Feather** và theme dùng Primer theo tech stack. Khi triển khai màn hình nghiệp vụ, bám wireframe Admin đã hoàn thành và cập nhật phần User theo tiến độ hoàn thiện wireframe.

Nút Hiển thị cung cấp chọn sáng/tối và giảm chuyển động. Tùy chọn giảm chuyển động bỏ hiệu ứng drawer/popover; có thể đặt mặc định bằng JVM property `-Dchatsystem.reducedMotion=true`. Tùy chọn hiện giữ trong lần chạy, chưa lưu vào cấu hình tài khoản.

## Kiểm thử

Từ repository root với JDK 25:

```sh
./mvnw clean verify
./mvnw -Pui-tests verify
```

Lệnh đầu build và biên dịch kiểm thử không cần màn hình; các lớp GUI được đánh dấu skipped. Lệnh thứ hai yêu cầu display, chạy kiểm thử điều hướng/cache, lỗi FXML, reset phiên, owner/modality, Esc/focus, đổi theme trong overlay và load/click route của hai ứng dụng.

Trên Linux đã có Xvfb:

```sh
xvfb-run -a -s "-screen 0 1440x1000x24" ./mvnw -Pui-tests verify
```

Thêm `-Dchatsystem.capture=true` để ghi ảnh render vào `src/user/target/screenshots/` và `src/admin/target/screenshots/`. Profile dùng software renderer và giảm chuyển động để chụp ảnh ổn định, chạy ở nội dung cửa sổ 1280 × 720 và 1024 × 640. Ảnh khung điều hướng đã duyệt được lưu trong [lịch sử Git](https://github.com/Ladenyx/ChatSystem/blob/47e3662b15611c6715c62e79295a83ef4a29d03d/docs/images/navigation-theme/README.md). Mục 1.3 bổ sung [bộ dữ liệu và trang xem mẫu](ui-mock-data.md), kèm kiểm thử riêng. Chưa nghiệm thu Windows/macOS, biểu mẫu nghiệp vụ hoặc fat-JAR.

## Khởi chạy

```sh
./mvnw install
./mvnw -pl src/user javafx:run
./mvnw -pl src/admin javafx:run
```

Mỗi lệnh launch mở một ứng dụng riêng. Chạy `install` sau khi sửa `common`/`desktop-ui` để module ứng dụng dùng đúng phiên bản local. Không chạy `javafx:run` cho toàn bộ reactor bằng `-am`.
