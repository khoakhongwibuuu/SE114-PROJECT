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
**CareNest** là một siêu ứng dụng (Super App) quản lý sức khỏe gia đình đa người dùng (Multi-tenant/Multi-family) tiên tiến, kết hợp giữa Mobile App và Backend mạnh mẽ. Dự án được thiết kế để giải quyết bài toán quản lý y tế phức tạp trong gia đình thông qua việc tích hợp Trí tuệ nhân tạo (AI) và tự động hóa quy trình nghiệp vụ.

Hệ thống không chỉ dừng lại ở việc lưu trữ dữ liệu mà còn chủ động hỗ trợ người dùng thông qua:
*   **AI Health Advisor:** Trợ lý ảo tư vấn sức khỏe dựa trên hồ sơ y tế thực tế của từng thành viên.
*   **Smart OCR:** Tự động nhận diện đơn thuốc từ ảnh chụp để lên lịch nhắc nhở uống thuốc.
*   **Smart Automation:** Tự động rải lịch tiêm chủng, điều chỉnh lịch uống thuốc thông minh và cảnh báo tương tác thuốc.
*   **Real-time Family Chat:** Hộp chat box gia đình thời gian thực dựa trên giao thức STOMP WebSocket bảo mật cao.

---

## 🛠 Công nghệ cốt lõi (Tech Stack)
Hệ thống được xây dựng trên nền tảng công nghệ mạnh mẽ, chia làm 2 phân hệ chính:

### Backend (Spring Boot Core)
*   **Core:** Java 17, Spring Boot 3.3.5
*   **Persistence:** Spring Data JPA, Hibernate, PostgreSQL 16
*   **Caching & Fast Data:** Redis 7 (Refresh Token, Metadata Caching)
*   **Intelligence:** Spring AI, Google Gemini LLM API
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

### 🤖 AI Hybrid Architecture
Ứng dụng mô hình chuẩn **"Human-in-the-loop"**:
*   **Context Injection:** Bơm dữ liệu y tế thực tế vào Prompt giúp Gemini phản hồi chính xác với tình trạng bệnh nhân.
*   **OCR Engine:** Kết hợp xử lý ảnh và LLM để bóc tách thông tin đơn thuốc, sau đó cho phép người dùng xác nhận lại trước khi lưu trữ chính thức.

### 📅 Automation Tasks
*   **Auto-rescheduling:** Thuật toán tự động tịnh tiến ngày tiêm chủng dựa trên ngày tiêm thực tế của các mũi trước đó.
*   **Background Jobs:** Sử dụng Cronjobs ngầm để tự động tính toán và rải lịch nhắc nhở uống thuốc hàng ngày, giảm tải cho Main Thread và gửi Push Notification.

---

## 🚀 Hướng dẫn chạy dự án (Getting Started)

### 1. Yêu cầu hệ thống (Prerequisites)
*   **Java 17** (Dành cho Spring Boot và Android Build)
*   **Docker & Docker Compose** (Dành cho Database)
*   **Android Studio** (Koala / Ladybug hoặc mới nhất)

### 2. Clone Repo
```bash
git clone https://github.com/khoakhongwibuuu/SE114-PROJECT.git
cd SE114-PROJECT
```

### 3. Thiết lập Backend (Spring Boot)

**Khởi động Database & Redis (bằng Docker):**
```bash
cd backend
docker-compose up -d
```

**Cấu hình biến môi trường (nếu cần sử dụng tính năng AI):**
Trong thư mục `backend`, copy file `.env.example` thành `.env` và điền `GEMINI_API_KEY` của bạn.

**Chạy Spring Boot Server:**
Bạn có thể mở thư mục `backend` bằng **IntelliJ IDEA** và chạy class `CarenestApplication`. Hoặc dùng dòng lệnh:
```bash
# Trên Windows
.\mvnw.cmd spring-boot:run

# Trên MacOS/Linux
./mvnw spring-boot:run
```
*(Backend sẽ chạy ở cổng `http://localhost:8080/api/v1`)*

### 4. Thiết lập Frontend (Android App)

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
