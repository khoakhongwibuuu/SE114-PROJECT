package com.example.carenest.network

import com.example.carenest.model.ApiResponse
import com.example.carenest.model.AuthResponse
import com.example.carenest.model.LoginRequest
import com.example.carenest.model.RefreshTokenRequest
import com.example.carenest.model.RegisterRequest
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
