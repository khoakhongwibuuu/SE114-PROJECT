package com.example.carenest.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.auth.data.remote.AuthApi
import com.example.carenest.feature.auth.domain.model.LoginRequest
import com.example.carenest.feature.auth.domain.model.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String? = null) : AuthState()
    data class Error(val error: String) : AuthState()
}

class AuthViewModel(
    private val authApi: AuthApi,
    private val secureSessionManager: SecureSessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    authApi.login(LoginRequest(email, password))
                }
                val envelope = response.body()
                val auth = envelope?.data
                if (response.isSuccessful && auth != null) {
                    withContext(Dispatchers.IO) {
                        secureSessionManager.saveSession(auth.accessToken, auth.refreshToken)
                    }
                    _authState.value = AuthState.Success(envelope.message ?: "Đăng nhập thành công")
                } else {
                    _authState.value = AuthState.Error(envelope?.message ?: "Đăng nhập thất bại: ${response.code()}")
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
                val response = withContext(Dispatchers.IO) {
                    authApi.register(RegisterRequest(email, password, fullName, phoneNumber))
                }
                val envelope = response.body()
                val auth = envelope?.data
                if (response.isSuccessful) {
                    if (auth != null) {
                        withContext(Dispatchers.IO) {
                            secureSessionManager.saveSession(auth.accessToken, auth.refreshToken)
                        }
                    }
                    _authState.value = AuthState.Success(envelope?.message ?: "Đăng ký thành công")
                } else {
                    _authState.value = AuthState.Error(envelope?.message ?: "Đăng ký thất bại: ${response.code()}")
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
    private val secureSessionManager: SecureSessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authApi, secureSessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
