package com.carenest.backend.features.notification.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.notification.dto.response.NotificationResponse;
import com.carenest.backend.features.notification.dto.response.UnreadCountResponse;
import com.carenest.backend.features.notification.enums.NotificationType;
import com.carenest.backend.features.notification.service.NotificationService;
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
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) NotificationType type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<NotificationResponse> page = notificationService.getUserNotifications(user.getId(), type, pageable);
        return ApiResponse.success("Láº¥y danh sÃ¡ch thÃ´ng bÃ¡o thÃ nh cÃ´ng", page);
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User user) {

        NotificationResponse response = notificationService.markAsRead(id, user.getId());
        return ApiResponse.success("ÄÃ£ Ä‘Ã¡nh dáº¥u thÃ´ng bÃ¡o lÃ  Ä‘Ã£ Ä‘á»c", response);
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal User user) {

        UnreadCountResponse response = notificationService.getUnreadCount(user.getId());
        return ApiResponse.success("Láº¥y sá»‘ thÃ´ng bÃ¡o chÆ°a Ä‘á»c thÃ nh cÃ´ng", response);
    }
}
