# 🏠 CareNest - Family Health Management System

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Latest-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.100+-009688?style=for-the-badge&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

---

## 🌟 Tổng quan dự án (Overview)
**CareNest** là ứng dụng quản lý sức khỏe gia đình đa người dùng, kết hợp Spring Boot backend với Android app Jetpack Compose, cùng với một vi dịch vụ (microservice) AI viết bằng Python FastAPI. Hệ thống hỗ trợ đầy đủ các luồng dữ liệu cho 3 role `patient`, `doctor`, `admin`.

Các tính năng nổi bật:
*   **Auth & Profile:** Đăng ký, đăng nhập an toàn, refresh token, tự động cấp phát hồ sơ y tế (auto-provisioning) cho mọi vai trò, quản lý hồ sơ cá nhân và sức khỏe.
*   **Family & Health Core:** Gia đình, thành viên, thuốc, lịch uống, tiêm chủng, tăng trưởng, lịch khám.
*   **Digital Clinic:** Hồ sơ bác sĩ, booking online/offline, không gian làm việc của bác sĩ, consultation room.
*   **Community & Chat:** Chat gia đình theo thời gian thực (real-time WebSocket), hội nhóm, bài viết nhóm.
*   **Notification:** Đếm thông báo chưa đọc, đánh dấu đã đọc, điều hướng tự động.
*   **AI Chatbot & OCR:** Phân tích nhãn thuốc (OCR) và trợ lý ảo thông minh được xử lý qua AI Gateway kết nối trực tiếp với **Google Gemini API**.

### Trạng thái triển khai hiện tại
*   **Frontend chính:** `frontend/app` (Android - Jetpack Compose).
*   **Backend chính:** `backend` (Spring Boot 3).
*   **AI Gateway:** `ai` (Python FastAPI xử lý mô hình LLM từ Google Gemini).

---

## 🛠 Công nghệ cốt lõi (Tech Stack)
Hệ thống được chia làm 3 phân hệ chính kết nối chặt chẽ với nhau:

### 1. Backend (Spring Boot Core)
*   **Core:** Java 17, Spring Boot 3.3.5
*   **Persistence:** Spring Data JPA, Hibernate, PostgreSQL 16
*   **Caching & Fast Data:** Redis 7 (Refresh Token, Metadata Caching)
*   **Real-time:** Spring Boot WebSocket (STOMP Broker, SimpMessagingTemplate)
*   **Mapping & Tooling:** MapStruct (Entity-DTO), Lombok, Validation API
*   **Security:** Spring Security, JWT (Access & Refresh Token strategy)

### 2. Frontend (Mobile App)
*   **Core:** Jetpack Compose (Kotlin)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Navigation:** Jetpack Navigation Compose
*   **Real-time Client:** STOMP over WebSocket (NaikSoftware)
*   **Network:** Retrofit 2 & OkHttp (với cơ chế interceptor cho Auth Token)
*   **Storage:** Jetpack DataStore (Preferences)

### 3. AI Gateway (Python Microservice)
*   **Core:** Python 3.12+, FastAPI, Uvicorn
*   **Integration:** urllib.request (No-SDK footprint), Google Gemini Developer API
*   **Data Validation:** Pydantic (Structured Output JSON Schema)

---

## 🏗 Điểm nhấn Kiến trúc & Kỹ thuật (Key Technical Highlights)

### 💬 Real-time WebSocket Messaging (Family Chat)
Kiến trúc chat thời gian thực đa người dùng hoạt động thông qua một STOMP message broker:
*   **JWT Handshake Authentication:** Khi client gửi khung `CONNECT`, `JwtChannelInterceptor` sẽ chặn và giải mã JWT token để xác thực người dùng ngay trước khi bắt đầu handshake.
*   **Optimistic UI & Deduplication:** Phía ứng dụng di động áp dụng cơ chế hiển thị tin nhắn ngay lập tức khi gửi (Optimistic) kết hợp với hàng đợi kiểm tra ID (`seenIds` ref) giúp loại bỏ tin nhắn bị lặp.

