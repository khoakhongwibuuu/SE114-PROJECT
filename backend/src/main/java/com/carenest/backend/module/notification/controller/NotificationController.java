package com.carenest.backend.module.notification.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.notification.dto.response.NotificationResponse;
import com.carenest.backend.module.notification.dto.response.UnreadCountResponse;
import com.carenest.backend.module.notification.enums.NotificationType;
import com.carenest.backend.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) NotificationType type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<NotificationResponse> page = notificationService.getUserNotifications(user.getId(), type, pageable);
        return ApiResponse.success("Fetched notifications successfully", page);
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        
        NotificationResponse response = notificationService.markAsRead(id, user.getId());
        return ApiResponse.success("Marked notification as read", response);
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal User user) {
        
        UnreadCountResponse response = notificationService.getUnreadCount(user.getId());
        return ApiResponse.success("Fetched unread count", response);
    }
}
