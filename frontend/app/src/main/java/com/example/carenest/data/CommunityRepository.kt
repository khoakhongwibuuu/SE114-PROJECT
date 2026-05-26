package com.example.carenest.data

import com.example.carenest.model.CommunityGroup
import com.example.carenest.model.CommunityGroupPreview
import com.example.carenest.model.CreateGroupPostRequest
import com.example.carenest.model.GroupPost
import com.example.carenest.network.CommunityApi

class CommunityRepository(private val api: CommunityApi) {
    suspend fun myGroups(search: String?): List<CommunityGroup> {
        val response = api.myGroups(search)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải danh sách nhóm của bạn")
        }
        return response.body()?.data.orEmpty()
    }

    suspend fun discoverGroups(search: String?): List<CommunityGroup> {
        val response = api.discoverGroups(search)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải danh sách nhóm gợi ý")
        }
        return response.body()?.data.orEmpty()
    }

    suspend fun join(groupId: Long): CommunityGroupPreview {
        val response = api.join(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tham gia nhóm")
        }
        return response.body()?.data ?: throw IllegalStateException("Không thể tham gia nhóm")
    }

    suspend fun posts(groupId: Long): List<GroupPost> {
        val response = api.posts(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải tin nhắn")
        }
        return response.body()?.data?.content.orEmpty()
    }

    suspend fun sendPost(groupId: Long, content: String, replyToPostId: Long? = null): GroupPost {
        val response = api.sendPost(groupId, CreateGroupPostRequest(content, replyToPostId))
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể gửi tin nhắn")
        }
        return response.body()?.data ?: throw IllegalStateException("Không thể gửi tin nhắn")
    }
}
