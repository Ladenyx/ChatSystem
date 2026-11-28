# Bạn bè, chặn và thêm nhóm

**DEC-03 — Đề xuất:** chưa có kết luận chung cho toàn mục.

- Người chưa kết bạn được tìm nhau và chat riêng. Mời nhóm cần là bạn bè giữa người mời và người được mời.
- Chặn hủy tình bạn/lời mời đang chờ, cấm lời mời mới và tin riêng mới hai chiều; giữ lịch sử cũ. Người bị chặn không thấy người chặn trong tìm kiếm.
- Tin server đã chấp nhận trước khi chặn vẫn được đồng bộ; tin gửi sau khi chặn bị từ chối, không giữ để tự gửi khi bỏ chặn.
- Bỏ chặn không phục hồi tình bạn/lời mời; chat riêng hoạt động lại khi không còn bên nào chặn. Hủy kết bạn không tự rời nhóm.
- Mời nhóm chỉ kiểm tra bạn bè/chặn hai chiều của cặp người mời–người được mời. Quan hệ chặn với thành viên khác không ngăn lời mời; cùng nhóm vẫn thấy tin nhau.

**Ví dụ:** A có thể mời D dù C trong nhóm chặn D; nếu D chặn A thì A không mời được, nhưng người bạn khác đủ quyền vẫn có thể mời D.

## Xử lý đồng thời (Concurrency — đã chốt theo Issue #82)

- Do `blocks`, `friend_requests` và `friendships` nằm trên 3 bảng độc lập dưới mức cô lập mặc định `READ COMMITTED` của PostgreSQL, mọi transaction thay đổi quan hệ giữa cặp người dùng `(u1, u2)` (`sendFriendRequest`, `acceptFriendRequest`, `rejectFriendRequest`, `cancelFriendRequest`, `unfriend`, `blockUser`, `unblockUser`) bắt buộc khóa tuần tự theo cặp tài khoản chuẩn hóa ngay đầu transaction trước khi kiểm tra điều kiện và ghi dữ liệu:
  ```sql
  SELECT pg_advisory_xact_lock(
      hashtextextended('rel:' || LEAST($1, $2)::text || ':' || GREATEST($1, $2)::text, 0)
  );
  ```

**Còn mở:** xác nhận quy tắc/ma trận quyền còn lại và xử lý chặn/thay đổi thành viên đồng thời với gửi tin. Cơ chế tuần tự hóa quan hệ giữa cặp người dùng đã chốt bằng `pg_advisory_xact_lock` theo cặp chuẩn hóa.
