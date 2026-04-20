package com.carenest.backend.module.auth.service.impl;

import com.carenest.backend.common.exception.DuplicateResourceException;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.config.security.JwtService;
import com.carenest.backend.module.auth.dto.request.LoginRequest;
import com.carenest.backend.module.auth.dto.request.RegisterRequest;
import com.carenest.backend.module.auth.dto.response.AuthResponse;
import com.carenest.backend.module.auth.dto.response.UserInfoResponse;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.auth.enums.Role;
import com.carenest.backend.module.auth.mapper.UserMapper;
import com.carenest.backend.module.auth.repository.UserRepository;
import com.carenest.backend.module.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Account", "email", request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setIsActive(true);
        user.setIsVerified(false); // Can trigger email verification logic later

        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .user(userMapper.toUserInfoResponse(user))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Authenticate credentials against SecurityContext
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .user(userMapper.toUserInfoResponse(user))
                .build();
    }

    @Override
    public UserInfoResponse getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserInfoResponse(user);
    }
}
