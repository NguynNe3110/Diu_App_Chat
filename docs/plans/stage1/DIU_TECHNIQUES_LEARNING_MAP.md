# Dịu — bản đồ kỹ thuật ngoài flow UI → ViewModel → Domain → Data

Ngày lập: 21/11/2026. Đọc cùng [kế hoạch triển khai](DIU_IMPLEMENTATION_PLAN.md).

Mục đích: biết một kỹ thuật giải quyết vấn đề gì, ứng dụng ở feature nào, hiện đã có hay mới dự kiến, đọc code ở đâu và tự kiểm tra mình đã hiểu chưa. Luồng Clean Architecture vẫn là nền tảng; các kỹ thuật dưới đây bổ sung cách xử lý thời gian, đồng bộ, quyền, vòng đời và vận hành.

## 1. Cách phân biệt trạng thái

- **CODE**: có implementation trong workspace; chỉ xác nhận code hiện diện, chưa khẳng định đã pass build/test hoặc đủ cho production.
- **MẪU**: có ở album/template hoặc mới là cấu hình; chưa được nối vào nghiệp vụ Dịu.
- **SQL**: có trong SQL thiết kế, chưa kiểm chứng đã migrate/deploy và có khoảng trống ghi ở kế hoạch.
- **ĐỀ XUẤT**: cần triển khai trong tương lai. Không được ghi vào CV/README là tính năng đang hoạt động.
- Một mục có thể có nhiều trạng thái: cơ chế cơ bản có rồi, ứng dụng đầy đủ chưa có.

Các tên như SyncCoordinator, OutboxWorker, CallCoordinator trong file này là vai trò/tên dự kiến trừ khi có đường dẫn source thật.

## 2. Danh mục nhanh

| ID | Kỹ thuật | Hiện trạng | Feature áp dụng | Mốc học/làm |
|---|---|---|---|---|
| T01 | Gradle convention plugins + version catalog | CODE | Toàn repo | P0/P1 |
| T02 | Architecture tests bằng Konsist | CODE | Luật layer/module/test | P0/P1 |
| T03 | Unidirectional data flow, reducer và immutable UI state | CODE, toàn bộ 11 feature module (onboarding/auth/home/contacts/chat/settings/calls/media/letters/journal/location) | Mọi màn; chat/settings/calls có sub-screen & sheet state | P2/P4/P12 |
| T04 | Debug state-transition timeline | CODE, cần giới hạn/redact | Debug ViewModel | P1 |
| T05 | Koin composition root và account-scoped lifecycle | CODE phần DI, scope đề xuất | Session/sync/player/call/auth/contacts | P1/P2 |
| T06 | DTO/Entity/Domain/UiModel + typed result/error | MẪU/CODE phần wrapper | Mọi feature mạng/local | P1/P4 |
| T07 | Structured concurrency, Flow và cancellation | CODE nền tảng, orchestration đề xuất | Search/sync/chat | P2/P4/P9 |
| T08 | Room làm single source of truth | CODE trong :feature:chat, :feature:contacts | Home/chat/contacts/media | P1/P4 |
| T09 | Transactional outbox + optimistic UI | CODE trong :feature:chat, :feature:contacts | Tin nhắn/media/offline/contacts | P1/P4/P6 |
| T10 | Idempotency + deduplication + reconciliation | CODE trong :feature:chat, SQL có ID | Gửi/retry/forward/jobs | P4/P5 |
| T11 | WorkManager và foreground send coordinator | ĐỀ XUẤT | Hàng đợi gửi/cleanup | P4/P6 |
| T12 | Realtime + reconnect catch-up, revision/tombstone | CODE trong :feature:chat (PostgresChanges + Broadcast); catch-up cursor đầy đủ đề xuất | Chat/reaction/mood | P4/P7 |
| T13 | Keyset pagination, stable keys và giữ scroll anchor | SQL index một phần; client đề xuất | Timeline/media/search | P4 |
| T14 | State machine nghiệp vụ | CODE cho OTP (:feature:auth), thư niêm phong (:feature:letters); ĐỀ XUẤT cho call | OTP/letter/call/record/location | Từng mốc |
| T15 | Atomic RPC, constraint và optimistic concurrency | SQL, CODE migration (00002, 00003, 00005) | Direct/group/letter/call | P1/P7/P8 |
| T16 | RLS + quyền theo cột + Storage access | SQL, CODE migration (00002, 00003, 00004, 00005) | Tất cả dữ liệu cá nhân | P1 trở đi |
| T17 | Private Broadcast/Presence + throttle/TTL | Client config; logic đề xuất | Typing/live/signaling | P4/P10/P11 |
| T18 | Server clock, timezone và scheduler | SQL helper; CODE (:feature:settings QuietSchedule) | Quiet/thư/digest/location | P5/P8 |
| T19 | Notification pipeline, privacy renderer, dedupe/digest | CODE (:feature:settings NotificationPrivacy) | Push/in-app/lock preview | P5/P9 |
| T20 | Recorder/player ownership, waveform và audio focus | CODE trong :feature:chat | Voice/media | P6 |
| T21 | Attachment pipeline, signed URL và cache index | CODE trong :feature:chat, :feature:media | Photo/voice/video/storage | P6/P12 |
| T22 | FTS + GIN + trigram + normalization | CODE trong :feature:journal, SQL một phần | Search contacts/messages | P9 |
| T23 | Verified App Links + QR + deferred navigation | CODE một phần (QR card :feature:contacts) | Invite/auth/contact | P3 |
| T24 | WebRTC signaling/ICE/STUN/TURN + media adapter | CODE trong :feature:calls | Voice/video calls | P10 |
| T25 | Telecom/call service và vòng đời độc lập UI | CODE trong :feature:calls | Incoming/ongoing call | P10 |
| T26 | Live location session/expiry + adaptive sampling | SQL table; Android đề xuất | Location | P11 |
| T27 | Design tokens, Ambient Glassmorphism, Custom Canvas Draw và Motion có kiểm soát | CODE (:feature:base, :app, toàn bộ 11 feature module) | Ambient blobs/Frosted glass/DiuRippleOrb/QuietHoursDial/Dashed borders | P3/P9/P12 |
| T28 | Saved state, nav guard và process restoration | CODE typed routes; đầy đủ đề xuất | Auth/chat/viewer | P2/P4 |
| T29 | Quan sát hệ thống, correlation ID và privacy logging | CODE Timber; metrics đề xuất | Toàn app/backend | P1/P12 |
| T30 | Privacy projection + aggregate trên dữ liệu được phép | CODE (:feature:journal, migration 00004, 00005) | Mood/Home/Journal/ChatInfo | P3/P9 |
| T31 | Migration, environment và CI release gates | SQL migration (00001 - 00006), CODE | Local DB/backend/release | P1/P12 |
| T32 | Quyền sở hữu dữ liệu, cleanup và deletion workflow | CODE trong :feature:media (ClearCacheUseCase); ĐỀ XUẤT backend | Cache/recall/logout/delete account | P6/P12 |
| T33 | DataStore Preferences cho cấu hình người dùng và riêng tư | CODE trong :feature:settings | Cấu hình theme, privacy toggle, quiet schedule, user profile | P1/P5 |
| T34 | Android Credential Manager + Google Sign-In & Profile Username Guard | CODE trong :feature:auth | M1 Google-first Auth | P2/G1a |

## 3. Phần đã có trong repo và điều cần đọc

### T01 — Gradle convention plugins và version catalog

**Vượt flow basic ở đâu:** đây là kỹ thuật tổ chức quá trình build. Mỗi feature không tự copy cấu hình Android/Compose/test; plugin nội bộ áp dụng quy ước.

- Đọc [FeatureConventionPlugin.kt](../../../build-logic/src/main/kotlin/com/uzuu/diuchat/buildlogic/FeatureConventionPlugin.kt), [build-logic/build.gradle.kts](../../../build-logic/build.gradle.kts), [libs.versions.toml](../../../gradle/libs.versions.toml), rồi [album/build.gradle.kts](../../../feature/album/build.gradle.kts).
- Hiện plugin feature tự thêm nhiều dependency như Room/Retrofit/Compose. Khi chuyển sang Supabase, xem dependency nào thực sự cần; không mặc định feature UI nào cũng phải có mọi SDK.
- Học: Gradle included build, plugin ID, extension configuration, dependency catalog, KSP và cấu hình tập trung.
- Bài tập: lần theo vì sao album dùng Room dù file build của album rất ngắn; giải thích dependency được thêm ở đâu.
- Hoàn thành khi: thêm một feature nhỏ dùng cùng convention mà không copy cấu hình cả module cũ; build kiểm chứng được.

### T02 — Architecture tests bằng Konsist

**Vượt flow basic:** biến quy tắc kiến trúc thành kiểm tra có thể chạy trong CI, thay vì chỉ ghi “ViewModel không import data” trong README.

- Đọc [CleanArchitectureKonsistTest.kt](../../../konsist-test/src/test/kotlin/com/uzuu/diuchat/konsisttest/CleanArchitectureKonsistTest.kt), [ViewModelKonsistTest.kt](../../../konsist-test/src/test/kotlin/com/uzuu/diuchat/konsisttest/ViewModelKonsistTest.kt), [UseCaseKonsistTest.kt](../../../konsist-test/src/test/kotlin/com/uzuu/diuchat/konsisttest/UseCaseKonsistTest.kt).
- Đã thống nhất source path/package về com.uzuu.diuchat, gồm onboarding; bỏ wrapper DiuCompat ở namespace cũ và module test scaffold không có trong settings.gradle.kts. Luật package vẫn bật, không dùng allowlist để bỏ qua đường dẫn cũ.
- Luật module chuẩn hóa cả dấu phân cách Windows và Linux trước khi so package. Luật JUnit áp cho local tests, giữ AndroidJUnit4 hợp lệ trong src/androidTest. [TestKonsistTest.kt](../../../konsist-test/src/test/kotlin/com/uzuu/diuchat/konsisttest/TestKonsistTest.kt).
- Onboarding có [OnBoardingViewModelTest.kt](../../../feature/onboarding/src/test/kotlin/com/uzuu/diuchat/feature/onboarding/presentation/screen/OnBoardingViewModelTest.kt) kiểm initial state, chuyển trang và quay lại trang đầu. Kiểm chứng local trong tác vụ sửa CI: 15/15 Konsist và 3/3 onboarding pass.
- Kiểm này không chứng minh logic gửi tin, SQL permission hay cancellation đúng; cần test hành vi riêng.
- Bài tập: cố ý import data type vào domain trong nhánh học, chạy test và giải thích lỗi; khôi phục sau thử nghiệm.
- Hoàn thành khi: hiểu luật nào bắt được vi phạm nào và mở rộng luật cho feature mới mà không bỏ lọt namespace.

### T03 — Unidirectional data flow và reducer

**Vượt flow basic:** state không bị sửa từ nhiều Composable; event được chuyển thành action rồi reducer tạo state mới.

