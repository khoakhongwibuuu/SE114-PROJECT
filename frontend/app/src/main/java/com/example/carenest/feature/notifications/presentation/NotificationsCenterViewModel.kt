package com.example.carenest.feature.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import java.util.*

data class NotificationsUiState(
    val isLoading: Boolean = false,
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

    private var currentProfileId: Long? = null

    fun loadNotifications(profileId: Long?) {
        currentProfileId = profileId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = withContext(Dispatchers.IO) {
                    notificationApi.getNotifications(profileId)
                }
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()?.data ?: emptyList()
                    val sortedList = list.sortedByDescending { it.createdAt ?: "" }
                    val unread = sortedList.count { !it.isRead }
                    val grouped = groupNotifications(sortedList)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = sortedList,
                            unreadCount = unread,
                            groupedNotifications = grouped
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.body()?.message ?: "Không thể tải danh sách thông báo"
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
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    notificationApi.markAsRead(notificationId)
                }
                if (response.isSuccessful) {
                    // Update state locally
                    val updatedList = _uiState.value.notifications.map {
                        if (it.notificationId == notificationId) {
                            it.copy(isRead = true)
                        } else {
                            it
                        }
                    }
                    _uiState.update {
                        it.copy(
                            notifications = updatedList,
                            unreadCount = updatedList.count { n -> !n.isRead },
                            groupedNotifications = groupNotifications(updatedList)
                        )
                    }
                }
            } catch (e: Exception) {
                // Keep UI updated or silent error
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val unreadNotifications = _uiState.value.notifications.filter { !it.isRead }
            if (unreadNotifications.isEmpty()) return@launch

            // Optimistically update locally
            val updatedList = _uiState.value.notifications.map { it.copy(isRead = true) }
            _uiState.update {
                it.copy(
                    notifications = updatedList,
                    unreadCount = 0,
                    groupedNotifications = groupNotifications(updatedList)
                )
            }

            // Sync with backend asynchronously
            withContext(Dispatchers.IO) {
                unreadNotifications.forEach {
                    try {
                        notificationApi.markAsRead(it.notificationId)
                    } catch (e: Exception) {
                        // ignore network errors for individual calls
                    }
                }
            }
        }
    }

    private fun groupNotifications(list: List<NotificationItem>): Map<String, List<NotificationItem>> {
        val todayList = mutableListOf<NotificationItem>()
        val yesterdayList = mutableListOf<NotificationItem>()
        val thisWeekList = mutableListOf<NotificationItem>()
        val olderList = mutableListOf<NotificationItem>()

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val todayCal = Calendar.getInstance()
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        val startOfWeekCal = Calendar.getInstance().apply { add(Calendar.DATE, -7) }

        for (item in list) {
            val dateStr = item.createdAt ?: ""
            val date = try {
                sdf.parse(dateStr)
            } catch (e: Exception) {
                null
            }

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

        val groups = LinkedHashMap<String, List<NotificationItem>>()
        if (todayList.isNotEmpty()) groups["today"] = todayList
        if (yesterdayList.isNotEmpty()) groups["yesterday"] = yesterdayList
        if (thisWeekList.isNotEmpty()) groups["this_week"] = thisWeekList
        if (olderList.isNotEmpty()) groups["older"] = olderList

        return groups
    }
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
