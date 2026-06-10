package com.carenest.backend.features.doctor.service.impl;

import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.doctor.dto.DoctorPublicProfileResponse;
import com.carenest.backend.features.doctor.service.DoctorProfileService;
import com.carenest.backend.features.doctorverification.entity.DoctorVerification;
import com.carenest.backend.features.doctorverification.enums.VerificationStatus;
import com.carenest.backend.features.doctorverification.repository.DoctorVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorProfileServiceImpl implements DoctorProfileService {

    private final UserRepository userRepository;
    private final DoctorVerificationRepository doctorVerificationRepository;

    @Override
    @Transactional(readOnly = true)
    public DoctorPublicProfileResponse getDoctorPublicProfile(Long doctorId) {
        User user = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ không tồn tại"));

        if (user.getRole() != Role.DOCTOR || !user.getIsActive()) {
            throw new ResourceNotFoundException("Bác sĩ không tồn tại hoặc đã bị khóa");
        }

        DoctorVerification verification = doctorVerificationRepository.findByUserId(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ chưa được xác thực"));

        if (verification.getStatus() != VerificationStatus.APPROVED) {
            throw new ResourceNotFoundException("Hồ sơ bác sĩ chưa được duyệt");
        }

        String certificationNumber = verification.getCertificationNumber();
        String maskedCert = "****";
        if (certificationNumber != null && certificationNumber.length() > 4) {
            maskedCert = "****" + certificationNumber.substring(certificationNumber.length() - 4);
        }

        return DoctorPublicProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .specialty(verification.getSpecialty())
                .hospitalName(verification.getHospitalName())
                .certificationNumber(maskedCert)
                .isVerified(true)
                .verifiedAt(verification.getUpdatedAt())
                .build();
    }
}
