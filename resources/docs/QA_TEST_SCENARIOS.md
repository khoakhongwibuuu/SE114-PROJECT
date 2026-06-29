# Kịch Bản Kiểm Thử Toàn Diện - CareNest App (QA Test Scenarios)

Mục tiêu: Đảm bảo toàn bộ các luồng chức năng MVP của CareNest hoạt động ổn định trên môi trường thực tế (Local/Staging) từ góc độ người dùng cuối (Bệnh nhân, Bác sĩ, Quản trị viên).

## 1. Chuẩn Bị Môi Trường & Dữ Liệu
- Chạy backend thông qua `docker-compose up -d` (PostgreSQL, Redis).
- Chạy backend Spring Boot với profile `qa` hoặc `dev` và bật `APP_SEED_QA_DEMO_ENABLED=true` trong `.env` để tự động khởi tạo dữ liệu mẫu.
- Cài đặt App (Android) và đảm bảo `HOST_IP` trỏ đúng vào server nội bộ. Bật `OCR_ENABLED=true` trong cài đặt môi trường nếu muốn test luồng quét tự động (tùy chọn).

### Tài Khoản Kiểm Thử (Seeded Accounts)
| Vai trò | Email | Mật khẩu | Mục đích kiểm thử |
| --- | --- | --- | --- |
| Admin | `admin@gmail.com` | `Password123!` | Quản trị hệ thống, duyệt nhóm, phân quyền |
| Bác sĩ (Nhi khoa) | `bacsinhikhoa@gmail.com` | `Password123!` | Tư vấn trực tuyến, quản lý lịch hẹn nhi |
| Bác sĩ (Đa khoa) | `bacsidakhoa@gmail.com` | `Password123!` | Xem lịch sử tư vấn cũ, tư vấn đa khoa |
| Bệnh nhân A | `kiet@gmail.com` | `Password123!` | Test luồng dữ liệu đầy đủ (Gia đình, Thuốc, Lịch khám) |
| Bệnh nhân B | `doletuankiet06@gmail.com` | `Password123!` | Test luồng dữ liệu trống (Sparse-data), xin quyền |
| QA Moderator | `qa.moderator@gmail.com` | `Password123!` | Kiểm duyệt bài viết, quản lý cộng đồng |

---

## 2. Các Kịch Bản Kiểm Thử Chính (Test Cases)

### Luồng 1: Xác Thực & Tài Khoản (Authentication & eKYC)
- **TC1.1:** Đăng nhập thành công với tài khoản Bệnh nhân, Bác sĩ và Admin. Xác minh app chuyển hướng đúng Dashboard tương ứng với vai trò.
- **TC1.2:** Đăng nhập sai thông tin (hiển thị thông báo lỗi rõ ràng).
- **TC1.3:** Refresh Token: Đăng nhập, tắt app trong đa nhiệm, mở lại app (phiên đăng nhập vẫn được giữ nguyên).
- **TC1.4:** (Tùy chọn) Flow tải ảnh CCCD/bằng cấp để xác thực Bác sĩ (eKYC).
- **TC1.5:** Khởi tạo hồ sơ tự động (Auto-provisioning): Đăng nhập bằng tài khoản Bác sĩ hoặc QA Moderator (những user được seed sẵn chưa có hồ sơ y tế). Xác minh hệ thống tự động cấp phát một hồ sơ y tế cơ bản và cho phép người dùng vào cập nhật ở màn "Hồ sơ y tế" thay vì bị chặn.

### Luồng 2: Hồ Sơ Gia Đình & Theo Dõi Sức Khỏe
- **TC2.1:** Chuyển đổi thành viên: Từ màn hình Home, đổi profile từ Bệnh nhân A sang "Bé Na" (thành viên gia đình). Xác minh dữ liệu hiển thị tương ứng với Bé Na.
- **TC2.2 (Camera OCR):** Mở Tủ thuốc (Medicine Cabinet) -> Chọn "Quét đơn thuốc/Nhãn thuốc" -> Cấp quyền Camera -> Quét một ảnh mẫu đơn thuốc. Xác minh công cụ nhận dạng chữ (OCR) trích xuất thành công văn bản và điền tự động vào form Thêm thuốc mới.
- **TC2.3:** Lịch uống thuốc: Tạo lịch nhắc uống thuốc, kiểm tra Dashboard hiển thị task cần làm hôm nay.
- **TC2.4:** Sổ tiêm chủng (Vaccine): Thêm mũi tiêm mới cho "Bé Na", kiểm tra trạng thái hoàn tất hiển thị đúng.

