package com.carenest.backend.features.family.util;

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
                .orElseThrow(() -> new UnauthorizedException("Vui lÃ²ng Ä‘Äƒng nháº­p"));
    }

    public void checkUserBelongsToFamily(Long familyId) {
        User currentUser = getCurrentUser();
        boolean belongs = familyMemberRepository.findByFamilyIdAndUserId(familyId, currentUser.getId()).isPresent();
        if (!belongs) {
            throw new AccessDeniedException("Báº¡n khÃ´ng cÃ³ quyá»n truy cáº­p gia Ä‘Ã¬nh nÃ y");
        }
    }

    public void checkUserBelongsToHealthProfile(Long profileId) {
        User currentUser = getCurrentUser();
        HealthProfile profile = healthProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Há»“ sÆ¡ sá»©c khá»e khÃ´ng tá»“n táº¡i"));

        Long activeFamilyId = FamilyRequestContext.getFamilyId();
        if (activeFamilyId != null
                && profile.getFamily() != null
                && !profile.getFamily().getId().equals(activeFamilyId)) {
            throw new AccessDeniedException("Há»“ sÆ¡ sá»©c khá»e khÃ´ng thuá»™c gia Ä‘Ã¬nh Ä‘ang chá»n");
        }

        if (profile.getFamily() != null) {
            boolean belongs = familyMemberRepository
                    .findByFamilyIdAndUserId(profile.getFamily().getId(), currentUser.getId())
                    .isPresent();
            if (!belongs) {
                throw new AccessDeniedException("Báº¡n khÃ´ng cÃ³ quyá»n truy cáº­p há»“ sÆ¡ sá»©c khá»e nÃ y");
            }
            return;
        }

        if (profile.getUser() == null || !profile.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Báº¡n khÃ´ng cÃ³ quyá»n truy cáº­p há»“ sÆ¡ sá»©c khá»e cÃ¡ nhÃ¢n nÃ y");
        }
    }

    public void checkHealthProfileBelongsToFamily(Long profileId, Long familyId) {
        if (profileId == null || familyId == null) {
            throw new AccessDeniedException("Cáº§n cÃ³ thÃ´ng tin gia Ä‘Ã¬nh vÃ  há»“ sÆ¡ sá»©c khá»e");
        }

        checkUserBelongsToFamily(familyId);
        checkUserBelongsToHealthProfile(profileId);

        HealthProfile profile = healthProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Há»“ sÆ¡ sá»©c khá»e khÃ´ng tá»“n táº¡i"));

        if (profile.getFamily() == null || !profile.getFamily().getId().equals(familyId)) {
            throw new AccessDeniedException("Há»“ sÆ¡ sá»©c khá»e khÃ´ng thuá»™c gia Ä‘Ã¬nh nÃ y");
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
