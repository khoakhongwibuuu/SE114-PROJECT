package com.carenest.backend.features.booking.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
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
import com.carenest.backend.features.doctorverification.enums.VerificationStatus;
import com.carenest.backend.features.doctorverification.repository.DoctorVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRequestRepository bookingRequestRepository;
    private final ConsultationThreadRepository consultationThreadRepository;
    private final UserRepository userRepository;
    private final DoctorVerificationRepository doctorVerificationRepository;

    @Override
    @Transactional
    public BookingResponse createBookingRequest(CreateBookingRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (patient.getId().equals(request.getDoctorId())) {
            throw new BadRequestException("Bệnh nhân không được phép đặt lịch với chính mình.");
        }

        // Check for active consultations
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
                .requestType(request.getRequestType())
                .status(BookingStatus.PENDING)
                .note(request.getNote())
                .preferredTimeNote(request.getPreferredTimeNote())
                .thread(thread)
                .build();

        BookingRequest saved = bookingRequestRepository.save(bookingRequest);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getDoctorBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (doctor.getRole() != Role.DOCTOR) {
            throw new BadRequestException("Tài khoản không phải là bác sĩ.");
        }

        List<BookingRequest> requests = bookingRequestRepository.findAllByDoctorIdOrderByCreatedAtDesc(doctor.getId());
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
                        .patientFullName(thread.getPatient().getFullName())
                        .patientAvatarUrl(thread.getPatient().getAvatarUrl())
                        .doctorId(thread.getDoctor().getId())
                        .doctorFullName(thread.getDoctor().getFullName())
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
        return mapToResponse(bookingRequestRepository.save(request));
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long id, RejectBookingRequest payload) {
        BookingRequest request = getBookingForTriage(id);
        request.setStatus(BookingStatus.REJECTED);
        request.setRejectReason(payload.getReason());
        return mapToResponse(bookingRequestRepository.save(request));
    }

    @Override
    @Transactional
    public BookingResponse completeConsultation(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        BookingRequest request = bookingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BookingRequest", "id", id.toString()));

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new BadRequestException("Bạn không có quyền kết thúc phiên tư vấn này.");
        }

        if (request.getStatus() == BookingStatus.COMPLETED || request.getStatus() == BookingStatus.REJECTED) {
            throw new BadRequestException("Phiến tư vấn này đã kết thúc.");
        }

        request.setStatus(BookingStatus.COMPLETED);
        return mapToResponse(bookingRequestRepository.save(request));
    }

    @Override
    @Transactional
    public BookingResponse restrictMessaging(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        BookingRequest request = bookingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BookingRequest", "id", id.toString()));

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new BadRequestException("Bạn không có quyền hạn chế phiên tư vấn này.");
        }

        if (request.getStatus() != BookingStatus.APPROVED && request.getStatus() != BookingStatus.ACTIVE) {
            throw new BadRequestException("Chỉ có thể hạn chế nhắn tin khi phiên đang hoạt động.");
        }

        request.setStatus(BookingStatus.RESTRICTED);
        return mapToResponse(bookingRequestRepository.save(request));
    }

    @Override
    @Transactional
    public BookingResponse unrestrictMessaging(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        BookingRequest request = bookingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BookingRequest", "id", id.toString()));

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new BadRequestException("Bạn không có quyền hủy hạn chế phiên tư vấn này.");
        }

        if (request.getStatus() != BookingStatus.RESTRICTED) {
            throw new BadRequestException("Chỉ có thể hủy hạn chế khi phiên đang bị hạn chế.");
        }

        request.setStatus(BookingStatus.ACTIVE);
        return mapToResponse(bookingRequestRepository.save(request));
    }

    private BookingRequest getBookingForTriage(Long bookingId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

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

    @Override
    @Transactional
    public ConsultationThreadResponse provisionConsultationThread(Long bookingId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        BookingRequest request = bookingRequestRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("BookingRequest", "id", bookingId.toString()));

        // Access Validation: caller must be either the patient or the doctor
        if (!request.getPatient().getId().equals(currentUser.getId()) && !request.getDoctor().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn không có quyền truy cập phòng tư vấn này.");
        }

        // Creation Gate: booking must be APPROVED, ACTIVE, RESTRICTED, or COMPLETED (read-only)
        if (request.getStatus() != BookingStatus.APPROVED
                && request.getStatus() != BookingStatus.ACTIVE
                && request.getStatus() != BookingStatus.COMPLETED
                && request.getStatus() != BookingStatus.RESTRICTED) {
            throw new BadRequestException("Phòng tư vấn không khả dụng với trạng thái này.");
        }

        // Idempotency check no longer needed since thread is created during booking creation
        ConsultationThread thread = request.getThread();

        return ConsultationThreadResponse.builder()
                .id(thread.getId())
                .bookingRequestId(request.getId())
                .patientId(request.getPatient().getId())
                .patientFullName(request.getPatient().getFullName())
                .patientAvatarUrl(request.getPatient().getAvatarUrl())
                .doctorId(request.getDoctor().getId())
                .doctorFullName(request.getDoctor().getFullName())
                .doctorAvatarUrl(request.getDoctor().getAvatarUrl())
                .status(request.getStatus())
                .build();
    }

    private BookingResponse mapToResponse(BookingRequest request) {
        return BookingResponse.builder()
                .id(request.getId())
                .patientId(request.getPatient().getId())
                .patientFullName(request.getPatient().getFullName())
                .patientAvatarUrl(request.getPatient().getAvatarUrl())
                .doctorId(request.getDoctor().getId())
                .doctorFullName(request.getDoctor().getFullName())
                .doctorAvatarUrl(request.getDoctor().getAvatarUrl())
                .requestType(request.getRequestType())
                .status(request.getStatus())
                .note(request.getNote())
                .preferredTimeNote(request.getPreferredTimeNote())
                .rejectReason(request.getRejectReason())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
