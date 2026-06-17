package com.example.carenest.feature.admin.presentation

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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.util.ArrayDeque

@OptIn(ExperimentalCoroutinesApi::class)
class AdminEkycViewModelTest {

    @Test
    fun approveVerification_movesPendingDoctorIntoApprovedList() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val pending = verification(id = 10L, userId = 101L, userEmail = "doctor@example.com", userFullName = "Dr. Lan")
            val api = FakeEkycApi().apply {
                pendingResponses += successPending(listOf(pending))
                doctorResponses += successDoctors(emptyList())
                approveResponses += successVerification(pending.copy(status = VerificationStatus.APPROVED))
            }
            val repository = EkycRepository(api, FakeMediaApi())
            val viewModel = AdminEkycViewModel(repository, dispatcher)
            advanceUntilIdle()

            viewModel.approveVerification(10L)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.pendingList.isEmpty())
            assertEquals(1, state.doctorList.size)
            assertEquals(101L, state.doctorList.first().id)
            assertNotNull(state.message)
            assertEquals(listOf(10L), api.approveCalls)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun rejectVerification_requiresReasonBeforeCallingApi() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val api = FakeEkycApi().apply {
                pendingResponses += successPending(emptyList())
                doctorResponses += successDoctors(emptyList())
            }
            val repository = EkycRepository(api, FakeMediaApi())
            val viewModel = AdminEkycViewModel(repository, dispatcher)
            advanceUntilIdle()

            viewModel.rejectVerification(15L, "   ")
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)
            assertTrue(api.rejectCalls.isEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun revokeDoctor_removesDoctorFromApprovedList() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val doctor = doctor(id = 200L, email = "approved@example.com")
            val api = FakeEkycApi().apply {
                pendingResponses += successPending(emptyList())
                doctorResponses += successDoctors(listOf(doctor))
                revokeResponses += successUnit()
            }
            val repository = EkycRepository(api, FakeMediaApi())
            val viewModel = AdminEkycViewModel(repository, dispatcher)
            advanceUntilIdle()

            viewModel.revokeDoctor(200L)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.doctorList.isEmpty())
            assertNotNull(state.message)
            assertEquals(listOf(200L), api.revokeCalls)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun verification(
        id: Long,
        userId: Long,
        userEmail: String,
        userFullName: String,
        status: VerificationStatus = VerificationStatus.PENDING,
    ): DoctorVerificationResponse {
        return DoctorVerificationResponse(
            id = id,
            userId = userId,
            userEmail = userEmail,
            userFullName = userFullName,
            certificationNumber = "CERT-$id",
            specialty = "Nhi khoa",
            hospitalName = "CareNest Hospital",
            documentUrl = "https://example.com/cert-$id.jpg",
            status = status,
            rejectionReason = null,
            createdAt = "2026-06-18T08:00:00Z",
            updatedAt = "2026-06-18T08:05:00Z",
        )
    }

    private fun doctor(id: Long, email: String): DoctorSummary {
        return DoctorSummary(
            id = id,
            email = email,
            fullName = "Doctor $id",
            avatarUrl = null,
            certificationNumber = "CERT-$id",
            specialty = "Nhi khoa",
            hospitalName = "CareNest Hospital",
            documentUrl = null,
            approvedAt = "2026-06-18T08:05:00Z",
        )
    }

    private fun successPending(items: List<DoctorVerificationResponse>): Response<ApiResponse<List<DoctorVerificationResponse>>> {
        return Response.success(ApiResponse(success = true, data = items, message = "ok"))
    }

    private fun successDoctors(items: List<DoctorSummary>): Response<ApiResponse<List<DoctorSummary>>> {
        return Response.success(ApiResponse(success = true, data = items, message = "ok"))
    }

    private fun successVerification(item: DoctorVerificationResponse): Response<ApiResponse<DoctorVerificationResponse>> {
        return Response.success(ApiResponse(success = true, data = item, message = "ok"))
    }

    private fun successUnit(): Response<ApiResponse<Unit>> {
        return Response.success(ApiResponse(success = true, data = Unit, message = "ok"))
    }
}

private class FakeEkycApi : EkycApi {
    val pendingResponses = ArrayDeque<Response<ApiResponse<List<DoctorVerificationResponse>>>>()
    val doctorResponses = ArrayDeque<Response<ApiResponse<List<DoctorSummary>>>>()
    val approveResponses = ArrayDeque<Response<ApiResponse<DoctorVerificationResponse>>>()
    val rejectResponses = ArrayDeque<Response<ApiResponse<DoctorVerificationResponse>>>()
    val revokeResponses = ArrayDeque<Response<ApiResponse<Unit>>>()
    val approveCalls = mutableListOf<Long>()
    val rejectCalls = mutableListOf<Pair<Long, String>>()
    val revokeCalls = mutableListOf<Long>()

    override suspend fun getMyVerificationStatus(): Response<ApiResponse<DoctorVerificationResponse>> {
        return Response.error(404, """{"message":"not found"}""".toResponseBody("application/json".toMediaType()))
    }

    override suspend fun submitVerification(request: SubmitDoctorVerificationRequest): Response<ApiResponse<DoctorVerificationResponse>> {
        error("submitVerification not expected in this test")
    }

    override suspend fun getPendingVerifications(): Response<ApiResponse<List<DoctorVerificationResponse>>> {
        return pendingResponses.removeFirst()
    }

    override suspend fun approveVerification(id: Long): Response<ApiResponse<DoctorVerificationResponse>> {
        approveCalls += id
        return approveResponses.removeFirst()
    }

    override suspend fun rejectVerification(
        id: Long,
        request: RejectVerificationRequest
    ): Response<ApiResponse<DoctorVerificationResponse>> {
        rejectCalls += (id to request.rejectionReason)
        return rejectResponses.removeFirst()
    }

    override suspend fun getAllDoctors(): Response<ApiResponse<List<DoctorSummary>>> {
        return doctorResponses.removeFirst()
    }

    override suspend fun revokeDoctor(userId: Long): Response<ApiResponse<Unit>> {
        revokeCalls += userId
        return revokeResponses.removeFirst()
    }
}

private class FakeMediaApi : MediaApi {
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
                    size = 100,
                    url = "https://example.com/certificate.jpg",
                ),
                message = "ok",
            )
        )
    }
}
