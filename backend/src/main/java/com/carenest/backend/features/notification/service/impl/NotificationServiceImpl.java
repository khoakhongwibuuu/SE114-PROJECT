package com.carenest.backend.features.notification.service.impl;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.notification.entity.Notification;
import com.carenest.backend.features.notification.enums.NotificationType;
import com.carenest.backend.features.notification.repository.NotificationRepository;
import com.carenest.backend.features.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void createNotificationForUser(User user, String title, String message, NotificationType type, String referenceType, Long referenceId) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void createNotificationForUsers(List<User> users, String title, String message, NotificationType type, String referenceType, Long referenceId) {
        if (users == null || users.isEmpty()) return;

        List<Notification> notifications = users.stream().map(user -> Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .isRead(false)
                .build()).collect(Collectors.toList());

        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.carenest.backend.features.notification.dto.response.NotificationResponse> getUserNotifications(Long userId, NotificationType type, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Notification> page;
        if (type != null) {
            page = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        } else {
            page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        return page.map(notification -> com.carenest.backend.features.notification.dto.response.NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build());
    }

    @Override
    @Transactional
    public com.carenest.backend.features.notification.dto.response.NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new com.carenest.backend.core.exception.ResourceNotFoundException("Notification", "id", notificationId.toString()));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Notification does not belong to the user");
        }

        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        return com.carenest.backend.features.notification.dto.response.NotificationResponse.builder()
                .id(saved.getId())
                .userId(saved.getUser().getId())
                .title(saved.getTitle())
                .message(saved.getMessage())
                .type(saved.getType())
                .referenceType(saved.getReferenceType())
                .referenceId(saved.getReferenceId())
                .isRead(saved.getIsRead())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public com.carenest.backend.features.notification.dto.response.UnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countUnreadNotifications(userId);
        return new com.carenest.backend.features.notification.dto.response.UnreadCountResponse(count);
    }
}
