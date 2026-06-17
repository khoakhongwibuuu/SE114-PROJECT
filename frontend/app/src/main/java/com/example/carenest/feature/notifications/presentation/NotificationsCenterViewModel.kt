package com.example.carenest.feature.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireData
import com.example.carenest.core.data.network.requireSuccess
import com.example.carenest.feature.notifications.data.remote.NotificationApi
import com.example.carenest.feature.notifications.domain.model.NotificationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.LinkedHashMap
import java.util.Locale
import java.util.TimeZone

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val notifications: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
    val groupedNotifications: Map<String, List<NotificationItem>> = emptyMap(),
    val error: String? = null
)

class NotificationsCenterViewModel(
    private val notificationApi: NotificationApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun loadNotifications(_profileId: Long?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val (listResponse, countResponse) = withContext(Dispatchers.IO) {
                    notificationApi.getNotifications() to notificationApi.getUnreadCount()
                }
                if (listResponse.isSuccessful) {
                    val list = listResponse.requireData("Không thể tải danh sách thông báo").content
                    val sortedList = list.sortedByDescending { parseIsoDate(it.createdAt)?.time ?: 0L }
                    var countError: String? = null
                    val unreadCount = if (countResponse.isSuccessful) {
                        runCatching {
                            countResponse.requireData("Không thể tải số thông báo chưa đọc")
                                .count
                                .coerceAtMost(Int.MAX_VALUE.toLong())
                                .toInt()
                        }.getOrElse { error ->
                            countError = error.message ?: "Không thể tải số thông báo chưa đọc"
                            sortedList.count { notification -> !notification.isRead }
                        }
                    } else {
                        countError = countResponse.errorMessage("Không thể tải số thông báo chưa đọc")
                        sortedList.count { notification -> !notification.isRead }
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = sortedList,
                            unreadCount = unreadCount,
                            groupedNotifications = groupNotifications(sortedList),
                            error = countError
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = listResponse.errorMessage("Không thể tải danh sách thông báo")
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Lỗi kết nối mạng"
                    )
                }
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        val target = _uiState.value.notifications.firstOrNull { it.id == notificationId }
        if (target?.isRead == true) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, error = null) }
            try {
                val response = withContext(Dispatchers.IO) {
                    notificationApi.markAsRead(notificationId)
                }
                if (response.isSuccessful) {
                    response.requireData("Không thể đánh dấu thông báo đã đọc")
                    val countResponse = withContext(Dispatchers.IO) {
                        notificationApi.getUnreadCount()
                    }
                    val fallbackCount = (_uiState.value.unreadCount - 1).coerceAtLeast(0)
                    updateNotificationReadState(
                        notificationId = notificationId,
                        unreadCount = if (countResponse.isSuccessful) {
                            runCatching {
                                countResponse.requireData("Không thể tải số thông báo chưa đọc")
                                    .count
                                    .coerceAtMost(Int.MAX_VALUE.toLong())
                                    .toInt()
                            }.getOrDefault(fallbackCount)
                        } else {
                            fallbackCount
                        }
                    )
                } else {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            error = response.errorMessage("Không thể đánh dấu thông báo đã đọc")
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        error = e.localizedMessage ?: "Lỗi kết nối mạng"
                    )
                }
            }
        }
    }

    fun markAllAsRead() {
        val unreadNotifications = _uiState.value.notifications.filter { !it.isRead }
        if (unreadNotifications.isEmpty()) {
            return
        }

        viewModelScope.launch {
            val previousState = _uiState.value
            val updatedList = previousState.notifications.map { it.copy(isRead = true) }
            applyNotificationList(updatedList, unreadCount = 0, isActionLoading = true, error = null)

            try {
                val response = withContext(Dispatchers.IO) {
                    notificationApi.markAllAsRead()
                }
                if (response.isSuccessful) {
                    response.requireSuccess("Không thể đánh dấu tất cả thông báo đã đọc")
                    applyNotificationList(updatedList, unreadCount = 0, isActionLoading = false, error = null)
                } else {
                    _uiState.value = previousState.copy(
                        isActionLoading = false,
                        error = response.errorMessage("Không thể đánh dấu tất cả thông báo đã đọc")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = previousState.copy(
                    isActionLoading = false,
                    error = e.localizedMessage ?: "Lỗi kết nối mạng"
                )
            }
        }
    }

    private fun updateNotificationReadState(notificationId: Long, unreadCount: Int?) {
        val updatedList = _uiState.value.notifications.map { item ->
            if (item.id == notificationId) item.copy(isRead = true) else item
        }
        applyNotificationList(
            list = updatedList,
            unreadCount = unreadCount ?: updatedList.count { notification -> !notification.isRead },
            isActionLoading = false,
            error = null
        )
    }

    private fun applyNotificationList(
        list: List<NotificationItem>,
        unreadCount: Int = list.count { notification -> !notification.isRead },
        isActionLoading: Boolean = _uiState.value.isActionLoading,
        error: String?
    ) {
        _uiState.update {
            it.copy(
                notifications = list,
                unreadCount = unreadCount,
                groupedNotifications = groupNotifications(list),
                isActionLoading = isActionLoading,
                error = error
            )
        }
    }

    private fun groupNotifications(list: List<NotificationItem>): Map<String, List<NotificationItem>> {
        val todayList = mutableListOf<NotificationItem>()
        val yesterdayList = mutableListOf<NotificationItem>()
        val thisWeekList = mutableListOf<NotificationItem>()
        val olderList = mutableListOf<NotificationItem>()

        val todayCal = Calendar.getInstance()
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        val startOfWeekCal = Calendar.getInstance().apply { add(Calendar.DATE, -7) }

        for (item in list) {
            val date = parseIsoDate(item.createdAt)
            if (date == null) {
                olderList.add(item)
                continue
            }

            val itemCal = Calendar.getInstance().apply { time = date }
            val isToday = itemCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                itemCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
            val isYesterday = itemCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
                itemCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR)
            val isThisWeek = itemCal.after(startOfWeekCal) && !isToday && !isYesterday

            when {
                isToday -> todayList.add(item)
                isYesterday -> yesterdayList.add(item)
                isThisWeek -> thisWeekList.add(item)
                else -> olderList.add(item)
            }
        }

        return LinkedHashMap<String, List<NotificationItem>>().apply {
            if (todayList.isNotEmpty()) put("today", todayList)
            if (yesterdayList.isNotEmpty()) put("yesterday", yesterdayList)
            if (thisWeekList.isNotEmpty()) put("this_week", thisWeekList)
            if (olderList.isNotEmpty()) put("older", olderList)
        }
    }
}

internal fun parseIsoDate(value: String?): Date? {
    if (value.isNullOrBlank()) {
        return null
    }

    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss"
    )

    for (pattern in patterns) {
        try {
            return SimpleDateFormat(pattern, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(value)
        } catch (_: Exception) {
            // Try the next backend timestamp shape.
        }
    }
    return null
}

class NotificationsCenterViewModelFactory(
    private val notificationApi: NotificationApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsCenterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationsCenterViewModel(notificationApi) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
