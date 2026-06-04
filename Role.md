 """# 🔐 TÀI LIỆU KIẾN TRÚC PHÂN QUYỀN (RBAC & ABAC POLICY) - HỆ THỐNG CARENEST

Tài liệu này xác định chi tiết ma trận phân quyền, phạm vi truy cập dữ liệu và cơ chế kiểm soát an ninh tầng hệ thống (System Roles) kết hợp ngữ cảnh không gian làm việc (Family/Workspace Context) cho ứng dụng CareNest.

---

## 1. TỔNG QUAN HỆ THỐNG VAI TRÒ (ROLES OVERVIEW)

Hệ thống áp dụng mô hình lai giữa **RBAC (Role-Based Access Control)** để kiểm soát tính năng hệ thống và **ABAC (Attribute-Based Access Control)** để kiểm soát phân lập dữ liệu gia đình.

### 1.1. Hệ thống Vai trò (System Roles)
Cấp quyền trên quy mô toàn nền tảng, được lưu trữ tại cột `role` trong bảng `User`:
* **`ROLE_ADMIN` (Quản trị viên hệ thống):** Toàn quyền điều hành hạ tầng, phê duyệt danh tính chuyên gia, xử lý báo cáo vi phạm và giám sát tài nguyên.
* **`ROLE_DOCTOR` (Bác sĩ / Chuyên gia y tế):** Tài khoản đã qua kiểm duyệt E-KYC (chứng chỉ hành nghề). Có đặc quyền xuất bản nội dung chuyên môn trên Wiki và tham gia định hướng y khoa tại các hội nhóm bệnh lý.
* **`ROLE_USER` (Thành viên tiêu chuẩn):** Người dùng mặc định sau khi đăng ký. Có toàn quyền sử dụng các tính năng chăm sóc cá nhân và quản lý không gian gia đình của chính họ.

### 1.2. Ngữ cảnh Không gian Gia đình (Family Workspace Context)
Do một `USER` hoặc `DOCTOR` có thể tham gia vào nhiều gia đình cùng lúc (Mối quan hệ N-N), quyền hạn đối với dữ liệu nội bộ (Sức khỏe, Lịch hẹn, Tủ thuốc) sẽ bị ràng buộc bởi `X-Family-Id` truyền lên Header từ Frontend, được xác thực chéo tại Service Layer của Backend.

---

## 2. MA TRẬN PHÂN QUYỀN TỔNG QUÁT (RBAC MATRIX)

| Ký hiệu quyền hạn | Định nghĩa |
| :---: | :--- |
| **ALL** | Có toàn quyền Tạo, Đọc, Sửa, Xóa trên mọi phạm vi (Global) |
| **OWN** | Chỉ có quyền thao tác trên dữ liệu thuộc sở hữu cá nhân hoặc thuộc Gia đình đang Active (`X-Family-Id`) |
| **R_ONLY** | Chỉ có quyền Đọc dữ liệu (Read-only) |
| **DENY** | Hoàn toàn bị chặn (HTTP 403 Forbidden) |

| STT | Phân mục chức năng / API Endpoint | ROLE_ADMIN | ROLE_DOCTOR | ROLE_USER |
| :---: | :--- | :---: | :---: | :---: |
| **1** | **Xác thực & Tài khoản (Auth & Profile)** | | | |
| | Lấy thông tin cá nhân (`/auth/me`) | ALL | ALL | ALL |
| | Cập nhật hồ sơ cá nhân | ALL | ALL | ALL |
| **2** | **Quản lý Không gian Gia đình (Family Context)** | | | |
| | Tạo gia đình mới (`POST /family`) | ALL | OWN | OWN |
| | Tạo mã mời / Gửi email invite | DENY | OWN (Chủ nhà) | OWN (Chủ nhà) |
| | Chuyển đổi không gian (`X-Family-Id`) | ALL | OWN | OWN |
| | Trục xuất thành viên / Giải tán nhà | DENY | OWN (Chủ nhà) | OWN (Chủ nhà) |
| **3** | **Quản lý Y tế Gia đình (Medical & Records)** | | | |
| | Xem/Sửa hồ sơ sức khỏe (`HealthProfile`) | ALL | OWN | OWN |
| | Nhật ký tăng trưởng (`GrowthRecord`) | ALL | OWN | OWN |
| | Quản lý tủ thuốc & Lịch uống (`Cabinet`) | ALL | OWN | OWN |
| | Quét đơn thuốc bằng AI (`OCR Scanner`) | ALL | OWN | OWN |
| | Xem/Thêm lịch tiêm chủng (`Vaccination`) | ALL | OWN | OWN |
| **4** | **Cộng đồng Y tế (Community Wiki & Forums)** | | | |
| | Đọc bài viết chuyên môn (`GET /articles`) | ALL | ALL | ALL |
| | Đăng/Sửa bài viết Wiki (`POST /articles`) | ALL | ALL | DENY |
| | Lấy danh sách nhóm bệnh lý (`GET /communities`) | ALL | ALL | ALL |
| | Chat real-time trong nhóm (`POST /posts`) | ALL | ALL | ALL |
| **5** | **Hệ thống & Thẩm định (E-KYC Verification)** | | | |
| | Nộp hồ sơ chứng chỉ hành nghề Bác sĩ | DENY | DENY | ALL |
| | Xem trạng thái đơn KYC cá nhân | DENY | DENY | ALL |
| | Lấy danh sách hồ sơ đang chờ duyệt | ALL | DENY | DENY |
| | Phê duyệt / Từ chối hồ sơ KYC | ALL | DENY | DENY |

---

## 3. PHÂN RÃ CHI TIẾT LOGIC PHÂN QUYỀN THEO MODULE

### 3.1. Module Xác thực & E-KYC Bác sĩ (Auth & Doctor Verification)
Module này chịu trách nhiệm kiểm soát đầu vào nghiêm ngặt để ngăn chặn tình trạng giả mạo chuyên gia y tế.

* **Quy trình State-Machine của `ROLE_USER`:**
    * Khi trạng thái KYC là `null`: Được phép gọi `POST /api/v1/doctor-verifications` để gửi số chứng chỉ, chuyên khoa, nơi công tác và ảnh minh chứng.
    * Khi trạng thái KYC là `PENDING`: Hệ thống khóa toàn bộ Form nộp ở Frontend, chặn API ghi đè ở Backend để tránh spam dữ liệu.
    * Khi trạng thái KYC là `REJECTED`: Người dùng nhận lý do từ chối, hệ thống mở lại quyền sửa đổi và nộp lại (chuyển trạng thái về lại `PENDING`).
    * Khi trạng thái KYC là `APPROVED`: Hệ thống thực thi một Transaction ngầm: Cập nhật `DoctorVerification.status = APPROVED` đồng thời nâng cấp `User.role = Role.DOCTOR`.
* **Quyền hạn của `ROLE_ADMIN`:**
    * Sử dụng `@PreAuthorize("hasRole('ADMIN')")` tại `AdminVerificationController`.
    * Có quyền xem danh sách `PENDING`, gọi `PATCH /approve` để nâng cấp role cho user, hoặc `PATCH /reject` kèm lý do cụ thể. Chặn đứng nguy cơ phân quyền leo thang (Privilege Escalation).

### 3.2. Module Không gian Gia đình & Bảo mật BOLA/IDOR
Kiểm soát chặt chẽ luồng truy cập chéo dữ liệu giữa các hộ gia đình (Multi-Family Context).

* **Logic Ràng buộc tầng Service (Service-Level Security):**
    * Mọi API liên quan đến nghiệp vụ nội bộ bắt buộc phải bóc tách `X-Family-Id` từ header hoặc tham số đầu vào.
    * Hệ thống thực hiện truy vấn kiểm tra chéo: `familyRepository.existsByIdAndUsers_Id(familyId, currentUserId)`.
    * Nếu kết quả trả về `false`, hệ thống lập tức ném ra lỗi `AccessDeniedException` (HTTP 403) kể cả khi Frontend cố tình gửi sai Header hoặc sửa đổi ID trên URL (Vá triệt để lỗ hổng IDOR/BOLA).
* **Sự khác biệt giữa các Role:**
    * `ROLE_USER` và `ROLE_DOCTOR` có quyền hạn như nhau khi ở trong một gia đình (quyền hạn phụ thuộc vào việc họ có phải là Chủ nhà tạo mã mời hay không).
    * `ROLE_ADMIN` có đặc quyền Bypass bộ lọc gia đình để xử lý các sự cố kỹ thuật.

### 3.3. Module Cộng đồng Y tế (Community Wiki & Forum Groups)
Module thực hiện mô hình phân tách luồng thông tin: Wiki (Một chiều - Học thuật) và Diễn đàn (Hai chiều - Thảo luận).

* **Luồng Wiki (Cẩm nang sức khỏe):**
    * **Quyền Đọc:** Mở public hoàn toàn cho mọi tài khoản đã xác thực thông qua endpoint `GET /api/v1/articles`.
    * **Quyền Ghi:** Chỉ cấu hình cho `ROLE_DOCTOR` và `ROLE_ADMIN` thông qua bảo mật.
    * `ROLE_USER` hoàn toàn bị ẩn nút tạo bài (FAB) ở Frontend và bị chặn chặt từ Gateway/Controller ở Backend.
* **Luồng Hội nhóm bệnh lý (Diễn đàn):**
    * Tất cả 3 vai trò (`USER`, `DOCTOR`, `ADMIN`) đều có quyền truy cập vào danh sách nhóm.
    * Mọi thành viên đều có quyền gửi bài đăng thảo luận dưới dạng phòng chat Real-time kết nối STOMP WebSocket.

---
