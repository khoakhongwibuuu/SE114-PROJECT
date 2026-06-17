# 🏥 CareNest Backend

Dự án Backend RESTful API cho ứng dụng sổ tay sức khỏe gia đình CareNest, xây dựng bằng Spring Boot 3 và PostgreSQL.
Backend hiện ưu tiên các luồng MVP thật: auth, family, health core, digital clinic, community, notification. AI Chatbot và OCR vẫn có contract cấu hình nhưng đang được tắt mặc định trong MVP.

## 🛠 Công nghệ sử dụng
- **Ngôn ngữ:** Java 17
- **Framework:** Spring Boot 3.4.5
- **Cơ sở dữ liệu:** PostgreSQL 16
- **Bộ nhớ đệm (Cache):** Redis 7
- **Bảo mật:** Spring Security + JWT Authentication
- **Object Mapper:** MapStruct, Lombok
- **Tài liệu API:** Swagger UI (OpenAPI 3)
- **AI/OCR (phase cuối):** feature-flagged, disabled mặc định

---

## 🚀 Hướng dẫn khởi chạy dự án

Bạn có thể chạy backend theo hai cách:
1. dùng `docker compose` ở root repo để dựng PostgreSQL + Redis + backend container;
2. chạy source Spring Boot tại thư mục `backend`, miễn là PostgreSQL và Redis đã sẵn sàng.

### Bước 1: Thiết lập biến môi trường
Từ thư mục root của repo, copy `.env.example` thành `.env` rồi điền tối thiểu:
- `POSTGRES_PASSWORD`
- `REDIS_PASSWORD`
- `JWT_SECRET`

Giữ `APP_FEATURE_AI_CHAT_ENABLED=false` và `APP_FEATURE_OCR_ENABLED=false` nếu bạn chỉ chạy MVP hiện tại.

### Bước 2: Khởi động bằng Docker Compose (khuyến nghị)
Bật **Docker Desktop**, mở terminal ở **root repo** và chạy:

```bash
docker compose up -d
```
Lệnh này sẽ dựng PostgreSQL, Redis và backend container theo `docker-compose.yml` ở root repo.

### Bước 3: Chạy Core Backend từ source (nếu không dùng container backend)
Nếu bạn muốn chạy backend từ source:
- PostgreSQL mặc định phải ở `localhost:5433`
- Redis mặc định phải ở `localhost:6379`
- các giá trị này được đọc từ `backend/src/main/resources/application.yml`

Có 2 cách để chạy ứng dụng:
- **Ngay trên IDE (IntelliJ / Eclipse):** Tìm file `CareNestBackendApplication.java` và ấn nút Run/Play.
- **Dùng Command Line (Maven Wrapper):** Nằm ở thư mục `backend`, gõ lệnh:

*Trên Windows (PowerShell):*
```powershell
.\mvnw.cmd spring-boot:run
```

*Lưu ý: profile mặc định đang là `dev`. Nếu chạy container backend, profile sẽ được inject qua env theo `docker-compose.yml`.*

### Bước 4: Truy cập tài liệu API (Swagger UI)
Khi backend đã chạy thành công (hiện chữ `Started CareNestBackendApplication` trên terminal), mở trình duyệt và truy cập vào đường dẫn sau:

👉 **[http://localhost:8080/api/v1/swagger-ui/index.html](http://localhost:8080/api/v1/swagger-ui/index.html)**

Tại đây, giao diện đồ hoạ Swagger sẽ liệt kê toàn bộ mọi API của ứng dụng, chuẩn hoá cấu trúc `Request/Response`, giúp bạn dễ dàng Test API mà không cần dùng Postman!

---

## 📋 Chuẩn hoá API Response

Tất cả các API trong hệ thống đều sử dụng một cấu trúc phản hồi (Envelope) duy nhất để đảm bảo tính đồng bộ cho Frontend:

**Success Response (HTTP 200/201):**
```json
{
  "success": true,
  "message": "Thông báo thành công",
  "data": { ... },
  "timestamp": "2024-04-29T..."
}
```

**Error Response (HTTP 4xx/5xx):**
```json
{
  "success": false,
  "message": "Thông báo lỗi chi tiết",
  "errors": { ... },
  "timestamp": "2024-04-29T..."
}
```

---

## 📦 Danh sách Module & Endpoint chính

Các module đang là trọng tâm của MVP:

### 1. Authentication (`/api/v1/auth`)
- `POST /register`: Đăng ký tài khoản mới.
  - Payload: `{ email, password, fullName }`
- `POST /login`: Đăng nhập hệ thống.
  - Response: `{ accessToken, refreshToken, user: { id, email, fullName, ... } }`
- `GET /me`: Lấy thông tin người dùng hiện tại đang đăng nhập.

### 2. Family & Health Profile (`/api/v1/families`)
- `POST /families`: Tạo tổ ấm mới.
- `GET /families/{id}`: Chi tiết tổ ấm và danh sách thành viên.
- `GET /health-profiles/{id}`: Chi tiết hồ sơ sức khỏe.

### AI/OCR hiện tại
- `app.features.ai-chat-enabled=false` và `app.features.ocr-enabled=false` theo mặc định.
- Khi các cờ này đang tắt, backend sẽ trả lỗi có chủ đích cho AI Chat/OCR thay vì chạy mock trong luồng thật.
- Chỉ bật lại khi đã có provider thật, model thật và contract xác nhận dữ liệu đầu ra rõ ràng.

---

## 👨‍💻 Workflow cho thành viên phát triển

Trong giai đoạn Code chức năng (Phase 3+), xin hãy tuân thủ nguyên tắc Git đã đề ra:

1. Đội hình Code luôn chụm lại ở nhánh **`develop`**. Đừng code trực tiếp lên `main`!
2. Mỗi tính năng phải có nhánh riêng. Ví dụ Anh Khoa làm tính năng tạo tủ thuốc: `git checkout -b feature/cabinet-create`.
3. Tuân theo kết cấu mẫu của **Module Auth** (Lớp `Controller` -> `Service` -> `Repository`).
4. Sử dụng `ApiResponse` và không ném Exception vô tội vạ.
5. Push nhánh lên và tạo Pull Request!

Chúc bạn code mượt mà và không lỗi! ❤️
