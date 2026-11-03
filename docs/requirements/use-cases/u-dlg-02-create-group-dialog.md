# ĐẶC TẢ USE-CASE

## Thông tin chung (Metadata)

- **Use Case ID**: UC-U-DLG-02
- **Use Case Name**: Tạo nhóm chat mới
- **Author / Create by**: Kiet
- **Date Create**: 30/10/2026
- **Last Updated by** / Date: Kiet / 30/10/2026
- **Primary Actor**: User đã đăng nhập và muốn tạo nhóm.
- **Secondary Actor(s)**: Những người bạn được User chọn làm thành viên nhóm.
- **Priority**: Must Have
- **Status**: Reviewed

## Mô tả tổng quan (Summary / Desciption)

- **Brief Description**: User mở hộp thoại tạo nhóm, nhập tên và chọn tối thiểu một người khác từ danh sách bạn bè đủ điều kiện. Nhóm được tạo mặc định dùng E2EE; người tạo được thêm vào nhóm với vai trò Admin đầu tiên.
- **Goal**: Tạo một nhóm chat gồm User và các thành viên đã chọn, sau đó mở hội thoại nhóm để sử dụng theo trạng thái khóa/quyền hiện tại.
- **Trigger**: User nhấn **Tạo nhóm mới** ở header danh sách hội thoại `U-SCR-07`, hoặc chọn **Tạo nhóm** từ phân mục Đang Online/kết quả tìm người dùng đang là bạn bè trong `U-SCR-08`.

## Điều kiện tiên quyết (Conditions)

- **Pre-conditions**:
  - User đã đăng nhập bằng phiên hợp lệ và đang ở màn hình có chức năng Tạo nhóm.
  - Luồng cơ bản có kết nối tới server, ít nhất một người bạn đủ điều kiện và khóa cần thiết trên thiết bị người tạo đã sẵn sàng.
  - Người được chọn phải đang là bạn bè của người tạo, không có quan hệ chặn hai chiều với người tạo tại thời điểm server xử lý.
  - Không bắt buộc người được chọn đang Online; việc cấp khóa cho thành viên offline được xử lý riêng.
- **Post-conditions**:
  - Khi thành công: nhóm có định danh, tên đã xác nhận, người tạo và các thành viên hợp lệ đã chọn; không tự thêm tài khoản ngoài danh sách đó.
  - Người tạo là Admin đầu tiên; những người được chọn ban đầu là thành viên thường.
  - Nhóm mặc định dùng E2EE; trạng thái khóa/chia sẻ khóa được thể hiện nếu chưa hoàn tất, không gửi tin nhóm dưới dạng bản rõ để thay thế.
  - Danh sách hội thoại cập nhật và mở nhóm trong `U-SCR-07`; thông tin thành viên có thể xem ở `U-SCR-09`.
  - Hủy trước khi gửi yêu cầu hoặc yêu cầu bị từ chối thì không tạo nhóm; giữ dữ liệu nhập còn phù hợp khi lỗi để User sửa.
  - Nếu chưa rõ kết quả do mất xác nhận, đối chiếu với server trước khi gửi lại; không coi nhóm chắc chắn chưa tồn tại và không tạo thêm nhóm trùng cho cùng yêu cầu.

## Luồng sự kiện (Flow of Events)

### Basic Flow

Luồng cơ bản: tạo nhóm từ danh sách hội thoại với tên hợp lệ và ít nhất một người bạn đủ điều kiện.

