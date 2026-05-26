package com.example.carenest.network

import com.example.carenest.data.DataStoreManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val dataStoreManager: DataStoreManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()

        dataStoreManager.getAccessToken()?.takeIf { it.isNotBlank() }?.let { token ->
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val urlPath = original.url.encodedPath.removePrefix("/api/v1")
        dataStoreManager.getFamilyId()?.takeIf { it.isNotBlank() }?.let { familyId ->
            if (shouldAttachActiveFamilyHeader(urlPath)) {
                requestBuilder.header("X-Family-Id", familyId)
            }
        }

        return chain.proceed(requestBuilder.build())
    }

    private fun shouldAttachActiveFamilyHeader(url: String): Boolean {
        return !url.startsWith("/auth") &&
            !url.startsWith("/media") &&
            !url.startsWith("/articles") &&
            !url.startsWith("/communities") &&
            !url.startsWith("/posts") &&
            !url.startsWith("/admin") &&
            !url.startsWith("/families/my-list") &&
            !url.startsWith("/families/join-by-code") &&
            !url.startsWith("/families/join-by-qr")
    }
}