- Đọc [BaseViewModel.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/presentation/viewmodel/BaseViewModel.kt), [BaseAction.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/presentation/viewmodel/BaseAction.kt), [OnBoardingViewModel.kt](../../../feature/onboarding/src/main/java/com/uzuu/diuchat/feature/onboarding/presentation/screen/OnBoardingViewModel.kt), [AuthViewModel.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/presentation/screen/AuthViewModel.kt).
- Áp dụng trong `:feature:auth`: [AuthViewModel.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/presentation/screen/AuthViewModel.kt), [AuthAction.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/presentation/screen/AuthAction.kt), [AuthUiState.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/presentation/screen/AuthUiState.kt). State immutable quản lý state machine đăng nhập OTP (`EditingPhone` -> `Sending` -> `AwaitingCode` -> `Verifying` -> `Success`/`Error`), số điện thoại, mã OTP và trạng thái đăng nhập Google (`isGoogleSigningIn`, `googleError`). Reducer `AuthAction` thuần túy (`SetPhoneNumber`, `SetOtpState`, `SetOtpCode`, `SetGoogleSigningIn`, `SetGoogleError`) đảm bảo chuyển đổi state có thể dự đoán được; sửa lỗi routing UI giữ lỗi gửi OTP tại giao diện nhập số điện thoại thay vì chuyển nhầm sang màn hình nhập mã với bàn phím bị khóa.
- Áp dụng trong `:feature:contacts`: [ContactsViewModel.kt](../../../feature/contacts/src/main/kotlin/com/uzuu/diuchat/feature/contacts/presentation/screen/ContactsViewModel.kt), [ContactsAction.kt](../../../feature/contacts/src/main/kotlin/com/uzuu/diuchat/feature/contacts/presentation/screen/ContactsAction.kt), [ContactsUiState.kt](../../../feature/contacts/src/main/kotlin/com/uzuu/diuchat/feature/contacts/presentation/screen/ContactsUiState.kt). State lưu trữ danh bạ, filter circle, query tìm kiếm, sheet QR và danh sách pending requests. Mọi thay đổi đều qua `ContactsAction` thuần, không mutate trực tiếp trong Composable. State không bao giờ bị can thiệp trực tiếp từ Composable; UI emit event qua hàm ViewModel, ViewModel dispatch `BaseAction` thuần vào `sendAction(action)` để tạo state mới.
- Áp dụng trong `:feature:home`: [HomeScreen.kt](../../../feature/home/src/main/kotlin/com/uzuu/diuchat/feature/home/presentation/HomeScreen.kt), [HomeViewModel.kt](../../../feature/home/src/main/kotlin/com/uzuu/diuchat/feature/home/presentation/HomeViewModel.kt), [HomeAction.kt](../../../feature/home/src/main/kotlin/com/uzuu/diuchat/feature/home/presentation/HomeAction.kt), [HomeUiState.kt](../../../feature/home/src/main/kotlin/com/uzuu/diuchat/feature/home/presentation/HomeUiState.kt). State immutable lưu trữ thông tin greeting theo giờ, danh sách chòm sao `StarPerson`, danh sách `RecentChat`, cờ giờ yên lặng `isQuietModeActive`. UI dispatch `HomeAction` (`SetLoading`, `SetData`, `ClearData`, `DismissQuietBanner`) qua reducer thuần `reduce(state: HomeUiState): HomeUiState`.
- Áp dụng trong `:feature:chat`: [ChatScreen.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatScreen.kt), [ChatInfoScreen.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatInfoScreen.kt), [NewGroupScreen.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/NewGroupScreen.kt), [ChatViewModel.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatViewModel.kt), [ChatAction.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatAction.kt), [ChatUiState.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatUiState.kt). State immutable chứa thông tin hội thoại, partner mood, quiet mode, danh sách tin nhắn thu thập từ Flow Room, input text, mood được chọn (trong 5 core mood), quote reply tin nhắn, trạng thái mở tray mood, voice recording state, và mở rộng cho P7 & UI Overhaul (22/11/2026): tạo nhóm (`isNewGroupOpen`, `newGroupSelectedMemberIds`, `newGroupTitle`, `newGroupMood`), chỉnh sửa nhóm (`isGroupEditSheetOpen`), chi tiết hội thoại (`isChatInfoOpen`), tin ghim (`pinnedSnippet`), phản ứng cảm xúc (`activeReactionDetail`), chuyển tiếp tin nhắn (`activeForwardMessage`), khay đính kèm (`isAttachmentSheetOpen`), và sheet gửi chậm ngay trong chat (`isSlowSendSheetOpen`, `slowSendDraftContent`, `slowSendScheduleLabel`). Reducer `ChatAction` thuần túy biến đổi state (`SetInputText`, `SelectMood`, `ToggleMoodTray`, `SetReplyTo`, `SetReplyingTo`, `SetPendingPhoto`, `SetVoiceRecordingState`, `SetActiveReactionDetail`, `SetActiveForwardMessage`, `SetChatInfoOpen`, `SetNewGroupOpen`, `SetGroupEditSheetOpen`, `ToggleNewGroupMember` với guard giới hạn tối đa 7 liên hệ + bản thân = 8 thành viên nhóm nhỏ, `UpdateNewGroupDraft`, `SetSlowSendSheetOpen`, `SetAttachmentSheetOpen`, `UpdateSlowSendDraft`, `AppendLocalMessage` cho bong bóng `MessageMediaType.SEALED_LETTER` / `LOCATION`, `ToggleRevealSealedMessage`, `CancelSealedMessage`, `SetConversation`, `SetMessages`, `SetSending`, `SetError`). Side-effect gửi tin, ghi âm, gửi ảnh, ghim, reaction, forward, rời nhóm được ViewModel điều phối qua UseCases, sau đó Room Flow tự cập nhật state. Đã kiểm chứng qua [ChatActionTest.kt](../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatActionTest.kt) và [ChatViewModelTest.kt](../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatViewModelTest.kt).
- Áp dụng trong `:feature:settings`: [SettingsScreen.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/presentation/screen/SettingsScreen.kt), [SettingsSubScreen.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/presentation/screen/SettingsSubScreen.kt), [QuietSchedule.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/domain/model/QuietSchedule.kt), [SettingsViewModel.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/presentation/screen/SettingsViewModel.kt), [SettingsAction.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/presentation/screen/SettingsAction.kt), [SettingsUiState.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/presentation/screen/SettingsUiState.kt). State immutable quản lý `UserProfile` (tên hiển thị, bio, ngày sinh, số điện thoại, mood đại diện), `QuietSchedule` (hỗ trợ bước nhảy `stepStartMinutes` / `stepEndMinutes` ±30 phút qua nửa đêm modulo `1440` và tính thời lượng `durationMinutes` / `durationHoursFormatted`), `NotificationPrivacy`, danh sách `BlockedContact`, cờ xác nhận đã đọc `readReceiptsEnabled`, tuỳ chọn rung một lần (`vibrateOnceEnabled`), tóm tắt nhóm buổi tối (`eveningGroupDigestEnabled`), dialog đăng xuất (`showLogoutConfirmDialog`), dialog xóa tài khoản (`showDeleteAccountDialog`), cờ điều hướng đăng xuất (`isLoggedOut`), trạng thái quy trình xóa tài khoản (`AccountDeletionState`), và điều hướng nội bộ 6 màn con `SettingsSubScreen` theo đúng prototype (`PROFILE_OVERVIEW` #34, `GENERAL_SETTINGS` #36, `EDIT_PROFILE` #35, `QUIET_HOURS` #37, `NOTIFICATIONS` #38, `BLOCKED_CONTACTS` #53). Reducer `SettingsAction` thuần túy biến đổi state (`SetUserProfile`, `SetQuietSchedule`, `SetNotificationPrivacy`, `SetBlockedContacts`, `ToggleReadReceipts`, `ToggleVibrateOnce`, `ToggleEveningGroupDigest`, `SetSubScreen`, `SetShowPrivacyDialog`, `SetShowQuietHoursDialog`, `SetShowEditProfileSheet`, `SetShowDeleteAccountDialog`, `SetShowLogoutConfirmDialog`, `SetAccountDeletionState`, `SetLoggedOut`) mà không phát sinh side effect hay mutate UI state trực tiếp. Đã kiểm chứng qua [SettingsActionTest.kt](../../../feature/settings/src/test/kotlin/com/uzuu/diuchat/feature/settings/presentation/screen/SettingsActionTest.kt) và [SettingsViewModelTest.kt](../../../feature/settings/src/test/kotlin/com/uzuu/diuchat/feature/settings/presentation/screen/SettingsViewModelTest.kt).
- Áp dụng trong `:feature:calls`: [CallHistoryScreen.kt](../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallHistoryScreen.kt), [CallHistoryViewModel.kt](../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallHistoryViewModel.kt), [CallHistoryUiState.kt](../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallHistoryUiState.kt), [CallScreen.kt](../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallScreen.kt), [CallViewModel.kt](../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallViewModel.kt), [CallAction.kt](../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallAction.kt), [CallUiState.kt](../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallUiState.kt). `CallHistoryUiState` gom nhóm lịch sử cuộc gọi theo ngày (`groupedLogsByDate`: `HÔM NAY`, `HÔM QUA`, `THỨ BA`) và quản lý danh sách người gọi yêu thích `favoriteCallers` (`FavoriteCaller`). `CallUiState` và `CallAction` quản lý metadata màu cảm xúc đối phương (`peerMoodHex`), trạng thái mở rộng trả lời nhanh khi có cuộc gọi đến toàn màn hình (`isQuickReplyExpanded`, `lastQuickReplyMessage`), và hẹn nhắc gọi lại (`reminderScheduled`), đảm bảo bấm trả lời nhanh tự động ghi nhận lời nhắn và từ chối cuộc gọi với `CallEndReason.REJECTED`. Đã kiểm chứng qua [CallHistoryViewModelTest.kt](../../../feature/calls/src/test/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallHistoryViewModelTest.kt) và [CallViewModelTest.kt](../../../feature/calls/src/test/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallViewModelTest.kt).
- Áp dụng trong `:feature:media`: [MediaVaultScreen.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/vault/MediaVaultScreen.kt), [MediaVaultViewModel.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/vault/MediaVaultViewModel.kt), [MediaVaultAction.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/vault/MediaVaultAction.kt), [MediaVaultUiState.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/vault/MediaVaultUiState.kt). State immutable nhóm media vault theo tháng, quản lý bộ lọc `MediaType` (All, Photo, Voice, Letter), loại trừ hoàn toàn định dạng Video theo DEC-11 để giữ trải nghiệm yên tĩnh, nhẹ nhàng và tiết kiệm dữ liệu/bộ nhớ; trạng thái phát audio hiện hành (`playingItemId`). Reducer `MediaVaultAction` thuần túy (`SetLoading`, `SetItems`, `SetFilter`, `TogglePlayVoice`, `SetError`) biến đổi state mà không mutate trực tiếp từ Compose. Màn hình quản lý dung lượng ([StorageScreen.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/storage/StorageScreen.kt), [StorageViewModel.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/storage/StorageViewModel.kt), [StorageAction.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/storage/StorageAction.kt), [StorageUiState.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/storage/StorageUiState.kt)) quản lý tính toán tỉ lệ phần trăm dung lượng, danh sách conversation breakdown, dọn dẹp cache toàn cục an toàn và dọn dẹp media đệm theo từng cuộc trò chuyện (`ClearConversationMediaUseCase`), cấu hình chính sách tự động tải phương tiện (`AutoDownloadPolicy`: WIFI_ONLY, CELLULAR_AND_WIFI, MANUAL), toggle chất lượng ảnh gốc qua `StorageAction` (`SetLoading`, `SetStats`, `SetAutoDownloadPolicy`, `ToggleAutoDownloadWifi`, `ToggleAutoDownloadVoice`, `ToggleKeepOriginalQuality`, `SetShowClearDialog`, `SetClearing`, `SetSelectedConversationToClear`, `SetClearingConversation`, `CacheCleared`).
- Áp dụng trong `:feature:letters`: [LettersScreen.kt](../../../feature/letters/src/main/kotlin/com/uzuu/diuchat/feature/letters/presentation/screen/LettersScreen.kt), [LettersViewModel.kt](../../../feature/letters/src/main/kotlin/com/uzuu/diuchat/feature/letters/presentation/screen/LettersViewModel.kt), [LettersAction.kt](../../../feature/letters/src/main/kotlin/com/uzuu/diuchat/feature/letters/presentation/screen/LettersAction.kt), [LettersUiState.kt](../../../feature/letters/src/main/kotlin/com/uzuu/diuchat/feature/letters/presentation/screen/LettersUiState.kt). State immutable lưu trữ danh sách `SealedLetter`, bộ lọc `LetterTab` (All, Pending, ReadyToOpen, Opened), trạng thái soạn thư nháp `isComposing`, modal `ScheduleSheet`, và thư đã mở `selectedOpenedLetter`. Reducer `LettersAction` thuần túy biến đổi state (SetLetters, SetTab, SetComposing, SetShowScheduleSheet, UpdateDraftTitle, UpdateDraftContent, UpdateDraftMood, SetDraftSchedule, SetSelectedOpenedLetter, ResetDraft) mà không mutate trực tiếp từ Compose. ViewModel điều phối use cases gửi thư, mở niêm phong, gửi ngay và hủy thư.
- Áp dụng trong `:feature:journal`: [JournalScreen.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/journal/JournalScreen.kt), [JournalViewModel.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/journal/JournalViewModel.kt), [JournalAction.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/journal/JournalAction.kt), [JournalUiState.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/journal/JournalUiState.kt). State immutable lưu trữ danh sách `DailyMoodCheckin`, ngày được chọn, thống kê `MonthMoodBreakdown`, so sánh cảm xúc `MoodComparison` ("Cùng Minh"), và trạng thái sheet ghi màu `MoodCheckinSheet` (chọn mood, private note, allowlist). Reducer `JournalAction` thuần túy (`SetCheckins`, `SelectDay`, `SetComparison`, `SetCheckinSheetVisible`, `SetCheckinSelectedMood`, `SetCheckinNote`, `ToggleCheckinContact`, `SetSavingCheckin`, `SetError`) biến đổi state mà không mutate từ Compose. Màn hình tìm kiếm ([SearchScreen.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/search/SearchScreen.kt), [SearchViewModel.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/search/SearchViewModel.kt), [SearchAction.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/search/SearchAction.kt), [SearchUiState.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/search/SearchUiState.kt)) xử lý bộ lọc từ khóa `query`, lọc mood `JournalMoodType`, phạm vi hội thoại `conversationId`, và danh sách kết quả tìm kiếm có highlight từ khóa.
- Hiện sendAction gọi action.reduce(state), phát qua StateFlow và bỏ state bằng nhau. BaseAction trong repo chủ yếu là state transformation; đừng nhầm mọi user intent và network effect đều nên thực hiện trong reduce.
- Với chat: intent SendClicked → coroutine/use case → action Queued/Synced/Failed; reducer thuần chỉ quyết state. Side effect IO không nằm trong reducer.
- Cần serialize state updates trên dispatcher/actor phù hợp; không tự coi Delegates.observable là cơ chế atomic cho nhiều coroutine.
- Bài tập: test reducer cho draft → queued/error, không cần Compose hay mạng.
- Hoàn thành khi: mô tả được state bền, state UI tạm và effect; xử lý lỗi không khiến spinner treo.

### T04 — Debug state-transition timeline

**Vượt flow basic:** ghi action và chênh lệch old/new state để hiểu lỗi state mà không rải log thủ công.

- Đọc [StateTimeTravelDebugger.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/presentation/viewmodel/StateTimeTravelDebugger.kt).
- Hiện class lưu transition, dùng reflection và log; chưa có UI tua lại/replay/undo thật dù tên có “TimeTravel”.
- Khi chat có body, OTP, phone, note, vị trí: phải redact. Timeline cần giới hạn số state để không giữ cả lịch sử chat trong RAM.
- Bài tập: theo dõi ba lần đổi trang onboarding; xác định action và trường state đổi; đề xuất mask cho phone.
- Hoàn thành khi: log giúp tìm lỗi mà không chứa dữ liệu nhạy cảm hoặc tăng bộ nhớ vô hạn.

### T05 — Koin composition root và lifecycle theo tài khoản

**Vượt flow basic:** DI còn quyết định ai sở hữu client, job, subscription, recorder và lúc nào chúng bị hủy.

- Đọc [ShowcaseApplication.kt](../../../app/src/main/kotlin/com/uzuu/diuchat/app/ShowcaseApplication.kt), [AppKoinModule.kt](../../../app/src/main/kotlin/com/uzuu/diuchat/app/di/AppKoinModule.kt), [SupabaseClientFactory.kt](../../../app/src/main/kotlin/com/uzuu/diuchat/app/data/supabase/SupabaseClientFactory.kt), [AuthKoinModule.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/AuthKoinModule.kt).
- Áp dụng trong `:feature:auth`: Tách module Koin theo từng layer Clean Architecture (`dataModule`, `domainModule`, `presentationModule`) và tập hợp tại [AuthKoinModule.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/AuthKoinModule.kt). Composition root tại `ShowcaseApplication` đăng ký `modules(featureAuthModules)`. Đăng ký `SignInWithGoogleUseCase`, `SendOtpUseCase`, `VerifyOtpUseCase` vào `domainModule` và inject vào `AuthViewModel`.
- Áp dụng trong `:feature:contacts`: [ContactsKoinModule.kt](../../../feature/contacts/src/main/kotlin/com/uzuu/diuchat/feature/contacts/ContactsKoinModule.kt) đăng ký các usecase và repository, nạp vào `ShowcaseApplication` qua `modules(featureContactsModules)`.
- Áp dụng trong `:feature:chat`: [ChatKoinModule.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/ChatKoinModule.kt) đăng ký Room Database (`DiuChatDatabase`), các DAO (`MessageDao`, `ConversationDao`, `OutboxDao`), `ChatRepositoryImpl`, 11 UseCases (`GetMessagesUseCase`, `SendMessageUseCase`, `SendVoiceMessageUseCase`, `SendPhotoMessageUseCase`, `CreateGroupUseCase`, `UpdateGroupInfoUseCase`, `ManageGroupMemberUseCase`, `LeaveGroupUseCase`, `ToggleReactionUseCase`, `TogglePinMessageUseCase`, `ForwardMessageUseCase`) và `ChatViewModel`, nạp vào `ShowcaseApplication` qua `modules(featureChatModules)`.
- Áp dụng trong `:feature:settings`: [SettingsKoinModule.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/SettingsKoinModule.kt) đăng ký `dataModule` (`SettingsRepositoryImpl`), `domainModule` (6 use cases: `GetQuietScheduleUseCase`, `UpdateQuietScheduleUseCase`, `GetNotificationPrivacyUseCase`, `UpdateNotificationPrivacyUseCase`, `GetBlockedContactsUseCase`, `UnblockContactUseCase`), và `presentationModule` (`SettingsViewModel`). Nạp vào Composition Root tại `ShowcaseApplication` qua `modules(featureSettingsModules)`.
- Áp dụng trong `:feature:media`: [MediaKoinModule.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/MediaKoinModule.kt) tách biệt 3 tầng Clean Architecture (`mediaRepositoryModule`, `mediaDomainModule`, `mediaPresentationModule`) đăng ký `MediaRepositoryImpl`, 6 use cases (`GetVaultMediaUseCase`, `GetStorageStatsUseCase`, `ClearCacheUseCase`, `ClearConversationMediaUseCase`, `GetAutoDownloadPolicyUseCase`, `UpdateAutoDownloadPolicyUseCase`) và 2 ViewModel (`MediaVaultViewModel`, `StorageViewModel`). Nạp vào Composition Root tại [ShowcaseApplication.kt](../../../app/src/main/kotlin/com/uzuu/diuchat/app/ShowcaseApplication.kt) qua `modules(featureMediaModule)`.
- Áp dụng trong `:feature:letters`: [LettersKoinModule.kt](../../../feature/letters/src/main/kotlin/com/uzuu/diuchat/feature/letters/LettersKoinModule.kt) tách biệt 3 tầng Clean Architecture (`dataModule`, `domainModule`, `presentationModule`) đăng ký `LettersRepositoryImpl`, 5 use cases (`CancelSealedLetterUseCase`, `GetLettersUseCase`, `OpenSealedLetterUseCase`, `SendLetterImmediatelyUseCase`, `SendSealedLetterUseCase`) và `LettersViewModel`. Nạp vào Composition Root tại [ShowcaseApplication.kt](../../../app/src/main/kotlin/com/uzuu/diuchat/app/ShowcaseApplication.kt) qua `modules(featureLettersModules)`.
- Áp dụng trong `:feature:journal`: [JournalKoinModule.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/JournalKoinModule.kt) tách biệt 3 tầng Clean Architecture (`journalRepositoryModule`, `journalDomainModule`, `journalPresentationModule`) đăng ký `JournalRepositoryImpl`, 4 use cases (`GetMonthlyMoodCheckinsUseCase`, `SaveMoodCheckinUseCase`, `GetMoodComparisonUseCase`, `SearchMessagesUseCase`) và 2 ViewModel (`JournalViewModel`, `SearchViewModel`). Nạp vào Composition Root tại [ShowcaseApplication.kt](../../../app/src/main/kotlin/com/uzuu/diuchat/app/ShowcaseApplication.kt) qua `modules(featureJournalModules)`.
- Có DI/client setup; account scope, cleanup và worker integration là đề xuất.
- App scope: client config/logging. Account scope: DB/repositories/sync channels. Feature/service scope: draft UI/player/recorder/call adapter theo ownership thực tế.
- Logout phải cancel user jobs/subscriptions, detach token và ngăn late response ghi vào account mới; không chỉ navigate về login.
- Bài tập: vẽ dependency có vòng đời dài/ngắn; chỉ ra vì sao singleton chứa currentUser mutable dễ gây đọc chéo account.
- Hoàn thành khi: A logout rồi B login trong lúc A đang upload không tạo dữ liệu của A ở B.

### T06 — Model mapping và typed errors

**Vượt flow basic:** một message có nhiều biểu diễn với trách nhiệm khác nhau, không dùng DTO server trực tiếp để render.

- Đọc [AlbumRepositoryImpl.kt](../../../feature/album/src/main/kotlin/com/uzuu/diuchat/feature/album/data/repository/AlbumRepositoryImpl.kt), [AlbumMapper.kt](../../../feature/album/src/main/kotlin/com/uzuu/diuchat/feature/album/data/mapper/AlbumMapper.kt), [SupabaseResult.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/data/supabase/SupabaseResult.kt).
- DTO theo wire schema; Entity có sync status/local URI; Domain giữ quy tắc; UiModel có formatted time/grouping/accessibility text.
- Đề xuất sealed content: Text/Photo/Voice/Location/Letter/System, parse JSONB có validation và unknown-type fallback.
- Error phân biệt network/auth/rate-limit/permission/validation/conflict/unknown outcome; không retry mọi Exception.
- Coroutine cancellation phải tiếp tục propagate; đừng biến CancellationException thành toast lỗi.
- Bài tập: ánh xạ RPC timeout, 403 và file quá lớn thành ba hành động UI khác nhau.
- Hoàn thành khi: đổi tên field server không phải sửa mọi Composable, và ViewModel không import DTO/Entity.

### T07 — Structured concurrency, Flow và cancellation

**Vượt flow basic:** quản lý nhiều luồng chạy đồng thời, kết quả cũ và job sống quá lâu.

- BaseViewModel có StateFlow; phần stream orchestration của chat/search còn cần làm.
- Chat: observe Room → map UI state → collect theo lifecycle; Realtime/sync có owner riêng, không tạo socket trong mỗi bubble.
- Search: debounce + distinctUntilChanged + mapLatest/flatMapLatest theo thiết kế; nếu SDK không hủy IO thật, dùng request generation để bỏ response cũ.
- Callback recorder/RTC/location cần adapter và unsubscribe/close khi scope kết thúc; IO/CPU nặng chạy dispatcher phù hợp.
- Tác vụ sửa CI: [AuthRepositoryImpl.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/data/repository/AuthRepositoryImpl.kt) rethrow CancellationException qua các lớp runCatching để hủy coroutine không trở thành fallback success. [AuthRepositoryImplTest.kt](../../../feature/auth/src/test/kotlin/com/uzuu/diuchat/feature/auth/data/repository/AuthRepositoryImplTest.kt) kiểm đường hủy lúc gửi và verify. Logic fallback demo sẵn có được giữ; các test này không chứng minh xác thực production hoặc SMS provider hoạt động.
- Bài tập: gõ “min” rồi “minh”, response “min” về sau; viết test đảm bảo UI vẫn hiển thị query mới.
- Hoàn thành khi: đổi màn/hủy job không còn callback cập nhật ViewModel đã đóng, không nuốt cancellation.

## 4. Phần làm chat đáng tin cậy

### T08 — Room là single source of truth

**Vượt flow basic:** UI đọc một nguồn local nhất quán thay vì request mạng rồi gán danh sách vào state.

- Đã triển khai trong `:feature:chat`: [DiuChatDatabase.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/data/datasource/database/DiuChatDatabase.kt), [MessageDao.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/data/datasource/database/dao/MessageDao.kt), [ConversationDao.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/data/datasource/database/dao/ConversationDao.kt), [ChatRepositoryImpl.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/data/repository/ChatRepositoryImpl.kt).
- Flow stream từ Room `messageDao.getMessagesFlow(conversationId)` là nguồn sự thật duy nhất cung cấp danh sách tin nhắn (`ChatMessage`) lên Domain/UI thông qua `GetMessagesUseCase`.
- UI không bao giờ giữ một danh sách cục bộ hoặc gán thủ công từ response mạng. Mọi thay đổi (gửi tin mới, nhận tin từ Realtime/sync, cập nhật trạng thái) đều ghi xuống Room trước, và Room Flow tự động emit danh sách mới đến `ChatViewModel`.
- `MessageEntity` lưu trữ đầy đủ `status` (`Pending`, `Uploading`, `Sent`, `Delivered`, `Read`, `Failed`), `moodType` (6 sắc thái cảm xúc), `replyToMessageId`, `replyToSnippet`, `replyToSenderName`.
- Mở rộng trong `:feature:contacts` (khắc phục việc lưu tạm bằng MutableStateFlow trong RAM theo Bước 1 I06): [DiuContactsDatabase.kt](../../../feature/contacts/src/main/kotlin/com/uzuu/diuchat/feature/contacts/data/datasource/database/DiuContactsDatabase.kt), [ContactDao.kt](../../../feature/contacts/src/main/kotlin/com/uzuu/diuchat/feature/contacts/data/datasource/database/dao/ContactDao.kt), [ContactEntity.kt](../../../feature/contacts/src/main/kotlin/com/uzuu/diuchat/feature/contacts/data/datasource/database/entity/ContactEntity.kt), [ConnectionRequestEntity.kt](../../../feature/contacts/src/main/kotlin/com/uzuu/diuchat/feature/contacts/data/datasource/database/entity/ConnectionRequestEntity.kt), [ContactsRepositoryImpl.kt](../../../feature/contacts/src/main/kotlin/com/uzuu/diuchat/feature/contacts/data/repository/ContactsRepositoryImpl.kt).
  - Trước đây: `ContactsRepositoryImpl` lưu danh bạ và yêu cầu kết nối trong `MutableStateFlow` (RAM), dẫn đến mất dữ liệu khi tắt app / restart process.
  - Hiện tại: Dùng Room SQLite database `DiuContactsDatabase` làm Single Source of Truth, stream qua reactive Flow `contactDao.observeAllContacts()` và `contactDao.observePendingRequests()`.
  - Cơ chế Seed an toàn: Chỉ seed danh bạ mẫu khi `contactDao.getContactsCount() == 0`, không ghi đè dữ liệu người dùng khi mở lại app.
  - Đã kiểm chứng: 6/6 unit test trong [ContactsRepositoryImplTest.kt](../../../feature/contacts/src/test/kotlin/com/uzuu/diuchat/feature/contacts/data/repository/ContactsRepositoryImplTest.kt) và 19/19 test `:feature:contacts` pass.
- Bài tập: mở chat từ cache trong airplane mode; dùng fake remote update và quan sát UI mà không gán list thủ công.
- Hoàn thành khi: không có hai bản message từ local/remote cùng tồn tại ở UI. Đã kiểm chứng qua [ChatViewModelTest.kt](../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/presentation/viewmodel/ChatViewModelTest.kt) và [GetMessagesUseCaseTest.kt](../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/domain/usecase/GetMessagesUseCaseTest.kt).
- Nền tảng tham khảo: [Android offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first).

### T09 — Transactional outbox và optimistic UI

**Vượt flow basic:** bấm gửi phải hiện ngay và sống được qua process death, dù request chưa thành công.

- Đã triển khai trong `:feature:chat`: [OutboxOperationEntity.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/data/datasource/database/entity/OutboxOperationEntity.kt), [OutboxDao.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/data/datasource/database/dao/OutboxDao.kt), [ChatRepositoryImpl.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/data/repository/ChatRepositoryImpl.kt).
- Room transaction nguyên tử `database.withTransaction` và phương thức DAO `@Transaction` (khắc phục lỗi B02/B03/B04 theo Bước 1 I06):
  - [MessageDao.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/data/datasource/database/dao/MessageDao.kt) cung cấp:
    - `@Transaction suspend fun insertMessageWithOutbox(message: MessageEntity, outbox: OutboxOperationEntity)`
    - `@Transaction suspend fun insertMessageWithOutboxAndSummary(message: MessageEntity, outbox: OutboxOperationEntity, ...)`
    - `@Transaction suspend fun retryMessageWithOutbox(...)`
  - Ghi đồng thời `MessageEntity` với status = `Pending` (hiển thị ngay tức thì trên UI qua Room Flow - Optimistic UI), bản ghi `OutboxOperationEntity` tương ứng với trạng thái `PENDING`, và tóm tắt hội thoại `updateConversationSummary` trong một SQLite transaction duy nhất. Nếu app bị kill đột ngột ngay sau khi bấm Send, cả message và outbox record đều đã được commit bền vững.
  - Sửa lỗi B02/B03: Tin nhắn giữ nguyên trạng thái `PENDING` lúc ghi vào Room, không còn tự ý đổi sang `SENT` giả lập trước khi có ACK thật từ mạng/server.
  - **Outbox ACK loop trong `ChatRepositoryImpl`:** Sau khi ghi Room transaction, `sendMessage`, `sendPhotoMessage`, `sendVoiceMessage`, `forwardMessage` và `retryMessage` gọi `supabaseService.upsertMessage(...)` để đồng bộ lên Supabase: thành công thì cập nhật `OutboxOperationEntity` (`SENT`) và `MessageEntity` (`MessageStatus.Sent`), thất bại thì đánh dấu `FAILED` (`OutboxStatus.FAILED`) kèm lỗi để người dùng retry thủ công. Khi chưa có `SupabaseClient` (chế độ local/offline test), vẫn giữ cơ chế fallback cục bộ có kiểm soát.
- Mở rộng trong `:feature:contacts`: [ContactDao.kt](../../../feature/contacts/src/main/kotlin/com/uzuu/diuchat/feature/contacts/data/datasource/database/dao/ContactDao.kt) cung cấp `@Transaction suspend fun acceptConnectionRequest(requestId: String, contact: ContactEntity)` xóa yêu cầu kết nối trong bảng `connection_requests` và chèn bản ghi liên hệ mới vào bảng `contacts` trong một transaction nguyên tử duy nhất.
- UI hiển thị trạng thái `Pending` (biểu tượng đồng hồ cát), `Sent` (1 tick), `Delivered` (2 tick), `Read` (2 tick đổi màu), `Failed` (chấm cảnh báo đỏ kèm nút retry).
- Bài tập: queue 3 tin khi offline, đóng process, mở lại; cả 3 vẫn chờ gửi.
- Hoàn thành khi: không mất “ý định gửi” do app chết sau khi người dùng bấm nút. Đã kiểm chứng qua [SendMessageUseCaseTest.kt](../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/domain/usecase/SendMessageUseCaseTest.kt) và [ChatRepositoryImplTest.kt](../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/data/repository/ChatRepositoryImplTest.kt).

### T10 — Idempotency, dedupe và reconciliation

**Vượt flow basic:** request có thể gửi lại, callback có thể lặp và ACK có thể mất; phải có cùng một kết quả nghiệp vụ.

- Đã triển khai trong `:feature:chat`: [SendMessageUseCase.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/domain/usecase/SendMessageUseCase.kt), [ChatRepositoryImpl.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/data/repository/ChatRepositoryImpl.kt).
- Tạo `clientMessageId` (UUID v4) ngay tại Domain UseCase trước khi thực hiện bất kỳ thao tác IO nào.
- Cả `MessageEntity.id` và `OutboxOperationEntity.messageId` dùng chung UUID này.
- Khi retry hoặc khi worker gửi request lên server: UUID này được dùng làm Idempotency Key (`id` / `client_assigned_uuid`). Nếu server đã nhận hoặc ACK bị rớt mạng, server không sinh bản ghi mới (trùng primary key).
- `MessageDao.insertOrUpdateMessage` sử dụng `OnConflictStrategy.REPLACE` để đối chiếu (reconcile) giữa bản ghi optimistic local và ACK từ server mà không nhân đôi tin nhắn trên UI.
- Bài tập: giả server commit rồi trả timeout, chạy retry 5 lần; DB vẫn có một message duy nhất.
- Hoàn thành khi: giải thích được khác biệt retry, dedupe phía client, unique phía server và fetch bù.

### T11 — WorkManager và gửi ngay khi foreground

**Vượt flow basic:** công việc bền không được gắn duy nhất vào ViewModel; cần phục hồi khi màn bị đóng.

- Đề xuất foreground coordinator gửi ngay sau enqueue, worker drain là đường phục hồi khi được Android cho chạy.
- Unique work/lease giúp giảm executor trùng; idempotency server vẫn là bảo vệ cuối.
- Network constraint/backoff cho lỗi tạm; permission/blocked/validation không retry vô hạn. Job kiểm account generation trước mỗi lượt IO.
- WorkManager không là đồng hồ niêm phong đúng giây, GPS loop hay RTC stream. Thời điểm thực thi chịu scheduler hệ thống.
- Bài tập: đóng màn chat khi gửi đang pending, enqueue work hai lần, kiểm không phát sinh hai logical send.
- Hoàn thành khi: hiểu dữ liệu phải nằm ở outbox chứ không chỉ ở WorkRequest hoặc ViewModel.
- Tham khảo: [Android task scheduling](https://developer.android.com/develop/background-work/background-tasks/persistent).

### T12 — Realtime, reconnect và tombstone

**Vượt flow basic:** WebSocket giúp nhanh nhưng lịch sử vẫn phải đúng nếu đã bỏ lỡ event.

- **Đã triển khai trong `:feature:chat` (Realtime Chat Sync):**
  - [ChatSupabaseService.kt](../../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/data/datasource/api/service/ChatSupabaseService.kt): `observeRealtimeMessages(conversationId)` mở `channelFlow` trên channel riêng của cuộc hội thoại, lắng nghe đồng thời hai nguồn qua `merge(...)`: `postgresChangeFlow<PostgresAction>` lọc `conversation_id` trên bảng `messages` (khi Postgres replication được bật) và `broadcastFlow<MessageDto>(event = "new_message")` (fallback tức thời khi `upsertMessage` broadcast non-fatal). Khi collector hủy, `awaitClose` gọi `removeChannelSafely` trong `NonCancellable` để dọn subscription tránh rò rỉ socket.
  - [ChatRepositoryImpl.kt](../../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/data/repository/ChatRepositoryImpl.kt): Khi mở cuộc hội thoại, vừa fetch initial messages từ REST/Postgrest vừa collect `observeRealtimeMessages(conversationId)` và ghi/reconcile vào Room `MessageDao`, tính chính xác `isFromMe` dựa trên `supabaseService.currentUserId()`.
  - Đã kiểm chứng qua [ChatSupabaseServiceTest.kt](../../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/data/datasource/api/service/ChatSupabaseServiceTest.kt) và [ChatRepositoryImplTest.kt](../../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/data/repository/ChatRepositoryImplTest.kt).
- **Giới hạn còn lại:** Chưa có cursor catch-up riêng cho sự kiện recall/update tin cũ khi client offline lâu ngày.
- Theo dõi connect/auth/channel state; đăng ký trước rồi fetch bù có merge, hoặc dùng handshake/cursor tương đương để không hở khoảng thời gian.
- Insert/update/recall cần cursor/version phù hợp. Fetch created_at mới nhất không tìm được recall của tin cũ.
- Tombstone giữ định danh và version của tin bị thu hồi; cache/search/quote phải xử lý nhất quán.
- Đừng tự coi sequence cấp lúc insert là commit order hoàn hảo; API cursor cần xử lý transaction đến muộn hoặc overlap/reconciliation có kiểm chứng.
- Bài tập: B offline, A sửa/thu hồi tin từ hôm qua; B lên mạng vẫn nhận thay đổi.
- Hoàn thành khi: Realtime rơi kết nối không làm dữ liệu local sai mãi mãi.

### T13 — Keyset paging, stable keys và scroll anchor

**Vượt flow basic:** timeline thay đổi liên tục nên offset đơn thuần dễ lặp hoặc hụt trang.

- SQL có index (conversation_id, created_at DESC), cần tie-break ID hoặc ordering token server rõ ràng.
- Đề xuất truy vấn cursor cặp (created_at, id) cho lịch sử bất biến theo thứ tự; sync mutation cursor là cơ chế khác T12.
- Lazy list dùng key message ID, load cũ giữ anchor; chỉ auto-scroll nếu đang gần đáy hoặc là tin mình gửi.
- Search jump cần API/page quanh message ID nếu tin chưa có ở local, không load toàn bộ lịch sử.
- Bài tập: đang xem tin cũ, nhận 5 tin mới đồng thời tải trang cũ; vị trí đọc không nhảy.
- Hoàn thành khi: có test timestamp trùng, message pending đổi thứ tự khi ACK, không duplicate items.

## 5. Phần backend, thời gian và riêng tư

### T14 — State machine thay cho nhiều boolean rời rạc

**Vượt flow basic:** tên state và transition loại bỏ tổ hợp vô nghĩa như isRinging=true nhưng isEnded=true.

- Đã triển khai [OtpState.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/domain/model/OtpState.kt) cho luồng xác thực Phone OTP: `EditingPhone -> Sending -> AwaitingCode -> Verifying -> Success / Error`. Loại bỏ hoàn toàn các biến cờ rời rạc (`isLoading`, `isOtpSent`, `isVerifying`), mỗi trạng thái chỉ cho phép tập hợp hành động hợp lệ.
- Áp dụng cho Thư niêm phong gửi chậm trong `:feature:letters`: [LetterStatus.kt](../../../feature/letters/src/main/kotlin/com/uzuu/diuchat/feature/letters/domain/model/LetterStatus.kt) và [SealedLetter.kt](../../../feature/letters/src/main/kotlin/com/uzuu/diuchat/feature/letters/domain/model/SealedLetter.kt) định nghĩa vòng đời rõ ràng: `SEALED_PENDING` (đang niêm phong) → `READY_TO_OPEN` (đã đến giờ hẹn) → `OPENED` (đã mở) hoặc `CANCELLED` (người gửi hủy trước hẹn). Phía người nhận khi ở trạng thái `SEALED_PENDING` tuyệt đối không thể đọc `contentPayload` (bảo vệ theo DEC-04/05), chỉ khi trạng thái chuyển sang `READY_TO_OPEN` mới xuất hiện hành động mở niêm phong, và sau khi mở chuyển sang `OPENED` bất biến. Sửa lỗi B06: `LettersRepositoryImpl.openSealedLetter` kiểm tra bắt buộc `now >= scheduledUnlockEpochMs` hoặc `READY_TO_OPEN`, ngăn ngừa việc mở thư trước thời hạn niêm phong. Đã kiểm chứng trong [LettersRepositoryImplTest.kt](../../../feature/letters/src/test/kotlin/com/uzuu/diuchat/feature/letters/data/repository/LettersRepositoryImplTest.kt).
- Cooldown timer được gắn với `resendDeadlineEpochMs` (thời điểm hết hạn thực tế theo epoch) thay vì đếm biến cục bộ, đảm bảo xoay màn hình hay recomposition không làm sai lệch bộ đếm.
- Dùng tiếp cho recording, message delivery, letter, call, live location; state có thể là sealed hierarchy hoặc enum + transition validator.
- Call kết thúc không quay lại Active vì event muộn; thư Cancelled không được scheduler Release.
- State transition bền phải được server chấp nhận; reducer UI không đủ bảo vệ khi hai máy hành động cùng lúc.
- Bài tập: vẽ transition call và liệt kê event timeout/decline/accept/end đến sai thứ tự.
- Hoàn thành khi: mỗi event có transition hợp lệ hoặc bị bỏ an toàn, test được cả event lặp/muộn (đã kiểm chứng qua [AuthViewModelTest.kt](../../../feature/auth/src/test/kotlin/com/uzuu/diuchat/feature/auth/presentation/screen/AuthViewModelTest.kt)).

### T15 — RPC transaction, constraints và optimistic concurrency

**Vượt flow basic:** nhiều thao tác phải đúng cùng nhau và chống tranh chấp giữa client.

- Đã triển khai migration: [00002_db01_security_definer_hardening.sql](../../migrations/00002_db01_security_definer_hardening.sql), [00003_db02_conversation_members_security.sql](../../migrations/00003_db02_conversation_members_security.sql), [00005_db06_mood_privacy_and_note_separation.sql](../../migrations/00005_db06_mood_privacy_and_note_separation.sql).
- Tạo direct conversation canonical: `get_or_create_direct_conversation` kiểm tra caller từ `auth.uid()`, kiểm tra chặn 2 chiều, sắp xếp cặp user canonical và dùng `pg_advisory_xact_lock` ngăn chặn tranh chấp tạo 2 conversation song song.
- Rời nhóm có bảo vệ admin cuối (DEC-07): `leave_conversation` kiểm tra nếu người rời là admin duy nhất còn lại trong nhóm có thành viên khác thì chặn và yêu cầu `transfer_group_admin` trước. Áp dụng nhất quán ở Android client trong `:feature:chat`: [LeaveGroupUseCase.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/domain/usecase/LeaveGroupUseCase.kt), [ChatRepositoryImpl.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/data/repository/ChatRepositoryImpl.kt), [GroupError.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/domain/model/GroupError.kt), [ChatInfoScreen.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatInfoScreen.kt) với `GroupError.LastAdminCannotLeave`, bảo vệ cả cấp độ UI Dialog cảnh báo lẫn UseCase exception guard; kiểm chứng bằng unit test [LeaveGroupUseCaseTest.kt](../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/domain/usecase/LeaveGroupUseCaseTest.kt) và [ChatViewModelTest.kt](../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatViewModelTest.kt).
- Quản lý vai trò thành viên nhóm nguyên tử: [ManageGroupMemberUseCase.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/domain/usecase/ManageGroupMemberUseCase.kt) hỗ trợ nâng hạ quyền (`GroupRole.ADMIN` / `GroupRole.MEMBER`) và xóa thành viên khỏi nhóm.
- Chuyển tiếp tin nhắn đa người nhận độc lập (Forwarding): [ForwardMessageUseCase.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/domain/usecase/ForwardMessageUseCase.kt) tạo các bản copy độc lập cho từng recipient trong transactional outbox queue, đảm bảo việc gửi tin vào một hội thoại không làm ảnh hưởng hay phụ thuộc vào các hội thoại khác.
- Thả cảm xúc Dịu (Reactions): [ToggleReactionUseCase.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/domain/usecase/ToggleReactionUseCase.kt) tổng hợp và toggle 6 loại cảm xúc Dịu theo từng user nguyên tử, đồng bộ qua Room entity `reactionsCsv` và UI badge [MessageBubble.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/composable/MessageBubble.kt) kèm modal [ReactDetailDialog.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/composable/ReactDetailDialog.kt).
- Mở thư nguyên tử: `open_sealed_letter` kiểm tra `recipient_id = auth.uid()`, `unlock_at <= now()`, `is_opened = FALSE` trong một câu lệnh UPDATE nguyên tử.
- Điểm danh cảm xúc và note riêng: `upsert_daily_mood_checkin` ghi nhận đồng thời `mood_checkins`, `mood_private_notes` và cập nhật `current_mood` trong một transaction an toàn.
- Idempotency trả lại cùng kết quả cho cùng command; optimistic concurrency chặn command mới dựa trên state cũ. Hai việc khác nhau.
- Không trao quyền đổi role/content/recipient tùy ý chỉ vì caller được UPDATE row.
- Bài tập: hai client cùng sửa/hủy thư, hoặc tạo direct cùng thời điểm; kiểm một kết quả được xác định.
- Hoàn thành khi: server không còn phụ thuộc chuỗi request client chạy “đúng thứ tự may mắn”.

### T16 — RLS, column privilege, projection và quyền Storage

**Vượt flow basic:** client Android không đáng tin để quyết định ai được đọc gì.

- Đã triển khai migration: [00002_db01_security_definer_hardening.sql](../../migrations/00002_db01_security_definer_hardening.sql), [00003_db02_conversation_members_security.sql](../../migrations/00003_db02_conversation_members_security.sql), [00004_db05_profiles_and_settings_separation.sql](../../migrations/00004_db05_profiles_and_settings_separation.sql), [00005_db06_mood_privacy_and_note_separation.sql](../../migrations/00005_db06_mood_privacy_and_note_separation.sql).
- Khóa `SET search_path = public, pg_temp` trên tất cả hàm `SECURITY DEFINER` để chống tấn công schema poisoning.
- Thu hồi quyền tự insert vào `conversation_members`; áp dụng trigger `tr_conversation_member_security` chống tự nâng role lên admin và bảo vệ các cột bất biến (`conversation_id`, `user_id`, `joined_at`).
- Tách biệt hoàn toàn thông tin cá nhân/cài đặt nhạy cảm sang bảng `user_settings` với policy RLS chỉ duy nhất `user_id = auth.uid()` được truy cập; bạn bè trong `contacts` không thể đọc lén số điện thoại, ngày sinh hay cài đặt app.
- Tách dòng ghi chú cảm xúc cá nhân sang `mood_private_notes` với RLS chỉ chủ tài khoản đọc được; bạn bè trong allowlist `shared_with` chỉ được đọc màu tâm trạng của ngày (`mood_id`), không đọc được ghi chú.
- Loại bỏ policy rò rỉ mảng `emergency_allowed` trên `quiet_schedules`; cung cấp RPC `is_contact_in_quiet_hours` chỉ trả về BOOLEAN.
- Rút quyền EXECUTE từ PUBLIC, chỉ cấp cho `authenticated` và `service_role`.
- Bài tập: dùng user C gọi API với ID của A/B, thử tự join group và tải file biết path.
- Hoàn thành khi: ma trận quyền chạy qua API thật, không chỉ UI ẩn nút.
- Tham khảo: [Supabase database functions](https://supabase.com/docs/guides/database/functions).
- Tham khảo: [Supabase database functions](https://supabase.com/docs/guides/database/functions).

### T17 — Private Broadcast/Presence, TTL và throttle

**Vượt flow basic:** dữ liệu thoáng qua cần độ mới và quyền kênh, không chỉ một bảng DB.

- Typing phát vừa đủ, tự hết TTL nếu app chết; không viết DB trên từng phím.
- Signaling và live coordinates dùng channel riêng, payload có session/event/version/time phù hợp.
- Topic UUID khó đoán không thay authorization; phải kiểm membership/session và cơ chế ngắt/re-auth khi revoke.
- Reaction cần persist SQL; nếu Broadcast phản hồi nhanh thì phải reconcile về bản bền.
- Không khẳng định Broadcast luôn miễn phí/latency vài ms; theo dõi quota, connection/message/egress.
- Bài tập: tắt process khi đang typing; chấm gõ tự biến mất; C biết topic vẫn không join được.
- Tham khảo: [Supabase Realtime authorization](https://supabase.com/docs/guides/realtime/authorization).

### T18 — Server time, timezone và scheduler

**Vượt flow basic:** thời gian là dữ liệu nghiệp vụ, không phải chỉ format chuỗi giờ ở UI.

- Đã triển khai trong `:feature:settings`: [QuietSchedule.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/domain/model/QuietSchedule.kt) mô hình hoá khung giờ yên lặng bằng `startMinuteOfDay` và `endMinuteOfDay` (mặc định 22:30 [1350] – 06:00 [360]), gắn với `timeZoneId` ("Asia/Ho_Chi_Minh") và danh sách ngoại lệ khẩn cấp `allowedEmergencyContactIds`.
- Thuật toán `isInQuietHours(minuteOfDay)` giải quyết triệt để trường hợp khung giờ qua nửa đêm (`startMinuteOfDay > endMinuteOfDay`, e.g. 22:30 tối đến 06:00 sáng hôm sau): `minuteOfDay >= startMinuteOfDay || minuteOfDay < endMinuteOfDay`, tránh lỗi logic phổ biến khi chỉ so sánh `minute in start..end`.
- Lưu Instant UTC cho unlock/expiry; local date cho nhật ký theo timezone tài khoản; dùng Clock injectable cho test.
- Quiet qua đêm gắn ngày bắt đầu; ví dụ bật thứ Hai 22:30–06:00 thì 01:00 thứ Ba vẫn thuộc phiên thứ Hai.
- Scheduler tạo release/digest job khi tới hạn; quyền đọc thư vẫn kiểm server deadline, độc lập việc app có mở hay cron vừa chạy.
- Cron có thể chạy lại/chậm; state transition và notification job idempotent, hiển thị thời gian dự kiến trung thực.
- SQL hiện hard-code timezone Việt Nam, cron mới là ghi chú; không coi đã có scheduler.
- Bài tập: test 23:59→00:00, hết tháng, timezone khác và chỉnh đồng hồ thiết bị.
- Hoàn thành khi: “sáng mai” thành một thời điểm cụ thể, và người dùng không mở thư sớm bằng đổi giờ máy.

### T19 — Notification pipeline, privacy renderer và digest

**Vượt flow basic:** một message có nhiều cách thông báo tùy người nhận, thiết bị, giờ, block và màn đang mở.

- Đã triển khai trong `:feature:settings`: [NotificationPrivacy.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/domain/model/NotificationPrivacy.kt) chuẩn hóa 3 mức hiển thị thông báo theo REQ-37:
  1. `MOOD_COLOR_ONLY` ("Màu cảm xúc"): chỉ hiển thị màu cảm xúc và chỉ báo tin nhắn mới, không hiển thị tên người gửi hoặc nội dung trên màn hình khoá và push notification.
  2. `SENDER_NAME` ("Tên người gửi"): hiển thị tên người gửi và màu cảm xúc, ẩn hoàn toàn nội dung tin nhắn trên thông báo đẩy.
  3. `FULL_CONTENT` ("Đầy đủ nội dung"): hiển thị đầy đủ tên, màu cảm xúc và nội dung tin nhắn.
- Quản lý chặn người dùng tại [BlockedContactsScreen](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/presentation/screen/SettingsScreen.kt) và use case [UnblockContactUseCase.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/domain/usecase/UnblockContactUseCase.kt): đảm bảo khi người dùng bị chặn, hệ thống chặn gửi push notification, cuộc gọi và cập nhật trạng thái theo REQ-40.
- Đề xuất DB commit event/job → backend policy → FCM → device renderer; dedupe bằng event/message ID và device delivery key.
- Mode color không chứa tên/body ở payload/public view; name chỉ safe name; full theo policy. Không gửi dữ liệu nhạy cảm rồi chỉ che bằng layout.
- App đang mở chat có thể bỏ banner cho chat đó; màn khác có banner; notification tap qua auth gate.
- Group digest tổng hợp deterministic theo kỳ/người nhận, không cần AI và không hứa push đúng giờ tuyệt đối.
- Quiet của Dịu khác DND Android; channel/sound do OS/người dùng kiểm soát. Fullscreen incoming phải theo quyền và hạn chế thực tế.
- Bài tập: ma trận foreground/background × color/name/full × quiet/block; kiểm nội dung payload và notification.
- Tham khảo: [FCM receiving](https://firebase.google.com/docs/cloud-messaging/android/receive-messages), [Android notifications](https://developer.android.com/design/ui/mobile/guides/home-screen/notifications).

### T30 — Privacy projection và thống kê trên dữ liệu được phép

**Vượt flow basic:** aggregate có thể làm lộ thông tin dù không trả row gốc.

- Đã triển khai migration: [00004_db05_profiles_and_settings_separation.sql](../../migrations/00004_db05_profiles_and_settings_separation.sql) và [00005_db06_mood_privacy_and_note_separation.sql](../../migrations/00005_db06_mood_privacy_and_note_separation.sql).
- Tạo view an toàn `safe_user_profiles` chiếu các trường công khai (`id`, `display_name`, `username`, `bio`, `avatar_url`, `onboarding_completed`), che giấu số điện thoại và cài đặt riêng tư.
- Bảo vệ `current_mood` theo DEC-08: cung cấp RPC `get_contact_current_mood` kiểm tra allowlist `shared_with` của ngày hiện tại, không cho phép dùng `current_mood` trên profile để lén đọc tâm trạng của người chưa chia sẻ.
- Safe quiet hours projection: RPC `is_contact_in_quiet_hours(p_contact_user_id)` chỉ cho phép người trong danh bạ kiểm tra trạng thái boolean đang nghỉ, không làm lộ mảng `emergency_allowed`, ngày kích hoạt hay giờ giấc chi tiết.
- Áp dụng trong `:feature:journal` (P9 Mood Check-in & Journal, REQ-26, REQ-27, DEC-08):
  - Check-in cảm xúc hàng ngày ([MoodCheckinSheet.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/checkin/MoodCheckinSheet.kt), [DailyMoodCheckin.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/domain/model/DailyMoodCheckin.kt), [SaveMoodCheckinUseCase.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/domain/usecase/SaveMoodCheckinUseCase.kt)): Tách biệt hoàn toàn giữa màu cảm xúc chia sẻ và ghi chú riêng tư (`private note`). Note chỉ lưu trữ và hiển thị cho chính người check-in.
  - Allowlist chia sẻ màu cảm xúc (`sharedWithContactIds`): Người dùng tự chọn ai trong danh bạ được thấy màu tâm trạng hôm nay, người ngoài allowlist không thể thấy màu hay trạng thái.
  - Thống kê tháng và so sánh "Cùng Minh" ([JournalScreen.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/journal/JournalScreen.kt), [MoodComparison.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/domain/model/MoodComparison.kt), [MonthMoodBreakdown.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/domain/model/MonthMoodBreakdown.kt)): Tổng hợp tỷ lệ % cảm xúc trong tháng và so sánh các ngày trùng tâm trạng giữa hai người mà không tiết lộ nội dung private notes. Đã kiểm chứng qua [GetMonthlyMoodCheckinsUseCaseTest.kt](../../../feature/journal/src/test/kotlin/com/uzuu/diuchat/feature/journal/domain/usecase/GetMonthlyMoodCheckinsUseCaseTest.kt), [SaveMoodCheckinUseCaseTest.kt](../../../feature/journal/src/test/kotlin/com/uzuu/diuchat/feature/journal/domain/usecase/SaveMoodCheckinUseCaseTest.kt), [GetMoodComparisonUseCaseTest.kt](../../../feature/journal/src/test/kotlin/com/uzuu/diuchat/feature/journal/domain/usecase/GetMoodComparisonUseCaseTest.kt), và [JournalViewModelTest.kt](../../../feature/journal/src/test/kotlin/com/uzuu/diuchat/feature/journal/presentation/journal/JournalViewModelTest.kt).
- Bài tập: rút share của một ngày; refresh comparison không còn tính ngày đó và không giữ note trong cache shared.
- Hoàn thành khi: thống kê/search/header có cùng policy với truy vấn chi tiết.

## 6. Phần media, hệ thống Android và UI

### T20 — Recorder/player ownership, waveform và audio focus

**Vượt flow basic:** recorder/player là tài nguyên cần đóng, không phải field boolean playing trong Composable.

- Đã triển khai trong `:feature:chat`: [VoiceRecordingState.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/domain/model/VoiceRecordingState.kt), [VoiceRecorderBar.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/composable/VoiceRecorderBar.kt), [MessageBubble.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/composable/MessageBubble.kt), [SendVoiceMessageUseCase.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/domain/usecase/SendVoiceMessageUseCase.kt), [ChatViewModel.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatViewModel.kt).
- State machine ghi âm chuẩn xác theo thiết kế: `Idle -> Recording -> Preview -> Uploading` (hoặc hủy về `Idle`).
- Quản lý lifecycle: Người dùng có thể xem trước (Preview) đoạn ghi âm, nghe lại và dừng trước khi gửi, hoặc hủy bỏ (Delete) để dọn dẹp tài nguyên và trạng thái tạm mà không rò rỉ.
- Metering Waveform: Mảng biên độ âm thanh `audioWaveform: List<Float>` được đo lường, lưu trữ và render trực quan qua các thanh biên độ (bars) trên cả thanh ghi âm và trong `MessageBubble` với viền màu mood.
- Player ownership & scrub: Trạng thái phát âm thanh được quản lý tập trung qua `playingAudioMessageId` và `audioPlaybackProgress` trong `ChatUiState`. Khi một tin nhắn phát thì tin nhắn khác tự động dừng, thanh waveform tô màu tiến trình đã phát (`isPlayed`) theo màu sắc thái cảm xúc của tin nhắn.
- Bài tập: play voice A rồi B, xoay máy, nhận cuộc gọi; kiểm không còn recorder/player mồ côi. Đã kiểm chứng qua [ChatViewModelTest.kt](../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatViewModelTest.kt) và [SendVoiceMessageUseCaseTest.kt](../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/domain/usecase/SendVoiceMessageUseCaseTest.kt).
- Hoàn thành khi: xóa temp file đúng lúc và mic indicator biến mất sau hủy.

### T21 — Attachment pipeline, signed URL và cache index

**Vượt flow basic:** file trải qua nhiều giai đoạn có thể lỗi độc lập với record message.

- Đã triển khai trong `:feature:chat`: [MessageMediaType.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/domain/model/MessageMediaType.kt), [SendPhotoMessageUseCase.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/domain/usecase/SendPhotoMessageUseCase.kt), [ChatComposer.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/composable/ChatComposer.kt), [MessageBubble.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/composable/MessageBubble.kt), [ChatRepositoryImpl.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/data/repository/ChatRepositoryImpl.kt).
- Pipeline đính kèm ảnh & chụp ảnh:
  - Dùng controller dùng chung trong `:feature:base`: [MediaPickerHelper.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/presentation/util/MediaPickerHelper.kt) và [DiuMediaSourceSheet.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/presentation/compose/composable/DiuMediaSourceSheet.kt).
  - **Photo Picker (`ActivityResultContracts.PickVisualMedia`)**: mở bộ chọn ảnh hệ thống (`ImageOnly`) mà không cần xin quyền `READ_EXTERNAL_STORAGE`.
  - **Camera Intent (`ActivityResultContracts.TakePicture`)**: tạo file tạm trong `cacheDir/camera/` qua `MediaFileProvider.createCameraImageUri` kết hợp `FileProvider` ([file_paths.xml](../../../app/src/main/res/xml/file_paths.xml), [AndroidManifest.xml](../../../app/src/main/AndroidManifest.xml)). Lưu `pendingCameraUri` bằng `rememberSaveable` để không mất URI khi Activity bị recreate trong lúc mở app Camera hệ thống.
  - Áp dụng tại `:feature:chat` ([ChatScreen.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatScreen.kt)) để chọn/chụp ảnh đính kèm tin nhắn, và tại `:feature:settings` ([EditProfileSheet.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/presentation/composable/EditProfileSheet.kt), [DiuOrbAvatar.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/presentation/compose/composable/DiuOrbAvatar.kt)) để chọn/chụp ảnh đại diện cá nhân.
  - Trade-off: không khai báo quyền `CAMERA` trong manifest để dùng `TakePicture()` không cần runtime permission, đổi lại phụ thuộc app Camera mặc định của thiết bị; URI ảnh chụp nằm trong `cacheDir/camera/` nên cần upload/copy ra lưu trữ bền vững khi đồng bộ lên backend.
- Hiển thị linh hoạt: `MessageBubble` tự động phân loại `MessageMediaType.PHOTO`, render ảnh qua Coil `AsyncImage` với góc bo 12.dp, viền accent màu mood, kèm caption và trạng thái gửi (Pending/Sent/Delivered/Read/Failed).
- Lưu trữ SQLite bền vững: `MessageEntity` lưu `mediaType = "PHOTO"`, `mediaUri`, và `caption`, sẵn sàng cho worker upload đồng bộ lên Supabase Storage bucket `chat-attachments` mà không làm mất liên kết khi restart app.
- Áp dụng trong `:feature:media` (P6 Media Gallery & Vault, REQ-29, REQ-30):
  - Kho lưu trữ Media Vault ([MediaVaultScreen.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/vault/MediaVaultScreen.kt), [MediaVaultViewModel.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/vault/MediaVaultViewModel.kt)): Gom nhóm các file đa phương tiện (ảnh, ghi âm, thư đã mở) theo từng tháng (`monthHeader`). Lưới ảnh 3 cột trực quan với tỉ lệ khung hình 1:1, viền mood glow, thẻ ghi âm (`VoiceVaultCard`) kèm nhãn thời lượng/nút play nghe thử tức thì, thẻ thư đã mở (`LetterVaultCard`) trích dẫn thư tay và thời gian mở, tích hợp thanh lọc `DiuFilterChip` (Tất cả, Ảnh, Ghi âm, Thư).
  - Trình xem ảnh Photo Viewer toàn màn hình ([PhotoViewerScreen.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/viewer/PhotoViewerScreen.kt)): Trải nghiệm xem ảnh toàn màn hình (immersive photo viewer), hỗ trợ chạm để ẩn/hiện thanh điều khiển mượt mà với animation `AnimatedVisibility` (FadeIn/FadeOut, SlideIn/SlideOut), hiển thị người gửi, timestamp nhân văn, mood tag, caption và nút chia sẻ (Share action).
  - Thống kê dung lượng trực quan ([StorageScreen.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/storage/StorageScreen.kt), [StorageViewModel.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/storage/StorageViewModel.kt)): Biểu đồ phân bổ dung lượng đa đoạn (Ảnh, Ghi âm, Bộ nhớ đệm) với màu sắc chủ đạo, danh sách chi tiết mức sử dụng theo từng cuộc hội thoại (`ConversationStorage`), và các tuỳ chọn quản lý tải về (Tự động tải qua Wi-Fi, Tự động tải tin nhắn thoại, Giữ ảnh/âm thanh gốc).
- Bài tập: mất mạng sau upload thành công trước message commit; recovery không tạo object vô chủ mãi. Đã kiểm chứng qua [SendPhotoMessageUseCaseTest.kt](../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/domain/usecase/SendPhotoMessageUseCaseTest.kt), [MediaVaultViewModelTest.kt](../../../feature/media/src/test/kotlin/com/uzuu/diuchat/feature/media/presentation/vault/MediaVaultViewModelTest.kt), và [StorageViewModelTest.kt](../../../feature/media/src/test/kotlin/com/uzuu/diuchat/feature/media/presentation/storage/StorageViewModelTest.kt).
- Hoàn thành khi: phân biệt xóa cache, xóa message và xóa remote object đang được nhiều record tham chiếu.

### T22 — Full-text search, GIN, trigram và normalization

**Vượt flow basic:** tìm kiếm cần chiến lược chỉ mục, chuẩn hóa ngôn ngữ, phân trang và quyền dữ liệu; không chỉ thêm ô input rồi LIKE mọi bảng.

- Áp dụng trong `:feature:journal` (P9 Message & Mood Search, REQ-32, REQ-33):
  - Use case & ViewModel: [SearchMessagesUseCase.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/domain/usecase/SearchMessagesUseCase.kt), [SearchViewModel.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/search/SearchViewModel.kt), [SearchScreen.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/search/SearchScreen.kt).
  - Tìm kiếm tin nhắn theo từ khóa case-insensitive kết hợp lọc theo 6 sắc thái cảm xúc (`JournalMoodType`), hỗ trợ cả phạm vi trò chuyện đơn lẻ (`ChatSearch`) và tìm kiếm toàn cục (`GlobalSearch`).
  - Highlight từ khóa: Sử dụng `buildAnnotatedString` và `SpanStyle` để đánh dấu nổi bật từ khóa trong đoạn trích dẫn kết quả (`SearchResultItem`).
  - Phản ứng tức thì & trạng thái rỗng: Lắng nghe luồng dữ liệu phản ứng từ `JournalRepositoryImpl`, hiển thị `DiuEmptyState` khi không tìm thấy kết quả phù hợp. Đã kiểm chứng qua [SearchMessagesUseCaseTest.kt](../../../feature/journal/src/test/kotlin/com/uzuu/diuchat/feature/journal/domain/usecase/SearchMessagesUseCaseTest.kt) và [SearchViewModelTest.kt](../../../feature/journal/src/test/kotlin/com/uzuu/diuchat/feature/journal/presentation/search/SearchViewModelTest.kt).
- SQL có GIN cho to_tsvector('simple', content), trigram cho display_name và extension unaccent; search_messages hiện chưa áp dụng unaccent và nhận p_user_id nên cần sửa scope.
- FTS tìm token/độ liên quan; trigram hỗ trợ similarity/partial matching theo thiết kế; unaccent hỗ trợ chuẩn hóa dấu. Ba thứ không thay thế lẫn nhau.
- Chuẩn hóa cả dữ liệu/index và query theo cùng quy tắc, gồm đ/Đ và tiếng Việt; không bọc hàm tùy ý rồi mặc định index cũ được dùng.
- Chat search scope theo conversation, global search theo các conversation có quyền; không index trả về nội dung thư chưa mở/note riêng bằng đường vòng.
- Highlight cần map vị trí về chuỗi gốc khi normalization đổi biểu diễn; query mới hủy/bỏ response cũ; offline chỉ tìm phần đã cache và ghi rõ phạm vi.
- Bài tập: fixture có “Bình yên”, query “binh yen”; kiểm kết quả, quyền user C, explain query và phân trang.
- Hoàn thành khi: có/không dấu đúng spec, query lớn bị giới hạn, không dùng một query unbounded tải mọi tin.
- Tham khảo: [PostgreSQL text search indexes](https://www.postgresql.org/docs/current/textsearch-indexes.html).

### T23 — Verified App Links, QR và deferred navigation

**Vượt flow basic:** một URL từ ngoài app phải đi qua xác thực, kiểm dữ liệu và khôi phục đích.

- SQL qr_token và lookup_by_qr_token có rồi, Android app link/QR flow còn cần làm.
- Áp dụng trong `:feature:base` & `:feature:contacts`:
  - [QrCodeGenerator.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/presentation/util/QrCodeGenerator.kt): dùng thư viện ZXing (`com.google.zxing:core`) chuyển đổi `userId` / `inviteLink` thành ma trận `QrMatrix` thuần Kotlin (dễ unit-test trên JVM) và `Bitmap` (`ARGB_8888`) ngay trên thiết bị, không gọi API bên ngoài.
  - [DiuQrCodeImage.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/presentation/compose/composable/DiuQrCodeImage.kt): tạo `Bitmap` bất đồng bộ trên `Dispatchers.Default` qua `produceState` để không block UI thread, bọc trong khung nền trắng cố định giúp máy quét đọc ổn định cả ở Dark Mode.
  - [DiuInviteCodeCard.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/presentation/compose/composable/DiuInviteCodeCard.kt) kết hợp [ContactsScreen.kt](../../../feature/contacts/src/main/kotlin/com/uzuu/diuchat/feature/contacts/presentation/screen/ContactsScreen.kt) hiển thị mã QR cá nhân (`https://${uiState.myInviteLink}`) cùng đường dẫn kết nối để sao chép nhanh mà không lộ số điện thoại.
  - Kiểm chứng: [QrCodeGeneratorTest.kt](../../../feature/base/src/test/kotlin/com/uzuu/diuchat/feature/base/presentation/util/QrCodeGeneratorTest.kt) kiểm tra tính tất định của `encodeMatrix`, khác biệt giữa các payload và guard input rỗng. Phần quét QR bằng CameraX/MLKit và deep-link cold-start vẫn chưa triển khai ở bước này.
- QR encode URL canonical; decoder chỉ nhận scheme/host/path/token hợp lệ, không tự mở arbitrary URI hoặc coi QR là auth token.
- assetlinks.json + signing fingerprint xác thực app/domain; domain diu.vn mới là mock cho tới khi có quyền sở hữu.
- Chưa login: giữ pending target đã validate → login/profile → safe lookup → xem profile/kết nối; không bypass quyền chỉ vì tới từ link.
- Bài tập: cold-start app từ QR hợp lệ, token sai, user bị xóa và link khi signed-out.
- Hoàn thành khi: không mất đích sau auth và không thêm một màn login mỗi lần quay lại app.

### T24 — WebRTC signaling, ICE/STUN/TURN và media adapter

**Vượt flow basic:** gọi điện gồm control plane và media plane có vòng đời độc lập.

- **Đã triển khai trong `:feature:calls` (REQ-22, REQ-23, REQ-24, REQ-25, Mốc P10):**
  - [RtcSignalingMessage.kt](../../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/domain/model/RtcSignalingMessage.kt): Định nghĩa các sự kiện signaling `@Serializable` `SignalingType` (`OFFER`, `ANSWER`, `ICE_CANDIDATE`, `BYE`, `DECLINE`) tách biệt rõ rệt giữa đường truyền điều khiển phiên và luồng media dữ liệu thực tế.
  - [WebRtcSignalingAdapter.kt](../../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/data/WebRtcSignalingAdapter.kt): Trừu tượng hóa signaling qua Supabase Realtime Broadcast trên channel `calls:<sessionId>` với event `"webrtc_signal"` (`channel.broadcastFlow<RtcSignalingMessage>` và `channel.broadcast`), tự fallback sang local `MutableSharedFlow` khi `supabaseClient == null` (unit test/offline). Cung cấp `connect`, `sendSignaling`, và `disconnect` (gửi `BYE`, hủy collection job và `unsubscribe` channel an toàn).
  - [CallRepositoryImpl.kt](../../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/data/repository/CallRepositoryImpl.kt): Lắng nghe `webRtcSignalingAdapter.signalingEvents` để tự động chuyển phiên sang `ACTIVE` khi nhận `ANSWER` (hoặc kết thúc khi nhận `BYE`/`DECLINE` từ remote peer), đồng thời gửi `ANSWER` khi `acceptCall` và `DECLINE` khi `rejectCall`.
  - [WebRtcSignalingAdapterTest.kt](../../../../feature/calls/src/test/kotlin/com/uzuu/diuchat/feature/calls/data/WebRtcSignalingAdapterTest.kt): Kiểm thử tự động việc phát sinh OFFER và trao đổi gói tin signaling ICE candidate.
- Signaling truyền offer/answer/ICE/session event; Supabase không trở thành máy chủ truyền audio/video chỉ vì có Broadcast.
- ICE chọn đường kết nối; STUN hỗ trợ tìm địa chỉ; TURN relay khi không nối trực tiếp. TURN cần credential/expiry, quota và đo băng thông.
- CallSession server giữ trạng thái/TTL, RTC engine là adapter thực thi media; có thể dùng engine managed sau một ADR có bằng chứng.
- Xử lý ICE trước remote description, late/duplicate candidate, disconnect/reconnect/end; chỉ log metadata kỹ thuật cần thiết, tránh log SDP/địa chỉ nhạy cảm.
- Audio-first, sau đó video tracks/camera switch/self preview; test hai mạng khác nhau chứ không chỉ emulator cùng máy.
- Bài tập: ép relay/TURN trong môi trường kiểm thử, hai phía vẫn nghe được và stats thể hiện route.
- Tham khảo: [WebRTC peer connections](https://webrtc.org/getting-started/peer-connections).

### T25 — Call lifecycle, Telecom và service

**Vượt flow basic:** cuộc gọi không được biến mất hoặc tạo lại chỉ vì Activity xoay máy.

- **Đã triển khai trong `:feature:calls` (REQ-22, REQ-23, REQ-24, REQ-25, Mốc P10):**
  - [CallServiceCoordinator.kt](../../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/data/CallServiceCoordinator.kt): Điều phối `AudioManager` thực tế (chuyển `MODE_IN_COMMUNICATION` khi bắt đầu và trả `MODE_NORMAL` khi kết thúc, bật/tắt loa ngoài bằng `setCommunicationDevice(TYPE_BUILTIN_SPEAKER)` trên API 31+ hoặc `isSpeakerphoneOn` fallback, điều khiển `isMicrophoneMute`), cùng trạng thái camera và vòng đời cuộc gọi độc lập với Activity/Compose (`Context` optional để vẫn unit-test được trên JVM).
  - [CallRepositoryImpl.kt](../../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/data/repository/CallRepositoryImpl.kt): Quản lý State Machine đơn điệu (monotonic state transitions: `IDLE` -> `INVITING`/`RINGING` -> `CONNECTING` -> `ACTIVE` -> `ENDED`). Ngăn chặn trạng thái hồi sinh cuộc gọi đã kết thúc (`ENDED` không thể quay lại `ACTIVE`). Tích hợp coroutine duration ticker theo giây khi ở trạng thái `ACTIVE` và tự động lưu `CallLog` vào bộ nhớ đệm lịch sử cuộc gọi khi phiên kết thúc.
  - [CallViewModel.kt](../../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallViewModel.kt), [CallScreen.kt](../../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallScreen.kt) & [AndroidManifest.xml](../../../../app/src/main/AndroidManifest.xml): UI chỉ đóng vai trò quan sát reactive `currentCallSession` StateFlow, tích hợp `rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions())` xin quyền runtime `RECORD_AUDIO` (và `CAMERA` khi `isVideo == true`), khai báo đầy đủ call permissions (`RECORD_AUDIO`, `CAMERA`, `MODIFY_AUDIO_SETTINGS`, `BLUETOOTH_CONNECT`, `FOREGROUND_SERVICE_MICROPHONE`, `FOREGROUND_SERVICE_CAMERA`).
  - [CallRepositoryImplTest.kt](../../../feature/calls/src/test/kotlin/com/uzuu/diuchat/feature/calls/data/repository/CallRepositoryImplTest.kt) & [CallServiceCoordinatorTest.kt](../../../feature/calls/src/test/kotlin/com/uzuu/diuchat/feature/calls/data/CallServiceCoordinatorTest.kt): Kiểm thử tự động kiểm chứng transition đơn điệu, chặn chuyển từ ENDED sang ACTIVE, và kiểm tra audio route.
- Đề xuất CallCoordinator/service owner, screen quan sát state; incoming/accept/end có idempotency và session expiry.
- Telecom/Core-Telecom và foreground service được chọn theo API/use case, không chỉ thêm mọi permission rồi mong chạy nền.
- Một người có nhiều device: server claim accept một device, các device còn lại dừng ringing.
- Android audio route/focus, Bluetooth, mic/camera permission và background restrictions cần test thật.
- Quick-decline message cần command key tránh gửi hai lần do double tap/retry.
- Bài tập: nhận call từ notification khi app background, rotate trong Active, hai bên end đồng thời.
- Tham khảo: [Foreground service types](https://developer.android.com/about/versions/14/changes/fgs-types-required).

### T26 — Live location session, expiry và adaptive sampling

**Vượt flow basic:** quyền chia sẻ là một phiên có thời hạn, không phải một Flow GPS bật vĩnh viễn.

- **Đã triển khai trong `:feature:location` (REQ-34, REQ-35, Mốc P11):**
  - [LiveLocationSession.kt](../../../feature/location/src/main/kotlin/com/uzuu/diuchat/feature/location/domain/model/LiveLocationSession.kt), [LocationSnapshot.kt](../../../feature/location/src/main/kotlin/com/uzuu/diuchat/feature/location/domain/model/LocationSnapshot.kt): mô hình hoá phiên chia sẻ vị trí trực tiếp gắn với `sessionId`, `conversationId`, `startedAt`, `expiresAt = startedAt + durationMinutes`, `isActive` và `lastKnownLocation`. Trạng thái hết hạn `isExpired` và thời gian còn lại `remainingSeconds` tính toán dựa trên mốc thời gian tuyệt đối (`Instant.now().isAfter(expiresAt)`) thay vì phụ thuộc biến đếm cục bộ, ngăn chặn việc sửa đồng hồ máy hay xoay màn hình làm sai lệch phiên.
  - [LocationProviderAdapter.kt](../../../feature/location/src/main/kotlin/com/uzuu/diuchat/feature/location/data/adapter/LocationProviderAdapter.kt) & [DefaultLocationProviderAdapter.kt](../../../feature/location/src/main/kotlin/com/uzuu/diuchat/feature/location/data/adapter/DefaultLocationProviderAdapter.kt): trừu tượng hoá nguồn cung cấp toạ độ và thuật toán lấy mẫu thích ứng `getAdaptiveSamplingIntervalMs(speedMps, accuracyMeters)` nhằm tiết kiệm pin:
    * Người dùng đứng yên (`speedMps < 0.5 m/s`): giãn chu kỳ lấy mẫu lên 30.000ms (30 giây) để giảm tải GPS/mạng.
    * Người dùng đi bộ (`0.5 <= speedMps <= 4.0 m/s`): chu kỳ 10.000ms (10 giây).
    * Người dùng di chuyển nhanh trên xe cơ giới (`speedMps > 4.0 m/s`): chu kỳ thu ngắn còn 3.000ms (3 giây) để toạ độ không bị giật.
    * Khi độ chính xác thấp (`accuracyMeters > 50m`): giãn chu kỳ 15.000ms tránh liên tục phát tán vị trí sai lệch.
  - [LocationRepositoryImpl.kt](../../../feature/location/src/main/kotlin/com/uzuu/diuchat/feature/location/data/repository/LocationRepositoryImpl.kt): quản lý vòng đời phiên chia sẻ qua `Flow<LiveLocationSession?>`. Kiểm tra điều kiện hết hạn trước khi cập nhật toạ độ mới; hàm `stopLiveSharing` lập tức cập nhật `isActive = false` và phát ra luồng mới để consumer dừng hiển thị.
  - Presentation & Composable UI:
    * [LocationPickerScreen.kt](../../../feature/location/src/main/kotlin/com/uzuu/diuchat/feature/location/presentation/LocationPickerScreen.kt) & [LocationPickerViewModel.kt](../../../feature/location/src/main/kotlin/com/uzuu/diuchat/feature/location/presentation/LocationPickerViewModel.kt): chuyển đổi linh hoạt giữa vị trí tĩnh (REQ-34) và chia sẻ trực tiếp (REQ-35); hỗ trợ bộ chọn thời lượng [LiveDurationPicker.kt](../../../feature/location/src/main/kotlin/com/uzuu/diuchat/feature/location/presentation/LiveDurationPicker.kt) (15 phút, 1 giờ, 8 giờ).
    * [LiveLocationScreen.kt](../../../feature/location/src/main/kotlin/com/uzuu/diuchat/feature/location/presentation/LiveLocationScreen.kt) & [LiveLocationViewModel.kt](../../../feature/location/src/main/kotlin/com/uzuu/diuchat/feature/location/presentation/LiveLocationViewModel.kt): giao diện theo dõi toạ độ thời gian thực với đồng hồ đếm ngược, chỉ báo bán kính độ chính xác [AccuracyRadiusIndicator.kt](../../../feature/location/src/main/kotlin/com/uzuu/diuchat/feature/location/presentation/AccuracyRadiusIndicator.kt), bản đồ mô phỏng sóng radar trực tiếp [DiuMapPreview.kt](../../../feature/location/src/main/kotlin/com/uzuu/diuchat/feature/location/presentation/DiuMapPreview.kt), và nút dừng chia sẻ khẩn cấp màu đỏ (`DiuButtonVariant.Danger`).
- SQL live_locations có metadata/snapshot; Android service/channel/expiry logic đã sẵn sàng trên kiến trúc Clean Architecture.
- Mỗi session có owner/conversation/start/expiry/stop reason; server deadline giữ quyền cuối, UI hiện freshness/accuracy.
- Sampling theo chuyển động/độ chính xác/mạng; không tự chọn “mỗi 3 giây” cho mọi tình huống vì tốn pin.
- Stop/expiry/revoke permission/leave/block phải dừng producer và consumer; đã biết tọa độ cũ không thể bị xóa khỏi trí nhớ người nhận.
- Bài tập: share 15 phút, đổi đồng hồ máy, tắt mạng và revoke permission; session vẫn kết thúc đúng quy tắc.
- Hoàn thành khi: notification dừng chia sẻ có tác dụng, không tự hồi sinh phiên sau restart khi chưa có consent; đã kiểm chứng đầy đủ qua bộ unit test `:feature:location:test`.

### T27 — Design tokens, Ambient Glassmorphism, Custom Canvas Draw (`QuietHoursDial`, Dashed Borders) và Motion có kiểm soát (`DiuRippleOrb`, Orb Pulse)

**Vượt flow basic:** hệ thống giao diện biểu đạt cảm xúc bằng kiến trúc nền 3 lớp kính mờ (Ambient Glassmorphism) tương thích mọi phiên bản Android, kết hợp custom Compose `Canvas` hình học (đồng hồ 24h qua đêm, viền nét đứt, bản đồ thu nhỏ) và các chu kỳ chuyển động đồng tâm có kiểm soát, thay vì dùng `AlertDialog`/`TopAppBar` đặc mặc định của Material 3 hoặc gọi `Modifier.blur()` chỉ chạy trên Android 12+.

- **1. Vấn đề cụ thể cần giải quyết & điểm bổ sung so với flow cơ bản:**
  - **Giới hạn `RenderEffect` / `Modifier.blur()` trên Android:** Hiệu ứng `backdrop-filter: blur(24px)` trong bản thiết kế HTML (`docs/context/Diu_Messenger_UI.html`) nếu dịch thẳng sang `Modifier.blur()` của Compose sẽ hoàn toàn không hoạt động trên thiết bị Android 11 trở xuống (API < 31) và gây tốn GPU khi cuộn danh sách dài.
  - **Lỗi mất nền Ambient hoặc đục nền do bọc trùng:** Khi các màn hình con đặt `Scaffold(containerColor = AppTheme.colors.background)` hoặc `Surface(color = AppTheme.colors.background)`, lớp nền đặc sẽ che kín các vệt màu cảm xúc ở root. Ngược lại, nếu bọc `DiuAmbientBackground` ở cả `MainShowcaseScreen` lẫn màn hình con (`HomeScreen`, `MediaVaultScreen`, `StorageScreen`), lớp phủ kính sữa (`glassOverlay`) bị nhân đôi làm đục màu.
  - **Biểu diễn hình học thời gian & trạng thái niêm phong:** Khung giờ yên lặng qua nửa đêm (`22:30 – 06:00`), ngày tương lai trong lịch cảm xúc, thư hẹn đang chờ niêm phong và sóng gọi thoại cần hình học trực quan (`Canvas` arc 24h, dashed stroke, concentric ripple rings) mà widget Material chuẩn không có sẵn.
- **2. Feature/ticket liên quan & kiến trúc hoạt động (Luồng dữ liệu / Vòng đời):**
  - **Kiến trúc 3 lớp Ambient Glassmorphism (UI Overhaul Tasks 1, 2, 7):**
    1. *Lớp 1 — Solid Base & Radial Gradient Blobs (`DiuAmbientBackground`):* Đặt duy nhất tại root `MainShowcaseScreen` (và biến thể nhuộm màu cảm xúc đối phương `primaryBlobColor = peerMoodColor` tại `CallScreen`). Dùng `Canvas` vẽ 3 vệt màu `Brush.radialGradient` lớn (`primaryBlobColor`, `secondaryBlobColor`, `tertiaryBlobColor`) với độ trong suốt mềm (`0.12f..0.38f`) để mô phỏng độ tán xạ ánh sáng mà không cần post-processing blur.
    2. *Lớp 2 — Frosted Glass Overlay & Transparent Child Containers:* Phủ lớp `colors.glassOverlay` lên trên Canvas; toàn bộ 14 màn hình con đặt `containerColor = Color.Transparent` (trừ `PhotoViewerScreen` dùng nền tối `AppPalette.ViewerBg`). Ở `MainShowcaseScreen`, khi thanh đảo nổi `BottomNavigationBar` hiển thị (`!hideBottomBar`), `Scaffold` bỏ cắt cứng `innerPadding.calculateBottomPadding()` và nhường cho danh sách con tự đặt `contentPadding(bottom = 110.dp)` để nội dung cuộn mượt xuyên qua lớp kính của thanh điều hướng.
    3. *Lớp 3 — Elevated Glass Cards & Mood Borders:* Các thẻ nội dung, bong bóng chat `.brow.them` và thanh viên thuốc `.composer` (`58.dp`) dùng `colors.surface` / `colors.surfaceElevated` kết hợp viền mảnh `1.dp` (`colors.line` hoặc `moodColor.copy(alpha = 0.55f)`).
  - **Đồng hồ 24h qua đêm (`QuietHoursDial` — Task 5):**
    - Chuyển đổi `startMinuteOfDay` và `durationMinutes` (tính cả trường hợp qua nửa đêm `(1440 - start) + end`) sang hệ góc độ của `Canvas.drawArc` (gốc 0h nằm ở đỉnh `-90f`):
      - `startAngle = (schedule.startMinuteOfDay / 1440f) * 360f - 90f`
      - `sweepAngle = (schedule.durationMinutes / 1440f) * 360f`
    - Vẽ vòng nền `colors.line` và cung tròn hoạt động màu tím oải hương `AppPalette.QuietPurple` với `Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)`, kèm 4 mốc giờ `0, 6, 12, 18` và thông tin giờ nghỉ ở tâm.
  - **Viền nét đứt (`PathEffect.dashPathEffect`) cho trạng thái chờ & tương lai (Tasks 3, 7):**
    - Dùng `Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f))` trên `Canvas` / `drawBehind` cho: bong bóng thư hẹn niêm phong `.sealed` trong `MessageBubble`, thẻ thư chờ `.lcard.pend` trong `LettersScreen`, vòng tròn ngày tương lai trong `JournalScreen` `CalendarGrid`, và nút `"+ Mời thêm người thân thiết"` (`.invite-row`) trong `ContactsScreen`.
  - **Chuyển động có kiểm soát (`DiuRippleOrb`, `DiuOrbAvatar` pulse & badge — Tasks 1, 4, 6, 7):**
    - `DiuRippleOrb`: Dùng một `rememberInfiniteTransition` duy nhất chạy `basePhase` từ `0f..1f` trong `3600ms` (`LinearEasing`). Hàm thuần `calculateRippleRingProgress(basePhase, ringIndex, ringCount = 3)` tính độ lệch pha `1/3` chu kỳ (`(basePhase + ringIndex / 3f) % 1f`) cho 3 vòng tròn đồng tâm mở rộng từ bán kính `0.5f` đến `0.98f` với độ mờ giảm dần `(1f - progress) * 0.55f`.
    - `DiuOrbAvatar`: Hỗ trợ `isPulsing = true` (nhịp thở `scale 1.0f -> 1.04f` chu kỳ 5s cho màn Hồ sơ `"Tôi"`, `ChatInfoScreen` và tâm `DiuRippleOrb`), `badgeText` (hiển thị số lượng thành viên nhóm nhỏ góc dưới phải trong `NewGroupScreen` và `ChatInfoScreen`), hàm guard `validateOrbSize` chặn kích thước `<= 0` hoặc `NaN`, và hàm thuần `parseMoodColorHex` parse mã màu `#RRGGBB`/`#AARRGGBB` trên JVM với fallback an toàn về `AppPalette.MoodThuong`.
- **3. Đường dẫn source thật & symbol quan trọng:**
  - Base tokens & primitives: [AppPalette.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/common/res/AppPalette.kt), [AppTheme.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/common/res/AppTheme.kt), [AppTypography.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/common/res/AppTypography.kt), [DiuAmbientBackground.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/presentation/compose/composable/DiuAmbientBackground.kt) (`DiuAmbientBackground`), [DiuOrbAvatar.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/presentation/compose/composable/DiuOrbAvatar.kt) (`DiuOrbAvatar`, `validateOrbSize`, `parseMoodColorHex`), [DiuRippleOrb.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/presentation/compose/composable/DiuRippleOrb.kt) (`DiuRippleOrb`, `calculateRippleRingProgress`), [DiuTabHeader.kt](../../../feature/base/src/main/kotlin/com/uzuu/diuchat/feature/base/presentation/compose/composable/DiuTabHeader.kt) (`DiuTabHeader`).
  - App Shell & Navigation: [MainShowcaseScreen.kt](../../../app/src/main/kotlin/com/uzuu/diuchat/app/presentation/MainShowcaseScreen.kt), [NavigationRoute.kt](../../../app/src/main/kotlin/com/uzuu/diuchat/app/presentation/NavigationRoute.kt) (`NavigationRoute.Call`).
  - Chat, Info & Group: [ChatScreen.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatScreen.kt), [ChatComposer.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/composable/ChatComposer.kt), [MessageBubble.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/composable/MessageBubble.kt), [ChatInfoScreen.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatInfoScreen.kt), [NewGroupScreen.kt](../../../feature/chat/src/main/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/NewGroupScreen.kt).
  - Settings & Quiet Dial: [QuietHoursDial.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/presentation/composable/QuietHoursDial.kt) (`QuietHoursDial`), [SettingsScreen.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/presentation/screen/SettingsScreen.kt).
  - Calls: [CallScreen.kt](../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallScreen.kt), [IncomingCallBanner.kt](../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/presentation/composable/IncomingCallBanner.kt) (`IncomingCallFullScreen`), [CallControls.kt](../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/presentation/composable/CallControls.kt), [CallHistoryScreen.kt](../../../feature/calls/src/main/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallHistoryScreen.kt).
  - Home, Contacts, Journal, Letters, Media, Location: [HomeScreen.kt](../../../feature/home/src/main/kotlin/com/uzuu/diuchat/feature/home/presentation/HomeScreen.kt), [ContactsScreen.kt](../../../feature/contacts/src/main/kotlin/com/uzuu/diuchat/feature/contacts/presentation/screen/ContactsScreen.kt), [JournalScreen.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/journal/JournalScreen.kt), [MoodCheckinSheet.kt](../../../feature/journal/src/main/kotlin/com/uzuu/diuchat/feature/journal/presentation/checkin/MoodCheckinSheet.kt), [LettersScreen.kt](../../../feature/letters/src/main/kotlin/com/uzuu/diuchat/feature/letters/presentation/screen/LettersScreen.kt), [MediaVaultScreen.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/vault/MediaVaultScreen.kt), [StorageScreen.kt](../../../feature/media/src/main/kotlin/com/uzuu/diuchat/feature/media/presentation/storage/StorageScreen.kt), [LocationPickerScreen.kt](../../../feature/location/src/main/kotlin/com/uzuu/diuchat/feature/location/presentation/LocationPickerScreen.kt).
- **4. Lý do chọn, trade-off, lỗi dễ gặp và giới hạn còn lại:**
  - *Lý do chọn & Trade-off:* Vẽ radial gradient blobs + translucent surface tokens cho hiệu năng 60fps ổn định và giao diện đồng nhất từ Android 8 đến Android 15, đánh đổi việc không làm nhòe trực tiếp chữ/ảnh nằm ngay dưới tấm thẻ kính (chỉ làm nhòe nền ambient).
  - *Lỗi dễ gặp:*
    1. Quên đổi `Scaffold` mặc định của Material 3 sang `containerColor = Color.Transparent` khiến màn hình biến thành khối màu đặc.
    2. Gọi `android.graphics.Color.parseColor()` trực tiếp trong helper UI khiến unit test trên local JVM bị crash `Method parseColor in android.graphics.Color not mocked` — đã khắc phục bằng hàm thuần Kotlin `parseMoodColorHex` trong `DiuOrbAvatar.kt`.
    3. Cấp phát `Brush` mới trong mỗi frame vẽ của `DiuOrbAvatar` khi animation pulse chạy — đã tối ưu bằng `Modifier.drawWithCache` chỉ tạo lại `Brush` khi kích thước hoặc `moodColor` thay đổi.
  - *Giới hạn còn lại:* `QuietHoursDial` hiện điều chỉnh giờ qua 2 nút stepper `−` / `+` (`±30` phút) để đảm bảo dễ thao tác và hỗ trợ TalkBack rõ ràng, chưa hỗ trợ kéo ngón tay xoay trực tiếp (drag gesture) trên cung tròn Canvas.
- **5. Cách kiểm chứng & kết quả thực tế:**
  - Unit tests cho logic hình học, màu sắc và state UI:
    - [DiuOrbComponentSpecTest.kt](../../../feature/base/src/test/kotlin/com/uzuu/diuchat/feature/base/presentation/compose/composable/DiuOrbComponentSpecTest.kt): kiểm tra `validateOrbSize`, fallback của `parseMoodColorHex`, và độ lệch pha 3 vòng sóng `calculateRippleRingProgress`.
    - [ChatActionTest.kt](../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatActionTest.kt) & [ChatViewModelTest.kt](../../../feature/chat/src/test/kotlin/com/uzuu/diuchat/feature/chat/presentation/screen/ChatViewModelTest.kt): kiểm tra niêm phong/mở/hủy thư hẹn `SEALED_LETTER` trong chat và giới hạn chọn tối đa 7 bạn (`+1` bản thân = 8 người) khi tạo nhóm nhỏ.
    - [SettingsActionTest.kt](../../../feature/settings/src/test/kotlin/com/uzuu/diuchat/feature/settings/presentation/screen/SettingsActionTest.kt) & [SettingsViewModelTest.kt](../../../feature/settings/src/test/kotlin/com/uzuu/diuchat/feature/settings/presentation/screen/SettingsViewModelTest.kt): kiểm tra bước nhảy 30 phút qua nửa đêm, tính thời lượng `"7,5 giờ nghỉ"`, và chuyển đổi qua 6 màn con `SettingsSubScreen`.
    - [CallHistoryViewModelTest.kt](../../../feature/calls/src/test/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallHistoryViewModelTest.kt) & [CallViewModelTest.kt](../../../feature/calls/src/test/kotlin/com/uzuu/diuchat/feature/calls/presentation/screen/CallViewModelTest.kt): kiểm tra gọi nhanh từ hàng Yêu thích, gom nhóm lịch sử theo ngày và từ chối cuộc gọi đến kèm tin nhắn trả lời nhanh.
    - [ContactsViewModelTest.kt](../../../feature/contacts/src/test/kotlin/com/uzuu/diuchat/feature/contacts/presentation/screen/ContactsViewModelTest.kt) & [JournalViewModelTest.kt](../../../feature/journal/src/test/kotlin/com/uzuu/diuchat/feature/journal/presentation/journal/JournalViewModelTest.kt): kiểm tra lọc danh bạ theo mã hex `AppPalette` và thứ tự 5 màu cảm xúc cốt lõi `JournalMoodType.coreFiveMoods()`.
  - Kiểm chứng toàn repo (`./gradlew :konsist-test:test testDebugUnitTest :app:assembleDebug`): được chạy và xác nhận bởi bước verification tổng thể của dự án.
- **6. Bài tập nhỏ gắn với code:**
  - *Bài tập 1 (Toán học góc Canvas 24h):* Trong [QuietHoursDial.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/presentation/composable/QuietHoursDial.kt), khi lịch nghỉ bắt đầu lúc `22:30` (`startMinuteOfDay = 1350`) và kết thúc lúc `06:00` sáng hôm sau (`endMinuteOfDay = 360`), hãy tính `durationMinutes`, góc bắt đầu `startAngle` và góc quét `sweepAngle` truyền vào `drawArc`. (Đáp án: `durationMinutes = (1440 - 1350) + 360 = 450` phút; `startAngle = (1350 / 1440f) * 360f - 90f = 247.5°`; `sweepAngle = (450 / 1440f) * 360f = 112.5°`).
  - *Bài tập 2 (Kiểm tra lớp nền trong suốt):* Thử thêm một màn hình con mới vào `MainShowcaseScreen` dùng `Scaffold` mặc định mà không truyền `containerColor = Color.Transparent`, quan sát hiện tượng mất vệt màu ambient và sửa lại cho đúng quy ước.
- **7. Trạng thái:** **CODE** (đã triển khai đầy đủ trên `:feature:base`, `:app` và 11 module tính năng kèm unit test JUnit 5).

### T28 — State restoration, nav guard và process recreation

**Vượt flow basic:** ViewModel sống qua rotation nhưng không bảo đảm sống qua process death; mỗi loại state cần nơi lưu phù hợp.

- Đọc [NavigationRoute.kt](../../../app/src/main/kotlin/com/uzuu/diuchat/app/presentation/NavigationRoute.kt), [MainShowcaseScreen.kt](../../../app/src/main/kotlin/com/uzuu/diuchat/app/presentation/MainShowcaseScreen.kt).
- Typed routes đã có; auth gate/pending link/restoration đầy đủ chưa có.
- remember cho state tạm một composition; rememberSaveable/SavedStateHandle cho state nhỏ cần restore; Room cho draft/outbox lớn/bền; DataStore cho preference.
- OTP không persist raw OTP tùy tiện; lưu phone/deadline có giới hạn phù hợp, xác thực lại server; không route body thư.
- Bottom tabs giữ backstack theo yêu cầu; auth clear backstack đúng lúc; notification action qua cùng permission/session gate.
- Bài tập: đang soạn tin, rotate rồi process recreate; restore draft/selection theo policy mà không gửi lặp.
- Hoàn thành khi: hiểu “giữ state” khác “giữ side effect chạy nền”.

### T29 — Observability, correlation và privacy logging

**Vượt flow basic:** biết lỗi nằm ở client queue, mạng, SQL, scheduler hay push mà không cần log nguyên nội dung chat.

- Timber/debug state đã có; structured metrics và backend correlation là đề xuất.
- Gắn operation/event/call ID xuyên client-server-job; log error code/latency/attempt/version có redaction.
- Đo queue age/sync lag/upload fail/letter lag/call setup/relay usage; threshold sau khi có baseline.
- Không log OTP, phone đầy đủ, body, private note, signed URL/key, vị trí chính xác; ngay cả debug vẫn có nguy cơ lộ.
- Perf tooling dùng khi có mục tiêu đo; performance tests cần data fixture và thiết bị tham chiếu rõ.
- Bài tập: một tin pending 2 phút; lần theo operation ID và giải thích đang chờ mạng hay server từ chối.
- Hoàn thành khi: lỗi có bằng chứng phục hồi, không chỉ toast “có lỗi xảy ra”.

### T31 — Migration, môi trường và CI gates

**Vượt flow basic:** app đang cài và dữ liệu server cũ phải nâng cấp được, không chỉ database mới tạo chạy được.

- Đã chuẩn hóa chuỗi migration tuần tự và kiểm chứng toàn vẹn tại [docs/migrations/](../migrations/):
  - `00001_initial_schema.sql`: Khung schema baseline.
  - `00002_db01_security_definer_hardening.sql`: DB-01 search_path và caller security.
  - `00003_db02_conversation_members_security.sql`: DB-02 bảo vệ thành viên hội thoại và admin guard.
  - `00004_db05_profiles_and_settings_separation.sql`: DB-05 tách biệt profile công khai và cài đặt riêng tư.
  - `00005_db06_mood_privacy_and_note_separation.sql`: DB-06 tách biệt ghi chú cảm xúc và allowlist.
  - `00006_seed_dev_fixtures.sql`: Dữ liệu kiểm thử dev/test an toàn, idempotent cho 3 tài khoản mẫu.
- Room cần migration path và test schema upgrade; backend dùng forward migrations và seed test riêng.
- Dev/staging/prod cấu hình tách, secret backend không vào APK; public anon key không thay thế authorization.
- CI: build/format/static/unit/Konsist, DB/RLS suite và release smoke theo mốc. Task phải khám phá từ repo trước chạy, không giả định tên task tồn tại.
- Test migration dữ liệu thực tế dạng giả lập; deployment có backup/rollback hoặc forward-fix phù hợp, không “reset prod”.
- Tác vụ sửa CI 21/11/2026: [SpotlessConventionPlugin.kt](../../../build-logic/src/main/kotlin/com/uzuu/diuchat/buildlogic/SpotlessConventionPlugin.kt) đặt LineEnding.UNIX để kết quả Windows/Linux thống nhất. [detekt.yml](../../../detekt.yml) giữ luật nghiệp vụ, điều chỉnh giới hạn dành cho @Composable và các named design-token properties. Gỡ workflow claude-review tự chạy trên PR; không gỡ các job build/lint/test.
- Bài học: một job CI đỏ có thể do resource XML hoặc cấu hình runner, chưa chắc do test thất bại. Đọc log từng job và chạy cùng Gradle task trước khi đổi luật.
- Bài tập: thêm attachment column, nâng từ schema cũ mà không mất message/outbox.
- Hoàn thành khi: install mới và upgrade đều có bằng chứng, môi trường release không gọi dev server.

### T32 — Data ownership, cleanup và deletion workflow

**Vượt flow basic:** “xóa” là nhiều nghiệp vụ với phạm vi và tác động khác nhau.

- Đã triển khai trong `:feature:media` (REQ-39 Quản lý bộ nhớ đệm): [ClearCacheUseCase.kt](../../../feature/media/domain/usecase/ClearCacheUseCase.kt), [MediaRepositoryImpl.kt](../../../feature/media/data/repository/MediaRepositoryImpl.kt), [StorageScreen.kt](../../../feature/media/presentation/storage/StorageScreen.kt), [StorageViewModel.kt](../../../feature/media/presentation/storage/StorageViewModel.kt).
  - Phân định ranh giới xóa an toàn: Xoá bộ nhớ đệm (Clear cache) chỉ dọn dẹp các tệp tạm (thumbnail ảnh, cached audio chunks) có thể tải lại bất kỳ lúc nào từ cloud Storage. Hoàn toàn không xâm phạm hay xóa các bản ghi SQLite Room, transactional outbox operations, hoặc các tệp pending upload chưa hoàn tất đồng bộ.
  - Hộp thoại cảnh báo & xác nhận: Người dùng phải xác nhận qua `DiuConfirmationDialog` trước khi thực hiện dọn dẹp, với thông báo minh bạch: "Hành động này sẽ giải phóng bộ nhớ tạm. Ảnh và tin nhắn quan trọng của bạn vẫn an toàn trên đám mây."
  - Cập nhật chỉ số phản ứng (Reactive Flow): `MediaRepositoryImpl.clearCache()` đặt `cacheBytes = 0L`, tính toán lại tỉ lệ phần trăm dung lượng và phát ra `StorageStats` mới đến ViewModel, đồng thời hiển thị thông báo inline `DiuInlineNotice` xác nhận thành công.
- Đã triển khai trong `:feature:settings` (Account Deletion & Session Cleanup per REQ-04, DEC-01, DEC-02, DB-09): [DeleteAccountUseCase.kt](../../../feature/settings/domain/usecase/DeleteAccountUseCase.kt), [LogoutUseCase.kt](../../../feature/settings/domain/usecase/LogoutUseCase.kt), [SettingsRepositoryImpl.kt](../../../feature/settings/data/repository/SettingsRepositoryImpl.kt), [DeleteAccountDialog.kt](../../../feature/settings/presentation/composable/DeleteAccountDialog.kt), [SettingsViewModel.kt](../../../feature/settings/presentation/screen/SettingsViewModel.kt).
  - Xác thực lại danh tính trước khi xóa vĩnh viễn: Người dùng bắt buộc phải nhập lại chính xác số điện thoại tài khoản đang sử dụng trong `DeleteAccountDialog`. Nếu số không khớp, UseCase quẳng ngoại lệ và ViewModel đưa state về `AccountDeletionState.Failed("Số điện thoại xác nhận không chính xác")` để ngăn chặn xóa nhầm.
  - Anonymize tin nhắn đối phương & bảo tồn toàn vẹn dữ liệu: Khi xóa tài khoản, toàn bộ tin nhắn phía người nhận được ẩn danh hóa ("Người dùng đã xóa tài khoản"), tên hiển thị và avatar chuyển về dạng vô danh theo chuẩn DEC-01 & DB-09. Không xóa cứng hàng loạt tin nhắn trong hộp thoại của bạn bè để tránh làm rách mạch trò chuyện của đối phương.
  - Thu hồi phiên & đánh dấu tombstone: Thu hồi token/session của Supabase Auth, hủy đăng ký các tác vụ đồng bộ nền, làm sạch dữ liệu local cache và đưa cờ `SettingsUiState.isLoggedOut` thành `true` để kích hoạt navigation guard chuyển hướng an toàn về màn hình khởi động.
  - Vòng đời đăng xuất (`LogoutUseCase`): Đóng scope người dùng, xóa token xác thực và đưa `isLoggedOut = true` mà không xóa dữ liệu người dùng trên cloud.
- Clear cache: xóa file có thể tải lại, tránh pending upload. Xóa chat local: đặt boundary để dữ liệu cũ không tự hiện lại ngay khi sync.
- Recall: tombstone chung, dọn preview/search/cache theo policy. Bỏ star: xóa bookmark của mình, không xóa source.
- Logout: đóng user scope và token; giữ/xóa cache theo chính sách rõ. Delete account: job backend revoke access, hủy scheduled/live/call, dọn objects và anonymize/retain nội dung theo quyết định.
- File forward/dùng chung cần reference accounting; không xóa remote object khi vẫn có message hợp lệ tham chiếu.
- Job dọn phải retry và idempotent; tính quyền sở hữu Storage khác quyền người đang xem.
- Bài tập: gửi voice khi offline rồi clear cache; file pending vẫn còn. Xóa account có job lỗi giữa chừng rồi retry không làm hỏng dữ liệu B.
- Hoàn thành khi: mỗi nút xóa có scope, precondition, confirmation copy và bằng chứng backend/local tương ứng.

### T33 — DataStore Preferences cho cấu hình người dùng và riêng tư

**Vượt flow basic:** Cấu hình ứng dụng nhẹ (theme, privacy toggle, quiet hours) không nên dùng SQLite table cồng kềnh, cũng không được dùng SharedPreferences kiểu cũ vì đồng bộ blocking main thread và dễ sinh ANR.

- Đã triển khai trong `:feature:settings` (theo Bước 1 I06): [SettingsPreferencesDataSource.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/data/datasource/preferences/SettingsPreferencesDataSource.kt), [SettingsRepository.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/domain/repository/SettingsRepository.kt), [SettingsRepositoryImpl.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/data/repository/SettingsRepositoryImpl.kt).
- Sử dụng Jetpack DataStore Preferences (`androidx.datastore:datastore-preferences` 1.1.3), chạy phi đồng bộ trên `Dispatchers.IO`, xử lý bắt lỗi `IOException` an toàn qua `catch { emit(emptyPreferences()) }`.
- Quản lý các cấu hình quan trọng theo REQ-37, REQ-38, REQ-40:
  - `theme_mode`: chế độ giao diện `SYSTEM`, `LIGHT`, `DARK`.
  - `read_receipts_enabled`: bật/tắt gửi trạng thái đã đọc tin nhắn (Read receipts toggle).
  - `notification_privacy`: 3 mức quyền riêng tư thông báo (`MOOD_COLOR_ONLY`, `SENDER_NAME`, `FULL_CONTENT`).
  - `quiet_schedule_*`: khung giờ yên lặng bắt đầu, kết thúc, múi giờ và danh bạ liên hệ khẩn cấp.
  - `profile_*`: thông tin người dùng (tên hiển thị, tiểu sử, ngày sinh, số điện thoại, mood).
  - `blocked_contacts_csv`: danh sách liên hệ bị chặn dạng CSV.
- Tách tầng Clean Architecture rõ ràng qua các UseCase: [GetThemeModeUseCase.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/domain/usecase/GetThemeModeUseCase.kt), [UpdateThemeModeUseCase.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/domain/usecase/UpdateThemeModeUseCase.kt), [GetReadReceiptsEnabledUseCase.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/domain/usecase/GetReadReceiptsEnabledUseCase.kt), [UpdateReadReceiptsEnabledUseCase.kt](../../../feature/settings/src/main/kotlin/com/uzuu/diuchat/feature/settings/domain/usecase/UpdateReadReceiptsEnabledUseCase.kt).
- Kết quả thu được: Cấu hình người dùng không bao giờ bị mất khi khởi động lại app / restart process, UI phản ứng tức thì khi cấu hình thay đổi thông qua DataStore Flow.
- Đã kiểm chứng: 46/46 unit test `:feature:settings` pass, 15/15 architecture test `:konsist-test` pass.

### T34 — Android Credential Manager Sign-In with Google & Username Onboarding Guard (M1)

**Vượt flow basic:** Không dùng web view OAuth cũ kỹ hay chờ đợi SMS OTP vốn tốn chi phí và rủi ro rớt mạng; sử dụng chuẩn hiện đại Android Credential Manager API kết hợp với cơ chế Profile Guard bắt buộc khởi tạo `@username` duy nhất trước khi người dùng được phép truy cập vào ứng dụng.

- Đã triển khai trong `:feature:auth` (theo Mốc M1 [DIU_TECHNOLOGY_INTEGRATION_PLAN.md](../DIU_TECHNOLOGY_INTEGRATION_PLAN.md)):
  - Presentation: [GoogleSignInHelper.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/presentation/util/GoogleSignInHelper.kt), [AuthScreen.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/presentation/screen/AuthScreen.kt), [AuthViewModel.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/presentation/screen/AuthViewModel.kt), [AuthAction.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/presentation/screen/AuthAction.kt), [AuthUiState.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/presentation/screen/AuthUiState.kt).
  - Domain Use Cases: [SignInWithGoogleUseCase.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/domain/usecase/SignInWithGoogleUseCase.kt), [CheckHasUsernameUseCase.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/domain/usecase/CheckHasUsernameUseCase.kt), [ClaimUsernameUseCase.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/domain/usecase/ClaimUsernameUseCase.kt), [AuthRepository.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/domain/repository/AuthRepository.kt).
  - Data & Backend: [AuthRepositoryImpl.kt](../../../feature/auth/src/main/kotlin/com/uzuu/diuchat/feature/auth/data/repository/AuthRepositoryImpl.kt).
- Luồng kỹ thuật chi tiết:
  1. **Credential Manager Integration:** Gọi `CredentialManager.getCredential()` với `GetGoogleIdOption` sử dụng Web Application Client ID từ Google Cloud Console (cấu hình an toàn qua `BuildConfig.GRADLE_GOOGLE_WEB_CLIENT_ID`).
  2. **Supabase IDToken Authentication:** Trích xuất ID Token và xác thực vào Supabase thông qua `client.auth.signInWith(IDToken)`.
  3. **Profile Guard & Username Claiming:**
     - Sau khi đăng nhập thành công, ViewModel lập tức gọi `CheckHasUsernameUseCase` query bảng `user_profiles` để kiểm tra sự tồn tại của `username`.
     - Nếu đã có username -> hoàn tất đăng nhập và chuyển hướng sang `Home`.
     - Nếu chưa có username -> hiển thị giao diện Onboarding Claim Username ngay trong `AuthScreen`. Kiểm tra định dạng username (chỉ chữ cái, số, gạch dưới, độ dài 3-20 ký tự) và gọi RPC `bootstrap_profile(p_username, ...)` trên Supabase để ghi nhận profile, settings và quiet schedule nguyên tử trong một transaction.
- Đã kiểm chứng: 35/35 unit test `:feature:auth` pass, 15/15 architecture test `:konsist-test` pass, kiểm tra định dạng Spotless pass.

## 7. Ba luồng đáng học trước

### Luồng A — Một tin nhắn sống qua mất mạng

~~~mermaid
sequenceDiagram
    participant UI as Compose
    participant VM as ViewModel
    participant UC as SendMessage
    participant DB as Room + Outbox
    participant SY as Sender / Worker
    participant API as Server
    UI->>VM: SendClicked
    VM->>UC: text + mood + conversation
    UC->>DB: Transaction message pending + operation
    DB-->>VM: Flow cập nhật
    VM-->>UI: Hiện pending
    SY->>DB: Claim operation theo account
    SY->>API: Gửi cùng message/operation ID
    API-->>SY: ACK hoặc timeout
    SY->>DB: Upsert ACK / giữ retry state
    API-->>DB: Realtime qua adapter → upsert cùng ID
    DB-->>UI: Flow render trạng thái nhất quán
~~~

Học T03 → T06 → T08 → T09 → T10 → T11 → T12 → T13. Câu hỏi tự kiểm tra: nếu server đã lưu nhưng response mất, phần nào ngăn sinh tin thứ hai?

### Luồng B — Thư tới hạn khi app người gửi đã tắt

~~~text
Tạo envelope + protected payload + release job trong transaction
→ server giữ deadline
→ scheduler/worker xử lý tới hạn
→ cập nhật Released và enqueue notification idempotent
→ recipient nhận push hoặc tự refresh khi mở app
→ API kiểm actor/deadline rồi trả payload được phép
→ open command ghi opened_at một lần
~~~

Học T14 → T15 → T16 → T18 → T19. Coroutine trong ViewModel và WorkManager trên máy gửi không được làm “đồng hồ sự thật” của thư.

### Luồng C — Gọi tới một người có hai thiết bị

~~~text
Caller tạo CallSession có TTL
→ private signaling + push cho thiết bị người nhận
→ device 1 và 2 cùng ringing
→ một device claim accept nguyên tử
→ device còn lại dừng ringing
→ RTC engine nối media qua ICE/STUN/TURN
→ UI chỉ quan sát call state
→ end/fail/timeout finalization tạo một log
~~~

Học T10 → T14 → T15 → T17 → T24 → T25. Câu hỏi tự kiểm tra: Broadcast signaling thành công đã đủ để chắc chắn hai máy nghe được nhau chưa?

## 8. Những điều dễ hiểu sai từ tài liệu hiện tại

| Cách hiểu dễ nhầm | Điều cần nhớ khi triển khai |
|---|---|
| Có dependency Supabase = app đã dùng backend đầy đủ | Hiện mới cấu hình client; cần flow, schema, permission và test thật |
| Có RLS = mọi dữ liệu đã an toàn | Cần kiểm quyền cột, RPC definer, Storage, Realtime topic và aggregate |
| SQL cài unaccent = search không dấu đã hoạt động | Index/query phải normalize nhất quán; SQL search hiện chưa gọi unaccent |
| Private bucket = chỉ thành viên chat tải được | Policy media hiện chỉ yêu cầu đăng nhập, cần sửa membership access |
| Chia sẻ mood row = note vẫn riêng tư vì UI không hiện | API có thể đọc cả row; phải tách private note/projection |
| Niêm phong/RLS = end-to-end encryption | Khác threat model; phải chốt key custody và khả năng người gửi xem/sửa |
| Broadcast không ghi DB = miễn phí, không quota | Vẫn cần kiểm quota/tariff và đo connection/message/egress |
| Có Realtime = không cần đồng bộ lại | Rớt kết nối/process death cần catch-up |
| Tên TimeTravelDebugger = có undo/replay | Hiện chỉ giữ/log transition, cần redact và bound |
| Optimistic UI = đã giao thành công | Chỉ phản ánh thao tác local; receipt là dữ liệu khác |
| Foreground service = tự do chạy mãi | Có loại service/quyền/điều kiện khởi chạy và giới hạn của Android |
| Call logs = đủ làm signaling state | Log kết quả khác phiên gọi đang diễn ra và device claim |
| Chặn âm thầm = người bị chặn tuyệt đối không suy ra | Chỉ giảm tiết lộ trực tiếp, không bảo đảm mọi suy luận |
| Mock lockscreen = app được tự vẽ lại màn khóa OS | Production dùng notification APIs và preview trong app |
| HTML có biometric = phải làm | feature_app ghi đã bỏ; legacy screen không tự thêm lại phạm vi |

## 9. Lộ trình học gắn với sản phẩm

| Chặng | Thực hành | Kỹ thuật trọng tâm | Đầu ra chứng minh đã hiểu |
|---|---|---|---|
| 1 | Đọc base và onboarding | T01–T07 | Tự lần một user event tới reducer và test; biết DI/build/test hoạt động ở đâu |
| 2 | OTP + QR + session gate | T14/T16/T23/T28 | Hai tài khoản xác thực/kết nối được, không lộ private profile |
| 3 | Text chat offline | T08–T13/T15 | Video/test restart+timeout vẫn đúng một message |
| 4 | Push + ảnh/voice | T19/T20/T21/T32 | Tải lại file đúng quyền; không mất pending media, không lộ notification |
| 5 | Nhóm/thư/mood/search | T14–T18/T22/T30 | Test concurrent command, thư đúng giờ server, private note và search không dấu |
| 6 | Call/location | T17/T24/T25/T26 | Hai mạng khác nhau gọi được; live session hết hạn/dừng thật |
| 7 | Tối ưu và phát hành | T27/T29/T31 | Đo trước/sau, release build, migration/restore và cập nhật tài liệu |

Không cần học hết 32 mục trước khi viết dòng tính năng đầu tiên. Mỗi ticket học kỹ thuật đang cần, triển khai một lát cắt nhỏ, kiểm lỗi thật rồi cập nhật hiểu biết.

## 10. Cách cập nhật file này sau mỗi feature

Quy tắc bắt buộc cho agent được lưu tại [AGENTS.md của dự án](../../../AGENTS.md). Mỗi tác vụ code có thêm, sửa hoặc áp dụng kỹ thuật ngoài flow cơ bản phải cập nhật mục tương ứng trong file này trước khi báo hoàn thành; áp dụng kỹ thuật đã biết vào feature mới cũng cần bổ sung source và bằng chứng.

Chỉ cập nhật CODE khi có bằng chứng implementation; ghi test chưa chạy hoặc giới hạn còn lại trung thực.

~~~text
Kỹ thuật: T09 Transactional outbox
Trạng thái: ĐỀ XUẤT → CODE một phần / CODE đã kiểm chứng
Feature/ticket: Chat text, P4-01, REQ-11/41
Vấn đề: App mất process sau khi bấm gửi
Luồng: Compose → use case → Room transaction → sender → server → Room
Code: [đường dẫn thật và symbol khi đã tạo]
Migration/API: [đường dẫn thật]
Kiểm chứng: test timeout-after-commit/process restart; kết quả và ngày chạy
Trade-off: thêm DB/worker complexity để bảo toàn queued operation
Giới hạn: chưa hỗ trợ media / chưa đo nhiều device
Bài học: phân biệt state local và receipt server
~~~

Checklist cho người học:

- [ ] Tôi giải thích được vấn đề trước khi gọi tên pattern.
- [ ] Tôi chỉ ra được source thật và trạng thái kỹ thuật hiện tại.
- [ ] Tôi mô tả được happy path, lỗi mạng, process death và thao tác lặp.
- [ ] Tôi biết client hay server giữ quyền quyết định cuối.
- [ ] Tôi có một test/demo tái hiện để kiểm tra hiểu biết.
- [ ] Tôi biết trade-off và điều kiện chưa hỗ trợ.
