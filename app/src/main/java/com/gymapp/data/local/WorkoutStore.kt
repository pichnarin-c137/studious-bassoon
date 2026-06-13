package com.gymapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gymapp.data.model.StoredSession
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local persistence for logged sessions — the durable side of [com.gymapp.data.api.FakeGymApi]. An
 * interface (like `GymApi`) so the fake API can be unit-tested with an in-memory double; a real
 * backend makes both this and the fake fall away.
 */
interface WorkoutStore {
    /** All persisted sessions, newest first. */
    suspend fun all(): List<StoredSession>

    /** Prepends [session] so [mostRecent] / "last session" reflect the latest log. */
    suspend fun append(session: StoredSession)

    suspend fun mostRecent(): StoredSession?
}

/** JSON-into-DataStore implementation. Single-user, so a read-modify-write on [append] is fine. */
@Singleton
class DataStoreWorkoutStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : WorkoutStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val key = stringPreferencesKey("logged_sessions")

    override suspend fun all(): List<StoredSession> {
        val raw = dataStore.data.first()[key] ?: return emptyList()
        return runCatching { json.decodeFromString<List<StoredSession>>(raw) }.getOrDefault(emptyList())
    }

    override suspend fun append(session: StoredSession) {
        val updated = listOf(session) + all()
        dataStore.edit { it[key] = json.encodeToString(updated) }
    }

    override suspend fun mostRecent(): StoredSession? = all().firstOrNull()
}
