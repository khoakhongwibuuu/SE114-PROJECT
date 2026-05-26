# 🚀 KẾ HOẠCH CHUYỂN ĐỔI FRONTEND: REACT NATIVE -> ANDROID NATIVE (KOTLIN)
**Dự án:** CareNest
**Kiến trúc:** MVVM (Model-View-ViewModel) + Retrofit2 + Jetpack Navigation

---

## 🛠️ Phase 1: Xây móng Kiến trúc & Setup (Infrastructure)
**Mục tiêu:** Dựng xong bộ khung (Boilerplate) giao tiếp an toàn với Spring Boot Backend.

* [ ] **Khởi tạo Project:** Tạo dự án trên Android Studio. Thiết lập cấu trúc thư mục theo chuẩn MVVM. Chốt sử dụng **Kotlin**.
* [ ] **Giao tiếp Mạng (Networking):** Cài đặt và cấu hình **Retrofit2** kết hợp **OkHttp**. 
    * *Lưu ý:* Viết Interceptor để tự động đính kèm `Authorization: Bearer <JWT>` và header `X-Family-Id` vào mọi request.
* [ ] **Lưu trữ & Điều hướng:** * Tích hợp **EncryptedSharedPreferences** hoặc **DataStore** để lưu trữ JWT Token và Refresh Token bảo mật.
    * Thiết lập bộ điều hướng trung tâm bằng **Jetpack Navigation Component**.
* [ ] **Luồng Xác thực (Auth):** Dựng UI và đấu nối API cho các màn hình: Đăng nhập, Đăng ký, Quên mật khẩu.

---

## 🏠 Phase 2: Core UX & Multi-Family Logic (Dashboard & Workspace)
**Mục tiêu:** Phục dựng linh hồn của CareNest - Bảng tin và luồng quản lý phân lập N-N Gia đình.

* [ ] **Dựng Form UI cơ bản:** Xây dựng khung `HomeDashboard` (Sử dụng XML Layout hoặc Jetpack Compose) và Bottom Navigation Bar.
* [ ] **Workspace Switcher (Chuyển đổi Gia đình):**
    * Tích hợp Header có icon Dropdown.
    * Dựng Bottom Sheet hiển thị danh sách gia đình user đang tham gia.
    * Xử lý logic an toàn: Khi đổi gia đình -> Gọi `ViewModel` để update biến global `X-Family-Id` -> Reset các state lọc cá nhân.
* [ ] **Thanh Thành viên (Avatar Story):** Thiết kế thanh cuộn ngang hiển thị Avatar thành viên của gia đình đang active. Xử lý trạng thái (Selected/Unselected).
* [ ] **Tích hợp Dữ liệu:** Map data từ API `/api/v1/dashboard` vào giao diện (hiển thị Lịch thuốc, Lịch hẹn, Nhắc tiêm chủng sắp tới).

---

## ⚙️ Phase 3: Các Module Nghiệp vụ Nặng & Real-time
**Mục tiêu:** Xử lý các Form nhập liệu sâu và duy trì tính năng cốt lõi (Bảo mật & Real-time).

* [ ] **Module Tiêm chủng & Khám bệnh:** Phục dựng form nhập liệu. 
    * *Bảo mật:* Tích hợp cơ chế chống double-submit (disable button khi đang call API).
* [ ] **Module Tủ thuốc (Tích hợp AI Offline):** * Thay thế API OCR gọi lên server bằng thư viện **Google ML Kit Text Recognition** chạy trực tiếp trên thiết bị (Android) để nhận diện tên thuốc từ Camera siêu tốc.
* [ ] **Real-time Chat (Luồng WebSocket):** * Sử dụng thư viện `stomp-protocol-android` để kết nối với luồng STOMP WebSocket của Spring Boot. 
    * Xử lý các trường hợp biên: Mất mạng, Stale Closure (ngắt kết nối cũ trước khi mở luồng mới).

---

## 🏥 Phase 4: Tích hợp Requirement Mới (Cộng đồng Y tế)
**Mục tiêu:** Triển khai tính năng Diễn đàn & Wiki theo requirement bổ sung của hội đồng.

* [ ] **Giao diện Cộng đồng:** Thiết kế UI cho Tab "Cộng đồng y tế" dưới Bottom Navigation.
* [ ] **Module Wiki / Cẩm nang:** * Xây dựng luồng đọc bài viết. 
    * Phân quyền hiển thị: Chỉ hiện nút "Đăng bài" nếu system role của user là `DOCTOR` hoặc `ADMIN`.
* [ ] **Module Hội nhóm:** Dựng UI danh sách các nhóm bệnh lý và luồng thảo luận (Comment/Post) bên trong từng nhóm.

---

## 💎 Phase 5: Polish, Security Test & Release
**Mục tiêu:** Đánh bóng trải nghiệm người dùng (UX) và rà soát lỗ hổng trước khi lên thớt bảo vệ.

* [ ] **UI/UX Polish:** * Bổ sung **Empty State** cho các màn hình trống dữ liệu (Tủ thuốc trống, Lịch sử tiêm chủng trống).
    * Thêm hiệu ứng **Shimmer Loading** khi chờ API.
    * Bọc chống tràn bàn phím cho tất cả các Form (`windowSoftInputMode`).
* [ ] **Kiểm thử Bảo mật (Cross-Family Security):** Test kỹ các kịch bản IDOR/BOLA. Cố tình gửi sai `X-Family-Id` hoặc ID của record không thuộc quyền sở hữu để đảm bảo server vẫn chặn chặt chẽ.
* [ ] **Deploy & Đóng gói:** * Build file `.apk` bản Release.
    * Triển khai Database lên Supabase và Spring Boot Backend lên nền tảng Cloud (Railway/Koyeb).