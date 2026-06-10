# SUBAGENT ACTIVE BATCH 05
## Group Posts -> Wiki-Style Interaction Pass

### Batch Status
Active

### Product Goal
Nâng bài viết trong `Cộng đồng -> Hội nhóm -> Thảo luận` từ dạng "structured text card" lên gần chuẩn bài đăng wiki:

- có `Tiêu đề`
- có `Nội dung`
- có `Ảnh` nếu có
- có `Tag`
- có `Tim / Thích`
- có `Bình luận`
- có `số lượt thích`
- có `số bình luận`
- có `bottom action row` giống tinh thần wiki article hiện tại

Lưu ý: đây là **group post interaction flow**, không phải wiki article flow. Không được sửa lan sang OCR, AI chat, family chat, hay admin area không liên quan.

---

## 1. Current Repo Truth

### What already exists
- `GroupPost` đã có cấu trúc hiển thị tốt hơn text 1 dòng:
  - title
  - content preview
  - tags
  - optional image
  - moderation status
- `CommunityWikiScreen.kt` đã có mẫu UI gần đúng cho:
  - like
  - comment count
  - comment sheet
  - action row `Thích / Bình luận`

### What is still missing
- `GroupPost` chưa có model dữ liệu cho:
  - `likeCount`
  - `commentCount`
  - `likedByMe`
- chưa có API cho:
  - like/unlike group post
  - load group post comments
  - create group post comment
- `StructuredGroupPostCard` chưa có:
  - stats row
  - action row
  - comment entry flow

### Important boundary
Không được báo cáo "đã giống wiki" nếu chưa có thật:
- tim hoạt động
- comment mở được
- comment tạo được
- count tăng đúng

---

## 2. Mission

Implement **Group Post Wiki-Style Interactions MVP** so that posts inside `GroupPostDetailScreen` behave like interactive community posts rather than static moderation cards.

---

## 3. Strict Scope

### In scope
#### Backend
- mở rộng entity / DTO / service / controller cho `GroupPost like + comment`
- trả về counters + liked-state cho frontend

#### Frontend
- mở rộng `GroupPost` model
- thêm repository / API methods cho like + comment
- nâng `StructuredGroupPostCard` để có:
  - stats row
  - like button
  - comment button
- thêm comment sheet/dialog cho group posts
- optimistic UI ở mức an toàn cho like

### Out of scope
- không làm notification
- không làm nested reply tree phức tạp
- không làm edit post
- không làm edit comment
- không làm delete post/comment trừ khi repo đã có sẵn API rất rõ
- không redesign toàn community
- không động tới wiki article APIs nếu không thực sự cần tái sử dụng UI logic

---

## 4. Execution Requirements

### 4.1 Backend data model
Tìm mô hình backend hiện tại của `GroupPost` và bổ sung các field cần thiết để frontend render đúng:

- `likeCount`
- `commentCount`
- `likedByMe` (computed in response, không nhất thiết phải là column)

Nếu backend đã có entity/comment table tương tự cho article, được phép tái sử dụng pattern nhưng **không được copy ẩu**.

#### Expected additions
- group-post comment entity hoặc equivalent relation
- group-post like persistence hoặc equivalent relation
- mapper/DTO response trả về:
  - `likeCount`
  - `commentCount`
  - `likedByMe`

### 4.2 Backend endpoints
Thêm hoặc hoàn thiện endpoints cho group posts:

- `POST /api/v1/communities/posts/{postId}/like`
- `GET /api/v1/communities/posts/{postId}/comments`
- `POST /api/v1/communities/posts/{postId}/comments`

Tên endpoint có thể khác nếu repo đã có convention rõ hơn, nhưng phải nhất quán với namespace communities hiện tại.

### 4.3 Backend behavior rules
- user chỉ được like một lần tại một thời điểm
- bấm like lần nữa thì unlike nếu product pattern đang là toggle
- `likeCount` phải phản ánh đúng sau mutation
- comment rỗng hoặc whitespace-only phải bị reject
- tất cả validation phải có cả DTO-level hoặc service-level guard

### 4.4 Frontend domain/model
Cập nhật `GroupPost` model để có:
- `likeCount: Long`
- `commentCount: Long`
- `likedByMe: Boolean`

Thêm `GroupPostComment` model nếu chưa có.

### 4.5 Frontend repository / API
Thêm methods tương ứng:
- toggle like group post
- load comments of group post
- create group post comment

Ngữ nghĩa message phải đúng:
- dùng `bài viết`
- không dùng nhầm `tin nhắn`

