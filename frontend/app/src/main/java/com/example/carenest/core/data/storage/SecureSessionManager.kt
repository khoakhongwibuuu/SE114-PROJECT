package com.example.carenest.core.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SecureSessionManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "secure_carenest_session"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val FAMILY_ID_KEY = "x_family_id"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _tokenFlow = MutableStateFlow(encryptedPrefs.getString(ACCESS_TOKEN_KEY, null))
    val tokenFlow: StateFlow<String?> = _tokenFlow

    private val _refreshTokenFlow = MutableStateFlow(encryptedPrefs.getString(REFRESH_TOKEN_KEY, null))
    val refreshTokenFlow: StateFlow<String?> = _refreshTokenFlow

    private val _familyIdFlow = MutableStateFlow(encryptedPrefs.getString(FAMILY_ID_KEY, null))
    val familyIdFlow: StateFlow<String?> = _familyIdFlow

    fun getAccessToken(): String? = encryptedPrefs.getString(ACCESS_TOKEN_KEY, null)

    fun getRefreshToken(): String? = encryptedPrefs.getString(REFRESH_TOKEN_KEY, null)

    fun getFamilyId(): String? = encryptedPrefs.getString(FAMILY_ID_KEY, null)

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

    suspend fun clearAll() {
        encryptedPrefs.edit().clear().apply()
        _tokenFlow.value = null
        _refreshTokenFlow.value = null
        _familyIdFlow.value = null
    }
}
