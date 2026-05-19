package com.carenest.backend.module.doctorverification.service.impl;

import com.carenest.backend.common.exception.BadRequestException;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.auth.enums.Role;
import com.carenest.backend.module.auth.repository.UserRepository;
import com.carenest.backend.module.doctorverification.dto.request.RejectDoctorVerificationRequest;
import com.carenest.backend.module.doctorverification.dto.request.SubmitDoctorVerificationRequest;
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
            throw new BadRequestException("User is already verified as a doctor");
        }

        if (doctorVerificationRepository.existsByUserIdAndStatus(currentUser.getId(), VerificationStatus.PENDING)) {
            throw new BadRequestException("A doctor verification request is already pending");
        }

        DoctorVerification verification = doctorVerificationRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> DoctorVerification.builder()
                        .user(currentUser)
                        .build());

        if (verification.getStatus() == VerificationStatus.APPROVED) {
            throw new BadRequestException("Doctor verification request has already been approved");
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
            throw new BadRequestException("Doctor verification request is already approved");
        }

        User user = verification.getUser();
        if (user == null) {
            throw new ResourceNotFoundException("User not found for doctor verification request");
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
            throw new BadRequestException("Approved doctor verification requests cannot be rejected");
        }

        verification.setStatus(VerificationStatus.REJECTED);
        verification.setRejectionReason(request.getRejectionReason().trim());

        return toResponse(doctorVerificationRepository.save(verification));
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
