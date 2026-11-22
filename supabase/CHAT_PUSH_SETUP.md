# Tự gửi thông báo khi có tin nhắn mới

Implementation: `functions/chat-message-push/index.ts`, `handler.ts`; policy và delivery ledger trong migration 10. Android hiện có đăng ký token và renderer foreground. Không dùng `send-push-test` làm webhook.

## 1. Chạy migration

Trong SQL Editor của database Dịu đã có migration 01–09, chạy toàn bộ:
[migration 10](migrations/20261122000010_chat_push_delivery.sql).

Migration tạo `chat_push_deliveries` và ba RPC chỉ `service_role` được gọi:
- `chat_push_targets`: đọc lại message và dựng preview theo từng người nhận.
- `claim_chat_push`: claim nguyên tử theo message/device, lease 2 phút.
- `finish_chat_push`: ghi accepted/failed nếu lease vẫn đúng.

Không chạy `tests/chat_push_policy.sql` trên Supabase thật; file đó dựng schema fixture.

## 2. Secrets của Edge Function

Trong Supabase → Edge Functions → Secrets:
- `FIREBASE_SERVICE_ACCOUNT`: giữ JSON service account đã dùng thành công ở function test, cùng Firebase project của Android.
- `CHAT_PUSH_WEBHOOK_SECRET`: secret ngẫu nhiên mạnh, tối thiểu 32 ký tự; dùng trình quản lý mật khẩu tạo. Đây là secret mới, không phải anon key, không đưa vào app/repository.
- Hosted Supabase cung cấp `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`. Nếu thiếu legacy service role environment trong project, cấu hình backend credential tương ứng trước khi chạy; không dùng anon key thay thế.

## 3. Deploy function

Tên chính xác: `chat-message-push`.

**Dashboard:** tạo function mới qua editor, đưa cả hai file `index.ts` và `handler.ts` vào cùng thư mục; giữ import `./handler.ts`. Deploy, tắt `Verify JWT with legacy secret` vì handler tự kiểm header `x-chat-push-secret` trước khi truy cập DB.

**Hoặc CLI**, chạy từ thư mục gốc dự án sau khi đăng nhập:
```powershell
npx.cmd supabase login
npx.cmd supabase functions deploy chat-message-push --project-ref YOUR_PROJECT_REF --no-verify-jwt
```
`YOUR_PROJECT_REF` là mã project trong URL dashboard. Không chạy `db push` để bootstrap schema lịch sử: docs/migrations đang bị ignore và không phải migration chain CLI hoàn chỉnh.

## 4. Test function trước khi gắn trigger

Dùng một `messages.id` mới, có người nhận khác đã đăng ký Android token. Người nhận không mute/block/quiet.

POST tới function bằng công cụ Test:
- Header `Content-Type: application/json`.
- Header `x-chat-push-secret`: giá trị secret mới.
- Body:
```json
{
  "type": "INSERT",
  "schema": "public",
  "table": "messages",
  "record": { "id": "REPLACE_WITH_REAL_MESSAGE_UUID" }
}
```
Chỉ ID được dùng; nội dung/tên/recipient trong request đều không phải nguồn quyết định. Trả về số `accepted/skipped/failed`, không trả token hay nội dung riêng tư. `accepted` là FCM đã nhận yêu cầu, không xác nhận điện thoại hiển thị.

Test lại cùng message: delivery đã accepted sẽ bị skip. Dùng tin mới để thử lại hiển thị.

## 5. Tạo Vault secrets và webhook

Trong Supabase → Vault (tìm Vault trong dashboard nếu menu đổi), tạo hai secret:
- `chat_push_function_url` = `https://YOUR_PROJECT_REF.supabase.co/functions/v1/chat-message-push`
- `chat_push_webhook_secret` = **cùng giá trị** Edge secret `CHAT_PUSH_WEBHOOK_SECRET`

Sau đó chạy toàn bộ [setup_chat_push_webhook.sql](setup_chat_push_webhook.sql) trong SQL Editor. Cần Vault và quyền quản trị extension pg_net.

Script cài trigger `tr_chat_message_push` AFTER INSERT trên `public.messages`, dùng pg_net gửi HTTP bất đồng bộ sau commit. Chỉ gửi envelope và message ID; secret đọc từ Vault khi chạy, không nhúng vào định nghĩa trigger. Không tạo thêm Dashboard Database Webhook cho cùng sự kiện.

