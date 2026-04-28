package com.carenest.backend.module.medication.job;

import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.family.entity.Family;
import com.carenest.backend.module.family.entity.FamilyMember;
import com.carenest.backend.module.family.repository.FamilyMemberRepository;
import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import com.carenest.backend.module.medication.entity.MedicationLog;
import com.carenest.backend.module.medication.enums.MedicationLogStatus;
import com.carenest.backend.module.medication.repository.MedicationLogRepository;
import com.carenest.backend.module.notification.enums.NotificationType;
import com.carenest.backend.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicationReminderJob {

    private final MedicationLogRepository medicationLogRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final NotificationService notificationService;

    /**
     * Chạy mỗi 5 phút (0 0/5 * * * *).
     * Quét các đơn thuốc sắp tới trong 15 phút tới chưa được thông báo.
     */
    @Scheduled(cron = "0 0/5 * * * *")
    @Transactional
    public void scanAndRemindMedications() {
        log.info("Bắt đầu Cronjob: Quét lịch uống thuốc sắp tới...");
        Instant now = Instant.now();
        Instant next15Mins = now.plus(15, ChronoUnit.MINUTES);

        List<MedicationLog> upcomingLogs = medicationLogRepository
                .findAllByStatusAndIsNotifiedFalseAndScheduledTimeBetween(
                        MedicationLogStatus.PENDING,
                        now,
                        next15Mins
                );

        if (upcomingLogs.isEmpty()) {
            log.info("Cronjob hoàn tất: Không có lịch uống thuốc mới nào.");
            return;
        }

        log.info("Tìm thấy {} cữ thuốc cần nhắc nhở.", upcomingLogs.size());

        for (MedicationLog logItem : upcomingLogs) {
            HealthProfile profile = logItem.getMedication().getHealthProfile();
            List<User> targetUsers = getNotificationTargets(profile);

            String title = "Đến giờ uống thuốc!";
            String message = String.format("Nhắc nhở: Sắp đến giờ uống %s cho %s (%s).",
                    logItem.getMedication().getMedicineName(),
                    profile.getFullName(),
                    logItem.getMedication().getDosage()
            );

            // Gửi thông báo
            notificationService.createNotificationForUsers(
                    targetUsers,
                    title,
                    message,
                    NotificationType.MEDICATION,
                    "MEDICATION_LOG",
                    logItem.getId()
            );

            // Cập nhật cờ
            logItem.setIsNotified(true);
            medicationLogRepository.save(logItem);
        }

        log.info("Cronjob hoàn tất: Đã gửi thông báo cho {} cữ thuốc.", upcomingLogs.size());
    }

    private List<User> getNotificationTargets(HealthProfile profile) {
        Family family = profile.getFamily();
        if (family != null) {
            // Lấy tất cả thành viên trong gia đình
            List<FamilyMember> members = familyMemberRepository.findAllByFamilyId(family.getId());
            return members.stream()
                    .map(FamilyMember::getUser)
                    .collect(Collectors.toList());
        } else {
            // Nếu hồ sơ cá nhân không thuộc gia đình, chỉ báo cho chính người đó
            return Collections.singletonList(profile.getUser());
        }
    }
}
