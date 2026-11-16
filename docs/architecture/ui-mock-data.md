# Dữ liệu giả lập cho UI — Version 1, mục 1.3

`MockData.create()` cung cấp một snapshot hư cấu, ổn định cho cả User Client và Admin App. Mã nằm trong package `com.chatsystem.desktop.mock` của module `desktop-ui`; `common` và `server` không phụ thuộc bộ mẫu này. Các record là dữ liệu hiển thị cục bộ, chưa phải DTO giao thức hay entity CSDL.

## Nội dung bộ mẫu

| Record / API | Nội dung | Màn hình / chức năng sẽ sử dụng |
| --- | --- | --- |
| `MockUser`, `users()`, `user(id)` | 20 hồ sơ, tên tiếng Việt, username, email `@example.test`, thông tin tùy chọn; `ACTIVE`/`LOCKED`/`DELETED`, online/offline và vai trò hệ thống | `U-SCR-08`, `U-DLG-01`, `A-SCR-03`, `A-01`–`A-06`, `A-12` |
| `MockConversation`, `conversations()` | 5 hội thoại 1–1, 4 hội thoại nhóm, thành viên và số chưa đọc của người xem mẫu | `U-SCR-07`, `U-12`–`U-19`, `U-24` |
| `MockMessage`, `messages()`, `messagesFor(id)` | 126 tin; một nhóm có 56 tin để thử ranh giới phân trang 50; chờ/thất bại/đã lưu/đã nhận/đã đọc/thu hồi, văn bản dài và nhiều dòng | `U-SCR-07`, `U-SCR-09`, `U-12`–`U-18`, `U-24`, `U-25` |
| `MockGroup`, `groups()` | 4 nhóm, 4–8 thành viên/nhóm, mỗi nhóm có 2 admin; ID nhóm trùng ID hội thoại nhóm | `U-SCR-09`, `U-DLG-02`, `A-SCR-04`, `A-08`, `A-09` |
| `MockSpamReport`, `spamReports()` | 6 báo cáo, chờ xử lý/đã xem xét, lý do, người gửi/đích và snapshot bằng chứng có nhãn `Selected`/`Context` | `U-DLG-04`, `A-SCR-05`, `U-26`, `A-10`, `A-11` |
| `MockMonthlyStatistics`, `monthlyStatistics(year)` | Đủ tháng 1–12; số đăng ký và người mở ứng dụng duy nhất; có dữ liệu năm 2025/2026 | `A-SCR-02`, `A-SCR-07`, `A-SCR-08`, `A-13`, `A-16` |
| `MockLoginLog`, `loginLogs()` | 37 lượt đăng nhập mẫu gần đây, mới nhất trước; phân biệt User Client/Admin App | `A-SCR-03`, `A-SCR-06`, `A-05`, `A-07` |
| `MockActivity`, `activities()` | 20 dòng theo tài khoản: lượt mở app, số người và số nhóm đã gửi tin thành công | `A-SCR-08`, `A-15` |

Người xem mẫu là `usr-01` / `minhan` (`previewUser()`), không phải phiên đã xác thực. Vai trò Admin hệ thống (`MockUser.Role`) và Admin nhóm (`MockGroup.Role`) là hai khái niệm riêng.

Mốc cố định là **15/11/2026 10:00, Asia/Ho_Chi_Minh** (`SNAPSHOT_AT = 2026-11-15T03:00:00Z`). Thời điểm dùng `Instant` UTC; ngày sinh dùng `LocalDate`, tháng dùng `YearMonth`. Không dùng đồng hồ máy, số ngẫu nhiên, file ngoài hoặc kết nối mạng; dữ liệu không thay đổi khi chạy ở ngày/múi giờ khác.

## Gắn vào TableView và ListView

Các danh sách trong snapshot và các danh sách lồng nhau đều bất biến. Tạo **ObservableList riêng cho từng màn hình** để có thể sắp xếp, lọc, xóa dòng demo mà không ảnh hưởng dữ liệu gốc hoặc màn hình khác:

```java
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockUser;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

MockData data = MockData.create();
TableView<MockUser> users = new TableView<>(FXCollections.observableArrayList(data.users()));
TableColumn<MockUser, String> name = new TableColumn<>("Họ tên");
name.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().fullName()));
users.getColumns().add(name);

ListView<MockMessage> messages = new ListView<>(
        FXCollections.observableArrayList(data.messagesFor("grp-01")));
// Gắn cellFactory để hiển thị senderId, sentAt, text và state theo bố cục màn hình.
```

Record dùng accessor `fullName()`, không phải getter JavaBean `getFullName()`. Dùng lambda cho `cellValueFactory`; không dùng `PropertyValueFactory("fullName")`. Khi mô phỏng sửa hồ sơ, tạo record mới rồi `set(...)`/`setAll(...)` trên ObservableList; khi reset phiên, bỏ danh sách UI cũ hoặc tạo snapshot mới. Chỉ thao tác control/danh sách đang gắn control trên JavaFX Application Thread.

