package com.carenest.backend.features.admin.service.impl;

import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.admin.dto.request.AdminUserRoleUpdateRequest;
import com.carenest.backend.features.admin.dto.request.AdminUserStatusUpdateRequest;
import com.carenest.backend.features.admin.dto.response.AdminDashboardStatsResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserAuditLogResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserRoleUpdateResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserStatusUpdateResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserSummaryResponse;
import com.carenest.backend.features.admin.entity.AdminUserAuditLog;
import com.carenest.backend.features.admin.enums.AdminUserAuditAction;
import com.carenest.backend.features.admin.repository.AdminUserAuditLogRepository;
import com.carenest.backend.features.admin.service.AdminService;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.community.enums.ReportStatus;
import com.carenest.backend.features.community.repository.ReportTicketRepository;
import com.carenest.backend.features.doctorverification.enums.VerificationStatus;
import com.carenest.backend.features.doctorverification.repository.DoctorVerificationRepository;
import com.carenest.backend.features.notification.enums.NotificationType;
import com.carenest.backend.features.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final DoctorVerificationRepository verificationRepository;
    private final ReportTicketRepository reportRepository;
    private final NotificationService notificationService;
    private final AdminUserAuditLogRepository adminUserAuditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalDoctors = userRepository.countByRole(Role.DOCTOR);
        long pendingEkycCount = verificationRepository.countByStatus(VerificationStatus.PENDING);
        long moderationQueueCount = reportRepository.countByStatus(ReportStatus.PENDING);

        return AdminDashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalDoctors(totalDoctors)
                .pendingEkycCount(pendingEkycCount)
                .moderationQueueCount(moderationQueueCount)
                .trend(List.of())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserSummaryResponse> getUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepository.findUsersBySearch(search, pageable);

        Page<AdminUserSummaryResponse> responsePage = userPage.map(user -> AdminUserSummaryResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .status(user.getIsActive() ? "ACTIVE" : "BANNED")
                .build());

        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserAuditLogResponse> getRecentUserAuditLogs() {
        return adminUserAuditLogRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(log -> AdminUserAuditLogResponse.builder()
                        .id(log.getId())
                        .action(log.getAction() != null ? log.getAction().name() : null)
                        .actorId(log.getActor() != null ? log.getActor().getId() : null)
                        .actorName(log.getActor() != null ? log.getActor().getFullName() : null)
                        .targetUserId(log.getTargetUser() != null ? log.getTargetUser().getId() : null)
                        .targetUserName(log.getTargetUser() != null ? log.getTargetUser().getFullName() : null)
                        .targetUserEmail(log.getTargetUser() != null ? log.getTargetUser().getEmail() : null)
                        .previousRole(log.getPreviousRole())
                        .newRole(log.getNewRole())
                        .previousStatus(log.getPreviousStatus())
                        .newStatus(log.getNewStatus())
                        .reason(log.getReason())
                        .createdAt(log.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public AdminUserStatusUpdateResponse updateUserStatus(Long userId, AdminUserStatusUpdateRequest request, User currentAdmin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        String normalizedReason = requireNormalizedReason(request.getReason());
        boolean isActive = !"BANNED".equalsIgnoreCase(request.getStatus());
        String previousStatus = user.getIsActive() ? "ACTIVE" : "BANNED";
        String newStatus = isActive ? "ACTIVE" : "BANNED";

        if (!isActive && currentAdmin != null && user.getId().equals(currentAdmin.getId())) {
            throw new BadRequestException("Admin không thể tự khóa tài khoản của chính mình");
        }
        if (!isActive && user.getRole() == Role.ADMIN && userRepository.countByRoleAndIsActiveTrue(Role.ADMIN) <= 1) {
            throw new BadRequestException("Không thể khóa admin hoạt động cuối cùng của hệ thống");
        }
        if (previousStatus.equals(newStatus)) {
            throw new BadRequestException("Tài khoản đã ở trạng thái " + newStatus);
        }

        user.setIsActive(isActive);
        userRepository.save(user);
        saveUserAuditLog(
                currentAdmin,
                user,
                isActive ? AdminUserAuditAction.USER_REACTIVATED : AdminUserAuditAction.USER_BANNED,
                user.getRole().name(),
                user.getRole().name(),
                previousStatus,
                newStatus,
                normalizedReason
        );
        notificationService.createNotificationForUser(
                user,
                isActive ? "Tài khoản đã được mở lại" : "Tài khoản đã bị khóa",
                isActive
                        ? "Tài khoản CareNest của bạn đã được quản trị viên mở lại. Lý do: " + normalizedReason
                        : "Tài khoản CareNest của bạn đã bị quản trị viên khóa. Lý do: " + normalizedReason,
                NotificationType.SYSTEM,
                "ADMIN_USER_STATUS",
                user.getId()
        );

        return AdminUserStatusUpdateResponse.builder()
                .id(user.getId())
                .status(newStatus)
                .build();
    }

    @Override
    @Transactional
    public AdminUserRoleUpdateResponse updateUserRole(Long userId, AdminUserRoleUpdateRequest request, User currentAdmin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        String normalizedReason = requireNormalizedReason(request.getReason());
        Role targetRole = parseManageableRole(request.getRole());
        String previousRole = user.getRole().name();

        if (currentAdmin != null && user.getId().equals(currentAdmin.getId()) && targetRole != Role.ADMIN) {
            throw new BadRequestException("Admin không thể tự hạ quyền quản trị của chính mình");
        }
        if (user.getRole() == Role.ADMIN && targetRole != Role.ADMIN && userRepository.countByRoleAndIsActiveTrue(Role.ADMIN) <= 1) {
            throw new BadRequestException("Không thể hạ quyền admin hoạt động cuối cùng của hệ thống");
        }
        if (targetRole == Role.ADMIN && !user.getIsActive()) {
            throw new BadRequestException("Không thể cấp quyền admin cho tài khoản đang bị khóa");
        }
        if (user.getRole() == targetRole) {
            throw new BadRequestException("Người dùng đã có quyền " + targetRole.name());
        }

        user.setRole(targetRole);
        userRepository.save(user);
        saveUserAuditLog(
                currentAdmin,
                user,
                targetRole == Role.ADMIN ? AdminUserAuditAction.ADMIN_ROLE_GRANTED : AdminUserAuditAction.ADMIN_ROLE_REVOKED,
                previousRole,
                targetRole.name(),
                user.getIsActive() ? "ACTIVE" : "BANNED",
                user.getIsActive() ? "ACTIVE" : "BANNED",
                normalizedReason
        );
        notificationService.createNotificationForUser(
                user,
                targetRole == Role.ADMIN ? "Bạn đã được cấp quyền admin" : "Quyền admin đã được gỡ",
                targetRole == Role.ADMIN
                        ? "Bạn đã được cấp quyền truy cập khu vực quản trị CareNest. Lý do: " + normalizedReason
                        : "Quyền truy cập khu vực quản trị CareNest của bạn đã được gỡ. Lý do: " + normalizedReason,
                NotificationType.SYSTEM,
                "ADMIN_USER_ROLE",
                user.getId()
        );

        return AdminUserRoleUpdateResponse.builder()
                .id(user.getId())
                .role(user.getRole().name())
                .build();
    }

    private void saveUserAuditLog(
            User actor,
            User targetUser,
            AdminUserAuditAction action,
            String previousRole,
            String newRole,
            String previousStatus,
            String newStatus,
            String reason
    ) {
        if (actor == null) {
            return;
        }
        adminUserAuditLogRepository.save(AdminUserAuditLog.builder()
                .actor(actor)
                .targetUser(targetUser)
                .action(action)
                .previousRole(previousRole)
                .newRole(newRole)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reason(reason)
                .build());
    }

    private Role parseManageableRole(String rawRole) {
        String normalized = rawRole == null ? "" : rawRole.trim().replace("ROLE_", "").toUpperCase();
        try {
            Role role = Role.valueOf(normalized);
            if (role == Role.DOCTOR) {
                throw new BadRequestException("Quyền bác sĩ phải được xử lý qua luồng xác thực bác sĩ");
            }
            return role;
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Role không hợp lệ. Chỉ hỗ trợ USER hoặc ADMIN");
        }
    }

    private String requireNormalizedReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Lý do thao tác không được để trống");
        }
        return reason.trim();
    }
}
