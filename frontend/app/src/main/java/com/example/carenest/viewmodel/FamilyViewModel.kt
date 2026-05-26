package com.example.carenest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.data.DataStoreManager
import com.example.carenest.data.FamilyRepository
import com.example.carenest.model.FamilyInvitationItem
import com.example.carenest.model.FamilyJoinCodeResponse
import com.example.carenest.model.FamilySummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FamilyUiState(
    val isLoading: Boolean = false,
    val isBusy: Boolean = false,
    val myFamilies: List<FamilySummary> = emptyList(),
    val activeFamilyId: Int? = null,
    val receivedInvitations: List<FamilyInvitationItem> = emptyList(),
    val sentInvitations: List<FamilyInvitationItem> = emptyList(),
    val joinCodeInfo: FamilyJoinCodeResponse? = null,
    val error: String? = null,
    val message: String? = null
)

class FamilyViewModel(private val repository: FamilyRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyUiState())
    val uiState: StateFlow<FamilyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val activeIdStr = repository.getActiveFamilyId()
            _uiState.update { it.copy(activeFamilyId = activeIdStr?.toIntOrNull()) }
        }
        loadFamilies()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun loadFamilies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getMyFamilyList()
            result.onSuccess { families ->
                _uiState.update { it.copy(isLoading = false, myFamilies = families) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectFamily(familyId: Int) {
        viewModelScope.launch {
            repository.saveActiveFamilyId(familyId.toString())
            _uiState.update { it.copy(activeFamilyId = familyId) }
        }
    }

    fun createFamily(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val result = repository.createFamily(name)
            result.onSuccess {
                _uiState.update { it.copy(isBusy = false, message = "Tạo gia đình thành công") }
                loadFamilies()
            }.onFailure { e ->
                _uiState.update { it.copy(isBusy = false, error = e.message) }
            }
        }
    }

    fun loadInvitations() {
        viewModelScope.launch {
            val receivedRes = repository.getReceivedInvitations()
            receivedRes.onSuccess { list ->
                _uiState.update { it.copy(receivedInvitations = list) }
            }
            
            // Should conditionally load sent ones if owner, but we can load both and let UI handle
            val sentRes = repository.getSentInvitations()
            sentRes.onSuccess { list ->
                _uiState.update { it.copy(sentInvitations = list) }
            }
        }
    }

    fun inviteMember(email: String, role: String) {
        val activeFamilyId = _uiState.value.activeFamilyId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val result = repository.inviteMember(activeFamilyId, email, role)
            result.onSuccess {
                _uiState.update { it.copy(isBusy = false, message = "Đã gửi lời mời") }
                // Reload sent invitations
                val sentRes = repository.getSentInvitations()
                sentRes.onSuccess { list ->
                    _uiState.update { it.copy(sentInvitations = list) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isBusy = false, error = e.message) }
            }
        }
    }

    fun handleInvitation(inviteId: Int, accept: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val result = if (accept) {
                repository.acceptInvitation(inviteId)
            } else {
                repository.rejectInvitation(inviteId)
            }
            
            result.onSuccess {
                _uiState.update { it.copy(isBusy = false) }
                loadInvitations()
                if (accept) {
                    loadFamilies()
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isBusy = false, error = e.message) }
            }
        }
    }

    fun joinFamilyByCode(code: String, role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val result = repository.joinFamilyByCode(code, role)
            result.onSuccess {
                _uiState.update { it.copy(isBusy = false, message = "Tham gia thành công") }
                loadFamilies()
            }.onFailure { e ->
                _uiState.update { it.copy(isBusy = false, error = e.message) }
            }
        }
    }

    fun loadJoinCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val result = repository.getFamilyJoinCode()
            result.onSuccess { codeInfo ->
                _uiState.update { it.copy(isBusy = false, joinCodeInfo = codeInfo) }
            }.onFailure {
                // Ignore error if not owner
                _uiState.update { it.copy(isBusy = false, joinCodeInfo = null) }
            }
        }
    }

    fun rotateJoinCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val result = repository.rotateFamilyJoinCode()
            result.onSuccess { codeInfo ->
                _uiState.update { it.copy(isBusy = false, joinCodeInfo = codeInfo) }
            }.onFailure { e ->
                _uiState.update { it.copy(isBusy = false, error = e.message) }
            }
        }
    }
}

class FamilyViewModelFactory(
    private val repository: FamilyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FamilyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FamilyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
