package com.carenest.backend.module.doctorverification;

import com.carenest.backend.common.exception.BadRequestException;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.auth.enums.Role;
import com.carenest.backend.module.auth.repository.UserRepository;
import com.carenest.backend.module.doctorverification.dto.request.RejectDoctorVerificationRequest;
import com.carenest.backend.module.doctorverification.dto.request.SubmitDoctorVerificationRequest;
import com.carenest.backend.module.doctorverification.dto.response.DoctorVerificationResponse;
import com.carenest.backend.module.doctorverification.entity.DoctorVerification;
import com.carenest.backend.module.doctorverification.enums.VerificationStatus;
import com.carenest.backend.module.doctorverification.repository.DoctorVerificationRepository;
import com.carenest.backend.module.doctorverification.service.impl.DoctorVerificationServiceImpl;
import com.carenest.backend.module.family.util.FamilySecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorVerificationServiceTest {

    @Mock
    private DoctorVerificationRepository doctorVerificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FamilySecurityUtil familySecurityUtil;

    @InjectMocks
    private DoctorVerificationServiceImpl doctorVerificationService;

    private User testUser;
    private SubmitDoctorVerificationRequest submitRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("doctor.test@carenest.com")
                .fullName("Test User")
                .role(Role.USER)
                .build();
        testUser.setId(1L);

        submitRequest = new SubmitDoctorVerificationRequest();
        submitRequest.setCertificationNumber("CERT-12345");
        submitRequest.setSpecialty("Pediatrics");
        submitRequest.setHospitalName("CareNest Children Hospital");
        submitRequest.setDocumentUrl("https://carenest.com/docs/cert.pdf");
    }

    @Test
    void submitRequest_shouldCreateNewRequest_whenNoneExists() {
        when(familySecurityUtil.getCurrentUser()).thenReturn(testUser);
        when(doctorVerificationRepository.existsByUserIdAndStatus(1L, VerificationStatus.PENDING)).thenReturn(false);
        when(doctorVerificationRepository.findByUserId(1L)).thenReturn(Optional.empty());

        DoctorVerification savedVerification = DoctorVerification.builder()
                .user(testUser)
                .certificationNumber(submitRequest.getCertificationNumber())
                .specialty(submitRequest.getSpecialty())
                .hospitalName(submitRequest.getHospitalName())
                .documentUrl(submitRequest.getDocumentUrl())
                .status(VerificationStatus.PENDING)
                .build();
        savedVerification.setId(10L);

        when(doctorVerificationRepository.save(any(DoctorVerification.class))).thenReturn(savedVerification);

        DoctorVerificationResponse response = doctorVerificationService.submitRequest(submitRequest);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals("doctor.test@carenest.com", response.getUserEmail());
        assertEquals(VerificationStatus.PENDING, response.getStatus());

        verify(doctorVerificationRepository).save(any(DoctorVerification.class));
    }

    @Test
    void submitRequest_shouldThrowBadRequest_whenUserIsAlreadyDoctor() {
        testUser.setRole(Role.DOCTOR);
        when(familySecurityUtil.getCurrentUser()).thenReturn(testUser);

        assertThrows(BadRequestException.class, () -> doctorVerificationService.submitRequest(submitRequest));
        verifyNoInteractions(doctorVerificationRepository);
    }

    @Test
    void submitRequest_shouldThrowBadRequest_whenRequestIsAlreadyPending() {
        when(familySecurityUtil.getCurrentUser()).thenReturn(testUser);
        when(doctorVerificationRepository.existsByUserIdAndStatus(1L, VerificationStatus.PENDING)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> doctorVerificationService.submitRequest(submitRequest));
        verify(doctorVerificationRepository, never()).save(any());
    }

    @Test
    void submitRequest_shouldUpdateExistingRequest_whenStatusIsRejected() {
        when(familySecurityUtil.getCurrentUser()).thenReturn(testUser);
        when(doctorVerificationRepository.existsByUserIdAndStatus(1L, VerificationStatus.PENDING)).thenReturn(false);

        DoctorVerification existingRejected = DoctorVerification.builder()
                .user(testUser)
                .certificationNumber("OLD-CERT")
                .specialty("General")
                .hospitalName("Old Hospital")
                .documentUrl("http://old.pdf")
                .status(VerificationStatus.REJECTED)
                .rejectionReason("Incomplete paperwork")
                .build();
        existingRejected.setId(10L);

        when(doctorVerificationRepository.findByUserId(1L)).thenReturn(Optional.of(existingRejected));
        when(doctorVerificationRepository.save(any(DoctorVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorVerificationResponse response = doctorVerificationService.submitRequest(submitRequest);

        assertNotNull(response);
        assertEquals(VerificationStatus.PENDING, response.getStatus());
        assertNull(response.getRejectionReason());
        assertEquals("CERT-12345", response.getCertificationNumber());

        verify(doctorVerificationRepository).save(existingRejected);
    }

    @Test
    void approveRequest_shouldPromoteUserToDoctor_whenPending() {
        DoctorVerification pendingVerification = DoctorVerification.builder()
                .user(testUser)
                .certificationNumber("CERT-12345")
                .status(VerificationStatus.PENDING)
                .build();
        pendingVerification.setId(10L);

        when(doctorVerificationRepository.findById(10L)).thenReturn(Optional.of(pendingVerification));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(doctorVerificationRepository.save(any(DoctorVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorVerificationResponse response = doctorVerificationService.approveRequest(10L);

        assertNotNull(response);
        assertEquals(VerificationStatus.APPROVED, response.getStatus());
        assertEquals(Role.DOCTOR, testUser.getRole());

        verify(userRepository).save(testUser);
        verify(doctorVerificationRepository).save(pendingVerification);
    }

    @Test
    void rejectRequest_shouldSaveRejectionReason_whenPending() {
        DoctorVerification pendingVerification = DoctorVerification.builder()
                .user(testUser)
                .certificationNumber("CERT-12345")
                .status(VerificationStatus.PENDING)
                .build();
        pendingVerification.setId(10L);

        RejectDoctorVerificationRequest rejectRequest = new RejectDoctorVerificationRequest();
        rejectRequest.setRejectionReason("Document is unreadable.");

        when(doctorVerificationRepository.findById(10L)).thenReturn(Optional.of(pendingVerification));
        when(doctorVerificationRepository.save(any(DoctorVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorVerificationResponse response = doctorVerificationService.rejectRequest(10L, rejectRequest);

        assertNotNull(response);
        assertEquals(VerificationStatus.REJECTED, response.getStatus());
        assertEquals("Document is unreadable.", response.getRejectionReason());
        assertEquals(Role.USER, testUser.getRole()); // Role unchanged

        verify(doctorVerificationRepository).save(pendingVerification);
        verifyNoInteractions(userRepository);
    }
}
