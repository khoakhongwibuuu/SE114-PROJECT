package com.carenest.backend.module.dashboard.service.impl;

import com.carenest.backend.module.dashboard.dto.response.DashboardResponse;
import com.carenest.backend.module.dashboard.dto.response.DashboardTask;
import com.carenest.backend.module.dashboard.service.DashboardService;
import com.carenest.backend.module.family.util.FamilySecurityUtil;
import com.carenest.backend.module.medication.entity.MedicationLog;
import com.carenest.backend.module.medication.enums.MedicationLogStatus;
import com.carenest.backend.module.medication.repository.MedicationLogRepository;
import com.carenest.backend.module.notification.repository.NotificationRepository;
import com.carenest.backend.module.vaccination.entity.VaccinationDose;
import com.carenest.backend.module.vaccination.enums.DoseStatus;
import com.carenest.backend.module.vaccination.repository.VaccinationDoseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final MedicationLogRepository medicationLogRepository;
    private final VaccinationDoseRepository vaccinationDoseRepository;
    private final NotificationRepository notificationRepository;
    private final FamilySecurityUtil familySecurityUtil;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardOverview(Long familyId) {
        // 1. Kiểm tra bảo mật: User hiện tại phải thuộc familyId này
        familySecurityUtil.checkUserBelongsToFamily(familyId);
        Long userId = familySecurityUtil.getCurrentUser().getId();

        // 2. Tính toán startOfDay và endOfDay
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        Instant startOfDay = now.toLocalDate().atStartOfDay(zoneId).toInstant();
        Instant endOfDay = now.toLocalDate().plusDays(1).atStartOfDay(zoneId).toInstant();

        // 3. Query 1: Lấy Medication Logs (Tuyệt đối không N+1 do đã dùng JOIN FETCH)
        List<MedicationLog> pendingMedications = medicationLogRepository.findPendingTasksForFamilyToday(
                familyId, MedicationLogStatus.PENDING, startOfDay, endOfDay);

        // 4. Query 2: Lấy Vaccination Doses (Đã dùng JOIN FETCH)
        List<VaccinationDose> upcomingVaccines = vaccinationDoseRepository.findUpcomingDosesForFamily(
                familyId, DoseStatus.PENDING);

        // 5. Query 3: Đếm Notifications
        long unreadCount = notificationRepository.countUnreadNotifications(userId);

        // 6. Map dữ liệu vào list chung DTO (BFF Pattern)
        List<DashboardTask> tasks = new ArrayList<>();

        for (MedicationLog logItem : pendingMedications) {
            tasks.add(DashboardTask.builder()
                    .type("MEDICATION")
                    .title(logItem.getMedication().getMedicineName())
                    .time(logItem.getScheduledTime().toString())
                    .memberName(logItem.getMedication().getHealthProfile().getFullName())
                    .referenceId(logItem.getId())
                    .build());
        }

        for (VaccinationDose dose : upcomingVaccines) {
            tasks.add(DashboardTask.builder()
                    .type("VACCINATION")
                    .title(dose.getVaccinationRecord().getVaccineName() + " (Mũi " + dose.getDoseNumber() + ")")
                    .time(dose.getScheduledDate().toString())
                    .memberName(dose.getVaccinationRecord().getHealthProfile().getFullName())
                    .referenceId(dose.getId())
                    .build());
        }

        // Sắp xếp các tasks hỗn hợp theo thời gian (chuỗi ISO 8601 tự động sort string đúng chuẩn)
        tasks.sort(Comparator.comparing(DashboardTask::getTime));

        return DashboardResponse.builder()
                .unreadNotifications(unreadCount)
                .todayTasks(tasks)
                .build();
    }
}
