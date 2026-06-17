package com.example.carenest.feature.notifications.presentation

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.core.data.network.PageResponse
import com.example.carenest.feature.notifications.data.remote.NotificationApi
import com.example.carenest.feature.notifications.data.remote.UnreadCountResponse
import com.example.carenest.feature.notifications.domain.model.NotificationItem
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.util.ArrayDeque

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsCenterViewModelTest {

    @Test
    fun loadNotifications_fallsBackToDerivedUnreadCountWhenCountRequestFails() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val api = FakeNotificationApi().apply {
                notificationsResponses += successPage(
                    listOf(
                        notification(
                            id = 2L,
                            isRead = false,
                            createdAt = "2026-06-17T10:00:00Z"
                        ),
                        notification(
                            id = 1L,
                            isRead = true,
                            createdAt = "2026-06-16T09:00:00Z"
                        ),
                        notification(
                            id = 3L,
                            isRead = false,
                            createdAt = "2026-06-18T08:00:00Z"
                        )
                    )
                )
                unreadCountResponses += errorUnreadCount("count failed")
            }
            val viewModel = NotificationsCenterViewModel(api, dispatcher)

            viewModel.loadNotifications(null)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(listOf(3L, 2L, 1L), state.notifications.map { it.id })
            assertEquals(2, state.unreadCount)
            assertNotNull(state.error)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun markAsRead_updatesItemAndUsesFallbackUnreadCountWhenRefreshFails() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val first = notification(id = 1L, isRead = false, createdAt = "2026-06-18T08:00:00Z")
            val second = notification(id = 2L, isRead = false, createdAt = "2026-06-17T08:00:00Z")
            val api = FakeNotificationApi().apply {
                notificationsResponses += successPage(listOf(first, second))
                unreadCountResponses += successUnreadCount(2)
                markAsReadResponses.getOrPut(1L) { ArrayDeque() } += successNotification(first.copy(isRead = true))
                unreadCountResponses += errorUnreadCount("refresh failed")
            }
            val viewModel = NotificationsCenterViewModel(api, dispatcher)

            viewModel.loadNotifications(null)
            advanceUntilIdle()
            viewModel.markAsRead(1L)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.notifications.first { it.id == 1L }.isRead)
            assertFalse(state.notifications.first { it.id == 2L }.isRead)
            assertEquals(1, state.unreadCount)
            assertNotNull(state.message)
            assertEquals(null, state.error)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun markAllAsRead_restoresPreviousStateWhenRequestFails() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val first = notification(id = 1L, isRead = false, createdAt = "2026-06-18T08:00:00Z")
            val second = notification(id = 2L, isRead = false, createdAt = "2026-06-17T08:00:00Z")
            val api = FakeNotificationApi().apply {
                notificationsResponses += successPage(listOf(first, second))
                unreadCountResponses += successUnreadCount(2)
                markAllResponses += errorUnreadCount("cannot mark all")
            }
            val viewModel = NotificationsCenterViewModel(api, dispatcher)

            viewModel.loadNotifications(null)
            advanceUntilIdle()
            viewModel.markAllAsRead()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(2, state.unreadCount)
            assertFalse(state.notifications.all { it.isRead })
            assertNotNull(state.error)
            assertFalse(state.isActionLoading)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun notification(
        id: Long,
        isRead: Boolean,
        createdAt: String,
    ): NotificationItem {
        return NotificationItem(
            id = id,
            userId = 10L,
            title = "Notification $id",
            message = "Message $id",
            type = "SYSTEM",
            referenceType = null,
            referenceId = null,
            isRead = isRead,
            createdAt = createdAt,
        )
    }

    private fun successPage(items: List<NotificationItem>): Response<ApiResponse<PageResponse<NotificationItem>>> {
        return Response.success(
            ApiResponse(
                success = true,
                data = PageResponse(
                    content = items,
                    page = 0,
                    size = items.size.coerceAtLeast(1),
                    totalElements = items.size.toLong(),
                    totalPages = 1,
                    last = true,
                ),
                message = "ok",
            )
        )
    }

    private fun successNotification(item: NotificationItem): Response<ApiResponse<NotificationItem>> {
        return Response.success(ApiResponse(success = true, data = item, message = "ok"))
    }

    private fun successUnreadCount(count: Long): Response<ApiResponse<UnreadCountResponse>> {
        return Response.success(
            ApiResponse(success = true, data = UnreadCountResponse(count = count), message = "ok")
        )
    }

    private fun errorUnreadCount(message: String): Response<ApiResponse<UnreadCountResponse>> {
        return errorResponse(message)
    }

    private fun <T> errorResponse(message: String, code: Int = 500): Response<ApiResponse<T>> {
        return Response.error(
            code,
            """{"message":"$message"}""".toResponseBody("application/json".toMediaType())
        )
    }
}

private class FakeNotificationApi : NotificationApi {
    val notificationsResponses = ArrayDeque<Response<ApiResponse<PageResponse<NotificationItem>>>>()
    val unreadCountResponses = ArrayDeque<Response<ApiResponse<UnreadCountResponse>>>()
    val markAllResponses = ArrayDeque<Response<ApiResponse<UnreadCountResponse>>>()
    val markAsReadResponses = mutableMapOf<Long, ArrayDeque<Response<ApiResponse<NotificationItem>>>>()

    override suspend fun getNotifications(
        type: String?,
        page: Int,
        size: Int
    ): Response<ApiResponse<PageResponse<NotificationItem>>> {
        return notificationsResponses.removeFirst()
    }

    override suspend fun markAsRead(id: Long): Response<ApiResponse<NotificationItem>> {
        return markAsReadResponses[id]?.removeFirst()
            ?: error("Missing markAsRead response for notification $id")
    }

    override suspend fun markAllAsRead(): Response<ApiResponse<UnreadCountResponse>> {
        return markAllResponses.removeFirst()
    }

    override suspend fun getUnreadCount(): Response<ApiResponse<UnreadCountResponse>> {
        return unreadCountResponses.removeFirst()
    }
}
