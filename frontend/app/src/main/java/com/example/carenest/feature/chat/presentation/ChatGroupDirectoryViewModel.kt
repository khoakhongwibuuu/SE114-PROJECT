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
    val searchMine: String = "",
    val searchDiscover: String = "",
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
    private val searchMineFlow = MutableStateFlow("")
    private val searchDiscoverFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            searchMineFlow.debounce(300).collect { loadMyGroups(it) }
        }
        viewModelScope.launch {
            searchDiscoverFlow.debounce(300).collect { loadDiscoverGroups(it) }
        }
    }

    fun onSearchChange(value: String, isMine: Boolean) {
        _uiState.value = if (isMine) {
            _uiState.value.copy(searchMine = value)
        } else {
            _uiState.value.copy(searchDiscover = value)
        }
        if (isMine) {
            searchMineFlow.value = value.trim()
        } else {
            searchDiscoverFlow.value = value.trim()
        }
    }

    fun onTabChanged(isMine: Boolean) {
        val query = if (isMine) _uiState.value.searchMine else _uiState.value.searchDiscover
        if (isMine && _uiState.value.myGroups.isEmpty()) {
            viewModelScope.launch { loadMyGroups(query.trim()) }
        } else if (!isMine && _uiState.value.discoverGroups.isEmpty()) {
            viewModelScope.launch { loadDiscoverGroups(query.trim()) }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadMyGroups(_uiState.value.searchMine.trim())
            loadDiscoverGroups(_uiState.value.searchDiscover.trim())
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
                val joinedGroup = group.copy(
                    joined = true,
                    memberCount = preview.memberCount,
                    latestMessage = group.latestMessage ?: "Nhóm vừa được tạo"
                )
                _uiState.value = _uiState.value.copy(
                    joiningGroupId = null,
                    myGroups = listOf(joinedGroup) + _uiState.value.myGroups.filterNot { it.id == group.id },
                    discoverGroups = _uiState.value.discoverGroups.map {
                        if (it.id == group.id) it.copy(joined = true, memberCount = preview.memberCount) else it
                    }
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

    fun joinFromPreview(preview: ChatGroupPreview, onSuccess: (ChatGroup) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(joiningGroupId = preview.id, error = null)
            try {
                val updatedPreview = withContext(Dispatchers.IO) {
                    repository.join(preview.id)
                }
                val joinedGroup = ChatGroup(
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
                    discoverGroups = _uiState.value.discoverGroups.map {
                        if (it.id == preview.id) it.copy(joined = true, memberCount = updatedPreview.memberCount) else it
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

    private suspend fun loadMyGroups(search: String) {
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
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                myGroups = mine,
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

    private suspend fun loadDiscoverGroups(search: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            isLoading = true,
            error = null,
            hasBlockingError = false
        )
        try {
            val query = search.ifBlank { null }
            val discover = withContext(Dispatchers.IO) {
                repository.discoverGroups(query)
            }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
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
