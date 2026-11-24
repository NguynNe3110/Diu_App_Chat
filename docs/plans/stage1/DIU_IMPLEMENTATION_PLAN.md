# Kế hoạch xây dựng toàn bộ ứng dụng Dịu

Ngày lập: 21/11/2026. Trạng thái: kế hoạch đề xuất dựa trên tài liệu và code trong workspace; chưa phải xác nhận tính năng đã hoàn thành.

Tài liệu đi cùng: [Bản đồ kỹ thuật và lộ trình học](DIU_TECHNIQUES_LEARNING_MAP.md).

## 1. Mục tiêu và cách dùng kế hoạch

Dịu là ứng dụng nhắn tin cho một vòng người thân, đặt cảm xúc và sự ít gián đoạn ở trung tâm. Vòng lặp chính cần chạy tốt trước: đăng nhập → kết nối người thân → gửi/nhận lời nhắn mang màu cảm xúc → đọc lại khi mất mạng → nhận thông báo đúng mức riêng tư.

Kế hoạch bao phủ cả Android, PostgreSQL/Supabase, Storage, Realtime, tác vụ backend, push notification, gọi thoại/video, vận hành và kiểm thử. Mỗi giai đoạn phải cho ra một luồng sử dụng hoàn chỉnh có thể demo trên hai tài khoản; không hoàn thành toàn bộ UI rồi mới nối backend.

Quy ước:

- **[NGUỒN]**: có trong feature_app.txt, HTML, SQL hoặc code được dẫn.
- **[ĐỀ XUẤT]**: lựa chọn kiến trúc/phạm vi trong kế hoạch, chưa được người dùng xác nhận.
- **[CẦN CHỐT]**: mâu thuẫn hoặc thiếu quy tắc ảnh hưởng tới triển khai.
- Mã REQ dùng truy vết nghiệp vụ; DB dùng cho phần SQL cần bổ sung; T dùng đối chiếu file học kỹ thuật.
- Các tên module, bảng bổ sung và RPC dưới đây là thiết kế dự kiến, chưa tồn tại nếu không được ghi rõ.

## 2. Nguồn đầu vào và thứ tự ưu tiên

| Nguồn | Vai trò |
|---|---|
| [feature_app.txt](../../context/feature_app.txt) | Danh mục 8 phân hệ, hành vi nghiệp vụ và ghi chú đã bỏ sinh trắc học |
| [Diu_Messenger_UI.html](../../context/Diu_Messenger_UI.html) | Luồng màn hình, trạng thái, tương tác và ngôn ngữ giao diện |
| [Diu_Component_Library.html](../../context/Diu_Component_Library.html) | 8 nhóm component, biến thể, token và tham số |
| [diu_database.sql](../../context/diu_database.sql) | Schema dự thảo: 14 bảng, helper functions, RLS, 2 bucket, publication |
| [diu-icons](../../context/diu-icons/README.md) | Bộ icon gốc |
| [instruction.txt](../../instruction.txt) | Quy ước typography đã có của dự án |
| Code Kotlin/Gradle hiện tại | Bằng chứng phần đã triển khai và kiến trúc cần kế thừa |
| [example_ReadMe.md](../../contextReadMe/example_ReadMe.md), structure.txt, knowledge_supabase.txt | Tài liệu tham khảo; có nội dung template/cũ, phải đối chiếu code |

Khi nguồn mâu thuẫn, giữ nguyên bản gốc và ghi quyết định riêng. Không coi các hành vi mô phỏng trong HTML là logic an toàn đã có ở backend. example_ReadMe.md là ví dụ Flutter; Dịu hiện là Kotlin/Compose, nên dùng Room thay cho Drift và Koin thay cho Bloc/get_it.

## 3. Hiện trạng đã kiểm tra

| Hạng mục | Bằng chứng | Kết luận |
|---|---|---|
| Application, Koin, Timber | ShowcaseApplication.kt; app/di/AppKoinModule.kt | Có nền tảng khởi tạo; vẫn nạp album mẫu và onboarding |
| Compose, theme, mood, component | feature/base/common/res và presentation/compose/composable | Đã có nhiều primitive Dịu; cần kiểm tra độ khớp HTML và bổ sung component chuyên biệt |
| MVI reducer + StateFlow | BaseViewModel.kt, BaseAction.kt | Đã có; nên kế thừa, bổ sung quản lý coroutine/effect có kiểm soát |
| Onboarding | OnBoardingScreen/ViewModel/UiState | Có UI/state trang; chưa thấy luồng hoàn tất onboarding gắn tài khoản |
| Auth | feature/auth/src/main | Mới có manifest và bộ khung Gradle/test mẫu; chưa có Phone OTP production |
| Navigation | MainShowcaseScreen.kt | Đang bắt đầu bằng onboarding, hoàn tất chuyển sang album |
| Supabase | SupabaseClientFactory.kt, SupabaseModule.kt, SupabaseResult.kt | Có cấu hình client; chưa chứng minh auth, chat hay SQL đã hoạt động trên server |
| Room + Clean Architecture mẫu | feature/album | Có ví dụ DAO, mapper, repository, use case, ViewModel/test; chưa phải dữ liệu Dịu |
| Architecture tests | konsist-test | Đã có luật layer, use case, ViewModel; cần rà scope khi thêm package/module |
| Convention plugins, catalog | build-logic; gradle/libs.versions.toml | Có sẵn quản lý cấu hình tập trung |
| Backend | docs/context/diu_database.sql | Là SQL mẫu trong repo; chưa kiểm chứng đã deploy/migration chạy thành công |

Không đưa phần đang có lên mức “hoàn thành” chỉ vì dependency đã được khai báo. Bản kế hoạch này không thực thi SQL, không thay đổi code tính năng và không xác nhận build hiện tại đã pass.

## 4. Quyết định nghiệp vụ cần quản lý

| Mã | Chủ đề | Trạng thái và hướng xử lý |
|---|---|---|
| DEC-01 | Stack | [NGUỒN] Kotlin, Compose, Koin, coroutines/Flow, Room, Supabase; kế thừa Navigation Compose hiện có |
| DEC-02 | Sản phẩm giai đoạn đầu | [ĐỀ XUẤT] Android trước; chat cá nhân trước nhóm, audio call trước video; toàn bộ tính năng còn lại vẫn có trong roadmap |
| DEC-03 | Vòng thân có đồng thuận? | [CẦN CHỐT] SQL contacts có hướng, chú thích nói hai chiều. Đề xuất người nhận chấp nhận lời mời; circle/nickname riêng của mỗi chủ tài khoản |
| DEC-04 | Thư có đọc lại/sửa được không? | [CẦN CHỐT] Feature nói cả hai không đọc trước hẹn, nhưng HTML cho xem lại và feature cho sửa. Đề xuất V1: người gửi được xem/sửa/hủy trước release, người nhận chỉ thấy metadata |
| DEC-05 | Niêm phong có nghĩa gì? | [CẦN CHỐT] RLS khóa đọc theo giờ không đồng nghĩa mã hóa đầu cuối. V1 đề xuất khóa truy cập phía server; không quảng bá E2EE hay “server cũng không đọc được” |
| DEC-06 | Thu hồi và xóa | [ĐỀ XUẤT] Thu hồi tạo tombstone chung; xóa chat chỉ trên thiết bị hiện tại đúng copy UI, không xóa dữ liệu đối phương; phân biệt xóa kỷ niệm và xóa file cache |
| DEC-07 | Nhóm | [ĐỀ XUẤT] Nhóm nhỏ; giới hạn thành viên và giới hạn dung lượng cần chốt ở P0; một admin cuối cùng phải chuyển quyền trước khi rời |
| DEC-08 | Mood và riêng tư | [ĐỀ XUẤT] note luôn riêng tư; chỉ mood/ngày được chia sẻ theo allowlist; không dùng current_mood công khai để vượt quyền chia sẻ |
| DEC-09 | Chặn âm thầm | [ĐỀ XUẤT] Không báo “bạn bị chặn”; server vẫn từ chối phát tán nội dung/cuộc gọi; UI không hiển thị delivered giả. Không hứa người kia không thể suy đoán |
| DEC-10 | Giờ yên lặng | [ĐỀ XUẤT] Tin vẫn được đồng bộ ngay, chỉ tắt chuông/rung; gửi chậm là lựa chọn riêng. Ngoại lệ chỉ vượt quy tắc Dịu, không tự vượt DND của Android |
| DEC-11 | Video trong kho/tìm kiếm | [CẦN CHỐT] UI search ghi “Ảnh & Video”, Storage nhận video/mp4, nhưng message_type chưa có video. Đề xuất thêm video ở bản đầy đủ; tạm chưa hiện bộ lọc video khi chưa hỗ trợ |
| DEC-12 | Sinh trắc học | [NGUỒN] feature_app ghi đã bỏ; HTML còn biometric. Để ngoài phạm vi, giữ lock là bản xem trước thông báo hệ thống |
| DEC-13 | Tóm tắt nhóm | [ĐỀ XUẤT] Digest xác định bằng số tin/người gửi/nội dung được phép hiện, không cần AI |
| DEC-14 | Mốc thời gian | [ĐỀ XUẤT] Timestamp lưu UTC, lịch/ngày mood tính theo timezone tài khoản; mặc định Asia/Ho_Chi_Minh; quy tắc lịch qua đêm gắn ngày bắt đầu phiên nghỉ |
| DEC-15 | Xác nhận đã đọc | [ĐỀ XUẤT] Có read cursor để tính unread; nếu thêm toggle chia sẻ đã xem thì tách quyền hiển thị receipt với cursor nội bộ |
| DEC-16 | Domain/link | [CẦN CHỐT] diu.vn trong mockup là địa chỉ dự kiến; chỉ dùng App Links sau khi xác nhận quyền sở hữu domain và hosting |

