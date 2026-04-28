package com.carenest.backend.module.medication.service.impl;

import com.carenest.backend.module.family.util.FamilySecurityUtil;
import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import com.carenest.backend.module.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.module.medication.dto.request.CreateMedicationRequest;
import com.carenest.backend.module.medication.dto.request.UpdateMedicationRequest;
import com.carenest.backend.module.medication.entity.Medication;
import com.carenest.backend.module.medication.entity.MedicationLog;
import com.carenest.backend.module.medication.enums.MedicationFrequency;
import com.carenest.backend.module.medication.mapper.MedicationMapper;
import com.carenest.backend.module.medication.repository.MedicationLogRepository;
import com.carenest.backend.module.medication.repository.MedicationRepository;
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
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedicationServiceImplTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private MedicationLogRepository medicationLogRepository;

    @Mock
    private HealthProfileRepository healthProfileRepository;

    @Mock
    private FamilySecurityUtil familySecurityUtil;

    @Mock
    private MedicationMapper medicationMapper;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private MedicationServiceImpl medicationService;

    @Captor
    private ArgumentCaptor<List<MedicationLog>> logsCaptor;

    private HealthProfile profile;

    @BeforeEach
    void setUp() {
        profile = HealthProfile.builder().build();
        profile.setId(1L);
    }

    @Test
    void createMedication_shouldLimitTo30Days_whenNoEndDate() {
        // Given
        CreateMedicationRequest request = new CreateMedicationRequest();
        request.setStartDate(LocalDate.now());
        // NO End Date -> Infinite schedule
        request.setEndDate(null);

        Medication mappedMedication = Medication.builder()
                .healthProfile(profile)
                .frequency(MedicationFrequency.DAILY)
                .startDate(LocalDate.now())
                .endDate(null)
                .timeSlots("08:00,20:00")
                .build();
        mappedMedication.setId(1L);

        doNothing().when(familySecurityUtil).checkUserBelongsToHealthProfile(any());
        when(healthProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(medicationMapper.toEntity(any())).thenReturn(mappedMedication);
        when(medicationRepository.save(any())).thenReturn(mappedMedication);
        when(medicationMapper.stringToList("08:00,20:00")).thenReturn(Arrays.asList("08:00", "20:00"));

        // When
        medicationService.createMedication(1L, request);

        // Then
        verify(medicationLogRepository).saveAll(logsCaptor.capture());
        List<MedicationLog> generatedLogs = logsCaptor.getValue();

        // 30 days * 2 timeslots (08:00 and 20:00) = maximum 60 logs
        // Actual might be slightly less if timeslots for TODAY have already passed.
        // But definitely should not be infinite and should cap around 60.
        assertTrue(generatedLogs.size() <= 62);
        assertTrue(generatedLogs.size() > 58); // Assert limit is working correctly
    }

    @Test
    void updateMedication_shouldDeleteFutureLogs_whenScheduleChanged() {
        // Given
        UpdateMedicationRequest request = new UpdateMedicationRequest();
        request.setFrequency(MedicationFrequency.EVERY_OTHER_DAY); // Changed from DAILY

        Medication existingMedication = Medication.builder()
                .healthProfile(profile)
                .frequency(MedicationFrequency.DAILY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .timeSlots("08:00")
                .build();
        existingMedication.setId(1L);

        when(medicationRepository.findById(1L)).thenReturn(Optional.of(existingMedication));
        doNothing().when(familySecurityUtil).checkUserBelongsToHealthProfile(any());
        when(medicationRepository.save(any())).thenReturn(existingMedication);
        when(medicationMapper.stringToList("08:00")).thenReturn(Arrays.asList("08:00"));

        // Dummy future logs
        MedicationLog futureLog = new MedicationLog();
        futureLog.setStatus(com.carenest.backend.module.medication.enums.MedicationLogStatus.PENDING);
        futureLog.setScheduledTime(LocalDate.now().plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(medicationLogRepository.findAllByMedicationId(1L)).thenReturn(Arrays.asList(futureLog));

        // When
        medicationService.updateMedication(1L, request);

        // Then
        // Verify delete was called with future logs
        verify(medicationLogRepository).deleteAll(anyList());
        
        // Verify regeneration logic triggered (saveAll called for new logs)
        verify(medicationLogRepository).saveAll(anyList());
    }
}