| Step | Actor Action | System Response |
| --- | --- | --- |
| 1 | Nhấn **Tạo nhóm mới** trong `U-SCR-07`. | Mở `U-DLG-02`, gồm ô tên nhóm, ô tìm bạn bè, danh sách chọn thành viên, vùng hiển thị người đã chọn và các nút **Tạo nhóm / Hủy**. |
| 2 | Nhập tên nhóm. | Giữ tên trong hộp thoại; kiểm tra tên không rỗng hoặc chỉ có khoảng trắng. |
| 3 | Nhập `username` hoặc họ tên của người muốn thêm. | Lọc trong danh sách bạn bè đủ điều kiện của User; không mở rộng sang tất cả tài khoản và không trả người có quan hệ chặn hai chiều với User. |
| 4 | Chọn ít nhất một người bạn. | Cập nhật danh sách người đã chọn, không chọn trùng cùng tài khoản; người tạo được tính riêng và không thay thế yêu cầu chọn ít nhất một người khác. |
| 5 | Kiểm tra tên và danh sách thành viên, nhấn **Tạo nhóm**. | Kiểm tra dữ liệu và khóa cần thiết; gửi yêu cầu tạo, hiển thị đang xử lý và ngăn nhấn tạo lặp. |
| 6 | Chờ kết quả. | Server xác thực phiên, kiểm tra lại quan hệ bạn bè/chặn và tính hợp lệ của toàn bộ lựa chọn; tạo nhóm với người tạo là Admin đầu tiên và các thành viên đã xác nhận. Client thực hiện phần chuẩn bị/chia sẻ khóa theo giao thức E2EE được chốt; server không giữ khóa bí mật có khả năng tự giải mã luồng chat nhóm. |
| 7 | Xem nhóm vừa tạo. | Sau xác nhận tạo thành công, đóng hộp thoại, cập nhật danh sách hội thoại và mở nhóm tại `U-SCR-07` với tên và nhãn E2EE. Nếu chia sẻ khóa còn chờ, hiển thị trạng thái theo Alt 6b; không coi mọi thành viên đã giải mã được tin chỉ từ việc tạo nhóm thành công. |

### Alternative Flows

#### Alt 1a — Tạo nhóm từ một người bạn trong Bạn bè & Kết nối

Tại bước 1:

1. User chọn **Tạo nhóm** với một người bạn trong `U-SCR-08`.
2. Hệ thống mở hộp thoại và chọn sẵn người bạn đó nếu còn đủ điều kiện; User có thể bỏ chọn hoặc chọn thêm người khác.
3. User tiếp tục từ bước 2; vẫn phải chọn ít nhất một người khác trước khi tạo.
4. Nếu người được chọn sẵn không còn là bạn hoặc vừa có quan hệ chặn, cập nhật trạng thái theo Ex 6a, không giữ lựa chọn cũ như vẫn hợp lệ.

#### Alt 3a — Xem danh sách và chọn nhiều người

Tại bước 3–4:

1. User xem danh sách bạn bè đủ điều kiện hoặc thay đổi từ khóa để tìm thêm người.
2. Hệ thống giữ các lựa chọn đã có khi User đổi/xóa từ khóa; không giới hạn thành viên vào riêng những người đang hiện trong kết quả lọc.
3. User có thể bỏ chọn từng người; số lượng được chọn cập nhật ngay. Nếu bỏ hết, nút Tạo nhóm không khả dụng cho đến khi chọn lại ít nhất một người.

#### Alt 3b — Không có bạn bè hoặc không có kết quả phù hợp

Tại bước 3:

1. Nếu không có bạn bè đủ điều kiện, hiển thị danh sách trống và giải thích cần ít nhất một người bạn để tạo nhóm.
2. Nếu chỉ không khớp từ khóa, hiển thị không có kết quả phù hợp, cho đổi/xóa từ khóa và giữ những người đã chọn còn hợp lệ.
3. Tài khoản chưa kết bạn, User đã chặn hoặc đang chặn User không được hiện như lựa chọn hợp lệ, kể cả tìm chính xác username.
4. User có thể hủy để quay về Bạn bè & Kết nối nhằm kết bạn/bỏ chặn khi cần; bỏ chặn không tự khôi phục tình bạn cũ.

#### Alt 4a — Các thành viên được chọn có quan hệ chặn với nhau

Tại bước 4–6:

