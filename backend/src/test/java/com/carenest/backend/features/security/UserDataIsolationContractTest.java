package com.carenest.backend.features.security;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.family.context.FamilyRequestContext;
import com.carenest.backend.features.family.entity.Family;
import com.carenest.backend.features.family.entity.FamilyMember;
import com.carenest.backend.features.family.enums.FamilyRole;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDataIsolationContractTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FamilyMemberRepository familyMemberRepository;
    @Mock
    private HealthProfileRepository healthProfileRepository;

    private FamilySecurityUtil familySecurityUtil;
    private User currentUser;

    @BeforeEach
    void setUp() {
        familySecurityUtil = new FamilySecurityUtil(userRepository, familyMemberRepository, healthProfileRepository);
        currentUser = User.builder()
                .email("parent@example.com")
                .fullName("Parent")
                .passwordHash("hash")
                .build();
        currentUser.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("parent@example.com", "n/a"));
        when(userRepository.findByEmail("parent@example.com")).thenReturn(Optional.of(currentUser));
    }

    @AfterEach
    void tearDown() {
        FamilyRequestContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void activeFamilyContext_deniesProfileFromAnotherFamily() {
        Family activeFamily = family(10L);
        Family otherFamily = family(20L);
        HealthProfile profile = profile(99L, otherFamily, currentUser);
        FamilyRequestContext.set(activeFamily.getId(), FamilyRole.MEMBER);

        when(healthProfileRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.of(profile));

        assertThrows(
                AccessDeniedException.class,
                () -> familySecurityUtil.checkCanReadHealthProfile(99L));
    }

    @Test
    void familyProfilePair_deniesProfileFromDifferentFamilyEvenIfUserBelongsToBoth() {
        Family requestedFamily = family(10L);
        Family profileFamily = family(20L);
        HealthProfile profile = profile(99L, profileFamily, currentUser);

        when(familyMemberRepository.findByFamilyIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(member(requestedFamily, currentUser)));
        when(familyMemberRepository.findByFamilyIdAndUserId(20L, 1L))
                .thenReturn(Optional.of(member(profileFamily, currentUser)));
        when(healthProfileRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.of(profile));

        assertThrows(
                AccessDeniedException.class,
                () -> familySecurityUtil.checkHealthProfileBelongsToFamily(99L, 10L));
    }

    private Family family(Long id) {
        Family family = Family.builder()
                .name("Family " + id)
                .owner(currentUser)
                .build();
        family.setId(id);
        return family;
    }

    private FamilyMember member(Family family, User user) {
        return FamilyMember.builder()
                .family(family)
                .user(user)
                .role(FamilyRole.MEMBER)
                .build();
    }

    private HealthProfile profile(Long id, Family family, User user) {
        HealthProfile profile = HealthProfile.builder()
                .family(family)
                .user(user)
                .fullName("Child")
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .gender(com.carenest.backend.features.auth.enums.Gender.OTHER)
                .build();
        profile.setId(id);
        return profile;
    }
}
