package com.carenest.backend.features.booking.service.impl;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.booking.dto.request.SendConsultationMessageRequest;
import com.carenest.backend.features.booking.entity.BookingRequest;
import com.carenest.backend.features.booking.entity.ConsultationMessage;
import com.carenest.backend.features.booking.entity.ConsultationThread;
import com.carenest.backend.features.booking.enums.BookingRequestType;
import com.carenest.backend.features.booking.enums.BookingStatus;
import com.carenest.backend.features.booking.repository.BookingRequestRepository;
import com.carenest.backend.features.booking.repository.ConsultationMessageRepository;
import com.carenest.backend.features.booking.repository.ConsultationThreadRepository;
import com.carenest.backend.features.notification.enums.NotificationType;
import com.carenest.backend.features.notification.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultationMessageServiceImplTest {

    @Mock
    private ConsultationMessageRepository consultationMessageRepository;
    @Mock
    private ConsultationThreadRepository consultationThreadRepository;
    @Mock
    private BookingRequestRepository bookingRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ConsultationMessageServiceImpl consultationMessageService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sendMessage_notifiesOtherThreadParticipant() {
        User patient = user(1L, "patient@example.com", "Patient", Role.USER);
        User doctor = user(2L, "doctor@example.com", "Doctor", Role.DOCTOR);
        ConsultationThread thread = ConsultationThread.builder()
                .patient(patient)
                .doctor(doctor)
                .build();
        thread.setId(99L);
        BookingRequest booking = BookingRequest.builder()
                .patient(patient)
                .doctor(doctor)
                .thread(thread)
                .requestType(BookingRequestType.ONLINE_CHAT)
                .status(BookingStatus.APPROVED)
                .note("Need help")
                .build();
        booking.setId(501L);
        SendConsultationMessageRequest request = new SendConsultationMessageRequest();
        request.setContent("Xin chào bác sĩ");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(patient.getEmail(), null)
        );
        when(userRepository.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));
        when(consultationThreadRepository.findById(99L)).thenReturn(Optional.of(thread));
        when(bookingRequestRepository.findLatestThreadBookingForMessageGate(
                eq(99L),
                eq(BookingRequestType.ONLINE_CHAT),
                any()
        )).thenReturn(Optional.of(booking));
        when(consultationMessageRepository.save(any(ConsultationMessage.class))).thenAnswer(invocation -> {
            ConsultationMessage message = invocation.getArgument(0);
            message.setId(700L);
            return message;
        });

        consultationMessageService.sendMessage(99L, request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BookingStatus>> statusCaptor = ArgumentCaptor.forClass((Class<List<BookingStatus>>) (Class<?>) List.class);
        verify(bookingRequestRepository).findLatestThreadBookingForMessageGate(
                eq(99L),
                eq(BookingRequestType.ONLINE_CHAT),
                statusCaptor.capture()
        );
        List<BookingStatus> gateStatuses = statusCaptor.getValue();
        assertTrue(gateStatuses.containsAll(List.of(
                BookingStatus.APPROVED,
                BookingStatus.ACTIVE,
                BookingStatus.RESTRICTED,
                BookingStatus.COMPLETED
        )));
        assertFalse(gateStatuses.contains(BookingStatus.PENDING));
        assertFalse(gateStatuses.contains(BookingStatus.REJECTED));

        verify(notificationService).createNotificationForUser(
                eq(doctor),
                eq("Tin nhắn tư vấn mới"),
                contains("Patient: Xin chào bác sĩ"),
                eq(NotificationType.CHAT),
                eq("BOOKING_REQUEST"),
                eq(501L)
        );
    }

    private static User user(Long id, String email, String fullName, Role role) {
        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .role(role)
                .build();
        user.setId(id);
        return user;
    }
}
