package com.carenest.backend.module.auth.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.auth.dto.request.LoginRequest;
import com.carenest.backend.module.auth.dto.request.RegisterRequest;
import com.carenest.backend.module.auth.dto.response.AuthResponse;
import com.carenest.backend.module.auth.dto.response.UserInfoResponse;
import com.carenest.backend.module.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletRequest httpRequest) {
        if (!resolveBucket(httpRequest).tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Quá nhiều yêu cầu. Vui lòng thử lại sau."));
        }
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký tài khoản thành công", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest) {
        if (!resolveBucket(httpRequest).tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Quá nhiều yêu cầu. Vui lòng thử lại sau."));
        }
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @GetMapping("/me")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        UserInfoResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateCurrentUser(@RequestBody @Valid com.carenest.backend.module.auth.dto.request.UpdateUserRequest request) {
        UserInfoResponse response = authService.updateCurrentUser(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin tài khoản thành công", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody @Valid com.carenest.backend.module.auth.dto.request.RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Làm mới phiên đăng nhập thành công", response));
    }

    private Bucket resolveBucket(HttpServletRequest request) {
        String key = resolveClientIp(request);
        return buckets.computeIfAbsent(key, ignored -> {
            Refill refill = Refill.greedy(5, Duration.ofMinutes(1));
            Bandwidth limit = Bandwidth.classic(5, refill);
            return Bucket.builder().addLimit(limit).build();
        });
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
