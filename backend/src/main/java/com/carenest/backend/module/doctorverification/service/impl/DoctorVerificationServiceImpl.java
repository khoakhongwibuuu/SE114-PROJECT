package com.carenest.backend.module.doctorverification.service.impl;

import com.carenest.backend.common.exception.BadRequestException;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.auth.enums.Role;
import com.carenest.backend.module.auth.repository.UserRepository;
import com.carenest.backend.module.doctorverification.dto.request.RejectDoctorVerificationRequest;
import com.carenest.backend.module.doctorverification.dto.request.SubmitDoctorVerificationRequest;
import com.carenest.backend.module.doctorverification.dto.response.DoctorSummaryResponse;
import com.carenest.backend.module.doctorverification.dto.response.DoctorVerificationResponse;
import com.carenest.backend.module.doctorverification.entity.DoctorVerification;
import com.carenest.backend.module.doctorverification.enums.VerificationStatus;
import com.carenest.backend.module.doctorverification.repository.DoctorVerificationRepository;
import com.carenest.backend.module.doctorverification.service.DoctorVerificationService;
import com.carenest.backend.module.family.util.FamilySecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorVerificationServiceImpl implements DoctorVerificationService {

    private final DoctorVerificationRepository doctorVerificationRepository;
    private final UserRepository userRepository;
    private final FamilySecurityUtil familySecurityUtil;

    @Override
    @Transactional
    public DoctorVerificationResponse submitRequest(SubmitDoctorVerificationRequest request) {
        User currentUser = familySecurityUtil.getCurrentUser();

        if (currentUser.getRole() == Role.DOCTOR) {
            throw new BadRequestException("Tài khoản đã được xác thực là bác sĩ");
        }

        if (doctorVerificationRepository.existsByUserIdAndStatus(currentUser.getId(), VerificationStatus.PENDING)) {
            throw new BadRequestException("Bạn đã có hồ sơ xác thực bác sĩ đang chờ duyệt");
        }

        DoctorVerification verification = doctorVerificationRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> DoctorVerification.builder()
                        .user(currentUser)
                        .build());

        if (verification.getStatus() == VerificationStatus.APPROVED) {
            throw new BadRequestException("Hồ sơ xác thực bác sĩ đã được phê duyệt");
        }

        verification.setCertificationNumber(request.getCertificationNumber().trim());
        verification.setSpecialty(request.getSpecialty().trim());
        verification.setHospitalName(request.getHospitalName().trim());
        verification.setDocumentUrl(request.getDocumentUrl().trim());
        verification.setStatus(VerificationStatus.PENDING);
        verification.setRejectionReason(null);

        return toResponse(doctorVerificationRepository.save(verification));
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorVerificationResponse getMyRequest() {
        User currentUser = familySecurityUtil.getCurrentUser();
        DoctorVerification verification = doctorVerificationRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("DoctorVerification", "userId", currentUser.getId().toString()));
        return toResponse(verification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorVerificationResponse> getPendingRequests() {
        return doctorVerificationRepository.findAllByStatusOrderByCreatedAtAsc(VerificationStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DoctorVerificationResponse approveRequest(Long requestId) {
        DoctorVerification verification = doctorVerificationRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("DoctorVerification", requestId));

        if (verification.getStatus() == VerificationStatus.APPROVED) {
            throw new BadRequestException("Hồ sơ xác thực bác sĩ đã được phê duyệt");
        }

        User user = verification.getUser();
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng của hồ sơ xác thực bác sĩ");
        }

        verification.setStatus(VerificationStatus.APPROVED);
        verification.setRejectionReason(null);
        user.setRole(Role.DOCTOR);
        userRepository.save(user);

        return toResponse(doctorVerificationRepository.save(verification));
    }

    @Override
    @Transactional
    public DoctorVerificationResponse rejectRequest(Long requestId, RejectDoctorVerificationRequest request) {
        DoctorVerification verification = doctorVerificationRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("DoctorVerification", requestId));

        if (verification.getStatus() == VerificationStatus.APPROVED) {
            throw new BadRequestException("Không thể từ chối hồ sơ bác sĩ đã được phê duyệt");
        }

        verification.setStatus(VerificationStatus.REJECTED);
        verification.setRejectionReason(request.getRejectionReason().trim());

        return toResponse(doctorVerificationRepository.save(verification));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorSummaryResponse> getAllDoctors() {
        return userRepository.findAllByRoleOrderByCreatedAtDesc(Role.DOCTOR).stream()
                .map(user -> {
                    var verification = doctorVerificationRepository.findByUserId(user.getId()).orElse(null);
                    return DoctorSummaryResponse.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .avatarUrl(user.getAvatarUrl())
                            .certificationNumber(verification != null ? verification.getCertificationNumber() : null)
                            .specialty(verification != null ? verification.getSpecialty() : null)
                            .hospitalName(verification != null ? verification.getHospitalName() : null)
                            .documentUrl(verification != null ? verification.getDocumentUrl() : null)
                            .approvedAt(verification != null ? verification.getUpdatedAt() : null)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public void revokeDoctor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getRole() != Role.DOCTOR) {
            throw new BadRequestException("Tài khoản này không phải Bác sĩ");
        }

        user.setRole(Role.USER);
        userRepository.save(user);

        // Also reset the verification status so they can re-apply
        doctorVerificationRepository.findByUserId(userId).ifPresent(v -> {
            v.setStatus(VerificationStatus.REJECTED);
            v.setRejectionReason("Quyền Bác sĩ đã bị thu hồi bởi Admin");
            doctorVerificationRepository.save(v);
        });
    }

    private DoctorVerificationResponse toResponse(DoctorVerification verification) {
        User user = verification.getUser();
        return DoctorVerificationResponse.builder()
                .id(verification.getId())
                .userId(user != null ? user.getId() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .userFullName(user != null ? user.getFullName() : null)
                .certificationNumber(verification.getCertificationNumber())
                .specialty(verification.getSpecialty())
                .hospitalName(verification.getHospitalName())
                .documentUrl(verification.getDocumentUrl())
                .status(verification.getStatus())
                .rejectionReason(verification.getRejectionReason())
                .createdAt(verification.getCreatedAt())
                .updatedAt(verification.getUpdatedAt())
                .build();
    }
}
