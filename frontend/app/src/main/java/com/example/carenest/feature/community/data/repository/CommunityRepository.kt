package com.example.carenest.feature.community.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.carenest.core.data.network.MediaApi
import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireData
import com.example.carenest.core.data.network.requireList
import com.example.carenest.core.data.network.requireSuccess
import com.example.carenest.feature.chat.domain.model.ChatGroup
import com.example.carenest.feature.chat.domain.model.ChatGroupPreview
import com.example.carenest.feature.community.data.remote.CommunityApi
import com.example.carenest.feature.community.data.remote.ReportPostRequest
import com.example.carenest.feature.community.domain.model.Article
import com.example.carenest.feature.community.domain.model.ArticleComment
import com.example.carenest.feature.community.domain.model.ArticleLikeResponse
import com.example.carenest.feature.community.domain.model.CreateArticleCommentRequest
import com.example.carenest.feature.community.domain.model.CreateArticleRequest
import com.example.carenest.feature.community.domain.model.CreateGroupPostCommentRequest
import com.example.carenest.feature.community.domain.model.CreateGroupPostRequest
import com.example.carenest.feature.community.domain.model.GroupGovernanceAuditEntry
import com.example.carenest.feature.community.domain.model.GroupPost
import com.example.carenest.feature.community.domain.model.GroupPostComment
import com.example.carenest.feature.community.domain.model.GroupPostInteractionResponse
import com.example.carenest.feature.community.domain.model.UpdateGroupRoleRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CommunityRepository(
    private val api: CommunityApi,
    private val mediaApi: MediaApi
) {
    suspend fun myGroups(search: String?): List<ChatGroup> {
        return api.myGroups(search).requireList("Không thể tải danh sách nhóm của bạn")
    }

    suspend fun discoverGroups(search: String?): List<ChatGroup> {
        return api.discoverGroups(search).requireList("Không thể tải danh sách nhóm gợi ý")
    }

    suspend fun join(groupId: Long): ChatGroupPreview {
        return api.join(groupId).requireData(
            fallback = "Không thể tham gia nhóm",
            missingDataMessage = "Không nhận được phản hồi khi tham gia nhóm"
        )
    }

    suspend fun preview(groupId: Long): ChatGroupPreview {
        return api.preview(groupId).requireData(
            fallback = "Không thể tải chi tiết nhóm",
            missingDataMessage = "Không nhận được thông tin nhóm"
        )
    }

    suspend fun posts(groupId: Long): List<GroupPost> {
        return api.posts(groupId).requireData("Không thể tải bài viết của nhóm").content
    }

    suspend fun myPosts(groupId: Long): List<GroupPost> {
        return api.myPosts(groupId).requireData("Không thể tải danh sách bài viết").content
    }

    suspend fun pendingPosts(groupId: Long): List<GroupPost> {
        return api.pendingPosts(groupId).requireData("Không thể tải danh sách chờ duyệt").content
    }

    suspend fun approvePost(postId: Long) {
        api.approvePost(postId).requireSuccess("Không thể duyệt bài viết")
    }

    suspend fun rejectPost(postId: Long, reason: String) {
        api.rejectPost(postId, reason).requireSuccess("Không thể từ chối bài viết")
    }

    suspend fun likeGroupPost(postId: Long): Result<GroupPostInteractionResponse> {
        return try {
            val response = api.likeGroupPost(postId)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.errorMessage("Không thể thả tim bài viết")))
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
                Result.failure(Exception(response.errorMessage("Không thể tải bình luận")))
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
                Result.failure(Exception(response.errorMessage("Không thể gửi bình luận")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPost(
        groupId: Long,
        title: String?,
        content: String,
        tags: String? = null,
        replyToPostId: Long? = null
    ): GroupPost {
        return api.sendPost(
            groupId,
            CreateGroupPostRequest(
                title = title,
                content = content,
                tags = tags,
                replyToPostId = replyToPostId
            )
        ).requireData(
            fallback = "Không thể đăng bài viết",
            missingDataMessage = "Không nhận được dữ liệu bài viết từ máy chủ"
        )
    }

    suspend fun getArticles(): List<Article> {
        return api.getArticles().requireList("Không thể tải bài viết")
    }

    suspend fun createArticle(
        title: String,
        content: String,
        tags: String?,
        imageUrl: String?
    ): Article {
        return api.createArticle(
            CreateArticleRequest(
                title = title,
                content = content,
                tags = tags?.takeIf { it.isNotBlank() },
                imageUrl = imageUrl?.takeIf { it.isNotBlank() }
            )
        ).requireData(
            fallback = "Không thể tạo bài viết",
            missingDataMessage = "Không nhận được bài viết mới"
        )
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
        return response.requireData(
            fallback = "Không thể tải ảnh lên",
            missingDataMessage = "Không nhận được thông tin ảnh đã tải lên"
        ).url
    }

    suspend fun toggleArticleLike(articleId: Long): ArticleLikeResponse {
        return api.toggleArticleLike(articleId).requireData(
            fallback = "Không thể cập nhật lượt thích",
            missingDataMessage = "Không nhận được trạng thái lượt thích"
        )
    }

    suspend fun getArticleComments(articleId: Long): List<ArticleComment> {
        return api.getArticleComments(articleId).requireList("Không thể tải bình luận")
    }

    suspend fun createArticleComment(articleId: Long, content: String): ArticleComment {
        return api.createArticleComment(
            articleId,
            CreateArticleCommentRequest(content)
        ).requireData(
            fallback = "Không thể gửi bình luận",
            missingDataMessage = "Không nhận được bình luận vừa gửi"
        )
    }

    suspend fun leave(groupId: Long) {
        api.leave(groupId).requireSuccess("Không thể rời nhóm")
    }

    suspend fun kickMember(groupId: Long, userId: Long, reason: String) {
        api.kickMember(groupId, userId, reason).requireSuccess("Không thể mời thành viên rời nhóm")
    }

    suspend fun reportPost(postId: Long, reason: String) {
        api.reportPost(postId, ReportPostRequest(reason)).requireSuccess("Không thể báo cáo bài viết")
    }

    suspend fun createGroupRequest(
        request: com.example.carenest.feature.community.domain.model.CreateGroupCreationRequest
    ): com.example.carenest.feature.community.domain.model.GroupCreationRequest {
        return api.createGroupRequest(request).requireData(
            fallback = "Không thể tạo yêu cầu",
            missingDataMessage = "Lỗi dữ liệu yêu cầu tạo nhóm"
        )
    }

    suspend fun getMyGroupRequests(): List<com.example.carenest.feature.community.domain.model.GroupCreationRequest> {
        return api.getMyGroupRequests().requireList("Không thể lấy trạng thái yêu cầu")
    }

    suspend fun getAdminGroupRequests(): List<com.example.carenest.feature.community.domain.model.GroupCreationRequest> {
        return api.getAdminGroupRequests().requireList("Không thể lấy danh sách yêu cầu")
    }

    suspend fun approveGroupRequest(id: Long) {
        api.approveGroupRequest(id).requireSuccess("Không thể duyệt yêu cầu")
    }

    suspend fun rejectGroupRequest(id: Long, reason: String) {
        api.rejectGroupRequest(id, reason).requireSuccess("Không thể từ chối yêu cầu")
    }

    suspend fun getMembers(id: Long): List<com.example.carenest.feature.community.domain.model.GroupMember> {
        return api.getMembers(id).requireList("Không thể lấy danh sách thành viên")
    }

    suspend fun getGovernanceAuditLogs(id: Long): List<GroupGovernanceAuditEntry> {
        return api.getGovernanceAuditLogs(id).requireList("Không thể lấy nhật ký quản trị")
    }

    suspend fun updatePost(postId: Long, title: String, content: String, tags: String?) {
        api.updatePost(
            postId,
            CreateGroupPostRequest(title = title, content = content, tags = tags)
        ).requireSuccess("Không thể cập nhật bài viết")
    }

    suspend fun deletePost(postId: Long) {
        api.deletePost(postId).requireSuccess("Không thể xóa bài viết")
    }

    suspend fun freezeGroup(id: Long, reason: String) {
        api.freezeGroup(id, reason).requireSuccess("Không thể tạm khóa nhóm")
    }

    suspend fun unfreezeGroup(id: Long, reason: String) {
        api.unfreezeGroup(id, reason).requireSuccess("Không thể mở lại nhóm")
    }

    suspend fun updateMemberRole(groupId: Long, userId: Long, role: String, reason: String) {
        api.updateMemberRole(
            groupId,
            userId,
            UpdateGroupRoleRequest(role = role, reason = reason)
        ).requireSuccess("Không thể cập nhật quyền")
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