1. User chọn nhiều người bạn mà trong số đó có quan hệ chặn với nhau, nhưng không ai có quan hệ chặn với người tạo.
2. Theo quy tắc mời nhóm của DEC-03, hệ thống vẫn cho tạo nhóm nếu từng cặp người tạo–người được chọn đều đủ điều kiện.
3. Khi mở nhóm, người có quan hệ chặn liên quan thấy banner: **Trong nhóm có người bạn đã chặn hoặc đã chặn bạn. Các thành viên vẫn có thể xem và gửi tin nhắn trong nhóm.**
4. Banner không yêu cầu xác nhận, không ẩn thanh soạn và không làm tin nhóm báo Không thể gửi chỉ vì quan hệ chặn.

#### Alt 6a — Thành viên được chọn đang Offline

Tại bước 6:

1. Server không từ chối tạo nhóm chỉ vì thành viên đang Offline.
2. Nhóm và tư cách thành viên được ghi nhận theo danh sách đã xác nhận.
3. Việc cung cấp khả năng giải mã cho thiết bị thành viên theo giao thức E2EE; nếu chưa hoàn tất, áp dụng Alt 6b. Không giả định Online đồng nghĩa có khóa hoặc Offline đồng nghĩa không được tham gia.

#### Alt 6b — Nhóm đã tạo nhưng chia sẻ khóa còn chờ

Sau khi server đã xác nhận nhóm tồn tại:

1. Hệ thống mở đúng nhóm đã tạo, hiển thị tiến độ hoặc trạng thái chờ/không hoàn tất chia sẻ khóa tương ứng.
2. Chỉ cho gửi/đọc nội dung theo quyền và khóa đã sẵn sàng; không chuyển sang gửi bản rõ để bỏ qua việc thiếu khóa.
3. Việc hoàn tất/thử lại chia sẻ khóa áp dụng cho nhóm hiện tại, không gửi lại yêu cầu tạo một nhóm khác.
4. Không cam kết thành viên có thể đọc lịch sử khi chưa được cấp khả năng giải mã; giao thức, thời điểm hoàn tất và khôi phục lỗi vẫn theo DEC-05 chưa chốt.

#### Alt 5a — Hủy trước khi gửi yêu cầu tạo

Trước bước 5:

1. User nhấn **Hủy** hoặc đóng hộp thoại.
2. Hệ thống đóng hộp thoại và quay về màn hình đã mở nó, không gửi yêu cầu tạo nhóm hoặc thay đổi quan hệ bạn bè.
3. Không ảnh hưởng hội thoại và bản nháp chat đang có ở màn hình trước.
4. Nếu yêu cầu tạo đã được gửi, việc đóng hộp thoại không đồng nghĩa server đã hủy tạo; kết quả phải được đối chiếu theo Ex 6b khi chưa rõ.

### Exception Flows

