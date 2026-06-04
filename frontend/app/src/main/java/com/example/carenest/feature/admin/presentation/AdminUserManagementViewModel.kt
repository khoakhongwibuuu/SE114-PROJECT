package com.example.carenest.feature.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.carenest.feature.admin.data.AdminUserPagingSource
import com.example.carenest.feature.admin.data.AdminUserSummaryResponse
import com.example.carenest.feature.admin.data.repository.AdminRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AdminUserManagementUiState(
    val search: String = "",
    val optimisticStatuses: Map<Long, String> = emptyMap(),
    val error: String? = null,
    val message: String? = null,
)

class AdminUserManagementViewModel(
    private val repository: AdminRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUserManagementUiState())
    val uiState: StateFlow<AdminUserManagementUiState> = _uiState.asStateFlow()

    private val searchFlow = MutableStateFlow("")

    val users: Flow<PagingData<AdminUserSummaryResponse>> = searchFlow
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(pageSize = 20, prefetchDistance = 4, enablePlaceholders = false),
            ) {
                AdminUserPagingSource(repository = repository, search = query.ifBlank { null })
            }.flow
        }
        .cachedIn(viewModelScope)

    fun onSearchChange(value: String) {
        _uiState.update { it.copy(search = value) }
        searchFlow.value = value.trim()
    }

    fun toggleUserStatus(user: AdminUserSummaryResponse) {
        val currentStatus = _uiState.value.optimisticStatuses[user.id] ?: user.status
        val targetStatus = if (currentStatus.equals("BANNED", ignoreCase = true)) "ACTIVE" else "BANNED"

        _uiState.update {
            it.copy(
                optimisticStatuses = it.optimisticStatuses + (user.id to targetStatus),
                error = null,
                message = if (targetStatus == "BANNED") "Đang khóa tài khoản..." else "Đang mở lại tài khoản...",
            )
        }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.updateUserStatus(user.id, targetStatus)
                }
            }.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        optimisticStatuses = it.optimisticStatuses + (user.id to updated.status),
                        error = null,
                        message = if (updated.status.equals("BANNED", ignoreCase = true)) {
                            "Đã khóa tài khoản người dùng"
                        } else {
                            "Đã mở lại tài khoản người dùng"
                        },
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        optimisticStatuses = it.optimisticStatuses - user.id,
                        error = error.localizedMessage ?: "Không thể cập nhật trạng thái người dùng",
                        message = null,
                    )
                }
            }
        }
    }

    fun clearTransientMessage() {
        _uiState.update { it.copy(error = null, message = null) }
    }
}

class AdminUserManagementViewModelFactory(
    private val repository: AdminRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminUserManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminUserManagementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
