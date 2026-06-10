package com.carenest.backend.features.booking.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.booking.dto.request.SendConsultationMessageRequest;
import com.carenest.backend.features.booking.dto.response.ConsultationMessageResponse;
import com.carenest.backend.features.booking.entity.BookingRequest;
import com.carenest.backend.features.booking.entity.ConsultationMessage;
import com.carenest.backend.features.booking.entity.ConsultationThread;
import com.carenest.backend.features.booking.enums.BookingStatus;
import com.carenest.backend.features.booking.repository.BookingRequestRepository;
import com.carenest.backend.features.booking.repository.ConsultationMessageRepository;
import com.carenest.backend.features.booking.repository.ConsultationThreadRepository;
import com.carenest.backend.features.booking.service.ConsultationMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationMessageServiceImpl implements ConsultationMessageService {

    private final ConsultationMessageRepository consultationMessageRepository;
    private final ConsultationThreadRepository consultationThreadRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationMessageResponse> getMessages(Long threadId) {
        User currentUser = getCurrentUser();
        ConsultationThread thread = getThreadAndValidateAccess(threadId, currentUser);

        return consultationMessageRepository.findAllByThreadIdOrderByCreatedAtAsc(thread.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConsultationMessageResponse sendMessage(Long threadId, SendConsultationMessageRequest request) {
        User currentUser = getCurrentUser();
        ConsultationThread thread = getThreadAndValidateAccess(threadId, currentUser);

        // Find active ONLINE_CHAT booking for this thread to check status
        com.carenest.backend.features.booking.entity.BookingRequest booking = bookingRequestRepository.findFirstByThreadIdAndRequestTypeAndStatusInOrderByCreatedAtDesc(
            thread.getId(), 
            com.carenest.backend.features.booking.enums.BookingRequestType.ONLINE_CHAT,
            java.util.List.of(BookingStatus.APPROVED, BookingStatus.ACTIVE, BookingStatus.RESTRICTED, BookingStatus.COMPLETED)
        ).orElseThrow(() -> new BadRequestException("Không tìm thấy phiên tư vấn trực tuyến cho luồng này."));

        if (booking.getStatus() != BookingStatus.APPROVED && booking.getStatus() != BookingStatus.ACTIVE) {
            throw new BadRequestException("Không thể gửi tin nhắn. Phiên tư vấn đã kết thúc, bị hạn chế hoặc chưa được duyệt.");
        }

        ConsultationMessage message = ConsultationMessage.builder()
                .thread(thread)
                .sender(currentUser)
                .content(request.getContent())
                // .createdAt will be set by AuditingEntityListener, but let's be safe for immediate response if needed
                .build();

        ConsultationMessage saved = consultationMessageRepository.save(message);

        // STOMP requires returning mapped DTO right away, if Auditing hasn't flushed createdAt may be null until commit.
        // We handle this in mapToResponse.

        return mapToResponse(saved);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private ConsultationThread getThreadAndValidateAccess(Long threadId, User user) {
        ConsultationThread thread = consultationThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("ConsultationThread", "id", threadId.toString()));

        if (!thread.getPatient().getId().equals(user.getId()) && !thread.getDoctor().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền truy cập vào luồng tư vấn này.");
        }

        return thread;
    }

    private ConsultationMessageResponse mapToResponse(ConsultationMessage message) {
        return ConsultationMessageResponse.builder()
                .id(message.getId())
                .threadId(message.getThread().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .senderAvatarUrl(message.getSender().getAvatarUrl())
                .content(message.getContent())
                .createdAt(message.getCreatedAt() != null ? message.getCreatedAt() : Instant.now())
                .build();
    }
}
