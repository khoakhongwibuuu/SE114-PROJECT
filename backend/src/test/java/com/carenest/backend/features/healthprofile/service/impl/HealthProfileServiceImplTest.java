package com.carenest.backend.features.healthprofile.service.impl;

import com.carenest.backend.core.exception.DuplicateResourceException;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Gender;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.family.context.FamilyRequestContext;
import com.carenest.backend.features.family.entity.Family;
import com.carenest.backend.features.family.repository.FamilyRepository;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import com.carenest.backend.features.growth.repository.GrowthRecordRepository;
import com.carenest.backend.features.healthprofile.dto.request.HealthProfileCreateRequest;
import com.carenest.backend.features.healthprofile.dto.response.HealthProfileResponse;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import com.carenest.backend.features.healthprofile.mapper.HealthProfileMapper;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthProfileServiceImplTest {

    @Mock private HealthProfileRepository healthProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private FamilyRepository familyRepository;
    @Mock private HealthProfileMapper healthProfileMapper;
    @Mock private GrowthRecordRepository growthRecordRepository;
    @Mock private FamilySecurityUtil familySecurityUtil;

    @InjectMocks
    private HealthProfileServiceImpl healthProfileService;

    private User user;
    private Family family;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("parent@example.com")
                .fullName("Parent")
                .passwordHash("hash")
                .gender(Gender.OTHER)
                .dateOfBirth(LocalDate.of(1995, 1, 1))
                .build();
        user.setId(1L);

        family = Family.builder()
                .name("Family One")
                .owner(user)
                .build();
        family.setId(10L);
    }

    @AfterEach
    void tearDown() {
        FamilyRequestContext.clear();
    }

    @Test
    void createHealthProfile_rejectsDuplicateProfileInSameFamily() {
        HealthProfileCreateRequest request = HealthProfileCreateRequest.builder()
                .familyId(10L)
                .fullName("Kid")
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .gender(Gender.OTHER)
                .relationship("Child")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(familySecurityUtil).checkUserBelongsToFamily(10L);
        when(familyRepository.findById(10L)).thenReturn(Optional.of(family));
        when(healthProfileRepository.existsByUserIdAndFamilyIdAndDeletedAtIsNull(1L, 10L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> healthProfileService.createHealthProfile(1L, request));
    }

    @Test
    void getMyHealthProfile_throwsWhenNoProfileExists() {
        when(healthProfileRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> healthProfileService.getMyHealthProfile(1L));
    }

    @Test
    void getMyHealthProfile_prefersActiveFamilyProfile() {
        FamilyRequestContext.set(20L, null);

        Family otherFamily = Family.builder().name("Family Two").owner(user).build();
        otherFamily.setId(20L);

        HealthProfile first = HealthProfile.builder()
                .user(user)
                .family(family)
                .fullName("Family One Profile")
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .gender(Gender.OTHER)
                .build();
        first.setId(100L);

        HealthProfile second = HealthProfile.builder()
                .user(user)
                .family(otherFamily)
                .fullName("Family Two Profile")
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .gender(Gender.OTHER)
                .build();
        second.setId(200L);

        HealthProfileResponse mapped = new HealthProfileResponse();
        mapped.setId(200L);
        mapped.setFullName("Family Two Profile");

        when(healthProfileRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(List.of(first, second));
        when(healthProfileMapper.toResponse(second)).thenReturn(mapped);
        when(growthRecordRepository.findByHealthProfileIdOrderByRecordDateDesc(200L)).thenReturn(List.of());

        HealthProfileResponse response = healthProfileService.getMyHealthProfile(1L);

        assertEquals(200L, response.getId());
        assertEquals("Family Two Profile", response.getFullName());
    }
}
