package com.carenest.backend.module.security;

import com.carenest.backend.module.appointment.dto.request.AppointmentCreateRequest;
import com.carenest.backend.module.appointment.mapper.AppointmentMapper;
import com.carenest.backend.module.appointment.repository.AppointmentRepository;
import com.carenest.backend.module.appointment.service.impl.AppointmentServiceImpl;
import com.carenest.backend.module.cabinet.dto.request.CabinetCreateRequest;
import com.carenest.backend.module.cabinet.mapper.CabinetMapper;
import com.carenest.backend.module.cabinet.repository.CabinetMedicineRepository;
import com.carenest.backend.module.cabinet.repository.MedicineCabinetRepository;
import com.carenest.backend.module.cabinet.service.impl.MedicineCabinetServiceImpl;
import com.carenest.backend.module.family.repository.FamilyMemberRepository;
import com.carenest.backend.module.family.repository.FamilyRepository;
import com.carenest.backend.module.family.util.FamilySecurityUtil;
import com.carenest.backend.module.growth.dto.request.GrowthRecordCreateRequest;
import com.carenest.backend.module.growth.mapper.GrowthMapper;
import com.carenest.backend.module.growth.repository.GrowthRecordRepository;
import com.carenest.backend.module.growth.service.WhoGrowthCalculatorService;
import com.carenest.backend.module.growth.service.impl.GrowthRecordServiceImpl;
import com.carenest.backend.module.healthprofile.dto.request.HealthProfileUpdateRequest;
import com.carenest.backend.module.healthprofile.mapper.HealthProfileMapper;
import com.carenest.backend.module.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.module.healthprofile.service.impl.HealthProfileServiceImpl;
import com.carenest.backend.module.notification.service.NotificationService;
import com.carenest.backend.module.auth.repository.UserRepository;
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
                .when(familySecurityUtil).checkUserBelongsToHealthProfile(99L);

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
                .when(familySecurityUtil).checkUserBelongsToHealthProfile(55L);

        assertThrows(
                AccessDeniedException.class,
                () -> healthProfileService.updateHealthProfile(55L, new HealthProfileUpdateRequest()));

        verifyNoInteractions(healthProfileRepository, healthProfileMapper, growthRecordRepository);
    }

    @Test
    void addGrowthRecord_deniesForeignProfileBeforeMutation() {
        doThrow(new AccessDeniedException("denied"))
                .when(familySecurityUtil).checkUserBelongsToHealthProfile(44L);

        assertThrows(
                AccessDeniedException.class,
                () -> growthRecordService.addRecord(44L, new GrowthRecordCreateRequest()));

        verifyNoInteractions(healthProfileRepository, growthRecordRepository, growthMapper);
    }
}
