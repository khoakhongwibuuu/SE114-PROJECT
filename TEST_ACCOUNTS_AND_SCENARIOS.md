# Hướng Dẫn Kiểm Thử: Tài Khoản & Kịch Bản QA Seed (CareNest)

Tài liệu này cung cấp hướng dẫn chi tiết về cách thiết lập lại cơ sở dữ liệu local và sử dụng bộ dữ liệu kiểm thử chuẩn (QA Seed Data) phục vụ cho quá trình kiểm thử các tính năng cốt lõi của hệ thống CareNest.

---

## 1. Hướng Dẫn Reset Cơ Sở Dữ Liệu Local

Để đảm bảo tính tất định và dọn sạch dữ liệu cũ trước khi thực hiện các kịch bản kiểm thử, vui lòng thực hiện reset database theo các bước sau:

1. Đảm bảo rằng Docker container `carenest-postgres` đang hoạt động.
2. Di chuyển đến thư mục root của backend (`CareNest/backend`).
3. Chạy file script tự động reset:
   - Trên Windows (Command Prompt hoặc PowerShell):
     ```cmd
     .\reset_db.bat
     ```
   - Hoặc chạy trực tiếp câu lệnh Docker:
     ```bash
     docker exec -i carenest-postgres psql -U carenest_user -d carenest_db -c "DROP SCHEMA IF EXISTS public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO carenest_user; GRANT ALL ON SCHEMA public TO public;"
     ```
4. Khởi động lại ứng dụng Spring Boot backend. Hibernate `ddl-auto: update` sẽ tự động sinh lại toàn bộ 34 bảng cấu trúc trống, sau đó `DatabaseSeeder` sẽ tự động nạp lại toàn bộ dữ liệu QA mẫu chuẩn hóa dưới đây.

---

## 2. Ma Trận Tài Khoản Kiểm Thử (QA Test Accounts)

Dưới đây là danh sách các tài khoản kiểm thử đã được nạp sẵn vào hệ thống với các vai trò và dữ liệu tương ứng:

| Vai trò | Email đăng nhập | Mật khẩu mặc định | Mục đích kiểm thử | Dữ liệu liên kết |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | `admin@gmail.com` | `Password123!` | Quản lý hệ thống toàn cục | Quyền ADMIN |
| **Bác sĩ Nhi Khoa** | `bacsinhikhoa@gmail.com` | `Bacsinhikhoa` | Tư vấn trực tuyến Nhi khoa, duyệt khám | Hồ sơ bác sĩ được xác thực (APPROVED), Chuyên khoa Nhi khoa |
| **Bác sĩ Đa Khoa** | `bacsidakhoa@gmail.com` | `Bacsidakhoa` | Tư vấn sức khỏe tổng quát | Hồ sơ bác sĩ được xác thực (APPROVED), Chuyên khoa Đa khoa |
| **Bệnh nhân 1 (Chính)** | `kiet@gmail.com` | `Kiet13012006` | Test chính: Family switching, Dashboard, Medicine, Vaccine, Booking, Chat | Chủ hộ Gia đình A, Thành viên Gia đình B, Có hồ sơ con phụ thuộc (Bé Na) |
| **Bệnh nhân 2 (Phụ)** | `doletuankiet06@gmail.com` | `Kiet13012006` | Test empty/sparse state, tương tác cộng đồng | Chủ hộ Gia đình B (rỗng) |
| **QA Moderator** | `qa.moderator@gmail.com` | `QaModerator123!` | Kiểm duyệt bài viết cộng đồng, Host nhóm | Host của toàn bộ các group cộng đồng |

---

## 3. Các Kịch Bản Kiểm Thử Chi Tiết (Test Scenarios)

