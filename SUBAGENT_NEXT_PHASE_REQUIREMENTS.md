# CareNest Kotlin Migration - Next Phase Subagent Requirements

## 1. Mục tiêu tài liệu

Tài liệu này dùng để giao việc cho subagent tiếp tục hoàn thiện bản Android Kotlin Jetpack Compose của dự án **CareNest** sao cho tiến dần tới mức **1:1 với bản React Native legacy** về:

- UI
- UX flow
- data / state
- routing
- text tiếng Việt

Subagent **không được tự sáng tạo UI mới**. Mọi quyết định giao diện đều phải bám theo bản React Native legacy.

---

## 2. Source of Truth bắt buộc

### 2.1. Bản tham chiếu UI/UX duy nhất

Subagent phải đối chiếu với code legacy tại:

- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\`

Và mốc tham chiếu nghiệp vụ/UI là commit legacy:

- `c56a8b8ae3ad2477fd11273ffb5aabc3215f279b`

### 2.2. Bản Kotlin hiện tại

Code Android Kotlin hiện tại nằm tại:

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\`

---

## 3. Ràng buộc bắt buộc

### 3.1. Không được làm

Subagent **không được**:

- tự thiết kế lại màn hình theo ý mình
- đổi UX flow nếu bản RN đã có flow rõ ràng
- thêm tính năng mới ngoài phạm vi parity
- refactor kiến trúc lớn nếu không thực sự cần để hoàn tất parity
- đổi API contract tùy hứng
- chạm vào backend nếu task chỉ là parity frontend
- sửa font/icon global ở giai đoạn này trừ khi lỗi đó chặn UI dùng được

### 3.2. Phải làm

Subagent **phải**:

- đọc màn hình React Native tương ứng trước khi sửa Compose
- giữ text tiếng Việt đúng UTF-8
- dùng `LazyColumn` / `LazyRow` có `key` cho list
- dùng `Modifier.windowInsetsPadding(WindowInsets.ime)` hoặc layout tương đương cho mọi màn có nhập liệu
- build lại bằng Gradle sau mỗi batch lớn
- giữ app ở trạng thái chạy được

### 3.3. Không ưu tiên ở giai đoạn này

Các việc sau **tạm hoãn** nếu không chặn sản phẩm:

- tinh font 1:1 tuyệt đối
- icon parity tuyệt đối ở mức pixel
- polish animation nhỏ
- cleanup warning không ảnh hưởng build

Ưu tiên hiện tại là: **hoàn thiện sản phẩm trước, polish sau**.

---

## 4. Trạng thái hiện tại đã có

### 4.1. Đã tương đối ổn

- Onboarding
- Login / Register
- Main Navigation shell
- Dashboard shell
- Family shell
- Medicine Cabinet lõi
- Add Medicine lõi
- Community shell
- Wiki create article flow
- Chat room cơ bản

### 4.2. Đã xử lý một phần

- theme token gần với RN hơn
- global mojibake đã dọn được nhiều chỗ
- family/profile state đã được vá nền tảng một phần
- bottom nav không còn bị đóng khung như trước

### 4.3. Còn dở / chưa parity

- Community Groups deep flow
- doctor actions / doctor bottom sheet
- chat hub parity sâu
- private/family chat hub behavior
- medical deep flows vẫn mới ở mức đủ dùng, chưa parity hoàn toàn
- OCR flow còn thiên về UI shell hơn là parity đầy đủ
- một số màn profile/medical/notifications vẫn còn sót text vỡ mã

---

## 5. Phạm vi công việc tiếp theo cho subagent

## Batch A - Community Deep Flow

### Mục tiêu

Hoàn thiện phần **Cộng đồng** để bớt “shell” và giống RN hơn trong các luồng chính.

### File Kotlin cần ưu tiên kiểm tra

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\community\presentation\CommunityScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\community\presentation\CommunityWikiScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\community\presentation\CommunityGroupsPane.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\main\presentation\ChatHubScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\chat\presentation\ChatScreen.kt`

### File RN phải đối chiếu

- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\community\CommunityWikiScreen.tsx`
- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\community\CommunityGroupsScreen.tsx`
- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\community\GroupDetailScreen.tsx`
- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\navigation\ChatHubNavigator.tsx`

### Việc cần làm

1. Hoàn thiện `CommunityGroupsPane`
   - list nhóm giống RN hơn
   - trạng thái joined / discover rõ ràng
   - preview/join flow không bị stub

2. Hoàn thiện `CommunityWikiScreen`
   - dọn sạch text tiếng Việt còn sót
   - kiểm tra lại feed card, like/comment UI, empty/loading state
   - đảm bảo create article sheet không vỡ layout khi bàn phím mở

3. Hoàn thiện `ChatHubScreen`
   - giảm cảm giác placeholder
   - kiểm tra route sang chat room / family chat / AI chat
   - nếu có card chết thì phải nối route hoặc disable có chủ đích

4. Hoàn thiện `ChatScreen`
   - giữ menu nhóm hoạt động được
   - nếu chưa có leave group thật thì phải hiện trạng thái/tip rõ ràng, không được nút chết
   - dọn các phần text còn vỡ mã nếu phát hiện thêm

### Acceptance criteria

- không còn nút chết rõ ràng trong Community/Wiki/Chat hub/chat room
- không còn text mojibake ở flow chính của Community
- mọi list chính có `key`
- mọi form/sheet nhập liệu không bị bàn phím che
- build pass

