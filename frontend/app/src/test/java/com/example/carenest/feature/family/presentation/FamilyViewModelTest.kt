package com.example.carenest.feature.family.presentation

import com.example.carenest.feature.family.domain.model.FamilyDetailResponse
import com.example.carenest.feature.family.domain.model.FamilyInvitationItem
import com.example.carenest.feature.family.domain.model.FamilyJoinCodeResponse
import com.example.carenest.feature.family.domain.model.FamilyMemberSummary
import com.example.carenest.feature.family.domain.model.FamilyResponse
import com.example.carenest.feature.family.domain.model.FamilySummary
import com.example.carenest.feature.family.domain.port.FamilyDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class FamilyViewModelTest {

    @Test
    fun createFamily_rejectsShortNameBeforeCallingRepository() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repository = FakeFamilyDataSource()
            val viewModel = FamilyViewModel(repository)
            advanceUntilIdle()

            viewModel.createFamily("A")
            advanceUntilIdle()

            assertEquals("Vui lòng nhập tên gia đình tối thiểu 2 ký tự", viewModel.uiState.value.error)
            assertTrue(repository.createFamilyCalls.isEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun inviteMember_requiresActiveFamilyAndValidEmail() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repository = FakeFamilyDataSource()
            val viewModel = FamilyViewModel(repository)
            advanceUntilIdle()

            viewModel.inviteMember("invalid-email", "MEMBER")
            advanceUntilIdle()
            assertEquals("Vui lòng chọn gia đình trước khi gửi lời mời", viewModel.uiState.value.error)

            viewModel.selectFamily(10L)
            advanceUntilIdle()
            viewModel.inviteMember("invalid-email", "MEMBER")
            advanceUntilIdle()

            assertEquals("Email không đúng định dạng", viewModel.uiState.value.error)
            assertTrue(repository.inviteCalls.isEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun joinFamilyByCode_normalizesCodeAndSavesActiveFamily() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val joinedFamily = familyDetail(id = 77L, name = "CareNest Home")
            val repository = FakeFamilyDataSource(
                joinFamilyResult = Result.success(joinedFamily),
                familyList = listOf(FamilySummary(77L, "CareNest Home", 2, "MEMBER", "Owner"))
            )
            val viewModel = FamilyViewModel(repository)
            advanceUntilIdle()

            viewModel.joinFamilyByCode(" ab12cd ", "MEMBER")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("AB12CD", repository.lastJoinCode)
            assertEquals("77", repository.savedActiveFamilyId)
            assertEquals(77L, state.activeFamilyId)
            assertEquals("Tham gia thành công", state.message)
            assertEquals(joinedFamily, state.activeFamily)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun familyDetail(id: Long, name: String): FamilyDetailResponse {
        return FamilyDetailResponse(
            id = id,
            name = name,
            ownerId = 1L,
            ownerUserId = 1L,
            memberCount = 2,
            createdAt = "2026-06-18T08:00:00Z",
            members = listOf(
                FamilyMemberSummary(
                    id = 1L,
                    familyMemberId = 1L,
                    profileId = 10L,
                    userId = 1L,
                    user = null,
                    fullName = "Owner",
                    role = "OWNER",
                    avatarUrl = null
                )
            )
        )
    }
}

private class FakeFamilyDataSource(
    private val familyList: List<FamilySummary> = emptyList(),
    private val activeFamilyId: String? = null,
    private val activeFamilyDetail: FamilyDetailResponse? = null,
    private val joinFamilyResult: Result<FamilyDetailResponse> = Result.failure(IllegalStateException("join not configured")),
) : FamilyDataSource {
    val createFamilyCalls = mutableListOf<String>()
    val inviteCalls = mutableListOf<Triple<Long, String, String>>()
    var savedActiveFamilyId: String? = null
    var lastJoinCode: String? = null

    override suspend fun getMyFamilyList(): Result<List<FamilySummary>> = Result.success(familyList)
    override suspend fun getFamilyById(familyId: Long): Result<FamilyDetailResponse> =
        activeFamilyDetail?.let { Result.success(it) } ?: Result.failure(IllegalStateException("family not found"))
    override suspend fun createFamily(name: String): Result<FamilyResponse> {
        createFamilyCalls += name
        return Result.failure(IllegalStateException("create not configured"))
    }
    override suspend fun joinFamilyByCode(code: String, role: String?): Result<FamilyDetailResponse> {
        lastJoinCode = code
        return joinFamilyResult
    }
    override suspend fun joinFamilyByQr(file: File, role: String): Result<FamilyDetailResponse> =
        Result.failure(IllegalStateException("join qr not configured"))
    override suspend fun inviteMember(familyId: Long, email: String, role: String): Result<Unit> {
        inviteCalls += Triple(familyId, email, role)
        return Result.success(Unit)
    }
    override suspend fun getReceivedInvitations(): Result<List<FamilyInvitationItem>> = Result.success(emptyList())
    override suspend fun getSentInvitations(): Result<List<FamilyInvitationItem>> = Result.success(emptyList())
    override suspend fun acceptInvitation(inviteId: Long): Result<Unit> = Result.success(Unit)
    override suspend fun rejectInvitation(inviteId: Long): Result<Unit> = Result.success(Unit)
    override suspend fun getFamilyJoinCode(): Result<FamilyJoinCodeResponse> =
        Result.failure(IllegalStateException("join code not configured"))
    override suspend fun rotateFamilyJoinCode(): Result<FamilyJoinCodeResponse> =
        Result.failure(IllegalStateException("rotate not configured"))
    override suspend fun saveActiveFamilyId(id: String) {
        savedActiveFamilyId = id
    }
    override suspend fun getActiveFamilyId(): String? = activeFamilyId
}
