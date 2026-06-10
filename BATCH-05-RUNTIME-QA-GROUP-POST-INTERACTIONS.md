# BATCH 05 RUNTIME QA
## Group Post Wiki-Style Interactions

### Batch Status
Ready for execution

### Purpose
Kiểm chứng runtime thật cho tính năng tương tác bài viết trong luồng:

`Cộng đồng -> Hội nhóm -> Thảo luận`

Scope chỉ gồm:
- like / unlike
- comment sheet
- create comment
- count update
- back-stack integrity

Không test lan sang:
- realtime chat
- family chat
- OCR
- AI Care
- article wiki cũ
- doctor consultation flows

### Execution Mode
Batch này là `guided runtime QA`.

Subagent **không tự claim đã test tay toàn bộ app**.
Nó phải làm đúng vai trò:

- đưa ra từng bước test rất ngắn cho user
- dừng lại sau mỗi step/case để chờ user thao tác
- khi user báo lỗi hoặc gửi ảnh/log, subagent chỉ:
  - đọc log
  - đối chiếu expected vs actual
  - xác định defect hoặc pass

Subagent không được tự nói "đã bấm / đã mở / đã xác nhận trên thiết bị" nếu chính user chưa thao tác.

### Current Role Assumption
Trong batch QA này, quyền kiểm duyệt trong app tạm thời được test theo `HOST của nhóm`.

Không dùng batch này để kết luận về thiết kế phân quyền dài hạn giữa:
- `ADMIN hệ thống`
- `HOST nhóm`
- `MODERATOR nhóm`

Lý do:
- hiện tại `ADMIN` mang ngữ nghĩa backoffice/monitor nhiều hơn là user-app moderator
- bài toán phân quyền sẽ được refactor ở batch riêng sau khi runtime QA hiện tại hoàn tất

Vì vậy:
- nếu cần test moderation UI trong app, ưu tiên account `HOST`
- account `ADMIN` trong batch này chỉ là optional cross-check, không phải acceptance blocker
- không được chặn kết luận runtime chỉ vì chưa có mô hình `MODERATOR` hoàn chỉnh

---

## 1. Preconditions (Mandatory)

Chỉ được bắt đầu khi đủ toàn bộ điều kiện sau:

- backend đang chạy
- database đang chạy
- app đã cài lên máy thật hoặc emulator
- user test đã đăng nhập được
- có ít nhất 1 hội nhóm mà user đang là member
- trong hội nhóm đó có ít nhất:
  - 1 bài viết `APPROVED`
  - nếu có thể, 1 bài viết `PENDING` hoặc `REJECTED` để negative-check

Nếu thiếu precondition nào, phải ghi rõ `BLOCKED` và dừng. Không được giả lập kết quả.

---

## 2. Test Scope

### In scope
- Approved group posts interaction
- My posts interaction behavior
- Comment sheet UX/data flow
- Like optimistic behavior
- Navigation back behavior inside discussion flow

### Out of scope
- moderation approve/reject runtime full pass
- realtime websocket chat
- article wiki feed
- image upload
- OCR / AI / family

---

## 3. Test Accounts

Ghi rõ account thật dùng để test:

- Member:
- Host:
- Admin:

Nếu chỉ test bằng 1 account, phải nói rõ account đó là gì và những case nào chưa test được vì thiếu role.

### Priority for account usage
1. `HOST` là account ưu tiên để test các case liên quan moderation presence trong app
2. `MEMBER` để test like/comment thường
3. `ADMIN` chỉ test thêm nếu user muốn, không phải điều kiện bắt buộc để pass batch này

---

## 4. Runtime Test Cases

### Operating rule for every case
Với mỗi case GP-RT-XX, subagent phải làm theo chu trình này:

1. Gửi cho user:
   - tên case
   - 1-3 bước thao tác rất ngắn
   - expected result ngắn
2. Dừng lại
3. Chờ user phản hồi:
   - `pass`
   - `fail`
   - ảnh
   - mô tả lỗi
