package com.example.carenest.feature.ekyc.presentation

import android.content.ContextWrapper
import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.core.data.network.MediaApi
import com.example.carenest.core.data.network.MediaUploadResponse
import com.example.carenest.feature.ekyc.data.remote.EkycApi
import com.example.carenest.feature.ekyc.data.repository.EkycRepository
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary
import com.example.carenest.feature.ekyc.domain.model.DoctorVerificationResponse
import com.example.carenest.feature.ekyc.domain.model.RejectVerificationRequest
import com.example.carenest.feature.ekyc.domain.model.SubmitDoctorVerificationRequest
import com.example.carenest.feature.ekyc.domain.model.VerificationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.util.ArrayDeque

@OptIn(ExperimentalCoroutinesApi::class)
class EkycViewModelTest {

    @Test
    fun loadStatus_populatesRejectedVerificationAndKeepsFormEditable() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val rejected = verification(
                status = VerificationStatus.REJECTED,
                rejectionReason = "Thiếu ảnh rõ nét",
            )
            val api = FakeDoctorEkycApi().apply {
                myVerificationResponses += successVerification(rejected)
            }
            val repository = EkycRepository(api, FakeDoctorMediaApi())
            val viewModel = EkycViewModel(repository, dispatcher)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(VerificationStatus.REJECTED, state.status)
            assertEquals(rejected.certificationNumber, state.certificationNumber)
            assertEquals(rejected.specialty, state.specialty)
            assertEquals(rejected.hospitalName, state.hospitalName)
            assertEquals(rejected.documentUrl, state.uploadedDocumentUrl)
            assertFalse(state.isLocked)
            assertTrue(state.canSubmit)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun submit_reusesExistingDocumentUrlAndSendsTrimmedFields() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val rejected = verification(status = VerificationStatus.REJECTED)
            val approvedAfterSubmit = rejected.copy(status = VerificationStatus.PENDING)
            val api = FakeDoctorEkycApi().apply {
                myVerificationResponses += successVerification(rejected)
                submitResponses += successVerification(approvedAfterSubmit)
            }
            val repository = EkycRepository(api, FakeDoctorMediaApi())
            val viewModel = EkycViewModel(repository, dispatcher)
            advanceUntilIdle()

            viewModel.onCertificationNumberChange("  CERT-UPDATED  ")
            viewModel.onSpecialtyChange("  Nội tổng quát ")
            viewModel.onHospitalNameChange("  CareNest Clinic  ")
            viewModel.submit(ContextWrapper(null))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(VerificationStatus.PENDING, state.status)
            assertFalse(state.isSubmitting)
            assertTrue(state.isLocked)
            assertEquals(null, state.selectedCertificateUri)
            assertNotNull(state.message)
            val request = api.submitRequests.single()
            assertEquals("CERT-UPDATED", request.certificationNumber)
            assertEquals("Nội tổng quát", request.specialty)
            assertEquals("CareNest Clinic", request.hospitalName)
            assertEquals(rejected.documentUrl, request.documentUrl)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun verification(
        status: VerificationStatus,
        rejectionReason: String? = null,
    ): DoctorVerificationResponse {
        return DoctorVerificationResponse(
            id = 99L,
            userId = 123L,
            userEmail = "doctor@example.com",
            userFullName = "Dr. Kiet",
            certificationNumber = "CERT-99",
            specialty = "Nhi khoa",
            hospitalName = "CareNest Hospital",
            documentUrl = "https://example.com/certificate-99.jpg",
            status = status,
            rejectionReason = rejectionReason,
            createdAt = "2026-06-18T08:00:00Z",
            updatedAt = "2026-06-18T08:05:00Z",
        )
    }

    private fun successVerification(item: DoctorVerificationResponse): Response<ApiResponse<DoctorVerificationResponse>> {
        return Response.success(ApiResponse(success = true, data = item, message = "ok"))
    }
}

private class FakeDoctorEkycApi : EkycApi {
    val myVerificationResponses = ArrayDeque<Response<ApiResponse<DoctorVerificationResponse>>>()
    val submitResponses = ArrayDeque<Response<ApiResponse<DoctorVerificationResponse>>>()
    val submitRequests = mutableListOf<SubmitDoctorVerificationRequest>()

    override suspend fun getMyVerificationStatus(): Response<ApiResponse<DoctorVerificationResponse>> {
        return myVerificationResponses.removeFirst()
    }

    override suspend fun submitVerification(
        request: SubmitDoctorVerificationRequest
    ): Response<ApiResponse<DoctorVerificationResponse>> {
        submitRequests += request
        return submitResponses.removeFirst()
    }

    override suspend fun getPendingVerifications(): Response<ApiResponse<List<DoctorVerificationResponse>>> {
        error("getPendingVerifications not expected in this test")
    }

    override suspend fun approveVerification(id: Long): Response<ApiResponse<DoctorVerificationResponse>> {
        error("approveVerification not expected in this test")
    }

    override suspend fun rejectVerification(
        id: Long,
        request: RejectVerificationRequest
    ): Response<ApiResponse<DoctorVerificationResponse>> {
        error("rejectVerification not expected in this test")
    }

    override suspend fun getAllDoctors(): Response<ApiResponse<List<DoctorSummary>>> {
        error("getAllDoctors not expected in this test")
    }

    override suspend fun revokeDoctor(userId: Long): Response<ApiResponse<Unit>> {
        error("revokeDoctor not expected in this test")
    }
}

private class FakeDoctorMediaApi : MediaApi {
    override suspend fun upload(
        file: MultipartBody.Part,
        category: String?
    ): Response<ApiResponse<MediaUploadResponse>> {
        return Response.success(
            ApiResponse(
                success = true,
                data = MediaUploadResponse(
                    fileName = "certificate.jpg",
                    contentType = "image/jpeg",
                    size = 100L,
                    url = "https://example.com/uploaded-certificate.jpg",
                ),
                message = "ok",
            )
        )
    }
}
