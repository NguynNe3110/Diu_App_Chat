# Quy tắc làm việc trong dự án Dịu

<!-- shared-personal-skills -->
For tasks matching an installed personal skill, read `C:/Users/ASUS/.agents/SKILLS.md` for the shared selection rules, then load the relevant skill. Resolve script paths from that skill's own directory. The user's request and applicable project instructions govern the task.
<!-- /shared-personal-skills -->

## Branch, commit và Pull Request

Áp dụng cho các tác vụ thay đổi code, cấu hình hoặc tài liệu tiếp theo trong dự án, trừ khi người dùng có chỉ dẫn khác cho tác vụ cụ thể. Tạo branch, commit, push và mở PR là các bước đã được người dùng yêu cầu; thực hiện trong phạm vi tác vụ mà không hỏi xác nhận lại cho từng bước.

1. **Tạo branch mới trước khi sửa file.** Kiểm tra branch hiện tại, working tree và base branch phù hợp. Mỗi tác vụ mới dùng branch riêng với tên mô tả ngắn như `feat/phone-auth`, `fix/chat-retry` hoặc `docs/update-plan`; tác vụ tiếp tục cùng PR thì dùng branch của PR đó. Không làm trực tiếp trên branch mặc định.
2. **Giữ nguyên thay đổi có sẵn của người dùng.** Chỉ đưa phần thuộc tác vụ vào branch/commit/PR. Nếu working tree có thay đổi không liên quan, dùng checkout/worktree riêng khi cần; không tự reset, stash, discard hoặc gom các thay đổi đó vào commit.
3. **Kiểm tra và cập nhật tài liệu trước khi commit.** Review diff, chạy các kiểm tra phù hợp với thay đổi và cập nhật bản đồ kỹ thuật khi thuộc diện bắt buộc. Ghi rõ kiểm tra đã chạy, kết quả và giới hạn; không báo pass khi chưa chạy.
4. **Phải commit phần đã hoàn thành.** Stage rõ file/hunk thuộc tác vụ, kiểm tra staged diff rồi tạo commit có message mô tả thay đổi. Không dừng ở việc sửa file trong workspace. Không dùng `git add .` hoặc `git add -A` một cách mù quáng khi có thay đổi của người dùng.
5. **Push branch và tạo PR.** Xác định đúng remote/base, push branch của tác vụ rồi mở PR sau khi nội dung sẵn sàng review. Nếu đã có PR cho tác vụ thì push commit mới và cập nhật PR hiện có. PR mô tả vấn đề, hành vi sau thay đổi, cách kiểm chứng, giới hạn còn lại và link mục kỹ thuật liên quan.
6. **Báo kết quả đầy đủ.** Phản hồi hoàn thành phải có tên branch, mã commit, link PR và kết quả kiểm tra. Nếu không thể commit/push/tạo PR vì thiếu remote, công cụ, xác thực hoặc quyền, hoàn tất các bước còn làm được và báo chính xác bước bị chặn; không nói đã tạo PR khi chưa có PR thật.
7. **Merge là bước riêng.** Không tự merge PR hoặc force-push nếu người dùng chưa yêu cầu.

File đang bị Git ignore không tự được đưa vào commit. Nếu tài liệu thuộc tác vụ bị ignore, báo rõ phần chưa được đưa vào PR; không tự sửa quy tắc ignore hoặc force-add các file không liên quan.

## Ghi nhận kỹ thuật để người dùng học trong quá trình viết code

Đây là yêu cầu bắt buộc cho các tác vụ sinh code, triển khai, sửa lỗi hoặc refactor trong dự án này. Người dùng muốn hiểu những kỹ thuật được sử dụng ngoài flow cơ bản UI → ViewModel → Domain → Data.

### Khi nào phải ghi nhận

- Khi thêm hoặc thay đổi một kỹ thuật ngoài việc truyền dữ liệu qua các layer cơ bản, phải cập nhật [bản đồ kỹ thuật](docs/plans/DIU_TECHNIQUES_LEARNING_MAP.md) ngay trong cùng tác vụ, trước khi báo hoàn thành.
- Khi áp dụng một kỹ thuật đã có vào feature mới, cập nhật mục tương ứng với nơi áp dụng và bằng chứng mới; không chỉ ghi nhận kỹ thuật hoàn toàn mới.
- Khi phát hiện cơ chế đáng học trong phần code hiện hữu đang xử lý, ghi nhận hoặc sửa mục hiện có nếu tài liệu chưa phản ánh đúng. Chỉ khảo sát phạm vi liên quan tác vụ, không tự mở rộng thành audit toàn repo.
- Ví dụ thuộc diện ghi nhận: reducer/MVI, phối hợp hoặc hủy Flow/coroutine, lifecycle/resource ownership, state machine, offline cache/outbox, retry/idempotency, Realtime, pagination, RPC/transaction/RLS, background work, notification routing/privacy, media/RTC, custom drawing/motion, DI scope, convention plugin và architecture test. Danh sách này không giới hạn các kỹ thuật khác phù hợp.

### Nội dung mỗi mục

Viết bằng tiếng Việt, giải thích cho người đang học Kotlin/Android, gồm:

1. Tên kỹ thuật và ID T hiện có hoặc ID mới không trùng.
2. Vấn đề cụ thể cần giải quyết và điểm bổ sung so với flow cơ bản.
3. Feature/ticket liên quan; cách hoạt động và luồng dữ liệu hoặc vòng đời.
4. Đường dẫn source thật và symbol quan trọng; migration/API nếu có.
5. Lý do chọn, trade-off, lỗi dễ gặp và giới hạn còn lại.
6. Cách kiểm chứng, kết quả thực tế và phần chưa kiểm chứng; không ghi đã pass nếu chưa chạy.
7. Một ví dụ hoặc bài tập nhỏ gắn với code khi giúp người dùng hiểu kỹ thuật.
8. Trạng thái trung thực: CODE, MẪU, SQL hoặc ĐỀ XUẤT theo quy ước trong bản đồ. Có implementation không đồng nghĩa đã kiểm chứng hay sẵn sàng production.

### Cách duy trì tài liệu

- Dùng `docs/plans/DIU_TECHNIQUES_LEARNING_MAP.md` làm file báo cáo kỹ thuật chính. Đọc mục liên quan trước khi sửa; tận dụng mẫu ở phần “Cách cập nhật file này sau mỗi feature”.
- Cập nhật mục T hiện có khi cùng kỹ thuật; chỉ thêm ID mới cho một kỹ thuật khác. Giữ bảng danh mục và phần giải thích nhất quán, không tạo file rời cho từng thay đổi nhỏ.
- Khi cách triển khai hoặc quyết định thay đổi, sửa mô tả, source và trạng thái cũ; tránh giữ hướng đã bỏ như thể vẫn đang dùng.
- Không tạo kỹ thuật mới hoặc tăng độ phức tạp chỉ để có nội dung báo cáo. Thay đổi đơn giản không cần mục mới; chỉ nêu chưa phát sinh kỹ thuật ngoài flow cơ bản nếu phù hợp.
- Không đưa secret, OTP, token, nội dung chat hoặc dữ liệu cá nhân thật vào báo cáo.
- Trong phản hồi hoàn thành tác vụ code, dẫn link mục kỹ thuật đã cập nhật và nêu ngắn gọn điều đáng học. Tài liệu kỹ thuật là một phần của việc hoàn thành thay đổi có áp dụng các cơ chế trên.
