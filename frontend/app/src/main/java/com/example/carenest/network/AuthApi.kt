package com.example.carenest.network

import com.example.carenest.model.LoginRequest
import com.example.carenest.model.LoginResponse
import com.example.carenest.model.RegisterRequest
import com.example.carenest.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}
