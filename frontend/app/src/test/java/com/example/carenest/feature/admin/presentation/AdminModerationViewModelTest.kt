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
class AdminModerationViewModelTest {

    @Test
    fun resolveReport_deletesCommentAndKeepsReportHiddenOnSuccess() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val api = FakeModerationAdminApi().apply {
                deleteCommentResponses += successUnit()
            }
            val repository = AdminRepository(api)
            val viewModel = AdminModerationViewModel(repository, dispatcher)
            val report = report(id = 11L, contentType = "COMMENT", commentId = 88L)

            viewModel.resolveReport(report, ModerationAction.DELETE_CONTENT)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.hiddenReportIds.contains(11L))
            assertEquals(listOf(88L), api.deletedCommentIds)
            assertNotNull(state.message)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun resolveReport_restoresReportWhenDismissFails() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val api = FakeModerationAdminApi().apply {
                dismissResponses += errorUnit("cannot dismiss")
            }
            val repository = AdminRepository(api)
            val viewModel = AdminModerationViewModel(repository, dispatcher)
            val report = report(id = 21L, contentType = "POST", postId = 77L)

            viewModel.resolveReport(report, ModerationAction.DISMISS)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.hiddenReportIds.isEmpty())
            assertNotNull(state.error)
            assertEquals(listOf(21L), api.dismissedReportIds)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun report(
        id: Long,
        contentType: String,
        postId: Long? = null,
        commentId: Long? = null,
        messageId: Long? = null,
    ): AdminReportSummaryResponse {
        return AdminReportSummaryResponse(
            id = id,
            postId = postId,
            messageId = messageId,
            commentId = commentId,
            contentType = contentType,
            reporterId = 1L,
            reporterName = "Reporter",
            reporterEmail = "reporter@example.com",
            reason = "Spam",
            previewText = "preview",
            previewImageUrl = null,
            contentAuthorName = "Author",
            status = "PENDING",
            createdAt = "2026-06-18T08:00:00Z",
        )
    }

    private fun successUnit(): Response<ApiResponse<Unit>> {
        return Response.success(ApiResponse(success = true, data = Unit, message = "ok"))
    }

    private fun errorUnit(message: String): Response<ApiResponse<Unit>> {
        return Response.error(
            400,
            """{"message":"$message"}""".toResponseBody("application/json".toMediaType())
        )
    }
}

private class FakeModerationAdminApi : AdminApi {
    val deleteCommentResponses = ArrayDeque<Response<ApiResponse<Unit>>>()
    val dismissResponses = ArrayDeque<Response<ApiResponse<Unit>>>()
    val deletedCommentIds = mutableListOf<Long>()
    val dismissedReportIds = mutableListOf<Long>()

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
        error("updateUserStatus not expected in this test")
    }

    override suspend fun updateUserRole(
        userId: Long,
        request: AdminUserRoleUpdateRequest
    ): Response<ApiResponse<AdminUserRoleUpdateResponse>> {
        error("updateUserRole not expected in this test")
    }

    override suspend fun getReports(
        status: String,
        page: Int,
        size: Int
    ): Response<ApiResponse<List<AdminReportSummaryResponse>>> {
        return Response.success(ApiResponse(success = true, data = emptyList(), message = "ok"))
    }

    override suspend fun deletePost(postId: Long): Response<ApiResponse<Unit>> {
        error("deletePost not expected in this test")
    }

    override suspend fun deleteComment(commentId: Long): Response<ApiResponse<Unit>> {
        deletedCommentIds += commentId
        return deleteCommentResponses.removeFirst()
    }

    override suspend fun deleteMessage(messageId: Long): Response<ApiResponse<Unit>> {
        error("deleteMessage not expected in this test")
    }

    override suspend fun dismissReport(reportId: Long): Response<ApiResponse<Unit>> {
        dismissedReportIds += reportId
        return dismissResponses.removeFirst()
    }
}
