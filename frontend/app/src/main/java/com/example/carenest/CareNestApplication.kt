package com.example.carenest

import android.app.Application
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.chat.data.remote.ChatWebSocketClient
import com.example.carenest.feature.chat.data.repository.ChatRepository
import com.example.carenest.feature.admin.data.AdminApi
import com.example.carenest.feature.admin.data.repository.AdminRepository
import com.example.carenest.feature.booking.data.remote.BookingApi
import com.example.carenest.feature.booking.data.repository.BookingRepository
import com.example.carenest.feature.community.data.repository.CommunityRepository
import com.example.carenest.feature.auth.data.remote.AuthApi
import com.example.carenest.core.data.network.MediaApi
import com.example.carenest.feature.community.data.remote.CommunityApi
import com.example.carenest.feature.dashboard.data.remote.DashboardApi
import com.example.carenest.core.data.network.RetrofitClient
import com.example.carenest.feature.ekyc.data.remote.EkycApi
import com.example.carenest.feature.ekyc.data.repository.EkycRepository
import com.example.carenest.feature.medical.data.remote.MedicineApi
import com.example.carenest.feature.appointment.data.remote.AppointmentApi
import com.example.carenest.feature.notifications.data.remote.NotificationApi
import com.example.carenest.feature.doctor.data.remote.DoctorApi
import com.example.carenest.feature.doctor.data.repository.DoctorRepository

class CareNestApplication : Application() {
    lateinit var secureSessionManager: SecureSessionManager
    lateinit var authApi: AuthApi
    lateinit var dashboardApi: DashboardApi
    lateinit var vaccinationApi: com.example.carenest.feature.health.data.remote.VaccinationApi
    lateinit var growthApi: com.example.carenest.feature.health.data.remote.GrowthApi
    lateinit var communityRepository: CommunityRepository
    lateinit var familyApi: com.example.carenest.feature.family.data.remote.FamilyApi
    lateinit var familyRepository: com.example.carenest.feature.family.data.repository.FamilyRepository
    lateinit var chatRepository: ChatRepository
    lateinit var familyChatRepository: com.example.carenest.feature.chat.data.repository.FamilyChatRepository
    lateinit var ekycRepository: EkycRepository
    lateinit var medicineApi: MedicineApi
    lateinit var appointmentApi: AppointmentApi
    lateinit var notificationApi: NotificationApi
    lateinit var adminRepository: AdminRepository
    lateinit var doctorApi: DoctorApi
    lateinit var doctorRepository: DoctorRepository
    lateinit var bookingApi: BookingApi
    lateinit var bookingRepository: BookingRepository

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
        growthApi = retrofit.create(com.example.carenest.feature.health.data.remote.GrowthApi::class.java)
        communityRepository = CommunityRepository(communityApi, mediaApi)
        familyApi = retrofit.create(com.example.carenest.feature.family.data.remote.FamilyApi::class.java)
        familyRepository = com.example.carenest.feature.family.data.repository.FamilyRepository(familyApi, secureSessionManager)
        chatRepository = ChatRepository(communityApi, ChatWebSocketClient(secureSessionManager), secureSessionManager)
        familyChatRepository = com.example.carenest.feature.chat.data.repository.FamilyChatRepository(
            familyApi,
            com.example.carenest.feature.chat.data.remote.FamilyChatWebSocketClient(secureSessionManager),
            secureSessionManager
        )
        ekycRepository = EkycRepository(ekycApi, mediaApi)
        medicineApi = retrofit.create(MedicineApi::class.java)
        appointmentApi = retrofit.create(AppointmentApi::class.java)
        notificationApi = retrofit.create(NotificationApi::class.java)
        val adminApi = retrofit.create(AdminApi::class.java)
        val bookingApi = retrofit.create(BookingApi::class.java)
        adminRepository = AdminRepository(adminApi)
        doctorApi = retrofit.create(DoctorApi::class.java)
        doctorRepository = DoctorRepository(doctorApi)
        bookingRepository = BookingRepository(bookingApi)

    }
}
