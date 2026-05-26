package com.example.carenest

import android.app.Application
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.chat.data.remote.ChatWebSocketClient
import com.example.carenest.feature.chat.data.repository.ChatRepository
import com.example.carenest.feature.community.data.repository.CommunityRepository
import com.example.carenest.feature.auth.data.remote.AuthApi
import com.example.carenest.feature.community.data.remote.CommunityApi
import com.example.carenest.feature.dashboard.data.remote.DashboardApi
import com.example.carenest.core.data.network.RetrofitClient

class CareNestApplication : Application() {
    lateinit var secureSessionManager: SecureSessionManager
    lateinit var authApi: AuthApi
    lateinit var dashboardApi: DashboardApi
    lateinit var communityRepository: CommunityRepository
    lateinit var chatRepository: ChatRepository

    override fun onCreate() {
        super.onCreate()
        secureSessionManager = SecureSessionManager(this)
        val retrofit = RetrofitClient.create(secureSessionManager)
        val communityApi = retrofit.create(CommunityApi::class.java)
        authApi = retrofit.create(AuthApi::class.java)
        dashboardApi = retrofit.create(DashboardApi::class.java)
        communityRepository = CommunityRepository(communityApi)
        chatRepository = ChatRepository(communityApi, ChatWebSocketClient(secureSessionManager))
    }
}
