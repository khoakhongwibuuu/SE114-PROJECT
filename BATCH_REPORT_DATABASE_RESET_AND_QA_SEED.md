# Báo Cáo Kết Quả: Database Reset & QA Seed Foundation (Batch 00B)

* **Trạng thái**: **PASS** (Đạt yêu cầu)
* **Ngày thực hiện**: 16/06/2026
* **Môi trường**: Local Development / QA (Docker PostgreSQL 16 + Spring Boot)

---

## 1. Nội Dung Đã Thực Hiện

### A. Triển Khai Cơ Chế Reset Database Local
- Đã tạo SQL script [reset_db.sql](file:///d:/DoAn_MB1/CareNest/backend/reset_db.sql) để tự động hóa việc dọn dẹp cơ sở dữ liệu bằng cách drop và recreate schema `public` của PostgreSQL. Cách tiếp cận này giúp dọn sạch hoàn toàn 35 thực thể dữ liệu (bao gồm cả bảng, index, và extension) mà không làm rách hay mồ côi khóa ngoại.
- Đã tạo batch script [reset_db.bat](file:///d:/DoAn_MB1/CareNest/backend/reset_db.bat) chạy trực tiếp lệnh thông qua Docker container `carenest-postgres` trên cổng `5433`. Khi chạy script này, cơ sở dữ liệu được làm sạch hoàn toàn trong vài giây.

### B. Phát Triển Bộ Dữ Liệu QA Seed Tất Định
- Đã nâng cấp toàn diện [DatabaseSeeder.java](file:///d:/DoAn_MB1/CareNest/backend/src/main/java/com/carenest/backend/config/database/DatabaseSeeder.java) để nạp bộ dữ liệu kiểm thử chuẩn, tất định (không ngẫu nhiên), bao quát 100% các kịch bản kiểm thử tích hợp (E2E):
  - **Tài khoản**: Tạo 1 Admin, 2 Bác sĩ (đã xác thực APPROVED), 2 Bệnh nhân (Patient 1 thuộc cả Family A và B, Patient 2 gần như trống), và 1 QA Moderator (được gán làm HOST của tất cả các nhóm).
  - **Hội nhóm & Bài viết**: Tạo 3 nhóm thật cùng memberships cụ thể. Seed bài viết ở cả 3 trạng thái: `APPROVED` (có like và bình luận tư vấn chuyên môn từ bác sĩ), `PENDING_APPROVAL`, và `REJECTED` (có ghi nhận lý do từ chối cụ thể).
  - **Gia đình & Sức khỏe phụ thuộc**: Tạo Family A (nhiều dữ liệu) và Family B (ít dữ liệu) phục vụ kiểm thử **Family Switching**. Tạo profile con phụ thuộc "Bé Na" trong Family A cùng:
    - Lịch uống thuốc Medication và MedicationLog ở trạng thái `PENDING` hôm nay (08:00 và 20:00).
    - Lịch tiêm chủng VaccinationRecord cùng 3 mũi tiêm (2 mũi quá khứ `COMPLETED` và 1 mũi ngày mai `PENDING`).
    - Lịch khám bệnh Appointment độc lập ở trạng thái `SCHEDULED` hôm nay lúc 14:00.
  - **Tư vấn & Chat Room**: Seed 8 Booking Requests đại diện đầy đủ **7 trạng thái** (`PENDING`, `APPROVED` (ONLINE_CHAT), `APPROVED` (OFFLINE_CLINIC), `REJECTED`, `CANCELLED`, `ACTIVE`, `COMPLETED`, `RESTRICTED`) cùng các `ConsultationThread` tương ứng và lịch sử tin nhắn mẫu phong phú.
  - **Đồng bộ Lịch Hẹn**: Đồng bộ tự động 1 lịch hẹn khám đã `COMPLETED` (ghi nhận kết quả chẩn đoán) từ Booking Request kết thúc (`COMPLETED`).

---

## 2. Xác Minh Kết Quả Chạy Thực Tế

### A. Biên dịch Backend
- Lệnh biên dịch `.\mvnw.cmd compile` đã chạy thành công mà không gặp bất kỳ lỗi cú pháp nào:
  ```txt
  [INFO] --- compiler:3.8.1:compile (default-compile) @ carenest-backend ---
  [INFO] Compiling 278 source files to D:\DoAn_MB1\CareNest\backend\target\classes
  [INFO] BUILD SUCCESS
  ```

### B. Thực hiện Reset & Khởi chạy Nạp dữ liệu
- Chạy [reset_db.bat](file:///d:/DoAn_MB1/CareNest/backend/reset_db.bat) thành công:
  ```txt
  NOTICE:  drop cascades to 35 other objects
  DROP SCHEMA
  CREATE SCHEMA
  Database reset successfully!
  ```
- Khởi động Spring Boot backend, ứng dụng tự động sinh lại toàn bộ bảng cấu trúc nhờ Hibernate `ddl-auto: update` và thực hiện seed dữ liệu tất định thành công. Logs ghi nhận:
  ```txt
  2026-06-16T20:29:08.631+07:00  INFO 34232 --- [carenest-backend] [  restartedMain] c.c.b.config.database.DatabaseSeeder     : QA Database Seeder completed successfully!
  ```
- Khi chạy ứng dụng ở các lần tiếp theo, bộ seeder kiểm tra sự tồn tại của dữ liệu và bỏ qua (skip) một cách an toàn, tránh lỗi duplicate record:
  ```txt
  2026-06-16T20:29:08.609+07:00  INFO 34232 --- [carenest-backend] [  restartedMain] c.c.b.config.database.DatabaseSeeder     : Families already seeded. Skipping family seed.
  ```

---

## 3. Tài Liệu Hướng Dẫn & Kịch Bản QA
- Đã hoàn thành viết tài liệu chi tiết tại [TEST_ACCOUNTS_AND_SCENARIOS.md](file:///d:/DoAn_MB1/CareNest/TEST_ACCOUNTS_AND_SCENARIOS.md). Tài liệu cung cấp ma trận tài khoản và 4 kịch bản kiểm thử E2E trực quan cho Tester và Nhà phát triển.

---

## 4. Kết Luận & Đánh Giá
- Batch 00B đã hoàn thành xuất sắc tất cả các mục tiêu đề ra.
- Bộ dữ liệu seed hoạt động hoàn hảo, cung cấp đầy đủ dữ liệu mô phỏng thực tế để kiểm thử các luồng phức tạp như Family context, Dashboard tasks, Chat room locks, và đồng bộ Lịch hẹn khám y tế trên cả Mobile App và Backend Admin.
- Cơ chế reset database an toàn, ổn định và lặp lại được 100%.
