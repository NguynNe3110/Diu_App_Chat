# Dịu — Kế hoạch nghiên cứu và tích hợp công nghệ

Ngày lập: 21/11/2026. Trạng thái: **ĐỀ XUẤT; chưa triển khai hoặc xác nhận dịch vụ đang hoạt động**.

Đọc cùng [kế hoạch dọn lỗi](DIU_BUG_CLEANUP_PLAN.md), [roadmap P0–P12](stage1/DIU_IMPLEMENTATION_PLAN.md), [bản đồ kỹ thuật](stage1/DIU_TECHNIQUES_LEARNING_MAP.md). Các ID I01–I28 dưới đây dùng cho backlog tích hợp; ID T giữ nguyên theo learning map.

## 1. Mục tiêu và cách sử dụng

Tài liệu trả lời: app cần tích hợp gì, phần nào đã có nền tảng, cần nghiên cứu gì, cấu hình ở đâu, làm theo thứ tự nào và kiểm chứng ra sao. Không đồng nghĩa cần mua 28 dịch vụ: nhiều hạng mục là API Android hoặc logic đồng bộ của chính ứng dụng.

Stack kế thừa: Kotlin/Compose, Koin, coroutines/Flow, Room và Supabase. Chưa có lý do trong khảo sát này để đổi Hilt, Navigation 3, Firestore hoặc thêm AI vào sản phẩm. Nếu chỉ dùng FCM thì không cần chuyển auth/database sang Firebase.

Bằng chứng: đọc tĩnh source trong workspace và tài liệu chính thức; chưa kiểm tra backend dashboard, migration thực deploy, thiết bị hay tài khoản dịch vụ. Source và DI có thể thay đổi trong lúc làm; kiểm tra lại trước mỗi ticket. Không đánh dấu hoàn thành chỉ vì đã thêm dependency hay có lớp tên Adapter.

Trạng thái mỗi hạng mục phải ghi riêng: CHƯA KHẢO SÁT → ĐANG NGHIÊN CỨU → THỬ NGHIỆM → ĐÃ NỐI STAGING → ĐÃ NGHIỆM THU. “Đã có code nền/mẫu” không phải một trạng thái nghiệm thu.

### 1.1 Bổ sung theo yêu cầu: Google + username và bốn cách kết nối

**Đã được người dùng yêu cầu:** ưu tiên đăng nhập nhanh bằng Google để ra luồng dùng thật sớm mà không cần gửi SMS; sau đăng nhập lần đầu bắt buộc chọn username. Hỗ trợ tìm nhau qua username, số điện thoại, mã mời hoặc QR. SMS OTP vẫn sẽ tích hợp sau, không phải điều kiện để dùng bản Google-first.

Luồng mục tiêu: Mở app → Google Sign-In → Supabase session → kiểm tra profile → chọn username nếu thiếu → Home. User cũ đã có username vào Home sau khi restore/xác minh phiên. Username là tên định danh tìm kiếm, không phải mật khẩu hoặc bằng chứng đăng nhập. ID nội bộ vẫn là Supabase user UUID ổn định; tên hiển thị có thể trùng nhau.

Các quy tắc sau là **đề xuất triển khai**, cần chốt trước migration:

- Username: 3–30 ký tự ASCII chữ thường, số và dấu gạch dưới; trim, chuẩn hóa lowercase, có thể bỏ một ký tự @ đầu vào ở ô tìm kiếm. Ví dụ minh_an. Giữ display name tiếng Việt riêng; không tự suy username từ Gmail.
- Backend là nơi quyết định uniqueness bằng constraint/index canonical; RLS/RPC chỉ cho chủ profile claim tên. Có danh sách reserved names; chính sách đổi/reuse username phải chốt, bản đầu đề xuất giữ cố định sau claim.
- Tra cứu username dạng chính xác trước để giảm phạm vi; tìm gần đúng là tùy chọn sau. Kết quả chỉ gồm ID/profile projection cho phép xem; có rate limit, block và lựa chọn quyền được tìm thấy.
- Số điện thoại không bắt buộc cho Google onboarding. Google Sign-In không tự xác minh quyền sở hữu số tự nhập. Trước khi có OTP, đề xuất cho thêm số như thông tin liên hệ tự khai, có nhãn chưa xác minh và tùy chọn cho phép tìm; không dùng để login, recovery, tự merge hay chứng minh một người.
- Lookup số tự khai chỉ trả ứng viên được phép hiển thị; có thể trùng nên không áp uniqueness tin cậy hoặc tự kết nối. Người dùng xác nhận qua username/profile và lời mời. Chỉ nâng thành số đã xác minh khi có bằng chứng server; SMS triển khai sau sẽ xử lý verify/link.
- Mã mời: token riêng do backend tạo, khó đoán, unique; nhập trực tiếp để lookup. QR mã hóa cùng token/đường dẫn, không chứa dữ liệu riêng tư. Quét hoặc nhập mã đều tới cùng safe lookup → profile → gửi lời mời.
- Bốn đầu vào chỉ là cách tìm người; không tự chấp nhận quan hệ, thêm thành viên hoặc bỏ qua block/quyền.
- Mã mời và QR cần dùng được sớm trong G2; domain/App Links đầy đủ thuộc G8. Khi chưa có domain, có thể encode payload token có version và parse trong app; chưa có web fallback.
- Giữ Google và phone như các phương thức của cùng hệ thống Supabase Auth; không thêm Firebase Auth chỉ để đăng nhập Google.

