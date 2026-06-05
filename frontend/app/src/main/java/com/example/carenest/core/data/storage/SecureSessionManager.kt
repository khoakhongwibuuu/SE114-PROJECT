package com.example.carenest.core.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SecureSessionManager(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "secure_carenest_session"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val FAMILY_ID_KEY = "x_family_id"
        private const val PROFILE_ID_KEY = "profile_id"
        private const val ACTIVE_PROFILE_ID_KEY = "active_profile_id"
        private const val USER_ID_KEY = "user_id"
        private const val USER_ROLE_KEY = "user_role"
        private const val USER_EMAIL_KEY = "user_email"
        private const val USER_NAME_KEY = "user_name"
        private const val ONBOARDING_DONE_KEY = "@carenest_onboarding_done"
    }

    private val encryptedPrefs = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        android.util.Log.e("SecureSessionManager", "Keystore failed, falling back to regular SharedPreferences", e)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _tokenFlow = MutableStateFlow(encryptedPrefs.getString(ACCESS_TOKEN_KEY, null))
    val tokenFlow: StateFlow<String?> = _tokenFlow

    private val _refreshTokenFlow = MutableStateFlow(encryptedPrefs.getString(REFRESH_TOKEN_KEY, null))
    val refreshTokenFlow: StateFlow<String?> = _refreshTokenFlow

    private val _familyIdFlow = MutableStateFlow(encryptedPrefs.getString(FAMILY_ID_KEY, null))
    val familyIdFlow: StateFlow<String?> = _familyIdFlow

    private val _activeProfileIdFlow = MutableStateFlow(
        encryptedPrefs.getLong(ACTIVE_PROFILE_ID_KEY, -1L).takeIf { it > 0 }
    )
    val activeProfileIdFlow: StateFlow<Long?> = _activeProfileIdFlow

    private val _userRoleFlow = MutableStateFlow(encryptedPrefs.getString(USER_ROLE_KEY, null))
    val userRoleFlow: StateFlow<String?> = _userRoleFlow

    private val _userEmailFlow = MutableStateFlow(encryptedPrefs.getString(USER_EMAIL_KEY, null))
    val userEmailFlow: StateFlow<String?> = _userEmailFlow

    private val _userNameFlow = MutableStateFlow(encryptedPrefs.getString(USER_NAME_KEY, null))
    val userNameFlow: StateFlow<String?> = _userNameFlow

    fun getAccessToken(): String? = encryptedPrefs.getString(ACCESS_TOKEN_KEY, null)

    fun getRefreshToken(): String? = encryptedPrefs.getString(REFRESH_TOKEN_KEY, null)

    fun getFamilyId(): String? = encryptedPrefs.getString(FAMILY_ID_KEY, null)

    fun isOnboardingDone(): Boolean = encryptedPrefs.getString(ONBOARDING_DONE_KEY, null) != null

    fun getProfileId(): Long? = encryptedPrefs.getLong(PROFILE_ID_KEY, -1L).takeIf { it > 0 }

    fun getActiveProfileId(): Long? = encryptedPrefs.getLong(ACTIVE_PROFILE_ID_KEY, -1L).takeIf { it > 0 }

    fun getUserId(): Long? = encryptedPrefs.getLong(USER_ID_KEY, -1L).takeIf { it > 0 }

    fun getUserRole(): String? = encryptedPrefs.getString(USER_ROLE_KEY, null)

    fun getUserEmail(): String? = encryptedPrefs.getString(USER_EMAIL_KEY, null)

    fun getUserName(): String? = encryptedPrefs.getString(USER_NAME_KEY, null)

    suspend fun saveToken(token: String) {
        saveAccessToken(token)
    }

    suspend fun saveAccessToken(token: String) {
        encryptedPrefs.edit().putString(ACCESS_TOKEN_KEY, token).apply()
        _tokenFlow.value = token
    }

    suspend fun saveRefreshToken(token: String) {
        encryptedPrefs.edit().putString(REFRESH_TOKEN_KEY, token).apply()
        _refreshTokenFlow.value = token
    }

    suspend fun saveFamilyId(familyId: String) {
        encryptedPrefs.edit().putString(FAMILY_ID_KEY, familyId).apply()
        _familyIdFlow.value = familyId
    }

    fun saveProfileIdSync(profileId: Long) {
        encryptedPrefs.edit().putLong(PROFILE_ID_KEY, profileId).apply()
    }

    fun saveUserIdSync(userId: Long) {
        encryptedPrefs.edit().putLong(USER_ID_KEY, userId).apply()
    }

    fun saveUserRoleSync(role: String?) {
        if (role.isNullOrBlank()) {
            encryptedPrefs.edit().remove(USER_ROLE_KEY).apply()
            _userRoleFlow.value = null
        } else {
            encryptedPrefs.edit().putString(USER_ROLE_KEY, role).apply()
            _userRoleFlow.value = role
        }
    }

    fun saveUserEmailSync(email: String?) {
        if (email.isNullOrBlank()) {
            encryptedPrefs.edit().remove(USER_EMAIL_KEY).apply()
            _userEmailFlow.value = null
        } else {
            encryptedPrefs.edit().putString(USER_EMAIL_KEY, email).apply()
            _userEmailFlow.value = email
        }
    }

    fun saveUserNameSync(name: String?) {
        if (name.isNullOrBlank()) {
            encryptedPrefs.edit().remove(USER_NAME_KEY).apply()
            _userNameFlow.value = null
        } else {
            encryptedPrefs.edit().putString(USER_NAME_KEY, name).apply()
            _userNameFlow.value = name
        }
    }

    suspend fun saveActiveProfileId(profileId: Long?) {
        if (profileId == null || profileId <= 0) {
            encryptedPrefs.edit().remove(ACTIVE_PROFILE_ID_KEY).apply()
            _activeProfileIdFlow.value = null
        } else {
            encryptedPrefs.edit().putLong(ACTIVE_PROFILE_ID_KEY, profileId).apply()
            _activeProfileIdFlow.value = profileId
        }
    }

    suspend fun saveSession(accessToken: String, refreshToken: String, familyId: String? = null) {
        encryptedPrefs.edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .apply()
        _tokenFlow.value = accessToken
        _refreshTokenFlow.value = refreshToken
        if (!familyId.isNullOrBlank()) {
            saveFamilyId(familyId)
        }
    }

    suspend fun completeOnboarding() {
        encryptedPrefs.edit().putString(ONBOARDING_DONE_KEY, "true").apply()
    }

    suspend fun clearAll() {
        var onboardingDone: String? = null
        try {
            onboardingDone = encryptedPrefs.getString(ONBOARDING_DONE_KEY, null)
        } catch (e: Exception) {
            android.util.Log.e("SecureSessionManager", "Failed to get onboarding state during clearAll", e)
        }

        try {
            encryptedPrefs.edit().clear().apply()
        } catch (e: Exception) {
            android.util.Log.e("SecureSessionManager", "EncryptedSharedPreferences clear failed", e)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        }

        try {
            if (onboardingDone != null) {
                encryptedPrefs.edit().putString(ONBOARDING_DONE_KEY, onboardingDone).apply()
            }
        } catch (e: Exception) {
             // Ignore
        }
        _tokenFlow.value = null
        _refreshTokenFlow.value = null
        _familyIdFlow.value = null
        _activeProfileIdFlow.value = null
        _userRoleFlow.value = null
        _userEmailFlow.value = null
        _userNameFlow.value = null
    }
}
