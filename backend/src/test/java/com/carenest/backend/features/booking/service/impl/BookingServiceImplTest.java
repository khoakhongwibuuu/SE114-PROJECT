package com.carenest.backend.features.booking.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.features.appointment.repository.AppointmentRepository;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.booking.dto.request.ConfirmBookingScheduleRequest;
import com.carenest.backend.features.booking.entity.BookingRequest;
import com.carenest.backend.features.booking.enums.BookingRequestType;
import com.carenest.backend.features.booking.enums.BookingStatus;
import com.carenest.backend.features.booking.repository.BookingRequestRepository;
import com.carenest.backend.features.booking.repository.ConsultationThreadRepository;
import com.carenest.backend.features.doctorverification.dto.response.DoctorSummaryResponse;
import com.carenest.backend.features.doctorverification.entity.DoctorVerification;
import com.carenest.backend.features.doctorverification.enums.VerificationStatus;
import com.carenest.backend.features.doctorverification.repository.DoctorVerificationRepository;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.features.notification.enums.NotificationType;
import com.carenest.backend.features.notification.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRequestRepository bookingRequestRepository;
    @Mock
    private ConsultationThreadRepository consultationThreadRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DoctorVerificationRepository doctorVerificationRepository;
    @Mock
    private HealthProfileRepository healthProfileRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private FamilySecurityUtil familySecurityUtil;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void confirmSchedule_rejectsPastScheduledAtBeforeAppointmentMutation() {
        User doctor = doctor();
        BookingRequest booking = pendingBooking(doctor, BookingRequestType.OFFLINE_CLINIC);
        ConfirmBookingScheduleRequest request = new ConfirmBookingScheduleRequest();
        request.setScheduledAt(Instant.now().minusSeconds(60));
        request.setConfirmedLocation("Clinic A");

        when(familySecurityUtil.getCurrentUser()).thenReturn(doctor);
        when(doctorVerificationRepository.existsByUserIdAndStatus(10L, VerificationStatus.APPROVED)).thenReturn(true);
        when(bookingRequestRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BadRequestException.class, () -> bookingService.confirmSchedule(1L, request));

        verifyNoInteractions(appointmentRepository);
        verify(bookingRequestRepository, never()).save(any());
    }

    @Test
    void confirmSchedule_requiresLocationForOfflineClinicBeforeAppointmentMutation() {
        User doctor = doctor();
        BookingRequest booking = pendingBooking(doctor, BookingRequestType.OFFLINE_CLINIC);
        ConfirmBookingScheduleRequest request = new ConfirmBookingScheduleRequest();
        request.setScheduledAt(Instant.now().plusSeconds(3600));
        request.setConfirmedLocation("   ");

        when(familySecurityUtil.getCurrentUser()).thenReturn(doctor);
        when(doctorVerificationRepository.existsByUserIdAndStatus(10L, VerificationStatus.APPROVED)).thenReturn(true);
        when(bookingRequestRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BadRequestException.class, () -> bookingService.confirmSchedule(1L, request));

        verifyNoInteractions(appointmentRepository);
        verify(bookingRequestRepository, never()).save(any());
    }

    @Test
    void getAvailableDoctors_returnsOnlyActiveApprovedDoctors() {
        User activeDoctor = doctor();
        activeDoctor.setIsActive(true);

        User inactiveDoctor = doctor();
        inactiveDoctor.setId(11L);
        inactiveDoctor.setEmail("inactive@example.com");
        inactiveDoctor.setIsActive(false);

        User nonDoctor = doctor();
        nonDoctor.setId(12L);
        nonDoctor.setEmail("user@example.com");
        nonDoctor.setRole(Role.USER);
        nonDoctor.setIsActive(true);

        when(doctorVerificationRepository.findAllByStatusOrderByUpdatedAtDesc(VerificationStatus.APPROVED))
                .thenReturn(List.of(
                        approvedVerification(activeDoctor, "CERT-123456"),
                        approvedVerification(inactiveDoctor, "CERT-INACTIVE"),
                        approvedVerification(nonDoctor, "CERT-USER")
                ));

        List<DoctorSummaryResponse> response = bookingService.getAvailableDoctors();

        assertEquals(1, response.size());
        assertEquals(10L, response.get(0).getId());
        assertEquals("*******3456", response.get(0).getCertificationNumber());
    }

    @Test
    void getDoctorBookings_rejectsDoctorWithoutApprovedVerification() {
        User doctor = doctor();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(doctor.getEmail(), null)
        );

        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctor));
        when(doctorVerificationRepository.existsByUserIdAndStatus(10L, VerificationStatus.APPROVED)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> bookingService.getDoctorBookings());

        verify(bookingRequestRepository, never()).findAllByDoctorIdOrderByCreatedAtDesc(any());
    }

    @Test
    void approveBooking_notifiesPatientWhenOnlineChatIsAccepted() {
        User doctor = doctor();
        BookingRequest booking = pendingBooking(doctor, BookingRequestType.ONLINE_CHAT);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(doctor.getEmail(), null)
        );

        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctor));
        when(doctorVerificationRepository.existsByUserIdAndStatus(10L, VerificationStatus.APPROVED)).thenReturn(true);
        when(bookingRequestRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRequestRepository.findFirstByPatientIdAndDoctorIdAndRequestTypeAndStatusInOrderByCreatedAtDesc(
                eq(20L),
                eq(10L),
                eq(BookingRequestType.ONLINE_CHAT),
                any()
        )).thenReturn(Optional.empty());
        when(bookingRequestRepository.save(booking)).thenReturn(booking);
        when(doctorVerificationRepository.findByUserId(10L)).thenReturn(Optional.empty());

        bookingService.approveBooking(1L);

        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        verify(notificationService).createNotificationForUser(
                eq(booking.getPatient()),
                contains("chấp nhận"),
                contains("Doctor"),
                eq(NotificationType.APPOINTMENT),
                eq("BOOKING_REQUEST"),
                eq(1L)
        );
    }

    private static User doctor() {
        User doctor = User.builder()
                .email("doctor@example.com")
                .fullName("Doctor")
                .role(Role.DOCTOR)
                .build();
        doctor.setId(10L);
        return doctor;
    }

    private static BookingRequest pendingBooking(User doctor, BookingRequestType type) {
        User patient = User.builder()
                .email("patient@example.com")
                .fullName("Patient")
                .role(Role.USER)
                .build();
        patient.setId(20L);

        BookingRequest booking = BookingRequest.builder()
                .patient(patient)
                .doctor(doctor)
                .requestType(type)
                .status(BookingStatus.PENDING)
                .note("Need help")
                .build();
        booking.setId(1L);
        return booking;
    }

    private static DoctorVerification approvedVerification(User user, String certificationNumber) {
        return DoctorVerification.builder()
                .user(user)
                .certificationNumber(certificationNumber)
                .specialty("Pediatrics")
                .hospitalName("CareNest Clinic")
                .status(VerificationStatus.APPROVED)
                .build();
    }
}
