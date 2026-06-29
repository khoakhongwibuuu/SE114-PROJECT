package com.carenest.backend.features.security;

import com.carenest.backend.features.appointment.dto.request.AppointmentCreateRequest;
import com.carenest.backend.features.appointment.mapper.AppointmentMapper;
import com.carenest.backend.features.appointment.repository.AppointmentRepository;
import com.carenest.backend.features.appointment.service.impl.AppointmentServiceImpl;
import com.carenest.backend.features.cabinet.dto.request.CabinetCreateRequest;
import com.carenest.backend.features.cabinet.mapper.CabinetMapper;
import com.carenest.backend.features.cabinet.repository.CabinetMedicineRepository;
import com.carenest.backend.features.cabinet.repository.MedicineCabinetRepository;
import com.carenest.backend.features.cabinet.service.impl.MedicineCabinetServiceImpl;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.carenest.backend.features.family.repository.FamilyRepository;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import com.carenest.backend.features.growth.dto.request.GrowthRecordCreateRequest;
import com.carenest.backend.features.growth.mapper.GrowthMapper;
import com.carenest.backend.features.growth.repository.GrowthRecordRepository;
import com.carenest.backend.features.growth.service.WhoGrowthCalculatorService;
import com.carenest.backend.features.growth.service.impl.GrowthRecordServiceImpl;
import com.carenest.backend.features.healthprofile.dto.request.HealthProfileUpdateRequest;
import com.carenest.backend.features.healthprofile.mapper.HealthProfileMapper;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.features.healthprofile.service.impl.HealthProfileServiceImpl;
import com.carenest.backend.features.notification.service.NotificationService;
import com.carenest.backend.features.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class FamilyAuthorizationContractTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private HealthProfileRepository healthProfileRepository;
    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private MedicineCabinetRepository cabinetRepository;
    @Mock
    private CabinetMedicineRepository cabinetMedicineRepository;
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private CabinetMapper cabinetMapper;

    @Mock
    private UserRepository userRepository;
    @Mock
    private HealthProfileMapper healthProfileMapper;
    @Mock
    private GrowthRecordRepository growthRecordRepository;

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
    private AppointmentServiceImpl appointmentService;
    @InjectMocks
    private MedicineCabinetServiceImpl medicineCabinetService;
    @InjectMocks
    private HealthProfileServiceImpl healthProfileService;
    @InjectMocks
    private GrowthRecordServiceImpl growthRecordService;

    @Test
    void createAppointment_deniesForeignProfileBeforeMutation() {
        AppointmentCreateRequest request = new AppointmentCreateRequest();
        request.setHealthProfileId(99L);
        doThrow(new AccessDeniedException("denied"))
                .when(familySecurityUtil).checkCanWriteHealthProfile(99L);

        assertThrows(AccessDeniedException.class, () -> appointmentService.createAppointment(request));

        verifyNoInteractions(appointmentRepository, appointmentMapper);
    }

    @Test
    void createCabinet_deniesForeignFamilyBeforeMutation() {
        CabinetCreateRequest request = new CabinetCreateRequest();
        request.setFamilyId(77L);
        doThrow(new AccessDeniedException("denied"))
                .when(familySecurityUtil).checkUserBelongsToFamily(77L);

        assertThrows(AccessDeniedException.class, () -> medicineCabinetService.createCabinet(request));

        verifyNoInteractions(cabinetRepository, familyRepository, cabinetMapper);
    }

    @Test
    void updateHealthProfile_deniesForeignProfileBeforeMutation() {
        doThrow(new AccessDeniedException("denied"))
                .when(familySecurityUtil).checkCanWriteHealthProfile(55L);

        assertThrows(
                AccessDeniedException.class,
                () -> healthProfileService.updateHealthProfile(55L, new HealthProfileUpdateRequest()));

        verifyNoInteractions(healthProfileRepository, healthProfileMapper, growthRecordRepository);
    }

    @Test
    void addGrowthRecord_deniesForeignProfileBeforeMutation() {
        doThrow(new AccessDeniedException("denied"))
                .when(familySecurityUtil).checkCanWriteHealthProfile(44L);

        assertThrows(
                AccessDeniedException.class,
                () -> growthRecordService.addRecord(44L, new GrowthRecordCreateRequest()));

        verifyNoInteractions(healthProfileRepository, growthRecordRepository, growthMapper);
    }
}
