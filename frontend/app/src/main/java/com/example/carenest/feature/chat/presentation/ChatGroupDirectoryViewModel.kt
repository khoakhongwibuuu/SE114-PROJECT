package com.example.carenest.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.chat.domain.model.ChatGroup
import com.example.carenest.feature.chat.domain.model.ChatGroupPreview
import com.example.carenest.feature.community.data.repository.CommunityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatGroupDirectoryUiState(
    val isLoading: Boolean = true,
    val search: String = "",
    val myGroups: List<ChatGroup> = emptyList(),
    val discoverGroups: List<ChatGroup> = emptyList(),
    val error: String? = null,
    val hasBlockingError: Boolean = false,
    val joiningGroupId: Long? = null,
    val previewGroup: ChatGroupPreview? = null,
    val isPreviewLoading: Boolean = false
)

@OptIn(FlowPreview::class)
class ChatGroupDirectoryViewModel(
    private val repository: CommunityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatGroupDirectoryUiState())
    val uiState: StateFlow<ChatGroupDirectoryUiState> = _uiState.asStateFlow()
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
            _uiState.value = _uiState.value.copy(
                isPreviewLoading = true,
                error = null,
                hasBlockingError = false,
                previewGroup = null
            )
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
        _uiState.value = _uiState.value.copy(previewGroup = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, hasBlockingError = false)
    }

    fun join(group: ChatGroup, onSuccess: (ChatGroup) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(joiningGroupId = group.id, error = null)
            try {
                val preview = withContext(Dispatchers.IO) {
                    repository.join(group.id)
                }
                val joinedGroup = group.toJoinedGroup(preview)
                _uiState.value = _uiState.value.copy(
                    joiningGroupId = null,
                    myGroups = listOf(joinedGroup) + _uiState.value.myGroups.filterNot { it.id == group.id },
                    discoverGroups = _uiState.value.discoverGroups.map { discoverGroup ->
                        if (discoverGroup.id == group.id) joinedGroup else discoverGroup
                    }
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

    fun joinFromPreview(preview: ChatGroupPreview, onSuccess: (ChatGroup) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(joiningGroupId = preview.id, error = null)
            try {
                val updatedPreview = withContext(Dispatchers.IO) {
                    repository.join(preview.id)
                }
                val joinedGroup = preview.toJoinedGroup(updatedPreview)
                _uiState.value = _uiState.value.copy(
                    joiningGroupId = null,
                    previewGroup = updatedPreview,
                    myGroups = listOf(joinedGroup) + _uiState.value.myGroups.filterNot { it.id == preview.id },
                    discoverGroups = _uiState.value.discoverGroups.map { discoverGroup ->
                        if (discoverGroup.id == preview.id) joinedGroup else discoverGroup
                    }
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
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            isLoading = true,
            error = null,
            hasBlockingError = false
        )
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
                discoverGroups = discover,
                error = null,
                hasBlockingError = false
            )
        } catch (e: Exception) {
            val hadPreviousData = currentState.myGroups.isNotEmpty() || currentState.discoverGroups.isNotEmpty()
            _uiState.value = currentState.copy(
                isLoading = false,
                error = e.localizedMessage ?: "Không thể tải danh sách hội nhóm",
                hasBlockingError = !hadPreviousData
            )
        }
    }
}

class ChatGroupDirectoryViewModelFactory(
    private val repository: CommunityRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatGroupDirectoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatGroupDirectoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Không tìm thấy ViewModel phù hợp")
    }
}

private fun ChatGroup.toJoinedGroup(preview: ChatGroupPreview): ChatGroup {
    return copy(
        name = name.ifBlank { preview.name },
        description = description ?: preview.description,
        category = category ?: preview.category,
        tags = tags ?: preview.tags,
        private = preview.private,
        leadDoctorId = leadDoctorId ?: preview.leadDoctorId,
        leadDoctorName = leadDoctorName ?: preview.leadDoctorName,
        memberCount = preview.memberCount,
        joined = true,
        latestMessage = latestMessage ?: "Nhóm vừa được tạo"
    )
}

private fun ChatGroupPreview.toJoinedGroup(updatedPreview: ChatGroupPreview): ChatGroup {
    return ChatGroup(
        id = id,
        name = name,
        description = description,
        category = category,
        tags = tags,
        private = private,
        leadDoctorId = leadDoctorId,
        leadDoctorName = leadDoctorName,
        memberCount = updatedPreview.memberCount,
        joined = true,
        latestMessage = "Nhóm vừa được tạo",
        latestActivityAt = null
    )
}