Các quyết định “cần chốt” chặn riêng phần phụ thuộc; vẫn làm nền tảng, auth, chat cơ bản trong lúc chốt. Ghi câu trả lời thực tế của người dùng vào đây khi có, không tự chuyển đề xuất thành xác nhận.

## 5. Kiến trúc Android đề xuất

### 5.1 Luồng đọc và luồng ghi

Giữ nguyên chiều phụ thuộc Clean Architecture, nhưng data layer phải hỗ trợ đồng bộ và vòng đời Android.

~~~mermaid
flowchart LR
    UI[Compose] --> VM[ViewModel + Reducer]
    VM --> UC[Use case / domain contract]
    UC --> REPO[Repository]
    REPO --> LOCAL[Room + Outbox]
    LOCAL --> FLOW[Flow dữ liệu local]
    FLOW --> VM
    WORK[Sync worker / foreground coordinator] --> UC
    REPO --> API[Supabase RPC / Storage]
    RT[Realtime / Push] --> SYNC[Fetch và reconciliation]
    SYNC --> LOCAL
~~~

- Với lịch sử chat, Room là nguồn dữ liệu UI đọc. Network ghi kết quả vào Room, UI không ghép hai danh sách local và remote.
- Tin mới được ghi local cùng outbox trong một transaction, hiện pending, rồi mới gửi mạng. ACK hoặc Realtime được upsert bằng cùng định danh.
- Khi app foreground và có mạng, thử gửi ngay. Worker là đường phục hồi bền vững, không buộc người dùng chờ lịch worker.
- Realtime chỉ giúp cập nhật nhanh; reconnect vẫn truy vấn bù. Push là tín hiệu thông báo/đánh thức, không là nguồn lịch sử.
- Chỉ typing, trạng thái ngắn hạn và tọa độ live đi Broadcast; message/reaction/call state quan trọng có bản ghi bền.
- Use case không nhận Supabase DTO, Room Entity, Context, NavController hay SDK call object.
- Điều hướng theo ID; repository nạp nội dung. Tránh đưa thư, số điện thoại hay toàn bộ message vào route.
- Effect ngắn như copy/share dùng lớp UI/platform; hành động cần bền như gửi tin luôn lưu trạng thái, không dựa vào event chỉ phát một lần.

Thiết kế local source of truth dựa trên [Android offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first). Mô hình outbox và quy tắc merge là thiết kế cụ thể đề xuất cho Dịu.

### 5.2 Quyền sở hữu module

Tạo module theo nhu cầu của từng mốc; không tạo sẵn tất cả ngay P0.

| Module dự kiến | Trách nhiệm |
|---|---|
| :app | Composition root, navigation tổng, manifest, deep link entry, đăng ký service/worker và DI |
| :feature:base | Giữ theme/component đang có; không biến thành nơi chứa mọi repository |
| :core:model | ID, Mood, các value object dùng chung; Kotlin thuần khi phù hợp |
| :core:network | Supabase factory/config, serialization, error mapping; app truyền cấu hình |
| :core:session | Domain contract và implementation session, user scope, token lifecycle |
| :core:database | Một Room database cho dữ liệu đồng bộ Dịu, DAO/entities/migrations; cách ly theo account |
| :core:messaging | Domain contracts/use case và data của messages/conversation query, outbox; dùng chung giữa chat, home, media, letters |
| :core:sync | Worker + điều phối sync qua domain contract; không để messaging phụ thuộc ngược sync |
| :core:notifications | Push ingress, renderer, channel/privacy/action routing; chỉ thêm khi P5 cần |
| :feature:auth, :feature:onboarding | OTP, hồ sơ ban đầu, permission explanation, hoàn tất onboarding |
| :feature:home, :feature:contacts | Vòng thân, danh sách chat, lời mời, danh bạ, quản trị nhóm |
| :feature:chat | Timeline/composer/reply/reaction/attachment UI và use case orchestration |
| :feature:letters, :feature:mood | Thư hẹn; check-in và nhật ký |
| :feature:media, :feature:search | Kho chung, photo viewer, kỷ niệm; tìm kiếm toàn cục/trong chat |
| :feature:calls, :feature:location | Call state + RTC adapter; location session + map adapter |
| :feature:settings | Profile, quiet, notification settings, storage, blocks, account deletion |
| :konsist-test, :library:test-utils | Mở rộng bộ kiểm tra hiện có |

Quy tắc dependency: app lắp ghép feature và core; feature không import presentation/data của feature khác. Shared repository contract ở core:messaging/domain; DAO ở core:database, được data implementation sử dụng. core:sync gọi domain use case, không để repository gọi Worker trực tiếp; app/coordinator yêu cầu worker chạy sau khi enqueue thành công. Tách data/domain thành Gradle module riêng chỉ khi có lý do build hoặc kiểm soát dependency, trước mắt package layer + Konsist là đủ.

Giữ Navigation Compose hiện tại; chưa cần migration Navigation 3, Hilt hay framework MVI khác. Dọn album mẫu sau khi auth → home hoạt động, không dùng các model album cho nghiệp vụ Dịu.

### 5.3 State machine bắt buộc thiết kế trước UI tương ứng

| Đối tượng | Trạng thái chính | Quy tắc |
|---|---|---|
| Session | Bootstrapping → SignedOut / NeedsProfile / Ready / Refreshing | Hết phiên dừng user-scoped job; không xóa dữ liệu chỉ vì offline |
| OTP | EditingPhone → Sending → AwaitingCode → Verifying → Success / Error | Cooldown theo deadline, restore khi xoay máy; server quyết định rate limit |
| Message local | Queued → Uploading nếu có file → Sending → Synced / RetryableFailure / PermanentFailure | Delivered/read là receipt riêng; retry không đổi message ID |
| Invitation | Pending → Accepted / Declined / Cancelled | Accept đồng thời chỉ tạo một quan hệ; self/blocked không hợp lệ |
| Letter | Draft local → Scheduled → Released → Opened; Scheduled → Cancelled | Cancel/release tranh chấp được quyết định bằng transaction server |
| Recording | Idle → RequestingPermission → Recording → Preview → Uploading | Cancel/background/lỗi phải đóng recorder và xóa temp hợp lý |
| Call | Idle → Inviting/Ringing → Connecting → Active → Ended | Failed/Rejected/Busy/TimedOut là kết quả; trạng thái kết thúc không được đảo ngược |
| Live location | Starting → Active → Stopping → Stopped / Expired | Hết hạn dựa server; UI không hiện stale location như còn hoạt động |

