package com.example.carenest

import android.app.Application
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.chat.data.remote.ChatWebSocketClient
import com.example.carenest.feature.chat.data.repository.ChatRepository
import com.example.carenest.feature.community.data.repository.CommunityRepository
import com.example.carenest.feature.auth.data.remote.AuthApi
import com.example.carenest.core.data.network.MediaApi
import com.example.carenest.feature.community.data.remote.CommunityApi
import com.example.carenest.feature.dashboard.data.remote.DashboardApi
import com.example.carenest.core.data.network.RetrofitClient
import com.example.carenest.feature.ekyc.data.remote.EkycApi
import com.example.carenest.feature.ekyc.data.repository.EkycRepository

class CareNestApplication : Application() {
    lateinit var secureSessionManager: SecureSessionManager
    lateinit var authApi: AuthApi
    lateinit var dashboardApi: DashboardApi
    lateinit var communityRepository: CommunityRepository
    lateinit var familyApi: com.example.carenest.core.data.network.FamilyApi
    lateinit var appointmentRepository: com.example.carenest.feature.medical.data.repository.AppointmentRepository
    lateinit var vaccineRepository: com.example.carenest.feature.medical.data.repository.VaccineRepository
    lateinit var chatRepository: ChatRepository
    lateinit var ekycRepository: EkycRepository

    override fun onCreate() {
        super.onCreate()
        secureSessionManager = SecureSessionManager(this)
        val retrofit = RetrofitClient.create(secureSessionManager)
        val communityApi = retrofit.create(CommunityApi::class.java)
        val ekycApi = retrofit.create(EkycApi::class.java)
        val mediaApi = retrofit.create(MediaApi::class.java)
        authApi = retrofit.create(AuthApi::class.java)
        dashboardApi = retrofit.create(DashboardApi::class.java)
        communityRepository = CommunityRepository(communityApi)
        familyApi = retrofit.create(com.example.carenest.core.data.network.FamilyApi::class.java)
        appointmentRepository = com.example.carenest.feature.medical.data.repository.AppointmentRepository(retrofit.create(com.example.carenest.feature.medical.data.remote.AppointmentApi::class.java))
        vaccineRepository = com.example.carenest.feature.medical.data.repository.VaccineRepository(retrofit.create(com.example.carenest.feature.medical.data.remote.VaccineApi::class.java))
        chatRepository = ChatRepository(communityApi, ChatWebSocketClient(secureSessionManager))
        ekycRepository = EkycRepository(ekycApi, mediaApi)
    }
}
