package com.example.carenest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.data.DataStoreManager
import com.example.carenest.model.LoginRequest
import com.example.carenest.model.RegisterRequest
import com.example.carenest.network.AuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String? = null) : AuthState()
    data class Error(val error: String) : AuthState()
}

class AuthViewModel(
    private val authApi: AuthApi,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authApi.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        dataStoreManager.saveToken(body.token)
                        if (body.familyId != null) {
                            dataStoreManager.saveFamilyId(body.familyId)
                        }
                        _authState.value = AuthState.Success("Đăng nhập thành công")
                    } else {
                        _authState.value = AuthState.Error("Phản hồi không hợp lệ")
                    }
                } else {
                    _authState.value = AuthState.Error("Đăng nhập thất bại: ${response.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }

    fun register(email: String, password: String, fullName: String, phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authApi.register(RegisterRequest(email, password, fullName, phoneNumber))
                if (response.isSuccessful) {
                    _authState.value = AuthState.Success("Đăng ký thành công, vui lòng đăng nhập")
                } else {
                    _authState.value = AuthState.Error("Đăng ký thất bại: ${response.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

class AuthViewModelFactory(
    private val authApi: AuthApi,
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authApi, dataStoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
