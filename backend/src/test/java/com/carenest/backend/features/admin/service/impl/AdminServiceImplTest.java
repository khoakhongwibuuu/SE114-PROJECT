package com.carenest.backend.features.admin.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.features.admin.dto.request.AdminUserRoleUpdateRequest;
import com.carenest.backend.features.admin.dto.request.AdminUserStatusUpdateRequest;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.community.repository.ReportTicketRepository;
import com.carenest.backend.features.doctorverification.repository.DoctorVerificationRepository;
import com.carenest.backend.features.notification.enums.NotificationType;
import com.carenest.backend.features.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DoctorVerificationRepository verificationRepository;
    @Mock
    private ReportTicketRepository reportRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void updateUserStatus_rejectsSelfBan() {
        User admin = admin(1L);
        AdminUserStatusUpdateRequest request = new AdminUserStatusUpdateRequest();
        request.setStatus("BANNED");

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(BadRequestException.class, () -> adminService.updateUserStatus(1L, request, admin));

        verify(userRepository, never()).save(admin);
    }

    @Test
    void updateUserStatus_rejectsBanningLastActiveAdmin() {
        User targetAdmin = admin(2L);
        User currentAdmin = admin(1L);
        AdminUserStatusUpdateRequest request = new AdminUserStatusUpdateRequest();
        request.setStatus("BANNED");

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetAdmin));
        when(userRepository.countByRoleAndIsActiveTrue(Role.ADMIN)).thenReturn(1L);

        assertThrows(BadRequestException.class, () -> adminService.updateUserStatus(2L, request, currentAdmin));

        verify(userRepository, never()).save(targetAdmin);
    }

    @Test
    void updateUserRole_rejectsDemotingLastActiveAdmin() {
        User targetAdmin = admin(2L);
        User currentAdmin = admin(1L);
        AdminUserRoleUpdateRequest request = new AdminUserRoleUpdateRequest();
        request.setRole("USER");

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetAdmin));
        when(userRepository.countByRoleAndIsActiveTrue(Role.ADMIN)).thenReturn(1L);

        assertThrows(BadRequestException.class, () -> adminService.updateUserRole(2L, request, currentAdmin));

        verify(userRepository, never()).save(targetAdmin);
    }

    @Test
    void updateUserRole_rejectsSelfDemotion() {
        User currentAdmin = admin(1L);
        AdminUserRoleUpdateRequest request = new AdminUserRoleUpdateRequest();
        request.setRole("USER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentAdmin));

        assertThrows(BadRequestException.class, () -> adminService.updateUserRole(1L, request, currentAdmin));

        verify(userRepository, never()).save(currentAdmin);
    }

    @Test
    void updateUserRole_rejectsGrantingAdminToBannedUser() {
        User user = User.builder()
                .email("user@example.com")
                .fullName("User")
                .role(Role.USER)
                .isActive(false)
                .build();
        user.setId(3L);
        AdminUserRoleUpdateRequest request = new AdminUserRoleUpdateRequest();
        request.setRole("ADMIN");

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> adminService.updateUserRole(3L, request, admin(1L)));

        verify(userRepository, never()).save(user);
    }

    @Test
    void updateUserStatus_notifiesUserWhenAccountIsBanned() {
        User user = user(3L);
        AdminUserStatusUpdateRequest request = new AdminUserStatusUpdateRequest();
        request.setStatus("BANNED");

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        var response = adminService.updateUserStatus(3L, request, admin(1L));

        assertEquals("BANNED", response.getStatus());
        verify(notificationService).createNotificationForUser(
                eq(user),
                contains("khóa"),
                contains("quản trị viên"),
                eq(NotificationType.SYSTEM),
                eq("ADMIN_USER_STATUS"),
                eq(3L)
        );
    }

    @Test
    void updateUserRole_notifiesUserWhenAdminRoleIsGranted() {
        User user = user(3L);
        AdminUserRoleUpdateRequest request = new AdminUserRoleUpdateRequest();
        request.setRole("ADMIN");

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        var response = adminService.updateUserRole(3L, request, admin(1L));

        assertEquals("ADMIN", response.getRole());
        verify(notificationService).createNotificationForUser(
                eq(user),
                contains("admin"),
                contains("quản trị"),
                eq(NotificationType.SYSTEM),
                eq("ADMIN_USER_ROLE"),
                eq(3L)
        );
    }

    private static User admin(Long id) {
        User admin = User.builder()
                .email("admin" + id + "@example.com")
                .fullName("Admin " + id)
                .role(Role.ADMIN)
                .isActive(true)
                .build();
        admin.setId(id);
        return admin;
    }

    private static User user(Long id) {
        User user = User.builder()
                .email("user" + id + "@example.com")
                .fullName("User " + id)
                .role(Role.USER)
                .isActive(true)
                .build();
        user.setId(id);
        return user;
    }
}
