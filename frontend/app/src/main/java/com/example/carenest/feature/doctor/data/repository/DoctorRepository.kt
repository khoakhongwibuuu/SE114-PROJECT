package com.example.carenest.feature.doctor.data.repository

import com.example.carenest.core.data.network.requireData
import com.example.carenest.feature.doctor.data.remote.DoctorApi
import com.example.carenest.feature.doctor.domain.model.DoctorPublicProfile

class DoctorRepository(private val api: DoctorApi) {
    suspend fun getDoctorProfile(doctorId: Long): DoctorPublicProfile {
        val response = api.getDoctorProfile(doctorId)
        return response.requireData("Không thể tải hồ sơ bác sĩ")
    }
}
