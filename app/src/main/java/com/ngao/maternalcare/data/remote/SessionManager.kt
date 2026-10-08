package com.ngao.maternalcare.data.remote

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ngao_session")

/**
 * Persists the Supabase auth session on-device so the user stays logged in
 * between app launches (mirrors what the web app does with localStorage).
 */
class SessionManager(private val context: Context) {

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val EMAIL = stringPreferencesKey("email")
        val ROLE = stringPreferencesKey("role")
        val FULL_NAME = stringPreferencesKey("full_name")
    }

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { it[Keys.ACCESS_TOKEN] }
    val roleFlow: Flow<String?> = context.dataStore.data.map { it[Keys.ROLE] }

    suspend fun currentAccessToken(): String? = context.dataStore.data.first()[Keys.ACCESS_TOKEN]
    suspend fun currentUserId(): String? = context.dataStore.data.first()[Keys.USER_ID]
    suspend fun currentRole(): String? = context.dataStore.data.first()[Keys.ROLE]
    suspend fun currentFullName(): String? = context.dataStore.data.first()[Keys.FULL_NAME]

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String?,
        userId: String,
        email: String?,
        role: String,
        fullName: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken ?: ""
            prefs[Keys.USER_ID] = userId
            prefs[Keys.EMAIL] = email ?: ""
            prefs[Keys.ROLE] = role
            prefs[Keys.FULL_NAME] = fullName
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