### 4.6 Frontend UI
#### Required UI shape
Trong `StructuredGroupPostCard`:
- giữ lại title/body/tag/image/status hiện có
- thêm stats row:
  - `X lượt thích`
  - `Y bình luận`
- thêm divider
- thêm action row:
  - `Thích`
  - `Bình luận`

#### Comment interaction
- bấm `Bình luận` mở bottom sheet hoặc dialog
- hiển thị danh sách comment
- có ô nhập comment
- gửi comment xong:
  - thêm comment mới vào UI
  - tăng `commentCount`

#### Like interaction
- bấm `Thích`:
  - đổi icon/state
  - cập nhật `likeCount`
  - rollback nếu API fail

### 4.7 Reuse guidance
Được phép tham khảo `CommunityWikiScreen.kt` cho:
- card interaction pattern
- comment sheet pattern
- optimistic like pattern

Nhưng phải tách thành code phù hợp cho `GroupPost`, tránh copy nguyên khối gây coupling bẩn.

---

## 5. Navigation / State Safety Rules

Đây là phần bắt buộc vì cụm này vừa mới ổn định back-stack:

- không được làm vỡ flow `Cộng đồng -> Hội nhóm -> Thảo luận`
- không được làm `Back` nhảy sai screen
- comment sheet đóng lại phải vẫn ở đúng post list
- chuyển tab `Bài viết / Bài của tôi / Chờ duyệt` không được làm mất state bất thường
- không được tạo tình trạng bấm `Thích` hoặc `Bình luận` mà mở nhầm sang realtime chat

---

## 6. Acceptance Criteria

Batch chỉ được gọi là xong khi thỏa đủ các điểm sau:

### AC-01 Layout parity
Trong `Bài viết` của group-post flow, mỗi card có:
- header
- title
- body preview
- tags/image nếu có
- stats row
- action row `Thích / Bình luận`

### AC-02 Like works
- bấm like đổi state ngay
- count tăng/giảm đúng
- API fail thì rollback

### AC-03 Comment works
- mở được comment sheet/dialog
- load được danh sách comment
- gửi comment mới thành công
- count tăng đúng

### AC-04 Moderation compatibility
- `Bài của tôi` vẫn giữ status chip
- `Chờ duyệt` vẫn giữ approve/reject action
- các phần tương tác mới không phá moderation UI cũ

### AC-05 Navigation integrity
- vào/ra comment sheet không làm vỡ screen
- back từ `GroupPostDetailScreen` vẫn về đúng `Hội nhóm`

### AC-06 Build integrity
- frontend `assembleDebug` pass
- backend compile/build pass

---

## 7. Verification Tasks (Mandatory)

### Automated
#### Frontend
```powershell
cd D:\DoAn_MB1\CareNest\frontend
.\gradlew.bat assembleDebug
```

#### Backend
```powershell
cd D:\DoAn_MB1\CareNest\backend
.\mvnw.cmd compile
```

### Manual code-level verification
Subagent phải tự đối chiếu:
- route discussion flow còn đúng
- `StructuredGroupPostCard` đã thực sự có `Thích / Bình luận`
- comment create path thực sự nối vào repository/API
- không dùng text sai ngữ nghĩa kiểu `tin nhắn`

Không được báo "đã có comment flow" nếu mới chỉ dựng icon mà chưa có data flow.

---

## 8. Reporting Format (No Sloppy Reporting)

Subagent phải trả về đúng format này:

### 1. Batch Status
- `Code-level complete`
- hoặc `Blocked`

### 2. Files Changed
Liệt kê đầy đủ frontend/backend files

### 3. Backend Changes
- entity / DTO / repository / service / controller đã thêm gì
- endpoint cuối cùng là gì
- validation nào đã enforce

### 4. Frontend Changes
- model / repository / API đã thêm gì
- UI card thay đổi ra sao
- comment flow hoạt động thế nào
- like rollback hoạt động thế nào

### 5. Acceptance Criteria Check
Đánh dấu từng AC-01 ... AC-06 là:
- `PASS`
- `PARTIAL`
- `FAIL`

### 6. Remaining Gaps
Nêu đúng phần chưa có, không lấp liếm

### 7. Build Result
Paste kết quả compile/build ở mức cần thiết

---

## 9. Hard Warnings

- Không được nói "đã giống wiki" nếu chưa có tim/comment thật
- Không được sửa lan sang family/chat nếu không bắt buộc
- Không được đổi endpoint community namespace bừa bãi
- Không được fake optimistic success khi API chưa được nối
- Không được xóa moderation features đã có

---

## 10. Priority

Ưu tiên:
1. chạy đúng luồng
2. data flow thật
3. build sạch
4. UI polish sau

Đây là batch functional MVP, không phải visual perfection pass.
