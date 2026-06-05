package com.example.carenest.feature.community.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.community.domain.model.SocialGroup
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SocialHubUiState(
    val isLoading: Boolean = true,
    val myGroups: List<SocialGroup> = emptyList(),
    val error: String? = null
)

class SocialHubViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SocialHubUiState())
    val uiState: StateFlow<SocialHubUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Mock delay to simulate network request
                delay(800)
                // Mock data for Social Groups
                val mockGroups = listOf(
                    SocialGroup(
                        id = 1,
                        name = "Cộng đồng Dinh dưỡng",
                        description = "Chia sẻ kiến thức về dinh dưỡng",
                        category = "Dinh dưỡng",
                        memberCount = 12500,
                        newPostsToday = 15,
                        joined = true
                    ),
                    SocialGroup(
                        id = 2,
                        name = "Cộng đồng Nhi khoa",
                        description = "Cộng đồng mẹ bỉm sữa",
                        category = "Nhi Khoa",
                        memberCount = 8400,
                        newPostsToday = 8,
                        joined = true
                    ),
                    SocialGroup(
                        id = 3,
                        name = "Sống khỏe mỗi ngày",
                        description = "Cộng đồng luyện tập và sức khỏe",
                        category = "Sức khỏe",
                        memberCount = 3200,
                        newPostsToday = 0,
                        joined = true
                    )
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    myGroups = mockGroups
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Lỗi tải dữ liệu"
                )
            }
        }
    }
}
