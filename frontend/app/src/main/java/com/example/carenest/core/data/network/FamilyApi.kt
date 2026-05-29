package com.example.carenest.network

import com.example.carenest.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface FamilyApi {
    @GET("/families/my-list")
    suspend fun getMyFamilyList(): Response<List<FamilySummary>>

    @GET("/families/{id}")
    suspend fun getFamilyById(@Path("id") familyId: Int): Response<FamilyDetailResponse>

    @GET("/health-profiles/{id}")
    suspend fun getFamilyProfile(@Path("id") profileId: Int): Response<RawHealthProfileResponse>

    @POST("/families")
    suspend fun createFamily(@Body request: CreateFamilyRequest): Response<Unit>

    @POST("/families/join-by-code")
    suspend fun joinFamilyByCode(@Body request: JoinFamilyByCodeRequest): Response<FamilyDetailResponse>

    @Multipart
    @POST("/families/join-by-qr")
    suspend fun joinFamilyByQr(
        @Part role: MultipartBody.Part,
        @Part image: MultipartBody.Part
    ): Response<FamilyDetailResponse>

    @POST("/families/{id}/invitations")
    suspend fun inviteMember(
        @Path("id") familyId: Int,
        @Body request: InviteMemberRequest
    ): Response<Unit>

    @GET("/invitations/received")
    suspend fun getReceivedInvitations(): Response<List<FamilyInvitationItem>>

    @GET("/invitations/sent")
    suspend fun getSentInvitations(): Response<List<FamilyInvitationItem>>

    @PUT("/invitations/{id}")
    suspend fun updateInvitationStatus(
        @Path("id") inviteId: Int,
        @Body request: UpdateInvitationRequest
    ): Response<Unit>

    @GET("/families/join-code")
    suspend fun getFamilyJoinCode(): Response<FamilyJoinCodeResponse>

    @POST("/families/join-code/rotate")
    suspend fun rotateFamilyJoinCode(): Response<FamilyJoinCodeResponse>
}
