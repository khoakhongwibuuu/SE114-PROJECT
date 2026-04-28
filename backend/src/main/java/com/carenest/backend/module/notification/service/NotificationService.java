package com.carenest.backend.module.notification.service;

import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.notification.enums.NotificationType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    void createNotificationForUser(User user, String title, String message, NotificationType type, String referenceType, Long referenceId);

    void createNotificationForUsers(List<User> users, String title, String message, NotificationType type, String referenceType, Long referenceId);

    Page<com.carenest.backend.module.notification.dto.response.NotificationResponse> getUserNotifications(Long userId, NotificationType type, Pageable pageable);

    com.carenest.backend.module.notification.dto.response.NotificationResponse markAsRead(Long notificationId, Long userId);

    com.carenest.backend.module.notification.dto.response.UnreadCountResponse getUnreadCount(Long userId);
}