- **Ex 1a — Không tải được danh sách bạn bè**: Thông báo lỗi và cho tải lại; không coi lỗi tải là không có bạn bè. Giữ tên nhóm và các lựa chọn còn có thể đối chiếu.
- **Ex 2a — Tên nhóm không hợp lệ**: Nếu tên rỗng/chỉ có khoảng trắng, hiển thị lỗi tại ô tên và không gửi yêu cầu. Giới hạn 75 kí tự.
- **Ex 4a — Chưa chọn người khác**: Không cho tạo nhóm chỉ có người tạo. Khi chưa chọn ai hoặc tất cả lựa chọn không còn hợp lệ, yêu cầu chọn ít nhất một người bạn đủ điều kiện.
- **Ex 5a — Khóa người tạo chưa sẵn sàng**: Trước khi gửi yêu cầu tạo, nếu thiếu khóa cần thiết, thông báo và hướng tới thiết lập/khôi phục `U-SCR-06`. Giữ dữ liệu phù hợp để User tiếp tục sau đó, không tự gửi lại khi chưa xác nhận và không tạo nhóm không có E2EE để thay thế.
- **Ex 6a — Quan hệ hoặc tài khoản thay đổi**: Server từ chối yêu cầu nếu một người được chọn không còn là bạn, có chặn hai chiều với người tạo hoặc không còn đủ điều kiện tham gia. Chỉ ra lựa chọn cần cập nhật trong phạm vi được phép, giữ tên và các lựa chọn còn hợp lệ; User sửa rồi xác nhận lại. Không tự bỏ người không hợp lệ và tạo nhóm khác danh sách User đã duyệt.
- **Ex 6b — Mất kết nối hoặc chưa nhận xác nhận tạo**: Hiển thị chưa rõ kết quả, giữ dữ liệu và đối chiếu với server trước khi gửi lại. Nếu đã tạo, dùng đúng nhóm đó; nếu xác định chưa tạo thì mới cho thử lại. Cùng một yêu cầu tạo phải xử lý lặp an toàn, không tạo nhiều nhóm do nhấn lặp/reconnect.
- **Ex 6c — Server từ chối hoặc lỗi tạo nhóm**: Thông báo thất bại và cho sửa/thử lại khi phù hợp; không báo tạo thành công hoặc mở nhóm chưa được xác nhận. Việc ghi nhận nhóm, người tạo có quyền Admin và danh sách thành viên phải nhất quán, không để nhóm bị tạo thiếu Admin hoặc thiếu người đã xác nhận mà vẫn báo thành công.
- **Ex 6d — Lỗi E2EE sau khi nhóm đã tạo**: Hiển thị rõ nhóm đã tồn tại nhưng phần khóa chưa hoàn tất, xử lý theo Alt 6b; không báo nhóm chưa tồn tại hoặc tạo lại nhóm để thử lại phần khóa. Không đưa khóa bí mật/bản rõ tin nhóm vào thông báo lỗi.
- **Ex 1b — Phiên không còn hợp lệ**: Ở bất kỳ bước nào, nếu phiên bị thu hồi/tài khoản bị khóa, dừng thao tác cần xác thực, thông báo và chuyển về đăng nhập; không tiếp tục tạo nhóm bằng phiên cũ.

## Business Rules & Non-Functional Requirements

### Business Rules (BR)

#### BR-01 — Thành phần hộp thoại

- Có ô tên nhóm, tìm bạn bè, danh sách chọn và vùng hiển thị những người đã chọn.
- Có nút **Tạo nhóm** và **Hủy**; thể hiện trạng thái đang xử lý khi gửi yêu cầu.
- Mở từ một người bạn có thể chọn sẵn người đó, nhưng vẫn phải kiểm tra điều kiện mới nhất.

#### BR-02 — Tên nhóm

- Bắt buộc có tên, không rỗng hoặc chỉ gồm khoảng trắng.
- Giới hạn 75 kí tự.

#### BR-03 — Số lượng và vai trò

- Cần ít nhất một tài khoản khác ngoài người tạo, tức ít nhất hai thành viên tính cả người tạo.
- Một tài khoản chỉ xuất hiện một lần trong lựa chọn; không tự chọn mình để đạt số lượng tối thiểu.
- Người tạo là Admin đầu tiên; những người được chọn ban đầu là thành viên thường.
- Giới hạn 50 thành viên/nhóm, tính cả người tạo.

#### BR-04 — Phạm vi tìm và chọn

- Chỉ tìm/chọn trong danh sách bạn bè đủ điều kiện, theo username hoặc họ tên.
- Loại cả tài khoản người tạo đang chặn và tài khoản đang chặn người tạo.
- Việc đổi bộ lọc không tự bỏ các lựa chọn hợp lệ đã có.
- Tìm trong hộp thoại không thay thế chức năng tìm tất cả người dùng/kết bạn của `U-SCR-08`.

#### BR-05 — Kiểm tra bạn bè và chặn

- Server kiểm tra lại từng cặp người tạo–người được chọn tại thời điểm xử lý.
- Phải là bạn bè và không có quan hệ chặn hai chiều giữa cặp đó.
- Chặn giữa những người được chọn với nhau không tự ngăn tạo nhóm nếu quan hệ với người tạo vẫn hợp lệ, theo đề xuất DEC-03.
- Không tự tạo nhóm với danh sách đã bị bớt/thêm người ngoài lựa chọn User xác nhận.

#### BR-06 — Online/Offline

