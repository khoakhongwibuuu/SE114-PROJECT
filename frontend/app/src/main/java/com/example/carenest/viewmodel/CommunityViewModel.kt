package com.example.carenest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.data.CommunityRepository
import com.example.carenest.model.CommunityGroup
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

data class CommunityUiState(
    val isLoading: Boolean = true,
    val search: String = "",
    val myGroups: List<CommunityGroup> = emptyList(),
    val discoverGroups: List<CommunityGroup> = emptyList(),
    val error: String? = null,
    val joiningGroupId: Long? = null
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

    fun join(group: CommunityGroup) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(joiningGroupId = group.id, error = null)
            try {
                val preview = repository.join(group.id)
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

    private suspend fun loadGroups(search: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            val mine = repository.myGroups(search.ifBlank { null })
            val discover = repository.discoverGroups(search.ifBlank { null })
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
