package com.carenest.backend.features.family.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class FamilySecurityUtilTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FamilyMemberRepository familyMemberRepository;

    @Mock
    private HealthProfileRepository healthProfileRepository;

    @InjectMocks
    private FamilySecurityUtil familySecurityUtil;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void checkCanReadHealthProfile_rejectsDeletedProfile() {
        User currentUser = User.builder()
                .email("user@example.com")
                .passwordHash("hash")
                .fullName("User")
                .build();
        currentUser.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", "token"));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(currentUser));
        when(healthProfileRepository.findByIdAndDeletedAtIsNull(55L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> familySecurityUtil.checkCanReadHealthProfile(55L));

        verifyNoInteractions(familyMemberRepository);
    }
}
