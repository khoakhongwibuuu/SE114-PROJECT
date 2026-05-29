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
import com.example.carenest.feature.chat.data.remote.AiChatApi
import com.example.carenest.feature.medical.data.remote.MedicineApi
import com.example.carenest.feature.appointment.data.remote.AppointmentApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CareNestApplication : Application() {
    lateinit var secureSessionManager: SecureSessionManager
    lateinit var authApi: AuthApi
    lateinit var dashboardApi: DashboardApi
    lateinit var vaccinationApi: com.example.carenest.feature.health.data.remote.VaccinationApi
    lateinit var communityRepository: CommunityRepository
    lateinit var familyApi: com.example.carenest.feature.family.data.remote.FamilyApi
    lateinit var familyRepository: com.example.carenest.feature.family.data.repository.FamilyRepository
    lateinit var chatRepository: ChatRepository
    lateinit var ekycRepository: EkycRepository
    lateinit var aiChatApi: AiChatApi
    lateinit var medicineApi: MedicineApi
    lateinit var appointmentApi: AppointmentApi

    override fun onCreate() {
        super.onCreate()
        secureSessionManager = SecureSessionManager(this)
        val retrofit = RetrofitClient.create(secureSessionManager)
        val communityApi = retrofit.create(CommunityApi::class.java)
        val ekycApi = retrofit.create(EkycApi::class.java)
        val mediaApi = retrofit.create(MediaApi::class.java)
        authApi = retrofit.create(AuthApi::class.java)
        dashboardApi = retrofit.create(DashboardApi::class.java)
        vaccinationApi = retrofit.create(com.example.carenest.feature.health.data.remote.VaccinationApi::class.java)
        communityRepository = CommunityRepository(communityApi)
        familyApi = retrofit.create(com.example.carenest.feature.family.data.remote.FamilyApi::class.java)
        familyRepository = com.example.carenest.feature.family.data.repository.FamilyRepository(familyApi, secureSessionManager)
        chatRepository = ChatRepository(communityApi, ChatWebSocketClient(secureSessionManager))
        ekycRepository = EkycRepository(ekycApi, mediaApi)
        medicineApi = retrofit.create(MedicineApi::class.java)
        appointmentApi = retrofit.create(AppointmentApi::class.java)

        val aiOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
        val aiRetrofit = Retrofit.Builder()
            .baseUrl(com.example.carenest.AppConfig.AI_BACKEND_URL)
            .client(aiOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        aiChatApi = aiRetrofit.create(AiChatApi::class.java)
    }
}