## 6. SQL: những việc cần làm trước khi dùng thật

Đây là các khoảng trống nhìn thấy từ SQL mẫu, không phải kết quả pentest hay kết quả chạy database. Viết migration bổ sung và test riêng; không sửa rồi chạy lại toàn bộ file một cách mù quáng.

### 6.1 Các điểm ưu tiên

| Mã | Bằng chứng trong SQL hiện tại | Việc phải có / kiểm chứng |
|---|---|---|
| DB-01 | Các helper SECURITY DEFINER chưa pin search_path; search_messages nhận p_user_id; direct RPC nhận hai user ID | Ràng buộc actor từ auth.uid(), allowlist role được execute, pin search_path, giới hạn input; user A không được query hộ B |
| DB-02 | Members INSERT cho phép user_id = auth.uid(); UPDATE own row không giới hạn cột | Không tự join conversation bất kỳ, không tự nâng admin hoặc đổi conversation_id; accept/invite/role/leave qua RPC kiểm quyền |
| DB-03 | Messages UPDATE cho phép người có quyền pin, nhưng không giới hạn cột | Tách pin/recall/edit thành thao tác kiểm quyền; người được pin không được sửa content/sender của người khác |
| DB-04 | Media SELECT/INSERT chỉ check đã đăng nhập, bất chấp comment “chỉ thành viên” | Storage path gắn conversation/uploader/object ID; kiểm membership ở upload/read/delete, signed URL ngắn hạn, không lưu URL hết hạn vào message |
| DB-05 | Profiles RLS là cấp row chứa cả phone, birthday, settings, qr_token; Quiet row chứa emergency_allowed | Tách profile công khai/riêng tư hoặc safe projection/RPC; người thân không đọc được cột riêng tư chỉ vì được SELECT row |
| DB-06 | Mood policy cho shared_with đọc cả row có note; current_mood ở profile bỏ qua allowlist | Private note riêng, shared mood projection theo allowlist; phản ánh quyền ở Home/Chat/Journal |
| DB-07 | Message INSERT không kiểm block; SELECT chỉ kiểm một chiều block | Chốt quy tắc direct/group; server kiểm chặn cho gửi, gọi, invite, lookup và location, không dựa vào client |
| DB-08 | get_or_create_direct_conversation tìm rồi insert, chưa có uniqueness theo cặp | Canonical pair key + unique constraint/lock transaction; hai yêu cầu song song trả cùng conversation |
| DB-09 | Contacts chú thích hai chiều nhưng mỗi row có hướng; chưa có invitation flow | contacts là dữ liệu cá nhân, thêm connection_requests + accepted connection hoặc RPC tương đương |
| DB-10 | sealed_letters người gửi luôn SELECT; người nhận trước hạn không SELECT được row; comment nói app chỉ lấy metadata | Metadata và payload tách quyền; không dùng UI che chữ làm bảo vệ; mode niêm phong phải theo DEC-04/05 |
| DB-11 | open_sealed_letter trả true sớm nếu is_opened trước kiểm recipient; UPDATE rộng | Kiểm actor trước mọi nhánh; mở atomically, opened_at một lần; sender không tự đánh dấu opened |
| DB-12 | Reaction/star/live INSERT chỉ kiểm sở hữu bản ghi | Kiểm quyền conversation/message; FK reply/forward/read cursor cùng conversation; metadata đúng loại, tọa độ/size hợp lệ |
| DB-13 | messages chưa có updated_at/version; chưa có change cursor/tombstone phục hồi | Thêm mutation version/change log phù hợp để backfill update/recall; fetch chỉ created_at sẽ bỏ sót sửa tin cũ |
| DB-14 | Chưa có client operation ID, outbox receipt, attachment lifecycle | Dùng UUID message tạo client hoặc (actor, operation_id) unique; command receipt cho mutation khác; không gửi lặp khi timeout |
| DB-15 | call_status chỉ completed/missed/rejected/busy | Thêm call_sessions cho signaling lifecycle, TTL và winner device; call_logs là kết quả cuối |
| DB-16 | Chưa có notification delivery/digest/release job | Transactional notification_jobs/outbox, device delivery key, retry/backoff và cleanup token lỗi |
| DB-17 | quiet numeric hour, không có timezone; is_in_quiet_hours hard-code VN và kiểm ngày hiện tại | Minute-of-day + timezone; test lịch qua nửa đêm và thứ trong tuần gắn ngày bắt đầu |
| DB-18 | 7 bảng realtime; chưa thấy policies cho Broadcast/Presence | Private channel authorization; subscribe/unsubscribe theo membership/session, kiểm thử sau leave/block |
| DB-19 | Avatar bucket public; media path/delete chưa thống nhất | Chốt avatar có thực sự công khai; chính sách thay avatar nhóm và dọn object mồ côi có reference check |
| DB-20 | Không có flow xóa auth user/file/token, admin cuối nhóm, video enum/link index | Thêm theo từng phase; xóa tài khoản là job backend có retry, không chỉ xóa profile trên máy |

