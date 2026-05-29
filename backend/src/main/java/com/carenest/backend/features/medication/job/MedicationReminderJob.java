package com.carenest.backend.features.medication.job;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.family.entity.Family;
import com.carenest.backend.features.family.entity.FamilyMember;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import com.carenest.backend.features.medication.entity.MedicationLog;
import com.carenest.backend.features.medication.enums.MedicationLogStatus;
import com.carenest.backend.features.medication.repository.MedicationLogRepository;
import com.carenest.backend.features.notification.enums.NotificationType;
import com.carenest.backend.features.notification.service.NotificationService;
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
     * Cháº¡y má»—i 5 phÃºt (0 0/5 * * * *).
     * QuÃ©t cÃ¡c Ä‘Æ¡n thuá»‘c sáº¯p tá»›i trong 15 phÃºt tá»›i chÆ°a Ä‘Æ°á»£c thÃ´ng bÃ¡o.
     */
    @Scheduled(cron = "0 0/5 * * * *")
    @Transactional
    public void scanAndRemindMedications() {
        log.info("Báº¯t Ä‘áº§u Cronjob: QuÃ©t lá»‹ch uá»‘ng thuá»‘c sáº¯p tá»›i...");
        Instant now = Instant.now();
        Instant next15Mins = now.plus(15, ChronoUnit.MINUTES);

        List<MedicationLog> upcomingLogs = medicationLogRepository
                .findAllByStatusAndIsNotifiedFalseAndScheduledTimeBetween(
                        MedicationLogStatus.PENDING,
                        now,
                        next15Mins
                );

        if (upcomingLogs.isEmpty()) {
            log.info("Cronjob hoÃ n táº¥t: KhÃ´ng cÃ³ lá»‹ch uá»‘ng thuá»‘c má»›i nÃ o.");
            return;
        }

        log.info("TÃ¬m tháº¥y {} cá»¯ thuá»‘c cáº§n nháº¯c nhá»Ÿ.", upcomingLogs.size());

        for (MedicationLog logItem : upcomingLogs) {
            HealthProfile profile = logItem.getMedication().getHealthProfile();
            List<User> targetUsers = getNotificationTargets(profile);

            String title = "Äáº¿n giá» uá»‘ng thuá»‘c!";
            String message = String.format("Nháº¯c nhá»Ÿ: Sáº¯p Ä‘áº¿n giá» uá»‘ng %s cho %s (%s).",
                    logItem.getMedication().getMedicineName(),
                    profile.getFullName(),
                    logItem.getMedication().getDosage()
            );

            // Gá»­i thÃ´ng bÃ¡o
            notificationService.createNotificationForUsers(
                    targetUsers,
                    title,
                    message,
                    NotificationType.MEDICATION,
                    "MEDICATION_LOG",
                    logItem.getId()
            );

            // Cáº­p nháº­t cá»
            logItem.setIsNotified(true);
            medicationLogRepository.save(logItem);
        }

        log.info("Cronjob hoÃ n táº¥t: ÄÃ£ gá»­i thÃ´ng bÃ¡o cho {} cá»¯ thuá»‘c.", upcomingLogs.size());
    }

    private List<User> getNotificationTargets(HealthProfile profile) {
        Family family = profile.getFamily();
        if (family != null) {
            // Láº¥y táº¥t cáº£ thÃ nh viÃªn trong gia Ä‘Ã¬nh
            List<FamilyMember> members = familyMemberRepository.findAllByFamilyId(family.getId());
            return members.stream()
                    .map(FamilyMember::getUser)
                    .collect(Collectors.toList());
        } else {
            // Náº¿u há»“ sÆ¡ cÃ¡ nhÃ¢n khÃ´ng thuá»™c gia Ä‘Ã¬nh, chá»‰ bÃ¡o cho chÃ­nh ngÆ°á»i Ä‘Ã³
            return Collections.singletonList(profile.getUser());
        }
    }
}
