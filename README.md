# 🏠 CareNest - Family Health Management System

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Latest-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![JWT](https://img.shields.io/badge/JWT-Secure-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)](https://jwt.io/)

---

## 🌟 Tổng quan dự án (Overview)
**CareNest** là ứng dụng quản lý sức khỏe gia đình đa người dùng, kết hợp Spring Boot backend với Android app Jetpack Compose. Trọng tâm hiện tại là đưa các luồng MVP chạy ổn với dữ liệu thật cho 3 role `patient`, `doctor`, `admin`, trước khi mở lại các tính năng AI nâng cao.

MVP hiện tập trung vào:
*   **Auth & Profile:** đăng ký, đăng nhập, refresh token, hồ sơ cá nhân và hồ sơ sức khỏe.
*   **Family & Health Core:** gia đình, thành viên, thuốc, lịch uống, tiêm chủng, tăng trưởng, lịch khám.
*   **Digital Clinic:** hồ sơ bác sĩ, booking online/offline, doctor workspace, consultation room.
*   **Community & Chat:** family chat, hội nhóm, bài viết nhóm, moderation cơ bản.
*   **Notification:** unread count, mark read, điều hướng về đúng luồng nghiệp vụ.

> AI Chatbot và OCR đang được để ở **phase cuối**. Trong mã nguồn hiện tại, hai tính năng này được tắt mặc định và không nên coi là luồng MVP đã hoàn tất.

### Trạng thái triển khai hiện tại
*   **Frontend chính:** `frontend/app` (Jetpack Compose).
*   **Frontend legacy/reference:** `frontend/CareNestApp` (React Native), không phải luồng phát triển chính.
*   **AI/OCR:** disabled mặc định bằng feature flag, chỉ bật khi đã có provider thật, contract rõ và bước xác nhận dữ liệu an toàn.

---

## 🛠 Công nghệ cốt lõi (Tech Stack)
Hệ thống được xây dựng trên nền tảng công nghệ mạnh mẽ, chia làm 2 phân hệ chính:

### Backend (Spring Boot Core)
*   **Core:** Java 17, Spring Boot 3.3.5
*   **Persistence:** Spring Data JPA, Hibernate, PostgreSQL 16
*   **Caching & Fast Data:** Redis 7 (Refresh Token, Metadata Caching)
*   **Intelligence (phase cuối):** provider AI qua env/config, disabled mặc định trong MVP
*   **Real-time:** Spring Boot WebSocket (STOMP Broker, SimpMessagingTemplate)
*   **Mapping & Tooling:** MapStruct (Entity-DTO), Lombok, Validation API
*   **Infrastructure:** Docker & Docker Compose
*   **Security:** Spring Security, JWT (Access & Refresh Token strategy)

### Frontend (Mobile App)
*   **Core:** Jetpack Compose (Kotlin)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Navigation:** Jetpack Navigation Compose
*   **Real-time Client:** STOMP over WebSocket (NaikSoftware)
*   **Network:** Retrofit 2 & OkHttp (với cơ chế interceptor cho Auth Token)
*   **Storage:** Jetpack DataStore (Preferences)

### Lưu ý về phạm vi MVP
*   Những phần AI/OCR trong tài liệu legacy không phản ánh trạng thái release hiện tại.
*   Khi chưa cấu hình provider thật, backend sẽ trả lỗi có chủ đích cho AI/OCR và Android sẽ hiển thị trạng thái tạm tắt thay vì chạy mock trong luồng thật.

---

## 🏗 Điểm nhấn Kiến trúc & Kỹ thuật (Key Technical Highlights)

### 💬 Real-time WebSocket Messaging (Family Chat)
Kiến trúc chat thời gian thực đa người dùng hoạt động thông qua một STOMP message broker:
*   **JWT Handshake Authentication:** Khi client gửi khung `CONNECT`, `JwtChannelInterceptor` sẽ chặn và giải mã JWT token để xác thực người dùng ngay trước khi bắt đầu bắt tay (handshake).
*   **Robust Security context mapping:** Khắc phục triệt để lỗi không đồng bộ hóa của Spring Security trong luồng WebSocket bất đồng bộ bằng cách sử dụng `java.security.Principal` nguyên bản để xác định email người gửi, kết hợp trực tiếp truy vấn DB thực thể người dùng để đảm bảo luôn thu được ID người dùng hợp lệ trước khi lưu tin nhắn.
*   **Optimistic UI & Deduplication:** Phía ứng dụng di động áp dụng cơ chế hiển thị tin nhắn ngay lập tức khi gửi (Optimistic) kết hợp với hàng đợi kiểm tra ID (`seenIds` ref) giúp loại bỏ tin nhắn bị lặp (WS echo) và nâng cao trải nghiệm người dùng tối đa.

### 🛡 Security First: Multi-Family Isolation
Kiến trúc phân quyền phức tạp được giải quyết thông qua `FamilySecurityUtil`. Hệ thống đảm bảo mỗi người dùng chỉ có thể truy cập dữ liệu thuộc về Family của họ. Kết hợp với bộ lọc JWT tùy chỉnh, CareNest cung cấp cơ chế bảo mật đa tầng, ngăn chặn triệt để lỗi rò rỉ dữ liệu chéo (IDOR).

### ⚡ Performance Optimization
*   **N+1 Query Resolution:** Giải quyết triệt để vấn đề hiệu năng tại màn hình Dashboard tổng quát bằng kỹ thuật `JOIN FETCH` trong Spring Data JPA.
*   **Hybrid Caching:** Tích hợp Redis làm lớp đệm cho các thông tin ít thay đổi. Sử dụng cơ chế `Programmatic Cache Eviction` để đảm bảo dữ liệu luôn nhất quán (Consistency) ngay khi có cập nhật.

### 🤖 AI/OCR (Phase cuối)
Kiến trúc AI/OCR vẫn được giữ sẵn dưới dạng feature flag và env contract:
*   **AI Chat:** chỉ nên bật khi đã cấu hình provider thật, mô hình thật và có ràng buộc safety/structured JSON rõ ràng.
*   **OCR:** chỉ nên bật khi đã có pipeline ảnh thật và bước xác nhận dữ liệu trước khi lưu vào medication thật.
*   **MVP hiện tại:** AI/OCR mặc định tắt, không được coi là tiêu chí PASS của runtime MVP.

### 📅 Automation Tasks
*   **Auto-rescheduling:** Thuật toán tự động tịnh tiến ngày tiêm chủng dựa trên ngày tiêm thực tế của các mũi trước đó.
*   **Background Jobs:** Sử dụng Cronjobs ngầm để tự động tính toán và rải lịch nhắc nhở uống thuốc hàng ngày, giảm tải cho Main Thread và gửi Push Notification.

---

## 🚀 Hướng dẫn chạy dự án (Getting Started)

### 1. Yêu cầu hệ thống (Prerequisites)
*   **Java 17** (cho Spring Boot và Android build)
*   **Docker & Docker Compose** (cho PostgreSQL + Redis nếu chạy runtime local)
*   **Android Studio** (để chạy `frontend/app`)

### 2. Clone Repo
```bash
git clone https://github.com/khoakhongwibuuu/SE114-PROJECT.git
cd SE114-PROJECT
```

### 3. Thiết lập Backend (Spring Boot)

**Thiết lập biến môi trường:**
1. Copy `.env.example` thành `.env`.
2. Điền các giá trị bắt buộc như `POSTGRES_PASSWORD`, `REDIS_PASSWORD`, `JWT_SECRET`.
3. Giữ `APP_FEATURE_AI_CHAT_ENABLED=false` và `APP_FEATURE_OCR_ENABLED=false` nếu bạn đang chạy đúng phạm vi MVP hiện tại.

**Khởi động Database, Redis và backend container (bằng Docker Compose ở root repo):**
```bash
docker compose up -d
```

**Hoặc chạy Spring Boot Server từ source:**
Nếu bạn chỉ muốn chạy service Spring Boot từ source, hãy đảm bảo PostgreSQL đang ở `localhost:5433` và Redis ở `localhost:6379` theo cấu hình mặc định trong `backend/src/main/resources/application.yml`.

Bạn có thể mở thư mục `backend` bằng **IntelliJ IDEA** và chạy class `CarenestApplication`. Hoặc dùng dòng lệnh:
```bash
# Trên Windows
cd backend
.\mvnw.cmd spring-boot:run

# Trên MacOS/Linux
cd backend
./mvnw spring-boot:run
```
*(Backend sẽ chạy ở cổng `http://localhost:8080/api/v1`)*

### 4. Thiết lập Frontend Android (`frontend/app`)

**Cấu hình IP máy tính:**
Để ứng dụng Android (chạy trên điện thoại hoặc máy ảo) kết nối được với Backend đang chạy trên máy tính, bạn cần cấu hình IP nội bộ:
1. Mở terminal, dùng lệnh `ipconfig` (Windows) hoặc `ifconfig` (Mac/Linux) để tìm **IPv4 Address** của máy tính (ví dụ: `192.168.1.5`).
2. Mở file `frontend/local.properties` (nếu chưa có thì tự tạo) và thêm dòng sau:
   `HOST_IP=192.168.1.5`

**Chạy ứng dụng:**

**Cách 1: Dùng Android Studio (Khuyến nghị)**
1. Mở thư mục `frontend` bằng phần mềm **Android Studio**.
2. Chờ Gradle đồng bộ (sync) hoàn tất.
3. Cắm cáp điện thoại Android hoặc mở máy ảo (Android Emulator).
4. Bấm nút **Run (▶️)** trên thanh công cụ của Android Studio.

**Cách 2: Dùng Command Line (Terminal)**
Nếu bạn không muốn mở Android Studio, chỉ cần cắm điện thoại thật (đã bật USB Debugging) hoặc bật máy ảo lên, sau đó mở terminal ở thư mục `frontend` và chạy lệnh sau để build và cài trực tiếp lên máy:
```bash
# Trên Windows
.\gradlew.bat :app:installDebug

# Trên MacOS/Linux
./gradlew :app:installDebug
```

### 5. Ghi chú AI/OCR
*   `AI_ENABLED`, `OCR_ENABLED`, `APP_FEATURE_AI_CHAT_ENABLED`, `APP_FEATURE_OCR_ENABLED` đều có trong `.env.example`.
*   Chỉ bật các cờ này khi đã có provider thật, model thật và kế hoạch xác minh dữ liệu đầu ra.
*   Nếu chỉ muốn chạy MVP, để toàn bộ cờ AI/OCR ở `false`.

---

## 📊 Cấu trúc Database (Entity Summary)
CareNest sở hữu cấu trúc Database mạnh mẽ gồm **18 bảng** thực thể được chuẩn hóa:
*   **Audit Engine:** Tất cả các bảng đều kế thừa `BaseEntity`, hỗ trợ tự động lưu vết `createdAt`, `updatedAt` và người thực hiện.
*   **Safety Layer:** Áp dụng **Soft Delete** (Xóa mềm) giúp phục hồi dữ liệu và đảm bảo tính toàn vẹn của lịch sử y tế thay vì xóa vĩnh viễn.

---

## 📖 Tài liệu API (API Documentation)
Sau khi Backend khởi động thành công, bạn có thể truy cập tài liệu API trực quan tại:

📍 **OpenAPI/Swagger UI:** [http://localhost:8080/api/v1/swagger-ui/index.html](http://localhost:8080/api/v1/swagger-ui/index.html)

---
> **CareNest** - *Hệ sinh thái chăm sóc sức khỏe toàn diện cho gia đình.*
