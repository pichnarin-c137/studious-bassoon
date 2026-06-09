package com.gymapp.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the signed-in session in the shared DataStore. The app is gated on [signedIn]:
 * `MainActivity` shows the login screen until a token is stored, and sign-out clears it.
 */
@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val tokenKey = stringPreferencesKey("session_token")
    private val memberIdKey = stringPreferencesKey("session_member_id")

    val signedIn: Flow<Boolean> = dataStore.data.map { !it[tokenKey].isNullOrBlank() }

    suspend fun signIn(token: String, memberId: String) {
        dataStore.edit {
            it[tokenKey] = token
            it[memberIdKey] = memberId
        }
    }

    suspend fun clear() {
        dataStore.edit {
            it.remove(tokenKey)
            it.remove(memberIdKey)
        }
    }
}
