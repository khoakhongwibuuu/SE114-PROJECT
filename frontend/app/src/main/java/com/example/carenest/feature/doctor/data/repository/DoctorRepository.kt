package com.example.carenest.feature.doctor.data.repository

import com.example.carenest.feature.doctor.data.remote.DoctorApi
import com.example.carenest.feature.doctor.domain.model.DoctorPublicProfile

class DoctorRepository(private val api: DoctorApi) {
    suspend fun getDoctorProfile(doctorId: Long): DoctorPublicProfile {
        val response = api.getDoctorProfile(doctorId)
        if (response.isSuccessful) {
            return response.body()?.data ?: throw IllegalStateException("Không thể tải hồ sơ bác sĩ")
        }
        throw IllegalStateException("Không thể tải hồ sơ bác sĩ")
    }
}
