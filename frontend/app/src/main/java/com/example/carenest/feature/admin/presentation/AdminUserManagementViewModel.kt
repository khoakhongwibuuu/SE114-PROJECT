package com.example.carenest.feature.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.carenest.feature.admin.data.AdminUserAuditLogItem
import com.example.carenest.feature.admin.data.AdminUserSummaryResponse
import com.example.carenest.feature.admin.data.repository.AdminRepository
import com.example.carenest.feature.admin.data.AdminUserPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
    val optimisticRoles: Map<Long, String> = emptyMap(),
    val pendingUserIds: Set<Long> = emptySet(),
    val auditLogs: List<AdminUserAuditLogItem> = emptyList(),
    val isAuditLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class AdminUserManagementViewModel(
    private val repository: AdminRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUserManagementUiState())
    val uiState: StateFlow<AdminUserManagementUiState> = _uiState.asStateFlow()

    private val searchFlow = MutableStateFlow("")

    val users: Flow<PagingData<AdminUserSummaryResponse>> = searchFlow
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(pageSize = 20, prefetchDistance = 4, enablePlaceholders = false)
            ) {
                AdminUserPagingSource(repository = repository, search = query.ifBlank { null })
            }.flow
        }
        .cachedIn(viewModelScope)

    init {
        refreshAuditLogs()
    }

    fun onSearchChange(value: String) {
        _uiState.update { it.copy(search = value) }
        searchFlow.value = value.trim()
    }

    fun refreshAuditLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuditLoading = true, error = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.getUserAuditLogs()
                }
            }.onSuccess { logs ->
                _uiState.update {
                    it.copy(
                        auditLogs = logs,
                        isAuditLoading = false,
                        error = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAuditLoading = false,
                        error = error.localizedMessage ?: "Không thể tải nhật ký override người dùng"
                    )
                }
            }
        }
    }

    fun toggleUserStatus(user: AdminUserSummaryResponse, reason: String) {
        if (_uiState.value.pendingUserIds.contains(user.id)) return
        val normalizedReason = reason.trim()
        if (normalizedReason.isEmpty()) {
            _uiState.update { it.copy(error = "Lý do thao tác không được để trống", message = null) }
            return
        }

        val currentStatus = _uiState.value.optimisticStatuses[user.id] ?: user.status
        val targetStatus = if (isLockedStatus(currentStatus)) "ACTIVE" else "BANNED"

        _uiState.update {
            it.copy(
                optimisticStatuses = it.optimisticStatuses + (user.id to targetStatus),
                pendingUserIds = it.pendingUserIds + user.id,
                error = null,
                message = if (targetStatus == "BANNED") "Đang khóa tài khoản..." else "Đang mở lại tài khoản..."
            )
        }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.updateUserStatus(user.id, targetStatus, normalizedReason)
                }
            }.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        optimisticStatuses = it.optimisticStatuses + (user.id to updated.status),
                        pendingUserIds = it.pendingUserIds - user.id,
                        error = null,
                        message = if (updated.status.equals("BANNED", ignoreCase = true)) {
                            "Đã khóa tài khoản người dùng"
                        } else {
                            "Đã mở lại tài khoản người dùng"
                        }
                    )
                }
                refreshAuditLogs()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        optimisticStatuses = it.optimisticStatuses - user.id,
                        pendingUserIds = it.pendingUserIds - user.id,
                        error = error.localizedMessage ?: "Không thể cập nhật trạng thái người dùng",
                        message = null
                    )
                }
            }
        }
    }

    fun toggleAdminRole(user: AdminUserSummaryResponse, reason: String) {
        if (_uiState.value.pendingUserIds.contains(user.id)) return
        val normalizedReason = reason.trim()
        if (normalizedReason.isEmpty()) {
            _uiState.update { it.copy(error = "Lý do thao tác không được để trống", message = null) }
            return
        }

        val currentStatus = _uiState.value.optimisticStatuses[user.id] ?: user.status
        if (isLockedStatus(currentStatus)) {
            _uiState.update {
                it.copy(
                    error = "Không thể đổi quyền quản trị cho tài khoản đang bị khóa",
                    message = null
                )
            }
            return
        }

        val currentRole = _uiState.value.optimisticRoles[user.id] ?: user.role
        val targetRole = if (currentRole.equals("ADMIN", ignoreCase = true)) "USER" else "ADMIN"

        _uiState.update {
            it.copy(
                optimisticRoles = it.optimisticRoles + (user.id to targetRole),
                pendingUserIds = it.pendingUserIds + user.id,
                error = null,
                message = if (targetRole == "ADMIN") "Đang cấp quyền admin..." else "Đang gỡ quyền admin..."
            )
        }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.updateUserRole(user.id, targetRole, normalizedReason)
                }
            }.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        optimisticRoles = it.optimisticRoles + (user.id to updated.role),
                        pendingUserIds = it.pendingUserIds - user.id,
                        error = null,
                        message = if (updated.role.equals("ADMIN", ignoreCase = true)) {
                            "Đã cấp quyền admin"
                        } else {
                            "Đã gỡ quyền admin"
                        }
                    )
                }
                refreshAuditLogs()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        optimisticRoles = it.optimisticRoles - user.id,
                        pendingUserIds = it.pendingUserIds - user.id,
                        error = error.localizedMessage ?: "Không thể cập nhật quyền người dùng",
                        message = null
                    )
                }
            }
        }
    }

    fun clearTransientMessage() {
        _uiState.update { it.copy(error = null, message = null) }
    }

    private fun isLockedStatus(status: String): Boolean {
        return status.equals("BANNED", ignoreCase = true) || status.equals("INACTIVE", ignoreCase = true)
    }
}

class AdminUserManagementViewModelFactory(
    private val repository: AdminRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminUserManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminUserManagementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
