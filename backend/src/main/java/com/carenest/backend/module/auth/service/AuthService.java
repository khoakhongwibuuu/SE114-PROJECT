package com.carenest.backend.module.auth.service;

import com.carenest.backend.module.auth.dto.request.LoginRequest;
import com.carenest.backend.module.auth.dto.request.RegisterRequest;
import com.carenest.backend.module.auth.dto.response.AuthResponse;
import com.carenest.backend.module.auth.dto.response.UserInfoResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserInfoResponse getCurrentUser();
}