### 🛡 Security First: Multi-Family Isolation
Kiến trúc phân quyền phức tạp được giải quyết thông qua `FamilySecurityUtil`. Đảm bảo mỗi người dùng chỉ có thể truy cập dữ liệu thuộc về Family của họ. Kết hợp bộ lọc JWT tùy chỉnh, hệ thống ngăn chặn triệt để lỗi rò rỉ dữ liệu chéo (IDOR).

### 🤖 Gemini AI & OCR Integration
Ứng dụng sử dụng API của Google Gemini thông qua dịch vụ Gateway Python độc lập:
*   **AI Chat:** Cho phép phân tích ý định (intent) để tư vấn hoặc hướng dẫn người dùng tới bác sĩ chuyên môn một cách tự nhiên. Dữ liệu trả về tuân thủ nghiêm ngặt chuẩn cấu trúc (Structured Output JSON Schema).
*   **OCR:** Tự động trích xuất thông tin thuốc từ nhãn dán, hình ảnh đơn thuốc, trả về kết quả cấu trúc để Android app dễ dàng parse vào form thêm thuốc.

---

## 🚀 Hướng dẫn chạy dự án (Getting Started)

Hệ thống đã được tối ưu hóa và đóng gói hoàn chỉnh bằng Docker, giúp việc thiết lập môi trường cực kỳ đơn giản cho tất cả thành viên trong nhóm.

### 1. Dành cho các thành viên trong nhóm (Testers / Developers)
Để chạy dự án, bạn chỉ cần cài đặt sẵn **Docker Desktop** và **Android Studio** trên máy tính.

**Bước 1: Lấy mã nguồn và Cấu hình**
1. Clone dự án:
   ```bash
   git clone https://github.com/khoakhongwibuuu/SE114-PROJECT.git
   cd SE114-PROJECT
   ```
2. **CỰC KỲ QUAN TRỌNG:** Liên hệ với Quản trị viên (người setup chính) để xin file `.env` chuẩn của dự án. (File này chứa các chuỗi kết nối Database Neon, khoá bảo mật JWT, và Google Gemini API Key - không được đưa lên Github).
3. Đặt file `.env` vừa nhận được vào ngay thư mục gốc của dự án (ngang hàng với file `docker-compose.yml`).

**Bước 2: Khởi động Hệ thống (One-Click Setup)**
Mở Terminal tại thư mục gốc của dự án và gõ lệnh:
```bash
docker compose up -d --build
```
*(Lệnh này sẽ tự động tải các service, biên dịch Spring Boot Backend, chạy Python AI Gateway và tự động móc nối lên Neon Database chung của nhóm).*

**Bước 3: Chạy ứng dụng Android (Frontend)**

Bạn có 2 cách để cấu hình mạng cho App gọi về Backend:

**Cách 1: Dùng ADB Reverse (Khuyên dùng khi cắm cáp test máy thật)**
Cách này giúp app chạy cực kỳ ổn định qua cáp USB, không phụ thuộc vào độ lag của WiFi (rất tốt khi mang lên trường demo).
1. Cắm cáp điện thoại vào máy tính, đảm bảo đã bật USB Debugging.
2. Mở Terminal (PowerShell) và chạy lệnh:
   ```bash
   & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:8080 tcp:8080
   ```
3. Mở file `frontend/local.properties` và để nguyên hoặc set thành `localhost`:
   ```properties
   HOST_IP=localhost
   AI_CHAT_ENABLED=true
   ```

**Cách 2: Cấu hình qua IP LAN (Dùng khi test máy thật qua WiFi)**
1. Mở Terminal và gõ `ipconfig` (trên Windows) để xem địa chỉ IPv4 mạng WiFi của máy tính (ví dụ: `192.168.1.71`).
2. Mở file `frontend/local.properties` và cấu hình lại IP:
   ```properties
   HOST_IP=192.168.1.71
   AI_CHAT_ENABLED=true
   ```

