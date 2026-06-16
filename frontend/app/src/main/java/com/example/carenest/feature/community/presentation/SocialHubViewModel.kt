package com.example.carenest.feature.community.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.community.data.repository.CommunityRepository
import com.example.carenest.feature.community.domain.model.SocialGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SocialHubUiState(
    val isLoading: Boolean = true,
    val myGroups: List<SocialGroup> = emptyList(),
    val error: String? = null
)

class SocialHubViewModel(
    private val repository: CommunityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SocialHubUiState())
    val uiState: StateFlow<SocialHubUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val groups = withContext(Dispatchers.IO) {
                    repository.myGroups(null)
                }.map { chatGroup ->
                    SocialGroup(
                        id = chatGroup.id,
                        name = chatGroup.name,
                        description = chatGroup.description,
                        category = chatGroup.category,
                        avatarUrl = null,
                        memberCount = chatGroup.memberCount,
                        newPostsToday = 0,
                        joined = chatGroup.joined
                    )
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    myGroups = groups,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    myGroups = emptyList(),
                    error = e.localizedMessage ?: "Không thể tải danh sách hội nhóm."
                )
            }
        }
    }
}

class SocialHubViewModelFactory(
    private val repository: CommunityRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialHubViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SocialHubViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
