# BATCH 06
## Deferred Group Moderation Role Refactor

### Status
Deferred until after current runtime QA

### Why this batch exists
Qua rà soát sản phẩm, role kiểm duyệt bài viết trong app đang bị gán lệch vào `ADMIN`.

Điều này không hợp lý về kiến trúc vì:
- `ADMIN` thực tế đi vào monitor/backoffice
- user app cần một role moderation theo từng nhóm
- quyền duyệt bài nên bám vào group ownership/moderation, không bám vào system admin

### Current temporary rule
Trong giai đoạn test hiện tại:
- coi `HOST` là role kiểm duyệt hợp lệ trong app
- không dùng `ADMIN` làm acceptance blocker cho community moderation runtime QA

### Future target design
Tách rõ:
- `ADMIN` = backoffice / monitor / system-level management
- `HOST` = chủ nhóm
- `MODERATOR` hoặc `GROUP_MODERATOR` = kiểm duyệt viên của nhóm
- `MEMBER` = thành viên thường

### Expected refactor scope later
- backend `GroupRole`
- backend `ensureCanModerate(...)`
- preview response `myRole`
- frontend moderation gating in `GroupPostDetailViewModel`
- runtime QA for moderator-specific flows
- seed/demo data for moderator assignment

### Out of scope for now
- không chặn Batch 05 runtime QA
- không sửa role system ngay trong lượt test hiện tại

### Trigger to activate this batch
Chỉ bắt đầu batch này sau khi:
1. Batch 05 runtime QA cho group-post interactions hoàn tất
2. user xác nhận chuyển sang phase phân quyền
