package com.example.carenest.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val FAMILY_ID_KEY = stringPreferencesKey("x_family_id")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val familyIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[FAMILY_ID_KEY]
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveFamilyId(familyId: String) {
        context.dataStore.edit { preferences ->
            preferences[FAMILY_ID_KEY] = familyId
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