### Luồng 3: Hệ Thống Chat Thực Tế (Real-time Chat & WebSocket)
- **TC3.1:** **Phòng Chat Gia Đình:** Các tài khoản chung một Family vào chat nội bộ. Kiểm tra tính năng gửi tin nhắn văn bản tức thời qua giao thức Stomp WebSocket.
- **TC3.2:** **Phòng Chat Tư Vấn (Doctor - Patient):** 
  - Gửi tin nhắn Text và Hình Ảnh.
  - Kiểm tra trạng thái "Đang gõ..." (Typing indicator) hiển thị khi đối phương nhập văn bản.
  - Xác minh thông báo tin nhắn đã xem (Read receipts).
- **TC3.3:** **Chat Pagination:** Cuộn ngược lên trên trong một luồng chat đã có lịch sử dài để kiểm tra tính năng tải thêm tin nhắn cũ từ server (Load more/Pagination).
- **TC3.4:** Bác sĩ "Kết thúc tư vấn", xác minh phòng chat tự động bị khóa (chuyển sang trạng thái read-only) ngay lập tức đối với Bệnh nhân và không thể gửi thêm tin nhắn. Bệnh nhân nhận được form đánh giá Bác sĩ.

### Luồng 4: Đặt Lịch Hẹn & Tư Vấn (Booking)
- **TC4.1:** Bệnh nhân A tạo yêu cầu tư vấn Online với Bác sĩ Nhi khoa.
- **TC4.2:** Bác sĩ tiến hành **Chấp nhận (Approve)** yêu cầu trong tab "Chờ duyệt".
- **TC4.3:** Trạng thái booking chuyển sang ACTIVE và phòng chat mới giữa hai bên được cấp tự động.

### Luồng 5: Thông Báo (Push/Socket Notifications)
- **TC5.1:** Bệnh nhân đang ở màn hình Home, Bác sĩ gửi một tin nhắn chat. Bệnh nhân nhận được thông báo đẩy (In-app Notification / System Notification) báo có tin nhắn mới theo thời gian thực.
- **TC5.2:** Thông báo cập nhật trạng thái lịch hẹn: Bác sĩ chấp nhận lịch hẹn, Bệnh nhân nhận được thông báo "Lịch hẹn của bạn đã được chấp nhận".
- **TC5.3:** Cảnh báo tới giờ uống thuốc (nếu mô phỏng thời gian hệ thống): App hiển thị thông báo nhắc nhở uống thuốc trên thanh thông báo của thiết bị.
- **TC5.4:** Tab Notifications Center: Kiểm tra danh sách thông báo hiển thị đầy đủ lịch sử, UI phân biệt được rõ ràng thông báo "chưa đọc" (unread) và "đã đọc" (read). Nhấp vào thông báo tin nhắn sẽ điều hướng thẳng vào đúng phòng chat đó.

### Luồng 6: Cộng Đồng & Quản Trị Nhóm (Community & Admin)
- **TC6.1:** Xin gia nhập nhóm: Bệnh nhân A vào tab Cộng đồng, xin gia nhập nhóm "Mẹ và Bé CareNest".
- **TC6.2:** Admin duyệt yêu cầu. Bệnh nhân nhận được thông báo được duyệt vào nhóm.
- **TC6.3:** Bệnh nhân đăng một bài viết (Post) vào nhóm. QA Moderator duyệt/từ chối bài viết đó.
- **TC6.4:** Chức năng khóa nhóm (Freeze Group): Admin khóa nhóm "Sức khỏe Gia đình". Bệnh nhân truy cập nhóm và thấy trạng thái Read-only (bị chặn đăng bài mới).
