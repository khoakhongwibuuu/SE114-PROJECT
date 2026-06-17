package com.example.carenest.feature.admin.presentation

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.core.data.network.PageResponse
import com.example.carenest.feature.admin.data.AdminApi
import com.example.carenest.feature.admin.data.AdminDashboardStatsResponse
import com.example.carenest.feature.admin.data.AdminReportSummaryResponse
import com.example.carenest.feature.admin.data.AdminUserRoleUpdateRequest
import com.example.carenest.feature.admin.data.AdminUserRoleUpdateResponse
import com.example.carenest.feature.admin.data.AdminUserStatusUpdateRequest
import com.example.carenest.feature.admin.data.AdminUserStatusUpdateResponse
import com.example.carenest.feature.admin.data.AdminUserSummaryResponse
import com.example.carenest.feature.admin.data.repository.AdminRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.util.ArrayDeque

@OptIn(ExperimentalCoroutinesApi::class)
class AdminUserManagementViewModelTest {

    @Test
    fun toggleAdminRole_rejectsLockedAccountBeforeCallingApi() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val api = FakeAdminApi()
            val repository = AdminRepository(api)
            val viewModel = AdminUserManagementViewModel(repository, dispatcher)
            val bannedUser = user(status = "BANNED")

            viewModel.toggleAdminRole(bannedUser)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.error)
            assertTrue(api.roleUpdateCalls.isEmpty())
            assertTrue(state.pendingUserIds.isEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun toggleUserStatus_updatesOptimisticStateWithRepositoryResult() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val api = FakeAdminApi().apply {
                statusResponses += Response.success(
                    ApiResponse(
                        success = true,
                        data = AdminUserStatusUpdateResponse(id = 7L, status = "BANNED"),
                        message = "ok",
                    )
                )
            }
            val repository = AdminRepository(api)
            val viewModel = AdminUserManagementViewModel(repository, dispatcher)
            val target = user(id = 7L, status = "ACTIVE")

            viewModel.toggleUserStatus(target)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("BANNED", state.optimisticStatuses[7L])
            assertTrue(state.pendingUserIds.isEmpty())
            assertNotNull(state.message)
            assertEquals("BANNED", api.statusUpdateCalls.single().status)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun toggleAdminRole_rollsBackOptimisticRoleWhenRepositoryFails() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val api = FakeAdminApi().apply {
                roleResponses += errorRoleResponse("cannot update role")
            }
            val repository = AdminRepository(api)
            val viewModel = AdminUserManagementViewModel(repository, dispatcher)
            val target = user(id = 9L, role = "USER", status = "ACTIVE")

            viewModel.toggleAdminRole(target)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(null, state.optimisticRoles[9L])
            assertTrue(state.pendingUserIds.isEmpty())
            assertNotNull(state.error)
            assertEquals("ADMIN", api.roleUpdateCalls.single().role)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun user(
        id: Long = 1L,
        role: String = "USER",
        status: String = "ACTIVE",
    ): AdminUserSummaryResponse {
        return AdminUserSummaryResponse(
            id = id,
            email = "user$id@example.com",
            fullName = "User $id",
            role = role,
            status = status,
        )
    }

    private fun errorRoleResponse(message: String): Response<ApiResponse<AdminUserRoleUpdateResponse>> {
        return Response.error(
            400,
            """{"message":"$message"}""".toResponseBody("application/json".toMediaType())
        )
    }
}

private class FakeAdminApi : AdminApi {
    val statusResponses = ArrayDeque<Response<ApiResponse<AdminUserStatusUpdateResponse>>>()
    val roleResponses = ArrayDeque<Response<ApiResponse<AdminUserRoleUpdateResponse>>>()
    val statusUpdateCalls = mutableListOf<AdminUserStatusUpdateRequest>()
    val roleUpdateCalls = mutableListOf<AdminUserRoleUpdateRequest>()

    override suspend fun getDashboardStats(): Response<ApiResponse<AdminDashboardStatsResponse>> {
        return Response.success(ApiResponse(success = true, data = AdminDashboardStatsResponse(), message = "ok"))
    }

    override suspend fun getUsers(
        page: Int,
        size: Int,
        search: String?
    ): Response<ApiResponse<PageResponse<AdminUserSummaryResponse>>> {
        return Response.success(
            ApiResponse(
                success = true,
                data = PageResponse(content = emptyList(), page = page, size = size, totalElements = 0, totalPages = 0, last = true),
                message = "ok",
            )
        )
    }

    override suspend fun updateUserStatus(
        userId: Long,
        request: AdminUserStatusUpdateRequest
    ): Response<ApiResponse<AdminUserStatusUpdateResponse>> {
        statusUpdateCalls += request
        return statusResponses.removeFirst()
    }

    override suspend fun updateUserRole(
        userId: Long,
        request: AdminUserRoleUpdateRequest
    ): Response<ApiResponse<AdminUserRoleUpdateResponse>> {
        roleUpdateCalls += request
        return roleResponses.removeFirst()
    }

    override suspend fun getReports(
        status: String,
        page: Int,
        size: Int
    ): Response<ApiResponse<List<AdminReportSummaryResponse>>> {
        return Response.success(ApiResponse(success = true, data = emptyList(), message = "ok"))
    }

    override suspend fun deletePost(postId: Long): Response<ApiResponse<Unit>> {
        return Response.success(ApiResponse(success = true, data = Unit, message = "ok"))
    }

    override suspend fun deleteComment(commentId: Long): Response<ApiResponse<Unit>> {
        return Response.success(ApiResponse(success = true, data = Unit, message = "ok"))
    }

    override suspend fun deleteMessage(messageId: Long): Response<ApiResponse<Unit>> {
        return Response.success(ApiResponse(success = true, data = Unit, message = "ok"))
    }

    override suspend fun dismissReport(reportId: Long): Response<ApiResponse<Unit>> {
        return Response.success(ApiResponse(success = true, data = Unit, message = "ok"))
    }
}
