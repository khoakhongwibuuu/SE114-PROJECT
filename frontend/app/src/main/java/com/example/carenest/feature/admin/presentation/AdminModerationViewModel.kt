package com.example.carenest.feature.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.carenest.feature.admin.data.AdminContentType
import com.example.carenest.feature.admin.data.AdminReportPagingSource
import com.example.carenest.feature.admin.data.AdminReportSummaryResponse
import com.example.carenest.feature.admin.data.repository.AdminRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ModerationAction {
    DELETE_CONTENT,
    DISMISS,
}

data class AdminModerationUiState(
    val hiddenReportIds: Set<Long> = emptySet(),
    val error: String? = null,
    val message: String? = null,
)

class AdminModerationViewModel(
    private val repository: AdminRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminModerationUiState())
    val uiState: StateFlow<AdminModerationUiState> = _uiState.asStateFlow()

    val reports: Flow<PagingData<AdminReportSummaryResponse>> = Pager(
        config = PagingConfig(pageSize = 20, prefetchDistance = 4, enablePlaceholders = false),
    ) {
        AdminReportPagingSource(repository)
    }.flow.cachedIn(viewModelScope)

    fun resolveReport(report: AdminReportSummaryResponse, action: ModerationAction) {
        val contentLabel = report.normalizedContentType().label()
        _uiState.update {
            it.copy(
                hiddenReportIds = it.hiddenReportIds + report.id,
                error = null,
                message = if (action == ModerationAction.DELETE_CONTENT) {
                    "Đang xóa $contentLabel vi phạm..."
                } else {
                    "Đang bỏ qua báo cáo..."
                },
            )
        }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    when (action) {
                        ModerationAction.DELETE_CONTENT -> deleteReportedContent(report)
                        ModerationAction.DISMISS -> repository.dismissReport(report.id)
                    }
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        message = if (action == ModerationAction.DELETE_CONTENT) {
                            "Đã xóa $contentLabel vi phạm"
                        } else {
                            "Đã bỏ qua báo cáo"
                        },
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        hiddenReportIds = it.hiddenReportIds - report.id,
                        error = error.localizedMessage ?: "Không thể xử lý báo cáo",
                        message = null,
                    )
                }
            }
        }
    }

    private suspend fun deleteReportedContent(report: AdminReportSummaryResponse) {
        when (report.normalizedContentType()) {
            AdminContentType.COMMENT -> {
                val commentId = report.commentId
                    ?: throw IllegalStateException("Báo cáo bình luận đang thiếu commentId")
                repository.deleteComment(commentId)
            }

            AdminContentType.POST -> {
                val postId = report.postId
                    ?: throw IllegalStateException("Báo cáo bài viết đang thiếu postId")
                repository.deletePost(postId)
            }
        }
    }

    fun clearTransientMessage() {
        _uiState.update { it.copy(error = null, message = null) }
    }

    private fun AdminContentType.label(): String {
        return if (this == AdminContentType.COMMENT) "bình luận" else "bài viết"
    }
}

class AdminModerationViewModelFactory(
    private val repository: AdminRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminModerationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminModerationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
