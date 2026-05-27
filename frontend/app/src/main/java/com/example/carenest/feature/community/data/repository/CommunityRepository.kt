package com.example.carenest.feature.community.data.repository

import com.example.carenest.feature.community.domain.model.CommunityGroup
import com.example.carenest.feature.community.domain.model.CommunityGroupPreview
import com.example.carenest.feature.community.domain.model.CreateGroupPostRequest
import com.example.carenest.feature.community.domain.model.CreateArticleCommentRequest
import com.example.carenest.feature.community.domain.model.GroupPost
import com.example.carenest.feature.community.domain.model.Article
import com.example.carenest.feature.community.domain.model.ArticleComment
import com.example.carenest.feature.community.domain.model.ArticleLikeResponse
import com.example.carenest.feature.community.data.remote.CommunityApi

class CommunityRepository(private val api: CommunityApi) {
    suspend fun myGroups(search: String?): List<CommunityGroup> {
        val response = api.myGroups(search)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "KhÃ´ng thá»ƒ táº£i danh sÃ¡ch nhÃ³m cá»§a báº¡n")
        }
        return response.body()?.data.orEmpty()
    }

    suspend fun discoverGroups(search: String?): List<CommunityGroup> {
        val response = api.discoverGroups(search)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "KhÃ´ng thá»ƒ táº£i danh sÃ¡ch nhÃ³m gá»£i Ã½")
        }
        return response.body()?.data.orEmpty()
    }

    suspend fun join(groupId: Long): CommunityGroupPreview {
        val response = api.join(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "KhÃ´ng thá»ƒ tham gia nhÃ³m")
        }
        return response.body()?.data ?: throw IllegalStateException("KhÃ´ng thá»ƒ tham gia nhÃ³m")
    }

    suspend fun posts(groupId: Long): List<GroupPost> {
        val response = api.posts(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "KhÃ´ng thá»ƒ táº£i tin nháº¯n")
        }
        return response.body()?.data?.content.orEmpty()
    }

    suspend fun sendPost(groupId: Long, content: String, replyToPostId: Long? = null): GroupPost {
        val response = api.sendPost(groupId, CreateGroupPostRequest(content, replyToPostId))
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "KhÃ´ng thá»ƒ gá»­i tin nháº¯n")
        }
        return response.body()?.data ?: throw IllegalStateException("KhÃ´ng thá»ƒ gá»­i tin nháº¯n")
    }

    suspend fun getArticles(): List<Article> {
        val response = api.getArticles()
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải bài viết")
        }
        return response.body()?.data.orEmpty()
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
}