---

## Batch B - Medical Deep Flow

### Mục tiêu

Kéo cụm **Medicine / Schedule / OCR** từ mức “dùng được” lên mức gần RN hơn.

### File Kotlin cần ưu tiên kiểm tra

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\medical\presentation\MedicineScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\medical\presentation\AddMedicineScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\medical\presentation\MedicineScheduleScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\medical\presentation\AddMedicineScheduleScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\medical\presentation\OcrScannerScreen.kt`

### File RN phải đối chiếu

- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\medicine\MedicineCabinetScreen.tsx`
- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\medicine\AddMedicineToCabinetScreen.tsx`
- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\medicine\MedicineScheduleScreen.tsx`
- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\medicine\AddMedicineScheduleScreen.tsx`
- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\medicine\OcrScannerScreen.tsx`

### Việc cần làm

1. Kiểm tra lại layout parity của schedule screens
   - spacing
   - date/time block
   - empty state
   - CTA state

2. Kiểm tra `OcrScannerScreen`
   - overlay
   - preview state
   - editable form sau scan
   - text và button state

3. Không bắt buộc nối CameraX hoàn chỉnh nếu chưa có hạ tầng
   - nhưng UI phải trung thực
   - không được làm như đã scan thật nếu thực tế chỉ là placeholder

### Acceptance criteria

- các màn schedule/OCR không bị vỡ layout
- text tiếng Việt sạch
- flow từ tủ thuốc sang add/schedule/OCR đi xuyên được
- build pass

---

## Batch C - Global UTF-8 Sweep cho các màn còn lại

### Mục tiêu

Dọn tiếp những màn còn vỡ mã nhưng **không ưu tiên chỉnh font tuyệt đối** ở giai đoạn này.

### Màn cần kiểm tra kỹ

- `feature/profile/presentation/ProfileScreen.kt`
- `feature/medical/...`
- `feature/community/...`
- `feature/notifications/...`
- `feature/main/...`

### Cách làm

- ưu tiên các màn người dùng thấy ngay
- sửa text theo RN hoặc theo nghĩa chuẩn tiếng Việt
- không thay đổi logic nếu chỉ cần sửa text

### Acceptance criteria

- không còn các chuỗi kiểu:
  - `ThÃ´ng tin`
  - `Gia Ä‘Ã¬nh`
  - `Cá»™ng Ä‘á»“ng`
  - `Äang...`

---

## 6. Global state / routing constraints

Subagent **không được làm gãy** các phần sau:

- `activeFamilyId`
- `activeProfileId`
- routing Home -> Vaccine
- routing Family -> Profile
- routing Community -> Group chat
- routing Main tabs

Nếu cần sửa route:

- chỉ sửa route thực sự đang chết
- không được tạo route mới vô cớ
- không để tồn tại button tap mà `onClick = {}`

---

## 7. Data/API constraints

Subagent không được tùy tiện đổi API contract đang hoạt động nếu chưa xác minh backend.

### Khi sửa API layer, bắt buộc kiểm tra:

- có `/api/v1/...` đúng không
- có dùng `Response<ApiResponse<T>>` đúng không
- repository map `body()?.data` đúng không
- không hardcode dữ liệu nếu backend đã có endpoint thật

### Nếu gặp thiếu backend support

Được phép:

- giữ UI an toàn
- hiển thị message trung thực
- dùng placeholder có kiểm soát

Không được:

- fake thành “đã hoàn thiện” nếu thực ra flow chưa nối thật

---

## 8. Quy trình làm việc bắt buộc

### Sau mỗi batch

1. sửa code trực tiếp vào file Kotlin
2. chạy:

```powershell
cd D:\DoAn_MB1\CareNest\frontend
./gradlew.bat assembleDebug
```

3. nếu build fail thì phải sửa xong mới được bàn giao
4. ghi rõ:
   - file nào đã sửa
   - phần nào còn chưa làm
   - blocker nếu có

### Nếu cần cho tester cài APK

APK debug nằm tại:

- `D:\DoAn_MB1\CareNest\frontend\app\build\outputs\apk\debug\app-debug.apk`

Nếu thiết bị online:

```powershell
cd D:\DoAn_MB1\CareNest\frontend
./gradlew.bat :app:installDebug
```

---

## 9. Định dạng báo cáo mong muốn từ subagent

Subagent nên trả về theo format ngắn gọn sau:

### Đã làm
- ...

### File đã sửa
- ...

### Build
- `BUILD SUCCESSFUL` hoặc lỗi còn lại

### Còn tồn tại
- ...

### Bước tiếp theo đề xuất
- ...

---

## 10. Definition of Done cho giai đoạn này

Giai đoạn này chỉ được xem là xong khi:

- app build pass
- Community main flows không còn nút chết
- Medical deep flows không còn là shell quá lộ
- text tiếng Việt ở các flow chính đã sạch
- routing chính không bị gãy
- app đủ ổn để tester tiếp tục test luồng thật trên thiết bị

---

## 11. Ghi chú cuối

Ưu tiên hiện tại là:

1. hoàn thiện sản phẩm
2. hoàn thiện luồng chính
3. dọn text lỗi
4. sau cùng mới quay lại tinh font/icon 1:1 tuyệt đối

Nói ngắn gọn: **đừng sa đà polish khi flow chính còn hở**.