### Kịch bản 1: Chuyển Đổi Gia Đình (Family Switching)
* **Tài khoản sử dụng**: Bệnh nhân 1 (`kiet@gmail.com`)
* **Mục tiêu**: Xác minh tính năng chuyển đổi ngữ cảnh gia đình hoạt động chính xác giữa gia đình có dữ liệu và gia đình trống.
* **Các bước thực hiện**:
  1. Đăng nhập ứng dụng CareNest bằng tài khoản Bệnh nhân 1.
  2. Truy cập chức năng quản lý gia đình. Xác minh tài khoản thuộc về hai gia đình:
     - **Gia đình A** (do Bệnh nhân 1 làm chủ hộ).
     - **Gia đình B** (do Bệnh nhân 2 làm chủ hộ, Bệnh nhân 1 là thành viên).
  3. Chọn **Gia đình A**: Hệ thống phải hiển thị đầy đủ các thông tin của profile con phụ thuộc "Bé Na", các nhiệm vụ hôm nay trên Dashboard.
  4. Thực hiện chuyển đổi sang **Gia đình B**: Hệ thống phải chuyển sang trạng thái trống (empty/sparse state) do Gia đình B chưa có hồ sơ con phụ thuộc và chưa thiết lập lịch uống thuốc/tiêm chủng.

### Kịch bản 2: Dashboard & Quản Lý Sức Khỏe Trẻ Nhỏ
* **Tài khoản sử dụng**: Bệnh nhân 1 (`kiet@gmail.com`)
* **Mục tiêu**: Kiểm tra hiển thị tổng hợp lịch trình uống thuốc, tiêm chủng và lịch khám bệnh trên Dashboard của con phụ thuộc.
* **Các bước thực hiện**:
  1. Chọn ngữ cảnh **Gia đình A** và truy cập màn hình chính (Dashboard).
  2. Xác minh các nhiệm vụ hôm nay hiển thị đúng và đầy đủ:
     - **Lịch uống thuốc**: Hiển thị thuốc **Siro ho Prospan** của **Bé Na** với 2 khung giờ cần uống (`08:00` và `20:00`) ở trạng thái PENDING.
     - **Lịch hẹn khám**: Hiển thị lịch khám với **Bác sĩ Nguyễn Văn An** tại **Bệnh viện Nhi Trung ương** vào lúc `14:00 hôm nay` (trạng thái SCHEDULED).
     - **Nhắc nhở tiêm chủng**: Hiển thị vắc-xin sắp tới **Vắc-xin 6 trong 1 (Mũi 3)** scheduled vào ngày mai với nhãn "Ngăn mai" (trạng thái PENDING).
  3. Vào chi tiết Sổ tiêm chủng của Bé Na:
     - Xác minh hiển thị đúng 2 mũi tiêm trước đó đã hoàn thành (Mũi 1 từ 60 ngày trước, Mũi 2 từ 30 ngày trước, trạng thái COMPLETED).
     - Xác minh hiển thị đúng Mũi 3 sắp tới (trạng thái PENDING).

### Kịch bản 3: Kiểm Duyệt Cộng Đồng & Trạng Thái Bài Viết
* **Tài khoản sử dụng**: Bệnh nhân 1 (`kiet@gmail.com`), Bệnh nhân 2 (`doletuankiet06@gmail.com`), QA Moderator (`qa.moderator@gmail.com`)
* **Mục tiêu**: Kiểm thử luồng kiểm duyệt bài viết cộng đồng và hiển thị trung thực trạng thái bài viết của tôi (My Posts).
* **Các bước thực hiện**:
  1. Đăng nhập tài khoản Bệnh nhân 1 (`kiet@gmail.com`):
     - Truy cập tab "Hội nhóm" (được hiển thị trung thực là **Nhóm của tôi** - Joined groups). Xác minh tài khoản đã tham gia 2 nhóm: *Hội Mẹ Bé CareNest* và *Chia sẻ kinh nghiệm Nhi khoa*.
     - Xem bài viết đã phê duyệt: Bài viết *"Hỏi về lịch tiêm chủng cho bé 6 tháng tuổi"* ở trạng thái `APPROVED` (có 1 lượt thích từ Bệnh nhân 2 và 1 bình luận tư vấn từ Bác sĩ Nhi Khoa).
     - Xem danh sách bài viết của tôi (My Posts): Xác minh bài viết *"Bé bị sốt nhẹ sau khi tiêm phòng phải làm sao?"* hiển thị chính xác ở trạng thái **Đang chờ duyệt** (`PENDING_APPROVAL`).
  2. Đăng nhập tài khoản Bệnh nhân 2 (`doletuankiet06@gmail.com`):
     - Xem danh sách bài viết của tôi: Xác minh bài viết *"Thần dược tăng chiều cao cho bé xách tay giá rẻ"* hiển thị ở trạng thái **Bị từ chối** (`REJECTED`) kèm lý do từ chối rõ ràng: *"Nội dung mang tính chất quảng cáo thương mại, spam, vi phạm quy tắc hội nhóm."*
  3. Đăng nhập tài khoản QA Moderator (`qa.moderator@gmail.com`):
     - Truy cập giao diện quản trị Admin/Moderator. Xác minh có quyền xem danh sách bài viết chờ duyệt và thực hiện phê duyệt/từ chối trực quan mà không cần truy vấn database thủ công.