- Thành viên được chọn không bắt buộc Online và không cần thao tác trực tuyến để hoàn tất yêu cầu tạo nhóm của người tạo.
- Tư cách thành viên và khả năng đọc tin E2EE là hai trạng thái cần phân biệt.
- Chia sẻ khóa chưa xong phải có trạng thái riêng, không coi Offline là lỗi quan hệ hoặc tự loại thành viên.

#### BR-07 — E2EE mặc định

- Nhóm tạo mới mặc định bật E2EE; không cung cấp phương án gửi bản rõ thay thế khi thiếu khóa.
- Client thực hiện phần mật mã theo giao thức được chốt; server lưu/chuyển bản mã và không tự giải mã luồng chat nhóm.
- Không coi đăng nhập hoặc tạo nhóm thành công là mọi thiết bị đã có khóa.
- Giao thức/thư viện và cách chia sẻ/khôi phục khóa vẫn theo DEC-05 đang đề xuất.

#### BR-08 — Kết quả tạo và thử lại

- Nhóm, Admin đầu tiên và danh sách thành viên phải được ghi nhận nhất quán trước khi báo tạo thành công.
- Khi chưa rõ kết quả, kiểm tra lại yêu cầu ban đầu trước khi thử lại để tránh tạo nhóm trùng.
- Lỗi chia sẻ khóa sau khi tạo phải xử lý trên nhóm đã tồn tại, không tạo nhóm mới cho cùng yêu cầu.
- Chống trùng yêu cầu không đồng nghĩa cấm User chủ động tạo những nhóm khác có cùng tên/thành viên sau đó.

#### BR-09 — Hủy và điều hướng

- Hủy trước khi gửi yêu cầu không tạo nhóm và quay về màn hình trước.
- Tạo thành công mở đúng nhóm tại `U-SCR-07`, cập nhật tên, thành viên và chỉ báo E2EE.
- Đóng hộp thoại sau khi yêu cầu đã gửi không tự thu hồi kết quả tạo ở server.

### Non-Functional Requirements (NFR)

#### NFR-01 — Phản hồi giao diện

- Tải/tìm bạn bè, tạo nhóm và chuẩn bị khóa không làm đóng băng JavaFX.
- Phân biệt đang tải, danh sách trống, không khớp từ khóa, đang tạo và lỗi.
- Kiểm tra dữ liệu tại vị trí nhập; chưa đủ tên/thành viên thì không cho gửi yêu cầu.

#### NFR-02 — Quyền truy cập và bảo mật

- Kiểm tra phiên và điều kiện thành viên tại server, không chỉ dựa vào nút hoặc danh sách đã tải ở client.
- Không trả người không đủ điều kiện như kết quả chọn hợp lệ trong hộp thoại.
- Không gửi khóa bí mật có khả năng giải mã cho server dưới dạng bản rõ hoặc hiển thị trong thông báo/log lỗi.

#### NFR-03 — Nhất quán và chống thao tác lặp

- Ngăn gửi lặp khi một yêu cầu tạo đang xử lý.
- Server xử lý lặp an toàn cho cùng yêu cầu; reconnect không làm nhân đôi nhóm, Admin hoặc thành viên.
- Sau xác nhận thành công, danh sách hội thoại và panel thông tin phản ánh đúng nhóm đã tạo.

#### NFR-04 — Bảo toàn dữ liệu nhập

- Lỗi tải/kiểm tra/tạo giữ tên và các lựa chọn còn hợp lệ để User sửa hoặc thử lại.
- Không làm mất bản nháp của hội thoại đã mở trước đó khi mở/hủy hộp thoại.
- Nếu chuyển sang thiết lập/khôi phục khóa, khi quay lại phải kiểm tra lại điều kiện lựa chọn; không tự gửi yêu cầu tạo chưa được User xác nhận lại.

#### NFR-05 — Thời gian và tiến độ

- Hiển thị trạng thái chờ hoặc tiến độ khi tạo/chia sẻ khóa, không trình bày như mọi thành viên đã đọc được tin khi còn xử lý.
- Chưa có ngưỡng độ trễ riêng cho tạo nhóm được chốt; không tự áp dụng ngưỡng tải lịch sử/tìm tin cho toàn bộ quy trình này.
