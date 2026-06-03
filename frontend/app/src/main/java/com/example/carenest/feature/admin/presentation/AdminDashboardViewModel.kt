package com.example.carenest.feature.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.admin.data.AdminDashboardStatsResponse
import com.example.carenest.feature.admin.data.repository.AdminRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val stats: AdminDashboardStatsResponse? = null,
    val error: String? = null,
)

class AdminDashboardViewModel(
    private val repository: AdminRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.getDashboardStats()
                }
            }.onSuccess { stats ->
                _uiState.update { it.copy(isLoading = false, stats = stats, error = null) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.localizedMessage ?: "Không thể tải thống kê quản trị",
                    )
                }
            }
        }
    }
}

class AdminDashboardViewModelFactory(
    private val repository: AdminRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminDashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
