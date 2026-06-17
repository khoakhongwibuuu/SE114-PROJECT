package com.example.carenest.feature.profile.presentation

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.health.data.remote.GrowthApi
import com.example.carenest.feature.health.domain.model.GrowthChartPointResponse
import com.example.carenest.feature.health.domain.model.GrowthRecordCreateRequest
import com.example.carenest.feature.health.domain.model.GrowthRecordResponse
import com.example.carenest.feature.profile.domain.port.MedicalProfileDataSource
import com.example.carenest.model.HealthProfile
import com.example.carenest.model.MedicalCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class UserMedicalViewModelTest {

    @Test
    fun saveGrowthRecord_rejectsFutureDateBeforeCallingApi() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repository = FakeMedicalProfileDataSource()
            val growthApi = FakeGrowthApi()
            val viewModel = UserMedicalViewModel(repository, growthApi)

            viewModel.onGrowthRecordDateChange(LocalDate.now().plusDays(1).toString())
            viewModel.onGrowthWeightChange("12.5")
            viewModel.onGrowthHeightChange("85")
            viewModel.saveGrowthRecord(profileId = 10L)
            advanceUntilIdle()

            assertEquals("Ngày ghi nhận không được ở tương lai", viewModel.uiState.value.growthError)
            assertEquals(0, growthApi.addGrowthCalls)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun saveGrowthRecord_clearsDraftAndReloadsOnSuccess() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repository = FakeMedicalProfileDataSource(profile = profile())
            val growthApi = FakeGrowthApi().apply {
                addGrowthResponse = Response.success(
                    ApiResponse(
                        success = true,
                        data = GrowthRecordResponse(
                            id = 1L,
                            recordDate = LocalDate.now().toString(),
                            weightKg = 12.5,
                            heightCm = 85.0
                        ),
                        message = "ok"
                    )
                )
                recordsResponse = Response.success(ApiResponse(success = true, data = listOf(record()), message = "ok"))
                chartResponse = Response.success(ApiResponse(success = true, data = listOf(chartPoint()), message = "ok"))
            }
            val viewModel = UserMedicalViewModel(repository, growthApi)

            viewModel.onGrowthWeightChange("12.5")
            viewModel.onGrowthHeightChange("85")
            viewModel.onGrowthHeadCircumferenceChange("45")
            viewModel.onGrowthNotesChange("Ổn định")
            viewModel.saveGrowthRecord(profileId = 10L)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("", state.growthWeight)
            assertEquals("", state.growthHeight)
            assertEquals("", state.growthHeadCircumference)
            assertEquals("", state.growthNotes)
            assertEquals("Đã lưu chỉ số tăng trưởng", state.successMessage)
            assertEquals(1, state.growthRecords.size)
            assertEquals(1, growthApi.addGrowthCalls)
            assertEquals(1, repository.getFamilyProfileCalls)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun saveMedicalProfile_requiresRequiredFields() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repository = FakeMedicalProfileDataSource()
            val growthApi = FakeGrowthApi()
            val viewModel = UserMedicalViewModel(repository, growthApi)

            viewModel.saveMedicalProfile(profileId = 10L)
            advanceUntilIdle()

            assertEquals("Vui lòng nhập đầy đủ họ tên, ngày sinh và giới tính", viewModel.uiState.value.error)
            assertEquals(0, repository.updateProfileCalls)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun saveMedicalProfile_updatesSuccessStateWhenRepositorySucceeds() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repository = FakeMedicalProfileDataSource(profile = profile())
            val growthApi = FakeGrowthApi()
            val viewModel = UserMedicalViewModel(repository, growthApi)

            viewModel.loadProfile(10L)
            advanceUntilIdle()
            viewModel.saveMedicalProfile(profileId = 10L)
            advanceUntilIdle()

            assertEquals(1, repository.updateProfileCalls)
            assertEquals("Cập nhật hồ sơ sức khỏe thành công.", viewModel.uiState.value.successMessage)
            assertNull(viewModel.uiState.value.error)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun profile(): HealthProfile {
        return HealthProfile(
            id = 10L,
            name = "Bé Na",
            role = "Con",
            age = 4,
            dateOfBirth = "2022-06-18",
            gender = "FEMALE",
            location = null,
            avatarUrl = null,
            bloodType = "A+",
            allergies = listOf("Sữa"),
            height = 95f,
            weight = 14f,
            bmi = 15.5f,
            medicalHistory = listOf(MedicalCondition("Hen nhẹ", "Theo dõi")),
            emergencyContact = null
        )
    }

    private fun record(): GrowthRecordResponse {
        return GrowthRecordResponse(
            id = 1L,
            recordDate = LocalDate.now().toString(),
            weightKg = 12.5,
            heightCm = 85.0,
            bmi = 17.3
        )
    }

    private fun chartPoint(): GrowthChartPointResponse {
        return GrowthChartPointResponse(
            recordDate = LocalDate.now().toString(),
            weightKg = 12.5,
            heightCm = 85.0,
            bmi = 17.3
        )
    }
}

private class FakeMedicalProfileDataSource(
    private val profile: HealthProfile? = null,
    private val updateResult: Result<Unit> = Result.success(Unit)
) : MedicalProfileDataSource {
    var getFamilyProfileCalls = 0
    var updateProfileCalls = 0

    override suspend fun getFamilyProfile(profileId: Long): Result<HealthProfile> {
        getFamilyProfileCalls += 1
        return profile?.let { Result.success(it) } ?: Result.failure(IllegalStateException("profile not configured"))
    }

    override suspend fun updateProfile(
        profileId: Long,
        fullName: String,
        birthday: String?,
        gender: String?,
        relationship: String,
        height: Double?,
        weight: Double?,
        bloodType: String?,
        allergy: String?,
        medicalHistory: String?
    ): Result<Unit> {
        updateProfileCalls += 1
        return updateResult
    }
}

private class FakeGrowthApi : GrowthApi {
    var addGrowthResponse: Response<ApiResponse<GrowthRecordResponse>> = Response.error(
        500,
        """{"message":"not configured"}""".toResponseBody("application/json".toMediaType())
    )
    var recordsResponse: Response<ApiResponse<List<GrowthRecordResponse>>> = Response.success(
        ApiResponse(success = true, data = emptyList(), message = "ok")
    )
    var chartResponse: Response<ApiResponse<List<GrowthChartPointResponse>>> = Response.success(
        ApiResponse(success = true, data = emptyList(), message = "ok")
    )
    var addGrowthCalls = 0

    override suspend fun addGrowthRecord(
        profileId: Long,
        request: GrowthRecordCreateRequest
    ): Response<ApiResponse<GrowthRecordResponse>> {
        addGrowthCalls += 1
        return addGrowthResponse
    }

    override suspend fun getGrowthRecords(profileId: Long): Response<ApiResponse<List<GrowthRecordResponse>>> {
        return recordsResponse
    }

    override suspend fun getGrowthChart(profileId: Long): Response<ApiResponse<List<GrowthChartPointResponse>>> {
        return chartResponse
    }
}