`messagesFor(id)` trả tin theo thời gian tăng dần; ID hội thoại không có dữ liệu trả danh sách rỗng. `user(id)` ném `IllegalArgumentException` nếu ID không tồn tại để phát hiện liên kết sai trong fixture.

## Gắn vào BarChart 12 tháng

```java
import com.chatsystem.desktop.mock.MockChartSeries;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;

BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
chart.setAnimated(false);
chart.getData().add(MockChartSeries.registrations(data, 2026));
// Biểu đồ khác hoặc khi đổi chỉ số:
chart.getData().setAll(java.util.List.of(MockChartSeries.activeUsers(data, 2026)));
```

Mỗi lần gọi adapter tạo series và các `XYChart.Data` mới; không chia sẻ cùng series giữa hai chart. Dữ liệu giữ thứ tự `T1` → `T12`, bao gồm giá trị `0`. Năm không có dữ liệu vẫn trả đủ 12 tháng bằng 0. Chưa có dữ liệu sau mốc snapshot, nên tháng 12/2026 bằng 0.

Số đăng ký lấy từ cùng 20 hồ sơ, gồm tài khoản hiện đã xóa mềm trong tháng đăng ký ban đầu. Người hoạt động tháng là **số tài khoản duy nhất có sự kiện mở User Client**; lượt mở thứ hai không tăng số người. `activities()` dùng khoảng cố định `activityFrom()` → `snapshotDate()` (01/01/2026 → 15/11/2026), đếm người/nhóm là đích gửi thành công khác nhau, gồm sự kiện của tin đã thu hồi. Đây là tổng hợp fixture để dựng UI theo [thống kê và thời gian](../decisions/statistics-and-time.md); bộ lọc thời gian tùy ý và dịch vụ thống kê thật thuộc giai đoạn tiếp theo.

## Bằng chứng spam và E2EE

Report giữ snapshot bằng chứng riêng: hợp theo ID của **Selected ∪ tối đa 30 Context**, sắp xếp thời gian tăng dần, giữ cả hai nhãn khi trùng. Báo cáo nhóm có một tin Selected cũ ngoài 30 Context (31 tin tổng cộng); báo cáo ngắn có Selected nằm trong Context và không bị lặp. Admin chỉ hiển thị `report.evidence()`, không lấy `messagesFor(...)` để tự bổ sung lịch sử. Xem [DEC-07](../decisions/account-lifecycle.md).

Văn bản nhóm mô phỏng nội dung đã giải mã **tại client**. Nhãn E2EE không thực hiện mã hóa; bằng chứng báo cáo là bản rõ hư cấu do người báo cáo cung cấp. Trang xem fixture ngoại tuyến của mục 1.3 hiển thị cả dữ liệu User và Admin để kiểm tra UI; trang này không đại diện cho quyền truy cập trong phiên thật và cần được gỡ/giới hạn khi tích hợp đăng nhập. Bộ mẫu không chứa mật khẩu, token, khóa mật mã hay cấu hình dịch vụ.

## Chạy và kiểm tra

```sh
./mvnw install
./mvnw -pl src/user javafx:run
./mvnw -pl src/admin javafx:run
```

Trong mỗi ứng dụng, bấm nút **hình cơ sở dữ liệu** có tooltip **Xem dữ liệu mẫu** trên toolbar. Trang có 5 tab: Người dùng, Hội thoại, Nhóm, Báo cáo spam và 12 tháng. Chọn hội thoại/báo cáo để đổi chi tiết; chọn năm/chỉ số để đổi chart; bảng dưới chart cung cấp giá trị chính xác khi dùng bàn phím. Nút Quay lại đưa về trang trước; theme áp dụng như các trang khác. Đây là trang kiểm tra bộ mẫu; các màn hình nghiệp vụ vẫn triển khai ở giai đoạn 3–4, theo tiến độ wireframe của nhóm.

```sh
./mvnw clean verify
./mvnw -Pui-tests -Dchatsystem.capture=true verify
```

Build mặc định chạy 7 test không cần display về tham chiếu/ID, tính bất biến, mốc thời gian, bằng chứng spam, tổng 12 tháng tính tay và series độc lập; 11 test GUI được skip. Profile `ui-tests` chạy đủ **18 test** (7 dữ liệu + 11 GUI), bao gồm trang mẫu trong hai ứng dụng, binding record, thay lựa chọn, đổi theme và cửa sổ 1024 × 640/1280 × 720. Đã kiểm tra trên Linux với Temurin JDK 25 + Xvfb; chưa nghiệm thu Windows/macOS. Xem [ảnh chạy thực tế trong lịch sử Git](https://github.com/Ladenyx/ChatSystem/blob/47e3662b15611c6715c62e79295a83ef4a29d03d/docs/images/ui-mock-data/README.md).
