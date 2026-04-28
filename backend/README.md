# 🏥 CareNest Backend

Dự án Backend RESTful API cho ứng dụng sổ tay sức khỏe gia đình CareNest, xây dựng bằng Spring Boot 3 và PostgreSQL.
Hệ thống bao gồm 12 modules cốt lõi trong đó có Quản lý thành viên, Thông minh nhân tạo (AI Chatbot) và OCR.

## 🛠 Công nghệ sử dụng
- **Ngôn ngữ:** Java 17
- **Framework:** Spring Boot 3.4.5
- **Cơ sở dữ liệu:** PostgreSQL 16 
- **Bộ nhớ đệm (Cache):** Redis 7
- **Bảo mật:** Spring Security + JWT Authentication
- **Object Mapper:** MapStruct, Lombok
- **Tài liệu API:** Swagger UI (OpenAPI 3)

---

## 🚀 Hướng dẫn khởi chạy dự án

Bạn không cần cài đặt cơ sở dữ liệu trên máy tính. Kho code này đã được tích hợp Docker.

### Bước 1: Khởi động Database (Bắt buộc)
Bật ứng dụng **Docker Desktop** trên máy tính của bạn lên. 
Mở Terminal / PowerShell, điều hướng vào thư mục chứa code `backend` và chạy lệnh sau để khởi tạo đồng thời CSDL PostgreSQL và Redis:

```bash
docker-compose up -d
```
Trạng thái thành công: Lúc này cả 2 máy chủ DB đều đang ngầm rinh trên máy bạn. Không cần cài tay!

### Bước 2: Chạy Core Backend
Có 2 cách để chạy ứng dụng:
- **Ngay trên IDE (IntelliJ / Eclipse):** Tìm file `CareNestBackendApplication.java` và ấn nút Run/Play.
- **Dùng Command Line (Maven Wrapper):** Nằm yên ở thư mục `backend`, gõ lệnh:

*Trên Windows (PowerShell):*
```powershell
.\mvnw.cmd spring-boot:run
```

*Lưu ý: Trong lần khởi chạy đầu tiên, mã nguồn (Hibernate) sẽ tự động sinh ra 18 bảng vật lý bên trong Database y như thiết kế của Leader.*

### Bước 3: Truy cập tài liệu API (Swagger UI)
Khi backend đã chạy thành công (hiện chữ `Started CareNestBackendApplication` trên terminal), mở trình duyệt và truy cập vào đường dẫn sau:

👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

Tại đây, giao diện đồ hoạ Swagger sẽ liệt kê toàn bộ mọi API của ứng dụng, chuẩn hoá cấu trúc `Request/Response`, giúp bạn dễ dàng Test API mà không cần dùng Postman!

---

## 👨‍💻 Workflow cho thành viên phát triển

Trong giai đoạn Code chức năng (Phase 3+), xin hãy tuân thủ nguyên tắc Git đã đề ra:

1. Đội hình Code luôn chụm lại ở nhánh **`develop`**. Đừng code trực tiếp lên `main`!
2. Mỗi tính năng phải có nhánh riêng. Ví dụ Anh Khoa làm tính năng tạo tủ thuốc: `git checkout -b feature/cabinet-create`.
3. Tuân theo kết cấu mẫu của **Module Auth** (Lớp `Controller` -> `Service` -> `Repository`).
4. Sử dụng `ApiResponse` và không ném Exception vô tội vạ.
5. Push nhánh lên và tạo Pull Request!

Chúc bạn code mượt mà và không lỗi! ❤️
