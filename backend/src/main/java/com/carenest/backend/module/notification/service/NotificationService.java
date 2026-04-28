package com.carenest.backend.module.notification.service;

import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.notification.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    void createNotificationForUser(User user, String title, String message, NotificationType type, String referenceType, Long referenceId);

    void createNotificationForUsers(List<User> users, String title, String message, NotificationType type, String referenceType, Long referenceId);

}
