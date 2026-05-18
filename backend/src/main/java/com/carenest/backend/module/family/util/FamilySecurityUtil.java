package com.carenest.backend.module.family.util;

import com.carenest.backend.common.exception.UnauthorizedException;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.auth.repository.UserRepository;
import com.carenest.backend.module.family.repository.FamilyMemberRepository;
import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import com.carenest.backend.module.healthprofile.repository.HealthProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FamilySecurityUtil {

    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final HealthProfileRepository healthProfileRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Vui lòng đăng nhập"));
    }

    public void checkUserBelongsToFamily(Long familyId) {
        User currentUser = getCurrentUser();
        boolean belongs = familyMemberRepository.findByFamilyIdAndUserId(familyId, currentUser.getId()).isPresent();
        if (!belongs) {
            throw new AccessDeniedException("Bạn không có quyền truy cập vào gia đình này");
        }
    }

    public void checkUserBelongsToHealthProfile(Long profileId) {
        User currentUser = getCurrentUser();
        HealthProfile profile = healthProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Hồ sơ sức khỏe không tồn tại"));

        if (profile.getFamily() != null) {
            boolean belongs = familyMemberRepository.findByFamilyIdAndUserId(profile.getFamily().getId(), currentUser.getId()).isPresent();
            if (!belongs) {
                throw new AccessDeniedException("Bạn không có quyền truy cập vào hồ sơ sức khỏe này");
            }
        } else {
            // Profile does not belong to a family, only the direct user can access
            if (!profile.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Bạn không có quyền truy cập vào hồ sơ cá nhân này");
            }
        }
    }

    public java.util.List<Long> getFamilyIdsForProfile(HealthProfile profile) {
        java.util.List<Long> familyIds = new java.util.ArrayList<>();
        if (profile == null) {
            return familyIds;
        }
        if (profile.getFamily() != null) {
            familyIds.add(profile.getFamily().getId());
        } else if (profile.getUser() != null) {
            for (com.carenest.backend.module.family.entity.FamilyMember fm : familyMemberRepository.findAllByUserId(profile.getUser().getId())) {
                if (fm.getFamily() != null) {
                    familyIds.add(fm.getFamily().getId());
                }
            }
        }
        return familyIds;
    }
}
