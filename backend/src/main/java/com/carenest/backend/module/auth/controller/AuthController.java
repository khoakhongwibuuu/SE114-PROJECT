package com.carenest.backend.module.auth.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.auth.dto.request.LoginRequest;
import com.carenest.backend.module.auth.dto.request.RegisterRequest;
import com.carenest.backend.module.auth.dto.response.AuthResponse;
import com.carenest.backend.module.auth.dto.response.UserInfoResponse;
import com.carenest.backend.module.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final Bucket bucket;

    public AuthController(AuthService authService) {
        this.authService = authService;
        Refill refill = Refill.greedy(5, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(5, refill);
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody @Valid RegisterRequest request) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many requests. Please try again later."));
        }
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest request) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many requests. Please try again later."));
        }
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        UserInfoResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateCurrentUser(@RequestBody @Valid com.carenest.backend.module.auth.dto.request.UpdateUserRequest request) {
        UserInfoResponse response = authService.updateCurrentUser(request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody @Valid com.carenest.backend.module.auth.dto.request.RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }
}
