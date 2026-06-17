package com.carenest.backend.features.auth.service.impl;

import com.carenest.backend.config.security.JwtService;
import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.features.auth.dto.request.UpdateUserRequest;
import com.carenest.backend.features.auth.dto.response.UserInfoResponse;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Gender;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.mapper.UserMapper;
import com.carenest.backend.features.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateCurrentUser_rejectsInvalidGender() {
        User user = user("patient@example.com");
        UpdateUserRequest request = updateRequest("UNKNOWN");

        authenticateAs(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> authService.updateCurrentUser(request));

        verify(userRepository, never()).save(user);
    }

    @Test
    void updateCurrentUser_normalizesGenderInput() {
        User user = user("patient@example.com");
        UpdateUserRequest request = updateRequest(" female ");
        UserInfoResponse response = new UserInfoResponse();
        response.setGender("FEMALE");

        authenticateAs(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserInfoResponse(user)).thenReturn(response);

        UserInfoResponse result = authService.updateCurrentUser(request);

        assertEquals(Gender.FEMALE, user.getGender());
        assertEquals("FEMALE", result.getGender());
        verify(userRepository).save(user);
    }

    private void authenticateAs(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, "n/a")
        );
    }

    private User user(String email) {
        return User.builder()
                .email(email)
                .passwordHash("hash")
                .fullName("Patient User")
                .role(Role.USER)
                .isActive(true)
                .isVerified(false)
                .build();
    }

    private UpdateUserRequest updateRequest(String gender) {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("Patient User");
        request.setGender(gender);
        return request;
    }
}