**Nghiệm thu:** không có số điện thoại vẫn Google → username → tìm/kết nối/chat được; username trùng bị server từ chối; chưa hoàn tất username không bypass bằng back/deep link/restart; bốn cách lookup tuân theo quyền, số chưa xác minh không được trình bày như đã xác minh. Phần phone lookup tự khai là đề xuất có giới hạn, không phải xác thực số miễn phí.

## 2. Những quyết định cần chốt

| ID | Quyết định | Ảnh hưởng |
|---|---|---|
| DEC-03 | Quan hệ vòng thân cần người nhận đồng ý; circle/nickname thuộc ai? | Lời mời, RLS, direct conversation |
| DEC-04/05 | Người gửi có được xem/sửa thư trước hẹn? Niêm phong bằng quyền server hay mật mã nâng cao? | Payload, RPC, scheduler và lời hứa bảo mật |
| DEC-07 | Giới hạn nhóm, file, độ dài voice; quy tắc admin cuối | Constraint, quota, UX |
| DEC-11 | Có gửi/lưu video media không? Tách biệt với video call | Schema và pipeline upload; không tự suy từ bộ lọc UI |
| DEC-16 | Có quyền sở hữu domain nào và hosting nào? | Verified App Links và QR/link mời |
| INT-DEC-01 | SMS provider phù hợp Việt Nam; thử khả năng gửi, hạn mức và chi phí | OTP giai đoạn sau, không chặn Google-first |
| INT-DEC-02 | WebRTC tự vận hành + TURN hay RTC managed? | Vận hành call, SDK, signaling, chi phí và lock-in |
| INT-DEC-03 | Map/location provider; live location có chạy nền không? | SDK/key/quota, service, quyền, pin |
| INT-DEC-04 | Nhiều thiết bị, xóa tài khoản và thời hạn lưu dữ liệu | Token/session, account partition, deletion jobs |

Các quyết định này đang chờ chốt theo từng feature; không chặn việc làm baseline, auth và chat tối thiểu. Phương án đề xuất trong roadmap cũ không tự trở thành quyết định đã được người dùng phê duyệt.

## 3. Bản đồ toàn bộ hạng mục

Ký hiệu: **DV** dịch vụ/backend; **OS** API Android; **APP** cơ chế tự xây. “Có nền” chỉ phản ánh khảo sát, không chứng minh tích hợp chạy thật.

