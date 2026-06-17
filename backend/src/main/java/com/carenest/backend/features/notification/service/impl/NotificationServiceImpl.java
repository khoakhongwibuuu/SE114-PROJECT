package com.carenest.backend.features.notification.service.impl;

import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.notification.dto.response.NotificationResponse;
import com.carenest.backend.features.notification.dto.response.UnreadCountResponse;
import com.carenest.backend.features.notification.entity.Notification;
import com.carenest.backend.features.notification.enums.NotificationType;
import com.carenest.backend.features.notification.repository.NotificationRepository;
import com.carenest.backend.features.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void createNotificationForUser(User user, String title, String message, NotificationType type, String referenceType, Long referenceId) {
        if (user == null) {
            return;
        }
        notificationRepository.save(buildNotification(user, title, message, type, referenceType, referenceId));
    }

    @Override
    @Transactional
    public void createNotificationForUsers(List<User> users, String title, String message, NotificationType type, String referenceType, Long referenceId) {
        if (users == null || users.isEmpty()) {
            return;
        }

        Set<Long> seenUserIds = new HashSet<>();
        List<Notification> notifications = users.stream()
                .filter(user -> user != null && user.getId() != null && seenUserIds.add(user.getId()))
                .map(user -> buildNotification(user, title, message, type, referenceType, referenceId))
                .toList();

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, NotificationType type, Pageable pageable) {
        Page<Notification> page = type != null
                ? notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId.toString()));

        if (!notification.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Thông báo không thuộc người dùng hiện tại");
        }

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification = notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Override
    @Transactional
    public UnreadCountResponse markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        return new UnreadCountResponse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countUnreadNotifications(userId);
        return new UnreadCountResponse(count);
    }

    private Notification buildNotification(User user, String title, String message, NotificationType type, String referenceType, Long referenceId) {
        return Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .isRead(false)
                .build();
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