4. Nếu fail:
   - yêu cầu đúng dữ liệu cần thiết nhất:
     - logcat/error text/screenshot
   - đọc log
   - kết luận nguyên nhân gần nhất
5. Chỉ sau đó mới chuyển sang case tiếp theo

Không được đẩy một cục 11 case rồi bắt user tự bơi.

## GP-RT-01 — Enter discussion screen
### Goal
Xác nhận vào được `Cộng đồng -> Hội nhóm -> Thảo luận` và list bài viết render thật.

### Steps to give user
1. Mở app
2. Vào `Cộng đồng`
3. Chuyển sang tab `Hội nhóm`
4. Chọn 1 nhóm
5. Bấm `Thảo luận`

### Expected
- mở đúng `GroupPostDetailScreen`
- không nhảy vào realtime chat
- thấy danh sách bài viết hoặc empty state đúng
- không crash

---

## GP-RT-02 — Approved card structure parity
### Goal
Xác nhận card approved có đủ shape wiki-like.

### What subagent asks user to verify visually
Mỗi card approved phải có:
- author block
- title
- body preview
- tag/image nếu có
- stats row
- action row `Thích / Bình luận`

### Expected
- stats row hiện cả khi `0 lượt thích • 0 bình luận`
- action row hiển thị ổn định, không lệch layout

---

## GP-RT-03 — Like optimistic success
### Goal
Xác nhận bấm like đổi state ngay.

### Steps to give user
1. Chọn 1 approved post chưa like
2. Ghi nhận count trước khi bấm
3. Bấm `Thích`

### Expected
- UI đổi trạng thái ngay lập tức
- count tăng ngay 1 đơn vị
- sau khi API trả về, trạng thái vẫn giữ đúng
- không toast/error sai ngữ nghĩa

---

## GP-RT-04 — Unlike / toggle back
### Goal
Xác nhận toggle like 2 chiều.

### Steps to give user
1. Trên chính bài đã like ở case trước
2. Bấm `Thích` lần nữa

### Expected
- state bỏ like
- count giảm đúng 1 đơn vị
- không âm count

---

## GP-RT-05 — Comment sheet open
### Goal
Xác nhận bottom sheet comment mở đúng post.

### Steps to give user
1. Chọn 1 approved post
2. Bấm `Bình luận`

### Expected
- mở bottom sheet
- title/label sheet rõ ràng
- load được list comment hoặc empty state
- không crash

---

## GP-RT-06 — Create comment success
### Goal
Xác nhận gửi bình luận thành công.

### Steps to give user
1. Mở bottom sheet comment
2. Nhập 1 comment test dễ nhận biết
3. Bấm `Gửi`

### Expected
- comment mới xuất hiện ngay trong sheet
- `commentCount` trên card tăng đúng +1
- không duplicate comment
- ô nhập được clear sau gửi

---

## GP-RT-07 — Comment draft isolation
### Goal
Xác nhận draft không rò giữa các post.

### Steps to give user
1. Mở comment sheet của post A
2. Gõ dở một đoạn, không gửi
3. Đóng sheet
4. Mở comment sheet của post B

### Expected
- ô nhập trống
- không còn draft từ post A

---

## GP-RT-08 — Back-stack integrity
### Goal
Xác nhận back không gãy flow.

### Steps to give user
1. Từ `GroupPostDetailScreen`, mở comment sheet
2. Bấm back / dismiss
3. Từ `GroupPostDetailScreen`, bấm back header/system back

### Expected
- back đầu: đóng comment sheet, vẫn ở discussion screen
- back tiếp: về đúng danh sách `Hội nhóm`
- không nhảy sang Home bất ngờ
- không nhảy vào chat room

---

## GP-RT-09 — My Posts compatibility
### Goal
Xác nhận tab `Bài của tôi` không bị phá.

### Steps to give user
1. Vào tab `Bài của tôi`
2. Quan sát post approved / pending / rejected nếu có

### Expected
- status chip còn đúng
- post `APPROVED` có stats/action row
- post `PENDING` / `REJECTED` không mở social interaction sai rule

---

## GP-RT-10 — Negative rule: non-approved interaction blocked
### Goal
Xác nhận rule sản phẩm giữ đúng.

