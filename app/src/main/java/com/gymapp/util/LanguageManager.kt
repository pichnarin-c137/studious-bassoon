package com.gymapp.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gymapp.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads/writes the language preference. [setLanguage] both persists the choice (so the
 * Settings UI reflects it) and applies it via [LocaleHelper], which AppCompat then restores
 * on subsequent launches (see the AppLocalesMetadataHolderService in the manifest).
 */
@Singleton
class LanguageManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val key = stringPreferencesKey("app_language")

    val language: Flow<AppLanguage> = dataStore.data.map { prefs ->
        prefs[key]?.let { AppLanguage.from(it) } ?: LocaleHelper.currentLanguage()
    }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[key] = language.code }
        LocaleHelper.apply(language)
    }
}
