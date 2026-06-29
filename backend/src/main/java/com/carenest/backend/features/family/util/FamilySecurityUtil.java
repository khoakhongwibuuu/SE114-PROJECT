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

    public void checkCanReadHealthProfile(Long profileId) {
        User currentUser = getCurrentUser();
        HealthProfile profile = healthProfileRepository.findByIdAndDeletedAtIsNull(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", profileId.toString()));

        // Relax check to allow cross-family navigation for members belonging to both families
        // and prevent mismatch errors with globally active family ID in request headers.

        if (profile.getFamily() != null) {
            boolean belongs = familyMemberRepository
                    .findByFamilyIdAndUserId(profile.getFamily().getId(), currentUser.getId())
                    .isPresent();
            if (!belongs) {
                throw new AccessDeniedException("Bạn không có quyền xem hồ sơ sức khỏe này");
            }
            return;
        }

        if (profile.getUser() == null) {
            throw new AccessDeniedException("Hồ sơ không hợp lệ");
        }

        if (profile.getUser().getId().equals(currentUser.getId())) {
            return;
        }

        // Allow if currentUser and profile.getUser() share any family
        boolean shareFamily = familyMemberRepository.findAllByUserId(currentUser.getId()).stream()
                .anyMatch(fm -> familyMemberRepository.existsByFamilyIdAndUserId(fm.getFamily().getId(), profile.getUser().getId()));

        if (!shareFamily) {
            throw new AccessDeniedException("Bạn không có quyền xem hồ sơ sức khỏe cá nhân này");
        }
    }

    public void checkCanWriteHealthProfile(Long profileId) {
        User currentUser = getCurrentUser();
        HealthProfile profile = healthProfileRepository.findByIdAndDeletedAtIsNull(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", profileId.toString()));

        // 1. Current user's own profile
        if (profile.getUser() != null && profile.getUser().getId().equals(currentUser.getId())) {
            return;
        }

        // 2. Dependent profile in a family the current user belongs to
        if (profile.getUser() == null && profile.getFamily() != null) {
            boolean belongsToFamily = familyMemberRepository
                    .findByFamilyIdAndUserId(profile.getFamily().getId(), currentUser.getId())
                    .isPresent();
            if (belongsToFamily) {
                return;
            }
        }

        throw new AccessDeniedException("Bạn không có quyền chỉnh sửa. Chỉ có thể chỉnh sửa hồ sơ cá nhân hoặc người phụ thuộc.");
    }

    public void checkHealthProfileBelongsToFamily(Long profileId, Long familyId) {
        if (profileId == null || familyId == null) {
            throw new AccessDeniedException("Cần có thông tin gia đình và hồ sơ sức khỏe");
        }

        checkUserBelongsToFamily(familyId);
        checkCanReadHealthProfile(profileId);

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

