package com.example.carenest.feature.community.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.chat.domain.model.ChatGroup
import com.example.carenest.feature.chat.domain.model.ChatGroupPreview
import com.example.carenest.feature.community.domain.model.CreateGroupPostRequest
import com.example.carenest.feature.community.domain.model.CreateArticleCommentRequest
import com.example.carenest.feature.community.domain.model.CreateArticleRequest
import com.example.carenest.feature.community.domain.model.GroupPost
import com.example.carenest.feature.community.domain.model.PageResponse
import com.example.carenest.feature.community.domain.model.Article
import com.example.carenest.feature.community.domain.model.ArticleComment
import com.example.carenest.feature.community.domain.model.ArticleLikeResponse
import com.example.carenest.feature.community.domain.model.GroupPostComment
import com.example.carenest.feature.community.domain.model.CreateGroupPostCommentRequest
import com.example.carenest.feature.community.domain.model.GroupPostInteractionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class ReportPostRequest(
    val reason: String
)

interface CommunityApi {
    @GET("/api/v1/communities/my")
    suspend fun myGroups(@Query("search") search: String? = null): Response<ApiResponse<List<ChatGroup>>>

    @GET("/api/v1/communities/discover")
    suspend fun discoverGroups(@Query("search") search: String? = null): Response<ApiResponse<List<ChatGroup>>>

    @GET("/api/v1/communities/{id}/preview")
    suspend fun preview(@Path("id") id: Long): Response<ApiResponse<ChatGroupPreview>>

    @POST("/api/v1/communities/{id}/join")
    suspend fun join(@Path("id") id: Long): Response<ApiResponse<ChatGroupPreview>>

    @GET("/api/v1/communities/{id}/posts")
    suspend fun posts(
        @Path("id") id: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30
    ): Response<ApiResponse<PageResponse<GroupPost>>>

    @GET("/api/v1/communities/{id}/posts/my")
    suspend fun myPosts(
        @Path("id") id: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30
    ): Response<ApiResponse<PageResponse<GroupPost>>>

    @GET("/api/v1/communities/{id}/posts/pending")
    suspend fun pendingPosts(
        @Path("id") id: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30
    ): Response<ApiResponse<PageResponse<GroupPost>>>

