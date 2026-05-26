package com.example.carenest.network

import com.example.carenest.data.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val dataStoreManager: DataStoreManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // Read token and familyId synchronously since OkHttp interceptors run on background threads
        val token = runBlocking { dataStoreManager.tokenFlow.first() }
        val familyId = runBlocking { dataStoreManager.familyIdFlow.first() }

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val urlPath = chain.request().url.encodedPath
        if (!familyId.isNullOrEmpty() && shouldAttachActiveFamilyHeader(urlPath)) {
            requestBuilder.addHeader("X-Family-Id", familyId)
        }

        return chain.proceed(requestBuilder.build())
    }

    private fun shouldAttachActiveFamilyHeader(url: String): Boolean {
        return !url.startsWith("/auth") &&
               !url.startsWith("/media") &&
               !url.startsWith("/articles") &&
               !url.startsWith("/communities") &&
               !url.startsWith("/admin") &&
               !url.startsWith("/families/my-list") &&
               !url.startsWith("/families/join-by-code") &&
               !url.startsWith("/families/join-by-qr")
    }
}
