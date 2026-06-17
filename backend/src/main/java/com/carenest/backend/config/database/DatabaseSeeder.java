package com.carenest.backend.config.database;

import com.carenest.backend.features.appointment.entity.Appointment;
import com.carenest.backend.features.appointment.enums.AppointmentStatus;
import com.carenest.backend.features.appointment.repository.AppointmentRepository;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Gender;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.booking.entity.BookingRequest;
import com.carenest.backend.features.booking.entity.ConsultationMessage;
import com.carenest.backend.features.booking.entity.ConsultationThread;
import com.carenest.backend.features.booking.enums.BookingRequestType;
import com.carenest.backend.features.booking.enums.BookingStatus;
import com.carenest.backend.features.booking.repository.BookingRequestRepository;
import com.carenest.backend.features.booking.repository.ConsultationMessageRepository;
import com.carenest.backend.features.booking.repository.ConsultationThreadRepository;
import com.carenest.backend.features.community.entity.ChatGroup;
import com.carenest.backend.features.community.entity.GroupPost;
import com.carenest.backend.features.community.entity.GroupPostComment;
import com.carenest.backend.features.community.entity.GroupPostLike;
import com.carenest.backend.features.community.entity.UserGroupMembership;
import com.carenest.backend.features.community.enums.GroupRole;
import com.carenest.backend.features.community.enums.PostStatus;
import com.carenest.backend.features.community.repository.ChatGroupRepository;
import com.carenest.backend.features.community.repository.GroupPostCommentRepository;
import com.carenest.backend.features.community.repository.GroupPostLikeRepository;
import com.carenest.backend.features.community.repository.GroupPostRepository;
import com.carenest.backend.features.community.repository.UserGroupMembershipRepository;
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
import com.carenest.backend.features.medication.enums.MedicationLogStatus;
import com.carenest.backend.features.medication.enums.MedicationStatus;
import com.carenest.backend.features.medication.repository.MedicationLogRepository;
import com.carenest.backend.features.medication.repository.MedicationRepository;
import com.carenest.backend.features.vaccination.entity.VaccinationDose;
import com.carenest.backend.features.vaccination.entity.VaccinationRecord;
import com.carenest.backend.features.vaccination.enums.DoseStatus;
import com.carenest.backend.features.vaccination.repository.VaccinationDoseRepository;
import com.carenest.backend.features.vaccination.repository.VaccinationRecordRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile({"dev", "qa"})
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private static final String FAMILY_A_CODE = "FAMA1234";
    private static final String FAMILY_B_CODE = "FAMB5678";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final QaDemoSeedProperties seedProperties;
    private final DoctorVerificationRepository verificationRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final UserGroupMembershipRepository membershipRepository;
    private final GroupPostRepository groupPostRepository;
    private final GroupPostCommentRepository groupPostCommentRepository;
    private final GroupPostLikeRepository groupPostLikeRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final MedicationRepository medicationRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final VaccinationDoseRepository vaccinationDoseRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final ConsultationThreadRepository consultationThreadRepository;
    private final ConsultationMessageRepository consultationMessageRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedProperties.isEnabled()) {
            log.info("QA demo seed is disabled.");
            return;
        }

        log.info("Starting QA database seeder for dev/qa profile...");
        String defaultPassword = seedProperties.requireDefaultPassword();

        User admin = seedUser(seedProperties.getAdminEmail(), defaultPassword, seedProperties.getAdminFullName(), Role.ADMIN);
        User patient1 = seedUser(seedProperties.getPatientPrimaryEmail(), defaultPassword, seedProperties.getPatientPrimaryFullName(), Role.USER);
        User patient2 = seedUser(seedProperties.getPatientSecondaryEmail(), defaultPassword, seedProperties.getPatientSecondaryFullName(), Role.USER);
        User doctor1 = seedUser(seedProperties.getDoctorPediatricEmail(), defaultPassword, seedProperties.getDoctorPediatricFullName(), Role.DOCTOR);
        User doctor2 = seedUser(seedProperties.getDoctorGeneralEmail(), defaultPassword, seedProperties.getDoctorGeneralFullName(), Role.DOCTOR);
        User qaModerator = seedUser(seedProperties.getModeratorEmail(), defaultPassword, seedProperties.getModeratorFullName(), Role.USER);

        ensureDoctorVerification(doctor1, "Nhi khoa", "VNC-123456");
        ensureDoctorVerification(doctor2, "Da khoa", "VNC-789012");

        seedGroupsAndMemberships(patient1, patient2, doctor1, qaModerator);
        seedFamiliesAndProfiles(patient1, patient2);
        seedBookingAndConsultations(patient1, patient2, doctor1, doctor2);

        log.info("QA database seeder completed successfully.");
    }

    private User seedUser(String email, String password, String fullName, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
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
    }

    private void ensureDoctorVerification(User doctor, String specialty, String certificationNumber) {
        if (verificationRepository.findByUserId(doctor.getId()).isPresent()) {
            return;
        }

        DoctorVerification verification = new DoctorVerification();
        verification.setUser(doctor);
        verification.setSpecialty(specialty);
        verification.setHospitalName("Benh vien CareNest");
        verification.setCertificationNumber(certificationNumber);
        verification.setDocumentUrl("https://example.com/document.jpg");
        verification.setStatus(VerificationStatus.APPROVED);
        verificationRepository.save(verification);
        log.info("Seeded verification for doctor: {}", doctor.getEmail());
    }

    private void seedGroupsAndMemberships(User patient1, User patient2, User doctor1, User qaModerator) {
        ChatGroup group1 = getOrCreateGroup(
            "Me va Be CareNest",
            "Hoi nhom chia se kinh nghiem nuoi day va cham soc tre nho.",
            "Me va Be",
            "tre-nho,dinh-duong,cham-con"
        );
        ChatGroup group2 = getOrCreateGroup(
            "Chia se kinh nghiem Nhi khoa",
            "Hoi dap cac van de Nhi khoa thuong gap cung bac si.",
            "Nhi khoa",
            "nhi-khoa,sot,ho,tieu-hoa"
        );
        ChatGroup group3 = getOrCreateGroup(
            "Suc khoe Gia dinh",
            "Cong dong tu van suc khoe tong quat cho gia dinh.",
            "Suc khoe chung",
            "gia-dinh,tu-van,suc-khoe"
        );

        ensureMembership(qaModerator, group1, GroupRole.HOST);
        ensureMembership(qaModerator, group2, GroupRole.HOST);
        ensureMembership(qaModerator, group3, GroupRole.HOST);

        ensureMembership(patient1, group1, GroupRole.MEMBER);
        ensureMembership(patient1, group2, GroupRole.MEMBER);
        ensureMembership(patient2, group1, GroupRole.MEMBER);
        ensureMembership(doctor1, group2, GroupRole.MEMBER);

        GroupPost approved = ensureGroupPost(
            group1,
            patient1,
            "Hoi ve lich tiem chung cho be 6 thang tuoi",
            "Be nha em duoc 6 thang tuoi thi can tiem nhung mui vac-xin gi? Mong moi nguoi tu van them.",
            "tiem-chung,vac-xin",
            PostStatus.APPROVED,
            null,
            null
        );
        ensureGroupPostLike(approved, patient2);
        ensureGroupPostComment(
            approved,
            doctor1,
            "Chao ban, be 6 thang tuoi can nhac lai mui 6 trong 1 va co the duoc tu van them ve Rotavirus."
        );

        ensureGroupPost(
            group1,
            patient1,
            "Be bi sot nhe sau khi tiem phong phai lam sao",
            "Be nha em vua tiem xong thi sot nhe 38 do C, van an ngu binh thuong. Em co can ha sot khong?",
            "sot,sau-tiem",
            PostStatus.PENDING_APPROVAL,
            null,
            null
        );

        ensureGroupPost(
            group1,
            patient2,
            "Than duoc tang chieu cao cho be gia re",
            "Noi dung quang cao san pham xach tay khong phu hop voi quy tac hoi nhom.",
            "quang-cao,spam",
            PostStatus.REJECTED,
            "Noi dung mang tinh chat quang cao thuong mai, spam, vi pham quy tac hoi nhom.",
            qaModerator
        );

        log.info("QA community groups and moderation seed ensured.");
    }

    private ChatGroup getOrCreateGroup(String name, String description, String category, String tags) {
        return chatGroupRepository.findAll().stream()
            .filter(group -> name.equals(group.getName()))
            .findFirst()
            .orElseGet(() -> chatGroupRepository.save(ChatGroup.builder()
                .name(name)
                .description(description)
                .category(category)
                .tags(tags)
                .isPrivate(false)
                .build()));
    }

    private void ensureMembership(User user, ChatGroup group, GroupRole role) {
        var existingMembership = membershipRepository.findByGroupIdAndUserId(group.getId(), user.getId());
        if (existingMembership.isPresent()) {
            UserGroupMembership membership = existingMembership.get();
            if (membership.getGroupRole() != role) {
                membership.setGroupRole(role);
                membershipRepository.save(membership);
            }
            return;
        }

        membershipRepository.save(UserGroupMembership.builder()
            .user(user)
            .group(group)
            .groupRole(role)
            .build());
    }

    private GroupPost ensureGroupPost(
        ChatGroup group,
        User author,
        String title,
        String content,
        String tags,
        PostStatus status,
        String rejectionReason,
        User reviewer
    ) {
        return groupPostRepository.findAll().stream()
            .filter(post -> Objects.equals(post.getTitle(), title))
            .findFirst()
            .orElseGet(() -> groupPostRepository.save(GroupPost.builder()
                .chatGroup(group)
                .author(author)
                .title(title)
                .content(content)
                .tags(tags)
                .status(status)
                .rejectionReason(rejectionReason)
                .reviewer(reviewer)
                .build()));
    }

    private void ensureGroupPostLike(GroupPost post, User user) {
        boolean exists = groupPostLikeRepository.findAll().stream()
            .anyMatch(like -> like.getGroupPost().getId().equals(post.getId()) && like.getUser().getId().equals(user.getId()));
        if (!exists) {
            groupPostLikeRepository.save(GroupPostLike.builder().groupPost(post).user(user).build());
        }
    }

    private void ensureGroupPostComment(GroupPost post, User author, String content) {
        boolean exists = groupPostCommentRepository.findAll().stream()
            .anyMatch(comment ->
                comment.getGroupPost().getId().equals(post.getId()) &&
                comment.getAuthor().getId().equals(author.getId()) &&
                Objects.equals(comment.getContent(), content)
            );
        if (!exists) {
            groupPostCommentRepository.save(GroupPostComment.builder()
                .groupPost(post)
                .author(author)
                .content(content)
                .build());
        }
    }

    private void seedFamiliesAndProfiles(User patient1, User patient2) {
        Family familyA = getOrCreateFamily("Gia dinh A", patient1, FAMILY_A_CODE);
        Family familyB = getOrCreateFamily("Gia dinh B", patient2, FAMILY_B_CODE);

        ensureFamilyMembership(familyA, patient1, FamilyRole.OWNER);
        ensureFamilyMembership(familyB, patient2, FamilyRole.OWNER);
        ensureFamilyMembership(familyB, patient1, FamilyRole.MEMBER);

        HealthProfile profileP1 = getOrCreateProfile(
            patient1,
            familyA,
            "Kiet Tuan",
            LocalDate.of(2000, 1, 1),
            Gender.MALE,
            "Ban than",
            false
        );

        getOrCreateProfile(
            patient2,
            familyB,
            "Tuan Kiet",
            LocalDate.of(2006, 1, 13),
            Gender.MALE,
            "Ban than",
            false
        );

        HealthProfile babyNa = getOrCreateProfile(
            patient1,
            familyA,
            "Be Na",
            LocalDate.now().minusYears(2),
            Gender.FEMALE,
            "Con gai",
            true
        );

        ensureMedicationScenario(babyNa);
        ensureVaccinationScenario(babyNa);
        ensureManualAppointmentScenario(babyNa);

        log.info("QA family and health-profile seed ensured.");
    }

    private Family getOrCreateFamily(String name, User owner, String joinCode) {
        return familyRepository.findByJoinCode(joinCode).orElseGet(() -> familyRepository.save(Family.builder()
            .name(name)
            .owner(owner)
            .joinCode(joinCode)
            .joinCodeExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
            .build()));
    }

    private void ensureFamilyMembership(Family family, User user, FamilyRole role) {
        var existing = familyMemberRepository.findByFamilyIdAndUserId(family.getId(), user.getId());
        if (existing.isPresent()) {
            FamilyMember member = existing.get();
            if (member.getRole() != role) {
                member.setRole(role);
                familyMemberRepository.save(member);
            }
            return;
        }

        familyMemberRepository.save(FamilyMember.builder()
            .family(family)
            .user(user)
            .role(role)
            .build());
    }

    private HealthProfile getOrCreateProfile(
        User user,
        Family family,
        String fullName,
        LocalDate dob,
        Gender gender,
        String relationship,
        boolean isChild
    ) {
        return healthProfileRepository.findAll().stream()
            .filter(profile -> profile.getFamily().getId().equals(family.getId()) && fullName.equals(profile.getFullName()))
            .findFirst()
            .orElseGet(() -> healthProfileRepository.save(HealthProfile.builder()
                .user(user)
                .family(family)
                .fullName(fullName)
                .dateOfBirth(dob)
                .gender(gender)
                .relationship(relationship)
                .bloodType(isChild ? BloodType.O_POSITIVE : BloodType.A_POSITIVE)
                .allergies(isChild ? "Di ung hai san nhe" : "Khong co")
                .chronicDiseases("Khong")
                .notes(isChild ? "Can theo doi lich tiem va thuoc." : "QA seed profile")
                .isChild(isChild)
                .build()));
    }

    private void ensureMedicationScenario(HealthProfile babyNa) {
        Medication medication = medicationRepository.findByHealthProfileId(babyNa.getId()).stream()
            .filter(item -> "Siro ho Prospan".equals(item.getMedicineName()))
            .findFirst()
            .orElseGet(() -> medicationRepository.save(Medication.builder()
                .healthProfile(babyNa)
                .medicineName("Siro ho Prospan")
                .dosage("5ml moi lan")
                .frequency(MedicationFrequency.TWICE_DAILY)
                .timesPerDay(2)
                .timeSlots("08:00,20:00")
                .startDate(LocalDate.now().minusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .status(MedicationStatus.ACTIVE)
                .notes("Uong sau khi an no.")
                .build()));

        LocalDate today = LocalDate.now();
        ZoneId zoneId = ZoneId.systemDefault();
        ensureMedicationLog(medication, today.atTime(8, 0).atZone(zoneId).toInstant());
        ensureMedicationLog(medication, today.atTime(20, 0).atZone(zoneId).toInstant());
    }

    private void ensureMedicationLog(Medication medication, Instant scheduledTime) {
        boolean exists = medicationLogRepository.findAll().stream()
            .anyMatch(log -> log.getMedication().getId().equals(medication.getId()) && log.getScheduledTime().equals(scheduledTime));
        if (!exists) {
            medicationLogRepository.save(MedicationLog.builder()
                .medication(medication)
                .scheduledTime(scheduledTime)
                .status(MedicationLogStatus.PENDING)
                .build());
        }
    }

    private void ensureVaccinationScenario(HealthProfile babyNa) {
        VaccinationRecord record = vaccinationRecordRepository.findAll().stream()
            .filter(item -> item.getHealthProfile().getId().equals(babyNa.getId()) && "Vac-xin 6 trong 1 (Infanrix Hexa)".equals(item.getVaccineName()))
            .findFirst()
            .orElseGet(() -> vaccinationRecordRepository.save(VaccinationRecord.builder()
                .healthProfile(babyNa)
                .vaccineName("Vac-xin 6 trong 1 (Infanrix Hexa)")
                .totalDoses(3)
                .doseIntervalDays(30)
                .notes("Ngua 6 benh co ban cho tre nho.")
                .build()));

        ensureVaccinationDose(record, 1, LocalDate.now().minusDays(60), DoseStatus.COMPLETED, "VNVC", "Dieu duong Mai", "Be on dinh sau tiem.");
        ensureVaccinationDose(record, 2, LocalDate.now().minusDays(30), DoseStatus.COMPLETED, "VNVC", "Dieu duong Hung", "Sung nhe tai vet tiem.");
        ensureVaccinationDose(record, 3, LocalDate.now().plusDays(1), DoseStatus.PENDING, null, null, "Mui hen ngay mai.");
    }

    private void ensureVaccinationDose(
        VaccinationRecord record,
        int doseNumber,
        LocalDate scheduledDate,
        DoseStatus status,
        String location,
        String administeredBy,
        String notes
    ) {
        boolean exists = vaccinationDoseRepository.findAll().stream()
            .anyMatch(dose -> dose.getVaccinationRecord().getId().equals(record.getId()) && dose.getDoseNumber() == doseNumber);
        if (!exists) {
            vaccinationDoseRepository.save(VaccinationDose.builder()
                .vaccinationRecord(record)
                .doseNumber(doseNumber)
                .scheduledDate(scheduledDate)
                .dateAdministered(status == DoseStatus.COMPLETED ? scheduledDate : null)
                .location(location)
                .administeredBy(administeredBy)
                .status(status)
                .notes(notes)
                .build());
        }
    }

    private void ensureManualAppointmentScenario(HealthProfile babyNa) {
        boolean exists = appointmentRepository.findByHealthProfileIdOrderByAppointmentDateDesc(babyNa.getId()).stream()
            .anyMatch(appointment ->
                "Bac si Nguyen Van An".equals(appointment.getDoctorName()) &&
                "Benh vien Nhi Trung Uong".equals(appointment.getHospitalName())
            );
        if (!exists) {
            LocalDate today = LocalDate.now();
            appointmentRepository.save(Appointment.builder()
                .healthProfile(babyNa)
                .doctorName("Bac si Nguyen Van An")
                .hospitalName("Benh vien Nhi Trung Uong")
                .address("18/879 La Thanh, Dong Da, Ha Noi")
                .appointmentDate(today.atTime(14, 0).atZone(ZoneId.systemDefault()).toInstant())
                .status(AppointmentStatus.SCHEDULED)
                .notes("Kham tong quat va tai mui hong.")
                .build());
        }
    }

    private void seedBookingAndConsultations(User patient1, User patient2, User doctor1, User doctor2) {
        HealthProfile profileP1 = findPrimaryProfile(patient1);
        HealthProfile profileP2 = findPrimaryProfile(patient2);

        ConsultationThread thread1 = getOrCreateThread(patient1, doctor1);
        ConsultationThread thread2 = getOrCreateThread(patient1, doctor2);
        ConsultationThread thread3 = getOrCreateThread(patient2, doctor1);

        ensureConsultationMessage(thread1, patient1, "Chao bac si, be nha em hay ho khan vao buoi toi.");
        ensureConsultationMessage(thread1, doctor1, "Be co sot, kho tho, hay bo an khong?");
        ensureConsultationMessage(thread1, patient1, "Be khong sot, van choi binh thuong nhung cu nam xuong la ho.");
        ensureConsultationMessage(thread1, doctor1, "Ban giu am co hong va theo doi them 2 ngay nhe.");

        ensureConsultationMessage(thread2, patient1, "Toi muon hoi ve che do an cho nguoi bi trao nguoc da day.");
        ensureConsultationMessage(thread2, doctor2, "Can tranh do chua cay, nuoc co gas va khong nam ngay sau khi an.");
        ensureConsultationMessage(thread2, patient1, "Cam on bac si, toi da ro.");

        ensureConsultationMessage(thread3, patient2, "Be nha toi an hai san xong bi man ngua quanh mieng.");
        ensureConsultationMessage(thread3, doctor1, "Ban theo doi xem be co sung moi, kho tho hay noi me day toan than khong.");

        Appointment syncedCompletedAppointment = getOrCreateSyncedAppointment(profileP1, doctor2);

        ensureBooking(
            patient1, doctor1, profileP1, BookingRequestType.ONLINE_CHAT, BookingStatus.ACTIVE,
            "Tu van be bi ho khan kho khe dem", null, null, null, null, null, thread1, null
        );
        ensureBooking(
            patient1, doctor2, profileP1, BookingRequestType.ONLINE_CHAT, BookingStatus.COMPLETED,
            "Tu van trao nguoc da day", null, null, null, null, null, thread2, syncedCompletedAppointment
        );
        ensureBooking(
            patient2, doctor1, profileP2, BookingRequestType.ONLINE_CHAT, BookingStatus.RESTRICTED,
            "Tu van be bi di ung hai san", null, null, null, null, null, thread3, null
        );
        ensureBooking(
            patient2, doctor2, profileP2, BookingRequestType.ONLINE_CHAT, BookingStatus.PENDING,
            "Can tu van che do dinh duong tang de khang", "Buoi toi 19:00 - 21:00", null, null, null, null, null, null
        );
        ensureBooking(
            patient1, doctor1, profileP1, BookingRequestType.ONLINE_CHAT, BookingStatus.APPROVED,
            "Tu van tiem chung vac-xin cho be 2 tuoi", null, null, Instant.now().plus(2, ChronoUnit.DAYS), null, null, thread1, null
        );
        ensureBooking(
            patient1, doctor1, profileP1, BookingRequestType.ONLINE_CHAT, BookingStatus.REJECTED,
            "Tu van sot phat ban o tre", null, "Lich lam viec cua bac si da kin, vui long chon ngay khac.", null, null, null, null, null
        );
        ensureBooking(
            patient1, doctor1, profileP1, BookingRequestType.ONLINE_CHAT, BookingStatus.CANCELLED,
            "Kham viem phe quan nhe", null, null, null, "Be da do va duoc kham tai co so y te gan nha.", null, null, null
        );
        ensureBooking(
            patient1, doctor1, profileP1, BookingRequestType.OFFLINE_CLINIC, BookingStatus.APPROVED,
            "Kham lam sang tai mui hong truc tiep", null, null, Instant.now().plus(3, ChronoUnit.DAYS),
            null, "Ma dat lich CN-OF-9912. Vui long den truoc 10 phut.", null, null,
            "Phong kham CareNest - Tang 1, Toa nha Y te Xanh, Cau Giay, Ha Noi"
        );

        log.info("QA booking and consultation seed ensured.");
    }

    private HealthProfile findPrimaryProfile(User user) {
        return healthProfileRepository.findByUserIdAndDeletedAtIsNull(user.getId()).stream()
            .filter(profile -> !Boolean.TRUE.equals(profile.getIsChild()))
            .min(Comparator.comparing(HealthProfile::getId))
            .orElse(null);
    }

    private ConsultationThread getOrCreateThread(User patient, User doctor) {
        return consultationThreadRepository.findByPatientAndDoctor(patient, doctor)
            .orElseGet(() -> consultationThreadRepository.save(ConsultationThread.builder()
                .patient(patient)
                .doctor(doctor)
                .build()));
    }

    private void ensureConsultationMessage(ConsultationThread thread, User sender, String content) {
        boolean exists = consultationMessageRepository.findAll().stream()
            .anyMatch(message ->
                message.getThread().getId().equals(thread.getId()) &&
                message.getSender().getId().equals(sender.getId()) &&
                Objects.equals(message.getContent(), content)
            );
        if (!exists) {
            consultationMessageRepository.save(ConsultationMessage.builder()
                .thread(thread)
                .sender(sender)
                .content(content)
                .build());
        }
    }

    private Appointment getOrCreateSyncedAppointment(HealthProfile profile, User doctor) {
        return appointmentRepository.findByHealthProfileIdOrderByAppointmentDateDesc(profile.getId()).stream()
            .filter(appointment ->
                doctor.getFullName().equals(appointment.getDoctorName()) &&
                appointment.getStatus() == AppointmentStatus.COMPLETED &&
                "Trao nguoc da day nhe. Thuc hien dieu chinh che do an uong sinh hoat.".equals(appointment.getResultNotes())
            )
            .findFirst()
            .orElseGet(() -> appointmentRepository.save(Appointment.builder()
                .healthProfile(profile)
                .doctorName(doctor.getFullName())
                .hospitalName("Benh vien CareNest")
                .address("Tu van truc tuyen qua CareNest App")
                .appointmentDate(Instant.now().minus(5, ChronoUnit.DAYS))
                .status(AppointmentStatus.COMPLETED)
                .notes("Tu van trao nguoc da day truc tuyen.")
                .resultNotes("Trao nguoc da day nhe. Thuc hien dieu chinh che do an uong sinh hoat.")
                .build()));
    }

    private void ensureBooking(
        User patient,
        User doctor,
        HealthProfile profile,
        BookingRequestType requestType,
        BookingStatus status,
        String note,
        String preferredTimeNote,
        String rejectReason,
        Instant scheduledAt,
        String cancellationReason,
        String confirmedNote,
        ConsultationThread thread,
        Appointment appointment
    ) {
        ensureBooking(patient, doctor, profile, requestType, status, note, preferredTimeNote, rejectReason, scheduledAt,
            cancellationReason, confirmedNote, thread, appointment, null);
    }

    private void ensureBooking(
        User patient,
        User doctor,
        HealthProfile profile,
        BookingRequestType requestType,
        BookingStatus status,
        String note,
        String preferredTimeNote,
        String rejectReason,
        Instant scheduledAt,
        String cancellationReason,
        String confirmedNote,
        ConsultationThread thread,
        Appointment appointment,
        String confirmedLocation
    ) {
        boolean exists = bookingRequestRepository.findAllByOrderByCreatedAtDesc().stream()
            .anyMatch(booking ->
                booking.getPatient().getId().equals(patient.getId()) &&
                booking.getDoctor().getId().equals(doctor.getId()) &&
                booking.getRequestType() == requestType &&
                booking.getStatus() == status &&
                Objects.equals(booking.getNote(), note)
            );
        if (exists) {
            return;
        }

        bookingRequestRepository.save(BookingRequest.builder()
            .patient(patient)
            .doctor(doctor)
            .healthProfile(profile)
            .requestType(requestType)
            .status(status)
            .note(note)
            .preferredTimeNote(preferredTimeNote)
            .rejectReason(rejectReason)
            .scheduledAt(scheduledAt)
            .cancellationReason(cancellationReason)
            .confirmedNote(confirmedNote)
            .confirmedLocation(confirmedLocation)
            .thread(thread)
            .appointment(appointment)
            .build());
    }
}
