package com.carenest.backend.features.notification.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.core.api.PageResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) NotificationType type,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<NotificationResponse> page = notificationService.getUserNotifications(user.getId(), type, pageable);
        return ApiResponse.success("Lấy danh sách thông báo thành công", PageResponse.of(page));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User user) {

        NotificationResponse response = notificationService.markAsRead(id, user.getId());
        return ApiResponse.success("Đã đánh dấu thông báo là đã đọc", response);
    }

    @PatchMapping("/read-all")
    public ApiResponse<UnreadCountResponse> markAllAsRead(
            @AuthenticationPrincipal User user) {

        UnreadCountResponse response = notificationService.markAllAsRead(user.getId());
        return ApiResponse.success("Đã đánh dấu tất cả thông báo là đã đọc", response);
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal User user) {

        UnreadCountResponse response = notificationService.getUnreadCount(user.getId());
        return ApiResponse.success("Lấy số thông báo chưa đọc thành công", response);
    }
}
