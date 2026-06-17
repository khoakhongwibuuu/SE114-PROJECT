package com.example.carenest.feature.ekyc.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.carenest.core.data.network.MediaApi
import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireData
import com.example.carenest.core.data.network.requireList
import com.example.carenest.core.data.network.requireSuccess
import com.example.carenest.feature.ekyc.data.remote.EkycApi
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary
import com.example.carenest.feature.ekyc.domain.model.DoctorVerificationResponse
import com.example.carenest.feature.ekyc.domain.model.RejectVerificationRequest
import com.example.carenest.feature.ekyc.domain.model.SubmitDoctorVerificationRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class EkycRepository(
    private val ekycApi: EkycApi,
    private val mediaApi: MediaApi,
) {
    suspend fun getMyVerification(): DoctorVerificationResponse? {
        val response = ekycApi.getMyVerificationStatus()
        if (response.code() == 404) {
            return null
        }
        if (!response.isSuccessful) {
            throw IllegalStateException(response.errorMessage("Không thể tải trạng thái hồ sơ"))
        }
        return response.requireData("Không thể tải trạng thái hồ sơ")
    }

    suspend fun uploadCertificate(context: Context, uri: Uri): String {
        val resolver = context.applicationContext.contentResolver
        val mimeType = resolver.getType(uri) ?: "image/jpeg"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Không thể đọc ảnh chứng chỉ")
        val fileName = resolver.resolveDisplayName(uri)
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
        val response = mediaApi.upload(part, "doctor-verifications")

        if (!response.isSuccessful) {
            throw IllegalStateException(response.errorMessage("Không thể tải ảnh chứng chỉ lên"))
        }
        return response.requireData(
            "Không thể tải ảnh chứng chỉ lên",
            "Không nhận được URL ảnh chứng chỉ",
        ).url
    }

    suspend fun submitVerification(
        certificationNumber: String,
        specialty: String,
        hospitalName: String,
        documentUrl: String,
    ): DoctorVerificationResponse {
        val response = ekycApi.submitVerification(
            SubmitDoctorVerificationRequest(
                certificationNumber = certificationNumber,
                specialty = specialty,
                hospitalName = hospitalName,
                documentUrl = documentUrl,
            ),
        )
        return response.requireData("Không thể gửi hồ sơ xác thực bác sĩ", "Không nhận được trạng thái hồ sơ")
    }

    suspend fun getPendingVerifications(): List<DoctorVerificationResponse> {
        val response = ekycApi.getPendingVerifications()
        return response.requireList("Không thể tải danh sách chờ duyệt")
    }

    suspend fun approveVerification(id: Long): DoctorVerificationResponse {
        val response = ekycApi.approveVerification(id)
        return response.requireData("Không thể phê duyệt hồ sơ", "Không nhận được thông tin phản hồi")
    }

    suspend fun rejectVerification(id: Long, rejectionReason: String): DoctorVerificationResponse {
        val response = ekycApi.rejectVerification(id, RejectVerificationRequest(rejectionReason))
        return response.requireData("Không thể từ chối hồ sơ", "Không nhận được thông tin phản hồi")
    }

    suspend fun getAllDoctors(): List<DoctorSummary> {
        val response = ekycApi.getAllDoctors()
        return response.requireList("Không thể tải danh sách bác sĩ")
    }

    suspend fun revokeDoctor(userId: Long) {
        val response = ekycApi.revokeDoctor(userId)
        response.requireSuccess("Không thể thu hồi quyền bác sĩ")
    }
}

private fun android.content.ContentResolver.resolveDisplayName(uri: Uri): String {
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            val name = cursor.getString(index)
            if (!name.isNullOrBlank()) return name
        }
    }
    return "doctor-certificate.jpg"
}
