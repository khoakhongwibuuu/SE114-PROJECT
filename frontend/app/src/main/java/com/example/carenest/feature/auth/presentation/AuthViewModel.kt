package com.example.carenest.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireData
import com.example.carenest.core.data.network.requireSuccess
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.auth.domain.model.AppRole
import com.example.carenest.feature.auth.data.remote.AuthApi
import com.example.carenest.feature.auth.domain.model.ForgotPasswordRequest
import com.example.carenest.feature.auth.domain.model.LoginRequest
import com.example.carenest.feature.auth.domain.model.RegisterRequest
import com.example.carenest.feature.auth.domain.model.ResetPasswordRequest
import com.example.carenest.feature.auth.domain.model.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val message: String? = null) : AuthState()
    data class Error(val error: String) : AuthState()
}

sealed class ForgotPasswordState {
    data object Idle : ForgotPasswordState()
    data object Loading : ForgotPasswordState()
    data class OtpSent(val error: String? = null, val isLoading: Boolean = false) : ForgotPasswordState()
    data object ResetSuccess : ForgotPasswordState()
    data class EmailError(val error: String) : ForgotPasswordState()
}

class AuthViewModel(
    private val authApi: AuthApi,
    private val secureSessionManager: SecureSessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    private val _currentUserRole = MutableStateFlow(secureSessionManager.getUserRole().toAppRole())
    val currentUserRole: StateFlow<AppRole?> = _currentUserRole.asStateFlow()

    init {
        viewModelScope.launch {
            secureSessionManager.userRoleFlow.collect { role ->
                _currentUserRole.value = role.toAppRole()
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    authApi.login(LoginRequest(email, password))
                }
                if (response.isSuccessful) {
                    val envelope = response.body()
                    runCatching {
                        val auth = envelope.requireData(
                            fallback = "Đăng nhập thất bại",
                            missingDataMessage = "Không nhận được dữ liệu đăng nhập"
                        )
                        withContext(Dispatchers.IO) {
                            secureSessionManager.saveSession(auth.accessToken, auth.refreshToken)
                            auth.profileId?.let { secureSessionManager.saveProfileIdSync(it) }
                            auth.isProfileComplete?.let { secureSessionManager.saveIsProfileCompleteSync(it) }
                            auth.user?.let(::persistAuthenticatedUser)
                                ?: pullCurrentUser()?.let(::persistAuthenticatedUser)
                        }
                        _authState.value = AuthState.Success(envelope?.message ?: "Đăng nhập thành công")
                    }.onFailure { error ->
                        _authState.value = AuthState.Error(error.localizedMessage ?: "Đăng nhập thất bại")
                    }
                } else {
                    _authState.value = AuthState.Error(response.errorMessage("Đăng nhập thất bại"))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    authApi.loginWithGoogle(com.example.carenest.feature.auth.domain.model.GoogleLoginRequest(idToken))
                }
                if (response.isSuccessful) {
                    val envelope = response.body()
                    runCatching {
                        val auth = envelope.requireData(
                            fallback = "Đăng nhập bằng Google thất bại",
                            missingDataMessage = "Không nhận được dữ liệu đăng nhập"
                        )
                        withContext(Dispatchers.IO) {
                            secureSessionManager.saveSession(auth.accessToken, auth.refreshToken)
                            auth.profileId?.let { secureSessionManager.saveProfileIdSync(it) }
                            auth.isProfileComplete?.let { secureSessionManager.saveIsProfileCompleteSync(it) }
                            auth.user?.let(::persistAuthenticatedUser)
                                ?: pullCurrentUser()?.let(::persistAuthenticatedUser)
                        }
                        _authState.value = AuthState.Success(envelope?.message ?: "Đăng nhập thành công")
                    }.onFailure { error ->
                        _authState.value = AuthState.Error(error.localizedMessage ?: "Đăng nhập bằng Google thất bại")
                    }
                } else {
                    _authState.value = AuthState.Error(response.errorMessage("Đăng nhập bằng Google thất bại"))
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
                    runCatching { envelope.requireSuccess("Đăng ký thất bại") }
                        .onFailure { error ->
                            _authState.value = AuthState.Error(error.localizedMessage ?: "Đăng ký thất bại")
                            return@launch
                        }
                    if (auth != null) {
                        withContext(Dispatchers.IO) {
                            secureSessionManager.saveSession(auth.accessToken, auth.refreshToken)
                            auth.profileId?.let { secureSessionManager.saveProfileIdSync(it) }
                            auth.isProfileComplete?.let { secureSessionManager.saveIsProfileCompleteSync(it) }
                            auth.user?.let(::persistAuthenticatedUser)
                                ?: pullCurrentUser()?.let(::persistAuthenticatedUser)
                        }
                    }
                    _authState.value = AuthState.Success(envelope?.message ?: "Đăng ký thành công")
                } else {
                    _authState.value = AuthState.Error(response.errorMessage("Đăng ký thất bại"))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    authApi.forgotPassword(ForgotPasswordRequest(email))
                }
                if (response.isSuccessful) {
                    response.requireSuccess("Gửi mã thất bại")
                    _forgotPasswordState.value = ForgotPasswordState.OtpSent()
                } else {
                    _forgotPasswordState.value = ForgotPasswordState.EmailError(
                        response.errorMessage("Gửi mã thất bại")
                    )
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
                if (response.isSuccessful) {
                    response.requireSuccess("Đặt lại mật khẩu thất bại")
                    _forgotPasswordState.value = ForgotPasswordState.ResetSuccess
                } else {
                    _forgotPasswordState.value = ForgotPasswordState.OtpSent(
                        error = response.errorMessage("Đặt lại mật khẩu thất bại")
                    )
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.OtpSent(error = e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { pullCurrentUser() }
            }.getOrNull()?.let { user ->
                _currentUser.value = user
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private suspend fun pullCurrentUser(): UserInfo? {
        val response = authApi.getMe()
        if (!response.isSuccessful) return null
        return runCatching {
            response.requireData("Không thể tải thông tin tài khoản").also(::persistAuthenticatedUser)
        }.getOrNull()
    }

    private fun persistAuthenticatedUser(user: UserInfo) {
        secureSessionManager.saveUserIdSync(user.id)
        secureSessionManager.saveUserRoleSync(user.role)
        secureSessionManager.saveUserEmailSync(user.email)
        secureSessionManager.saveUserNameSync(user.fullName)
        _currentUser.value = user
    }

    private fun String?.toAppRole(): AppRole? {
        val normalized = this?.trim()?.removePrefix("ROLE_")?.uppercase() ?: return null
        return AppRole.entries.firstOrNull { it.name == normalized }
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