| ID | Hạng mục | Loại / thành phần cần có | Hiện trạng đã quan sát / cần xác minh | P / T liên quan |
|---|---|---|---|---|
| I01 | Gửi/xác minh OTP — sau Google | DV: Supabase Phone Auth + SMS provider | Có lời gọi Auth nhưng fallback sai; tạm không bật production khi chưa sửa/verify | P2 / T14 |
| I02 | Tự điền OTP, tùy chọn | OS: SMS Retriever hoặc User Consent | Chưa xác minh đã tích hợp; nhập tay vẫn phải hoạt động | P2 / T14 |
| I03 | Session và scope tài khoản | APP + Supabase Auth session | Chưa nghiệm thu restore/refresh/logout; auth DI cho phép client null | P2 / T05,T28 |
| I04 | Database, API, quyền | DV: PostgreSQL, PostgREST/RPC, RLS, migration | Có SQL/migrations; chưa xác minh môi trường deploy | P1 / T15,T16,T31 |
| I05 | Danh bạ, lời mời, home | DV + APP: lookup username/phone/mã mời/QR, accept/direct chat/summary | Contacts dùng mẫu; bổ sung safe lookup chung và quy tắc số chưa xác minh | P3 / T15,T30 |
| I06 | Local persistence | APP: Room, DataStore cho cấu hình phù hợp | Chat có Room; nhiều repository khác dùng StateFlow mẫu | P4 / T08,T28 |
| I07 | Outbox và retry | APP + OS: transaction, coordinator, WorkManager | Có entity outbox nhưng gửi tự SENT; chưa chứng minh durable dispatch | P4 / T09,T10,T11 |
| I08 | Đồng bộ realtime bền vững | DV + APP: change stream, fetch bù, cursor/tombstone | Có cấu hình SDK; chưa chứng minh sync đầu cuối | P4 / T12,T13 |
| I09 | Typing/presence | DV: private Broadcast/Presence + APP TTL | Chưa nghiệm thu channel quyền và vòng đời | P4 / T17 |
| I10 | Push và token thiết bị | DV + OS: FCM, đăng ký token, backend sender | Chưa xác minh pipeline FCM end-to-end | P5 / T19 |
| I11 | Quiet/privacy/digest | DV + APP: jobs + renderer/channels | Có model/rule settings; chưa chứng minh OS notification và delivery job | P5 / T18,T19 |
| I12 | Upload/download riêng tư | DV: Supabase Storage + metadata/lifecycle | SDK có; gửi photo local chưa chứng minh upload thật | P6 / T21,T32 |
| I13 | Chọn/chụp ảnh | OS: Photo Picker, camera intent hoặc CameraX | Cần kiểm entry point, URI, temp file, quyền; không chỉ preview | P6 / T21 |
| I14 | Recorder/player | OS + APP: recorder/player, audio focus | Có code media/voice; chưa kiểm thiết bị và interruption | P6 / T20 |
| I15 | Kho media/cache | APP + DV: file index, policy download, query media | Repository có dữ liệu/số byte mẫu | P6/P12 / T21,T32 |
| I16 | Nhóm và thao tác chat | DV + APP: member/role/reaction/pin/forward/recall/receipt | Có logic local trong chat; chưa nghiệm thu quyền server | P7 / T10,T15,T16 |
| I17 | Mood/journal | DV + APP: check-in, note riêng tư, allowlist | Repository dùng seed/StateFlow; SQL quyền cần test deploy | P9 / T30 |
| I18 | Search/starred | DV + APP: search có index + cache query | Search mẫu có; chưa xác nhận dữ liệu thật và quyền | P9 / T13,T22 |
| I19 | Thư/gửi chậm | DV: RPC, server clock, scheduler, payload gate | Repository thư dùng RAM và payload mẫu | P8 / T14,T15,T18 |
| I20 | QR và link mời | OS + DV: encode/decode, token lookup, domain/App Links | Có QR/link mẫu; domain và routing thật cần xác minh | P3 / T23,T28 |
| I21 | Audio/video transport | DV + APP: RTC engine + signaling + ICE/STUN/TURN | Signaling adapter hiện là mô phỏng cục bộ | P10 / T24 |
| I22 | Vòng đời call Android | OS + APP: Telecom phù hợp, service, notification, push | Có coordinator; cần kiểm OS registration và tài nguyên thật | P10 / T25 |
| I23 | Vị trí tĩnh/bản đồ | OS + DV: location provider, map, geocoding nếu cần | Default adapter trả tọa độ cố định | P11 / T26 |
| I24 | Live location | OS + DV + APP: updates, session expiry, private transport | Chưa nghiệm thu tracking/session thực | P11 / T17,T26 |
| I25 | Profile/block/account lifecycle | DV + APP: update/block/logout/delete + cleanup | Settings dùng RAM; logout rỗng, delete chưa phải backend | P2/P5/P12 / T16,T32 |
| I26 | Môi trường và vận hành | CI + crash/metrics + migration/backup | Có build logic/tests/Timber; chưa kiểm release gates/restore | P1/P12 / T01,T02,T29,T31 |
| I27 | Google Sign-In — ưu tiên | OS + DV: Credential Manager + Google provider trên Supabase Auth | Yêu cầu mới, chưa triển khai/kiểm chứng trong tác vụ này | P2 / T05,T28 |
| I28 | Username bắt buộc và identity lookup | DV + APP: claim username unique, profile guard, canonical lookup | Yêu cầu mới, cần migration/API/UI; mã mời/QR liên quan I05/I20 | P2/P3 / T15,T16,T28 |