### Kịch bản 4: Đặt Lịch Tư Vấn, Phòng Chat & Đồng Bộ Lịch Hẹn
* **Tài khoản sử dụng**: Bệnh nhân 1 (`kiet@gmail.com`), Bệnh nhân 2 (`doletuankiet06@gmail.com`), Bác sĩ Nhi Khoa (`bacsinhikhoa@gmail.com`)
* **Mục tiêu**: Xác minh toàn bộ 7 trạng thái của Booking request, hoạt động của CTA phòng tư vấn tương ứng với từng trạng thái và cơ chế đồng bộ lịch hẹn y tế (Appointment Ledger).
* **Các bước thực hiện**:
  1. Đăng nhập Bệnh nhân 1 (`kiet@gmail.com`):
     - Truy cập lịch sử đặt lịch. Xác minh danh sách booking chứa đầy đủ các trạng thái được seed:
       - **ACTIVE** (Đặt tư vấn ho khan với Bác sĩ Nhi Khoa): Nút CTA hiển thị **"Vào phòng tư vấn riêng tư"** (Phòng chat hoạt động).
       - **APPROVED** (Đặt tư vấn tiêm chủng với Bác sĩ Nhi Khoa): Nút CTA hiển thị **"Vào phòng tư vấn riêng tư"**.
       - **COMPLETED** (Đặt tư vấn trào ngược dạ dày với Bác sĩ Đa Khoa): Nút CTA hiển thị **"Xem lịch sử tư vấn"** (Khóa chat, chỉ cho xem lịch sử).
       - **REJECTED** (Từ chối tư vấn sốt phát ban): Không hiển thị nút CTA vào phòng chat. Hiển thị lý do từ chối: *"Lịch làm việc của bác sĩ đã kín..."*.
       - **CANCELLED** (Hủy khám viêm phế quản): Không hiển thị nút CTA phòng chat. Hiển thị lý do hủy: *"Bé đã đỡ và được khám tại cơ sở y tế gần nhà."*.
       - **APPROVED (OFFLINE_CLINIC)** (Khám trực tiếp tại phòng khám): Xác minh thông tin địa điểm phòng khám và ghi chú chuẩn bị hiển thị rõ ràng. Không hiển thị nút CTA phòng chat (do là khám offline).
     - Bấm **"Vào phòng tư vấn riêng tư"** của Booking ACTIVE: Xác minh lịch sử tin nhắn mẫu giữa Bệnh nhân và Bác sĩ hiển thị đầy đủ, mượt mà (message history).
     - Bấm **"Xem lịch sử tư vấn"** của Booking COMPLETED: Xác minh lịch sử tin nhắn cũ hiển thị đầy đủ nhưng ô nhập tin nhắn bị khóa (không cho gửi thêm tin nhắn).
     - Truy cập Lịch khám bệnh: Xác minh lịch khám có 1 mục đã hoàn thành tự động đồng bộ từ Booking COMPLETED với đầy đủ ghi chú kết quả khám: *"Trào ngược dạ dày nhẹ. Thực hiện điều chỉnh chế độ ăn uống sinh hoạt."*
  2. Đăng nhập Bệnh nhân 2 (`doletuankiet06@gmail.com`):
     - Xác minh booking trạng thái **RESTRICTED** (Tư vấn dị ứng hải sản với Bác sĩ Nhi Khoa): Nút CTA hiển thị **"Xem lịch sử tư vấn"** (Người dùng bị hạn chế chat, chỉ xem lịch sử tin nhắn cũ).
     - Xác minh booking trạng thái **PENDING** (Tư vấn dinh dưỡng với Bác sĩ Đa Khoa): Đang chờ bác sĩ duyệt, không hiển thị nút CTA vào phòng chat.
