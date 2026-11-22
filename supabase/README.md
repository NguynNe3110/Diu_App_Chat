# Đăng ký thiết bị nhận push của Dịu

## Schema đã đối chiếu

- `docs/migrations/00001_initial_schema.sql`: đã có `push_tokens`, FK `user_id → user_profiles.id`, `uq_push_token(user_id, token)`, trigger `tr_push_tokens_updated`, policy chủ sở hữu.
- `docs/migrations/00004_db05_profiles_and_settings_separation.sql`: cấu hình riêng tư nằm ở `user_settings` (`notif_display`, `notif_sound`, ...).
- `docs/migrations/00007_grant_authenticated_table_privileges.sql`: chưa cấp CRUD cho `push_tokens`.
- Không tạo bảng `user_devices` thứ hai. Tên endpoint phía Android/backend tiếp tục là `push_tokens`.
- Các file lịch sử trong `docs/` đang bị Git ignore. Migration ở đây là phần bổ sung cho database đã có schema Dịu, **không phải bộ bootstrap độc lập cho một Supabase project trống**.

## Áp dụng trên Supabase

1. Đảm bảo database đã có `public.user_profiles`, `public.set_updated_at()`, Supabase Auth và các migration lịch sử phù hợp.
2. Mở SQL Editor của đúng project, tạo query mới.
3. Copy **toàn bộ** [migration](migrations/20261122000009_push_tokens_registration.sql), bao gồm BEGIN/COMMIT, rồi Run.
4. Nếu dữ liệu cũ có token rỗng hoặc cùng platform/token thuộc nhiều tài khoản, migration báo lỗi và rollback. Đối chiếu quyền sở hữu rồi xử lý thủ công; migration không tự xóa token. Không đưa giá trị token thật vào báo cáo.
5. Kiểm tra Table Editor: `push_tokens` có thêm `installation_id`; RLS bật. Bảng trống sau migration là bình thường vì Android chưa đồng bộ token.

Có thể kiểm tra cấu trúc bằng query không chứa token:

```sql
select column_name, data_type, is_nullable
from information_schema.columns
where table_schema = 'public' and table_name = 'push_tokens';

select policyname, permissive, roles, cmd, qual, with_check
from pg_policies
where schemaname = 'public' and tablename = 'push_tokens';
```

## Hợp đồng Android cần triển khai tiếp

- Sau đăng nhập và bootstrap profile, tạo/lấy UUID ngẫu nhiên bền theo bản cài app; không dùng Android ID hoặc mã phần cứng.
- Đọc row của chính mình theo `installation_id`. Nếu đã có, UPDATE `token/platform`; nếu chưa có, INSERT `token/platform/installation_id`. `user_id` mặc định từ JWT (`auth.uid()`), hoặc truyền đúng UID hiện tại.
- Nếu hai lần đăng ký đồng thời gây unique conflict, đọc lại và cập nhật. Không dùng upsert cập nhật mọi cột: quyền UPDATE chỉ cấp cho `token/platform/installation_id`, không cho sửa `user_id/id/timestamps`. Unique index installation là partial; PostgREST on_conflict thông thường không tự suy luận predicate này.
- Một tài khoản có nhiều bản cài app. Một (platform, token) chỉ có một chủ sở hữu. Row cũ giữ `installation_id = NULL`; cần kế hoạch đối chiếu khi client mới tiếp quản row cũ.
- Token thay đổi phải cập nhật cùng row; `updated_at` do trigger server ghi. Cột này chỉ phản ánh lần đăng ký/cập nhật, không phải trạng thái online.
- Trước logout, DELETE row theo installation khi session cũ còn hiệu lực. Nếu detach thất bại, không coi token đã tách; đổi tài khoản phải có luồng thu hồi/đổi token và retry được thiết kế riêng. Không bỏ unique constraint hoặc nới RLS để giải quyết lỗi chuyển tài khoản.
- Backend dùng `service_role` trong Edge Function để đọc endpoint, xóa endpoint hết hạn. Không đặt service key vào Android.
- Android registration/logout/token refresh đã được thêm trong app. Backend chat push có source và hướng dẫn tại [CHAT_PUSH_SETUP.md](CHAT_PUSH_SETUP.md); phải deploy function và cài webhook riêng sau migration 10. Migration 09 một mình **chưa tự tạo thông báo khi có tin mới**. Worker tự retry và notification routing vẫn chưa có.
- Backend tương lai phải đọc privacy/quiet/block/membership trước khi gửi, không lấy body/tên từ client làm nguồn tin cậy.

## Kiểm chứng

Đã chạy [push_tokens_rls.sql](tests/push_tokens_rls.sql) bằng PostgreSQL 18 cục bộ, với fixture tối thiểu mô phỏng schema cũ và `auth.uid()`. Kiểm: giữ row cũ, chạy lại migration, anon bị từ chối, phân tách A/B dù tồn tại policy SELECT quá rộng, giả UID, chặn sửa chủ sở hữu, token rỗng/platform sai, trùng installation/endpoint, nhiều máy, đổi token, logout delete, service_role đọc, cascade xóa profile. Kết quả: pass.

Test tạo roles/schema fixture nên **chỉ chạy trên database trống dùng riêng cho test**, không chạy trong SQL Editor production:

```powershell
$env:PGCLIENTENCODING = "UTF8"
psql -h 127.0.0.1 -U postgres -d diu_push_test -v ON_ERROR_STOP=1 -f supabase/tests/push_tokens_rls.sql
```

Chưa chạy trên Supabase thật, chưa kiểm qua JWT/PostgREST và chưa kiểm push end-to-end. Test không mô phỏng toàn bộ các migration lịch sử.

Kỹ thuật liên quan: bản đồ local `docs/plans/DIU_TECHNIQUES_LEARNING_MAP.md`, mục T16 (RLS + column grants) và T19 (đăng ký endpoint trước pipeline push). Xem [RLS chính thức](https://supabase.com/docs/guides/database/postgres/row-level-security) để phân biệt USING (row hiện có) và WITH CHECK (row mới).
