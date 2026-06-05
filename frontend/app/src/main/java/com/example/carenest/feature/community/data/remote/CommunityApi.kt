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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
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

    @POST("/api/v1/communities/{id}/posts")
    suspend fun sendPost(
        @Path("id") id: Long,
        @Body request: CreateGroupPostRequest
    ): Response<ApiResponse<GroupPost>>

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
}
