package com.example.carenest.feature.social.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.social.domain.model.Comment
import com.example.carenest.feature.social.domain.model.Group
import com.example.carenest.feature.social.domain.model.PaginatedResponse
import com.example.carenest.feature.social.domain.model.Post
import com.example.carenest.feature.social.domain.model.Reaction
import com.example.carenest.feature.social.domain.model.ReactionType
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class ReactToPostRequest(
    val reactionType: ReactionType,
)

data class CreateCommentRequest(
    val content: String,
    val parentCommentId: Long? = null,
)

interface SocialApi {
    @GET("/api/v1/groups")
    suspend fun getGroups(
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
    ): Response<ApiResponse<PaginatedResponse<Group>>>

    @GET("/api/v1/groups/{groupId}/posts")
    suspend fun getGroupPosts(
        @Path("groupId") groupId: Long,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null,
    ): Response<ApiResponse<PaginatedResponse<Post>>>

    @GET("/api/v1/posts/{postId}/comments")
    suspend fun getPostComments(
        @Path("postId") postId: Long,
        @Query("parentCommentId") parentCommentId: Long? = null,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null,
    ): Response<ApiResponse<PaginatedResponse<Comment>>>

    @POST("/api/v1/posts/{postId}/react")
    suspend fun reactToPost(
        @Path("postId") postId: Long,
        @Body request: ReactToPostRequest,
    ): Response<ApiResponse<Reaction>>

    @POST("/api/v1/posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Long,
        @Body request: CreateCommentRequest,
    ): Response<ApiResponse<Comment>>
}
