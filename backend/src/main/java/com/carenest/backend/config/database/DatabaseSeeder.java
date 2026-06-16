package com.carenest.backend.config.database;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.enums.Gender;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.doctorverification.entity.DoctorVerification;
import com.carenest.backend.features.doctorverification.enums.VerificationStatus;
import com.carenest.backend.features.doctorverification.repository.DoctorVerificationRepository;
import com.carenest.backend.features.family.entity.Family;
import com.carenest.backend.features.family.entity.FamilyMember;
import com.carenest.backend.features.family.enums.FamilyRole;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.carenest.backend.features.family.repository.FamilyRepository;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import com.carenest.backend.features.healthprofile.enums.BloodType;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.features.medication.entity.Medication;
import com.carenest.backend.features.medication.entity.MedicationLog;
import com.carenest.backend.features.medication.enums.MedicationFrequency;
import com.carenest.backend.features.medication.enums.MedicationStatus;
import com.carenest.backend.features.medication.enums.MedicationLogStatus;
import com.carenest.backend.features.medication.repository.MedicationLogRepository;
import com.carenest.backend.features.medication.repository.MedicationRepository;
import com.carenest.backend.features.vaccination.entity.VaccinationRecord;
import com.carenest.backend.features.vaccination.entity.VaccinationDose;
import com.carenest.backend.features.vaccination.enums.DoseStatus;
import com.carenest.backend.features.vaccination.repository.VaccinationRecordRepository;
import com.carenest.backend.features.vaccination.repository.VaccinationDoseRepository;
import com.carenest.backend.features.community.entity.ChatGroup;
import com.carenest.backend.features.community.entity.UserGroupMembership;
import com.carenest.backend.features.community.entity.GroupPost;
import com.carenest.backend.features.community.entity.GroupPostComment;
import com.carenest.backend.features.community.entity.GroupPostLike;
import com.carenest.backend.features.community.enums.GroupRole;
import com.carenest.backend.features.community.enums.PostStatus;
import com.carenest.backend.features.community.repository.ChatGroupRepository;
import com.carenest.backend.features.community.repository.UserGroupMembershipRepository;
import com.carenest.backend.features.community.repository.GroupPostRepository;
import com.carenest.backend.features.community.repository.GroupPostCommentRepository;
import com.carenest.backend.features.community.repository.GroupPostLikeRepository;
import com.carenest.backend.features.booking.entity.BookingRequest;
import com.carenest.backend.features.booking.entity.ConsultationThread;
import com.carenest.backend.features.booking.entity.ConsultationMessage;
import com.carenest.backend.features.booking.enums.BookingRequestType;
import com.carenest.backend.features.booking.enums.BookingStatus;
import com.carenest.backend.features.booking.repository.BookingRequestRepository;
import com.carenest.backend.features.booking.repository.ConsultationThreadRepository;
import com.carenest.backend.features.booking.repository.ConsultationMessageRepository;
import com.carenest.backend.features.appointment.entity.Appointment;
import com.carenest.backend.features.appointment.enums.AppointmentStatus;
import com.carenest.backend.features.appointment.repository.AppointmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private static final String QA_MODERATOR_EMAIL = "qa.moderator@gmail.com";
    private static final String QA_MODERATOR_PASSWORD = "QaModerator123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChatGroupRepository chatGroupRepository;
    private final UserGroupMembershipRepository membershipRepository;
    private final DoctorVerificationRepository verificationRepository;
    
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final MedicationRepository medicationRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final VaccinationDoseRepository vaccinationDoseRepository;
    private final GroupPostRepository groupPostRepository;
    private final GroupPostCommentRepository groupPostCommentRepository;
    private final GroupPostLikeRepository groupPostLikeRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final ConsultationThreadRepository consultationThreadRepository;
    private final ConsultationMessageRepository consultationMessageRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting QA Database Seeder...");

        // 1. Seed Users
        User admin = seedUser("admin@gmail.com", "Password123!", "Admin", Role.ADMIN);
        User patient1 = seedUser("kiet@gmail.com", "Kiet13012006", "Kiet Tuan", Role.USER);
        User patient2 = seedUser("doletuankiet06@gmail.com", "Kiet13012006", "Tuan Kiet", Role.USER);
        User doctor1 = seedUser("bacsinhikhoa@gmail.com", "Bacsinhikhoa", "Bac si Nhi Khoa", Role.DOCTOR);
        User doctor2 = seedUser("bacsidakhoa@gmail.com", "Bacsidakhoa", "Bac si Da Khoa", Role.DOCTOR);
        User qaModerator = seedUser(QA_MODERATOR_EMAIL, QA_MODERATOR_PASSWORD, "QA Moderator", Role.USER);

        // 2. Seed Groups
        seedGroupsAndMemberships(patient1, patient2, qaModerator);

        // 3. Seed Families and Health Profiles
        seedFamiliesAndProfiles(patient1, patient2);

        // 4. Seed Booking and Consultation
        seedBookingAndConsultations(patient1, patient2, doctor1, doctor2);

        log.info("QA Database Seeder completed successfully!");
    }

    private User seedUser(String email, String password, String fullName, Role role) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPasswordHash(passwordEncoder.encode(password));
            newUser.setFullName(fullName);
            newUser.setRole(role);
            newUser.setIsActive(true);
            newUser.setIsVerified(true);
            User saved = userRepository.save(newUser);
            log.info("Seeded account: {} ({})", email, role.name());
            return saved;
        });

        if (role == Role.DOCTOR && verificationRepository.findByUserId(user.getId()).isEmpty()) {
            DoctorVerification verification = new DoctorVerification();
            verification.setUser(user);
            verification.setSpecialty(email.equals("bacsinhikhoa@gmail.com") ? "Nhi khoa" : "Đa khoa");
            verification.setHospitalName("Bệnh viện CareNest");
            verification.setCertificationNumber(email.equals("bacsinhikhoa@gmail.com") ? "VNC-123456" : "VNC-789012");
            verification.setDocumentUrl("https://example.com/document.jpg");
            verification.setStatus(VerificationStatus.APPROVED);
            verificationRepository.save(verification);
            log.info("Seeded verification for doctor: {}", email);
        }

        return user;
    }

    private void seedGroupsAndMemberships(User patient1, User patient2, User qaModerator) {
        if (chatGroupRepository.count() > 0) {
            log.info("Groups already seeded. Skipping group seed.");
            ensureQaModeratorHostsAllGroups(qaModerator);
            return;
        }

        ChatGroup group1 = chatGroupRepository.save(ChatGroup.builder()
                .name("Hội Mẹ Bé CareNest")
                .description("Hội nhóm chia sẻ kinh nghiệm nuôi dạy và chăm sóc trẻ sơ sinh, dinh dưỡng cho mẹ và bé.")
                .category("Mẹ & Bé")
                .tags("sơ sinh,dinh dưỡng,chăm con")
                .isPrivate(false)
                .build());

        ChatGroup group2 = chatGroupRepository.save(ChatGroup.builder()
                .name("Chia sẻ kinh nghiệm Nhi khoa")
                .description("Hỏi đáp các bệnh thường gặp ở trẻ nhỏ cùng chuyên gia và bác sĩ Nhi khoa.")
                .category("Nhi khoa")
                .tags("nhi khoa,sốt,ho,tiêu hóa")
                .isPrivate(false)
                .build());

        ChatGroup group3 = chatGroupRepository.save(ChatGroup.builder()
                .name("Hỏi đáp Sức khỏe Gia đình")
                .description("Cộng đồng tư vấn sức khỏe tổng quát cho mọi thành viên trong gia đình.")
                .category("Sức khỏe chung")
                .tags("sức khỏe,gia đình,tư vấn")
                .isPrivate(false)
                .build());

        log.info("Seeded 3 community groups.");

        // Add QA Moderator as HOST
        membershipRepository.save(UserGroupMembership.builder().user(qaModerator).group(group1).groupRole(GroupRole.HOST).build());
        membershipRepository.save(UserGroupMembership.builder().user(qaModerator).group(group2).groupRole(GroupRole.HOST).build());
        membershipRepository.save(UserGroupMembership.builder().user(qaModerator).group(group3).groupRole(GroupRole.HOST).build());

        // Add Patient 1
        membershipRepository.save(UserGroupMembership.builder().user(patient1).group(group1).groupRole(GroupRole.MEMBER).build());
        membershipRepository.save(UserGroupMembership.builder().user(patient1).group(group2).groupRole(GroupRole.MEMBER).build());

        // Add Patient 2
        membershipRepository.save(UserGroupMembership.builder().user(patient2).group(group1).groupRole(GroupRole.MEMBER).build());

        log.info("Seeded group memberships.");

        // Seed Posts
        // Post 1 (APPROVED)
        GroupPost post1 = groupPostRepository.save(GroupPost.builder()
                .chatGroup(group1)
                .author(patient1)
                .title("Hỏi về lịch tiêm chủng cho bé 6 tháng tuổi")
                .content("Bé nhà em được 6 tháng tuổi thì cần tiêm những mũi vắc-xin gì ạ? Em thấy có gói tiêm vắc-xin 6 trong 1 rất tiện lợi nhưng phân vân không biết có nên chọn không. Mong mọi người tư vấn giúp.")
                .tags("tiêm chủng,vắc xin")
                .status(PostStatus.APPROVED)
                .build());

        // Like for Post 1
        groupPostLikeRepository.save(GroupPostLike.builder().groupPost(post1).user(patient2).build());

        // Comment for Post 1
        User doctor1 = userRepository.findByEmail("bacsinhikhoa@gmail.com").orElse(null);
        if (doctor1 != null) {
            groupPostCommentRepository.save(GroupPostComment.builder()
                    .groupPost(post1)
                    .author(doctor1)
                    .content("Chào bạn, bé 6 tháng tuổi cần tiêm mũi vắc-xin 6 trong 1 nhắc lại và uống vắc-xin phòng tiêu chảy cấp do Rotavirus nhé. Bạn nên đưa bé đến trung tâm tiêm chủng gần nhất để bác sĩ khám và tư vấn chi tiết.")
                    .build());
        }

        // Post 2 (PENDING_APPROVAL)
        groupPostRepository.save(GroupPost.builder()
                .chatGroup(group1)
                .author(patient1)
                .title("Bé bị sốt nhẹ sau khi tiêm phòng phải làm sao?")
                .content("Bé nhà em mới tiêm phòng mũi 6 trong 1 về hôm qua, hôm nay hơi âm ấm sốt khoảng 38 độ C. Bé vẫn ăn ngủ bình thường thì em có cần cho bé uống thuốc hạ sốt không ạ? Hay chỉ cần chườm ấm thưa các mẹ?")
                .tags("sốt,sau tiêm phòng")
                .status(PostStatus.PENDING_APPROVAL)
                .build());

        // Post 3 (REJECTED)
        groupPostRepository.save(GroupPost.builder()
                .chatGroup(group1)
                .author(patient2)
                .title("Thần dược tăng chiều cao cho bé xách tay giá rẻ")
                .content("Em có bán sữa ngoại nhập khẩu nguyên lon và thuốc bổ tăng chiều cao vượt trội cho bé cam kết hiệu quả sau 2 tuần sử dụng. Mẹ nào quan tâm ib em nhé, giá hạt dẻ giao hàng toàn quốc!")
                .tags("quảng cáo,sữa")
                .status(PostStatus.REJECTED)
                .rejectionReason("Nội dung mang tính chất quảng cáo thương mại, spam, vi phạm quy tắc hội nhóm.")
                .reviewer(qaModerator)
                .build());

        log.info("Seeded group posts, likes, and comments.");
    }

    private void ensureQaModeratorHostsAllGroups(User qaModerator) {
        int assignments = 0;
        for (var group : chatGroupRepository.findAllByOrderByNameAsc()) {
            var existingMembership = membershipRepository.findByGroupIdAndUserId(group.getId(), qaModerator.getId());
            if (existingMembership.isPresent()) {
                UserGroupMembership membership = existingMembership.get();
                if (membership.getGroupRole() != GroupRole.HOST) {
                    membership.setGroupRole(GroupRole.HOST);
                    membershipRepository.save(membership);
                    assignments++;
                }
                continue;
            }
            membershipRepository.save(UserGroupMembership.builder()
                    .user(qaModerator)
                    .group(group)
                    .groupRole(GroupRole.HOST)
                    .build());
            assignments++;
        }
        log.info("QA moderator updated: HOST on {} groups", assignments);
    }

    private void seedFamiliesAndProfiles(User patient1, User patient2) {
        if (familyRepository.count() > 0) {
            log.info("Families already seeded. Skipping family seed.");
            return;
        }

        // Family A (Owned by Patient 1)
        Family familyA = familyRepository.save(Family.builder()
                .name("Gia đình A")
                .owner(patient1)
                .joinCode("FAMA1234")
                .joinCodeExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build());

        familyMemberRepository.save(FamilyMember.builder()
                .family(familyA)
                .user(patient1)
                .role(FamilyRole.OWNER)
                .build());

        // Family B (Owned by Patient 2)
        Family familyB = familyRepository.save(Family.builder()
                .name("Gia đình B")
                .owner(patient2)
                .joinCode("FAMB5678")
                .joinCodeExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build());

        familyMemberRepository.save(FamilyMember.builder()
                .family(familyB)
                .user(patient2)
                .role(FamilyRole.OWNER)
                .build());

        // Add Patient 1 to Family B as MEMBER
        familyMemberRepository.save(FamilyMember.builder()
                .family(familyB)
                .user(patient1)
                .role(FamilyRole.MEMBER)
                .build());

        log.info("Seeded 2 families and memberships.");

        // Create HealthProfile for Patient 1 in Family A
        HealthProfile profileP1 = healthProfileRepository.save(HealthProfile.builder()
                .user(patient1)
                .family(familyA)
                .fullName(patient1.getFullName())
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .gender(Gender.MALE)
                .relationship("Bản thân")
                .bloodType(BloodType.A_POSITIVE)
                .allergies("Không có")
                .chronicDiseases("Không")
                .notes("Tài khoản chính")
                .isChild(false)
                .build());

        // Create HealthProfile for Patient 2 in Family B
        healthProfileRepository.save(HealthProfile.builder()
                .user(patient2)
                .family(familyB)
                .fullName(patient2.getFullName())
                .dateOfBirth(LocalDate.of(2006, 1, 13))
                .gender(Gender.MALE)
                .relationship("Bản thân")
                .bloodType(BloodType.B_POSITIVE)
                .allergies("Dị ứng phấn hoa")
                .chronicDiseases("Không")
                .notes("Tài khoản phụ")
                .isChild(false)
                .build());

        // Create Child Profile Bé Na in Family A
        HealthProfile babyNa = healthProfileRepository.save(HealthProfile.builder()
                .user(patient1)
                .family(familyA)
                .fullName("Bé Na")
                .dateOfBirth(LocalDate.now().minusYears(2)) // 2 years old
                .gender(Gender.FEMALE)
                .relationship("Con gái")
                .bloodType(BloodType.O_POSITIVE)
                .allergies("Dị ứng hải sản nhẹ")
                .chronicDiseases("Không")
                .notes("Cần theo dõi sát lịch tiêm phòng và uống sữa ấm trước khi đi ngủ.")
                .isChild(true)
                .build());

        log.info("Seeded health profiles (including dependent child profile Bé Na).");

        // Seed Medication for Bé Na
        Medication medication = medicationRepository.save(Medication.builder()
                .healthProfile(babyNa)
                .medicineName("Siro ho Prospan")
                .dosage("5ml mỗi lần")
                .frequency(MedicationFrequency.TWICE_DAILY)
                .timesPerDay(2)
                .timeSlots("08:00, 20:00")
                .startDate(LocalDate.now().minusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .status(MedicationStatus.ACTIVE)
                .notes("Uống sau khi ăn no. Lắc đều trước khi sử dụng.")
                .build());

        // Seed MedicationLogs for today
        LocalDate today = LocalDate.now();
        ZoneId zoneId = ZoneId.systemDefault();
        Instant time8AM = today.atTime(8, 0).atZone(zoneId).toInstant();
        Instant time8PM = today.atTime(20, 0).atZone(zoneId).toInstant();

        medicationLogRepository.save(MedicationLog.builder()
                .medication(medication)
                .scheduledTime(time8AM)
                .status(MedicationLogStatus.PENDING)
                .build());

        medicationLogRepository.save(MedicationLog.builder()
                .medication(medication)
                .scheduledTime(time8PM)
                .status(MedicationLogStatus.PENDING)
                .build());

        log.info("Seeded medication and medication logs for Bé Na.");

        // Seed Vaccination for Bé Na
        VaccinationRecord vacRecord = vaccinationRecordRepository.save(VaccinationRecord.builder()
                .healthProfile(babyNa)
                .vaccineName("Vắc-xin 6 trong 1 (Infanrix Hexa)")
                .totalDoses(3)
                .doseIntervalDays(30)
                .notes("Ngừa 6 bệnh: Bạch hầu, Ho gà, Uốn ván, Bại liệt, Viêm gan B và Hib.")
                .build());

        // Dose 1 (Completed 60 days ago)
        vaccinationDoseRepository.save(VaccinationDose.builder()
                .vaccinationRecord(vacRecord)
                .doseNumber(1)
                .scheduledDate(LocalDate.now().minusDays(60))
                .dateAdministered(LocalDate.now().minusDays(60))
                .location("Trung tâm tiêm chủng VNVC")
                .administeredBy("Điều dưỡng Nguyễn Thị Mai")
                .status(DoseStatus.COMPLETED)
                .notes("Bé khỏe mạnh sau tiêm, không sốt.")
                .build());

        // Dose 2 (Completed 30 days ago)
        vaccinationDoseRepository.save(VaccinationDose.builder()
                .vaccinationRecord(vacRecord)
                .doseNumber(2)
                .scheduledDate(LocalDate.now().minusDays(30))
                .dateAdministered(LocalDate.now().minusDays(30))
                .location("Trung tâm tiêm chủng VNVC")
                .administeredBy("Điều dưỡng Trần Văn Hùng")
                .status(DoseStatus.COMPLETED)
                .notes("Hơi sưng nhẹ tại vết tiêm, chườm mát tự khỏi.")
                .build());

        // Dose 3 (Pending, scheduled for tomorrow)
        vaccinationDoseRepository.save(VaccinationDose.builder()
                .vaccinationRecord(vacRecord)
                .doseNumber(3)
                .scheduledDate(LocalDate.now().plusDays(1))
                .status(DoseStatus.PENDING)
                .notes("Chuẩn bị sức khỏe tốt cho bé trước ngày tiêm.")
                .build());

        log.info("Seeded vaccination record and doses for Bé Na.");

        // Seed Appointment for Bé Na (Independent, manual creation)
        appointmentRepository.save(Appointment.builder()
                .healthProfile(babyNa)
                .doctorName("Bác sĩ Nguyễn Văn An")
                .hospitalName("Bệnh viện Nhi Trung ương")
                .address("18/879 La Thành, Đống Đa, Hà Nội")
                .appointmentDate(today.atTime(14, 0).atZone(zoneId).toInstant()) // 14:00 today
                .status(AppointmentStatus.SCHEDULED)
                .notes("Khám sức khỏe tổng quát và nội soi tai mũi họng.")
                .build());

        log.info("Seeded manual independent appointment for Bé Na.");
    }

    private void seedBookingAndConsultations(User patient1, User patient2, User doctor1, User doctor2) {
        if (bookingRequestRepository.count() > 0) {
            log.info("Booking requests already seeded. Skipping booking seed.");
            return;
        }

        // Get patient profiles
        HealthProfile profileP1 = healthProfileRepository.findAll().stream()
                .filter(hp -> hp.getUser().getId().equals(patient1.getId()) && !hp.getIsChild())
                .findFirst().orElse(null);

        HealthProfile profileP2 = healthProfileRepository.findAll().stream()
                .filter(hp -> hp.getUser().getId().equals(patient2.getId()) && !hp.getIsChild())
                .findFirst().orElse(null);

        // We create up to 4 consultation threads due to unique index (patient_id, doctor_id)
        
        // Thread 1: Patient 1 & Doctor 1 (Active Chat Room)
        ConsultationThread thread1 = consultationThreadRepository.save(ConsultationThread.builder()
                .patient(patient1)
                .doctor(doctor1)
                .build());

        // Messages for Thread 1
        consultationMessageRepository.save(ConsultationMessage.builder().thread(thread1).sender(patient1).content("Chào bác sĩ, bé nhà em dạo này ho nhiều về đêm và sáng sớm, có đờm khò khè ạ.").build());
        consultationMessageRepository.save(ConsultationMessage.builder().thread(thread1).sender(doctor1).content("Chào bạn, bé có biểu hiện sốt, khó thở hay bỏ bú bỏ ăn không? Bạn đo nhiệt độ bé chưa?").build());
        consultationMessageRepository.save(ConsultationMessage.builder().thread(thread1).sender(patient1).content("Dạ bé không sốt, nhiệt độ bình thường 36.8 độ C, bé vẫn chơi ngoan nhưng cứ nằm xuống là ho.").build());
        consultationMessageRepository.save(ConsultationMessage.builder().thread(thread1).sender(doctor1).content("Đó có thể là biểu hiện của kích ứng hô hấp do thời tiết lạnh hoặc khô. Bạn giữ ấm cổ họng cho bé, nhỏ nước muối sinh lý làm sạch mũi, và cho bé uống nước ấm nhé. Theo dõi thêm 2 ngày, nếu bé ho tăng kèm sốt thì đưa đi khám ngay.").build());

        // Booking 1 (ACTIVE) -> Thread 1
        bookingRequestRepository.save(BookingRequest.builder()
                .patient(patient1)
                .doctor(doctor1)
                .healthProfile(profileP1)
                .requestType(BookingRequestType.ONLINE_CHAT)
                .status(BookingStatus.ACTIVE)
                .note("Tư vấn bé bị ho khan khò khè đêm")
                .scheduledAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .thread(thread1)
                .build());

        // Thread 2: Patient 1 & Doctor 2 (Completed Consultation Room -> history viewable)
        ConsultationThread thread2 = consultationThreadRepository.save(ConsultationThread.builder()
                .patient(patient1)
                .doctor(doctor2)
                .build());

        // Messages for Thread 2
        consultationMessageRepository.save(ConsultationMessage.builder().thread(thread2).sender(patient1).content("Chào bác sĩ, tôi muốn hỏi về chế độ ăn uống cho người bị trào ngược dạ dày.").build());
        consultationMessageRepository.save(ConsultationMessage.builder().thread(thread2).sender(doctor2).content("Chào bạn, người trào ngược cần tránh đồ chua cay, đồ uống có gas, cồn. Không ăn quá no và không nằm ngay sau khi ăn.").build());
        consultationMessageRepository.save(ConsultationMessage.builder().thread(thread2).sender(patient1).content("Vâng tôi đã rõ, xin cảm ơn bác sĩ.").build());

        // Booking 2 (COMPLETED) -> Thread 2. It has a linked appointment.
        Appointment appointmentSync = appointmentRepository.save(Appointment.builder()
                .healthProfile(profileP1)
                .doctorName(doctor2.getFullName())
                .hospitalName("Bệnh viện CareNest")
                .address("Tư vấn trực tuyến qua CareNest App")
                .appointmentDate(Instant.now().minus(5, ChronoUnit.DAYS))
                .status(AppointmentStatus.COMPLETED)
                .notes("Tư vấn trào ngược dạ dày trực tuyến.")
                .resultNotes("Trào ngược dạ dày nhẹ. Thực hiện điều chỉnh chế độ ăn uống sinh hoạt.")
                .build());

        bookingRequestRepository.save(BookingRequest.builder()
                .patient(patient1)
                .doctor(doctor2)
                .healthProfile(profileP1)
                .requestType(BookingRequestType.ONLINE_CHAT)
                .status(BookingStatus.COMPLETED)
                .note("Tư vấn trào ngược dạ dày")
                .scheduledAt(Instant.now().minus(5, ChronoUnit.DAYS))
                .thread(thread2)
                .appointment(appointmentSync)
                .build());

        // Thread 3: Patient 2 & Doctor 1 (Restricted Consultation Room -> history viewable but chat input locked)
        ConsultationThread thread3 = consultationThreadRepository.save(ConsultationThread.builder()
                .patient(patient2)
                .doctor(doctor1)
                .build());

        // Messages for Thread 3
        consultationMessageRepository.save(ConsultationMessage.builder().thread(thread3).sender(patient2).content("Chào bác sĩ Nhi khoa, bé nhà tôi ăn hải sản xong bị mẩn ngứa quanh miệng.").build());
        consultationMessageRepository.save(ConsultationMessage.builder().thread(thread3).sender(doctor1).content("Chào bạn, bé có bị sưng húp môi mắt, nổi mề đay toàn thân hay khó thở không?").build());

        // Booking 3 (RESTRICTED) -> Thread 3
        bookingRequestRepository.save(BookingRequest.builder()
                .patient(patient2)
                .doctor(doctor1)
                .healthProfile(profileP2)
                .requestType(BookingRequestType.ONLINE_CHAT)
                .status(BookingStatus.RESTRICTED)
                .note("Tư vấn bé bị dị ứng hải sản")
                .scheduledAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .thread(thread3)
                .build());

        // Thread 4: Patient 2 & Doctor 2 (Pending/No thread chat room activity)
        // Booking 4 (PENDING)
        bookingRequestRepository.save(BookingRequest.builder()
                .patient(patient2)
                .doctor(doctor2)
                .healthProfile(profileP2)
                .requestType(BookingRequestType.ONLINE_CHAT)
                .status(BookingStatus.PENDING)
                .note("Cần tư vấn chế độ dinh dưỡng tăng đề kháng mùa dịch")
                .preferredTimeNote("Buổi tối từ 19:00 - 21:00")
                .scheduledAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build());

        // Extra bookings to test other statuses
        // Booking 5 (APPROVED) -> ONLINE_CHAT
        bookingRequestRepository.save(BookingRequest.builder()
                .patient(patient1)
                .doctor(doctor1)
                .healthProfile(profileP1)
                .requestType(BookingRequestType.ONLINE_CHAT)
                .status(BookingStatus.APPROVED)
                .note("Tư vấn tiêm chủng vắc-xin cho bé 2 tuổi")
                .scheduledAt(Instant.now().plus(2, ChronoUnit.DAYS))
                .thread(thread1) // Reuse thread1 since same patient-doctor pair
                .build());

        // Booking 6 (REJECTED) -> ONLINE_CHAT
        bookingRequestRepository.save(BookingRequest.builder()
                .patient(patient1)
                .doctor(doctor1)
                .healthProfile(profileP1)
                .requestType(BookingRequestType.ONLINE_CHAT)
                .status(BookingStatus.REJECTED)
                .note("Tư vấn sốt phát ban ở trẻ")
                .rejectReason("Lịch làm việc của bác sĩ đã kín ngày này, vui lòng chọn ngày khác.")
                .scheduledAt(Instant.now().minus(3, ChronoUnit.DAYS))
                .build());

        // Booking 7 (CANCELLED) -> ONLINE_CHAT
        bookingRequestRepository.save(BookingRequest.builder()
                .patient(patient1)
                .doctor(doctor1)
                .healthProfile(profileP1)
                .requestType(BookingRequestType.ONLINE_CHAT)
                .status(BookingStatus.CANCELLED)
                .note("Khám viêm phế quản nhẹ")
                .cancellationReason("Bé đã đỡ và được khám tại cơ sở y tế gần nhà.")
                .scheduledAt(Instant.now().minus(4, ChronoUnit.DAYS))
                .build());

        // Booking 8 (APPROVED) -> OFFLINE_CLINIC (Clinic visit - no chat room)
        bookingRequestRepository.save(BookingRequest.builder()
                .patient(patient1)
                .doctor(doctor1)
                .healthProfile(profileP1)
                .requestType(BookingRequestType.OFFLINE_CLINIC)
                .status(BookingStatus.APPROVED)
                .note("Khám lâm sàng tai mũi họng trực tiếp tại phòng khám")
                .confirmedLocation("Phòng khám CareNest - Tầng 1, Tòa nhà Y Tế Xanh, Cầu Giấy, Hà Nội")
                .confirmedNote("Mã đặt lịch CN-OF-9912. Quý khách vui lòng đến trước 10 phút để tiếp đón.")
                .scheduledAt(Instant.now().plus(3, ChronoUnit.DAYS))
                .build());

        log.info("Seeded 8 booking requests representing all 7 statuses, offline clinic type, and thread messages.");
    }
}
