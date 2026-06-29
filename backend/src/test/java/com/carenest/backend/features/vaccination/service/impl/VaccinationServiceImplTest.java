package com.carenest.backend.features.vaccination.service.impl;

import com.carenest.backend.features.family.util.FamilySecurityUtil;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import com.carenest.backend.features.vaccination.dto.request.AdministerDoseRequest;
import com.carenest.backend.features.vaccination.entity.VaccinationDose;
import com.carenest.backend.features.vaccination.entity.VaccinationRecord;
import com.carenest.backend.features.vaccination.enums.DoseStatus;
import com.carenest.backend.features.vaccination.mapper.VaccinationMapper;
import com.carenest.backend.features.vaccination.repository.VaccinationDoseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VaccinationServiceImplTest {

    @Mock
    private VaccinationDoseRepository vaccinationDoseRepository;

    @Mock
    private FamilySecurityUtil familySecurityUtil;

    @Mock
    private VaccinationMapper vaccinationMapper;

    @Mock
    private CacheManager cacheManager; // Needed because we injected it in Service

    @InjectMocks
    private VaccinationServiceImpl vaccinationService;

    @Captor
    private ArgumentCaptor<List<VaccinationDose>> doseListCaptor;

    private VaccinationRecord record;
    private VaccinationDose dose1;
    private VaccinationDose dose2;
    private VaccinationDose dose3;

    @BeforeEach
    void setUp() {
        HealthProfile profile = HealthProfile.builder().build();
        profile.setId(1L);

        record = VaccinationRecord.builder()
                .healthProfile(profile)
                .vaccineName("Test Vaccine")
                .totalDoses(3)
                .doseIntervalDays(30)
                .build();
        record.setId(1L);

        // Ban đầu: Lịch dự kiến là 01/01, 31/01, 02/03 (cách 30 ngày)
        dose1 = VaccinationDose.builder()
                .vaccinationRecord(record)
                .doseNumber(1)
                .scheduledDate(LocalDate.of(2026, 1, 1))
                .status(DoseStatus.PENDING)
                .build();
        dose1.setId(1L);

        dose2 = VaccinationDose.builder()
                .vaccinationRecord(record)
                .doseNumber(2)
                .scheduledDate(LocalDate.of(2026, 1, 31))
                .status(DoseStatus.PENDING)
                .build();
        dose2.setId(2L);

        dose3 = VaccinationDose.builder()
                .vaccinationRecord(record)
                .doseNumber(3)
                .scheduledDate(LocalDate.of(2026, 3, 2))
                .status(DoseStatus.PENDING)
                .build();
        dose3.setId(3L);
    }

    @Test
    void administerDose_shouldRescheduleFutureDoses_whenAdministeredLate() {
        // Given
        AdministerDoseRequest request = new AdministerDoseRequest();
        // Tiêm trễ 5 ngày (06/01 thay vì 01/01)
        LocalDate actualDate = LocalDate.of(2026, 1, 6);
        request.setDateAdministered(actualDate);

        when(vaccinationDoseRepository.findById(1L)).thenReturn(Optional.of(dose1));
        doNothing().when(familySecurityUtil).checkCanWriteHealthProfile(any());
        when(vaccinationDoseRepository.findAllByVaccinationRecordIdOrderByDoseNumberAsc(1L))
                .thenReturn(Arrays.asList(dose1, dose2, dose3));

        // When
        vaccinationService.administerDose(1L, request);

        // Then
        assertEquals(DoseStatus.COMPLETED, dose1.getStatus());
        assertEquals(actualDate, dose1.getDateAdministered());

        // Verify saveAll was called for future doses
        verify(vaccinationDoseRepository).saveAll(doseListCaptor.capture());
        List<VaccinationDose> updatedDoses = doseListCaptor.getValue();

        assertEquals(2, updatedDoses.size());

        // Mũi 2 phải được dịch sang: 06/01 + 30 ngày = 05/02
        assertEquals(LocalDate.of(2026, 2, 5), updatedDoses.get(0).getScheduledDate());

        // Mũi 3 phải được dịch sang: 06/01 + 60 ngày = 07/03
        assertEquals(LocalDate.of(2026, 3, 7), updatedDoses.get(1).getScheduledDate());
    }
}
