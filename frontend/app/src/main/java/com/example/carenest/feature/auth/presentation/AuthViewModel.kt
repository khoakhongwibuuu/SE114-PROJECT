package com.example.carenest.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.auth.data.remote.AuthApi
import com.example.carenest.feature.auth.domain.model.LoginRequest
import com.example.carenest.feature.auth.domain.model.RegisterRequest
import com.example.carenest.feature.auth.domain.model.ForgotPasswordRequest
import com.example.carenest.feature.auth.domain.model.ResetPasswordRequest
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

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class OtpSent(val error: String? = null, val isLoading: Boolean = false) : ForgotPasswordState()
    object ResetSuccess : ForgotPasswordState()
    data class EmailError(val error: String) : ForgotPasswordState()
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

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    authApi.forgotPassword(ForgotPasswordRequest(email))
                }
                val envelope = response.body()
                if (response.isSuccessful) {
                    _forgotPasswordState.value = ForgotPasswordState.OtpSent()
                } else {
                    _forgotPasswordState.value = ForgotPasswordState.EmailError(envelope?.message ?: "Gửi mã thất bại: ${response.code()}")
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.EmailError(e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }

    fun resetPassword(email: String, otp: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.OtpSent(isLoading = true)
            try {
                val response = withContext(Dispatchers.IO) {
                    authApi.resetPassword(ResetPasswordRequest(email, otp, newPassword, confirmPassword))
                }
                val envelope = response.body()
                if (response.isSuccessful) {
                    _forgotPasswordState.value = ForgotPasswordState.ResetSuccess
                } else {
                    _forgotPasswordState.value = ForgotPasswordState.OtpSent(error = envelope?.message ?: "Đặt lại mật khẩu thất bại: ${response.code()}")
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.OtpSent(error = e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
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