**Cuối cùng:** Mở thư mục `frontend` bằng phần mềm **Android Studio**. Bấm nút **Run (▶️)** để cài app lên điện thoại thật hoặc máy ảo.

---

### 2. Dành cho Quản trị viên (Người cung cấp file cấu hình)
Với tư cách là người thiết lập dự án, bạn có trách nhiệm tạo và gửi file `.env` (hoặc nội dung của nó) cho các bạn khác trong nhóm. Một file `.env` chuẩn để nhóm chạy Local với Neon Database sẽ cần có tối thiểu:

```env
# Môi trường chạy (qa để bật chế độ tạo dữ liệu mẫu giả lập)
SPRING_PROFILES_ACTIVE=qa,prod

# Chuỗi kết nối Neon Database
SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-url>/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=<neon-user>
SPRING_DATASOURCE_PASSWORD=<neon-pass>

# API Key của Gemini AI
AI_API_KEY=<your-google-gemini-key>

# Bật tính năng giả lập dữ liệu ban đầu
APP_SEED_QA_DEMO_ENABLED=true
APP_SEED_QA_DEMO_DEFAULT_PASSWORD=Password123!
```

---

## 7. Dữ liệu mẫu (QA Seeding)
Để khởi tạo dữ liệu mẫu cho lần chạy đầu tiên:
*   Trong `.env`, đặt `APP_SEED_QA_DEMO_ENABLED=true` và `APP_SEED_QA_DEMO_DEFAULT_PASSWORD=Password123!`
*   Xem chi tiết các tài khoản (Admin, Doctor, Patient) tại [TEST_ACCOUNTS_AND_SCENARIOS.md](TEST_ACCOUNTS_AND_SCENARIOS.md).

---

## ☁️ Định hướng Triển khai (Deployment Strategy)
Dự án được thiết kế với kiến trúc linh hoạt, sẵn sàng để deploy lên các nền tảng Cloud hiện đại (Serverless & PaaS). Dưới đây là mô hình hạ tầng dự kiến trong tương lai để tối ưu chi phí và hiệu năng:

*   **🐘 Cơ sở dữ liệu (PostgreSQL):** Triển khai trên **Neon.tech** (Serverless Postgres) giúp tự động mở rộng (auto-scaling) và tiết kiệm tài nguyên khi không có truy vấn.
*   **⚡ In-memory Cache & Pub/Sub (Redis):** Sử dụng **Upstash** (Serverless Data) quản lý session, refresh token và hỗ trợ tối ưu hiệu suất cho STOMP WebSocket.
*   **☕ Backend (Spring Boot Core):** Triển khai dưới dạng Web Service trên **Render.com**. Spring Boot sẽ kết nối với Neon và Upstash thông qua biến môi trường (Environment Variables).
*   **🐍 AI Gateway (Python FastAPI):** Triển khai thành một Web Service độc lập trên **Render.com**. Cung cấp API nội bộ cho Spring Boot gọi qua mạng nội bộ hoặc public endpoint bảo mật.

Việc tách rời các dịch vụ như trên giúp hệ thống dễ dàng chịu tải (horizontal scaling) đối với từng nút cổ chai cụ thể (ví dụ như dịch vụ xử lý AI thường tiêu tốn nhiều CPU hơn).

---

## 📖 Tài liệu API (API Documentation)
Sau khi Backend khởi động thành công, bạn có thể truy cập tài liệu API trực quan tại:

📍 **OpenAPI/Swagger UI:** [http://localhost:8080/api/v1/swagger-ui/index.html](http://localhost:8080/api/v1/swagger-ui/index.html)

---
> **CareNest** - *Hệ sinh thái chăm sóc sức khỏe toàn diện cho gia đình.*
