package com.example.carenest.feature.auth.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.auth.domain.model.AuthResponse
import com.example.carenest.feature.auth.domain.model.LoginRequest
import com.example.carenest.feature.auth.domain.model.RefreshTokenRequest
import com.example.carenest.feature.auth.domain.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("/api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): Response<ApiResponse<AuthResponse>>
}