Gửi tin mới từ A tới B trong app. Kiểm tra function invocations, notification trên B và delivery ledger:
```sql
select message_id, device_id, status, attempts, updated_at
from public.chat_push_deliveries
order by updated_at desc limit 20;
```

Dừng webhook khi cần:
```sql
drop trigger if exists tr_chat_message_push on public.messages;
```
Xoay secret: tạm dừng trigger, cập nhật cùng giá trị trong Vault và Edge Secrets rồi chạy lại setup. Không gửi giá trị secret trong ảnh/log.

## Hành vi và giới hạn

- Chỉ gửi tới Android của người nhận hiện còn là thành viên, đã tham gia trước thời điểm tin được tạo; loại người gửi, mute, block hai chiều, tin đã xóa và sender không còn membership.
- Chỉ text/photo/voice/location. Sealed letters và system events không gửi trong pipeline này để tránh lộ thư trước giờ mở hoặc báo lại sự kiện cũ.
- `color`/thiếu settings: title Dịu, body chung, chỉ mood + routing IDs trong data. `name`: thêm display_name từ server, không có content. `full`: preview text tối đa 160 ký tự; media chỉ nhãn, không có URL/file.
- Quiet dùng helper SQL hiện có `is_in_quiet_hours`: còn giới hạn timezone Việt Nam/cách tính ngày qua đêm của schema cũ; chưa sửa trong tác vụ này.
- Nhóm có `group_digest = true` (mặc định true nếu thiếu settings) **không nhận push tức thời**; scheduler digest chưa triển khai. Muốn thử push tức thời cho nhóm, tắt tùy chọn digest của tài khoản nhận.
- Dùng channel Android `chat_messages` hiện có; chưa ánh xạ các lựa chọn âm/rung riêng của Dịu sang channel. OS/user điều khiển âm thực tế.
- Data có conversation_id/message_id để chuẩn bị routing; chưa thay đổi Android để bấm mở đúng chat. Foreground renderer hiện dùng ID 1001, nên thông báo mới thay cái trước; không có suppression theo chat đang mở.
- Request lỗi trả 503, không log payload/token/private key. Chỉ xóa endpoint khi FCM trả UNREGISTERED; xóa có điều kiện id + token để không xóa token vừa rotate.
- Claim chặn replay/concurrent send đã accepted, cho retry failed/lease hết hạn tối đa 5 lần. **Không có worker tự retry**, pg_net/webhook không được coi là queue bảo đảm giao; cần vận hành replay message ID hoặc bổ sung durable outbox sau này.
- Không bảo đảm exactly-once: crash sau FCM accepted trước ghi receipt có thể gửi lại khi lease hết hạn. Tag message ID hạn chế notification trùng ở nền, không thay delivery guarantee.
- Recheck policy/token sau claim giảm race, không tạo transaction nguyên tử với FCM; quyền có thể thay đổi sau snapshot. Tin recall sau khi push không thể thu hồi nội dung đã lên lock screen.
- Ledger cascade khi xóa message/device; chưa có TTL cleanup cho lịch sử lâu ngày.

## Kiểm chứng

- `npx.cmd --yes deno check --no-lock supabase/functions/chat-message-push/index.ts`
- `npx.cmd --yes deno test --no-lock supabase/functions/chat-message-push/handler_test.ts`
- `psql ... -v ON_ERROR_STOP=1 -f supabase/tests/chat_push_policy.sql` trong database trống dùng riêng.

Handler có 9 test auth/envelope/replay/concurrent/eligibility change/failure/retry/expiry cleanup/receipt failure/sanitized errors. SQL fixture kiểm privacy, membership, mute, block, quiet, digest, media, grants và lease.

Chưa deploy Supabase thật, chưa test pg_net/Vault và FCM thật của pipeline mới. Không cần rebuild Android để deploy backend này.

Kỹ thuật học: T16 (service-only RPC), T19 (privacy trước payload + webhook), T10 (lease/idempotency) trong bản đồ local `docs/plans/DIU_TECHNIQUES_LEARNING_MAP.md`. docs bị Git ignore nên bản đồ không vào PR.

Nguồn: [Database Webhooks](https://supabase.com/docs/guides/database/webhooks), [FCM từ Edge Functions](https://supabase.com/docs/guides/functions/examples/push-notifications), [FCM HTTP v1](https://firebase.google.com/docs/cloud-messaging/send/v1-api).
