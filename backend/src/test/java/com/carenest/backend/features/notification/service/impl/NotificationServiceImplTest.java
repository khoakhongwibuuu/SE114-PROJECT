package com.carenest.backend.features.notification.service.impl;

import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.notification.dto.response.NotificationResponse;
import com.carenest.backend.features.notification.dto.response.UnreadCountResponse;
import com.carenest.backend.features.notification.entity.Notification;
import com.carenest.backend.features.notification.enums.NotificationType;
import com.carenest.backend.features.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void markAsReadRejectsNotificationOwnedByAnotherUser() {
        Notification notification = notification(10L, user(2L), false);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class, () -> notificationService.markAsRead(10L, 1L));

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAsReadReturnsUpdatedNotificationForOwner() {
        Notification notification = notification(10L, user(1L), false);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        NotificationResponse response = notificationService.markAsRead(10L, 1L);

        assertTrue(response.getIsRead());
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getUserId());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsReadThrowsWhenNotificationDoesNotExist() {
        when(notificationRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(404L, 1L));
    }

    @Test
    void markAllAsReadUpdatesOnlyCurrentUserAndReturnsZeroUnread() {
        when(notificationRepository.markAllAsReadByUserId(1L)).thenReturn(3);

        UnreadCountResponse response = notificationService.markAllAsRead(1L);

        assertEquals(0L, response.getCount());
        verify(notificationRepository).markAllAsReadByUserId(1L);
    }

    @Test
    void createNotificationForUsersSkipsNullUsersUsersWithoutIdAndDuplicateIds() {
        User validUser = user(1L);
        User duplicateUser = user(1L);
        User userWithoutId = user(null);

        notificationService.createNotificationForUsers(
                java.util.Arrays.asList(validUser, null, userWithoutId, duplicateUser),
                "Có cập nhật mới",
                "Bạn có một cập nhật mới trong CareNest.",
                NotificationType.FAMILY,
                "FAMILY",
                22L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass((Class<List<Notification>>) (Class<?>) List.class);
        verify(notificationRepository).saveAll(captor.capture());
        List<Notification> savedNotifications = captor.getValue();

        assertEquals(1, savedNotifications.size());
        Notification saved = savedNotifications.get(0);
        assertEquals(validUser, saved.getUser());
        assertEquals("Có cập nhật mới", saved.getTitle());
        assertEquals(NotificationType.FAMILY, saved.getType());
        assertEquals("FAMILY", saved.getReferenceType());
        assertEquals(22L, saved.getReferenceId());
        assertFalse(saved.getIsRead());
    }

    private Notification notification(Long id, User user, boolean isRead) {
        Notification notification = Notification.builder()
                .user(user)
                .title("Thông báo")
                .message("Nội dung")
                .type(NotificationType.SYSTEM)
                .referenceType("SYSTEM")
                .referenceId(1L)
                .isRead(isRead)
                .build();
        notification.setId(id);
        return notification;
    }

    private User user(Long id) {
        User user = User.builder()
                .email("user" + id + "@example.com")
                .passwordHash("hash")
                .fullName("User " + id)
                .build();
        user.setId(id);
        return user;
    }
}
