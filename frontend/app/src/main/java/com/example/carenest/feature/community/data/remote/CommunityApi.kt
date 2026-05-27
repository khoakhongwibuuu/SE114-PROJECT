package com.example.carenest.feature.community.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.community.domain.model.CommunityGroup
import com.example.carenest.feature.community.domain.model.CommunityGroupPreview
import com.example.carenest.feature.community.domain.model.CreateGroupPostRequest
import com.example.carenest.feature.community.domain.model.CreateArticleCommentRequest
import com.example.carenest.feature.community.domain.model.GroupPost
import com.example.carenest.feature.community.domain.model.PageResponse
import com.example.carenest.feature.community.domain.model.Article
import com.example.carenest.feature.community.domain.model.ArticleComment
import com.example.carenest.feature.community.domain.model.ArticleLikeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityApi {
    @GET("/api/v1/communities/my")
    suspend fun myGroups(@Query("search") search: String? = null): Response<ApiResponse<List<CommunityGroup>>>

    @GET("/api/v1/communities/discover")
    suspend fun discoverGroups(@Query("search") search: String? = null): Response<ApiResponse<List<CommunityGroup>>>

    @GET("/api/v1/communities/{id}/preview")
    suspend fun preview(@Path("id") id: Long): Response<ApiResponse<CommunityGroupPreview>>

    @POST("/api/v1/communities/{id}/join")
    suspend fun join(@Path("id") id: Long): Response<ApiResponse<CommunityGroupPreview>>

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

    @GET("/articles")
    suspend fun getArticles(): Response<ApiResponse<List<Article>>>

    @POST("/articles/{id}/likes")
    suspend fun toggleArticleLike(
        @Path("id") id: Long
    ): Response<ApiResponse<ArticleLikeResponse>>

    @GET("/articles/{id}/comments")
    suspend fun getArticleComments(
        @Path("id") id: Long
    ): Response<ApiResponse<List<ArticleComment>>>

    @POST("/articles/{id}/comments")
    suspend fun createArticleComment(
        @Path("id") id: Long,
        @Body request: CreateArticleCommentRequest
    ): Response<ApiResponse<ArticleComment>>
}
