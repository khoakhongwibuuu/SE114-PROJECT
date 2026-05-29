package com.carenest.backend.features.auth.service;

import com.carenest.backend.features.auth.dto.request.LoginRequest;
import com.carenest.backend.features.auth.dto.request.RegisterRequest;
import com.carenest.backend.features.auth.dto.response.AuthResponse;
import com.carenest.backend.features.auth.dto.response.UserInfoResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserInfoResponse getCurrentUser();
    UserInfoResponse updateCurrentUser(com.carenest.backend.features.auth.dto.request.UpdateUserRequest request);
    AuthResponse refreshToken(String refreshToken);
}
