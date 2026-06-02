package com.example.carenest.feature.community.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.community.data.repository.CommunityRepository
import com.example.carenest.feature.community.domain.model.CommunityGroup
import com.example.carenest.feature.community.domain.model.CommunityGroupPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CommunityUiState(
    val isLoading: Boolean = true,
    val search: String = "",
    val myGroups: List<CommunityGroup> = emptyList(),
    val discoverGroups: List<CommunityGroup> = emptyList(),
    val error: String? = null,
    val joiningGroupId: Long? = null,
    val previewGroup: CommunityGroupPreview? = null,
    val isPreviewLoading: Boolean = false
)

@OptIn(FlowPreview::class)
class CommunityViewModel(
    private val repository: CommunityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()
    private val searchFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            searchFlow.debounce(300).collect { loadGroups(it) }
        }
    }

    fun onSearchChange(value: String) {
        _uiState.value = _uiState.value.copy(search = value)
        searchFlow.value = value.trim()
    }

    fun refresh() {
        viewModelScope.launch {
            loadGroups(_uiState.value.search.trim())
        }
    }

    fun loadGroupPreview(groupId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPreviewLoading = true, error = null, previewGroup = null)
            try {
                val preview = withContext(Dispatchers.IO) {
                    repository.preview(groupId)
                }
                _uiState.value = _uiState.value.copy(
                    isPreviewLoading = false,
                    previewGroup = preview
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPreviewLoading = false,
                    error = e.localizedMessage ?: "Không thể tải chi tiết nhóm"
                )
            }
        }
    }

    fun clearPreview() {
        _uiState.value = _uiState.value.copy(previewGroup = null, error = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun join(group: CommunityGroup, onSuccess: (CommunityGroup) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(joiningGroupId = group.id, error = null)
            try {
                val preview = withContext(Dispatchers.IO) {
                    repository.join(group.id)
                }
                val joinedGroup = group.copy(
                    joined = true,
                    memberCount = preview.memberCount,
                    latestMessage = group.latestMessage ?: "Nhóm vừa được tạo"
                )
                _uiState.value = _uiState.value.copy(
                    joiningGroupId = null,
                    myGroups = listOf(joinedGroup) + _uiState.value.myGroups.filterNot { it.id == group.id },
                    discoverGroups = _uiState.value.discoverGroups.filterNot { it.id == group.id }
                )
                onSuccess(joinedGroup)
                if (!preview.joined) {
                    refresh()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    joiningGroupId = null,
                    error = e.localizedMessage ?: "Không thể tham gia nhóm"
                )
            }
        }
    }

    fun joinFromPreview(preview: CommunityGroupPreview, onSuccess: (CommunityGroup) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(joiningGroupId = preview.id, error = null)
            try {
                val updatedPreview = withContext(Dispatchers.IO) {
                    repository.join(preview.id)
                }
                val discoverList = _uiState.value.discoverGroups.filterNot { it.id == preview.id }
                val joinedGroup = CommunityGroup(
                    id = preview.id,
                    name = preview.name,
                    description = preview.description,
                    private = preview.private,
                    category = preview.category,
                    memberCount = updatedPreview.memberCount,
                    leadDoctorName = preview.leadDoctorName,
                    joined = true,
                    latestMessage = "Nhóm vừa được tạo",
                    latestActivityAt = null
                )
                _uiState.value = _uiState.value.copy(
                    joiningGroupId = null,
                    previewGroup = updatedPreview,
                    myGroups = listOf(joinedGroup) + _uiState.value.myGroups.filterNot { it.id == preview.id },
                    discoverGroups = discoverList
                )
                onSuccess(joinedGroup)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    joiningGroupId = null,
                    error = e.localizedMessage ?: "Không thể tham gia nhóm"
                )
            }
        }
    }

    private suspend fun loadGroups(search: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            val query = search.ifBlank { null }
            val mine = withContext(Dispatchers.IO) {
                repository.myGroups(query)
            }
            val discover = withContext(Dispatchers.IO) {
                repository.discoverGroups(query)
            }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                myGroups = mine,
                discoverGroups = discover
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                myGroups = emptyList(),
                discoverGroups = emptyList(),
                error = e.localizedMessage ?: "Không thể tải danh sách hội nhóm"
            )
        }
    }
}

class CommunityViewModelFactory(
    private val repository: CommunityRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommunityViewModel(repository) as T
        }
        throw IllegalArgumentException("Không tìm thấy ViewModel phù hợp")
    }
}
