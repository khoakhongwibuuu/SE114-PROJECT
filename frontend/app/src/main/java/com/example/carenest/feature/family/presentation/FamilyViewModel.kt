package com.example.carenest.feature.family.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.family.data.repository.FamilyRepository
import com.example.carenest.feature.family.domain.model.FamilyDetailResponse
import com.example.carenest.feature.family.domain.model.FamilyInvitationItem
import com.example.carenest.feature.family.domain.model.FamilyJoinCodeResponse
import com.example.carenest.feature.family.domain.model.FamilySummary
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FamilyUiState(
    val isLoading: Boolean = false,
    val isBusy: Boolean = false,
    val myFamilies: List<FamilySummary> = emptyList(),
    val activeFamilyId: Long? = null,
    val activeFamily: FamilyDetailResponse? = null,
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
            val id = activeIdStr?.toLongOrNull()
            _uiState.update { it.copy(activeFamilyId = id) }
            id?.let { loadActiveFamilyDetail(it) }
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

    fun selectFamily(familyId: Long) {
        viewModelScope.launch {
            repository.saveActiveFamilyId(familyId.toString())
            _uiState.update { it.copy(activeFamilyId = familyId) }
            loadActiveFamilyDetail(familyId)
        }
    }

    private fun loadActiveFamilyDetail(familyId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getFamilyById(familyId)
            result.onSuccess { detail ->
                _uiState.update { it.copy(isLoading = false, activeFamily = detail) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun createFamily(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.length < 2) {
            _uiState.update { it.copy(error = "Vui lòng nhập tên gia đình tối thiểu 2 ký tự") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val result = repository.createFamily(trimmedName)
            result.onSuccess { family ->
                repository.saveActiveFamilyId(family.id.toString())
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        activeFamilyId = family.id,
                        message = "Tạo gia đình thành công"
                    )
                }
                loadFamilies()
                loadActiveFamilyDetail(family.id)
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

            val sentRes = repository.getSentInvitations()
            sentRes.onSuccess { list ->
                _uiState.update { it.copy(sentInvitations = list) }
            }
        }
    }

    fun inviteMember(email: String, role: String) {
        val activeFamilyId = _uiState.value.activeFamilyId
        if (activeFamilyId == null) {
            _uiState.update { it.copy(error = "Vui lòng chọn gia đình trước khi gửi lời mời") }
            return
        }

        val trimmedEmail = email.trim()
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            _uiState.update { it.copy(error = "Email không đúng định dạng") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val result = repository.inviteMember(activeFamilyId, trimmedEmail, role)
            result.onSuccess {
                _uiState.update { it.copy(isBusy = false, message = "Đã gửi lời mời") }
                repository.getSentInvitations().onSuccess { list ->
                    _uiState.update { it.copy(sentInvitations = list) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isBusy = false, error = e.message) }
            }
        }
    }

    fun handleInvitation(inviteId: Long, accept: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val result = if (accept) {
                repository.acceptInvitation(inviteId)
            } else {
                repository.rejectInvitation(inviteId)
            }

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        message = if (accept) "Đã chấp nhận lời mời" else "Đã từ chối lời mời"
                    )
                }
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
        val normalizedCode = code.trim().uppercase()
        if (normalizedCode.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng nhập mã gia đình") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val result = repository.joinFamilyByCode(normalizedCode, role)
            result.onSuccess { family ->
                repository.saveActiveFamilyId(family.id.toString())
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        activeFamilyId = family.id,
                        activeFamily = family,
                        message = "Tham gia thành công"
                    )
                }
                loadFamilies()
                loadJoinCode()
            }.onFailure { e ->
                _uiState.update { it.copy(isBusy = false, error = e.message) }
            }
        }
    }

    fun joinFamilyByQr(file: File, role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val result = repository.joinFamilyByQr(file, role)
            result.onSuccess { family ->
                repository.saveActiveFamilyId(family.id.toString())
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        activeFamilyId = family.id,
                        activeFamily = family,
                        message = "Tham gia bằng QR thành công"
                    )
                }
                loadFamilies()
                loadJoinCode()
            }.onFailure { e ->
                _uiState.update { it.copy(isBusy = false, error = e.message) }
            }
        }
    }

    fun joinFamilyByQr(context: Context, uri: Uri, role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            var tempFile: File? = null
            try {
                tempFile = withContext(Dispatchers.IO) {
                    copyQrImageToCache(context.applicationContext, uri)
                }
                val result = repository.joinFamilyByQr(tempFile, role)
                result.onSuccess { family ->
                    repository.saveActiveFamilyId(family.id.toString())
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            activeFamilyId = family.id,
                            activeFamily = family,
                            message = "Tham gia bằng QR thành công"
                        )
                    }
                    loadFamilies()
                    loadJoinCode()
                }.onFailure { e ->
                    _uiState.update { it.copy(isBusy = false, error = e.message) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        error = e.message ?: "Không thể đọc ảnh QR"
                    )
                }
            } finally {
                withContext(Dispatchers.IO) {
                    tempFile?.delete()
                }
            }
        }
    }

    fun loadJoinCode() {
        if (_uiState.value.activeFamilyId == null) {
            _uiState.update { it.copy(joinCodeInfo = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val result = repository.getFamilyJoinCode()
            result.onSuccess { codeInfo ->
                _uiState.update { it.copy(isBusy = false, joinCodeInfo = codeInfo) }
            }.onFailure {
                _uiState.update { it.copy(isBusy = false, joinCodeInfo = null) }
            }
        }
    }

    fun rotateJoinCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val result = repository.rotateFamilyJoinCode()
            result.onSuccess { codeInfo ->
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        joinCodeInfo = codeInfo,
                        message = "Đã tạo lại mã gia đình"
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isBusy = false, error = e.message) }
            }
        }
    }
}

private fun copyQrImageToCache(context: Context, uri: Uri): File {
    val tempFile = File.createTempFile("family-join-qr-", ".img", context.cacheDir)
    context.contentResolver.openInputStream(uri)?.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    } ?: throw IllegalStateException("Không thể đọc ảnh QR")
    return tempFile
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
