package com.carenest.backend.features.booking.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.appointment.entity.Appointment;
import com.carenest.backend.features.appointment.enums.AppointmentStatus;
import com.carenest.backend.features.appointment.repository.AppointmentRepository;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.booking.dto.request.CancelBookingRequest;
import com.carenest.backend.features.booking.dto.request.ConfirmBookingScheduleRequest;
import com.carenest.backend.features.booking.dto.request.CreateBookingRequest;
import com.carenest.backend.features.booking.dto.request.RejectBookingRequest;
import com.carenest.backend.features.booking.dto.response.BookingResponse;
import com.carenest.backend.features.booking.dto.response.ConsultationThreadInboxResponse;
import com.carenest.backend.features.booking.dto.response.ConsultationThreadResponse;
import com.carenest.backend.features.booking.entity.BookingRequest;
import com.carenest.backend.features.booking.entity.ConsultationThread;
import com.carenest.backend.features.booking.enums.BookingRequestType;
import com.carenest.backend.features.booking.enums.BookingStatus;
import com.carenest.backend.features.booking.repository.BookingRequestRepository;
import com.carenest.backend.features.booking.repository.ConsultationThreadRepository;
import com.carenest.backend.features.booking.service.BookingService;
import com.carenest.backend.features.doctorverification.dto.response.DoctorSummaryResponse;
import com.carenest.backend.features.doctorverification.entity.DoctorVerification;
import com.carenest.backend.features.doctorverification.enums.VerificationStatus;
import com.carenest.backend.features.doctorverification.repository.DoctorVerificationRepository;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.features.notification.enums.NotificationType;
import com.carenest.backend.features.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private static final String BOOKING_WORKSPACE_REFERENCE = "BOOKING_WORKSPACE";
    private static final String BOOKING_HISTORY_REFERENCE = "BOOKING_HISTORY";

    private final BookingRequestRepository bookingRequestRepository;
    private final ConsultationThreadRepository consultationThreadRepository;
    private final UserRepository userRepository;
    private final DoctorVerificationRepository doctorVerificationRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final FamilySecurityUtil familySecurityUtil;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<DoctorSummaryResponse> getAvailableDoctors() {
        return doctorVerificationRepository.findAllByStatusOrderByUpdatedAtDesc(VerificationStatus.APPROVED).stream()
                .filter(verification -> verification.getUser() != null)
                .filter(verification -> verification.getUser().getRole() == Role.DOCTOR)
                .filter(verification -> Boolean.TRUE.equals(verification.getUser().getIsActive()))
                .map(this::toDoctorSummary)
                .toList();
    }

    @Override
    @Transactional
    public BookingResponse createBookingRequest(CreateBookingRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (patient.getId().equals(request.getDoctorId())) {
            throw new BadRequestException("Bệnh nhân không được phép đặt lịch với chính mình.");
        }

        // Security check for Health Profile
        familySecurityUtil.checkUserBelongsToHealthProfile(request.getHealthProfileId());
        HealthProfile healthProfile = healthProfileRepository.findByIdAndDeletedAtIsNull(request.getHealthProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", request.getHealthProfileId().toString()));

        // Check for active consultations if ONLINE_CHAT
        if (request.getRequestType() == BookingRequestType.ONLINE_CHAT) {
            bookingRequestRepository.findFirstByPatientIdAndDoctorIdAndRequestTypeAndStatusInOrderByCreatedAtDesc(
                patient.getId(),
                request.getDoctorId(),
                BookingRequestType.ONLINE_CHAT,
                List.of(BookingStatus.PENDING, BookingStatus.APPROVED, BookingStatus.ACTIVE, BookingStatus.RESTRICTED)
            ).ifPresent(activeBooking -> {
                throw new com.carenest.backend.core.exception.DuplicateActiveConsultationException(
                    "Bạn đã có một yêu cầu hoặc phiên tư vấn trực tuyến đang hoạt động với bác sĩ này.",
                    activeBooking.getId(),
                    activeBooking.getStatus()
                );
            });
        }

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getDoctorId().toString()));

        if (doctor.getRole() != Role.DOCTOR) {
            throw new BadRequestException("Tài khoản không phải là bác sĩ.");
        }

        if (!doctor.getIsActive()) {
            throw new BadRequestException("Bác sĩ hiện không hoạt động.");
        }

        boolean isVerified = doctorVerificationRepository.existsByUserIdAndStatus(doctor.getId(), VerificationStatus.APPROVED);
        if (!isVerified) {
            throw new BadRequestException("Bác sĩ chưa được phê duyệt hồ sơ xác thực.");
        }

        ConsultationThread thread = null;
        if (request.getRequestType() == BookingRequestType.ONLINE_CHAT) {
            thread = consultationThreadRepository.findByPatientAndDoctor(patient, doctor)
                    .orElseGet(() -> {
                        ConsultationThread newThread = ConsultationThread.builder()
                                .patient(patient)
                                .doctor(doctor)
                                .build();
                        return consultationThreadRepository.save(newThread);
                    });
        }

        BookingRequest bookingRequest = BookingRequest.builder()
                .patient(patient)
                .doctor(doctor)
                .healthProfile(healthProfile)
                .requestType(request.getRequestType())
                .status(BookingStatus.PENDING)
                .note(request.getNote())
                .preferredTimeNote(trimToNull(request.getPreferredTimeNote()))
                .thread(thread)
                .build();

        BookingRequest saved = bookingRequestRepository.save(bookingRequest);
        notifyBookingCreated(saved);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getDoctorBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (doctor.getRole() != Role.DOCTOR && doctor.getRole() != Role.ADMIN) {
            throw new BadRequestException("Tài khoản không có quyền.");
        }
        if (doctor.getRole() == Role.DOCTOR) {
            ensureDoctorIsApproved(doctor);
        }

        List<BookingRequest> requests;
        if (doctor.getRole() == Role.ADMIN) {
            requests = bookingRequestRepository.findAllByOrderByCreatedAtDesc();
        } else {
            requests = bookingRequestRepository.findAllByDoctorIdOrderByCreatedAtDesc(doctor.getId());
        }
        return requests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getPatientBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        List<BookingRequest> requests = bookingRequestRepository.findAllByPatientIdOrderByCreatedAtDesc(patient.getId());
        return requests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationThreadInboxResponse> getConsultationInbox() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        List<ConsultationThread> threads = consultationThreadRepository.findAllByPatientIdOrDoctorId(user.getId(), user.getId());
        List<ConsultationThreadInboxResponse> inbox = new java.util.ArrayList<>();
        
        for (ConsultationThread thread : threads) {
            bookingRequestRepository.findFirstByThreadIdAndRequestTypeAndStatusInOrderByCreatedAtDesc(
                thread.getId(),
                BookingRequestType.ONLINE_CHAT,
                List.of(BookingStatus.APPROVED, BookingStatus.ACTIVE, BookingStatus.COMPLETED, BookingStatus.RESTRICTED)
            ).ifPresent(latestBooking -> {
                inbox.add(ConsultationThreadInboxResponse.builder()
                        .id(thread.getId())
                        .latestBookingId(latestBooking.getId())
                        .patientId(thread.getPatient().getId())
                        .patientFullName(resolvePatientName(latestBooking))
                        .patientAvatarUrl(thread.getPatient().getAvatarUrl())
                        .doctorId(thread.getDoctor().getId())
                        .doctorFullName(resolveDoctorName(thread.getDoctor()))
                        .doctorAvatarUrl(thread.getDoctor().getAvatarUrl())
                        .status(latestBooking.getStatus())
                        .build());
            });
        }
        
        return inbox;
    }

    @Override
    @Transactional
    public BookingResponse approveBooking(Long id) {
        BookingRequest request = getBookingForTriage(id);

        if (request.getRequestType() == BookingRequestType.OFFLINE_CLINIC) {
            throw new BadRequestException("Lịch khám trực tiếp bắt buộc phải được xác nhận với thời gian cụ thể (sử dụng confirmSchedule).");
        }

        // Guard: block if an APPROVED, ACTIVE or RESTRICTED online chat session already exists for this patient-doctor pair
        if (request.getRequestType() == BookingRequestType.ONLINE_CHAT) {
            bookingRequestRepository.findFirstByPatientIdAndDoctorIdAndRequestTypeAndStatusInOrderByCreatedAtDesc(
                request.getPatient().getId(),
                request.getDoctor().getId(),
                BookingRequestType.ONLINE_CHAT,
                List.of(BookingStatus.APPROVED, BookingStatus.ACTIVE, BookingStatus.RESTRICTED)
            ).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BadRequestException(
                        "Bệnh nhân này đã có một phiên tư vấn trực tuyến đang hoạt động (ID: " + existing.getId() + "). Vui lòng đóng phiên cũ trước khi chấp nhận yêu cầu mới."
                    );
                }
            });
        }

        request.setStatus(BookingStatus.APPROVED);
        BookingRequest saved = bookingRequestRepository.save(request);
        notifyPatient(
                saved,
                "Yêu cầu tư vấn đã được chấp nhận",
                "Bác sĩ " + resolveDoctorName(saved.getDoctor()) + " đã chấp nhận yêu cầu tư vấn trực tuyến của bạn."
        );
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse confirmSchedule(Long bookingId, ConfirmBookingScheduleRequest requestPayload) {
        User currentUser = familySecurityUtil.getCurrentUser();
        BookingRequest booking = getBookingForDoctorAction(bookingId, currentUser);

        if (booking.getStatus() == BookingStatus.REJECTED
                || booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Không thể xác nhận lịch cho yêu cầu hiện tại");
        }
        if (requestPayload.getScheduledAt() == null) {
            throw new BadRequestException("scheduledAt is required");
        }
        if (requestPayload.getScheduledAt().isBefore(Instant.now())) {
            throw new BadRequestException("Thời gian hẹn không được ở quá khứ");
        }
        if (booking.getRequestType() == BookingRequestType.OFFLINE_CLINIC
                && trimToNull(requestPayload.getConfirmedLocation()) == null) {
            throw new BadRequestException("Khám trực tiếp cần có địa điểm hoặc phòng khám");
        }

        // Apply same guard as approveBooking for ONLINE_CHAT
        if (booking.getRequestType() == BookingRequestType.ONLINE_CHAT) {
            bookingRequestRepository.findFirstByPatientIdAndDoctorIdAndRequestTypeAndStatusInOrderByCreatedAtDesc(
                booking.getPatient().getId(),
                booking.getDoctor().getId(),
                BookingRequestType.ONLINE_CHAT,
                List.of(BookingStatus.APPROVED, BookingStatus.ACTIVE, BookingStatus.RESTRICTED)
            ).ifPresent(existing -> {
                if (!existing.getId().equals(bookingId)) {
                    throw new BadRequestException(
                        "Bệnh nhân này đã có một phiên tư vấn trực tuyến đang hoạt động (ID: " + existing.getId() + "). Vui lòng đóng phiên cũ trước khi xác nhận lịch mới."
                    );
                }
            });
        }

        booking.setStatus(BookingStatus.APPROVED);
        booking.setScheduledAt(requestPayload.getScheduledAt());
        booking.setConfirmedLocation(trimToNull(requestPayload.getConfirmedLocation()));
        booking.setConfirmedNote(trimToNull(requestPayload.getConfirmedNote()));
        booking.setRejectReason(null);
        booking.setCancellationReason(null);

        Appointment appointment = booking.getAppointment();
        if (appointment == null) {
            appointment = Appointment.builder()
                    .healthProfile(booking.getHealthProfile())
                    .build();
        }

        appointment.setHealthProfile(booking.getHealthProfile());
        appointment.setDoctorName(resolveDoctorName(booking.getDoctor()));
        appointment.setHospitalName(resolveHospitalName(booking));
        appointment.setAddress(trimToNull(requestPayload.getConfirmedLocation()));
        appointment.setAppointmentDate(requestPayload.getScheduledAt());
        appointment.setNotes(buildAppointmentNotes(booking, requestPayload));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReminderSent(false);

        booking.setAppointment(appointmentRepository.save(appointment));
        BookingRequest saved = bookingRequestRepository.save(booking);
        notifyPatient(
                saved,
                "Lịch khám đã được xác nhận",
                "Bác sĩ " + resolveDoctorName(saved.getDoctor()) + " đã xác nhận lịch hẹn của bạn."
        );
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long id, RejectBookingRequest payload) {
        BookingRequest request = getBookingForTriage(id);
        request.setStatus(BookingStatus.REJECTED);
        request.setRejectReason(payload.getRejectionReason());
        request.setScheduledAt(null);
        request.setConfirmedLocation(null);
        request.setConfirmedNote(null);
        
        if (request.getAppointment() != null) {
            Appointment appointment = request.getAppointment();
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
        }

        BookingRequest saved = bookingRequestRepository.save(request);
        notifyPatient(
                saved,
                "Yêu cầu đặt lịch bị từ chối",
                "Bác sĩ " + resolveDoctorName(saved.getDoctor()) + " đã từ chối yêu cầu đặt lịch của bạn."
        );
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, CancelBookingRequest request) {
        User currentUser = familySecurityUtil.getCurrentUser();
        BookingRequest booking = bookingRequestRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("BookingRequest", "id", bookingId.toString()));

        boolean canCancel = currentUser.getRole() == Role.ADMIN
                || booking.getPatient().getId().equals(currentUser.getId())
                || (currentUser.getRole() == Role.DOCTOR && booking.getDoctor().getId().equals(currentUser.getId()));
        if (!canCancel) {
            throw new AccessDeniedException("Bạn không có quyền hủy yêu cầu này");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Không thể hủy yêu cầu đã hoàn tất");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(trimToNull(request.getCancellationReason()));

        if (booking.getAppointment() != null) {
            Appointment appointment = booking.getAppointment();
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
        }

        BookingRequest saved = bookingRequestRepository.save(booking);
        notifyBookingCancelled(saved, currentUser);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse completeConsultation(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        ensureDoctorIsApproved(doctor);

        BookingRequest request = bookingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BookingRequest", "id", id.toString()));

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new BadRequestException("Bạn không có quyền kết thúc phiên này.");
        }

        if (request.getStatus() == BookingStatus.COMPLETED || request.getStatus() == BookingStatus.REJECTED) {
            throw new BadRequestException("Phiên này đã kết thúc.");
        }

        request.setStatus(BookingStatus.COMPLETED);
        
        if (request.getAppointment() != null) {
            Appointment appointment = request.getAppointment();
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
        }
        
        BookingRequest saved = bookingRequestRepository.save(request);
        notifyPatient(
                saved,
                "Phiên tư vấn đã hoàn tất",
                "Bác sĩ " + resolveDoctorName(saved.getDoctor()) + " đã đánh dấu phiên tư vấn là hoàn tất."
        );
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse restrictMessaging(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        ensureDoctorIsApproved(doctor);

        BookingRequest request = bookingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BookingRequest", "id", id.toString()));

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new BadRequestException("Bạn không có quyền hạn chế phiên tư vấn này.");
        }

        if (request.getStatus() != BookingStatus.APPROVED && request.getStatus() != BookingStatus.ACTIVE) {
            throw new BadRequestException("Chỉ có thể hạn chế nhắn tin khi phiên đang hoạt động.");
        }

        request.setStatus(BookingStatus.RESTRICTED);
        BookingRequest saved = bookingRequestRepository.save(request);
        notifyPatient(
                saved,
                "Phiên tư vấn bị hạn chế nhắn tin",
                "Bác sĩ " + resolveDoctorName(saved.getDoctor()) + " đã tạm hạn chế nhắn tin trong phiên tư vấn."
        );
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse unrestrictMessaging(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        ensureDoctorIsApproved(doctor);

        BookingRequest request = bookingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BookingRequest", "id", id.toString()));

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new BadRequestException("Bạn không có quyền hủy hạn chế phiên tư vấn này.");
        }

        if (request.getStatus() != BookingStatus.RESTRICTED) {
            throw new BadRequestException("Chỉ có thể hủy hạn chế khi phiên đang bị hạn chế.");
        }

        request.setStatus(BookingStatus.ACTIVE);
        BookingRequest saved = bookingRequestRepository.save(request);
        notifyPatient(
                saved,
                "Phiên tư vấn đã mở lại nhắn tin",
                "Bác sĩ " + resolveDoctorName(saved.getDoctor()) + " đã mở lại nhắn tin trong phiên tư vấn."
        );
        return mapToResponse(saved);
    }

    private BookingRequest getBookingForTriage(Long bookingId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        ensureDoctorIsApproved(doctor);

        BookingRequest request = bookingRequestRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("BookingRequest", "id", bookingId.toString()));

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new BadRequestException("Bạn không có quyền xử lý yêu cầu này.");
        }

        if (request.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể xử lý các yêu cầu đang chờ (PENDING).");
        }

        return request;
    }

    private BookingRequest getBookingForDoctorAction(Long bookingId, User currentUser) {
        if (currentUser.getRole() != Role.DOCTOR && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Chỉ bác sĩ mới có thể xử lý yêu cầu đặt lịch");
        }
        if (currentUser.getRole() == Role.DOCTOR) {
            ensureDoctorIsApproved(currentUser);
        }

        BookingRequest booking = bookingRequestRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("BookingRequest", "id", bookingId.toString()));

        if (currentUser.getRole() == Role.DOCTOR && !booking.getDoctor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không phụ trách yêu cầu đặt lịch này");
        }

        return booking;
    }

    @Override
    @Transactional
    public ConsultationThreadResponse provisionConsultationThread(Long bookingId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        BookingRequest request = bookingRequestRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("BookingRequest", "id", bookingId.toString()));

        if (!request.getPatient().getId().equals(currentUser.getId()) && !request.getDoctor().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn không có quyền truy cập phòng tư vấn này.");
        }

        if (request.getRequestType() != BookingRequestType.ONLINE_CHAT) {
            throw new BadRequestException("Phòng tư vấn chỉ khả dụng cho yêu cầu tư vấn trực tuyến.");
        }

        if (request.getStatus() != BookingStatus.APPROVED
                && request.getStatus() != BookingStatus.ACTIVE
                && request.getStatus() != BookingStatus.COMPLETED
                && request.getStatus() != BookingStatus.RESTRICTED) {
            throw new BadRequestException("Phòng tư vấn không khả dụng với trạng thái này.");
        }

        ConsultationThread thread = request.getThread();
        if (thread == null) {
            throw new BadRequestException("Yêu cầu tư vấn chưa có phòng chat. Vui lòng thử lại sau.");
        }

        return ConsultationThreadResponse.builder()
                .id(thread.getId())
                .bookingRequestId(request.getId())
                .patientId(request.getPatient().getId())
                .patientFullName(resolvePatientName(request))
                .patientAvatarUrl(request.getPatient().getAvatarUrl())
                .doctorId(request.getDoctor().getId())
                .doctorFullName(resolveDoctorName(request.getDoctor()))
                .doctorAvatarUrl(request.getDoctor().getAvatarUrl())
                .status(request.getStatus())
                .build();
    }

    private void notifyBookingCreated(BookingRequest booking) {
        String patientName = resolvePatientName(booking);
        String requestLabel = booking.getRequestType() == BookingRequestType.ONLINE_CHAT
                ? "tư vấn trực tuyến"
                : "khám trực tiếp";
        notificationService.createNotificationForUser(
                booking.getDoctor(),
                "Có yêu cầu " + requestLabel + " mới",
                patientName + " vừa gửi yêu cầu " + requestLabel + ".",
                NotificationType.APPOINTMENT,
                BOOKING_WORKSPACE_REFERENCE,
                booking.getId()
        );
    }

    private void notifyPatient(BookingRequest booking, String title, String message) {
        notificationService.createNotificationForUser(
                booking.getPatient(),
                title,
                message,
                NotificationType.APPOINTMENT,
                BOOKING_HISTORY_REFERENCE,
                booking.getId()
        );
    }

    private void notifyDoctor(BookingRequest booking, String title, String message) {
        notificationService.createNotificationForUser(
                booking.getDoctor(),
                title,
                message,
                NotificationType.APPOINTMENT,
                BOOKING_WORKSPACE_REFERENCE,
                booking.getId()
        );
    }

    private void notifyBookingCancelled(BookingRequest booking, User actor) {
        boolean cancelledByPatient = booking.getPatient().getId().equals(actor.getId());
        if (cancelledByPatient) {
            notifyDoctor(
                    booking,
                    "Bệnh nhân đã hủy lịch",
                    resolvePatientName(booking) + " đã hủy yêu cầu đặt lịch."
            );
            return;
        }

        notifyPatient(
                booking,
                "Yêu cầu đặt lịch đã bị hủy",
                "Yêu cầu đặt lịch với bác sĩ " + resolveDoctorName(booking.getDoctor()) + " đã bị hủy."
        );
    }

    private DoctorSummaryResponse toDoctorSummary(DoctorVerification verification) {
        User user = verification.getUser();
        return DoctorSummaryResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .certificationNumber(maskCertificationNumber(verification.getCertificationNumber()))
                .specialty(verification.getSpecialty())
                .hospitalName(verification.getHospitalName())
                .documentUrl(null)
                .approvedAt(verification.getUpdatedAt())
                .build();
    }

    private void ensureDoctorIsApproved(User doctor) {
        if (doctor.getRole() != Role.DOCTOR) {
            throw new AccessDeniedException("Chỉ bác sĩ mới có thể thao tác trong khu vực phòng khám");
        }
        if (!Boolean.TRUE.equals(doctor.getIsActive())) {
            throw new AccessDeniedException("Tài khoản bác sĩ đã bị khóa");
        }
        boolean approved = doctorVerificationRepository.existsByUserIdAndStatus(doctor.getId(), VerificationStatus.APPROVED);
        if (!approved) {
            throw new AccessDeniedException("Hồ sơ bác sĩ chưa được phê duyệt hoặc đã bị thu hồi");
        }
    }

    private BookingResponse mapToResponse(BookingRequest request) {
        DoctorVerification verification = doctorVerificationRepository.findByUserId(request.getDoctor().getId()).orElse(null);
        return BookingResponse.builder()
                .id(request.getId())
                .patientId(request.getPatient().getId())
                .patientFullName(resolvePatientName(request))
                .patientAvatarUrl(request.getPatient().getAvatarUrl())
                .doctorId(request.getDoctor().getId())
                .doctorFullName(resolveDoctorName(request.getDoctor()))
                .doctorAvatarUrl(request.getDoctor().getAvatarUrl())
                .doctorSpecialty(verification != null ? verification.getSpecialty() : null)
                .doctorHospitalName(verification != null ? verification.getHospitalName() : null)
                .healthProfileId(request.getHealthProfile() != null ? request.getHealthProfile().getId() : null)
                .healthProfileName(request.getHealthProfile() != null ? request.getHealthProfile().getFullName() : null)
                .requestType(request.getRequestType())
                .status(request.getStatus())
                .note(request.getNote())
                .preferredTimeNote(request.getPreferredTimeNote())
                .rejectReason(request.getRejectReason())
                .scheduledAt(request.getScheduledAt())
                .confirmedLocation(request.getConfirmedLocation())
                .confirmedNote(request.getConfirmedNote())
                .cancellationReason(request.getCancellationReason())
                .appointmentId(request.getAppointment() != null ? request.getAppointment().getId() : null)
                .appointmentStatus(request.getAppointment() != null ? request.getAppointment().getStatus().name() : null)
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    private String buildAppointmentNotes(BookingRequest booking, ConfirmBookingScheduleRequest request) {
        StringBuilder builder = new StringBuilder();
        if (booking.getRequestType() == BookingRequestType.ONLINE_CHAT) {
            builder.append("Lịch tư vấn trực tuyến");
        } else {
            builder.append("Lịch khám trực tiếp");
        }
        if (booking.getNote() != null && !booking.getNote().isBlank()) {
            builder.append(" | Nhu cầu: ").append(booking.getNote().trim());
        }
        if (request.getConfirmedNote() != null && !request.getConfirmedNote().isBlank()) {
            builder.append(" | Ghi chú bác sĩ: ").append(request.getConfirmedNote().trim());
        }
        return builder.toString();
    }

    private String resolveHospitalName(BookingRequest booking) {
        DoctorVerification verification = doctorVerificationRepository.findByUserId(booking.getDoctor().getId()).orElse(null);
        if (booking.getRequestType() == BookingRequestType.ONLINE_CHAT) {
            return "Tư vấn trực tuyến";
        }
        if (booking.getConfirmedLocation() != null && !booking.getConfirmedLocation().isBlank()) {
            return booking.getConfirmedLocation().trim();
        }
        if (verification != null && verification.getHospitalName() != null && !verification.getHospitalName().isBlank()) {
            return verification.getHospitalName().trim();
        }
        return "Lịch khám bác sĩ";
    }

    private String resolveDoctorName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName().trim();
        }
        return user.getEmail();
    }

    private String resolvePatientName(BookingRequest booking) {
        if (booking.getHealthProfile() != null) {
            String profileName = booking.getHealthProfile().getFullName();
            if (profileName != null && !profileName.isBlank()) {
                return profileName.trim();
            }
        }
        if (booking.getPatient().getFullName() != null && !booking.getPatient().getFullName().isBlank()) {
            return booking.getPatient().getFullName().trim();
        }
        return booking.getPatient().getEmail();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String maskCertificationNumber(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 4) {
            return "*".repeat(trimmed.length());
        }
        return "*".repeat(trimmed.length() - 4) + trimmed.substring(trimmed.length() - 4);
    }
}
