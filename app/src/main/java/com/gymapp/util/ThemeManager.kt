package com.gymapp.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gymapp.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Reads/writes the theme preference. The whole app re-themes reactively off [themeMode]. */
@Singleton
class ThemeManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val key = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> = dataStore.data.map { ThemeMode.from(it[key]) }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[key] = mode.storageValue }
    }
}
