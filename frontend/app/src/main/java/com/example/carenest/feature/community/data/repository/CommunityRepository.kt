package com.example.carenest.feature.community.data.repository

import com.example.carenest.feature.community.domain.model.CommunityGroup
import com.example.carenest.feature.community.domain.model.CommunityGroupPreview
import com.example.carenest.feature.community.domain.model.CreateGroupPostRequest
import com.example.carenest.feature.community.domain.model.GroupPost
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
}
