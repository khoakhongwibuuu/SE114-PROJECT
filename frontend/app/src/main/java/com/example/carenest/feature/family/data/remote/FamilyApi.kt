package com.example.carenest.feature.family.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.family.domain.model.CreateFamilyRequest
import com.example.carenest.feature.family.domain.model.FamilyDetailResponse
import com.example.carenest.feature.family.domain.model.FamilyInvitationItem
import com.example.carenest.feature.family.domain.model.FamilyJoinCodeResponse
import com.example.carenest.feature.family.domain.model.FamilyResponse
import com.example.carenest.feature.family.domain.model.FamilySummary
import com.example.carenest.feature.family.domain.model.InviteMemberRequest
import com.example.carenest.feature.family.domain.model.JoinFamilyByCodeRequest
import com.example.carenest.feature.family.domain.model.UpdateInvitationRequest
import com.example.carenest.feature.family.domain.model.FamilyChatPageResponse
import com.example.carenest.feature.family.domain.model.UpdateMedicalInfoRequest
import com.example.carenest.feature.family.domain.model.UpdateProfileDetailsRequest
import com.example.carenest.feature.family.domain.model.CreateDependentRequest
import com.example.carenest.model.RawHealthProfileResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface FamilyApi {
    @GET("/api/v1/families/{id}/messages")
    suspend fun getChatHistory(
        @Path("id") familyId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<FamilyChatPageResponse>>
    @GET("/api/v1/families/my-list")
    suspend fun getMyFamilyList(): Response<ApiResponse<List<FamilySummary>>>

    @GET("/api/v1/families/{id}")
    suspend fun getFamilyById(@Path("id") familyId: Long): Response<ApiResponse<FamilyDetailResponse>>

    @GET("/api/v1/health-profiles/{id}")
    suspend fun getFamilyProfile(@Path("id") profileId: Long): Response<ApiResponse<RawHealthProfileResponse>>

    @GET("/api/v1/health-profiles/me")
    suspend fun getMyHealthProfile(): Response<ApiResponse<RawHealthProfileResponse>>

    @POST("/api/v1/families")
    suspend fun createFamily(@Body request: CreateFamilyRequest): Response<ApiResponse<FamilyResponse>>

    @POST("/api/v1/profiles/dependents")
    suspend fun createDependentProfile(@Body request: CreateDependentRequest): Response<ApiResponse<RawHealthProfileResponse>>

    @POST("/api/v1/families/join-by-code")
    suspend fun joinFamilyByCode(@Body request: JoinFamilyByCodeRequest): Response<ApiResponse<FamilyDetailResponse>>

    @Multipart
    @POST("/api/v1/families/join-by-qr")
    suspend fun joinFamilyByQr(
        @Part image: MultipartBody.Part,
        @Query("role") role: String? = null
    ): Response<ApiResponse<FamilyDetailResponse>>

    @POST("/api/v1/families/{id}/invitations")
    suspend fun inviteMember(
        @Path("id") familyId: Long,
        @Body request: InviteMemberRequest
    ): Response<ApiResponse<Unit>>

    @GET("/api/v1/invitations/received")
    suspend fun getReceivedInvitations(): Response<ApiResponse<List<FamilyInvitationItem>>>

    @GET("/api/v1/invitations/sent")
    suspend fun getSentInvitations(): Response<ApiResponse<List<FamilyInvitationItem>>>

    @PUT("/api/v1/invitations/{id}")
    suspend fun updateInvitationStatus(
        @Path("id") inviteId: Long,
        @Body request: UpdateInvitationRequest
    ): Response<ApiResponse<Unit>>

    @GET("/api/v1/families/join-code")
    suspend fun getFamilyJoinCode(): Response<ApiResponse<FamilyJoinCodeResponse>>

    @POST("/api/v1/families/join-code/rotate")
    suspend fun rotateFamilyJoinCode(): Response<ApiResponse<FamilyJoinCodeResponse>>

    @PUT("/api/v1/health-profiles/{id}")
    suspend fun updateProfileDetails(
        @Path("id") profileId: Long,
        @Body request: UpdateProfileDetailsRequest
    ): Response<ApiResponse<RawHealthProfileResponse>>

    @PUT("/api/v1/health-profiles/{id}/medical-info")
    suspend fun updateProfileMedicalInfo(
        @Path("id") profileId: Long,
        @Body request: UpdateMedicalInfoRequest
    ): Response<ApiResponse<RawHealthProfileResponse>>

    @Multipart
    @POST("/api/v1/media/upload")
    suspend fun uploadMedia(
        @Part file: MultipartBody.Part,
        @Query("category") category: String? = null
    ): Response<ApiResponse<com.example.carenest.core.data.network.MediaUploadResponse>>
}