    @POST("/api/v1/communities/posts/{id}/approve")
    suspend fun approvePost(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>

    @POST("/api/v1/communities/posts/{id}/reject")
    suspend fun rejectPost(
        @Path("id") id: Long,
        @Query("reason") reason: String
    ): Response<ApiResponse<Unit>>

    @POST("/api/v1/communities/{id}/posts")
    suspend fun sendPost(
        @Path("id") id: Long,
        @Body request: CreateGroupPostRequest
    ): Response<ApiResponse<GroupPost>>

    @POST("/api/v1/communities/posts/{id}/like")
    suspend fun likeGroupPost(
        @Path("id") id: Long
    ): Response<ApiResponse<GroupPostInteractionResponse>>

    @GET("/api/v1/communities/posts/{id}/comments")
    suspend fun getGroupPostComments(
        @Path("id") id: Long
    ): Response<ApiResponse<PageResponse<GroupPostComment>>>

    @POST("/api/v1/communities/posts/{id}/comments")
    suspend fun createGroupPostComment(
        @Path("id") id: Long,
        @Body request: CreateGroupPostCommentRequest
    ): Response<ApiResponse<GroupPostComment>>

    @GET("/api/v1/articles")
    suspend fun getArticles(): Response<ApiResponse<List<Article>>>

    @POST("/api/v1/articles")
    suspend fun createArticle(
        @Body request: CreateArticleRequest
    ): Response<ApiResponse<Article>>

    @POST("/api/v1/articles/{id}/like")
    suspend fun toggleArticleLike(
        @Path("id") id: Long
    ): Response<ApiResponse<ArticleLikeResponse>>

    @GET("/api/v1/articles/{id}/comments")
    suspend fun getArticleComments(
        @Path("id") id: Long
    ): Response<ApiResponse<List<ArticleComment>>>

    @POST("/api/v1/articles/{id}/comments")
    suspend fun createArticleComment(
        @Path("id") id: Long,
        @Body request: CreateArticleCommentRequest
    ): Response<ApiResponse<ArticleComment>>

    @POST("/api/v1/communities/{id}/leave")
    suspend fun leave(@Path("id") id: Long): Response<ApiResponse<Unit>>

    @DELETE("/api/v1/communities/{id}/members/{userId}")
    suspend fun kickMember(
        @Path("id") id: Long,
        @Path("userId") userId: Long
    ): Response<ApiResponse<Unit>>

    @POST("/api/v1/posts/{id}/report")
    suspend fun reportPost(
        @Path("id") id: Long,
        @Body request: ReportPostRequest
    ): Response<ApiResponse<Unit>>

    @POST("/api/v1/group-requests")
    suspend fun createGroupRequest(
        @Body request: com.example.carenest.feature.community.domain.model.CreateGroupCreationRequest
    ): Response<ApiResponse<com.example.carenest.feature.community.domain.model.GroupCreationRequest>>

    @GET("/api/v1/group-requests/mine")
    suspend fun getMyGroupRequests(): Response<ApiResponse<List<com.example.carenest.feature.community.domain.model.GroupCreationRequest>>>

    @GET("/api/v1/admin/group-requests")
    suspend fun getAdminGroupRequests(): Response<ApiResponse<List<com.example.carenest.feature.community.domain.model.GroupCreationRequest>>>

    @POST("/api/v1/admin/group-requests/{id}/approve")
    suspend fun approveGroupRequest(@Path("id") id: Long): Response<ApiResponse<Unit>>

    @POST("/api/v1/admin/group-requests/{id}/reject")
    suspend fun rejectGroupRequest(
        @Path("id") id: Long,
        @Query("reason") reason: String
    ): Response<ApiResponse<Unit>>

    @GET("/api/v1/communities/{id}/members")
    suspend fun getMembers(@Path("id") id: Long): Response<ApiResponse<List<com.example.carenest.feature.community.domain.model.GroupMember>>>

    @POST("/api/v1/admin/groups/{id}/freeze")
    suspend fun freezeGroup(@Path("id") id: Long): Response<ApiResponse<Unit>>

    @POST("/api/v1/admin/groups/{id}/unfreeze")
    suspend fun unfreezeGroup(@Path("id") id: Long): Response<ApiResponse<Unit>>

    @PATCH("/api/v1/communities/{id}/members/{userId}/role")
    suspend fun updateMemberRole(
        @Path("id") id: Long,
        @Path("userId") userId: Long,
        @Body request: com.example.carenest.feature.community.domain.model.UpdateGroupRoleRequest
    ): Response<ApiResponse<Unit>>

    @GET("/api/v1/chat/groups/{id}/messages")
    suspend fun groupMessages(
        @Path("id") id: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<ApiResponse<com.example.carenest.feature.community.domain.model.PageResponse<com.example.carenest.feature.community.data.remote.ChatMessageResponseDto>>>

    @POST("/api/v1/chat/groups/{id}/messages")
    suspend fun sendGroupMessage(
        @Path("id") id: Long,
        @Body request: com.example.carenest.feature.community.data.remote.SendGroupMessageRequest
    ): Response<ApiResponse<com.example.carenest.feature.community.data.remote.ChatMessageResponseDto>>

    @POST("/api/v1/chat/messages/{id}/report")
    suspend fun reportGroupMessage(
        @Path("id") id: Long,
        @Body request: com.example.carenest.feature.community.data.remote.ReportPostRequest
    ): Response<ApiResponse<Unit>>

    @POST("/api/v1/communities/posts/{id}/update")
    suspend fun updatePost(
        @Path("id") id: Long,
        @Body request: com.example.carenest.feature.community.domain.model.CreateGroupPostRequest
    ): Response<ApiResponse<com.example.carenest.feature.community.domain.model.GroupPost>>

    @DELETE("/api/v1/communities/posts/{id}")
    suspend fun deletePost(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>
}
