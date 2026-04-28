package com.carenest.backend.module.notification.service.impl;

import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.notification.entity.Notification;
import com.carenest.backend.module.notification.enums.NotificationType;
import com.carenest.backend.module.notification.repository.NotificationRepository;
import com.carenest.backend.module.notification.service.NotificationService;
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
}
