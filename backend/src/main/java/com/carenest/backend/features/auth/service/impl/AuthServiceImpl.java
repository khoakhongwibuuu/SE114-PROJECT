package com.carenest.backend.features.auth.service.impl;

import com.carenest.backend.config.security.JwtService;
import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.core.exception.DuplicateResourceException;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.auth.dto.request.LoginRequest;
import com.carenest.backend.features.auth.dto.request.RegisterRequest;
import com.carenest.backend.features.auth.dto.response.AuthResponse;
import com.carenest.backend.features.auth.dto.response.UserInfoResponse;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Gender;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.mapper.UserMapper;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carenest.backend.features.auth.dto.request.ForgotPasswordRequest;
import com.carenest.backend.features.auth.dto.request.ResetPasswordRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, Object> redisTemplate;

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
        String email = request.getEmail() != null ? request.getEmail().trim() : null;

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return userMapper.toUserInfoResponse(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .user(userMapper.toUserInfoResponse(user))
                        .build();
            }
        }
        throw new IllegalArgumentException("Refresh token không hợp lệ");
    }

    @Override
    @Transactional
    public UserInfoResponse updateCurrentUser(
            com.carenest.backend.features.auth.dto.request.UpdateUserRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setDateOfBirth(request.getDateOfBirth());

        if (request.getGender() != null) {
            try {
                user.setGender(Gender.valueOf(request.getGender().trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Gioi tinh khong hop le");
            }
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        User savedUser = userRepository.save(user);
        return userMapper.toUserInfoResponse(savedUser);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        if (email == null) {
            throw new BadRequestException("Email không được để trống");
        }

        if (!userRepository.existsByEmail(email)) {
            throw new ResourceNotFoundException("Không tìm thấy tài khoản với email này");
        }

        String otp = generateOtp();
        redisTemplate.opsForValue().set("otp:" + email, otp, Duration.ofMinutes(5));

        try {
            sendOtpEmail(email, otp);
        } catch (Exception e) {
            throw new BadRequestException("Không thể gửi email OTP. Vui lòng thử lại sau.");
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        if (email == null) {
            throw new BadRequestException("Email không được để trống");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }

        String cachedOtp = (String) redisTemplate.opsForValue().get("otp:" + email);
        if (cachedOtp == null) {
            throw new BadRequestException("Mã OTP đã hết hạn hoặc không tồn tại. Vui lòng yêu cầu mã mới.");
        }

        if (!cachedOtp.equals(request.getOtp().trim())) {
            throw new BadRequestException("Mã OTP không chính xác");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete("otp:" + email);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int num = 100000 + random.nextInt(900000);
        return String.valueOf(num);
    }

    private void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[CareNest] Mã OTP xác nhận đặt lại mật khẩu");
        message.setText("Chào bạn,\n\n" +
                "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản CareNest.\n" +
                "Mã OTP của bạn là: " + otp + "\n" +
                "Mã này có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n" +
                "Thân mến,\n" +
                "Đội ngũ CareNest");
        mailSender.send(message);
    }
}
