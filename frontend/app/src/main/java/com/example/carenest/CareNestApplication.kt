package com.example.carenest

import android.app.Application
import com.example.carenest.data.DataStoreManager
import com.example.carenest.data.CommunityRepository
import com.example.carenest.network.AuthApi
import com.example.carenest.network.CommunityApi
import com.example.carenest.network.DashboardApi
import com.example.carenest.network.RetrofitClient

class CareNestApplication : Application() {
    lateinit var dataStoreManager: DataStoreManager
    lateinit var authApi: AuthApi
    lateinit var dashboardApi: DashboardApi
    lateinit var communityRepository: CommunityRepository

    override fun onCreate() {
        super.onCreate()
        dataStoreManager = DataStoreManager(this)
        val retrofit = RetrofitClient.create(dataStoreManager)
        authApi = retrofit.create(AuthApi::class.java)
        dashboardApi = retrofit.create(DashboardApi::class.java)
        communityRepository = CommunityRepository(retrofit.create(CommunityApi::class.java))
    }
}