### Steps to give user
Nếu có post `PENDING` hoặc `REJECTED` trong `Bài của tôi`:
1. Quan sát card

### Expected
- không có action row `Thích / Bình luận`
- không có đường nào để mở comment sheet cho non-approved post từ UI

Nếu không có sample data phù hợp, ghi rõ `NOT EXECUTED - no pending/rejected sample`.

---

## GP-RT-11 — Persistence after re-entry
### Goal
Xác nhận dữ liệu không chỉ đổi cục bộ.

### Steps to give user
1. Like hoặc comment trên 1 approved post
2. Back về `Hội nhóm`
3. Vào lại `Thảo luận`

### Expected
- count vẫn đúng
- state like vẫn đúng
- comment mới vẫn còn

---

## 5. Evidence Rules

Subagent phải ưu tiên lấy bằng chứng từ user thay vì tự bịa runtime:
- screenshot user gửi
- video user mô tả nếu có
- logcat/error text khi user gặp lỗi
- ghi rõ post nào user đã dùng để test

Tối thiểu nên có bằng chứng cho:
- screen discussion list
- screen comment sheet
- sau khi like
- sau khi comment

Nếu user không gửi được ảnh/log, subagent phải ghi rõ mức độ tự tin giảm đi ở case đó.

---

## 6. Honesty Rules

Không được:
- suy đoán kết quả nếu chưa bấm thật
- nói "pass runtime" nếu chỉ kiểm tra code
- nhập nhằng giữa article wiki và group post
- bỏ qua negative cases rồi vẫn kết luận full pass
- tự nhận đã thao tác trên thiết bị khi thực chất user là người test

Nếu blocked bởi môi trường, ghi:
- blocked reason
- bước nào bị chặn
- đã thử gì để mở khóa

---

## 7. Interaction Format For Subagent

Khi chạy batch này, subagent phải nhắn theo kiểu ngắn, từng bước:

### Step prompt template
```md
GP-RT-0X — <case name>

Hãy làm đúng 2-3 bước sau:
1. ...
2. ...
3. ...

Kỳ vọng:
- ...
- ...

Sau khi làm xong, trả lời một trong 3 dạng:
- PASS
- FAIL + mô tả lỗi
- FAIL + ảnh/log
```

### If FAIL template
```md
Đã nhận lỗi ở GP-RT-0X.

Hãy gửi thêm đúng một trong các dữ liệu sau:
- ảnh màn hình lỗi
- logcat/error text
- mô tả actual result khác expected ở bước nào

Tôi sẽ chỉ đọc lỗi và chốt nguyên nhân trước khi sang case tiếp theo.
```

---

## 8. Final Report Format

Báo cáo cuối phải theo đúng format:

### 1. Runtime QA Status
- `PASS`
- `PARTIAL`
- `BLOCKED`
- `FAIL`

### 2. Environment Summary
- date
- device/emulator
- backend running: yes/no
- db running: yes/no
- build used
- account(s) used

### 3. Case-by-Case Results
Liệt kê GP-RT-01 -> GP-RT-11:
- PASS / FAIL / BLOCKED / NOT EXECUTED
- 1-3 dòng evidence ngắn cho mỗi case

### 4. Defects Found
Nếu có bug:
- severity
- exact screen/flow
- reproduce steps ngắn
- expected vs actual

### 5. Acceptance Recommendation
Một trong các kết luận:
- `Accept Batch 05 runtime`
- `Accept with minor follow-up`
- `Do not accept yet`

### 6. Attachments / Evidence
Liệt kê ảnh hoặc log đã lưu

---

## 9. Acceptance Threshold

Chỉ được đề xuất `Accept Batch 05 runtime` khi:
- GP-RT-01 đến GP-RT-08 đều PASS
- GP-RT-09 PASS
- GP-RT-11 PASS
- GP-RT-10 ít nhất phải PASS hoặc NOT EXECUTED có lý do chính đáng

Nếu like/comment chạy nhưng back-stack gãy, batch chưa được accept.
