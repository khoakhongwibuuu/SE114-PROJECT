package com.example.carenest.core.data.network

import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.auth.domain.model.AuthResponse
import com.example.carenest.feature.auth.domain.model.RefreshTokenRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val secureSessionManager: SecureSessionManager,
    private val baseUrl: String,
) : Authenticator {
    private val gson = Gson()
    private val refreshClient = OkHttpClient.Builder().build()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            return null
        }

        val refreshToken = secureSessionManager.getRefreshToken() ?: return null
        val refreshResponse = refreshAccessToken(refreshToken) ?: return null

        runBlocking {
            secureSessionManager.saveSession(refreshResponse.accessToken, refreshResponse.refreshToken)
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${refreshResponse.accessToken}")
            .build()
    }

    private fun refreshAccessToken(refreshToken: String): AuthResponse? {
        val body = gson.toJson(RefreshTokenRequest(refreshToken))
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/v1/auth/refresh")
            .post(body)
            .build()

        return try {
            refreshClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val type = object : TypeToken<ApiResponse<AuthResponse>>() {}.type
                val envelope: ApiResponse<AuthResponse> = gson.fromJson(response.body?.string(), type)
                envelope.data
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
