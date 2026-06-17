package com.carenest.backend.features.family.util;

import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.core.exception.UnauthorizedException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.family.context.FamilyRequestContext;
import com.carenest.backend.features.family.entity.FamilyMember;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
            throw new AccessDeniedException("Bạn không có quyền truy cập gia đình này");
        }
    }

    public Long getDefaultFamilyId() {
        User currentUser = getCurrentUser();
        return familyMemberRepository.findAllByUserId(currentUser.getId()).stream()
                .findFirst()
                .map(fm -> fm.getFamily().getId())
                .orElse(null);
    }

    public void checkUserBelongsToHealthProfile(Long profileId) {
        User currentUser = getCurrentUser();
        HealthProfile profile = healthProfileRepository.findByIdAndDeletedAtIsNull(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", profileId.toString()));

        Long activeFamilyId = FamilyRequestContext.getFamilyId();
        if (activeFamilyId != null
                && profile.getFamily() != null
                && !profile.getFamily().getId().equals(activeFamilyId)) {
            throw new AccessDeniedException("Hồ sơ sức khỏe không thuộc gia đình đang chọn");
        }

        if (profile.getFamily() != null) {
            boolean belongs = familyMemberRepository
                    .findByFamilyIdAndUserId(profile.getFamily().getId(), currentUser.getId())
                    .isPresent();
            if (!belongs) {
                throw new AccessDeniedException("Bạn không có quyền truy cập hồ sơ sức khỏe này");
            }
            return;
        }

        if (profile.getUser() == null || !profile.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập hồ sơ sức khỏe cá nhân này");
        }
    }

    public void checkHealthProfileBelongsToFamily(Long profileId, Long familyId) {
        if (profileId == null || familyId == null) {
            throw new AccessDeniedException("Cần có thông tin gia đình và hồ sơ sức khỏe");
        }

        checkUserBelongsToFamily(familyId);
        checkUserBelongsToHealthProfile(profileId);

        HealthProfile profile = healthProfileRepository.findByIdAndDeletedAtIsNull(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", profileId.toString()));

        if (profile.getFamily() == null || !profile.getFamily().getId().equals(familyId)) {
            throw new AccessDeniedException("Hồ sơ sức khỏe không thuộc gia đình này");
        }
    }

    public List<Long> getFamilyIdsForProfile(HealthProfile profile) {
        List<Long> familyIds = new ArrayList<>();
        if (profile == null) {
            return familyIds;
        }
        if (profile.getFamily() != null) {
            familyIds.add(profile.getFamily().getId());
        } else if (profile.getUser() != null) {
            for (FamilyMember familyMember : familyMemberRepository.findAllByUserId(profile.getUser().getId())) {
                if (familyMember.getFamily() != null) {
                    familyIds.add(familyMember.getFamily().getId());
                }
            }
        }
        return familyIds;
    }
}

