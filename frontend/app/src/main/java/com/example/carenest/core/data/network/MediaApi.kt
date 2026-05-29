package com.example.carenest.core.data.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface MediaApi {
    @Multipart
    @POST("/api/v1/media/upload")
    suspend fun upload(
        @Part file: MultipartBody.Part,
        @Query("category") category: String? = null
    ): Response<ApiResponse<MediaUploadResponse>>
}

data class MediaUploadResponse(
    val fileName: String,
    val contentType: String?,
    val size: Long,
    val url: String
)
