package com.carenest.backend.features.admin.service.impl;

import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.admin.dto.request.AdminUserStatusUpdateRequest;
import com.carenest.backend.features.admin.dto.response.AdminDashboardStatsResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserStatusUpdateResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserSummaryResponse;
import com.carenest.backend.features.admin.service.AdminService;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.community.repository.ReportTicketRepository;
import com.carenest.backend.features.doctorverification.enums.VerificationStatus;
import com.carenest.backend.features.doctorverification.repository.DoctorVerificationRepository;
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

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalDoctors = userRepository.countByRole(Role.DOCTOR);
        long pendingEkycCount = verificationRepository.countByStatus(VerificationStatus.PENDING);
        long moderationQueueCount = reportRepository.count(); // All reports are essentially pending until handled

        return AdminDashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalDoctors(totalDoctors)
                .pendingEkycCount(pendingEkycCount)
                .moderationQueueCount(moderationQueueCount)
                .trend(List.of(10L, 12L, 15L, 18L, 25L, 30L)) // Mock trend data
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
    @Transactional
    public AdminUserStatusUpdateResponse updateUserStatus(Long userId, AdminUserStatusUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        
        boolean isActive = !"BANNED".equalsIgnoreCase(request.getStatus());
        user.setIsActive(isActive);
        userRepository.save(user);
        
        return AdminUserStatusUpdateResponse.builder()
                .id(user.getId())
                .status(isActive ? "ACTIVE" : "BANNED")
                .build();
    }
}
