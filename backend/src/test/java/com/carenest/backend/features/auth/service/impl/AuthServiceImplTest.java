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

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import com.carenest.backend.features.auth.dto.request.ForgotPasswordRequest;
import com.carenest.backend.features.auth.dto.request.ResetPasswordRequest;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

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
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;

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

    @Test
    void forgotPassword_success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        authService.forgotPassword(request);

        verify(valueOperations).set(eq("otp:test@example.com"), any(String.class), any());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void forgotPassword_userNotFound() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("notfound@example.com");

        when(userRepository.existsByEmail("notfound@example.com")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> authService.forgotPassword(request));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void resetPassword_success() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newpassword123");
        request.setConfirmPassword("newpassword123");

        User user = user("test@example.com");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:test@example.com")).thenReturn("123456");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword123")).thenReturn("new_hash");

        authService.resetPassword(request);

        assertEquals("new_hash", user.getPasswordHash());
        verify(userRepository).save(user);
        verify(redisTemplate).delete("otp:test@example.com");
    }

    @Test
    void resetPassword_otpExpired() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newpassword123");
        request.setConfirmPassword("newpassword123");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:test@example.com")).thenReturn(null);

        assertThrows(BadRequestException.class, () -> authService.resetPassword(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_otpMismatch() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newpassword123");
        request.setConfirmPassword("newpassword123");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:test@example.com")).thenReturn("654321");

        assertThrows(BadRequestException.class, () -> authService.resetPassword(request));
        verify(userRepository, never()).save(any());
    }
}