Đường dẫn source và bằng chứng cụ thể của các khoảng trống nằm trong [bảng B01–B11](DIU_BUG_CLEANUP_PLAN.md#2-hiện-trạng-cần-xử-lý-trước). Phần “chưa xác minh” cần khảo sát tiếp, không phải khẳng định hoàn toàn không có implementation.

## 4. Lộ trình nghiên cứu và thử nghiệm

Mỗi gói phải tạo: ghi chú nghiên cứu ngắn, hợp đồng dữ liệu/API, prototype staging, ca kiểm thử lỗi và quyết định tích hợp. Prototype chỉ là thử nghiệm; sau đó mới nối luồng sản phẩm.

### G0 — Môi trường và hợp đồng nền (I04, I26)

- Nghiên cứu: migration versioning, PostgreSQL constraint/transaction, RPC và RLS; cấu hình môi trường của project.
- Chuẩn bị: Supabase dev/staging, tài khoản thử A/B/C, schema version; xác minh migrations 00001–00006 thực chạy được và trạng thái deploy, không chạy lại file gốc mù quáng.
- Thực hiện: đối chiếu các mục DB trong roadmap với SQL hiện tại; API cần có input/output/error/idempotency rõ.
- Ranh giới: app dùng publishable/anon config theo SDK và JWT user; service-role/FCM server credential chỉ ở backend.
- Bài thử: A/B query được đúng dữ liệu, C và anonymous bị từ chối; rollback/retry không phá invariant.
- Đầu ra: bảng API + ma trận quyền + hướng dẫn staging tái lập. Chưa có backend này thì chưa đánh dấu gói mạng sau đã tích hợp.

### G1 — Google trước, username bắt buộc; SMS OTP bổ sung sau (I27–I28, I03, I25; I01–I02 giai đoạn sau)

**G1a — Ưu tiên triển khai ngay:**

- Nghiên cứu: Credential Manager Sign in with Google; Supabase Google provider và đăng nhập bằng Google ID token; session/profile guard.
- Chuẩn bị: Google Cloud OAuth consent/branding, test users khi cần; OAuth Android client theo package + signing certificate debug/release, Web client ID dùng đúng audience; bật Google provider trong Supabase. Client secret chỉ cấu hình ở backend/provider, không đưa vào APK.
- Luồng: chọn Google account → Credential Manager trả ID token → Supabase xác minh và cấp session → đọc profile → NeedsUsername hoặc Ready. Đối chiếu yêu cầu nonce, issuer/audience và API đúng phiên bản khi triển khai; không coi decode token phía client là xác thực.
- Nếu chưa có username hợp lệ, bắt buộc hoàn tất username trước Home/Chat. Nếu thoát hoặc mất mạng ở bước này, giữ trạng thái chưa hoàn tất; mở lại tiếp tục bước chọn username. Deep link/QR đến lúc này phải chờ hoàn tất profile.
- Username claim qua backend có unique constraint trên dạng canonical, xử lý hai người chọn cùng lúc bằng transaction/constraint; UI check availability chỉ để hỗ trợ.
- Hủy chọn tài khoản, không có credential, token bị từ chối, profile load lỗi phải có trạng thái riêng; không fallback thành success hoặc gọi SMS tự động.
- Bài thử: Google lần đầu → username → vào app; đăng nhập lại không hỏi lại username; cancel/token sai; hai máy claim cùng username; kill process giữa auth và profile; logout A → Google B không lẫn dữ liệu.
- Đầu ra M1: A/B đăng nhập bằng Google, chọn username duy nhất và kết nối mà không phát sinh SMS do luồng này.

**G1b — Giữ trong roadmap, không chặn M1:**

- I01: Phone OTP thật, provider SMS phù hợp Việt Nam, E.164, resend/cooldown, hết hạn và rate limit; I02 tự điền là tùy chọn.
- SMS chưa cấu hình thì ẩn/khóa luồng chưa hỗ trợ; bỏ fallback xác thực giả trước khi bật cho người dùng.
- Khi thêm số cho user Google: xác minh quyền sở hữu trước khi dùng làm định danh tin cậy, đăng nhập hoặc recovery; gắn vào cùng Supabase user theo khả năng linking đã kiểm chứng.
- Không tự merge tài khoản từ username, số tự khai hoặc email phía client. Có xung đột identity phải có quy trình linking/re-auth và xử lý dữ liệu rõ.
- Bài thử: mã sai/hết hạn không cấp phiên; xác minh thêm số không tạo tài khoản thứ hai ngoài ý muốn; logout/refresh giống luồng Google.

### G2 — Kết nối và dữ liệu local theo tài khoản (I05–I06)

- Nghiên cứu: invitation state machine, canonical pair direct conversation, data ownership; Room Flow/transaction/migration, DataStore.
- Backend: lookup có kiểm soát theo username, số điện thoại, mã mời và token QR; chung projection an toàn, request/accept/decline, block và quyền tạo direct. Không trả phone/email riêng tư; số chưa xác minh chỉ là ứng viên tự khai theo mục 1.1.
- Làm mã mời và QR token tối thiểu ngay G2 cùng I20; không chờ domain. User không có phone vẫn kết nối được bằng username/mã/QR.
- App: danh bạ/home đọc dữ liệu thật và cache hợp lý; các feature dùng định danh nhất quán. Không bắt buộc tách nhiều module core mới nếu package hiện tại đủ.
- Bài thử: accept đồng thời, request trùng, người bị block, restart, đổi tài khoản; Home và Chat cùng phản ánh một hội thoại.
- Đầu ra: A kết nối B qua nghiệp vụ thật; C không truy cập trái phép.

### G3 — Text chat, outbox và đồng bộ (I07–I09, phần chat của I04/I06)

- Nghiên cứu: optimistic UI, transactional outbox, idempotency key, reconciliation, server order, retry/backoff, WorkManager constraints.
- Luồng ghi đề xuất: UI → enqueue message/outbox cùng transaction → coordinator/worker gửi → ACK → upsert Room.
- Luồng nhận: change event → fetch/validate khi cần → upsert Room → Flow UI; UI không tự ghép hai nguồn danh sách độc lập.
- I08: chọn cơ chế database change notification phù hợp (Postgres Changes hoặc database-triggered Broadcast), kèm keyset/cursor và catch-up sau reconnect; không coi WebSocket là lịch sử bền.
- I09: private Broadcast cho typing, TTL/throttle và unsubscribe; presence chỉ là trạng thái ngắn hạn.
- Bài thử: mất mạng trước gửi, sau server commit trước ACK, duplicate event, kill process, reconnect sau edit/recall, rời nhóm khi đang subscribe.
- Đầu ra: A/B nhắn text thật; retry một operation không tạo trùng; queue còn sau restart; C không vào channel/truy vấn được.

### G4 — Push, quiet hours và riêng tư (I10–I11)

- Nghiên cứu: FCM token rotation, FirebaseMessagingService, notification/data payload behavior, Android notification permission/channels, routing khi process chưa có.
- Chuẩn bị: Firebase project/app registration, client config; sender credential ở backend; bảng thiết bị/token gắn user và cleanup khi logout.
- Backend: tạo delivery job/outbox từ nghiệp vụ đã commit; áp dụng block, quiet schedule/timezone, privacy và digest; retry không báo trùng.
- App: renderer theo ba mức riêng tư; tap mở bằng ID và kiểm lại quyền/session. Đánh giá payload để nội dung không tự hiện vượt privacy qua OS handling.
- Bài thử: foreground/background/process chết, token đổi, nhiều thiết bị, deny permission, logout/login, tap hai lần, lịch qua đêm.
- Phân biệt process bị hệ thống thu hồi với force-stop; không hứa delivery tức thời tuyệt đối.
- Đầu ra: tin thật tạo thông báo đúng privacy và route; không chỉ gửi thử thành công từ console.

### G5 — Ảnh, voice và cache (I12–I15)

- Nghiên cứu: Storage RLS/private bucket/signed URL; attachment lifecycle; URI grant, Photo Picker/camera; recorder/player và audio focus.
- Chuẩn bị: bucket/path convention, MIME/size/voice duration limits, metadata owner/conversation, cleanup policy.
- Chọn camera intent nếu đủ nhu cầu chụp; chỉ dùng CameraX khi cần camera trong app. Không thêm SDK vì tên feature.
- Pipeline: local file bền → upload có retry → metadata/message commit → ACK; xử lý file upload thành công nhưng message thất bại.
- Recorder/player: chỉ một chủ sở hữu, stop/release khi cancel/error; xử lý cuộc gọi đến, tai nghe, background và quyền mic bị thu hồi.
- Cache: file index thật, dung lượng thật, xóa không đụng pending attachment hoặc file đang dùng; policy Wi-Fi/cellular được thực thi.
- Bài thử: hết dung lượng, URI hết quyền, signed URL hết hạn, upload dở rồi restart, C tải file trái quyền, interruption khi ghi/phát.
- Đầu ra: B mở ảnh/nghe voice từ máy A; dọn cache làm giảm byte trên thiết bị và không xóa dữ liệu server.

### G6 — Nhóm, mood, search và settings (I16–I18, phần settings/block I25)

- Nghiên cứu: atomic RPC và role constraints; privacy projection, server query filtering, FTS/index và tìm tiếng Việt.
- Nhóm: create/invite/remove/leave/role, admin cuối; reaction/pin/forward/recall idempotent; receipt tách khỏi trạng thái gửi.
- Mood: note luôn riêng tư theo quyết định sản phẩm; share/revoke áp dụng ở API, timezone và month/year đúng.
- Search: phạm vi user/conversation, keyset paging, normalize có/không dấu; starred và jump tới tin thật; recall cập nhật index/cache.
- Settings: profile, quiet, read receipt/privacy được lưu đúng nơi; block enforcement phía server, không chỉ ẩn UI.
- Bài thử: C query trực tiếp, admin rời đồng thời, quyền bị revoke khi màn đang mở; check-in cùng ngày khác tháng/năm.
- Đầu ra: dữ liệu thật qua restart/thiết bị khác theo hợp đồng; aggregate không lộ dữ liệu ngoài quyền.

### G7 — Thư hẹn và gửi chậm (I19)

- Dependency: chốt DEC-04/05 trước payload/API.
- Nghiên cứu: server clock, timezone, scheduler, transaction release/cancel/open, job idempotency.
- Backend: metadata tách payload; chỉ cung cấp nội dung khi đúng quyền và deadline; thiết kế index/job cùng retry, không dựa vào đồng hồ máy.
- App: state machine draft/scheduled/released/opened/cancelled; hiển thị server result; không tải sẵn payload người nhận rồi che bằng UI.
- Bài thử: đổi giờ máy trước một năm; gọi API trực tiếp trước hạn; job chạy hai lần; cancel và release đồng thời.
- Đầu ra: mở đúng hạn kể cả hai máy offline lúc phát hành và online lại sau đó.
- RLS khóa đọc theo giờ không đồng nghĩa E2EE; nếu chọn mật mã nâng cao phải lập thiết kế khóa/recovery riêng trước triển khai.

### G8 — QR, domain và App Links (I20)

- Nghiên cứu: encoder/decoder QR, camera/ảnh thư viện; Verified App Links, assetlinks.json và signing certificate; navigation sau login.
- Chuẩn bị: domain sở hữu thật, HTTPS hosting và token lookup; không mặc định địa chỉ diu.vn trong mockup đã thuộc dự án.
- App/backend: lấy invite token đúng user, xử lý token không hợp lệ, link parse an toàn; pending intent được tiếp tục sau auth.
- Bài thử: cài/chưa cài app, login/chưa login, link lỗi, scan ảnh, share/save, release signing.
- Đầu ra: QR và link dẫn đúng profile và yêu cầu kết nối. Trải nghiệm khi chưa cài app cần web fallback; deferred deep link sau cài là quyết định riêng.

### G9 — Gọi thoại rồi video (I21–I22)

- Dependency: session, quyền thành viên, push và lựa chọn RTC.
- Nghiên cứu: RTC engine, SDP/ICE, STUN/TURN hoặc SDK managed; private signaling, call state machine; Telecom/Core-Telecom, foreground service, audio routing.
- Chuẩn bị: TURN/service account và credential có thời hạn cấp từ backend nếu tự WebRTC; không nhúng secret vận hành vào APK.
- App/backend: invite/accept/reject/busy/timeout/end có trạng thái bền; OS notification/service theo API target; camera/mic/peer connection được release đúng.
- Bài thử: hai mạng khác nhau, bắt buộc relay TURN, app nền/khóa màn hình, cuộc gọi hệ thống xen vào, mạng đổi, tai nghe, permission denied.
- Nghiệm thu audio trước; sau đó mới video renderer/camera switch/video enable-disable.
- Đầu ra: hai thiết bị truyền media thật và kết thúc giải phóng tài nguyên; Flow signaling cục bộ không tính là nghiệm thu.

### G10 — Vị trí tĩnh rồi live (I23–I24)

- Nghiên cứu: location provider thật, approximate/precise, stale fix; map SDK và geocoding; updates/sampling, expiry và quyền nền nếu cần.
- Chuẩn bị: chọn provider/key restrictions/quota/attribution; chốt chia sẻ khi background.
- Backend: session có expires_at theo server, người nhận được phép, stop/revoke; lưu vị trí theo retention đã chốt.
- App: static fix có accuracy/timestamp; live cập nhật theo mức di chuyển, hiển thị stale rõ, dừng khi hết hạn/thu hồi.
- Bài thử: GPS tắt, quyền approximate/deny/revoke, background, stop/offline, hết hạn và đổi giờ máy, C subscribe trái phép.
- Đầu ra: tọa độ thật, hiển thị bản đồ thật, dừng updates và phát tọa độ khi phiên hết hiệu lực.

### G11 — Dữ liệu tài khoản và vận hành release (phần còn lại I25–I26)

- Nghiên cứu: deletion workflow, reauthentication, retry jobs, token/device cleanup, retention; CI và observability.
- Backend: xóa/ẩn danh theo chính sách đã chốt; xử lý Auth, Storage, quan hệ và dữ liệu dùng chung; có trạng thái theo dõi, không chỉ đổi tên profile.
- App: logout và account switch dọn scope; xóa cache theo quyền sở hữu; không log OTP/token/chat/tọa độ.
- User Google không có số điện thoại vẫn xóa tài khoản được qua xác nhận/reauth phù hợp provider; không bắt nhập phone để xóa. Kiểm tra clear Credential Manager state theo hướng dẫn SDK khi logout, không đăng xuất toàn bộ Google account của thiết bị.
- Vận hành: unit/architecture/integration checks, staging release gates, crash reporting, queue age/sync gap/upload fail metrics; backup và thử restore.
- Bài thử: deletion lỗi giữa chừng và retry, phiên cũ sau logout/delete, migration nâng cấp, restore môi trường thử.
- Đầu ra: release checklist có bằng chứng; các giới hạn và known issues được công bố.

## 5. Mốc sản phẩm và thứ tự ưu tiên

| Mốc | Gói cần có | Demo bắt buộc |
|---|---|---|
| M1 — Người dùng thật, không cần SMS | G0–G2 + mã mời/QR tối thiểu | A/B Google → username duy nhất; kết nối qua username/mã/QR; lookup phone theo quyền nếu có số tự khai |
| M2 — Nhắn tin đáng tin | G3 | A gửi B nhận; offline/restart/reconnect không mất/trùng |
| M3 — Dùng được hằng ngày | G4–G5 | Push đúng riêng tư, ảnh/voice thật và cache thật |
| M4 — Nghiệp vụ đầy đủ | G6–G8 | Nhóm/mood/search/settings/thư/QR theo đúng quyền |
| M5 — Realtime media và vị trí | G9–G10 | Call qua hai mạng; location thật và hết hạn đúng |
| M6 — Phát hành | G11 và hồi quy D6 | Account deletion, vận hành, release gates có kiểm chứng |

G8 có thể làm sớm sau G2 khi có domain; các việc độc lập không cần chờ theo số thứ tự. G0/G11 có phần công việc xuyên suốt, không dồn mọi kiểm thử/vận hành tới cuối.

## 6. Dịch vụ, tài khoản và cấu hình cần chuẩn bị

| Thành phần | Cần chuẩn bị | Khi nào |
|---|---|---|
| Supabase | Dev/staging/project config, migration, RLS, RPC, Storage, Realtime | G0 trở đi |
| Google Sign-In | Google Cloud OAuth Android/Web clients, signing cert, consent/test users, Supabase Google provider | G1a — ưu tiên |
| SMS | Provider, sender/template, số thử, giới hạn quốc gia/chi phí | G1b — bổ sung sau, không chặn M1 |
| Firebase FCM | Android app config; backend sender credential và token registry | G4 |
| Backend jobs | Edge Functions hoặc backend tương đương, scheduler/queue, retry và secrets | G4/G7/G11 |
| Domain/hosting | DNS/HTTPS, assetlinks.json, web fallback | G8 |
| RTC | TURN relay + signaling tự xây hoặc dịch vụ RTC managed | G9 |
| Maps/location | Provider bản đồ/location phù hợp; key/quota nếu yêu cầu | G10 |
| Vận hành | CI, crash reporting phù hợp, metrics, backup/restore | G0 và xuyên suốt |

Không cần APNs cho Android-only; chỉ bổ sung khi có iOS. Digest trong roadmap là tổng hợp xác định, chưa cần LLM. Không tự mua dịch vụ/cấu hình production trong tác vụ lập kế hoạch này.

## 7. Mẫu ghi chép khi bạn nghiên cứu một hạng mục

1. ID Ixx, mục tiêu và feature sử dụng.
2. API/SDK và phiên bản phù hợp catalog/toolchain; nguồn chính thức và ngày đọc.
3. Source/DI hiện có; đoạn nào demo, đoạn nào thực chạy.
4. Cấu hình phía app/backend/provider; không chép giá trị secret.
5. Hợp đồng input/output, error, quyền, ID và thời gian.
6. Vòng đời: khởi tạo, owner, background, cancel, cleanup.
7. Retry/idempotency, offline, reconnect và dữ liệu cần lưu bền.
8. Prototype nhỏ nhất; kịch bản thành công và thất bại; bằng chứng thực chạy.
9. Chi phí/quota/lock-in và quyết định chọn; giới hạn chưa kiểm chứng.
10. Ticket triển khai, mục T cần cập nhật và tiêu chí nghiệm thu.

Không học toàn bộ SDK trước khi làm prototype. Ví dụ I01 kết thúc bằng OTP thật trên một số thử và lỗi mã sai được xử lý đúng; I08 kết thúc bằng hai thiết bị nhận thay đổi và fetch bù sau mất mạng.

## 8. Tài liệu chính thức để bắt đầu nghiên cứu

Các nguồn Phone Auth, SMS Retriever/User Consent, Realtime, Storage, FCM, offline-first và WebRTC dưới đây đã được đối chiếu trong lượt khảo sát trước. Khi triển khai, kiểm tra lại API/phiên bản Kotlin và target SDK thực tế; ví dụ ngôn ngữ khác chỉ dùng để hiểu hợp đồng dịch vụ.

| Chủ đề | Nguồn |
|---|---|
| Google trên Android | [Credential Manager Sign in with Google](https://developer.android.com/identity/sign-in/credential-manager-siwg) |
| Google với Supabase | [Supabase Google Sign-In](https://supabase.com/docs/guides/auth/social-sign-in/auth-google) |
| Phone Auth/provider | [Supabase Phone sign-in](https://supabase.com/docs/guides/auth/phone-login) |
| Tự điền OTP | [Google SMS Retriever và User Consent](https://developers.google.com/android/reference/com/google/android/gms/auth/api/phone/package-summary) |
| Realtime dữ liệu | [Supabase Postgres Changes](https://supabase.com/docs/guides/realtime/postgres-changes) |
| Quyền private channel | [Supabase Realtime Authorization](https://supabase.com/docs/guides/realtime/authorization) |
| Storage policy | [Supabase Storage Access Control](https://supabase.com/docs/guides/storage/security/access-control) |
| Nhận push Android | [FCM receive messages](https://firebase.google.com/docs/cloud-messaging/android/receive-messages) |
| Offline/queue | [Android offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first) |
| RTC connection | [WebRTC peer connections](https://webrtc.org/getting-started/peer-connections) |
| TURN relay | [WebRTC TURN server](https://webrtc.org/getting-started/turn-server) |

Đối với Photo Picker, CameraX, Telecom, location, App Links và scheduler: tra tài liệu chính thức của Android/provider đã chọn khi mở gói tương ứng; chưa chốt SDK/vendor/version trong kế hoạch này.

## 9. Theo dõi tiến độ

| Gói | Trạng thái ban đầu | Bằng chứng cần bổ sung |
|---|---|---|
| G0–G11 | Chưa nghiệm thu; mới lập kế hoạch dựa trên đọc code | Prototype, config staging, test report và PR tương ứng |

Khi hoàn thành một gói: cập nhật trạng thái Ixx, điền kết quả vào ticket và mục T tương ứng trong learning map. Không đánh dấu toàn bộ gói hoàn thành khi chỉ có unit test mock hoặc một lần gọi API thành công.
