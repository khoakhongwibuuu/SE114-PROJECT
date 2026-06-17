package com.carenest.backend.features.growth.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import com.carenest.backend.features.growth.dto.request.GrowthRecordCreateRequest;
import com.carenest.backend.features.growth.mapper.GrowthMapper;
import com.carenest.backend.features.growth.repository.GrowthRecordRepository;
import com.carenest.backend.features.growth.service.WhoGrowthCalculatorService;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.features.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrowthRecordServiceImplTest {

    @Mock
    private GrowthRecordRepository growthRecordRepository;
    @Mock
    private HealthProfileRepository healthProfileRepository;
    @Mock
    private GrowthMapper growthMapper;
    @Mock
    private WhoGrowthCalculatorService whoGrowthCalculatorService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private FamilyMemberRepository familyMemberRepository;
    @Mock
    private FamilySecurityUtil familySecurityUtil;

    @InjectMocks
    private GrowthRecordServiceImpl growthRecordService;

    @Test
    void addRecord_rejectsFutureRecordDate() {
        HealthProfile profile = HealthProfile.builder()
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .build();
        GrowthRecordCreateRequest request = GrowthRecordCreateRequest.builder()
                .recordDate(LocalDate.now().plusDays(1))
                .build();

        when(healthProfileRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(profile));

        assertThrows(BadRequestException.class, () -> growthRecordService.addRecord(1L, request));

        verifyNoInteractions(growthRecordRepository, growthMapper);
    }

    @Test
    void addRecord_rejectsRecordDateBeforeBirthDate() {
        HealthProfile profile = HealthProfile.builder()
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .build();
        GrowthRecordCreateRequest request = GrowthRecordCreateRequest.builder()
                .recordDate(LocalDate.of(2019, 12, 31))
                .build();

        when(healthProfileRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(profile));

        assertThrows(BadRequestException.class, () -> growthRecordService.addRecord(1L, request));

        verifyNoInteractions(growthRecordRepository, growthMapper);
    }
}
