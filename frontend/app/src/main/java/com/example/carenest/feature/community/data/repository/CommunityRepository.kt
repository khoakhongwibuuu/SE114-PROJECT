package com.example.carenest.feature.community.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.carenest.core.data.network.MediaApi
import com.example.carenest.feature.community.data.remote.CommunityApi
import com.example.carenest.feature.community.domain.model.Article
import com.example.carenest.feature.community.domain.model.ArticleComment
import com.example.carenest.feature.community.domain.model.ArticleLikeResponse
import com.example.carenest.feature.community.domain.model.GroupPostComment
import com.example.carenest.feature.community.domain.model.CreateGroupPostCommentRequest
import com.example.carenest.feature.community.domain.model.GroupPostInteractionResponse
import com.example.carenest.feature.chat.domain.model.ChatGroup
import com.example.carenest.feature.chat.domain.model.ChatGroupPreview
import com.example.carenest.feature.community.domain.model.CreateArticleCommentRequest
import com.example.carenest.feature.community.domain.model.CreateArticleRequest
import com.example.carenest.feature.community.domain.model.CreateGroupPostRequest
import com.example.carenest.feature.community.domain.model.GroupPost
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CommunityRepository(
    private val api: CommunityApi,
    private val mediaApi: MediaApi
) {
    suspend fun myGroups(search: String?): List<ChatGroup> {
        val response = api.myGroups(search)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải danh sách nhóm của bạn")
        }
        return response.body()?.data.orEmpty()
    }

    suspend fun discoverGroups(search: String?): List<ChatGroup> {
        val response = api.discoverGroups(search)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải danh sách nhóm gợi ý")
        }
        return response.body()?.data.orEmpty()
    }

    suspend fun join(groupId: Long): ChatGroupPreview {
        val response = api.join(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tham gia nhóm")
        }
        return response.body()?.data ?: throw IllegalStateException("Không thể tham gia nhóm")
    }

    suspend fun preview(groupId: Long): ChatGroupPreview {
        val response = api.preview(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải chi tiết nhóm")
        }
        return response.body()?.data ?: throw IllegalStateException("Không thể tải chi tiết nhóm")
    }

    suspend fun posts(groupId: Long): List<GroupPost> {
        val response = api.posts(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải bài viết của nhóm")
        }
        return response.body()?.data?.content.orEmpty()
    }

    suspend fun myPosts(groupId: Long): List<GroupPost> {
        val response = api.myPosts(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải danh sách bài viết")
        }
        return response.body()?.data?.content.orEmpty()
    }

    suspend fun pendingPosts(groupId: Long): List<GroupPost> {
        val response = api.pendingPosts(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải danh sách chờ duyệt")
        }
        return response.body()?.data?.content.orEmpty()
    }

    suspend fun approvePost(postId: Long) {
        val response = api.approvePost(postId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể duyệt bài viết")
        }
    }

    suspend fun rejectPost(postId: Long, reason: String) {
        val response = api.rejectPost(postId, reason)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể từ chối bài viết")
        }
    }

    suspend fun deleteGroupPost(postId: Long) {
        val response = api.deleteGroupPost(postId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "KhÃ´ng thá»ƒ gá»¡ bÃ i viáº¿t")
        }
    }

    suspend fun likeGroupPost(postId: Long): Result<GroupPostInteractionResponse> {
        return try {
            val response = api.likeGroupPost(postId)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Không thể thả tim bài viết"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroupPostComments(postId: Long): Result<List<GroupPostComment>> {
        return try {
            val response = api.getGroupPostComments(postId)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.content)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Không thể tải bình luận"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGroupPostComment(postId: Long, content: String): Result<GroupPostComment> {
        return try {
            val response = api.createGroupPostComment(
                postId,
                CreateGroupPostCommentRequest(content)
            )
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Không thể gửi bình luận"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPost(groupId: Long, title: String?, content: String, tags: String? = null, replyToPostId: Long? = null): GroupPost {
        val response = api.sendPost(groupId, CreateGroupPostRequest(title = title, content = content, tags = tags, replyToPostId = replyToPostId))
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể đăng bài viết")
        }
        return response.body()?.data ?: throw IllegalStateException("Không nhận được dữ liệu bài viết từ server")
    }

    suspend fun getArticles(): List<Article> {
        val response = api.getArticles()
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải bài viết")
        }
        return response.body()?.data.orEmpty()
    }

    suspend fun createArticle(
        title: String,
        content: String,
        tags: String?,
        imageUrl: String?
    ): Article {
        val response = api.createArticle(
            CreateArticleRequest(
                title = title,
                content = content,
                tags = tags?.takeIf { it.isNotBlank() },
                imageUrl = imageUrl?.takeIf { it.isNotBlank() }
            )
        )
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tạo bài viết")
        }
        return response.body()?.data ?: throw IllegalStateException("Không nhận được bài viết mới")
    }

    suspend fun uploadArticleImage(context: Context, uri: Uri): String {
        val resolver = context.applicationContext.contentResolver
        val mimeType = resolver.getType(uri) ?: "image/jpeg"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Không thể đọc ảnh minh họa")
        val fileName = resolver.resolveDisplayName(uri)
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
        val response = mediaApi.upload(part, "articles")

        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải ảnh lên")
        }
        return response.body()?.data?.url ?: throw IllegalStateException("Không nhận được URL ảnh")
    }

    suspend fun toggleArticleLike(articleId: Long): ArticleLikeResponse {
        val response = api.toggleArticleLike(articleId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể cập nhật lượt thích")
        }
        return response.body()?.data ?: throw IllegalStateException("Không thể cập nhật lượt thích")
    }

    suspend fun getArticleComments(articleId: Long): List<ArticleComment> {
        val response = api.getArticleComments(articleId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải bình luận")
        }
        return response.body()?.data.orEmpty()
    }

    suspend fun createArticleComment(articleId: Long, content: String): ArticleComment {
        val response = api.createArticleComment(articleId, CreateArticleCommentRequest(content))
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể gửi bình luận")
        }
        return response.body()?.data ?: throw IllegalStateException("Không thể gửi bình luận")
    }

    suspend fun leave(groupId: Long) {
        val response = api.leave(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể rời nhóm")
        }
    }

    suspend fun kickMember(groupId: Long, userId: Long) {
        val response = api.kickMember(groupId, userId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể mời thành viên rời nhóm")
        }
    }

    suspend fun reportPost(postId: Long, reason: String) {
        val response = api.reportPost(postId, com.example.carenest.feature.community.data.remote.ReportPostRequest(reason))
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể báo cáo bài viết")
        }
    }
}

private fun ContentResolver.resolveDisplayName(uri: Uri): String {
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            val name = cursor.getString(index)
            if (!name.isNullOrBlank()) return name
        }
    }
    return "community-article.jpg"
}