Cần kiểm column privilege và RPC grant cùng RLS: RLS không tự biến thành quyền theo cột. SECURITY DEFINER có thể chạy với quyền cao hơn caller; thiết kế actor/execute/search_path phải được test. Tham chiếu [Supabase database functions](https://supabase.com/docs/guides/database/functions).

### 6.2 Bảng/migration bổ sung dự kiến

| Nhóm | Bổ sung đề xuất | Có ở mốc |
|---|---|---|
| Identity | private profile/settings hoặc safe public projection, timezone, connection_requests | P1–P3 |
| Conversation | canonical direct pair, permission RPC, membership mutation audit/version | P3/P7 |
| Messaging | updated_at/version hoặc change feed, server cursor, idempotency, typed metadata validation | P4 |
| Attachment | storage object key, mime/size/status/uploader, reference/cleanup tracking | P6 |
| Delivery | notification_jobs, delivery attempts, group_digest buckets, token/device mapping | P5/P9 |
| Letters | lifecycle, revision/version, release state, metadata/payload split | P8 |
| Calls | call_sessions, call participant/device acceptance, expiry, final log | P10 |
| Location | session status/revision/expiry; snapshot freshness; authorized stop | P11 |
| Account | deletion_requests/job state, retained-content policy và cleanup tasks | P12 |

Local Room thêm messages, conversations, members, contacts, receipts, attachments, outbox_operations, sync_cursors; các bảng mood/letters/cache tạo khi feature cần. Dữ liệu private phải nằm trong account scope; lựa chọn một DB/file theo account giúp tránh đọc chéo tài khoản.

### 6.3 Hợp đồng backend tối thiểu dự kiến

Tất cả mutation có auth, validation, quyền và error code ổn định. Actor lấy từ JWT; operation_id dùng cho thao tác có thể retry. Danh sách dưới là thiết kế, không phải API đã tồn tại.

| RPC / endpoint dự kiến | Input quan trọng | Đảm bảo |
|---|---|---|
| bootstrap_profile | display name, username, operation_id | Upsert hồ sơ riêng + default quiet một lần sau xác thực; không lộ số điện thoại |
| lookup_contact / lookup_by_qr_token | phone E.164 hoặc QR token | Chỉ trả safe profile; rate limit chống dò tài khoản |
| request_connection / respond_connection | target/request ID, quyết định, circle cá nhân | Không tự kết nối không đồng thuận; accept nguyên tử |
| get_or_create_direct_conversation | peer ID | Caller thuộc cặp; unique cặp, kiểm connection/block |
| create_group / mutate_group | group command, expected_version | Tạo group + admin + members nguyên tử; allowlist trường sửa |
| send_message | message UUID, conversation, type, content, mood, metadata | Kiểm member/block/reference; một bản ghi; timestamp server |
| recall_message / pin_message | message ID, operation_id | Quyền theo hành động, tombstone, pin trong cùng conversation |
| set_reaction / set_read_cursor | target ID/mood hoặc ordered cursor | Không lùi read cursor; reaction unique người/tin |
| list_changes / conversation_summary | opaque cursor, limit | Keyset bền với thứ tự rõ; trả tombstone, unread và last-message projection |
| create/update/cancel/release_letter | metadata, payload, expected_version | Kiểm deadline phía server; release một lần; không lộ payload trước hẹn |
| open_letter | letter ID | Chỉ recipient hợp lệ, đúng giờ; opened_at server một lần |
| search_messages | query, scope, mood, cursor | Scope từ auth.uid(); lọc deleted/unreleased/private content; giới hạn query |
| create/accept/end_call | session ID, device ID, revision | Một winner nhận cuộc gọi, monotonic state, TTL |
| start/stop_live_location | conversation, duration/session ID | Thành viên hợp lệ, expires_at server, explicit stop |
| request_account_deletion | recent auth proof, operation_id | Job có trạng thái, revoke token và dọn dữ liệu theo policy |

Đọc danh sách đơn giản có thể dùng PostgREST + RLS; chỉ dùng RPC khi cần transaction/quyền theo cột/quy tắc nguyên tử. Không xây mọi thứ thành Edge Function hoặc RPC nếu truy vấn thông thường đã đủ.

## 7. Ma trận toàn bộ tính năng → UI → dữ liệu → kiểm chứng

Nguồn của các REQ dưới đây: feature_app.txt theo 8 phân hệ, đối chiếu tên màn ở SCREENS trong HTML. “Kiểm chứng” là tiêu chí nghiệm thu rút gọn; mỗi ticket khi triển khai bổ sung Given/When/Then cụ thể.

| REQ | Tính năng và UI tương ứng | Dữ liệu / xử lý chính | Mốc | Kiểm chứng |
|---|---|---|---|---|
| REQ-01 | Onboarding 0–3, đặt tên | DataStore local + profile onboarding_completed | P2 | Restart chọn đúng điểm tiếp tục, không chạy lại onboarding vô hạn |
| REQ-02 | login, OTP, keypad, resend | Supabase Phone Auth + session | P2 | Đúng/sai/hết hạn/cooldown; xoay máy không gửi OTP lặp |
| REQ-03 | permissions | Android permission state thực | P2/P6/P10 | Từ chối vẫn chat text được; xin lại đúng thời điểm cần |
| REQ-04 | invite, QR cá nhân, quét camera/ảnh, chia sẻ/lưu | qr_token, safe lookup, QR encoder/decoder, App Links | P3 | Link cũ vẫn dùng; mở khi chưa login rồi tiếp tục; QR lạ không mở route tùy ý |
| REQ-05 | Tìm số điện thoại, chọn love/family/friends | connection_requests, contacts cá nhân | P3 | Tìm đúng safe profile; blocked/self/rate limit; không lộ số cho người khác |
| REQ-06 | home, emptyhome, constellation | contacts + conversation summaries + shared mood + quiet projection | P3/P4 | Người mới thấy empty; chạm orb mở đúng chat; fallback danh sách truy cập được |
| REQ-07 | Recent chats, unread, pending letter, thời gian nhân văn | summaries, read cursor, letter metadata | P4/P8 | Không âm/tăng đôi unread; chữ thời gian đúng timezone và ranh giới ngày |
| REQ-08 | contacts: filter/search/circle/nickname/anniversary | contacts + safe profiles | P3 | Nhãn của A không sửa nhãn B; tìm tiếng Việt; ngày kỷ niệm hiển thị đúng |
| REQ-09 | newgroup, selected chips, tên/mood/avatar | create_group transaction + members | P7 | Double tap không tạo hai nhóm; retry không sót thành viên |
| REQ-10 | groupedit: quyền edit/invite/pin, xóa/rời nhóm | permission RPC + role/membership | P7 | Member không tự nâng quyền; admin cuối được hướng dẫn chuyển quyền |
| REQ-11 | chat: text, mood tray, nhóm/direct, ngày và receipt | messages + Room/outbox + read cursor | P4/P7 | Hai máy nhận đúng một tin; offline/restart/reconnect không mất hoặc nhân đôi |
| REQ-12 | Header mood/quiet, typing ba chấm | safe mood/quiet + ephemeral Broadcast TTL | P4/P9 | Typing tự hết hạn; mood không vượt allowlist |
| REQ-13 | reply, msgmenu, copy, pin/unpin | reply reference, pin RPC, clipboard | P4/P7 | Quote đúng chat; recall nguồn có fallback; pin không cho sửa nội dung |
| REQ-14 | Reaction picker, reactdetail | message_reactions persisted + local merge | P7 | Một người một mood/tin, đổi/gỡ được, kết quả giữ sau restart |
| REQ-15 | forward đa người | recipient selection + per-recipient idempotent command | P7 | Hiện số thành công/thất bại; retry chỉ gửi phần lỗi; không lộ nguồn riêng tư |
| REQ-16 | recall, confirm xóa chat/rời/chặn/xóa account | tombstone, local deletion boundary, server command | P4/P7/P12 | Recall tới thiết bị offline sau reconnect; xóa local không xóa bản đối phương |
| REQ-17 | Voice ghi/preview/play/scrub/waveform | recorder, audio file, metadata, Media3 adapter, Storage | P6 | Audio thật; xin mic; hủy/dừng app không rò recorder; không nhiều player phát chồng |
| REQ-18 | attach camera/photo, caption | Photo Picker/camera adapter, URI file, upload state | P6 | Permission bị từ chối/URI mất/file quá lớn/upload lỗi có xử lý |
| REQ-19 | ScheduleSheet: thức dậy/sáng/tối/kỷ niệm | resolve thời điểm theo timezone + letter API | P8 | Mốc đã qua được tính lại rõ ràng; không lấy giờ máy làm quyền mở |
| REQ-20 | composeletter: recipient/date/body/seal | letter revision, metadata/payload, transaction | P8 | Người nhận gọi API trực tiếp trước hẹn vẫn không lấy được payload |
| REQ-21 | letters pending/delivered, sửa/hủy/gửi ngay/opened time | letter lifecycle + server scheduler | P8 | Race hủy/release và mở nhiều máy có một kết quả nhất quán |
| REQ-22 | incoming + quick decline | call session, push, decline idempotent message | P10 | Nhận/từ chối từ notification; quá hạn không hồi sinh màn incoming |
| REQ-23 | audio call: mic/speaker/duration/end | RTC engine + audio routing + Android call lifecycle | P10 | Hai mạng khác nhau gọi được; mất mạng/phone interruption kết thúc đúng |
| REQ-24 | video call: self overlay, flip/mic/camera | RTC media tracks, video renderer | P10 | Đổi camera không mất audio; xoay máy không tạo call mới; nền giải phóng camera theo policy |
| REQ-25 | calls history/missed/frequent contacts | finalized call_logs, local cache, aggregate | P10 | Caller/callee hiển thị cùng outcome/thời lượng hợp lý, không duplicate log |
| REQ-26 | checkin mood + private note + share từng người | mood_checkins + private note + visibility projection | P9 | Unshared user không đọc màu/note; shared user chỉ đọc mood/ngày |
| REQ-27 | journal lịch tháng, tỷ lệ mood, “Cùng Minh” | local calendar + aggregate authorized shared days | P9 | Tháng 28/29/30/31 ngày; ngày thiếu không tính là cùng mood; chỉ so ngày được chia sẻ |
| REQ-28 | info: ngày bên nhau/kỷ niệm/số thư/nickname/mute/preview | relationship metadata + message-day aggregate + member prefs | P7/P8 | Phân biệt “ngày có nhắn” với “số ngày quen”; không tải mọi message để đếm |
| REQ-29 | media: photo/voice/opened letters theo tháng | authorized attachment queries + paging/local cache | P6/P8 | Tab thư không lộ thư chưa đến; phân trang không lặp; avatar/media quyền khác nhau |
| REQ-30 | photo viewer, caption, ẩn thanh, external share | Storage object key, signed URL, file share URI | P6 | URL hết hạn refresh được; chia sẻ file an toàn; back/rotation giữ vị trí xem |
| REQ-31 | starred kỷ niệm đã lưu | starred_messages + message projection | P7 | Chỉ lưu tin có quyền; nguồn bị recall không giữ nội dung trái policy |
| REQ-32 | chatsearch keyword/mood/highlight/jump | scoped FTS + local cached search | P9 | Query đổi nhanh không ghi đè kết quả mới; mở được message chưa có ở trang hiện tại |
| REQ-33 | globalsearch: all/people/message/media/link | multiple safe search sources + type support | P9/P12 | Scope theo account; tiếng Việt có/không dấu theo spec; video chỉ có khi schema hỗ trợ |
| REQ-34 | location tĩnh, map/radius/address/accuracy | one-shot location + typed message metadata | P11 | Approximate/no fix/permission off vẫn thao tác được, hiện độ mới và accuracy |
| REQ-35 | live location 15m/1h/8h, stop | foreground session + private Broadcast + snapshot + expiry | P11 | Tắt màn/mất mạng/khởi động lại/expired không tiếp tục chia sẻ âm thầm |
| REQ-36 | quiet dial, weekdays, emergency whitelist | quiet schedule timezone + backend routing policy | P5/P9 | Qua đêm đúng thứ; whitelist không được mô tả là vượt DND Android |
| REQ-37 | notif color/name/full, sounds/vibration/group digest, lock preview | settings + channels + renderer + notification jobs | P5/P9 | Color không chứa tên/body trong payload/public view; digest không báo lặp |
| REQ-38 | inappnotif, tap vào chat, notification actions | app-level coordinator + safe pending navigation | P5 | Không hiện banner cho chat đang mở; hết phiên phải login trước khi vào chat |
| REQ-39 | storage chart/by conversation/clear cache/auto download/original | local file index + cache policies + prefs | P6/P12 | Số byte có thật; dọn cache không xóa outbox/chưa upload hay remote của đối phương |
| REQ-40 | blocked list/unblock | blocks + enforcement server | P5/P12 | Block ngăn push/call/live và message theo DEC-09; unblock không gửi lại lệnh đã bị từ chối |
| REQ-41 | connlost, retry và automatic resend | connectivity hint + persisted outbox + reconciliation | P4 | Không chặn đọc cache; gửi lại sau process death; có nút xử lý lỗi vĩnh viễn |
| REQ-42 | profile/editprofile/settings/theme/logout/delete | safe/private profile, DataStore, session/deletion job | P2/P12 | Theme restore; account switch không lộ cache; xóa có trạng thái và quy tắc dữ liệu người còn lại |
| REQ-43 | Video attachment nếu chốt hỗ trợ | video enum/type, transcode/limits, thumb/player/storage | P12 | Mime/type thống nhất; giới hạn dung lượng, upload retry và search filter đúng |
| REQ-44 | Read-receipt privacy nếu giữ toggle HTML | private settings + public receipt policy | P9 | Tắt chia sẻ receipt vẫn tính unread nội bộ; không quảng bá presence sai quyền |

Không biến emptyhome/connlost/lock/inappnotif/confirm thành các destination production riêng chỉ để khớp SCREENS. Trong Compose chúng có thể là state, preview, banner hoặc dialog. biometric là màn legacy loại trừ theo DEC-12; bộ khung điện thoại, fake Android status bar và tweak panel chỉ thuộc bản demo HTML.

## 8. Roadmap triển khai chi tiết

Ước lượng dưới đây là **ngày công tập trung** của một người làm Android và backend, đã có kỹ năng Kotlin/SQL cơ bản, chưa cộng thời gian học sâu, chờ SMS/domain/provider hoặc duyệt phát hành. Mỗi mốc kết thúc bằng demo + bộ kiểm chứng tương ứng; không coi thời gian là cam kết.

### P0 — Khóa phạm vi, baseline và backlog (3–5 ngày)

Đầu vào: toàn bộ docs/context và code hiện có.

1. Ghi quyết định DEC-03/04/05/07/11/16; tạo issue cho các điểm chưa chốt.
2. Kiểm kê route/component và thiết kế contrast/motion còn thiếu; giữ màu, typography và icon đã có.
3. Chạy baseline build/unit/Konsist/static checks phù hợp; ghi lỗi có sẵn trước khi đổi code.
4. Xác định môi trường local/dev/staging/prod và dữ liệu test A/B/C.
5. Chốt định nghĩa delivered/read, chính sách block, lịch sử nhóm trước/sau membership, xóa dữ liệu và mục tiêu hiệu năng.
6. Chia các REQ thành ticket theo schema → contract → logic → UI → kiểm chứng.

Đầu ra: decision log, module ownership, backlog và danh sách lỗi baseline. Điều kiện qua mốc: không còn hiểu khác nhau về bảo mật thư và consent kết nối trước khi viết những phần đó.

### P1 — Backend và nền tảng dùng chung (7–10 ngày)

Phụ thuộc P0; chỉ tạo các core module cần ngay.

1. Chuyển SQL mẫu thành migration theo thứ tự; tạo seed fixtures giả, không chứa thông tin người thật.
2. Làm DB-01 đến DB-06/08/12 ở phạm vi auth/contact/message; các module sau tiếp tục bổ sung policy riêng.
3. Tạo public profile projection và private settings, chính sách Storage/path và skeleton test RLS.
4. Bổ sung dispatcher/Clock/session abstractions, domain errors, UUID/operation ID và kết quả lỗi typed.
5. Thiết kế Room schema/account scope, migration test, local transaction và fake datasource cho preview/test.
6. Koin root cấp dependency; credentials admin, SMS, push và TURN chỉ ở backend.
7. Cấu hình CI checks tương thích Gradle task thực tế, secret injection và environment guard.

Nghiệm thu: database reset/migrate được trên môi trường kiểm thử; A không đọc/sửa private row của B; client không có service-role key. Profile bootstrap retry không tạo nhiều default rows.

### P2 — Onboarding, auth và profile đầu tiên (8–12 ngày)

REQ-01/02/03 và phần đầu REQ-42; phụ thuộc P1.

1. Sửa start route theo SessionState, không hard-code onboarding → album.
2. Hoàn thiện intro 0–2, nhập phone E.164 + keypad có accessibility, OTP 6 số + paste/autofill phù hợp.
3. Resend cooldown theo thời điểm kết thúc, không đếm bằng giá trị reset mỗi recomposition.
4. Verify OTP → bootstrap profile → đặt tên/mood → lưu hoàn tất → Home rỗng.
5. Notification permission giải thích nhẹ nhàng; mic/camera xin khi dùng, trạng thái toggle phản ánh OS.
6. Session restore/refresh/logout và pending App Link; lỗi mạng không bị hiểu là logout.
7. Loại album khỏi luồng chính khi luồng mới hoạt động.

Nghiệm thu: Given tài khoản mới, When OTP hợp lệ và đặt tên, Then restart mở Home; OTP sai/hết hạn không vượt auth. Signed-out link phải quay lại đúng người sau login. Xoay máy không resend hoặc bootstrap lặp.

### P3 — Kết nối vòng thân và Home cơ bản (8–12 ngày)

REQ-04/05/06/08; phụ thuộc P2.

1. Safe lookup số điện thoại/QR; request/accept/decline connection theo DEC-03.
2. QR cá nhân sinh từ link thật, không dùng mã DIU-7K2M demo; decoder từ camera/ảnh; save/share.
3. App Links verify domain; link/QR invalid/deleted/self/blocked có thông báo rõ.
4. Contacts filter circle, tìm tên, nickname/anniversary riêng; Room cache.
5. Home empty/non-empty, orb constellation bố trí ổn định theo ID, có danh sách tương đương cho TalkBack.
6. get-or-create direct bằng transaction, sau accept điều hướng chat trống.

Nghiệm thu: Hai máy cùng mở chat tạo một conversation. Người C biết ID/QR không đọc được private profile. Hủy hoặc nhận lời mời hai lần cho một kết quả.

### P4 — Chat text offline-first hoàn chỉnh (12–18 ngày)

REQ-07/11/12/13/16/41 ở phạm vi direct/text; phụ thuộc P3.

1. Implement message model, DTO/Entity/Domain/UiModel tách biệt và typed content.
2. Ghi message + outbox nguyên tử; sender UUID ổn định, local pending indicator.
3. Gửi foreground + worker drain, lease/unique work để không có hai executor tranh cùng operation.
4. Server idempotency, ACK/Realtime upsert cùng ID; phân biệt retryable/permanent/unknown outcome.
5. Timeline keyset paging, grouping ngày, scroll-to-bottom đúng lúc và giữ anchor khi load tin cũ.
6. read cursor tăng đơn điệu, unread summary và snapshot last message; không dùng UUID ngẫu nhiên làm thứ tự.
7. Realtime subscription theo session/conversation; reconnect fetch bù insert/update/recall.
8. Reply, copy, recall/tombstone; local deletion boundary chống tin cũ tải lại sau “xóa chat”.
9. Typing throttle/TTL, quiet/mood chỉ từ safe projection, network banner không chặn cache.

Nghiệm thu: Given 10 tin queued khi offline, When tắt process rồi mở lại và có mạng, Then cả hai máy có đúng 10 tin, đúng thứ tự server. Mô phỏng server đã lưu nhưng client timeout vẫn chỉ một tin. Recall tin cũ khi B offline phải cập nhật sau B reconnect.

### P5 — Push, riêng tư thông báo và block cơ bản (10–15 ngày)

REQ-36/37/38/40 bản cơ bản; phụ thuộc P4.

1. FCM registration/token rotation/device logout, backend HTTP v1 credentials; job được tạo cùng transaction sự kiện.
2. Dispatcher kiểm membership/block/mute/quiet/privacy trước gửi; trạng thái mỗi delivery có retry.
3. Renderer cho foreground/background/lock screen; mode color/name/full từ dữ liệu cho phép.
4. PendingIntent mở chat qua auth guard; reply/read actions idempotent.
5. App-level banner khi ở màn khác; tránh trùng FCM + Realtime theo event/message ID.
6. Quiet schedule mặc định 22:30–06:00, whitelist theo Dịu; UI không hứa ghi đè OS.
7. Block/unblock kiểm ở server; không tự retry lệnh bị cấm như lỗi mạng.

Nghiệm thu: A gửi lúc B nghỉ, dữ liệu vẫn đến nhưng không báo chuông theo chính sách; A bị block không tạo push/call cho B. Mode color không chứa tên/body trong payload và notification public view. Token hết hiệu lực được cleanup.

### P6 — Ảnh, voice, kho chung và cache (10–15 ngày)

REQ-17/18/29/30/39 phần media; phụ thuộc P4/P5.

1. Photo Picker, camera flow và caption; file URI permission/temp copy đủ bền cho retry.
2. Nén/resize theo settings, kiểm mime/dung lượng, giữ object key thay URL hết hạn.
3. Attachment state và upload retry trước send_message; cleanup object upload xong nhưng message thất bại.
4. Recorder lifecycle, metering waveform thật, preview/cancel, xử lý interruption và mic denied.
5. Media3 player adapter: một nguồn phát, progress/scrub, audio focus, dispose đúng owner.
6. Media grid theo tháng + voice list, photo full screen/share file URI, signed URL refresh.
7. File cache index theo conversation/account, đo byte và clear cache tránh outbox/unuploaded file.

Nghiệm thu: Tắt mạng giữa upload rồi restart không mất attachment hoặc gửi hai message. Người ngoài không tải file khi biết object path. Clear cache không xóa voice chưa gửi.

### P7 — Nhóm nhỏ, reaction, pin, forward và kỷ niệm (8–12 ngày)

REQ-09/10/13/14/15/16/28/31; phụ thuộc P4/P6.

1. Group create/edit/avatar/mood, permission RPC, add/remove/leave/transfer admin.
2. Reaction persisted + detail; pin/unpin theo quyền; quote jump và source recalled fallback.
3. Forward nhiều người với operation ID theo recipient và kết quả từng người; không tự cấp quyền object nguồn.
4. ChatInfo, nickname/mute, thống kê server query, anniversary; starred messages private.
5. Render system events từ backend tin cậy; client không tự giả “admin thêm người”.

Nghiệm thu: Gọi API trực tiếp để tự join/nâng role/sửa tin người khác đều bị chặn. Forward lỗi 1/3, retry không gửi lại cho 2 người thành công. Admin cuối không làm nhóm vô quản trị.

### P8 — Gửi chậm và thư niêm phong (8–12 ngày, có thể tăng nếu chọn mật mã nâng cao)

REQ-19/20/21 và phần thư REQ-07/28/29; phụ thuộc DEC-04/05 và P4/P5.

1. Resolve các lựa chọn lịch từ timezone người nhận; chốt “khi thức dậy” theo quiet schedule và fallback.
2. Tách letter metadata/payload và quyền; không đặt nội dung trong messages.content hoặc metadata công khai.
3. Create message envelope + letter + release job nguyên tử, revision/expected_version.
4. Pending/delivered/opened lists; sửa/hủy/gửi ngay theo chính sách đã chốt.
5. Server cron/worker release với deadline server và notification job idempotent.
6. Recipient fetch khi tới hạn; opened_at được server ghi một lần; subscribe hoặc refresh khi foreground.
7. Test cả cached search, notification payload, telemetry và media tab không lộ thư trước hạn.

Nghiệm thu: Given recipient chỉnh đồng hồ trước 1 năm, When gọi open_letter, Then chưa đến server deadline vẫn không đọc được. Cron chạy hai lần chỉ tạo một logical release/job; mạng chậm có thể trì hoãn thông báo. Nếu quyết định thật sự “cả người gửi cũng không đọc”, phải thiết kế lại khả năng sửa và key custody trước mốc này.

### P9 — Mood journal, search và settings nâng cao (8–12 ngày)

REQ-26/27/32/33/36/37/44; phụ thuộc P5/P7/P8.

1. Mood upsert một bản/ngày timezone tài khoản; note private, allowlist chia sẻ màu.
2. Journal lịch thật, aggregate theo tháng và so cùng mood chỉ trên phần giao quyền.
3. FTS/index/query normalization đồng nhất; test có/không dấu/đ; search debounce + cancel request cũ.
4. Chat search, global tabs, paging, highlight và jump-to-message quanh message ID.
5. Quiet dial/minutes/weekdays/timezone, emergency exceptions; digest tối theo timezone, không AI.
6. Read-receipt privacy nếu giữ yêu cầu; profile mood visibility phải cùng một policy.

Nghiệm thu: Người được share mood không đọc note qua API. Query “binh yen” tìm được văn bản “Bình yên” nếu đã chốt hỗ trợ; thư chưa release không có trong kết quả. Digest retry không tạo nhiều thông báo cùng kỳ.

### P10 — Gọi thoại rồi video (18–28 ngày)

REQ-22/23/24/25; phụ thuộc P5/P7. Spike chọn RTC adapter và đo trên mạng thật trước khi hoàn thiện UI.

1. Chọn WebRTC tự tích hợp + TURN hoặc SDK RTC managed; ghi ADR về vận hành, chi phí và lock-in. Không chọn nhà cung cấp chỉ từ tên trong mockup.
2. call_sessions bền, caller/callee, TTL, state transition và revision; call_logs tách kết quả.
3. Private signaling SDP/ICE, version/sequence, xử lý duplicate/late event.
4. Push incoming có expiry; một device claim accept, các device khác ngừng đổ chuông.
5. Android Telecom/Core-Telecom phù hợp, ongoing call service/notification, audio focus, Bluetooth/speaker, mic permission.
6. Audio-first; reject/busy/no-answer/quick decline; xử lý app background, lock screen và call interruption.
7. Thêm video tracks, front/back, self preview, mic/camera toggles, lifecycle rotate/background.
8. Stats call quality và finalization idempotent; lịch sử theo ngày, missed và frequent contacts.

Nghiệm thu: Hai thiết bị Wi-Fi/4G khác nhau gọi thành công qua TURN khi cần. Hai bên kết thúc đồng thời chỉ một final outcome. Nhận trên máy thứ hai không được mở hai media session. Màn incoming hết hạn không thể accept. Không dùng WorkManager để duy trì stream cuộc gọi.

### P11 — Vị trí tĩnh và live location (7–12 ngày)

REQ-34/35; phụ thuộc P5/P6.

1. Adapter map/provider và location; API key restrictions, billing/env, chính sách cache.
2. Xin location lúc dùng; approximate/denied/no GPS/stale fix có fallback.
3. Static message gồm tọa độ/accuracy/captured_at/address khi có.
4. Start live session có expires_at server, stop action trong app/notification; foreground service theo yêu cầu nền tảng.
5. Private channel theo session/conversation; adaptive interval theo di chuyển/chất lượng mạng thay vì cố định 3 giây mọi lúc.
6. Lưu snapshot vừa phải; phát lại snapshot có timestamp, hết hạn/leave/block phải dừng hiển thị và chặn nhận mới.
7. Pinch/map gestures và radius là biểu diễn độ chính xác, không giả vị trí rất chính xác.

Nghiệm thu: Hết 15 phút không phát tiếp dù đổi giờ máy; người ngoài không subscribe được tọa độ. Revoke permission/dừng service có state rõ. Restart không tự khởi động chia sẻ mới nếu chưa có consent.

### P12 — Hoàn thiện toàn bộ, dữ liệu tài khoản và phát hành (10–15 ngày)

REQ-39/40/42/43 và các nhánh lỗi còn lại; phụ thuộc các mốc trên.

1. Storage chart theo byte thật, per-conversation cleanup, original/auto-download settings.
2. Xóa tài khoản: re-auth cần thiết, deletion job retry, revoke sessions/tokens, chặn scheduled jobs, dọn Storage; chốt nội dung người khác được giữ/anonymize.
3. Profile edit/avatar/birthday/bio, theme restore, account switch, backup/restore policy.
4. Video attachment nếu DEC-11 chọn hỗ trợ; nếu không chọn thì chỉnh copy/tab nhất quán và ghi loại trừ.
5. Hoàn thiện TalkBack/font lớn/reduced motion, edge-to-edge/IME, window resize và offline views.
6. Kiểm thử release/R8, signing, environment secrets, cold start, jank, file retention và network/battery.
7. Monitoring/error budget, backup/restore drill, staged rollout và rollback rehearsal.
8. Tài liệu người dùng/README theo Kotlin thực tế, ảnh chụp, danh sách kỹ thuật cập nhật source thật.

Nghiệm thu: Tất cả REQ có bằng chứng test/demo hoặc quyết định loại trừ được ghi rõ. Hai tài khoản không đọc chéo cache. Không còn dịch vụ dùng credential dev trong production.

### Các mốc sản phẩm

| Mốc | Thành phần | Giá trị demo |
|---|---|---|
| M1 — Kết nối | P0–P3 | OTP → hồ sơ → QR/lời mời → Home có người thân |
| M2 — Chat dùng được | Thêm P4/P5 | Text/mood/reply/offline/push/private/block |
| M3 — Bản beta | Thêm P6–P9 | Ảnh/voice/nhóm/thư/mood/search/quiet/digest/kỷ niệm |
| M4 — Đủ phạm vi | Thêm P10–P12 | Audio/video/live location/storage/account và release hardening |

Tổng cộng 13 mốc P0–P12: khoảng **117–178 ngày công** theo các khoảng trên. Thêm 20–30% dự phòng cho tích hợp/lỗi thiết bị: khoảng **141–232 ngày công**. Khi vừa làm vừa học, nên chia tiếp ticket 0,5–2 ngày và hiệu chỉnh ước lượng sau P4; không dùng tổng này như lịch cố định. Công việc có thể chồng lấn về mặt đội nhóm, nhưng một người nên đi theo thứ tự phụ thuộc.

## 9. Các tình huống khó phải có trong thiết kế

### 9.1 Gửi và đồng bộ

- Operation đã tới server nhưng ACK mất: tra cùng ID/receipt rồi upsert; không tạo ID mới.
- Worker và foreground cùng gửi: lease/unique execution local + idempotency server bảo vệ cả hai đầu.
- Timestamp trùng: phân trang theo cặp server-created-at và ID, hoặc server sequence; không sort bằng thời gian thiết bị.
- Realtime tới trước fetch hoặc event lặp: upsert, revision không lùi; reconnect backfill có overlap an toàn.
- Recall/update tin cũ lúc offline: change feed hoặc updated/version cursor, không chỉ fetch tin mới.
- Lệnh queued từ user cũ: account/job generation check trước mọi IO; logout hủy lease/work/subscription tương ứng.
- Group permission thay đổi khi offline: server có quyền cuối, phản hồi lỗi nghiệp vụ; UI cho người dùng sửa/xóa pending item.
- Membership revoked: dừng subscription, xóa cache nhạy cảm theo policy; signed URL đã cấp có khoảng sống hữu hạn.

### 9.2 Thời gian và thông báo

- Các phép tính “sáng mai/tối nay/thức dậy” phải trả thời điểm cụ thể để người dùng kiểm tra trước gửi.
- Server quyết định unlock/expiry; Clock có thể fake trong test; UI đếm ngược không cấp quyền.
- Quiet qua đêm phải tính cả ngày bắt đầu, không chỉ thứ của thời điểm hiện tại.
- Notification delivery có thể lặp hoặc chậm: dedupe theo event ID; không hứa tới đúng giây.
- Notification sound/channel là hành vi chịu ảnh hưởng cài đặt hệ thống; preview trong app chỉ là preview.
- Cron release và quyền đọc đúng giờ là hai việc khác nhau: scheduler phục vụ lifecycle/push, quyền đọc phải có kiểm deadline server.
- Force-stop/OEM battery restrictions/mất mạng nằm trong ma trận test; không dùng vòng lặp nền vô hạn để hứa luôn online.

### 9.3 Tính riêng tư

- Truyền số điện thoại/notes/letters vào debugger log là rò dữ liệu dù chỉ debug; redact trước khi thêm các field thật.
- Block không chỉ ẩn UI: kiểm message, call, push, lookup, invite và live data.
- Media private cần cả table permission lẫn object permission.
- Tìm kiếm và thống kê không được là đường vòng đọc note/thư/private profile.
- Không thể thu hồi những gì người nhận đã nhìn/chụp màn hình; việc recall dọn cache theo khả năng, không quảng bá xóa tuyệt đối.
- Broadcast không lưu bền không có nghĩa miễn phí/không quota; theo dõi connection/message/egress theo môi trường.

## 10. Chiến lược kiểm chứng và chất lượng

### 10.1 Theo tầng

| Tầng | Nội dung |
|---|---|
| Domain unit | Reducer, OTP/session/message/letter/call transitions, quiet time, timezone, permissions, search normalization |
| Repository integration | Local transaction, merge ACK/Realtime, outbox retry/lease, cursor/tombstone, attachment cleanup |
| Database | Migration/reset/replay, FK/unique/constraint, concurrent direct create, letter race, row/column/RPC/storage permissions |
| ViewModel | Initial/loading/error/empty/success, coroutine cancellation, không mất draft khi rotation/process recreation phù hợp |
| Compose UI | Semantics, keyboard/IME, navigation, dialog, long press, mood ngoài màu, font lớn và các state quan trọng |
| Device E2E | Hai/ba user, offline/process death, FCM foreground/background, RTC Wi-Fi↔mobile, location expiry |
| Release | Minified build, signing, crash/jank/startup, bandwidth/battery và môi trường đúng |

Dùng JUnit/coroutines-test/MockK/Konsist và test-utils hiện có. Bổ sung Room migration tests, Compose tests và test SQL/RLS khi tới mốc tương ứng; không mock toàn bộ network rồi kết luận auth/RLS an toàn.

### 10.2 Ma trận quyền bắt buộc

Fixtures: A và B đã kết nối; C là người ngoài; G có admin A, member B; D là thành viên đã rời.

- Chưa login không đọc message/media/private profile.
- A không sửa field riêng của B hoặc gọi RPC giả p_user_id của B.
- B không tự thành admin, thêm mình vào group khác, sửa content bằng quyền pin.
- C biết UUID/path/topic vẫn không đọc được dữ liệu.
- D không còn nhận event/tọa độ mới theo chính sách membership.
- Người share mood không đọc note; người nhận thư chưa hẹn không đọc payload từ bất cứ endpoint nào.
- Call recipient/device claim, letter open và cursor update không giả actor được.

### 10.3 Tiêu chí mục tiêu đề xuất

Các số sau là mục tiêu kiểm thử để đo và điều chỉnh ở P4, không phải SLA đã đạt:

- Hiện pending message sau bấm gửi dưới 150 ms trên thiết bị tham chiếu; không chờ upload/network.
- Đọc trang chat từ cache dưới 300 ms với dữ liệu test 10.000 message/conversation.
- 100 chu kỳ offline → retry → process restart không có duplicate logical message hoặc mất queued operation trong kịch bản test.
- Không có request/payload/log trái policy trong ma trận A/B/C/D.
- Mỗi network fetch có page size/timeout/cancellation; media có kích thước và quota được chốt, không load mọi ảnh để đo dung lượng.
- Animation constellation/orb không làm timeline recompose liên tục; đo trên thiết bị tầm trung với font lớn.

## 11. Hạ tầng, môi trường và vận hành

| Thành phần | Cần chuẩn bị | Rủi ro/điều kiện |
|---|---|---|
| Supabase | Local/dev/staging/prod, migrations, Auth/DB/Storage/Realtime/Functions/Cron, backup | Không thao tác production từ script dev; test restore |
| Phone OTP | SMS provider, sender/rate limit/test numbers, ngân sách | Phí SMS/abuse và khả năng nhận tại Việt Nam phải kiểm thực tế |
| FCM Android | App registration, config đúng package, credential backend HTTP v1 | Không nhúng private credential vào APK; token rotation/device cleanup |
| Domain/App Links | Domain thuộc quyền kiểm soát, HTTPS, assetlinks.json, signing fingerprint | Cold-start link, login gate và domain verification |
| RTC/TURN | Provider hoặc tự vận hành, ephemeral credentials, region/limits | TURN relay bandwidth và vận hành có chi phí; đo trước chọn |
| Maps/location | Provider, hạn chế API key, billing/quota, attribution | Không chốt vendor khi chưa test Android SDK và ngân sách |
| CI/release | Java/Gradle tương thích repo, cache, secrets, signing và artifact | Kiểm targetSdk/policy theo thời điểm phát hành; không mặc định catalog đang thỏa mọi yêu cầu store |
| Observability | Event ID, sync backlog, notification/call failure, scheduler lag | Không lưu body chat/OTP/private notes; redaction theo mặc định |

Chỉ metrics cần thiết cho vận hành: queue age, retry count, sync gap, upload fail rate, push delivery attempt, letter release lag, call setup/relay usage, location session expiry, crash/ANR. Chọn crash reporting riêng khi cần; chưa có yêu cầu phải chuyển app sang Firebase database/auth.

## 12. Rủi ro lớn và kế hoạch giảm thiểu

| Rủi ro | Hệ quả | Cách giảm thiểu |
|---|---|---|
| Coi HTML là spec backend hoàn chỉnh | Làm UI đẹp nhưng dữ liệu/quyền sai | Trace REQ và chốt DEC trước từng mốc |
| RLS/RPC/Storage quá rộng trong SQL mẫu | Lộ dữ liệu hoặc leo quyền | P1 matrix tests, column allowlist, actor từ JWT |
| Offline retry không idempotent | Trùng/mất tin | Làm outbox/merge/reconciliation ở P4 trước media |
| Hứa niêm phong mật mã nhưng chỉ che UI/RLS | Sai cam kết sản phẩm | Chốt threat model; khóa metadata/payload và sửa copy phù hợp |
| Tự dựng RTC kéo dài | Chậm toàn bộ app | Spike audio/TURN riêng, adapter có thể đổi engine |
| Cuộc gọi/vị trí bị Android hạn chế nền | Gọi lỡ/chia sẻ ngắt | Thiết kế service/permission sớm, test thiết bị thật |
| Shared base chứa mọi logic | Module vòng và khó học | Ownership core/feature rõ, Konsist kiểm chiều phụ thuộc |
| Scope tăng lúc vừa học | Không có bản chạy được | Khóa M1/M2; mỗi ticket có demo nhỏ và bài học T tương ứng |
| Dọn cache/xóa account sai | Mất queued media hoặc dữ liệu vượt phạm vi | Phân biệt local cache/remote ownership và job dọn có reference check |

## 13. Backlog bắt đầu ngay

Làm theo thứ tự này khi bắt đầu triển khai:

1. [ ] P0-01: Chốt consent kết nối và thư niêm phong; ghi DEC-03/04/05.
2. [ ] P0-02: Ghi baseline build/check và map component HTML → Compose đã có/chưa có.
3. [ ] P1-01: Thiết kế private/public profile và SQL grant/RLS/RPC tests A/B/C.
4. [ ] P1-02: Dựng migrations dev + account-scoped Room + domain errors/Clock.
5. [ ] P2-01: SessionState và navigation gate thay hard-code onboarding.
6. [ ] P2-02: Phone/OTP/profile bootstrap chạy thật.
7. [ ] P3-01: QR/lời mời/accept/get-or-create direct.
8. [ ] P4-01: Một text message đi trọn local outbox → server → máy thứ hai.
9. [ ] P4-02: Timeout/restart/reconnect/recall cũ trước mở rộng UI.
10. [ ] P5-01: Push riêng tư, quiet và block.
11. [ ] Sau mỗi ticket: cập nhật REQ, kiểm chứng và mục T đã học trong file kỹ thuật.

Mẫu ticket:

~~~text
ID: P4-01
Nguồn: REQ-11/41; feature_app phân hệ 3/8
Điều kiện: A/B đã xác thực, là thành viên direct conversation
Thay đổi: Room transaction + Outbox + send_message idempotent + reducer pending/synced
Happy path: A gửi “Thương”, B nhận đúng một tin
Lỗi: timeout sau commit; process chết; user logout trong lúc gửi
Given: server đã commit, ACK bị mất
When: cùng operation được retry
Then: server và hai client vẫn có một message cùng ID
Kỹ thuật: T08/T09/T10/T11
Bằng chứng hoàn thành: test + video hai thiết bị + link code + migration
~~~

## 14. Tài liệu chính thức đã đối chiếu

Các lựa chọn tổ chức module, state machine và mốc triển khai là đề xuất cho repo này. Những giới hạn nền tảng dưới đây đã được đối chiếu tài liệu chính thức:

- Worker dành cho công việc bền và có thể lên lịch linh hoạt; không dùng làm đồng hồ mở thư đúng giây hay media loop. [Android task scheduling](https://developer.android.com/develop/background-work/background-tasks/persistent).
- Private Broadcast/Presence cần authorization riêng theo channel; table RLS không tự bảo vệ mọi topic. [Supabase Realtime authorization](https://supabase.com/docs/guides/realtime/authorization).
- Mic/location/phone-call service có loại và điều kiện nền tảng tương ứng; triển khai phải kiểm theo API/target thực tế. [Android foreground service types](https://developer.android.com/about/versions/14/changes/fgs-types-required).
- FCM behavior khác giữa foreground/background và có giới hạn xử lý; client cần fetch bù. [FCM receiving messages](https://firebase.google.com/docs/cloud-messaging/android/receive-messages).
- Notification channel do người dùng/OS kiểm soát hành vi hiển thị và âm thanh. [Android notifications](https://developer.android.com/design/ui/mobile/guides/home-screen/notifications).
- Media call dùng ICE/STUN/TURN để thiết lập đường truyền; signaling riêng không vận chuyển audio/video. [WebRTC peer connections](https://webrtc.org/getting-started/peer-connections).
