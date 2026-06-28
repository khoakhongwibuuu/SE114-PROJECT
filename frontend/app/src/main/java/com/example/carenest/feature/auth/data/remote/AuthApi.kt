package com.example.carenest.feature.auth.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.auth.domain.model.AuthResponse
import com.example.carenest.feature.auth.domain.model.LoginRequest
import com.example.carenest.feature.auth.domain.model.ForgotPasswordRequest
import com.example.carenest.feature.auth.domain.model.RefreshTokenRequest
import com.example.carenest.feature.auth.domain.model.GoogleLoginRequest
import com.example.carenest.feature.auth.domain.model.RegisterRequest
import com.example.carenest.feature.auth.domain.model.ResetPasswordRequest
import com.example.carenest.feature.auth.domain.model.UpdateCurrentUserRequest
import com.example.carenest.feature.auth.domain.model.UserInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApi {
    @GET("/api/v1/auth/me")
    suspend fun getMe(): Response<ApiResponse<UserInfo>>

    @PUT("/api/v1/auth/me")
    suspend fun updateCurrentUser(@Body request: UpdateCurrentUserRequest): Response<ApiResponse<UserInfo>>

    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("/api/v1/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("/api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): Response<ApiResponse<AuthResponse>>

    @POST("/api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Unit>>

    @POST("/api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Unit>>
}
